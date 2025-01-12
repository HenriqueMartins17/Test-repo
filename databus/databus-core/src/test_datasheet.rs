#[cfg(test)]
mod tests {
  use std::collections::HashMap;
  use std::rc::Rc;
  use crate::fields::property::{FieldPropertySO, TimeFormat, DateFormat, SingleSelectProperty};
  use crate::logic::IAddRecordsOptions;
use crate::ot::{CollaCommandName, RecordDelete};
  use crate::ot::commands::{CollaCommandExecuteResult, ExecuteResult, SaveResult, ExecuteType, AddRecordsOptions, SaveOptions, ExecuteFailReason, SetRecordOptions, AddFieldOptions, DeleteFieldData, AddView};
  use crate::mock::{get_datasheet_map_pack, get_datasheet_pack, get_datasheet};
  use crate::ot::changeset::{Operation, ResourceOpsCollect};
  use crate::ot::types::{ActionOTO, ResourceType};
  use crate::so::{FieldKindSO, RecordSO, prepare_context_data, FieldSO, ViewSO, ViewRowSO, ViewColumnSO, DatasheetPackContext};
  use crate::prelude::CellValueSo;
  use databus_shared::prelude::HashMapExt;
  use json0::operation::{PathSegment, OperationKind};
  use serde_json::{Value, to_value, from_value};
  use serde::{Deserialize, Serialize};

  struct AddRecordsRow {
    pub view: usize,
    pub index: usize,
  }

  struct AddRecordsOperation {
    pub id: Value,
    pub rows: Vec<AddRecordsRow>,
    pub values: Option<HashMap<String, CellValueSo>>,
  }

  #[derive(Debug, Deserialize, Serialize)]
  #[serde(rename_all = "camelCase")]
  struct RecordValue {
    pub r#type: usize,
    pub text: String,
  }

  #[derive(Debug, Deserialize, Serialize)]
  #[serde(rename_all = "camelCase")]
  struct ListInsert {
    pub field_id: Value,
    pub hidden: bool
  }

  #[derive(Debug, Deserialize, Serialize)]
  #[serde(rename_all = "camelCase")]
  struct ListDelete {
    pub field_id: String,
  }

  fn get_context() -> Rc<DatasheetPackContext>{
    let base_datasheet_pack = get_datasheet_map_pack(1).unwrap();
    let snapshot = base_datasheet_pack.snapshot;
    let mut datasheet_pack = get_datasheet_pack().unwrap();
    datasheet_pack.snapshot = snapshot.clone();
    let context = prepare_context_data(Box::new(datasheet_pack));
    let context = Rc::new(context);
    context
  }

  fn normal_compare(mut result: CollaCommandExecuteResult)-> CollaCommandExecuteResult{
    assert_eq!(result.result, ExecuteResult::Success);

    let mut save_result = result.save_result.clone().unwrap();
    assert_eq!(save_result.len(), 1);
    assert_eq!(save_result[0].message_id.len() > 0, true);

    result.data = None;
    save_result[0].message_id = "msg1".to_string();
    result.save_result = Some(save_result);
    result
  }

  fn compare_result_and_expected(operation: Operation, result: CollaCommandExecuteResult){
    let expected = CollaCommandExecuteResult {
      resource_id: "dst1".to_string(),
      resource_type: ResourceType::Datasheet,
      result: ExecuteResult::Success,
      operation: Some(operation.clone()),
      linked_actions: Some(vec![]),
      resource_ops_collects: Some(vec![
            ResourceOpsCollect {
                operations: vec![operation.clone()],
                resource_id: "dst1".to_string(),
                resource_type: ResourceType::Datasheet,
                ..Default::default()
            },
      ]),
      save_result: Some(vec![
          SaveResult {
              base_revision: 12,
              message_id: "msg1".to_string(),
              operations: vec![operation.clone()],
              resource_id: "dst1".to_string(),
              resource_type: ResourceType::Datasheet,
          },
      ]),
      execute_type: Some(ExecuteType::Execute),
      ..Default::default()
    };
    // one_by_one_test(result, expected);
    assert_eq!(result, expected);
  }

