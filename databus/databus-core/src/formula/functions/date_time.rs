use chrono::{
  Datelike, DateTime, Days, Local, LocalResult, NaiveDate, NaiveDateTime, NaiveTime, Timelike, TimeZone, Weekday,
};

use crate::fields::property::field_types::{BasicValueType, FormulaFuncType};
use crate::formula::errors::{Error, FormulaResult};
use crate::formula::functions::basic::{DEFAULT_ACCEPT_VALUE_TYPE, FormulaEvaluateContext, FormulaFunc, FormulaParam};
use crate::formula::i18n::Strings;
use crate::formula::parser::AstNode;
use crate::params_i18n;
use crate::prelude::CellValue;
use crate::utils::string::date_str_replace_cn;

fn get_index_by_weekday_name(name: &str) -> u32 {
  return match name.to_lowercase().as_str() {
    "sunday" => 0,
    "monday" => 1,
    "tuesday" => 2,
    "wednesday" => 3,
    "thursday" => 4,
    "friday" => 5,
    "saturday" => 6,
    _ => 0,
  };
}

pub fn get_day_js(time_stamp: &CellValue) -> FormulaResult<DateTime<Local>> {
  // TODO follow-up and lookup synchronous transformation (the timeStamp of string should not be passed in)
  if time_stamp.is_null() {
    return Err(Error::new("timeStamp is null"));
  }

  let is_timestamp = !time_stamp.is_string() || !time_stamp.to_number().is_nan();

  return if is_timestamp {
    match Local.timestamp_millis_opt(time_stamp.to_number() as i64) {
      LocalResult::Single(v) => Ok(v),
      _ => Err(Error::new("date parse error")),
    }
  } else {
    parse_date_time(&date_str_replace_cn(&time_stamp.to_string()))
  };
}

fn parse_date_time(date_str: &str) -> FormulaResult<DateTime<Local>> {
  return if let Ok(dt) = NaiveDateTime::parse_from_str(date_str, "%Y/%-m/%-d %H:%M:%S") {
    match Local.from_local_datetime(&dt) {
      LocalResult::Single(dt) => Ok(dt),
      _ => Err(Error::new("No valid local datetime")),
    }
  } else if let Ok(dt) = NaiveDate::parse_from_str(date_str, "%Y/%-m/%-d") {
    match Local.from_local_datetime(&dt.and_time(NaiveTime::from_hms_opt(0, 0, 0).unwrap())) {
      LocalResult::Single(dt) => Ok(dt),
      _ => Err(Error::new("No valid local datetime")),
    }
  } else {
    Err(Error::new("Unexpected date format"))
  };
}

struct DateTimeFunc;
impl DateTimeFunc {
  fn get_type() -> FormulaFuncType {
    FormulaFuncType::Logical
  }

  fn get_accept_value_type() -> Vec<BasicValueType> {
    let mut types = DEFAULT_ACCEPT_VALUE_TYPE.to_vec();
    types.push(BasicValueType::DateTime);
    types
  }
}

/// Year Function
pub struct YearFunc;

impl YearFunc {
  pub fn new() -> Self {
    Self
  }
}

impl FormulaFunc for YearFunc {
  fn get_type(&self) -> FormulaFuncType {
    DateTimeFunc::get_type()
  }

  fn get_accept_value_type(&self) -> Vec<BasicValueType> {
    DateTimeFunc::get_accept_value_type()
  }

  fn validate_params(&self, params: &Vec<&AstNode>) -> FormulaResult<()> {
    if params.len() < 1 {
      return Err(Error::new(
        Strings::ParamsCountError.with_params(params_i18n!(paramsName = "YEAR", paramsCount = 1)),
      ));
    }
    Ok(())
  }

  fn get_return_type(&self, params: &Vec<&AstNode>) -> FormulaResult<BasicValueType> {
    self.validate_params(params)?;
    Ok(BasicValueType::Number)
  }

  fn func(&self, params: &Vec<FormulaParam>, _context: &FormulaEvaluateContext) -> FormulaResult<CellValue> {
    let result = get_day_js(&params[0].value)?;
    Ok(CellValue::Number(result.year() as f64))
  }
}

/// Month Function
pub struct MonthFunc;

impl MonthFunc {
  pub fn new() -> Self {
    Self
  }
}

