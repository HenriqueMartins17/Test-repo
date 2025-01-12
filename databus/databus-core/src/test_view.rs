
#[cfg(test)]
mod tests {
    use std::collections::HashMap;
    use std::rc::Rc;
    use crate::logic::FieldsOptions;
    use crate::logic::IAddRecordsOptions;
    use crate::logic::Pagination;
    use crate::mock::get_datasheet;
    use crate::ot::CollaCommandName;
    use crate::ot::changeset::Operation;
    use crate::ot::changeset::ResourceOpsCollect;
    use crate::ot::commands::AddRecordsOptions;
    use crate::ot::commands::CollaCommandExecuteResult;
    use crate::ot::commands::ExecuteResult;
    use crate::ot::commands::ExecuteType;
    use crate::ot::commands::ModifyView;
    use crate::ot::commands::SaveOptions;
    use crate::ot::commands::SaveResult;
    use crate::ot::types::ActionOTO;
    use crate::ot::types::ResourceType;
    use crate::so::CellValueSo;
    use crate::so::FieldKindSO;
    use crate::so::ViewColumnSO;
    use crate::so::ViewRowSO;
    use crate::so::types::ViewType;
    use databus_shared::prelude::HashMapExt;
    use json0::operation::OperationKind;
    use json0::operation::PathSegment;
    use serde_json::Value;
    use serde_json::to_value;
    use serde::{Deserialize, Serialize};
    use crate::prelude::ViewSO;

    use crate::{so::{DatasheetPackContext, prepare_context_data, RecordSO}, mock::{get_datasheet_map_pack, get_datasheet_pack}, logic::IRecordsOptions};

    #[derive(Debug, Deserialize, Serialize)]
    #[serde(rename_all = "camelCase")]
    struct RecordValue {
      pub r#type: usize,
      pub text: String,
    }

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

    #[test]
    fn basic_view_info() {
        let mut dst1 = get_datasheet(get_context());
        let view1 = dst1.get_view("viw1");
        assert!(view1.is_some());
        let mut view1 = view1.unwrap();
        assert_eq!(view1.id(), "viw1");

        assert_eq!(view1.view_type(), ViewType::Grid);
        assert_eq!(view1.name(), "view 1");
        assert_eq!(view1.columns(), vec![ViewColumnSO { field_id: "fld1".to_string(), ..Default::default() }, ViewColumnSO { field_id: "fld2".to_string(), ..Default::default() }]);
        assert_eq!(view1.rows(), vec![
            ViewRowSO { record_id: "rec1".to_string(), ..Default::default() }, 
            ViewRowSO { record_id: "rec2".to_string(), ..Default::default() }, 
            ViewRowSO { record_id: "rec3".to_string(), ..Default::default() }, 
            ViewRowSO { record_id: "rec4".to_string(), ..Default::default() }, 
            ViewRowSO { record_id: "rec5".to_string(), ..Default::default() }
        ]);
    }

    #[test]
    fn should_not_include_hidden_fields_by_default() {
        let mut dst1 = get_datasheet(get_context());
        let view2 = dst1.get_view("viw2");
        assert!(view2.is_some());
        let view2 = view2.unwrap();
        assert_eq!(view2.id(), "viw2");

        let fields = view2.get_fields(FieldsOptions::default());

        let field_ids = fields.iter().map(|field| field.id().clone()).collect::<Vec<_>>();

        assert_eq!(field_ids, vec!["fld1"]);
    }

    #[test]
    fn should_include_hidden_fields_if_include_hidden_is_true() {
        let mut dst1 = get_datasheet(get_context());
        let view2 = dst1.get_view("viw2");
        assert!(view2.is_some());
        let view2 = view2.unwrap();
        assert_eq!(view2.id(), "viw2");

        let fields = view2.get_fields(FieldsOptions { include_hidden: Some(true) });

        let field_ids = fields.iter().map(|field| field.id().clone()).collect::<Vec<_>>();

        assert_eq!(field_ids, vec!["fld1", "fld2"]);
    }

    #[test]
    fn should_return_records_in_order_of_rows_in_the_view() {
        let mut dst1 = get_datasheet(get_context());
        let view2 = dst1.get_view("viw2");
        assert!(view2.is_some());
        let view2 = view2.unwrap();
        assert_eq!(view2.id(), "viw2");

        let records = view2.get_records(IRecordsOptions::default());

        let record_ids = records.iter().map(|record| record.id().clone()).collect::<Vec<_>>();

        assert_eq!(record_ids, vec!["rec2", "rec3", "rec5", "rec1", "rec4"]);
    }

    #[test]
    fn max_records_limit_number_of_records() {
        let mut dst1 = get_datasheet(get_context());
        let view2 = dst1.get_view("viw2");
        assert!(view2.is_some());
        let view2 = view2.unwrap();
        assert_eq!(view2.id(), "viw2");

        let records = view2.get_records(IRecordsOptions { max_records: Some(3), ..Default::default() });

        let record_ids = records.iter().map(|record| record.id().clone()).collect::<Vec<_>>();

        assert_eq!(record_ids, vec!["rec2", "rec3", "rec5"]);
    }

