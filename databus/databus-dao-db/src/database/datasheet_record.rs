use std::collections::HashMap;
use std::sync::Arc;

use anyhow::Context;
use async_trait::async_trait;
use futures::TryStreamExt;
use mysql_async::{Params, Value};
use mysql_common::params;
use serde_json::to_string;
use time::PrimitiveDateTime;

use databus_core::{prelude::*, utils::utils::generate_u64_id};
use databus_shared::prelude::*;

use crate::DBManager;

use super::datasheet_record_comment::DatasheetRecordCommentDAO;

#[async_trait]
pub trait DatasheetRecordDAO: Send + Sync {
  async fn get_records(
    &self,
    dst_id: &str,
    record_ids: Option<Vec<String>>,
    is_deleted: bool,
    with_comment: bool,
  ) -> anyhow::Result<HashMap<String, RecordSO>>;

  async fn get_ids_by_dst_id_and_record_ids(
    &self, 
    dst_id: &str,
    record_ids: Vec<String>,
  ) -> anyhow::Result<Vec<String>>;

  async fn get_archived_ids_by_dst_id_and_record_ids(&self, dst_id: &str, record_ids: Vec<String>) -> anyhow::Result<Vec<String>>;

  async fn update_record_replace(
    &self,
    dst_id: &str,
    record_id: &str,
    json_map: HashMap<&str, serde_json::Value>,
    record_meta: String,
    revision: &u32,
    updated_by: &str,
  );

  async fn update_record_delete(
    &self,
    dst_id: &str,
    record_ids: &Vec<String>,
    is_delete_data: bool,
    revision: &u32,
    updated_by: &str,
  );

  async fn update_record_archive_delete(
    &self,
    dst_id: &str,
    record_ids: &Vec<String>,
    updated_by: &str,
  );

  async fn update_record_remove(
    &self,
    dst_id: &str,
    record_id: &str,
    json_path: String,
    record_meta: String,
    revision: &u32,
    updated_by: &str,
  );

  async fn create_record_source(
    &self,
    user_id: &str,
    dst_id: &str,
    source_id: &str,
    record_ids: Vec<String>,
    source_type: &u32,
  );

  async fn create_record(
    &self,
    dst_id: &str,
    revision: &u32,
    user_id: &str,
    save_record_entities: Vec<(&String, HashMap<String, serde_json::Value>, RecordMeta)>
  );
}

struct DatasheetRecordDAOImpl {
  repo: Arc<dyn DBManager>,
  record_comment_dao: Arc<dyn DatasheetRecordCommentDAO>,
}

pub fn new_dao(
  repo: Arc<dyn DBManager>,
  record_comment_dao: Arc<dyn DatasheetRecordCommentDAO>,
) -> Arc<dyn DatasheetRecordDAO> {
  Arc::new(DatasheetRecordDAOImpl {
    repo,
    record_comment_dao: record_comment_dao,
  })
}

