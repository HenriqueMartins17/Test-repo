use crate::{ot::{commands::{CollaCommandDefExecuteResult, SetViewAutoSaveOptions, ExecuteResult, ManualSaveViewOptions}, types::{ActionOTO, ResourceType}, SetViewAutoSaveOTO, CollaCommandName}, so::DatasheetSnapshotSO, DatasheetActions};

use super::ManualSaveView;

pub struct SetViewAutoSave {

}

impl SetViewAutoSave {
    pub fn execute (
        snapshot: Option<DatasheetSnapshotSO>,
        options: SetViewAutoSaveOptions,
    ) -> anyhow::Result<Option<CollaCommandDefExecuteResult>> {
        let SetViewAutoSaveOptions { auto_save, view_id, view_property, cmd:_ } = options;
        // let datasheet_id = Selectors::get_active_datasheet_id(state).unwrap();
        // let snapshot = Selectors::get_snapshot(state, datasheet_id);

        if snapshot.is_none() {
            return Ok(None);
        }
        let snapshot_new = snapshot.clone().unwrap();
        let datasheet_id = snapshot_new.datasheet_id.clone();
        let field_map_snapshot = snapshot_new.meta.field_map.clone();

        let set_view_auto_save_action = DatasheetActions::set_view_auto_save_to_action(snapshot_new, SetViewAutoSaveOTO { view_id: view_id.clone(), auto_save }).unwrap();

        if set_view_auto_save_action.is_none() {
            return Ok(None);
        }

        let mut actions: Vec<ActionOTO> = vec![set_view_auto_save_action.unwrap()];

        if let Some(view_property) = view_property {
            let manual_save_view_actions = ManualSaveView::execute(snapshot, ManualSaveViewOptions { cmd: CollaCommandName::ManualSaveView, view_id, view_property }).unwrap();

            if let Some(manual_save_view_actions) = manual_save_view_actions {
                // if manual_save_view_actions.result == ExecuteResult::Fail {
                //     return Some(manual_save_view_actions);
                // }
                actions.extend(manual_save_view_actions.actions);
            }
        }

        Ok(Some(CollaCommandDefExecuteResult {
            result: ExecuteResult::Success,
            resource_id: datasheet_id,
            resource_type: ResourceType::Datasheet,
            actions,
            field_map_snapshot: Some(field_map_snapshot),
            ..Default::default()
        }))
    }
}