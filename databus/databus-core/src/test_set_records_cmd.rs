#[cfg(test)]
mod tests {
  use std::rc::Rc;
  use crate::ot::commands::{CollaCommandExecuteResult, ExecuteResult, SaveResult, LinkedActions, ExecuteType};
  use crate::mock::{get_snapshot_pack, get_datasheet_pack};
  use crate::ot::changeset::{Operation, ResourceOpsCollect};
  use crate::ot::types::{ActionOTO, ResourceType};
  use crate::ro::record_update_ro::RecordUpdateRO;
  use crate::so::{FieldKindSO, DatasheetPackContext, prepare_context_data};
  use crate::Datasheet;
  use databus_shared::prelude::HashMapExt;
  use serde_json::json;
  use crate::mock::MockDataStorageProvider;

  fn get_context() -> Rc<DatasheetPackContext>{
    let datasheet_pack = get_snapshot_pack(1).unwrap();
    let snapshot = datasheet_pack.snapshot;
    let mut datasheet_pack = get_datasheet_pack().unwrap();
    datasheet_pack.snapshot = snapshot.clone();
    let context = prepare_context_data(Box::new(datasheet_pack));
    let context = Rc::new(context);
    context
  }

  #[tokio::test]
  async fn test_update_records_single_line_text(){
      let dst_id: String = "dstiaeZJYXq9G2Rt62".to_string();
      let view_id: String = "viwUFDJoVvY1Q".to_string();
      let user_id: String = "".to_string();
      let expected = CollaCommandExecuteResult {
        resource_id: dst_id.to_string(),
        resource_type: ResourceType::Datasheet,
        result: ExecuteResult::None,
        ..Default::default()
      };
      let cmd_name = "SetRecords";
      let resource_id = dst_id.to_string();
      let resource_type = ResourceType::Datasheet;
      let message_id = "jbXlnbWx7ojndVP9ATDb";
      let context = get_context();
      let data_storage_provider = Box::new(MockDataStorageProvider{});

      //single line Text相同数据修改失败测试
      let test_value = json!({
        "records": [
            {"recordId": "recykfXMQCq0E", "fields": {"Title":"100"}},
        ], 
        "fieldKey": "name"
      });
      let record_update:RecordUpdateRO = serde_json::from_value(test_value).unwrap();
      let result = Datasheet::update_records_origin(dst_id.to_string(), view_id.to_string(), user_id.to_string(), record_update, context.clone(), data_storage_provider.clone()).await.unwrap();
      assert_eq!(expected, result); 

      //single line Text不同数据修改成功测试
      let test_value = json!({
        "records": [
            {"recordId": "recykfXMQCq0E", "fields": {"Title":"300"}},
        ], 
        "fieldKey": "name"
      });
      let record_update:RecordUpdateRO = serde_json::from_value(test_value).unwrap();
      let result = Datasheet::update_records_origin(dst_id.to_string(), view_id.to_string(), user_id.to_string(), record_update, context, data_storage_provider).await.unwrap();

      let actions_value = json!([
        {
          "n":"OR",
          "p":["recordMap","recykfXMQCq0E","data","fldvoJy0VpMV5"],
          "od":[{"text":"100","type":1}], //这个为啥single line Text是1,1是text
          "oi":[{"text":"300","type":1}]
        },
      ]);

      let actions: Vec<ActionOTO> = serde_json::from_value(actions_value).unwrap();
      let mut field_type_map = HashMapExt::default();
      field_type_map.insert("fldvoJy0VpMV5".to_string(), FieldKindSO::SingleText);
      let operation = Operation{
        cmd: cmd_name.to_string(),
        actions: actions,
        field_type_map: Some(field_type_map),
        ..Default::default()
      };

      let mut operations = Vec::new();
      operations.push(operation.clone());
      let mut resource_ops_collects = Vec::new();
      let resource_ops_collect = ResourceOpsCollect {
        resource_id: resource_id.to_string(),
        resource_type: resource_type.clone(),
        operations,
        ..Default::default()
      };
      let mut operations = Vec::new();
      operations.push(operation.clone());
      resource_ops_collects.push(resource_ops_collect);
      let mut save_results = Vec::new();
      let save_result = SaveResult {
        base_revision: 12,
        message_id: message_id.to_string(),
        resource_id: resource_id.to_string(),
        resource_type: resource_type.clone(),
        operations,
      };
      save_results.push(save_result);
      let expected = CollaCommandExecuteResult {
        result: ExecuteResult::Success,
        resource_id: resource_id.to_string(),
        resource_type: resource_type.clone(),
        operation: Some(operation),
        linked_actions: Some(Vec::new()),
        resource_ops_collects: Some(resource_ops_collects),
        save_result: Some(save_results),
        execute_type: Some(ExecuteType::Execute),
        ..Default::default()
      };
      assert_eq!(expected, result);
  }

