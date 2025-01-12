use std::collections::HashMap;
use std::sync::Arc;

use anyhow::Context;
use async_trait::async_trait;
use databus_core::utils::utils::generate_u64_id;
use futures::TryStreamExt;
use mysql_common::frunk::labelled::chars::N;
use mysql_common::params::Params;
use mysql_common::{params, Value};
use serde_json::json;

use databus_shared::prelude::{Json, SqlExt};

use crate::db_manager::DBManager;
use crate::model::{AutomationTriggerIntroductionPO, AutomationTriggerPO, ResourceRobotDto, TriggerPO, TriggerSO};
use crate::trigger_schedule::AutomationTriggerScheduleDAO;

#[async_trait]
pub trait AutomationTriggerDAO: Send + Sync {
  async fn create(
    &self,
    user_id: &u64,
    robot_id: &str,
    trigger_id: &str,
    trigger_type_id: String,
    resource_id: String,
    space_id: Option<String>,
    prev_trigger_id: Option<String>,
    input: Option<String>,
    schedule_conf: Option<String>,
    schedule_id: Option<u64>,
  );

  async fn update(
    &self,
    user_id: &u64,
    robot_id: &str,
    trigger_id: &str,
    trigger_type_id: Option<String>,
    resource_id: Option<String>,
    space_id: Option<String>,
    prev_trigger_id: Option<String>,
    input: Option<String>,
    schedule_conf: Option<String>,
    is_deleted: Option<bool>,
  ) -> Option<u64>;

  async fn get_by_robot_ids(&self, robot_ids: Vec<String>) -> anyhow::Result<Vec<TriggerPO>>;
  async fn update_input_by_trigger_id(&self, user_id: &u64, trigger_id: String, input: Json);

  async fn update_trigger_type_id_by_trigger_id(&self, user_id: &u64, trigger_id: String, trigger_type_id: String);

  async fn get_trigger_by_robot_id_and_trigger_type_id(
    &self,
    robot_id: String,
    trigger_type_id: String,
  ) -> anyhow::Result<Vec<TriggerSO>>;

  async fn get_introduction_by_robot_ids(
    &self,
    robot_ids: Vec<String>,
  ) -> anyhow::Result<Vec<AutomationTriggerIntroductionPO>>;

  async fn get_by_robot_id(&self, robot_id: &str) -> anyhow::Result<Vec<AutomationTriggerPO>>;
  async fn get_by_robot_id_and_trigger_id(
    &self,
    robot_id: &str,
    trigger_id: String,
  ) -> anyhow::Result<Option<AutomationTriggerPO>>;

  async fn get_resource_ids_by_robot_id(&self, robot_id: &str) -> anyhow::Result<Vec<String>>;

  async fn get_count_by_robot_id(&self, robot_id: &str) -> anyhow::Result<i64>;

  async fn get_robot_id_and_resource_id_by_resource_ids(
    &self,
    resource_ids: Vec<String>,
  ) -> anyhow::Result<Vec<ResourceRobotDto>>;
}

struct AutomationTriggerDAOImpl {
  repo: Arc<dyn DBManager>,
  schedule_dao: Arc<dyn AutomationTriggerScheduleDAO>,
}

pub fn new_dao(
  repo: Arc<dyn DBManager>,
  schedule_dao: Arc<dyn AutomationTriggerScheduleDAO>,
) -> Arc<dyn AutomationTriggerDAO> {
  Arc::new(AutomationTriggerDAOImpl { repo, schedule_dao })
}

