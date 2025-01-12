use super::types::ResourceType;
use crate::so::field::FieldKindSO;
use databus_shared::prelude::HashMapExt;
use serde::{Deserialize, Serialize};
use crate::ot::types::ActionOTO;

#[derive(Debug, Clone)]
pub struct Changeset {
  pub message_id: String,
  pub resource_type: ResourceType,
  pub resource_d: String,
  pub operations: Vec<Operation>,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Default)]
#[serde(rename_all = "camelCase")]
pub struct LocalChangeset {
  pub base_revision: i32,
  pub message_id: String,
  pub resource_type: ResourceType,
  pub resource_id: String,
  pub operations: Vec<Operation>,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Default)]
#[serde(rename_all = "camelCase")]
pub struct RemoteChangeset {
  #[serde(skip_serializing_if = "Option::is_none")]
  pub user_id: Option<String>,
  pub revision: i32,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub created_at: Option<i64>,
  pub message_id: String,
  pub resource_type: ResourceType,
  pub resource_id: String,
  pub operations: Vec<Operation>,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Default)]
#[serde(rename_all = "camelCase")]
pub struct ResourceOpsCollect {
  pub resource_id: String,
  pub resource_type: ResourceType,
  pub operations: Vec<Operation>,
  pub field_type_map: Option<HashMapExt<String, FieldKindSO>>,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Default)]
#[serde(rename_all = "camelCase")]
pub struct Operation {
  pub cmd: String,
  pub actions: Vec<ActionOTO>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub main_link_dst_id: Option<String>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub field_type_map: Option<HashMapExt<String, FieldKindSO>>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub resource_type: Option<ResourceType>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub revision: Option<i32>,
}
