use crate::fields::base_field::{BaseField, IBaseField};
use crate::fields::ext::types::CellToStringOption;
use crate::so::view_operation::filter::{ConditionValue, FOperator};
use crate::so::CellValue;

pub struct OptFn {
  contains_fn: Option<Box<dyn Fn(&str) -> bool>>,
  does_not_contain_fn: Option<Box<dyn Fn(&str) -> bool>>,
  default_fn: Option<Box<dyn Fn() -> bool>>,
}

/// Text, SingleText, Email, URL, Phone
pub struct TextBaseField;

impl TextBaseField {
  pub fn compare(
    self_dyn: &dyn IBaseField,
    cell_value1: &CellValue,
    cell_value2: &CellValue,
    order_in_cell_value_sensitive: Option<bool>,
  ) -> i32 {
    BaseField::compare(self_dyn, cell_value1, cell_value2, order_in_cell_value_sensitive)
  }

  pub fn eq(self_base: &dyn IBaseField, cv1: &CellValue, cv2: &CellValue) -> bool {
    return self_base.cell_value_to_string(cv1.clone(), None) == self_base.cell_value_to_string(cv2.clone(), None);
  }

  pub fn is_meet_filter(operator: &FOperator, cell_value: &CellValue, condition_value: &ConditionValue) -> bool {
    let cell_text = Self::cell_value_to_string(cell_value.clone(), None);
    let cell_text2 = cell_text.clone();

    let operator2 = operator.clone();
    let cell_value2 = cell_value.clone();
    let condition_value2 = condition_value.clone();

    return Self::_is_meet_filter(
      operator,
      &cell_text.clone(),
      condition_value,
      Some(OptFn {
        contains_fn: Some(Box::new(move |filter_value| {
          cell_text.is_some() && Self::string_include(cell_text.as_ref().clone().unwrap().as_str(), filter_value)
        })),
        does_not_contain_fn: Some(Box::new(move |filter_value| {
          cell_text2.is_none() || !Self::string_include(cell_text2.clone().as_ref().unwrap(), filter_value)
        })),
        default_fn: Some(Box::new(move || {
          BaseField::is_meet_filter(&operator2, &cell_value2, &condition_value2)
        })),
      }),
    );
  }

  pub fn cell_value_to_string(cell_value: CellValue, _option: Option<CellToStringOption>) -> Option<String> {
    return match cell_value {
      CellValue::Array(arr) => {
        let values = arr
          .into_iter()
          .map(|v| v.get_text().unwrap_or("".to_string()))
          .collect::<Vec<_>>();

        Some(values.join(","))
      }
      _ => None,
    };
  }

  pub fn _is_meet_filter(
    operator: &FOperator,
    cell_text: &Option<String>,
    condition_value: &ConditionValue,
    opt_fn: Option<OptFn>,
  ) -> bool {
    if operator == &FOperator::IsEmpty {
      return cell_text.is_none();
    }
    if operator == &FOperator::IsNotEmpty {
      return cell_text.is_some();
    }
    if condition_value.is_null() {
      return true;
    }
    let filter_value = condition_value.as_string().unwrap();
    let OptFn {
      contains_fn,
      default_fn,
      does_not_contain_fn,
      ..
    } = opt_fn.unwrap_or(OptFn {
      contains_fn: None,
      default_fn: None,
      does_not_contain_fn: None,
    });
    return match operator {
      FOperator::Is => {
        cell_text.clone().map(|text| text.trim().to_lowercase()) == Some(filter_value.trim().to_lowercase())
      }
      FOperator::IsNot => {
        cell_text.clone().map(|text| text.trim().to_lowercase()) != Some(filter_value.trim().to_lowercase())
      }
      FOperator::Contains => {
        if let Some(contains_fn) = contains_fn {
          return contains_fn(filter_value);
        }
        cell_text
          .clone()
          .as_ref()
          .map(|text| Self::string_include(text, filter_value))
          .unwrap_or(false)
      }
      FOperator::DoesNotContain => {
        if let Some(does_not_contain_fn) = does_not_contain_fn {
          return does_not_contain_fn(filter_value);
        }
        cell_text
          .clone()
          .as_ref()
          .map(|text| !Self::string_include(text, filter_value))
          .unwrap_or(true)
      }
      _ => {
        if let Some(default_fn) = default_fn {
          return default_fn();
        }
        false
      }
    };
  }

  pub fn string_include(s: &str, search_str: &str) -> bool {
    s.to_lowercase().contains(&search_str.trim().to_lowercase())
  }
}

#[cfg(test)]
mod test {
  use crate::formula::helper::tests::mock_text_cell_value;
  use crate::prelude::view_operation::filter::{ConditionValue, FOperator};
  use crate::prelude::CellValue;

  #[test]
  pub fn test_is_meet_filter() {
    let cell_value = mock_text_cell_value("1111");
    // is operator
    let result = super::TextBaseField::is_meet_filter(
      &FOperator::Is,
      &cell_value,
      &ConditionValue::StringArray(vec!["1111".to_string()]),
    );
    assert_eq!(result, true);

    let result = super::TextBaseField::is_meet_filter(
      &FOperator::Is,
      &cell_value,
      &ConditionValue::StringArray(vec!["111".to_string()]),
    );
    assert_eq!(result, false);

    let result = super::TextBaseField::is_meet_filter(
      &FOperator::Is,
      &CellValue::Null,
      &ConditionValue::StringArray(vec!["111".to_string()]),
    );
    assert_eq!(result, false);

    // is_not operator
    let result = super::TextBaseField::is_meet_filter(
      &FOperator::IsNot,
      &cell_value,
      &ConditionValue::StringArray(vec!["1111".to_string()]),
    );
    assert_eq!(result, false);

    let result = super::TextBaseField::is_meet_filter(
      &FOperator::IsNot,
      &cell_value,
      &ConditionValue::StringArray(vec!["111".to_string()]),
    );
    assert_eq!(result, true);

    let result = super::TextBaseField::is_meet_filter(
      &FOperator::IsNot,
      &CellValue::Null,
      &ConditionValue::StringArray(vec!["111".to_string()]),
    );
    assert_eq!(result, true);

    // contains operator
    let result = super::TextBaseField::is_meet_filter(
      &FOperator::Contains,
      &cell_value,
      &ConditionValue::StringArray(vec!["111".to_string()]),
    );
    assert_eq!(result, true);
    let result = super::TextBaseField::is_meet_filter(
      &FOperator::Contains,
      &cell_value,
      &ConditionValue::StringArray(vec!["112".to_string()]),
    );
    assert_eq!(result, false);
    let result = super::TextBaseField::is_meet_filter(
      &FOperator::Contains,
      &CellValue::Null,
      &ConditionValue::StringArray(vec!["112".to_string()]),
    );
    assert_eq!(result, false);

    // does_not_contain operator
    let result = super::TextBaseField::is_meet_filter(
      &FOperator::DoesNotContain,
      &cell_value,
      &ConditionValue::StringArray(vec!["111".to_string()]),
    );
    assert_eq!(result, false);
    let result = super::TextBaseField::is_meet_filter(
      &FOperator::DoesNotContain,
      &cell_value,
      &ConditionValue::StringArray(vec!["112".to_string()]),
    );
    assert_eq!(result, true);
    let result = super::TextBaseField::is_meet_filter(
      &FOperator::DoesNotContain,
      &CellValue::Null,
      &ConditionValue::StringArray(vec!["112".to_string()]),
    );
    assert_eq!(result, true);
  }
}
