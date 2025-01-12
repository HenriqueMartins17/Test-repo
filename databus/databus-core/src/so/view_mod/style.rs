use std::collections::HashMap;
use serde::{Deserialize, Serialize};
use utoipa::ToSchema;
use crate::prelude::color::GanttColorOption;
use crate::prelude::constants::AnyBaseField;
use std::fmt;

pub type HiddenGroupMap = HashMap<String, bool>;

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, ToSchema, Default)]
#[serde(rename_all = "camelCase")]
pub struct ViewStyleSo {

    #[serde(skip_serializing_if = "Option::is_none")]
    cover_field_id: Option<String>,

    #[serde(skip_serializing_if = "Option::is_none")]
    is_cover_fit: Option<bool>,

    #[serde(skip_serializing_if = "Option::is_none")]
    pub is_col_name_visible: Option<bool>,

    #[serde(skip_serializing_if = "Option::is_none")]
    pub kanban_field_id: Option<String>,

    #[serde(skip_serializing_if = "Option::is_none")]
    hidden_group_map: Option<HashMap<String, bool>>,


    #[serde(skip_serializing_if = "Option::is_none")]
    layout_type: Option<AnyBaseField>,

    #[serde(skip_serializing_if = "Option::is_none")]
    is_auto_layout: Option<bool>,

    #[serde(skip_serializing_if = "Option::is_none")]
    card_count: Option<i32>,


    #[serde(skip_serializing_if = "Option::is_none")]
    pub start_field_id: Option<String>,

    #[serde(skip_serializing_if = "Option::is_none")]
    pub end_field_id: Option<String>,

    #[serde(skip_serializing_if = "Option::is_none")]
    pub color_option: Option<GanttColorOption>,

    #[serde(skip_serializing_if = "Option::is_none")]
    work_days: Option<Vec<i32>>,

    #[serde(skip_serializing_if = "Option::is_none")]
    only_calc_work_day: Option<bool>,

    #[serde(skip_serializing_if = "Option::is_none")]
    link_field_id: Option<String>,

    #[serde(skip_serializing_if = "Option::is_none")]
    auto_task_layout: Option<bool>,

    #[serde(skip_serializing_if = "Option::is_none")]
    horizontal: Option<bool>,
}

#[derive(Deserialize, Serialize, Debug, Clone)]
pub enum CalendarStyleKeyType {
    ColorOption,
    StartFieldId,
    EndFieldId,
    IsColNameVisible,
}

impl fmt::Display for CalendarStyleKeyType {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            CalendarStyleKeyType::ColorOption => write!(f, "colorOption"),
            CalendarStyleKeyType::StartFieldId => write!(f, "startFieldId"),
            CalendarStyleKeyType::EndFieldId => write!(f, "endFieldId"),
            CalendarStyleKeyType::IsColNameVisible => write!(f, "isColNameVisible"),
        }
    }
}

#[derive(Deserialize, Serialize, Debug, Clone)]
pub enum OrgChartStyleKeyType {
    CoverFieldId,
    IsCoverFit,
    IsColNameVisible,
    LinkFieldId,
    Horizontal,
}

impl fmt::Display for OrgChartStyleKeyType {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            OrgChartStyleKeyType::CoverFieldId => write!(f, "coverFieldId"),
            OrgChartStyleKeyType::IsCoverFit => write!(f, "isCoverFit"),
            OrgChartStyleKeyType::IsColNameVisible => write!(f, "isColNameVisible"),
            OrgChartStyleKeyType::LinkFieldId => write!(f, "linkFieldId"),
            OrgChartStyleKeyType::Horizontal => write!(f, "horizontal"),
        }
    }
}

#[derive(Deserialize, Serialize, Debug, Clone)]
pub enum KanbanStyleKeyType {
    CoverFieldId,
    IsCoverFit,
    KanbanFieldId,
    IsColNameVisible,
    HiddenGroupMap,
}

impl fmt::Display for KanbanStyleKeyType {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            KanbanStyleKeyType::CoverFieldId => write!(f, "coverFieldId"),
            KanbanStyleKeyType::IsCoverFit => write!(f, "isCoverFit"),
            KanbanStyleKeyType::KanbanFieldId => write!(f, "kanbanFieldId"),
            KanbanStyleKeyType::IsColNameVisible => write!(f, "isColNameVisible"),
            KanbanStyleKeyType::HiddenGroupMap => write!(f, "hiddenGroupMap"),
        }
    }
}

#[derive(Deserialize, Serialize, Debug, Clone)]
pub enum GanttStyleKeyType {
    ColorOption,
    StartFieldId,
    EndFieldId,
    WorkDays,
    OnlyCalcWorkDay,
    LinkFieldId,
    AutoTaskLayout,
}

impl fmt::Display for GanttStyleKeyType {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            GanttStyleKeyType::ColorOption => write!(f, "colorOption"),
            GanttStyleKeyType::StartFieldId => write!(f, "startFieldId"),
            GanttStyleKeyType::EndFieldId => write!(f, "endFieldId"),
            GanttStyleKeyType::WorkDays => write!(f, "workDays"),
            GanttStyleKeyType::OnlyCalcWorkDay => write!(f, "onlyCalcWorkDay"),
            GanttStyleKeyType::LinkFieldId => write!(f, "linkFieldId"),
            GanttStyleKeyType::AutoTaskLayout => write!(f, "autoTaskLayout"),
        }
    }
}
