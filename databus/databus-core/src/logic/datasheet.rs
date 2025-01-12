use std::{collections::HashMap, rc::Rc};
use crate::{ot::{commands::{SetRecordOptions, SetRecordsOptions, CollaCommandExecuteResult, ExecuteResult, ExecuteType, CollaCommandDefExecuteResult, SaveOptions, AddRecordsOptions, AddFieldOptions, AddFieldsOptions, DeleteFieldOptions, DeleteFieldData, SetFieldAttrOptions, AddViewsOptions, AddView, DeleteViewsOptions, DeleteView, DeleteRecordOptions, CommandOptions, ModifyView, ModifyViewsOptions, Resource}, changeset::{Operation, ResourceOpsCollect}, CollaCommandName, types::ResourceType, CollaCommandManager}, ro::record_update_ro::{RecordUpdateRO, FieldKeyEnum}, so::{ViewRowSO, DatasheetPackContext, FieldSO, ViewSO}, shared::AuthHeader};
use databus_shared::prelude::HashMapExt;
use crate::so::DatasheetSnapshotSO;
use super::{ViewLogic, IViewInfo, DataSaver, SaveOpsOptions, IAddRecordsOptions};

type ILinkedRecordMap = HashMap<String, Vec<String>>;

pub struct Datasheet{
  pub id: String,
  command_manager: CollaCommandManager,
  saver: Box<dyn DataSaver>,
  pub context: Rc<DatasheetPackContext>,
}

impl Datasheet {
  pub fn new(
    dst_id: String,
    context: Rc<DatasheetPackContext>,
    saver: Box<dyn DataSaver>,
  ) -> Self {
    let command_manager = CollaCommandManager::new();
    Self { id: dst_id, command_manager, saver, context }
  }

  // pub fn change_context(self: &mut Self, new_snapshot: DatasheetSnapshotSO){
  //   let context = Rc::get_mut(&mut self.context).unwrap();
  //   context.datasheet_pack.snapshot = new_snapshot;
  // }

  pub fn execute_actions(
    cmd: CollaCommandName,
    ret: CollaCommandDefExecuteResult,
    execute_type: ExecuteType,
    _snapshot: &DatasheetSnapshotSO,
  ) -> anyhow::Result<Option<CollaCommandExecuteResult>> {
    let CollaCommandDefExecuteResult {  resource_id, resource_type, actions, data, linked_actions, field_map_snapshot, .. } = ret;
    if actions.is_empty() {
      return Ok(None);
    }
    let _actions = actions;
    let _field_map_snapshot = field_map_snapshot;
    let mut operation = Operation {
      cmd: cmd.to_string(),
      actions: _actions,
      ..Default::default()
    };
    if let Some(field_map_snapshot) = _field_map_snapshot {
      let mut field_type_map = HashMapExt::default();
      for (id, field) in field_map_snapshot.iter() {
        field_type_map.insert(id.clone(), field.kind.clone());
      }
      operation.field_type_map = Some(field_type_map);
    }
    let mut new_operation = operation.clone();
    
    if let Some(_linked_actions) = &linked_actions {
      if _linked_actions.len() > 0 {
        new_operation.main_link_dst_id = Some(resource_id.clone());
      }
    }
    let mut operations = Vec::new();
    operations.push(new_operation.clone());
    let mut datasheet_ops_collects = Vec::new();
    let datasheet_ops_collect = ResourceOpsCollect {
      resource_id: resource_id.to_string(),
      resource_type: resource_type.clone(),
      operations,
      ..Default::default()
    };
    datasheet_ops_collects.push(datasheet_ops_collect.clone());

    if let Some(_linked_actions) = &linked_actions {
      for l_cmd in _linked_actions {
        if l_cmd.actions.is_empty() {
            continue;
        }
        let op = Operation {
            cmd: cmd.to_string(),
            actions: l_cmd.actions.clone(),
            main_link_dst_id: Some(resource_id.clone()),
            ..Default::default()
        };
        let resource_ops_collect = ResourceOpsCollect {
            resource_id: l_cmd.datasheet_id.to_string(),
            resource_type: resource_type.clone(),
            operations: vec![op],
            ..Default::default()
        };
        datasheet_ops_collects.push(resource_ops_collect);
      }
    }

    // After the op is applied, perform some hook operations.
    // this.didExecutedHook(datasheetOpsCollects);

    Ok(
      Some(CollaCommandExecuteResult{
        result: ExecuteResult::Success,
        resource_id: resource_id.to_string(),
        resource_type: resource_type.clone(),
        data: data,
        operation: Some(operation),
        execute_type: Some(execute_type),
        linked_actions: linked_actions,
        resource_ops_collects: Some(datasheet_ops_collects),
        ..Default::default()
      })
    )
  }

