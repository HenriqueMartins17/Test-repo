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
    let datasheet_pack = get_snapshot_pack(2).unwrap();
    let snapshot = datasheet_pack.snapshot;
    let mut datasheet_pack = get_datasheet_pack().unwrap();
    datasheet_pack.snapshot = snapshot.clone();
    let context = prepare_context_data(Box::new(datasheet_pack));
    let context = Rc::new(context);
    context
  }

  #[tokio::test]
  async fn test_update_records_long_text(){
      let dst_id: String = "dst9TgbZAZXzp8ESoD".to_string();
      let view_id: String = "viwkKhepmhhTM".to_string();
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
            {"recordId": "recVXri0Gw8xh", "fields": {"多行文本列":"系统测试：\n1.UAT测试\n2.AT测试"}},
        ], 
        "fieldKey": "name"
      });
      let record_update:RecordUpdateRO = serde_json::from_value(test_value).unwrap();
      let result = Datasheet::update_records_origin(dst_id.to_string(), view_id.to_string(), user_id.to_string(), record_update, context.clone(), data_storage_provider.clone()).await.unwrap();
      assert_eq!(expected, result); 

      //single line Text不同数据修改成功测试
      let test_value = json!({
        "records": [
            {"recordId": "recVXri0Gw8xh", "fields": {"多行文本列":"系统测试：\n1.UAT测试\n2.AT测试\n3.dh测试"}},
        ], 
        "fieldKey": "name"
      });
      let record_update:RecordUpdateRO = serde_json::from_value(test_value).unwrap();
      let result = Datasheet::update_records_origin(dst_id.to_string(), view_id.to_string(), user_id.to_string(), record_update, context, data_storage_provider).await.unwrap();

      let actions_value = json!([
        {
          "n":"OR",
          "p":["recordMap","recVXri0Gw8xh","data","fldz5wgqkk5Du"],
          "od":[{"text":"系统测试：\n1.UAT测试\n2.AT测试","type":1}],
          "oi":[{"text":"系统测试：\n1.UAT测试\n2.AT测试\n3.dh测试","type":1}]
        },
      ]);

      let actions: Vec<ActionOTO> = serde_json::from_value(actions_value).unwrap();
      let mut field_type_map = HashMapExt::default();
      field_type_map.insert("fldz5wgqkk5Du".to_string(), FieldKindSO::Text);
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
  async fn test_update_records_check_box(){
      let dst_id: String = "dst9TgbZAZXzp8ESoD".to_string();
      let view_id: String = "viwkKhepmhhTM".to_string();
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
            {"recordId": "recVXri0Gw8xh", "fields": {"复选框类型":true}},
        ], 
        "fieldKey": "name"
      });
      let record_update:RecordUpdateRO = serde_json::from_value(test_value).unwrap();
      let result = Datasheet::update_records_origin(dst_id.to_string(), view_id.to_string(), user_id.to_string(), record_update, context.clone(), data_storage_provider.clone()).await.unwrap();
      assert_eq!(expected, result); 

      //single line Text不同数据修改成功测试
      let test_value = json!({
        "records": [
            {"recordId": "recVXri0Gw8xh", "fields": {"复选框类型":false}},
        ], 
        "fieldKey": "name"
      });
      let record_update:RecordUpdateRO = serde_json::from_value(test_value).unwrap();
      let result = Datasheet::update_records_origin(dst_id.to_string(), view_id.to_string(), user_id.to_string(), record_update, context, data_storage_provider).await.unwrap();

      let actions_value = json!([
        {
          "n":"OD",
          "p":["recordMap","recVXri0Gw8xh","data","fldby3h7iXMkB"],
          "od":true
        },
      ]);

      let actions: Vec<ActionOTO> = serde_json::from_value(actions_value).unwrap();
      let mut field_type_map = HashMapExt::default();
      field_type_map.insert("fldby3h7iXMkB".to_string(), FieldKindSO::Checkbox);
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
  async fn test_update_records_rating(){
      let dst_id: String = "dst9TgbZAZXzp8ESoD".to_string();
      let view_id: String = "viwkKhepmhhTM".to_string();
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
            {"recordId": "recVXri0Gw8xh", "fields": {"分数类型":6}},
        ], 
        "fieldKey": "name"
      });
      let record_update:RecordUpdateRO = serde_json::from_value(test_value).unwrap();
      let result = Datasheet::update_records_origin(dst_id.to_string(), view_id.to_string(), user_id.to_string(), record_update, context.clone(), data_storage_provider.clone()).await.unwrap();
      assert_eq!(expected, result); 

      //single line Text不同数据修改成功测试
      let test_value = json!({
        "records": [
            {"recordId": "recVXri0Gw8xh", "fields": {"分数类型":7}},
        ], 
        "fieldKey": "name"
      });
      let record_update:RecordUpdateRO = serde_json::from_value(test_value).unwrap();
      let result = Datasheet::update_records_origin(dst_id.to_string(), view_id.to_string(), user_id.to_string(), record_update, context, data_storage_provider).await.unwrap();

      let actions_value = json!([
        {
          "n":"OR",
          "p":["recordMap","recVXri0Gw8xh","data","fldSaL0gdR8bF"],
          "od":6,
          "oi":7,
        },
      ]);

      let actions: Vec<ActionOTO> = serde_json::from_value(actions_value).unwrap();
      let mut field_type_map = HashMapExt::default();
      field_type_map.insert("fldSaL0gdR8bF".to_string(), FieldKindSO::Rating);
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
  async fn test_update_records_phone(){
      let dst_id: String = "dst9TgbZAZXzp8ESoD".to_string();
      let view_id: String = "viwkKhepmhhTM".to_string();
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
            {"recordId": "recVXri0Gw8xh", "fields": {"电话类型":"123456789"}},
        ], 
        "fieldKey": "name"
      });
      let record_update:RecordUpdateRO = serde_json::from_value(test_value).unwrap();
      let result = Datasheet::update_records_origin(dst_id.to_string(), view_id.to_string(), user_id.to_string(), record_update, context.clone(), data_storage_provider.clone()).await.unwrap();
      assert_eq!(expected, result); 

      //single line Text不同数据修改成功测试
      let test_value = json!({
        "records": [
            {"recordId": "recVXri0Gw8xh", "fields": {"电话类型":"1234567891"}},
        ], 
        "fieldKey": "name"
      });
      let record_update:RecordUpdateRO = serde_json::from_value(test_value).unwrap();
      let result = Datasheet::update_records_origin(dst_id.to_string(), view_id.to_string(), user_id.to_string(), record_update, context, data_storage_provider).await.unwrap();

      let actions_value = json!([
        {
          "n":"OR",
          "p":["recordMap","recVXri0Gw8xh","data","fldKXUwEujc32"],
          "od":[{"text":"123456789","type":1}],
          "oi":[{"text":"1234567891","type":1}]
        },
      ]);

      let actions: Vec<ActionOTO> = serde_json::from_value(actions_value).unwrap();
      let mut field_type_map = HashMapExt::default();
      field_type_map.insert("fldKXUwEujc32".to_string(), FieldKindSO::Phone);
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
  async fn test_update_records_email(){
      let dst_id: String = "dst9TgbZAZXzp8ESoD".to_string();
      let view_id: String = "viwkKhepmhhTM".to_string();
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
            {"recordId": "recVXri0Gw8xh", "fields": {"邮箱类型":"12345@vikadata.com"}},
        ], 
        "fieldKey": "name"
      });
      let record_update:RecordUpdateRO = serde_json::from_value(test_value).unwrap();
      let result = Datasheet::update_records_origin(dst_id.to_string(), view_id.to_string(), user_id.to_string(), record_update, context.clone(), data_storage_provider.clone()).await.unwrap();
      assert_eq!(expected, result); 

      let test_value = json!({
        "records": [
            {"recordId": "recVXri0Gw8xh", "fields": {"邮箱类型":"123456@vikadata.com"}},
        ], 
        "fieldKey": "name"
      });
      let record_update:RecordUpdateRO = serde_json::from_value(test_value).unwrap();
      let result = Datasheet::update_records_origin(dst_id.to_string(), view_id.to_string(), user_id.to_string(), record_update, context, data_storage_provider).await.unwrap();

      let actions_value = json!([
        {
          "n":"OR",
          "p":["recordMap","recVXri0Gw8xh","data","fldCPGi8aXPnJ"],
          "od":[{"text":"12345@vikadata.com","type":1}],
          "oi":[{"text":"123456@vikadata.com","type":1}],
        },
      ]);

      let actions: Vec<ActionOTO> = serde_json::from_value(actions_value).unwrap();
      let mut field_type_map = HashMapExt::default();
      field_type_map.insert("fldCPGi8aXPnJ".to_string(), FieldKindSO::Email);
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
}
