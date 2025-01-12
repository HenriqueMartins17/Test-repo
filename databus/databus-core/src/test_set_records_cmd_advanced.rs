#[cfg(test)]
mod tests {
  use std::rc::Rc;
  use crate::ot::commands::{CollaCommandExecuteResult, ExecuteResult, SaveResult, ExecuteType};
  use crate::mock::{get_snapshot_pack, get_datasheet_pack, MockDataStorageProvider};
  use crate::ot::changeset::{Operation, ResourceOpsCollect};
  use crate::ot::types::{ActionOTO, ResourceType};
  use crate::ro::record_update_ro::RecordUpdateRO;
  use crate::so::{FieldKindSO, DatasheetPackContext, prepare_context_data};
  use crate::Datasheet;
  use databus_shared::prelude::HashMapExt;
  use serde_json::json;

  fn get_context() -> Rc<DatasheetPackContext>{
    let datasheet_pack = get_snapshot_pack(3).unwrap();
    let snapshot = datasheet_pack.snapshot;
    let mut datasheet_pack = get_datasheet_pack().unwrap();
    datasheet_pack.snapshot = snapshot.clone();
    let context = prepare_context_data(Box::new(datasheet_pack));
    let context = Rc::new(context);
    context
  }
  #[tokio::test]
  async fn test_update_records_look_up(){
    let dst_id: String = "dstRUJJUjbiX0YE8MZ".to_string();
    let view_id: String = "viwsRqraWn04W".to_string();
    let user_id: String = "".to_string();
    let expected = "Lookup field can't be edited".to_string();
    let context = get_context();
    let data_storage_provider = Box::new(MockDataStorageProvider{});

    let test_value = json!({
      "records": [
          {"recordId": "recsoU03ufryC", "fields": {"引用班级表数据":1}},
      ], 
      "fieldKey": "name"
    });
    let record_update:RecordUpdateRO = serde_json::from_value(test_value).unwrap();
    let result = Datasheet::update_records_origin(dst_id.to_string(), view_id.to_string(), user_id.to_string(), record_update, context, data_storage_provider).await;
    match result {
      Ok(_) => println!("ok"),
      Err(e) => assert_eq!(expected, e.to_string()),
    }
  }

  #[tokio::test]
  async fn test_update_records_formula(){
    let dst_id: String = "dstRUJJUjbiX0YE8MZ".to_string();
    let view_id: String = "viwsRqraWn04W".to_string();
    let user_id: String = "".to_string();
    let expected = "Formula field can't be edited".to_string();
    let context = get_context();
    let data_storage_provider = Box::new(MockDataStorageProvider{});

    let test_value = json!({
      "records": [
          {"recordId": "recsoU03ufryC", "fields": {"智能公式":1}},
      ], 
      "fieldKey": "name"
    });
    let record_update:RecordUpdateRO = serde_json::from_value(test_value).unwrap();
    let result = Datasheet::update_records_origin(dst_id.to_string(), view_id.to_string(), user_id.to_string(), record_update, context, data_storage_provider).await;
    match result {
      Ok(_) => println!("ok"),
      Err(e) => assert_eq!(expected, e.to_string()),
    }
  }

  #[tokio::test]
  async fn test_update_records_auto_number(){
    let dst_id: String = "dstRUJJUjbiX0YE8MZ".to_string();
    let view_id: String = "viwsRqraWn04W".to_string();
    let user_id: String = "".to_string();
    let expected = "Autonumber field can't be edited".to_string();
    let context = get_context();
    let data_storage_provider = Box::new(MockDataStorageProvider{});

    let test_value = json!({
      "records": [
          {"recordId": "recsoU03ufryC", "fields": {"自增数字类型":2}},
      ], 
      "fieldKey": "name"
    });
    let record_update:RecordUpdateRO = serde_json::from_value(test_value).unwrap();
    let result = Datasheet::update_records_origin(dst_id.to_string(), view_id.to_string(), user_id.to_string(), record_update, context, data_storage_provider).await;
    match result {
      Ok(_) => println!("ok"),
      Err(e) => assert_eq!(expected, e.to_string()),
    }
  }

