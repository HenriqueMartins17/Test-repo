use super::description::NodeDescDAO;
use super::permission::NodePermDAO;
use super::rel_repo::NodeRelRepositoryDAO;
use super::share_setting::NodeShareSettingDAO;
use crate::database::datasheet_revision::DatasheetRevisionDAO;
use crate::node::model::NodeSimplePO;
use crate::resource::meta::ResourceMetaDAO;
use crate::types::NodeDetailPO;
use crate::types::*;
use crate::DBManager;
use anyhow::Context;
use async_trait::async_trait;
use databus_core::ot::SourceTypeEnum;
use databus_core::prelude::*;
use databus_core::shared::AuthHeader;
use databus_shared::prelude::*;
use futures::TryStreamExt;
use mysql_async::params;
use mysql_common::params::Params;
use mysql_common::Value;
use serde_json::json;

use std::sync::Arc;

#[async_trait]
pub trait INodeDAO: Send + Sync {
  async fn get_node_detail_info(
    &self,
    node_id: &str,
    auth: &AuthHeader,
    origin: &FetchDataPackOrigin,
  ) -> anyhow::Result<NodeDetailPO>;

  async fn get_simple_info_by_node_ids(&self, node_ids: Vec<String>) -> anyhow::Result<Vec<NodeSimplePO>>;

  async fn get_space_id_by_node_id(&self, node_id: &str) -> anyhow::Result<Option<String>>;

  async fn get_node_role(
    &self,
    node_id: String,
    auth: AuthHeader,
    share_id: Option<String>,
    room_id: Option<String>,
    source_datasheet_id: Option<String>,
    source_type: Option<SourceTypeEnum>,
    allow_all_entrance: Option<bool>,
  ) -> anyhow::Result<NodePermission>;

  async fn get_rel_node_id_by_main_node_id(&self, main_node_id: &str) -> anyhow::Result<Vec<String>>;
}

struct NodeDAO {
  repo: Arc<dyn DBManager>,
  res_meta_dao: Arc<ResourceMetaDAO>,
  dst_rev_dao: Arc<dyn DatasheetRevisionDAO>,
  node_desc_dao: Arc<dyn NodeDescDAO>,
  node_perm_dao: Arc<dyn NodePermDAO>,
  node_share_setting_dao: Arc<dyn NodeShareSettingDAO>,
  node_rel_repo_dao: Arc<dyn NodeRelRepositoryDAO>,
}

pub fn new_dao(
  repo: Arc<dyn DBManager>,
  res_meta_dao: Arc<ResourceMetaDAO>,
  dst_rev_dao: Arc<dyn DatasheetRevisionDAO>,
  node_desc_dao: Arc<dyn NodeDescDAO>,
  node_perm_dao: Arc<dyn NodePermDAO>,
  node_share_setting_dao: Arc<dyn NodeShareSettingDAO>,
  node_rel_repo_dao: Arc<dyn NodeRelRepositoryDAO>,
) -> Arc<dyn INodeDAO> {
  Arc::new(NodeDAO {
    repo,
    res_meta_dao,
    dst_rev_dao,
    node_desc_dao,
    node_perm_dao,
    node_share_setting_dao,
    node_rel_repo_dao
  })
}

