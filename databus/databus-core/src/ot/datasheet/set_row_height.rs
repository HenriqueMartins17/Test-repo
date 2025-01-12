use crate::{ot::{commands::{CollaCommandDefExecuteResult, SetRowHeightOptions, ExecuteResult}, types::{ActionOTO, ResourceType}, SetRowHeightLevelOTO}, so::DatasheetSnapshotSO, DatasheetActions};


pub struct SetRowHeight {

}

impl SetRowHeight {
    pub fn execute (
        snapshot: DatasheetSnapshotSO,
        options: SetRowHeightOptions,
    ) -> anyhow::Result<Option<CollaCommandDefExecuteResult>> {
        let SetRowHeightOptions { level, view_id, cmd:_ } = options;
        // let datasheet_id = get_active_datasheet_id(state)?;
        let datasheet_id = snapshot.datasheet_id.clone();
        // let datasheet = get_datasheet(state, datasheet_id)?;

        // if state.is_none() || datasheet.is_none() {
        //     return None;
        // }

        // Determine whether the currently operating view is the active view
        // if datasheet.active_view != view_id {
        //     return Err(Error::new(Strings.error_set_row_height_failed_wrong_target_view));
        // }

        let mut actions: Vec<ActionOTO> = Vec::new();
        let level = level as i32;
        let set_row_height_action = DatasheetActions::set_row_height_level_to_action(snapshot, SetRowHeightLevelOTO { view_id, level }).unwrap();
        // action && collected.push(action);
        if let Some(action) = set_row_height_action {
            actions.push(action);
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