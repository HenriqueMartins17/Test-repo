use std::collections::HashMap;

use serde::{Deserialize, Serialize};
use serde_json::Value;
use utoipa::ToSchema;

use crate::ro::record_update_ro::FieldKeyEnum;

#[derive(Debug, Deserialize, Serialize, ToSchema, Clone)]
#[serde(rename_all = "camelCase")]
pub struct RecordCreateItemRO {
  pub fields: HashMap<String, Value>,
}

#[derive(Debug, Deserialize, Serialize, ToSchema, Clone)]
#[serde(rename_all = "camelCase")]
pub struct RecordCreateRO {
  pub records: Vec<RecordCreateItemRO>,
  pub field_key: FieldKeyEnum,
}