  #[tokio::test]
  async fn test_update_records_member(){
      let dst_id: String = "dstiaeZJYXq9G2Rt62".to_string();
      let view_id: String = "viwUFDJoVvY1Q".to_string();
      let user_id: String = "".to_string();
      let expected = CollaCommandExecuteResult {
        resource_id: dst_id.to_string(),
        resource_type: ResourceType::Datasheet,
        result: ExecuteResult::None,
        ..Default::default()
      };
      let cmd_name = "SetRecords";
      let resource_id = dst_id.to_string();
      let resource_type = ResourceType::Datasheet;
      let message_id = "jbXlnbWx7ojndVP9ATDb";
      let context = get_context();
      let data_storage_provider = Box::new(MockDataStorageProvider{});

      //成员类型校验13="Member", 主要看id=1696426247320952834,name和avatar无效
      //result =  {"resourceId":"dstiaeZJYXq9G2Rt62","resourceType":0,"result":"None"}
      //"message": "The unit specified by the unit ID does not exist"
      //新增其他账号成员1702157936122826753，报错同上，删除自己，再添加自己有效
      //跑oi得另外弄一个空member的表
      let test_value = json!({
        "records": [
            {"recordId": "recykfXMQCq0E", "fields": {
              "成员类型":[{
                "id": "1696426247320952834",
                "type": 13
              }]
          }},
        ], 
        "fieldKey": "name"
      });
      let record_update:RecordUpdateRO = serde_json::from_value(test_value).unwrap();
      let result = Datasheet::update_records_origin(dst_id.to_string(), view_id.to_string(), user_id.to_string(), record_update, context, data_storage_provider).await.unwrap();
      assert_eq!(expected, result); 

      let actions_value = json!([
        {
          "n":"OI",
          "p":["recordMap","recykfXMQCq0E","data","fldhCpbPF5wgg"],
          "oi":["1696426247320952834"]
        },
      ]);
      let actions: Vec<ActionOTO> = serde_json::from_value(actions_value).unwrap();
      //Member 13
      let mut field_type_map = HashMapExt::default();
      field_type_map.insert("fldhCpbPF5wgg".to_string(), FieldKindSO::Member);
      let operation = Operation{
        cmd: cmd_name.to_string(),
        actions: actions,
        field_type_map: Some(field_type_map),
        ..Default::default()
      };
      let mut operations = Vec::new();
      operations.push(operation.clone());
      let mut resource_ops_collects = Vec::new();
      let resource_ops_collect = ResourceOpsCollect {
        resource_id: resource_id.to_string(),
        resource_type: resource_type.clone(),
        operations,
        ..Default::default()
      };
      let mut operations = Vec::new();
      operations.push(operation.clone());
      resource_ops_collects.push(resource_ops_collect);
      let mut save_results = Vec::new();
      let save_result = SaveResult {
        base_revision: 12,
        message_id: message_id.to_string(),
        resource_id: resource_id.to_string(),
        resource_type: resource_type.clone(),
        operations,
      };
      save_results.push(save_result);
      let _expected = CollaCommandExecuteResult {
        result: ExecuteResult::Success,
        resource_id: resource_id.to_string(),
        resource_type: resource_type.clone(),
        operation: Some(operation),
        linked_actions: Some(Vec::new()),
        resource_ops_collects: Some(resource_ops_collects),
        save_result: Some(save_results),
        ..Default::default()
      };
      // assert_eq!(expected, result); 
  }

  #[tokio::test]
  async fn test_update_records_magic_link(){
      //要另外找数据跑测试
      let dst_id: String = "dstiaeZJYXq9G2Rt62".to_string();
      let view_id: String = "viwUFDJoVvY1Q".to_string();
      let user_id: String = "".to_string();
      let _expected = CollaCommandExecuteResult {
        resource_id: dst_id.to_string(),
        resource_type: ResourceType::Datasheet,
        result: ExecuteResult::None,
        ..Default::default()
      };
      let cmd_name = "SetRecords";
      let resource_id = dst_id.to_string();
      let resource_type = ResourceType::Datasheet;
      let _message_id = "jbXlnbWx7ojndVP9ATDb";
      let context = get_context();
      let data_storage_provider = Box::new(MockDataStorageProvider{});

      //神奇关联recwPms4kZBFk另一张表的rec，另一个recgPXtYBxUqh
      let test_value = json!({
        "records": [
            {"recordId": "recykfXMQCq0E", "fields": {"关联成绩表":["recwPms4kZBFk"]}},
        ], 
        "fieldKey": "name"
      });
      let record_update:RecordUpdateRO = serde_json::from_value(test_value).unwrap();
      let result = Datasheet::update_records_origin(dst_id.to_string(), view_id.to_string(), user_id.to_string(), record_update, context, data_storage_provider).await.unwrap();
      // assert_eq!(expected, result);

      let actions_value = json!([
        {
          // "n":"OR",
          "n":"OI",
          "p":["recordMap","recykfXMQCq0E","data","fldUWoHnuqCbB"],
          "oi": ["recwPms4kZBFk"]
          // "od":"recwPms4kZBFk",
          // "oi":"recgPXtYBxUqh"
        },
      ]);
      let actions: Vec<ActionOTO> = serde_json::from_value(actions_value).unwrap();
      //Link 7
      let mut field_type_map = HashMapExt::default();
      field_type_map.insert("fldUWoHnuqCbB".to_string(), FieldKindSO::Link);
      let operation = Operation{
        cmd: cmd_name.to_string(),
        actions: actions,
        field_type_map: Some(field_type_map),
        ..Default::default()
      };

      //转成struct
      let linked_actions_value = json!(
        {
          // "datasheetId": "dstiaeZJYXq9G2Rt62",
          "datasheetId": "dstNXByeylmqUBLB70",
          "actions":[
            {
              "n":"OI",
              "p":["recordMap","recwPms4kZBFk","data","fldiQVEetmF27"],
              "oi":["recykfXMQCq0E"]
            }
          ]
        }
      );
      // let linked_actions_value = json!(
      //   {
      //     "datasheetId": "dstiaeZJYXq9G2Rt62",
      //     "actions":[
      //       {
      //         "n":"OI",
      //         "p":["recordMap","recgPXtYBxUqh","data","fldiQVEetmF27"],
      //         "oi":["recykfXMQCq0E"]
      //       },
      //       {
      //         "n":"OD",
      //         "p":["recordMap","recwPms4kZBFk","data","fldiQVEetmF27"],
      //         "od":["recykfXMQCq0E"]
      //       }
      //     ]
      //   }
      // );
      let linked_action: LinkedActions = serde_json::from_value(linked_actions_value).unwrap();
      let mut linked_actions = Vec::new();
      linked_actions.push(linked_action.clone());

      let mut resource_ops_collects = Vec::new();

      let mut operations = Vec::new();
      let mut operation_collect = operation.clone();
      operation_collect.main_link_dst_id = Some(dst_id.to_string());
      operations.push(operation_collect);
      let resource_ops_collect = ResourceOpsCollect {
        resource_id: resource_id.to_string(),
        resource_type: resource_type.clone(),
        operations,
        ..Default::default()
      };
      resource_ops_collects.push(resource_ops_collect);

      let new_resource_id = "dstNXByeylmqUBLB70"; //神奇关联要两张表，两组数据
      let mut operations = Vec::new();
      let mut operation_collect = operation.clone();
      operation_collect.main_link_dst_id = Some(dst_id.to_string());
      operation_collect.actions = linked_action.actions.clone();
      operation_collect.field_type_map = None;
      operations.push(operation_collect);
      let resource_ops_collect = ResourceOpsCollect {
        resource_id: new_resource_id.to_string(),
        resource_type: resource_type.clone(),
        operations,
        ..Default::default()
      };
      resource_ops_collects.push(resource_ops_collect);

      let mut save_results = Vec::new();

      let mut operations = Vec::new();
      let mut operation_collect = operation.clone();
      operation_collect.main_link_dst_id = Some(dst_id.to_string());
      operations.push(operation_collect.clone());
      let message_id = "jbXlnbWx7ojndVP9ATDb";
      let save_result = SaveResult {
        base_revision: 12,
        message_id: message_id.to_string(),
        resource_id: resource_id.to_string(),
        resource_type: resource_type.clone(),
        operations,
      };
      save_results.push(save_result);

      let mut operations = Vec::new();
      let mut operation_collect = operation.clone();
      operation_collect.main_link_dst_id = Some(dst_id.to_string());
      operation_collect.actions = linked_action.actions.clone();
      operation_collect.field_type_map = None;
      operations.push(operation_collect.clone());
      let message_id = "jbXlnbWx7ojndVP9ATDb";
      
      // let resource_id = "dstiaeZJYXq9G2Rt62"; //神奇关联要两张表，两组数据
      let save_result = SaveResult {
        base_revision: 12,
        message_id: message_id.to_string(),
        resource_id: new_resource_id.to_string(),
        resource_type: resource_type.clone(),
        operations,
      };
      save_results.push(save_result);
      let expected = CollaCommandExecuteResult {
        result: ExecuteResult::Success,
        resource_id: resource_id.to_string(),
        resource_type: resource_type.clone(),
        operation: Some(operation),
        linked_actions: Some(linked_actions),
        resource_ops_collects: Some(resource_ops_collects),
        save_result: Some(save_results),
        execute_type: Some(ExecuteType::Execute),
        ..Default::default()
      };
      assert_eq!(expected, result);
      // one_by_one_test(expected, result);
  }

