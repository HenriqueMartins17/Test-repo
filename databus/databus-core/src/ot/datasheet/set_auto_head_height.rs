use crate::{ot::{commands::{CollaCommandDefExecuteResult, SetAutoHeadHeightOptions, ExecuteResult}, types::{ActionOTO, ResourceType}, SetAutoHeadHeightOTO}, so::DatasheetSnapshotSO, DatasheetActions};

pub struct SetAutoHeadHeight {

}

impl SetAutoHeadHeight {
    pub fn execute (
        options: SetAutoHeadHeightOptions,
        snapshot: DatasheetSnapshotSO,
    ) -> anyhow::Result<Option<CollaCommandDefExecuteResult>> { 
        // let state = context.state;
        let SetAutoHeadHeightOptions { is_auto, view_id, cmd:_ } = options;
        // let datasheet_id = get_active_datasheet_id(state)?;
        // let datasheet = get_datasheet(state, datasheet_id)?;

        // if state.is_none() || datasheet.is_none() {
        //     return None;
        // }

        // Determine whether the current operating view is the active view
        // if datasheet.active_view != view_id {
        //     return Err(Error::new(Strings.error_set_row_height_failed_wrong_target_view));
        // }
        let datasheet_id = snapshot.datasheet_id.clone();

        let mut actions: Vec<ActionOTO> = Vec::new();
        let set_auto_head_height_action = DatasheetActions::set_auto_head_height_to_action(snapshot, SetAutoHeadHeightOTO { view_id, is_auto }).unwrap();
        if let Some(action) = set_auto_head_height_action {
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