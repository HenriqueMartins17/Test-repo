use std::rc::Rc;

use crate::fields::property::field_types::{BasicValueType, FormulaFuncType};
use crate::formula::errors::FormulaResult;
use crate::formula::parser::ast::AstNode;
use crate::formula::types::IField;
use crate::prelude::{CellValue, RecordSO};
use crate::so::DatasheetPackContext;

pub struct FormulaParam<'a> {
  pub node: &'a AstNode,
  pub value: CellValue,
}

pub struct FormulaEvaluateContext {
  pub state: Rc<DatasheetPackContext>,
  pub field: Rc<IField>,
  pub record: Rc<RecordSO>,
}

pub fn get_black_value_by_type(t: &BasicValueType, value: &CellValue) -> CellValue {
  if value.is_null() {
    return CellValue::Null;
  }
  return match t {
    BasicValueType::String => CellValue::String(String::new()),
    BasicValueType::Boolean => CellValue::Bool(false),
    _ => CellValue::Null,
  };
}

pub const DEFAULT_ACCEPT_VALUE_TYPE: [BasicValueType; 3] =
  [BasicValueType::Boolean, BasicValueType::Number, BasicValueType::String];

pub trait FormulaFunc {
  fn get_type(&self) -> FormulaFuncType;
  fn get_accept_value_type(&self) -> Vec<BasicValueType>;
  fn validate_params(&self, params: &Vec<&AstNode>) -> FormulaResult<()>;
  fn get_return_type(&self, params: &Vec<&AstNode>) -> FormulaResult<BasicValueType>;
  fn func(&self, params: &Vec<FormulaParam>, context: &FormulaEvaluateContext) -> FormulaResult<CellValue>;
}
