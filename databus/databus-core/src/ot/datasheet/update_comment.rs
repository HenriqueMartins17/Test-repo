use std::rc::Rc;

use crate::{ot::{commands::{CollaCommandDefExecuteResult, UpdateCommentOptions, ExecuteResult, ICollaCommandDef, CommandOptions}, types::{ActionOTO, ResourceType}, UpdateCommentOTO}, so::DatasheetPackContext, DatasheetActions};

pub struct UpdateComment {

}

impl ICollaCommandDef for UpdateComment {
    fn execute (
        &self,
        context: Rc<DatasheetPackContext>,
        options: CommandOptions
    ) -> anyhow::Result<Option<CollaCommandDefExecuteResult>> {
        let options =  match options {
            CommandOptions::UpdateCommentOptions(new_option) => new_option,
            _ => panic!("UpdateComment execute options error"),
        };
        let UpdateCommentOptions {cmd: _, datasheet_id, record_id, comments, emoji_action} = options;
        let mut actions: Vec<ActionOTO> = Vec::new();
        let snapshot = context.datasheet_pack.snapshot.clone();
        let record = snapshot.record_map.get(&record_id);
    //     let record = get_record(&state, &recordId, &datasheetId);
        if record.is_none() {
            return Ok(None);
        }

        let update_comment_action = DatasheetActions::update_comment_to_action(UpdateCommentOTO {
            datasheet_id: datasheet_id.clone(),
            record_id,
            update_comments: vec![comments],
            emoji_action,
        }).unwrap();

        if update_comment_action.is_none() {
            return Ok(None);
        }

        actions.extend(update_comment_action.unwrap());

        Ok(Some(CollaCommandDefExecuteResult {
            result: ExecuteResult::Success,
            resource_id: datasheet_id,
            resource_type: ResourceType::Datasheet,
            actions,
            ..Default::default()
        }))
    }
}