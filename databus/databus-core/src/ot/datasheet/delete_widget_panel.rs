use crate::{ot::{commands::{CollaCommandDefExecuteResult, DeleteWidgetPanelOptions, ExecuteResult}, get_resource_widget_panels}, so::DatasheetSnapshotSO, DatasheetActions};

pub struct DeleteWidgetPanel {

}

impl DeleteWidgetPanel {
    pub fn execute (
        options: DeleteWidgetPanelOptions,
        snapshot: DatasheetSnapshotSO,
    ) -> anyhow::Result<Option<CollaCommandDefExecuteResult>> { 
        let DeleteWidgetPanelOptions { cmd:_, delete_panel_id, resource_id, resource_type } = options;
        // let state = &context.state;
        let widget_panels = get_resource_widget_panels(&snapshot, &resource_id, &resource_type);
        if widget_panels.is_none() {
            return Ok(None);
        }
        let widget_panels = widget_panels.unwrap();
        let panel = widget_panels.iter().find(|item| item.id == delete_panel_id);

        if panel.is_none() {
            return Ok(None);
        }

        let delete_widget_panel_action = DatasheetActions::delete_widget_panel_to_action( delete_panel_id, &widget_panels, resource_type.clone()).unwrap();

        if delete_widget_panel_action.is_none() {
            return Ok(None);
        }

        Ok(Some(CollaCommandDefExecuteResult {
            result: ExecuteResult::Success,
            resource_id,
            resource_type,
            actions: delete_widget_panel_action.unwrap(),
            ..Default::default()
        }))
    }
}