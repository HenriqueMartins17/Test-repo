use crate::fields::property::field_types::{BasicValueType, FormulaFuncType};
use crate::formula::errors::FormulaResult;
use crate::formula::functions::basic::{FormulaEvaluateContext, FormulaFunc, FormulaParam, DEFAULT_ACCEPT_VALUE_TYPE};
use crate::formula::parser::ast::AstNode;
use crate::prelude::CellValue;

pub struct RecordIdFunc;

impl RecordIdFunc {
  pub fn new() -> Self {
    Self
  }
}

impl FormulaFunc for RecordIdFunc {
  fn get_type(&self) -> FormulaFuncType {
    FormulaFuncType::Record
  }

  fn get_accept_value_type(&self) -> Vec<BasicValueType> {
    DEFAULT_ACCEPT_VALUE_TYPE.to_vec()
  }

  fn validate_params(&self, _params: &Vec<&AstNode>) -> FormulaResult<()> {
    Ok(())
  }

  fn get_return_type(&self, params: &Vec<&AstNode>) -> FormulaResult<BasicValueType> {
    self.validate_params(params)?;
    Ok(BasicValueType::String)
  }

  fn func(&self, _params: &Vec<FormulaParam>, context: &FormulaEvaluateContext) -> FormulaResult<CellValue> {
    return Ok(CellValue::String(context.record.id.clone()));
  }
}

#[cfg(test)]
mod tests {
  use std::collections::HashMap;

  use crate::formula::helper::tests::test_assert_result;
  use crate::prelude::CellValue;

  pub fn assert_result(expected: CellValue, expression: &str, record_data: &HashMap<String, CellValue>) {
    test_assert_result(expected, expression, record_data, &HashMap::new(), None);
  }

  #[test]
  fn test_record_function_test_record_id() {
    let record_data = vec![("a".to_string(), CellValue::Number(0.0))].into_iter().collect();

    assert_result(CellValue::String("xyz".to_string()), "RECORD_ID()", &record_data);
    assert_result(CellValue::String("xyz".to_string()), "RECORD_ID({a})", &record_data);
  }
}
