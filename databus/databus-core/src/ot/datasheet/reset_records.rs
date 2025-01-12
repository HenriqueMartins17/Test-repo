use json0::{Operation, operation::{PathSegment, OperationKind}};
use serde_json::Value;

use crate::{ot::{commands::{CollaCommandDefExecuteResult, ResetRecordsOptions, ExecuteResult}, types::{ActionOTO, ResourceType}}, so::DatasheetSnapshotSO};

pub struct ResetRecords {

}

impl ResetRecords {
    pub fn execute (
        options: ResetRecordsOptions,
        snapshot: Option<DatasheetSnapshotSO>,
    ) -> anyhow::Result<Option<CollaCommandDefExecuteResult>> {
        // let state = context.state;
        // let field_map_snapshot = context.field_map_snapshot;
        let _data = options.data;
        let datasheet_id = options.datasheet_id.unwrap_or_else(|| "datasheet_id".to_string());
        // let datasheet_id = options.datasheet_id.unwrap_or_else(|| Selectors::get_active_datasheet_id(state).unwrap());
        // let snapshot = Selectors::get_snapshot(state, datasheet_id);

        if snapshot.is_none() {
            return Ok(None);
        }
        let snapshot = snapshot.unwrap();
        let field_map_snapshot = snapshot.meta.field_map.clone();

        let json_str = serde_json::to_string(&_data).unwrap();
        let json_value_oi = serde_json::from_str::<Value>(&json_str).unwrap();
        let json_str = serde_json::to_string(&snapshot.record_map).unwrap();
        let json_value = serde_json::from_str::<Value>(&json_str).unwrap();
        let actions: Vec<ActionOTO> = vec![ActionOTO {
            op_name: "OR".to_string(),
            op: Operation {
                p: vec![
                    PathSegment::String("recordMap".to_string()),
                ],
                kind: OperationKind::ObjectReplace { 
                    od: json_value, 
                    oi: json_value_oi 
                }
            },
          }];

        Ok(Some(CollaCommandDefExecuteResult {
            result: ExecuteResult::Success,
            resource_id: datasheet_id,
            resource_type: ResourceType::Datasheet,
            actions,
            field_map_snapshot: Some(field_map_snapshot),
            ..Default::default()
        }))
    }
}