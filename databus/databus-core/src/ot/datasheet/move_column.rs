use std::collections::HashMap;

use crate::{ot::{commands::{CollaCommandDefExecuteResult, MoveColumnOptions, ExecuteResult, MoveColumnData, DropDirectionType}, types::ResourceType, MoveColumnsOTO, SetFrozenColumnCountOTO}, so::DatasheetSnapshotSO, DatasheetActions};

pub struct MoveColumn {

}

impl MoveColumn {
    pub fn execute (
        options: MoveColumnOptions,
        snapshot: Option<DatasheetSnapshotSO>,
    ) -> anyhow::Result<Option<CollaCommandDefExecuteResult>> {
        // let state = context.state;
        let MoveColumnOptions { data, view_id, cmd:_ } = options;
        // let datasheet_id = Selectors::get_active_datasheet_id(state).unwrap();
        // let snapshot = Selectors::get_snapshot(state, datasheet_id);
        if snapshot.is_none() {
            return Ok(None);
        }
        let snapshot = snapshot.unwrap();
        let datasheet_id = snapshot.datasheet_id.to_string();

        let view = snapshot.meta.views.iter().find(|v| v.id.clone().unwrap() == view_id);

        let get_column_index_map = || -> HashMap<String, usize> {
            let mut columns_map: HashMap<String, usize> = HashMap::new();
            if let Some(view) = view {
                for (k, v) in view.columns.iter().enumerate() {
                    columns_map.insert(v.field_id.clone(), k);
                }
            }
            columns_map
        };

        let column_index_map_by_id = get_column_index_map();

        if data.is_empty() {
            return Ok(None);
        }

        if view.is_none() {
            // panic!(t(Strings.error_move_column_failed_invalid_params));
            panic!("Error: move column failed invalid params");
        }

        let frozen_column_count = view.unwrap().frozen_column_count;
        // ts这里final_frozen_column_count=frozen_column_count，option无法进行+操作
        let mut final_frozen_column_count = frozen_column_count.unwrap_or(0);

        let mut actions = data.into_iter().fold(Vec::new(), |mut collected, record_option| {
            let MoveColumnData { field_id, over_target_id, direction } = record_option;
            let origin_column_index = column_index_map_by_id[&field_id].clone();
            let target_column_index = column_index_map_by_id[&over_target_id].clone();
            let mut target_index = if origin_column_index > target_column_index {
                target_column_index + 1
            } else {
                target_column_index
            };
            if direction == DropDirectionType::BEFORE {
                target_index -= 1;
            }
            if target_index == 0 {
                return collected;
            }
            if origin_column_index == 0 {
                return collected;
            }
            let payload = MoveColumnsOTO {
                view_id: view_id.clone(),
                field_id: field_id.clone(),
                target: target_index,
            };
            let action = DatasheetActions::move_columns_to_action(snapshot.clone(), payload).unwrap();

            if action.is_none() {
                return collected;
            }

            if let Some(frozen_column_count) = frozen_column_count {
                let frozen_column_count = frozen_column_count as usize;
                if target_index < frozen_column_count && origin_column_index >= frozen_column_count {
                    final_frozen_column_count += 1;
                }
                if target_index >= frozen_column_count && origin_column_index < frozen_column_count {
                    final_frozen_column_count -= 1;
                }
            }

            if !collected.is_empty() {
                // let transformed_action = jot::transform(&[action.unwrap()], &collected, "right");
                // collected.extend_from_slice(&transformed_action);
                collected.push(action.unwrap());
            } else {
                collected.push(action.unwrap());
            }

            collected
        });

        if actions.is_empty() {
            return Ok(None);
        }

        if let Some(frozen_column_count) = frozen_column_count {
            if frozen_column_count != final_frozen_column_count {
                let action = DatasheetActions::set_frozen_column_count_to_action(snapshot.clone(), SetFrozenColumnCountOTO {
                    view_id: view_id.clone(),
                    count: final_frozen_column_count,
                }).unwrap();
                if let Some(action) = action {
                    actions.push(action);
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