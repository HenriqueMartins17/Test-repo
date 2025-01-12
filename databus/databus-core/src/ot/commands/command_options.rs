use std::fmt;
use std::collections::HashMap;

use crate::ot::WidgetOTO;
use crate::ot::changeset::Operation;
use crate::ot::types::ResourceType;
use crate::shared::AuthHeader;
use crate::shared::UserBaseInfo;
use crate::so::CellValueSo;
use crate::so::Comments;
use crate::so::FieldKindSO;
use crate::so::RecordAlarm;
use crate::so::RecordSO;
use crate::so::ViewColumnSO;
use crate::so::ViewRowSO;
use crate::so::ViewSO;
use crate::so::constants::RowHeightLevel;
use crate::so::constants::StatType;
use crate::so::field::FieldSO;
use crate::so::style::CalendarStyleKeyType;
use crate::so::style::GanttStyleKeyType;
use crate::so::style::KanbanStyleKeyType;
use crate::so::style::OrgChartStyleKeyType;
use crate::so::types::IViewLockInfo;
use crate::so::view_operation::filter::IFilterInfo;
use crate::so::view_operation::sort::ISortInfo;
use crate::so::view_operation::sort::ISortedField;
use super::CollaCommandName;

use serde::{Deserialize, Serialize};
use serde_json::Value;

#[derive(Deserialize, Serialize, Debug)]
#[serde(rename_all = "camelCase")]
pub enum CommandOptions {
  AddFieldsOptions(AddFieldsOptions),
  AddRecordsOptions(AddRecordsOptions),
  AddViewsOptions(AddViewsOptions),
  DeleteCommentOptions(DeleteCommentOptions),
  DeleteFieldOptions(DeleteFieldOptions),
  DeleteRecordOptions(DeleteRecordOptions),
  DeleteViewsOptions(DeleteViewsOptions),
  ModifyViewsOptions(ModifyViewsOptions),
  SetFieldAttrOptions(SetFieldAttrOptions),
  SetRecordsOptions(SetRecordsOptions),
  UpdateCommentOptions(UpdateCommentOptions),
  //no execute
  SystemSetFieldAttrOptions(SystemSetFieldAttrOptions),
}

impl fmt::Display for CommandOptions {
  fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
    match self {
      CommandOptions::AddFieldsOptions(_) => write!(f, "{}", CollaCommandName::AddFields.to_string()),
      CommandOptions::AddRecordsOptions(_) => write!(f, "{}", CollaCommandName::AddRecords.to_string()),
      CommandOptions::AddViewsOptions(_) => write!(f, "{}", CollaCommandName::AddViews.to_string()),
      CommandOptions::DeleteCommentOptions(_) => write!(f, "{}", CollaCommandName::DeleteComment.to_string()),
      CommandOptions::DeleteFieldOptions(_) => write!(f, "{}", CollaCommandName::DeleteField.to_string()),
      CommandOptions::DeleteRecordOptions(_) => write!(f, "{}", CollaCommandName::DeleteRecords.to_string()),
      CommandOptions::DeleteViewsOptions(_) => write!(f, "{}", CollaCommandName::DeleteViews.to_string()),
      CommandOptions::ModifyViewsOptions(_) => write!(f, "{}", CollaCommandName::ModifyViews.to_string()),
      CommandOptions::SetFieldAttrOptions(_) => write!(f, "{}", CollaCommandName::SetFieldAttr.to_string()),
      CommandOptions::SetRecordsOptions(_) => write!(f, "{}", CollaCommandName::SetRecords.to_string()),
      CommandOptions::UpdateCommentOptions(_) => write!(f, "{}", CollaCommandName::UpdateComment.to_string()),

      //no execute
      CommandOptions::SystemSetFieldAttrOptions(_) => write!(f, "{}", CollaCommandName::SystemSetFieldAttr.to_string()),
    }
  }
}

#[derive(Deserialize, Serialize, Debug, Default, Clone)]
#[serde(rename_all = "camelCase")]
pub struct AddRecordsOptions {
  pub cmd: CollaCommandName,
  pub datasheet_id: Option<String>,
  pub view_id: String,
  pub index: usize,
  pub count: usize,
  pub group_cell_values: Option<Vec<CellValueSo>>,
  pub cell_values: Option<Vec<HashMap<String, CellValueSo>>>,
  pub ignore_field_permission: bool,
}

#[derive(Deserialize, Serialize, Debug, Default)]
#[serde(rename_all = "camelCase")]
pub struct AddFieldsOptions {
  pub cmd: CollaCommandName,
  pub data: Vec<AddFieldOptions>,
  pub datasheet_id: Option<String>,
  pub copy_cell: Option<bool>,
  pub field_id: Option<String>,
  pub internal_fix: Option<InternalFix>,
}

