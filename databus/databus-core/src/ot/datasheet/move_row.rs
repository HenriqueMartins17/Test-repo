use crate::{ot::{commands::{CollaCommandDefExecuteResult, MoveRowOptions, ExecuteResult, LinkedActions, MoveRowData, DropDirectionType}, types::ResourceType, MoveRowOTO}, so::DatasheetSnapshotSO, DatasheetActions};

pub struct MoveRow {

}

impl MoveRow {
    pub fn execute (
        options: MoveRowOptions,
        snapshot: Option<DatasheetSnapshotSO>,
    ) -> anyhow::Result<Option<CollaCommandDefExecuteResult>> {
        // let state = context.state;
        let MoveRowOptions { data, view_id, cmd:_, record_data } = options;
        // let datasheet_id = Selectors::get_active_datasheet_id(state).unwrap();
        // let snapshot = Selectors::get_snapshot(state, datasheet_id);
        if snapshot.is_none() {
            return Ok(None);
        }
        let snapshot = snapshot.unwrap();
        let views = snapshot.meta.views.clone();
        let datasheet_id = snapshot.datasheet_id.to_string();
        // 缺data_pack逻辑
        let record_map = snapshot.record_map.clone();
        // const recordMap = Selectors.getRowsIndexMap(state, datasheetId)!;

        // if data.is_empty() {
        //     return None;
        // }

        if !views.iter().any(|item| item.id.clone().unwrap() == view_id) {
            return Err(anyhow::anyhow!("Error: move row failed invalid params"));
        }

        let linked_actions: Vec<LinkedActions> = Vec::new();
        let actions = data.into_iter().fold(Vec::new(), |mut collected, record_option| {
            let MoveRowData { record_id, over_target_id, direction } = record_option;
            let target_record_id = over_target_id;
            let origin_row_index = record_map.get(&record_id);
            let target_row_index = record_map.get(&target_record_id);
            if target_row_index.is_none() || origin_row_index.is_none() {
                // Player::do_trigger(Events::app_error_logger, Error {
                //     error: Error::new("There is a problem with the moved row record data"),
                //     metaData: MetaData {
                //         record_id,
                //         target_record_id,
                //         targetRowIndex: target_row_index,
                //         originRowIndex: origin_row_index,
                //         rowIndexMap: serde_json::to_string(&record_map).unwrap(),
                //         recordIds: serde_json::to_string(&snapshot.unwrap().record_map.keys().collect::<Vec<_>>()).unwrap(),
                //         rows: serde_json::to_string(&snapshot.unwrap().meta.views[0].rows).unwrap(),
                //     },
                // });
                return collected;
            }
            let _origin_row_index = origin_row_index.unwrap();
            let _target_row_index = target_row_index.unwrap();
            let target_index = 0;
            // let target_index = if origin_row_index > target_row_index {
            //     target_row_index + 1
            // } else {
            //     target_row_index
            // };
            if direction == DropDirectionType::BEFORE {
                // target_index -= 1;
            }
            let action = DatasheetActions::move_row_to_action(snapshot.clone(), MoveRowOTO {
                record_id,
                target: target_index,
                view_id: view_id.to_string(),
            }).unwrap();

            if action.is_none() {
                return collected;
            }

            if !collected.is_empty() {
                // let transformedAction = jot::transform(vec![action.unwrap()], &collected, "right");
                // collected.extend(transformedAction);
                collected.push(action.unwrap())
            } else {
                collected.push(action.unwrap());
            }

            collected
        });

        if let Some(_record_data) = record_data {
            // let rst = SetRecords::execute(&snapshot, SetRecordsOptions {
            //     cmd: CollaCommandName::SetRecords,
            //     data: record_data,
            //     ..Default::default()
            // }).unwrap();
            // if let Some(rst) = rst {
            //     if rst.result == ExecuteResult::Fail {
            //         return Ok(Some(rst));
            //     }
            //     actions.extend(rst.actions);
            //     linked_actions.extend(rst.linked_actions.unwrap_or_default());
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