  #[tokio::test]
  async fn test_update_records_created_time(){
    let dst_id: String = "dstRUJJUjbiX0YE8MZ".to_string();
    let view_id: String = "viwsRqraWn04W".to_string();
    let user_id: String = "".to_string();
    let expected = "Created Time field can't be edited".to_string();
    let context = get_context();
    let data_storage_provider = Box::new(MockDataStorageProvider{});

    let test_value = json!({
      "records": [
          {"recordId": "recsoU03ufryC", "fields": {"创建时间类型":"创建时间测试"}},
      ], 
      "fieldKey": "name"
    });
    let record_update:RecordUpdateRO = serde_json::from_value(test_value).unwrap();
    let result = Datasheet::update_records_origin(dst_id.to_string(), view_id.to_string(), user_id.to_string(), record_update, context, data_storage_provider).await;
    match result {
      Ok(_) => println!("ok"),
      Err(e) => assert_eq!(expected, e.to_string()),
    }
  }

  #[tokio::test]
  async fn test_update_records_last_edited_time(){
    let dst_id: String = "dstRUJJUjbiX0YE8MZ".to_string();
    let view_id: String = "viwsRqraWn04W".to_string();
    let user_id: String = "".to_string();
    let expected = "Last Edited Time field can't be edited".to_string();
    let context = get_context();
    let data_storage_provider = Box::new(MockDataStorageProvider{});

    let test_value = json!({
      "records": [
          {"recordId": "recsoU03ufryC", "fields": {"更新时间类型":"更新时间测试"}},
      ], 
      "fieldKey": "name"
    });
    let record_update:RecordUpdateRO = serde_json::from_value(test_value).unwrap();
    let result = Datasheet::update_records_origin(dst_id.to_string(), view_id.to_string(), user_id.to_string(), record_update, context, data_storage_provider).await;
    match result {
      Ok(_) => println!("ok"),
      Err(e) => assert_eq!(expected, e.to_string()),
    }
  }
  
  #[tokio::test]
  async fn test_update_records_created_by(){
    let dst_id: String = "dstRUJJUjbiX0YE8MZ".to_string();
    let view_id: String = "viwsRqraWn04W".to_string();
    let user_id: String = "".to_string();
    let expected = "Created By field can't be edited".to_string();
    let context = get_context();
    let data_storage_provider = Box::new(MockDataStorageProvider{});

    let test_value = json!({
      "records": [
          {"recordId": "recsoU03ufryC", "fields": {"创建人类型":"创建人测试"}},
      ], 
      "fieldKey": "name"
    });
    let record_update:RecordUpdateRO = serde_json::from_value(test_value).unwrap();
    let result = Datasheet::update_records_origin(dst_id.to_string(), view_id.to_string(), user_id.to_string(), record_update, context, data_storage_provider).await;
    match result {
      Ok(_) => println!("ok"),
      Err(e) => assert_eq!(expected, e.to_string()),
    }
  }

  #[tokio::test]
  async fn test_update_records_last_edited_by(){
      let dst_id: String = "dstRUJJUjbiX0YE8MZ".to_string();
      let view_id: String = "viwsRqraWn04W".to_string();
      let user_id: String = "".to_string();
      let expected = "Last Edited By field can't be edited".to_string();
      let context = get_context();
      let data_storage_provider = Box::new(MockDataStorageProvider{});

      let test_value = json!({
        "records": [
            {"recordId": "recsoU03ufryC", "fields": {"修改人类型":"修改人测试"}},
        ], 
        "fieldKey": "name"
      });
      let record_update:RecordUpdateRO = serde_json::from_value(test_value).unwrap();
      let result = Datasheet::update_records_origin(dst_id.to_string(), view_id.to_string(), user_id.to_string(), record_update, context, data_storage_provider).await;
      match result {
        Ok(_) => println!("ok"),
        Err(e) => assert_eq!(expected, e.to_string()),
      }
  }

