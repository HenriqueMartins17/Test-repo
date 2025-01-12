use crate::db_manager::DBManager;
use async_trait::async_trait;
use mysql_common::params;
use std::sync::Arc;

#[async_trait]
pub trait AutomationTriggerScheduleDAO: Send + Sync {
  async fn get_id_by_trigger_id(&self, trigger_id: &str) -> Option<u64>;
}

pub struct AutomationTriggerScheduleDAOImpl {
  repo: Arc<dyn DBManager>,
}

pub fn new_dao(repo: Arc<dyn DBManager>) -> Arc<dyn AutomationTriggerScheduleDAO> {
  Arc::new(AutomationTriggerScheduleDAOImpl { repo })
}

#[async_trait]
impl AutomationTriggerScheduleDAO for AutomationTriggerScheduleDAOImpl {
  async fn get_id_by_trigger_id(&self, trigger_id: &str) -> Option<u64> {
    let mut client = self.repo.get_client().await.ok().unwrap();
    let query = format!(
      "
      SELECT id \
      FROM `{}automation_trigger_schedule` \
      WHERE trigger_id = :trigger_id \
      AND is_deleted = 0 \
      ",
      self.repo.table_prefix()
    );
    return client.query_one(query, params! {trigger_id}).await.unwrap();
  }
}
