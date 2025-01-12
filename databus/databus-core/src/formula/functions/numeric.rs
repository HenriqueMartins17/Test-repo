use crate::fields::property::field_types::{BasicValueType, FormulaFuncType};
use crate::formula::errors::{Error, FormulaResult};
use crate::formula::functions::basic::{FormulaEvaluateContext, FormulaFunc, FormulaParam, DEFAULT_ACCEPT_VALUE_TYPE};
use crate::formula::i18n::Strings;
use crate::formula::parser::ast::AstNode;
use crate::params_i18n;
use crate::prelude::CellValue;
use std::cmp::max;

struct NumericFunc;
impl NumericFunc {
  fn get_type() -> FormulaFuncType {
    FormulaFuncType::Numeric
  }

  fn get_accept_value_type() -> Vec<BasicValueType> {
    DEFAULT_ACCEPT_VALUE_TYPE.to_vec()
  }
}

/// Too class for some public functions
struct NumericUtilsFunc;
impl NumericUtilsFunc {
  pub fn get_return_type(func: &impl FormulaFunc, params: &Vec<&AstNode>) -> FormulaResult<BasicValueType> {
    if !params.is_empty() {
      func.validate_params(params)?;
    }
    Ok(BasicValueType::Number)
  }

  /// CEILING, FLOOR
  pub fn calc_2_round_fc(params: &Vec<FormulaParam>, calc_fn: fn(f64) -> f64) -> CellValue {
    if let Some(param) = params.get(0) {
      if param.value.is_null() {
        return CellValue::Null;
      }
      let value = param.value.to_number();
      let sign = params.get(1);
      if let Some(sign) = sign {
        let sign = sign.value.to_number();
        let result = times(calc_fn(divide(value, sign)), sign);
        return CellValue::Number(result);
      }

      let result = calc_fn(value);
      return CellValue::Number(result);
    }

    return CellValue::Null;
  }

  /// ROUNDUP, ROUNDDOWN
  pub fn calc_2_round_du(params: &Vec<FormulaParam>, calc_fn1: fn(f64) -> f64, calc_fn2: fn(f64) -> f64) -> CellValue {
    if let Some(param) = params.get(0) {
      if param.value.is_null() {
        return CellValue::Null;
      }

      let value = param.value.to_number();
      let precision = params.get(1).map(|it| it.value.to_number()).unwrap_or(0.0);
      let offset = 10.0_f64.powf(precision);
      let round_fn = if value > 0.0 { calc_fn1 } else { calc_fn2 };

      let rounded = round_fn(value * offset) / offset;
      return CellValue::Number(rounded);
    }

    return CellValue::Null;
  }
}

/// Sum Function
pub struct SumFunc;
impl SumFunc {
  pub fn new() -> Self {
    Self
  }
}

impl FormulaFunc for SumFunc {
  fn get_type(&self) -> FormulaFuncType {
    NumericFunc::get_type()
  }

  fn get_accept_value_type(&self) -> Vec<BasicValueType> {
    let mut types = NumericFunc::get_accept_value_type();
    types.push(BasicValueType::Array);
    types
  }

  fn validate_params(&self, _params: &Vec<&AstNode>) -> FormulaResult<()> {
    Ok(())
  }

  fn get_return_type(&self, _params: &Vec<&AstNode>) -> FormulaResult<BasicValueType> {
    Ok(BasicValueType::Number)
  }

  fn func(&self, params: &Vec<FormulaParam>, _context: &FormulaEvaluateContext) -> FormulaResult<CellValue> {
    let mut result = 0.0;
    // If there is only one parameter and it is an array, it means that the value of the array type field is summed
    if first_param_is_array(params) {
      let param = &params[0];

      if let Some(inner_value_type) = param.node.get_inner_value_type() {
        if inner_value_type == &BasicValueType::DateTime {
          return Ok(CellValue::Number(0.0));
        }
      }

      if let CellValue::Array(arr) = &param.value {
        result = arr.iter().fold(0.0, |pre, cur| {
          return plus(pre, no_nan(cur.to_number()));
        });
      }
    } else {
      result = params.iter().fold(0.0, |pre, cur| {
        return plus(pre, no_nan(cur.value.to_number()));
      });
    }

    return Ok(CellValue::Number(result));
  }
}

/// Abs Function
pub struct AbsFunc;
impl AbsFunc {
  pub fn new() -> Self {
    Self
  }
}

impl FormulaFunc for AbsFunc {
  fn get_type(&self) -> FormulaFuncType {
    NumericFunc::get_type()
  }

  fn get_accept_value_type(&self) -> Vec<BasicValueType> {
    NumericFunc::get_accept_value_type()
  }

  fn validate_params(&self, _params: &Vec<&AstNode>) -> FormulaResult<()> {
    Ok(())
  }

  fn get_return_type(&self, params: &Vec<&AstNode>) -> FormulaResult<BasicValueType> {
    self.validate_params(params)?;
    Ok(BasicValueType::Number)
  }

  fn func(&self, params: &Vec<FormulaParam>, _context: &FormulaEvaluateContext) -> FormulaResult<CellValue> {
    if let Some(param) = params.get(0) {
      return Ok(CellValue::from(f64::abs(param.value.to_number())));
    }

    Ok(CellValue::from(f64::NAN))
  }
}

/// Sqrt Function
pub struct SqrtFunc;
impl SqrtFunc {
  pub fn new() -> Self {
    Self
  }
}

impl FormulaFunc for SqrtFunc {
  fn get_type(&self) -> FormulaFuncType {
    NumericFunc::get_type()
  }

  fn get_accept_value_type(&self) -> Vec<BasicValueType> {
    NumericFunc::get_accept_value_type()
  }

