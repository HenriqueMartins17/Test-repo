use std::collections::HashMap;

use serde::{Deserialize, Serialize};
use serde_json::Value;

use crate::{ot::{types::{ActionOTO, ResourceType}, changeset::{Operation, ResourceOpsCollect}}, so::FieldSO};

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Default)]
pub enum ExecuteType {
  #[default]
  Execute,
  Undo,
  Redo,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Default)]
#[serde(rename_all = "camelCase")]
pub struct LinkedActions {
  pub datasheet_id: String,
  pub actions: Vec<ActionOTO>,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Default)]
pub enum ExecuteResult {
  /** No need to execute */
  #[default]
  None,
  Fail,
  Success,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Default)]
pub enum ExecuteFailReason {
  #[default]
  /** Don't know what went wrong */
  Unknown,

  /** action validation failed */
  ActionError,

  /** Operate on unsupported field, view */
  NotSupport,

  /** table, view name duplicate */
  NameRepeat,

  /** The last one, cannot be deleted */
  LastOne,

  /** Field type mismatch */
  FieldTypeNotMatch,

  /** Passed parameter problem */
  WrongOptions,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Default)]
#[serde(rename_all = "camelCase")]
pub struct SaveResult {
  pub base_revision: i32,
  pub message_id: String,
  pub resource_id: String,
  pub resource_type: ResourceType,
  pub operations: Vec<Operation>,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Default)]
#[serde(rename_all = "camelCase")]
pub struct CollaCommandExecuteResult {
  pub result: ExecuteResult,
  pub resource_id: String,
  pub resource_type: ResourceType,
  
  #[serde(skip_serializing_if = "Option::is_none")]
  pub data: Option<Value>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub operation: Option<Operation>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub execute_type: Option<ExecuteType>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub linked_actions: Option<Vec<LinkedActions>>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub resource_ops_collects: Option<Vec<ResourceOpsCollect>>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub save_result: Option<Vec<SaveResult>>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub reason: Option<ExecuteFailReason>,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Default)]
#[serde(rename_all = "camelCase")]
pub struct CollaCommandDefExecuteResult {
  pub result: ExecuteResult,
  pub resource_id: String,
  pub resource_type: ResourceType,
  pub actions: Vec<ActionOTO>,

  #[serde(skip_serializing_if = "Option::is_none")]
  pub data: Option<Value>, // Option<String> ||  Option<Vec<String>>
  // pub data: Option<String>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub linked_actions: Option<Vec<LinkedActions>>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub field_map_snapshot: Option<HashMap<String, FieldSO>>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub datasheet_id: Option<String>,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Default)]
#[serde(rename_all = "camelCase")]
pub struct SetFieldResult {
  pub actions: Vec<ActionOTO>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub linked_actions: Option<Vec<LinkedActions>>, 
}