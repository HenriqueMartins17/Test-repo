use std::collections::HashMap;

use std::sync::Arc;

use anyhow::Context;
use async_trait::async_trait;
use futures::TryStreamExt;

use crate::db_manager::DBManager;
use crate::model::{AutomationRobotIntroductionPO, AutomationRobotPO, CreateAutomationRobotSO};
use databus_core::utils::utils::generate_u64_id;
use databus_core::utils::uuid::IdUtil;
use mysql_common::params::Params;
use mysql_common::{params, Value};

#[async_trait]
pub trait AutomationRobotDAO: Send + Sync {
  async fn get_introduction_by_resource_id(
    &self,
    resource_id: &str,
  ) -> anyhow::Result<Vec<AutomationRobotIntroductionPO>>;

  async fn get_introduction_by_robot_id(&self, resource_id: &str) -> anyhow::Result<AutomationRobotPO>;
  async fn update_by_robot_id(
    &self,
    robot_id: &str,
    updated_by: &u64,
    name: Option<String>,
    description: Option<String>,
    props: Option<String>,
    is_active: Option<bool>,
    is_deleted: Option<bool>,
  );

  async fn create(&self, robots: Vec<CreateAutomationRobotSO>);

  async fn is_robot_exist(&self, robot_id: &str) -> anyhow::Result<bool>;

  async fn is_resources_has_robots(&self, resource_ids: Vec<String>) -> anyhow::Result<bool>;

  async fn get_active_count_by_robot_ids(&self, robot_ids: Vec<String>) -> anyhow::Result<i64>;
}

struct AutomationRobotDAOImpl {
  repo: Arc<dyn DBManager>,
}
pub fn new_dao(repo: Arc<dyn DBManager>) -> Arc<dyn AutomationRobotDAO> {
  Arc::new(AutomationRobotDAOImpl { repo })
}

#[async_trait]
impl AutomationRobotDAO for AutomationRobotDAOImpl {
  async fn get_introduction_by_resource_id(
    &self,
    resource_id: &str,
  ) -> anyhow::Result<Vec<AutomationRobotIntroductionPO>> {
    let mut client = self.repo.get_client().await?;
    let query = format!(
      "
      SELECT robot_id, resource_id, name, description, is_active, props \
      FROM `{}automation_robot` \
      WHERE `resource_id` = ? \
      AND is_deleted = 0",
      self.repo.table_prefix()
    );
    let results: Vec<AutomationRobotIntroductionPO> = client
      .query_all(query, Params::Positional(vec![resource_id.into()]))
      .await?
      .try_collect::<Vec<AutomationRobotIntroductionPO>>()
      .await
      .with_context(|| format!("Collect robot_ids of {resource_id}"))?;
    Ok(results)
  }

  async fn get_introduction_by_robot_id(&self, robot_id: &str) -> anyhow::Result<AutomationRobotPO> {
    let mut client = self.repo.get_client().await?;
    let query = format!(
      "
      SELECT robot_id, resource_id, name, description, props, is_active, updated_by, updated_at \
      FROM `{}automation_robot` \
      WHERE `robot_id` = :robot_id \
      AND is_deleted = 0",
      self.repo.table_prefix()
    );
    let result: AutomationRobotPO = client
      .query_one(query, params! {robot_id})
      .await?
      .with_context(|| format!("Robot information of {robot_id}"))?;
    Ok(result)
  }

  async fn update_by_robot_id(
    &self,
    robot_id: &str,
    updated_by: &u64,
    name: Option<String>,
    description: Option<String>,
    props: Option<String>,
    is_active: Option<bool>,
    is_deleted: Option<bool>,
  ) {
    let mut client = self.repo.get_client().await.unwrap();
    let mut sql = format!(
      "
      UPDATE {}automation_robot \
      SET updated_by = :updated_by",
      self.repo.table_prefix(),
    );
    let mut params: HashMap<Vec<u8>, Value> = HashMap::new();
    params.insert(Vec::from("updated_by"), Value::from(updated_by));
    if name.is_some() {
      sql.push_str(", name = :name");
      params.insert(Vec::from("name"), Value::from(name));
    }
    if description.is_some() {
      sql.push_str(", description = :description");
      params.insert(Vec::from("description"), Value::from(description));
    }
    if props.is_some() {
      sql.push_str(", props = :props");
      params.insert(Vec::from("props"), Value::from(props));
    }
    if is_active.is_some() {
      sql.push_str(", is_active = :is_active");
      params.insert(Vec::from("is_active"), Value::from(is_active));
    }
    if is_deleted.is_some() {
      sql.push_str(", is_deleted = :is_deleted");
      params.insert(Vec::from("is_deleted"), Value::from(is_deleted));
    }
    sql.push_str(" WHERE robot_id = :robot_id AND is_deleted = 0");
    params.insert(Vec::from("robot_id"), Value::from(robot_id));
    client.execute(sql, Params::Named(params)).await.unwrap()
  }