  #[tokio::test]
  async fn test_update_records_select(){
    let dst_id: String = "dstiaeZJYXq9G2Rt62".to_string();
    let view_id: String = "viwUFDJoVvY1Q".to_string();
    let user_id: String = "".to_string();
    let expected = CollaCommandExecuteResult {
      resource_id: dst_id.to_string(),
      resource_type: ResourceType::Datasheet,
      result: ExecuteResult::None,
      ..Default::default()
    };
    let cmd_name = "SetRecords";
    let resource_id = dst_id.to_string();
    let resource_type = ResourceType::Datasheet;
    let message_id = "jbXlnbWx7ojndVP9ATDb";
    let context = get_context();
    let data_storage_provider = Box::new(MockDataStorageProvider{});

    //单选校验
    let test_value = json!({
      "records": [
          {"recordId": "recykfXMQCq0E", "fields": {"单选类型":"差"}},
      ], 
      "fieldKey": "name"
    });
    let record_update:RecordUpdateRO = serde_json::from_value(test_value).unwrap();
    let result = Datasheet::update_records_origin(dst_id.to_string(), view_id.to_string(), user_id.to_string(), record_update, context.clone(), data_storage_provider.clone()).await.unwrap();
    assert_eq!(expected, result); 

    let test_value = json!({
      "records": [
          {"recordId": "recykfXMQCq0E", "fields": {"单选类型":"好"}},
      ], 
      "fieldKey": "name"
    });
    let record_update:RecordUpdateRO = serde_json::from_value(test_value).unwrap();
    let result = Datasheet::update_records_origin(dst_id.to_string(), view_id.to_string(), user_id.to_string(), record_update, context, data_storage_provider).await.unwrap();
    let actions_value = json!([
      {
        "n":"OR",
        "p":["recordMap","recykfXMQCq0E","data","fldYTgHxM4Nkx"],
        "od":"opt1ASNU4a2jg",
        "oi":"optcecFvI3KW7"
      },
    ]);
    let actions: Vec<ActionOTO> = serde_json::from_value(actions_value).unwrap();
    //SingleSelect 3
    let mut field_type_map = HashMapExt::default();
    field_type_map.insert("fldYTgHxM4Nkx".to_string(), FieldKindSO::SingleSelect);
    let operation = Operation{
      cmd: cmd_name.to_string(),
      actions: actions,
      field_type_map: Some(field_type_map),
      ..Default::default()
    };
    let mut operations = Vec::new();
    operations.push(operation.clone());
    let mut resource_ops_collects = Vec::new();
    let resource_ops_collect = ResourceOpsCollect {
      resource_id: resource_id.to_string(),
      resource_type: resource_type.clone(),
      operations,
      ..Default::default()
    };
    resource_ops_collects.push(resource_ops_collect);
    let mut operations = Vec::new();
    operations.push(operation.clone());
    let mut save_results = Vec::new();
    let save_result = SaveResult {
      base_revision: 12,
      message_id: message_id.to_string(),
      resource_id: resource_id.to_string(),
      resource_type: resource_type.clone(),
      operations,
    };
    save_results.push(save_result);
    let expected = CollaCommandExecuteResult {
      result: ExecuteResult::Success,
      resource_id: resource_id.to_string(),
      resource_type: resource_type.clone(),
      operation: Some(operation),
      linked_actions: Some(Vec::new()),
      resource_ops_collects: Some(resource_ops_collects),
      save_result: Some(save_results),
      execute_type: Some(ExecuteType::Execute),
      ..Default::default()
    };
    assert_eq!(result, expected); 
  }