#[async_trait]
impl DatasheetRecordDAO for DatasheetRecordDAOImpl {
  async fn get_records(
    &self,
    dst_id: &str,
    record_ids: Option<Vec<String>>,
    is_deleted: bool,
    with_comment: bool,
  ) -> anyhow::Result<HashMap<String, RecordSO>> {
    if record_ids.as_ref().map(|record_ids| record_ids.is_empty()).is_truthy() {
      return Ok(Default::default());
    }

    let mut query = format!(
      "\
        SELECT `record_id`, `data`, `revision_history`, `field_updated_info`, `created_at`, `updated_at` \
        FROM `{prefix}datasheet_record` \
        WHERE `dst_id` = ? AND `is_deleted` = ?\
      ",
      prefix = self.repo.table_prefix()
    );
    if let Some(record_ids) = &record_ids {
      query.push_str(" AND `record_id`");
      query = query.append_in_condition(record_ids.len());
    }
    let mut client = self.repo.get_client().await?;
    let comment_counts = if with_comment {
      self
        .record_comment_dao
        .get_record_comment_map_by_dst_id(dst_id)
        .await
        .with_context(|| format!("get record comment counts of dst id {dst_id} for build record map"))?
    } else {
      HashMapExt::default()
    };
    let record_map = client
      .query_all(query, {
        let mut values: Vec<Value> = vec![dst_id.into(), is_deleted.into()];
        if let Some(record_ids) = &record_ids {
          values.extend(record_ids.iter().map(Value::from));
        }
        Params::Positional(values)
      })
      .await
      .with_context(|| format!("get records stream by dst id {dst_id}, record id {record_ids:?}"))?
      .map_ok(|row| {
        let (record_id, data, revision_history, record_meta, created_at, updated_at): (
          String,
          Option<Json>,
          Option<String>,
          Option<Json>,
          PrimitiveDateTime,
          Option<PrimitiveDateTime>,
        ) = row;
        let comment_count = comment_counts.get(&record_id).copied().unwrap_or(0) as u32;
        let record = RecordSO {
          id: record_id.clone(),
          data: data.unwrap_or_else(|| Json::Object(Default::default())),
          comment_count,
          created_at: Some(self.repo.utc_timestamp(created_at)),
          updated_at: updated_at.map(|d| self.repo.utc_timestamp(d)),
          revision_history: revision_history.map(|s| s.split(',').map(|n| n.parse().unwrap_or(0)).collect()),
          record_meta: record_meta.and_then(|it|Some(serde_json::from_value::<RecordMeta>(it.clone()).unwrap())),
          ..Default::default()
        };
        (record_id, record)
      })
      .try_collect::<HashMap<String, RecordSO>>()
      .await
      .with_context(|| format!("get records by dst id {dst_id}, record id {record_ids:?}"));
    record_map
  }

  async fn get_ids_by_dst_id_and_record_ids(
    &self,
    dst_id: &str,
    record_ids: Vec<String>,
  ) -> anyhow::Result<Vec<String>> {
    let mut query = format!(
      "\
        SELECT `record_id` \
        FROM `{prefix}datasheet_record` \
        WHERE `dst_id` = ? AND is_deleted = 0 \
      ",
      prefix = self.repo.table_prefix()
    );
    query.push_str(" AND `record_id`");
    query = query.append_in_condition(record_ids.len());
    let mut client = self.repo.get_client().await?;
    let ids = client
      .query_all(query, {
        let mut values: Vec<Value> = vec![dst_id.into()];
        values.extend(record_ids.iter().map(Value::from));
        Params::Positional(values)
      })
      .await
      .with_context(|| format!("get_ids by dst id {dst_id}, record id {record_ids:?}"))?
      .map_ok(|row| {
        row
      })
      .try_collect::<Vec<String>>()
      .await
      .with_context(|| format!("get_ids by dst id {dst_id}, record id {record_ids:?}"));
    ids
  }

  async fn get_archived_ids_by_dst_id_and_record_ids(&self, dst_id: &str, record_ids: Vec<String>) -> anyhow::Result<Vec<String>> {
    let mut query = format!(
      "\
        SELECT `record_id` \
        FROM `{prefix}datasheet_record_archive` \
        WHERE `dst_id` = ? AND is_deleted = 0 AND is_archived = 1\
      ",
      prefix = self.repo.table_prefix()
    );
    query.push_str(" AND `record_id`");
    query = query.append_in_condition(record_ids.len());
    let mut client = self.repo.get_client().await?;
    let ids = client
      .query_all(query, {
        let mut values: Vec<Value> = vec![dst_id.into()];
        values.extend(record_ids.iter().map(Value::from));
        Params::Positional(values)
      })
      .await
      .with_context(|| format!("get_archived_ids by dst id {dst_id}, record id {record_ids:?}"))?
      .map_ok(|row| {
        row
      })
      .try_collect::<Vec<String>>()
      .await
      .with_context(|| format!("get_archived_ids by dst id {dst_id}, record id {record_ids:?}"));
    ids
  }

