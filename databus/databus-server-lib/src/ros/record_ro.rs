use serde::{Deserialize, Serialize};
use utoipa::IntoParams;

#[derive(Serialize, Deserialize, IntoParams)]
#[into_params(parameter_in = Query)]
#[serde(rename_all = "camelCase")]
pub struct RecordRO {
  pub dst_id: Option<String>,
  pub user_id: Option<String>,
  pub space_id: Option<String>,
  pub view_id: Option<String>
}

#[derive(Serialize, Deserialize, IntoParams)]
#[into_params(parameter_in = Query)]
#[serde(rename_all = "camelCase")]
pub struct RecordViewQueryRO {
  pub view_id: Option<String>,
}

// #[derive(Serialize, Deserialize)]
// pub enum StringOrVec {
//   S(String),
//   V(Vec<String>),
// }

#[derive(Serialize, Deserialize, IntoParams)]
#[into_params(parameter_in = Query)]
#[serde(rename_all = "camelCase")]
pub struct RecordDeleteRO {
  // pub record_ids: Option<StringOrVec>,
  pub record_ids: Option<String>,
}
