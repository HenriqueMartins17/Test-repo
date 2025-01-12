use std::{collections::HashMap, rc::Rc};

use crate::{ot::{commands::{CollaCommandDefExecuteResult, DeleteRecordOptions, ExecuteResult, ICollaCommandDef, CommandOptions}, types::ResourceType, DeleteRecordsOTO}, so::{FieldKindSO, FieldSO, DatasheetPackContext}, DatasheetActions};

pub struct DeleteRecord {

}

impl ICollaCommandDef for DeleteRecord {
    fn undoable(&self) -> bool {
        true
    }
    fn execute (
        &self,
        context: Rc<DatasheetPackContext>,
        options: CommandOptions
    ) -> anyhow::Result<Option<CollaCommandDefExecuteResult>> {
        let options =  match options {
            CommandOptions::DeleteRecordOptions(new_option) => new_option,
            _ => panic!("DeleteRecord execute options error"),
        };
        // let Context { state, ldc_maintainer } = context;
        let DeleteRecordOptions { data, datasheet_id:_, cmd:_ } = options;
        // let datasheet_id = datasheet_id.unwrap_or_else(|| Selectors::get_active_datasheet_id(state).unwrap());
        // let snapshot = Selectors::get_snapshot(state, datasheet_id);
        let snapshot = context.datasheet_pack.snapshot.clone();
        let datasheet_id = snapshot.datasheet_id.to_string();
        let mut link_field: Vec<FieldSO> = Vec::new();
        for (_, field) in snapshot.meta.field_map.iter() {
            if field.kind == FieldKindSO::Link {
                link_field.push(field.clone());
            }
        }

        let _get_field_by_field_id = |field_id: String| -> FieldSO {
            // Selectors::get_field(state, field_id, datasheet_id)
            snapshot.meta.field_map.get(&field_id).unwrap().clone()
        };

        let actions = DatasheetActions::delete_records(snapshot.clone(), DeleteRecordsOTO {
            record_ids: data.clone(),
            // get_field_by_field_id,
            // state,
        }).unwrap();

        let mut field_relink_map: HashMap<String, HashMap<String, Vec<String>>> = HashMap::new();
        for field in link_field.iter().filter(|field| field.property.clone().unwrap().brother_field_id.is_none()) {
            let mut re_link_records: HashMap<String, Vec<String>> = HashMap::new();
            for (_, v) in snapshot.record_map.iter() {
                if let Some(link_records) = v.data.get(&field.id) {
                    let link_records = link_records.as_array().unwrap();
                    for id in link_records {
                        let id = id.as_str().unwrap().to_string();
                        re_link_records.entry(id).or_insert_with(Vec::new).push(v.id.clone());
                    }
                }
            }
            field_relink_map.insert(field.id.clone(), re_link_records);
        }

        for record_id in data.clone() {
            if let Some(record) = snapshot.record_map.get(&record_id) {
                for field in link_field.iter() {
                    let mut old_value: Option<Vec<String>> = None;
                    if let Some(_brother_field_id) = field.property.clone().unwrap().brother_field_id.clone() {
                        // old_value = record.data.get(&field.id);
                        let tmp = record.data.get(&field.id).unwrap();
                        old_value = Some(tmp.as_array().unwrap().iter().map(|item| item.as_str().unwrap().to_string()).collect());
                    } else {
                        old_value = field_relink_map.get(&field.id).and_then(|relink_map| relink_map.get(&record.id).and_then(|data| Some(data.clone())));
                        if let Some(mut old_value_tmp) = old_value {
                            // oldValue = oldValue?.filter(item => !data.includes(item));
                            // old_value = Some(old_value.into_iter().filter(|item| !data.contains(item)).collect());
                            old_value_tmp.retain(|item| !data.contains(item));
                            old_value = if old_value_tmp.is_empty() { None } else { Some(old_value_tmp) };
                        }
                    }

                    // if let Some(linked_snapshot) = Selectors::get_snapshot(state, field.property.foreign_datasheet_id) {
                    //     if let Some(old_value) = old_value {
                    //         if !old_value.is_empty() {
                    //             ldc_maintainer.insert(state, linked_snapshot.clone(), record.id.clone(), field.clone(), None, old_value);
                    //         }
                    //     } else {
                    //         Player::do_trigger(Events::app_error_logger, ErrorData {
                    //             error: Error::new(format!("foreignDatasheet: {} has been deleted", field.property.foreign_datasheet_id)),
                    //             meta_data: MetaData { foreign_datasheet_id: field.property.foreign_datasheet_id },
                    //         });
                    //     }
                    // }
                }
            }
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