  async fn create(&self, robots: Vec<CreateAutomationRobotSO>) {
    let mut client = self.repo.get_client().await.unwrap();
    let mut execution = vec![];
    for robot in robots {
      let robot_id = IdUtil::create_automation_robot_id();
      execution.push(self.get_insert_robot_sql(
        robot.created_by.clone(),
        robot.is_active,
        robot_id.clone(),
        robot.resource_id,
        robot.name,
        robot.props,
      ));
      if robot.triggers.is_some() {
        let mut prev_trigger_id = None;
        for trigger in robot.triggers.unwrap() {
          let trigger_id = IdUtil::create_automation_trigger_id();
          execution.push(self.get_insert_robot_trigger_sql(
            robot.created_by.clone(),
            robot_id.clone(),
            trigger_id.clone(),
            trigger.trigger_type_id,
            prev_trigger_id,
            trigger.resource_id,
            trigger.input,
          ));
          prev_trigger_id = Some(trigger_id.clone());
        }
      }
      if robot.actions.is_some() {
        let mut prev_action_id = None;
        for action in robot.actions.unwrap() {
          let action_id = IdUtil::create_automation_action_id();
          execution.push(self.get_insert_robot_action_sql(
            robot.created_by.clone(),
            robot_id.clone(),
            action_id.clone(),
            action.action_type_id,
            prev_action_id,
            action.input,
          ));
          prev_action_id = Some(action_id.clone());
        }
      }
    }
    client.execute_with_transaction(execution).await.unwrap();
  }

  async fn is_robot_exist(&self, robot_id: &str) -> anyhow::Result<bool> {
    let mut client = self.repo.get_client().await.unwrap();
    let query = format!(
      "
      SELECT COUNT(*) as count \
      FROM {}automation_robot \
      WHERE robot_id = :robot_id \
      AND is_deleted = 0",
      self.repo.table_prefix()
    );
    let count: i64 = client
      .query_one(query, params! {robot_id})
      .await?
      .with_context(|| format!("get count in a month {robot_id}"))
      .map_or(0, |count: i64| count);
    if count > 0 {
      return Ok(true);
    }
    return Ok(false);
  }

  async fn is_resources_has_robots(&self, resource_ids: Vec<String>) -> anyhow::Result<bool> {
    let mut client = self.repo.get_client().await.unwrap();
    let ids = resource_ids.join(",");
    let query = format!(
      "
      SELECT COUNT(*) as count \
      FROM {}automation_robot \
      WHERE resource_id IN (:ids)\
      AND is_deleted = 0 AND is_active = 1",
      self.repo.table_prefix()
    );
    let count: i64 = client
      .query_one(query, params! {ids})
      .await?
      .with_context(|| format!("is_resources_has_robots err {resource_ids:?}"))
      .map_or(0, |count: i64| count);
    if count > 0 {
      return Ok(true);
    }
    return Ok(false);
  }

  async fn get_active_count_by_robot_ids(&self, robot_ids: Vec<String>) -> anyhow::Result<i64> {
    let mut client = self.repo.get_client().await.unwrap();
    let ids = robot_ids.join(",");
    let query = format!(
      "
      SELECT COUNT(*) as count \
      FROM {}automation_robot \
      WHERE robot_id IN (:ids)\
      AND is_deleted = 0 AND is_active = 1",
      self.repo.table_prefix()
    );
    let count: i64 = client
      .query_one(query, params! {ids})
      .await?
      .with_context(|| format!("get_active_count_by_robot_ids err {robot_ids:?}"))
      .map_or(0, |count: i64| count);
    Ok(count)
  }
}

