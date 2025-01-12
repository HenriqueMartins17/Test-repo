use crate::{ot::{commands::{CollaCommandDefExecuteResult, SetColumnsPropertyOptions, ExecuteResult, ColumnsProperty}, types::{ResourceType, ActionOTO}}, so::DatasheetSnapshotSO, DatasheetActions, ColumnWidthOTO};

pub struct SetColumnsProperty {

}

impl SetColumnsProperty {
    pub fn execute (
        options: SetColumnsPropertyOptions,
        snapshot: DatasheetSnapshotSO,
    ) -> anyhow::Result<Option<CollaCommandDefExecuteResult>> {
    // fn execute(context: Context, options: Options) -> Option<ExecutionResult> {
        // let state = context.state;
        let SetColumnsPropertyOptions { field_id, view_id, data, cmd:_ } = options;
        let ColumnsProperty { width, stat_type } = data;
        // let datasheet_id = get_active_datasheet_id(state).unwrap();
        // let datasheet = get_datasheet(state, datasheet_id);
        let datasheet_id = snapshot.datasheet_id.to_string();
        // let datashhet = snapshot.datasheet;
        // let field_map = get_field_map(state, datasheet_id).unwrap();
        let field_map = snapshot.meta.field_map.clone();

        // if state.is_none() || datasheet.is_none() {
        //     return None;
        // }

        // Determine whether the currently operating view is the active view
        // if datasheet.active_view != view_id {
        //     panic!(t(Strings.error_modify_column_failed_wrong_target_view));
        // }

        if !field_map.contains_key(&field_id) {
            // panic!(t(Strings.error_modify_column_failed_column_not_exist));
            panic!("Error: modify column failed column not exist");
        }

        let mut actions: Vec<ActionOTO> = Vec::new();
        let payload = ColumnWidthOTO { view_id, field_id, width };
        if let Some(_width) = width {
            let change_columns_width_action = DatasheetActions::set_column_width_to_action(snapshot.clone(), payload.clone()).unwrap();
            if let Some(action) = change_columns_width_action {
                actions.push(action);
            }
        }
        if let Some(_stat_type) = stat_type {
            let change_field_stat_action = DatasheetActions::set_column_width_to_action(snapshot.clone(), payload).unwrap();
            if let Some(action) = change_field_stat_action {
                actions.push(action);
            }
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