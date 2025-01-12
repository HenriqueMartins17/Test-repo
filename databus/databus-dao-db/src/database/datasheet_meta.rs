use std::collections::HashMap;
use crate::DBManager;
use anyhow::Context;
use async_trait::async_trait;
use databus_core::so::FieldSO;
use databus_shared::prelude::Json;
use mysql_async::params;
use std::sync::Arc;
use mysql_common::params::Params;
use mysql_common::Value;

#[async_trait]
pub trait DatasheetMetaDAO: Send + Sync {
  async fn get_meta_data_by_dst_id(&self, dst_id: &str, include_deleted: bool) -> anyhow::Result<Option<Json>>;
  async fn get_field_map_by_dst_id(&self, dst_id: &str) -> anyhow::Result<Option<HashMap<String, FieldSO>>> ;
  async fn get_field_by_fld_id_and_dst_id(&self, fld_id: &str, dst_id: &str) -> anyhow::Result<Option<FieldSO>>;
  async fn get_meta_data_by_dst_id_and_view_id(&self, dst_id: &str, view_id: &str) -> anyhow::Result<Option<Json>>;
  async fn update_meta_data_by_dst_id(&self, dst_id: &str, meta_data: &str);
  async fn count_rows_by_dst_id(&self, dst_id: &str) -> anyhow::Result<u32>;
  async fn update_meta_data(&self, dst_id: &str, meta_data: &str, revision: &u32, updated_by: &str);
  async fn select_count_by_dst_id_and_field_name(&self, dst_id: &str, field_name: &str) -> anyhow::Result<u32>;
  async fn select_field_type_by_fld_id_and_dst_id(&self, fld_id: &str, dst_id: &str) -> anyhow::Result<Option<u32>> ;
}

struct DatasheetMetaDAOImpl {
  repo: Arc<dyn DBManager>,
}

pub fn new_dao(repo: Arc<dyn DBManager>) -> Arc<dyn DatasheetMetaDAO> {
  Arc::new(DatasheetMetaDAOImpl { repo })
}

#[async_trait]
impl DatasheetMetaDAO for DatasheetMetaDAOImpl {
  async fn get_meta_data_by_dst_id(&self, dst_id: &str, include_deleted: bool) -> anyhow::Result<Option<Json>> {
    let mut client = self.repo.get_client().await?;
    let mut query = format!(
      "
      SELECT `meta_data` \
      FROM `{prefix}datasheet_meta` \
      WHERE `dst_id` = :dst_id",
      prefix = self.repo.table_prefix()
    );
    if !include_deleted {
      query.push_str(" AND is_deleted = 0");
    }
    Ok(
      client
        .query_one(
          query,
          params! {
            dst_id
          },
        )
        .await
        .with_context(|| format!("get datasheet meta data of {dst_id}"))?,
    )
  }

  async fn get_field_map_by_dst_id(&self, dst_id: &str) -> anyhow::Result<Option<HashMap<String, FieldSO>>> {
    let mut client = self.repo.get_client().await?;
    let mut query = format!(
      "
      SELECT `meta_data`->'$.fieldMap' as fieldMap \
      FROM `{prefix}datasheet_meta` \
      WHERE `dst_id` = :dst_id",
      prefix = self.repo.table_prefix()
    );
    query.push_str(" AND is_deleted = 0");
    Ok(
      client
        .query_one(
          query,
          params! {
            dst_id
          },
        )
        .await
        .with_context(|| format!("get datasheet fieldMap of {dst_id}"))?
        .map_or(None, |field_map: serde_json::Value| {
          let field_map: HashMap<String, FieldSO> = serde_json::from_value(field_map).unwrap();
          Some(field_map)
        }),
    )
  }

