use std::{collections::{HashMap, HashSet}, rc::Rc};

use cell::CellValueSo;
use serde_json::{Value, to_value};

use crate::{so::{FieldPermissionMap, FieldSO, DatasheetSnapshotSO, FieldKindSO, RecordSO, cell, DatasheetPackContext}, config::Role, ot::{commands::{LinkedActions, datasheet_action::set_record_to_action}, types::{ActionOTO, SetRecordOTO}, LinkedFieldActionsOTO, CellValueData, FieldOTO, RecordAlarmOTO, get_date_time_cell_alarm}, DatasheetActions, PayloadDelFieldVO, prelude::CellValueVo, fields::{get_is_computed, bind_field_context, property::FieldPropertySO, field_factory::FieldFactory}, utils::utils::handle_empty_cell_value};

pub struct FormulaEvaluateContext {
    // pub state: ReduxState,
    pub snapshot: DatasheetSnapshotSO,
    pub field: FieldSO,
    pub record: RecordSO,
}

pub struct FormulaContext {
    // pub state: ReduxState,
    pub snapshot: DatasheetSnapshotSO,
    pub field: FieldSO,
    pub field_map: HashMap<String, FieldSO>,
}

pub fn get_field_role_by_field_id(field_permission_map: Option<&FieldPermissionMap>, field_id: &str) -> Option<Role> {
    if let Some(map) = field_permission_map {
        if let Some(permission) = map.get(field_id) {
            return Some(permission.role.clone());
        }
        return Some(Role::None);
    }
    None
}

pub fn clear_old_brother_field(
    context: Rc<DatasheetPackContext>,
    snapshot: DatasheetSnapshotSO,
    old_field: &FieldSO,
    delete_field: Option<bool>,
) -> Option<LinkedActions> {
    // If the old field is not associated with a sibling field, no additional operations are required
    if old_field.property.is_some() && old_field.property.clone().unwrap().brother_field_id.is_none() {
        return Some(LinkedActions {
            datasheet_id: "".to_string(),
            actions: vec![],
        });
    }
    let foreign_datasheet_id = old_field.property.clone().unwrap().foreign_datasheet_id.clone().unwrap();
    let datasheet_id = snapshot.datasheet_id.clone();
    let new_context = context.clone();
    let foreign_snapshot = new_context.get_snapshot(&foreign_datasheet_id).unwrap();
    let foreign_field_map = &foreign_snapshot.meta.field_map;
    let brother_field_id = old_field.property.clone().unwrap().brother_field_id.clone().unwrap();
    let foreign_old_field = foreign_field_map.get(&brother_field_id);
    if foreign_old_field.is_none() {
        return None;
    }
    let foreign_old_field = foreign_old_field.unwrap();
    if foreign_old_field.property.is_some() && foreign_old_field.property.clone().unwrap().brother_field_id != Some(old_field.id.clone()) {
        return None;
    }
    if let Some(_delete_field) = delete_field {
        // delete field
        let actions = DatasheetActions::delete_field_to_action(
            foreign_snapshot.clone(),
            PayloadDelFieldVO {
                field_id: old_field.property.clone().unwrap().brother_field_id.clone().unwrap(),
                // datasheet_id: state.page_params.datasheet_id.clone().unwrap(),
                datasheet_id,
                view_id: None,
            },
        ).unwrap();

        return actions.map(|actions| LinkedActions {
            datasheet_id: old_field.property.clone().unwrap().foreign_datasheet_id.clone().unwrap(),
            actions,
        });
    }

    /*
     * Convert the sibling link field of the associated table to text type.
     */
    let actions = set_field(
        context,
        &foreign_snapshot,
        foreign_old_field,
        &FieldSO {
            id: foreign_old_field.id.clone(),
            name: foreign_old_field.name.clone(),
            kind: FieldKindSO::Text,
            // property: TextField::default_property(),
            ..Default::default()
        },
        None,
    ).unwrap()
    .actions;

    Some(LinkedActions {
        datasheet_id: old_field.property.clone().unwrap().foreign_datasheet_id.clone().unwrap(),
        actions,
    })
}

