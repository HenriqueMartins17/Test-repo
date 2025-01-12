use crate::{ot::{commands::{CollaCommandDefExecuteResult, SetKanbanStyleOptions, ExecuteResult}, types::{ActionOTO, ResourceType}, ViewAction, SetKanbanStyleOTO}, so::DatasheetSnapshotSO};


pub struct SetKanbanStyle {

}

impl SetKanbanStyle {
    pub fn execute (
        snapshot: DatasheetSnapshotSO,
        options: SetKanbanStyleOptions,
    ) -> anyhow::Result<Option<CollaCommandDefExecuteResult>> {
        let SetKanbanStyleOptions { cmd:_, view_id, style_key, style_value, add_record } = options;
        // let datasheet_id = get_active_datasheet_id(state).unwrap();
        let datasheet_id = snapshot.datasheet_id.clone();
        // let datasheet = get_datasheet(state, datasheet_id);
        // if state.is_none() || datasheet.is_none() {
        //     return None;
        // }

        // Determine whether the currently operating view is the active view
        // if datasheet.active_view != view_id {
        //     panic!(t(Strings.error_modify_column_failed_wrong_target_view));
        // }

        let mut actions: Vec<ActionOTO> = Vec::new();

        let set_kanban_style_action = ViewAction::set_view_style_to_action(snapshot.clone(), SetKanbanStyleOTO{
            style_key,
            style_value,
            view_id: view_id.clone(),
        }).unwrap();
        if let Some(action) = set_kanban_style_action {
            actions.push(action);
        }

        if add_record.is_some() {
            // let row_count = get_actual_row_count(state).unwrap();
            let current_view =  snapshot.meta.views.iter().find(|item| item.id.is_some() && item.id.clone().unwrap() == view_id).unwrap();
            let _row_count = current_view.rows.clone().unwrap().len();
            // let unit_id = state.user.info.unit_id;
            // let add_record_actions = AddRecords::execute(context, AddRecordsOptions {
            //     cmd: CollaCommandName::AddRecords,
            //     view_id,
            //     index: row_count,
            //     count: 1,
            //     cell_values: vec![{ style_value: vec![unit_id] }],
            // });

            // if let Some(add_record_actions) = add_record_actions {
            //     if add_record_actions.result == ExecuteResult::Fail {
            //         return Some(add_record_actions);
            //     }
            //     actions.extend(add_record_actions.actions);
            // }
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