#[derive(Deserialize, Serialize, Debug, Clone, Default)]
#[serde(rename_all = "camelCase")]
pub struct AddFieldOptions {
  pub view_id: Option<String>,
  pub index: i32,
  pub data: FieldSO, //id实际要变成option
  // The fieldId that triggers the operation of adding a new column
  pub field_id: Option<String>,
  // Offset relative to fieldId position
  pub offset: Option<i32>,
  // whether to hide this newly created field
  pub hidden_column: Option<bool>,
}

#[derive(Deserialize, Serialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct AddViewsOptions {
  pub cmd: CollaCommandName,
  pub data: Vec<AddView>,
}

#[derive(Deserialize, Serialize, Debug, Default)]
#[serde(rename_all = "camelCase")]
pub struct AddView {
  pub start_index: Option<usize>,
  pub view: ViewSO,
}

#[derive(Deserialize, Serialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct ChangeWidgetInPanelHeightOptions {
  pub cmd: CollaCommandName,
  pub panel_id: String,
  pub widget_id: String,
  pub widget_height: usize,
  pub resource_id: String,
  pub resource_type: ResourceType,
}

#[derive(Deserialize, Serialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct DeleteCommentOptions {
  pub cmd: CollaCommandName,
  pub datasheet_id: String,
  pub record_id: String,
  pub comment: Comments,
}

#[derive(Deserialize, Serialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct DeleteWidgetPanelOptions {
  pub cmd: CollaCommandName,
  pub delete_panel_id: String,
  pub resource_id: String,
  pub resource_type: ResourceType,
}

#[derive(Deserialize, Serialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct FixOneWayLinkDstIdOptions {
  pub cmd: CollaCommandName,
  pub data: Vec<FixOneWayLinkOptions>,
  pub datasheet_id: String,
  pub field_id: String,
}

#[derive(Deserialize, Serialize, Debug, Default)]
#[serde(rename_all = "camelCase")]
pub struct FixOneWayLinkOptions {
  pub old_brother_field_id: String,
  pub old_foreign_datasheet_id: Option<String>,
  pub new_brother_field_id: String,
  pub new_foreign_datasheet_id: Option<String>,
}

#[derive(Deserialize, Serialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct InsertCommentOptions {
  pub cmd: CollaCommandName,
  pub datasheet_id: String,
  pub record_id: String,
  pub comments: Vec<Comments>,
}

#[derive(Deserialize, Serialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct ManualSaveViewOptions {
  pub cmd: CollaCommandName,
  pub view_id: String,
  pub view_property: ViewSO,
}

#[derive(Deserialize, Serialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct MoveViewsOptions {
  pub cmd: CollaCommandName,
  pub data: Vec<MoveView>,
}

#[derive(Deserialize, Serialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct MoveView {
  pub new_index: usize,
  pub view_id: String,
}

#[derive(Deserialize, Serialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct MoveWidgetPanelOptions {
    pub cmd: CollaCommandName,
    pub panel_id: String,
    pub target_index: usize,
    pub resource_id: String,
    pub resource_type: ResourceType,
}

#[derive(Deserialize, Serialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct MoveWidgetOptions {
  pub cmd: CollaCommandName,
  pub layout: Vec<WidgetOTO>,
  pub resource_id: String,
  pub resource_type: ResourceType,
  pub panel_id: String,
}

#[derive(Deserialize, Serialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct DeleteFieldOptions {
  pub cmd: CollaCommandName,
  pub data: Vec<DeleteFieldData>,
  pub datasheet_id: Option<String>
}

#[derive(Deserialize, Serialize, Debug, Default)]
#[serde(rename_all = "camelCase")]
pub struct DeleteRecordOptions {
  pub cmd: CollaCommandName,
  pub data: Vec<String>,
  pub datasheet_id: Option<String>
}

#[derive(Deserialize, Serialize, Debug, Default)]
#[serde(rename_all = "camelCase")]
pub struct DeleteFieldData {
  pub delete_brother_field: Option<bool>,
  pub field_id: String,
}

#[derive(Deserialize, Serialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct DeleteViewsOptions {
  pub cmd: CollaCommandName,
  pub data: Vec<DeleteView>,
}

#[derive(Deserialize, Serialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct DeleteView {
  pub view_id: String,
}

#[derive(Deserialize, Serialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct FillDataToCellOptions {
  pub cmd: CollaCommandName,
  pub selection_range: Vec<IRange>,
  pub fill_range: Option<IRange>,
  pub direction: Option<String>,
}

#[derive(Deserialize, Serialize, Debug)]
pub struct IRange {
  pub start: ICell,
  pub end: ICell,
}

