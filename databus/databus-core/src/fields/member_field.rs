use serde_json::Value;

use crate::fields::member_base_field::MemberBaseField;
use std::ops::Deref;
use std::rc::Rc;

use crate::fields::ext::types::CellToStringOption;
use crate::fields::member_base_field::IMemberBaseField;
use crate::fields::property::field_types::BasicValueType;
use crate::prelude::view_operation::filter::{ConditionValue, FOperator};
use crate::prelude::{CellValue, DatasheetPackContext};
use crate::so::api_value::ApiValue;
use crate::so::MemberValue;
use crate::types::field_api_enums::MemberType;
use crate::utils::utils::get_member_type_string;

use super::base_field::IBaseField;
use super::property::FieldPropertySO;

pub struct Member {
  field: crate::prelude::FieldSO,
  context: Rc<DatasheetPackContext>,
}

impl IBaseField for Member {
  fn basic_value_type(&self) -> BasicValueType {
    BasicValueType::Array
  }

  fn inner_basic_value_type(&self) -> BasicValueType {
    BasicValueType::String
  }

  fn cell_value_to_string(&self, cell_value: CellValue, _option: Option<CellToStringOption>) -> Option<String> {
    MemberBaseField::cell_value_to_string(self, cell_value, _option)
  }

  fn cell_value_to_api_standard_value(&self, cell_value: CellValue) -> ApiValue {
    if let Some(arr) = cell_value.to_string_array() {
      let result = arr
        .into_iter()
        .map(|it| self.context.unit_map.get(&it))
        .filter(|it| it.is_some())
        .map(|it| {
          let unit = it.unwrap().deref();
          let r#type = unit.r#type.unwrap();
          MemberValue {
            id: unit.clone().unit_id.unwrap(),
            unit_id: unit.clone().original_unit_id.unwrap(),
            name: unit.clone().name.unwrap(),
            r#type: get_member_type_string(MemberType::from_repr(r#type).unwrap()),
            avatar: unit.clone().avatar,
          }
        })
        .collect::<Vec<MemberValue>>();

      return ApiValue::from(result);
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

    MemberBaseField::is_meet_filter(operator, cell_value, &ConditionValue::StringArray(condition_value))
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

impl IMemberBaseField for Member {
  fn get_unit_names(&self, cell_value: Vec<String>) -> Option<Vec<String>> {
    let unit_map = &self.context.unit_map;

    let names = cell_value
      .into_iter()
      .map(|uuid| {
        let name = unit_map
          .get(&uuid)
          .map(|user| user.name.clone().unwrap_or("".to_string()));

        name.unwrap_or("".to_string())
      })
      .collect::<Vec<_>>();

    Some(names)
  }
}

impl Member {
  pub fn new(field: crate::prelude::FieldSO, context: Rc<DatasheetPackContext>) -> Self {
    return Self { field, context };
  }

  pub fn validate_add_open_field_property(_property: Value) -> anyhow::Result<()>{
    Ok(())
  }

  pub fn update_open_field_property_transform_property(open_field_property: FieldPropertySO, default_property: FieldPropertySO) -> FieldPropertySO{
    FieldPropertySO {
      is_multi: open_field_property.is_multi.or(default_property.is_multi),
      should_send_msg: open_field_property.should_send_msg.or(default_property.should_send_msg),
      subscription: open_field_property.subscription.or(default_property.subscription),
      unit_ids: Some(Vec::new()),
      ..Default::default()
    }
  }
}
