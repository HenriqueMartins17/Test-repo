use chrono::NaiveDateTime;
use databus_shared::prelude::Json;
use fred::bytes_utils::Str;
use mysql_async::prelude::FromRow;
use serde::{Deserialize, Serialize};
use utoipa::ToSchema;

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Eq, ToSchema)]
pub struct AutomationRobotPropertyPO {
  pub failure_notify_enable: bool,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Eq, FromRow, ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct AutomationRobotIntroductionPO {
  pub robot_id: String,
  pub resource_id: String,
  pub name: String,
  pub description: Option<String>,
  pub is_active: u8,
  pub props: Option<String>,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Eq, FromRow)]
#[serde(rename_all = "camelCase")]
pub struct AutomationRobotPO {
  pub robot_id: String,
  pub resource_id: String,
  pub name: String,
  pub description: Option<String>,
  pub props: Option<String>,
  pub is_active: u8,
  pub updated_by: Option<u64>,
  pub updated_at: Option<NaiveDateTime>,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Eq, FromRow, ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct AutomationActionIntroductionPO {
  pub robot_id: String,
  pub action_type_id: String,
  pub action_id: String,
  pub prev_action_id: Option<String>,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Eq, FromRow)]
#[serde(rename_all = "camelCase")]
pub struct TriggerPO {
  pub id: u64,
  pub robot_id: String,
  pub trigger_id: String,
  pub trigger_type_id: String,
  pub input: Json,
  pub is_deleted: u8,
  pub created_by: u64,
  pub updated_by: u64,
  pub created_at: NaiveDateTime,
  pub updated_at: NaiveDateTime,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Eq, FromRow)]
#[serde(rename_all = "camelCase")]
pub struct TriggerSO {
  pub robot_id: String,
  pub trigger_id: String,
  pub trigger_type_id: String,
  pub input: Json,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Eq, FromRow, ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct AutomationTriggerIntroductionPO {
  pub robot_id: String,
  pub trigger_id: String,
  pub trigger_type_id: String,
  pub prev_trigger_id: Option<String>,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Eq, FromRow, ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct AutomationTriggerPO {
  pub robot_id: String,
  pub trigger_id: String,
  pub resource_id: Option<String>,
  pub trigger_type_id: String,
  pub prev_trigger_id: Option<String>,
  pub input: Option<String>,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Eq, FromRow, ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct AutomationTriggerSO {
  pub robot_id: String,
  pub trigger_id: String,
  pub resource_id: Option<String>,
  pub trigger_type_id: String,
  pub prev_trigger_id: Option<String>,
  pub input: Option<String>,
  pub schedule_id: Option<u64>,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Eq, FromRow, ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct AutomationActionPO {
  pub robot_id: String,
  pub action_type_id: String,
  pub action_id: String,
  pub prev_action_id: Option<String>,
  pub input: Option<String>,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Eq, FromRow, ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct AutomationRunHistoryPO {
  pub task_id: String,
  pub robot_id: String,
  pub status: u8,
  #[schema(value_type = String)]
  pub created_at: NaiveDateTime,
  pub action_ids: Option<String>,
  pub action_type_ids: Option<String>,
  pub error_stacks: Option<String>,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Eq)]
pub struct CreateRobotTriggerSO {
  pub resource_id: Option<String>,
  pub trigger_type_id: String,
  pub input: Option<String>,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Eq)]
pub struct CreateRobotActionSO {
  pub action_type_id: String,
  pub input: Option<String>,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Eq)]
pub struct CreateAutomationRobotSO {
  pub created_by: u64,
  pub is_active: u8,
  pub resource_id: String,
  pub name: String,
  pub props: Option<String>,
  pub triggers: Option<Vec<CreateRobotTriggerSO>>,
  pub actions: Option<Vec<CreateRobotActionSO>>,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Eq, FromRow)]
#[serde(rename_all = "camelCase")]
pub struct ResourceRobotDto {
  pub robot_id: String,
  pub resource_id: String,
}