  fn mock_operation_of_add_records(records: Vec<AddRecordsOperation>) -> Operation {
    let actions: Vec<ActionOTO> = records
        .iter()
        .flat_map(|record| {
            let AddRecordsOperation { id, rows, values } = record;
            let mut record: HashMap<String, Value> = HashMap::new();
            record.insert("recordId".to_owned(), id.clone());
            let list_insert_actions: Vec<ActionOTO> = rows
                .iter()
                .map(|row| ActionOTO {
                  op_name: "LI".to_string(),
                  op: json0::Operation {
                      p: vec![
                          PathSegment::String("meta".to_string()),
                          PathSegment::String("views".to_string()),
                          PathSegment::Number(row.view.clone()),
                          PathSegment::String("rows".to_string()),
                          PathSegment::Number(row.index.clone()),
                      ],
                      kind: OperationKind::ListInsert {
                        li: to_value(&record).unwrap()
                      },
                    }
                })
                .collect();
            let data_value = ["opt2".to_string(), "opt1".to_string()];
            let mut hm = HashMap::new();
            hm.insert("fld2".to_string(), data_value);
            let str_id: String = serde_json::from_value(id.clone()).unwrap();
            let record = RecordSO {
              id: str_id.clone(),
              comment_count: 0,
              data: if values.is_some() {
                to_value(values.clone().unwrap()).unwrap()
              }else{
                to_value(hm).unwrap()
              },
              ..Default::default()
            };
            let object_insert_action = ActionOTO {
                op_name: "OI".to_string(),
                op: json0::Operation {
                    p: vec![
                        PathSegment::String("recordMap".to_string()),
                        PathSegment::String(str_id.clone()),
                    ],
                    kind: OperationKind::ObjectInsert {
                      oi: to_value(&record).unwrap()
                    },
                },
            };
            list_insert_actions.into_iter().chain(std::iter::once(object_insert_action))
        })
        .collect();
    let field_type_map = if records.iter().any(|record| record.values.is_some() && record.values.as_ref().unwrap().contains_key("fld1")) {
      HashMapExt::from_iter(vec![("fld1".to_string(), FieldKindSO::Text), ("fld2".to_string(), FieldKindSO::MultiSelect)])
    } else {
      HashMapExt::from_iter(vec![("fld2".to_string(), FieldKindSO::MultiSelect)])
    };
    Operation {
        actions,
        cmd: "AddRecords".to_string(),
        field_type_map: Some(field_type_map),
        ..Default::default()
    }
  }

  // fn one_by_one_test(result: CollaCommandExecuteResult, expected: CollaCommandExecuteResult){
  //   assert_eq!(result.result, expected.result);
  //   assert_eq!(result.resource_id, expected.resource_id);
  //   assert_eq!(result.resource_type, expected.resource_type);
  //   assert_eq!(result.data, expected.data);
  //   // compare result.operation and expected.operation one by one
  //   if let Some(expected_op) = expected.operation {
  //     if let Some(result_op) = result.operation {
  //         assert_eq!(result_op.cmd, expected_op.cmd);
  //         // assert_eq!(result_op.actions, expected_op.actions);
  //         let result_actions = result_op.actions;
  //         let expected_actions = expected_op.actions;
  //         assert_eq!(result_actions.len(), expected_actions.len());
  //         for (result_action, expected_action) in result_actions.iter().zip(expected_actions.iter()) {
  //           assert_eq!(result_action.op_name, expected_action.op_name);
  //           // assert_eq!(result_action.op, expected_action.op);
  //           let result_op = result_action.op.clone();
  //           let expected_op = expected_action.op.clone();
  //           assert_eq!(result_op.p, expected_op.p);
  //           let result_kind = result_op.kind;
  //           let expected_kind = expected_op.kind;
  //           assert_eq!(result_kind, expected_kind);
  //         }
  //         assert_eq!(result_op.main_link_dst_id, expected_op.main_link_dst_id);
  //         assert_eq!(result_op.field_type_map, expected_op.field_type_map);
  //         assert_eq!(result_op.resource_type, expected_op.resource_type);
  //         assert_eq!(result_op.revision, expected_op.revision);
  //     }
  //   }
  //   // assert_eq!(result.operation, expected.operation);
  //   assert_eq!(result.execute_type, expected.execute_type);
  //   assert_eq!(result.linked_actions, expected.linked_actions);
  //   assert_eq!(result.resource_ops_collects, expected.resource_ops_collects);
  //   assert_eq!(result.save_result, expected.save_result);
  // }

  #[test]
  fn should_return_view_for_existing_view() {
    let base_datasheet_pack = get_datasheet_map_pack(1).unwrap();
    let snapshot = base_datasheet_pack.snapshot;

    let view1 = snapshot.get_view("viw1");
    assert!(view1.is_some());
    assert_eq!(view1.clone().unwrap().id.clone().unwrap(), "viw1".to_string());
  }

  #[test]
  fn should_return_null_if_view_does_not_exist() {
    let base_datasheet_pack = get_datasheet_map_pack(1).unwrap();
    let snapshot = base_datasheet_pack.snapshot;

    let view1 = snapshot.get_view("viw100");
    assert!(view1.is_none());
  }

  #[tokio::test]
  async fn should_return_success_if_command_execution_succeeded() {
    let context = get_context();
    let mut dst1 = get_datasheet(context);
    let result = dst1.add_records(
        IAddRecordsOptions {
            view_id: "viw1".to_string(),
            index: 3,
            count: 1,
            ..Default::default()
        },
        SaveOptions { ..Default::default() },
    ).await.unwrap();

    assert_eq!(result.result, ExecuteResult::Success);
  }

