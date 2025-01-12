
use std::rc::Rc;

use crate::{ot::{commands::{CollaCommandDefExecuteResult, ExecuteResult, ModifyView, ICollaCommandDef, CommandOptions}, types::ResourceType}, so::DatasheetPackContext, DatasheetActions, ModifyViewOTO};

pub struct ModifyViews {

}

impl ICollaCommandDef for ModifyViews {
    fn undoable(&self) -> bool {
        true
    }
    fn execute (
        &self,
        context: Rc<DatasheetPackContext>,
        options: CommandOptions,
    ) -> anyhow::Result<Option<CollaCommandDefExecuteResult>> {
        let options =  match options {
            CommandOptions::ModifyViewsOptions(new_option) => new_option,
            _ => panic!("ModifyViews execute options error"),
        };
        // let state = context.state;
        let data = options.data;
        // let datasheet_id = Selectors::get_active_datasheet_id(state).unwrap();
        // let snapshot = Selectors::get_snapshot(state, datasheet_id);
        let snapshot = context.datasheet_pack.snapshot.clone();
        let views = snapshot.meta.views.clone();
        let datasheet_id = snapshot.datasheet_id.to_string();

        // if data.is_empty() {
        //     return None;
        // }

        let actions = data.into_iter().fold(Vec::new(), |mut collected, record_option| {
            let ModifyView { view_id, key, value } = record_option;

            // character is too long or not filled
            // if key == "name" && (value.len() > (getCustomConfig().VIEW_NAME_MAX_COUNT.unwrap_or(30)) || value.len() < 1) {
            //     return collected;
            // }
            if key == "name" && value.is_string() && (value.as_str().unwrap().len() > 30 || value.as_str().unwrap().len() < 1) {
                return collected;
            }

            // Check if there is a view with the same name
            if key == "name" && views.iter().any(|view| view.name.as_deref() == value.as_str()) {
                // panic!(t(Strings.error_modify_view_failed_duplicate_name));
                panic!("Error: modify view failed duplicate name");
            }
            // Check if viewId exists
            if !views.iter().any(|view| view.id.clone().unwrap() == view_id) {
                // panic!(t(Strings.error_modify_view_failed_not_found_target));
                panic!("Error: modify view failed not found target");
            }
            let action = DatasheetActions::modify_view_to_action(snapshot.clone(), ModifyViewOTO { view_id, key, value }).unwrap();
            if let Some(action) = action {
                collected.extend(action);
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