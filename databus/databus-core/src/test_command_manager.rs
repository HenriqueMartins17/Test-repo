#[cfg(test)]
mod tests {
  use std::collections::HashMap;
  use std::rc::Rc;
  use crate::fields::property::FieldPropertySO;
  use crate::logic::IAddRecordsOptions;
  use crate::ot::{CollaCommandName, CollaCommandManager};
  use crate::ot::commands::{CollaCommandExecuteResult, ExecuteResult, ExecuteType, SaveOptions, ExecuteFailReason, DeleteFieldData, DeleteFieldOptions, LinkedActions, UpdateCommentOptions, ICollaCommandDef, CollaCommandDefExecuteResult, CommandOptions, SystemSetFieldAttrOptions};
  use crate::mock::{get_datasheet_map_pack, get_datasheet_pack, mock_ops_collects_of_delete_link_field_in_dst2, mock_operation_of_delete_link_field_in_dst2, mock_linked_operations_of_delete_link_field_in_dst2, get_datasheet};
  use crate::ot::changeset::{Operation, ResourceOpsCollect};
  use crate::ot::types::{ActionOTO, ResourceType};
  use crate::so::{FieldKindSO, RecordSO, prepare_context_data, FieldSO, Comments, CommentMsg, DatasheetPackContext};
  use crate::prelude::CellValueSo;
  use databus_shared::prelude::HashMapExt;
  use json0::operation::{PathSegment, OperationKind};
  use serde_json::{Value, to_value, from_value};
  use serde::{Deserialize, Serialize};

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

  fn get_context(i: i32) -> Rc<DatasheetPackContext>{
      let base_datasheet_pack = get_datasheet_map_pack(i).unwrap();
      let snapshot = base_datasheet_pack.snapshot;
      let mut datasheet_pack = get_datasheet_pack().unwrap();
      datasheet_pack.snapshot = snapshot.clone();
      let context = prepare_context_data(Box::new(datasheet_pack));
      let context = Rc::new(context);
      context
  }

  fn get_context_with_foreign(i: i32, f: i32) -> Rc<DatasheetPackContext>{
      let base_datasheet_pack = get_datasheet_map_pack(i).unwrap();
      let snapshot = base_datasheet_pack.snapshot;
      let mut datasheet_pack = get_datasheet_pack().unwrap();
      datasheet_pack.snapshot = snapshot.clone();
      let base_datasheet_pack = get_datasheet_map_pack(f).unwrap();
      let snapshot = base_datasheet_pack.snapshot;
      let mut pre = datasheet_pack.foreign_datasheet_map.clone().unwrap();
      for (_k, v) in &pre {
        let mut n_v = v.clone();
        n_v.snapshot = snapshot.clone();
        pre.insert(snapshot.datasheet_id.to_string(), n_v);
        break;
      }
      datasheet_pack.foreign_datasheet_map = Some(pre);
      let context = prepare_context_data(Box::new(datasheet_pack));
      let context = Rc::new(context);
      context
  }

  fn mock_operation_of_add_one_default_record_in_dst1(record_id: &str) -> Operation {
    let mut hm = HashMap::new();
    hm.insert("recordId".to_string(), record_id);
    let mut hm2 = HashMap::new();
    hm2.insert("fld2".to_string(), vec!["opt2".to_string(), "opt1".to_string()]);

    let actions = vec![
        ActionOTO {
            op_name: "LI".to_string(),
            op: json0::Operation {
                p: vec![
                    PathSegment::String("meta".to_string()),
                    PathSegment::String("views".to_string()),
                    PathSegment::Number(0),
                    PathSegment::String("rows".to_string()),
                    PathSegment::Number(0),
                ],
                kind: OperationKind::ListInsert {
                    li: to_value(&hm).unwrap(),
                },
            },
        },
        ActionOTO {
            op_name: "LI".to_string(),
            op: json0::Operation {
                p: vec![
                    PathSegment::String("meta".to_string()),
                    PathSegment::String("views".to_string()),
                    PathSegment::Number(1),
                    PathSegment::String("rows".to_string()),
                    PathSegment::Number(3),
                ],
                kind: OperationKind::ListInsert {
                    li: to_value(&hm).unwrap(),
                },
            },
        },
        ActionOTO {
            op_name: "OI".to_string(),
            op: json0::Operation {
                p: vec![
                    PathSegment::String("recordMap".to_string()),
                    PathSegment::String(record_id.to_string()),
                ],
                kind: OperationKind::ObjectInsert {
                    oi: to_value(&RecordSO {
                        id: record_id.to_string(),
                        comment_count: 0,
                        data: to_value(hm2).unwrap(),
                        ..Default::default()
                    }).unwrap(),
                },
            },
        },
    ];
    let field_type_map = HashMapExt::from_iter(vec![("fld2".to_owned(), FieldKindSO::MultiSelect)]);
    Operation {
        cmd: "AddRecords".to_string(),
        actions,
        field_type_map: Some(field_type_map),
        ..Default::default()
    }
  }

