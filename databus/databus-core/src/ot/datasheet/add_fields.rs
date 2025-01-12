use core::panic;
use std::{collections::HashMap, rc::Rc};
use serde_json::Value;

use crate::{ot::{commands::{CollaCommandDefExecuteResult, AddFieldsOptions, ExecuteResult, AddFieldOptions, create_new_field, SetRecordOptions, FixOneWayLinkOptions, ICollaCommandDef, CommandOptions, create_new_brother_field}, types::ResourceType, SetFrozenColumnCountOTO}, so::{FieldKindSO, FieldSO, DatasheetPackContext}, DatasheetActions, utils::uuid::{get_new_ids, IDPrefix}, fields::{property::FieldPropertySO, CreatedBy, field_factory::FieldFactory}};

pub struct AddFields {
    
}

fn convert_to_value(input: Vec<Option<String>>) -> Vec<Option<Value>> {
    input
        .into_iter()
        .map(|opt_str| opt_str.map(|s| Value::String(s)))
        .collect()
}

impl ICollaCommandDef for AddFields {
    fn undoable(&self) -> bool {
        true
    }
    fn execute (
        &self,
        context: Rc<DatasheetPackContext>,
        options: CommandOptions
    ) -> anyhow::Result<Option<CollaCommandDefExecuteResult>> {   
        let options =  match options {
            CommandOptions::AddFieldsOptions(new_option) => new_option,
            _ => panic!("AddFields execute options error"),
        };
        let AddFieldsOptions {
            data,
            copy_cell,
            internal_fix,
            field_id:_,
            datasheet_id:_,
            cmd:_,
        } = options;
        // let max_field_count_per_sheet = get_max_field_count_per_sheet();
        let max_field_count_per_sheet = 200;

        if data.is_empty() {
            return Ok(None);
        }
        let snapshot = context.datasheet_pack.snapshot.clone();
        let _views = snapshot.meta.views.clone();
        let datasheet_id = snapshot.datasheet_id.to_string();
        let record_map = snapshot.record_map.clone();

        let is_over_limit =
            data.len() + snapshot.meta.views[0].columns.len() > max_field_count_per_sheet;
        if is_over_limit {
            let error_message = format!(
                "{}",
                // t(Strings.columns_count_limit_tips, max_field_count_per_sheet)
                format!("columns count limit tips: {}", max_field_count_per_sheet)
            );
            panic!("{}", error_message);
        }
        let new_field_ids = get_new_ids(
            IDPrefix::Field,
            data.len(),
            snapshot.meta.field_map.keys().cloned().collect(),
        );
        let mut frozen_count_map = HashMap::new();

        let mut new_field_id = String::new();
        let mut linked_actions: Vec<crate::ot::commands::LinkedActions> = Vec::new();
        let actions = data.iter().enumerate().fold(Vec::new(), |mut collected, (_index, field_option)| {
            new_field_id = new_field_ids[_index.clone()].clone();
            let AddFieldOptions {
                index: column_index,
                view_id,
                data: field_data,
                field_id,
                offset:_,
                hidden_column:_,
            } = field_option.clone();
            let view = snapshot
                .meta
                .views
                .iter()
                .find(|view| view.id == view_id);
            let frozen_column_count = view.and_then(|view| view.frozen_column_count);

            // special handling for associated fields
            // When the table associated with the newly added associated field cannot be queried in the state,
            // an association cannot be established at this time.
            // Here we convert this field directly to a text field.
            let mut field_option = field_option.clone();
            if field_data.kind == FieldKindSO::Link
                && context.get_snapshot(field_data.property.clone().unwrap().foreign_datasheet_id.unwrap().as_str()).is_none()
            {
                field_option = AddFieldOptions {
                    data: FieldSO {
                        kind: FieldKindSO::Text,
                        property: None,
                        ..field_data.clone()
                    },
                    ..field_option.clone()
                };
            }

            let id = if field_data.id.clone().len() > 0 {
                field_data.id.clone()
            } else {
                new_field_id.clone()
            };
            let mut field = FieldSO {
                id,
                property: field_option.data.property.clone(),
                ..field_data.clone()
            };
            // Calculated fields need to determine their own datasheet through the field property,
            // here we force him to specify the datasheet_id of the current command
            if FieldFactory::create_field(field.clone(), context.clone()).is_computed() {
                field.property = Some(FieldPropertySO {
                    datasheet_id: Some(datasheet_id.clone()),
                    ..field.property.clone().unwrap()
                });
            }

            if field.kind == FieldKindSO::Link {
                field.property = field.property.map(|mut property| {
                    property.brother_field_id = None;
                    property
                });
                let linked_action = create_new_brother_field(context.clone(), &mut field, &datasheet_id);
                if let Some(linked_action) = linked_action {
                    linked_actions.push(linked_action);
                }
            }
            // AutoNumber needs to record the current view index
            if field.kind == FieldKindSO::AutoNumber {
                // let datasheet = get_datasheet(state, datasheet_id.clone());
                // let datasheet = state.datasheet.clone();
                // let view_idx = snapshot
                // .meta
                // .views
                // .iter()
                // .position(|item| item.id == datasheet.active_view)
                // .unwrap_or(0);
                // field.property = field.property.map(|mut property| {
                //     property.view_idx = Some(view_idx as i32);
                //     property
                // });
            }

            if field.kind == FieldKindSO::CreatedBy || field.kind == FieldKindSO::LastModifiedBy {
                // let uuids = (Field::bind_context(&field, state) as CreatedByField)
                let uuids = CreatedBy::get_uuids_by_record_map(record_map.clone());
                field.property = field.property.map(|mut property| {
                    property.uuids = Some(convert_to_value(uuids));
                    property
                });
            }
            let mut self_create_new_field = true;
            if let Some(internal_fix) = &internal_fix {
                if let Some(self_create_new_field_tmp) = internal_fix.self_create_new_field {
                    self_create_new_field = self_create_new_field_tmp;
                }
            }
            // let self_create_new_field = internal_fix.map(|fix| fix.self_create_new_field).unwrap_or(true);
            // Special repair one-way association, if False, this table does not create a new field
            if self_create_new_field {
                let action = create_new_field(&snapshot, field.clone(), Some(field_option));
                collected.extend(action);
            }

            if copy_cell.is_some() && copy_cell.unwrap() {
                let _record_data = record_map.iter().flat_map(|(record_id, record)| {
                    let data = record.data.clone();
                    if data.is_object() {
                        if let Some(value) = data.as_object().unwrap().get(&field_id.clone().unwrap()) {
                            let json_value = serde_json::from_value(value.clone()).unwrap();
                            Some(SetRecordOptions {
                                record_id: record_id.clone(),
                                field_id: new_field_id.clone(),
                                field: Some(field.clone()),
                                value: json_value,
                            })
                        } else {
                            None
                        }
                    }else {
                        None
                    }
                }).collect::<Vec<_>>();

                // let set_record = Box::new(SetRecords{});
                // let ret = set_record.execute(context.clone(), CommandOptions::SetRecordsOptions(SetRecordsOptions {
                //     cmd: CollaCommandName::SetRecords,
                //     data: record_data,
                //     internal_fix: internal_fix.clone(),
                //     ..Default::default()
                // })).unwrap();

                // if let Some(ret) = ret {
                //     if ret.result == ExecuteResult::Success {
                //         collected.extend(ret.actions);
                //     }
                // }
            }

            if let Some(linked_action) = linked_actions.first_mut() {
                if internal_fix.is_some() && internal_fix.clone().unwrap().change_one_way_link_dst_id.is_some() {
                    let field_id_tmp = &field_id.clone().unwrap();
                    let _action = linked_action.actions[0].clone();
                    let field = snapshot.meta.field_map.get(field_id_tmp).unwrap();
                    let brother_field_id = field.property.clone().unwrap().brother_field_id.unwrap();
                    let _fix_one_way_link_data = FixOneWayLinkOptions {
                        old_brother_field_id: brother_field_id,
                        // new_brother_field_id: linked_action.actions[0].li.field_id.clone(),
                        ..Default::default()
                    };
                    // linked_action.actions.last_mut().unwrap().oi.property.brother_field_id = Some(field_id_tmp);
                    // let result = fix_one_way_link_dst_id.execute(context, &FixOneWayLinkDstIdOptions {
                    //     cmd: CollaCommandName::FixOneWayLinkDstId,
                    //     data: vec![fix_one_way_link_data],
                    //     datasheet_id: datasheet_id.clone(),
                    //     field_id: field_id.clone().unwrap(),
                    // });

                    // if let Some(result) = result {
                    //     if result.result == ExecuteResult::Success {
                    //         collected.extend(result.actions);
                    //     }
                    // }
                }
            }

            if let Some(frozen_column_count) = frozen_column_count {
                if column_index < frozen_column_count {
                    let view_id_tmp = &view_id.clone().unwrap();
                    let count = frozen_count_map.get(view_id_tmp).cloned().unwrap_or(frozen_column_count) + 1;
                    let action = DatasheetActions::set_frozen_column_count_to_action(snapshot.clone(), SetFrozenColumnCountOTO {
                        view_id: view_id.clone().unwrap(),
                        count,
                    }).unwrap();

                    if let Some(action) = action {
                        frozen_count_map.insert(view_id.clone().unwrap(), frozen_column_count + 1);
                        collected.push(action);
                    }
                }
            }

            collected
        });

        if actions.is_empty() {
            Ok(None)
        } else {
            Ok(Some(CollaCommandDefExecuteResult {
                result: ExecuteResult::Success,
                resource_id: datasheet_id.clone(),
                resource_type: ResourceType::Datasheet,
                data: Some(serde_json::to_value(new_field_id).unwrap()),
                actions,
                datasheet_id: Some(datasheet_id.clone()),
                linked_actions: Some(linked_actions),
                ..Default::default()
            }))
        }
    }
}