#[async_trait]
impl INodeDAO for NodeDAO {
  async fn get_node_detail_info(
    &self,
    node_id: &str,
    auth: &AuthHeader,
    origin: &FetchDataPackOrigin,
  ) -> anyhow::Result<NodeDetailPO> {
    // Node permission view. If no auth is given, it is template access or share access.
    let permission = self
      .node_perm_dao
      .get_node_permission(node_id, auth, origin)
      .await
      .with_context(|| format!("get node permission {node_id}"))?;
    // Node base info
    let node_info = self
      .get_node_info(node_id)
      .await
      .with_context(|| format!("get node info {node_id}"))?;
    // Node description
    let description = self
      .node_desc_dao
      .get_description(node_id)
      .await
      .with_context(|| format!("get description {node_id}"))?;
    // Node revision
    let revision = if origin.not_dst.is_truthy() {
      self.res_meta_dao.get_revision_by_res_id(node_id).await?
    } else {
      self.dst_rev_dao.get_revision_by_dst_id(node_id).await?
    };
    // Obtain node sharing state
    let node_shared = self
      .node_share_setting_dao
      .get_share_status_by_node_id(node_id)
      .await
      .with_context(|| format!("get share status {node_id}"))?;
    // Obtain node permissions
    let node_permit_set = self
      .node_perm_dao
      .get_node_permission_set_status(node_id)
      .await
      .with_context(|| format!("get node permission set status {node_id}"))?;
    Ok(NodeDetailPO {
      node: NodeSO {
        id: node_id.to_owned(),
        name: node_info.as_ref().map_or(String::new(), |info| info.node_name.clone()),
        description: description.unwrap_or_else(|| "{}".to_owned()),
        parent_id: node_info
          .as_ref()
          .and_then(|info| info.parent_id.none_if_empty().cloned())
          .unwrap_or(String::new()),
        icon: node_info
          .as_ref()
          .and_then(|info| info.icon.clone())
          .unwrap_or(String::new()),
        node_shared,
        node_permit_set,
        revision: revision.map_or(0, |rev| rev as u32),
        space_id: node_info.as_ref().map_or(String::new(), |info| info.space_id.clone()),
        role: permission.role,
        node_favorite: Some(permission.node_favorite.is_truthy()),
        extra: Some(format_node_extra(
          node_info.as_ref().and_then(|info| info.extra.clone()),
        )),
        is_ghost_node: permission.is_ghost_node,
        active_view: None,
        permissions: NodePermissionStateSO {
          is_deleted: permission.is_deleted,
          permissions: permission.permissions,
        },
      },
      field_permission_map: permission.field_permission_map,
    })
  }

  async fn get_simple_info_by_node_ids(&self, node_ids: Vec<String>) -> anyhow::Result<Vec<NodeSimplePO>> {
    if node_ids.is_empty() {
      return Ok(Vec::new());
    }
    let mut client = self.repo.get_client().await?;
    let mut query = format!(
      "
      SELECT node_id, node_name, icon \
      FROM `{}node` \
      WHERE is_deleted = 0 \
      AND is_rubbish = 0 \
      AND `node_id`",
      self.repo.table_prefix()
    );
    query = query.append_in_condition(node_ids.len());
    let results: Vec<NodeSimplePO> = client
      .query_all(query, {
        let mut values: Vec<Value> = Vec::new();
        values.extend(node_ids.iter().map(Value::from));
        Params::Positional(values)
      })
      .await?
      .try_collect()
      .await
      .with_context(|| "Get node simple infos")?;
    Ok(results)
  }

  async fn get_space_id_by_node_id(&self, node_id: &str) -> anyhow::Result<Option<String>> {
    let mut client = self.repo.get_client().await?;
    let result = client
      .query_one(
        format!(
          "\
        SELECT `space_id` \
        FROM `{prefix}node` \
        WHERE `node_id` = :node_id AND `is_rubbish` = 0\
      ",
          prefix = self.repo.table_prefix()
        ),
        params! { node_id },
      )
      .await
      .with_context(|| format!("get node info {node_id}"))?
      .map(|space_id| space_id);
    Ok(result)
  }

  async fn get_node_role(
    &self,
    node_id: String,
    auth: AuthHeader, // replace with actual type
    share_id: Option<String>,
    _room_id: Option<String>,
    source_datasheet_id: Option<String>,
    source_type: Option<SourceTypeEnum>,
    allow_all_entrance: Option<bool>,
  ) -> anyhow::Result<NodePermission> {
      match source_type {
          Some(SourceTypeEnum::FORM) => {
              // Datasheet resource OP resulted from form submitting, use permission of form
              // let (user_id, uuid) = self.user_service.get_me_nullable(auth.cookie.unwrap()).await?;
              // ... rest of the code
          }
          Some(SourceTypeEnum::MIRROR) => {
              if node_id != source_datasheet_id.unwrap() {
                  // ... rest of the code
              }
          }
          _ => {}
      }
      let permission = self.node_perm_dao.get_node_role(&node_id, &auth, share_id.clone()).await?;
      // Editable or above permission, no need to do full-scale loading of data source entrance
      if permission.editable.unwrap_or(false)  || !allow_all_entrance.unwrap_or(false) {
          return Ok(permission);
      }
      // Obtain related node resource (form, mirror, etc) of the datasheet
      // let rel_node_ids = self.node_service.get_rel_node_ids(node_id).await?;
      // if rel_node_ids.is_empty() {
      //     return Ok(permission);
      // }
      // // TODO Batch loading node permission sets of multiple mirrors
      // for rel_node_id in rel_node_ids {
      //     if !rel_node_id.starts_with(ResourceIdPrefix::Mirror.as_str()) {
      //         continue;
      //     }
      //     let rel_node_permission = self.permission_services.get_node_role(rel_node_id, auth.clone(), share_id.clone()).await?;
      //     if rel_node_permission.editable {
      //         // Rewrite mirror permission set
      //         self.mirror_service.rewrite_mirror_permission(rel_node_permission.clone());
      //         return Ok(rel_node_permission);
      //     }
      // }
      Ok(permission)
  }

