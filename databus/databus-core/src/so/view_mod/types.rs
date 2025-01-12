use serde::{Deserialize, Serialize};
use utoipa::ToSchema;

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq)]
#[serde(rename_all = "camelCase")]
#[repr(u64)]
pub enum ViewType {
    NotSupport = 0,
    Grid = 1,
    Kanban = 2,
    Gallery = 3,
    Form = 4,
    Calendar = 5,
    Gantt = 6,
    OrgChart = 7,
}


// #[derive(Deserialize, Serialize, Debug, Clone, PartialEq)]
// #[serde(rename_all = "camelCase")]
// pub struct IViewRow {
//     pub record_id: String,
//     pub hidden: Option<bool>,
// }


// #[derive(Deserialize, Serialize, Debug, Clone, PartialEq)]
// #[serde(rename_all = "camelCase")]
// pub struct IViewColumn {
//     pub field_id: String,
//     pub hidden: Option<bool>,
// }

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct IViewLockInfo {
    description: Option<String>,
    unit_id: String,
}


#[cfg(test)]
mod tests{

}