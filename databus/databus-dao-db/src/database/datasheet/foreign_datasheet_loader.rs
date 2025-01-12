use std::collections::HashMap;
use crate::database::datasheet_meta::DatasheetMetaDAO;
use crate::database::datasheet_record::DatasheetRecordDAO;
use crate::node::node::INodeDAO;
use crate::types::*;
use crate::DBManager;
use databus_core::prelude::{BaseDatasheetPackSO, DatasheetSnapshotSO, FieldSO, RecordSO};
use databus_core::prelude::{DatasheetMetaSO, WidgetPanelSO};
// use crate::database::NodeDAO;
use anyhow::{anyhow, Context};
use async_trait::async_trait;
use databus_core::prelude::ViewSO;
use databus_core::shared::AuthHeader;
use databus_shared::prelude::{HashSet, Json};
use mysql_async::params;
use serde::{Deserialize, Serialize};
use std::sync::Arc;
use tokio::sync::Mutex;

/// Only used for dependency analysis
// #[derive(Debug, Clone)]
#[derive(Deserialize, Serialize, Debug, Clone, PartialEq)]
#[serde(rename_all = "camelCase")]
pub struct InternalDatasheetMeta {
  pub field_map: Arc<HashMap<String, FieldSO>>,
  pub views: Vec<ViewSO>,
  pub widget_panels: Option<Vec<WidgetPanelSO>>,
}

/// Only used for dependency analysis. Since tokio `Mutex` is not serde-able, a new type is required
/// to be able to modify `record_map`.
#[derive(Debug, Clone)]
pub struct InternalDatasheetSnapshot {
  pub meta: InternalDatasheetMeta,
  pub record_map: Arc<Mutex<HashMap<String, RecordSO>>>,
  pub datasheet_id: String,
}

/// Only used for dependency analysis
#[derive(Debug, Clone)]
pub struct InternalBaseDatasheetPack {
  pub snapshot: InternalDatasheetSnapshot,
  pub datasheet: Json,
  pub field_permission_map: Option<Json>,
}

#[cfg(test)]
#[derive(Debug, Clone, PartialEq, Eq)]
pub enum ForeignDatasheetLoadLog {
  LoadDatasheet {
    dst_id: String,
  },
  FetchRecords {
    dst_id: String,
    record_ids: HashSet<String>,
  },
}

#[async_trait]
pub trait ForeignDatasheetLoader: Send + Sync {
  /// Loads a foreign datasheet pack without recordMap.
  ///
  /// If the datasheet does not exist or is not unaccessible, `None` is returned.
  async fn load_foreign_datasheet(&self, dst_id: &str) -> anyhow::Result<Option<InternalBaseDatasheetPack>>;

  async fn fetch_record_map(&self, dst_id: &str, record_ids: HashSet<String>) -> anyhow::Result<HashMap<String, RecordSO>>;
}

pub(super) struct ForeignDatasheetLoaderImpl {
  meta_dao: Arc<dyn DatasheetMetaDAO>,
  node_dao: Arc<dyn INodeDAO>,
  record_dao: Arc<dyn DatasheetRecordDAO>,
  repo: Arc<dyn DBManager>,
  auth: AuthHeader,
  origin: FetchDataPackOrigin,
  without_permission: bool,
}

impl ForeignDatasheetLoaderImpl {
  pub(super) fn new(
    meta_dao: Arc<dyn DatasheetMetaDAO>,
    node_dao: Arc<dyn INodeDAO>,
    record_dao: Arc<dyn DatasheetRecordDAO>,
    repo: Arc<dyn DBManager>,
    auth: AuthHeader,
    origin: FetchDataPackOrigin,
    without_permission: bool,
  ) -> Self {
    Self {
      meta_dao,
      node_dao: node_dao,
      record_dao: record_dao,
      repo,
      auth,
      origin,
      without_permission,
    }
  }
}

#[async_trait]
impl ForeignDatasheetLoader for ForeignDatasheetLoaderImpl {
  async fn load_foreign_datasheet(&self, dst_id: &str) -> anyhow::Result<Option<InternalBaseDatasheetPack>> {
    let meta: DatasheetMetaSO = match self.meta_dao.get_meta_data_by_dst_id(dst_id, false).await {
      Ok(Some(meta)) => serde_json::from_value(meta)
        .map_err(|err| anyhow!("load foreign datasheet {dst_id}: parse meta data error: {err}"))?,
      Ok(None) => return Ok(None),
      Err(err) => {
        tracing::error!("load foreign datasheet {dst_id}: get meta data error: {err}");
        return Ok(None);
      }
    };

    if self.without_permission {
      match self.get_base_info_by_dst_id(dst_id).await {
        Ok(Some(node)) => {
          return Ok(Some(InternalBaseDatasheetPack {
            snapshot: InternalDatasheetSnapshot {
              meta: meta.into(),
              record_map: Default::default(),
              datasheet_id: dst_id.to_owned(),
            },
            datasheet: node,
            field_permission_map: None,
          }))
        }
        Ok(None) => return Ok(None),
        Err(err) => {
          tracing::error!("load foreign datasheet {dst_id}: get base info error: {err}");
          return Ok(None);
        }
      }
    }

    let info = match self
      .node_dao
      .get_node_detail_info(dst_id, &self.auth, &self.origin)
      .await
    {
      Ok(info) => info,
      Err(err) => {
        tracing::error!("load foreign datasheet {}: get node detail info error: {}", dst_id, err);
        return Ok(None);
      }
    };

    let node = serde_json::to_value(&info.node)
      .map_err(|err| anyhow!("load foreign datasheet {}: node info to error: {}", dst_id, err))?;

    Ok(Some(InternalBaseDatasheetPack {
      snapshot: InternalDatasheetSnapshot {
        meta: meta.into(),
        record_map: Default::default(),
        datasheet_id: dst_id.to_owned(),
      },
      datasheet: node,
      field_permission_map: info.field_permission_map,
    }))
  }

