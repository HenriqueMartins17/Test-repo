use tracing::debug;

use crate::fields::property::field_types::IComputedFieldFormattingProperty;
use crate::so::view_operation::filter::{ConditionValue, FOperator};
use crate::so::CellValue;

pub fn number_format(cell_value: &CellValue, _formatting: &Option<IComputedFieldFormattingProperty>) -> Option<String> {
  // TODO: implement this
  Some(cell_value.to_string())
}

pub struct NumberBaseField;

impl NumberBaseField {
  pub fn is_meet_filter(operator: &FOperator, cell_value: &CellValue, condition_value: &ConditionValue) -> bool {
    Self::_is_meet_filter(operator, cell_value, condition_value)
  }

  pub fn _is_meet_filter(operator: &FOperator, cell_value: &CellValue, condition_value: &ConditionValue) -> bool {
    match operator {
      FOperator::IsEmpty => cell_value.is_null(),
      FOperator::IsNotEmpty => !cell_value.is_null(),
      _ if condition_value.is_null() => true,
      _ => {
        let condition_value = condition_value.as_string();
        if condition_value.is_none() {
          true
        } else {
          let filter_value = parse_float(&condition_value.unwrap());
          let cell_value = cell_value.as_f64();

          debug!("filter_value: {:?}, cell_value: {:?}", filter_value, cell_value);

          match operator {
            FOperator::Is => cell_value.map_or(false, |cv| filter_value.map_or(false, |fv| fv == cv)),
            FOperator::IsNot => cell_value.map_or(true, |cv| filter_value.map_or(true, |fv| fv != cv)),
            FOperator::IsGreater => cell_value.map_or(false, |cv| filter_value.map_or(false, |fv| cv > fv)),
            FOperator::IsGreaterEqual => cell_value.map_or(false, |cv| filter_value.map_or(false, |fv| cv >= fv)),
            FOperator::IsLess => cell_value.map_or(false, |cv| filter_value.map_or(false, |fv| cv < fv)),
            FOperator::IsLessEqual => cell_value.map_or(false, |cv| filter_value.map_or(false, |fv| cv <= fv)),
            _ => false,
          }
        }
      }
    }
  }
}

fn parse_float(value: &str) -> Option<f64> {
  value.trim().parse().ok()
}

#[cfg(test)]
mod test {
  use std::cmp::Ordering;
  use std::str::FromStr;

  use serde_json::Number;

  use crate::fields::number_base_field::NumberBaseField;
  use crate::prelude::view_operation::filter::{ConditionValue, FOperator};
  use crate::prelude::CellValue;

  #[test]
  pub fn test() {
    let num: Number = Number::from(1111);
    let num1: Number = Number::from_f64(2222.1_f64).unwrap();
    let num2 = Number::from_str("11111").unwrap();

    let b = (num.as_f64().unwrap()).partial_cmp(&(num1.as_f64().unwrap())).unwrap();
    assert_eq!(b, Ordering::Less);
  }

  #[test]
  pub fn test_is_meet_filter() {
    let cell_value = CellValue::from(11.1_f64);
    let condition_value = ConditionValue::StringArray(vec!["11.1".to_string()]);
    let condition_value_1 = ConditionValue::StringArray(vec!["11.0".to_string()]);

    let result = NumberBaseField::is_meet_filter(&FOperator::Is, &CellValue::Null, &ConditionValue::Null);
    assert_eq!(result, true);

    let result = NumberBaseField::is_meet_filter(&FOperator::Is, &CellValue::Null, &condition_value);
    assert_eq!(result, false);

    let result = NumberBaseField::is_meet_filter(&FOperator::Is, &cell_value, &condition_value);
    assert_eq!(result, true);

    let result = NumberBaseField::is_meet_filter(&FOperator::Is, &cell_value, &condition_value_1);
    assert_eq!(result, false);

    let result = NumberBaseField::is_meet_filter(&FOperator::IsNot, &CellValue::Null, &condition_value);
    assert_eq!(result, true);

    let result = NumberBaseField::is_meet_filter(&FOperator::IsNot, &cell_value, &condition_value);
    assert_eq!(result, false);

    let result = NumberBaseField::is_meet_filter(&FOperator::IsNot, &cell_value, &condition_value_1);
    assert_eq!(result, true);

    let result = NumberBaseField::is_meet_filter(&FOperator::IsGreater, &cell_value, &condition_value);
    assert_eq!(result, false);

    let result = NumberBaseField::is_meet_filter(&FOperator::IsGreater, &cell_value, &condition_value_1);
    assert_eq!(result, true);

    let result = NumberBaseField::is_meet_filter(&FOperator::IsGreaterEqual, &cell_value, &condition_value);
    assert_eq!(result, true);

    let result = NumberBaseField::is_meet_filter(&FOperator::IsGreaterEqual, &cell_value, &condition_value_1);
    assert_eq!(result, true);

    let result = NumberBaseField::is_meet_filter(&FOperator::IsLess, &cell_value, &condition_value);
    assert_eq!(result, false);

    let result = NumberBaseField::is_meet_filter(&FOperator::IsLess, &cell_value, &condition_value_1);
    assert_eq!(result, false);

    let result = NumberBaseField::is_meet_filter(&FOperator::IsLessEqual, &cell_value, &condition_value);
    assert_eq!(result, true);

    let result = NumberBaseField::is_meet_filter(&FOperator::IsLessEqual, &cell_value, &condition_value_1);
    assert_eq!(result, false);
  }
}
