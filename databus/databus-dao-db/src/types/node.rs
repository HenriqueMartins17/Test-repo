use databus_core::prelude::NodeSO;
/// Persistent Objects Definitions
use databus_shared::prelude::{Json, JsonExt};
use serde::{Deserialize, Serialize};

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq)]
#[serde(rename_all = "camelCase")]
pub struct NodeDetailPO {
  pub node: NodeSO,
  pub field_permission_map: Option<Json>,
}

#[derive(Deserialize, Serialize, Debug, Clone, Default, PartialEq, Eq)]
#[serde(rename_all = "camelCase")]
pub struct NodePermission {
  pub has_role: bool,

  #[serde(skip_serializing_if = "Option::is_none")]
  pub user_id: Option<String>,

  #[serde(skip_serializing_if = "Option::is_none")]
  pub uuid: Option<String>,

  pub role: String,

  #[serde(skip_serializing_if = "Option::is_none")]
  pub node_favorite: Option<bool>,

  #[serde(skip_serializing_if = "JsonExt::is_falsy")]
  pub field_permission_map: Option<Json>,

  #[serde(skip_serializing_if = "Option::is_none")]
  pub is_ghost_node: Option<bool>,

  #[serde(skip_serializing_if = "Option::is_none")]
  pub is_deleted: Option<bool>,

  #[serde(flatten)]
  #[serde(skip_serializing_if = "Option::is_none")]
  pub permissions: Option<Json>,

  #[serde(skip_serializing_if = "Option::is_none")]
  pub editable: Option<bool>,
}

#[allow(unused)]
#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum PermissionRole {
  Manager,
  Editor,
  Reader,
  Updater,
  TemplateVisitor,
  Owner,
  Anonymous,
  Foreigner,
  ShareReader,
  ShareEditor,
  ShareSave,
}

impl PermissionRole {
  pub fn as_str(&self) -> &'static str {
    match self {
      Self::Manager => "manager",
      Self::Editor => "editor",
      Self::Reader => "reader",
      Self::Updater => "updater",
      Self::TemplateVisitor => "templateVisitor",
      Self::Owner => "manager",
      Self::Anonymous => "reader",
      Self::Foreigner => "reader",
      Self::ShareReader => "shareReader",
      Self::ShareEditor => "shareEditor",
      Self::ShareSave => "shareSave",
    }
  }
}

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum NodeExtraConstant {
  ShowRecordHistory,
}

impl NodeExtraConstant {
  pub fn as_str(&self) -> &'static str {
    match self {
      Self::ShowRecordHistory => "showRecordHistory",
    }
  }
}
