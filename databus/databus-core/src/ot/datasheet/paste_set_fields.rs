use std::collections::{HashMap, HashSet};

use crate::{ot::{commands::{CollaCommandDefExecuteResult, PasteSetFieldsOptions, ExecuteResult, LinkedActions, IStandardValue}, types::{ResourceType, ActionOTO}, get_view_by_id}, so::{DatasheetSnapshotSO, FieldKindSO, FieldSO, ViewColumnSO}, utils::uuid::NamePrefix, fields::Text};

pub struct PasteSetFields {

}

impl PasteSetFields {
    pub fn execute (
        snapshot: DatasheetSnapshotSO,
        options: PasteSetFieldsOptions,
    ) -> anyhow::Result<Option<CollaCommandDefExecuteResult>> { 
        // let state = &context.state;
        let PasteSetFieldsOptions { cmd:_, view_id, fields, std_values, column } = options;
        // let datasheet_id = Selectors::get_active_datasheet_id(state)?;
        let datasheet_id = snapshot.datasheet_id.clone();
        // let snapshot = Selectors::get_snapshot(state, datasheet_id)?;
        let view = get_view_by_id(snapshot.clone(), view_id);
        let mut actions: Vec<ActionOTO> = Vec::new();
        let mut linked_actions: Vec<LinkedActions> = Vec::new();

        // if view.is_none() || ![ViewType::Grid, ViewType::Gantt].contains(&view.unwrap().r#type) {
        if view.is_none() || ![1, 6].contains(&view.clone().unwrap().r#type.clone().unwrap()) {
            return Ok(None);
        }

        // if column.is_nan() || column < 0 {
        if column < 0 {
            return Ok(None);
        }

        let _field_count = fields.len();
        // let visible_columns = get_visible_columns(state);
        // let columns_to_paste = visible_columns.get(column..column + field_count)?;
        // if columns_to_paste.is_empty() {
        //     return Ok(None);
        // }

        let mut field_map = snapshot.meta.field_map.clone();
        // let permissions = Selectors::get_permissions(state);
        // let field_property_editable = permissions.field_property_editable;
        // let field_creatable = permissions.field_creatable;

        fn enrich_column_property(
            column: &ViewColumnSO, 
            _std_values: &[IStandardValue],
            field_map: &mut HashMap<String, FieldSO>,
            _snapshot: &DatasheetSnapshotSO,
            _actions: &mut Vec<ActionOTO>,
            _linked_actions: &mut Vec<LinkedActions>,
        ) {
            let old_field = field_map.get(&column.field_id).unwrap();
            // if !field_property_editable {
            //     return;
            // }
            // let mut new_field = fast_clone_deep(old_field);
            let new_field = old_field.clone();
            if new_field.kind == FieldKindSO::Member {
                return;
            }
            // let new_property = Field::bind_context(&new_field, state).enrich_property(std_values);
            // if new_property == new_field.property {
            //     return;
            // }
            // let rst = SetFieldAttr::execute(Some(snapshot.clone()), SetFieldAttrOptions {
            //     cmd: CollaCommandName::SetFieldAttr,
            //     field_id: column.field_id.clone(),
            //     data: FieldSO {
            //         // field: new_field,
            //         // property: new_property,
            //         ..new_field
            //     },
            //     ..Default::default()
            // }).unwrap();
            // if let Some(rst) = rst {
            //     if rst.result == ExecuteResult::Success {
            //         actions.extend(rst.actions);
            //         if let Some(linked) = rst.linked_actions {
            //             linked_actions.extend(linked);
            //         }
            //     }
            // }
        }

        let single_cell_paste = std_values.len() == 1 && std_values[0].len() == 1;
        if single_cell_paste {
            // let ranges = get_select_ranges(state)?;
            // let range = ranges[0];
            // let fields = Selectors::get_range_fields(state, range, datasheet_id)?;
            let std_value = std_values[0][0].clone();
            let data = std_value.data.iter().filter(|d| d.get("text").is_some()).cloned().collect();
            let std_value = IStandardValue {
                data,
                ..std_value
            };
            for field in &fields {
                enrich_column_property(&ViewColumnSO { field_id: field.id.clone(), ..Default::default() }, &[std_value.clone()], &mut field_map, &snapshot, &mut actions, &mut linked_actions);
            }
        } else {
            // for (i, column) in columns_to_paste.iter().enumerate() {
            //     let std_value_field = std_values.iter().fold(Vec::new(), |mut result, std_value_row| {
            //         if let Some(std_value) = std_value_row.get(i) {
            //             let data = std_value.data.iter().filter(|d| d.text.is_some()).cloned().collect();
            //             result.push(IStandardValue {
            //                 data,
            //                 ..std_value.clone()
            //             });
            //         }
            //         result
            //     });
            //     enrich_column_property(column, &std_value_field, context, &mut actions, &mut linked_actions);
            // }
        }

        // let mut new_fields = fields.get(columns_to_paste.len()..)?;
        let mut new_fields = fields.get(1..).unwrap().iter().cloned().collect::<Vec<_>>();
        // if field_creatable && !new_fields.is_empty() {
        if !new_fields.is_empty() {
            let mut field_names = HashSet::new();
            for field_id in field_map.keys() {
                if let Some(field) = field_map.get(field_id) {
                    field_names.insert(field.name.clone());
                }
            }
            new_fields = new_fields.iter().map(|field| {
                let origin_name = field.name.clone();
                let mut name = origin_name.clone();
                let mut i = 1;
                if origin_name.is_empty() {
                    loop {
                        name = format!("{} {}", NamePrefix::Field, i);
                        i += 1;
                        if !field_names.contains(&name) {

                            break;
                        }
                    }
                } else {
                    while field_names.contains(&name) {
                        name = format!("{} ({})", origin_name, i);
                        i += 1;
                    }
                }
                field_names.insert(name.clone());
                if field.kind == FieldKindSO::LookUp {
                    let related_link_field_id = field.property.clone().unwrap().related_link_field_id.clone().unwrap();
                    if let Some(related_link_field) = field_map.get(&related_link_field_id) {
                        if related_link_field.kind != FieldKindSO::Link {
                            return FieldSO {
                                name,
                                kind: FieldKindSO::Text,
                                property: Text::default_property(),
                                ..Default::default()
                            };
                        }
                    }
                }
                FieldSO {
                    name,
                    ..field.clone()
                }
            }).collect::<Vec<FieldSO>>();
            // if let Some(rst) = AddFields::execute(context, AddFieldsOptions {
            //     cmd: CollaCommandName::AddFields,
            //     data: new_fields.iter().enumerate().map(|(index, field)| AddFieldData {
            //         view_id: view.id,
            //         index: index + visible_columns.len(),
            //         data: field.clone(),
            //     }).collect(),
            // }) {
            //     if rst.result == ExecuteResult::Fail {
            //         return Some(rst);
            //     }
            //     actions.extend(rst.actions);
            //     if let Some(linked) = rst.linked_actions {
            //         linked_actions.extend(linked);
            //     }
            // }
        }

        if actions.is_empty() {
            return Ok(None);
        }

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