pub fn set_affect_field_attr_to_action(snapshot: &DatasheetSnapshotSO, field_id: &str) -> Vec<ActionOTO> {
    let mut actions = Vec::new();
    // if let Some(field_map) = &snapshot.meta.field_map {
        for field in snapshot.meta.field_map.values() {
            match field.kind {
                FieldKindSO::LastModifiedBy | FieldKindSO::LastModifiedTime => {
                    let mut field_id_collection = field.property.clone().unwrap().field_id_collection.clone().unwrap();
                    if let Some(index) = field_id_collection.iter().position(|id| id == field_id) {
                        field_id_collection.remove(index);
                        let new_field = FieldSO {
                            kind: field.kind.clone(),
                            property: Some(FieldPropertySO {
                                field_id_collection: Some(field_id_collection),
                                ..field.property.clone().unwrap()
                            }),
                            ..field.clone()
                        };
                        if let Some(action) = DatasheetActions::set_field_attr_to_action(snapshot.clone(), FieldOTO {field: new_field}).unwrap() {
                            actions.push(action);
                        }
                    }
                }
                _ => {}
            }
        }
    // }
    actions
}

pub fn set_field(
    context: Rc<DatasheetPackContext>,
    snapshot: &DatasheetSnapshotSO,
    old_field: &FieldSO,
    new_field: &FieldSO,
    _datasheet_id: Option<String>,
) -> anyhow::Result<LinkedFieldActionsOTO> {
    let mut readonly_fields = HashSet::new();
    readonly_fields.insert(FieldKindSO::Formula);
    readonly_fields.insert(FieldKindSO::LookUp);
    readonly_fields.insert(FieldKindSO::AutoNumber);
    readonly_fields.insert(FieldKindSO::CreatedTime);
    readonly_fields.insert(FieldKindSO::LastModifiedTime);
    readonly_fields.insert(FieldKindSO::CreatedBy);
    readonly_fields.insert(FieldKindSO::LastModifiedBy);
    
    // let state = &context.state;
    let mut actions: Vec<ActionOTO> = vec![];

    // When different types are converted to each other, the property needs to be updated
    // if new_field.kind != old_field.kind {
    //     let cell_values = DatasheetActions::get_cell_values_by_field_id(snapshot, &new_field.id);
    //     let std_vals = cell_values
    //         .iter()
    //         .map(|cv| Field::bind_context(old_field, state).cell_value_to_std_value(cv))
    //         .collect::<Vec<_>>();
    //     let property = Field::bind_context(new_field, state).enrich_property(&std_vals);
    //     let mut new_field = new_field.clone();
    //     new_field.property = property;

    //     // Calculated fields need to determine their own datasheet through the field property,
    //     // here we force him to specify the datasheetId of the current command
    //     if Field::bind_context(&new_field, state).is_computed() {
    //         let mut new_property = new_field.property.clone();
    //         new_property.datasheet_id = datasheet_id.clone();
    //         new_field.property = new_property;
    //     }

    //     if new_field.kind == FieldKindSO::CreatedBy || new_field.kind == FieldKindSO::LastModifiedBy {
    //         let uuids = (Field::bind_context(&new_field, state) as CreatedByField)
    //             .get_uuids_by_record_map(&snapshot.record_map);
    //         let mut new_property = new_field.property.clone();
    //         new_property.uuids = uuids;
    //         new_field.property = new_property;
    //     }
    // }

    // let validate_field_property_error = Field::bind_context(&new_field, state).validate_property().error;
    // if let Some(error) = validate_field_property_error {
    //     return Err(format!(
    //         "{}: {}",
    //         t(Strings.error_set_column_failed_bad_property),
    //         error.details.iter().map(|d| d.message.clone()).collect::<Vec<_>>().join(",\n")
    //     ));
    // }

    // When modifying a field, if the target field of the conversion is a calculated field or an initial non-editable field,
    // For LastModifiedBy/LastModifiedTime field type, fieldIdCollection needs to be updated
    if !readonly_fields.contains(&old_field.kind) && readonly_fields.contains(&new_field.kind) {
        let new_actions = set_affect_field_attr_to_action(snapshot, &new_field.id);
        actions.extend(new_actions);
    }

    let set_field_action = DatasheetActions::set_field_attr_to_action(snapshot.clone(), FieldOTO {
        field: new_field.clone(),
    }).unwrap();
    if let Some(set_field_action) = set_field_action {
        actions.push(set_field_action);
    }

    // Convert the value in record to type
    let _converted = create_convert_actions(context, snapshot, old_field, new_field);
    // actions.extend(converted.actions);

    /*
     * After the field is converted/deleted, the corresponding functions on the view,
     * such as filtering/grouping, need to be deleted or adjusted synchronously
     */
    // actions.extend(clear_view_attribute(snapshot, old_field, new_field));

    Ok(LinkedFieldActionsOTO {
        actions,
        linked_actions: None,
    })
}

