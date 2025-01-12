use crate::{ot::{commands::{CollaCommandDefExecuteResult, SetViewFilterOptions, ExecuteResult}, types::{ActionOTO, ResourceType}, SetFilterInfoOTO}, so::DatasheetSnapshotSO, DatasheetActions};

pub struct SetViewFilter {

}

impl SetViewFilter {
    pub fn execute (
        options: SetViewFilterOptions,
        snapshot: DatasheetSnapshotSO,
    ) -> anyhow::Result<Option<CollaCommandDefExecuteResult>> {
        let SetViewFilterOptions {cmd: _, data, view_id} = options;
        // let datasheet_id = get_active_datasheet_id(state).unwrap();
        // let datasheet = get_datasheet(state, datasheet_id);

        // if state.is_none() || datasheet.is_none() {
        //     return None;
        // }

        // Determine whether the currently operating view is the active view
        // if datasheet.active_view != view_id {
        //     panic!(t(Strings.error_filter_failed_wrong_target_view));
        // }
        let datasheet_id = snapshot.datasheet_id.clone();

        let mut actions: Vec<ActionOTO> = Vec::new();
        let set_filter_info_action = DatasheetActions::set_filter_info_to_action(snapshot, SetFilterInfoOTO { view_id, filter_info: data }).unwrap();

        // action && collected.push(action);
        if let Some(action) = set_filter_info_action {
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