  #[tokio::test]
  async fn test_update_records_multi_select(){
      let dst_id: String = "dstiaeZJYXq9G2Rt62".to_string();
      let view_id: String = "viwUFDJoVvY1Q".to_string();
      let user_id: String = "".to_string();
      let expected = CollaCommandExecuteResult {
        resource_id: dst_id.to_string(),
        resource_type: ResourceType::Datasheet,
        result: ExecuteResult::None,
        ..Default::default()
      };
      let cmd_name = "SetRecords";
      let resource_id = dst_id.to_string();
      let resource_type = ResourceType::Datasheet;
      let _message_id = "jbXlnbWx7ojndVP9ATDb";
      let context = get_context();
      let data_storage_provider = Box::new(MockDataStorageProvider{});

      //多选
      let test_value = json!({
        "records": [
            {"recordId": "recykfXMQCq0E", "fields": {"多选类型":["好","良","差"]}},
        ], 
        "fieldKey": "name"
      });
      let record_update:RecordUpdateRO = serde_json::from_value(test_value).unwrap();
      let result = Datasheet::update_records_origin(dst_id.to_string(), view_id.to_string(), user_id.to_string(), record_update, context.clone(), data_storage_provider.clone()).await.unwrap();
      assert_eq!(expected, result);

      let test_value = json!({
        "records": [
            {"recordId": "recykfXMQCq0E", "fields": {"多选类型":["好","良"]}},
        ], 
        "fieldKey": "name"
      });
      let record_update:RecordUpdateRO = serde_json::from_value(test_value).unwrap();
      let result = Datasheet::update_records_origin(dst_id.to_string(), view_id.to_string(), user_id.to_string(), record_update, context, data_storage_provider).await.unwrap();
      let actions_value = json!([
        {
          "n":"OR",
          "p":["recordMap","recykfXMQCq0E","data","fldypjJb300zp"],
          "od":["opt3PIgcGz7Mf","optxHHBOJ1Ot5","optBSB4eKgy30"],
          "oi":["opt3PIgcGz7Mf","optxHHBOJ1Ot5"]
        },
      ]);
      let actions: Vec<ActionOTO> = serde_json::from_value(actions_value).unwrap();
      //MultiSelect 4
      let mut field_type_map = HashMapExt::default();
      field_type_map.insert("fldypjJb300zp".to_string(), FieldKindSO::MultiSelect);
      let operation = Operation{
        cmd: cmd_name.to_string(),
        actions: actions,
        field_type_map: Some(field_type_map),
        ..Default::default()
      };
      let mut operations = Vec::new();
      operations.push(operation.clone());
      let mut resource_ops_collects = Vec::new();
      let resource_ops_collect = ResourceOpsCollect {
        resource_id: resource_id.to_string(),
        resource_type: resource_type.clone(),
        operations,
        ..Default::default()
      };
      resource_ops_collects.push(resource_ops_collect);
      let mut operations = Vec::new();
      operations.push(operation.clone());
      let mut save_results = Vec::new();
      let message_id = "jbXlnbWx7ojndVP9ATDb";
      let save_result = SaveResult {
        base_revision: 12,
        message_id: message_id.to_string(),
        resource_id: resource_id.to_string(),
        resource_type: resource_type.clone(),
        operations,
      };
      save_results.push(save_result);
      let expected = CollaCommandExecuteResult {
        result: ExecuteResult::Success,
        resource_id: resource_id.to_string(),
        resource_type: resource_type.clone(),
        operation: Some(operation),
        linked_actions: Some(Vec::new()),
        resource_ops_collects: Some(resource_ops_collects),
        save_result: Some(save_results),
        execute_type: Some(ExecuteType::Execute),
        ..Default::default()
      };
      assert_eq!(expected, result); 
  }

