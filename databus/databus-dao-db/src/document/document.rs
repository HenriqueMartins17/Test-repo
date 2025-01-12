use std::{collections::HashMap, sync::Arc};

use crate::db_manager::DBManager;

use anyhow::Context;
use async_trait::async_trait;
use databus_core::utils::utils::generate_u64_id;
use databus_shared::prelude::SqlExt;
use mysql_async::Params;
use mysql_common::{params, Value};

#[async_trait]
pub trait DocumentDAO: Send + Sync {
  async fn is_document_exist(&self, name: &str) -> anyhow::Result<bool>;

  async fn select_data_by_name(&self, name: &str) -> anyhow::Result<Option<Vec<u8>>>;

  async fn create(
    &self,
    space_id: &str,
    resource_id: &str,
    document_type: &u8,
    name: &str,
    data: &Vec<u8>,
    props: Option<String>,
    title: Option<String>,
    created_by: Option<u64>,
  );

  async fn update_by_name(&self, name: &str, data: &Vec<u8>, title: Option<String>, updated_by: Option<u64>);

  async fn update_props(&self, resource_id: &str, document_names: &Vec<String>, record_id: &str);
}

struct DocumentDAOImpl {
  repo: Arc<dyn DBManager>,
}

pub fn new_dao(repo: Arc<dyn DBManager>) -> Arc<dyn DocumentDAO> {
  Arc::new(DocumentDAOImpl { repo })
}

#[async_trait]
impl DocumentDAO for DocumentDAOImpl {
  async fn is_document_exist(&self, name: &str) -> anyhow::Result<bool> {
    let mut client = self.repo.get_client().await.unwrap();
    let query = format!(
      "
      SELECT COUNT(*) as count \
      FROM {}document \
      WHERE name = :name",
      self.repo.table_prefix()
    );
    let count: i64 = client
      .query_one(query, params! {name})
      .await?
      .with_context(|| format!("get count in a month {name}"))
      .map_or(0, |count: i64| count);
    return Ok(count > 0);
  }

  async fn select_data_by_name(&self, name: &str) -> anyhow::Result<Option<Vec<u8>>> {
    let mut client = self.repo.get_client().await?;
    let query = format!(
      "
      SELECT data \
      FROM `{}document` \
      WHERE name = :name \
      AND is_deleted = 0 \
      ",
      self.repo.table_prefix()
    );
    Ok(client.query_one(query, params! {name}).await.unwrap())
  }

  async fn create(
    &self,
    space_id: &str,
    resource_id: &str,
    document_type: &u8,
    name: &str,
    data: &Vec<u8>,
    props: Option<String>,
    title: Option<String>,
    created_by: Option<u64>,
  ) {
    let mut client = self.repo.get_client().await.unwrap();
    let id = generate_u64_id();
    let mut sql = format!(
      "INSERT INTO {}document (id, space_id, resource_id, type, name, data, created_by, updated_by",
      self.repo.table_prefix(),
    );
    let mut sql_suffix =
      ") VALUES (:id, :space_id, :resource_id, :document_type, :name, :data, :created_by, :created_by".to_string();
    let mut params: HashMap<Vec<u8>, Value> = HashMap::new();
    params.insert(Vec::from("id"), Value::from(id));
    params.insert(Vec::from("space_id"), Value::from(space_id));
    params.insert(Vec::from("resource_id"), Value::from(resource_id));
    params.insert(Vec::from("document_type"), Value::from(document_type));
    params.insert(Vec::from("name"), Value::from(name));
    params.insert(Vec::from("data"), Value::from(data));
    params.insert(Vec::from("created_by"), Value::from(created_by));
    if let Some(props) = props {
      sql = format!("{}, props", sql);
      sql_suffix = format!("{}, :props", sql_suffix);
      params.insert(Vec::from("props"), Value::from(props));
    }
    if let Some(title) = title {
      sql = format!("{}, title", sql);
      sql_suffix = format!("{}, :title", sql_suffix);
      params.insert(Vec::from("title"), Value::from(title));
    }
    sql = format!("{}{}{}", sql, sql_suffix, ")");
    client.execute(sql, Params::Named(params)).await.unwrap()
  }

  async fn update_by_name(&self, name: &str, data: &Vec<u8>, title: Option<String>, updated_by: Option<u64>) {
    let mut client = self.repo.get_client().await.unwrap();
    let mut sql = format!(
      "
      UPDATE {}document \
      SET data = :data",
      self.repo.table_prefix(),
    );
    let mut params: HashMap<Vec<u8>, Value> = HashMap::new();
    params.insert(Vec::from("data"), Value::from(data));
    if title.is_some() {
      sql.push_str(", title = :title");
      params.insert(Vec::from("title"), Value::from(title));
    }
    if updated_by.is_some() {
      sql.push_str(", updated_by = :updated_by");
      params.insert(Vec::from("updated_by"), Value::from(updated_by));
    }
    sql.push_str(" WHERE name = :name AND is_deleted = 0");
    params.insert(Vec::from("name"), Value::from(name));
    client.execute(sql, Params::Named(params)).await.unwrap();
  }

  async fn update_props(&self, resource_id: &str, document_names: &Vec<String>, record_id: &str) {
    let mut client = self.repo.get_client().await.unwrap();
    let mut sql: String = format!(
      "
      UPDATE {}document \
      SET props = JSON_SET(props, '$.recordId', '{}') \
      WHERE resource_id = ? \
      AND name",
      self.repo.table_prefix(), record_id
    );
    sql = sql.append_in_condition(document_names.len());
    client.execute(sql, {
      let mut values: Vec<Value> = vec![resource_id.into()];
      values.extend(document_names.iter().map(Value::from));
      Params::Positional(values)
    }).await.unwrap();
  }
}
