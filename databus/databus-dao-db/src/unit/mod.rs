// pub mod entities;
use databus_core::prelude::UnitSO;

mod unit_po;
use crate::DBManager;
use anyhow::Context;
use async_trait::async_trait;
use databus_shared::prelude::HashSet;
use databus_shared::prelude::SqlExt;
use futures::TryStreamExt;
use mysql_async::{Params, Value};
use std::sync::Arc;
pub use unit_po::UnitPO;

#[async_trait]
pub trait UnitDAO: Send + Sync {
  async fn get_unit_info_by_unit_ids(&self, space_id: &str, unit_ids: HashSet<String>) -> anyhow::Result<Vec<UnitSO>>;
}

struct UnitDAOImpl {
  oss_host: String,
  repo: Arc<dyn DBManager>,
}

pub fn new_dao(oss_host: String, repo: Arc<dyn DBManager>) -> Arc<dyn UnitDAO> {
  Arc::new(UnitDAOImpl { oss_host, repo })
}

#[async_trait]
impl UnitDAO for UnitDAOImpl {
  async fn get_unit_info_by_unit_ids(&self, space_id: &str, unit_ids: HashSet<String>) -> anyhow::Result<Vec<UnitSO>> {
    if unit_ids.is_empty() {
      return Ok(vec![]);
    }

    let mut client = self.repo.get_client().await?;

    let mut units: Vec<UnitPO> = client
      .query_all(
        format!(
          // TODO remove dummy original_unit_id column after mysql_common allows default value for missing columns.
          "\
          SELECT \
            vu.id unit_id, \
            vu.unit_type type, \
            COALESCE(vut.team_name, vum.member_name, vur.role_name) name, \
            u.uuid uuid, \
            u.uuid user_id, \
            u.avatar avatar, \
            vum.is_active is_active, \
            vu.is_deleted is_deleted, \
            u.nick_name nick_name, \
            u.color avatar_color, \
            IFNULL(vum.is_social_name_modified, 2) > 0 AS is_member_name_modified, \
            NULL AS is_nick_name_modified, \
            vu.unit_id original_unit_id \
          FROM {prefix}unit vu \
          LEFT JOIN {prefix}unit_team vut ON vu.unit_ref_id = vut.id \
          LEFT JOIN {prefix}unit_member vum ON vu.unit_ref_id = vum.id \
          LEFT JOIN {prefix}unit_role vur ON vu.unit_ref_id = vur.id \
          LEFT JOIN {prefix}user u ON vum.user_id = u.id \
          WHERE vu.space_id = ? AND vu.id\
          ",
          prefix = self.repo.table_prefix()
        )
        .append_in_condition(unit_ids.len()),
        {
          let mut values: Vec<Value> = vec![space_id.into()];
          values.extend(unit_ids.into_iter().map(Value::from));
          Params::Positional(values)
        },
      )
      .await?
      .try_collect()
      .await
      .with_context(|| format!("get unit info by unit ids, space id {space_id}"))?;

    let mut units_infos: Vec<UnitSO> = Vec::new();
    for unit in &mut units {
      if let Some(avatar) = &unit.avatar {
        if !avatar.starts_with("http") {
          unit.avatar = Some(format!("{}/{avatar}", self.oss_host));
        }
      }
      unit.is_member_name_modified = unit.is_member_name_modified.or(Some(false));
      units_infos.push(unit.to_vo());
    }

    Ok(units_infos)
  }
}

#[cfg(test)]
pub mod mock {
  use std::collections::HashMap;
  use super::*;

  #[derive(Default)]
  pub struct MockUnitDAOImpl {
    units: HashMap<&'static str, UnitSO>,
  }

  impl MockUnitDAOImpl {
    pub fn new() -> Self {
      Self::default()
    }

    pub fn with_units(mut self, units: HashMap<&'static str, UnitSO>) -> Self {
      self.units = units;
      self
    }

    pub fn build(self) -> Arc<dyn UnitDAO> {
      Arc::new(self)
    }
  }

  #[async_trait]
  impl UnitDAO for MockUnitDAOImpl {
    async fn get_unit_info_by_unit_ids(
      &self,
      _space_id: &str,
      unit_ids: HashSet<String>,
    ) -> anyhow::Result<Vec<UnitSO>> {
      Ok(
        unit_ids
          .iter()
          .filter_map(|unit_id| self.units.get(unit_id.as_str()))
          .cloned()
          .collect(),
      )
    }
  }
}

#[cfg(test)]
mod tests {
  use super::*;
  use crate::mock::{mock_rows, MockRepositoryImpl, MockSqlLog};
  use mysql_async::consts::ColumnType;
  use mysql_async::Row;
  use pretty_assertions::assert_eq;
  use tokio_test::assert_ok;

  fn mock_dao<I>(results: I) -> (Arc<dyn DBManager>, Arc<dyn UnitDAO>)
  where
    I: IntoIterator<Item = Vec<Row>>,
  {
    let repo = MockRepositoryImpl::new(results);
    (repo.clone(), new_dao("https://mock.com".into(), repo))
  }