#[async_trait]
impl AutomationTriggerDAO for AutomationTriggerDAOImpl {
  async fn create(
    &self,
    user_id: &u64,
    robot_id: &str,
    trigger_id: &str,
    trigger_type_id: String,
    resource_id: String,
    space_id: Option<String>,
    prev_trigger_id: Option<String>,
    input: Option<String>,
    schedule_conf: Option<String>,
    schedule_id: Option<u64>,
  ) {
    let mut client = self.repo.get_client().await.unwrap();
    let id = generate_u64_id();
    let trigger_sql = format!(
      "INSERT INTO {}automation_trigger (id, robot_id, trigger_type_id, trigger_id, prev_trigger_id, resource_id, input, created_by, updated_by) \
      VALUES (:id, :robot_id, :trigger_type_id, :trigger_id, :prev_trigger_id, :resource_id, :input, :user_id, :user_id)",
      self.repo.table_prefix(),
    );
    let robot_sql = format!(
      " UPDATE {}automation_robot SET updated_by = :user_id, updated_at = CURRENT_TIMESTAMP() WHERE `robot_id` = :robot_id AND is_deleted = 0",
      self.repo.table_prefix(),
    );
    let mut execution = vec![
      (
        trigger_sql,
        params! {id, robot_id, trigger_type_id, trigger_id, prev_trigger_id, resource_id, input, user_id},
      ),
      (robot_sql, params! {user_id, robot_id}),
    ];
    if schedule_conf.is_some() {
      let trigger_schedule_sql = format!(
        "INSERT INTO {}automation_trigger_schedule (id, space_id, trigger_id, schedule_conf, created_by, updated_by) \
        VALUES (:schedule_id, :space_id, :trigger_id, :schedule_conf, :user_id, :user_id)",
        self.repo.table_prefix(),
      );
      execution.push((
        trigger_schedule_sql,
        params! {schedule_id, space_id, trigger_id, schedule_conf, user_id},
      ))
    }
    return client.execute_with_transaction(execution).await.unwrap();
  }

  async fn update(
    &self,
    user_id: &u64,
    robot_id: &str,
    trigger_id: &str,
    trigger_type_id: Option<String>,
    resource_id: Option<String>,
    space_id: Option<String>,
    prev_trigger_id: Option<String>,
    input: Option<String>,
    schedule_conf: Option<String>,
    is_deleted: Option<bool>,
  ) -> Option<u64> {
    let mut client = self.repo.get_client().await.unwrap();
    let mut trigger_sql = format!(
      "
      UPDATE {}automation_trigger \
      SET updated_by = :user_id",
      self.repo.table_prefix(),
    );
    let mut schedule_sql: Option<String> = None;
    let mut params: HashMap<Vec<u8>, Value> = HashMap::new();
    let mut schedule_id = None;
    params.insert(Vec::from("user_id"), Value::from(user_id));
    if trigger_type_id.is_some() {
      trigger_sql.push_str(", trigger_type_id = :trigger_type_id");
      params.insert(Vec::from("trigger_type_id"), Value::from(trigger_type_id));
    }
    if resource_id.is_some() {
      trigger_sql.push_str(", resource_id = :resource_id");
      params.insert(Vec::from("resource_id"), Value::from(resource_id));
    }
    if prev_trigger_id.is_some() {
      trigger_sql.push_str(", prev_trigger_id = :prev_trigger_id");
      params.insert(Vec::from("prev_trigger_id"), Value::from(prev_trigger_id));
    }
    if input.is_some() {
      trigger_sql.push_str(", input = :input");
      params.insert(Vec::from("input"), Value::from(input));
    }
    if is_deleted.is_some() {
      trigger_sql.push_str(", is_deleted = :is_deleted");
      params.insert(Vec::from("is_deleted"), Value::from(is_deleted));
      schedule_sql = Some(format!(
        "UPDATE {}automation_trigger_schedule SET is_deleted = :is_deleted, updated_by = :user_id WHERE `trigger_id` = :trigger_id AND is_deleted = 0",
        self.repo.table_prefix(),
      ));
    }
    if schedule_conf.is_some() {
      schedule_id = self.schedule_dao.get_id_by_trigger_id(trigger_id).await;
      if schedule_id.is_none() {
        // should create
        schedule_sql = Some(format!(
          "INSERT INTO {}automation_trigger_schedule (id, space_id, trigger_id, schedule_conf, created_by, updated_by) \
           VALUES (:schedule_id, :space_id, :trigger_id, :schedule_conf, :user_id, :user_id)",
          self.repo.table_prefix(),
        ));
        schedule_id = Some(generate_u64_id());
        params.insert(Vec::from("space_id"), Value::from(space_id));
        params.insert(Vec::from("schedule_id"), Value::from(schedule_id));
      } else {
        schedule_sql = Some(format!(
          "UPDATE {}automation_trigger_schedule SET schedule_conf = :schedule_conf, updated_by = :user_id WHERE `trigger_id` = :trigger_id AND is_deleted = 0",
          self.repo.table_prefix(),
        ));
      }
      params.insert(Vec::from("schedule_conf"), Value::from(schedule_conf));
    } else if is_deleted.is_none() {
      schedule_sql = Some(format!(
        "UPDATE {}automation_trigger_schedule SET schedule_conf = :schedule_conf, updated_by = :user_id, is_pushed = 0 WHERE `trigger_id` = :trigger_id AND is_deleted = 0",
        self.repo.table_prefix(),
      ));
      params.insert(Vec::from("schedule_conf"), Value::from(json!({})));
    }
    trigger_sql.push_str(" WHERE trigger_id = :trigger_id AND is_deleted = 0");
    params.insert(Vec::from("trigger_id"), Value::from(trigger_id));
    let robot_sql = format!(
      "UPDATE {}automation_robot SET updated_by = :user_id, updated_at = CURRENT_TIMESTAMP() WHERE `robot_id` = :robot_id AND is_deleted = 0",
      self.repo.table_prefix(),
    );
    let mut execution = vec![
      (trigger_sql, Params::Named(params.clone())),
      (robot_sql, params! {user_id, robot_id}),
    ];
    if schedule_sql.is_some() {
      execution.push((schedule_sql.unwrap(), Params::Named(params)))
    }
    client.execute_with_transaction(execution).await.unwrap();
    return schedule_id;
  }

