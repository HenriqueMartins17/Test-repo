use serde::{Serialize, Deserialize};
use utoipa::ToSchema;

#[derive(Debug, Deserialize, Serialize, ToSchema)]
pub struct AssetVo {
    pub token: Option<String>,
    pub upload_url: Option<String>,
    pub upload_request_method: Option<String>,
}