  fn mock_ops_collects_of_add_one_default_record_in_dst1(record_id: &str) -> Vec<ResourceOpsCollect> {
    vec![ResourceOpsCollect {
        operations: vec![mock_operation_of_add_one_default_record_in_dst1(record_id)],
        resource_id: "dst1".to_string(),
        resource_type: ResourceType::Datasheet,
        ..Default::default()
    }]
  }

  fn mock_result_of_add_one_default_record_in_dst1(record_id: &str) -> CollaCommandExecuteResult {
    CollaCommandExecuteResult {
        resource_id: "dst1".to_string(),
        resource_type: ResourceType::Datasheet,
        result: ExecuteResult::Success,
        data: Some(to_value(vec![record_id.to_string()]).unwrap()),
        operation: Some(mock_operation_of_add_one_default_record_in_dst1(record_id)),
        linked_actions: Some(vec![]),
        execute_type: Some(ExecuteType::Execute),
        resource_ops_collects: Some(mock_ops_collects_of_add_one_default_record_in_dst1(record_id)),
        ..Default::default()
    }
  }

  fn mock_result_of_delete_link_field_in_dst2() -> CollaCommandExecuteResult {
    CollaCommandExecuteResult{
      resource_id: "dst2".to_string(),
      resource_type: ResourceType::Datasheet,
      result: ExecuteResult::Success,
      data: None,
      operation: Some(mock_operation_of_delete_link_field_in_dst2()),
      linked_actions: Some(mock_linked_operations_of_delete_link_field_in_dst2()
          .iter()
          .map(|op| LinkedActions {
              actions: op.actions.clone(),
              datasheet_id: "dst3".to_string(),
          })
          .collect()),
      execute_type: Some(ExecuteType::Execute),
      resource_ops_collects: Some(mock_ops_collects_of_delete_link_field_in_dst2()),
      ..Default::default()
    }
  }

  fn mock_add_one_comment_result(dst_id: String, record_id: String, comment: Comments) -> CollaCommandExecuteResult{
    let operation = Operation {
      cmd: "UpdateComment".to_string(),
      actions: vec![
        ActionOTO {
          op_name: "LI".to_string(),
          op: json0::Operation {
              p: vec![
                  PathSegment::String("recordMap".to_string()),
                  PathSegment::String(record_id),
                  PathSegment::String("comments".to_string()),
                  PathSegment::String("emojis".to_string()),
              ],
              kind: OperationKind::ListInsert {
                  li: to_value(&comment).unwrap(),
              },
          },
        },
      ],
      ..Default::default()
    };
    CollaCommandExecuteResult {
      resource_id: dst_id.to_string(),
      resource_type: ResourceType::Datasheet,
      result: ExecuteResult::Success,
      data: None,
      operation: Some(operation.clone()),
      linked_actions: Some(vec![]),
      execute_type: Some(ExecuteType::Execute),
      resource_ops_collects: Some(vec![ResourceOpsCollect {
          operations: vec![operation],
          resource_id: dst_id.to_string(),
          resource_type: ResourceType::Datasheet,
          ..Default::default()
      }]),
      ..Default::default()
    }
  }

