use serde_json::{to_value, Value};

use databus_core::prelude::{AttachmentValue, MemberValue, UrlValue};

/// Value types of addable and updatable field
#[derive(Debug, PartialEq)]
pub enum FieldValue {
  SingleLineText(String),
  MultiLineText(String),
  Select(String),
  MultiSelect(Vec<String>),
  Number(f64),
  Currency(f64),
  Percent(f64),
  Date(i64),
  Attachment(Vec<AttachmentValue>),
  Member(Vec<MemberValue>),
  Checkbox(bool),
  Rating(i64),
  Url(UrlValue),
  Phone(String),
  Email(String),
  OneWayLink(Vec<String>),
  TwoWayLink(Vec<String>),
  Cascader(String),
}

impl FieldValue {
  pub fn to_json_value(&self) -> Value {
    return match self {
      FieldValue::SingleLineText(v) => to_value(v).unwrap(),
      FieldValue::MultiLineText(v) => to_value(v).unwrap(),
      FieldValue::Select(v) => to_value(v).unwrap(),
      FieldValue::MultiSelect(v) => to_value(v).unwrap(),
      FieldValue::Number(v) => to_value(v).unwrap(),
      FieldValue::Currency(v) => to_value(v).unwrap(),
      FieldValue::Percent(v) => to_value(v).unwrap(),
      FieldValue::Date(v) => to_value(v).unwrap(),
      FieldValue::Attachment(v) => to_value(v).unwrap(),
      FieldValue::Member(v) => to_value(v).unwrap(),
      FieldValue::Checkbox(v) => to_value(v).unwrap(),
      FieldValue::Rating(v) => to_value(v).unwrap(),
      FieldValue::Url(v) => to_value(v).unwrap(),
      FieldValue::Phone(v) => to_value(v).unwrap(),
      FieldValue::Email(v) => to_value(v).unwrap(),
      FieldValue::OneWayLink(v) => to_value(v).unwrap(),
      FieldValue::TwoWayLink(v) => to_value(v).unwrap(),
      FieldValue::Cascader(v) => to_value(v).unwrap(),
    };
  }
}
