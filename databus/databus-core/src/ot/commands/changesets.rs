use std::collections::HashMap;

use crate::{ot::{changeset::{ResourceOpsCollect, LocalChangeset}, types::ResourceType}, utils::utils::generate_random_string, so::DatasheetPackContext};


pub fn resource_ops_to_changesets(
    resource_ops_collects: Vec<ResourceOpsCollect>,
    // context: Rc<DatasheetPackContext>,
    context: &mut DatasheetPackContext,
) -> Vec<LocalChangeset> {
    let mut changeset_map: HashMap<String, LocalChangeset> = HashMap::new();
    let mut changeset_map_res = Vec::new();
    let mut resource_ids = Vec::new();
    
    for collect in resource_ops_collects {
        let ResourceOpsCollect { resource_id, resource_type, operations, field_type_map: _ } = collect;
        let changeset = changeset_map.get_mut(&resource_id);
        if changeset.is_none() {
            let revision = if resource_type == ResourceType::Dashboard {
                // Selectors::get_dashboard(&state, &resource_id)?.revision
                Some(22)
            } else {
                // Selectors::get_datasheet(&state, &resource_id)?.revision  need pack
                if let Some(datasheet) = context.get_datasheet(&resource_id){
                    Some(datasheet.revision as i32)
                } else {
                    Some(22)
                }
            };
            resource_ids.push(resource_id.clone());
            changeset_map.insert(resource_id.clone(), LocalChangeset {
                base_revision: revision.unwrap(),
                message_id: generate_random_string(20),
                resource_id: resource_id.clone(),
                resource_type: resource_type.clone(),
                operations: operations.clone(),
            });
        } else {
            changeset.unwrap().operations.append(&mut operations.clone());
        }
    }
    for id in resource_ids {
        changeset_map_res.push(changeset_map.get(&id).unwrap().clone());
    }
    changeset_map_res
}