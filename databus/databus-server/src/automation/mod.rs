pub mod automation_dao_api;
pub mod automation_model;

#[derive(Debug, PartialEq)]
#[repr(u16)]
pub enum AutomationErrorCode {
  AutomationRobotNotExist = 1104,
  AutomationTriggerCountLimit = 1105,
  AutomationActionCountLimit = 446,
}
