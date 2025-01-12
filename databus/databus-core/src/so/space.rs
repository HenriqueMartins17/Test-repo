use std::collections::HashMap;

use serde::{Deserialize, Serialize};
use utoipa::ToSchema;

#[derive(Debug, Deserialize, Serialize, ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct APISpaceSO {
    pub id: String,
    pub name: String,
    pub is_admin: Option<bool>
}

#[derive(Debug, Deserialize, Serialize, Default)]
#[serde(rename_all = "camelCase")]
pub struct InternalSpaceUsageView {
    pub record_nums: u32, // The number of all records of all datasheets in the space
    pub gallery_view_nums: u32, // The number of all views in the space
    pub kanban_view_nums: u32, // The number of all kanban views in the space
    pub gantt_view_nums: u32, // The number of all gantt views in the space
    pub calendar_view_nums: u32, // The number of all calendar views in the space
    pub used_credit: u32, // The number of credits in the space
}

#[derive(Debug, Deserialize, Serialize, Default)]
#[serde(rename_all = "camelCase")]
pub struct InternalSpaceSubscriptionView {
    pub max_rows_per_sheet: i32, // The maximum record allowed per datasheet
    pub max_archived_rows_per_sheet: i32, // The maximum archived record allowed per datasheet
    pub max_rows_in_space: i32, // The maximum record allowed by the current space
    pub max_gallery_views_in_space: i32, // The maximum number of gallery views allowed in the space
    pub max_kanban_views_in_space: i32, // The maximum number of kanban views allowed in the space
    pub max_gantt_views_in_space: i32, // The maximum quantity of the allowable Gantt view in the space
    pub max_calendar_views_in_space: i32, // The maximum number of calendar views allowed in the space
    pub max_message_credits: i32, // The maximum number of chatBot credits allowed in the space
    pub max_widget_nums: i32, // The maximum number of widgets allowed in the space
    pub max_automation_runs_nums: i32, // The maximum number of automation runs allowed in the space
    pub allow_embed: bool, // Is it possible to call enterprise-level api?
    pub allow_org_api: bool,
}

#[derive(Debug, Deserialize, Serialize, Default)]
#[serde(rename_all = "camelCase")]
pub struct InternalSpaceStatisticsRo {
    pub view_count: Option<HashMap<u32, u32>>,
    pub record_count: Option<i32>,
}