  async fn get_rel_node_id_by_main_node_id(&self, main_node_id: &str) -> anyhow::Result<Vec<String>> {
    self.node_rel_repo_dao.get_rel_node_id_by_main_node_id(main_node_id).await
  }
}

impl NodeDAO {
  async fn get_node_info(&self, node_id: &str) -> anyhow::Result<Option<PartialNodeInfo>> {
    let mut client = self.repo.get_client().await?;
    Ok(
      client
        .query_one(
          format!(
            "\
          SELECT `node_id`, `node_name`, `space_id`, `parent_id`, `icon`, `extra`, `type` \
          FROM `{prefix}node` \
          WHERE `node_id` = :node_id AND `is_rubbish` = 0\
        ",
            prefix = self.repo.table_prefix()
          ),
          params! { node_id },
        )
        .await
        .with_context(|| format!("get node info {node_id}"))?
        .map(
          |(node_id, node_name, space_id, parent_id, icon, extra, ty)| PartialNodeInfo {
            space_id,
            parent_id,
            node_id,
            node_name,
            icon,
            r#type: ty,
            extra,
          },
        ),
    )
  }
}

fn format_node_extra(extra: Option<Json>) -> Json {
  if let Some(Json::Object(mut extra)) = extra {
    if let Some(show_record_history) = extra.get(NodeExtraConstant::ShowRecordHistory.as_str()) {
      extra.insert(
        NodeExtraConstant::ShowRecordHistory.as_str().to_owned(),
        show_record_history.is_truthy().into(),
      );
      return extra.into();
    }
    // Default to show both
    extra.insert(NodeExtraConstant::ShowRecordHistory.as_str().to_owned(), true.into());
    return extra.into();
  }
  json!({
    "showRecordHistory": true
  })
}

#[derive(Debug, Clone)]
pub struct PartialNodeInfo {
  pub space_id: String,
  pub parent_id: String,
  pub node_id: String,
  pub node_name: String,
  pub icon: Option<String>,
  pub r#type: u8,
  pub extra: Option<Json>,
}

#[cfg(test)]
pub mod mock {
  use std::collections::HashMap;
  use super::*;
  use anyhow::anyhow;

  #[derive(Default)]
  pub struct MockNodeDAOImpl {
    node_details: HashMap<(&'static str, FetchDataPackOrigin), NodeDetailPO>,
  }

  impl MockNodeDAOImpl {
    pub fn new() -> Self {
      Self::default()
    }

    pub fn with_node_details(
      mut self,
      node_details: HashMap<(&'static str, FetchDataPackOrigin), NodeDetailPO>,
    ) -> Self {
      self.node_details = node_details;
      self
    }

    pub fn build(self) -> Arc<dyn INodeDAO> {
      Arc::new(self)
    }
  }

  #[async_trait]
  impl INodeDAO for MockNodeDAOImpl {
    async fn get_node_detail_info(
      &self,
      node_id: &str,
      _auth: &AuthHeader,
      origin: &FetchDataPackOrigin,
    ) -> anyhow::Result<NodeDetailPO> {
      self
        .node_details
        .get(&(node_id, origin.clone()))
        .cloned()
        .ok_or_else(|| anyhow!("node detail ({node_id}, {origin:?}) not exist"))
    }

    async fn get_simple_info_by_node_ids(&self, _node_ids: Vec<String>) -> anyhow::Result<Vec<NodeSimplePO>> {
      todo!()
    }

    async fn get_space_id_by_node_id(&self, _node_id: &str) -> anyhow::Result<Option<String>> {
      todo!()
    }

    async fn get_node_role(
      &self,
      _node_id: String,
      _auth: AuthHeader, // replace with actual type
      _share_id: Option<String>,
      _room_id: Option<String>,
      _source_datasheet_id: Option<String>,
      _source_type: Option<SourceTypeEnum>,
      _allow_all_entrance: Option<bool>,
    ) -> anyhow::Result<NodePermission> {
      todo!()
    }

