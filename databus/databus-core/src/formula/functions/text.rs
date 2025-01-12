use crate::fields::property::field_types::{BasicValueType, FormulaFuncType};
use crate::formula::errors::{Error, FormulaResult};
use crate::formula::functions::array::flatten_params;
use crate::formula::functions::basic::{DEFAULT_ACCEPT_VALUE_TYPE, FormulaEvaluateContext, FormulaFunc, FormulaParam};
use crate::formula::functions::numeric::no_nan;
use crate::formula::i18n::Strings;
use crate::formula::parser::AstNode;
use crate::params_i18n;
use crate::prelude::CellValue;
use crate::utils::compatible_type::JsString;

struct TextFunc;
impl TextFunc {
  fn get_type() -> FormulaFuncType {
    FormulaFuncType::Text
  }

  fn get_accept_value_type() -> Vec<BasicValueType> {
    DEFAULT_ACCEPT_VALUE_TYPE.to_vec()
  }
}

/// Find Function
pub struct FindFunc;
impl FindFunc {
  pub fn new() -> Self {
    Self
  }
}

impl FormulaFunc for FindFunc {
  fn get_type(&self) -> FormulaFuncType {
    TextFunc::get_type()
  }

  fn get_accept_value_type(&self) -> Vec<BasicValueType> {
    TextFunc::get_accept_value_type()
  }

  fn validate_params(&self, params: &Vec<&AstNode>) -> FormulaResult<()> {
    if params.len() < 2 {
      return Err(Error::new(
        Strings::ParamsCountError.with_params(params_i18n!(paramsName = "FIND", paramsCount = 2)),
      ));
    }
    Ok(())
  }

  fn get_return_type(&self, params: &Vec<&AstNode>) -> FormulaResult<BasicValueType> {
    if !params.is_empty() {
      self.validate_params(params)?;
    }
    Ok(BasicValueType::Number)
  }

  fn func(&self, params: &Vec<FormulaParam>, _context: &FormulaEvaluateContext) -> FormulaResult<CellValue> {
    if params[0].value.is_null() || params[1].value.is_null() {
      return Ok(CellValue::Number(0.0));
    }
    let string_to_find = JsString::from(params[0].value.to_string());
    let where_to_search = JsString::from(params[1].value.to_string());

    let start_from_position = params.get(2).map(|it| it.value.to_number()).unwrap_or(0.0);

    if start_from_position >= 0.0 {
      let start_from_position = if start_from_position > 0.0 {
        start_from_position - 1.0
      } else {
        start_from_position
      };

      let index = where_to_search.index_of(&string_to_find, start_from_position as usize) + 1;
      return Ok(CellValue::Number(index as f64));
    }

    // positionIndex supports negative numbers, if a negative number is filled in, the position is calculated from the back to the front
    let start_from_position = where_to_search.len() as f64 + start_from_position;
    let index = where_to_search.last_index_of(&string_to_find, start_from_position as usize) + 1;
    Ok(CellValue::Number(index as f64))
  }
}

/// Search Function
pub struct SearchFunc;
impl SearchFunc {
  pub fn new() -> Self {
    Self
  }
}

impl FormulaFunc for SearchFunc {
  fn get_type(&self) -> FormulaFuncType {
    TextFunc::get_type()
  }

  fn get_accept_value_type(&self) -> Vec<BasicValueType> {
    TextFunc::get_accept_value_type()
  }

  fn validate_params(&self, params: &Vec<&AstNode>) -> FormulaResult<()> {
    if params.len() < 2 {
      return Err(Error::new(
        Strings::ParamsCountError.with_params(params_i18n!(paramsName = "SEARCH", paramsCount = 2)),
      ));
    }
    Ok(())
  }

  fn get_return_type(&self, params: &Vec<&AstNode>) -> FormulaResult<BasicValueType> {
    if !params.is_empty() {
      self.validate_params(params)?;
    }
    Ok(BasicValueType::Number)
  }

