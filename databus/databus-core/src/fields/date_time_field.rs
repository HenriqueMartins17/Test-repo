use std::rc::Rc;

use serde_json::{Number, Value, from_value};

use crate::fields::date_time_base_field::DateTimeBaseField;
use crate::fields::ext::types::CellToStringOption;
use crate::prelude::view_operation::filter::{ConditionValue, FOperator};
use crate::prelude::{CellValue, DatasheetPackContext};
use crate::so::api_value::ApiValue;

use super::property::DateTimeFieldPropertySO;
use super::{
  base_field::IBaseField,
  property::{field_types::BasicValueType, DateFormat, FieldPropertySO, TimeFormat},
};

pub struct DateTime {
  base: DateTimeBaseField,
}

impl IBaseField for DateTime {
  fn basic_value_type(&self) -> BasicValueType {
    BasicValueType::DateTime
  }

  fn inner_basic_value_type(&self) -> BasicValueType {
    BasicValueType::String
  }

  fn cell_value_to_string(&self, cell_value: CellValue, _option: Option<CellToStringOption>) -> Option<String> {
    self.base.cell_value_to_string(cell_value)
  }

  fn cell_value_to_api_standard_value(&self, cell_value: CellValue) -> ApiValue {
    if let CellValue::Number(value) = cell_value {
      return ApiValue::from(Number::from(value as i64));
    }
    ApiValue::Null
  }

  fn is_meet_filter(&self, operator: &FOperator, cell_value: &CellValue, condition_value: &ConditionValue) -> bool {
    DateTimeBaseField::is_meet_filter(operator, cell_value, condition_value)
  }
}

impl DateTime {
  pub fn new(field_conf: crate::prelude::FieldSO, state: Rc<DatasheetPackContext>) -> Self {
    return Self {
      base: DateTimeBaseField::new(field_conf, state),
    };
  }

  pub fn default_property() -> Option<FieldPropertySO> {
    Some(FieldPropertySO {
      date_format: Some(DateFormat::SYyyyMmDd),
      time_format: Some(TimeFormat::Hhmm),
      include_time: Some(false),
      auto_fill: Some(false),
      ..Default::default()
    })
  }

  pub fn validate_add_open_field_property(value: Value) -> anyhow::Result<()>{
    let property = from_value::<DateTimeFieldPropertySO>(value.clone());
    match property{
      Ok(_property) => {
        return Ok(());
      },
      Err(e) => {
        let str = format!("{}",e);
        if str.contains("dateFormat") {
          return Err(anyhow::Error::msg(format!("api_param_validate_error={}", e)));
        }
      }
    }
    Ok(())
  }

  pub fn update_open_field_property_transform_property(open_field_property: FieldPropertySO, default_property: FieldPropertySO) -> FieldPropertySO{
    FieldPropertySO {
      date_format: open_field_property.date_format,
      time_format: match open_field_property.time_format {
          Some(time_format) => Some(time_format),
          None => default_property.time_format,
      },
      auto_fill: open_field_property.auto_fill.or(default_property.auto_fill),
      include_time: open_field_property.include_time.or(default_property.include_time),
      ..Default::default()
    }
  }
}