  pub async fn update_records_origin(
      dst_id: String,
      _view_id: String,
      _user_id: String,
      mut record_update: RecordUpdateRO,
      context: Rc<DatasheetPackContext>,
      saver: Box<dyn DataSaver>,
    ) -> anyhow::Result<CollaCommandExecuteResult> {
      let snapshot = context.datasheet_pack.snapshot.clone();
      let meta = &snapshot.meta;
      let mut field_map = meta.field_map.clone();
      if record_update.field_key == FieldKeyEnum::NAME {
          field_map = field_map.into_iter().map(|(_, v)| (v.name.clone(), v)).collect();
      }
      let result = record_update.transform(&field_map, None).await;
      match result {
        Ok(ret) => {
          record_update = ret;
        },
        Err(err) => {
          return Err(anyhow::anyhow!("{}", err));
        }
      }
      let record_id_set: std::collections::HashSet<String> = record_update.records.iter().map(|record| record.record_id.clone().unwrap()).collect();
      
      let link_datasheet: ILinkedRecordMap = HashMap::new();

      // //use for getBasicRecordsByRecordIds
      let record_ids: Vec<String> = record_id_set.into_iter().collect();

      let update_cell_options = record_update.records
        .iter()
        .flat_map(|record| {
            record
                .fields
                .iter()
                .map(|(field_id, value)| SetRecordOptions {
                    record_id: record.record_id.clone().unwrap(),
                    field_id: field_id.clone(),
                    value: value.clone(),
                    ..Default::default()
                })
                .collect::<Vec<_>>()
        })
        .collect::<Vec<_>>();
      
      let _resource_type = ResourceType::Datasheet;
      let _resource_id = dst_id.clone();

      let _datasheet_id = Some(dst_id.clone());

      let save_options = SaveOptions {
        auth: AuthHeader{
          token: Some("bearer uskWD3scKoxH9ik7eqXHtup".to_string()),
          ..Default::default()
        },
        prepend_ops: Vec::new(),
      };
      let mut dst1 = Datasheet::new(dst_id, context, saver);
      let result = dst1.update_records(update_cell_options, save_options).await?;
      Ok(result)
  }

  async fn do_command(
    &mut self,
    command: CommandOptions,
    save_options: SaveOptions,
  ) -> anyhow::Result<CollaCommandExecuteResult> {
    let mut result = self.command_manager.execute(command, self.context.clone()).unwrap();

    if result.result == ExecuteResult::Success {
      let options = SaveOpsOptions {
        resource: Resource {
          id: result.resource_id.clone(),
          resource_type: result.resource_type.clone(),
          name: "datasheet.name".to_string(),
          revision: 22,
        },
        auth: Some(save_options.auth),
        internal_fix: None,
        prepend_ops: Some(save_options.prepend_ops),
      };
      let context = Rc::get_mut(&mut self.context).unwrap();
      let save_result = self.saver.save_ops(result.resource_ops_collects.clone().unwrap(), options, context).await?;
      result.save_result = Some(save_result);
    }
    Ok(result)
  }

  pub async fn add_records(&mut self, options: IAddRecordsOptions, 
    save_options: SaveOptions, 
  ) -> anyhow::Result<CollaCommandExecuteResult> {
    let mut new_options = AddRecordsOptions {
      cmd: CollaCommandName::AddRecords,
      datasheet_id: Some(self.id.clone()),
      view_id: options.view_id,
      index: options.index,
      cell_values: options.record_values.clone(),
      group_cell_values: options.group_cell_values,
      ignore_field_permission: options.ignore_field_permission.unwrap_or(false),
      ..Default::default()
    };
    new_options.count = if options.count > 0 {
      options.count
    } else {
      let value = options.record_values.clone();
      if value.is_none() {
        0
      } else {
        value.unwrap().len()
      }
    };
    let options = CommandOptions::AddRecordsOptions(new_options);

    self.do_command(options, save_options).await
  }

  pub async fn update_records(&mut self, 
    options: Vec<SetRecordOptions>, 
    save_options: SaveOptions, 
  ) -> anyhow::Result<CollaCommandExecuteResult> {
    let snapshot = self.context.datasheet_pack.snapshot.clone();
    let dst_id = snapshot.datasheet_id.clone();
    let cmd = CollaCommandName::SetRecords;
    let options = CommandOptions::SetRecordsOptions(SetRecordsOptions {
        cmd: cmd.clone(),
        datasheet_id: Some(dst_id.clone()),
        data: options,
        ..Default::default()
    });
    self.do_command(options, save_options).await
  }

  pub async fn delete_records(&mut self, options: Vec<String>, 
    save_options: SaveOptions, 
  ) -> anyhow::Result<CollaCommandExecuteResult> {
    let cmd = CollaCommandName::DeleteRecords;
    let options = CommandOptions::DeleteRecordOptions(DeleteRecordOptions {
        cmd: cmd.clone(),
        data: options,
        ..Default::default()
    });
    self.do_command(options, save_options).await
  }