  fn validate_params(&self, params: &Vec<&AstNode>) -> FormulaResult<()> {
    if params.len() < 1 {
      return Err(Error::new(
        Strings::ParamsCountError.with_params(params_i18n!(paramsName = "SQRT", paramsCount = 1)),
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
    let value = &params[0].value;
    if value.is_null() {
      return Ok(CellValue::Null);
    }
    Ok(CellValue::Number(value.to_number().sqrt()))
  }
}

/// Mod Function
pub struct ModFunc;
impl ModFunc {
  pub fn new() -> Self {
    Self
  }
}

impl FormulaFunc for ModFunc {
  fn get_type(&self) -> FormulaFuncType {
    NumericFunc::get_type()
  }

  fn get_accept_value_type(&self) -> Vec<BasicValueType> {
    NumericFunc::get_accept_value_type()
  }

  fn validate_params(&self, params: &Vec<&AstNode>) -> FormulaResult<()> {
    if params.len() < 2 {
      return Err(Error::new(
        Strings::ParamsCountError.with_params(params_i18n!(paramsName = "MOD", paramsCount = 2)),
      ));
    }
    Ok(())
  }

  fn get_return_type(&self, params: &Vec<&AstNode>) -> FormulaResult<BasicValueType> {
    self.validate_params(params)?;
    Ok(BasicValueType::Number)
  }

  fn func(&self, params: &Vec<FormulaParam>, _context: &FormulaEvaluateContext) -> FormulaResult<CellValue> {
    let first = &params[0];
    if first.value.is_null() {
      return Ok(CellValue::Null);
    }
    let num = first.value.to_number();
    let divisor = params[1].value.to_number();
    let mod_result = num % divisor;
    if (num as i64 ^ divisor as i64) < 0 {
      return Ok(CellValue::Number(mod_result * (-1.0)));
    }
    return Ok(CellValue::Number(mod_result));
  }
}

/// Power Function
pub struct PowerFunc;
impl PowerFunc {
  pub fn new() -> Self {
    Self
  }
}

impl FormulaFunc for PowerFunc {
  fn get_type(&self) -> FormulaFuncType {
    NumericFunc::get_type()
  }

  fn get_accept_value_type(&self) -> Vec<BasicValueType> {
    NumericFunc::get_accept_value_type()
  }

  fn validate_params(&self, params: &Vec<&AstNode>) -> FormulaResult<()> {
    if params.len() < 2 {
      return Err(Error::new(
        Strings::ParamsCountError.with_params(params_i18n!(paramsName = "POWER", paramsCount = 2)),
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
    let first = &params[0];
    if first.value.is_null() {
      return Ok(CellValue::Null);
    }
    let num = first.value.to_number();
    let power = params[1].value.to_number();
    let result = num.powf(power);
    return Ok(CellValue::Number(result));
  }
}

/// Exp Function
pub struct ExpFunc;
impl ExpFunc {
  pub fn new() -> Self {
    Self
  }
}

impl FormulaFunc for ExpFunc {
  fn get_type(&self) -> FormulaFuncType {
    NumericFunc::get_type()
  }

  fn get_accept_value_type(&self) -> Vec<BasicValueType> {
    NumericFunc::get_accept_value_type()
  }

  fn validate_params(&self, params: &Vec<&AstNode>) -> FormulaResult<()> {
    if params.len() < 1 {
      return Err(Error::new(
        Strings::ParamsCountError.with_params(params_i18n!(paramsName = "EXP", paramsCount = 1)),
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
    let param = &params[0];
    if param.value.is_null() {
      return Ok(CellValue::Null);
    }
    let result = param.value.to_number().exp();
    return Ok(CellValue::Number(result));
  }
}

/// Log Function
pub struct LogFunc;
impl LogFunc {
  pub fn new() -> Self {
    Self
  }
}

impl FormulaFunc for LogFunc {
  fn get_type(&self) -> FormulaFuncType {
    NumericFunc::get_type()
  }

  fn get_accept_value_type(&self) -> Vec<BasicValueType> {
    NumericFunc::get_accept_value_type()
  }

  fn validate_params(&self, params: &Vec<&AstNode>) -> FormulaResult<()> {
    if params.len() < 1 {
      return Err(Error::new(
        Strings::ParamsCountError.with_params(params_i18n!(paramsName = "LOG", paramsCount = 1)),
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
    let value = &params[0].value;
    if value.is_null() {
      return Ok(CellValue::Null);
    }
    let num = value.to_number();
    let base = params.get(1).map(|it| it.value.to_number()).unwrap_or(10.0);
    let result = num.log(base);
    return Ok(CellValue::Number(result));

  }
}

/// Average Function
pub struct AverageFunc;
impl AverageFunc {
  pub fn new() -> Self {
    Self
  }
}

impl FormulaFunc for AverageFunc {
  fn get_type(&self) -> FormulaFuncType {
    NumericFunc::get_type()
  }

  fn get_accept_value_type(&self) -> Vec<BasicValueType> {
    let mut types = NumericFunc::get_accept_value_type();
    types.push(BasicValueType::Array);
    types
  }

  fn validate_params(&self, _params: &Vec<&AstNode>) -> FormulaResult<()> {
    Ok(())
  }

  fn get_return_type(&self, _params: &Vec<&AstNode>) -> FormulaResult<BasicValueType> {
    Ok(BasicValueType::Number)
  }

  fn func(&self, params: &Vec<FormulaParam>, _context: &FormulaEvaluateContext) -> FormulaResult<CellValue> {
    // If there is only one parameter and it is an array, it means that the value of the array type field is summed
    if first_param_is_array(params) {
      let param = &params[0];

      if let Some(inner_value_type) = param.node.get_inner_value_type() {
        if inner_value_type == &BasicValueType::DateTime {
          return Ok(CellValue::Number(0.0));
        }
      }

      if let CellValue::Array(arr) = &param.value {
        let sum: f64 = arr.into_iter().fold(0.0, |pre, cur| plus(pre, no_nan(cur.to_number())));

        let count = max(arr.len(), 1) as f64;
        return Ok(CellValue::Number(sum / count));
      }
    }

    let count = max(
      params
        .iter()
        .filter(|it| {
          if it.value.is_null() {
            return false;
          }
          return !it.value.to_number().is_nan();
        })
        .count(),
      1,
    ) as f64;

    let sum = params.iter().fold(0.0, |pre, cur| {
      return plus(pre, no_nan(cur.value.to_number()));
    });

    Ok(CellValue::Number(sum / count))
  }
}

/// Max Function
pub struct MaxFunc;
impl MaxFunc {
  pub fn new() -> Self {
    Self
  }

  fn calc(&self, params: &Vec<FormulaParam>, calc_fn: fn(&Vec<f64>) -> f64) -> CellValue {
    let mut params_ref: Vec<&FormulaParam> = params.iter().collect();

    if first_param_is_array(params) {
      return match &params[0].value {
        CellValue::Array(array) => {
          let v = calc_fn(&array.iter().map(|it| it.to_number()).filter(|d| !d.is_nan()).collect());
          CellValue::Number(v)
        }
        _ => CellValue::Null,
      };
    }

    let node_params = params.iter().map(|it| it.node).collect();
    if matches!(self.get_return_type(&node_params), Ok(v) if v != BasicValueType::DateTime) {
      params_ref = params
        .iter()
        .filter(|p| p.node.get_value_type() != &BasicValueType::DateTime)
        .collect();
    }

    let v = calc_fn(
      &params_ref
        .iter()
        .map(|it| it.value.to_number())
        .filter(|d| !d.is_nan())
        .collect(),
    );

    CellValue::Number(v)
  }
}

impl FormulaFunc for MaxFunc {
  fn get_type(&self) -> FormulaFuncType {
    NumericFunc::get_type()
  }

  fn get_accept_value_type(&self) -> Vec<BasicValueType> {
    let mut types = NumericFunc::get_accept_value_type();
    types.extend(vec![BasicValueType::DateTime, BasicValueType::Array]);
    types
  }

  fn validate_params(&self, _params: &Vec<&AstNode>) -> FormulaResult<()> {
    Ok(())
  }

  fn get_return_type(&self, params: &Vec<&AstNode>) -> FormulaResult<BasicValueType> {
    if params.is_empty() {
      return Ok(BasicValueType::Number);
    }

    if first_node_is_array(params) {
      return match params[0].get_inner_value_type() {
        Some(value_type) if value_type == &BasicValueType::DateTime => Ok(BasicValueType::DateTime),
        _ => Ok(BasicValueType::Number),
      };
    }

    // All parameters are date type, return date type
    if params.iter().all(|node| {
      let x = node.get_value_type() == &BasicValueType::DateTime;
      x
    }) {
      return Ok(BasicValueType::DateTime);
    }

    Ok(BasicValueType::Number)
  }

  fn func(&self, params: &Vec<FormulaParam>, _context: &FormulaEvaluateContext) -> FormulaResult<CellValue> {
    let value = self.calc(params, |numbers| {
      *numbers.iter().max_by(|a, b| a.partial_cmp(b).unwrap()).unwrap_or(&0.0)
    });
    Ok(value)
  }
}

/// Min Function
pub struct MinFunc {
  base: MaxFunc,
}
impl MinFunc {
  pub fn new() -> Self {
    Self { base: MaxFunc::new() }
  }
}

impl FormulaFunc for MinFunc {
  fn get_type(&self) -> FormulaFuncType {
    self.base.get_type()
  }

  fn get_accept_value_type(&self) -> Vec<BasicValueType> {
    self.base.get_accept_value_type()
  }

  fn validate_params(&self, params: &Vec<&AstNode>) -> FormulaResult<()> {
    self.base.validate_params(params)
  }

  fn get_return_type(&self, params: &Vec<&AstNode>) -> FormulaResult<BasicValueType> {
    self.base.get_return_type(params)
  }

  fn func(&self, params: &Vec<FormulaParam>, _context: &FormulaEvaluateContext) -> FormulaResult<CellValue> {
    let value = self.base.calc(params, |numbers| {
      *numbers.iter().min_by(|a, b| a.partial_cmp(b).unwrap()).unwrap_or(&0.0)
    });
    Ok(value)
  }
}

/// Round Function
pub struct RoundFunc;
impl RoundFunc {
  pub fn new() -> Self {
    Self
  }
}

impl FormulaFunc for RoundFunc {
  fn get_type(&self) -> FormulaFuncType {
    NumericFunc::get_type()
  }

  fn get_accept_value_type(&self) -> Vec<BasicValueType> {
    NumericFunc::get_accept_value_type()
  }

  fn validate_params(&self, params: &Vec<&AstNode>) -> FormulaResult<()> {
    if params.len() < 1 {
      return Err(Error::new(
        Strings::ParamsCountError.with_params(params_i18n!(paramsName = "ROUND", paramsCount = 1)),
      ));
    }
    Ok(())
  }

  fn get_return_type(&self, params: &Vec<&AstNode>) -> FormulaResult<BasicValueType> {
    self.validate_params(params)?;
    Ok(BasicValueType::Number)
  }

  fn func(&self, params: &Vec<FormulaParam>, _context: &FormulaEvaluateContext) -> FormulaResult<CellValue> {
    if let Some(num) = params.get(0) {
      let num = num.value.to_number();
      let precision = match params.get(1) {
        Some(precision) => precision.value.to_number().floor() as i32,
        None => 0,
      };

      let offset = 10_f64.powi(precision);
      let result = divide((num * offset).round(), offset);
      return Ok(CellValue::Number(result));
    }

    Ok(CellValue::Null)
  }
}

/// RoundUp Function
pub struct RoundUpFunc;
impl RoundUpFunc {
  pub fn new() -> Self {
    Self
  }
}

impl FormulaFunc for RoundUpFunc {
  fn get_type(&self) -> FormulaFuncType {
    NumericFunc::get_type()
  }

  fn get_accept_value_type(&self) -> Vec<BasicValueType> {
    NumericFunc::get_accept_value_type()
  }

  fn validate_params(&self, params: &Vec<&AstNode>) -> FormulaResult<()> {
    if params.len() < 1 {
      return Err(Error::new(
        Strings::ParamsCountError.with_params(params_i18n!(paramsName = "ROUNDUP", paramsCount = 1)),
      ));
    }
    Ok(())
  }

  fn get_return_type(&self, params: &Vec<&AstNode>) -> FormulaResult<BasicValueType> {
    NumericUtilsFunc::get_return_type(self, params)
  }

  fn func(&self, params: &Vec<FormulaParam>, _context: &FormulaEvaluateContext) -> FormulaResult<CellValue> {
    let result = NumericUtilsFunc::calc_2_round_du(params, |it| it.ceil(), |it| it.floor());
    Ok(result)
  }
}

/// RoundDown Function
pub struct RoundDownFunc;
impl RoundDownFunc {
  pub fn new() -> Self {
    Self
  }
}

impl FormulaFunc for RoundDownFunc {
  fn get_type(&self) -> FormulaFuncType {
    NumericFunc::get_type()
  }

  fn get_accept_value_type(&self) -> Vec<BasicValueType> {
    NumericFunc::get_accept_value_type()
  }

  fn validate_params(&self, params: &Vec<&AstNode>) -> FormulaResult<()> {
    if params.len() < 1 {
      return Err(Error::new(
        Strings::ParamsCountError.with_params(params_i18n!(paramsName = "ROUNDDOWN", paramsCount = 1)),
      ));
    }
    Ok(())
  }

  fn get_return_type(&self, params: &Vec<&AstNode>) -> FormulaResult<BasicValueType> {
    NumericUtilsFunc::get_return_type(self, params)
  }

  fn func(&self, params: &Vec<FormulaParam>, _context: &FormulaEvaluateContext) -> FormulaResult<CellValue> {
    let result = NumericUtilsFunc::calc_2_round_du(params, |it| it.floor(), |it| it.ceil());
    Ok(result)
  }
}

/// Ceiling Function
pub struct CeilingFunc;
impl CeilingFunc {
  pub fn new() -> Self {
    Self
  }
}

impl FormulaFunc for CeilingFunc {
  fn get_type(&self) -> FormulaFuncType {
    NumericFunc::get_type()
  }

  fn get_accept_value_type(&self) -> Vec<BasicValueType> {
    NumericFunc::get_accept_value_type()
  }

  fn validate_params(&self, params: &Vec<&AstNode>) -> FormulaResult<()> {
    if params.len() < 1 {
      return Err(Error::new(
        Strings::ParamsCountError.with_params(params_i18n!(paramsName = "CEILING", paramsCount = 1)),
      ));
    }
    Ok(())
  }

  fn get_return_type(&self, params: &Vec<&AstNode>) -> FormulaResult<BasicValueType> {
    NumericUtilsFunc::get_return_type(self, params)
  }

  fn func(&self, params: &Vec<FormulaParam>, _context: &FormulaEvaluateContext) -> FormulaResult<CellValue> {
    let result = NumericUtilsFunc::calc_2_round_fc(params, |it| it.ceil());
    Ok(result)
  }
}

pub struct FloorFunc;
impl FloorFunc {
  pub fn new() -> Self {
    Self
  }
}

impl FormulaFunc for FloorFunc {
  fn get_type(&self) -> FormulaFuncType {
    NumericFunc::get_type()
  }

  fn get_accept_value_type(&self) -> Vec<BasicValueType> {
    NumericFunc::get_accept_value_type()
  }

  fn validate_params(&self, params: &Vec<&AstNode>) -> FormulaResult<()> {
    if params.len() < 1 {
      return Err(Error::new(
        Strings::ParamsCountError.with_params(params_i18n!(paramsName = "FLOOR", paramsCount = 1)),
      ));
    }
    Ok(())
  }

  fn get_return_type(&self, params: &Vec<&AstNode>) -> FormulaResult<BasicValueType> {
    NumericUtilsFunc::get_return_type(self, params)
  }

  fn func(&self, params: &Vec<FormulaParam>, _context: &FormulaEvaluateContext) -> FormulaResult<CellValue> {
    let result = NumericUtilsFunc::calc_2_round_fc(params, |it| it.floor());
    Ok(result)
  }
}

/// Even Function
pub struct EvenFunc;
impl EvenFunc {
  pub fn new() -> Self {
    Self
  }
}

impl FormulaFunc for EvenFunc {
  fn get_type(&self) -> FormulaFuncType {
    NumericFunc::get_type()
  }

  fn get_accept_value_type(&self) -> Vec<BasicValueType> {
    NumericFunc::get_accept_value_type()
  }

  fn validate_params(&self, params: &Vec<&AstNode>) -> FormulaResult<()> {
    if params.len() < 1 {
      return Err(Error::new(
        Strings::ParamsCountError.with_params(params_i18n!(paramsName = "EVEN", paramsCount = 1)),
      ));
    }
    Ok(())
  }

  fn get_return_type(&self, params: &Vec<&AstNode>) -> FormulaResult<BasicValueType> {
    if !params.is_empty() {
      self.validate_params(params)?
    }
    Ok(BasicValueType::Number)
  }

  fn func(&self, params: &Vec<FormulaParam>, _context: &FormulaEvaluateContext) -> FormulaResult<CellValue> {
    let param = &params[0];
    if param.value.is_null() {
      return Ok(CellValue::Null);
    }
    let num = param.value.to_number();
    let rounded = if num > 0.0 { num.ceil() } else { num.floor() };
    if rounded % 2.0 == 0.0 {
      return Ok(CellValue::Number(rounded));
    }

    let result = if rounded > 0.0 { rounded + 1.0 } else { rounded - 1.0 };
    Ok(CellValue::Number(result))
  }
}

/// Odd Function
pub struct OddFunc;
impl OddFunc {
  pub fn new() -> Self {
    Self
  }
}

impl FormulaFunc for OddFunc {
  fn get_type(&self) -> FormulaFuncType {
    NumericFunc::get_type()
  }

  fn get_accept_value_type(&self) -> Vec<BasicValueType> {
    NumericFunc::get_accept_value_type()
  }

  fn validate_params(&self, params: &Vec<&AstNode>) -> FormulaResult<()> {
    if params.len() < 1 {
      return Err(Error::new(
        Strings::ParamsCountError.with_params(params_i18n!(paramsName = "ODD", paramsCount = 1)),
      ));
    }
    Ok(())
  }

  fn get_return_type(&self, params: &Vec<&AstNode>) -> FormulaResult<BasicValueType> {
    if !params.is_empty() {
      self.validate_params(params)?
    }
    Ok(BasicValueType::Number)
  }

  fn func(&self, params: &Vec<FormulaParam>, _context: &FormulaEvaluateContext) -> FormulaResult<CellValue> {
    let param = &params[0];
    if param.value.is_null() {
      return Ok(CellValue::Null);
    }
    let num = param.value.to_number();
    let rounded = if num > 0.0 { num.ceil() } else { num.floor() };
    if rounded % 2.0 != 0.0 {
      return Ok(CellValue::Number(rounded));
    }

    let result = if rounded >= 0.0 { rounded + 1.0 } else { rounded - 1.0 };
    Ok(CellValue::Number(result))
  }
}

/// Int Function
pub struct IntFunc;
impl IntFunc {
  pub fn new() -> Self {
    Self
  }
}

impl FormulaFunc for IntFunc {
  fn get_type(&self) -> FormulaFuncType {
    NumericFunc::get_type()
  }

  fn get_accept_value_type(&self) -> Vec<BasicValueType> {
    NumericFunc::get_accept_value_type()
  }

  fn validate_params(&self, params: &Vec<&AstNode>) -> FormulaResult<()> {
    if params.len() < 1 {
      return Err(Error::new(
        Strings::ParamsCountError.with_params(params_i18n!(paramsName = "INT", paramsCount = 1)),
      ));
    }
    Ok(())
  }

  fn get_return_type(&self, params: &Vec<&AstNode>) -> FormulaResult<BasicValueType> {
    self.validate_params(params)?;
    Ok(BasicValueType::Number)
  }

  fn func(&self, params: &Vec<FormulaParam>, _context: &FormulaEvaluateContext) -> FormulaResult<CellValue> {
    if let Some(num) = params.get(0) {
      let num = num.value.to_number();
      let result = num.floor();
      return Ok(CellValue::Number(result));
    }

    Ok(CellValue::Null)
  }
}

/// Value Function
pub struct ValueFunc;
impl ValueFunc {
  pub fn new() -> Self {
    Self
  }
}

impl FormulaFunc for ValueFunc {
  fn get_type(&self) -> FormulaFuncType {
    NumericFunc::get_type()
  }

  fn get_accept_value_type(&self) -> Vec<BasicValueType> {
    NumericFunc::get_accept_value_type()
  }

  fn validate_params(&self, params: &Vec<&AstNode>) -> FormulaResult<()> {
    if params.len() < 1 {
      return Err(Error::new(
        Strings::ParamsCountError.with_params(params_i18n!(paramsName = "VALUE", paramsCount = 1)),
      ));
    }
    Ok(())
  }

  fn get_return_type(&self, params: &Vec<&AstNode>) -> FormulaResult<BasicValueType> {
    self.validate_params(params)?;
    Ok(BasicValueType::Number)
  }

  fn func(&self, params: &Vec<FormulaParam>, _context: &FormulaEvaluateContext) -> FormulaResult<CellValue> {
    if let Some(first) = params.get(0) {
      let text = first.value.to_string();

      let reg_number = regex::Regex::new(r"[^0-9.+-]").unwrap();
      let reg_symbol = regex::Regex::new(r"(\+|-|\.)+").unwrap();

      let text = reg_number.replace_all(&text, "");
      let text = reg_symbol.replace_all(&text, "$1");

      let value = text.to_string().parse::<f64>().unwrap_or(0.0);
      return Ok(CellValue::Number(value));
    }

    Ok(CellValue::Number(0.0))
  }
}

pub fn first_param_is_array(params: &Vec<FormulaParam>) -> bool {
  if params.len() != 1 {
    return false;
  }
  if params[0].node.get_value_type() == &BasicValueType::Array {
    return true;
  }
  return false;
}

fn first_node_is_array(nodes: &Vec<&AstNode>) -> bool {
  if nodes.len() != 1 {
    return false;
  }
  if nodes[0].get_value_type() == &BasicValueType::Array {
    return true;
  }
  return false;
}

pub fn no_nan(n: f64) -> f64 {
  if n.is_nan() {
    0.0
  } else {
    n
  }
}

fn digit_length(num: f64) -> usize {
  let num = num.to_string();
  let e_split: Vec<&str> = num.split('e').collect();
  let d_len = match e_split[0].find('.') {
    Some(pos) => e_split[0][(pos + 1)..].len(),
    None => 0,
  };

  let power: isize = if e_split.len() > 1 {
    e_split[1].parse().unwrap_or(0)
  } else {
    0
  };

  let len = d_len as isize - power;
  if len > 0 {
    len as usize
  } else {
    0
  }
}

fn float_to_fixed(num: f64) -> f64 {
  if !num.to_string().contains('e') {
    return num.to_string().replace(".", "").parse::<f64>().unwrap_or(0.0);
  }
  let d_len = digit_length(num);
  if d_len > 0 {
    return (num * 10_f64.powi(d_len as i32)).round();
  }
  num
}

fn times(num1: f64, num2: f64) -> f64 {
  let int_num1 = float_to_fixed(num1);
  let int_num2 = float_to_fixed(num2);
  let base_num = digit_length(num1) + digit_length(num2);
  let dividend = int_num1 * int_num2;
  dividend / 10_f64.powi(base_num as i32)
}

fn divide(num1: f64, num2: f64) -> f64 {
  let int_num1 = float_to_fixed(num1);
  let int_num2 = float_to_fixed(num2);
  let base_num = digit_length(num2) as i32 - digit_length(num1) as i32;
  let dividend = int_num1 / int_num2;
  times(dividend, 10_f64.powi(base_num))
}

fn plus(num1: f64, num2: f64) -> f64 {
  let digit_len = digit_length(num1).max(digit_length(num2)) as i32;
  let base = 10_f64.powi(digit_len);
  (times(num1, base) + times(num2, base)) / base
}

#[cfg(test)]
mod tests {
  use std::collections::HashMap;

  use crate::fields::property::SingleSelectProperty;
  use crate::formula::helper::tests::{test_assert_error, test_assert_result};
  use crate::formula::i18n::Strings;
  use crate::formula::types::IField;
  use crate::prelude::{CellValue, TextValue};

  fn transform(
    field_map: HashMap<String, IField>,
    mut record_data: HashMap<String, CellValue>,
  ) -> (HashMap<String, IField>, HashMap<String, CellValue>) {
    let field_map = field_map
      .into_iter()
      .map(|(id, field)| {
        // string to text
        let field = if let IField::Text(field) = field {
          if let Some(text) = record_data.get(&id) {
            record_data.insert(
              id.clone(),
              CellValue::from(vec![CellValue::Text(TextValue {
                r#type: 1,
                text: text.to_string(),
              })]),
            );
          }

          IField::Text(field)
        }
        // array to multiselect
        else if let IField::MultiSelect(mut field) = field {
          if let Some(data) = record_data.get(&id) {
            if let Some(arr) = data.as_string_array() {
              let options = arr
                .into_iter()
                .map(|it| SingleSelectProperty {
                  id: it.to_string(),
                  name: it.to_string(),
                  color: 0,
                })
                .collect();

              field.property.options = options;
            } else {
              panic!("invalid data");
            }
          }

          IField::MultiSelect(field)
        }
        // ignore
        else {
          field
        };

        (id, field)
      })
      .collect();

    (field_map, record_data)
  }

  fn assert_result(expected: CellValue, expression: &str, record_data: &HashMap<String, CellValue>) {
    test_assert_result(expected, expression, record_data, &HashMap::new(), Some(transform))
  }

  fn assert_error(expected: &str, expression: &str, record_data: &HashMap<String, CellValue>) {
    test_assert_error(expected, expression, record_data, &HashMap::new(), Some(transform));
  }

  #[test]
  fn test_numeric_function_test_sum() {
    let record_data = vec![
      ("b".to_string(), CellValue::from("456")),
      ("d".to_string(), CellValue::from(vec!["0", "2", "3"])),
    ]
    .into_iter()
    .collect();

    assert_result(CellValue::Number(6.0), "SUM(1, 2, 3)", &record_data);
    assert_result(CellValue::Number(2.03), "SUM(1.01, 1.02)", &record_data);
    assert_result(CellValue::Number(5.0), "SUM({d})", &record_data);
    assert_result(CellValue::Number(0.0), "SUM()", &record_data);
    assert_result(CellValue::Number(456.0), "SUM({d}, {b})", &record_data);
  }

  #[test]
  fn test_numeric_function_test_abs() {
    fn data(a: f64, d: Vec<&str>) -> HashMap<String, CellValue> {
      vec![
        ("a".to_string(), CellValue::from(a)),
        ("d".to_string(), CellValue::from(d)),
      ]
      .into_iter()
      .collect()
    }

    assert_result(CellValue::from(1.0), "ABS(1, 2, 3)", &data(0.0, vec!["x", "y"]));
    assert_result(CellValue::from(1.0), "ABS({a})", &data(-1.0, vec!["x", "y"]));
    assert_error("NaN", "ABS()", &data(0.0, vec!["x", "y"]));
    assert_error("NaN", "ABS({d}, {d})", &data(0.0, vec!["x", "y"]));
  }

  #[test]
  fn test_numeric_function_test_sqrt() {
    fn data(a: f64) -> HashMap<String, CellValue> {
      vec![("a".to_string(), CellValue::from(a))].into_iter().collect()
    }

    assert_result(CellValue::Number(100.0), "SQRT({a})", &data(10000.0));
    assert_error("NaN", "SQRT({a})", &data(-10000.0));
    assert_error(&Strings::ParamsCountError.to_string(), "SQRT()", &data(1.0));
  }

  #[test]
  fn test_numeric_function_test_mod() {
    fn data(a: f64, b: f64) -> HashMap<String, CellValue> {
      vec![
        ("a".to_string(), CellValue::from(a)),
        ("b".to_string(), CellValue::from(b.to_string())),
      ]
      .into_iter()
      .collect()
    }

    assert_result(CellValue::Number(1.0), "MOD({a}, {b})", &data(5.0, 2.0));
    assert_result(CellValue::Number(0.0), "MOD({a}, {b})", &data(5.0, 5.0));
    assert_result(CellValue::Number(1.0), "MOD({a}, {b})", &data(-5.0, 2.0));
    assert_error(&Strings::ParamsCountError.to_string(), "MOD({a})", &data(1.0, 0.0));
  }

  #[test]
  fn test_numeric_function_test_power() {
    fn data(a: f64, b: f64) -> HashMap<String, CellValue> {
      vec![
        ("a".to_string(), CellValue::from(a)),
        ("b".to_string(), CellValue::from(b.to_string())),
      ]
      .into_iter()
      .collect()
    }

    assert_result(CellValue::Number(100.0), "POWER({a}, {b})", &data(10.0, 2.0));
    assert_result(CellValue::Number(0.01), "POWER({a}, {b})", &data(10.0, -2.0));
    assert_result(CellValue::Number(-1000.0), "POWER({a}, {b})", &data(-10.0, 3.0));
    assert_error(&Strings::ParamsCountError.to_string(), "POWER({a})", &data(1.0, 0.0));
  }

  #[test]
  fn test_numeric_function_test_exp() {
    assert_result(CellValue::Number(std::f64::consts::E), "EXP(1)", &HashMap::new());
    assert_result(CellValue::Number(1.0), "EXP(0)", &HashMap::new());
    assert_error(&Strings::ParamsCountError.to_string(), "EXP()", &HashMap::new());
  }

  #[test]
  fn test_numeric_function_test_log() {
    assert_result(CellValue::Number(10.0), "LOG(1024, 2)", &HashMap::new());
    assert_result(CellValue::Number(4.0), "LOG(10000)", &HashMap::new());
    assert_error(&Strings::ParamsCountError.to_string(), "LOG()", &HashMap::new());
  }

  #[test]
  fn test_numeric_function_test_average() {
    fn data(d: Vec<&str>) -> HashMap<String, CellValue> {
      vec![
        ("a".to_string(), CellValue::from(0.0)),
        ("b".to_string(), CellValue::from("8")),
        ("c".to_string(), CellValue::from(1591414562369.0)),
        ("d".to_string(), CellValue::from(d)),
      ]
      .into_iter()
      .collect()
    }

    assert_result(CellValue::Number(2.0), "AVERAGE(1, 2, 3)", &data(vec![]));
    assert_result(CellValue::Number(1.015), "AVERAGE(1.01, 1.02)", &data(vec![]));
    assert_result(CellValue::Number(1.5), "AVERAGE(1, {d}, 2)", &data(vec!["x", "y"]));
    // sum of array types
    assert_result(CellValue::Number(2.0), "AVERAGE({d})", &data(vec!["1", "2", "3"]));
    assert_result(CellValue::Number(0.0), "AVERAGE()", &data(vec![]));
    assert_result(CellValue::Number(4.0), "AVERAGE(a, b)", &data(vec![]));
    assert_result(CellValue::Number(0.0), "AVERAGE(c)", &data(vec![]));
  }

  #[test]
  fn test_numeric_function_test_round() {
    fn data(a: f64, b: f64) -> HashMap<String, CellValue> {
      vec![
        ("a".to_string(), CellValue::from(a)),
        ("b".to_string(), CellValue::from(b.to_string())),
      ]
      .into_iter()
      .collect()
    }

    assert_result(CellValue::Number(1.0), "ROUND(a)", &data(1.49, 0.0));
    assert_result(CellValue::Number(1.0), "ROUND(a, b)", &data(1.49, 0.0));
    assert_result(CellValue::Number(2.0), "ROUND(a, b)", &data(1.99, 0.0));
    // TODO: different from TS
    assert_result(CellValue::Number(-1.6), "ROUND(a, b)", &data(-1.55, 1.0));
    assert_result(CellValue::Number(-1.5), "ROUND(a, b)", &data(-1.49, 1.0));
    assert_result(CellValue::Number(1.5), "ROUND(a, b)", &data(1.49, 1.2));
    assert_result(CellValue::Number(1.5), "ROUND(a, b)", &data(1.49, 1.9));
    // TODO: different from TS, `65.115 == 65.114999999999995`
    assert_result(CellValue::Number(65.11), "ROUND(a, b)", &data(65.115, 2.0));
    assert_error(&Strings::ParamsCountError.to_string(), "ROUND()", &data(1.49, 0.0));
  }

  #[test]
  fn test_numeric_function_test_round_up() {
    fn data(a: f64, b: f64) -> HashMap<String, CellValue> {
      vec![
        ("a".to_string(), CellValue::from(a)),
        ("b".to_string(), CellValue::from(b.to_string())),
      ]
      .into_iter()
      .collect()
    }

    assert_result(CellValue::Number(2.0), "ROUNDUP({a})", &data(1.11, 0.0));
    assert_result(CellValue::Number(2.0), "ROUNDUP({a}, {b})", &data(1.11, 0.0));
    assert_result(CellValue::Number(1.2), "ROUNDUP({a}, {b})", &data(1.11, 1.0));
    assert_result(CellValue::Number(-2.0), "ROUNDUP({a}, {b})", &data(-1.11, 0.0));
    assert_error(&Strings::ParamsCountError.to_string(), "ROUNDUP()", &data(0.0, 0.0));
  }

  #[test]
  fn test_numeric_function_test_round_down() {
    fn data(a: f64, b: f64) -> HashMap<String, CellValue> {
      vec![
        ("a".to_string(), CellValue::from(a)),
        ("b".to_string(), CellValue::from(b.to_string())),
      ]
      .into_iter()
      .collect()
    }

    assert_result(CellValue::Number(1.0), "ROUNDDOWN({a})", &data(1.11, 0.0));
    assert_result(CellValue::Number(1.0), "ROUNDDOWN({a}, {b})", &data(1.11, 0.0));
    assert_result(CellValue::Number(1.1), "ROUNDDOWN({a}, {b})", &data(1.11, 1.0));
    assert_result(CellValue::Number(-1.0), "ROUNDDOWN({a}, {b})", &data(-1.11, 0.0));
    assert_error(&Strings::ParamsCountError.to_string(), "ROUNDDOWN()", &data(0.0, 0.0));
  }

  #[test]
  fn test_numeric_function_test_ceiling() {
    let data = HashMap::new();

    assert_result(CellValue::Number(2.0), "CEILING(1.01)", &data);
    assert_result(CellValue::Number(1.1), "CEILING(1.01, 0.1)", &data);
    assert_result(CellValue::Number(1.2), "CEILING(1.01, 0.2)", &data);
    assert_result(CellValue::Number(1.01), "CEILING(1.01, 0.00001)", &data);
    assert_result(CellValue::Number(120.0), "CEILING(111.01, 10)", &data);
    assert_error(&Strings::ParamsCountError.to_string(), "CEILING()", &data);
  }

  #[test]
  fn test_numeric_function_test_floor() {
    let data = HashMap::new();

    assert_result(CellValue::Number(1.0), "FLOOR(1.01)", &data);
    assert_result(CellValue::Number(1.1), "FLOOR(1.11, 0.1)", &data);
    assert_result(CellValue::Number(0.8938), "FLOOR(1.111111, 0.22345)", &data);
    assert_result(CellValue::Number(1.01), "FLOOR(1.01, 0.00001)", &data);
    assert_result(CellValue::Number(110.0), "FLOOR(111.01, 10)", &data);
    assert_error(&Strings::ParamsCountError.to_string(), "FLOOR()", &data);
  }

  #[test]
  fn test_numeric_function_test_even() {
    let data = HashMap::new();

    assert_result(CellValue::Number(4.0), "EVEN(3.1)", &data);
    assert_result(CellValue::Number(2.0), "EVEN(0.1)", &data);
    assert_result(CellValue::Number(-4.0), "EVEN(-2.9)", &data);
    assert_result(CellValue::Number(-4.0), "EVEN(-3.1)", &data);
    assert_result(CellValue::Number(0.0), "EVEN(0)", &data);
    assert_error(&Strings::ParamsCountError.to_string(), "EVEN()", &data);
  }

  #[test]
  fn test_numeric_function_test_odd() {
    let data = HashMap::new();

    assert_result(CellValue::Number(3.0), "ODD(1.9)", &data);
    assert_result(CellValue::Number(3.0), "ODD(2.1)", &data);
    assert_result(CellValue::Number(-3.0), "ODD(-1.9)", &data);
    assert_result(CellValue::Number(-3.0), "ODD(-2.1)", &data);
    assert_result(CellValue::Number(1.0), "ODD(0)", &data);
    assert_error(&Strings::ParamsCountError.to_string(), "ODD()", &data);
  }

  #[test]
  fn test_numeric_function_test_max() {
    let record_data = vec![
      ("a".to_string(), CellValue::from(0.0)),
      ("b".to_string(), CellValue::from("8")),
      ("c".to_string(), CellValue::from(1591414562369f64)),
      ("d".to_string(), CellValue::from(vec!["1", "2", "3"])),
      ("e".to_string(), CellValue::from(1691414562369f64)),
    ]
    .into_iter()
    .collect();

    assert_result(CellValue::Number(3.0), "MAX(1, 2, 3)", &record_data);
    // array type
    assert_result(CellValue::Number(3.0), "MAX({d})", &record_data);
    assert_result(CellValue::Number(0.0), "MAX()", &record_data);

    // With string
    let mut data = HashMap::new();
    data.extend(record_data.clone());
    data.insert("b".to_string(), CellValue::from("xx"));
    assert_result(CellValue::Number(0.0), "MAX({a}, {b}, -1)", &data);

    assert_result(CellValue::Number(1591414562369f64), "MAX({c})", &record_data);

    // Multiple datetimes can participate in the calculation
    assert_result(CellValue::Number(1691414562369f64), "MAX({c}, {e})", &record_data);

    // datetime + number, then filter out datetime
    assert_result(CellValue::Number(0.0), "MAX({c}, {e}, {a})", &record_data);
    assert_result(CellValue::Number(8.0), "MAX({c}, {e}, {b})", &record_data);
  }

  #[test]
  fn test_numeric_function_test_min() {
    let record_data = vec![
      ("a".to_string(), CellValue::from(0.0)),
      ("b".to_string(), CellValue::from("8")),
      ("c".to_string(), CellValue::from(1591414562369f64)),
      ("d".to_string(), CellValue::from(vec!["1", "2", "3"])),
      ("e".to_string(), CellValue::from(1691414562369f64)),
    ]
    .into_iter()
    .collect();

    assert_result(CellValue::Number(1.0), "MIN(1, 2, 3)", &record_data);
    // array type
    assert_result(CellValue::Number(1.0), "MIN({d})", &record_data);
    assert_result(CellValue::Number(0.0), "MIN()", &record_data);
    assert_result(CellValue::Number(-1.0), "MIN({a}, {b}, -1)", &record_data);
    assert_result(CellValue::Number(1591414562369f64), "MIN({c})", &record_data);

    // Multiple datetimes can participate in the calculation
    assert_result(CellValue::Number(1591414562369f64), "MIN({c}, {e})", &record_data);

    // datetime + number, then filter out datetime
    assert_result(CellValue::Number(0.0), "MIN({c}, {e}, {a})", &record_data);

    assert_result(CellValue::Number(8.0), "MIN({c}, {e}, {b})", &record_data);
  }

  #[test]
  fn test_numeric_function_test_int() {
    let record_data = HashMap::new();
    assert_result(CellValue::Number(1.0), "INT(1.01)", &record_data);
    assert_result(CellValue::Number(-2.0), "INT(-1.11)", &record_data);
    assert_error(&Strings::ParamsCountError.to_string(), "INT()", &record_data);
  }

  #[test]
  fn test_numeric_function_test_value() {
    fn data(b: &str) -> HashMap<String, CellValue> {
      vec![("b".to_string(), CellValue::from(b))].into_iter().collect()
    }

    assert_result(CellValue::Number(456.0), "VALUE({b})", &data("456"));
    assert_result(CellValue::Number(-456.0), "VALUE({b})", &data("-456"));
    assert_result(CellValue::Number(456.0), "VALUE({b})", &data("ss456"));
    assert_result(CellValue::Number(456.123), "VALUE({b})", &data("ss456.123"));
    assert_error(&Strings::ParamsCountError.to_string(), "VALUE()", &data("456"))
  }
}
