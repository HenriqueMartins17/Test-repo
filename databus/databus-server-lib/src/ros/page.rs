use serde::{Deserialize, Serialize};
use utoipa::{IntoParams, ToSchema};
use validator::Validate;

#[derive(Debug, Validate, Deserialize, IntoParams)]
pub struct PageRO {
  #[serde(default = "default_page_size", rename = "pageSize")]
  #[validate(range(min = 1, max = 1000))]
  pub page_size: i32,

  #[serde(rename = "maxRecords")]
  pub max_records: Option<i32>,

  #[serde(default = "default_page_num", rename = "pageNum")]
  #[validate(range(min = 1))]
  pub page_num: i32,

  pub sort: Option<Vec<SortRO>>,
}

#[derive(Debug, Validate, Deserialize, Serialize, ToSchema)]
pub struct SortRO {
  #[serde(rename = "field")]
  pub field: String,

  #[serde(rename = "order")]
  pub order: OrderEnum,
}

#[derive(Debug, Deserialize, Serialize, ToSchema)]
#[serde(rename_all = "lowercase")]
pub enum OrderEnum {
  Desc,
  Asc,
}

impl Default for OrderEnum {
  fn default() -> Self {
    OrderEnum::Desc
  }
}

fn default_page_size() -> i32 {
  100
}

fn default_page_num() -> i32 {
  1
}
