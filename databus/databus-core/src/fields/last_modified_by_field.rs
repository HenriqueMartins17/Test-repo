use serde_json::Value;

use crate::fields::member_base_field::MemberBaseField;
use std::rc::Rc;

use crate::fields::ext::types::CellToStringOption;
use crate::fields::member_base_field::IMemberBaseField;
use crate::fields::property::field_types::BasicValueType;
use crate::fields::property::CollectType;
use crate::prelude::view_operation::filter::{ConditionValue, FOperator};
use crate::prelude::{CellValue, DatasheetPackContext};
use crate::so::api_value::ApiValue;
use crate::so::{MemberValue, RecordSO};
use crate::types::field_api_enums::MemberType;
use crate::utils::utils::get_member_type_string;

use super::base_field::IBaseField;
use super::property::FieldPropertySO;

pub struct LastModifiedBy {
  field: crate::prelude::FieldSO,
  context: Rc<DatasheetPackContext>,
}

impl IBaseField for LastModifiedBy {
  fn basic_value_type(&self) -> BasicValueType {
    BasicValueType::String
  }

  fn inner_basic_value_type(&self) -> BasicValueType {
    BasicValueType::String
  }

  fn is_computed(&self) -> bool {
    true
  }

  fn get_cell_value(&self, record: &RecordSO) -> CellValue {
    let property = self
      .field
      .clone()
      .property
      .unwrap()
      .to_last_modified_by_field_property();
    let collect_type = property.collect_type;
    let field_id_collection = &property.field_id_collection;
    let updated_map = record.clone().record_meta.unwrap().field_updated_map;

    // Depends on field_updated_map, otherwise returns None
    if updated_map.is_none() {
      return CellValue::Null;
    }

    let is_all_field = collect_type == CollectType::AllFields;
    let field_ids = if is_all_field {
      updated_map.as_ref().unwrap().keys().cloned().collect()
    } else {
      field_id_collection.clone()
    };

    let target_file_update = updated_map
      .as_ref()
      .unwrap()
      .iter()
      .filter(|(field_id, _field_updated_value)| field_ids.contains(field_id))
      .map(|(_field_id, field_updated_value)| field_updated_value)
      .filter(|it| it.at.is_some() && it.by.is_some())
      .max_by_key(|it| it.at.unwrap());

    if let Some(target_file_update) = target_file_update {
      let by = target_file_update.clone().by.unwrap();
      return CellValue::String(by);
    }
    CellValue::Null
  }

  fn cell_value_to_string(&self, cell_value: CellValue, _option: Option<CellToStringOption>) -> Option<String> {
    MemberBaseField::cell_value_to_string(self, cell_value, _option)
  }

  fn cell_value_to_api_standard_value(&self, cell_value: CellValue) -> ApiValue {
    if let CellValue::String(value) = cell_value {
      let unit = self.context.user_map.get(&value);
      if let Some(unit) = unit {
        let r#type = unit.r#type.as_ref().unwrap().clone();
        let member = MemberValue {
          id: unit.user_id.as_ref().unwrap().clone(),
          unit_id: unit.original_unit_id.as_ref().unwrap().clone(),
          name: unit.name.as_ref().unwrap().clone(),
          r#type: get_member_type_string(MemberType::from_repr(r#type).unwrap()),
          avatar: unit.avatar.clone(),
        };
        return ApiValue::from(member);
      }
    }
    ApiValue::Null
  }

  fn is_meet_filter(&self, operator: &FOperator, cell_value: &CellValue, condition_value: &ConditionValue) -> bool {
    let condition_value = if condition_value.is_null() {
      vec![]
    } else {
      condition_value.clone().as_string_array().unwrap()
    };
    let condition_value = MemberBaseField::condition_value_to_real(&self.field, &self.context, condition_value);
    let cell_value = CellValue::from(Self::get_unit_ids(cell_value.clone()));
    MemberBaseField::is_meet_filter(operator, &cell_value, &ConditionValue::StringArray(condition_value))
  }

  fn compare(
    &self,
    cell_value1: &CellValue,
    cell_value2: &CellValue,
    order_in_cell_value_sensitive: Option<bool>,
  ) -> i32 {
    MemberBaseField::compare(self, self, cell_value1, cell_value2, order_in_cell_value_sensitive)
  }

  fn cell_value_to_array(&self, cell_value: CellValue) -> CellValue {
    MemberBaseField::cell_value_to_array(self, cell_value)
  }

  fn array_value_to_string(&self, cell_value: CellValue) -> Option<String> {
    MemberBaseField::array_value_to_string(cell_value)
  }
}

impl IMemberBaseField for LastModifiedBy {
  fn get_unit_names(&self, cell_value: Vec<String>) -> Option<Vec<String>> {
    let user_map = &self.context.user_map;

    let names = cell_value
      .into_iter()
      .map(|uuid| {
        let name = user_map
          .get(&uuid)
          .map(|user| user.name.clone().unwrap_or("".to_string()));

        name.unwrap_or("".to_string())
      })
      .collect::<Vec<_>>();

    Some(names)
  }
}

impl LastModifiedBy {
  pub fn new(field: crate::prelude::FieldSO, context: Rc<DatasheetPackContext>) -> Self {
    return Self { field, context };
  }

  pub fn get_unit_ids(cell_value: CellValue) -> Option<Vec<String>> {
    if cell_value.is_null() {
      return None;
    }
    Some(vec![cell_value.as_str().unwrap().to_string()])
  }

  pub fn validate_add_open_field_property(_property: Value) -> anyhow::Result<()>{
    Ok(())
  }

  pub fn update_open_field_property_transform_property(open_field_property: FieldPropertySO, default_property: FieldPropertySO) -> FieldPropertySO{
    FieldPropertySO {
      uuids: Some(default_property.uuids.unwrap_or(Vec::new())),
      datasheet_id: default_property.datasheet_id,
      collect_type: open_field_property.collect_type.or(default_property.collect_type),
      field_id_collection: Some(open_field_property.field_id_collection.unwrap_or(Vec::new())),
      ..Default::default()
    }
  }
}
