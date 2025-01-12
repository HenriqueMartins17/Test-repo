use std::collections::{HashMap, HashSet};
use std::rc::Rc;

use serde_json::Value;

use crate::fields::ext::types::CellToStringOption;
use crate::fields::member_base_field::{IMemberBaseField, MemberBaseField};
use crate::fields::property::field_types::BasicValueType;
use crate::prelude::view_operation::filter::{ConditionValue, FOperator};
use crate::prelude::{CellValue, DatasheetPackContext};
use crate::so::api_value::ApiValue;
use crate::so::{MemberValue, RecordSO};
use crate::types::field_api_enums::MemberType;
use crate::utils::utils::get_member_type_string;

use super::base_field::IBaseField;

pub struct CreatedBy {
  field: crate::prelude::FieldSO,
  context: Rc<DatasheetPackContext>,
}

impl IBaseField for CreatedBy {
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
    if let Some(uuid) = record.record_meta.clone().unwrap().created_by {
      return CellValue::String(uuid);
    }
    CellValue::Null
  }

  fn cell_value_to_string(&self, cell_value: CellValue, _option: Option<CellToStringOption>) -> Option<String> {
    MemberBaseField::cell_value_to_string(self, cell_value, _option)
  }

  fn cell_value_to_api_standard_value(&self, cell_value: CellValue) -> ApiValue {
    if let CellValue::String(uuid) = cell_value {
      let unit = self.context.user_map.get(&uuid);
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
    let cell_value = CellValue::from(self.get_unit_ids(cell_value));
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

impl IMemberBaseField for CreatedBy {
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

impl CreatedBy {
  pub fn new(field: crate::prelude::FieldSO, context: Rc<DatasheetPackContext>) -> Self {
    return Self { field, context };
  }

  pub fn get_uuids_by_record_map(record_map: HashMap<String, RecordSO>) -> Vec<Option<String>> {
    let mut uuids: HashSet<Option<String>> = HashSet::new();
    for record in record_map.values() {
      if let Some(created_by) = record.record_meta.clone().unwrap().created_by {
        uuids.insert(Some(created_by));
      }
    }
    uuids.into_iter().collect()
  }

  pub fn validate_add_open_field_property(_property: Value) -> anyhow::Result<()>{
    Ok(())
  }
}
