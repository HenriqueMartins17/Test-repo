use crate::fields::property::field_types::{BasicValueType, FormulaFuncType};
use crate::formula::errors::{Error, FormulaResult};
use crate::formula::functions::basic::{DEFAULT_ACCEPT_VALUE_TYPE, FormulaEvaluateContext, FormulaFunc, FormulaParam};
use crate::formula::functions::date_time::get_day_js;
use crate::formula::functions::numeric::first_param_is_array;
use crate::formula::i18n::Strings;
use crate::formula::parser::ast::AstNode;
use crate::params_i18n;
use crate::prelude::CellValue;

struct LogicalFunc;
impl LogicalFunc {
  fn get_type() -> FormulaFuncType {
    FormulaFuncType::Logical
  }

  fn get_accept_value_type() -> Vec<BasicValueType> {
    DEFAULT_ACCEPT_VALUE_TYPE.to_vec()
  }
}

/// If Function
pub struct IfFunc;

impl IfFunc {
  pub fn new() -> Self {
    Self
  }
}

impl FormulaFunc for IfFunc {
  fn get_type(&self) -> FormulaFuncType {
    LogicalFunc::get_type()
  }

  fn get_accept_value_type(&self) -> Vec<BasicValueType> {
    LogicalFunc::get_accept_value_type()
  }

  fn validate_params(&self, params: &Vec<&AstNode>) -> FormulaResult<()> {
    if params.len() < 3 {
      return Err(Error::new(
        Strings::ParamsCountError.with_params(params_i18n!(paramsName = "IF", paramsCount = 3)),
      ));
    }
    Ok(())
  }

  fn get_return_type(&self, params: &Vec<&AstNode>) -> FormulaResult<BasicValueType> {
    self.validate_params(params)?;

    if params.is_empty() {
      return Ok(BasicValueType::String);
    }

    let [_, a, b, ..] = params.as_slice() else { return Err(Error::new("logic error")) };
    let a_type = a.get_value_type();
    let b_type = b.get_value_type();
    if a_type == &BasicValueType::Array || b_type == &BasicValueType::Array {
      return Ok(BasicValueType::String);
    }
    // If one of the two values is 'BLANK', the result type is determined by the type of the other
    if a.get_token().value.to_uppercase() == "BLANK" {
      return Ok(b_type.clone());
    }
    if b.get_token().value.to_uppercase() == "BLANK" {
      return Ok(a_type.clone());
    }
    // When both value parameters are Number/Boolean/DateTime/String, the return value of the corresponding type is inferred
    if a_type == b_type {
      return Ok(a_type.clone());
    }

    Ok(BasicValueType::String)
  }

  fn func(&self, params: &Vec<FormulaParam>, _context: &FormulaEvaluateContext) -> FormulaResult<CellValue> {
    let [logical, a, b, ..] = params.as_slice() else { return Err(Error::new("logic error")) };

    return if logical.value.is_true() {
      Ok(a.value.clone())
    } else {
      Ok(b.value.clone())
    };
  }
}

/// Switch Function
pub struct SwitchFunc;
impl SwitchFunc {
  pub fn new() -> Self {
    Self
  }
}

impl FormulaFunc for SwitchFunc {
  fn get_type(&self) -> FormulaFuncType {
    LogicalFunc::get_type()
  }

  fn get_accept_value_type(&self) -> Vec<BasicValueType> {
    let mut types = LogicalFunc::get_accept_value_type();
    types.push(BasicValueType::DateTime);
    types
  }

  fn validate_params(&self, params: &Vec<&AstNode>) -> FormulaResult<()> {
    if params.len() < 2 {
      return Err(Error::new(
        Strings::ParamsCountError.with_params(params_i18n!(paramsName = "SWITCH", paramsCount = 2)),
      ));
    }
    Ok(())
  }

