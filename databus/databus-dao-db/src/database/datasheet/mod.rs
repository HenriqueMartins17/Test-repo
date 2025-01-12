use std::collections::HashMap;

use self::dependency_analyzer::DependencyAnalysisResult;
use self::foreign_datasheet_loader::ForeignDatasheetLoaderImpl;
use self::foreign_datasheet_loader::InternalDatasheetMeta;
use self::reference_manager::ReferenceManagerImpl;
use super::datasheet_meta::DatasheetMetaDAO;
use super::datasheet_record::DatasheetRecordDAO;
use super::datasheet_revision::DatasheetRevisionDAO;
use crate::node::node::INodeDAO;
use crate::redis::RedisDAO;
use crate::types::NodeDetailPO;
use crate::types::*;
use crate::unit::UnitDAO;
use crate::user::UserDAO;
use crate::DBManager;
use anyhow::Context;
use async_trait::async_trait;
use databus_core::prelude::*;
use databus_core::shared::AuthHeader;
use databus_shared::prelude::*;

use mysql_async::params;
use std::sync::Arc;
use std::time::Instant;
use tokio::sync::Mutex;

mod dependency_analyzer;
pub mod foreign_datasheet_loader;
mod reference_manager;




use crate::po::datasheet_po::DatasheetEntityPO;


#[async_trait]
pub trait DatasheetDAOTrait: Send + Sync {}
impl DatasheetDAOTrait for DatasheetDAO {}

// #[async_trait]
pub struct DatasheetDAO {
  meta_dao: Arc<dyn DatasheetMetaDAO>,
  record_dao: Arc<dyn DatasheetRecordDAO>,
  node_dao: Arc<dyn INodeDAO>,
  revision_dao: Arc<dyn DatasheetRevisionDAO>,
  user_dao: Arc<dyn UserDAO>,
  unit_dao: Arc<dyn UnitDAO>,
  redis_dao: Arc<dyn RedisDAO>,
  repo: Arc<dyn DBManager>,
}

pub fn new_dao(
  meta_dao: Arc<dyn DatasheetMetaDAO>,
  record_dao: Arc<dyn DatasheetRecordDAO>,
  node_dao: Arc<dyn INodeDAO>,
  revision_dao: Arc<dyn DatasheetRevisionDAO>,
  user_dao: Arc<dyn UserDAO>,
  unit_dao: Arc<dyn UnitDAO>,
  redis_dao: Arc<dyn RedisDAO>,
  repo: Arc<dyn DBManager>,
) -> Arc<DatasheetDAO> {
  Arc::new(DatasheetDAO {
    meta_dao,
    record_dao,
    node_dao,
    revision_dao,
    user_dao,
    unit_dao,
    redis_dao,
    repo,
  })
}

// #[async_trait]
impl DatasheetDAO {
  pub async fn fetch_data_pack(
    &self,
    source: &str,
    dst_id: &str,
    auth: AuthHeader,
    origin: FetchDataPackOrigin,
    mut options: Option<FetchDataPackOptions>,
  ) -> anyhow::Result<DatasheetPackSO> {
    let start = Instant::now();
    tracing::info!(
      "Start loading {source} data {dst_id}, origin: {}",
      serde_json::to_string(&origin).unwrap()
    );

    let meta = self
      .meta_dao
      .get_meta_data_by_dst_id(dst_id, false)
      .await
      .with_context(|| format!("get meta data for fetch_data_pack {dst_id}"))?;
    let Some(meta) = meta else {
      return Err(
        NodeNotExistError {
          node_id: dst_id.to_owned(),
        }
        .into(),
      );
    };
    let meta: DatasheetMetaSO = serde_json::from_value(meta)
      .with_context(|| format!("convert meta to DatasheetMeta for fetch_data_pack {dst_id}"))?;
    let meta = meta.into();

    let NodeDetailPO {
      node,
      field_permission_map,
    } = self
      .node_dao
      .get_node_detail_info(dst_id, &auth, &origin)
      .await
      .with_context(|| format!("get node detail info for fetch_data_pack {dst_id}"))?;
    let record_map = Arc::new(Mutex::new(
      self
        .record_dao
        .get_records(
          dst_id,
          options
            .as_mut()
            .and_then(|options| options.record_ids.as_mut().map(std::mem::take)),
          false,
          true,
        )
        .await
        .with_context(|| format!("get record map for fetch_data_pack {dst_id}"))?,
    ));
    let is_template = options.as_ref().and_then(|options| options.is_template).is_truthy();
    let need_extend_main_dst_records = options
      .as_ref()
      .and_then(|options| options.need_extend_main_dst_records)
      .is_truthy();
    let dependency_result = self
      .analyze_dependencies(
        dst_id,
        &meta,
        record_map.clone(),
        options
          .and_then(|options| if is_template { None } else { options.linked_record_map })
          .map(|linked_record_map| {
            linked_record_map
              .into_iter()
              .map(|(dst_id, record_ids)| (dst_id, record_ids.into_iter().collect()))
              .collect()
          }),
        false,
        if is_template { Default::default() } else { auth },
        origin,
        need_extend_main_dst_records,
      )
      .await
      .with_context(|| format!("analyze dependencies for fetch_data_pack {dst_id}"))?;
    let duration = start.elapsed().as_millis();
    tracing::info!("Finished loading {source} data {dst_id}, duration: {duration}ms");
    Ok(DatasheetPackSO {
      snapshot: DatasheetSnapshotSO {
        meta: meta.into(),
        record_map: Arc::try_unwrap(record_map).unwrap().into_inner(),
        datasheet_id: dst_id.to_owned(),
      },
      datasheet: node,
      field_permission_map: if is_template { None } else { field_permission_map },
      foreign_datasheet_map: Some(dependency_result.foreign_datasheet_map),
      units: Some(dependency_result.units),
    })
  }

  pub async fn get_revision_by_dst_id(&self, dst_id: &str) -> anyhow::Result<Option<u64>> {
    self.revision_dao.get_revision_by_dst_id(dst_id).await
  }

  pub async fn update_revision_by_dst_id(&self, dst_id: &str, revision: &u32, updated_by: &str) {
    self.revision_dao.update_revision_by_dst_id(dst_id, revision, updated_by).await
  }

  pub async fn get_space_id_by_dst_id(&self, dst_id: &str) -> anyhow::Result<Option<String>> {
    let mut client = self.repo.get_client().await?;
    Ok(
      client
        .query_one(
          format!(
            "\
              SELECT `space_id` \
              FROM `{prefix}datasheet` \
              WHERE `dst_id` = :dst_id AND `is_deleted` = 0\
            ",
            prefix = self.repo.table_prefix()
          ),
          params! {
            dst_id
          },
        )
        .await?
        .with_context(|| format!("get space id by dst id {dst_id}"))?,
    )
  }

  pub async fn get_datasheet_by_dst_id(&self, dst_id: &str) -> anyhow::Result<Option<DatasheetEntityPO>> {
    let sql = format!(
      "\
      SELECT `id`, `dst_id`, `node_id`, `dst_name`, `space_id`, `creator`, `revision` \
      FROM `{prefix}datasheet` WHERE `dst_id` = :dst_id",
      prefix = self.repo.table_prefix(),
    );
    let params = params! {
      dst_id,
    };

    let mut client = self.repo.get_client().await?;
    let result: Option<DatasheetEntityPO> = client.query_one(sql, params).await?;

    Ok(result)
  }
}

#[derive(Debug, Clone)]
pub(super) struct DependencyAnalysisOutput {
  pub foreign_datasheet_map: HashMap<String, BaseDatasheetPackSO>,
  pub units: Vec<UnitSO>,
}

