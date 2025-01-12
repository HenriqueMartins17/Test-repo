use std::rc::Rc;

use crate::{ot::{commands::{CollaCommandDefExecuteResult, DeleteCommentOptions, ExecuteResult, ICollaCommandDef, CommandOptions}, DeleteCommentOTO, types::ResourceType}, so::DatasheetPackContext, DatasheetActions};

pub struct DeleteComment {
}

impl ICollaCommandDef for DeleteComment {
    fn execute (
        &self,
        context: Rc<DatasheetPackContext>,
        options: CommandOptions
    ) -> anyhow::Result<Option<CollaCommandDefExecuteResult>> { 
        let options =  match options {
            CommandOptions::DeleteCommentOptions(new_option) => new_option,
            _ => panic!("DeleteComment execute options error"),
        };
        let snapshot = context.datasheet_pack.snapshot.clone();
        let DeleteCommentOptions {
            cmd:_, 
            datasheet_id, 
            record_id, 
            comment, 
        } = options;
        let mut actions = Vec::new();
        let record = snapshot.record_map.get(&record_id);
        if record.is_none() {
            return Ok(None);
        }

        let delete_comment_action = DatasheetActions::delete_comment_to_action(DeleteCommentOTO {
            datasheet_id: datasheet_id.clone(),
            record_id,
            comments: vec![comment],
        }).unwrap();

        if delete_comment_action.is_empty() {
            return Ok(None);
        }

        actions.extend(delete_comment_action);

        Ok(Some(CollaCommandDefExecuteResult {
            result: ExecuteResult::Success,
            resource_id: datasheet_id,
            resource_type: ResourceType::Datasheet,
            actions,
            ..Default::default()
        }))
    }
}