fn create_convert_actions(
    context: Rc<DatasheetPackContext>,
    snapshot: &DatasheetSnapshotSO,
    old_field: &FieldSO,
    new_field: &FieldSO,
) -> Option<Vec<ActionOTO>> {
    if old_field.kind == new_field.kind {
        Some(change_field_setting(snapshot, old_field, new_field))
    } else {
        Some(switch_field_record_data(context, snapshot, old_field, new_field))
    }
}

fn change_field_setting(
    snapshot: &DatasheetSnapshotSO,
    old_field: &FieldSO,
    new_field: &FieldSO,
) -> Vec<ActionOTO> {
    let mut actions = Vec::new();
    if new_field.kind != old_field.kind {
        return actions;
    }
    match old_field.kind {
        FieldKindSO::MultiSelect | FieldKindSO::SingleSelect => {
            // let option_id_map = new_field.property.unwrap().options.iter().map(|option| {
            //     let tmp = option.iter().map(|select_property| (select_property.id.clone(), select_property)).collect::<HashMap<String, &SingleSelectProperty>>();
            //     tmp
            // }).collect::<HashMap<String, &SingleSelectProperty>>();
            let option_id_map = new_field.property.as_ref().unwrap().options.iter().flat_map(|option| {
                option.iter().map(|select_property| (select_property.id.clone(), select_property))
            }).collect::<HashMap<_, _>>();
            for (record_id, record) in snapshot.record_map.iter() {
                if let Some(cell_value) = record.data.get(&new_field.id) {
                    let mut convert_value: Value = if let Some(cell_value) = cell_value.as_array() {
                        let tmp: Vec<Value> = cell_value.iter().filter_map(|cv| {
                            if let Some(option) = option_id_map.get(cv.as_str().unwrap_or("")) {
                                Some(to_value(option.id.clone()).unwrap())
                            } else {
                                None
                            }
                        }).collect();
                        to_value(tmp).unwrap()
                    } else {
                        cell_value.clone()
                    };
                    if let Some(cell_value) = cell_value.as_str() {
                        if !option_id_map.contains_key(cell_value) {
                            convert_value = Value::Null;
                        }
                    }
                    if cell_value != &convert_value {
                        if let Some(action) = set_record_to_action(snapshot.clone(), SetRecordOTO {
                            record_id: record_id.clone(),
                            field_id: new_field.id.clone(),
                            value: convert_value.clone(),
                        }).unwrap() {
                            actions.push(action);
                        }
                    }
                }
            }
        }
        FieldKindSO::OneWayLink | FieldKindSO::Link => {
            if old_field.property.clone().unwrap().foreign_datasheet_id != new_field.property.clone().unwrap().foreign_datasheet_id {
                for (record_id, record) in snapshot.record_map.iter() {
                    if let Some(_cell_value) = record.data.get(&new_field.id) {
                        if let Some(action) = set_record_to_action(snapshot.clone(), SetRecordOTO {
                            record_id: record_id.clone(),
                            field_id: new_field.id.clone(),
                            value: Value::Null,
                        }).unwrap() {
                            actions.push(action);
                        }
                    }
                }
            }
        }
        FieldKindSO::Rating => {
            let max = new_field.property.clone().unwrap().max.clone().unwrap();
            if max < old_field.property.clone().unwrap().max.clone().unwrap() {
                for (record_id, record) in snapshot.record_map.iter() {
                    if let Some(cell_value) = record.data.get(&new_field.id) {
                        let cv = cell_value.as_i64().unwrap() as u32;
                        if cv > max {
                            if let Some(action) = set_record_to_action(snapshot.clone(), SetRecordOTO {
                                record_id: record_id.clone(),
                                field_id: new_field.id.clone(),
                                value: to_value(max).unwrap(),
                            }).unwrap() {
                                actions.push(action);
                            }
                        }
                    }
                }
            }
        }
        _ => {}
    }
    actions
}

