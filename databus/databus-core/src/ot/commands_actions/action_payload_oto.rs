use std::collections::HashMap;

use serde::{Serialize, Deserialize};
use serde_json::Value;
use crate::ot::commands::LinkedActions;

use crate::{so::{ViewSO, RecordAlarm, view_operation::{sort::{ISortInfo, ISortedField}, filter::IFilterInfo}, FieldSO, types::IViewLockInfo, Comments, WidgetPanelSO, style::{CalendarStyleKeyType, OrgChartStyleKeyType, KanbanStyleKeyType}}, ot::{types::{ResourceType, ActionOTO}, commands::{SetCalendarStyleOption, SetGanttStyleOption}}};

#[derive(Deserialize, Serialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct LinkedFieldActionsOTO {
  pub actions: Vec<ActionOTO>,
  pub linked_actions: Option<Vec<LinkedActions>>,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct PayloadAddViewVO{
    pub start_index: Option<usize>,
    pub view: ViewSO,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct PayloadMoveViewVO{
    pub view_id: String,
    pub target: usize,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct PayloadDelViewVO{
    pub view_id: String,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct ModifyViewOTO{
    pub view_id: String,
    pub key: String,
    pub value: Value,
}

#[derive(Debug, Serialize, Deserialize, Clone)]
#[serde(rename_all = "camelCase")]
pub struct ColumnWidthOTO{
    pub view_id: String,
    pub field_id: String,
    pub width: Option<i32>,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct MoveColumnsOTO{
    pub view_id: String,
    pub field_id: String,
    pub target: usize,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct SetColumnStatTypeOTO{
    pub view_id: String,
    pub field_id: String,
    pub stat_type: Option<i32>,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct SetRowHeightLevelOTO{
    pub view_id: String,
    pub level: i32,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct SetAutoHeadHeightOTO{
    pub view_id: String,
    pub is_auto: bool,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct SetFrozenColumnCountOTO{
    pub view_id: String,
    pub count: i32,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct PayloadAddRecordVO {
    pub view_id: String,
    pub record: Value,
    pub index: usize,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct DeleteRecordsOTO{
    pub record_ids: Vec<String>,
}

#[derive(Debug, Deserialize, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct DeleteRecordItem {
    pub record_id: String,
    pub index: usize,
}

#[derive(Debug, Deserialize, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct RecordDelete {
pub record_id: String,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct RecordAlarmOTO{
    pub record_id: String,
    pub field_id: String,
    pub alarm: Option<RecordAlarm>,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct MoveRowOTO{
    pub record_id: String,
    pub view_id: String,
    pub target: usize,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct ViewSortOTO{
    pub view_id: String,
    pub sort_info: Option<ISortInfo>,
    pub apply_sort: bool,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct FieldOTO{
    pub field: FieldSO,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct SetFilterInfoOTO{
    pub view_id: String,
    pub filter_info: Option<IFilterInfo>,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct SetViewLockInfoOTO{
    pub view_id: String,
    pub view_lock_info: Option<IViewLockInfo>,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct SetGroupInfoFieldOTO{
    pub view_id: String,
    pub group_info: Option<Vec<ISortedField>>,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct InsertCommentOTO{
    pub datasheet_id: String,
    pub record_id: String,
    pub insert_comments: Option<Vec<Comments>>,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct UpdateCommentOTO{
    pub datasheet_id: String,
    pub record_id: String,
    pub update_comments: Vec<Comments>,
    pub emoji_action: Option<bool>,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct DeleteCommentOTO{
    pub datasheet_id: String,
    pub record_id: String,
    pub comments: Vec<Comments>,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct WidgetOTO{
    pub id: String,
    pub height: f64,
    pub y: i64,
}

/**
     * The value of the largest integer n such that n and n + 1 are both exactly representable as
     * a Number value.
     * The value of Number.MAX_SAFE_INTEGER is 9007199254740991 2^53 − 1.
     */
pub const MAX_SAFE_INTEGER: i64 = 9007199254740991;

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct DeleteWidgetOTO{
    pub widget_panel_index: usize,
    pub widget: WidgetPanelSO,
    pub widget_index: usize,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct ChangeWidgetHeightOTO{
    pub widget_panel_index: usize,
    pub widget_index: usize,
    pub widget_height: usize,
    pub resource_id: String,
    pub resource_type: ResourceType,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct MoveWidgetOTO{
    pub widget_panel_index: usize,
    // pub layout: Vec<WidgetOTO>,
    pub layout: Vec<HashMap<String, Value>>,
    pub resource_type: ResourceType,
    pub resource_id: String,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct ViewOTO{
    pub view_id: String,
    pub view_property: ViewSO,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct SetViewAutoSaveOTO {
    pub view_id: String,
    pub auto_save: bool,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct ChangeOneWayLinkDstIdOTO {
    pub field_id: String,
    pub new_field: FieldSO,
}

#[derive(Debug, Serialize, Deserialize, Default)]
#[serde(rename_all = "camelCase")]
pub struct PayloadAddFieldVO {
    pub field: FieldSO,
    pub view_id: Option<String>,
    pub index: Option<i32>,
    pub field_id: Option<String>,
    pub offset: Option<i32>,
    pub hidden_column: Option<bool>,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct PayloadDelFieldVO{
    pub field_id: String,
    pub datasheet_id: String,
    pub view_id: Option<String>,
}

//ViewAction
#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct SetCalendarStyleOTO{
    pub view_id: String,
    pub data: Vec<SetCalendarStyleOption>,
    pub is_clear: Option<bool>
}

#[derive(Deserialize, Serialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct SetGalleryStyleOTO {
  pub view_id: String,
  pub style_key: CalendarStyleKeyType,
  pub style_value: Value,
}

#[derive(Deserialize, Serialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct SetKanbanStyleOTO {
  pub view_id: String,
  pub style_key: KanbanStyleKeyType,
  pub style_value: Value,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct SetOrgChartStyleOTO{
    pub view_id: String,
    pub style_key: OrgChartStyleKeyType,
    pub style_value: Value,
}


#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct SetGanttStyleOTO{
    pub view_id: String,
    pub data: Vec<SetGanttStyleOption>,
}