use serde::{Deserialize, Serialize};
use serde_json::Value;

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq)]
#[serde(rename_all = "camelCase")]
pub struct DatasheetRecordCommentPO {
    pub id: i64,
    pub is_deleted: bool,
    pub comment_msg: Option<Value>,
    pub field_updated_info: Option<Value>,
    pub revision: Option<i64>,
    pub created_by: Option<i64>,
    pub updated_by: Option<i64>,
    pub created_at: i64,
    pub updated_at: i64,
    pub dst_id: Option<String>,
    pub record_id: Option<String>,
    pub comment_id: Option<String>,
    pub revision_history: Option<String>,
}
