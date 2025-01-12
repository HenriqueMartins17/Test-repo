use serde::{Deserialize, Serialize};
use serde_json::Value;
use utoipa::ToSchema;



use crate::ot::types::NodeTypeEnum;

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct NodeSO {
  pub id: String,
  pub name: String,
  pub description: String,
  pub parent_id: String,
  pub icon: String,
  pub node_shared: bool,
  pub node_permit_set: bool,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub node_favorite: Option<bool>,
  pub space_id: String,
  pub role: String,
  pub permissions: NodePermissionStateSO,
  pub revision: u32,

  #[serde(skip_serializing_if = "Option::is_none")]
  pub is_ghost_node: Option<bool>,

  #[serde(skip_serializing_if = "Option::is_none")]
  pub active_view: Option<String>,

  #[serde(skip_serializing_if = "Option::is_none")]
  pub extra: Option<Value>,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct NodePermissionStateSO {
  #[serde(skip_serializing_if = "Option::is_none")]
  pub is_deleted: Option<bool>,

  #[serde(flatten)]
  #[serde(skip_serializing_if = "Option::is_none")]
  pub permissions: Option<Value>,
}

#[derive(Debug, Deserialize, Serialize, ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct APINodeSO {
    pub id: String,
    pub name: String,
    pub r#type: NodeTypeEnum,
    pub icon: String,
    pub is_fav: bool,
    pub permission: Option<i32>,
}
