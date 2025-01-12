use serde::{Deserialize, Serialize};
use serde_json::Value;

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq)]
#[serde(rename_all = "camelCase")]
pub struct NodePO {
    pub id: i64,
    pub space_id: Option<String>,
    pub parent_id: Option<String>,
    pub pre_node_id: Option<String>,
    pub node_id: Option<String>,
    pub icon: Option<String>,
    pub extra: Option<Value>,
    pub r#type: Option<i32>,
    pub node_name: Option<String>,
    pub cover: Option<String>,
    pub deleted_path: Option<String>,
    pub is_template: bool,
    pub is_rubbish: bool,
    pub is_banned: bool,
    pub is_deleted: bool,
    pub comment_msg: Option<Value>,
    pub field_updated_info: Option<Value>,
    pub revision: Option<i64>,
    pub created_by: Option<i64>,
    pub updated_by: Option<i64>,
    pub created_at: i64,
    pub updated_at: i64,
    pub creator: i64,
}
