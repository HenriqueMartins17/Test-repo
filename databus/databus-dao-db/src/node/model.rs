use mysql_async::prelude::FromRow;
use serde::{Deserialize, Serialize};
use utoipa::ToSchema;

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Eq, FromRow, ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct NodeSimplePO {
  pub node_id: String,
  pub node_name: String,
  pub icon: Option<String>,
}