impl AutomationRobotDAOImpl {
  fn get_insert_robot_sql(
    &self,
    user_id: u64,
    is_active: u8,
    robot_id: String,
    resource_id: String,
    name: String,
    props: Option<String>,
  ) -> (String, Params) {
    let mut robot_params: HashMap<Vec<u8>, Value> = HashMap::new();
    robot_params.insert(Vec::from("created_by"), Value::from(user_id));
    robot_params.insert(Vec::from("robot_id"), Value::from(robot_id));
    robot_params.insert(Vec::from("name"), Value::from(name));
    robot_params.insert(Vec::from("is_active"), Value::from(is_active));
    robot_params.insert(Vec::from("resource_id"), Value::from(resource_id));
    robot_params.insert(Vec::from("id"), Value::from(generate_u64_id()));
    robot_params.insert(Vec::from("props"), Value::from(props));
    let robot_sql = format!(
      "INSERT INTO {}automation_robot (id, resource_id, robot_id, name, props, is_active, created_by, updated_by) \
      VALUES (:id, :resource_id, :robot_id, :name, :props, :is_active, :created_by, :created_by)",
      self.repo.table_prefix(),
    );
    return (robot_sql, Params::Named(robot_params));
  }

  fn get_insert_robot_trigger_sql(
    &self,
    user_id: u64,
    robot_id: String,
    trigger_id: String,
    trigger_type_id: String,
    prev_trigger_id: Option<String>,
    resource_id: Option<String>,
    input: Option<String>,
  ) -> (String, Params) {
    let mut trigger_params: HashMap<Vec<u8>, Value> = HashMap::new();
    trigger_params.insert(Vec::from("id"), Value::from(generate_u64_id()));
    trigger_params.insert(Vec::from("robot_id"), Value::from(robot_id));
    trigger_params.insert(Vec::from("trigger_type_id"), Value::from(trigger_type_id));
    trigger_params.insert(Vec::from("trigger_id"), Value::from(trigger_id));
    trigger_params.insert(Vec::from("prev_trigger_id"), Value::from(prev_trigger_id));
    trigger_params.insert(Vec::from("resource_id"), Value::from(resource_id));
    trigger_params.insert(Vec::from("input"), Value::from(input));
    trigger_params.insert(Vec::from("created_by"), Value::from(user_id));
    let trigger_sql = format!(
      "INSERT INTO {}automation_trigger (id, robot_id, trigger_type_id, trigger_id, prev_trigger_id, resource_id, input, created_by, updated_by) \
      VALUES  (:id, :robot_id, :trigger_type_id, :trigger_id, :prev_trigger_id, :resource_id, :input, :created_by, :created_by)",
      self.repo.table_prefix(),
    );
    return (trigger_sql, Params::Named(trigger_params));
  }

  fn get_insert_robot_action_sql(
    &self,
    user_id: u64,
    robot_id: String,
    action_id: String,
    action_type_id: String,
    prev_action_id: Option<String>,
    input: Option<String>,
  ) -> (String, Params) {
    let mut action_params: HashMap<Vec<u8>, Value> = HashMap::new();
    action_params.insert(Vec::from("id"), Value::from(generate_u64_id()));
    action_params.insert(Vec::from("robot_id"), Value::from(robot_id));
    action_params.insert(Vec::from("action_type_id"), Value::from(action_type_id));
    action_params.insert(Vec::from("action_id"), Value::from(action_id));
    action_params.insert(Vec::from("prev_action_id"), Value::from(prev_action_id));
    action_params.insert(Vec::from("input"), Value::from(input));
    action_params.insert(Vec::from("created_by"), Value::from(user_id));
    let action_sql = format!(
      "INSERT INTO {}automation_action (id, robot_id, action_type_id, action_id, prev_action_id, input, created_by, updated_by) \
      VALUES  (:id, :robot_id, :action_type_id, :action_id, :prev_action_id, :input, :created_by, :created_by)",
      self.repo.table_prefix(),
    );
    return (action_sql, Params::Named(action_params));
  }
}