  #[tokio::test]
  async fn test_update_records_number(){
      let dst_id: String = "dstiaeZJYXq9G2Rt62".to_string();
      let view_id: String = "viwUFDJoVvY1Q".to_string();
      let user_id: String = "".to_string();
      let expected = CollaCommandExecuteResult {
        resource_id: dst_id.to_string(),
        resource_type: ResourceType::Datasheet,
        result: ExecuteResult::None,
        ..Default::default()
      };
      let cmd_name = "SetRecords";
      let resource_id = dst_id.to_string();
      let resource_type = ResourceType::Datasheet;
      let message_id = "jbXlnbWx7ojndVP9ATDb";
      let context = get_context();
      let data_storage_provider = Box::new(MockDataStorageProvider{});

      //数字
      let test_value = json!({
        "records": [
            {"recordId": "recykfXMQCq0E", "fields": {"数字类型01":40}},
        ], 
        "fieldKey": "name"
      });
      let record_update:RecordUpdateRO = serde_json::from_value(test_value).unwrap();
      let result = Datasheet::update_records_origin(dst_id.to_string(), view_id.to_string(), user_id.to_string(), record_update, context.clone(), data_storage_provider.clone()).await.unwrap();
      assert_eq!(expected, result); 

      let test_value = json!({
        "records": [
            {"recordId": "recykfXMQCq0E", "fields": {"数字类型01":30}},
        ], 
        "fieldKey": "name"
      });
      let record_update:RecordUpdateRO = serde_json::from_value(test_value).unwrap();
      let result = Datasheet::update_records_origin(dst_id.to_string(), view_id.to_string(), user_id.to_string(), record_update, context, data_storage_provider).await.unwrap();

      let actions_value = json!([
        {
          "n":"OR",
          "p":["recordMap","recykfXMQCq0E","data","fldMtxjFIJdbm"],
          "od":40,
          "oi":30
        },
      ]);

      let actions: Vec<ActionOTO> = serde_json::from_value(actions_value).unwrap();
      let mut field_type_map = HashMapExt::default();
      field_type_map.insert("fldMtxjFIJdbm".to_string(), FieldKindSO::Number);
      let operation = Operation{
        cmd: cmd_name.to_string(),
        actions: actions,
        field_type_map: Some(field_type_map),
        ..Default::default()
      };

      let mut operations = Vec::new();
      operations.push(operation.clone());
      let mut resource_ops_collects = Vec::new();
      let resource_ops_collect = ResourceOpsCollect {
        resource_id: resource_id.to_string(),
        resource_type: resource_type.clone(),
        operations,
        ..Default::default()
      };
      let mut operations = Vec::new();
      operations.push(operation.clone());
      resource_ops_collects.push(resource_ops_collect);
      let mut save_results = Vec::new();
      let save_result = SaveResult {
        base_revision: 12,
        message_id: message_id.to_string(),
        resource_id: resource_id.to_string(),
        resource_type: resource_type.clone(),
        operations,
      };
      save_results.push(save_result);
      let expected = CollaCommandExecuteResult {
        result: ExecuteResult::Success,
        resource_id: resource_id.to_string(),
        resource_type: resource_type.clone(),
        operation: Some(operation),
        linked_actions: Some(Vec::new()),
        resource_ops_collects: Some(resource_ops_collects),
        save_result: Some(save_results),
        execute_type: Some(ExecuteType::Execute),
        ..Default::default()
      };
      assert_eq!(expected, result); 
  }

  #[tokio::test]
  async fn test_update_records_currency(){
    let dst_id: String = "dstiaeZJYXq9G2Rt62".to_string();
    let view_id: String = "viwUFDJoVvY1Q".to_string();
    let user_id: String = "".to_string();
    let expected = CollaCommandExecuteResult {
      resource_id: dst_id.to_string(),
      resource_type: ResourceType::Datasheet,
      result: ExecuteResult::None,
      ..Default::default()
    };
    let cmd_name = "SetRecords";
    let resource_id = dst_id.to_string();
    let resource_type = ResourceType::Datasheet;
    let message_id = "jbXlnbWx7ojndVP9ATDb";
    let context = get_context();
    let data_storage_provider = Box::new(MockDataStorageProvider{});

    //货币
    let test_value = json!({
      "records": [
          {"recordId": "recykfXMQCq0E", "fields": {"货币类型":3}},
      ], 
      "fieldKey": "name"
    });
    let record_update:RecordUpdateRO = serde_json::from_value(test_value).unwrap();
    let result = Datasheet::update_records_origin(dst_id.to_string(), view_id.to_string(), user_id.to_string(), record_update, context.clone(), data_storage_provider.clone()).await.unwrap();
    assert_eq!(expected, result); 

    let test_value = json!({
      "records": [
          {"recordId": "recykfXMQCq0E", "fields": {"货币类型":4}},
      ], 
      "fieldKey": "name"
    });
    let record_update:RecordUpdateRO = serde_json::from_value(test_value).unwrap();
    let result = Datasheet::update_records_origin(dst_id.to_string(), view_id.to_string(), user_id.to_string(), record_update, context, data_storage_provider).await.unwrap();

    let actions_value = json!([
      {
        "n":"OR",
        "p":["recordMap","recykfXMQCq0E","data","fldCBnoSSX0Qk"],
        "od":3,
        "oi":4
      },
    ]);

    let actions: Vec<ActionOTO> = serde_json::from_value(actions_value).unwrap();
    let mut field_type_map = HashMapExt::default();
    field_type_map.insert("fldCBnoSSX0Qk".to_string(), FieldKindSO::Currency);
    let operation = Operation{
      cmd: cmd_name.to_string(),
      actions: actions,
      field_type_map: Some(field_type_map),
      ..Default::default()
    };

    let mut operations = Vec::new();
    operations.push(operation.clone());
    let mut resource_ops_collects = Vec::new();
    let resource_ops_collect = ResourceOpsCollect {
      resource_id: resource_id.to_string(),
      resource_type: resource_type.clone(),
      operations,
      ..Default::default()
    };
    let mut operations = Vec::new();
    operations.push(operation.clone());
    resource_ops_collects.push(resource_ops_collect);
    let mut save_results = Vec::new();
    let save_result = SaveResult {
      base_revision: 12,
      message_id: message_id.to_string(),
      resource_id: resource_id.to_string(),
      resource_type: resource_type.clone(),
      operations,
    };
    save_results.push(save_result);
    let expected = CollaCommandExecuteResult {
      result: ExecuteResult::Success,
      resource_id: resource_id.to_string(),
      resource_type: resource_type.clone(),
      operation: Some(operation),
      linked_actions: Some(Vec::new()),
      resource_ops_collects: Some(resource_ops_collects),
      save_result: Some(save_results),
      execute_type: Some(ExecuteType::Execute),
      ..Default::default()
    };
    assert_eq!(expected, result); 
  }

