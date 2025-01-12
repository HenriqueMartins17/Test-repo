use serde_json::{Value, from_value};

use crate::fields::ext::types::CellToStringOption;
use crate::fields::number_base_field::NumberBaseField;
use crate::fields::property::field_types::BasicValueType;
use crate::prelude::view_operation::filter::{ConditionValue, FOperator};
use crate::prelude::CellValue;
use crate::so::api_value::ApiValue;

use super::property::NumberFieldPropertySO;
use super::{base_field::IBaseField, property::FieldPropertySO};

pub struct Number {
  field_conf: crate::prelude::FieldSO,
}

impl IBaseField for Number {
  fn basic_value_type(&self) -> BasicValueType {
    BasicValueType::Number
  }

  fn inner_basic_value_type(&self) -> BasicValueType {
    BasicValueType::String
  }

  fn cell_value_to_string(&self, cell_value: CellValue, _option: Option<CellToStringOption>) -> Option<String> {
    if let CellValue::Number(cell_value) = cell_value {
      if cell_value.is_nan() {
        return None;
      }
      // TODO: more precision

      return Some(cell_value.to_string());
    }
    return None;
  }

  fn cell_value_to_api_standard_value(&self, cell_value: crate::prelude::CellValue) -> ApiValue {
    if let crate::prelude::CellValue::Number(val) = cell_value {
      return ApiValue::from(serde_json::Number::from_f64(val).unwrap());
    }
    ApiValue::Null
  }

  fn is_meet_filter(&self, operator: &FOperator, cell_value: &CellValue, condition_value: &ConditionValue) -> bool {
    NumberBaseField::is_meet_filter(operator, cell_value, condition_value)
  }
}

impl Number {
  pub fn new(field_conf: crate::prelude::FieldSO) -> Self {
    return Self { field_conf };
  }

  pub fn default_property() -> Option<FieldPropertySO> {
    Some(FieldPropertySO {
      precision: Some(0),
      symbol_align: Some(super::property::field_types::SymbolAlign::Right),
      ..Default::default()
    })
  }

  pub fn validate_add_open_field_property(value: Value) -> anyhow::Result<()>{
    let property = from_value::<NumberFieldPropertySO>(value.clone());
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
