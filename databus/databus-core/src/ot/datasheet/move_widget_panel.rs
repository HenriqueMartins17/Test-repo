use crate::{ot::{commands::{CollaCommandDefExecuteResult, MoveWidgetPanelOptions, ExecuteResult}, get_resource_widget_panels}, so::DatasheetSnapshotSO, DatasheetActions};

pub struct MoveWidgetPanel {

}

impl MoveWidgetPanel {
    pub fn execute (
        options: MoveWidgetPanelOptions,
        snapshot: DatasheetSnapshotSO,
    ) -> anyhow::Result<Option<CollaCommandDefExecuteResult>> { 
        // let state = context.state;
        let MoveWidgetPanelOptions {
            panel_id,
            target_index,
            resource_id,
            resource_type,
            cmd:_,
        } = options;

        if target_index > 2 {
            return Ok(None);
        }

        let widget_panels = get_resource_widget_panels(&snapshot, &resource_id, &resource_type);

        if widget_panels.is_none() {
            return Ok(None);
        }

        let source_index = widget_panels.unwrap().iter().position(|item| item.id == panel_id);

        if source_index.is_none() {
            return Ok(None);
        }

        let move_widget_panel_action = DatasheetActions::move_panel_to_action(target_index, source_index.unwrap(), resource_type.clone()).unwrap();
        if move_widget_panel_action.is_none() {
            return Ok(None);
        }

        Ok(Some(CollaCommandDefExecuteResult {
            result: ExecuteResult::Success,
            resource_id,
            resource_type,
            actions: move_widget_panel_action.unwrap(),
            ..Default::default()
        }))
    }
}