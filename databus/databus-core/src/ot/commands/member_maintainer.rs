use std::{collections::HashMap, rc::Rc};

use crate::{DatasheetActions, ot::{types::ActionOTO, FieldOTO}, so::{FieldKindSO, FieldSO, DatasheetPackContext}, fields::property::FieldPropertySO};

#[derive(Debug, Clone, PartialEq)]
pub struct MemberFieldMaintainer {
    member_data_changes: HashMap<String, HashMap<String, Vec<String>>>,
}

impl MemberFieldMaintainer {
    pub fn new() -> Self {
        Self {
            member_data_changes: HashMap::new(),
        }
    }

    pub fn insert(&mut self, field_id: String, insert_unit_ids: Vec<String>, datasheet_id: String) {
        let member_field_map = self.member_data_changes.entry(datasheet_id).or_insert_with(HashMap::new);
        let unit_ids = member_field_map.entry(field_id).or_insert_with(Vec::new);
        unit_ids.extend(insert_unit_ids);
        unit_ids.sort();
        unit_ids.dedup();
    }

    pub fn flush_member_action(&mut self, 
        context: Rc<DatasheetPackContext>,
    ) -> Vec<ActionOTO> {
        if self.member_data_changes.is_empty() {
            return vec![];
        }

        let mut actions = vec![];

        self.member_data_changes.iter().for_each(|(datasheet_id, member_field_map)| {
            let snapshot = context.get_snapshot(datasheet_id).unwrap();
            let field_map = snapshot.meta.field_map.clone();

            member_field_map.iter().for_each(|(field_id, cell_value_for_unit_ids)| {
                let field = field_map.get(field_id).unwrap();

                // here, do a redundant check for field type, as a fallback behavior
                if field.kind != FieldKindSO::Member {
                    return;
                }

                let action = DatasheetActions::set_field_attr_to_action(snapshot.clone(), FieldOTO {
                    field: FieldSO {
                        property: Some(FieldPropertySO {
                            unit_ids: Some(cell_value_for_unit_ids.clone()),
                            ..field.property.clone().unwrap()
                        }),
                        ..field.clone()
                    },
                }).unwrap();

                if let Some(action) = action {
                    actions.push(action);
                }
            });
        });

        self.member_data_changes.clear();
        actions
    }
}