  #[tokio::test]
  async fn should_fail_if_count_not_equal_to_cell_values_length_when_adding_records() {
    let context = get_context();
    let mut dst1 = get_datasheet(context);
    let result = dst1.add_records(
        IAddRecordsOptions {
            view_id: "viw1".to_string(),
            index: 3,
            count: 1,
            record_values: Some(vec![]),
            ..Default::default()
        },
        SaveOptions { ..Default::default() },
    ).await.unwrap();
    assert_eq!(
      result,
      CollaCommandExecuteResult {
          resource_id: "dst1".to_string(),
          resource_type: ResourceType::Datasheet,
          result: ExecuteResult::Fail,
          reason: Some(ExecuteFailReason::ActionError),
          ..Default::default()
      }
  );
  }

  #[tokio::test]
  async fn should_return_none_when_adding_zero_records() {
    let context = get_context();
    let mut dst1 = get_datasheet(context);
    let result = dst1.add_records(
        IAddRecordsOptions {
            view_id: "viw1".to_string(),
            index: 3,
            count: 0,
            record_values: Some(vec![]),
            ..Default::default()
        },
        SaveOptions { ..Default::default() },
    ).await.unwrap();

    assert_eq!(
        result,
        CollaCommandExecuteResult {
            resource_id: "dst1".to_string(),
            resource_type: ResourceType::Datasheet,
            result: ExecuteResult::None,
            ..Default::default()
        }
    );
  }

  #[tokio::test]
  async fn add_two_records_with_count_in_viw1() {
    let context = get_context();
    let mut dst1 = get_datasheet(context);
    let mut result = dst1.add_records(
      IAddRecordsOptions {
          view_id: "viw1".to_string(),
          index: 3,
          count: 2,
          ..Default::default()
      },
      SaveOptions { ..Default::default() },
    ).await.unwrap();

    let record_ids = result.data.clone().unwrap();
    
    let data = result.data.clone().unwrap();
    if data.is_array() {
      let data = data.as_array().unwrap();
      assert_eq!(data.len(), 2);
    } else {
      panic!("data is not array");
    }
    
    result = normal_compare(result);

    let operation = mock_operation_of_add_records(vec![
        AddRecordsOperation {
            id: record_ids[0].clone(),
            rows: vec![
                AddRecordsRow { view: 0, index: 3 },
                AddRecordsRow { view: 1, index: 5 },
                AddRecordsRow { view: 2, index: 5 },
            ],
            values: None,
        },
        AddRecordsOperation {
            id: record_ids[1].clone(),
            rows: vec![
                AddRecordsRow { view: 0, index: 4 },
                AddRecordsRow { view: 1, index: 5 },
                AddRecordsRow { view: 2, index: 5 },
            ],
            values: None,
        },
    ]);
    compare_result_and_expected(operation, result)
  }   

  #[tokio::test]
  async fn add_two_records_with_record_values() {
    let context = get_context();
    let mut dst1 = get_datasheet(context);
    let mut hm = HashMap::new();
    let rv = RecordValue {
      r#type: 1,
      text: "first".to_string(),
    };
    hm.insert("fld1".to_string(), serde_json::to_value(&rv).unwrap());
    let rv2 = vec!["opt2".to_string(), "opt1".to_string()];
    hm.insert("fld2".to_string(), serde_json::to_value(&rv2).unwrap());

    let mut hm2 = HashMap::new();
    let rv = RecordValue {
      r#type: 1,
      text: "second".to_string(),
    };
    hm2.insert("fld1".to_string(), serde_json::to_value(&rv).unwrap());
    let rv2: Vec<String> = vec![];
    hm2.insert("fld2".to_string(), serde_json::to_value(&rv2).unwrap());
    let mut result = dst1.add_records(
      IAddRecordsOptions {
            view_id: "viw1".to_string(),
            index: 3,
            record_values: Some(vec![hm.clone(), hm2.clone()]),
            ..Default::default()
        },
        SaveOptions { ..Default::default() },
    ).await.unwrap();

    let data = result.data.clone().unwrap();
    if data.is_array() {
      let data = data.as_array().unwrap();
      assert_eq!(data.len(), 2);
    } else {
      panic!("data is not array");
    }
    
    let record_ids = result.data.clone().unwrap();
    result = normal_compare(result);

    let operation = mock_operation_of_add_records(vec![
        AddRecordsOperation {
            id: record_ids[0].clone(),
            rows: vec![
                AddRecordsRow { view: 0, index: 3 },
                AddRecordsRow { view: 1, index: 5 },
                AddRecordsRow { view: 2, index: 5 },
            ],
            values: Some(hm.clone()),
        },
        AddRecordsOperation {
            id: record_ids[1].clone(),
            rows: vec![
                AddRecordsRow { view: 0, index: 4 },
                AddRecordsRow { view: 1, index: 5 },
                AddRecordsRow { view: 2, index: 5 },
            ],
            values: Some(hm2.clone()),
        },
    ]);

    compare_result_and_expected(operation, result)
  }