  async fn get_by_robot_ids(&self, robot_ids: Vec<String>) -> anyhow::Result<Vec<TriggerPO>> {
    let mut client = self.repo.get_client().await?;
    let mut query = format!(
      "
      SELECT * \
      FROM `{}automation_trigger` \
      WHERE is_deleted = 0 \
      AND `robot_id`",
      self.repo.table_prefix()
    );
    query = query.append_in_condition(robot_ids.len());
    let results: Vec<TriggerPO> = client
      .query_all(query, {
        let mut values: Vec<Value> = Vec::new();
        values.extend(robot_ids.iter().map(Value::from));
        Params::Positional(values)
      })
      .await?
      .try_collect()
      .await
      .with_context(|| "get robots trigger")?;
    Ok(results)
  }

  async fn update_input_by_trigger_id(&self, user_id: &u64, trigger_id: String, input: Json) {
    let mut client = self.repo.get_client().await.unwrap();
    let sql = format!(
      "
      UPDATE {}automation_trigger \
      SET input = :input, updated_by = :user_id \
      WHERE `trigger_id` = :trigger_id \
      AND is_deleted = 0",
      self.repo.table_prefix(),
    );
    client.execute(sql, params! {trigger_id, input, user_id}).await.unwrap()
  }

  async fn update_trigger_type_id_by_trigger_id(&self, user_id: &u64, trigger_id: String, trigger_type_id: String) {
    let mut client = self.repo.get_client().await.unwrap();
    let sql = format!(
      "
      UPDATE {}automation_trigger \
      SET trigger_type_id = :trigger_type_id, updated_by = :user_id\
      WHERE `trigger_id` = :trigger_id",
      self.repo.table_prefix(),
    );
    client
      .execute(sql, params! {trigger_id, trigger_type_id, user_id})
      .await
      .unwrap()
  }

  async fn get_trigger_by_robot_id_and_trigger_type_id(
    &self,
    robot_id: String,
    trigger_type_id: String,
  ) -> anyhow::Result<Vec<TriggerSO>> {
    let mut client = self.repo.get_client().await?;
    let query = format!(
      "
      SELECT robot_id, trigger_id, trigger_type_id, input \
      FROM `{}automation_trigger` \
      WHERE robot_id = :robot_id \
      AND trigger_type_id = :trigger_type_id \
      AND is_deleted = 0 \
      ",
      self.repo.table_prefix()
    );
    let results: Vec<TriggerSO> = client
      .query_all(
        query,
        params! {
         robot_id, trigger_type_id,
        },
      )
      .await?
      .try_collect()
      .await
      .with_context(|| "get robot trigger")?;
    Ok(results)
  }

