use std::rc::Rc;

use crate::{so::DatasheetPackContext, ot::types::{ActionOTO, ResourceType}};

use super::{CollaCommandDefExecuteResult, CollaCommandName, CommandOptions};


pub trait ICollaCommandDef<T = CommandOptions> {
    fn execute(&self, context: Rc<DatasheetPackContext>, options: T) -> anyhow::Result<Option<CollaCommandDefExecuteResult>>;

    fn can_undo(&self, _context: Rc<DatasheetPackContext>, _actions: &[ActionOTO]) -> bool {
        true
    }

    fn can_redo(&self, _context: Rc<DatasheetPackContext>, _actions: &[ActionOTO]) -> bool {
        true
    }

    fn undoable(&self) -> bool {
        false
    }
}

pub struct ICommandOptionBase {
    _cmd: CollaCommandName,
    _resource_id: String,
    _resource_type: ResourceType,
}

// pub struct CollaCommand<T = ICommandOptionBase> {
pub struct CollaCommand<T = CommandOptions> {
    pub cmd_def: Box<dyn ICollaCommandDef<T>>,
    _name: String,
}

impl<T> CollaCommand<T> {
    pub fn new(cmd_def: Box<dyn ICollaCommandDef<T>>, _name: String) -> Self {
        Self { cmd_def, _name }
    }

    pub fn undoable(&self) -> bool {
        self.cmd_def.undoable()
    }

    pub fn can_undo(&self, context: Rc<DatasheetPackContext>, actions: &[ActionOTO]) -> bool {
        if !self.undoable() {
            return false;
        }

        self.cmd_def.can_undo(context, actions)
    }

    pub fn can_redo(&self, context: Rc<DatasheetPackContext>, actions: &[ActionOTO]) -> bool {
        if !self.undoable() {
            return false;
        }

        self.cmd_def.can_redo(context, actions)
    }

    pub fn execute(&self, context: Rc<DatasheetPackContext>, options: T) -> anyhow::Result<Option<CollaCommandDefExecuteResult>> {
        self.cmd_def.execute(context, options)
    }
}