impl FormulaFunc for MonthFunc {
  fn get_type(&self) -> FormulaFuncType {
    DateTimeFunc::get_type()
  }

  fn get_accept_value_type(&self) -> Vec<BasicValueType> {
    DateTimeFunc::get_accept_value_type()
  }

  fn validate_params(&self, params: &Vec<&AstNode>) -> FormulaResult<()> {
    if params.len() < 1 {
      return Err(Error::new(
        Strings::ParamsCountError.with_params(params_i18n!(paramsName = "MONTH", paramsCount = 1)),
      ));
    }
    Ok(())
  }

  fn get_return_type(&self, params: &Vec<&AstNode>) -> FormulaResult<BasicValueType> {
    self.validate_params(params)?;
    Ok(BasicValueType::Number)
  }

  fn func(&self, params: &Vec<FormulaParam>, _context: &FormulaEvaluateContext) -> FormulaResult<CellValue> {
    let result = get_day_js(&params[0].value)?;
    Ok(CellValue::Number(result.month() as f64))
  }
}

/// Day Function
pub struct DayFunc;

impl DayFunc {
  pub fn new() -> Self {
    Self
  }
}

impl FormulaFunc for DayFunc {
  fn get_type(&self) -> FormulaFuncType {
    DateTimeFunc::get_type()
  }

  fn get_accept_value_type(&self) -> Vec<BasicValueType> {
    DateTimeFunc::get_accept_value_type()
  }

  fn validate_params(&self, params: &Vec<&AstNode>) -> FormulaResult<()> {
    if params.len() < 1 {
      return Err(Error::new(
        Strings::ParamsCountError.with_params(params_i18n!(paramsName = "DAY", paramsCount = 1)),
      ));
    }
    Ok(())
  }

  fn get_return_type(&self, params: &Vec<&AstNode>) -> FormulaResult<BasicValueType> {
    self.validate_params(params)?;
    Ok(BasicValueType::Number)
  }

  fn func(&self, params: &Vec<FormulaParam>, _context: &FormulaEvaluateContext) -> FormulaResult<CellValue> {
    let result = get_day_js(&params[0].value)?;
    Ok(CellValue::Number(result.day() as f64))
  }
}

/// Hour Function
pub struct HourFunc;

impl HourFunc {
  pub fn new() -> Self {
    Self
  }
}

impl FormulaFunc for HourFunc {
  fn get_type(&self) -> FormulaFuncType {
    DateTimeFunc::get_type()
  }

  fn get_accept_value_type(&self) -> Vec<BasicValueType> {
    DateTimeFunc::get_accept_value_type()
  }

  fn validate_params(&self, params: &Vec<&AstNode>) -> FormulaResult<()> {
    if params.len() < 1 {
      return Err(Error::new(
        Strings::ParamsCountError.with_params(params_i18n!(paramsName = "HOUR", paramsCount = 1)),
      ));
    }
    Ok(())
  }

  fn get_return_type(&self, params: &Vec<&AstNode>) -> FormulaResult<BasicValueType> {
    self.validate_params(params)?;
    Ok(BasicValueType::Number)
  }

  fn func(&self, params: &Vec<FormulaParam>, _context: &FormulaEvaluateContext) -> FormulaResult<CellValue> {
    let result = get_day_js(&params[0].value)?;
    Ok(CellValue::Number(result.hour() as f64))
  }
}

/// Minute Function
pub struct MinuteFunc;

impl MinuteFunc {
  pub fn new() -> Self {
    Self
  }
}

impl FormulaFunc for MinuteFunc {
  fn get_type(&self) -> FormulaFuncType {
    DateTimeFunc::get_type()
  }

  fn get_accept_value_type(&self) -> Vec<BasicValueType> {
    DateTimeFunc::get_accept_value_type()
  }

  fn validate_params(&self, params: &Vec<&AstNode>) -> FormulaResult<()> {
    if params.len() < 1 {
      return Err(Error::new(
        Strings::ParamsCountError.with_params(params_i18n!(paramsName = "MINUTE", paramsCount = 1)),
      ));
    }
    Ok(())
  }

  fn get_return_type(&self, params: &Vec<&AstNode>) -> FormulaResult<BasicValueType> {
    self.validate_params(params)?;
    Ok(BasicValueType::Number)
  }

