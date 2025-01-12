use crate::{ot::{commands::{CollaCommandDefExecuteResult, SetGalleryStyleOptions, ExecuteResult}, types::{ResourceType, ActionOTO}, ViewAction, SetGalleryStyleOTO}, so::DatasheetSnapshotSO};

pub struct SetGalleryStyle {

}

impl SetGalleryStyle {
    pub fn execute (
        options: SetGalleryStyleOptions,
        snapshot: DatasheetSnapshotSO,
    ) -> anyhow::Result<Option<CollaCommandDefExecuteResult>> { 
        // let state = context.state;
        let view_id = options.view_id;
        // let datasheet_id = get_active_datasheet_id(state).unwrap();
        // let datasheet = get_datasheet(state, datasheet_id);
        // if state.is_none() || datasheet.is_none() {
        //     return None;
        // }

        // Determine whether the currently operating view is the active view
        // if datasheet.active_view != view_id {
        //     panic!(Strings::error_modify_column_failed_wrong_target_view);
        // }
        let datasheet_id = snapshot.datasheet_id.clone();

        let mut actions: Vec<ActionOTO> = Vec::new();
        let set_gallery_style_action = ViewAction::set_gallery_style_to_action(snapshot, SetGalleryStyleOTO{
            view_id: view_id.clone(),
            style_key: options.style_key.clone(),
            style_value: options.style_value.clone(),
        }).unwrap();
        // action && collected.push(action);
        if let Some(action) = set_gallery_style_action {
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