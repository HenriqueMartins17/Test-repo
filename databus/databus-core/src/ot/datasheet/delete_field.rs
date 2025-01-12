use std::rc::Rc;

use json0::TransformSide;
use serde_json::Value;

use crate::{ot::{commands::{CollaCommandDefExecuteResult, ExecuteResult, LinkedActions, datasheet_action::set_record_to_action, CommandOptions, ICollaCommandDef}, types::{ResourceType, ActionOTO, SetRecordOTO}, datasheet::clear_old_brother_field, json0_transform}, so::{FieldKindSO, DatasheetPackContext}, DatasheetActions, PayloadDelFieldVO};

use super::set_affect_field_attr_to_action;

pub struct DeleteField {

}

impl ICollaCommandDef for DeleteField {
    fn undoable(&self) -> bool {
        true
    }
    fn execute (
        &self,
        context: Rc<DatasheetPackContext>,
        options: CommandOptions
    ) -> anyhow::Result<Option<CollaCommandDefExecuteResult>> {   
        let options =  match options {
            CommandOptions::DeleteFieldOptions(new_option) => new_option,
            _ => panic!("DeleteField execute options error"),
        };
        // let state = &context.state;
        let data = &options.data;
        // let datasheet_id = options.datasheet_id.unwrap_or_else(|| Selectors::get_active_datasheet_id(state).unwrap());
        // let snapshot = Selectors::get_snapshot(state, datasheet_id);
        // 单项关联这里获取的snapshot是另一张表的数据
        // if snapshot.is_none() {
        //     return Ok(None);
        // }
        // let snapshot = snapshot.unwrap();
        let snapshot = context.datasheet_pack.snapshot.clone();
        let datasheet_id = snapshot.datasheet_id.to_string();
        let mut actions: Vec<ActionOTO> = Vec::new();
        let mut linked_actions: Vec<LinkedActions> = Vec::new();

        // delete all cellValues of this field
        for field_data in data {
            let field_id = field_data.field_id.clone();
            if snapshot.meta.field_map.get(&field_id).is_none() {
                // throw new Error(t(Strings.field_had_deleted));
                return Err(anyhow::anyhow!("field_had_deleted"));
            }
            let mut keys = snapshot.record_map.keys().cloned().collect::<Vec<_>>();
            keys.sort();
            for record_id in keys {
                let action = set_record_to_action(snapshot.clone(), SetRecordOTO {
                    record_id: record_id.clone(),
                    field_id: field_id.clone(),
                    value: Value::Null,
                }).unwrap();
                if let Some(action) = action {
                    actions.push(action);
                }
            }
            // For LastModifiedBy/LastModifiedTime field type, field_id_collection needs to be updated
            let new_actions = set_affect_field_attr_to_action(&snapshot, &field_id);
            actions.extend(new_actions);
        }

        // let view_id = state.page_params.view_id;
        let view_id = "view_id".to_string();
        let collected: Vec<ActionOTO> = data.iter().enumerate().fold(Vec::new(), |mut collected, (_index, field_data)| {
            let field_id = field_data.field_id.clone();
            let delete_brother_field = field_data.delete_brother_field;
            if let Some(field) = snapshot.meta.field_map.get(&field_id) {
                if field.kind == FieldKindSO::Link {
                    if let Some(linked_action) = clear_old_brother_field(context.clone(), snapshot.clone(), field, delete_brother_field) {
                        linked_actions.push(linked_action);
                    }
                }

                let action = DatasheetActions::delete_field_to_action(snapshot.clone(), PayloadDelFieldVO {
                    field_id: field_id.clone(),
                    datasheet_id: datasheet_id.clone(),
                    view_id: Some(view_id.clone()),
                }).unwrap();
                
                if action.is_none() {
                    return collected;
                }
                let mut action = action.unwrap();
                if collected.len() > 0 {
                    action = json0_transform(action, collected.clone(), TransformSide::Right).unwrap();
                }
                collected.extend(action);
                collected
            } else {
                Vec::new()
            }
        });

        actions.extend(collected);

        Ok(Some(CollaCommandDefExecuteResult {
            result: ExecuteResult::Success,
            resource_id: datasheet_id,
            resource_type: ResourceType::Datasheet,
            actions,
            linked_actions: Some(linked_actions),
            ..Default::default()
        }))
    }
}