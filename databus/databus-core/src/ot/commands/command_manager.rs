// migrate from the origin `command manager`

use databus_shared::prelude::HashMapExt;

use crate::ot::changeset::Operation;
use crate::ot::commands::ExecuteFailReason;
use crate::ot::datasheet::{AddFields, DeleteComment, AddRecords, ModifyViews, DeleteViews, DeleteField, UpdateComment, SetRecords, DeleteRecord, SetFieldAttr, AddViews};
use crate::ot::types::ResourceType;
use crate::so::DatasheetPackContext;
use crate::ot::changeset::ResourceOpsCollect;

use super::{CommandOptions, CollaCommand, ICollaCommandDef, CollaCommandName, CollaCommandDefExecuteResult, ExecuteType, CollaCommandExecuteResult, ExecuteResult, CellFormatChecker};

use std::collections::HashMap;
use std::rc::Rc;

pub struct CollaCommandManager {
    commands: HashMap<String, Box<CollaCommand>>,
    // cell_format_checker: CellFormatChecker,
    // link_integrity_checker: LinkIntegrityChecker,
    // listener: Box<dyn ICollaCommandManagerListener>,
    // pub context: Rc<DatasheetPackContext>,
}

impl CollaCommandManager {
    pub fn new(
      // listener: Box<dyn ICollaCommandManagerListener>, 
      // store: Store<IReduxState, AnyAction>
      // context: Rc<DatasheetPackContext>,
    ) -> Self {
        let mut manager = Self {
            commands: HashMap::new(),
            // cell_format_checker: CellFormatChecker::new(context.clone()),
            // link_integrity_checker: LinkIntegrityChecker::new(store.clone()),
            // listener,
            // context
        };
        // let mut colla_command_map: HashMap<&str, Box<dyn ICollaCommandDef>> = HashMap::new();

        // for command_name in colla_command_map.keys() {
        //     manager.register(command_name.clone(), colla_command_map.get(command_name).unwrap().clone());
        // }
        manager.register(CollaCommandName::AddFields.to_string(), Box::new(AddFields{}));
        manager.register(CollaCommandName::AddRecords.to_string(), Box::new(AddRecords{}));
        manager.register(CollaCommandName::AddViews.to_string(), Box::new(AddViews{}));
        manager.register(CollaCommandName::DeleteComment.to_string(), Box::new(DeleteComment{}));
        manager.register(CollaCommandName::DeleteField.to_string(), Box::new(DeleteField{}));
        manager.register(CollaCommandName::DeleteRecords.to_string(), Box::new(DeleteRecord{}));
        manager.register(CollaCommandName::DeleteViews.to_string(), Box::new(DeleteViews{}));
        manager.register(CollaCommandName::ModifyViews.to_string(), Box::new(ModifyViews{}));
        manager.register(CollaCommandName::SetFieldAttr.to_string(), Box::new(SetFieldAttr{}));
        manager.register(CollaCommandName::SetRecords.to_string(), Box::new(SetRecords{}));
        manager.register(CollaCommandName::UpdateComment.to_string(), Box::new(UpdateComment{}));
        manager
    }

    pub fn register(&mut self, name: String, command_def: Box<dyn ICollaCommandDef>) {
        if self.commands.contains_key(&name) {
            println!("the command name {} is registered and will be unregistered", name);
            self.unregister(name.clone());
        }
        self.commands.insert(name.clone(), Box::new(CollaCommand::new(command_def, name)));
    }

    pub fn unregister(&mut self, name: String) {
        self.commands.remove(&name);
    }

    pub fn has_command(&self, name: &str) -> bool {
        self.commands.contains_key(name)
    }

    pub fn execute(&self,
      options: CommandOptions,
      context: Rc<DatasheetPackContext>,
    ) -> anyhow::Result<CollaCommandExecuteResult> {
        let command = self.commands.get(&options.to_string());
        let dst_id = context.datasheet_pack.snapshot.datasheet_id.clone();
        if command.is_none() {
            let none_result = CollaCommandExecuteResult{
              resource_id: dst_id.to_string(),
              resource_type: ResourceType::Datasheet,
              result: ExecuteResult::None,
              ..Default::default()
            };
            return Ok(none_result);
        }
        let cmd = options.to_string();
        let ret_pre = command.unwrap().cmd_def.execute(context.clone(), options);
        self.get_colla_command_execute_result(cmd, dst_id, ret_pre, context)
    }