#[derive(Deserialize, Serialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct ICell {
  pub record_id: String,
  pub field_id: String,
}

#[derive(Deserialize, Serialize, Debug, Default)]
#[serde(rename_all = "camelCase")]
pub struct ModifyViewsOptions {
  pub cmd: CollaCommandName,
  pub data: Vec<ModifyView>,
  pub datasheet_id: Option<String>,
}

#[derive(Deserialize, Serialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct ColumnsProperty {
  pub width: Option<i32>,
  pub stat_type: Option<StatType>,
}

#[derive(Deserialize, Serialize, Debug, Eq, PartialEq, Clone)]
pub enum DropDirectionType {
  BEFORE,
  AFTER,
  NONE,
}

#[derive(Deserialize, Serialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct MoveColumnOptions {
  pub cmd: CollaCommandName,
  pub data: Vec<MoveColumnData>,
  pub view_id: String,
}

#[derive(Deserialize, Serialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct MoveColumnData {
  pub field_id: String,
  pub over_target_id: String,
  pub direction: DropDirectionType,
}

#[derive(Deserialize, Serialize, Debug, Default)]
#[serde(rename_all = "camelCase")]
pub struct ModifyView {
  pub view_id: String,
  pub key: String,
  pub value: Value,
}

#[derive(Deserialize, Serialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct MoveRowOptions {
  pub cmd: CollaCommandName,
  pub data: Vec<MoveRowData>,
  pub view_id: String,
  pub record_data: Option<Vec<SetRecordOptions>>,
}

#[derive(Deserialize, Serialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct MoveRowData {
  pub record_id: String,
  pub over_target_id: String,
  pub direction: DropDirectionType,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq)]
#[serde(rename_all = "camelCase")]
pub struct InternalFix {
  pub anonymou_fix: Option<bool>,
  pub fix_user: Option<UserBaseInfo>,
  pub self_create_new_field: Option<bool>,
  pub change_one_way_link_dst_id: Option<bool>,
  // When a one-way association is converted to text - clean up the one-way association cell content
  pub clear_one_way_link_cell: Option<bool>,
}

#[derive(Deserialize, Serialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct PasteSetFieldsOptions {
  pub cmd: CollaCommandName,
  pub view_id: String,
  pub column: i32,
  pub fields: Vec<FieldSO>,
  pub std_values: Vec<Vec<IStandardValue>>,
}

#[derive(Deserialize, Serialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct PasteSetRecordsOptions {
  pub cmd: CollaCommandName,
  pub row: i32,
  pub column: i32,
  pub view_id: String,
  pub fields: Vec<FieldSO>,
  pub record_ids: Option<Vec<String>>,
  pub std_values: Vec<Vec<IStandardValue>>,
  pub cut: Option<CutOption>,
  pub group_cell_values: Option<Vec<CellValueSo>>,
  pub notify_exist_incompatible_field: Option<String>,
  // pub notify_exist_incompatible_field: Option<fn()>,
}

#[derive(Deserialize, Serialize, Debug, Clone)]
#[serde(rename_all = "camelCase")]
pub struct CutOption {
  pub datasheet_id: String,
  pub rows: Vec<ViewRowSO>,
  pub columns: Vec<ViewColumnSO>,
}

#[derive(Deserialize, Serialize, Debug, Clone, Default)]
#[serde(rename_all = "camelCase")]
pub struct IStandardValue {
  pub source_type: FieldKindSO,
  pub data: Vec<HashMap<String, Value>>,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq)]
#[serde(rename_all = "camelCase")]
pub struct Resource {
  pub id: String,
  pub resource_type: ResourceType,
  pub name: String,
  pub revision: u32,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Default)]
#[serde(rename_all = "camelCase")]
pub struct SetRecordsOptions {
  pub cmd: CollaCommandName,
  pub datasheet_id: Option<String>,
  // pub alarm: Option<IRecordAlarm>,
  pub data: Vec<SetRecordOptions>,
  // pub mirror_id: Option<String>,
  pub internal_fix: Option<InternalFix>,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Default)]
#[serde(rename_all = "camelCase")]
pub struct SetRecordOptions {
  pub record_id: String,
  pub field_id: String,
  pub field: Option<FieldSO>, // Optional, pass in field information. Applicable to addRecords on fields that have not been applied to snapshot
  pub value: CellValueSo,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq)]
#[serde(rename_all = "camelCase")]
pub struct ResetRecordsOptions {
  pub cmd: CollaCommandName,
  pub datasheet_id: Option<String>,
  pub data: HashMap<String, RecordSO>,
  // store: Store<IReduxState>,
}

#[derive(Deserialize, Serialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct RollbackOptions {
  pub cmd: CollaCommandName,
  pub datasheet_id: String,
  pub data: Option<IRollback>,
}