  fn get_return_type(&self, params: &Vec<&AstNode>) -> FormulaResult<BasicValueType> {
    if !params.is_empty() {
      self.validate_params(params)?;
    }

    if params.is_empty() {
      return Ok(BasicValueType::String);
    }

    // Only when the return type of the value of all matched expressions is Number/Boolean/DateTime/String,
    // it is inferred as the return value of the corresponding type
    let args_length = params.len() - 1;
    let result_type = if args_length & 1 == 1 {
      Some(params[args_length].get_value_type().clone())
    } else {
      None
    };
    for i in (2..=args_length).step_by(2) {
      let cur_params = params[i];
      // If the matched expression contains BLANK, it is determined by other return types
      if cur_params.get_token().value.to_uppercase() == "BLANK" {
        continue;
      }
      if let Some(result_type) = &result_type {
        if result_type != cur_params.get_value_type() {
          return Ok(BasicValueType::String);
        }
      }
    }
    let result_type = result_type.unwrap_or(BasicValueType::String);
    if result_type == BasicValueType::Array {
      return Ok(BasicValueType::String);
    }
    Ok(result_type)
  }

  fn func(&self, params: &Vec<FormulaParam>, _context: &FormulaEvaluateContext) -> FormulaResult<CellValue> {
    // TODO: handle lookup null value
    let mut target_value = params[0].value.clone();
    let args_length = params.len() - 1;
    let switch_count = args_length / 2;
    let default_value = if args_length & 1 == 1 {
      &params[args_length].value
    } else {
      &CellValue::Null
    };
    let is_date_time_type = params[0]
      .node
      .get_inner_value_type()
      .as_ref()
      .map(|it| it == &BasicValueType::DateTime)
      .unwrap_or(false);

    // Specially handle the matching of Array type fields to BLANK
    let is_empty_array = |param: &FormulaParam| -> bool {
      param.node.get_value_type() == &BasicValueType::Array && param.value.is_array() && param.value.len() == 0
    };

    if is_date_time_type {
      let time = get_day_js(&target_value)?;
      target_value = CellValue::Number(time.timestamp_millis() as f64);
    }

    if is_empty_array(&params[0]) {
      target_value = CellValue::Null;
    }

    if switch_count > 0 {
      for i in 0..switch_count {
        let current_param = &params[i * 2 + 1];
        let mut current_value = params[i * 2 + 1].value.clone();

        if is_date_time_type {
          let time = get_day_js(&current_value)?;
          current_value = CellValue::Number(time.timestamp_millis() as f64);
        }

        if is_empty_array(current_param) {
          current_value = CellValue::Null;
        }

        if target_value == current_value {
          return Ok(params[i * 2 + 2].value.clone());
        }
      }
    }

    Ok(default_value.clone())
  }
}

/// Blank Function
pub struct BlankFunc;

impl BlankFunc {
  pub fn new() -> Self {
    Self
  }
}

impl FormulaFunc for BlankFunc {
  fn get_type(&self) -> FormulaFuncType {
    LogicalFunc::get_type()
  }

  fn get_accept_value_type(&self) -> Vec<BasicValueType> {
    LogicalFunc::get_accept_value_type()
  }

  fn validate_params(&self, _params: &Vec<&AstNode>) -> FormulaResult<()> {
    Ok(())
  }

  fn get_return_type(&self, params: &Vec<&AstNode>) -> FormulaResult<BasicValueType> {
    self.validate_params(params)?;
    Ok(BasicValueType::String)
  }

  fn func(&self, _params: &Vec<FormulaParam>, _context: &FormulaEvaluateContext) -> FormulaResult<CellValue> {
    Ok(CellValue::Null)
  }
}

/// True Function
pub struct TrueFunc;

impl TrueFunc {
  pub fn new() -> Self {
    Self
  }
}

impl FormulaFunc for TrueFunc {
  fn get_type(&self) -> FormulaFuncType {
    LogicalFunc::get_type()
  }

  fn get_accept_value_type(&self) -> Vec<BasicValueType> {
    LogicalFunc::get_accept_value_type()
  }

  fn validate_params(&self, _params: &Vec<&AstNode>) -> FormulaResult<()> {
    Ok(())
  }

  fn get_return_type(&self, _params: &Vec<&AstNode>) -> FormulaResult<BasicValueType> {
    Ok(BasicValueType::Boolean)
  }

