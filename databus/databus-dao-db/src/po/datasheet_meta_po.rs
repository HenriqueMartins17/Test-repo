use serde::{Deserialize, Serialize};
use serde_json::Value;

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq)]
#[serde(rename_all = "camelCase")]
pub struct DatasheetMetaPO {
    pub id: i64,
    pub is_deleted: bool,
    pub meta_data: Option<Value>,
    pub revision: i64,
    pub created_by: Option<i64>,
    pub updated_by: Option<i64>,
    pub created_at: i64,
    pub updated_at: i64,
    pub dst_id: Option<String>,
}
