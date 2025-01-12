use crate::{ot::{commands::{CollaCommandDefExecuteResult, SetCalendarStyleOptions, ExecuteResult}, types::{ActionOTO, ResourceType}, ViewAction, SetCalendarStyleOTO}, so::DatasheetSnapshotSO};

pub struct SetCalendarStyle {

}

impl SetCalendarStyle {
    pub fn execute (
        options: SetCalendarStyleOptions,
        snapshot: DatasheetSnapshotSO,
    ) -> anyhow::Result<Option<CollaCommandDefExecuteResult>> { 
        // let state = context.state;
        let view_id = options.view_id;
        // let datasheet_id = get_active_datasheet_id(state)?;
        // let datasheet = get_datasheet(state, datasheet_id)?;
        let datasheet_id = snapshot.datasheet_id.clone();
        
        // if state.is_none() || datasheet.is_none() {
        //     return None;
        // }
        
        // Determine whether the currently operating view is the active view
        // if datasheet.active_view != view_id {
        //     panic!(t(Strings.error_modify_column_failed_wrong_target_view));
        // }
        
        let mut actions: Vec<ActionOTO> = Vec::new();
        let set_calendar_style_action = ViewAction::set_calendar_style_to_action(snapshot, SetCalendarStyleOTO {
            view_id,
            data: options.data,
            is_clear: options.is_clear,
        }).unwrap();
        if let Some(action) = set_calendar_style_action {
            actions.extend(action);
        }
        
        if actions.is_empty() {
            return Ok(None);
        }
        
        Ok(Some(CollaCommandDefExecuteResult {
            result: ExecuteResult::Success,
            resource_id: datasheet_id,
            resource_type: ResourceType::Datasheet,
            actions,
            ..Default::default()
        }))
    }
}