impl DatasheetDAO {
  /// `main_record_map` may be modified if `need_extend_main_dst_records` is true.
  async fn analyze_dependencies(
    &self,
    main_dst_id: &str,
    main_meta: &InternalDatasheetMeta,
    main_record_map: Arc<Mutex<HashMap<String, RecordSO>>>,
    linked_record_map: Option<HashMap<String, HashSet<String>>>,
    without_permission: bool,
    auth: AuthHeader,
    mut origin: FetchDataPackOrigin,
    need_extend_main_dst_records: bool,
  ) -> anyhow::Result<DependencyAnalysisOutput> {
    let start = Instant::now();
    tracing::info!("Start analyzing dependencies of {main_dst_id}");

    let ref_man = Arc::new(Mutex::new(ReferenceManagerImpl::new(
      self.redis_dao.get_connection().await?,
    )));
    origin.main = Some(false);
    let frn_dst_loader = Arc::new(ForeignDatasheetLoaderImpl::new(
      self.meta_dao.clone(),
      self.node_dao.clone(),
      self.record_dao.clone(),
      self.repo.clone(),
      auth,
      origin,
      without_permission,
    ));
    let analyzer = dependency_analyzer::DependencyAnalyzer::new(
      main_dst_id,
      ref_man,
      frn_dst_loader,
      main_meta,
      main_record_map.clone(),
      need_extend_main_dst_records,
    );

    // Process all fields of the datasheet
    let DependencyAnalysisResult {
      foreign_datasheet_map,
      member_field_unit_ids,
      operator_field_uuids,
    } = analyzer
      .analyze(
        main_dst_id,
        main_meta.field_map.keys().cloned().collect(),
        linked_record_map,
      )
      .await?;

    let mut units: Vec<UnitSO>;
    if !member_field_unit_ids.is_empty() || !operator_field_uuids.is_empty() {
      // Get the space ID which the datasheet belongs to
      let space_id = self.get_space_id_by_dst_id(main_dst_id).await?;
      let Some(space_id) = space_id else {
        return Err(
          NodeNotExistError {
            node_id: main_dst_id.to_owned(),
          }
          .into(),
        );
      };

      // Batch query member info
      units = self
        .unit_dao
        .get_unit_info_by_unit_ids(&space_id, member_field_unit_ids)
        .await?;
      units.extend(
        self
          .user_dao
          .get_user_info_by_uuids(&space_id, operator_field_uuids)
          .await?,
      );
    } else {
      units = vec![];
    }

    let foreign_datasheet_map = foreign_datasheet_map
      .into_iter()
      .map(|(dst_id, dst_pack)| (dst_id, dst_pack.into()))
      .collect::<HashMap<_, BaseDatasheetPackSO>>();

    let duration = start.elapsed().as_millis();
    let mut num_records: HashMap<_, _> = foreign_datasheet_map
      .iter()
      .map(|(id, dst)| (id.as_str(), dst.snapshot.record_map.len()))
      .collect();
    num_records.insert(main_dst_id, main_record_map.lock().await.len());
    tracing::info!(
      "Finished analyzing dependencies of {main_dst_id}, duration {duration}ms. \
      Loaded datasheets and number of records: {num_records:?}"
    );

    Ok(DependencyAnalysisOutput {
      foreign_datasheet_map,
      units,
    })
  }
}

#[cfg(test)]
mod tests {
  use super::*;
  use crate::consts::REF_STORAGE_EXPIRE_TIME;
  use crate::database::datasheet_meta::mock::MockDatasheetMetaDAOImpl;
  use crate::database::datasheet_record::mock::MockDatasheetRecordDAOImpl;
  use crate::database::datasheet_revision;
  use crate::mock::{mock_rows, MockRepositoryImpl, MockSqlLog};
  use crate::node::node::mock::MockNodeDAOImpl;
  use crate::redis;
  use crate::redis::mock::{MockRedis, MockValue};
  use crate::unit::mock::MockUnitDAOImpl;
  use crate::user::mock::MockUserDAOImpl;
  use fred::mocks::MockCommand;
  use fred::prelude::*;
  use mysql_async::consts::ColumnType;
  use mysql_async::Row;
  use mysql_common::value::Value;
  use pretty_assertions::assert_eq;
  use serde_json::json;
  use tokio_test::assert_ok;
  use databus_core::fields::property::FieldPropertySO;

  async fn mock_dao<I>(results: I, mock_redis: Arc<MockRedis>) -> (Arc<dyn DBManager>, Arc<DatasheetDAO>)
  where
    I: IntoIterator<Item = Vec<Row>>,
  {
    let redis_dao = redis::new_dao(RedisConfig {
      mocks: mock_redis,
      ..Default::default()
    })
    .await
    .unwrap();

    let repo = MockRepositoryImpl::new(results);

    let meta_dao = MockDatasheetMetaDAOImpl::new()
      .with_metas(hashmap_standard! {
        "dst1" => serde_json::to_value(mock_dst1_meta()).unwrap(),
        "dst11" => serde_json::to_value(mock_dst11_meta()).unwrap(),
        "dst12" => serde_json::to_value(mock_dst12_meta()).unwrap(),
        "dst13" => serde_json::to_value(mock_dst13_meta()).unwrap(),
      })
      .build();

    let record_dao = MockDatasheetRecordDAOImpl::new()
      .with_records(hashmap_standard! {
        "dst1".into() => mock_dst1_record_map(None),
        "dst11".into() => mock_dst11_record_map(None),
        "dst12".into() => mock_dst12_record_map(None),
        "dst13".into() => mock_dst13_record_map(None),
      })
      .build();

    let node_dao = MockNodeDAOImpl::new()
      .with_node_details(hashmap_standard! {
        ("dst1", FetchDataPackOrigin {
          internal: true,
          main: Some(true),
          ..Default::default()
        }) => mock_dst1_detail_info(),
        ("dst11", FetchDataPackOrigin {
          internal: true,
          main: Some(true),
          ..Default::default()
        }) => mock_dst11_detail_info(),
        ("dst12", FetchDataPackOrigin {
          internal: true,
          main: Some(false),
          ..Default::default()
        }) => mock_dst12_detail_info(),
        ("dst13", FetchDataPackOrigin {
          internal: true,
          main: Some(false),
          ..Default::default()
        }) => mock_dst13_detail_info(),
        ("dst11", FetchDataPackOrigin {
          internal: false,
          main: Some(true),
          share_id: Some("shr1".into()),
          ..Default::default()
        }) => mock_dst11_detail_info(),
        ("dst12", FetchDataPackOrigin {
          internal: false,
          main: Some(false),
          share_id: Some("shr1".into()),
          ..Default::default()
        }) => mock_dst12_detail_info(),
        ("dst13", FetchDataPackOrigin {
          internal: false,
          main: Some(false),
          share_id: Some("shr1".into()),
          ..Default::default()
        }) => mock_dst13_detail_info(),
        ("dst1", FetchDataPackOrigin {
          internal: false,
          main: Some(true),
          ..Default::default()
        }) => mock_dst1_detail_info(),
      })
      .build();

    let revision_dao = datasheet_revision::new_dao(repo.clone());

    let user_dao = MockUserDAOImpl::new().with_users(mock_user_infos()).build();

    let unit_dao = MockUnitDAOImpl::new().with_units(mock_unit_infos()).build();

    (
      repo.clone(),
      new_dao(
        meta_dao,
        record_dao,
        node_dao,
        revision_dao,
        user_dao,
        unit_dao,
        redis_dao,
        repo,
      ),
    )
  }