  fn func(&self, params: &Vec<FormulaParam>, _context: &FormulaEvaluateContext) -> FormulaResult<CellValue> {
    let result = get_day_js(&params[0].value)?;
    Ok(CellValue::Number(result.minute() as f64))
  }
}

/// Second Function
pub struct SecondFunc;

impl SecondFunc {
  pub fn new() -> Self {
    Self
  }
}

impl FormulaFunc for SecondFunc {
  fn get_type(&self) -> FormulaFuncType {
    DateTimeFunc::get_type()
  }

  fn get_accept_value_type(&self) -> Vec<BasicValueType> {
    DateTimeFunc::get_accept_value_type()
  }

  fn validate_params(&self, params: &Vec<&AstNode>) -> FormulaResult<()> {
    if params.len() < 1 {
      return Err(Error::new(
        Strings::ParamsCountError.with_params(params_i18n!(paramsName = "SECOND", paramsCount = 1)),
      ));
    }
    Ok(())
  }

  fn get_return_type(&self, params: &Vec<&AstNode>) -> FormulaResult<BasicValueType> {
    self.validate_params(params)?;
    Ok(BasicValueType::Number)
  }

  fn func(&self, params: &Vec<FormulaParam>, _context: &FormulaEvaluateContext) -> FormulaResult<CellValue> {
    let result = get_day_js(&params[0].value)?;
    Ok(CellValue::Number(result.second() as f64))
  }
}

/// Weekday Function
pub struct WeekdayFunc;

impl WeekdayFunc {
  pub fn new() -> Self {
    Self
  }
}

impl FormulaFunc for WeekdayFunc {
  fn get_type(&self) -> FormulaFuncType {
    DateTimeFunc::get_type()
  }

  fn get_accept_value_type(&self) -> Vec<BasicValueType> {
    DateTimeFunc::get_accept_value_type()
  }

  fn validate_params(&self, params: &Vec<&AstNode>) -> FormulaResult<()> {
    if params.len() < 1 {
      return Err(Error::new(
        Strings::ParamsCountError.with_params(params_i18n!(paramsName = "WEEKDAY", paramsCount = 1)),
      ));
    }
    Ok(())
  }

  fn get_return_type(&self, params: &Vec<&AstNode>) -> FormulaResult<BasicValueType> {
    self.validate_params(params)?;
    Ok(BasicValueType::Number)
  }

  fn func(&self, params: &Vec<FormulaParam>, _context: &FormulaEvaluateContext) -> FormulaResult<CellValue> {
    let result = get_day_js(&params[0].value)?;
    let day = result.weekday().num_days_from_sunday();

    if matches!(params.get(1), Some(start_day_of_week) if start_day_of_week.value.to_string().to_lowercase() == "monday")
    {
      let day = if day == 0 { 6 } else { day - 1 };
      return Ok(CellValue::Number(day as f64));
    }
    Ok(CellValue::Number(day as f64))
  }
}

/// WeekNum Function
pub struct WeekNumFunc;

impl WeekNumFunc {
  pub fn new() -> Self {
    Self
  }

  fn day_js_get_week(&self, datetime: &DateTime<Local>) -> u32 {
    let week = datetime.iso_week().week();
    let weekday = datetime.weekday();

    if weekday == Weekday::Sun {
      // if is last day of year and is sunday, return 1
      if datetime.month() == 12 && datetime.day() == 31 {
        return 1;
      }
      return week + 1;
    }

    week
  }
}

impl FormulaFunc for WeekNumFunc {
  fn get_type(&self) -> FormulaFuncType {
    DateTimeFunc::get_type()
  }

  fn get_accept_value_type(&self) -> Vec<BasicValueType> {
    DateTimeFunc::get_accept_value_type()
  }