  fn mock_add_one_record_result(
    dst_id: &str,
    record_id: &str,
    cell_values: HashMap<String, CellValueSo>,
    actions: Vec<ActionOTO>,
  ) -> CollaCommandExecuteResult {
    let mut hm = HashMap::new();
    hm.insert("recordId".to_string(), record_id);
    let mut operation_actions = vec![
      ActionOTO {
        op_name: "LI".to_string(),
        op: json0::Operation {
            p: vec![
                PathSegment::String("meta".to_string()),
                PathSegment::String("views".to_string()),
                PathSegment::Number(0),
                PathSegment::String("rows".to_string()),
                PathSegment::Number(0),
            ],
            kind: OperationKind::ListInsert {
                li: to_value(&hm).unwrap(),
            },
        },
      },
      ActionOTO {
        op_name: "LI".to_string(),
        op: json0::Operation {
            p: vec![
                PathSegment::String("meta".to_string()),
                PathSegment::String("views".to_string()),
                PathSegment::Number(1),
                PathSegment::String("rows".to_string()),
                PathSegment::Number(3),
            ],
            kind: OperationKind::ListInsert {
                li: to_value(&hm).unwrap(),
            },
        },
      },
      ActionOTO {
        op_name: "OI".to_string(),
        op: json0::Operation {
            p: vec![
                PathSegment::String("recordMap".to_string()),
                PathSegment::String(record_id.to_string()),
            ],
            kind: OperationKind::ObjectInsert {
                oi: to_value(&RecordSO {
                    id: record_id.to_string(),
                    comment_count: 0,
                    data: to_value(cell_values).unwrap(),
                    ..Default::default()
                }).unwrap(),
            },
        },
      },
    ];
    operation_actions.extend(actions);

    let mut field_type_map = HashMapExt::default();
    field_type_map.insert("fld2".to_string(), FieldKindSO::MultiSelect);
    field_type_map.insert("fld3".to_string(), FieldKindSO::Member);
    let operation = Operation {
        cmd: "AddRecords".to_string(),
        actions: operation_actions,
        field_type_map: Some(field_type_map),
        ..Default::default()
    };

    CollaCommandExecuteResult {
        resource_id: dst_id.to_string(),
        resource_type: ResourceType::Datasheet,
        result: ExecuteResult::Success,
        data: Some(to_value(vec![record_id.to_string()]).unwrap()),
        operation: Some(operation.clone()),
        linked_actions: Some(vec![]),
        execute_type: Some(ExecuteType::Execute),
        resource_ops_collects: Some(vec![ResourceOpsCollect {
            operations: vec![operation],
            resource_id: dst_id.to_string(),
            resource_type: ResourceType::Datasheet,
            ..Default::default()
        }]),
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
  //           // println!("result_op: {:?}", result_op);
  //           // println!("expected_op: {:?}", expected_op);
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

  fn no_save_result_compare(mut result:CollaCommandExecuteResult, expected: CollaCommandExecuteResult){
    result.save_result = None;
    assert_eq!(result, expected);
    // one_by_one_test(result, expected)
  }

  #[tokio::test]
  async fn add_one_default_record_succeed() {
      let context = get_context(2);
      let mut dst1 = get_datasheet(context);
      let options = IAddRecordsOptions {
          view_id: "viw1".to_string(),
          index: 0,
          count: 1,
          ..Default::default()
      };
      let result = dst1.add_records(
        options,
        SaveOptions { ..Default::default() },
      ).await.unwrap();
      assert_eq!(result.result, ExecuteResult::Success);

      let record_id: String = from_value(result.data.clone().unwrap()[0].clone()).unwrap();
    
      let expected = mock_result_of_add_one_default_record_in_dst1(&record_id);

      no_save_result_compare(result, expected);
  }

  #[tokio::test]
  async fn should_return_none_if_adding_zero_records() {
      let context = get_context(2);
      let mut dst1 = get_datasheet(context);
      let options = IAddRecordsOptions {
          view_id: "viw1".to_string(),
          index: 0,
          count: 0,
          ..Default::default()
      };
      let result = dst1.add_records(
          options,
          SaveOptions { ..Default::default() },
      ).await.unwrap();
      assert_eq!(result.result, ExecuteResult::None);

      let expected = CollaCommandExecuteResult {
          resource_id: "dst1".to_string(),
          resource_type: ResourceType::Datasheet,
          result: ExecuteResult::None,
          ..Default::default()
      };
      no_save_result_compare(result, expected)
  }

  #[test]
  fn should_return_none_if_deleting_zero_fields() {
      let context = get_context(2);
      let data = Vec::new();
      let cmd = CollaCommandName::DeleteField;
      let dst_id = "dst1".to_string();
      let options = DeleteFieldOptions {
        cmd: cmd.clone(),
        datasheet_id: Some(dst_id.clone()),
        data,
      };
      let command_manager = CollaCommandManager::new();
      let result = command_manager.execute(CommandOptions::DeleteFieldOptions(options),context).unwrap();

      let expected = CollaCommandExecuteResult {
        resource_id: "dst1".to_string(),
        resource_type: ResourceType::Datasheet,
        result: ExecuteResult::None,
        ..Default::default()
      };
      no_save_result_compare(result, expected)
  }

  #[tokio::test]
  async fn should_fail_if_cell_value_length_not_equal_to_count_length() {
      let context = get_context(2);
      let mut dst1 = get_datasheet(context);
      let cv = Vec::new();
      let options = IAddRecordsOptions {
          view_id: "viw1".to_string(),
          index: 0,
          count: 1,
          record_values: Some(cv),
          ..Default::default()
      };
      let result = dst1.add_records(
          options,
          SaveOptions { ..Default::default() },
      ).await.unwrap();

      let expected = CollaCommandExecuteResult {
          resource_id: "dst1".to_string(),
          resource_type: ResourceType::Datasheet,
          result: ExecuteResult::Fail,
          reason: Some(ExecuteFailReason::ActionError),
          ..Default::default()
      };

      no_save_result_compare(result, expected)
  }

  #[test]
  fn should_return_none_if_command_name_not_found() {
      let context = get_context(2);
      let command_manager = CollaCommandManager::new();
      let result = command_manager.execute(CommandOptions::SystemSetFieldAttrOptions(SystemSetFieldAttrOptions {
          cmd: CollaCommandName::SystemSetFieldAttr,
      }),context).unwrap();

      assert_eq!(result, CollaCommandExecuteResult {
          resource_id: "dst1".to_string(),
          resource_type: ResourceType::Datasheet,
          result: ExecuteResult::None,
          ..Default::default()
      });
  }

  #[tokio::test]
  async fn fullfill_resource_id() {
      let context = get_context(2);
      let mut dst1 = get_datasheet(context);
      let options = IAddRecordsOptions {
        //   datasheet_id: Some("dst1".to_string()),
          view_id: "viw1".to_string(),
          index: 0,
          count: 1,
          ..Default::default()
      };
      let result = dst1.add_records(
          options,
          SaveOptions { ..Default::default() },
      ).await.unwrap();
      assert_eq!(result.result, ExecuteResult::Success);

      let record_id: String = from_value(result.data.clone().unwrap()[0].clone()).unwrap();
      let expected = mock_result_of_add_one_default_record_in_dst1(&record_id);
      no_save_result_compare(result, expected)
  }

  #[tokio::test]
  async fn handle_command_execute_error_listener() {
      let context = get_context(2);
      let mut dst1 = get_datasheet(context);
      let cv= Vec::new();
      let options = IAddRecordsOptions {
          view_id: "viw1".to_string(),
          index: 0,
          count: 1,
          record_values: Some(cv),
          ..Default::default()
      };
      let result = dst1.add_records(
          options,
          SaveOptions { ..Default::default() },
      ).await.unwrap();

      let expected = CollaCommandExecuteResult {
          resource_id: "dst1".to_string(),
          resource_type: ResourceType::Datasheet,
          result: ExecuteResult::Fail,
          reason: Some(ExecuteFailReason::ActionError),
          ..Default::default()
      };

      no_save_result_compare(result, expected)

      // assert_eq!(error, ICollaError {
      //     error_type: ErrorType::CollaError,
      //     code: ErrorCode::CommandExecuteFailed,
      //     message: t(Strings::ErrorAddRowFailedWrongLengthOfValue),
      // });
  }

  #[tokio::test]
  async fn handle_command_executed_listener() {
      let context = get_context(2);
      let mut dst1 = get_datasheet(context);
      let options = IAddRecordsOptions {
          view_id: "viw1".to_string(),
          index: 0,
          count: 1,
          ..Default::default()
      };
      let result = dst1.add_records(
          options,
          SaveOptions { ..Default::default() },
      ).await.unwrap();
      assert_eq!(result.result, ExecuteResult::Success);

      let record_id: String = from_value(result.data.clone().unwrap()[0].clone()).unwrap();
      let expected = mock_result_of_add_one_default_record_in_dst1(&record_id);
      no_save_result_compare(result, expected);

      // assert_eq!(ops_collect, mock_ops_collects_of_add_one_default_record_in_dst1(&record_id));
  }

  #[tokio::test]
  async fn should_contain_actions_to_modify_field_if_adding_a_record_with_member_cell() {
      let context = get_context(2);
      let mut dst1 = get_datasheet(context);
      let mut hm = HashMap::new();
      let vector: Vec<String> = Vec::new();
      hm.insert("fld2".to_string(), to_value(vector).unwrap());
      hm.insert("fld3".to_string(), to_value(vec!["100006".to_string(), "100002".to_string()]).unwrap());
      let options = IAddRecordsOptions {
          view_id: "viw1".to_string(),
          index: 0,
          count: 1,
          record_values: Some(vec![hm.clone()]),
          ..Default::default()
      };
      let result = dst1.add_records(
          options,
          SaveOptions { ..Default::default() },
      ).await.unwrap();
      assert_eq!(result.result, ExecuteResult::Success);
      
      let record_id: String = from_value(result.data.clone().unwrap()[0].clone()).unwrap();
      let expected = mock_add_one_record_result(
          "dst1",
          &record_id,
          hm,
          vec![ActionOTO {
            op_name: "OR".to_string(),
            op: json0::Operation {
                p: vec![
                    PathSegment::String("meta".to_string()),
                    PathSegment::String("fieldMap".to_string()),
                    PathSegment::String("fld3".to_string()),
                ],
                kind: OperationKind::ObjectReplace {
                  od: serde_json::to_value(FieldSO {
                    id: "fld3".to_string(),
                    name: "Field 3".to_string(),
                    property: Some(FieldPropertySO {
                        is_multi: Some(true),
                        should_send_msg: Some(false),
                        unit_ids: Some(vec!["100000".to_string(), "100001".to_string(), "100002".to_string()]),
                        ..Default::default()
                    }),
                    kind: FieldKindSO::Member,
                    ..Default::default()
                  }).unwrap(),
                  oi: serde_json::to_value(FieldSO {
                    id: "fld3".to_string(),
                    name: "Field 3".to_string(),
                    property: Some(FieldPropertySO {
                        is_multi: Some(true),
                        should_send_msg: Some(false),
                        unit_ids: Some(vec!["100000".to_string(), "100001".to_string(), "100002".to_string(), "100006".to_string()]),
                        ..Default::default()
                    }),
                    kind: FieldKindSO::Member,
                    ..Default::default()
                  }).unwrap(),
                },
            },
          }]
      );
      no_save_result_compare(result, expected)
  }

  #[test]
  fn should_contain_actions_to_delete_link_field_in_linked_datasheet_if_deleting_a_link_field() {
      let context = get_context_with_foreign(3, 4); //dst2 dst3
      let cmd = CollaCommandName::DeleteField;
      let options = DeleteFieldOptions {
        cmd: cmd.clone(),
        data: vec![DeleteFieldData{
            delete_brother_field: Some(true),
            field_id: "fld2-2".to_string(),
        }],
        datasheet_id: None,
      };
      let command_manager = CollaCommandManager::new();
      let result = command_manager.execute(CommandOptions::DeleteFieldOptions(options),context).unwrap();
      assert_eq!(result.result, ExecuteResult::Success);

      let expected = mock_result_of_delete_link_field_in_dst2();
      no_save_result_compare(result, expected)
  }

  #[test]
  fn update_comment() {
      let context = get_context(2);
      let cmd = CollaCommandName::UpdateComment;
      let dst_id = "dst1".to_string();
      let record_id = "rec1".to_string();
      let emojis = HashMap::new();
      let comments = Comments {
        revision: 1,
        created_at: 1000000000000,
        comment_id: "cmt1".to_string(),
        unit_id: "100001".to_string(),
        comment_msg: CommentMsg {
            r#type: "dummy".to_string(),
            content: "comment 1".to_string(),
            html: "<span>comment&nbsp;1</span>".to_string(),
            emojis: Some(emojis),
            ..Default::default()
        },
        ..Default::default()
      };
      let options = UpdateCommentOptions {
        cmd: cmd.clone(),
        datasheet_id: dst_id.clone(),
        record_id: record_id.clone(),
        comments: comments.clone(),
        emoji_action: Some(true),
      };
      let command_manager = CollaCommandManager::new();
      let result = command_manager.execute(CommandOptions::UpdateCommentOptions(options),context).unwrap();
      assert_eq!(result.result, ExecuteResult::Success);

      let expected = mock_add_one_comment_result(dst_id, record_id, comments);
      no_save_result_compare(result, expected)
  }

  #[test]
  fn register_command() {
      struct Tmp {}
      impl ICollaCommandDef for Tmp {
          fn execute (
            &self,
            _context: Rc<DatasheetPackContext>,
            _options: CommandOptions,
          ) -> anyhow::Result<Option<CollaCommandDefExecuteResult>> {         
            Ok(None)
          }
      }
      let mut command_manager = CollaCommandManager::new();
      command_manager.register("foo".to_string(), Box::new(Tmp{}));

      assert!(command_manager.has_command("foo"));
  }

  #[test]
  fn unregister_command() {
      struct Tmp {}
      impl ICollaCommandDef for Tmp {
          fn execute (
            &self,
            _context: Rc<DatasheetPackContext>,
            _options: CommandOptions,
          ) -> anyhow::Result<Option<CollaCommandDefExecuteResult>> {         
            Ok(None)
          }
      }
      let mut command_manager = CollaCommandManager::new();
      command_manager.register("foo".to_string(), Box::new(Tmp{}));
      command_manager.unregister("foo".to_string());
      
      assert!(!command_manager.has_command("foo"));
  }

  #[test]
  fn register_the_same_command_twice() {
      struct Tmp {}
      impl ICollaCommandDef for Tmp {
          fn execute (
            &self,
            _context: Rc<DatasheetPackContext>,
            _options: CommandOptions,
          ) -> anyhow::Result<Option<CollaCommandDefExecuteResult>> {         
            Ok(None)
          }
      }
      let mut command_manager = CollaCommandManager::new();
      command_manager.register("foo".to_string(), Box::new(Tmp{}));
      command_manager.register("foo".to_string(), Box::new(Tmp{}));

      assert!(command_manager.has_command("foo"));
  }

  #[test]
  fn attempt_to_undo_un_undoable_command() {
      let context = get_context(2);
      let command_manager = CollaCommandManager::new();
      let result = command_manager.execute_actions(
          CollaCommandName::DeleteComment.to_string(),
          CollaCommandDefExecuteResult {
              resource_id: "dst1".to_string(),
              resource_type: ResourceType::Datasheet,
              result: ExecuteResult::Success,
              actions: vec![],
              ..Default::default() // assuming other fields can be defaulted
          },
          ExecuteType::Undo,
          context
      ).unwrap();

      assert!(result.is_none());
  }

  #[test]
  fn attempt_to_redo_un_undoable_command() {
      let context = get_context(2);
      let command_manager = CollaCommandManager::new();
      let result = command_manager.execute_actions(
          CollaCommandName::DeleteComment.to_string(),
          CollaCommandDefExecuteResult {
              resource_id: "dst1".to_string(),
              resource_type: ResourceType::Datasheet,
              result: ExecuteResult::Success,
              actions: vec![],
              ..Default::default() // assuming other fields can be defaulted
          },
          ExecuteType::Redo,
          context
      ).unwrap();

      assert!(result.is_none());
  }

  #[test]
  fn execute_actions_of_redo_add_records() {
      let context = get_context(2);
      let command_manager = CollaCommandManager::new();
      let mut hm = HashMap::new();
      let field = context.datasheet_pack.snapshot.meta.field_map.get("fld2").clone().unwrap().clone();
      hm.insert("fld2".to_string(), field);
      let result = command_manager.execute_actions(
          CollaCommandName::AddRecords.to_string(),
          CollaCommandDefExecuteResult {
              resource_id: "dst1".to_string(),
              resource_type: ResourceType::Datasheet,
              result: ExecuteResult::Success,
              data: Some(to_value(vec!["rec1".to_string()]).unwrap()),
              actions: mock_operation_of_add_one_default_record_in_dst1("rec1").actions,
              linked_actions: Some(vec![]),
              field_map_snapshot: Some(hm),
              ..Default::default() // assuming other fields can be defaulted
          },
          ExecuteType::Redo,
          context
      ).unwrap();
      let expected = CollaCommandExecuteResult {
        execute_type: Some(ExecuteType::Redo),
        ..mock_result_of_add_one_default_record_in_dst1("rec1")
      };
      assert_eq!(result.unwrap(), expected);
  }
}
