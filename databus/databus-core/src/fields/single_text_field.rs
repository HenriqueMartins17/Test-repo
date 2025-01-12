use serde_json::Value;

use crate::fields::ext::types::CellToStringOption;
use crate::fields::property::field_types::BasicValueType;
use crate::fields::text_base_field::TextBaseField;
use crate::prelude::view_operation::filter::{ConditionValue, FOperator};
use crate::prelude::CellValue;
use crate::so::api_value::ApiValue;

use super::base_field::IBaseField;

pub struct SingleText {
  field_conf: crate::prelude::FieldSO,
}

impl IBaseField for SingleText {
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
      if let Some(val) = arr.into_iter().nth(0) {
        return ApiValue::from(val.text);
      }
    }
    ApiValue::Null
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

impl SingleText {
  pub fn new(field_conf: crate::prelude::FieldSO) -> Self {
    return Self { field_conf };
  }

  // pub fn default_property() -> Option<FieldPropertySO> {
  // Some(SingleTextFieldPropertySO::default())
  //   None
  // }

  pub fn validate_add_open_field_property(_property: Value) -> anyhow::Result<()> {
    Ok(())
  }
}
