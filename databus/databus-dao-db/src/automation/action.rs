use crate::db_manager::DBManager;
use crate::model::{AutomationActionIntroductionPO, AutomationActionPO};
use anyhow::Context;
use async_trait::async_trait;
use databus_core::utils::uuid::IdUtil;
use databus_shared::prelude::SqlExt;

use futures::TryStreamExt;

use databus_core::utils::utils::generate_u64_id;
use mysql_common::params::Params;
use mysql_common::{params, Value};
use std::collections::HashMap;
use std::sync::Arc;

#[async_trait]
pub trait AutomationActionDAO: Send + Sync {
  async fn get_introduction_by_robot_ids(
    &self,
    robot_ids: Vec<String>,
  ) -> anyhow::Result<Vec<AutomationActionIntroductionPO>>;

  async fn get_by_robot_id(&self, robot_id: &str) -> anyhow::Result<Vec<AutomationActionPO>>;

  async fn insert_or_update(
    &self,
    user_id: &u64,
    robot_id: &str,
    action_id: Option<String>,
    action_type_id: Option<String>,
    prev_action_id: Option<String>,
    input: Option<String>,
    is_deleted: Option<bool>,
  );

  async fn get_action_id_by_robot_id_and_pre_action_id(
    &self,
    robot_id: &str,
    prev_action_id: String,
  ) -> anyhow::Result<String>;

  async fn get_prev_action_id_by_robot_id_and_action_id(
    &self,
    robot_id: &str,
    action_id: String,
  ) -> anyhow::Result<String>;
}

struct AutomationActionDAOImpl {
  repo: Arc<dyn DBManager>,
}

pub fn new_dao(repo: Arc<dyn DBManager>) -> Arc<dyn AutomationActionDAO> {
  Arc::new(AutomationActionDAOImpl { repo })
}

#[async_trait]
impl AutomationActionDAO for AutomationActionDAOImpl {
  async fn get_introduction_by_robot_ids(
    &self,
    robot_ids: Vec<String>,
  ) -> anyhow::Result<Vec<AutomationActionIntroductionPO>> {
    let mut client = self.repo.get_client().await?;
    let mut query = format!(
      "
      SELECT action_id, action_type_id, prev_action_id, robot_id \
      FROM `{}automation_action` \
      WHERE is_deleted = 0 \
      AND `robot_id`",
      self.repo.table_prefix()
    );
    query = query.append_in_condition(robot_ids.len());
    let results: Vec<AutomationActionIntroductionPO> = client
      .query_all(query, {
        let mut values: Vec<Value> = Vec::new();
        values.extend(robot_ids.iter().map(Value::from));
        Params::Positional(values)
      })
      .await?
      .try_collect()
      .await
      .with_context(|| "get robots actions")?;
    Ok(results)
  }

