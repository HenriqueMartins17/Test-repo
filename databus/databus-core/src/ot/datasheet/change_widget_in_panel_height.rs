use crate::{ot::{commands::{CollaCommandDefExecuteResult, ChangeWidgetInPanelHeightOptions, ExecuteResult}, get_resource_widget_panels, ChangeWidgetHeightOTO}, so::DatasheetSnapshotSO, DatasheetActions};

pub struct ChangeWidgetInPanelHeight {

}

impl ChangeWidgetInPanelHeight {
    pub fn execute (
        options: ChangeWidgetInPanelHeightOptions,
        snapshot: DatasheetSnapshotSO,
    ) -> anyhow::Result<Option<CollaCommandDefExecuteResult>> { 
        let ChangeWidgetInPanelHeightOptions {
            cmd:_, 
            panel_id, 
            widget_id, 
            widget_height, 
            resource_id, 
            resource_type 
        } = options;
        // let state = &context.state;
        let widget_panels = get_resource_widget_panels(&snapshot, &resource_id, &resource_type);

        if widget_panels.is_none() {
            return Ok(None);
        }
        let widget_panels = widget_panels.unwrap();
        let widget_panel_index = widget_panels.iter().position(|item| item.id == panel_id);

        if widget_panel_index.is_none() {
            return Ok(None);
        }
        let widget_panel_index = widget_panel_index.unwrap();
        let widgets = &widget_panels[widget_panel_index].widgets;
        let widget_index = widgets.iter().position(|item| item.id == widget_id);

        if widget_index.is_none() {
            return Ok(None);
        }
        let widget_index = widget_index.unwrap();
        let change_widget_height_action = DatasheetActions::change_widget_height_to_action(
            snapshot.clone(),
            ChangeWidgetHeightOTO {
                widget_panel_index,
                widget_index,
                widget_height,
                resource_id: resource_id.clone(),
                resource_type: resource_type.clone(),
            },
        ).unwrap();

        if change_widget_height_action.is_none() {
            return Ok(None);
        }

        Ok(Some(CollaCommandDefExecuteResult {
            result: ExecuteResult::Success,
            resource_id,
            resource_type,
            actions: change_widget_height_action.unwrap(),
            ..Default::default()
        }))
    }
}