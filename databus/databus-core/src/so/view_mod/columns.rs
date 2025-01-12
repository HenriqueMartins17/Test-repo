use serde::{Deserialize, Serialize};


#[derive(Deserialize, Serialize, Debug, Clone, PartialEq)]
#[serde(rename_all = "camelCase")]
pub struct IViewColumn {
    field_id: String,
    hidden: Option<bool>,
}