    pub fn get_colla_command_execute_result(
      &self,
      cmd: String,
      dst_id: String,
      ret_pre: anyhow::Result<Option<CollaCommandDefExecuteResult>>,
      context: Rc<DatasheetPackContext>
    ) -> anyhow::Result<CollaCommandExecuteResult> {
      let none_result = CollaCommandExecuteResult{
        resource_id: dst_id.to_string(),
        resource_type: ResourceType::Datasheet,
        result: ExecuteResult::None,
        ..Default::default()
      };
      match ret_pre {
        Ok(ret) => {
          match ret {
            Some(mut ret) => {
                let flushed_actions = Vec::new();
                if ret.linked_actions.is_some() {
                    let _linked_actions = ret.linked_actions.clone().unwrap();
                    // linked_actions.push()
                }else {
                    let linked_actions = flushed_actions;
                    ret.linked_actions = Some(linked_actions);
                }
                // const memberFieldAction = context.memberFieldMaintainer.flushMemberAction(context.state);
  
                // if (memberFieldAction.length) {
                //   ret.actions.push(...memberFieldAction);
                // }
                let result = self.execute_actions(cmd, ret, ExecuteType::Execute, context).unwrap();
                match result {
                    Some(result) => {
                        if result.result == ExecuteResult::Success {
                          return Ok(result);
                        }
                        Ok(none_result)
                    },
                    None => {
                        Ok(none_result)
                    }
                }
            },
            None => {
                Ok(none_result)
            }
          }
        },
        Err(err) => {
          println!("err: {:#?}", err);
          // Player.doTrigger(Events.app_error_logger, {
          //   error: new Error(`command execution error: ${(e as any).message}`),
          //   metaData: {
          //     resourceId: options.resourceId,
          //     resourceType: options.resourceType,
          //   },
          // });
          // this._listener.handleCommandExecuteError &&
          //   this._listener.handleCommandExecuteError({
          //     type: ErrorType.CollaError,
          //     code: ErrorCode.CommandExecuteFailed,
          //     message: (e as any).message,
          //   });
          // return {
          //   resourceId: options.resourceId,
          //   resourceType: options.resourceType,
          //   result: ExecuteResult.Fail,
          //   reason: ExecuteFailReason.ActionError,
          // };
          Ok(CollaCommandExecuteResult{
            resource_id: dst_id.to_string(),
            resource_type: ResourceType::Datasheet,
            result: ExecuteResult::Fail,
            reason: Some(ExecuteFailReason::ActionError),
            ..Default::default()
          })
        }
      }
    }

    //tmp code, lost some logic
    pub fn execute_actions(
      &self,
      cmd: String,
      ret: CollaCommandDefExecuteResult,
      execute_type: ExecuteType,
      context: Rc<DatasheetPackContext>
    ) -> anyhow::Result<Option<CollaCommandExecuteResult>> {
      let CollaCommandDefExecuteResult {  resource_id, resource_type, actions, data, linked_actions, field_map_snapshot, .. } = ret;
      let command = self.commands.get(&cmd.to_string());

      if command.is_none() || resource_id.is_empty() {
          eprintln!("can't find command or resource {:?} {:?}", cmd, resource_id);
          return Ok(None);
      }

      match execute_type {
          ExecuteType::Redo => {
              if !command.unwrap().can_redo(context.clone(), &actions) {
                  return Ok(None);
              }
          }
          ExecuteType::Undo => {
              if !command.unwrap().can_undo(context.clone(), &actions) {
                  return Ok(None);
              }
          }
          _ => {}
      }
      if actions.is_empty() {
        return Ok(None);
      }

      // if (this.addUndoStack && command.undoable()) {
      //   this.addUndoStack(cmd, ret, executeType);
      // }

      let mut _actions = actions;
      let _field_map_snapshot = field_map_snapshot;
      if resource_type == ResourceType::Datasheet {
        let cell_format_checker = CellFormatChecker::new(context.clone());
        // println!("_actions = {:?}", _actions);
        // This is adjusted to check all ops involved in cell writing, one is to judge the type, and if the type is consistent, check the format
        // _actions = self.cell_format_checker.parse(_actions, &resource_id, &_field_map_snapshot);
        _actions = cell_format_checker.parse(_actions, &resource_id, &_field_map_snapshot);
        // println!("_actions2 = {:?}", _actions);
        if execute_type == ExecuteType::Redo || execute_type == ExecuteType::Undo {
            _actions = if _field_map_snapshot.is_some() {
              _actions
            } else {
              _actions
                // self.link_integrity_checker.parse(_actions, &resource_id, &linked_actions)
            };
        }
      }
      let _cmd = if execute_type == ExecuteType::Undo {
          format!("UNDO:{}", cmd)
      } else {
          cmd.to_string()
      };
      let mut operation = Operation {
        cmd: _cmd.to_string(),
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
}
