use std::collections::HashMap;

use serde::{Deserialize, Serialize};
use utoipa::ToSchema;

use crate::prelude::{CellValueVo, Comments, RecordMeta};
use crate::so::api_value::ApiValue;

#[derive(Debug, Deserialize, Serialize, ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct RecordDTO {
  pub record_id: String,
  pub fields: HashMap<String, ApiValue>,
  pub created_at: u64,
  pub updated_at: u64,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Eq, ToSchema, Default)]
#[serde(rename_all = "camelCase")]
pub struct RecordVO {
  pub id: String,
  pub comment_count: u32,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub comments: Option<Vec<Comments>>,
  pub data: HashMap<String, CellValueVo>,
  pub created_at: Option<i64>,
  pub updated_at: Option<i64>,
  pub revision_history: Option<Vec<u32>>,
  pub record_meta: Option<RecordMeta>,
}

#[derive(Debug, Deserialize, Serialize, ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct RecordCreateItemVo {
  pub record_id: String,
  pub fields: HashMap<String, serde_json::Value>,
}

#[derive(Debug, Deserialize, Serialize, ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct RecordCreateVo {
  pub records: Vec<RecordCreateItemVo>,
}