  fn mock_unit_infos() -> HashMap<&'static str, UnitSO> {
    hashmap_standard! {
      "u1" => UnitSO {
        unit_id: Some("123".to_string()),
        r#type: Some(0),
        name: Some("Unit 1".into()),
        uuid: Some("uuuu1".into()),
        user_id: Some("7197".into()),
        avatar: Some("https://abc.com/abc1.png".into()),
        is_active: Some(1),
        is_deleted: None,
        nick_name: Some("Unit 1 nick".into()),
        avatar_color: Some(1),
        is_member_name_modified: Some(true),
        is_nick_name_modified: None,
        original_unit_id: Some("uu1".into()),
      },
      "u2" => UnitSO {
        unit_id: Some("124".to_string()),
        r#type: Some(0),
        name: Some("Unit 2".into()),
        uuid: Some("uuuu2".into()),
        user_id: Some("7250".into()),
        avatar: Some("https://abc.com/abc2.png".into()),
        is_active: Some(1),
        is_deleted: None,
        nick_name: Some("Unit 2 nick".into()),
        avatar_color: Some(2),
        is_member_name_modified: Some(false),
        is_nick_name_modified: None,
        original_unit_id: Some("uu2".into()),
      },
      "u3" => UnitSO {
        unit_id: Some("125".to_string()),
        r#type: Some(0),
        name: Some("Unit 3".into()),
        uuid: Some("uuuu3".into()),
        user_id: Some("1744".into()),
        avatar: Some("https://abc.com/abc5.png".into()),
        is_active: Some(1),
        is_deleted: Some(0),
        nick_name: Some("Unit 3 nick".into()),
        avatar_color: Some(3),
        is_member_name_modified: Some(false),
        is_nick_name_modified: None,
        original_unit_id: Some("uu3".into()),
      },
    }
  }

  fn mock_user_infos() -> HashMap<&'static str, UnitSO> {
    hashmap_standard! {
      "7197" => UnitSO {
        unit_id: Some("123".to_string()),
        r#type: None,
        name: Some("Unit 1".into()),
        uuid: Some("uuuu1".into()),
        user_id: Some("7197".into()),
        avatar: Some("https://abc.com/abc1.png".into()),
        is_active: Some(1),
        is_deleted: None,
        nick_name: Some("Unit 1 nick".into()),
        avatar_color: Some(1),
        is_member_name_modified: Some(true),
        is_nick_name_modified: Some(true),
        original_unit_id: Some("uuuu11".into()),
      },
      "1120" => UnitSO {
        unit_id: None,
        r#type: Some(0),
        name: Some("User 1120".into()),
        uuid: Some("uuuu57".into()),
        user_id: Some("1120".into()),
        avatar: Some("https://abc.com/791j.png".into()),
        is_active: Some(1),
        is_deleted: Some(0),
        nick_name: Some("nick nick".into()),
        avatar_color: Some(3),
        is_member_name_modified: Some(false),
        is_nick_name_modified: Some(false),
        original_unit_id: Some("uuuu12".into()),
      },
    }
  }

  fn mock_dst1_detail_info() -> NodeDetailPO {
    NodeDetailPO {
      node: NodeSO {
        id: "dst1".into(),
        name: "Dst 1".into(),
        description: "{}".into(),
        parent_id: "fod888".into(),
        icon: "tick_100".into(),
        node_shared: false,
        node_permit_set: false,
        node_favorite: Some(false),
        space_id: "spc1".into(),
        role: "editor".into(),
        permissions: NodePermissionStateSO {
          is_deleted: None,
          permissions: Some(json!({
            "readable": true,
            "editable": true,
            "mock": "editor",
          })),
        },
        revision: 107,
        is_ghost_node: None,
        active_view: None,
        extra: Some(json!({
          "showRecordHistory": true
        })),
      },
      field_permission_map: None,
    }
  }

  fn mock_dst1_meta() -> DatasheetMetaSO {
    DatasheetMetaSO {
      field_map: hashmap_standard! {
        "fld1w1".into() => new_field("fld1w1", FieldKindSO::Text, json!({})),
        "fld1w2".into() => new_field("fld1w2", FieldKindSO::Formula, json!({
          "datasheetId": "dst1",
          "expression": "{fld1w30000000}+{fld1w50000000}",
        })),
        "fld1w30000000".into() => new_field("fld1w30000000", FieldKindSO::Member, json!({
          "isMulti": true,
          "shouldSendMsg": false,
          "unitIds": ["u1", "u2"]
        })),
        "fld1w4".into() => new_field("fld1w4", FieldKindSO::Link, json!({
          "foreignDatasheetId": "dst1"
        })),
        "fld1w50000000".into() => new_field("fld1w50000000", FieldKindSO::CreatedBy, json!({
          "uuids": ["7197"],
          "datasheetId": "dst1"
        })),
      },
      views: vec![serde_json::from_value(json!({
        "columns": [
          { "fieldId": "fld1w1" },
          { "fieldId": "fld1w2" },
          { "fieldId": "fld1w3" },
          { "fieldId": "fld1w4" },
        ]
      }))
      .unwrap()],
      widget_panels: None,
    }
  }

  fn mock_dst1_record_map(record_ids: Option<Vec<&'static str>>) -> HashMap<String, RecordSO> {
    let records = hashmap_standard! {
      "rec1w1".into() => new_record("rec1w1", json!({
        "fld1w4": ["rec1w5"],
      })),
      "rec1w2".into() => new_record("rec1w2", json!({})),
      "rec1w3".into() => new_record("rec1w3", json!({
        "fld1w4": ["rec1w6", "rec1w4"],
      })),
      "rec1w4".into() => new_record("rec1w4", json!({
        "fld1w4": ["rec1w1"]
      })),
      "rec1w5".into() => new_record("rec1w5", json!({})),
      "rec1w6".into() => new_record("rec1w6", json!({})),
    };
    if let Some(record_ids) = record_ids {
      records
        .into_iter()
        .filter(|(id, _)| record_ids.contains_ref(id))
        .collect()
    } else {
      records
    }
  }

  fn mock_dst11_meta() -> DatasheetMetaSO {
    DatasheetMetaSO {
      field_map: hashmap_standard! {
        "fld11w1".into() => new_field("fld11w1", FieldKindSO::Text, json!({})),
        "fld11w2".into() => new_field("fld11w2", FieldKindSO::Link, json!({
          "foreignDatasheetId": "dst12",
          "brotherFieldId": "fld12w2",
        })),
        "fld11w3".into() => new_field("fld11w3", FieldKindSO::LookUp, json!({
          "datasheetId": "dst11",
          "relatedLinkFieldId": "fld11w2",
          "lookUpTargetFieldId": "fld12w3",
          "openFilter": true,
          "filterInfo": {
            "conjunction": "and",
            "conditions": [{ "fieldId": "fld12w5" }]
          }
        })),
        "fld11w4".into() => new_field("fld11w4", FieldKindSO::Link, json!({
          "foreignDatasheetId": "dst13",
          "brotherFieldId": "fld13w4",
        })),
        "fld11w5".into() => new_field("fld11w5", FieldKindSO::LookUp, json!({
          "datasheetId": "dst11",
          "relatedLinkFieldId": "fld11w4",
          "lookUpTargetFieldId": "fld13w2",
        })),
      },
      views: vec![serde_json::from_value(json!({
        "columns": [
          { "fieldId": "fld11w1" },
          { "fieldId": "fld11w2" },
          { "fieldId": "fld11w3" },
          { "fieldId": "fld11w4" },
          { "fieldId": "fld11w5" },
        ]
      }))
      .unwrap()],
      widget_panels: None,
    }
  }

  fn mock_dst11_detail_info() -> NodeDetailPO {
    NodeDetailPO {
      node: NodeSO {
        id: "dst11".into(),
        name: "Dst 11".into(),
        description: "desc 11".into(),
        parent_id: "fod888".into(),
        icon: "cross".into(),
        node_shared: false,
        node_permit_set: false,
        node_favorite: Some(false),
        space_id: "spc1".into(),
        role: "reader".into(),
        permissions: NodePermissionStateSO {
          is_deleted: None,
          permissions: Some(json!({
            "readable": true,
            "editable": false,
            "mock": "reader",
          })),
        },
        revision: 7,
        is_ghost_node: Some(false),
        active_view: None,
        extra: Some(json!({
          "showRecordHistory": false
        })),
      },
      field_permission_map: None,
    }
  }

  fn mock_dst11_record_map(record_ids: Option<Vec<&'static str>>) -> HashMap<String, RecordSO> {
    let records = hashmap_standard! {
      "rec11w1".into() => new_record("rec11w1", json!({
        "fld11w2": ["rec12w2"]
      })),
      "rec11w2".into() => new_record("rec11w2", json!({
        "fld11w2": ["rec12w1", "rec12w3"],
        "fld11w4": ["rec13w1"],
      })),
      "rec11w3".into() => new_record("rec11w3", json!({})),
      "rec11w10".into() => new_record("rec11w10", json!({
        "fld11w2": ["rec12w10", "rec12w11"]
      })),
      "rec11w11".into() => new_record("rec11w11", json!({
        "fld11w2": ["rec12w12"],
        "fld11w4": ["rec13w10"],
      })),
      "rec11w12".into() => new_record("rec11w12", json!({})),
    };
    if let Some(record_ids) = record_ids {
      records
        .into_iter()
        .filter(|(id, _)| record_ids.contains_ref(id))
        .collect()
    } else {
      records
    }
  }

  fn mock_dst12_meta() -> DatasheetMetaSO {
    DatasheetMetaSO {
      field_map: hashmap_standard! {
        "fld12w1".into() => new_field("fld12w1", FieldKindSO::Text, json!({
        })),
        "fld12w2".into() => new_field("fld12w2", FieldKindSO::Link, json!({
          "foreignDatasheetId": "dst11",
          "brotherFieldId": "fld11w2",
        })),
        "fld12w3".into() => new_field("fld12w3", FieldKindSO::Formula, json!({
          "datasheetId": "dst12",
          "expression": "+{fld12w4000000}",
        })),
        "fld12w4000000".into() => new_field("fld12w4000000", FieldKindSO::Link, json!({
          "foreignDatasheetId": "dst12",
        })),
        "fld12w5".into() => new_field(
          "fld12w5",
          FieldKindSO::Member,
          json!({
            "isMulti": true,
            "shouldSendMsg": false,
            "unitIds": ["u2", "u3"]
          }
        )),
        "fld12w6".into() => new_field(
          "fld12w6",
          FieldKindSO::Member,
          json!({
            "isMulti": true,
            "shouldSendMsg": false,
            "unitIds": ["u1", "u3"],
          }
        )),
        "fld12w7".into() => new_field("fld12w7", FieldKindSO::Link, json!({
          "foreignDatasheetId": "dst13",
          "brotherFieldId": "fld13w3",
        })),
      },
      views: vec![serde_json::from_value(json!({
        "columns": [
          { "fieldId": "fld12w1" },
          { "fieldId": "fld12w2" },
          { "fieldId": "fld12w3" },
          { "fieldId": "fld12w4000000" },
          { "fieldId": "fld12w5" },
          { "fieldId": "fld12w6" },
          { "fieldId": "fld12w7" },
        ]
      }))
      .unwrap()],
      widget_panels: None,
    }
  }

  fn mock_dst12_detail_info() -> NodeDetailPO {
    NodeDetailPO {
      node: NodeSO {
        id: "dst12".into(),
        name: "Dst 12".into(),
        description: "{}".into(),
        parent_id: "fod714".into(),
        icon: "cross".into(),
        node_shared: true,
        node_permit_set: false,
        node_favorite: Some(true),
        space_id: "spc1".into(),
        role: "manager".into(),
        permissions: NodePermissionStateSO {
          is_deleted: None,
          permissions: Some(json!({
            "readable": true,
            "editable": true,
            "mock": "manager",
          })),
        },
        revision: 14179,
        is_ghost_node: Some(false),
        active_view: None,
        extra: Some(json!({
          "showRecordHistory": true
        })),
      },
      field_permission_map: None,
    }
  }

  fn mock_dst12_record_map(record_ids: Option<Vec<&'static str>>) -> HashMap<String, RecordSO> {
    let records = hashmap_standard! {
      "rec12w1".into() => new_record("rec12w1", json!({
        "fld12w2": ["rec11w2"],
        "fld12w4000000": ["rec12w5"],
      })),
      "rec12w2".into() => new_record("rec12w2", json!({
        "fld12w2": ["rec11w1"],
        "fld12w7": ["rec13w1"]
      })),
      "rec12w3".into() => new_record("rec12w3", json!({
        "fld12w2": ["rec11w2"],
        "fld12w4000000": ["rec12w5", "rec12w4"]
      })),
      "rec12w4".into() => new_record("rec12w4", json!({
        "fld12w4000000": ["rec12w6"],
      })),
      "rec12w5".into() => new_record("rec12w5", json!({
        "fld12w4000000": ["rec12w2"],
      })),
      "rec12w6".into() => new_record("rec12w6", json!({
        "fld12w4000000": ["rec12w8"],
      })),
      "rec12w7".into() => new_record("rec12w7", json!({
        "fld12w4000000": ["rec12w1"]
      })),
      "rec12w8".into() => new_record("rec12w7", json!({})),
      "rec12w10".into() => new_record("rec12w10", json!({
        "fld12w2": ["rec11w10"],
        "fld12w4000000": ["rec12w12"]
      })),
      "rec12w11".into() => new_record("rec12w11", json!({
        "fld12w2": ["rec11w10"],
        "fld12w7": ["rec13w10"]
      })),
      "rec12w12".into() => new_record("rec12w12", json!({
        "fld12w2": ["rec11w11"],
        "fld12w4000000": ["rec12w11"]
      })),
    };
    if let Some(record_ids) = record_ids {
      records
        .into_iter()
        .filter(|(id, _)| record_ids.contains_ref(id))
        .collect()
    } else {
      records
    }
  }

  fn mock_dst13_meta() -> DatasheetMetaSO {
    DatasheetMetaSO {
      field_map: hashmap_standard! {
        "fld13w1".into() => new_field("fld13w1", FieldKindSO::Text, json!({})),
        "fld13w2".into() => new_field("fld13w2", FieldKindSO::LookUp, json!({
          "datasheetId": "dst13",
          "relatedLinkFieldId": "fld13w3",
          "lookUpTargetFieldId": "fld12w4000000",
          "openFilter": false,
          "filterInfo": {
            "conjunction": "and",
            "conditions": [{ "fieldId": "fld12w6" }]
          }
        })),
        "fld13w3".into() => new_field("fld13w3", FieldKindSO::Link, json!({
          "foreignDatasheetId": "dst12",
          "brotherFieldId": "fld12w7",
        })),
        "fld13w4".into() => new_field("fld13w4", FieldKindSO::Link, json!({
          "foreignDatasheetId": "dst11",
          "brotherFieldId": "fld11w4",
        })),
      },
      views: vec![serde_json::from_value(json!({
        "columns": [
          { "fieldId": "fld13w1" },
          { "fieldId": "fld13w2" },
          { "fieldId": "fld13w3" },
          { "fieldId": "fld13w4" },
        ]
      }))
      .unwrap()],
      widget_panels: None,
    }
  }

  fn mock_dst13_detail_info() -> NodeDetailPO {
    NodeDetailPO {
      node: NodeSO {
        id: "dst13".into(),
        name: "Dst 13".into(),
        description: "{}".into(),
        parent_id: "fod7777".into(),
        icon: "jaugt".into(),
        node_shared: false,
        node_permit_set: true,
        node_favorite: Some(true),
        space_id: "spc1".into(),
        role: "manager".into(),
        permissions: NodePermissionStateSO {
          is_deleted: None,
          permissions: Some(json!({
            "readable": true,
            "editable": true,
            "mock": "manager",
          })),
        },
        revision: 2873,
        is_ghost_node: None,
        active_view: None,
        extra: Some(json!({
          "showRecordHistory": true
        })),
      },
      field_permission_map: None,
    }
  }

  fn mock_dst13_record_map(record_ids: Option<Vec<&'static str>>) -> HashMap<String, RecordSO> {
    let records = hashmap_standard! {
      "rec13w1".into() => new_record("rec13w1", json!({
        "fld13w3": ["rec12w2"],
        "fld13w4": ["rec11w2"],
      })),
      "rec13w2".into() => new_record("rec13w2", json!({})),
      "rec13w3".into() => new_record("rec13w3", json!({})),
      "rec13w10".into() => new_record("rec13w10", json!({
        "fld13w3": ["rec12w11"],
        "fld13w4": ["rec11w11"],
      })),
    };
    if let Some(record_ids) = record_ids {
      records
        .into_iter()
        .filter(|(id, _)| record_ids.contains_ref(id))
        .collect()
    } else {
      records
    }
  }

  fn new_field(id: &str, kind: FieldKindSO, property: Json) -> FieldSO {
    FieldSO {
      id: id.into(),
      name: id.to_uppercase(),
      desc: None,
      required: None,
      kind,
      property: Some(serde_json::from_value::<FieldPropertySO>(property).unwrap()),
    }
  }

  fn new_record(id: &str, data: Json) -> RecordSO {
    RecordSO {
      id: id.into(),
      comment_count: 0,
      data,
      created_at: Some(19999999i64),
      updated_at: None,
      revision_history: None,
      record_meta: None,
      ..Default::default()
    }
  }

  fn mock_cmd(cmd: &str, args: Vec<RedisValue>) -> MockCommand {
    MockCommand {
      cmd: cmd.into(),
      subcommand: None,
      args,
    }
  }

  const MOCK_SPACE_ID_QUERY_SQL: &str = "\
    SELECT `space_id` \
    FROM `apitable_datasheet` \
    WHERE `dst_id` = :dst_id AND `is_deleted` = 0 \
    LIMIT 1";

  #[tokio::test]
  async fn single_datasheet_self_linking() {
    let mock_redis = Arc::new(MockRedis::new());
    let (repo, datasheet_dao) = mock_dao(
      [
        mock_rows([("space_id", ColumnType::MYSQL_TYPE_VARCHAR)], [["spc1".into()]]),
        mock_rows([], [] as [Vec<Value>; 0]),
        mock_rows([], [] as [Vec<Value>; 0]),
        mock_rows([("is_enabled", ColumnType::MYSQL_TYPE_VARCHAR)], [[false.into()]]),
      ],
      mock_redis.clone(),
    )
    .await;

    let data_pack = assert_ok!(
      datasheet_dao
        .fetch_data_pack(
          "main datasheet",
          "dst1",
          Default::default(),
          FetchDataPackOrigin {
            internal: true,
            main: Some(true),
            ..Default::default()
          },
          Default::default()
        )
        .await
    );

    assert_eq!(
      data_pack,
      DatasheetPackSO {
        snapshot: DatasheetSnapshotSO {
          meta: mock_dst1_meta(),
          record_map: mock_dst1_record_map(None),
          datasheet_id: "dst1".into()
        },
        datasheet: mock_dst1_detail_info().node,
        field_permission_map: None,
        foreign_datasheet_map: Some(hashmap_standard! {}),
        units: Some(vec![
          mock_unit_infos()["u1"].clone(),
          mock_unit_infos()["u2"].clone(),
          mock_user_infos()["7197"].clone(),
        ])
      },
    );

    assert_eq!(
      repo.take_logs().await,
      [MockSqlLog {
        sql: MOCK_SPACE_ID_QUERY_SQL.into(),
        params: params! {
          "dst_id" => "dst1"
        }
      }]
    );

    assert_eq!(
      mock_redis.take_logs(),
      vec![
        mock_cmd(
          "SISMEMBER",
          vec![
            RedisValue::Bytes("vikadata:nest:fieldReRef:dst1:fld1w30000000".into()),
            RedisValue::String("dst1:fld1w2".into()),
          ]
        ),
        mock_cmd(
          "SADD",
          vec![
            RedisValue::Bytes("vikadata:nest:fieldReRef:dst1:fld1w30000000".into()),
            RedisValue::String("dst1:fld1w2".into()),
          ]
        ),
        mock_cmd(
          "EXPIRE",
          vec![
            RedisValue::Bytes("vikadata:nest:fieldReRef:dst1:fld1w30000000".into()),
            RedisValue::Integer(*REF_STORAGE_EXPIRE_TIME),
          ]
        ),
        mock_cmd(
          "SISMEMBER",
          vec![
            RedisValue::Bytes("vikadata:nest:fieldReRef:dst1:fld1w50000000".into()),
            RedisValue::String("dst1:fld1w2".into()),
          ]
        ),
        mock_cmd(
          "SADD",
          vec![
            RedisValue::Bytes("vikadata:nest:fieldReRef:dst1:fld1w50000000".into()),
            RedisValue::String("dst1:fld1w2".into()),
          ]
        ),
        mock_cmd(
          "EXPIRE",
          vec![
            RedisValue::Bytes("vikadata:nest:fieldReRef:dst1:fld1w50000000".into()),
            RedisValue::Integer(*REF_STORAGE_EXPIRE_TIME),
          ]
        ),
        mock_cmd(
          "SMEMBERS",
          vec![RedisValue::Bytes("vikadata:nest:fieldRef:dst1:fld1w2".into()),]
        ),
        mock_cmd(
          "SADD",
          vec![
            RedisValue::Bytes("vikadata:nest:fieldRef:dst1:fld1w2".into()),
            RedisValue::String("dst1:fld1w30000000".into()),
            RedisValue::String("dst1:fld1w50000000".into()),
          ]
        ),
        mock_cmd(
          "EXPIRE",
          vec![
            RedisValue::Bytes("vikadata:nest:fieldRef:dst1:fld1w2".into()),
            RedisValue::Integer(*REF_STORAGE_EXPIRE_TIME),
          ]
        ),
      ]
    );

    assert_eq!(
      mock_redis.take_store(),
      hashmap_standard! {
        "vikadata:nest:fieldRef:dst1:fld1w2".into() =>
          MockValue::Set(hashset!["dst1:fld1w30000000".into(), "dst1:fld1w50000000".into()]),
        "vikadata:nest:fieldReRef:dst1:fld1w30000000".into() => MockValue::Set(hashset!["dst1:fld1w2".into()]),
        "vikadata:nest:fieldReRef:dst1:fld1w50000000".into() => MockValue::Set(hashset!["dst1:fld1w2".into()]),
      }
    );
  }

  #[tokio::test]
  async fn single_datasheet_self_linking_override_old_references() {
    let mock_redis = Arc::new(MockRedis::new().with_store(hashmap_standard! {
      "vikadata:nest:fieldRef:dst1:fld1w2".into() =>
        MockValue::Set(hashset!["dst1:fld1w20000000".into(), "dst1:fld1w30000000".into()]),
      "vikadata:nest:fieldReRef:dst1:fld1w30000000".into() =>
        MockValue::Set(hashset!["dst1:fld1w2".into()]),
      "vikadata:nest:fieldReRef:dst1:fld1w20000000".into() =>
        MockValue::Set(hashset!["dst1:fld1w2".into()]),
    }));
    let (repo, datasheet_dao) = mock_dao(
      [
        mock_rows([("space_id", ColumnType::MYSQL_TYPE_VARCHAR)], [["spc1".into()]]),
        mock_rows([], [] as [Vec<Value>; 0]),
        mock_rows([], [] as [Vec<Value>; 0]),
        mock_rows([("is_enabled", ColumnType::MYSQL_TYPE_VARCHAR)], [[false.into()]]),
      ],
      mock_redis.clone(),
    )
    .await;

    let data_pack = assert_ok!(
      datasheet_dao
        .fetch_data_pack(
          "main datasheet",
          "dst1",
          Default::default(),
          FetchDataPackOrigin {
            internal: true,
            main: Some(true),
            ..Default::default()
          },
          Default::default()
        )
        .await
    );

    assert_eq!(
      data_pack,
      DatasheetPackSO {
        snapshot: DatasheetSnapshotSO {
          meta: mock_dst1_meta(),
          record_map: mock_dst1_record_map(None),
          datasheet_id: "dst1".into()
        },
        datasheet: mock_dst1_detail_info().node,
        field_permission_map: None,
        foreign_datasheet_map: Some(hashmap_standard! {}),
        units: Some(vec![
          mock_unit_infos()["u1"].clone(),
          mock_unit_infos()["u2"].clone(),
          mock_user_infos()["7197"].clone(),
        ])
      },
    );

    assert_eq!(
      repo.take_logs().await,
      [MockSqlLog {
        sql: MOCK_SPACE_ID_QUERY_SQL.into(),
        params: params! {
          "dst_id" => "dst1"
        },
      }],
    );

    assert_eq!(
      mock_redis.take_logs(),
      vec![
        mock_cmd(
          "SISMEMBER",
          vec![
            RedisValue::Bytes("vikadata:nest:fieldReRef:dst1:fld1w30000000".into()),
            "dst1:fld1w2".into(),
          ]
        ),
        mock_cmd(
          "SISMEMBER",
          vec![
            RedisValue::Bytes("vikadata:nest:fieldReRef:dst1:fld1w50000000".into()),
            "dst1:fld1w2".into(),
          ]
        ),
        mock_cmd(
          "SADD",
          vec![
            RedisValue::Bytes("vikadata:nest:fieldReRef:dst1:fld1w50000000".into()),
            "dst1:fld1w2".into(),
          ]
        ),
        mock_cmd(
          "EXPIRE",
          vec![
            RedisValue::Bytes("vikadata:nest:fieldReRef:dst1:fld1w50000000".into()),
            (*REF_STORAGE_EXPIRE_TIME).into(),
          ]
        ),
        mock_cmd(
          "SMEMBERS",
          vec![RedisValue::Bytes("vikadata:nest:fieldRef:dst1:fld1w2".into()),]
        ),
        mock_cmd(
          "DEL",
          vec![RedisValue::Bytes("vikadata:nest:fieldRef:dst1:fld1w2".into()),]
        ),
        mock_cmd(
          "SISMEMBER",
          vec![
            RedisValue::Bytes("vikadata:nest:fieldReRef:dst1:fld1w20000000".into()),
            "dst1:fld1w2".into(),
          ]
        ),
        mock_cmd(
          "SCARD",
          vec![RedisValue::Bytes("vikadata:nest:fieldReRef:dst1:fld1w20000000".into()),]
        ),
        mock_cmd(
          "DEL",
          vec![RedisValue::Bytes("vikadata:nest:fieldReRef:dst1:fld1w20000000".into()),]
        ),
        mock_cmd(
          "SADD",
          vec![
            RedisValue::Bytes("vikadata:nest:fieldRef:dst1:fld1w2".into()),
            "dst1:fld1w30000000".into(),
            "dst1:fld1w50000000".into(),
          ]
        ),
        mock_cmd(
          "EXPIRE",
          vec![
            RedisValue::Bytes("vikadata:nest:fieldRef:dst1:fld1w2".into()),
            (*REF_STORAGE_EXPIRE_TIME).into(),
          ]
        ),
      ]
    );

    assert_eq!(
      mock_redis.take_store(),
      hashmap_standard! {
        "vikadata:nest:fieldRef:dst1:fld1w2".into() => MockValue::Set(hashset!["dst1:fld1w30000000".into(), "dst1:fld1w50000000".into()]),
        "vikadata:nest:fieldReRef:dst1:fld1w30000000".into() => MockValue::Set(hashset!["dst1:fld1w2".into()]),
        "vikadata:nest:fieldReRef:dst1:fld1w50000000".into() => MockValue::Set(hashset!["dst1:fld1w2".into()]),
      }
    );
  }

  #[tokio::test]
  async fn single_datasheet_self_linking_partial_records() {
    let mock_redis = Arc::new(MockRedis::new());
    let (repo, datasheet_dao) = mock_dao(
      [
        mock_rows([("space_id", ColumnType::MYSQL_TYPE_VARCHAR)], [["spc1".into()]]),
        mock_rows([], [] as [Vec<Value>; 0]),
        mock_rows([], [] as [Vec<Value>; 0]),
        mock_rows([("is_enabled", ColumnType::MYSQL_TYPE_VARCHAR)], [[false.into()]]),
      ],
      mock_redis.clone(),
    )
    .await;

    let data_pack = assert_ok!(
      datasheet_dao
        .fetch_data_pack(
          "main datasheet",
          "dst1",
          Default::default(),
          FetchDataPackOrigin {
            internal: true,
            main: Some(true),
            ..Default::default()
          },
          Some(FetchDataPackOptions {
            record_ids: Some(vec!["rec1w1".into(), "rec1w2".into(), "rec1w3".into()]),
            ..Default::default()
          })
        )
        .await
    );

    assert_eq!(
      data_pack,
      DatasheetPackSO {
        snapshot: DatasheetSnapshotSO {
          meta: mock_dst1_meta(),
          record_map: mock_dst1_record_map(Some(vec!["rec1w1", "rec1w2", "rec1w3"])),
          datasheet_id: "dst1".into()
        },
        datasheet: mock_dst1_detail_info().node,
        field_permission_map: None,
        foreign_datasheet_map: Some(hashmap_standard! {}),
        units: Some(vec![
          mock_unit_infos()["u1"].clone(),
          mock_unit_infos()["u2"].clone(),
          mock_user_infos()["7197"].clone(),
        ])
      },
    );

    assert_eq!(
      repo.take_logs().await,
      [MockSqlLog {
        sql: MOCK_SPACE_ID_QUERY_SQL.into(),
        params: params! {
          "dst_id" => "dst1"
        },
      }]
    );

    assert_eq!(
      mock_redis.take_store(),
      hashmap_standard! {
        "vikadata:nest:fieldRef:dst1:fld1w2".into() => MockValue::Set(hashset!["dst1:fld1w30000000".into(), "dst1:fld1w50000000".into()]),
        "vikadata:nest:fieldReRef:dst1:fld1w30000000".into() => MockValue::Set(hashset!["dst1:fld1w2".into()]),
        "vikadata:nest:fieldReRef:dst1:fld1w50000000".into() => MockValue::Set(hashset!["dst1:fld1w2".into()]),
      }
    );
  }

  #[tokio::test]
  async fn reprocess_dirty_fields() {
    let mock_redis = Arc::new(MockRedis::new());
    let (repo, datasheet_dao) = mock_dao(
      [
        mock_rows([("space_id", ColumnType::MYSQL_TYPE_VARCHAR)], [["spc1".into()]]),
        mock_rows([], [] as [Vec<Value>; 0]),
        mock_rows([], [] as [Vec<Value>; 0]),
        mock_rows([("is_enabled", ColumnType::MYSQL_TYPE_VARCHAR)], [[false.into()]]),
      ],
      mock_redis.clone(),
    )
    .await;

    let data_pack = assert_ok!(
      datasheet_dao
        .fetch_data_pack(
          "main datasheet",
          "dst11",
          Default::default(),
          FetchDataPackOrigin {
            internal: true,
            main: Some(true),
            ..Default::default()
          },
          Some(FetchDataPackOptions {
            record_ids: Some(vec!["rec11w1".into(), "rec11w2".into(), "rec11w3".into()]),
            ..Default::default()
          }),
        )
        .await
    );

    assert_eq!(
      data_pack,
      DatasheetPackSO {
        snapshot: DatasheetSnapshotSO {
          meta: mock_dst11_meta(),
          record_map: mock_dst11_record_map(Some(vec!["rec11w1", "rec11w2", "rec11w3"])),
          datasheet_id: "dst11".into()
        },
        datasheet: mock_dst11_detail_info().node,
        field_permission_map: None,
        foreign_datasheet_map: Some(hashmap_standard! {
          "dst12".into() => BaseDatasheetPackSO {
            snapshot: DatasheetSnapshotSO {
              meta: mock_dst12_meta(),
              record_map: mock_dst12_record_map(
                Some(vec!["rec12w1", "rec12w2", "rec12w3", "rec12w4", "rec12w5", "rec12w6"])),
              datasheet_id: "dst12".into(),
            },
            datasheet: serde_json::to_value(mock_dst12_detail_info().node).unwrap(),
            field_permission_map: None,
          },
          "dst13".into() => BaseDatasheetPackSO {
            snapshot: DatasheetSnapshotSO {
              meta: mock_dst13_meta(),
              record_map: mock_dst13_record_map(Some(vec!["rec13w1"])),
              datasheet_id: "dst13".into(),
            },
            datasheet: serde_json::to_value(mock_dst13_detail_info().node).unwrap(),
            field_permission_map: None,
          },
        }),
        units: Some(vec![mock_unit_infos()["u3"].clone(), mock_unit_infos()["u2"].clone(),])
      },
    );

    assert_eq!(
      repo.take_logs().await,
      [MockSqlLog {
        sql: MOCK_SPACE_ID_QUERY_SQL.into(),
        params: params! {
          "dst_id" => "dst11",
        }
      }]
    );

    let mut redis_store = mock_redis.take_store().into_iter().collect::<Vec<_>>();
    redis_store.sort_by(|(key1, _), (key2, _)| key1.cmp(key2));
    assert_eq!(
      redis_store,
      vec![
        (
          "vikadata:nest:fieldReRef:dst12:fld12w1".into(),
          MockValue::Set(hashset![
            "dst11:fld11w2".into(),
            "dst12:fld12w4000000".into(),
            "dst13:fld13w3".into(),
          ])
        ),
        (
          "vikadata:nest:fieldReRef:dst12:fld12w3".into(),
          MockValue::Set(hashset!["dst11:fld11w3".into()])
        ),
        (
          "vikadata:nest:fieldReRef:dst12:fld12w4000000".into(),
          MockValue::Set(hashset!["dst12:fld12w3".into(), "dst13:fld13w2".into()])
        ),
        (
          "vikadata:nest:fieldReRef:dst12:fld12w5".into(),
          MockValue::Set(hashset!["dst11:fld11w3".into()])
        ),
        (
          "vikadata:nest:fieldReRef:dst13:fld13w1".into(),
          MockValue::Set(hashset!["dst11:fld11w4".into()])
        ),
        (
          "vikadata:nest:fieldReRef:dst13:fld13w2".into(),
          MockValue::Set(hashset!["dst11:fld11w5".into()])
        ),
        (
          "vikadata:nest:fieldRef:dst11:fld11w2".into(),
          MockValue::Set(hashset!["dst12:fld12w1".into()])
        ),
        (
          "vikadata:nest:fieldRef:dst11:fld11w3".into(),
          MockValue::Set(hashset!["dst12:fld12w3".into(), "dst12:fld12w5".into()])
        ),
        (
          "vikadata:nest:fieldRef:dst11:fld11w4".into(),
          MockValue::Set(hashset!["dst13:fld13w1".into()])
        ),
        (
          "vikadata:nest:fieldRef:dst11:fld11w5".into(),
          MockValue::Set(hashset!["dst13:fld13w2".into()])
        ),
        (
          "vikadata:nest:fieldRef:dst12:fld12w3".into(),
          MockValue::Set(hashset!["dst12:fld12w4000000".into()])
        ),
        (
          "vikadata:nest:fieldRef:dst12:fld12w4000000".into(),
          MockValue::Set(hashset!["dst12:fld12w1".into()])
        ),
        (
          "vikadata:nest:fieldRef:dst13:fld13w2".into(),
          MockValue::Set(hashset!["dst12:fld12w4000000".into()])
        ),
        (
          "vikadata:nest:fieldRef:dst13:fld13w3".into(),
          MockValue::Set(hashset!["dst12:fld12w1".into()])
        ),
      ]
    );
  }

  #[tokio::test]
  async fn reprocess_nondirty_fields() {
    let mock_redis = Arc::new(MockRedis::new());
    let (repo, datasheet_dao) = mock_dao(
      [
        mock_rows([("space_id", ColumnType::MYSQL_TYPE_VARCHAR)], [["spc1".into()]]),
        mock_rows([], [] as [Vec<Value>; 0]),
        mock_rows([], [] as [Vec<Value>; 0]),
        mock_rows([("is_enabled", ColumnType::MYSQL_TYPE_VARCHAR)], [[false.into()]]),
      ],
      mock_redis.clone(),
    )
    .await;

    let data_pack = assert_ok!(
      datasheet_dao
        .fetch_data_pack(
          "main datasheet",
          "dst11",
          Default::default(),
          FetchDataPackOrigin {
            internal: true,
            main: Some(true),
            ..Default::default()
          },
          Some(FetchDataPackOptions {
            record_ids: Some(vec!["rec11w10".into(), "rec11w11".into(), "rec11w12".into()]),
            ..Default::default()
          }),
        )
        .await
    );

    assert_eq!(
      data_pack,
      DatasheetPackSO {
        snapshot: DatasheetSnapshotSO {
          meta: mock_dst11_meta(),
          record_map: mock_dst11_record_map(Some(vec!["rec11w10", "rec11w11", "rec11w12"])),
          datasheet_id: "dst11".into()
        },
        datasheet: mock_dst11_detail_info().node,
        field_permission_map: None,
        foreign_datasheet_map: Some(hashmap_standard! {
          "dst12".into() => BaseDatasheetPackSO {
            snapshot: DatasheetSnapshotSO {
              meta: mock_dst12_meta(),
              record_map: mock_dst12_record_map(Some(vec!["rec12w10", "rec12w11", "rec12w12"])),
              datasheet_id: "dst12".into(),
            },
            datasheet: serde_json::to_value(mock_dst12_detail_info().node).unwrap(),
            field_permission_map: None,
          },
          "dst13".into() => BaseDatasheetPackSO {
            snapshot: DatasheetSnapshotSO {
              meta: mock_dst13_meta(),
              record_map: mock_dst13_record_map(Some(vec!["rec13w10"])),
              datasheet_id: "dst13".into(),
            },
            datasheet: serde_json::to_value(mock_dst13_detail_info().node).unwrap(),
            field_permission_map: None,
          },
        }),
        units: Some(vec![mock_unit_infos()["u3"].clone(), mock_unit_infos()["u2"].clone(),])
      },
    );

    assert_eq!(
      repo.take_logs().await,
      [MockSqlLog {
        sql: MOCK_SPACE_ID_QUERY_SQL.into(),
        params: params! {
          "dst_id" => "dst11"
        }
      }]
    );

    let mut redis_store = mock_redis.take_store().into_iter().collect::<Vec<_>>();
    redis_store.sort_by(|(key1, _), (key2, _)| key1.cmp(key2));
    assert_eq!(
      redis_store,
      vec![
        (
          "vikadata:nest:fieldReRef:dst12:fld12w1".into(),
          MockValue::Set(hashset![
            "dst11:fld11w2".into(),
            "dst12:fld12w4000000".into(),
            "dst13:fld13w3".into(),
          ])
        ),
        (
          "vikadata:nest:fieldReRef:dst12:fld12w3".into(),
          MockValue::Set(hashset!["dst11:fld11w3".into()])
        ),
        (
          "vikadata:nest:fieldReRef:dst12:fld12w4000000".into(),
          MockValue::Set(hashset!["dst12:fld12w3".into(), "dst13:fld13w2".into()])
        ),
        (
          "vikadata:nest:fieldReRef:dst12:fld12w5".into(),
          MockValue::Set(hashset!["dst11:fld11w3".into()])
        ),
        (
          "vikadata:nest:fieldReRef:dst13:fld13w1".into(),
          MockValue::Set(hashset!["dst11:fld11w4".into()])
        ),
        (
          "vikadata:nest:fieldReRef:dst13:fld13w2".into(),
          MockValue::Set(hashset!["dst11:fld11w5".into()])
        ),
        (
          "vikadata:nest:fieldRef:dst11:fld11w2".into(),
          MockValue::Set(hashset!["dst12:fld12w1".into()])
        ),
        (
          "vikadata:nest:fieldRef:dst11:fld11w3".into(),
          MockValue::Set(hashset!["dst12:fld12w3".into(), "dst12:fld12w5".into()])
        ),
        (
          "vikadata:nest:fieldRef:dst11:fld11w4".into(),
          MockValue::Set(hashset!["dst13:fld13w1".into()])
        ),
        (
          "vikadata:nest:fieldRef:dst11:fld11w5".into(),
          MockValue::Set(hashset!["dst13:fld13w2".into()])
        ),
        (
          "vikadata:nest:fieldRef:dst12:fld12w3".into(),
          MockValue::Set(hashset!["dst12:fld12w4000000".into()])
        ),
        (
          "vikadata:nest:fieldRef:dst12:fld12w4000000".into(),
          MockValue::Set(hashset!["dst12:fld12w1".into()])
        ),
        (
          "vikadata:nest:fieldRef:dst13:fld13w2".into(),
          MockValue::Set(hashset!["dst12:fld12w4000000".into()])
        ),
        (
          "vikadata:nest:fieldRef:dst13:fld13w3".into(),
          MockValue::Set(hashset!["dst12:fld12w1".into()])
        ),
      ]
    );
  }

  #[tokio::test]
  async fn share_linked_datasheets() {
    let mock_redis = Arc::new(MockRedis::new());
    let (repo, datasheet_dao) = mock_dao(
      [
        mock_rows([("space_id", ColumnType::MYSQL_TYPE_VARCHAR)], [["spc1".into()]]),
        mock_rows([], [] as [Vec<Value>; 0]),
        mock_rows([], [] as [Vec<Value>; 0]),
        mock_rows([("is_enabled", ColumnType::MYSQL_TYPE_VARCHAR)], [[false.into()]]),
      ],
      mock_redis.clone(),
    )
    .await;

    let data_pack = assert_ok!(
      datasheet_dao
        .fetch_data_pack(
          "shared datasheet",
          "dst11",
          Default::default(),
          FetchDataPackOrigin {
            internal: false,
            main: Some(true),
            share_id: Some("shr1".into()),
            ..Default::default()
          },
          Some(FetchDataPackOptions {
            record_ids: Some(vec!["rec11w10".into(), "rec11w11".into(), "rec11w12".into()]),
            ..Default::default()
          }),
        )
        .await
    );

    assert_eq!(
      data_pack,
      DatasheetPackSO {
        snapshot: DatasheetSnapshotSO {
          meta: mock_dst11_meta(),
          record_map: mock_dst11_record_map(Some(vec!["rec11w10", "rec11w11", "rec11w12"])),
          datasheet_id: "dst11".into()
        },
        datasheet: mock_dst11_detail_info().node,
        field_permission_map: None,
        foreign_datasheet_map: Some(hashmap_standard! {
          "dst12".into() => BaseDatasheetPackSO {
            snapshot: DatasheetSnapshotSO {
              meta: mock_dst12_meta(),
              record_map: mock_dst12_record_map(Some(vec!["rec12w10", "rec12w11", "rec12w12"])),
              datasheet_id: "dst12".into(),
            },
            datasheet: serde_json::to_value(mock_dst12_detail_info().node).unwrap(),
            field_permission_map: None,
          },
          "dst13".into() => BaseDatasheetPackSO {
            snapshot: DatasheetSnapshotSO {
              meta: mock_dst13_meta(),
              record_map: mock_dst13_record_map(Some(vec!["rec13w10"])),
              datasheet_id: "dst13".into(),
            },
            datasheet: serde_json::to_value(mock_dst13_detail_info().node).unwrap(),
            field_permission_map: None,
          },
        }),
        units: Some(vec![mock_unit_infos()["u3"].clone(), mock_unit_infos()["u2"].clone(),])
      },
    );

    assert_eq!(
      repo.take_logs().await,
      [MockSqlLog {
        sql: MOCK_SPACE_ID_QUERY_SQL.into(),
        params: params! {
          "dst_id" => "dst11"
        }
      }]
    );
  }

  #[tokio::test]
  async fn template_self_linking() {
    let mock_redis = Arc::new(MockRedis::new());
    let (repo, datasheet_dao) = mock_dao(
      [
        mock_rows([("space_id", ColumnType::MYSQL_TYPE_VARCHAR)], [["spc1".into()]]),
        mock_rows([], [] as [Vec<Value>; 0]),
        mock_rows([], [] as [Vec<Value>; 0]),
        mock_rows([("is_enabled", ColumnType::MYSQL_TYPE_VARCHAR)], [[false.into()]]),
      ],
      mock_redis.clone(),
    )
    .await;

    let data_pack = assert_ok!(
      datasheet_dao
        .fetch_data_pack(
          "main datasheet",
          "dst1",
          Default::default(),
          FetchDataPackOrigin {
            internal: false,
            main: Some(true),
            ..Default::default()
          },
          Default::default()
        )
        .await
    );

    assert_eq!(
      data_pack,
      DatasheetPackSO {
        snapshot: DatasheetSnapshotSO {
          meta: mock_dst1_meta(),
          record_map: mock_dst1_record_map(None),
          datasheet_id: "dst1".into()
        },
        datasheet: mock_dst1_detail_info().node,
        field_permission_map: None,
        foreign_datasheet_map: Some(hashmap_standard! {}),
        units: Some(vec![
          mock_unit_infos()["u1"].clone(),
          mock_unit_infos()["u2"].clone(),
          mock_user_infos()["7197"].clone(),
        ])
      },
    );

    assert_eq!(
      repo.take_logs().await,
      [MockSqlLog {
        sql: MOCK_SPACE_ID_QUERY_SQL.into(),
        params: params! {
          "dst_id" => "dst1"
        }
      }]
    );

    assert_eq!(
      mock_redis.take_store(),
      hashmap_standard! {
        "vikadata:nest:fieldRef:dst1:fld1w2".into() =>
          MockValue::Set(hashset!["dst1:fld1w30000000".into(), "dst1:fld1w50000000".into()]),
        "vikadata:nest:fieldReRef:dst1:fld1w30000000".into() => MockValue::Set(hashset!["dst1:fld1w2".into()]),
        "vikadata:nest:fieldReRef:dst1:fld1w50000000".into() => MockValue::Set(hashset!["dst1:fld1w2".into()]),
      }
    );
  }

  #[tokio::test]
  async fn specify_linked_record_map() {
    let mock_redis = Arc::new(MockRedis::new());
    let (repo, datasheet_dao) = mock_dao(
      [
        mock_rows([("space_id", ColumnType::MYSQL_TYPE_VARCHAR)], [["spc1".into()]]),
        mock_rows([], [] as [Vec<Value>; 0]),
        mock_rows([], [] as [Vec<Value>; 0]),
        mock_rows([("is_enabled", ColumnType::MYSQL_TYPE_VARCHAR)], [[false.into()]]),
      ],
      mock_redis.clone(),
    )
    .await;

    let data_pack = assert_ok!(
      datasheet_dao
        .fetch_data_pack(
          "main datasheet",
          "dst11",
          Default::default(),
          FetchDataPackOrigin {
            internal: true,
            main: Some(true),
            ..Default::default()
          },
          Some(FetchDataPackOptions {
            record_ids: Some(vec!["rec11w10".into(), "rec11w11".into(), "rec11w12".into()]),
            linked_record_map: Some(hashmap_standard! {
              "dst12".into() => vec!["rec12w11".into()],
            }),
            ..Default::default()
          }),
        )
        .await
    );

    assert_eq!(
      data_pack,
      DatasheetPackSO {
        snapshot: DatasheetSnapshotSO {
          meta: mock_dst11_meta(),
          record_map: mock_dst11_record_map(Some(vec!["rec11w10", "rec11w11", "rec11w12"])),
          datasheet_id: "dst11".into()
        },
        datasheet: mock_dst11_detail_info().node,
        field_permission_map: None,
        foreign_datasheet_map: Some(hashmap_standard! {
          "dst12".into() => BaseDatasheetPackSO {
            snapshot: DatasheetSnapshotSO {
              meta: mock_dst12_meta(),
              record_map: mock_dst12_record_map(Some(vec!["rec12w11"])),
              datasheet_id: "dst12".into(),
            },
            datasheet: serde_json::to_value(mock_dst12_detail_info().node).unwrap(),
            field_permission_map: None,
          },
          "dst13".into() => BaseDatasheetPackSO {
            snapshot: DatasheetSnapshotSO {
              meta: mock_dst13_meta(),
              record_map: mock_dst13_record_map(Some(vec![])),
              datasheet_id: "dst13".into(),
            },
            datasheet: serde_json::to_value(mock_dst13_detail_info().node).unwrap(),
            field_permission_map: None,
          },
        }),
        units: Some(vec![mock_unit_infos()["u3"].clone(), mock_unit_infos()["u2"].clone(),])
      },
    );

    assert_eq!(
      repo.take_logs().await,
      [MockSqlLog {
        sql: MOCK_SPACE_ID_QUERY_SQL.into(),
        params: params! {
          "dst_id" => "dst11"
        }
      }]
    );
  }
}
