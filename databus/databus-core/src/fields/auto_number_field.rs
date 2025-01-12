use crate::fields::number_base_field::NumberBaseField;
use crate::fields::property::field_types::BasicValueType;
use crate::prelude::view_operation::filter::{ConditionValue, FOperator};
use crate::so::api_value::ApiValue;
use crate::so::{CellValue, RecordSO};
use serde_json::Number;

use super::base_field::IBaseField;

pub struct AutoNumber {
  field_conf: crate::prelude::FieldSO,
}

impl IBaseField for AutoNumber {
  fn basic_value_type(&self) -> BasicValueType {
    BasicValueType::Number
  }

  fn inner_basic_value_type(&self) -> BasicValueType {
    BasicValueType::String
  }

  fn is_computed(&self) -> bool {
    true
  }

  fn get_cell_value(&self, record: &RecordSO) -> CellValue {
    match &record.record_meta {
      None => CellValue::Null,
      Some(record_meta) => CellValue::from(
        record_meta.field_updated_map.as_ref().unwrap()[&self.field_conf.id]
          .auto_number
          .unwrap() as f64,
      ),
    }
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

impl AutoNumber {
  pub fn new(field_conf: crate::prelude::FieldSO) -> Self {
    return Self { field_conf };
  }
}
