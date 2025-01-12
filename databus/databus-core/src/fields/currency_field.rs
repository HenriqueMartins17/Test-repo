use serde_json::{Number, Value};

use crate::fields::number_base_field::NumberBaseField;
use crate::prelude::view_operation::filter::{ConditionValue, FOperator};
use crate::prelude::CellValue;
use crate::so::api_value::ApiValue;

use super::{
  base_field::IBaseField,
  property::{field_types::BasicValueType, FieldPropertySO},
};

pub struct Currency {
  field_conf: crate::prelude::FieldSO,
}

impl IBaseField for Currency {
  fn basic_value_type(&self) -> BasicValueType {
    BasicValueType::Number
  }

  fn inner_basic_value_type(&self) -> BasicValueType {
    BasicValueType::String
  }

  fn cell_value_to_api_standard_value(&self, cell_value: CellValue) -> ApiValue {
    if let CellValue::Number(value) = cell_value {
      return ApiValue::from(Number::from_f64(value).unwrap());
    }
    ApiValue::Null
  }

  fn is_meet_filter(&self, operator: &FOperator, cell_value: &CellValue, condition_value: &ConditionValue) -> bool {
    NumberBaseField::is_meet_filter(operator, cell_value, condition_value)
  }
}

impl Currency {
  pub fn new(field_conf: crate::prelude::FieldSO) -> Self {
    return Self { field_conf };
  }

  pub fn default_property() -> Option<FieldPropertySO> {
    Some(FieldPropertySO {
      symbol: Some("$".to_string()),
      precision: Some(2),
      symbol_align: Some(super::property::field_types::SymbolAlign::Default),
      ..Default::default()
    })
  }

  pub fn validate_add_open_field_property(_property: Value) -> anyhow::Result<()>{
    Ok(())
  }

  pub fn update_open_field_property_transform_property(open_field_property: FieldPropertySO, default_property: FieldPropertySO) -> FieldPropertySO{
    let symbol_align = match open_field_property.symbol_align {
      Some(symbol_align_str) => Some(symbol_align_str),
      None => default_property.symbol_align,
    };
    FieldPropertySO {
      default_value: open_field_property.default_value,
      symbol: open_field_property.symbol.or(default_property.symbol),
      precision: open_field_property.precision.or(default_property.precision),
      symbol_align,
      ..Default::default()
    }
  }
}