  async fn update_record_replace(
    &self,
    dst_id: &str,
    record_id: &str,
    json_map: HashMap<&str, serde_json::Value>,
    record_meta: String,
    revision: &u32,
    updated_by: &str,
  ) {
    let mut client = self.repo.get_client().await.unwrap();
    let mut sql = format!(
      "
      UPDATE {}datasheet_record \
      SET data = JSON_SET(data,",
      self.repo.table_prefix()
    );
    let mut params: HashMap<Vec<u8>, Value> = HashMap::new();
    let mut num = 1;
    for (key, value) in json_map {
      let param_name = format!("json_value_{}", num);
      sql.push_str(&format!(" '$.{}', CAST(:{} AS JSON),", key, param_name));
      params.insert(Vec::from(param_name), Value::from(value));
      num += 1;
    }
    // Remove the trailing comma
    sql.pop();
    sql.push_str(&format!(
      "), field_updated_info = :record_meta, revision = :revision, revision_history = CONCAT_WS(',', revision_history, '{}'), \
      is_deleted = false, updated_by = :updated_by \
      WHERE dst_id = :dst_id AND record_id = :record_id",
      revision
    ));
    params.insert(Vec::from("record_meta"), Value::from(record_meta));
    params.insert(Vec::from("revision"), Value::from(revision));
    params.insert(Vec::from("updated_by"), Value::from(updated_by));
    params.insert(Vec::from("dst_id"), Value::from(dst_id));
    params.insert(Vec::from("record_id"), Value::from(record_id));
    let params = Params::Named(params);
    client.execute(sql, params).await.unwrap()
  }

  async fn update_record_delete(
    &self,
    dst_id: &str,
    record_ids: &Vec<String>,
    is_delete_data: bool,
    revision: &u32,
    updated_by: &str,
  ) {
    let mut client = self.repo.get_client().await.unwrap();
    let mut sql = if is_delete_data {
      let data = HashMap::<&str, serde_json::Value>::new();
      let str_data = to_string(&data).unwrap();
      format!(
        "
        UPDATE {}datasheet_record \
        SET data = '{}', is_deleted = true, revision = ?, revision_history = CONCAT_WS(',', revision_history, '{}'), \
        updated_by = ? \
        WHERE dst_id = ? AND record_id",
        self.repo.table_prefix(), str_data, revision
      )
    } else {
      format!(
        "
        UPDATE {}datasheet_record \
        SET is_deleted = true, revision = ?, revision_history = CONCAT_WS(',', revision_history, '{}'), \
        updated_by = ? \
        WHERE dst_id = ? AND record_id",
        self.repo.table_prefix(), revision
      )
    };
    sql = sql.append_in_condition(record_ids.len());
    let params = Params::Positional({
      let mut values: Vec<Value> = vec![revision.into(), updated_by.into(), dst_id.into()];
      values.extend(record_ids.iter().map(Value::from));
      values
    });
    client.execute(sql, params).await.unwrap()
  }

  async fn update_record_archive_delete(
    &self,
    dst_id: &str,
    record_ids: &Vec<String>,
    updated_by: &str,
  ) {
    let mut client = self.repo.get_client().await.unwrap();
    let mut sql = format!(
      "
      UPDATE {}datasheet_record_archive \
      SET is_deleted = true, updated_by = ? \
      WHERE dst_id = ? AND record_id",
      self.repo.table_prefix()
    );
    sql = sql.append_in_condition(record_ids.len());
    let params = Params::Positional({
      let mut values: Vec<Value> = vec![updated_by.into(), dst_id.into()];
      values.extend(record_ids.iter().map(Value::from));
      values
    });
    client.execute(sql, params).await.unwrap()
  }

  async fn update_record_remove(
    &self,
    dst_id: &str,
    record_id: &str,
    json_path: String,
    record_meta: String,
    revision: &u32,
    updated_by: &str,
  ) {
    let mut client = self.repo.get_client().await.unwrap();
    let sql = format!(
      "
      UPDATE {}datasheet_record \
      SET data = JSON_REMOVE(data, '{}'), field_updated_info = :record_meta, revision = :revision, revision_history = CONCAT_WS(',', revision_history, '{}'), \
      updated_by = :updated_by \
      WHERE dst_id = :dst_id AND record_id = :record_id",
      self.repo.table_prefix(), json_path, revision
    );
    client.execute(sql, params! {record_meta, revision, updated_by, dst_id, record_id}).await.unwrap()
  }

  async fn create_record_source(
    &self,
    user_id: &str,
    dst_id: &str,
    source_id: &str,
    record_ids: Vec<String>,
    source_type: &u32,
  ) {
    let entities: Vec<_> = record_ids
      .iter()
      .map(|record_id| {
        let id = generate_u64_id().to_string();
        (
          id.clone(),
          user_id.to_owned(),
          dst_id.to_owned(),
          source_id.to_owned(),
          record_id.to_owned(),
          source_type.to_owned(),
        )
      })
      .collect();
    let mut client = self.repo.get_client().await.unwrap();
    let mut sql = format!(
      "INSERT INTO {}datasheet_record_source (id, created_by, dst_id, source_id, record_id, type) \
      VALUES ",
      self.repo.table_prefix()
    );
    for (i, (id, user_id, dst_id, source_id, record_id, source_type)) in entities.iter().enumerate() {
      sql.push_str(&format!(
        "('{}', '{}', '{}', '{}', '{}', {})",
        id, user_id, dst_id, source_id, record_id, source_type
      ));
      if i < entities.len() - 1 {
          sql.push_str(", ");
      }
    }
    let params = Params::Empty;
    client.execute(sql, params).await.unwrap()
  }

  async fn create_record(
    &self,
    dst_id: &str,
    revision: &u32,
    user_id: &str,
    save_record_entities: Vec<(&String, HashMap<String, serde_json::Value>, RecordMeta)>
  ) {
    let mut client = self.repo.get_client().await.unwrap();
    let mut sql = format!(
      "INSERT INTO {}datasheet_record (id, dst_id, record_id, data, revision, revision_history, created_by, updated_by, field_updated_info) \
      VALUES ",
      self.repo.table_prefix()
    );
    for (i, (record_id, data, record_meta)) in save_record_entities.iter().enumerate() {
      let str_data = to_string(data).unwrap();
      let str_data = str_data.replace("\\n", "n");
      let id = generate_u64_id().to_string();
      sql.push_str(&format!(
        "('{}', '{}', '{}', '{}', {}, '{}', '{}', '{}', '{}')",
        id, dst_id, record_id, str_data, revision, revision, user_id, user_id, to_string(record_meta).unwrap()
      ));
      if i < save_record_entities.len() - 1 {
          sql.push_str(", ");
      }
    }
    let params = Params::Empty;
    client.execute(sql, params).await.unwrap()
  }
}

#[cfg(test)]
pub mod mock {
  use super::*;

