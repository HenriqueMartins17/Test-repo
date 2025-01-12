use std::collections::HashMap;
use std::rc::Rc;


use json0::operation::{PathSegment, OperationKind};
use serde_json::{json, Value};
use crate::fields::bind_field_context;
use crate::ot::commands::{CollaCommandDefExecuteResult, LinkedActions, ICollaCommandDef, CommandOptions};

use crate::ot::commands::{SetRecordOptions, SetRecordsOptions};
use crate::ot::commands::datasheet_action::set_record_to_action;
use crate::ot::types::{ActionOTO, ResourceType, SetRecordOTO};
use crate::ro::record_update_ro::SegmentType;
use crate::so::{FieldKindSO, DatasheetPackContext};
use crate::ot::commands::ExecuteResult;
use crate::utils::utils::handle_empty_cell_value;

pub struct SetRecords {

}

impl ICollaCommandDef for SetRecords {
    fn undoable(&self) -> bool {
        true
    }
    fn execute (
        &self,
        context: Rc<DatasheetPackContext>,
        options: CommandOptions
    ) -> anyhow::Result<Option<CollaCommandDefExecuteResult>> {
        let options =  match options {
            CommandOptions::SetRecordsOptions(new_option) => new_option,
            _ => panic!("SetRecords execute options error"),
        };
        let SetRecordsOptions { cmd: _, data, datasheet_id, internal_fix:_ } = options;

        // const fieldPermissionMap = Selectors.getFieldPermissionMap(state);
        // let data = data.into_iter().filter(|item| {
        //   let field_role = Selectors::getFieldRoleByFieldId(&fieldPermissionMap, &item.field_id);
        //   if let Some(field_role) = field_role {
        //     if field_role != ConfigConstant::Role::Editor {
        //         return false;
        //     }
        //   }
        //   true
        // }).collect::<Vec<_>>();
        if data.is_empty() {
            return Ok(None);
        }
        let mut field_map_snapshot = HashMap::new();
        // kind_map.insert("fldm00Cuo2tjC".to_string(), FieldKindSO::Text);
        let snapshot = context.datasheet_pack.snapshot.clone();
        let field_map = snapshot.meta.field_map.clone();
        let mut linked_actions = None;
        let actions: Vec<ActionOTO> = data.iter().enumerate().fold(Vec::new(), |mut collected, (_index, record_option)| {
            let SetRecordOptions { record_id, field_id, value, field:_ } = record_option;
            let field = field_map.get(field_id).unwrap();
            let mut value = value.clone();
            // println!("value pre is {:?}", value);
            // Number/currency/percentage fields need special processing, string to number, number of significant digits, etc.
            if field.kind == FieldKindSO::Number || field.kind == FieldKindSO::Currency || field.kind == FieldKindSO::Percent {
                if let Some(value_str) = value.as_str() {
                    value = Value::Number(serde_json::Number::from_f64(value_str.parse::<f64>().unwrap()).unwrap());
                } else if let Some(_value_num) = value.as_f64() {
                    // value = Some(value_num);
                } else if let Some(_value_num) = value.as_i64() {
                    // value = Some(value_num);
                } else {
                    value = Value::Null;
                }
            }
            if field.kind == FieldKindSO::URL && value.is_array() {
                let mut new_value = Vec::new();
                if let Some(value_arr) = value.as_array() {
                    for v in value_arr {
                        if let Some(v_obj) = v.as_object() {
                            let new_obj = json!({
                                "type": SegmentType::Url as i64,
                                "text": v_obj.get("link").unwrap_or(&v_obj.get("text").unwrap()),
                                "title": v_obj.get("title").unwrap_or(&v_obj.get("text").unwrap()),
                            });
                            new_value.push(new_obj);
                        }
                    }
                }
                value = Value::Array(new_value);
            }
            if field.kind == FieldKindSO::Link {
                if let Some(property) = &field.property {
                    //要两组snapshot
                    if property.brother_field_id.is_some() && property.foreign_datasheet_id != datasheet_id {
                        let foreign_datasheet_id = property.foreign_datasheet_id.as_ref().unwrap();
                        let mut linked_actions_tmp = Vec::new();
                        let mut linked_action = LinkedActions {
                            datasheet_id: foreign_datasheet_id.to_string(),
                            actions: Vec::new(),
                        };
                        let record_ids = value.as_array().unwrap();
                        let new_record_id = record_ids[0].as_str().unwrap();
                        let field_id = property.brother_field_id.as_ref().unwrap();
                        let str = Value::String(record_id.to_string());
                        let value = Value::Array(vec![str]);
                        let action = ActionOTO {
                            op_name: "OI".to_string(),
                            op: json0::Operation {
                                p: vec![
                                    PathSegment::String("recordMap".to_string()),
                                    PathSegment::String(new_record_id.to_string()),
                                    PathSegment::String("data".to_string()),
                                    PathSegment::String(field_id.clone()),
                                ],
                                kind: OperationKind::ObjectInsert {
                                    oi: value.clone(),
                                },
                            },
                        };
                        linked_action.actions.push(action);
                        linked_actions_tmp.push(linked_action);
                        linked_actions = Some(linked_actions_tmp);
                        //新的payload和snapshot
                        // let payload = SetRecordOTO {
                        //     record_id: record_id.clone(),
                        //     field_id: field_id.clone(),
                        //     value: value.clone(),
                        // };
                        // let action = set_record_to_action(snapshot.clone(), payload).unwrap();
                        // match action {
                        //     Some(action) => {
                        //         linked_action.actions.push(action);
                        //     }
                        //     None => {}
                        // }
                    }
                }
            }
            let field_type = bind_field_context(field.clone());
            value = handle_empty_cell_value(value, Some(field_type));
            // value = handleEmptyCellValue(value, Field.bindContext(field, state).basicValueType);
            // println!("value is {:?}", value);
            field_map_snapshot.insert(field.id.to_string(), field.clone());
            let payload = SetRecordOTO {
                record_id: record_id.clone(),
                field_id: field_id.clone(),
                value: value.clone(),
            };
            // println!("payload is {:?}", payload);
            let action = set_record_to_action(snapshot.clone(), payload).unwrap();
            // println!("action is {:?}", action);
            match action {
                Some(action) => {
                    collected.push(action);
                }
                None => {}
            }

            collected
        });
        Ok(Some(CollaCommandDefExecuteResult {
            result: ExecuteResult::Success,
            resource_id: datasheet_id.unwrap(),
            resource_type: ResourceType::Datasheet,
            actions,
            field_map_snapshot: Some(field_map_snapshot),
            linked_actions,
            ..Default::default()
        }))
    }
}