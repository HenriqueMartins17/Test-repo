use std::fmt::Debug;
use std::sync::Arc;

use anyhow::Context;
use async_trait::async_trait;
use chrono::format::Fixed::{TimezoneName, TimezoneOffsetZ};
use chrono::{Datelike, FixedOffset, Local, NaiveDate, NaiveDateTime, NaiveTime, TimeZone, Utc};
use databus_core::utils::utils::generate_u64_id;
use futures::TryStreamExt;
use mysql_async::prelude::FromRow;
use mysql_async::Params;
use mysql_common::{params, Value};
use serde::{Deserialize, Serialize};

use databus_shared::prelude::Json;

use crate::db_manager::DBManager;
use crate::model::AutomationRunHistoryPO;

#[async_trait]
pub trait AutomationRunHistoryDAO: Send + Sync {
  async fn get_by_robot_id_with_pagination(
    &self,
    robot_id: &str,
    page_size: &u32,
    page_num: &u32,
    start_at: i64,
    end_at: i64,
  ) -> anyhow::Result<Vec<AutomationRunHistoryPO>>;
  async fn get_by_task_id(&self, task_id: &str) -> anyhow::Result<RunHistoryTaskSO>;
  /// Get automation robot running context.
  /// # params
  /// * `task_id` - The unique id of the serial action running.
  /// *  `action_id` - The action id of the robot, starting with `atr`, `acc`.
  async fn get_run_context_by_task_id_and_action_id(
    &self,
    task_id: &str,
    action_id: &str,
  ) -> anyhow::Result<RunContextSO>;
  async fn update_status_by_task_id(&self, task_id: &str, status: &u8);

  async fn create(
    &self,
    task_id: &str,
    robot_id: &str,
    space_id: &str,
    status: &u8,
    data: &Option<Json>,
  ) -> anyhow::Result<u64>;

  async fn get_count_between_start_and_end_by_robot_id(
    &self,
    robot_id: &str,
    start_at: i64,
    end_at: i64,
  ) -> anyhow::Result<i64>;

  async fn get_count_between_start_and_end_by_space_id(
    &self,
    space_id: &str,
    start_at: String,
    end_at: String,
  ) -> anyhow::Result<i64>;
}

struct AutomationRunHistoryDAOImpl {
  repo: Arc<dyn DBManager>,
}

pub fn new_dao(repo: Arc<dyn DBManager>) -> Arc<dyn AutomationRunHistoryDAO> {
  Arc::new(AutomationRunHistoryDAOImpl { repo })
}