  async fn get_field_by_fld_id_and_dst_id(&self, fld_id: &str, dst_id: &str) -> anyhow::Result<Option<FieldSO>> {
    let mut client: crate::db_manager::client::DbClient = self.repo.get_client().await?;
    let sql = format!(
      "
      SELECT JSON_EXTRACT(meta_data, CONCAT('$.fieldMap.', :fld_id))
      FROM `{prefix}datasheet_meta`
      WHERE dst_id = :dst_id AND is_deleted = 0",
      prefix = self.repo.table_prefix()
    );
    let field: Option<Value> = client
      .query_one(sql, params! {fld_id, dst_id})
      .await?
      .with_context(|| format!("get_field_by_fld_id_and_dst_id err {fld_id:?}"))?;
    match field {
      Some(Value::Bytes(bytes)) => {
        let field: FieldSO = serde_json::from_slice(&bytes)?;
        println!("get_field_by_fld_id_and_dst_id field: {:?}", field);
        Ok(Some(field))
      },
      _ => Ok(None)
    }
  }

  async fn select_field_type_by_fld_id_and_dst_id(&self, fld_id: &str, dst_id: &str) -> anyhow::Result<Option<u32>> {
    let mut client = self.repo.get_client().await?;
    let sql = format!(
      "
      SELECT JSON_EXTRACT(meta_data, CONCAT('$.fieldMap.', :fld_id, '.type'))
      FROM `{prefix}datasheet_meta`
      WHERE dst_id = :dst_id AND is_deleted = 0",
      prefix = self.repo.table_prefix()
    );
    let field_type: Option<Value> = client
      .query_one(sql, params! {fld_id, dst_id})
      .await?
      .with_context(|| format!("select_field_type_by_fld_id_and_dst_id err {fld_id:?}"))?;
    match field_type {
      Some(Value::Bytes(bytes)) => {
        let count = String::from_utf8(bytes.to_vec())?;
        Ok(Some(count.parse::<u32>()?))
      },
      _ => Ok(None)
    }
  }

  async fn get_meta_data_by_dst_id_and_view_id(&self, dst_id: &str, view_id: &str) -> anyhow::Result<Option<Json>> {
    let sql = format!(
      r#"
      SELECT json_object(
        'fieldMap', meta_data->'$.fieldMap',
        'views', json_array(json_extract(meta_data, trim(trailing '.id' from
            json_unquote(json_search(meta_data, 'one', :view_id, null, '$.views[*].id')))))
      ) AS metadata
      FROM `{prefix}datasheet_meta`
      WHERE dst_id = :dst_id
      AND is_deleted = 0
      "#,
      prefix = self.repo.table_prefix()
    );
    Ok(
      self.repo.get_client().await?
        .query_one(
          sql,
          params! {
            view_id,
            dst_id,
          },
        )
        .await
        .with_context(|| format!("get_meta_data_by_dst_id_and_view_id of {dst_id} and {view_id}"))?,
    )
  }

  async fn update_meta_data_by_dst_id(&self, dst_id: &str, meta_data: &str) {
    let mut client = self.repo.get_client().await.unwrap();
    let update_sql = format!(
      "
      UPDATE {}datasheet_meta \
      SET meta_data = :meta_data \
      WHERE dst_id = :dst_id\
      ",
      self.repo.table_prefix(),
    );
    let mut params: HashMap<Vec<u8>, Value> = HashMap::new();
    params.insert(Vec::from("meta_data"), Value::from(meta_data));
    params.insert(Vec::from("dst_id"), Value::from(dst_id));
    client.execute(update_sql, Params::Named(params)).await.unwrap()
  }

  async fn count_rows_by_dst_id(&self, dst_id: &str) -> anyhow::Result<u32>{
    let mut client = self.repo.get_client().await?;
    let sql = format!(
      "
      SELECT IFNULL(SUM(JSON_LENGTH( meta_data -> '$.views[0].rows' )), 0) as count
      FROM `{prefix}datasheet_meta`
      WHERE dst_id = :dst_id AND is_deleted = 0",
      prefix = self.repo.table_prefix()
    );
    let count: Option<Value> = client.query_one(sql, params!{dst_id}).await.with_context(|| format!("get datasheet meta data of {dst_id}"))?;
    match count {
      Some(Value::Bytes(bytes)) => {
        let count = String::from_utf8(bytes.to_vec())?;
        Ok(count.parse::<u32>()?)
      },
      _ => Ok(0)
    }
  }

