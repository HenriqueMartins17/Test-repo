use serde::{Deserialize, Serialize};

#[allow(unused)]
#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Default, Eq, Hash)]
pub enum CollaCommandName {
  #[default]
  AddFields,
  AddRecords,
  SetRecords,
  SetFieldAttr,
  PasteSetFields,
  PasteSetRecords,
  MoveViews,
  ModifyViews,
  DeleteViews,
  AddViews,
  MoveRow,
  DeleteRecords,
  MoveColumn,
  DeleteField,
  SetSortInfo,
  SetRowHeight,
  SetAutoHeadHeight,
  SetColumnsProperty,
  SetViewFilter,
  SetViewLockInfo,
  SetViewFrozenColumnCount,
  SetGroup,
  SetGalleryStyle,
  SetGanttStyle,
  SetOrgChartStyle,
  SetCalendarStyle,
  FillDataToCells,
  FixConsistency,     // special command to fix data consistency issues
  SystemSetRecords, // special command, used for some special records to set data only in the middle layer to fix the data consistency problem
  SystemSetFieldAttr, // special command, used for some special fields to set attributes only in the middle layer to fix the data consistency problem
  SetKanbanStyle,
  InsertComment, // insert a comment into the record
  UpdateComment,
  DeleteComment,        // delete a comment in the record
  SystemCorrectComment, // special command, correct time in comment
  Rollback,             // snapshot rollback
  // widgetPanel
  AddWidgetPanel,
  MoveWidgetPanel,
  ModifyWidgetPanelName,
  DeleteWidgetPanel,
  AddWidgetToPanel,
  DeleteWidget,
  ChangeWidgetInPanelHeight,
  MoveWidget,

  // Widget
  SetGlobalStorage,
  SetWidgetName,

  // Dashboard
  AddWidgetToDashboard,
  ChangeDashboardLayout,
  DeleteDashboardWidget,
  SetWidgetDepDstId,

  // Form
  UpdateFormProps,
  // Date cell alarm
  SetDateTimeCellAlarm,

  // Manually save the view configuration
  ManualSaveView,

  // Modify the save mode of the configuration
  SetViewAutoSave,

  // special command, correct one-way association DstId
  FixOneWayLinkDstId,

  // Only used for Fusion API for reload recordMap
  ResetRecords,
}

impl std::fmt::Display for CollaCommandName {
  fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
    match self {
      CollaCommandName::AddFields => write!(f, "AddFields"),
      CollaCommandName::AddRecords => write!(f, "AddRecords"),
      CollaCommandName::SetRecords => write!(f, "SetRecords"),
      CollaCommandName::SetFieldAttr => write!(f, "SetFieldAttr"),
      CollaCommandName::PasteSetFields => write!(f, "PasteSetFields"),
      CollaCommandName::PasteSetRecords => write!(f, "PasteSetRecords"),
      CollaCommandName::MoveViews => write!(f, "MoveViews"),
      CollaCommandName::ModifyViews => write!(f, "ModifyViews"),
      CollaCommandName::DeleteViews => write!(f, "DeleteViews"),
      CollaCommandName::AddViews => write!(f, "AddViews"),
      CollaCommandName::MoveRow => write!(f, "MoveRow"),
      CollaCommandName::DeleteRecords => write!(f, "DeleteRecords"),
      CollaCommandName::MoveColumn => write!(f, "MoveColumn"),
      CollaCommandName::DeleteField => write!(f, "DeleteField"),
      CollaCommandName::SetSortInfo => write!(f, "SetSortInfo"),
      CollaCommandName::SetRowHeight => write!(f, "SetRowHeight"),
      CollaCommandName::SetAutoHeadHeight => write!(f, "SetAutoHeadHeight"),
      CollaCommandName::SetColumnsProperty => write!(f, "SetColumnsProperty"),
      CollaCommandName::SetViewFilter => write!(f, "SetViewFilter"),
      CollaCommandName::SetViewLockInfo => write!(f, "SetViewLockInfo"),
      CollaCommandName::SetViewFrozenColumnCount => write!(f, "SetViewFrozenColumnCount"),
      CollaCommandName::SetGroup => write!(f, "SetGroup"),
      CollaCommandName::SetGalleryStyle => write!(f, "SetGalleryStyle"),
      CollaCommandName::SetGanttStyle => write!(f, "SetGanttStyle"),
      CollaCommandName::SetOrgChartStyle => write!(f, "SetOrgChartStyle"),
      CollaCommandName::SetCalendarStyle => write!(f, "SetCalendarStyle"),
      CollaCommandName::FillDataToCells => write!(f, "FillDataToCells"),
      CollaCommandName::FixConsistency => write!(f, "FixConsistency"),
      CollaCommandName::SystemSetRecords => write!(f, "SystemSetRecords"),
      CollaCommandName::SystemSetFieldAttr => write!(f, "SystemSetFieldAttr"),
      CollaCommandName::SetKanbanStyle => write!(f, "SetKanbanStyle"),
      CollaCommandName::InsertComment => write!(f, "InsertComment"),
      CollaCommandName::UpdateComment => write!(f, "UpdateComment"),
      CollaCommandName::DeleteComment => write!(f, "DeleteComment"),
      CollaCommandName::SystemCorrectComment => write!(f, "SystemCorrectComment"),
      CollaCommandName::Rollback => write!(f, "Rollback"),
      CollaCommandName::AddWidgetPanel => write!(f, "AddWidgetPanel"),
      CollaCommandName::MoveWidgetPanel => write!(f, "MoveWidgetPanel"),
      CollaCommandName::ModifyWidgetPanelName => write!(f, "ModifyWidgetPanelName"),
      CollaCommandName::DeleteWidgetPanel => write!(f, "DeleteWidgetPanel"),
      CollaCommandName::AddWidgetToPanel => write!(f, "AddWidgetToPanel"),
      CollaCommandName::DeleteWidget => write!(f, "DeleteWidget"),
      CollaCommandName::ChangeWidgetInPanelHeight => write!(f, "ChangeWidgetInPanelHeight"),
      CollaCommandName::MoveWidget => write!(f, "MoveWidget"),
      CollaCommandName::SetGlobalStorage => write!(f, "SetGlobalStorage"),
      CollaCommandName::SetWidgetName => write!(f, "SetWidgetName"),
      CollaCommandName::AddWidgetToDashboard => write!(f, "AddWidgetToDashboard"),
      CollaCommandName::ChangeDashboardLayout => write!(f, "ChangeDashboardLayout"),
      CollaCommandName::DeleteDashboardWidget => write!(f, "DeleteDashboardWidget"),
      CollaCommandName::SetWidgetDepDstId => write!(f, "SetWidgetDepDstId"),
      CollaCommandName::UpdateFormProps => write!(f, "UpdateFormProps"),
      CollaCommandName::SetDateTimeCellAlarm => write!(f, "SetDateTimeCellAlarm"),
      CollaCommandName::ManualSaveView => write!(f, "ManualSaveView"),
      CollaCommandName::SetViewAutoSave => write!(f, "SetViewAutoSave"),
      CollaCommandName::FixOneWayLinkDstId => write!(f, "FixOneWayLinkDstId"),
      CollaCommandName::ResetRecords => write!(f, "ResetRecords"),
    }
  }
}