#[derive(Deserialize, Serialize, Debug)]
pub struct IRollback {
  pub operations: Vec<Operation>, //Operation to IOperation
}

#[derive(Deserialize, Serialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct SetColumnsPropertyOptions {
  pub cmd: CollaCommandName,
  pub data: ColumnsProperty,
  pub view_id: String,
  pub field_id: String,
}

#[derive(Deserialize, Serialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct SetDateTimeCellAlarmOptions {
  pub cmd: CollaCommandName,
  pub datasheet_id: Option<String>,
  pub field_id: String,
  pub record_id: String,
  pub alarm: Option<RecordAlarm>,
}

#[derive(Deserialize, Serialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct SetGalleryStyleOptions {
  pub cmd: CollaCommandName,
  pub view_id: String,
  pub style_key: CalendarStyleKeyType,
  pub style_value: Value,
}

#[derive(Deserialize, Serialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct SetAutoHeadHeightOptions {
  pub cmd: CollaCommandName,
  pub is_auto: bool,
  pub view_id: String,
}

#[derive(Deserialize, Serialize, Debug, Default)]
#[serde(rename_all = "camelCase")]
pub struct SetFieldAttrOptions {
  pub cmd: CollaCommandName,
  pub datasheet_id: Option<String>,
  pub field_id: String,
  pub delete_brother_field: Option<bool>,
  pub data: FieldSO,
  pub internal_fix: Option<InternalFix>,
}

#[derive(Deserialize, Serialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct SetGroupOptions {
  pub cmd: CollaCommandName,
  pub view_id: String,
  pub data: Option<Vec<ISortedField>>,
}

#[derive(Deserialize, Serialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct SetCalendarStyleOptions {
  pub cmd: CollaCommandName,
  pub view_id: String,
  pub data: Vec<SetCalendarStyleOption>,
  pub is_clear: Option<bool>,
}

#[derive(Deserialize, Serialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct SetCalendarStyleOption {
  pub style_key: CalendarStyleKeyType,
  pub style_value: Value,
}

#[derive(Deserialize, Serialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct SetGanttStyleOptions {
  pub cmd: CollaCommandName,
  pub view_id: String,
  pub data: Vec<SetGanttStyleOption>,
}

#[derive(Deserialize, Serialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct SetGanttStyleOption {
  pub style_key: GanttStyleKeyType,
  pub style_value: Value,
}

#[derive(Deserialize, Serialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct SetOrgChartStyleOptions {
  pub cmd: CollaCommandName,
  pub view_id: String,
  pub style_key: OrgChartStyleKeyType,
  pub style_value: Value,
}

#[derive(Deserialize, Serialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct SetKanbanStyleOptions {
  pub cmd: CollaCommandName,
  pub view_id: String,
  pub add_record: Option<bool>,
  pub style_key: KanbanStyleKeyType,
  pub style_value: Value,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Default)]
#[serde(rename_all = "camelCase")]
pub struct SaveOptions{
    pub auth: AuthHeader,
    pub prepend_ops: Vec<Operation>,
}

#[derive(Deserialize, Serialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct SetRowHeightOptions {
  pub cmd: CollaCommandName,
  pub level: RowHeightLevel,
  pub view_id: String,
}

#[derive(Deserialize, Serialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct SetSortInfoOptions {
  pub cmd: CollaCommandName,
  pub view_id: String,
  pub data: Option<ISortInfo>,
  pub apply_sort: Option<bool>,
}

#[derive(Deserialize, Serialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct SetViewAutoSaveOptions {
  pub cmd: CollaCommandName,
  pub view_id: String,
  pub auto_save: bool,
  pub view_property: Option<ViewSO>,
}

#[derive(Deserialize, Serialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct SetViewFilterOptions {
  pub cmd: CollaCommandName,
  pub data: Option<IFilterInfo>,
  pub view_id: String,
}

#[derive(Deserialize, Serialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct SetViewFrozenColumnCountOptions {
  pub cmd: CollaCommandName,
  pub count: i32,
  pub view_id: String,
}

#[derive(Deserialize, Serialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct SetViewLockInfoOptions {
  pub cmd: CollaCommandName,
  pub data: Option<IViewLockInfo>,
  pub view_id: String,
}

#[derive(Deserialize, Serialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct UpdateCommentOptions {
  pub cmd: CollaCommandName,
  pub datasheet_id: String,
  pub record_id: String,
  pub comments: Comments,
  pub emoji_action: Option<bool>,
}


#[derive(Deserialize, Serialize, Debug)]
pub struct SystemSetFieldAttrOptions {
  pub cmd: CollaCommandName,
}
