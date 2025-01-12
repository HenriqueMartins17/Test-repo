use crate::{ot::{commands::{CollaCommandDefExecuteResult, MoveViewsOptions, ExecuteResult}, types::ResourceType}, so::DatasheetSnapshotSO, DatasheetActions, PayloadMoveViewVO};

pub struct MoveViews {

}

impl MoveViews {
    pub fn execute (
        options: MoveViewsOptions,
        snapshot: Option<DatasheetSnapshotSO>,
    ) -> anyhow::Result<Option<CollaCommandDefExecuteResult>> {
        // let state = context.state;
        let data = options.data;
        // let datasheet_id = Selectors::get_active_datasheet_id(state).unwrap();
        // let snapshot = Selectors::get_snapshot(state, datasheet_id);
        if snapshot.is_none() {
            return Ok(None);
        }
        let snapshot = snapshot.unwrap();
        let views = snapshot.meta.views.clone();
        let datasheet_id = snapshot.datasheet_id.to_string();

        // if data.is_empty() {
        //     return None;
        // }

        let actions = data.into_iter().fold(Vec::new(), |mut collected, record_option| {
            let new_index = record_option.new_index;
            let view_id = record_option.view_id;

            // Check if viewId exists

            if !views.iter().any(|view| view.id.is_some() && view.id.clone().unwrap() == view_id) {
                // panic!(t(Strings.error_move_view_failed_not_found_target));
                panic!("Error: move view failed not found target");
            }
            let payload = PayloadMoveViewVO {
                view_id,
                target: new_index,
            };
            let action = DatasheetActions::move_view_to_action(snapshot.clone(), payload).unwrap();
            if let Some(action) = action {
                collected.push(action);
            }
            collected
        });

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