  #[tokio::test]
  async fn update_rec2_fld2() {
    let context = get_context();
    let mut dst1 = get_datasheet(context);
    let mut result = dst1.update_records(
        vec![
          SetRecordOptions {
                record_id: "rec2".to_string(),
                field_id: "fld2".to_string(),
                value: serde_json::to_value(vec!["opt3".to_string(), "opt2".to_string()]).unwrap(),
                ..Default::default()
            },
        ],
        SaveOptions { ..Default::default() },
    ).await.unwrap();

    result = normal_compare(result);

    let mut field_type_map = HashMapExt::default();
    field_type_map.insert("fld2".to_string(), FieldKindSO::MultiSelect);
    let operation = Operation {
        actions: vec![
          ActionOTO {
            op_name: "OR".to_string(),
            op: json0::Operation {
                p: vec![
                    PathSegment::String("recordMap".to_string()),
                    PathSegment::String("rec2".to_string()),
                    PathSegment::String("data".to_string()),
                    PathSegment::String("fld2".to_string()),
                ],
                kind: OperationKind::ObjectReplace {
                    od: serde_json::to_value(vec!["opt1".to_string()]).unwrap(),
                    oi: serde_json::to_value(vec!["opt3".to_string(), "opt2".to_string()]).unwrap(),
                },
            },
          }
        ],
        cmd: CollaCommandName::SetRecords.to_string(),
        field_type_map: Some(field_type_map),
        ..Default::default()
    };

    compare_result_and_expected(operation, result)
  }

  #[tokio::test]
  async fn delete_rec2() {
    let context = get_context();
    let mut dst1 = get_datasheet(context);
    let mut result = dst1.delete_records(
        vec!["rec2".to_string()],
        SaveOptions { ..Default::default() },
    ).await.unwrap();

    result = normal_compare(result);
    
    let mut hm = HashMap::new();
    let data_value = vec![to_value(RecordValue {
      r#type: 1,
      text: "text 2".to_string(),
    }).unwrap()];
    hm.insert("fld1".to_string(), data_value);
    let data_value = vec![to_value("opt1".to_string()).unwrap()];
    hm.insert("fld2".to_string(), data_value);

    let operation = Operation {
        actions: vec![
          ActionOTO {
            op_name: "LD".to_string(),
            op: json0::Operation {
                p: vec![
                    PathSegment::String("meta".to_string()),
                    PathSegment::String("views".to_string()),
                    PathSegment::Number(0),
                    PathSegment::String("rows".to_string()),
                    PathSegment::Number(1),
                ],
                kind: OperationKind::ListDelete {
                  ld: to_value(RecordDelete {record_id: "rec2".to_string()}).unwrap()
                },
              }
          },
          ActionOTO {
            op_name: "LD".to_string(),
            op: json0::Operation {
                p: vec![
                    PathSegment::String("meta".to_string()),
                    PathSegment::String("views".to_string()),
                    PathSegment::Number(1),
                    PathSegment::String("rows".to_string()),
                    PathSegment::Number(0),
                ],
                kind: OperationKind::ListDelete {
                  ld: to_value(RecordDelete {record_id: "rec2".to_string()}).unwrap()
                },
              }
          },
          ActionOTO {
            op_name: "LD".to_string(),
            op: json0::Operation {
                p: vec![
                    PathSegment::String("meta".to_string()),
                    PathSegment::String("views".to_string()),
                    PathSegment::Number(2),
                    PathSegment::String("rows".to_string()),
                    PathSegment::Number(2),
                ],
                kind: OperationKind::ListDelete {
                  ld: to_value(RecordDelete {record_id: "rec2".to_string()}).unwrap()
                },
              }
          },
          ActionOTO {
            op_name: "OD".to_string(),
            op: json0::Operation {
                p: vec![
                    PathSegment::String("recordMap".to_string()),
                    PathSegment::String("rec2".to_string()),
                ],
                kind: OperationKind::ObjectDelete {
                  od: to_value(RecordSO {
                    id: "rec2".to_string(),
                    comment_count: 0,
                    data: to_value(hm).unwrap(),
                    ..Default::default()
                  }).unwrap()
                },
              }
          },
        ],
        cmd: CollaCommandName::DeleteRecords.to_string(),
        ..Default::default()
    };

    compare_result_and_expected(operation, result)
  }
  