  fn func(&self, _params: &Vec<FormulaParam>, _context: &FormulaEvaluateContext) -> FormulaResult<CellValue> {
    Ok(CellValue::Bool(true))
  }
}

/// True Function
pub struct FalseFunc;

impl FalseFunc {
  pub fn new() -> Self {
    Self
  }
}

impl FormulaFunc for FalseFunc {
  fn get_type(&self) -> FormulaFuncType {
    LogicalFunc::get_type()
  }

  fn get_accept_value_type(&self) -> Vec<BasicValueType> {
    LogicalFunc::get_accept_value_type()
  }

  fn validate_params(&self, _params: &Vec<&AstNode>) -> FormulaResult<()> {
    Ok(())
  }

  fn get_return_type(&self, _params: &Vec<&AstNode>) -> FormulaResult<BasicValueType> {
    Ok(BasicValueType::Boolean)
  }

  fn func(&self, _params: &Vec<FormulaParam>, _context: &FormulaEvaluateContext) -> FormulaResult<CellValue> {
    Ok(CellValue::Bool(false))
  }
}

/// And Function
pub struct AndFunc;
impl AndFunc {
  pub fn new() -> Self {
    Self
  }
}

impl FormulaFunc for AndFunc {
  fn get_type(&self) -> FormulaFuncType {
    LogicalFunc::get_type()
  }

  fn get_accept_value_type(&self) -> Vec<BasicValueType> {
    let mut types = LogicalFunc::get_accept_value_type();
    types.push(BasicValueType::Array);
    types
  }

  fn validate_params(&self, params: &Vec<&AstNode>) -> FormulaResult<()> {
    if params.len() < 1 {
      return Err(Error::new(
        Strings::ParamsCountError.with_params(params_i18n!(paramsName = "AND", paramsCount = 1)),
      ));
    }
    Ok(())
  }

  fn get_return_type(&self, params: &Vec<&AstNode>) -> FormulaResult<BasicValueType> {
    if !params.is_empty() {
      self.validate_params(params)?;
    }
    Ok(BasicValueType::Boolean)
  }

  fn func(&self, params: &Vec<FormulaParam>, _context: &FormulaEvaluateContext) -> FormulaResult<CellValue> {
    // TODO handle lookup null value
    if first_param_is_array(params) {
      if let CellValue::Array(arr) = &params[0].value {
        return Ok(CellValue::Bool(arr.iter().all(|v| v.is_true())));
      }
    }

    Ok(CellValue::Bool(params.iter().all(|v| v.value.is_true())))
  }
}

/// Or Function
pub struct OrFunc;
impl OrFunc {
  pub fn new() -> Self {
    Self
  }
}

impl FormulaFunc for OrFunc {
  fn get_type(&self) -> FormulaFuncType {
    LogicalFunc::get_type()
  }

  fn get_accept_value_type(&self) -> Vec<BasicValueType> {
    let mut types = LogicalFunc::get_accept_value_type();
    types.push(BasicValueType::Array);
    types
  }

  fn validate_params(&self, params: &Vec<&AstNode>) -> FormulaResult<()> {
    if params.len() < 1 {
      return Err(Error::new(
        Strings::ParamsCountError.with_params(params_i18n!(paramsName = "OR", paramsCount = 1)),
      ));
    }
    Ok(())
  }

  fn get_return_type(&self, params: &Vec<&AstNode>) -> FormulaResult<BasicValueType> {
    if !params.is_empty() {
      self.validate_params(params)?;
    }
    Ok(BasicValueType::Boolean)
  }

  fn func(&self, params: &Vec<FormulaParam>, _context: &FormulaEvaluateContext) -> FormulaResult<CellValue> {
    // TODO handle lookup null value
    if first_param_is_array(params) {
      if let CellValue::Array(arr) = &params[0].value {
        return Ok(CellValue::Bool(arr.iter().any(|v| v.is_true())));
      }
    }

    Ok(CellValue::Bool(params.iter().any(|v| v.value.is_true())))
  }
}