  #[tokio::test]
  async fn test_update_records_percent(){
    let dst_id: String = "dstiaeZJYXq9G2Rt62".to_string();
    let view_id: String = "viwUFDJoVvY1Q".to_string();
    let user_id: String = "".to_string();
    let expected = CollaCommandExecuteResult {
      resource_id: dst_id.to_string(),
      resource_type: ResourceType::Datasheet,
      result: ExecuteResult::None,
      ..Default::default()
    };
    let cmd_name = "SetRecords";
    let resource_id = dst_id.to_string();
    let resource_type = ResourceType::Datasheet;
    let message_id = "jbXlnbWx7ojndVP9ATDb";
    let context = get_context();
    let data_storage_provider = Box::new(MockDataStorageProvider{});

    //百分比
    let test_value = json!({
      "records": [
          {"recordId": "recykfXMQCq0E", "fields": {"百分比类型":0.15}},
      ], 
      "fieldKey": "name"
    });
    let record_update:RecordUpdateRO = serde_json::from_value(test_value).unwrap();
    let result = Datasheet::update_records_origin(dst_id.to_string(), view_id.to_string(), user_id.to_string(), record_update, context.clone(), data_storage_provider.clone()).await.unwrap();
    assert_eq!(expected, result); 

    let test_value = json!({
      "records": [
          {"recordId": "recykfXMQCq0E", "fields": {"百分比类型":0.14}},
      ], 
      "fieldKey": "name"
    });
    let record_update:RecordUpdateRO = serde_json::from_value(test_value).unwrap();
    let result = Datasheet::update_records_origin(dst_id.to_string(), view_id.to_string(), user_id.to_string(), record_update, context, data_storage_provider).await.unwrap();

    let actions_value = json!([
      {
        "n":"OR",
        "p":["recordMap","recykfXMQCq0E","data","fldkCruCJZbq5"],
        "od":0.15,
        "oi":0.14
      },
    ]);

    let actions: Vec<ActionOTO> = serde_json::from_value(actions_value).unwrap();
    let mut field_type_map = HashMapExt::default();
    field_type_map.insert("fldkCruCJZbq5".to_string(), FieldKindSO::Percent);
    let operation = Operation{
      cmd: cmd_name.to_string(),
      actions: actions,
      field_type_map: Some(field_type_map),
      ..Default::default()
    };

    let mut operations = Vec::new();
    operations.push(operation.clone());
    let mut resource_ops_collects = Vec::new();
    let resource_ops_collect = ResourceOpsCollect {
      resource_id: resource_id.to_string(),
      resource_type: resource_type.clone(),
      operations,
      ..Default::default()
    };
    let mut operations = Vec::new();
    operations.push(operation.clone());
    resource_ops_collects.push(resource_ops_collect);
    let mut save_results = Vec::new();
    let save_result = SaveResult {
      base_revision: 12,
      message_id: message_id.to_string(),
      resource_id: resource_id.to_string(),
      resource_type: resource_type.clone(),
      operations,
    };
    save_results.push(save_result);
    let expected = CollaCommandExecuteResult {
      result: ExecuteResult::Success,
      resource_id: resource_id.to_string(),
      resource_type: resource_type.clone(),
      operation: Some(operation),
      linked_actions: Some(Vec::new()),
      resource_ops_collects: Some(resource_ops_collects),
      save_result: Some(save_results),
      execute_type: Some(ExecuteType::Execute),
      ..Default::default()
    };
    assert_eq!(expected, result); 
  }

  //比实际少3个0
  #[tokio::test]
  async fn test_update_records_date_time(){
    let dst_id: String = "dstiaeZJYXq9G2Rt62".to_string();
    let view_id: String = "viwUFDJoVvY1Q".to_string();
    let user_id: String = "".to_string();
    let expected = CollaCommandExecuteResult {
      resource_id: dst_id.to_string(),
      resource_type: ResourceType::Datasheet,
      result: ExecuteResult::None,
      ..Default::default()
    };
    let cmd_name = "SetRecords";
    let resource_id = dst_id.to_string();
    let resource_type = ResourceType::Datasheet;
    let message_id = "jbXlnbWx7ojndVP9ATDb";
    let context = get_context();
    let data_storage_provider = Box::new(MockDataStorageProvider{});

    //日期1644895020000超过了i32=2022-02-15 11:17:00 少3个0=1644895020=1970/01/20
    //1664895020000=2022-10-04 22:50:20
    let test_value = json!({
      "records": [
          {"recordId": "recykfXMQCq0E", "fields": {"日期类型":1694534400000i64}},
      ], 
      "fieldKey": "name"
    });
    let record_update:RecordUpdateRO = serde_json::from_value(test_value).unwrap();
    let result = Datasheet::update_records_origin(dst_id.to_string(), view_id.to_string(), user_id.to_string(), record_update, context.clone(), data_storage_provider.clone()).await.unwrap();
    assert_eq!(expected, result); 

    let test_value = json!({
      "records": [
          {"recordId": "recykfXMQCq0E", "fields": {"日期类型":1664895020000i64}},
      ], 
      "fieldKey": "name"
    });
    let record_update:RecordUpdateRO = serde_json::from_value(test_value).unwrap();
    let result = Datasheet::update_records_origin(dst_id.to_string(), view_id.to_string(), user_id.to_string(), record_update, context, data_storage_provider).await.unwrap();

    let actions_value = json!([
      {
        "n":"OR",
        "p":["recordMap","recykfXMQCq0E","data","fldrxkZjcba2P"],
        "od":1694534400000i64,
        "oi":1664895020000i64
      },
    ]);

    let actions: Vec<ActionOTO> = serde_json::from_value(actions_value).unwrap();
    let mut field_type_map = HashMapExt::default();
    field_type_map.insert("fldrxkZjcba2P".to_string(), FieldKindSO::DateTime);
    let operation = Operation{
      cmd: cmd_name.to_string(),
      actions: actions,
      field_type_map: Some(field_type_map),
      ..Default::default()
    };

    let mut operations = Vec::new();
    operations.push(operation.clone());
    let mut resource_ops_collects = Vec::new();
    let resource_ops_collect = ResourceOpsCollect {
      resource_id: resource_id.to_string(),
      resource_type: resource_type.clone(),
      operations,
      ..Default::default()
    };
    let mut operations = Vec::new();
    operations.push(operation.clone());
    resource_ops_collects.push(resource_ops_collect);
    let mut save_results = Vec::new();
    let save_result = SaveResult {
      base_revision: 12,
      message_id: message_id.to_string(),
      resource_id: resource_id.to_string(),
      resource_type: resource_type.clone(),
      operations,
    };
    save_results.push(save_result);
    let expected = CollaCommandExecuteResult {
      result: ExecuteResult::Success,
      resource_id: resource_id.to_string(),
      resource_type: resource_type.clone(),
      operation: Some(operation),
      linked_actions: Some(Vec::new()),
      resource_ops_collects: Some(resource_ops_collects),
      save_result: Some(save_results),
      execute_type: Some(ExecuteType::Execute),
      ..Default::default()
    };
    assert_eq!(expected, result); 
    // one_by_one_test(expected, result)
  }

