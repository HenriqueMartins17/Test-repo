use crate::fields::base_field::BaseField;
use serde_json::{Value, from_value};

use crate::fields::property::field_types::BasicValueType;
use crate::prelude::view_operation::filter::{ConditionValue, FOperator};
use crate::prelude::CellValue;
use crate::so::api_value::ApiValue;

use super::base_field::IBaseField;
use super::property::{FieldPropertySO, SelectFieldPropertySO};

pub struct SingleSelect {
  field_conf: crate::prelude::FieldSO,
}

impl IBaseField for SingleSelect {
  fn basic_value_type(&self) -> BasicValueType {
    BasicValueType::String
  }

  fn inner_basic_value_type(&self) -> BasicValueType {
    BasicValueType::String
  }

  fn default_value(&self) -> Option<Value> {
    let default_value_pre = self.field_conf.property.clone().unwrap().default_value;
    if default_value_pre.is_none() {
      return None;
    }
    let default_value_pre = default_value_pre.clone().unwrap();
    let default_value = default_value_pre.as_str();
    if default_value.is_none() || default_value.unwrap().trim().is_empty() {
      return None;
    }
    Some(default_value_pre)
  }

  fn cell_value_to_api_standard_value(&self, cell_value: CellValue) -> ApiValue {
    if let CellValue::String(key) = cell_value {
      let property = self
        .field_conf
        .property
        .as_ref()
        .unwrap()
        .to_single_select_field_property();
      return ApiValue::from(property.options.iter().find(|it| it.id == key).unwrap().clone().name);
    }
    ApiValue::Null
  }

  fn is_meet_filter(&self, operator: &FOperator, cell_value: &CellValue, condition_value: &ConditionValue) -> bool {
    let real_condition_value = condition_value.as_string_array();
    let real_cell_value = cell_value.as_str();

    match operator {
      FOperator::IsEmpty => real_cell_value.is_none(),
      FOperator::IsNotEmpty => real_cell_value.is_some(),
      FOperator::Is => real_condition_value.map_or(false, |values| {
        values.len() == 1 && real_cell_value.map_or(false, |cell| cell == values[0])
      }),
      FOperator::IsNot => real_condition_value.map_or(false, |values| {
        values.len() == 1 && real_cell_value.map_or(true, |cell| cell != values[0])
      }),
      FOperator::Contains => real_condition_value.map_or(false, |values| {
        real_cell_value.map_or(false, |cell| values.iter().any(|value| value == &cell))
      }),
      FOperator::DoesNotContain => real_condition_value.map_or(true, |values| {
        real_cell_value.map_or(true, |cell| values.iter().all(|value| value != &cell))
      }),
      _ => BaseField::is_meet_filter(operator, cell_value, condition_value), // Add appropriate handling for other operators if needed
    }
  }
}

impl SingleSelect {
  pub fn new(field_conf: crate::prelude::FieldSO) -> Self {
    return Self { field_conf };
  }

  pub fn default_property() -> Option<FieldPropertySO> {
    Some(FieldPropertySO {
      options: Some(vec![]),
      ..Default::default()
    })
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