  #[derive(Default)]
  pub struct MockDatasheetRecordDAOImpl {
    records: HashMap<&'static str, HashMap<String, RecordSO>>,
  }

  impl MockDatasheetRecordDAOImpl {
    pub fn new() -> Self {
      Self::default()
    }

    pub fn with_records(mut self, records: HashMap<&'static str, HashMap<String, RecordSO>>) -> Self {
      self.records = records;
      self
    }

    pub fn build(self) -> Arc<dyn DatasheetRecordDAO> {
      Arc::new(self)
    }
  }

  #[async_trait]
  impl DatasheetRecordDAO for MockDatasheetRecordDAOImpl {
    async fn get_records(
      &self,
      dst_id: &str,
      record_ids: Option<Vec<String>>,
      _is_deleted: bool,
      _with_comment: bool,
    ) -> anyhow::Result<HashMap<String, RecordSO>> {
      if let Some(record_ids) = record_ids {
        Ok(self.records.get(dst_id).map_or(HashMap::default(), |records| {
          records
            .iter()
            .filter(|(id, _)| record_ids.contains_ref(*id))
            .map(|(id, record)| (id.to_owned(), record.clone()))
            .collect()
        }))
      } else {
        Ok(self.records.get(dst_id).map_or(HashMap::default(), |records| {
          records
            .iter()
            .map(|(id, record)| (id.to_owned(), record.clone()))
            .collect()
        }))
      }
    }