  //依赖另一个表里的附件,重复提交不会失败。。需要loader，mock没有
  // #[tokio::test]
  // async fn test_update_records_attachment(){
  //   let dst_id: String = "dstiaeZJYXq9G2Rt62".to_string();
  //   let view_id: String = "viwUFDJoVvY1Q".to_string();
  //   let user_id: String = "".to_string();
  //   let cmd_name = "SetRecords";
  //   let resource_id = dst_id.to_string();
  //   let resource_type = ResourceType::Datasheet;
  //   let message_id = "jbXlnbWx7ojndVP9ATDb";
  //   let context = get_context();
  //   let data_storage_provider = Box::new(MockDataStorageProvider{});

  //   //附件 未知附件报错500 server error
  //   // let test_value = json!({
  //   //   "records": [
  //   //       {"recordId": "recykfXMQCq0E", "fields": {
  //   //         "附件类型":[{
  //   //           "id": "atcXZLLhtOuWl",
  //   //           "name": "208.gif",
  //   //           "size": 969376,
  //   //           "mimeType": "image/gif",
  //   //           "token": "space/2022/02/15/f4c2c8ecac7f480eae5635a65ec25334",
  //   //           "width": 2282,
  //   //           "height": 1304,
  //   //           "url": "https://s1.vika.cn/space/2022/02/15/f4c2c8ecac7f480eae5635a65ec25334"
  //   //         }]
  //   //       }},
  //   //   ], 
  //   //   "fieldKey": "name"
  //   // });
  //   //用另一张表的附件
  //   // "fldHRitun7smQ": [
  //   //                       {
  //   //                           "id": "atcr11FYH5FdR",
  //   //                           "mimeType": "image/gif",
  //   //                           "name": "208.gif",
  //   //                           "size": 757314,
  //   //                           "token": "space/2023/09/13/d91bffa32dbd4463973846b4c27cb1f4",
  //   //                           "bucket": "QNY1",
  //   //                           "width": 2282,
  //   //                           "height": 1304
  //   //                       },
  //   //                       {
  //   //                           "id": "atcM58yzTqgkv",
  //   //                           "mimeType": "image/png",
  //   //                           "name": "207.png",
  //   //                           "size": 237738,
  //   //                           "token": "space/2023/09/13/da349d0487af429f9a46d933ca71e5ee",
  //   //                           "bucket": "QNY1",
  //   //                           "width": 1660,
  //   //                           "height": 1006
  //   //                       }
  //   //                   ]
  //   let test_value = json!({
  //     "records": [
  //         {"recordId": "recykfXMQCq0E", "fields": {
  //           "附件类型":[{
  //             "id": "atcGHR3njgTaq",
  //             "mimeType": "image/gif",
  //             "name": "208.gif",
  //             "size": 757314,
  //             "token": "space/2023/09/13/d91bffa32dbd4463973846b4c27cb1f4",
  //             "bucket": "QNY1",
  //             "width": 2282,
  //             "height": 1304
  //           }]
  //         }},
  //     ], 
  //     "fieldKey": "name"
  //   });
  //   let record_update:RecordUpdateRO = serde_json::from_value(test_value).unwrap();
  //   let result = Datasheet::update_records_origin(dst_id.to_string(), view_id.to_string(), user_id.to_string(), record_update, context, data_storage_provider).await.unwrap();

  //   let actions_value = json!([
  //     {
  //       "n":"OR",
  //       "p":["recordMap","recykfXMQCq0E","data","fldigwAJUXuvE"],
  //       "od":[{
  //         "id":"atcywoE8o3e52",
  //         "name":"207.png",
  //         "size":237738,
  //         "token":"space/2023/09/13/da349d0487af429f9a46d933ca71e5ee",
  //         "width":1660,
  //         "bucket":"QNY1",
  //         "height":1006,
  //         "mimeType":"image/png"
  //       }],
  //       "oi":[{
  //         "id":"atcGHR3njgTaq",
  //         "name":"208.gif",
  //         "size":757314,
  //         "token":"space/2023/09/13/d91bffa32dbd4463973846b4c27cb1f4",
  //         "width":2282,
  //         "height":1304,
  //         "bucket":"QNY1",
  //         "mimeType":"image/gif"
  //       }]
  //     },
  //   ]);