  #[tokio::test]
  async fn add_a_datetime_field() {
    let context = get_context();
    let mut dst1 = get_datasheet(context);
    // let command_manager = CollaCommandManager::new();
    let options = vec![
      AddFieldOptions {
          index: 2,
          data: FieldSO {
              name: "field 3".to_string(),
              kind: FieldKindSO::DateTime,
              property: Some(FieldPropertySO {
                  date_format: Some(DateFormat::YyyyMm),
                  time_format: Some(TimeFormat::HHmm),
                  include_time: Some(false),
                  auto_fill: Some(false),
                  ..Default::default()
              }),
              ..Default::default()
        },
        ..Default::default()
      },
    ];
    // let mut result = command_manager.execute(options, SaveOptions { ..Default::default() }, context).unwrap();
    let mut result = dst1.add_fields(
        options,
        SaveOptions { ..Default::default() },
        // context
    ).await.unwrap();

    let field_id = result.data.clone().unwrap();
    let str_field_id: String = from_value(field_id.clone()).unwrap();

    result = normal_compare(result);

    let operation = Operation {
        actions: vec![
            ActionOTO {
              op_name: "LI".to_string(),
              op: json0::Operation {
                  p: vec![
                      PathSegment::String("meta".to_string()),
                      PathSegment::String("views".to_string()),
                      PathSegment::Number(0),
                      PathSegment::String("columns".to_string()),
                      PathSegment::Number(2),
                  ],
                  kind: OperationKind::ListInsert {
                    li: to_value(ListInsert{
                      field_id: field_id.clone(),
                      hidden: false,
                    }).unwrap()
                  },
                }
            },
            ActionOTO {
              op_name: "LI".to_string(),
              op: json0::Operation {
                  p: vec![
                      PathSegment::String("meta".to_string()),
                      PathSegment::String("views".to_string()),
                      PathSegment::Number(1),
                      PathSegment::String("columns".to_string()),
                      PathSegment::Number(2),
                  ],
                  kind: OperationKind::ListInsert {
                    li: to_value(ListInsert{
                      field_id: field_id.clone(),
                      hidden: true,
                    }).unwrap()
                  },
                }
            },
            ActionOTO {
              op_name: "LI".to_string(),
              op: json0::Operation {
                  p: vec![
                      PathSegment::String("meta".to_string()),
                      PathSegment::String("views".to_string()),
                      PathSegment::Number(2),
                      PathSegment::String("columns".to_string()),
                      PathSegment::Number(2),
                  ],
                  kind: OperationKind::ListInsert {
                    li: to_value(ListInsert{
                      field_id: field_id.clone(),
                      hidden: false,
                    }).unwrap()
                  },
                }
            },
            ActionOTO {
              op_name: "OI".to_string(),
              op: json0::Operation {
                  p: vec![
                      PathSegment::String("meta".to_string()),
                      PathSegment::String("fieldMap".to_string()),
                      PathSegment::String(str_field_id.clone()),
                  ],
                  kind: OperationKind::ObjectInsert {
                    oi: to_value(FieldSO{
                      id: str_field_id,
                      name: "field 3".to_string(),
                      property: Some(FieldPropertySO {
                          auto_fill: Some(false),
                          date_format: Some(DateFormat::YyyyMm),
                          include_time: Some(false),
                          time_format: Some(TimeFormat::HHmm),
                          ..Default::default()
                      }),
                      kind: FieldKindSO::DateTime,
                      ..Default::default()
                    }).unwrap()
                  },
                }
            },
        ],
        cmd: CollaCommandName::AddFields.to_string(),
        ..Default::default()
    };

    compare_result_and_expected(operation, result)
  }

