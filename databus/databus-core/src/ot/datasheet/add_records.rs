use std::{collections::HashMap, rc::Rc};

use serde_json::{Value, from_value, to_value};

use crate::{ot::{commands::{CollaCommandDefExecuteResult, AddRecordsOptions, ExecuteResult, MemberFieldMaintainer, CommandOptions, ICollaCommandDef}, types::{ResourceType, ActionOTO}, FieldOTO}, so::{FieldKindSO, FieldSO, DatasheetPackContext}, DatasheetActions, utils::uuid::{get_new_ids, IDPrefix}, fields::property::FieldPropertySO, config::Role, PayloadAddRecordVO};

use super::get_field_role_by_field_id;

const MAX_RECORD_NUM: usize = 50000;
pub struct AddRecords {
}

impl ICollaCommandDef for AddRecords {
    fn undoable(&self) -> bool {
        true
    }
    fn execute (
        &self,
        context: Rc<DatasheetPackContext>,
        options: CommandOptions
    ) -> anyhow::Result<Option<CollaCommandDefExecuteResult>> { 
        let options =  match options {
            CommandOptions::AddRecordsOptions(new_option) => new_option,
            _ => panic!("AddRecords execute options error"),
        };
        let AddRecordsOptions { count, cell_values, group_cell_values, index, view_id, ignore_field_permission,.. } = options;
        // let datasheet_id = options.datasheet_id.unwrap_or_else(|| Selectors::get_active_datasheet_id(state).unwrap());
        // let snapshot = Selectors::get_snapshot(state, datasheet_id);
        // let field_permission_map = Selectors::get_field_permission_map(state, datasheet_id);
        let field_permission_map = Some(HashMap::new());
        // let loading = get_datasheet_loading(state, datasheet_id);

        // if loading {
        //     return Err(Error::new(t(Strings::datasheet_is_loading)));
        // }
        let snapshot = context.datasheet_pack.snapshot.clone();
        let datasheet_id = options.datasheet_id;
        let datasheet_id = if datasheet_id.is_some() {
            datasheet_id.unwrap()
        } else {
            snapshot.datasheet_id.clone()
        };

        let mut field_map_snapshot = HashMap::new();
        // let field_map = snapshot.meta.field_map.clone();
        // if count <= 0 || count.is_nan() {
        //     return Ok(None);
        // }
        if count <= 0 {
            return Ok(None);
        }
        if let Some(cell_values) = &cell_values {
            if cell_values.len() != count {
                return Err(anyhow::anyhow!("error_add_row_failed_wrong_length_of_value"));
            }
        }

        let record_ids = snapshot.record_map.keys().collect::<Vec<_>>();
        let new_record_ids = get_new_ids(IDPrefix::Record, count, if record_ids.is_empty() { snapshot.meta.views[0].rows.clone().unwrap().iter().map(|item| item.record_id.clone()).collect() } else { record_ids.clone().into_iter().map(|item| item.clone()).collect() });

        if record_ids.len() + new_record_ids.len() > MAX_RECORD_NUM {
            // return Err(Error::new(t(Strings::max_record_num_per_dst)));
            return Err(anyhow::anyhow!("max_record_num_per_dst"));
        }

        let mut link_field_ids: Vec<FieldSO> = Vec::new();
        let mut special_actions: Vec<ActionOTO> = Vec::new();
        let field_map = snapshot.meta.field_map.clone();

        for (_field_id, field) in field_map.iter() {
            if field.kind == FieldKindSO::Link && field.property.clone().unwrap().brother_field_id.is_some() {
                link_field_ids.push(field.clone());
            }
            if field.kind == FieldKindSO::CreatedBy {
                let uuids = field.property.clone().unwrap().uuids.clone().unwrap();
                let uuids_tmp = uuids.iter().map(|item| item.clone().unwrap()).collect::<Vec<_>>();
                // let uuid = state.user.info.and_then(|info| info.get("uuid"));
                let uuid = Some("uuid".to_string());
                if let Some(uuid) = uuid {
                    if !uuids_tmp.contains(&serde_json::Value::String(uuid.clone())) {
                        let mut uuids_new = uuids.clone();
                        uuids_new.push(Some(serde_json::Value::String(uuid)));
                        let new_field = FieldSO {
                            id: field.id.clone(),
                            kind: field.kind.clone(),
                            property: Some(FieldPropertySO {
                                uuids: Some(uuids_new),
                                ..field.property.clone().unwrap()
                            }),
                            ..field.clone()
                        };
                        let action = DatasheetActions::set_field_attr_to_action(snapshot.clone(), FieldOTO { field: new_field }).unwrap();
                        if let Some(action) = action {
                            special_actions.push(action);
                        }
                    }
                }
            }
        }
        let mut member_field_map: HashMap<String, Vec<String>> = HashMap::new();

        // Add a new record, the record may be a blank record, or there may be some initialized data,
        // The data of the initialized data has three parts:
        // 1. Copy a record, the original data in the target record
        // 2. There is a filter item, if the filter value is a certain value, the filter item will be included
        // 3. There is a group item, adding a record in a group will bring the data of the group
        // Assuming that a record is added, and the above three parts have corresponding data,
        // the weights will decrease in turn, that is, for the same field,
        // the data from the next-level source will be overwritten by the data from the previous-level source.

        let mut actions: Vec<ActionOTO> = new_record_ids.iter().enumerate().fold(Vec::new(), |mut collected, (_index, record_id)| {
        // let actions: Vec<ActionOTO> = new_record_ids.iter().enumerate().flat_map(|(i, record_id)| {
            // let user_info = state.user.info.unwrap();
            let user_info = None;
            let mut new_record = DatasheetActions::get_default_new_record(context.clone(), snapshot.clone(), record_id.clone(), Some(view_id.clone()), group_cell_values.clone(), user_info);
            if let Some(cell_values) = &cell_values {
                let mut pre_data:HashMap<String, Value> = match from_value(new_record.data.clone()) {
                    Ok(data) => data,
                    Err(_) => HashMap::new(),
                };
                pre_data.extend(cell_values[_index].clone());
                new_record.data = to_value(pre_data).unwrap();
            }
            // if let Some(cell_values) = cell_values {
            //     // new_record.data.extend(cell_values[i].clone());
            // }
            // Add a new record, which may be substituted into the initial value due to filtering, grouping, and copying a row.
            // If permission is set for one of the columns, and the current user does not have editing permission,
            // the data of the corresponding column needs to be filtered out when setting the data.
            // Because all data will pass here after processing, unified filtering is performed here
            if let Some(field_permission_map) = &field_permission_map {
                if !ignore_field_permission {
                    let mut filtered_data = HashMap::new();
                    let data_tmp = new_record.data.as_object().unwrap();
                    for (field_id, cell_value) in data_tmp {
                        if let Some(field_role) = get_field_role_by_field_id(Some(&field_permission_map), field_id) {
                            if field_role == Role::None || field_role == Role::Editor {
                                filtered_data.insert(field_id.clone(), cell_value.clone());
                            }
                        }
                    }
                    new_record.data = serde_json::to_value(filtered_data).unwrap();
                }
            }
            // If there is data in the member field in the added record, special processing is required.
            // In the current logic, the member field header will record the unitId of all members in the current column after deduplication,
            // Therefore, when a new record is added and there is initialization data,
            // and the content of the member field exists in the data,
            // it is necessary to check whether the newly added unitId exists in the header.
            // If it does not exist, the data of the member header needs to be updated synchronously

            // if let Some(record_data) = &new_record.data {
            if new_record.data != Value::Null {
                let mut updated_record_data = HashMap::new();
                let data_tmp = new_record.data.as_object().unwrap();
                for (field_id, cell_value) in data_tmp.iter() {
                    if let Some(field) = field_map.get(field_id) {
                        updated_record_data.insert(field_id.clone(), cell_value.clone());
                        field_map_snapshot.insert(field_id.clone(), field_map.get(field_id).unwrap().clone());
                        
                        if field.kind != FieldKindSO::Member {
                            continue;
                        }
                        let unit_ids = cell_value.as_array();
                        let unit_ids = if unit_ids.is_some() {
                            unit_ids.unwrap().iter().map(|item| item.as_str().unwrap().to_string()).collect::<Vec<_>>()
                        } else {
                            vec![]
                        };
                        if let Some(existing_unit_ids) = member_field_map.get_mut(field_id) {
                            existing_unit_ids.extend(unit_ids.clone());
                        } else {
                            member_field_map.insert(field_id.clone(), unit_ids.clone());
                        }
                    }
                }
                new_record.data = serde_json::to_value(updated_record_data).unwrap();
            }
            let action = DatasheetActions::add_record_to_action(snapshot.clone(), PayloadAddRecordVO {
                view_id: view_id.clone(),
                record: serde_json::to_value(&new_record).unwrap(),
                index: index + _index,
            }).unwrap();

            if action.is_none(){
                return collected;
            }
            link_field_ids.iter().for_each(|field| {
                if let Some(_value) = new_record.data.get(&field.id) {
                    // if let Some(linked_snapshot) = Selectors::get_snapshot(state, field.property.foreign_datasheet_id) {
                    //     if let Some(value) = value.as_ref() {
                    //         ldc_maintainer.insert(state, linked_snapshot, new_record.id.clone(), field.clone(), value.clone(), None);
                    //     }
                    // }
                }
            });
            collected.extend(action.unwrap());
            collected
        });

        let mut member_field_maintainer = MemberFieldMaintainer::new();
        for (field_id, cell_value_for_unit_ids) in member_field_map.iter() {
            let field = field_map.get(field_id).unwrap();
            let unit_ids = field.property.clone().unwrap().unit_ids.clone().unwrap_or_else(|| vec![]);
            let mut _unit_ids = unit_ids.iter().chain(cell_value_for_unit_ids.iter()).cloned().collect::<Vec<_>>();
            _unit_ids.sort();
            _unit_ids.dedup();
            if _unit_ids.len() != unit_ids.len() {
                member_field_maintainer.insert(field_id.clone(), _unit_ids, datasheet_id.clone());
            }
        }
        actions.extend(special_actions);

        let member_field_action = member_field_maintainer.flush_member_action(context.clone());
        if member_field_action.len() > 0 {
            actions.extend(member_field_action);
        }
        
        Ok(Some(CollaCommandDefExecuteResult {
            result: ExecuteResult::Success,
            resource_id: datasheet_id,
            resource_type: ResourceType::Datasheet,
            data: Some(serde_json::to_value(new_record_ids).unwrap()),
            actions,
            field_map_snapshot: Some(field_map_snapshot),
            ..Default::default()
        }))
    }
}