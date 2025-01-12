use serde::{Serialize, Deserialize};
use utoipa::ToSchema;
use crate::dtos::fusion_api_dtos::ApiRecordDto;

#[derive(Debug, Deserialize, Serialize, ToSchema)]
pub struct ListVO {
    pub records: Vec<ApiRecordDto>,
}