    async fn get_ids_by_dst_id_and_record_ids(
      &self, 
      _dst_id: &str,
      record_ids: Vec<String>,
    ) -> anyhow::Result<Vec<String>>{
      Ok(record_ids)
    }

    async fn get_archived_ids_by_dst_id_and_record_ids(&self, _dst_id: &str, _record_ids: Vec<String>) -> anyhow::Result<Vec<String>> {
      Ok(Vec::new())
    }

    async fn update_record_replace(
      &self,
      _dst_id: &str,
      _record_id: &str,
      _json_map: HashMap<&str, serde_json::Value>,
      _record_meta: String,
      _revision: &u32,
      _updated_by: &str,
    ) {}

    async fn update_record_delete(
      &self,
      _dst_id: &str,
      _record_ids: &Vec<String>,
      _is_delete_data: bool,
      _revision: &u32,
      _updated_by: &str,
    ) {}

    async fn update_record_archive_delete(
      &self,
      _dst_id: &str,
      _record_ids: &Vec<String>,
      _updated_by: &str,
    ) {}

    async fn update_record_remove(
      &self,
      _dst_id: &str,
      _record_id: &str,
      _json_path: String,
      _record_meta: String,
      _revision: &u32,
      _updated_by: &str,
    ) {}

    async fn create_record_source(
      &self,
      _user_id: &str,
      _dst_id: &str,
      _source_id: &str,
      _record_ids: Vec<String>,
      _source_type: &u32,
    ) {}

    async fn create_record(
      &self,
      _dst_id: &str,
      _revision: &u32,
      _user_id: &str,
      _save_record_entities: Vec<(&String, HashMap<String, serde_json::Value>, RecordMeta)>
    ) {}
  }
}

#[cfg(test)]
mod tests {
  use mysql_async::{params, Row};
  use mysql_async::consts::ColumnType;
  use pretty_assertions::assert_eq;
  use serde_json::json;
  use time::OffsetDateTime;
  use tokio_test::assert_ok;

  use crate::datasheet_record_comment::DatasheetRecordCommentDAOImpl;
  use crate::mock::{mock_rows, MockRepositoryImpl, MockSqlLog};

  use super::*;

  #[test]
  pub fn test_alarm_deserialize(){
    let json: &str = r#"{
      "field_extra_map": {
        "kkk": {
          "alarm": {}
        }
      }
    }"#;
    let record_meta: Option<Json> = Some(serde_json::from_str::<Json>(json).expect("parse error"));
    let result = record_meta
        .and_then(|it| Some(serde_json::from_value::<RecordMeta>(it.clone()).unwrap()));
  }

  fn timestamp(n: i64) -> PrimitiveDateTime {
    let d = OffsetDateTime::from_unix_timestamp_nanos(n as i128 * 1_000_000).unwrap();
    PrimitiveDateTime::new(d.date(), d.time())
      .replace_millisecond(d.millisecond())
      .unwrap()
  }

  fn mock_record_query_results(_is_deleted: bool) -> Vec<Row> {
    mock_rows(
      [
        ("record_id", ColumnType::MYSQL_TYPE_VARCHAR),
        ("data", ColumnType::MYSQL_TYPE_JSON),
        ("revision_history", ColumnType::MYSQL_TYPE_VARCHAR),
        ("field_updated_info", ColumnType::MYSQL_TYPE_JSON),
        ("created_at", ColumnType::MYSQL_TYPE_TIMESTAMP),
        ("updated_at", ColumnType::MYSQL_TYPE_TIMESTAMP),
      ],
      [
        [
          "rec1".into(),
          json!({ "fld1": 123 }).into(),
          "0,3,7,8".into(),
          Value::NULL,
          timestamp(1676945874561).into(),
          timestamp(1676945875561).into(),
        ],
        [
          "rec2".into(),
          json!({ "fld1": 889 }).into(),
          "4".into(),
          json!( { "createdAt": 1676945874561u64 } ).into(),
          timestamp(1676945874561).into(),
          timestamp(1676945875562).into(),
        ],
        [
          "rec3".into(),
          Value::NULL,
          Value::NULL,
          Value::NULL,
          timestamp(1676945874561).into(),
          Value::NULL,
        ],
      ],
    )
  }

