use serde_json::{Value, from_value};

use crate::fields::base_field::BaseField;
use crate::fields::field_factory::FieldFactory;
use crate::modules::database::store::selectors::resource::datasheet::cell_calc::get_cell_value;
use crate::prelude::view_operation::filter::{ConditionValue, FOperator};
use crate::prelude::{CellValue, DatasheetPackContext};
use crate::so::api_value::ApiValue;
use crate::so::FieldSO;
use std::rc::Rc;

use super::property::OneWayLinkFieldPropertySO;
use super::{
  base_field::IBaseField,
  property::{field_types::BasicValueType, FieldPropertySO},
};

pub struct OneWayLinkField {
  field: FieldSO,
  context: Rc<DatasheetPackContext>,
}

impl IBaseField for OneWayLinkField {
  fn basic_value_type(&self) -> BasicValueType {
    BasicValueType::Array
  }

  fn inner_basic_value_type(&self) -> BasicValueType {
    BasicValueType::String
  }

  fn cell_value_to_api_standard_value(&self, cell_value: CellValue) -> ApiValue {
    if let Some(arr) = cell_value.to_string_array() {
      return ApiValue::from(arr.into_iter().map(|it| it).collect::<Vec<_>>());
    }
    ApiValue::Null
  }

  fn is_meet_filter(&self, operator: &FOperator, cell_value: &CellValue, condition_value: &ConditionValue) -> bool {
    if operator == &FOperator::IsEmpty {
      return cell_value.is_null();
    }
    if operator == &FOperator::IsNotEmpty {
      return !cell_value.is_null();
    }
    let cell_text_array = self.cell_value_to_text_array(cell_value);
    let filter_value = condition_value.as_string().unwrap();

    return match operator {
      FOperator::Contains => {
        !cell_value.is_null()
          && cell_text_array
            .iter()
            .any(|text| BaseField::string_include(text, filter_value))
      }
      FOperator::DoesNotContain => {
        !cell_value.is_null()
          || !cell_text_array
            .iter()
            .any(|text| BaseField::string_include(text, filter_value))
      }
      _ => BaseField::is_meet_filter(operator, cell_value, condition_value),
    };
  }
}

impl OneWayLinkField {
  pub fn new(field: FieldSO, context: Rc<DatasheetPackContext>) -> Self {
    return Self { field, context };
  }

  pub fn default_property() -> Option<FieldPropertySO> {
    Some(FieldPropertySO { ..Default::default() })
  }

  pub fn cell_value_to_text_array(&self, cell_value: &CellValue) -> Vec<String> {
    if let Some(cell_values) = cell_value.as_string_array() {
      cell_values
        .into_iter()
        .map(|record_id| {
          self
            .get_linked_record_cell_string(record_id)
            .unwrap_or_else(|| "".to_string())
        })
        .collect()
    } else {
      vec![]
    }
  }

  fn get_linked_record_cell_string(&self, record_id: &str) -> Option<String> {
    if let Some(field) = self.get_foreign_primary_field() {
      let cell_value = self.get_linked_cell_value(Some(record_id));
      return FieldFactory::create_field(field, self.context.clone()).cell_value_to_string(cell_value, None);
    }
    None
  }

  fn get_foreign_primary_field(&self) -> Option<FieldSO> {
    let foreign_datasheet_id = self
      .field
      .property
      .as_ref()
      .unwrap()
      .foreign_datasheet_id
      .as_ref()
      .unwrap();
    if let Some(view) = self.context.get_first_view(foreign_datasheet_id) {
      if let Some(column) = view.columns.get(0) {
        let field_id = &column.field_id;
        if let Some(foreign_primary_field) = self.context.get_field_map(&foreign_datasheet_id) {
          return foreign_primary_field.get(field_id).cloned();
        }
      }
    }

    None
  }

  fn get_linked_cell_value(&self, record_id: Option<&str>) -> CellValue {
    // 实现 getLinkedCellValue 方法的逻辑
    if let Some(record_id) = record_id {
      if let Some(field) = self.get_foreign_primary_field() {
        if let Some(snapshot) = self.context.get_snapshot(
          self
            .field
            .property
            .as_ref()
            .unwrap()
            .foreign_datasheet_id
            .as_ref()
            .unwrap()
            .as_str(),
        ) {
          return get_cell_value(self.context.clone(), snapshot, record_id, &field.id);
        }
      }
    }
    CellValue::Null
  }

  pub fn validate_add_open_field_property(value: Value) -> anyhow::Result<()>{
    let property = from_value::<OneWayLinkFieldPropertySO>(value.clone());
    match property{
      Ok(_property) => {
        return Ok(());
      },
      Err(e) => {
        return Err(anyhow::Error::msg(format!("api_param_validate_error={}", e)));
      }
    }
  }

  pub fn update_open_field_property_transform_property(open_field_property: FieldPropertySO, _default_property: FieldPropertySO) -> FieldPropertySO{
    FieldPropertySO {
      foreign_datasheet_id: open_field_property.foreign_datasheet_id,
      limit_to_view: open_field_property.limit_to_view,
      limit_single_record: open_field_property.limit_single_record,
      ..Default::default()
    }
  }
}