    async fn get_rel_node_id_by_main_node_id(&self, _main_node_id: &str) -> anyhow::Result<Vec<String>> {
      todo!()
    }
  }
}

#[cfg(test)]
mod tests {
  use super::*;
  use crate::mock::{mock_rows, MockRepositoryImpl, MockSqlLog};
  use crate::node::{self, rel_repo};
  use crate::node::permission::mock::MockNodePermDAOImpl;
  use crate::rest::mock::MockRestDAOImpl;
  use crate::types::NodePermission;
  use mysql_async::consts::ColumnType;
  use mysql_async::{Row, Value};
  use pretty_assertions::assert_eq;
  use serde_json::json;
  use tokio_test::assert_ok;

  fn mock_dao<I>(results: I) -> (Arc<dyn DBManager>, Arc<dyn INodeDAO>)
  where
    I: IntoIterator<Item = Vec<Row>>,
  {
    let repo = MockRepositoryImpl::new(results);
    let res_meta_dao = ResourceMetaDAO::new(repo.clone());
    let dst_rev_dao = crate::database::datasheet_revision::new_dao(repo.clone());
    let node_desc_dao = node::description::new_dao(repo.clone());
    let rest_dao = MockRestDAOImpl::new().build();
    let share_setting_dao = node::share_setting::new_dao(repo.clone(), rest_dao);
    let rel_repo_dao = node::rel_repo::new_dao(repo.clone());
    let node_perm_dao = MockNodePermDAOImpl::new()
      .with_permissions(hashmap! {
        ("dst1", FetchDataPackOrigin {
          internal: true,
          main: Some(true),
          ..Default::default()
        }) => NodePermission {
          has_role: true,
          user_id: Some("17".into()),
          uuid: Some("17".into()),
          role: "editor".into(),
          node_favorite: None,
          field_permission_map: None,
          is_ghost_node: Some(false),
          is_deleted: None,
          permissions: Some(json!({
            "readable": true,
            "editable": true,
            "mock": "editor"
          })),
          ..Default::default()
        },
        ("dst2", FetchDataPackOrigin {
          internal: true,
          main: Some(true),
          ..Default::default()
        }) => NodePermission {
          has_role: true,
          user_id: Some("17".into()),
          uuid: Some("17".into()),
          role: "reader".into(),
          node_favorite: Some(true),
          field_permission_map: Some(json!({
            "fld1": {
              "fieldId": "fld1",
              "setting": {
                "formSheetAccessible": false
              },
              "hasRole": true,
              "role": "reader",
              "manageable": false,
              "permission": {
                "readable": true,
                "editable": false
              }
            }
          })),
          is_ghost_node: None,
          is_deleted: Some(false),
          permissions: Some(json!({
            "readable": true,
            "editable": false,
            "mock": "reader"
          })),
          ..Default::default()
        },
      })
      .build();
    (
      repo.clone(),
      new_dao(
        repo.clone(),
        res_meta_dao,
        dst_rev_dao,
        node_desc_dao,
        node_perm_dao,
        share_setting_dao,
        rel_repo_dao
      ),
    )
  }

  fn mock_internal_main_sql_logs(dst_id: &str) -> Vec<MockSqlLog> {
    vec![
      MockSqlLog {
        sql: "SELECT \
            `node_id`, \
            `node_name`, \
            `space_id`, \
            `parent_id`, \
            `icon`, \
            `extra`, \
            `type` \
            FROM `apitable_node` \
            WHERE `node_id` = :node_id AND `is_rubbish` = 0 \
            LIMIT 1"
          .into(),
        params: params! {
          "node_id" => dst_id,
        },
      },
      MockSqlLog {
        sql: "SELECT `description` \
            FROM `apitable_node_desc` \
            WHERE `node_id` = :node_id \
            LIMIT 1"
          .into(),
        params: params! {
          "node_id" => dst_id,
        },
      },
      MockSqlLog {
        sql: "SELECT `revision` \
            FROM `apitable_datasheet` \
            WHERE `dst_id` = :dst_id AND `is_deleted` = 0 \
            LIMIT 1"
          .into(),
        params: params! {
          "dst_id" => dst_id,
        },
      },
      MockSqlLog {
        sql: "SELECT `is_enabled` \
            FROM `apitable_node_share_setting` \
            WHERE `node_id` = :node_id \
            LIMIT 1"
          .into(),
        params: params! {
          "node_id" => dst_id,
        },
      },
    ]
  }

