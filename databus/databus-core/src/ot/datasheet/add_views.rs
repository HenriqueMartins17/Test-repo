use std::rc::Rc;

use crate::{ot::{commands::{CollaCommandDefExecuteResult, ExecuteResult, ICollaCommandDef, CommandOptions}, types::ResourceType}, so::DatasheetPackContext, DatasheetActions, PayloadAddViewVO};

pub struct AddViews {

}

impl ICollaCommandDef for AddViews {
    fn undoable(&self) -> bool {
        true
    }
    fn execute (
        &self,
        context: Rc<DatasheetPackContext>,
        options: CommandOptions
    ) -> anyhow::Result<Option<CollaCommandDefExecuteResult>> {
        let options =  match options {
            CommandOptions::AddViewsOptions(new_option) => new_option,
            _ => panic!("AddViews execute options error"),
        };
    //     let state = context.state;
        let data = options.data;
    //     let datasheet_id = Selectors::get_active_datasheet_id(state).unwrap();
    //     let snapshot = Selectors::get_snapshot(state, datasheet_id);
        let snapshot = context.datasheet_pack.snapshot.clone();
        let views = snapshot.clone().meta.views;
        let datasheet_id = snapshot.datasheet_id.to_string();

    //     if data.is_empty() {
    //         return None;
    //     }

        let actions = data.into_iter().fold(Vec::new(), |mut collected, record_option| {
            let start_index = record_option.start_index;
            let view = record_option.view;

            if views.iter().any(|item| item.id == view.id) {
                // Equivalent Rust code to raise an error
                panic!("Error: create view failed duplicate view id");
            }

            let payload = PayloadAddViewVO {
                view: view.clone(),
                start_index,
            };
            let action = DatasheetActions::add_view_to_action(snapshot.clone(), payload).unwrap();

            if action.is_none() {
                return collected;
            }

            if !collected.is_empty() {
                // let transformed_action = jot::transform(vec![action.unwrap()], &collected, "right");
                // collected.extend(transformed_action);
                collected.push(action.unwrap());
            } else {
                collected.push(action.unwrap());
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