/// Xor Function
pub struct XorFunc;
impl XorFunc {
  pub fn new() -> Self {
    Self
  }
}

impl FormulaFunc for XorFunc {
  fn get_type(&self) -> FormulaFuncType {
    LogicalFunc::get_type()
  }

  fn get_accept_value_type(&self) -> Vec<BasicValueType> {
    let mut types = LogicalFunc::get_accept_value_type();
    types.push(BasicValueType::Array);
    types
  }

  fn validate_params(&self, params: &Vec<&AstNode>) -> FormulaResult<()> {
    if params.len() < 1 {
      return Err(Error::new(
        Strings::ParamsCountError.with_params(params_i18n!(paramsName = "XOR", paramsCount = 1)),
      ));
    }
    Ok(())
  }

  fn get_return_type(&self, params: &Vec<&AstNode>) -> FormulaResult<BasicValueType> {
    if !params.is_empty() {
      self.validate_params(params)?;
    }
    Ok(BasicValueType::Boolean)
  }

  fn func(&self, params: &Vec<FormulaParam>, _context: &FormulaEvaluateContext) -> FormulaResult<CellValue> {
    // TODO handle lookup null value
    if first_param_is_array(params) {
      if let CellValue::Array(arr) = &params[0].value {
        return Ok(CellValue::Bool(arr.iter().filter(|v| v.is_true()).count() == 1));
      }
    }

    Ok(CellValue::Bool(
      params.iter().filter(|v| v.value.is_true()).count() == 1,
    ))
  }
}

/// Not Function
pub struct NotFunc;
impl NotFunc {
  pub fn new() -> Self {
    Self
  }
}

impl FormulaFunc for NotFunc {
  fn get_type(&self) -> FormulaFuncType {
    LogicalFunc::get_type()
  }

  fn get_accept_value_type(&self) -> Vec<BasicValueType> {
    LogicalFunc::get_accept_value_type()
  }

  fn validate_params(&self, params: &Vec<&AstNode>) -> FormulaResult<()> {
    if params.len() != 1 {
      return Err(Error::new(
        Strings::ParamsCountError.with_params(params_i18n!(paramsName = "NOT", paramsCount = 1)),
      ));
    }
    Ok(())
  }

  fn get_return_type(&self, params: &Vec<&AstNode>) -> FormulaResult<BasicValueType> {
    self.validate_params(params)?;
    Ok(BasicValueType::Boolean)
  }

  fn func(&self, params: &Vec<FormulaParam>, _context: &FormulaEvaluateContext) -> FormulaResult<CellValue> {
    // TODO handle lookup null value
    Ok(CellValue::Bool(!params[0].value.is_true()))
  }
}

/// Error Function
pub struct ErrorFunc;
impl ErrorFunc {
  pub fn new() -> Self {
    Self
  }
}

impl FormulaFunc for ErrorFunc {
  fn get_type(&self) -> FormulaFuncType {
    LogicalFunc::get_type()
  }

  fn get_accept_value_type(&self) -> Vec<BasicValueType> {
    LogicalFunc::get_accept_value_type()
  }

  fn validate_params(&self, params: &Vec<&AstNode>) -> FormulaResult<()> {
    if params.len() != 1 {
      return Err(Error::new(
        Strings::ParamsCountError.with_params(params_i18n!(paramsName = "ERROR", paramsCount = 1)),
      ));
    }
    Ok(())
  }

  fn get_return_type(&self, params: &Vec<&AstNode>) -> FormulaResult<BasicValueType> {
    if !params.is_empty() {
      self.validate_params(params)?;
    }
    Ok(BasicValueType::String)
  }

  fn func(&self, params: &Vec<FormulaParam>, _context: &FormulaEvaluateContext) -> FormulaResult<CellValue> {
    let error_text = params.get(0).map(|it| it.value.to_string()).unwrap_or("".to_string());
    Err(Error::new(error_text))
  }
}

/// IsError Function
pub struct IsErrorFunc;
impl IsErrorFunc {
  pub fn new() -> Self {
    Self
  }
}

