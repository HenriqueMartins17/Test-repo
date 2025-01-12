use std::rc::Rc;

use serde_json::Value;

use crate::{ot::{commands::{CollaCommandDefExecuteResult, SetFieldAttrOptions, ExecuteResult, InternalFix, LinkedActions, datasheet_action::set_record_to_action, ICollaCommandDef, CommandOptions}, types::{ActionOTO, ResourceType, SetRecordOTO}, LinkedFieldActionsOTO}, so::{DatasheetSnapshotSO, FieldKindSO, FieldSO, DatasheetPackContext}, fields::{property::FieldPropertySO, validate_property}};

use super::set_field;

fn generate_linked_field_actions(
    context: Rc<DatasheetPackContext>,
    snapshot: &DatasheetSnapshotSO,
    old_field: FieldSO,
    new_field: FieldSO,
    datasheet_id: String,
    _delete_brother_field: Option<bool>,
    internal_fix: Option<InternalFix>,
) -> LinkedFieldActionsOTO {
    let mut actions: Vec<ActionOTO> = Vec::new();
    let linked_actions: Option<Vec<LinkedActions>> = None;
    // let state = context.state;
    if old_field.kind == FieldKindSO::Link && new_field.kind == FieldKindSO::Link {
        // If the associated table id has not changed, no related operations are required.
        if old_field.property.is_some() && new_field.property.is_some() &&
        old_field.property.clone().unwrap().foreign_datasheet_id == new_field.property.clone().unwrap().foreign_datasheet_id {
            return set_field(context, snapshot, &old_field, &new_field, Some(datasheet_id)).unwrap();
        }
    }

    if old_field.kind == FieldKindSO::Link {
        // let cleared_actions = clear_old_brother_field(context, old_field, delete_brother_field);
        // if let Some(cleared_actions) = cleared_actions {
        //     linked_actions.get_or_insert(Vec::new()).push(cleared_actions);
        // }
    }

    if new_field.kind == FieldKindSO::Link {
        // let created_actions = create_new_brother_field(state, new_field, datasheet_id);
        // if let Some(created_actions) = created_actions {
        //     linked_actions.get_or_insert(Vec::new()).push(created_actions);
        // }
    }

    if new_field.kind == FieldKindSO::Text && internal_fix.is_some() && internal_fix.unwrap().clear_one_way_link_cell.is_some() {
        let field_id = old_field.id;
        // Clean up the content of the one-way association cell
        if let Some(_field) = snapshot.meta.field_map.get(&field_id) {
            for (record_id, _) in &snapshot.record_map {
                if let Some(action) = set_record_to_action(snapshot.clone(), SetRecordOTO {
                    record_id: record_id.clone(),
                    field_id: field_id.clone(),
                    value: Value::Null,
                }).unwrap() {
                    actions.push(action);
                }
            }
        }
    }

    // let new_field_data = set_field(context, snapshot, old_field, new_field, datasheet_id);
    // actions.extend(new_field_data.actions);
    LinkedFieldActionsOTO {
        actions,
        linked_actions,
    }
    // (actions, linked_actions)
}

pub struct SetFieldAttr {

}

