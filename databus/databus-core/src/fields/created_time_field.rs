use crate::fields::date_time_base_field::DateTimeBaseField;
use crate::fields::ext::types::CellToStringOption;
use crate::fields::property::field_types::BasicValueType;
use crate::prelude::view_operation::filter::{ConditionValue, FOperator};
use crate::prelude::{CellValue, DatasheetPackContext};
use crate::so::api_value::ApiValue;
use crate::so::RecordSO;
use serde_json::{Number, from_value, Value};
use std::rc::Rc;

use super::base_field::IBaseField;
use super::property::{CreatedTimeFieldPropertySO, FieldPropertySO};

pub struct CreatedTime {
  base: DateTimeBaseField,
}

impl IBaseField for CreatedTime {
  fn basic_value_type(&self) -> BasicValueType {
    BasicValueType::DateTime
  }

  fn inner_basic_value_type(&self) -> BasicValueType {
    BasicValueType::String
  }

  fn is_computed(&self) -> bool {
    true
  }

  fn get_cell_value(&self, record: &RecordSO) -> CellValue {
    if let Some(record_meta) = record.record_meta.as_ref() {
      return CellValue::from(record_meta.created_at.as_ref().unwrap().clone() as f64);
    }
    CellValue::Null
  }

  fn cell_value_to_string(&self, cell_value: CellValue, _option: Option<CellToStringOption>) -> Option<String> {
    self.base.cell_value_to_string(cell_value)
  }

  fn cell_value_to_api_standard_value(&self, cell_value: CellValue) -> ApiValue {
    if let CellValue::Number(created_at) = cell_value {
      return ApiValue::from(Number::from(created_at as i64));
    }
    ApiValue::Null
  }

  fn is_meet_filter(&self, operator: &FOperator, cell_value: &CellValue, condition_value: &ConditionValue) -> bool {
    DateTimeBaseField::is_meet_filter(operator, cell_value, condition_value)
  }
}

impl CreatedTime {
  pub fn new(field_conf: crate::prelude::FieldSO, state: Rc<DatasheetPackContext>) -> Self {
    return Self {
      base: DateTimeBaseField::new(field_conf, state),
    };
  }

  pub fn validate_add_open_field_property(value: Value) -> anyhow::Result<()>{
    let property = from_value::<CreatedTimeFieldPropertySO>(value.clone());
    match property{
      Ok(_property) => {
        return Ok(());
      },
      Err(e) => {
        let str = format!("{}",e);
        println!("zzq see str {}", str);
        if str.contains("dateFormat") {
          return Err(anyhow::Error::msg(format!("api_param_validate_error={}", e)));
        }
      }
    }
    Ok(())
  }

  pub fn update_open_field_property_transform_property(open_field_property: FieldPropertySO, default_property: FieldPropertySO) -> FieldPropertySO{
    FieldPropertySO {
      datasheet_id: default_property.datasheet_id.clone(),
      date_format: match open_field_property.date_format {
          Some(date_format) => Some(date_format),
          None => default_property.date_format,
      },
      time_format: match open_field_property.time_format {
          Some(time_format) => Some(time_format),
          None => default_property.time_format,
      },
      include_time: open_field_property.include_time.or(default_property.include_time),
      ..Default::default()
    }
  }
}