    #[test]
    fn max_records_greater_than_total_number_of_records() {
        let mut dst1 = get_datasheet(get_context());
        let view2 = dst1.get_view("viw2");
        assert!(view2.is_some());
        let view2 = view2.unwrap();
        assert_eq!(view2.id(), "viw2");

        let records = view2.get_records(IRecordsOptions { max_records: Some(20), ..Default::default() });

        let record_ids = records.iter().map(|record| record.id().clone()).collect::<Vec<_>>();

        assert_eq!(record_ids, vec!["rec2", "rec3", "rec5", "rec1", "rec4"]);
    }

    #[test]
    fn page_num_1_page_size_3() {
        let mut dst1 = get_datasheet(get_context());
        let view2 = dst1.get_view("viw2");
        assert!(view2.is_some());
        let view2 = view2.unwrap();
        assert_eq!(view2.id(), "viw2");

        let records = view2.get_records(IRecordsOptions { pagination: Some(Pagination { page_num: 1, page_size: 3 }), ..Default::default() });

        let record_ids = records.iter().map(|record| record.id().clone()).collect::<Vec<_>>();

        assert_eq!(record_ids, vec!["rec2", "rec3", "rec5"]);
    }

    #[test]
    fn page_num_1_page_size_0() {
        let mut dst1 = get_datasheet(get_context());
        let view2 = dst1.get_view("viw2");
        assert!(view2.is_some());
        let view2 = view2.unwrap();
        assert_eq!(view2.id(), "viw2");

        let records = view2.get_records(IRecordsOptions { pagination: Some(Pagination { page_num: 1, page_size: 0 }), ..Default::default() });

        let record_ids = records.iter().map(|record| record.id().clone()).collect::<Vec<_>>();
        let tmp: Vec<&str> = Vec::new();
        assert_eq!(record_ids, tmp);
    }

    #[test]
    fn page_num_2_page_size_2() {
        let mut dst1 = get_datasheet(get_context());
        let view2 = dst1.get_view("viw2");
        assert!(view2.is_some());
        let view2 = view2.unwrap();
        assert_eq!(view2.id(), "viw2");

        let records = view2.get_records(IRecordsOptions { pagination: Some(Pagination { page_num: 2, page_size: 2 }), ..Default::default() });

        let record_ids = records.iter().map(|record| record.id().clone()).collect::<Vec<_>>();

        assert_eq!(record_ids, vec!["rec5", "rec1"]);
    }

    #[test]
    fn page_num_2_page_size_3() {
        let mut dst1 = get_datasheet(get_context());
        let view2 = dst1.get_view("viw2");
        assert!(view2.is_some());
        let view2 = view2.unwrap();
        assert_eq!(view2.id(), "viw2");

        let records = view2.get_records(IRecordsOptions { pagination: Some(Pagination { page_num: 2, page_size: 3 }), ..Default::default() });

        let record_ids = records.iter().map(|record| record.id().clone()).collect::<Vec<_>>();

        assert_eq!(record_ids, vec!["rec1", "rec4"]);
    }

    #[test]
    fn page_num_3_page_size_3() {
        let mut dst1 = get_datasheet(get_context());
        let view2 = dst1.get_view("viw2");
        assert!(view2.is_some());
        let view2 = view2.unwrap();
        assert_eq!(view2.id(), "viw2");

        let records = view2.get_records(IRecordsOptions { pagination: Some(Pagination { page_num: 3, page_size: 3 }), ..Default::default() });

        let record_ids = records.iter().map(|record| record.id().clone()).collect::<Vec<_>>();
        let tmp: Vec<&str> = Vec::new();
        assert_eq!(record_ids, tmp);
    }

    #[test]
    fn max_records_4_and_page_num_2_page_size_3() {
        let mut dst1 = get_datasheet(get_context());
        let view2 = dst1.get_view("viw2");
        assert!(view2.is_some());
        let view2 = view2.unwrap();
        assert_eq!(view2.id(), "viw2");

        let records = view2.get_records(IRecordsOptions { max_records: Some(4), pagination: Some(Pagination { page_num: 2, page_size: 3 }) });

        let record_ids = records.iter().map(|record| record.id().clone()).collect::<Vec<_>>();

        assert_eq!(record_ids, vec!["rec1"]);
    }