#[async_trait]
impl AutomationRunHistoryDAO for AutomationRunHistoryDAOImpl {
  async fn get_by_robot_id_with_pagination(
    &self,
    robot_id: &str,
    skip: &u32,
    take: &u32,
    start_at: i64,
    end_at: i64,
  ) -> anyhow::Result<Vec<AutomationRunHistoryPO>> {
    let mut client = self.repo.get_client().await?;
    let query = format!(
      "
      SELECT task_id, robot_id, status, created_at, \
      JSON_KEYS(`data`->'$.nodeByIds') as action_ids, \
      JSON_EXTRACT(data ->'$.nodeByIds', '$**.typeId') as action_type_ids, \
      JSON_EXTRACT(data ->'$.nodeByIds', '$**.errorStacks') as error_stacks \
      FROM `{}automation_run_history` \
      WHERE `robot_id` = :robot_id \
      AND status IN ({:#?}, {:#?}, {:#?}, {:#?}) \
      AND `created_at` >= FROM_UNIXTIME(:start_at) \
      AND `created_at` < FROM_UNIXTIME(:end_at) \
      ORDER BY created_at DESC \
      LIMIT {}, {}
      ",
      self.repo.table_prefix(),
      RunHistoryStatus::Running as u8,
      RunHistoryStatus::Success as u8,
      RunHistoryStatus::Failure as u8,
      RunHistoryStatus::EXCESS as u8,
      skip,
      take,
    );
    let results: Vec<AutomationRunHistoryPO> = client
      .query_all(query, params! {robot_id, start_at, end_at})
      .await?
      .try_collect()
      .await
      .with_context(|| "get robot run history")?;
    Ok(results)
  }

  async fn get_by_task_id(&self, task_id: &str) -> anyhow::Result<RunHistoryTaskSO> {
    let mut client = self.repo.get_client().await?;
    let query = format!(
      "
      SELECT * \
      FROM `{}automation_run_history` \
      WHERE `task_id` = ?",
      self.repo.table_prefix(),
    );
    let result: RunHistoryTaskSO = client
      .query_one(query, Params::Positional(vec![task_id.into()]))
      .await?
      .with_context(|| "get robot run history task")?;
    Ok(result)
  }

  async fn get_run_context_by_task_id_and_action_id(
    &self,
    task_id: &str,
    action_id: &str,
  ) -> anyhow::Result<RunContextSO> {
    let mut client = self.repo.get_client().await?;
    let query = format!(
      "
      SELECT robot_id, task_id, status, \
        JSON_EXTRACT(data, CONCAT('$.nodeByIds.', :action_id, '.input')) as input, \
        JSON_EXTRACT(data, CONCAT('$.nodeByIds.', :action_id, '.output')) as output \
      FROM `{}automation_run_history` \
      WHERE `task_id` = :task_id",
      self.repo.table_prefix(),
    );
    let result: RunContextSO = client
      .query_one(
        query,
        params! {
          action_id,
          task_id,
        },
      )
      .await?
      .with_context(|| "get robot run context")?;
    Ok(result)
  }

  async fn update_status_by_task_id(&self, task_id: &str, status: &u8) {
    let mut client = self.repo.get_client().await.unwrap();
    let sql = format!(
      "
      UPDATE {}automation_run_history \
      SET status = :status \
      WHERE `task_id` = :task_id",
      self.repo.table_prefix(),
    );
    client.execute(sql, params! {status, task_id}).await.unwrap()
  }

  async fn create(
    &self,
    task_id: &str,
    robot_id: &str,
    space_id: &str,
    status: &u8,
    data: &Option<Json>,
  ) -> anyhow::Result<u64> {
    let mut client = self.repo.get_client().await.unwrap();
    let id = generate_u64_id();
    let sql = format!(
      "INSERT INTO {}automation_run_history (id, task_id, robot_id, space_id, status, data) \
      VALUES (:id, :task_id, :robot_id, :space_id, :status, :data)",
      self.repo.table_prefix(),
    );
    client
      .execute(sql, params! {id, task_id, robot_id, space_id, status, data})
      .await
      .unwrap();
    Ok(id)
  }

  async fn get_count_between_start_and_end_by_robot_id(
    &self,
    robot_id: &str,
    start_at: i64,
    end_at: i64,
  ) -> anyhow::Result<i64> {
    let mut client = self.repo.get_client().await.unwrap();
    let result: i64 = client
      .query_one(
        format!(
          "\
          SELECT COUNT(*) AS `count` \
          FROM `{prefix}automation_run_history` \
          WHERE `robot_id` = :robot_id \
          AND `status` != 4 \
          AND `created_at` >= FROM_UNIXTIME(:start_at) \
          AND `created_at` < FROM_UNIXTIME(:end_at) \
          ",
          prefix = self.repo.table_prefix()
        ),
        params! {
          robot_id,
          start_at,
          end_at
        },
      )
      .await?
      .with_context(|| format!("get count in a month {robot_id}"))
      .map_or(0, |count: i64| count);
    Ok(result)
  }

  async fn get_count_between_start_and_end_by_space_id(
    &self,
    space_id: &str,
    start_at: String,
    end_at: String,
  ) -> anyhow::Result<i64> {
    let mut client = self.repo.get_client().await.unwrap();
    let result: i64 = client
      .query_one(
        format!(
          "\
          SELECT COUNT(*) AS `count` \
          FROM `{prefix}automation_run_history` \
          WHERE `space_id` = :space_id \
          AND `status` != 4 \
          AND `created_at` >= :start_at \
          AND `created_at` < :end_at \
          ",
          prefix = self.repo.table_prefix()
        ),
        params! {
          space_id,
          start_at,
          end_at
        },
      )
      .await?
      .with_context(|| format!("get count in a month {space_id}"))
      .map_or(0, |count: i64| count);
    Ok(result)
  }
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Eq, FromRow)]
#[serde(rename_all = "camelCase")]
pub struct RunHistoryTaskSO {
  pub id: isize,
  pub task_id: String,
  pub robot_id: String,
  pub space_id: String,
  pub status: u8,
  pub data: Json,
  pub created_at: NaiveDateTime,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Eq, FromRow)]
#[serde(rename_all = "camelCase")]
pub struct RunContextSO {
  pub task_id: String,
  pub robot_id: String,
  pub status: u8,
  pub input: Option<Json>,
  pub output: Option<Json>,
}

#[derive(Debug, PartialEq)]
#[repr(u8)]
pub enum RunHistoryStatus {
  Running = 0,
  Success = 1,
  Failure = 2,
  PENDING = 3,
  EXCESS = 4,
}