impl ICollaCommandDef for SetFieldAttr {
    fn undoable(&self) -> bool {
        true
    }
    fn execute (
        &self,
        context: Rc<DatasheetPackContext>,
        options: CommandOptions
    ) -> anyhow::Result<Option<CollaCommandDefExecuteResult>> {
        let options =  match options {
            CommandOptions::SetFieldAttrOptions(new_option) => new_option,
            _ => panic!("SetFieldAttr execute options error"),
        };
        // let active_datasheet_id = Selectors::get_active_datasheet_id(state).unwrap();
        let SetFieldAttrOptions {
            cmd:_,
            datasheet_id:_,
            field_id,
            data,
            delete_brother_field,
            internal_fix,
        } = options;
        // let snapshot = Selectors::get_snapshot(state, datasheet_id);
        let mut new_field = data.clone();
        if field_id != new_field.id {
            return Ok(None);
        }
        let snapshot = context.datasheet_pack.snapshot.clone();
        let datasheet_id = snapshot.datasheet_id.clone();
        // let field_map = getFieldMap(state, datasheet_id).unwrap();
        let field_map = snapshot.meta.field_map.clone();
        let old_field = field_map.get(&field_id);
        if old_field.is_none() {
            return Ok(None);
        }
        let old_field = old_field.clone().unwrap();
        // Check for duplicate names
        let duplicate = field_map.values().any(|f| f.id != field_id && f.name == data.name);
        
        if duplicate {
            // return Err(Error::new(t(Strings.error_set_column_failed_duplicate_column_name)));
            return Err(anyhow::Error::msg("error_set_column_failed_duplicate_column_name"));
        }
        
        if old_field.kind == FieldKindSO::NotSupport || new_field.kind == FieldKindSO::NotSupport {
            // return Err(Error::new(t(Strings.error_set_column_failed_no_support_unknown_column)));
            return Err(anyhow::Error::msg("error_set_column_failed_no_support_unknown_column"));
        }
        if old_field.kind == new_field.kind &&
            old_field.name == new_field.name &&
            old_field.desc == new_field.desc &&
            old_field.required == new_field.required &&
            old_field.property == new_field.property {
            return Ok(None);
        }
        // Compatible with errors caused by defaultValue of some online fields being null
        if [FieldKindSO::Currency, FieldKindSO::Percent, FieldKindSO::Number].contains(&new_field.kind) &&
            new_field.property.is_some() && new_field.property.clone().unwrap().default_value.is_none() {
            new_field.property = Some(FieldPropertySO {
                default_value: Some(Value::String("".to_string())),
                ..new_field.property.unwrap()
            });
        }
        
        // AutoNumber needs to record the current view index
        if new_field.kind == FieldKindSO::AutoNumber {
            // let datasheet = get_datasheet(state);
            let view_idx = snapshot.meta.views.iter().position(|item| item.id == Some(datasheet_id.clone())).unwrap_or(0);
            // new_field.property.view_idx = Some(view_idx);
            let view_idx = view_idx as i32;
            new_field.property = Some(FieldPropertySO {
                view_idx: Some(view_idx),
                ..new_field.property.unwrap()
            });
        }
        
        // Ensure that the properties of the following fields must have datasheetId
        if new_field.property.is_some() && !new_field.property.clone().unwrap().datasheet_id.is_some() &&
            [FieldKindSO::AutoNumber, FieldKindSO::CreatedBy, FieldKindSO::CreatedTime, FieldKindSO::LastModifiedBy, FieldKindSO::LastModifiedTime].contains(&new_field.kind) {
            // new_field.property.datasheet_id = Some(datasheet_id);
            new_field.property = Some(FieldPropertySO {
                datasheet_id: Some(datasheet_id.clone()),
                ..new_field.property.unwrap()
            });
        }
        
        // When modifying the associated field, it is necessary to maintain the sibling field data of the associated table
        if old_field.kind == FieldKindSO::Link || new_field.kind == FieldKindSO::Link {
            // let validate_field_property_error = Field::bind_context(&new_field, state).validate_property().error;
            let validate_field_property_error = validate_property(new_field.clone()).error;
            if validate_field_property_error.is_some() {
                // let error_message = format!("{}: {}", t(Strings.error_set_column_failed_bad_property), validate_field_property_error.details.iter().map(|d| d.message).collect::<Vec<_>>().join(",\n"));
                // return Err(Error::new(error_message));
                return Err(anyhow::Error::msg("error_set_column_failed_bad_property"));
            }
            
            let result = generate_linked_field_actions(context, &snapshot, old_field.clone(), new_field, datasheet_id.clone(), delete_brother_field, internal_fix);
            let linked_actions = result.linked_actions.unwrap_or_default();
            
            return Ok(Some(CollaCommandDefExecuteResult {
                result: ExecuteResult::Success,
                resource_id: datasheet_id,
                resource_type: ResourceType::Datasheet,
                actions: result.actions,
                linked_actions: Some(linked_actions),
                ..Default::default()
            }));
        }
        let actions = set_field(context, &snapshot, old_field, &new_field, Some(datasheet_id.clone())).unwrap().actions;
        Ok(Some(CollaCommandDefExecuteResult {
            result: ExecuteResult::Success,
            resource_id: datasheet_id,
            resource_type: ResourceType::Datasheet,
            actions,
            ..Default::default()
        }))
    }
}