  fn func(&self, params: &Vec<FormulaParam>, _context: &FormulaEvaluateContext) -> FormulaResult<CellValue> {
    if params[0].value.is_null() || params[1].value.is_null() {
      return Ok(CellValue::Null);
    }
    let string_to_find = JsString::from(params[0].value.to_string());
    let where_to_search = JsString::from(params[1].value.to_string());

    let start_from_position = if let Some(v) = params.get(2) {
      let value = v.value.to_number();
      let v = if value > 0.0 { value - 1.0 } else { value };
      v
    } else {
      0.0
    };

    let result = where_to_search.index_of(&string_to_find, start_from_position as usize);
    if result == -1 {
      return Ok(CellValue::Null);
    }

    Ok(CellValue::Number((result + 1) as f64))
  }
}

/// Mid Function
pub struct MidFunc;
impl MidFunc {
  pub fn new() -> Self {
    Self
  }
}

impl FormulaFunc for MidFunc {
  fn get_type(&self) -> FormulaFuncType {
    TextFunc::get_type()
  }

  fn get_accept_value_type(&self) -> Vec<BasicValueType> {
    TextFunc::get_accept_value_type()
  }

  fn validate_params(&self, params: &Vec<&AstNode>) -> FormulaResult<()> {
    if params.len() < 3 {
      return Err(Error::new(
        Strings::ParamsCountError.with_params(params_i18n!(paramsName = "MID", paramsCount = 3)),
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
    let str = &params[0].value;
    let where_to_start = &params[1].value;
    let count = &params[2].value;
    if str.is_null() {
      return Ok(CellValue::Null);
    }

    let str = JsString::from(str.to_string());
    let where_to_start = where_to_start.to_number() - 1.0;
    let count = count.to_number();

    let result = str.slice(where_to_start as usize, (where_to_start + count) as usize);
    Ok(CellValue::String(result.to_string()))
  }
}

/// Replace Function
pub struct ReplaceFunc;
impl ReplaceFunc {
  pub fn new() -> Self {
    Self
  }
}

impl FormulaFunc for ReplaceFunc {
  fn get_type(&self) -> FormulaFuncType {
    TextFunc::get_type()
  }

  fn get_accept_value_type(&self) -> Vec<BasicValueType> {
    TextFunc::get_accept_value_type()
  }

  fn validate_params(&self, params: &Vec<&AstNode>) -> FormulaResult<()> {
    if params.len() < 4 {
      return Err(Error::new(
        Strings::ParamsCountError.with_params(params_i18n!(paramsName = "REPLACE", paramsCount = 4)),
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
    let str = &params[0].value;
    let where_to_start = &params[1].value;
    let count = &params[2].value;
    let replace_str = &params[3].value;
    if str.is_null() {
      return Ok(CellValue::Null);
    }

    let str = JsString::from(str.to_string());
    let where_to_start = no_nan(where_to_start.to_number() - 1.0);
    let count = no_nan(count.to_number());
    let replace_str = if replace_str.is_null() {
      JsString::from("")
    } else {
      JsString::from(replace_str.to_string())
    };

    if str.len() <= where_to_start as usize {
      return Ok(CellValue::String((str + replace_str).to_string()));
    }

    let rst = str.sub_string(0, Some(where_to_start as usize))
      + replace_str
      + str.sub_string((where_to_start + count) as usize, None);

    Ok(CellValue::String(rst.to_string()))
  }
}

/// Substitute Function
pub struct SubstituteFunc;
impl SubstituteFunc {
  pub fn new() -> Self {
    Self
  }
}

impl FormulaFunc for SubstituteFunc {
  fn get_type(&self) -> FormulaFuncType {
    TextFunc::get_type()
  }

  fn get_accept_value_type(&self) -> Vec<BasicValueType> {
    TextFunc::get_accept_value_type()
  }

  fn validate_params(&self, params: &Vec<&AstNode>) -> FormulaResult<()> {
    if params.len() < 3 {
      return Err(Error::new(
        Strings::ParamsCountError.with_params(params_i18n!(paramsName = "SUBSTITUTE", paramsCount = 3)),
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
    let str = &params[0].value;
    let old_text = &params[1].value.to_string();
    let new_text = &params[2].value.to_string();

    let mut str = if str.is_null() {
      return Ok(CellValue::Null);
    } else {
      str.to_string()
    };

    return if let Some(param) = params.get(3) {
      let n = no_nan(param.value.to_number() - 1.0) as usize;
      if n < 1 {
        return Ok(CellValue::String(str));
      }

      let mut count = 0;
      let mut start = 0;

      while let Some(pos) = str[start..].find(old_text) {
        if count == n {
          let start_pos = start + pos;
          str.replace_range(start_pos..start_pos + old_text.len(), new_text);
          break;
        }
        start += pos + old_text.len();
        count += 1;
      }

      Ok(CellValue::String(str))
    } else {
      str = str.replace(old_text, new_text);
      Ok(CellValue::String(str))
    };
  }
}

/// Concatenate Function
pub struct ConcatenateFunc;
impl ConcatenateFunc {
  pub fn new() -> Self {
    Self
  }
}

impl FormulaFunc for ConcatenateFunc {
  fn get_type(&self) -> FormulaFuncType {
    TextFunc::get_type()
  }

  fn get_accept_value_type(&self) -> Vec<BasicValueType> {
    let mut types = TextFunc::get_accept_value_type();
    types.push(BasicValueType::Array);
    types
  }

  fn validate_params(&self, params: &Vec<&AstNode>) -> FormulaResult<()> {
    if params.len() < 1 {
      return Err(Error::new(
        Strings::ParamsCountError.with_params(params_i18n!(paramsName = "CONCATENATE", paramsCount = 1)),
      ));
    }
    Ok(())
  }

  fn get_return_type(&self, params: &Vec<&AstNode>) -> FormulaResult<BasicValueType> {
    self.validate_params(params)?;
    Ok(BasicValueType::String)
  }

  fn func(&self, params: &Vec<FormulaParam>, _context: &FormulaEvaluateContext) -> FormulaResult<CellValue> {
    let flatten_value = flatten_params(params)?;
    let s = flatten_value.into_iter().map(|v| v.to_string()).collect();
    Ok(CellValue::String(s))
  }
}

/// Len Function
pub struct LenFunc;

impl LenFunc {
  pub fn new() -> Self {
    Self
  }
}

impl FormulaFunc for LenFunc {
  fn get_type(&self) -> FormulaFuncType {
    TextFunc::get_type()
  }

  fn get_accept_value_type(&self) -> Vec<BasicValueType> {
    TextFunc::get_accept_value_type()
  }

  fn validate_params(&self, params: &Vec<&AstNode>) -> FormulaResult<()> {
    if params.len() < 1 {
      return Err(Error::new(
        Strings::ParamsCountError.with_params(params_i18n!(paramsName = "LEN", paramsCount = 1)),
      ));
    }
    Ok(())
  }

  fn get_return_type(&self, params: &Vec<&AstNode>) -> FormulaResult<BasicValueType> {
    self.validate_params(params)?;
    Ok(BasicValueType::Number)
  }

  fn func(&self, params: &Vec<FormulaParam>, _context: &FormulaEvaluateContext) -> FormulaResult<CellValue> {
    let first_param = params.get(0).ok_or(Error::new(Strings::UnexpectedError))?;
    if first_param.value.is_null() {
      return Ok(CellValue::Null);
    }

    Ok(CellValue::Number(
      first_param.value.to_string().encode_utf16().count() as f64
    ))
  }
}

/// Left Function
pub struct LeftFunc;
impl LeftFunc {
  pub fn new() -> Self {
    Self
  }
}

impl FormulaFunc for LeftFunc {
  fn get_type(&self) -> FormulaFuncType {
    TextFunc::get_type()
  }

  fn get_accept_value_type(&self) -> Vec<BasicValueType> {
    TextFunc::get_accept_value_type()
  }

  fn validate_params(&self, params: &Vec<&AstNode>) -> FormulaResult<()> {
    if params.len() < 1 {
      return Err(Error::new(
        Strings::ParamsCountError.with_params(params_i18n!(paramsName = "LEFT", paramsCount = 1)),
      ));
    }
    Ok(())
  }

  fn get_return_type(&self, params: &Vec<&AstNode>) -> FormulaResult<BasicValueType> {
    self.validate_params(params)?;
    Ok(BasicValueType::String)
  }

  fn func(&self, params: &Vec<FormulaParam>, _context: &FormulaEvaluateContext) -> FormulaResult<CellValue> {
    let value = &params[0].value;
    if value.is_null() {
      return Ok(CellValue::Null);
    }

    let value = JsString::from(value.to_string());
    let count = params.get(1).map(|it| it.value.to_number() as usize).unwrap_or(1);

    let result = value.slice(0, count);
    Ok(CellValue::String(result.to_string()))
  }
}

/// Right Function
pub struct RightFunc;
impl RightFunc {
  pub fn new() -> Self {
    Self
  }
}

impl FormulaFunc for RightFunc {
  fn get_type(&self) -> FormulaFuncType {
    TextFunc::get_type()
  }

  fn get_accept_value_type(&self) -> Vec<BasicValueType> {
    TextFunc::get_accept_value_type()
  }

  fn validate_params(&self, params: &Vec<&AstNode>) -> FormulaResult<()> {
    if params.len() < 1 {
      return Err(Error::new(
        Strings::ParamsCountError.with_params(params_i18n!(paramsName = "RIGHT", paramsCount = 1)),
      ));
    }
    Ok(())
  }

  fn get_return_type(&self, params: &Vec<&AstNode>) -> FormulaResult<BasicValueType> {
    self.validate_params(params)?;
    Ok(BasicValueType::String)
  }

  fn func(&self, params: &Vec<FormulaParam>, _context: &FormulaEvaluateContext) -> FormulaResult<CellValue> {
    let value = &params[0].value;
    if value.is_null() {
      return Ok(CellValue::Null);
    }

    let value = JsString::from(value.to_string());
    let count = params
      .get(1)
      .map(|it| it.value.to_number() as usize)
      .unwrap_or(1)
      .min(value.len());

    let result = value.slice(value.len() - count, value.len());
    Ok(CellValue::String(result.to_string()))
  }
}

/// Repeat Function
pub struct ReptFunc;
impl ReptFunc {
  pub fn new() -> Self {
    Self
  }
}

impl FormulaFunc for ReptFunc {
  fn get_type(&self) -> FormulaFuncType {
    TextFunc::get_type()
  }

  fn get_accept_value_type(&self) -> Vec<BasicValueType> {
    TextFunc::get_accept_value_type()
  }

  fn validate_params(&self, params: &Vec<&AstNode>) -> FormulaResult<()> {
    if params.len() < 2 {
      return Err(Error::new(
        Strings::ParamsCountError.with_params(params_i18n!(paramsName = "REPT", paramsCount = 2)),
      ));
    }
    Ok(())
  }

  fn get_return_type(&self, params: &Vec<&AstNode>) -> FormulaResult<BasicValueType> {
    self.validate_params(params)?;
    Ok(BasicValueType::String)
  }

  fn func(&self, params: &Vec<FormulaParam>, _context: &FormulaEvaluateContext) -> FormulaResult<CellValue> {
    let first_param = params.get(0).ok_or(Error::new(Strings::UnexpectedError))?;
    let second_param = params.get(1).ok_or(Error::new(Strings::UnexpectedError))?;
    if first_param.value.is_null() {
      return Ok(CellValue::Null);
    }
    if second_param.value.is_null() {
      return Ok(first_param.value.clone());
    }

    let s = first_param.value.to_string();
    let count = second_param.value.to_number() as usize;

    Ok(CellValue::String(s.repeat(count)))
  }
}

#[cfg(test)]
mod tests {
  use std::collections::HashMap;

  use crate::formula::helper::tests::{test_assert_error, test_assert_result};
  use crate::formula::i18n::Strings;
  use crate::prelude::CellValue;

  const ENGLISH_STRING: &str = "Welcome to join APITable Team";
  const CHINESE_STRING: &str = "欢迎加入APITable科技";
  const CHINESE_STRING2: &str = "欢迎加入APITable科技，维格科技真棒";

  fn assert_result(expected: CellValue, expression: &str, record_data: &HashMap<String, CellValue>) {
    test_assert_result(expected, expression, record_data, &HashMap::new(), None)
  }

  fn assert_error(expected: &str, expression: &str, record_data: &HashMap<String, CellValue>) {
    test_assert_error(expected, expression, record_data, &HashMap::new(), None);
  }

  fn data(a: f64, b: &str) -> HashMap<String, CellValue> {
    vec![
      ("a".to_string(), CellValue::from(a)),
      ("b".to_string(), CellValue::from(b)),
      ("c".to_string(), CellValue::from(1591414562369f64)),
      ("d".to_string(), CellValue::from(vec!["opt1"])),
    ]
    .into_iter()
    .collect()
  }

  #[test]
  fn test_function_test_find() {
    fn data(a: f64, b: &str, d: Vec<&str>) -> HashMap<String, CellValue> {
      vec![
        ("a".to_string(), CellValue::from(a)),
        ("b".to_string(), CellValue::from(b)),
        ("c".to_string(), CellValue::from(1591414562369f64)),
        ("d".to_string(), CellValue::from(d)),
      ]
      .into_iter()
      .collect()
    }

    assert_result(
      CellValue::Number(17.0),
      "FIND(\"APITable\", {b})",
      &data(0.0, ENGLISH_STRING, vec![]),
    );
    assert_result(
      CellValue::Number(5.0),
      "FIND({d}, {b})",
      &data(0.0, CHINESE_STRING, vec!["opt3"]),
    );
    assert_result(
      CellValue::Number(5.0),
      "FIND({d}, {b}, {a})",
      &data(3.0, CHINESE_STRING, vec!["opt3"]),
    );
    assert_result(
      CellValue::Number(0.0),
      "FIND({d}, {b}, {a})",
      &data(7.0, CHINESE_STRING, vec!["opt2"]),
    );
    assert_result(
      CellValue::Number(0.0),
      "FIND({b}, {d})",
      &data(0.0, CHINESE_STRING, vec!["opt1"]),
    );
    assert_error(
      &Strings::ParamsCountError.to_string(),
      "FIND({b})",
      &data(0.0, CHINESE_STRING, vec![]),
    );
    assert_result(
      CellValue::Number(16.0),
      "FIND(\"维\", {b}, {a})",
      &data(-1.0, CHINESE_STRING2, vec![]),
    );
    assert_result(
      CellValue::Number(6.0),
      "FIND(\"APITable\", {b}, {a})",
      &data(-19.0, "I am APITable and the APITable.", vec![]),
    );
    assert_result(
      CellValue::Number(0.0),
      "FIND(\"维\", {b}, {a})",
      &data(-100.0, CHINESE_STRING2, vec![]),
    );
  }

  #[test]
  fn test_function_test_search() {
    fn data(a: f64, b: &str, d: Vec<&str>) -> HashMap<String, CellValue> {
      vec![
        ("a".to_string(), CellValue::from(a)),
        ("b".to_string(), CellValue::from(b)),
        ("d".to_string(), CellValue::from(d)),
      ]
      .into_iter()
      .collect()
    }

    assert_result(
      CellValue::Number(13.0),
      "SEARCH({d}, {b})",
      &data(0.0, CHINESE_STRING, vec!["opt1"]),
    );
    assert_result(
      CellValue::Number(5.0),
      "SEARCH({d}, {b})",
      &data(0.0, CHINESE_STRING, vec!["opt3"]),
    );
    assert_result(
      CellValue::Number(5.0),
      "SEARCH({d}, {b}, {a})",
      &data(3.0, CHINESE_STRING, vec!["opt3"]),
    );
    assert_result(
      CellValue::Null,
      "SEARCH({d}, {b}, {a})",
      &data(7.0, CHINESE_STRING, vec!["opt2"]),
    );
    assert_result(
      CellValue::Null,
      "SEARCH({b}, {d})",
      &data(0.0, CHINESE_STRING, vec!["opt1"]),
    );
    assert_error(
      &Strings::ParamsCountError.to_string(),
      "SEARCH({b})",
      &data(0.0, CHINESE_STRING, vec!["opt1"]),
    );
  }

  #[test]
  fn test_function_test_mid() {
    fn data(a: f64, b: &str) -> HashMap<String, CellValue> {
      vec![
        ("a".to_string(), CellValue::from(a)),
        ("b".to_string(), CellValue::from(b)),
      ]
      .into_iter()
      .collect()
    }

    assert_result(
      CellValue::String("API".to_string()),
      "MID({b}, 5, 3)",
      &data(0.0, CHINESE_STRING),
    );
    assert_result(
      CellValue::String("APITab".to_string()),
      "MID({b}, 5, 6)",
      &data(0.0, CHINESE_STRING),
    );
    assert_result(
      CellValue::String("".to_string()),
      "MID({b}, 55, 6)",
      &data(0.0, CHINESE_STRING),
    );
    assert_result(
      CellValue::Null,
      "MID({a}, 5, 3)",
      &vec![("b".to_string(), CellValue::from(CHINESE_STRING))]
        .into_iter()
        .collect(),
    );
    assert_error(
      &Strings::ParamsCountError.to_string(),
      "MID({b}, 5)",
      &data(0.0, CHINESE_STRING),
    );
  }

  #[test]
  fn test_function_test_replace() {
    fn data(b: &str) -> HashMap<String, CellValue> {
      vec![("b".to_string(), CellValue::from(b))].into_iter().collect()
    }

    assert_result(
      CellValue::String("欢迎加入goTable科技".to_string()),
      "REPLACE({b}, 5, 3, \"go\")",
      &data(CHINESE_STRING),
    );
    assert_result(
      CellValue::String("欢迎加入Table科技".to_string()),
      "REPLACE({b}, 5, 3, a)",
      &data(CHINESE_STRING),
    );
    assert_result(
      CellValue::String("欢迎加入APITable科技go".to_string()),
      "REPLACE({b}, 35, 3, \"go\")",
      &data(CHINESE_STRING),
    );
    assert_result(
      CellValue::String("3已被Replaced".to_string()),
      "REPLACE(3, 4, \"This is an error argument\", \"已被Replaced\")",
      &data(CHINESE_STRING),
    );
    assert_result(
      CellValue::String("欢迎加入go".to_string()),
      "REPLACE({b}, 5, 33, \"go\")",
      &data(CHINESE_STRING),
    );
    assert_result(CellValue::Null, "REPLACE({b}, 5, 33, \"go\")", &HashMap::new());
    assert_error(
      &Strings::ParamsCountError.to_string(),
      "REPLACE({b}, 5)",
      &data(CHINESE_STRING),
    );
  }

  #[test]
  fn test_function_test_substitute() {
    fn data(b: &str) -> HashMap<String, CellValue> {
      vec![("b".to_string(), CellValue::from(b))].into_iter().collect()
    }

    assert_result(
      CellValue::String("老胡，老张，老王".to_string()),
      "SUBSTITUTE({b}, \"小\", \"老\")",
      &data("小胡，小张，小王"),
    );
    assert_result(
      CellValue::String("little tom，big mary，little lucy".to_string()),
      "SUBSTITUTE({b}, \"little\", \"big\", 2)",
      &data("little tom，little mary，little lucy"),
    );
    assert_result(
      CellValue::String("little tom，little mary，little lucy".to_string()),
      "SUBSTITUTE({b}, \"little\", \"big\", 4)",
      &data("little tom，little mary，little lucy"),
    );
    assert_result(
      CellValue::String("little tom，little mary，little lucy".to_string()),
      "SUBSTITUTE({b}, \"little\", \"big\", -1)",
      &data("little tom，little mary，little lucy"),
    );
    assert_error(
      &Strings::ParamsCountError.to_string(),
      "SUBSTITUTE({b}, \"APITable\")",
      &data(CHINESE_STRING),
    );
  }

  #[test]
  fn test_function_test_concatenate() {
    assert_result(
      CellValue::String("欢迎加入APITable科技科".to_string()),
      "CONCATENATE({b}, {d})",
      &data(0.0, CHINESE_STRING),
    );
    assert_result(
      CellValue::String("0欢迎加入APITable科技2020/06/06科".to_string()),
      "CONCATENATE({a}, {b}, {c}, {d})",
      &data(0.0, CHINESE_STRING),
    );
    assert_error(
      &Strings::ParamsCountError.to_string(),
      "CONCATENATE()",
      &data(0.0, CHINESE_STRING),
    );
  }

  #[test]
  fn test_function_test_len() {
    assert_result(CellValue::Number(14.0), "LEN({b}, {a})", &data(0.0, CHINESE_STRING));
    assert_result(CellValue::Number(3.0), "LEN(a)", &data(100.0, CHINESE_STRING));
    assert_error(&Strings::ParamsCountError.to_string(), "LEN()", &data(0.0, ""));
  }

  #[test]
  fn test_function_test_left() {
    fn data(a: f64, b: &str) -> HashMap<String, CellValue> {
      vec![
        ("a".to_string(), CellValue::from(a)),
        ("b".to_string(), CellValue::from(b)),
      ]
      .into_iter()
      .collect()
    }

    assert_result(
      CellValue::String("".to_string()),
      "LEFT({b}, {a})",
      &data(0.0, CHINESE_STRING),
    );
    assert_result(
      CellValue::String("欢".to_string()),
      "LEFT({b})",
      &data(0.0, CHINESE_STRING),
    );
    assert_result(
      CellValue::String("欢迎加".to_string()),
      "LEFT({b}, 3)",
      &data(0.0, CHINESE_STRING),
    );
    assert_result(
      CellValue::Null,
      "LEFT({b}, 3)",
      &vec![("a".to_string(), CellValue::from(0.0))].into_iter().collect(),
    );
    assert_result(
      CellValue::String(CHINESE_STRING.to_string()),
      "LEFT({b}, 33)",
      &data(0.0, CHINESE_STRING),
    );
    assert_result(
      CellValue::String("2".to_string()),
      "LEFT({a})",
      &data(2021.0, CHINESE_STRING),
    );
    assert_result(
      CellValue::String("20".to_string()),
      "LEFT({a}, 2)",
      &data(2021.0, CHINESE_STRING),
    );
    assert_error(
      &Strings::ParamsCountError.to_string(),
      "LEFT()",
      &data(0.0, CHINESE_STRING),
    );
  }

  #[test]
  fn test_function_test_right() {
    fn data(a: f64, b: &str) -> HashMap<String, CellValue> {
      vec![
        ("a".to_string(), CellValue::from(a)),
        ("b".to_string(), CellValue::from(b)),
      ]
      .into_iter()
      .collect()
    }

    assert_result(
      CellValue::String("".to_string()),
      "RIGHT({b}, {a})",
      &data(0.0, CHINESE_STRING),
    );
    assert_result(
      CellValue::String("技".to_string()),
      "RIGHT({b})",
      &data(0.0, CHINESE_STRING),
    );
    assert_result(
      CellValue::String("e科技".to_string()),
      "RIGHT({b}, 3)",
      &data(0.0, CHINESE_STRING),
    );
    assert_result(
      CellValue::String(CHINESE_STRING.to_string()),
      "RIGHT({b}, 33)",
      &data(0.0, CHINESE_STRING),
    );
    assert_result(
      CellValue::String("1".to_string()),
      "RIGHT({a})",
      &data(2021.0, CHINESE_STRING),
    );
    assert_result(
      CellValue::String("21".to_string()),
      "RIGHT({a}, 2)",
      &data(2021.0, CHINESE_STRING),
    );
    assert_error(
      &Strings::ParamsCountError.to_string(),
      "RIGHT()",
      &data(0.0, CHINESE_STRING),
    );
  }

  #[test]
  fn test_function_test_rept() {
    assert_result(
      CellValue::String("000".to_string()),
      "REPT({a}, 3)",
      &data(0.0, CHINESE_STRING),
    );
    assert_result(
      CellValue::String(
        "Welcome to join APITable TeamWelcome to join APITable TeamWelcome to join APITable Team".to_string(),
      ),
      "REPT({b}, 3)",
      &data(0.0, "Welcome to join APITable Team"),
    );
    assert_result(
      CellValue::String("科科科".to_string()),
      "REPT({d}, 3)",
      &data(0.0, CHINESE_STRING),
    );
    assert_error(
      &Strings::ParamsCountError.to_string(),
      "REPT({a})",
      &data(0.0, CHINESE_STRING),
    );
  }
}
