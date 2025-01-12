use serde_json::{from_value, Value};

use crate::fields::ext::types::CellToStringOption;
use crate::prelude::view_operation::filter::{ConditionValue, FOperator};
use crate::prelude::CellValue;
use crate::so::api_value::ApiValue;

use super::property::CheckboxFieldPropertySO;
use super::{
  base_field::IBaseField,
  property::{field_types::BasicValueType, FieldPropertySO},
};

pub struct Checkbox {
  field_conf: crate::prelude::FieldSO,
}

impl IBaseField for Checkbox {
  fn basic_value_type(&self) -> BasicValueType {
    BasicValueType::Boolean
  }

  fn inner_basic_value_type(&self) -> BasicValueType {
    BasicValueType::String
  }

  fn cell_value_to_string(&self, cell_value: CellValue, _option: Option<CellToStringOption>) -> Option<String> {
    return Some(match cell_value.as_bool() {
      Some(true) => "1".to_string(),
      Some(false) | None => "0".to_string(),
    });
  }

  fn cell_value_to_api_standard_value(&self, cell_value: CellValue) -> ApiValue {
    if let CellValue::Bool(value) = cell_value {
      return ApiValue::from(value);
    }
    ApiValue::Null
  }

  fn is_meet_filter(&self, operator: &FOperator, cell_value: &CellValue, condition_value: &ConditionValue) -> bool {
    Self::_is_meet_filter(operator, cell_value, condition_value)
  }

  fn compare(
    &self,
    cell_value1: &CellValue,
    cell_value2: &CellValue,
    order_in_cell_value_sensitive: Option<bool>,
  ) -> i32 {
    Checkbox::_compare(cell_value1, cell_value2, order_in_cell_value_sensitive)
  }
}

impl Checkbox {
  pub fn new(field_conf: crate::prelude::FieldSO) -> Self {
    return Self { field_conf };
  }

  pub fn default_property() -> Option<FieldPropertySO> {
    Some(FieldPropertySO {
      icon: Some("white_check_mark".to_string()),
      ..Default::default()
    })
  }

  pub fn _is_meet_filter(operator: &FOperator, cell_value: &CellValue, condition_value: &ConditionValue) -> bool {
    match operator {
      FOperator::Is => condition_value.as_bool().unwrap() == cell_value.as_bool().unwrap_or_default(),
      _ => {
        println!("Method should be overwrite!");
        true
      }
    }
  }

  pub fn validate_add_open_field_property(value: Value) -> anyhow::Result<()> {
    let property = from_value::<CheckboxFieldPropertySO>(value.clone());
    match property {
      Ok(_property) => {
        return Ok(());
      }
      Err(e) => {
        return Err(anyhow::Error::msg(format!("api_param_validate_error={}", e)));
      }
    }
  }

  fn _compare(cell_value1: &CellValue, cell_value2: &CellValue, order_in_cell_value_sensitive: Option<bool>) -> i32 {
    cell_value1
      .as_bool()
      .unwrap_or_default()
      .cmp(&cell_value2.as_bool().unwrap_or_default()) as i32
  }
}
