use serde_json::Value;
use serde_json::Value::Null;
use json0::Operation;
use json0::operation::{OperationKind, PathSegment};
use crate::prelude::cell::CellValueSo;
use crate::prelude::DatasheetSnapshotSO;
use crate::ot::types::{ActionOTO, SetRecordOTO};

// refactor from apitable/packages/core/src/commands_actions/datasheet.ts#L778 setRecord2Action
pub fn set_record_to_action(
    snapshot: DatasheetSnapshotSO,
    payload: SetRecordOTO,
) -> anyhow::Result<Option<ActionOTO>> {
    let SetRecordOTO {
        record_id,
        field_id,
        value,
    } = payload;
    if  !snapshot.record_map.contains_key(record_id.as_str()) {
        return Ok(None);
    };
    let cv = snapshot.record_map.get(&record_id)
        .and_then(|record| record.data.get(&field_id))
        .cloned()
        .unwrap_or(Value::Null);

    let cv = serde_json::from_value(cv).unwrap_or(CellValueSo::Null);
    if  cv == value {
        return Ok(None);
    }
    let old_cell_value = if cv == CellValueSo::Null {
        CellValueSo::Null
    } else {
        cv
    };

    // when value is empty(empty array), we should delete the key to avoid redundancy
    if value == CellValueSo::Null || (value.is_array() && value.as_array().unwrap().is_empty()) {
        return Ok(Some(ActionOTO {
            op_name: "OD".to_string(),
            op: Operation {
                p: vec![
                    PathSegment::String("recordMap".to_string()),
                    PathSegment::String(record_id.clone()),
                    PathSegment::String("data".to_string()),
                    PathSegment::String(field_id.clone()),
                ],
                kind: OperationKind::ObjectDelete {
                    od: old_cell_value,
                },
            },
        }));
    }

    // when origin cellValue is empty, in fact it need insert one fieldId key
    if old_cell_value == Null {
        return {
            return Ok(Some(ActionOTO {
                op_name: "OI".to_string(),
                op: Operation {
                    p: vec![
                        PathSegment::String("recordMap".to_string()),
                        PathSegment::String(record_id.clone()),
                        PathSegment::String("data".to_string()),
                        PathSegment::String(field_id.clone()),
                    ],
                    kind: OperationKind::ObjectInsert {
                        oi: value,
                    },
                },
            }));
        };
    }

    return {
        return Ok(Some(ActionOTO {
            op_name: "OR".to_string(),
            op: Operation {
                p: vec![
                    PathSegment::String("recordMap".to_string()),
                    PathSegment::String(record_id.clone()),
                    PathSegment::String("data".to_string()),
                    PathSegment::String(field_id.clone()),
                ],
                kind: OperationKind::ObjectReplace {
                    od: old_cell_value,
                    oi: value,
                },
            },
        }));
    };
}

#[cfg(test)]
mod tests {
    
    use crate::ot::commands::test::mock_data::MOCK_SNAP_SHOT_DATA;
    use super::*;
    use crate::prelude::cell::CellValueSo;

    #[test]
    fn test_record_map_have_no_data() {
        let snapshot = serde_json::from_str::<DatasheetSnapshotSO>(MOCK_SNAP_SHOT_DATA).unwrap();
        let payload = SetRecordOTO {
            record_id: "x".to_string(),
            field_id: "x".to_string(),
            value: CellValueSo::from(serde_json::Number::from_f64(42.0).unwrap()),
        };
        let result = set_record_to_action(snapshot, payload);
        assert_eq!(result.unwrap(), None);
    }