  #[tokio::test]
  async fn delete_fld2() {
    let context = get_context();
    let mut dst1 = get_datasheet(context);
    let mut result = dst1.delete_fields(
        vec![
            DeleteFieldData {
                field_id: "fld2".to_string(),
                ..Default::default()
            },
        ],
        SaveOptions { ..Default::default() },
        // context,
    ).await.unwrap();

    result = normal_compare(result);

    let operation = Operation {
        actions: vec![
          ActionOTO {
            op_name: "OD".to_string(),
            op: json0::Operation {
                p: vec![
                    PathSegment::String("recordMap".to_string()),
                    PathSegment::String("rec1".to_string()),
                    PathSegment::String("data".to_string()),
                    PathSegment::String("fld2".to_string()),
                ],
                kind: OperationKind::ObjectDelete {
                  od: to_value(vec!["opt2".to_string(), "opt1".to_string()]).unwrap()
                },
              }
          },
          ActionOTO {
            op_name: "OD".to_string(),
            op: json0::Operation {
                p: vec![
                    PathSegment::String("recordMap".to_string()),
                    PathSegment::String("rec2".to_string()),
                    PathSegment::String("data".to_string()),
                    PathSegment::String("fld2".to_string()),
                ],
                kind: OperationKind::ObjectDelete {
                  od: to_value(vec!["opt1".to_string()]).unwrap()
                },
              }
          },
          ActionOTO {
            op_name: "OD".to_string(),
            op: json0::Operation {
                p: vec![
                    PathSegment::String("recordMap".to_string()),
                    PathSegment::String("rec3".to_string()),
                    PathSegment::String("data".to_string()),
                    PathSegment::String("fld2".to_string()),
                ],
                kind: OperationKind::ObjectDelete {
                  od: Value::Array(vec![])
                },
              }
          },
          ActionOTO {
            op_name: "OD".to_string(),
            op: json0::Operation {
                p: vec![
                    PathSegment::String("recordMap".to_string()),
                    PathSegment::String("rec4".to_string()),
                    PathSegment::String("data".to_string()),
                    PathSegment::String("fld2".to_string()),
                ],
                kind: OperationKind::ObjectDelete {
                  od: to_value(vec!["opt3".to_string(), "opt2".to_string(), "opt1".to_string()]).unwrap()
                },
              }
          },
          ActionOTO {
            op_name: "OD".to_string(),
            op: json0::Operation {
                p: vec![
                    PathSegment::String("recordMap".to_string()),
                    PathSegment::String("rec5".to_string()),
                    PathSegment::String("data".to_string()),
                    PathSegment::String("fld2".to_string()),
                ],
                kind: OperationKind::ObjectDelete {
                  od: to_value(vec!["opt3".to_string()]).unwrap()
                },
              }
          },
          ActionOTO {
            op_name: "LD".to_string(),
            op: json0::Operation {
                p: vec![
                    PathSegment::String("meta".to_string()),
                    PathSegment::String("views".to_string()),
                    PathSegment::Number(0),
                    PathSegment::String("columns".to_string()),
                    PathSegment::Number(1),
                ],
                kind: OperationKind::ListDelete {
                  ld: to_value(ListDelete {field_id: "fld2".to_string()}).unwrap()
                },
              }
          },
          ActionOTO {
            op_name: "LD".to_string(),
            op: json0::Operation {
                p: vec![
                    PathSegment::String("meta".to_string()),
                    PathSegment::String("views".to_string()),
                    PathSegment::Number(1),
                    PathSegment::String("columns".to_string()),
                    PathSegment::Number(1),
                ],
                kind: OperationKind::ListDelete {
                  ld: to_value(ListInsert {
                    field_id: to_value("fld2".to_string()).unwrap(),
                    hidden: true
                  }).unwrap()
                },
              }
          },
          ActionOTO {
            op_name: "LD".to_string(),
            op: json0::Operation {
                p: vec![
                    PathSegment::String("meta".to_string()),
                    PathSegment::String("views".to_string()),
                    PathSegment::Number(2),
                    PathSegment::String("columns".to_string()),
                    PathSegment::Number(1),
                ],
                kind: OperationKind::ListDelete {
                  ld: to_value(ListDelete {field_id: "fld2".to_string()}).unwrap()
                },
              }
          },
          ActionOTO {
            op_name: "OD".to_string(),
            op: json0::Operation {
                p: vec![
                    PathSegment::String("meta".to_string()),
                    PathSegment::String("fieldMap".to_string()),
                    PathSegment::String("fld2".to_string()),
                ],
                kind: OperationKind::ObjectDelete {
                  od: to_value(FieldSO{
                    id: "fld2".to_string(),
                    name: "field 2".to_string(),
                    property: Some(FieldPropertySO {
                      default_value: Some(to_value(vec!["opt2".to_string(),"opt1".to_string()]).unwrap()),
                      options: Some(vec![
                        SingleSelectProperty {
                          color: 0,
                          id: "opt1".to_string(),
                          name: "option 1".to_string()
                        },
                        SingleSelectProperty {
                          color: 1,
                          id: "opt2".to_string(),
                          name: "option 2".to_string()
                        },
                        SingleSelectProperty {
                          color: 2,
                          id: "opt3".to_string(),
                          name: "option 3".to_string()
                        }
                      ]),
                      ..Default::default()
                    }),
                    kind: FieldKindSO::MultiSelect,
                    ..Default::default()
                  }).unwrap()
                },
              }
          },
        ],
        cmd: CollaCommandName::DeleteField.to_string(),
        ..Default::default()
    };

    compare_result_and_expected(operation, result)
  }

  #[tokio::test]
  async fn update_fld2() {
    let context = get_context();
    let mut dst1 = get_datasheet(context);
    let mut result = dst1.update_field(
        FieldSO {
            id: "fld2".to_string(),
            name: "FIELD 2".to_string(),
            kind: FieldKindSO::MultiSelect,
            property: Some(FieldPropertySO {
                options: Some(vec![
                    SingleSelectProperty {
                        id: "opt1".to_string(),
                        name: "OPTION 1".to_string(),
                        color: 3,
                    },
                    SingleSelectProperty {
                        id: "opt2".to_string(),
                        name: "option 2".to_string(),
                        color: 6,
                    },
                    SingleSelectProperty {
                        id: "opt3".to_string(),
                        name: "option 3".to_string(),
                        color: 4,
                    },
                ]),
                default_value: Some(to_value(vec!["opt1".to_string()]).unwrap()),
                ..Default::default()
            }),
            ..Default::default()
        },
        SaveOptions { ..Default::default() },
    ).await.unwrap();

    result = normal_compare(result);

    let operation = Operation {
        actions: vec![
          ActionOTO {
            op_name: "OR".to_string(),
            op: json0::Operation {
                p: vec![
                    PathSegment::String("meta".to_string()),
                    PathSegment::String("fieldMap".to_string()),
                    PathSegment::String("fld2".to_string()),
                ],
                kind: OperationKind::ObjectReplace {
                    od: serde_json::to_value(FieldSO {
                      id: "fld2".to_string(),
                      name: "field 2".to_string(),
                      property: Some(FieldPropertySO {
                          default_value: Some(to_value(vec!["opt2".to_string(), "opt1".to_string()]).unwrap()),
                          options: Some(vec![
                              SingleSelectProperty {
                                  color: 0,
                                  id: "opt1".to_string(),
                                  name: "option 1".to_string(),
                              },
                              SingleSelectProperty {
                                  color: 1,
                                  id: "opt2".to_string(),
                                  name: "option 2".to_string(),
                              },
                              SingleSelectProperty {
                                  color: 2,
                                  id: "opt3".to_string(),
                                  name: "option 3".to_string(),
                              },
                          ]),
                          ..Default::default()
                      }),
                      kind: FieldKindSO::MultiSelect,
                      ..Default::default()
                    }).unwrap(),
                    oi: serde_json::to_value(FieldSO {
                      id: "fld2".to_string(),
                      name: "FIELD 2".to_string(),
                      property: Some(FieldPropertySO {
                          default_value: Some(to_value(vec![ "opt1".to_string()]).unwrap()),
                          options: Some(vec![
                              SingleSelectProperty {
                                  color: 3,
                                  id: "opt1".to_string(),
                                  name: "OPTION 1".to_string(),
                              },
                              SingleSelectProperty {
                                  color: 6,
                                  id: "opt2".to_string(),
                                  name: "option 2".to_string(),
                              },
                              SingleSelectProperty {
                                  color: 4,
                                  id: "opt3".to_string(),
                                  name: "option 3".to_string(),
                              },
                          ]),
                          ..Default::default()
                      }),
                      kind: FieldKindSO::MultiSelect,
                      ..Default::default()
                    }).unwrap(),
                },
            },
          }
        ],
        cmd: CollaCommandName::SetFieldAttr.to_string(),
        ..Default::default()
    };

    compare_result_and_expected(operation, result)
  }

