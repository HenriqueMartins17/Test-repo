use crate::fields::property::field_types::BasicValueType;

use super::base_field::IBaseField;

pub struct EmptyField {
  field_conf: crate::prelude::FieldSO,
}

impl IBaseField for EmptyField {
  fn basic_value_type(&self) -> BasicValueType {
    BasicValueType::String
  }

  fn inner_basic_value_type(&self) -> BasicValueType {
    BasicValueType::String
  }
}

impl EmptyField {
  pub fn new(field_conf: crate::prelude::FieldSO) -> Self {
    return Self { field_conf };
  }
}
