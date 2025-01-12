use serde::{Deserialize, Serialize};
use utoipa::ToSchema;

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Eq, ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct RecordAlarm {
  #[serde(skip_serializing_if = "Option::is_none")]
  pub id: Option<String>,
  pub subtract: Option<String>,
  pub time: Option<String>,
  pub alarm_users: Option<Vec<AlarmUser>>,
  pub record_id: Option<String>,
  pub field_id: Option<String>,
  pub alarm_at: Option<u64>,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Eq, ToSchema)]
pub struct AlarmUser {
  pub r#type: AlarmUsersType,
  pub data: String,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Eq, ToSchema)]
#[serde(rename_all = "snake_case")]
pub enum AlarmUsersType {
  Field,
  Member,
}