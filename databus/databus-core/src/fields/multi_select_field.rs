use std::collections::HashMap;

use crate::fields::base_field::BaseField;
use crate::fields::ext::types::CellToStringOption;
use crate::fields::property::SingleSelectProperty;
use crate::prelude::view_operation::filter::{ConditionValue, FOperator};
use databus_shared::prelude::HashSet;
use serde_json::{Value, from_value};

use crate::so::api_value::ApiValue;
use crate::so::CellValue;
use crate::utils::hash_set_ext::{has_intersect, is_same_set};

use super::{
  base_field::IBaseField,
  property::{field_types::BasicValueType, FieldPropertySO, SelectFieldPropertySO},
};

pub struct MultiSelect {
  field_conf: crate::prelude::FieldSO,
}

impl IBaseField for MultiSelect {
  fn basic_value_type(&self) -> BasicValueType {
    BasicValueType::Array
  }

  fn inner_basic_value_type(&self) -> BasicValueType {
    BasicValueType::String
  }

  fn default_value(&self) -> Option<Value> {
    let default_value_pre = self.field_conf.property.clone().unwrap().default_value.clone();
    if default_value_pre.is_none() {
      return None;
    }
    let default_value_pre = default_value_pre.unwrap();
    let default_value = default_value_pre.as_array();
    if default_value.is_none() || default_value.unwrap().is_empty() {
      return None;
    }
    Some(default_value_pre)
  }

  fn cell_value_to_string(&self, cell_value: CellValue, _option: Option<CellToStringOption>) -> Option<String> {
    if let CellValue::Array(arr) = cell_value {
      let cell_value = arr.into_iter().map(|it| it.to_string()).collect();
      let array = Self::cell_value_to_array(cell_value);
      return Self::array_value_to_string(array);
    }
    return None;
  }

  fn cell_value_to_api_standard_value(&self, cell_value: CellValue) -> ApiValue {
    if let Some(arr) = cell_value.to_string_array() {
      let property = self.field_conf.property.as_ref().unwrap();
      let select_field_property = property.to_multi_select_field_property();

      let option_map: HashMap<String, String> = select_field_property
        .options
        .clone()
        .into_iter()
        .map(|it| (it.id, it.name))
        .collect();
      let value = arr
        .iter()
        .map(|it| option_map[&it.to_string()].clone())
        .collect::<Vec<_>>();

      return ApiValue::from(value);
    }

    ApiValue::Null
  }

  fn cell_value_to_array(&self, cell_value: CellValue) -> CellValue {
    return match cell_value {
      CellValue::Array(array) => {
        let mut result = vec![];
        for value in array {
          let option = self.find_option_by_id(&value.to_string());
          if let Some(option) = option {
            result.push(CellValue::from(option.name));
          }
        }

        CellValue::from(result)
      }
      _ => CellValue::Null,
    };
  }

  fn is_meet_filter(&self, operator: &FOperator, cell_value: &CellValue, condition_value: &ConditionValue) -> bool {
    let condition_value_set = condition_value
      .as_string_array()
      .unwrap_or(vec![])
      .into_iter()
      .collect::<HashSet<String>>();
    let cell_value_set = cell_value
      .clone()
      .to_string_array()
      .unwrap_or(vec![])
      .into_iter()
      .collect::<HashSet<String>>();

    match operator {
      FOperator::Is => !cell_value.is_null() && is_same_set(&cell_value_set, &condition_value_set),
      FOperator::IsNot => cell_value.is_null() || !is_same_set(&cell_value_set, &condition_value_set),
      FOperator::Contains => !cell_value.is_null() && has_intersect(&cell_value_set, &condition_value_set),
      FOperator::DoesNotContain => cell_value.is_null() || !has_intersect(&cell_value_set, &condition_value_set),
      _ => BaseField::is_meet_filter(operator, cell_value, condition_value),
    }
  }
}

impl MultiSelect {
  pub fn new(field_conf: crate::prelude::FieldSO) -> Self {
    return Self { field_conf };
  }

  pub fn default_property() -> Option<FieldPropertySO> {
    Some(FieldPropertySO {
      options: Some(vec![]),
      ..Default::default()
    })
  }

  fn cell_value_to_array(cell_value: Vec<String>) -> Option<Vec<String>> {
    if cell_value.is_empty() {
      return None;
    }
    Some(cell_value)
  }

  fn array_value_to_string(cell_values: Option<Vec<String>>) -> Option<String> {
    match cell_values {
      Some(cell_values) => Some(cell_values.join(", ")),
      None => None,
    }
  }

  fn find_option_by_id(&self, id: &str) -> Option<SingleSelectProperty> {
    return match &self.field_conf.property {
      Some(property) => match &property.options {
        Some(options) => {
          for option in options {
            if option.id == id {
              return Some(option.clone());
            }
          }
          None
        }
        None => None,
      },
      None => None,
    };
  }

  pub fn validate_add_open_field_property(value: Value) -> anyhow::Result<()>{
    let property = from_value::<SelectFieldPropertySO>(value.clone());
    match property{
      Ok(_property) => {
        return Ok(());
      },
      Err(e) => {
        return Err(anyhow::Error::msg(format!("api_param_validate_error={}", e)));
      }
    }
  }
}

#[cfg(test)]
mod test {

  #[test]
  pub fn test_compare_string() {
    let x = "a";
    let y = vec!["a".to_string(), "b".to_string()];
    println!("{}", x == y[0]);
  }
}