  const MOCK_RECORD_WITHOUT_RECORD_IDS_QUERY_SQL: &str = "SELECT \
    `record_id`, \
    `data`, \
    `revision_history`, \
    `field_updated_info`, \
    `created_at`, \
    `updated_at` \
    FROM `apitable_datasheet_record` \
    WHERE `dst_id` = ? \
    AND `is_deleted` = ?";

  const MOCK_RECORD_COMMENT_QUERY_SQL: &str = "SELECT \
    `record_id`, \
    COUNT(*) AS `count` \
    FROM `apitable_datasheet_record_comment` \
    WHERE `dst_id` = :dst_id \
    AND `is_deleted` = 0 \
    GROUP BY `record_id`";

  fn mock_dao<I>(results: I) -> (Arc<dyn DBManager>, Arc<dyn DatasheetRecordDAO>)
  where
    I: IntoIterator<Item = Vec<Row>>,
  {
    let repo = MockRepositoryImpl::new(results);
    (
      repo.clone(),
      new_dao(repo.clone(), DatasheetRecordCommentDAOImpl::new(repo)),
    )
  }

  #[tokio::test]
  async fn get_records_without_record_ids() {
    let (repo, record_dao) = mock_dao([
      mock_rows(
        [
          ("record_id", ColumnType::MYSQL_TYPE_VARCHAR),
          ("count", ColumnType::MYSQL_TYPE_LONG),
        ],
        [["rec1".into(), 2i64.into()], ["rec2".into(), 1i64.into()]],
      ),
      mock_record_query_results(false),
    ]);

    let record_map = assert_ok!(record_dao.get_records("dst1", None, false, true).await);
    assert_eq!(
      record_map,
      [
        (
          "rec1".to_owned(),
          RecordSO {
            id: "rec1".to_owned(),
            comment_count: 2,
            data: json!( { "fld1": 123 } ),
            created_at: Some(1676945874561),
            updated_at: Some(1676945875561),
            revision_history: Some(vec![0, 3, 7, 8]),
            ..Default::default()
          }
        ),
        (
          "rec2".to_owned(),
          RecordSO {
            id: "rec2".to_owned(),
            comment_count: 1,
            data: json!( { "fld1": 889 } ),
            created_at: Some(1676945874561),
            updated_at: Some(1676945875562),
            revision_history: Some(vec![4]),
            record_meta: Some(RecordMeta {
              field_updated_map: None,
              created_by: None,
              updated_by: None,
              created_at: Some(1676945874561u64),
              updated_at: None,
              ..Default::default()
            }),
            ..Default::default()
          }
        ),
        (
          "rec3".to_owned(),
          RecordSO {
            id: "rec3".to_owned(),
            comment_count: 0,
            data: json!({}),
            created_at: Some(1676945874561),
            updated_at: None,
            revision_history: None,
            record_meta: None,
            ..Default::default()
          }
        ),
      ]
      .into_iter()
      .collect::<HashMap<_, _>>()
    );

    assert_eq!(
      repo.take_logs().await,
      [
        MockSqlLog {
          sql: MOCK_RECORD_COMMENT_QUERY_SQL.into(),
          params: params! {
            "dst_id" => "dst1"
          },
        },
        MockSqlLog {
          sql: MOCK_RECORD_WITHOUT_RECORD_IDS_QUERY_SQL.into(),
          params: Params::Positional(vec!["dst1".into(), false.into()])
        },
      ]
    );
  }

