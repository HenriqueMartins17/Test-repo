use crate::{ot::{commands::{CollaCommandDefExecuteResult, ManualSaveViewOptions, ExecuteResult}, types::{ActionOTO, ResourceType}, ViewOTO}, so::DatasheetSnapshotSO, DatasheetActions};

pub struct ManualSaveView {

}

impl ManualSaveView {
    pub fn execute (
        snapshot: Option<DatasheetSnapshotSO>,
        options: ManualSaveViewOptions,
    ) -> anyhow::Result<Option<CollaCommandDefExecuteResult>> { 
        // let state = context.state;
        // let field_map_snapshot = context.field_map_snapshot;
        let ManualSaveViewOptions {
            view_id,
            view_property,
            cmd:_,
        } = options;
        // let datasheet_id = selectors::get_active_datasheet_id(state).unwrap();
        // let snapshot = selectors::get_snapshot(state, datasheet_id);

        if snapshot.is_none() {
            return Ok(None);
        }
        let snapshot = snapshot.unwrap();
        let datasheet_id = snapshot.datasheet_id.clone();
        let field_map = snapshot.meta.field_map.clone();
        let manual_save_view_actions = DatasheetActions::manual_save_view_to_action(snapshot.clone(), ViewOTO { view_id, view_property }).unwrap();

        if manual_save_view_actions.is_none() {
            return Ok(None);
        }

        let actions: Vec<ActionOTO> = manual_save_view_actions.unwrap().into();

        Ok(Some(CollaCommandDefExecuteResult {
            result: ExecuteResult::Success,
            resource_id: datasheet_id.clone(),
            resource_type: ResourceType::Datasheet,
            actions,
            field_map_snapshot: Some(field_map),
            ..Default::default()
        }))
    }
}