  pub async fn add_fields(&mut self, options: Vec<AddFieldOptions>, 
    save_options: SaveOptions, 
  ) -> anyhow::Result<CollaCommandExecuteResult> {
    let snapshot = self.context.datasheet_pack.snapshot.clone();
    let dst_id = snapshot.datasheet_id.clone();
    let cmd = CollaCommandName::AddFields;
    let options = CommandOptions::AddFieldsOptions(AddFieldsOptions {
        cmd: cmd.clone(),
        datasheet_id: Some(dst_id.clone()),
        data: options,
        ..Default::default()
    });
    self.do_command(options, save_options).await
  }

  pub async fn delete_fields(&mut self, options: Vec<DeleteFieldData>, 
    save_options: SaveOptions, 
  ) -> anyhow::Result<CollaCommandExecuteResult> {
    let snapshot = self.context.datasheet_pack.snapshot.clone();
    let dst_id = snapshot.datasheet_id.clone();
    let cmd = CollaCommandName::DeleteField;
    let options = CommandOptions::DeleteFieldOptions(DeleteFieldOptions {
        cmd: cmd.clone(),
        datasheet_id: Some(dst_id.clone()),
        data: options
    });
    self.do_command(options, save_options).await
  }

  pub async fn update_field(&mut self, options: FieldSO, 
    save_options: SaveOptions, 
  ) -> anyhow::Result<CollaCommandExecuteResult> {
    let snapshot = self.context.datasheet_pack.snapshot.clone();
    let dst_id = snapshot.datasheet_id.clone();
    let cmd = CollaCommandName::SetFieldAttr;
    let options = CommandOptions::SetFieldAttrOptions(SetFieldAttrOptions {
        cmd: cmd.clone(),
        field_id: options.id.clone(),
        datasheet_id: Some(dst_id.clone()),
        data: options,
        ..Default::default()
    });
    self.do_command(options, save_options).await
  }

  pub async fn add_views(&mut self, options: Vec<AddView>, 
    save_options: SaveOptions, 
  ) -> anyhow::Result<CollaCommandExecuteResult> {
    let cmd = CollaCommandName::AddViews;
    let options = CommandOptions::AddViewsOptions(AddViewsOptions {
        cmd: cmd.clone(),
        data: options,
    });
    self.do_command(options, save_options).await
  }

  pub async fn delete_views(&mut self, options: Vec<String>, 
    save_options: SaveOptions, 
  ) -> anyhow::Result<CollaCommandExecuteResult> {
    let cmd = CollaCommandName::DeleteViews;
    let options = CommandOptions::DeleteViewsOptions(DeleteViewsOptions {
        cmd: cmd.clone(),
        data: options.iter().map(|view_id| DeleteView{view_id: view_id.to_string()}).collect(),
    });
    self.do_command(options, save_options).await
  }

  pub async fn modify_views(&mut self, options: Vec<ModifyView>, 
    save_options: SaveOptions, 
  ) -> anyhow::Result<CollaCommandExecuteResult> {
    let cmd = CollaCommandName::ModifyViews;
    let options = CommandOptions::ModifyViewsOptions(ModifyViewsOptions {
        cmd: cmd.clone(),
        data: options,
        ..Default::default()
    });
    self.do_command(options, save_options).await
  }

  // pub fn get_view(&mut self, id_or_options: Option<Either<&str, IViewOptions>>) -> Option<ViewLogic> {
  pub fn get_view(&mut self, id_or_options: &str) -> Option<ViewLogic> {
    // let state = self.store.get_state();

    // let view_id: Option<&str>;
    let view_id: Option<&str> = Some(id_or_options);
    // match id_or_options {
    //     Some(Either::Left(id)) => view_id = Some(id),
    //     Some(Either::Right(options)) => {
    //         let info = (options.get_view_info)();
    //         if info.is_none() {
    //             return None;
    //         }

    //         return Some(ViewLogic::new(self, info.unwrap()));
    //     }
    //     _ => view_id = None,
    // }

    let snapshot = self.context.datasheet_pack.snapshot.clone();
    // if snapshot.is_none() {
    //     return None;
    // }

    let view: Option<ViewSO>;
    if let Some(id) = view_id {
        // view = Selectors::get_view_by_id(&snapshot.unwrap(), &id);
        let view_tmp = snapshot.get_view(id).clone();
        // view = Some(view_tmp);
        if view_tmp.is_none() {
            return None;
        }
        view = Some(view_tmp.unwrap().clone());
    } else {
        view = Some(snapshot.meta.views[0].clone());
    }
    // view
    Some(ViewLogic::new(
      self, 
      // &self.store, 
      // self.context.clone(),
      IViewInfo {
        property: view.unwrap(),
        field_map: snapshot.meta.field_map.clone(),
    }))
  }
}