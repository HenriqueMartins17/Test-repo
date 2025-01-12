use crate::{ot::{commands::{CollaCommandDefExecuteResult, FixOneWayLinkDstIdOptions, ExecuteResult}, ChangeOneWayLinkDstIdOTO, types::{ActionOTO, ResourceType}}, so::{DatasheetSnapshotSO, FieldSO}, DatasheetActions, fields::property::FieldPropertySO};

pub struct FixOneWayLinkDstId {

}

impl FixOneWayLinkDstId {
    pub fn execute (
        options: FixOneWayLinkDstIdOptions,
        snapshot: Option<DatasheetSnapshotSO>,
    ) -> anyhow::Result<Option<CollaCommandDefExecuteResult>> { 
        // let state = context.state;
        let FixOneWayLinkDstIdOptions { cmd:_, data, datasheet_id, field_id } = options;
        // let snapshot = get_snapshot(state, datasheet_id);

        if data.is_empty() || snapshot.is_none() {
            return Ok(None);
        }
        let snapshot = snapshot.unwrap();
        let field = snapshot.meta.field_map.get(&field_id).unwrap();

        let actions: Vec<ActionOTO> = data.into_iter().filter_map(|link_field_option| {
            // if link_field_option.is_none() {
            //     return None;
            // }

            let new_field = FieldSO {
                property: Some(FieldPropertySO {
                    brother_field_id: Some(link_field_option.new_brother_field_id),
                    ..field.property.clone().unwrap()
                }),
                ..field.clone()
            };

            let action = DatasheetActions::change_one_way_link_dst_id_to_action(snapshot.clone(), ChangeOneWayLinkDstIdOTO { field_id: field_id.clone(), new_field}).unwrap();
            if action.is_some() {
                return action;
            } else {
                return None;
            }
        }).collect();

        if actions.is_empty() {
            return Ok(None);
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