  #[tokio::test]
  async fn add_a_grid_view() {
    let context = get_context();
    let mut dst1 = get_datasheet(context);
    let mut result = dst1.add_views(
        vec![
            AddView{
              view: ViewSO {
                id: Some("viw12".to_string()),
                name: Some("New View Gantt 12".to_string()),
                // r#type: Some(ViewType::Gantt),
                r#type: Some(6),
                frozen_column_count: Some(1),
                ..Default::default()
              },
              ..Default::default()
          },
        ],
        SaveOptions { ..Default::default() },
    ).await.unwrap();

    result = normal_compare(result);

    let operation = Operation {
        actions: vec![
          ActionOTO {
            op_name: "LI".to_string(),
            op: json0::Operation {
                p: vec![
                    PathSegment::String("meta".to_string()),
                    PathSegment::String("views".to_string()),
                    PathSegment::Number(3),
                ],
                kind: OperationKind::ListInsert {
                  li: to_value(&ViewSO {
                    id: Some("viw12".to_string()),
                    name: Some("New View Gantt 12".to_string()),
                    // r#type: Some(ViewType::Gantt),
                    r#type: Some(6),
                    // style: Some(ViewStyleSo {..Default::default()}),
                    // rows: vec![],
                    // columns: vec![],
                    frozen_column_count: Some(1),
                    ..Default::default()
                  }).unwrap()
                },
              }
          }
        ],
        cmd: CollaCommandName::AddViews.to_string(),
        ..Default::default()
    };

    compare_result_and_expected(operation, result)
  }

  #[tokio::test]
  async fn add_a_gantt_view_in_middle() {
    let context = get_context();
    let mut dst1 = get_datasheet(context);
    let result = dst1.add_views(
        vec![
          AddView {
                start_index: Some(1),
                view: ViewSO {
                  id: Some("viw11".to_string()),
                  name: Some("New View 111".to_string()),
                  // r#type: Some(ViewType::Grid),
                  r#type: Some(1),
                  frozen_column_count: Some(1),
                  ..Default::default()
                },
            },
        ],
        SaveOptions { ..Default::default() },
    ).await.unwrap();

    let result = normal_compare(result);

    let operation = Operation {
        actions: vec![
          ActionOTO {
            op_name: "LI".to_string(),
            op: json0::Operation {
                p: vec![
                    PathSegment::String("meta".to_string()),
                    PathSegment::String("views".to_string()),
                    PathSegment::Number(1),
                ],
                kind: OperationKind::ListInsert {
                  li: to_value(&ViewSO {
                    id: Some("viw11".to_string()),
                    name: Some("New View 111".to_string()),
                    // r#type: Some(ViewType::Grid),
                    r#type: Some(1),
                    // style: Some(ViewStyleSo {..Default::default()}),
                    // rows: vec![],
                    // columns: vec![],
                    frozen_column_count: Some(1),
                    ..Default::default()
                  }).unwrap()
                },
              }
          }
        ],
        cmd: CollaCommandName::AddViews.to_string(),
        ..Default::default()
    };

    compare_result_and_expected(operation, result)
  }

