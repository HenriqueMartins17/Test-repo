use crate::{ot::{commands::{CollaCommandDefExecuteResult, SetGanttStyleOptions, ExecuteResult}, types::{ActionOTO, ResourceType}, ViewAction, SetGanttStyleOTO}, so::DatasheetSnapshotSO};


pub struct SetGanttStyle {

}

impl SetGanttStyle {
    pub fn execute (
        snapshot: DatasheetSnapshotSO,
        options: SetGanttStyleOptions,
    ) -> anyhow::Result<Option<CollaCommandDefExecuteResult>> {
        let SetGanttStyleOptions { cmd:_, view_id, data } = options;
        // let datasheet_id = get_active_datasheet_id(state).unwrap();
        let datasheet_id = snapshot.datasheet_id.clone();
        // let datasheet = get_datasheet(state, datasheet_id);
        // if state.is_none() || datasheet.is_none() {
        //     return None;
        // }

        // // Determine whether the currently operating view is the active view
        // if datasheet.active_view != view_id {
        //     panic!(t(Strings.error_modify_column_failed_wrong_target_view));
        // }

        let mut actions: Vec<ActionOTO> = Vec::new();
        let set_gantt_style_action = ViewAction::set_gantt_style_to_action(snapshot, SetGanttStyleOTO {
            view_id,
            data,
        }).unwrap();
        if let Some(action) = set_gantt_style_action {
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