    #[test]
    fn test_no_diff_modify() {
        let snapshot = serde_json::from_str::<DatasheetSnapshotSO>(MOCK_SNAP_SHOT_DATA).unwrap();
        let payload = SetRecordOTO {
            record_id: "reclx3H5CZbZP".to_string(),
            field_id: "fldmHjmSjZxVn".to_string(),
            value: serde_json::from_str(r#"
                [
                    {
                        "text": "说的是",
                        "type": 1
                    }
                ]
            "#).unwrap(),
        };
        let result = set_record_to_action(snapshot.clone(), payload);
        assert_eq!(result.unwrap(), None);
        let payload = SetRecordOTO {
            record_id: "reclx3H5CZbZP".to_string(),
            field_id: "xxx".to_string(),
            value: Value::Null
        };
        let result = set_record_to_action(snapshot.clone(), payload);
        assert_eq!(result.unwrap(), None);
    }


    #[test]
    fn test_no_modify() {
        // null value
        let result_value = serde_json::from_str::<Value>(r#"{"n":"OD","od":[{"text":"说的是","type":1}],"p":["recordMap","reclx3H5CZbZP","data","fldmHjmSjZxVn"]}"#).unwrap();
        let snapshot = serde_json::from_str::<DatasheetSnapshotSO>(MOCK_SNAP_SHOT_DATA).unwrap();
        let payload = SetRecordOTO {
            record_id: "reclx3H5CZbZP".to_string(),
            field_id: "fldmHjmSjZxVn".to_string(),
            value: serde_json::from_str(r#"null"#).unwrap(),
        };
        let result = set_record_to_action(snapshot.clone(), payload);
        let result = result.unwrap();
        assert_ne!(result, None);
        let json_value = serde_json::to_value(result.unwrap()).unwrap();
        assert_eq!(json_value, result_value);
        // empty array
        let payload = SetRecordOTO {
            record_id: "reclx3H5CZbZP".to_string(),
            field_id: "fldmHjmSjZxVn".to_string(),
            value: serde_json::from_str(r#"[]"#).unwrap(),
        };
        let result = set_record_to_action(snapshot.clone(), payload);
        let result = result.unwrap();
        assert_ne!(result, None);
        let json_value = serde_json::to_value(result.unwrap()).unwrap();
        assert_eq!(json_value, result_value);
    }


    #[test]
    fn test_add_data() {
        let result_value = serde_json::from_str::<Value>(r#"{"n":"OI","oi":["reciZgdFWE4eC"],"p":["recordMap","recthZXaIAaOW","data","fldpYxbYNp5L4"]}"#).unwrap();
        let snapshot = serde_json::from_str::<DatasheetSnapshotSO>(MOCK_SNAP_SHOT_DATA).unwrap();
        let payload = SetRecordOTO {
            record_id: "recthZXaIAaOW".to_string(),
            field_id: "fldpYxbYNp5L4".to_string(),
            value: serde_json::from_str(r#"["reciZgdFWE4eC"]"#).unwrap(),
        };
        let result = set_record_to_action(snapshot.clone(), payload);
        let result = result.unwrap();
        assert_ne!(result, None);
        let json_value = serde_json::to_value(result.unwrap()).unwrap();
        println!("json_value is {}", json_value.to_string());
        assert_eq!(json_value, result_value);
    }

    #[test]
    fn test_replace_data() {
        let result_value = serde_json::from_str::<Value>(r#"{"n":"OR","od":[{"text":"说的是","type":1}],"oi":[{"text":"说的是你吗","type":1}],"p":["recordMap","reclx3H5CZbZP","data","fldmHjmSjZxVn"]}"#).unwrap();
        let snapshot = serde_json::from_str::<DatasheetSnapshotSO>(MOCK_SNAP_SHOT_DATA).unwrap();
        let payload = SetRecordOTO {
            record_id: "reclx3H5CZbZP".to_string(),
            field_id: "fldmHjmSjZxVn".to_string(),
            value: serde_json::from_str(r#"
                [
                    {
                        "text": "说的是你吗",
                        "type": 1
                    }
                ]
            "#).unwrap(),
        };
        let result = set_record_to_action(snapshot.clone(), payload);
        let result = result.unwrap();
        assert_ne!(result, None);
        let json_value = serde_json::to_value(result.unwrap()).unwrap();
        println!("json_value is {}", json_value.to_string());
        assert_eq!(json_value, result_value);
    }
}
