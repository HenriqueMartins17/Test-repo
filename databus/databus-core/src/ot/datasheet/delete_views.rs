use std::rc::Rc;

use json0::TransformSide;

use crate::{ot::{commands::{CollaCommandDefExecuteResult, ExecuteResult, ICollaCommandDef, CommandOptions}, types::ResourceType, json0_transform}, so::DatasheetPackContext, DatasheetActions, PayloadDelViewVO};

pub struct DeleteViews {

}

impl ICollaCommandDef for DeleteViews {
    fn undoable(&self) -> bool {
        true
    }
    fn execute (
        &self,
        context: Rc<DatasheetPackContext>,
        options: CommandOptions
    ) -> anyhow::Result<Option<CollaCommandDefExecuteResult>> {   
        let options =  match options {
            CommandOptions::DeleteViewsOptions(new_option) => new_option,
            _ => panic!("DeleteViews execute options error"),
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
            let view_id = record_option.view_id;

            if views.len() == 1 {
                // The last view cannot be deleted
                return collected;
            }

            // Check if view_id exists
            if !views.iter().any(|view| view.id.is_some() && view.id.clone().unwrap() == view_id) {
                // panic!(t(Strings.error_del_view_failed_not_found_target));
                panic!("Error: del view failed not found target");
            }

            let payload = PayloadDelViewVO {
                view_id,
            };
            let action = DatasheetActions::delete_view_to_action(snapshot.clone(), payload).unwrap();

            if let Some(action) = action {
                if !collected.is_empty() {
                    let transformed_action = json0_transform(
                        vec![action.clone()],
                        collected.clone(),
                        TransformSide::Right,
                    ).unwrap();
                    collected.extend(transformed_action);
                } else {
                    collected.push(action);
                }
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