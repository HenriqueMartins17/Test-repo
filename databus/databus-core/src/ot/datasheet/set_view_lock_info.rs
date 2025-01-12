use crate::{ot::{commands::{CollaCommandDefExecuteResult, SetViewLockInfoOptions, ExecuteResult}, types::{ActionOTO, ResourceType}, SetViewLockInfoOTO}, so::DatasheetSnapshotSO, DatasheetActions};

pub struct SetViewLockInfo {

}

impl SetViewLockInfo {
    pub fn execute (
        options: SetViewLockInfoOptions,
        snapshot: DatasheetSnapshotSO,
    ) -> anyhow::Result<Option<CollaCommandDefExecuteResult>> {
        // let state = context.state;
        let SetViewLockInfoOptions {cmd: _, data, view_id} = options;
        // let datasheet_id = get_active_datasheet_id(state).unwrap();
        // let datasheet = get_datasheet(state, datasheet_id);

        // if state.is_none() || datasheet.is_none() {
        //     return Ok(None);
        // }
        let datasheet_id = snapshot.datasheet_id.clone();

        let mut actions: Vec<ActionOTO> = Vec::new();
        let set_view_lock_action = DatasheetActions::set_view_lock_info_to_action(snapshot, SetViewLockInfoOTO { view_id, view_lock_info: data }).unwrap();

        if let Some(action) = set_view_lock_action {
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