  #[tokio::test]
  async fn test_update_records_cascader(){
      let dst_id: String = "dstRUJJUjbiX0YE8MZ".to_string();
      let view_id: String = "viwsRqraWn04W".to_string();
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

      let test_value = json!({
        "records": [
            {"recordId": "recsoU03ufryC", "fields": {"what":"100/202"}},
        ], 
        "fieldKey": "name"
      });
      let record_update:RecordUpdateRO = serde_json::from_value(test_value).unwrap();
      let result = Datasheet::update_records_origin(dst_id.to_string(), view_id.to_string(), user_id.to_string(), record_update, context.clone(), data_storage_provider.clone()).await.unwrap();
      assert_eq!(expected, result); 

      let test_value = json!({
        "records": [
            {"recordId": "recsoU03ufryC", "fields": {"what":"22/101"}},
        ], 
        "fieldKey": "name"
      });
      let record_update:RecordUpdateRO = serde_json::from_value(test_value).unwrap();
      let result = Datasheet::update_records_origin(dst_id.to_string(), view_id.to_string(), user_id.to_string(), record_update, context, data_storage_provider).await.unwrap();

      let actions_value = json!([
        {
          "n":"OR",
          "p":["recordMap","recsoU03ufryC","data","fld5q3e4zu3e4"],
          "od":[{"text":"100/202","type":1}],
          "oi":[{"text":"22/101","type":1}],
        },
      ]);

      let actions: Vec<ActionOTO> = serde_json::from_value(actions_value).unwrap();
      let mut field_type_map = HashMapExt::default();
      field_type_map.insert("fld5q3e4zu3e4".to_string(), FieldKindSO::Cascader);
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
      // one_by_one_test(expected, result);
  }

  #[tokio::test]
  async fn test_update_records_one_way_link(){
      //要另外找数据跑测试
      let dst_id: String = "dstRUJJUjbiX0YE8MZ".to_string();
      let view_id: String = "viwsRqraWn04W".to_string();
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

      //神奇关联recwPms4kZBFk另一张表的rec，另一个recgPXtYBxUqh
      let test_value = json!({
        "records": [
            {"recordId": "recsoU03ufryC", "fields": {"单项关联类型":["recKRKW9ZbMiL"]}},
        ], 
        "fieldKey": "name"
      });
      let record_update:RecordUpdateRO = serde_json::from_value(test_value).unwrap();
      let result = Datasheet::update_records_origin(dst_id.to_string(), view_id.to_string(), user_id.to_string(), record_update, context.clone(), data_storage_provider.clone()).await.unwrap();
      assert_eq!(expected, result);

      let test_value = json!({
        "records": [
            {"recordId": "recsoU03ufryC", "fields": {"单项关联类型":["recjrP9nhaww9"]}},
        ], 
        "fieldKey": "name"
      });
      let record_update:RecordUpdateRO = serde_json::from_value(test_value).unwrap();
      let result = Datasheet::update_records_origin(dst_id.to_string(), view_id.to_string(), user_id.to_string(), record_update, context, data_storage_provider).await.unwrap();
      let actions_value = json!([
        {
          "n":"OR",
          "p":["recordMap","recsoU03ufryC","data","fldaZiYMYLZuD"],
          "od":["recKRKW9ZbMiL"],
          "oi":["recjrP9nhaww9"]
        },
      ]);
      let actions: Vec<ActionOTO> = serde_json::from_value(actions_value).unwrap();
      //Link 7
      let mut field_type_map = HashMapExt::default();
      field_type_map.insert("fldaZiYMYLZuD".to_string(), FieldKindSO::OneWayLink);
      let operation = Operation{
        cmd: cmd_name.to_string(),
        actions: actions,
        field_type_map: Some(field_type_map),
        ..Default::default()
      };

      let mut resource_ops_collects = Vec::new();

      let mut operations = Vec::new();
      let operation_collect = operation.clone();
      operations.push(operation_collect);
      let resource_ops_collect = ResourceOpsCollect {
        resource_id: resource_id.to_string(),
        resource_type: resource_type.clone(),
        operations,
        ..Default::default()
      };
      resource_ops_collects.push(resource_ops_collect);

      let mut save_results = Vec::new();

      let mut operations = Vec::new();
      let operation_collect = operation.clone();
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
      operation_collect.field_type_map = None;
      operations.push(operation_collect.clone());
      let _message_id = "jbXlnbWx7ojndVP9ATDb";

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
      // one_by_one_test(expected, result);
  }
}
