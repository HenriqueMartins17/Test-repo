use serde::{Deserialize, Serialize};
use utoipa::ToSchema;
use mysql_common::prelude::FromRow;



#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, FromRow, ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct DatasheetEntityPO {
  pub id: i64,
  pub dst_id: Option<String>,
  pub node_id: Option<String>,
  pub dst_name: Option<String>,
  pub space_id: Option<String>,
  pub creator: Option<i64>,
  pub revision: Option<i64>,
}