  async fn get_introduction_by_robot_ids(
    &self,
    robot_ids: Vec<String>,
  ) -> anyhow::Result<Vec<AutomationTriggerIntroductionPO>> {
    let mut client = self.repo.get_client().await?;
    let mut query = format!(
      "
      SELECT robot_id, trigger_id, trigger_type_id, prev_trigger_id \
      FROM `{}automation_trigger` \
      WHERE is_deleted = 0 \
      AND `robot_id`",
      self.repo.table_prefix()
    );
    query = query.append_in_condition(robot_ids.len());
    let results: Vec<AutomationTriggerIntroductionPO> = client
      .query_all(query, {
        let mut values: Vec<Value> = Vec::new();
        values.extend(robot_ids.iter().map(Value::from));
        Params::Positional(values)
      })
      .await?
      .try_collect()
      .await
      .with_context(|| "get robots trigger")?;
    Ok(results)
  }

  async fn get_by_robot_id(&self, robot_id: &str) -> anyhow::Result<Vec<AutomationTriggerPO>> {
    let mut client = self.repo.get_client().await?;
    let query = format!(
      "
      SELECT robot_id, trigger_id, resource_id, trigger_type_id, prev_trigger_id, input \
      FROM `{}automation_trigger` \
      WHERE robot_id = :robot_id \
      AND is_deleted = 0 \
      ORDER BY created_at asc \
      ",
      self.repo.table_prefix()
    );
    let results: Vec<AutomationTriggerPO> = client
      .query_all(
        query,
        params! {
         robot_id,
        },
      )
      .await?
      .try_collect()
      .await
      .with_context(|| "Get robot triggers")?;
    Ok(results)
  }

  async fn get_by_robot_id_and_trigger_id(
    &self,
    robot_id: &str,
    trigger_id: String,
  ) -> anyhow::Result<Option<AutomationTriggerPO>> {
    let mut client = self.repo.get_client().await?;
    let query = format!(
      "
      SELECT robot_id, trigger_id, resource_id, trigger_type_id, prev_trigger_id, input \
      FROM `{}automation_trigger` \
      WHERE robot_id = :robot_id \
      AND trigger_id = :trigger_id \
      AND is_deleted = 0 \
      ORDER BY created_at asc \
      ",
      self.repo.table_prefix()
    );
    return client.query_one(query, params! {robot_id,trigger_id}).await;
  }

  async fn get_resource_ids_by_robot_id(&self, robot_id: &str) -> anyhow::Result<Vec<String>> {
    let mut client = self.repo.get_client().await?;
    let query = format!(
      "
      SELECT resource_id \
      FROM `{}automation_trigger` \
      WHERE robot_id = :robot_id \
      AND is_deleted = 0 \
      ",
      self.repo.table_prefix()
    );
    let results: Vec<String> = client
      .query_all(
        query,
        params! {
         robot_id,
        },
      )
      .await?
      .try_collect()
      .await
      .with_context(|| "Get robot related node")?;
    Ok(results)
  }

  async fn get_count_by_robot_id(&self, robot_id: &str) -> anyhow::Result<i64> {
    let mut client = self.repo.get_client().await.unwrap();
    let query = format!(
      "
      SELECT COUNT(*) as count \
      FROM {}automation_trigger \
      WHERE robot_id = :robot_id \
      AND is_deleted = 0",
      self.repo.table_prefix()
    );
    let result: i64 = client
      .query_one(query, params! {robot_id})
      .await?
      .with_context(|| format!("get count in a month {robot_id}"))
      .map_or(0, |count: i64| count);
    Ok(result)
  }

  async fn get_robot_id_and_resource_id_by_resource_ids(
    &self,
    resource_ids: Vec<String>,
  ) -> anyhow::Result<Vec<ResourceRobotDto>> {
    let mut client = self.repo.get_client().await.unwrap();
    let ids = resource_ids.join(",");
    let query = format!(
      "
      SELECT robot_id, resource_id \
      FROM {}automation_trigger \
      WHERE resource_id IN (:ids)\
      AND input IS NOT NULL AND is_deleted = 0",
      self.repo.table_prefix()
    );
    let results: Vec<ResourceRobotDto> = client
      .query_all(query, params! {ids})
      .await?
      .try_collect()
      .await
      .with_context(|| "get robot trigger {resource_ids:?}")?;
    Ok(results)
  }
}
