use crate::{ot::{commands::{CollaCommandDefExecuteResult, InsertCommentOptions, ExecuteResult}, types::{ActionOTO, ResourceType}, InsertCommentOTO}, so::{DatasheetSnapshotSO, Comments}, DatasheetActions, utils::uuid::{get_new_ids, IDPrefix}};

pub struct InsertComment {

}

impl InsertComment {
    pub fn execute (
        options: InsertCommentOptions,
        snapshot: Option<DatasheetSnapshotSO>,
    ) -> anyhow::Result<Option<CollaCommandDefExecuteResult>> { 
        // let state = &context.state;
        let InsertCommentOptions {
            datasheet_id,
            record_id,
            comments,
            cmd:_,
        } = options;

        // let snapshot = Selectors::get_snapshot(state, datasheet_id);

        if snapshot.is_none() {
            return Ok(None);
        }
        let snapshot = snapshot.unwrap();
        let record_map = snapshot.record_map.clone();

        if !record_map.contains_key(&record_id) {
            return Ok(None);
        }

        let mut actions: Vec<ActionOTO> = Vec::new();

        let record_comments = record_map.get(&record_id).unwrap().comments.clone();
        let mut comment_ids = if record_comments.is_some() {
            record_comments.unwrap().into_iter().map(|item| item.comment_id).collect::<Vec<_>>()
        }else {
            Vec::new()
        };
        let action = comments.iter().fold(Vec::new(), |mut collection, comment| {
            let comment_id = &get_new_ids(IDPrefix::Comment, 1, comment_ids.clone())[0];
            comment_ids.push(comment_id.clone());
            let insert_action = DatasheetActions::insert_comment_to_action(
                snapshot.clone(),
                InsertCommentOTO {
                    datasheet_id: datasheet_id.clone(),
                    record_id: record_id.clone(),
                    insert_comments: Some(vec![Comments {
                        comment_id: comment_id.clone(),
                        ..comment.clone()
                    }]),
                },
            ).unwrap();
            if insert_action.is_none() {
                return collection;
            }
            collection.extend(insert_action.unwrap());
            collection
        });

        // if action.is_empty() {
        //     return Ok(None);
        // }
        actions.extend(action);
        if actions.is_empty() {
            return Ok(None);
        }
        Ok(Some(CollaCommandDefExecuteResult {
            result: ExecuteResult::Success,
            resource_id: datasheet_id.clone(),
            resource_type: ResourceType::Datasheet,
            actions,
            datasheet_id: Some(datasheet_id),
            ..Default::default()
        }))
    }
}