  // TODO remove dummy original_unit_id column after mysql_common allows default value for missing columns.
  const MOCK_UNIT_INFO_QUERY_SQL: &str = "\
    SELECT \
      vu.id unit_id, \
      vu.unit_type type, \
      COALESCE(vut.team_name, vum.member_name, vur.role_name) name, \
      u.uuid uuid, \
      u.uuid user_id, \
      u.avatar avatar, \
      vum.is_active is_active, \
      vu.is_deleted is_deleted, \
      u.nick_name nick_name, \
      u.color avatar_color, \
      IFNULL(vum.is_social_name_modified, 2) > 0 AS is_member_name_modified, \
      NULL AS is_nick_name_modified, \
      vu.unit_id original_unit_id \
    FROM apitable_unit vu \
    LEFT JOIN apitable_unit_team vut ON vu.unit_ref_id = vut.id \
    LEFT JOIN apitable_unit_member vum ON vu.unit_ref_id = vum.id \
    LEFT JOIN apitable_unit_role vur ON vu.unit_ref_id = vur.id \
    LEFT JOIN apitable_user u ON vum.user_id = u.id \
    WHERE vu.space_id = ? AND vu.id IN (?)\
    ";

  fn mock_columns() -> Vec<(&'static str, ColumnType)> {
    vec![
      ("unit_id", ColumnType::MYSQL_TYPE_LONG),
      ("type", ColumnType::MYSQL_TYPE_TINY),
      ("name", ColumnType::MYSQL_TYPE_VARCHAR),
      ("uuid", ColumnType::MYSQL_TYPE_VARCHAR),
      ("user_id", ColumnType::MYSQL_TYPE_VARCHAR),
      ("avatar", ColumnType::MYSQL_TYPE_VARCHAR),
      ("is_active", ColumnType::MYSQL_TYPE_BIT),
      ("is_deleted", ColumnType::MYSQL_TYPE_BIT),
      ("nick_name", ColumnType::MYSQL_TYPE_INT24),
      ("avatar_color", ColumnType::MYSQL_TYPE_TINY),
      ("is_member_name_modified", ColumnType::MYSQL_TYPE_BIT),
      ("is_nick_name_modified", ColumnType::MYSQL_TYPE_BIT),
      ("original_unit_id", ColumnType::MYSQL_TYPE_VARCHAR),
    ]
  }

  #[tokio::test]
  async fn get_one_unit_info() {
    let (repo, unit_dao) = mock_dao([mock_rows(
      mock_columns(),
      [[
        4675354i64.into(),
        1u8.into(),
        "mock user".into(),
        "1749124".into(),
        "1749124".into(),
        "https://example.com/avatar.png".into(),
        true.into(),
        Value::NULL,
        "MockUser".into(),
        Value::NULL,
        false.into(),
        Value::NULL,
        "abcdef".into(),
      ]],
    )]);

    let unit_info = assert_ok!(
      unit_dao
        .get_unit_info_by_unit_ids("spc1", hashset!("4675354".to_owned()))
        .await
    );

    assert_eq!(
      unit_info,
      vec![UnitSO {
        unit_id: Some("4675354".to_string()),
        r#type: Some(1),
        name: Some("mock user".into()),
        uuid: Some("1749124".into()),
        user_id: Some("1749124".into()),
        avatar: Some("https://example.com/avatar.png".to_owned()),
        is_active: Some(1),
        is_deleted: None,
        nick_name: Some("MockUser".to_owned()),
        avatar_color: None,
        is_member_name_modified: Some(false),
        is_nick_name_modified: None,
        original_unit_id: Some("abcdef".into()),
      }]
    );

    assert_eq!(
      repo.take_logs().await,
      [MockSqlLog {
        sql: MOCK_UNIT_INFO_QUERY_SQL.into(),
        params: Params::Positional(vec!["spc1".into(), "4675354".into()])
      }]
    );
  }

  #[tokio::test]
  async fn unit_info_avatar_no_host() {
    let (repo, unit_dao) = mock_dao([mock_rows(
      mock_columns(),
      [[
        4675354i64.into(),
        Value::NULL,
        "mock user".into(),
        "1749124".into(),
        "1749124".into(),
        "avatar.png".into(),
        true.into(),
        false.into(),
        "MockUser".into(),
        3i32.into(),
        true.into(),
        Value::NULL,
        "abcdef".into(),
      ]],
    )]);

    let unit_info = assert_ok!(
      unit_dao
        .get_unit_info_by_unit_ids("spc1", hashset!("4675354".to_owned()))
        .await
    );

    assert_eq!(
      unit_info,
      vec![UnitSO {
        unit_id: Some("4675354".to_string()),
        r#type: None,
        name: Some("mock user".into()),
        uuid: Some("1749124".into()),
        user_id: Some("1749124".into()),
        avatar: Some("https://mock.com/avatar.png".to_owned()),
        is_active: Some(1),
        is_deleted: Some(0),
        nick_name: Some("MockUser".to_owned()),
        avatar_color: Some(3),
        is_member_name_modified: Some(true),
        is_nick_name_modified: None,
        original_unit_id: Some("abcdef".into()),
      }]
    );

    assert_eq!(
      repo.take_logs().await,
      [MockSqlLog {
        sql: MOCK_UNIT_INFO_QUERY_SQL.into(),
        params: Params::Positional(vec!["spc1".into(), "4675354".into()])
      }]
    );
  }
}
