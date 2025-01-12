use serde::{Deserialize, Serialize};
use utoipa::IntoParams;

#[derive(Serialize, Deserialize, IntoParams)]
#[into_params(parameter_in = Query)]
#[serde(rename_all = "camelCase")]
pub struct DatasheetPackRO {
  pub user_id: Option<String>,
  pub space_id: Option<String>,
  pub view_id: Option<String>,
}