  async fn update_meta_data(&self, dst_id: &str, meta_data: &str, revision: &u32, updated_by: &str) {
    let mut client = self.repo.get_client().await.unwrap();
    let update_sql = format!(
      "
      UPDATE `{prefix}datasheet_meta` \
      SET meta_data = :meta_data, revision = :revision, updated_by = :updated_by \
      WHERE dst_id = :dst_id\
      ",
      prefix = self.repo.table_prefix(),
    );
    client.execute(update_sql, params! {meta_data,revision,updated_by,dst_id}).await.unwrap()
  }

  async fn select_count_by_dst_id_and_field_name(&self, dst_id: &str, field_name: &str) -> anyhow::Result<u32> {
    let mut client = self.repo.get_client().await?;
    let sql = format!(
      "
      SELECT COUNT(*) \
      FROM `{prefix}datasheet_meta` \
      WHERE json_search(meta_data, 'one', '{}', null, '$.fieldMap.*.name') \
      AND dst_id = :dst_id AND is_deleted = 0",
      field_name,
      prefix = self.repo.table_prefix()
    );
    let count: u32 = client
      .query_one(sql, params! {dst_id})
      .await?
      .with_context(|| format!("select_count_by_dst_id_and_field_name err {field_name:?}"))
      .map_or(0, |count: u32| count);
    Ok(count)
  }
}

#[cfg(test)]
pub mod mock {
  use std::collections::HashMap;
  use serde_json::Value;
  use super::*;

  #[derive(Default)]
  pub struct MockDatasheetMetaDAOImpl {
    metas: HashMap<&'static str, Json>,
  }

  impl MockDatasheetMetaDAOImpl {
    pub fn new() -> Self {
      Self::default()
    }

    pub fn with_metas(mut self, metas: HashMap<&'static str, Value>) -> Self {
      self.metas = metas;
      self
    }

    pub fn build(self) -> Arc<dyn DatasheetMetaDAO> {
      Arc::new(self)
    }
  }

  #[async_trait]
  impl DatasheetMetaDAO for MockDatasheetMetaDAOImpl {
    async fn get_meta_data_by_dst_id(&self, dst_id: &str, _include_deleted: bool) -> anyhow::Result<Option<Json>> {
      Ok(self.metas.get(dst_id).cloned())
    }

    async fn get_field_map_by_dst_id(&self, _dst_id: &str) -> anyhow::Result<Option<HashMap<String, FieldSO>>> {
      Ok(None)
    }

    async fn get_field_by_fld_id_and_dst_id(&self, _fld_id: &str, _dst_id: &str) -> anyhow::Result<Option<FieldSO>> {
      Ok(None)
    }

    async fn select_field_type_by_fld_id_and_dst_id(&self, _fld_id: &str, _dst_id: &str) -> anyhow::Result<Option<u32>> {
      Ok(None)
    }

    async fn get_meta_data_by_dst_id_and_view_id(&self,  _dst_id: &str, _view_id: &str,) -> anyhow::Result<Option<Json>> {
      println!("MockDatasheetMetaDAOImpl get_meta_data_by_dst_id_and_view_id");
      Ok(None)
    }

    async fn update_meta_data_by_dst_id(&self, _dst_id: &str, _meta_data: &str) {
      println!("MockDatasheetMetaDAOImpl update_meta_data_by_dst_id");
    }

    async fn count_rows_by_dst_id(&self, _dst_id: &str) -> anyhow::Result<u32>{
      Ok(0)
    }

    async fn update_meta_data(&self, _dst_id: &str, _meta_data: &str, _revision: &u32, _updated_by: &str) {}

    async fn select_count_by_dst_id_and_field_name(&self, _dst_id: &str, _field_name: &str) -> anyhow::Result<u32> {
      Ok(0)
    }
  }
}
