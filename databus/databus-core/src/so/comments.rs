use std::collections::HashMap;

use serde::{Serialize, Deserialize};
use utoipa::ToSchema;

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Eq, ToSchema, Default)]
#[serde(rename_all = "camelCase")]
pub struct Comments {
    pub revision: u32,
    pub created_at: u64,
    pub comment_id: String,
    pub unit_id: String,
    pub comment_msg: CommentMsg,
    pub updated_at: Option<u64>,
}

// type EmojisHashMap = HashMap<String, Vec<String>>;

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Eq, ToSchema, Default)]
#[serde(rename_all = "camelCase")]
pub struct CommentMsg {
    pub r#type: String,
    pub content: String,
    pub html: String,
    pub reply: Option<String>,
    pub emojis: Option<HashMap<String, Vec<String>>>,
}