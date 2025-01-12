use crate::fields::number_base_field::NumberBaseField;
use crate::prelude::view_operation::filter::{ConditionValue, FOperator};
use crate::prelude::CellValue;
use crate::so::api_value::ApiValue;
use serde_json::{Number, Value, from_value};

use super::property::RatingFieldPropertySO;
use super::{
  base_field::IBaseField,
  property::{field_types::BasicValueType, FieldPropertySO},
};

pub struct Rating {
  field_conf: crate::prelude::FieldSO,
}

impl IBaseField for Rating {
  fn basic_value_type(&self) -> BasicValueType {
    BasicValueType::Number
  }

  fn inner_basic_value_type(&self) -> BasicValueType {
    BasicValueType::String
  }

  fn cell_value_to_api_standard_value(&self, cell_value: CellValue) -> ApiValue {
    if let CellValue::Number(val) = cell_value {
      return ApiValue::from(Number::from_f64(val).unwrap());
    }
    ApiValue::Null
  }

  fn is_meet_filter(&self, operator: &FOperator, cell_value: &CellValue, condition_value: &ConditionValue) -> bool {
    NumberBaseField::is_meet_filter(operator, cell_value, condition_value)
  }
}

impl Rating {
  pub fn new(field_conf: crate::prelude::FieldSO) -> Self {
    return Self { field_conf };
  }

  pub fn default_property() -> Option<FieldPropertySO> {
    Some(FieldPropertySO {
      icon: Some("star".to_string()),
      max: Some(5),
      ..Default::default()
    })
  }

  pub fn validate_add_open_field_property(value: Value) -> anyhow::Result<()>{
    let property = from_value::<RatingFieldPropertySO>(value.clone());
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