impl FormulaFunc for IsErrorFunc {
  fn get_type(&self) -> FormulaFuncType {
    LogicalFunc::get_type()
  }

  fn get_accept_value_type(&self) -> Vec<BasicValueType> {
    LogicalFunc::get_accept_value_type()
  }

  fn validate_params(&self, params: &Vec<&AstNode>) -> FormulaResult<()> {
    if params.len() < 1 {
      return Err(Error::new(
        Strings::ParamsCountError.with_params(params_i18n!(paramsName = "IS_ERROR", paramsCount = 1)),
      ));
    }
    Ok(())
  }

  fn get_return_type(&self, params: &Vec<&AstNode>) -> FormulaResult<BasicValueType> {
    if !params.is_empty() {
      self.validate_params(params)?;
    }
    Ok(BasicValueType::Boolean)
  }

  fn func(&self, params: &Vec<FormulaParam>, _context: &FormulaEvaluateContext) -> FormulaResult<CellValue> {
    let num = &params[0].value.to_number();
    if num.is_nan() || num.is_infinite() {
      return Ok(CellValue::Bool(true));
    }

    Ok(CellValue::Bool(false))
  }
}

#[cfg(test)]
mod tests {
  use std::collections::HashMap;

  use crate::formula::helper::tests::{test_assert_error, test_assert_result};
  use crate::formula::i18n::Strings;
  use crate::prelude::CellValue;

  pub fn assert_result(expected: CellValue, expression: &str, record_data: &HashMap<String, CellValue>) {
    test_assert_result(expected, expression, record_data, &HashMap::new(), None);
  }

  pub fn assert_error(expected: &str, expression: &str, record_data: &HashMap<String, CellValue>) {
    test_assert_error(expected, expression, record_data, &HashMap::new(), None);
  }

