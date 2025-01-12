use crate::fields::ext::types::CellToStringOption;
use crate::fields::property::field_types::BasicValueType;
use crate::fields::property::CascaderFieldPropertySO;
use crate::fields::text_base_field::TextBaseField;
use crate::prelude::view_operation::filter::{ConditionValue, FOperator};
use crate::prelude::CellValue;
use crate::so::api_value::ApiValue;

use super::base_field::IBaseField;

pub struct Cascader {
  field: crate::prelude::FieldSO,
  property: CascaderFieldPropertySO,
}

impl IBaseField for Cascader {
  fn basic_value_type(&self) -> BasicValueType {
    BasicValueType::String
  }

  fn inner_basic_value_type(&self) -> BasicValueType {
    BasicValueType::String
  }

  fn cell_value_to_string(&self, cell_value: CellValue, _option: Option<CellToStringOption>) -> Option<String> {
    if cell_value.is_null() {
      return None;
    }
    let show_all = &self.property.show_all;
    return Some(
      cell_value
        .to_cascader_array()
        .unwrap()
        .into_iter()
        .map(|seg| {
          return if !show_all {
            seg.text.split('/').last().unwrap().to_string()
          } else {
            seg.text
          };
        })
        .collect::<Vec<String>>()
        .join("")
        .to_string(),
    );
  }

  fn cell_value_to_api_standard_value(&self, cell_value: CellValue) -> ApiValue {
    let property = &self.property;

    if let Some(arr) = cell_value.to_cascader_array() {
      return arr
        .into_iter()
        .nth(0)
        .map(|val| {
          let text = val.text;
          return if property.show_all {
            ApiValue::from(text)
          } else {
            ApiValue::from(text.split('/').last().unwrap())
          };
        })
        .unwrap_or(ApiValue::Null);
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

impl Cascader {
  pub fn new(field_conf: crate::prelude::FieldSO) -> Self {
    return Self {
      field: field_conf.clone(),
      property: field_conf
        .property
        .clone()
        .as_ref()
        .unwrap()
        .clone()
        .to_cascader_field_property(),
    };
  }
}
