use std::rc::Rc;

use crate::{so::{FieldSO, DatasheetSnapshotSO, DatasheetPackContext, FieldKindSO}, ot::{commands::{LinkedActions, AddFieldOptions}, types::ActionOTO}, utils::uuid::{get_new_id, IDPrefix, get_uniq_name}, fields::{property::FieldPropertySO, get_field_default_property}, DatasheetActions, PayloadAddFieldVO};


pub fn create_new_brother_field(
    context: Rc<DatasheetPackContext>,
    new_field: &mut FieldSO, 
    datasheet_id: &str
) -> Option<LinkedActions> {
    // If the new field is the associated table, no action is required
    if new_field.property.is_some() && new_field.property.clone().unwrap().foreign_datasheet_id == Some(datasheet_id.to_string()) {
        return Some(LinkedActions {
            datasheet_id: String::new(),
            actions: vec![],
        });
    }
    
    let foreign_snapshot = context.get_snapshot(datasheet_id).unwrap();
    let foreign_field_map = foreign_snapshot.meta.field_map.clone();
    let foreign_field_ids = foreign_field_map.keys().cloned().collect::<Vec<_>>();
    let foreign_field_new_id = get_new_id(IDPrefix::Field, foreign_field_ids.clone());
    new_field.property = Some(FieldPropertySO {
        brother_field_id: Some(foreign_field_new_id.clone()),
        ..new_field.property.clone().unwrap()
    });
    
    // Create a field that is a sibling field of each other in the associated table.
    let actions = create_new_field(&foreign_snapshot, FieldSO {
        id: foreign_field_new_id,
        name: get_uniq_name("New datasheet", &foreign_field_ids.iter().map(|id| foreign_field_map[id].name.to_string()).collect::<Vec<_>>()),
        kind: FieldKindSO::Link,
        property: Some(FieldPropertySO {
            foreign_datasheet_id: Some(datasheet_id.to_string()),
            brother_field_id: Some(new_field.id.clone()),
            ..Default::default()
        }),
        ..Default::default()
    }, None);
    Some(LinkedActions {
        datasheet_id: new_field.property.clone().unwrap().foreign_datasheet_id.clone().unwrap(),
        actions,
        ..Default::default()
    })
}

pub fn create_new_field(
    snapshot: &DatasheetSnapshotSO,
    mut field: FieldSO,
    options: Option<AddFieldOptions>,
) -> Vec<ActionOTO> {
    if field.property.is_none() {
        field.property = get_field_default_property(field.kind);
    }
    let payload = PayloadAddFieldVO {
        view_id: options.as_ref().and_then(|o| o.view_id.clone()),
        index: options.as_ref().and_then(|o| Some(o.index)),
        field_id: options.as_ref().and_then(|o| o.field_id.clone()),
        offset: options.as_ref().and_then(|o| o.offset),
        hidden_column: options.and_then(|o| o.hidden_column),
        field: field,
    };
    let action = DatasheetActions::add_field_to_action(snapshot.clone(), payload).unwrap();

    if action.is_none() {
        return vec![];
    }

    action.unwrap()
}