use std::collections::HashMap;

use serde_json::Value;

use crate::{ot::{commands::{CollaCommandDefExecuteResult, MoveWidgetOptions, ExecuteResult}, get_resource_widget_panels, MoveWidgetOTO}, so::DatasheetSnapshotSO, DatasheetActions};

pub struct MoveWidget {

}

impl MoveWidget {
    pub fn execute (
        options: MoveWidgetOptions,
        snapshot: DatasheetSnapshotSO,
    ) -> anyhow::Result<Option<CollaCommandDefExecuteResult>> { 
        // let state = context.state;
        let MoveWidgetOptions {
            layout,
            panel_id,
            resource_type,
            resource_id,
            cmd:_,
        } = options;
        let widget_panels = get_resource_widget_panels(&snapshot, &resource_id, &resource_type);

        if widget_panels.is_none() {
            return Ok(None);
        }

        let active_panel_index = widget_panels.clone().unwrap().iter().position(|item| item.id == panel_id);

        if active_panel_index.is_none() {
            return Ok(None);
        }
        let active_panel_index = active_panel_index.unwrap();
        let widgets = widget_panels.unwrap()[active_panel_index.clone()].widgets.clone();
        let installed_widget_ids = widgets.iter().map(|widget| widget.id.clone()).collect::<Vec<_>>();
        let ids = layout.iter().map(|v| v.id.clone()).collect::<Vec<_>>();
        let _ids: Vec<_> = ids.iter().chain(installed_widget_ids.iter()).collect();
        if _ids.len() != ids.len() {
            return Ok(None);
        }
        let json_str = serde_json::to_string(&layout).unwrap();
        let layout: Vec<HashMap<String, Value>> = serde_json::from_str(&json_str).unwrap();
        let move_widget_action = DatasheetActions::move_widget_to_action(snapshot.clone(), MoveWidgetOTO {
            widget_panel_index: active_panel_index,
            layout,
            resource_type: resource_type.clone(),
            resource_id: resource_id.clone(),
        }).unwrap();

        if move_widget_action.is_none() {
            return Ok(None);
        }

        Ok(Some(CollaCommandDefExecuteResult {
            result: ExecuteResult::Success,
            resource_id,
            resource_type,
            actions: move_widget_action.unwrap(),
            ..Default::default()
        }))
    }
}