
use serde::{Deserialize, Serialize};
use utoipa::IntoParams;

#[derive(Serialize, Deserialize, IntoParams)]
#[into_params(parameter_in = Query)]
#[serde(rename_all = "camelCase")]
pub struct AssetUploadQueryRo {
  pub count: Option<i32>,
}