  //   let actions: Vec<ActionOTO> = serde_json::from_value(actions_value).unwrap();
  //   let mut field_type_map = HashMapExt::default();
  //   field_type_map.insert("fldigwAJUXuvE".to_string(), FieldKindSO::Attachment);
  //   let operation = Operation{
  //     cmd: cmd_name.to_string(),
  //     actions: actions,
  //     field_type_map: Some(field_type_map),
  //     ..Default::default()
  //   };

  //   let mut operations = Vec::new();
  //   operations.push(operation.clone());
  //   let mut resource_ops_collects = Vec::new();
  //   let resource_ops_collect = ResourceOpsCollect {
  //     resource_id: resource_id.to_string(),
  //     resource_type: resource_type.clone(),
  //     operations,
  //     ..Default::default()
  //   };
  //   let mut operations = Vec::new();
  //   operations.push(operation.clone());
  //   resource_ops_collects.push(resource_ops_collect);
  //   let mut save_results = Vec::new();
  //   let save_result = SaveResult {
  //     base_revision: 12,
  //     message_id: message_id.to_string(),
  //     resource_id: resource_id.to_string(),
  //     resource_type: resource_type.clone(),
  //     operations,
  //   };
  //   save_results.push(save_result);
  //   let expected = CollaCommandExecuteResult {
  //     result: ExecuteResult::Success,
  //     resource_id: resource_id.to_string(),
  //     resource_type: resource_type.clone(),
  //     operation: Some(operation),
  //     linked_actions: Some(Vec::new()),
  //     resource_ops_collects: Some(resource_ops_collects),
  //     save_result: Some(save_results),
  //     execute_type: Some(ExecuteType::Execute),
  //     ..Default::default()
  //   };
  //   assert_eq!(expected, result); 
  // }

  #[tokio::test]
  async fn test_update_records_url(){
    let dst_id: String = "dstiaeZJYXq9G2Rt62".to_string();
    let view_id: String = "viwUFDJoVvY1Q".to_string();
    let user_id: String = "".to_string();
    let expected = CollaCommandExecuteResult {
      resource_id: dst_id.to_string(),
      resource_type: ResourceType::Datasheet,
      result: ExecuteResult::None,
      ..Default::default()
    };
    let cmd_name = "SetRecords";
    let resource_id = dst_id.to_string();
    let resource_type = ResourceType::Datasheet;
    let message_id = "jbXlnbWx7ojndVP9ATDb";
    let context = get_context();
    let data_storage_provider = Box::new(MockDataStorageProvider{});

    //url类型
    let test_value = json!({
      "records": [
          {"recordId": "recykfXMQCq0E", "fields": {"url类型":"www.google.com"}},
      ], 
      "fieldKey": "name"
    });
    let record_update:RecordUpdateRO = serde_json::from_value(test_value).unwrap();
    let result = Datasheet::update_records_origin(dst_id.to_string(), view_id.to_string(), user_id.to_string(), record_update, context.clone(), data_storage_provider.clone()).await.unwrap();
    assert_eq!(expected, result); 

    let test_value = json!({
      "records": [
          {"recordId": "recykfXMQCq0E", "fields": {"url类型":"www.baidu.com"}},
      ], 
      "fieldKey": "name"
    });
    let record_update:RecordUpdateRO = serde_json::from_value(test_value).unwrap();
    let result = Datasheet::update_records_origin(dst_id.to_string(), view_id.to_string(), user_id.to_string(), record_update, context, data_storage_provider).await.unwrap();

    let actions_value = json!([
      {
        "n":"OR",
        "p":["recordMap","recykfXMQCq0E","data","fldO3TerxFBoT"],
        "od":[{"text":"www.google.com","type":2,"title":"www.google.com"}],
        "oi":[{"type":2,"text":"www.baidu.com","title":"www.baidu.com"}]
      },
    ]);

    let actions: Vec<ActionOTO> = serde_json::from_value(actions_value).unwrap();
    let mut field_type_map = HashMapExt::default();
    field_type_map.insert("fldO3TerxFBoT".to_string(), FieldKindSO::URL);
    let operation = Operation{
      cmd: cmd_name.to_string(),
      actions: actions,
      field_type_map: Some(field_type_map),
      ..Default::default()
    };

    let mut operations = Vec::new();
    operations.push(operation.clone());
    let mut resource_ops_collects = Vec::new();
    let resource_ops_collect = ResourceOpsCollect {
      resource_id: resource_id.to_string(),
      resource_type: resource_type.clone(),
      operations,
      ..Default::default()
    };
    let mut operations = Vec::new();
    operations.push(operation.clone());
    resource_ops_collects.push(resource_ops_collect);
    let mut save_results = Vec::new();
    let save_result = SaveResult {
      base_revision: 12,
      message_id: message_id.to_string(),
      resource_id: resource_id.to_string(),
      resource_type: resource_type.clone(),
      operations,
    };
    save_results.push(save_result);
    let expected = CollaCommandExecuteResult {
      result: ExecuteResult::Success,
      resource_id: resource_id.to_string(),
      resource_type: resource_type.clone(),
      operation: Some(operation),
      linked_actions: Some(Vec::new()),
      resource_ops_collects: Some(resource_ops_collects),
      save_result: Some(save_results),
      execute_type: Some(ExecuteType::Execute),
      ..Default::default()
    };
    assert_eq!(expected, result); 
  }
}