  fn validate_params(&self, params: &Vec<&AstNode>) -> FormulaResult<()> {
    if params.len() < 1 {
      return Err(Error::new(
        Strings::ParamsCountError.with_params(params_i18n!(paramsName = "WEEKNUM", paramsCount = 1)),
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
    let current_date = get_day_js(&first_param.value)?;
    let start_of_week = match params.get(1) {
      Some(param) => get_index_by_weekday_name(&param.value.to_string()),
      None => 0,
    };

    if ![0, 1].contains(&start_of_week) {
      return Err(Error::new("NaN"));
    }

    if current_date.iso_week().year() != current_date.year() && self.day_js_get_week(&current_date) == 1 {
      let week_offset = if start_of_week == 0 {
        1
      } else {
        if current_date.weekday().num_days_from_sunday() == 0 {
          0
        } else {
          1
        }
      };
      return Ok(CellValue::Number(
        self.day_js_get_week(&current_date) as f64 + week_offset as f64,
      ));
    }

    let prev_week_date = current_date
      .checked_sub_days(Days::new(start_of_week as u64))
      .ok_or(Error::new("UnexpectedError"))?;
    if prev_week_date.iso_week().year() != current_date.year() {
      return Ok(CellValue::Number(1.0));
    }

    if start_of_week == 1 && current_date.weekday().num_days_from_sunday() == 0 {
      return Ok(CellValue::Number(self.day_js_get_week(&current_date) as f64 - 1.0));
    }

    return Ok(CellValue::Number(self.day_js_get_week(&current_date) as f64));
  }
}

#[cfg(test)]
mod tests {
  use std::collections::HashMap;
  use std::str::FromStr;

  use chrono::{Datelike, Local, NaiveDateTime, TimeZone};

  use crate::formula::functions::date_time::get_day_js;
  use crate::formula::helper::tests::{test_assert_error, test_assert_result};
  use crate::formula::i18n::Strings;
  use crate::prelude::CellValue;

  fn assert_result(expected: CellValue, expression: &str, record_data: &HashMap<String, CellValue>) {
    test_assert_result(expected, expression, record_data, &HashMap::new(), None)
  }

  fn assert_error(expected: &str, expression: &str, record_data: &HashMap<String, CellValue>) {
    test_assert_error(expected, expression, record_data, &HashMap::new(), None);
  }

  fn data(b: &str, c: f64) -> HashMap<String, CellValue> {
    vec![
      ("a".to_string(), CellValue::from(0.0)),
      ("b".to_string(), CellValue::from(b)),
      ("c".to_string(), CellValue::from(c)),
    ]
    .into_iter()
    .collect()
  }

  #[test]
  fn test_datetime_function_test_day() {
    assert_result(CellValue::Number(6.0), "DAY({c})", &data("", 1591414562369.0));
    // support parsing string type
    assert_result(CellValue::Number(3.0), "DAY({b})", &data("2012/2/3 23:22:44", 0.0));
    assert_result(CellValue::Number(3.0), "DAY({b})", &data("2012年2月3日", 0.0));
    // ignore redundant parameters
    assert_result(CellValue::Number(6.0), "DAY({c}, {a})", &data("", 1591414562369.0));
    // requires at least one parameter
    assert_error(&Strings::ParamsCountError.to_string(), "DAY()", &data("", 0.0));
  }

  #[test]
  fn test_datetime_function_test_year() {
    assert_result(CellValue::Number(2020.0), "YEAR({c})", &data("", 1591414562369.0));
    assert_result(CellValue::Number(2012.0), "YEAR({b})", &data("2012/2/3 23:22:44", 0.0));

    // requires at least one parameter
    assert_error(&Strings::ParamsCountError.to_string(), "YEAR()", &data("", 0.0));
  }

  #[test]
  fn test_datetime_function_test_month() {
    assert_result(
      CellValue::Number(6.0),
      "MONTH({c})",
      &data(
        "",
        NaiveDateTime::from_str("2020-6-6T00:00:00").unwrap().timestamp_millis() as f64,
      ),
    );
    // support parsing string type
    assert_result(CellValue::Number(2.0), "MONTH({b})", &data("2012/2/3 23:22:44", 0.0));
    // requires at least one parameter
    assert_error(&Strings::ParamsCountError.to_string(), "MONTH()", &data("", 0.0));
  }

  #[test]
  fn test_datetime_function_test_hour() {
    assert_result(CellValue::Number(11.0), "HOUR({c})", &data("", 1591414562369.0));
    // support parsing string type
    assert_result(CellValue::Number(23.0), "HOUR({b})", &data("2012/2/3 23:22:44", 0.0));
    // requires at least one parameter
    assert_error(&Strings::ParamsCountError.to_string(), "HOUR()", &data("", 0.0));
  }

  #[test]
  fn test_datetime_function_test_minute() {
    assert_result(CellValue::Number(36.0), "MINUTE({c})", &data("", 1591414562369.0));
    // support parsing string type
    assert_result(CellValue::Number(22.0), "MINUTE({b})", &data("2012/2/3 23:22:44", 0.0));
    // requires at least one parameter
    assert_error(&Strings::ParamsCountError.to_string(), "MINUTE()", &data("", 0.0));
  }

  #[test]
  fn test_datetime_function_test_second() {
    assert_result(
      CellValue::Number(2.0),
      "SECOND({c})",
      &data(
        "",
        NaiveDateTime::from_str("2020-6-10T00:00:02")
          .unwrap()
          .timestamp_millis() as f64,
      ),
    );
    // support parsing string type
    assert_result(CellValue::Number(44.0), "SECOND({b})", &data("2012/2/3 23:22:44", 0.0));
    // requires at least one parameter
    assert_error(&Strings::ParamsCountError.to_string(), "SECOND()", &data("", 0.0));
  }

  #[test]
  fn test_datetime_function_test_weekday() {
    assert_result(
      CellValue::Number(3.0),
      "WEEKDAY({c})",
      &data(
        "",
        NaiveDateTime::from_str("2020-6-10T00:00:00")
          .unwrap()
          .timestamp_millis() as f64,
      ),
    );

    assert_result(
      CellValue::Number(2.0),
      "WEEKDAY({c}, \"Monday\")",
      &data(
        "",
        NaiveDateTime::from_str("2020-6-10T00:00:00")
          .unwrap()
          .timestamp_millis() as f64,
      ),
    );
    assert_result(
      CellValue::Number(3.0),
      "WEEKDAY({c}, \"Sunday\")",
      &data(
        "",
        NaiveDateTime::from_str("2020-6-10T00:00:00")
          .unwrap()
          .timestamp_millis() as f64,
      ),
    );

    // requires at least one parameter
    assert_error(&Strings::ParamsCountError.to_string(), "WEEKDAY()", &data("", 0.0));
  }

  #[test]
  fn test_datetime_function_test_weeknum() {
    assert_result(
      CellValue::Number(1.0),
      "WEEKNUM({c})",
      &data(
        "",
        Local
          .from_local_datetime(&NaiveDateTime::from_str("2021-1-1T00:00:00").unwrap())
          .unwrap()
          .timestamp_millis() as f64,
      ),
    );
    assert_result(
      CellValue::Number(23.0),
      "WEEKNUM({c})",
      &data(
        "",
        Local
          .from_local_datetime(&NaiveDateTime::from_str("2020-6-6T18:30:15").unwrap())
          .unwrap()
          .timestamp_millis() as f64,
      ),
    );
    assert_result(
      CellValue::Number(24.0),
      "WEEKNUM({c})",
      &data(
        "",
        Local
          .from_local_datetime(&NaiveDateTime::from_str("2020-6-7T18:30:15").unwrap())
          .unwrap()
          .timestamp_millis() as f64,
      ),
    );
    assert_result(
      CellValue::Number(23.0),
      "WEEKNUM({c}, \"Monday\")",
      &data(
        "",
        Local
          .from_local_datetime(&NaiveDateTime::from_str("2020-6-7T18:30:15").unwrap())
          .unwrap()
          .timestamp_millis() as f64,
      ),
    );
    // requires at least one parameter
    assert_error(&Strings::ParamsCountError.to_string(), "WEEKNUM()", &data("", 0.0));
  }

  #[test]
  fn test_datetime_function_test_validate_get_day_js_function() {
    match get_day_js(&CellValue::Null) {
      Err(_) => {}
      Ok(_) => assert!(false),
    }

    match get_day_js(&CellValue::String("null".to_string())) {
      Err(_) => {}
      Ok(_) => assert!(false),
    }

    match get_day_js(&CellValue::String("1636965086541".to_string())) {
      Ok(result) => assert_eq!(1636965086541, result.timestamp_millis()),
      Err(_) => assert!(false),
    };

    match get_day_js(&CellValue::Number(1591414562369.0)) {
      Ok(result) => assert_eq!(2020, result.year()),
      Err(_) => assert!(false),
    };
  }

  #[test]
  fn test() {
    let time = Local
      .from_local_datetime(&NaiveDateTime::from_str("2020-6-8T18:30:15").unwrap())
      .unwrap();
    println!("{}", time.iso_week().week());
  }
}