  #[test]
  fn test_logical_function_test_if() {
    let data = vec![("a".to_string(), CellValue::from(0.0))].into_iter().collect();

    assert_result(CellValue::Number(2.0), "IF(0, 1, 2)", &data);
    assert_result(CellValue::Number(1.0), "IF(1, 1, 2)", &data);
    assert_result(CellValue::Number(3.0), "IF(IF({a}, 1, 0), 2, 3)", &data);
    assert_error(&Strings::ParamsCountError.to_string(), r#"IF("x")"#, &data);
  }

  #[test]
  fn test_logical_function_test_switch() {
    let data = vec![
      ("a".to_string(), CellValue::from(0.0)),
      ("b".to_string(), CellValue::from("456")),
    ]
    .into_iter()
    .collect();

    assert_result(
      CellValue::String("one two three".to_string()),
      r#"SWITCH({b}, "one two three")"#,
      &data,
    );
    assert_result(
      CellValue::String("default value".to_string()),
      r#"SWITCH({a}, "123", "one two three", "456", "four five six", "default value")"#,
      &data,
    );
    assert_result(
      CellValue::String("four five six".to_string()),
      r#"SWITCH({b}, "123", "one two three", "456", "four five six", "default value")"#,
      &data,
    );
    assert_result(
      CellValue::Null,
      r#"SWITCH({a}, "123", "one two three", "456", "four five six")"#,
      &data,
    );
    assert_error(&Strings::ParamsCountError.to_string(), r#"SWITCH({a})"#, &data);
  }

  #[test]
  fn test_logical_function_test_true() {
    let data = vec![("b".to_string(), CellValue::from("456"))].into_iter().collect();

    assert_result(CellValue::Bool(true), "TRUE()", &data);
    assert_result(CellValue::Bool(true), "TRUE({b})", &data);
  }

  #[test]
  fn test_logical_function_test_false() {
    let data = vec![("b".to_string(), CellValue::from("456"))].into_iter().collect();

    assert_result(CellValue::Bool(false), "FALSE()", &data);
    assert_result(CellValue::Bool(false), "FALSE({b})", &data);
  }

  #[test]
  fn test_logical_function_test_and() {
    let data = vec![
      ("a".to_string(), CellValue::from(0.0)),
      ("b".to_string(), CellValue::from("456")),
      ("c".to_string(), CellValue::from(1591414562369f64)),
      ("d".to_string(), CellValue::from(vec!["opt1"])),
    ]
    .into_iter()
    .collect();

    assert_result(CellValue::Bool(false), "AND({a})", &data);
    assert_result(CellValue::Bool(true), "AND({b})", &data);
    assert_result(CellValue::Bool(false), "AND({a}, {b}, {c})", &data);
    assert_error(&Strings::ParamsCountError.to_string(), "AND()", &data);
  }

  #[test]
  fn test_logical_function_test_or() {
    let data = vec![
      ("a".to_string(), CellValue::from(0.0)),
      ("b".to_string(), CellValue::from("456")),
      ("c".to_string(), CellValue::from(1591414562369f64)),
      ("d".to_string(), CellValue::from(vec!["opt1", "opt2"])),
    ]
    .into_iter()
    .collect();

    assert_result(CellValue::Bool(false), "OR({a})", &data);
    assert_result(CellValue::Bool(true), "OR({b})", &data);
    assert_result(CellValue::Bool(true), "OR({a}, {b}, {c})", &data);
    assert_error(&Strings::ParamsCountError.to_string(), "OR()", &data);
  }

  #[test]
  fn test_logical_function_test_xor() {
    let data = vec![
      ("a".to_string(), CellValue::from(0.0)),
      ("b".to_string(), CellValue::from("456")),
      ("c".to_string(), CellValue::from(1591414562369f64)),
    ]
    .into_iter()
    .collect();

    assert_result(CellValue::Bool(false), "XOR({a})", &data);
    assert_result(CellValue::Bool(true), "XOR({a}, {b})", &data);
    assert_result(CellValue::Bool(false), "XOR({a}, {b}, {c})", &data);
    assert_error(&Strings::ParamsCountError.to_string(), "XOR()", &data);
  }

  #[test]
  fn test_logical_function_test_not() {
    let data = vec![
      ("a".to_string(), CellValue::from(0.0)),
      ("b".to_string(), CellValue::from("456")),
    ]
    .into_iter()
    .collect();

    assert_result(CellValue::Bool(true), "NOT({a})", &data);
    assert_result(CellValue::Bool(false), "NOT({b})", &data);
    assert_error(&Strings::ParamsCountError.to_string(), "NOT()", &data);
  }

  #[test]
  fn test_logical_function_test_blank() {
    let data = vec![("b".to_string(), CellValue::from("456"))].into_iter().collect();

    assert_result(CellValue::Null, "BLANK()", &data);
    assert_result(CellValue::Number(0.0), "BLANK() + BLANK()", &data);
    assert_result(CellValue::Number(1.0), "1 + BLANK()", &data);
    assert_result(CellValue::Null, "BLANK({b})", &data);
  }

  #[test]
  fn test_logical_function_test_error() {
    let data = vec![
      ("a".to_string(), CellValue::from(0.0)),
      ("b".to_string(), CellValue::from("456")),
    ]
    .into_iter()
    .collect();

    assert_error("", "ERROR()", &data);
    assert_error("0", "ERROR({a})", &data);
    assert_error("", "ERROR(\"\")", &data);
    assert_error("456", "ERROR({b})", &data);
  }

  #[test]
  fn test_logical_function_test_is_error() {
    let data = vec![
      ("a".to_string(), CellValue::from(0.0)),
      ("b".to_string(), CellValue::from("abc")),
    ]
    .into_iter()
    .collect();

    assert_result(CellValue::Bool(true), "IS_ERROR(-1/{a})", &data);
    assert_result(CellValue::Bool(true), "IS_ERROR({a}/{a})", &data);
    assert_result(CellValue::Bool(true), "IS_ERROR({b}/{a})", &data);
    assert_result(CellValue::Bool(true), "IS_ERROR({a}/{b})", &data);
    assert_result(CellValue::Bool(true), "IS_ERROR({b}/{b})", &data);
    assert_result(CellValue::Bool(false), "IS_ERROR({a}/1)", &data);
    assert_error(&Strings::ParamsCountError.to_string(), "IS_ERROR()", &data);
  }
}
