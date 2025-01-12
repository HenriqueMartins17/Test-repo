use crate::{ot::{commands::{CollaCommandDefExecuteResult, SetViewFrozenColumnCountOptions, ExecuteResult}, types::{ActionOTO, ResourceType}, SetFrozenColumnCountOTO}, so::DatasheetSnapshotSO, DatasheetActions};

pub struct SetViewFrozenColumnCount {

}

impl SetViewFrozenColumnCount {
    pub fn execute (
        options: SetViewFrozenColumnCountOptions,
        snapshot: DatasheetSnapshotSO,
    ) -> anyhow::Result<Option<CollaCommandDefExecuteResult>> {
        // let state = context.state;
        let SetViewFrozenColumnCountOptions {cmd: _, count, view_id} = options;
        // let datasheet_id = get_active_datasheet_id(state).unwrap();
        // let datasheet = get_datasheet(state, datasheet_id);

        // if state.is_none() || datasheet.is_none() {
        //     return None;
        // }
        let datasheet_id = snapshot.datasheet_id.clone();

        let mut actions: Vec<ActionOTO> = Vec::new();
        let set_view_frozen_column_count_action = DatasheetActions::set_frozen_column_count_to_action(snapshot, SetFrozenColumnCountOTO { view_id, count }).unwrap();

        if let Some(action) = set_view_frozen_column_count_action {
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