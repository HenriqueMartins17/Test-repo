use serde::{Serialize, Deserialize};

#[derive(Debug, Serialize, Deserialize, Clone, Default)]
#[serde(rename_all = "camelCase")]
pub struct WidgetPanelStatus {
    pub opening: bool,
    pub width: i32,
    pub active_panel_id: Option<String>,
    pub loading: bool,
  }