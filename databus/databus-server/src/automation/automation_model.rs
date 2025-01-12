use databus_dao_db::model::{
  AutomationActionIntroductionPO, AutomationActionPO, AutomationRobotIntroductionPO, AutomationTriggerIntroductionPO,
  AutomationTriggerPO,
};
use databus_dao_db::node::model::NodeSimplePO;
use serde::{Deserialize, Serialize};
use serde_json::Value;
use utoipa::{IntoParams, ToSchema};

#[derive(Deserialize, Serialize, ToSchema)]
pub struct AutomationHistoryStatusRO {
  pub status: u8,
}

#[derive(Deserialize, Serialize, ToSchema)]
pub struct AutomationHistoryRO {
  pub task_id: String,
  pub space_id: String,
  pub data: Option<Value>,
  pub status: u8,
}

#[derive(Debug, Clone, PartialEq, Serialize, Deserialize, IntoParams)]
#[into_params(style = Form, parameter_in = Query)]
pub struct RobotIdVecDTO {
  #[param(example = json!(["arb"]), value_type=Vec<String>)]
  pub robot_ids: Vec<String>,
}

#[derive(Debug, Clone, PartialEq, Serialize, Deserialize, IntoParams)]
#[into_params(style = Form, parameter_in = Query)]
pub struct ResourceIdDTO {
  #[param(example = "dst***")]
  pub resource_id: String,
}

#[derive(Debug, Clone, PartialEq, Serialize, Deserialize, IntoParams)]
#[into_params(style = Form, parameter_in = Query)]
pub struct PageDTO {
  #[param(example = "20")]
  pub page_size: u32,

  #[param(example = "1")]
  pub page_num: u32,
}

#[derive(Deserialize, Serialize, Debug, Clone, IntoParams, ToSchema)]
pub struct AutomationRobotIntroductionSO {
  pub robots: Vec<AutomationRobotIntroductionPO>,
  pub actions: Vec<AutomationActionIntroductionPO>,
  pub triggers: Vec<AutomationTriggerIntroductionPO>,
}

#[derive(Deserialize, Serialize, Debug, Clone, ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct AutomationSO {
  pub robot: AutomationRobotSO,
  pub actions: Vec<AutomationActionPO>,
  pub triggers: Vec<AutomationTriggerPO>,
  pub related_resources: Option<Vec<NodeSimplePO>>,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Eq, ToSchema)]
pub struct AutomationRobotUpdateRO {
  pub updated_by: u64,
  pub name: Option<String>,
  pub description: Option<String>,
  pub props: Option<String>,
  pub is_active: Option<bool>,
  pub is_deleted: Option<bool>,
}

#[derive(Deserialize, Serialize, Debug, Clone, ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct AutomationRobotSO {
  pub robot_id: String,
  pub resource_id: String,
  pub name: String,
  pub description: Option<String>,
  pub props: Option<String>,
  pub is_active: u8,
  pub updated_by: Option<u64>,
  pub updated_at: Option<i64>,
  pub recently_run_count: i64,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Eq, ToSchema)]
pub struct AutomationRobotTriggerRO {
  pub user_id: u64,
  pub trigger_id: Option<String>,
  pub limit_count: Option<i64>,
  pub trigger_type_id: Option<String>,
  pub prev_trigger_id: Option<String>,
  pub input: Option<String>,
  pub resource_id: Option<String>,
  pub is_deleted: Option<bool>,
  pub space_id: Option<String>,
  pub schedule_conf: Option<String>,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Eq, ToSchema)]
pub struct AutomationRobotActionRO {
  pub user_id: u64,
  pub action_id: Option<String>,
  pub limit_count: Option<i64>,
  pub action_type_id: Option<String>,
  pub prev_action_id: Option<String>,
  pub input: Option<String>,
  pub is_deleted: Option<bool>,
}

#[derive(Deserialize, Serialize, Debug, Clone, ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct AutomationRobotRunNumsSO {
  pub recently_run_count: i64,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Eq, ToSchema)]
pub struct AutomationRobotCopyRO {
  pub user_id: u64,
  pub original_resource_id: String,
  pub resource_id: String,
  pub automation_name: Option<String>,
}