fn switch_field_record_data(
    context: Rc<DatasheetPackContext>,
    snapshot: &DatasheetSnapshotSO,
    old_field: &FieldSO,
    new_field: &FieldSO,
) ->Vec<ActionOTO> {
    // let state = &context.state;
    // let ldc_maintainer = &context.ldc_maintainer;
    let mut actions = Vec::new();
    let need_create_link_data = new_field.kind == FieldKindSO::Link && new_field.property.clone().unwrap().brother_field_id.is_some();
    if FieldFactory::create_field(old_field.clone(), context.clone()).is_computed() &&
    FieldFactory::create_field(new_field.clone(), context.clone()).is_computed() {
        return actions;
    }
    for (record_id, _record) in snapshot.record_map.iter() {
        let cell_value = get_cell_value(
            snapshot.clone(), 
            record_id.clone(), 
            new_field.id.clone(),
        None,None,None
        );
        let mut set_value = |convert_value: Value| {
                let action = set_record_to_action(snapshot.clone(), SetRecordOTO {
                record_id: record_id.clone(),
                field_id: new_field.id.clone(),
                value: convert_value.clone(),
            }).unwrap();
            if let Some(action) = action {
                actions.push(action);
            }
            if need_create_link_data {
                // let linked_snapshot = get_snapshot(state, new_field.property.foreign_datasheet_id).unwrap();
                // ldc_maintainer.insert(
                //     state,
                //     linked_snapshot,
                //     record_id.clone(),
                //     new_field as &ILinkField,
                //     convert_value as Vec<String>,
                //     None,
                // );
            };
        };
        if let Some(_cell_value) = cell_value {
            if FieldFactory::create_field(new_field.clone(), context.clone()).is_computed() {
                set_value(Value::Null);
            } else {
                // let std_val = FieldFactory::create_field(&old_field.kind, old_field.clone(), &context).cell_value_to_std_value(cell_value);
                // let mut convert_value = FieldFactory::create_field(&new_field.kind, new_field.clone(), &context).std_value_to_cell_value(std_val);
                // convert_value = handle_empty_cell_value(convert_value, Some(FieldFactory::create_field(&new_field.kind, new_field.clone(), &context).basic_value_type()));
                // set_value(convert_value);
            }
        }
        if old_field.kind == FieldKindSO::DateTime {
            if let Some(_alarm) = get_date_time_cell_alarm(snapshot, record_id, &old_field.id) {
                let alarm_actions = DatasheetActions::set_date_time_cell_alarm(snapshot.clone(), RecordAlarmOTO {
                    record_id: record_id.clone(),
                    field_id: old_field.id.clone(),
                    alarm: None,
                }).unwrap();
                if let Some(alarm_actions) = alarm_actions {
                    actions.extend(alarm_actions);
                }
            }
        }
    };
    actions
}

/**
 * get cell value
 *
 * @param state
 * @param snapshot
 * @param record_id
 * @param field_id
 * @param with_error
 * @param datasheet_id
 * @param ignore_field_permission
 * @returns
 */
pub fn get_cell_value(
    // state,
    snapshot: DatasheetSnapshotSO,
    record_id: String,
    field_id: String,
    with_error: Option<bool>,
    datasheet_id: Option<String>,
    ignore_field_permission: Option<bool>,
) -> Option<CellValueSo> {
    // TODO: temp code for the first version of column permission, delete this logic in next version
    // let field_permission_map = get_field_permission_map(state, snapshot.datasheet_id);
    let field_permission_map = HashMap::new();
    let field_role = get_field_role_by_field_id(Some(&field_permission_map), &field_id);

    if ignore_field_permission.is_none() && field_role == Some(Role::None) {
        return None;
    }
    let datasheet_id_tmp = Some(snapshot.datasheet_id.clone());
    let ds_id = datasheet_id.or(datasheet_id_tmp);
    // .or(state.page_params.datasheet_id);
    let calc = || {
        calc_cell_value_and_string(
            // state,
            &snapshot,
            &field_id,
            &record_id,
            Some(ds_id.clone().unwrap().as_str()),
            with_error,
            ignore_field_permission,
        )
    };
    if ds_id.is_none() {
        return calc().cell_value;
    }
    let _ds_id_tmp = ds_id.clone().unwrap();
    // let cache_value = CacheManager::get_cell_cache(&ds_id_tmp, &field_id, &record_id);
    // if cache_value.is_some() {
    //     return cache_value.unwrap().cell_value;
    // }
    let res = calc();
    if res.ignore_cache.is_some() {
        // CacheManager::set_cell_cache(&ds_id_tmp, &field_id, &record_id, res.clone());
    }
    res.cell_value
}

