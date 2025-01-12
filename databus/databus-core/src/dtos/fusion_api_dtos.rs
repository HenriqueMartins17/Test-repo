use std::collections::HashMap;

use serde::{Deserialize, Serialize};
use serde_json::Value;
use utoipa::ToSchema;
use crate::so::types::ViewType;

#[derive(Debug, Deserialize, Serialize, ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct ApiRecordDto {
    pub record_id: String,
    pub fields: HashMap<String, Value>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub created_at: Option<i64>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub updated_at: Option<i64>,
}
// pub type IFieldValueMap = std::collections::HashMap<String, String>;

#[derive(Debug, Deserialize, Serialize, ToSchema)]
pub struct DatasheetViewDto {
    pub id: String,
    pub name: String,
    pub r#type: ViewType,
}

#[derive(Debug, Deserialize, Serialize, ToSchema)]
pub struct DatasheetViewListDto {
    pub views: Vec<DatasheetViewDto>,
}

#[derive(Debug, Deserialize, Serialize, ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct DatasheetCreateDto {
    pub id: Option<String>,
    pub created_at: Option<i64>,
    pub fields: Vec<FieldCreateDto>,
}

#[derive(Debug, Deserialize, Serialize, ToSchema)]
pub struct FieldCreateDto {
    pub id: Option<String>,
    pub name: Option<String>,
}

#[derive(Debug, Deserialize, Serialize, Default)]
#[serde(rename_all = "camelCase")]
pub struct IAssetDTO {
    pub mime_type: String,
    pub token: String,
    pub bucket: String,
    pub size: u32,
    pub width: Option<u32>,
    pub height: Option<u32>,
    pub preview: Option<String>,
}