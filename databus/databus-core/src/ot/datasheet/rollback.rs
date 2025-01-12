
use crate::{ot::{commands::{CollaCommandDefExecuteResult, RollbackOptions, ExecuteResult, LinkedActions}, types::ResourceType}, so::DatasheetSnapshotSO, fields::property::LinkFieldPropertySO};

pub struct Rollback {

}

impl Rollback {
    pub fn execute (
        _snapshot: DatasheetSnapshotSO,
        options: RollbackOptions,
    ) -> anyhow::Result<Option<CollaCommandDefExecuteResult>> { 
        // let state = context.state;
        let RollbackOptions { cmd:_, datasheet_id, data:_ } = options;
        // let pre_datasheet = get_datasheet(state, datasheet_id);
        // if pre_datasheet.is_none() {
        //     return Ok(None);
        // }
        // let pre_snapshot = pre_datasheet.snapshot;
        // let post_snapshot = fast_clone_deep(pre_snapshot);
        // let actions = get_rollback_actions(operations, state, post_snapshot);

        // if actions.is_empty() {
        //     return Ok(None);
        // }

        let linked_actions: Vec<LinkedActions> = Vec::new();

        // fn set_linked_actions(
        //     datasheet_id: String, 
        //     actions: Vec<ActionOTO>,
        //     linked_actions: &mut Vec<LinkedActions>,
        // ) {
        //     if actions.is_empty() {
        //         return;
        //     }

        //     let la = linked_actions.iter_mut().find(|la| la.datasheet_id == datasheet_id);
        //     if let Some(la) = la {
        //         la.actions.extend(actions);
        //     } else {
        //         linked_actions.push(LinkedActions {
        //             datasheet_id,
        //             actions,
        //         });
        //     }
        // }

        // let link_field_change = get_link_field_change(pre_snapshot.meta.field_map, post_snapshot.meta.field_map);

        // let deleted_link_fields = link_field_change.deleted_link_fields;
        // let new_link_fields = link_field_change.new_link_fields;
        // let normal_link_fields = link_field_change.normal_link_fields;

        // for source_field in deleted_link_fields {
        //     let foreign_datasheet_id = source_field.property.foreign_datasheet_id;
        //     let foreign_snapshot = get_snapshot(state, foreign_datasheet_id).unwrap();
        //     let foreign_field = get_field(state, source_field.property.brother_field_id.unwrap(), foreign_datasheet_id);
        //     let actions = set_field(context, foreign_snapshot, foreign_field, Field {
        //         id: foreign_field.id,
        //         name: foreign_field.name,
        //         field_type: FieldType::Text,
        //         property: TextField::default_property(),
        //     }).actions;
        //     set_linked_actions(foreign_datasheet_id, actions);
        // }

        let _new_foreign_field: Vec<Option<LinkFieldPropertySO>> = Vec::new();

        // for (index, source_field) in new_link_fields.iter().enumerate() {
        //     let foreign_datasheet_id = source_field.property.foreign_datasheet_id;
        //     let brother_field_id = source_field.property.brother_field_id.unwrap();
        //     let foreign_snapshot = get_snapshot(state, foreign_datasheet_id).unwrap();
        //     if foreign_snapshot.is_none() {
        //         continue;
        //     }
        //     let foreign_field = get_field(state, brother_field_id, foreign_datasheet_id);

        //     let foreign_field_ids = Object.keys(foreign_snapshot.meta.field_map);

        //     if foreign_field && foreign_field.field_type != FieldType::Text {
        //         brother_field_id = get_new_id(IDPrefix::Field, foreign_field_ids);
        //         source_field = clone_deep(source_field);
        //         source_field.property.brother_field_id = brother_field_id;

        //         let ac = DatasheetActions::set_field_attr2_action(post_snapshot, Field {
        //             field: source_field,
        //         });
        //         if let Some(ac) = ac {
        //             actions.push(ac);
        //             jot.apply(post_snapshot, vec![ac]);
        //         }
        //         let new_field = LinkField {
        //             id: brother_field_id,
        //             field_type: FieldType::Link,
        //             name: get_uniq_name(pre_datasheet.name, foreign_field_ids.map(|id| foreign_snapshot.meta.field_map[id].name)),
        //             property: LinkFieldProperty {
        //                 foreign_datasheet_id: datasheet_id,
        //                 brother_field_id: source_field.id,
        //             },
        //         };

        //         let new_field_actions = create_new_field(foreign_snapshot, new_field);

        //         set_linked_actions(foreign_datasheet_id, new_field_actions);
        //         new_foreign_field.push(Some(new_field));
        //     } else {
        //         let modified_field = LinkField {
        //             ..foreign_field,
        //             field_type: FieldType::Link,
        //             property: LinkFieldProperty {
        //                 foreign_datasheet_id: datasheet_id,
        //                 brother_field_id: source_field.id,
        //             },
        //         };

        //         let modified_field_actions = set_field(context, foreign_snapshot, foreign_field, modified_field).actions;
        //         set_linked_actions(foreign_datasheet_id, modified_field_actions);
        //         new_foreign_field.push(Some(source_field));
        //     }
        // }

        // for (index, source_field) in new_link_fields.iter().enumerate() {
        //     if new_foreign_field[index].is_none() {
        //         continue;
        //     }
        //     let result = patch_field_values(state, post_snapshot, source_field, new_foreign_field[index].unwrap());
        //     actions.extend(result.source_actions);
        //     set_linked_actions(source_field.property.foreign_datasheet_id, result.foreign_actions);
        // }

        // for source_field in normal_link_fields {
        //     let result = patch_field_values(state, post_snapshot, source_field);
        //     actions.extend(result.source_actions);
        //     set_linked_actions(source_field.property.foreign_datasheet_id, result.foreign_actions);
        // }

        Ok(Some(CollaCommandDefExecuteResult {
            result: ExecuteResult::Success,
            resource_id: datasheet_id,
            resource_type: ResourceType::Datasheet,
            // actions,
            linked_actions: Some(linked_actions),
            ..Default::default()
        }))
    }
}