  #[tokio::test]
  async fn get_records_with_record_ids() {
    let (repo, record_dao) = mock_dao([vec![], mock_record_query_results(false)]);

    let record_map = assert_ok!(
      record_dao
        .get_records(
          "dst1",
          Some(vec!["rec1".to_owned(), "rec2".to_owned(), "rec3".to_owned(),]),
          false,
          true
        )
        .await
    );

    assert_eq!(
      record_map,
      [
        (
          "rec1".to_owned(),
          RecordSO {
            id: "rec1".to_owned(),
            comment_count: 0,
            data: json!( { "fld1": 123 } ),
            created_at: Some(1676945874561),
            updated_at: Some(1676945875561),
            revision_history: Some(vec![0, 3, 7, 8]),
            record_meta: None,
            ..Default::default()
          }
        ),
        (
          "rec2".to_owned(),
          RecordSO {
            id: "rec2".to_owned(),
            comment_count: 0,
            data: json!( { "fld1": 889 } ),
            created_at: Some(1676945874561),
            updated_at: Some(1676945875562),
            revision_history: Some(vec![4]),
            record_meta: Some(RecordMeta {
              field_updated_map: None,
              created_by: None,
              updated_by: None,
              created_at: Some(1676945874561u64),
              updated_at: None,
              ..Default::default()
            }),
            ..Default::default()
          }
        ),
        (
          "rec3".to_owned(),
          RecordSO {
            id: "rec3".to_owned(),
            comment_count: 0,
            data: json!({}),
            created_at: Some(1676945874561),
            updated_at: None,
            revision_history: None,
            record_meta: None,
            ..Default::default()
          }
        ),
      ]
      .into_iter()
      .collect::<HashMap<_, _>>()
    );

    assert_eq!(
      repo.take_logs().await,
      [
        MockSqlLog {
          sql: MOCK_RECORD_COMMENT_QUERY_SQL.into(),
          params: params! {
            "dst_id" => "dst1",
          },
        },
        MockSqlLog {
          sql: "SELECT `record_id`, \
        `data`, \
        `revision_history`, \
        `field_updated_info`, \
        `created_at`, \
        `updated_at` \
        FROM `apitable_datasheet_record` \
        WHERE `dst_id` = ? \
        AND `is_deleted` = ? \
        AND `record_id` IN (?,?,?)"
            .into(),
          params: Params::Positional(vec![
            "dst1".into(),
            false.into(),
            "rec1".into(),
            "rec2".into(),
            "rec3".into()
          ])
        },
      ]
    );
  }

  #[tokio::test]
  async fn get_records_deleted() {
    let (repo, record_dao) = mock_dao([vec![], mock_record_query_results(true)]);

    let record_map = assert_ok!(record_dao.get_records("dst1", None, true, true).await);

    assert_eq!(
      record_map,
      [
        (
          "rec1".to_owned(),
          RecordSO {
            id: "rec1".to_owned(),
            comment_count: 0,
            data: json!( { "fld1": 123 } ),
            created_at: Some(1676945874561),
            updated_at: Some(1676945875561),
            revision_history: Some(vec![0, 3, 7, 8]),
            record_meta: None,
            ..Default::default()
          }
        ),
        (
          "rec2".to_owned(),
          RecordSO {
            id: "rec2".to_owned(),
            comment_count: 0,
            data: json!( { "fld1": 889 } ),
            created_at: Some(1676945874561),
            updated_at: Some(1676945875562),
            revision_history: Some(vec![4]),
            record_meta: Some(RecordMeta {
              field_updated_map: None,
              created_by: None,
              updated_by: None,
              created_at: Some(1676945874561),
              updated_at: None,
              ..Default::default()
            }),
            ..Default::default()
          }
        ),
        (
          "rec3".to_owned(),
          RecordSO {
            id: "rec3".to_owned(),
            comment_count: 0,
            data: json!({}),
            created_at: Some(1676945874561),
            updated_at: None,
            revision_history: None,
            record_meta: None,
            ..Default::default()
          }
        ),
      ]
      .into_iter()
      .collect::<HashMap<_, _>>()
    );

    assert_eq!(
      repo.take_logs().await,
      [
        MockSqlLog {
          sql: MOCK_RECORD_COMMENT_QUERY_SQL.into(),
          params: params! {
            "dst_id" => "dst1"
          },
        },
        MockSqlLog {
          sql: MOCK_RECORD_WITHOUT_RECORD_IDS_QUERY_SQL.into(),
          params: Params::Positional(vec!["dst1".into(), true.into()])
        },
      ]
    );
  }
}