  async fn get_by_robot_id(&self, robot_id: &str) -> anyhow::Result<Vec<AutomationActionPO>> {
    let mut client = self.repo.get_client().await?;
    let query = format!(
      "
      SELECT robot_id, action_id, action_type_id, prev_action_id, input \
      FROM `{}automation_action` \
      WHERE robot_id = :robot_id \
      AND is_deleted = 0 \
      ORDER BY created_at asc \
      ",
      self.repo.table_prefix()
    );
    let results: Vec<AutomationActionPO> = client
      .query_all(
        query,
        params! {
         robot_id,
        },
      )
      .await?
      .try_collect()
      .await
      .with_context(|| "Get robot actions")?;
    Ok(results)
  }
  async fn insert_or_update(
    &self,
    user_id: &u64,
    robot_id: &str,
    action_id: Option<String>,
    action_type_id: Option<String>,
    prev_action_id: Option<String>,
    input: Option<String>,
    is_deleted: Option<bool>,
  ) {
    let mut client = self.repo.get_client().await.unwrap();
    // init param
    let mut params: HashMap<Vec<u8>, Value> = HashMap::new();
    params.insert(Vec::from("user_id"), Value::from(user_id));
    params.insert(Vec::from("robot_id"), Value::from(robot_id));
    // init execution
    let update_robot_sql = format!(
      " UPDATE {}automation_robot SET updated_by = :user_id, updated_at = CURRENT_TIMESTAMP() WHERE `robot_id` = :robot_id AND is_deleted = 0",
      self.repo.table_prefix(),
    );
    let mut execution = vec![(update_robot_sql, Params::Named(params.clone()))];
    // format update sql
    if action_id.is_some() {
      let mut update_sql = format!(
        "UPDATE {}automation_action SET updated_by = :user_id",
        self.repo.table_prefix()
      );
      params.insert(Vec::from("action_id"), Value::from(action_id.clone().unwrap()));
      if action_type_id.is_some() {
        update_sql.push_str(", action_type_id = :action_type_id");
        params.insert(Vec::from("action_type_id"), Value::from(action_type_id));
      }
      if prev_action_id.is_some() {
        update_sql.push_str(", prev_action_id = :prev_action_id");
        params.insert(Vec::from("prev_action_id"), Value::from(prev_action_id.clone()));
      }
      if input.is_some() {
        update_sql.push_str(", input = :input");
        params.insert(Vec::from("input"), Value::from(input));
      }
      if is_deleted.is_some() {
        update_sql.push_str(", is_deleted = :is_deleted");
        params.insert(Vec::from("is_deleted"), Value::from(is_deleted));
        // change next action's prev_action_id to current prev_action_id
        let current_next_action_id = self
          .get_action_id_by_robot_id_and_pre_action_id(robot_id, action_id.clone().unwrap())
          .await
          .ok();
        if current_next_action_id.is_some() {
          let current_prev_action_id = self
            .get_prev_action_id_by_robot_id_and_action_id(robot_id, action_id.clone().unwrap())
            .await
            .ok();
          let update_next_action_prev_sql = format!(
            "UPDATE {}automation_action SET prev_action_id = :current_prev_action_id, updated_by = :user_id, updated_at = CURRENT_TIMESTAMP() \
            WHERE `action_id` = :next_action_id AND is_deleted = 0",
            self.repo.table_prefix());
          params.insert(Vec::from("next_action_id"), Value::from(current_next_action_id));
          params.insert(Vec::from("current_prev_action_id"), Value::from(current_prev_action_id));
          execution.push((update_next_action_prev_sql, Params::Named(params.clone())))
        }
      }
      update_sql.push_str(" WHERE action_id = :action_id AND is_deleted = 0");
      execution.push((update_sql, Params::Named(params.clone())))
    } else {
      // format insert sql
      let insert_action_sql = format!(
        "INSERT INTO {}automation_action (id, robot_id, action_type_id, action_id, prev_action_id, input, created_by, updated_by) \
          VALUES (:id, :robot_id, :action_type_id, :action_id, :prev_action_id, :input, :user_id, :user_id)",
        self.repo.table_prefix(),
      );
      params.insert(Vec::from("action_type_id"), Value::from(action_type_id));
      params.insert(Vec::from("prev_action_id"), Value::from(prev_action_id.clone()));
      params.insert(Vec::from("input"), Value::from(input));
      params.insert(
        Vec::from("action_id"),
        Value::from(IdUtil::create_automation_action_id()),
      );
      let id = generate_u64_id();
      params.insert(Vec::from("id"), Value::from(id));
      execution.push((insert_action_sql, Params::Named(params.clone())))
    }
    // change action's order when created or updated
    if prev_action_id.is_some() {
      let next_action_id = self
        .get_action_id_by_robot_id_and_pre_action_id(robot_id, prev_action_id.clone().unwrap())
        .await
        .ok();
      if next_action_id.is_some() {
        let update_next_action_sql = format!(
          "UPDATE {}automation_action SET prev_action_id = :action_id, updated_by = :user_id, updated_at = CURRENT_TIMESTAMP() \
          WHERE `action_id` = :next_action_id AND is_deleted = 0",
          self.repo.table_prefix());
        params.insert(Vec::from("next_action_id"), Value::from(next_action_id.unwrap()));
        execution.push((update_next_action_sql, Params::Named(params.clone())))
      }
    }
    client.execute_with_transaction(execution).await.unwrap()
  }

  async fn get_action_id_by_robot_id_and_pre_action_id(
    &self,
    robot_id: &str,
    prev_action_id: String,
  ) -> anyhow::Result<String> {
    let mut client = self.repo.get_client().await?;
    let query = format!(
      "
      SELECT action_id \
      FROM `{}automation_action` \
      WHERE robot_id = :robot_id \
      AND prev_action_id = :prev_action_id \
      AND is_deleted = 0 \
      ORDER BY created_at asc \
      ",
      self.repo.table_prefix()
    );
    Ok(
      client
        .query_one(query, params! {robot_id, prev_action_id})
        .await?
        .with_context(|| "Get action id")?,
    )
  }

  async fn get_prev_action_id_by_robot_id_and_action_id(
    &self,
    robot_id: &str,
    action_id: String,
  ) -> anyhow::Result<String> {
    let mut client = self.repo.get_client().await?;
    let query = format!(
      "
      SELECT prev_action_id \
      FROM `{}automation_action` \
      WHERE robot_id = :robot_id \
      AND action_id = :action_id \
      AND is_deleted = 0 \
      ORDER BY created_at asc \
      ",
      self.repo.table_prefix()
    );
    let result = client
      .query_one(query, params! {robot_id, action_id})
      .await?
      .map_or(None, |prev_action_id: Option<String>| prev_action_id)
      .with_context(|| "Get prev action id")?;
    Ok(result)
  }
}
