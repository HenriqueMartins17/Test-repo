use std::any::type_name;
use std::cmp::Ordering;

pub use serde_json::Value;

use crate::fields::ext::types::CellToStringOption;
use crate::fields::property::field_types::BasicValueType;
use crate::logic::CellFormatEnum;
use crate::ot::commands::IStandardValue;
use crate::prelude::view_operation::filter::FOperator;
use crate::prelude::{CellValue, RecordSO};
use crate::so::api_value::ApiValue;
use crate::so::view_operation::filter::ConditionValue;
use crate::so::{FieldKindSO, FieldSO};
use crate::transformer::fusion_api_transformer::IFieldVoTransformOptions;

#[allow(unused_variables)]
pub trait IBaseField {
  fn basic_value_type(&self) -> BasicValueType;
  fn inner_basic_value_type(&self) -> BasicValueType;
  fn default_value(&self) -> Option<Value> {
    None
  }
  fn is_computed(&self) -> bool {
    false
  }

  fn get_cell_value(&self, record: &RecordSO) -> CellValue {
    CellValue::Null
  }

  fn cell_value_to_string(&self, cell_value: CellValue, _option: Option<CellToStringOption>) -> Option<String> {
    None
  }

  fn cell_value_to_api_standard_value(&self, cell_value: CellValue) -> ApiValue {
    ApiValue::Null
  }

  fn cell_value_to_api_string_value(&self, cell_value: CellValue) -> Option<String> {
    None
  }

  fn validate(&self, cell_value: &Value) -> bool {
    true
  }

  fn cell_value_to_std_value(&self, cell_value: Value) -> IStandardValue {
    IStandardValue::default()
  }

  fn std_value_to_cell_value(&self, std_value: IStandardValue) -> Value {
    Value::Null
  }
  /**
   * beside the operator calc function  that check whether empty,
   * other functions need to implement themselves
   * when no inheritance is needed, the default is true, which means no filtering
   */
  fn is_meet_filter(&self, operator: &FOperator, cell_value: &CellValue, condition_value: &ConditionValue) -> bool {
    BaseField::is_meet_filter(operator, cell_value, condition_value)
  }

  fn eq(&self, cv1: &CellValue, cv2: &CellValue) -> bool {
    cv1 == cv2
  }

  fn compare(
    &self,
    cell_value1: &CellValue,
    cell_value2: &CellValue,
    order_in_cell_value_sensitive: Option<bool>,
  ) -> i32 {
    // TODO remove this when all inheritance
    if 1 == 1 {
      return -1;
    }
    panic!("{} field not implement compare function", type_name::<Self>())
  }

  fn is_empty_or_not(&self, operator: &FOperator, cell_value: &CellValue) -> bool {
    BaseField::is_empty_or_not(operator, cell_value)
  }

  fn vo_transform(&self, cell_value: CellValue, field: FieldSO, options: IFieldVoTransformOptions) -> ApiValue {
    if options.cell_format == Some(CellFormatEnum::String) {
      // return self.cell_value_to_api_string_value(cell_value);
    }
    self.cell_value_to_api_standard_value(cell_value)
  }

  fn validate_add_open_field_property(&self, add_property: Value, kind: FieldKindSO) -> anyhow::Result<()> {
    self.validate_update_open_property(add_property, kind)
  }

  fn validate_update_open_property(&self, _update_property: Value, kind: FieldKindSO) -> anyhow::Result<()> {
    Err(anyhow::Error::msg(format!(
      "{} not support set property",
      kind.to_string()
    )))
  }

  /// array field only
  fn cell_value_to_array(&self, cell_value: CellValue) -> CellValue {
    CellValue::Null
  }

  fn array_value_to_string(&self, cell_value: CellValue) -> Option<String> {
    None
  }
}

pub struct BaseField;

impl BaseField {
  pub fn compare(
    self_dyn: &dyn IBaseField,
    cell_value1: &CellValue,
    cell_value2: &CellValue,
    order_in_cell_value_sensitive: Option<bool>,
  ) -> i32 {
    if self_dyn.eq(cell_value1, cell_value2) {
      return 0;
    }
    if *cell_value1 == CellValue::Null {
      return -1;
    }
    if *cell_value2 == CellValue::Null {
      return 1;
    }

    let str1 = self_dyn.cell_value_to_string(
      cell_value1.clone(),
      Some(CellToStringOption::new(None, None, order_in_cell_value_sensitive)),
    );
    let str2 = self_dyn.cell_value_to_string(
      cell_value2.clone(),
      Some(CellToStringOption::new(None, None, order_in_cell_value_sensitive)),
    );

    if str1 == str2 {
      return 0;
    }
    if str1.is_none() {
      return -1;
    }
    if str2.is_none() {
      return 1;
    }
    let binding = str1.unwrap();
    let str1 = binding.trim();
    let binding = str2.unwrap();
    let str2 = binding.trim();

    // test pinyin sort
    return if str1 == str2 {
      0
    } else {
      match str1.partial_cmp(str2).unwrap() {
        Ordering::Less => -1,
        Ordering::Equal => 0,
        Ordering::Greater => 1,
      }
    };
  }

  pub fn is_meet_filter(operator: &FOperator, cell_value: &CellValue, _condition_value: &ConditionValue) -> bool {
    return match operator {
      FOperator::IsEmpty | FOperator::IsNotEmpty => Self::is_empty_or_not(operator, cell_value),
      _ => {
        println!("Method should be overwritten!");
        true
      }
    };
  }

  pub fn is_empty_or_not(operator: &FOperator, cell_value: &CellValue) -> bool {
    return match operator {
      FOperator::IsEmpty => *cell_value == CellValue::Null,
      FOperator::IsNotEmpty => *cell_value != CellValue::Null,
      _ => panic!("compare operator type error"),
    };
  }

  pub fn string_include(str: &str, search_str: &str) -> bool {
    str.to_lowercase().contains(&search_str.trim().to_lowercase())
  }
}

// impl_downcast!(IBaseField);
