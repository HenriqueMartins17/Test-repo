use crate::fields::ext::types::CellToStringOption;
use crate::fields::text_base_field::TextBaseField;
use crate::prelude::view_operation::filter::FOperator;
use crate::prelude::CellValue;
use crate::so::api_value::ApiValue;
use crate::so::view_operation::filter::ConditionValue;

use super::{
  base_field::IBaseField,
  property::{field_types::BasicValueType, FieldPropertySO},
};

pub struct Text {
  _field_conf: crate::prelude::FieldSO,
}

impl IBaseField for Text {
  fn basic_value_type(&self) -> BasicValueType {
    BasicValueType::String
  }

  fn inner_basic_value_type(&self) -> BasicValueType {
    BasicValueType::String
  }

  fn cell_value_to_string(&self, cell_value: CellValue, _option: Option<CellToStringOption>) -> Option<String> {
    TextBaseField::cell_value_to_string(cell_value, _option)
  }

  fn cell_value_to_api_standard_value(&self, cell_value: CellValue) -> ApiValue {
    if let Some(arr) = cell_value.to_text_array() {
      return arr
        .into_iter()
        .nth(0)
        .map(|val| {
          return ApiValue::from(val.text);
        })
        .unwrap_or(ApiValue::Null);
    }
    return ApiValue::Null;
  }

  fn is_meet_filter(&self, operator: &FOperator, cell_value: &CellValue, condition_value: &ConditionValue) -> bool {
    TextBaseField::is_meet_filter(operator, cell_value, condition_value)
  }

  fn eq(&self, cv1: &CellValue, cv2: &CellValue) -> bool {
    TextBaseField::eq(self, cv1, cv2)
  }

  fn compare(
    &self,
    cell_value1: &CellValue,
    cell_value2: &CellValue,
    order_in_cell_value_sensitive: Option<bool>,
  ) -> i32 {
    TextBaseField::compare(self, cell_value1, cell_value2, order_in_cell_value_sensitive)
  }
}

impl Text {
  pub fn new(_field_conf: crate::prelude::FieldSO) -> Self {
    return Self { _field_conf };
  }

  pub fn default_property() -> Option<FieldPropertySO> {
    None
  }
}