  async fn fetch_record_map(&self, dst_id: &str, record_ids: HashSet<String>) -> anyhow::Result<HashMap<String, RecordSO>> {
    Ok(
      self
        .record_dao
        .get_records(dst_id, Some(record_ids.into_iter().collect()), false, true)
        .await
        .with_context(|| format!("fetch record map of {dst_id}"))?,
    )
  }
}

impl ForeignDatasheetLoaderImpl {
  async fn get_base_info_by_dst_id(&self, dst_id: &str) -> anyhow::Result<Option<Json>> {
    let mut client = self.repo.get_client().await?;

    Ok(
      client
        .query_one(
          format!(
            "\
              SELECT `dst_id`, `dst_name`, `revision` \
              FROM `{prefix}datasheet` \
              WHERE `dst_id` = :dst_id and `is_deleted` = 0\
            ",
            prefix = self.repo.table_prefix()
          ),
          params! {
            dst_id,
          },
        )
        .await
        .with_context(|| format!("get base info of dst {dst_id}"))?,
    )
  }
}

impl Into<BaseDatasheetPackSO> for InternalBaseDatasheetPack {
  fn into(self) -> BaseDatasheetPackSO {
    BaseDatasheetPackSO {
      snapshot: self.snapshot.into(),
      datasheet: self.datasheet,
      field_permission_map: self.field_permission_map,
    }
  }
}

impl Into<DatasheetSnapshotSO> for InternalDatasheetSnapshot {
  fn into(self) -> DatasheetSnapshotSO {
    DatasheetSnapshotSO {
      meta: self.meta.into(),
      record_map: Arc::try_unwrap(self.record_map).unwrap().into_inner(),
      datasheet_id: self.datasheet_id,
    }
  }
}

impl From<DatasheetSnapshotSO> for InternalDatasheetSnapshot {
  fn from(value: DatasheetSnapshotSO) -> Self {
    Self {
      meta: value.meta.into(),
      record_map: Arc::new(Mutex::new(value.record_map)),
      datasheet_id: value.datasheet_id,
    }
  }
}

impl Into<DatasheetMetaSO> for InternalDatasheetMeta {
  fn into(self) -> DatasheetMetaSO {
    DatasheetMetaSO {
      field_map: Arc::try_unwrap(self.field_map).unwrap(),
      views: self.views,
      widget_panels: self.widget_panels,
    }
  }
}

impl From<DatasheetMetaSO> for InternalDatasheetMeta {
  fn from(value: DatasheetMetaSO) -> Self {
    Self {
      field_map: Arc::new(value.field_map),
      views: value.views,
      widget_panels: value.widget_panels,
    }
  }
}

#[cfg(test)]
pub mod mock {
  use super::*;
  use databus_shared::prelude::HashMapExt;

  #[derive(Debug)]
  pub struct MockForeignDatasheetLoaderImpl {
    datasheets: HashMapExt<String, BaseDatasheetPackSO>,
    logs: Mutex<Vec<ForeignDatasheetLoadLog>>,
  }

  impl MockForeignDatasheetLoaderImpl {
    pub fn new(datasheets: HashMapExt<String, BaseDatasheetPackSO>) -> Arc<Self> {
      Arc::new(Self {
        datasheets,
        logs: Default::default(),
      })
    }

    pub fn into_logs(self) -> Vec<ForeignDatasheetLoadLog> {
      self.logs.into_inner()
    }
  }

  #[async_trait]
  impl ForeignDatasheetLoader for MockForeignDatasheetLoaderImpl {
    async fn load_foreign_datasheet(&self, dst_id: &str) -> anyhow::Result<Option<InternalBaseDatasheetPack>> {
      self
        .logs
        .lock()
        .await
        .push(ForeignDatasheetLoadLog::LoadDatasheet { dst_id: dst_id.into() });
      Ok(self.datasheets.get(dst_id).map(|dst_pack| InternalBaseDatasheetPack {
        snapshot: InternalDatasheetSnapshot {
          meta: InternalDatasheetMeta {
            field_map: Arc::new(dst_pack.snapshot.meta.field_map.clone()),
            views: dst_pack.snapshot.meta.views.clone(),
            widget_panels: dst_pack.snapshot.meta.widget_panels.clone(),
          },
          record_map: Default::default(),
          datasheet_id: dst_pack.snapshot.datasheet_id.clone(),
        },
        datasheet: dst_pack.datasheet.clone(),
        field_permission_map: dst_pack.field_permission_map.clone(),
      }))
    }

    async fn fetch_record_map(&self, dst_id: &str, record_ids: HashSet<String>) -> anyhow::Result<HashMap<String, RecordSO>> {
      self.logs.lock().await.push(ForeignDatasheetLoadLog::FetchRecords {
        dst_id: dst_id.into(),
        record_ids: record_ids.clone(),
      });
      Ok(self.datasheets.get(dst_id).map_or(Default::default(), |dst_pack| {
        dst_pack
          .snapshot
          .record_map
          .iter()
          .filter(|(id, _)| record_ids.contains(*id))
          .map(|(id, record)| (id.clone(), record.clone()))
          .collect()
      }))
    }
  }
}
