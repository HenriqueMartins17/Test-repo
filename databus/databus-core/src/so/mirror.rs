use serde::{Serialize, Deserialize};

use super::WidgetPanelSO;

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct MirrorSnapshot {
    // widget panels and widgets
    pub widget_panels: Option<Vec<WidgetPanelSO>>,
}