    #[tokio::test]
    async fn add_two_records_with_count() {
        let mut dst1 = get_datasheet(get_context());
        let view1 = dst1.get_view("viw1");
        assert!(view1.is_some());
        let mut view1 = view1.unwrap();
        assert_eq!(view1.id(), "viw1");

        let result = view1.add_records(IAddRecordsOptions { index: 3, count: 2, ..Default::default() }, SaveOptions{..Default::default()}).await.unwrap();

        assert_eq!(result.result, ExecuteResult::Success);

        let record_ids = result.data.clone().unwrap();
    
        let data = result.data.clone().unwrap();
        if data.is_array() {
        let data = data.as_array().unwrap();
            assert_eq!(data.len(), 2);
        } else {
            panic!("data is not array");
        }
        
        let result = normal_compare(result);

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
        let mut dst1 = get_datasheet(get_context());
        let view1 = dst1.get_view("viw1");
        assert!(view1.is_some());
        let mut view1 = view1.unwrap();
        assert_eq!(view1.id(), "viw1");

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

        let result = view1.add_records(IAddRecordsOptions { index: 3, record_values: Some(vec![hm.clone(), hm2.clone()]), ..Default::default() }, SaveOptions{..Default::default()}).await.unwrap();

        let record_ids = result.data.clone().unwrap();
        let result = normal_compare(result);

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
    async fn modify_view_name() {
        let mut dst1 = get_datasheet(get_context());
        let view1 = dst1.get_view("viw1");
        assert!(view1.is_some());
        let mut view1 = view1.unwrap();
        assert_eq!(view1.id(), "viw1");

        let result = view1.modify(ModifyView { key: "name".to_string(), value: to_value("VIEW_1".to_string()).unwrap(), ..Default::default() }, SaveOptions::default()).await.unwrap();

        let result = normal_compare(result);

        let operation = Operation {
            cmd: CollaCommandName::ModifyViews.to_string(),
            actions: vec![
                ActionOTO {
                    op_name: "OR".to_string(),
                    op: json0::Operation {
                        p: vec![
                            PathSegment::String("meta".to_string()),
                            PathSegment::String("views".to_string()),
                            PathSegment::Number(0),
                            PathSegment::String("name".to_string()),
                        ],
                        kind: OperationKind::ObjectReplace {
                          od: to_value("view 1".to_string()).unwrap(),
                          oi: to_value("VIEW_1".to_string()).unwrap()
                        },
                      }
                }
            ],
            ..Default::default()
        };

        compare_result_and_expected(operation, result)
    }

    #[tokio::test]
    async fn modify_view_columns() {
        let mut dst1 = get_datasheet(get_context());
        let view1 = dst1.get_view("viw1");
        assert!(view1.is_some());
        let mut view1 = view1.unwrap();
        assert_eq!(view1.id(), "viw1");

        let result = view1.modify(ModifyView { key: "columns".to_string(), value: to_value(vec![ListInsert{
            field_id: to_value("fld2".to_string()).unwrap(),
            hidden: true
        }]).unwrap(), ..Default::default() }, SaveOptions::default()).await.unwrap();

        let result = normal_compare(result);

        let operation = Operation {
            cmd: CollaCommandName::ModifyViews.to_string(),
            actions: vec![
                ActionOTO {
                    op_name: "LR".to_string(),
                    op: json0::Operation {
                        p: vec![
                            PathSegment::String("meta".to_string()),
                            PathSegment::String("views".to_string()),
                            PathSegment::Number(0),
                            PathSegment::String("columns".to_string()),
                            PathSegment::Number(1),
                        ],
                        kind: OperationKind::ListReplace {
                          ld: to_value(ListDelete{field_id: "fld2".to_string()}).unwrap(),
                          li: to_value(ListInsert{field_id: to_value("fld2".to_string()).unwrap(), hidden: true}).unwrap()
                        },
                      }
                }
            ],
            ..Default::default()
        };

        compare_result_and_expected(operation, result)
    }

    #[tokio::test]
    async fn delete_single_view() {
        let mut dst1 = get_datasheet(get_context());
        let view1 = dst1.get_view("viw1");
        assert!(view1.is_some());
        let mut view1 = view1.unwrap();
        assert_eq!(view1.id(), "viw1");

        let result = view1.delete(SaveOptions::default()).await.unwrap();

        let result = normal_compare(result);

        let operation = Operation {
            cmd: CollaCommandName::DeleteViews.to_string(),
            actions: vec![
                ActionOTO {
                    op_name: "LD".to_string(),
                    op: json0::Operation {
                        p: vec![
                            PathSegment::String("meta".to_string()),
                            PathSegment::String("views".to_string()),
                            PathSegment::Number(0),
                        ],
                        kind: OperationKind::ListDelete {
                            ld: to_value(ViewSO {
                                id: Some("viw1".to_string()),
                                // r#type: Some(ViewType::Grid),
                                r#type: Some(1),
                                columns: vec![
                                    ViewColumnSO { field_id: "fld1".to_string(), ..Default::default() },
                                    ViewColumnSO { field_id: "fld2".to_string(), ..Default::default() },
                                ],
                                frozen_column_count: Some(1),
                                name: Some("view 1".to_string()),
                                rows: Some(vec![
                                    ViewRowSO { record_id: "rec1".to_string(), ..Default::default() },
                                    ViewRowSO { record_id: "rec2".to_string(), ..Default::default() },
                                    ViewRowSO { record_id: "rec3".to_string(), ..Default::default() },
                                    ViewRowSO { record_id: "rec4".to_string(), ..Default::default() },
                                    ViewRowSO { record_id: "rec5".to_string(), ..Default::default() },
                                ]),
                                ..Default::default()
                            }).unwrap(),
                        },
                    }
                }
            ],
            ..Default::default()
        };

        compare_result_and_expected(operation, result)
    }
}