pub fn calc_cell_value_and_string(
    // state: &IReduxState,
    snapshot: &DatasheetSnapshotSO,
    field_id: &str,
    record_id: &str,
    datasheet_id: Option<&str>,
    with_error: Option<bool>,
    ignore_field_permission: Option<bool>,
// ) -> Result<(Option<JsonValue>, Option<String>, bool), Box<dyn Error>> {
)-> CellValueData {
    let cell_value = calc_cell_value(
        // state,
        snapshot,
        field_id,
        record_id,
        with_error,
        datasheet_id,
        ignore_field_permission,
    );
    let field_map = &snapshot.meta.field_map;
    let field = field_map.get(field_id).ok_or("Field not found").unwrap();
    if cell_value.is_none() {
        return CellValueData {
            cell_value: None,
            cell_str: None,
            ignore_cache: Some(true),
        };
    }
    if field.kind == FieldKindSO::Attachment {
        let cell_value_tmp = cell_value.clone().unwrap();
        let cell_value_tmp = serde_json::from_value(cell_value_tmp).unwrap();
        match cell_value_tmp {
            CellValueVo::AttachmentCellValue(attachment_cells) => {
                // Do something with the attachment cells
                let cell_str = attachment_cells
                    .iter()
                    .map(|item| item.name.clone().unwrap())
                    .collect::<Vec<String>>()
                    .join(",");
                return CellValueData {
                    cell_value,
                    cell_str: Some(cell_str),
                    ignore_cache: Some(true),
                };
            },
            _ => {
                // Handle other variants of CellValueVo
            },
        }
    }
    // let instance = Field::bind_context(field, state)?;
    let _cell_str = if field.kind == FieldKindSO::URL {
        // Field::bind_context(field, state)?.cell_value_to_title(cell_value)
    } else {
        // instance.cell_value_to_string(cell_value)
    };
    CellValueData {
        cell_value,
        cell_str: Some("cell_str".to_string()),
        ignore_cache: Some(false),
        // ignoreCache: workerCompute() ? false : !instance.isComputed,
    }
    // Ok((cell_value, cell_str, !worker_compute() && !instance.is_computed()))
}

pub fn calc_cell_value(
    // state: &IReduxState,
    snapshot: &DatasheetSnapshotSO,
    field_id: &str,
    record_id: &str,
    with_error: Option<bool>,
    _datasheet_id: Option<&str>,
    ignore_field_permission: Option<bool>,
) -> Option<CellValueSo> {
    let field_map = &snapshot.meta.field_map;
    let field = field_map.get(field_id);
    // let field_permission_map = getFieldPermissionMap(state, snapshot.datasheet_id);
    let field_permission_map = HashMap::new();
    let field_role = get_field_role_by_field_id(Some(&field_permission_map), field_id);

    if !(ignore_field_permission.is_some() && ignore_field_permission.unwrap()) && field_role == Some(Role::None) {
        return None;
    }

    if field.is_none() {
        return None;
    }
    let field = field.unwrap();
    // let instance = Field::bind_context(field.unwrap(), state);

    if get_is_computed(field.clone()) {
        let cv = get_compute_cell_value(snapshot, record_id, field_id, with_error);
        // let cv = handle_empty_cell_value(cv, instance.basic_value_type());
        let kind = bind_field_context(field.clone());
        let cv = handle_empty_cell_value(cv.unwrap(), Some(kind));
        return Some(cv);
    }

    // get_entity_cell_value(state, snapshot, record_id, field_id, datasheet_id)
    None
}

pub fn get_compute_cell_value(
    // state: &IReduxState,
    snapshot: &DatasheetSnapshotSO,
    record_id: &str,
    field_id: &str,
    _with_error: Option<bool>,
) -> Option<CellValueSo> {
    let record_map = &snapshot.record_map;
    let field_map = &snapshot.meta.field_map;
    let field = field_map.get(field_id)?;
    let _record = record_map.get(record_id)?;

    match field.kind {
        // FieldKindSO::Formula => {
        //     get_formula_cell_value(snapshot, field, record, with_error)
        // }
        // FieldKindSO::LookUp => {
        //     Field::bind_context(field, state).get_cell_value(record_id, with_error)
        // }
        // FieldKindSO::CreatedBy | FieldKindSO::LastModifiedBy | FieldKindSO::CreatedTime | FieldKindSO::LastModifiedTime => {
        //     (Field::bind_context(field, state) as IAutomaticallyField).get_cell_value(record)
        // }
        // FieldKindSO::AutoNumber => {
        //     (Field::bind_context(field, state) as Any).get_cell_value(record, field_id)
        // }
        _ => None,
    }
}

pub fn get_formula_cell_value(_snapshot: &DatasheetSnapshotSO, _field: FieldSO, _record: RecordSO, _with_error: Option<bool>
) -> Option<CellValueVo> {
    // evaluate(field.property.unwrap().expression.unwrap().as_ref(), &FormulaEvaluateContext{
    //     snapshot: snapshot.clone(),
    //     field: field.clone(),
    //     record: record.clone(),
    // }, with_error.unwrap_or(false), false).unwrap()
    None
    // return evaluate(field.property.expression, { field, record, state }, withError, false);
}
