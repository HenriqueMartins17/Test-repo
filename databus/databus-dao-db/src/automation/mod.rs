use std::sync::Arc;

use crate::trigger_schedule::AutomationTriggerScheduleDAO;
use action::AutomationActionDAO;
use robot::AutomationRobotDAO;
use run_history::AutomationRunHistoryDAO;
use trigger::AutomationTriggerDAO;

pub mod action;
pub mod model;
pub mod robot;
pub mod run_history;
pub mod trigger;
pub mod trigger_schedule;

pub struct AutomationDAO {
  pub run_history_dao: Arc<dyn AutomationRunHistoryDAO>,
  pub trigger_dao: Arc<dyn AutomationTriggerDAO>,
  pub robot_dao: Arc<dyn AutomationRobotDAO>,
  pub action_dao: Arc<dyn AutomationActionDAO>,
  pub trigger_schedule_dao: Arc<dyn AutomationTriggerScheduleDAO>,
}

pub fn new_dao(
  run_history_dao: Arc<dyn AutomationRunHistoryDAO>,
  trigger_dao: Arc<dyn AutomationTriggerDAO>,
  robot_dao: Arc<dyn AutomationRobotDAO>,
  action_dao: Arc<dyn AutomationActionDAO>,
  trigger_schedule_dao: Arc<dyn AutomationTriggerScheduleDAO>,
) -> Arc<AutomationDAO> {
  Arc::new(AutomationDAO {
    run_history_dao,
    trigger_dao,
    robot_dao,
    action_dao,
    trigger_schedule_dao,
  })
}