  #[tokio::test]
  async fn delete_single_view() {
    let context = get_context();
    let mut dst1 = get_datasheet(context);
    let result = dst1.delete_views(
        vec!["viw2".to_string()],
        SaveOptions { ..Default::default() },
    ).await.unwrap();

    let result = normal_compare(result);

    let operation = Operation {
        actions: vec![
          ActionOTO {
            op_name: "LD".to_string(),
            op: json0::Operation {
                p: vec![
                    PathSegment::String("meta".to_string()),
                    PathSegment::String("views".to_string()),
                    PathSegment::Number(1),
                ],
                kind: OperationKind::ListDelete {
                  ld: to_value(ViewSO {
                        id: Some("viw2".to_string()),
                        // view_type: ViewType::Grid,
                        r#type: Some(1),
                        columns: vec![
                            ViewColumnSO {
                                field_id: "fld1".to_string(),
                                ..Default::default()
                            },
                            ViewColumnSO {
                                field_id: "fld2".to_string(),
                                hidden: Some(true),
                                ..Default::default()
                            },
                        ],
                        frozen_column_count: Some(1),
                        name: Some("view 2".to_string()),
                        rows: Some(vec![
                            ViewRowSO {
                                record_id: "rec2".to_string(),
                                ..Default::default()
                            },
                            ViewRowSO {
                                record_id: "rec3".to_string(),
                                ..Default::default()
                            },
                            ViewRowSO {
                                record_id: "rec5".to_string(),
                                ..Default::default()
                            },
                            ViewRowSO {
                                record_id: "rec1".to_string(),
                                ..Default::default()
                            },
                            ViewRowSO {
                                record_id: "rec4".to_string(),
                                ..Default::default()
                            },
                        ]),
                        ..Default::default()
                    }).unwrap()
                },
              }
          },
        ],
        cmd: CollaCommandName::DeleteViews.to_string(),
        ..Default::default()
    };

    compare_result_and_expected(operation, result)
  }

  #[tokio::test]
  async fn delete_multiple_views() {
    let context = get_context();
    let mut dst1 = get_datasheet(context);
    let result = dst1.delete_views(
        vec!["viw2".to_string(), "viw3".to_string()],
        SaveOptions { ..Default::default() },
    ).await.unwrap();

    let result = normal_compare(result);

    let operation = Operation {
        actions: vec![
          ActionOTO {
            op_name: "LD".to_string(),
            op: json0::Operation {
                p: vec![
                    PathSegment::String("meta".to_string()),
                    PathSegment::String("views".to_string()),
                    PathSegment::Number(1),
                ],
                kind: OperationKind::ListDelete {
                  ld: to_value(ViewSO {
                        id: Some("viw2".to_string()),
                        // view_type: ViewType::Grid,
                        r#type: Some(1),
                        columns: vec![
                            ViewColumnSO {
                                field_id: "fld1".to_string(),
                                ..Default::default()
                            },
                            ViewColumnSO {
                                field_id: "fld2".to_string(),
                                hidden: Some(true),
                                ..Default::default()
                            },
                        ],
                        frozen_column_count: Some(1),
                        name: Some("view 2".to_string()),
                        rows: Some(vec![
                            ViewRowSO {
                                record_id: "rec2".to_string(),
                                ..Default::default()
                            },
                            ViewRowSO {
                                record_id: "rec3".to_string(),
                                ..Default::default()
                            },
                            ViewRowSO {
                                record_id: "rec5".to_string(),
                                ..Default::default()
                            },
                            ViewRowSO {
                                record_id: "rec1".to_string(),
                                ..Default::default()
                            },
                            ViewRowSO {
                                record_id: "rec4".to_string(),
                                ..Default::default()
                            },
                        ]),
                        ..Default::default()
                    }).unwrap()
                },
              }
          },
          ActionOTO {
            op_name: "LD".to_string(),
            op: json0::Operation {
                p: vec![
                    PathSegment::String("meta".to_string()),
                    PathSegment::String("views".to_string()),
                    PathSegment::Number(1),
                ],
                kind: OperationKind::ListDelete {
                  ld: to_value(ViewSO {
                        id: Some("viw3".to_string()),
                        // view_type: ViewType::Grid,
                        r#type: Some(1),
                        columns: vec![
                            ViewColumnSO {
                                field_id: "fld1".to_string(),
                                ..Default::default()
                            },
                            ViewColumnSO {
                                field_id: "fld2".to_string(),
                                ..Default::default()
                            },
                        ],
                        frozen_column_count: Some(1),
                        name: Some("view 3".to_string()),
                        rows: Some(vec![
                            ViewRowSO {
                                record_id: "rec3".to_string(),
                                ..Default::default()
                            },
                            ViewRowSO {
                                record_id: "rec1".to_string(),
                                ..Default::default()
                            },
                            ViewRowSO {
                                record_id: "rec2".to_string(),
                                ..Default::default()
                            },
                            ViewRowSO {
                                record_id: "rec6".to_string(),
                                ..Default::default()
                            },
                            ViewRowSO {
                                record_id: "rec4".to_string(),
                                ..Default::default()
                            },
                        ]),
                        ..Default::default()
                    }).unwrap()
                },
              }
          },
        ],
        cmd: CollaCommandName::DeleteViews.to_string(),
        ..Default::default()
    };

    compare_result_and_expected(operation, result)
  }
}
