use serde_json::{Number, Value, from_value};

use crate::fields::date_time_base_field::DateTimeBaseField;
use crate::fields::property::field_types::BasicValueType;
use crate::prelude::view_operation::filter::{ConditionValue, FOperator};
use crate::prelude::CellValue;
use crate::so::api_value::ApiValue;
use crate::so::RecordSO;

use super::base_field::IBaseField;
use super::property::{LastModifiedTimeFieldPropertySO, FieldPropertySO};

pub struct LastModifiedTime {
  field_conf: crate::prelude::FieldSO,
}

impl IBaseField for LastModifiedTime {
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
      if let Some(field_updated_map) = record_meta.field_updated_map.as_ref() {
        let option = field_updated_map
          .values()
          .filter(|it| it.at.is_none() == false)
          .map(|it| it.at.unwrap())
          .max();
        if option.is_some() {
          return CellValue::from(option.unwrap() as f64);
        }
      }
    }
    CellValue::Null
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

impl LastModifiedTime {
  pub fn new(field_conf: crate::prelude::FieldSO) -> Self {
    return Self { field_conf };
  }

  pub fn validate_add_open_field_property(value: Value) -> anyhow::Result<()>{
    let property = from_value::<LastModifiedTimeFieldPropertySO>(value.clone());
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
      datasheet_id: default_property.datasheet_id.clone(),
      date_format: open_field_property.date_format,
      time_format: open_field_property.time_format.or(default_property.time_format),
      collect_type: open_field_property.collect_type.or(default_property.collect_type),
      field_id_collection: Some(open_field_property.field_id_collection.unwrap_or(Vec::new())),
      include_time: open_field_property.include_time.or(default_property.include_time),
      ..Default::default()
    }
  }
}