  #[tokio::test]
  async fn internal_main_editor() {
    let (repo, node_dao) = mock_dao([
      mock_rows(
        [
          ("node_id", ColumnType::MYSQL_TYPE_VARCHAR),
          ("node_name", ColumnType::MYSQL_TYPE_VARCHAR),
          ("space_id", ColumnType::MYSQL_TYPE_VARCHAR),
          ("parent_id", ColumnType::MYSQL_TYPE_VARCHAR),
          ("icon", ColumnType::MYSQL_TYPE_VARCHAR),
          ("extra", ColumnType::MYSQL_TYPE_JSON),
          ("type", ColumnType::MYSQL_TYPE_TINY),
        ],
        [[
          "dst1".into(),
          "Dst 1".into(),
          "spc1".into(),
          "fod1j".into(),
          Value::NULL,
          Value::NULL,
          0u8.into(),
        ]],
      ),
      mock_rows([("description", ColumnType::MYSQL_TYPE_VARCHAR)], [["desc 1".into()]]),
      mock_rows([("revision", ColumnType::MYSQL_TYPE_LONG)], [[13u64.into()]]),
      mock_rows([("is_enabled", ColumnType::MYSQL_TYPE_BIT)], [[true.into()]]),
    ]);

    let detail = assert_ok!(
      node_dao
        .get_node_detail_info(
          "dst1",
          &Default::default(),
          &FetchDataPackOrigin {
            internal: true,
            main: Some(true),
            ..Default::default()
          }
        )
        .await
    );

    assert_eq!(
      detail,
      NodeDetailPO {
        node: NodeSO {
          id: "dst1".into(),
          name: "Dst 1".into(),
          description: "desc 1".into(),
          parent_id: "fod1j".into(),
          icon: "".into(),
          node_shared: true,
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
            }))
          },
          revision: 13,
          is_ghost_node: Some(false),
          active_view: None,
          extra: Some(json!({
            "showRecordHistory": true
          }))
        },
        field_permission_map: None,
      }
    );

    assert_eq!(repo.take_logs().await, mock_internal_main_sql_logs("dst1"));
  }

  #[tokio::test]
  async fn internal_main_reader() {
    let (repo, node_dao) = mock_dao([
      mock_rows(
        [
          ("node_id", ColumnType::MYSQL_TYPE_VARCHAR),
          ("node_name", ColumnType::MYSQL_TYPE_VARCHAR),
          ("space_id", ColumnType::MYSQL_TYPE_VARCHAR),
          ("parent_id", ColumnType::MYSQL_TYPE_VARCHAR),
          ("icon", ColumnType::MYSQL_TYPE_VARCHAR),
          ("extra", ColumnType::MYSQL_TYPE_JSON),
          ("type", ColumnType::MYSQL_TYPE_TINY),
        ],
        [[
          "dst2".into(),
          "Dst 2".into(),
          "spc1".into(),
          "fod1j".into(),
          "smiling_face_with_3_hearts".into(),
          json!({ "showRecordHistory": false }).into(),
          0u8.into(),
        ]],
      ),
      mock_rows([("description", ColumnType::MYSQL_TYPE_VARCHAR)], [] as [Vec<Value>; 0]),
      mock_rows([("revision", ColumnType::MYSQL_TYPE_LONG)], [] as [Vec<Value>; 0]),
      mock_rows([("is_enabled", ColumnType::MYSQL_TYPE_BIT)], [[false.into()]]),
    ]);

    let detail = assert_ok!(
      node_dao
        .get_node_detail_info(
          "dst2",
          &Default::default(),
          &FetchDataPackOrigin {
            internal: true,
            main: Some(true),
            ..Default::default()
          }
        )
        .await
    );

    assert_eq!(
      detail,
      NodeDetailPO {
        node: NodeSO {
          id: "dst2".into(),
          name: "Dst 2".into(),
          description: "{}".into(),
          parent_id: "fod1j".into(),
          icon: "smiling_face_with_3_hearts".into(),
          node_shared: false,
          node_permit_set: false,
          node_favorite: Some(true),
          space_id: "spc1".into(),
          role: "reader".into(),
          permissions: NodePermissionStateSO {
            is_deleted: Some(false),
            permissions: Some(json!({
              "readable": true,
              "editable": false,
              "mock": "reader",
            }))
          },
          revision: 0,
          is_ghost_node: None,
          active_view: None,
          extra: Some(json!({
            "showRecordHistory": false
          }))
        },
        field_permission_map: Some(json!({
          "fld1": {
            "fieldId": "fld1",
            "setting": {
              "formSheetAccessible": false
            },
            "hasRole": true,
            "role": "reader",
            "manageable": false,
            "permission": {
              "readable": true,
              "editable": false
            }
          }
        })),
      }
    );

    assert_eq!(repo.take_logs().await, mock_internal_main_sql_logs("dst2"));
  }
}
