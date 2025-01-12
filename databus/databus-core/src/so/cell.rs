use std::borrow::Borrow;
use std::fmt::Display;
use std::ops::{Add, Div, Mul, Rem, Sub};
use std::str::FromStr;

use derivative::Derivative;
use serde::{Deserialize, Serialize};
use serde_json::{Number, Value};
use utoipa::ToSchema;

use crate::prelude::FieldKindSO;
use crate::so::view_operation::filter::ConditionValue;
use crate::so::FieldSO;

#[derive(Deserialize, Serialize, Debug, Clone)]
#[serde(rename_all = "camelCase")]
pub enum CellValue {
  Null,
  Number(f64),
  Bool(bool),
  String(String),
  Array(Vec<Self>),

  Text(TextValue),
  URL(UrlValue),
  Email(EmailValue),
  Phone(PhoneValue),
  Cascader(CascaderValue),
  Attachment(AttachmentValue),

  LookUpTree(Box<LookUpTreeValue>),
}

impl From<ConditionValue> for CellValue {
  fn from(f: ConditionValue) -> Self {
    match f {
      ConditionValue::Bool(v) => CellValue::from(v),
      // ConditionValue::IFilterDateTime(_) => {}
      // ConditionValue::Value(_) => {}
      ConditionValue::Null => CellValue::Null,
      _ => CellValue::Null,
    }
  }
}

impl<T> From<Option<T>> for CellValue
where
  T: Into<CellValue>,
{
  fn from(opt: Option<T>) -> Self {
    match opt {
      None => CellValue::Null,
      Some(value) => Into::into(value),
    }
  }
}

impl From<bool> for CellValue {
  fn from(f: bool) -> Self {
    CellValue::Bool(f)
  }
}

impl From<String> for CellValue {
  fn from(f: String) -> Self {
    CellValue::String(f)
  }
}

impl<'a> From<&'a str> for CellValue {
  fn from(f: &str) -> Self {
    CellValue::String(f.to_string())
  }
}

impl From<Vec<String>> for CellValue {
  fn from(f: Vec<String>) -> Self {
    CellValue::Array(f.into_iter().map(|v| CellValue::from(v)).collect())
  }
}

impl From<Vec<&str>> for CellValue {
  fn from(f: Vec<&str>) -> Self {
    CellValue::Array(f.into_iter().map(|v| CellValue::from(v)).collect())
  }
}

impl From<Vec<AttachmentValue>> for CellValue {
  fn from(f: Vec<AttachmentValue>) -> Self {
    CellValue::Array(f.into_iter().map(|v| CellValue::Attachment(v)).collect())
  }
}

impl From<f64> for CellValue {
  fn from(f: f64) -> Self {
    CellValue::Number(f)
  }
}

impl From<Vec<UrlValue>> for CellValue {
  fn from(f: Vec<UrlValue>) -> Self {
    CellValue::Array(f.into_iter().map(|v| CellValue::URL(v)).collect())
  }
}

impl From<Vec<CellValue>> for CellValue {
  fn from(f: Vec<CellValue>) -> Self {
    CellValue::Array(f)
  }
}

impl CellValue {
  pub fn from_value(data: &Value, field_kind: &FieldKindSO) -> CellValue {
    if data.is_null() {
      return CellValue::Null;
    }
    return match &field_kind {
      FieldKindSO::Text | FieldKindSO::SingleText => match data {
        Value::Array(arr) => {
          let value = arr
            .iter()
            .map(|v| CellValue::Text(serde_json::from_value(v.clone()).unwrap()))
            .collect();
          CellValue::Array(value)
        }
        _ => CellValue::Null,
      },
      FieldKindSO::URL => match data {
        Value::Array(arr) => {
          let value = arr
            .iter()
            .map(|v| CellValue::URL(serde_json::from_value(v.clone()).unwrap()))
            .collect();
          CellValue::Array(value)
        }
        _ => CellValue::Null,
      },
      FieldKindSO::Email => match data {
        Value::Array(arr) => {
          let value = arr
            .iter()
            .map(|v| CellValue::Email(serde_json::from_value(v.clone()).unwrap()))
            .collect();
          CellValue::Array(value)
        }
        _ => CellValue::Null,
      },
      FieldKindSO::Phone => match data {
        Value::Array(arr) => {
          let value = arr
            .iter()
            .map(|v| CellValue::Phone(serde_json::from_value(v.clone()).unwrap()))
            .collect();
          CellValue::Array(value)
        }
        _ => CellValue::Null,
      },
      FieldKindSO::Cascader => match data {
        Value::Array(arr) => {
          let value = arr
            .iter()
            .map(|v| CellValue::Cascader(serde_json::from_value(v.clone()).unwrap()))
            .collect();
          CellValue::Array(value)
        }
        _ => CellValue::Null,
      },
      FieldKindSO::Attachment => match data {
        Value::Array(arr) => {
          let value = arr
            .iter()
            .map(|v| CellValue::Attachment(serde_json::from_value(v.clone()).unwrap()))
            .collect();
          CellValue::Array(value)
        }
        _ => CellValue::Null,
      },
      FieldKindSO::LookUp => CellValue::Array(serde_json::from_value(data.clone()).unwrap()),
      FieldKindSO::DateTime => match data {
        Value::Number(num) => CellValue::Number(to_f64(num)),
        _ => CellValue::Null,
      },
      FieldKindSO::Number
      | FieldKindSO::Rating
      | FieldKindSO::Currency
      | FieldKindSO::Percent
      | FieldKindSO::AutoNumber
      | FieldKindSO::CreatedTime
      | FieldKindSO::LastModifiedTime => CellValue::Number(serde_json::from_value(data.clone()).unwrap()),
      FieldKindSO::SingleSelect | FieldKindSO::CreatedBy | FieldKindSO::LastModifiedBy => {
        CellValue::String(serde_json::from_value(data.clone()).unwrap())
      }
      FieldKindSO::Member => {
        return match data {
          Value::Array(arr) => {
            let value = arr
              .iter()
              .map(|v| {
                // compatibility,  old function polyfillOldData
                CellValue::String(if v.is_object() {
                  v.clone()
                    .as_object()
                    .unwrap()
                    .get("unitId")
                    .unwrap()
                    .as_str()
                    .unwrap()
                    .to_string()
                } else {
                  v.clone().as_str().unwrap().to_string()
                })
              })
              .collect();
            CellValue::Array(value)
          }
          _ => CellValue::Null,
        };
      }
      FieldKindSO::MultiSelect | FieldKindSO::Link | FieldKindSO::OneWayLink => {
        return match data {
          Value::Array(arr) => {
            let value = arr
              .iter()
              .map(|v| CellValue::String(serde_json::from_value(v.clone()).unwrap()))
              .collect();
            CellValue::Array(value)
          }
          _ => CellValue::Null,
        };
      }
      FieldKindSO::Checkbox => CellValue::Bool(serde_json::from_value(data.clone()).unwrap()),
      _ => CellValue::Null,
    };
  }

  pub fn len(&self) -> usize {
    return match self {
      Self::String(string) => string.len(),
      Self::Array(arr) => arr.len(),
      _ => 0,
    };
  }

  pub fn is_true(&self) -> bool {
    return match self {
      Self::Null => false,
      Self::String(val) => val.len() > 0,
      Self::Number(val) => *val != 0.0,
      Self::Bool(val) => *val,
      Self::Array(_) => true,
      Self::Text(_) => true,
      Self::URL(_) => true,
      Self::Email(_) => true,
      Self::Phone(_) => true,
      Self::Cascader(_) => true,
      Self::Attachment(_) => true,
      Self::LookUpTree(_) => true,
    };
  }

  pub fn is_array(&self) -> bool {
    return match self {
      CellValue::Array(_) => true,
      _ => false,
    };
  }

  pub fn is_object(&self) -> bool {
    match self {
      CellValue::Null => false,
      CellValue::Number(_) => false,
      CellValue::Bool(_) => false,
      CellValue::String(_) => false,
      _ => true,
    }
  }

  pub fn is_number(&self) -> bool {
    return match self {
      Self::Number(_) => true,
      _ => false,
    };
  }

  pub fn is_string(&self) -> bool {
    return match self {
      Self::String(_) => true,
      _ => false,
    };
  }

  pub fn is_null(&self) -> bool {
    match self {
      CellValue::Null => true,
      _ => false,
    }
  }

  pub fn to_number(&self) -> f64 {
    return match self {
      Self::Number(val) => *val,
      Self::String(val) => f64::from_str(val).unwrap_or(f64::NAN),
      Self::Null => 0.0,
      _ => f64::NAN,
    };
  }

  pub fn flat(self, depth: i32) -> CellValue {
    if depth <= 0 {
      return self;
    }

    return if let CellValue::Array(arr) = self {
      let mut flat_array = Vec::new();
      for item in arr {
        if item.is_array() {
          let flattened = item.flat(depth - 1);
          if let CellValue::Array(inner_items) = flattened {
            flat_array.extend(inner_items);
          }
        } else {
          flat_array.push(item);
        }
      }

      CellValue::Array(flat_array)
    } else {
      panic!("not support type")
    };
  }

  pub fn get_text(&self) -> Option<String> {
    return match self {
      Self::Text(val) => Some(val.text.clone()),
      Self::URL(val) => Some(val.text.clone()),
      Self::Email(val) => Some(val.text.clone()),
      Self::Phone(val) => Some(val.text.clone()),
      Self::Cascader(val) => Some(val.text.clone()),
      _ => None,
    };
  }

  pub fn take_by_index(mut self, index: usize) -> (Self, Self) {
    match self {
      Self::Array(ref mut array) => {
        if index < array.len() {
          let value = array.remove(index);
          (self, value)
        } else {
          (self, CellValue::Null)
        }
      }
      _ => (self, CellValue::Null),
    }
  }

  pub fn as_i64(&self) -> Option<i64> {
    if let CellValue::Number(v) = self {
      return Some(*v as i64);
    }
    return None;
  }

  pub fn as_f64(&self) -> Option<f64> {
    match self {
      CellValue::Number(n) => Some(*n),
      _ => None,
    }
  }

  pub fn as_bool(&self) -> Option<bool> {
    if let CellValue::Bool(v) = *self {
      return Some(v);
    }
    return None;
  }

  pub fn as_string(&self) -> Option<&String> {
    if let CellValue::String(v) = self {
      return Some(v);
    }
    return None;
  }

  pub fn as_str(&self) -> Option<&str> {
    if let CellValue::String(v) = self {
      return Some(v);
    }
    return None;
  }

  pub fn as_cell_value_array(&self) -> Option<&Vec<CellValue>> {
    if let CellValue::Array(v) = self {
      return Some(v);
    }
    return None;
  }

  pub fn as_array(&self) -> Option<&Vec<CellValue>> {
    if let CellValue::Array(v) = self {
      return Some(v);
    }
    return None;
  }

  pub fn as_string_array(&self) -> Option<Vec<&String>> {
    return self.as_item_array(|v| match v {
      CellValue::String(v) => Some(v),
      _ => None,
    });
  }

  pub fn as_text_array(&self) -> Option<Vec<&TextValue>> {
    return self.as_item_array(|v| match v {
      CellValue::Text(v) => Some(v),
      _ => None,
    });
  }

  pub fn as_url_array(&self) -> Option<Vec<&UrlValue>> {
    return self.as_item_array(|v| match v {
      CellValue::URL(v) => Some(v),
      _ => None,
    });
  }

  pub fn as_email_array(&self) -> Option<Vec<&EmailValue>> {
    return self.as_item_array(|v| match v {
      CellValue::Email(v) => Some(v),
      _ => None,
    });
  }

  pub fn as_phone_array(&self) -> Option<Vec<&PhoneValue>> {
    return self.as_item_array(|v| match v {
      CellValue::Phone(v) => Some(v),
      _ => None,
    });
  }

  pub fn as_cascader_array(&self) -> Option<Vec<&CascaderValue>> {
    return self.as_item_array(|v| match v {
      CellValue::Cascader(v) => Some(v),
      _ => None,
    });
  }

  pub fn as_attachment_array(&self) -> Option<Vec<&AttachmentValue>> {
    return self.as_item_array(|v| match v {
      CellValue::Attachment(v) => Some(v),
      _ => None,
    });
  }

  pub fn as_lookup_tree_array(&self) -> Option<Vec<&LookUpTreeValue>> {
    return self.as_item_array(|v| match v {
      CellValue::LookUpTree(v) => Some(v),
      _ => None,
    });
  }

  pub fn to_string_array(self) -> Option<Vec<String>> {
    return self.to_item_array(|v| match v {
      CellValue::String(v) => Some(v),
      _ => None,
    });
  }

  pub fn to_text_array(self) -> Option<Vec<TextValue>> {
    return self.to_item_array(|v| match v {
      CellValue::Text(v) => Some(v),
      _ => None,
    });
  }

  pub fn to_url_array(self) -> Option<Vec<UrlValue>> {
    return self.to_item_array(|v| match v {
      CellValue::URL(v) => Some(v),
      _ => None,
    });
  }

  pub fn to_email_array(self) -> Option<Vec<EmailValue>> {
    return self.to_item_array(|v| match v {
      CellValue::Email(v) => Some(v),
      _ => None,
    });
  }

  pub fn to_phone_array(self) -> Option<Vec<PhoneValue>> {
    return self.to_item_array(|v| match v {
      CellValue::Phone(v) => Some(v),
      _ => None,
    });
  }

  pub fn to_cascader_array(self) -> Option<Vec<CascaderValue>> {
    return self.to_item_array(|v| match v {
      CellValue::Cascader(v) => Some(v),
      _ => None,
    });
  }

  pub fn to_attachment_array(self) -> Option<Vec<AttachmentValue>> {
    return self.to_item_array(|v| match v {
      CellValue::Attachment(v) => Some(v),
      _ => None,
    });
  }

  pub fn to_lookup_tree_array(self) -> Option<Vec<LookUpTreeValue>> {
    return self.to_item_array(|v| match v {
      CellValue::LookUpTree(v) => Some(*v),
      _ => None,
    });
  }

  fn as_item_array<R>(&self, get_item_fn: fn(&CellValue) -> Option<&R>) -> Option<Vec<&R>> {
    if let CellValue::Array(arr) = self {
      let len = arr.len();
      return Self::do_map_cell_value_array(arr, len, get_item_fn);
    }
    None
  }

  fn to_item_array<R>(self, get_item_fn: fn(CellValue) -> Option<R>) -> Option<Vec<R>> {
    if let CellValue::Array(arr) = self {
      let len = arr.len();
      return Self::do_map_cell_value_array(arr, len, get_item_fn);
    }
    None
  }

  fn do_map_cell_value_array<T, R, F>(cell_value: T, len: usize, map_fn: F) -> Option<Vec<R>>
  where
    T: IntoIterator,
    T::Item: Borrow<CellValue>,
    F: Fn(T::Item) -> Option<R>,
  {
    let mut result = Vec::with_capacity(len);
    for item in cell_value.into_iter() {
      if let Some(mapped_item) = map_fn(item) {
        result.push(mapped_item);
      } else {
        return None;
      }
    }
    Some(result)
  }
}

impl Display for CellValue {
  fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
    let str = match self {
      Self::Null => "null".to_string(),
      Self::String(val) => val.clone(),
      Self::Number(val) => val.to_string(),
      Self::Array(val) => {
        let mut result = String::new();
        for item in val {
          result.push_str(&item.to_string());
        }
        return write!(f, "{}", result);
      }
      Self::Bool(val) => val.to_string(),
      _ => todo!("not support type"),
    };
    return write!(f, "{}", str);
  }
}

fn to_f64(number: &Number) -> f64 {
  if let Some(n) = number.as_f64() {
    n
  } else if let Some(n) = number.as_i64() {
    n as f64
  } else if let Some(n) = number.as_u64() {
    n as f64
  } else {
    f64::NAN
  }
}

impl Add for CellValue {
  type Output = Self;

  fn add(self, rhs: Self) -> Self::Output {
    return match (self, rhs) {
      (Self::Number(a), Self::Number(b)) => Self::Number(a + b),

      (Self::String(a), Self::String(b)) => Self::String(format!("{}{}", a, b)),

      (Self::Number(a), Self::String(b)) | (Self::String(b), Self::Number(a)) => Self::String(format!("{}{}", a, b)),

      // TODO: support more types
      (_, _) => Self::String(String::new()),
    };
  }
}

impl Sub for CellValue {
  type Output = Self;

  fn sub(self, rhs: Self) -> Self::Output {
    CellValue::Number(self.to_number() - rhs.to_number())
  }
}

impl Mul for CellValue {
  type Output = Self;

  fn mul(self, rhs: Self) -> Self::Output {
    CellValue::Number(self.to_number() * rhs.to_number())
  }
}

impl Div for CellValue {
  type Output = Self;

  fn div(self, rhs: Self) -> Self::Output {
    CellValue::Number(self.to_number() / rhs.to_number())
  }
}

impl Rem for CellValue {
  type Output = Self;

  fn rem(self, rhs: Self) -> Self::Output {
    return match (self, rhs) {
      (Self::Number(a), Self::Number(b)) => Self::Number(a % b),
      _ => panic!("not support type"),
    };
  }
}

impl Eq for CellValue {}

impl PartialEq<Self> for CellValue {
  fn eq(&self, other: &Self) -> bool {
    return match (self, other) {
      (Self::Null, Self::Null) => true,
      (Self::String(a), Self::String(b)) => a == b,
      (Self::Number(a), Self::Number(b)) => a == b,
      (Self::Bool(a), Self::Bool(b)) => a == b,
      (Self::Array(a), Self::Array(b)) => a == b,
      (_, _) => false,
    };
  }
}

impl PartialOrd<Self> for CellValue {
  fn partial_cmp(&self, other: &Self) -> Option<std::cmp::Ordering> {
    return match (self, other) {
      (Self::Null, Self::Null) => Some(std::cmp::Ordering::Equal),
      (Self::String(a), Self::String(b)) => Some(a.cmp(b)),
      (Self::Number(a), Self::Number(b)) => a.partial_cmp(b),
      (Self::Bool(a), Self::Bool(b)) => Some(a.cmp(b)),
      (_, _) => None,
    };
  }
}

#[derive(Derivative, Deserialize, Serialize, Clone, Eq, PartialEq, ToSchema)]
#[derivative(Debug, Default)]
pub struct SingleTextValue {
  #[derivative(Default(value = "1"))]
  pub(crate) r#type: i32,

  pub(crate) text: String,
}

#[derive(Derivative, Deserialize, Serialize, Clone, Eq, PartialEq, ToSchema)]
#[derivative(Debug, Default)]
pub struct CascaderValue {
  #[derivative(Default(value = "1"))]
  pub(crate) r#type: i32,

  pub(crate) text: String,
}

#[derive(Derivative, Deserialize, Serialize, Clone, Eq, PartialEq, ToSchema)]
#[derivative(Debug, Default)]
pub struct PhoneValue {
  #[derivative(Default(value = "1"))]
  pub(crate) r#type: i32,

  pub(crate) text: String,
}

#[derive(Derivative, Deserialize, Serialize, Clone, Eq, PartialEq, ToSchema)]
#[derivative(Debug, Default)]
pub struct TextValue {
  #[derivative(Default(value = "1"))]
  pub(crate) r#type: i32,

  pub(crate) text: String,
}

#[derive(Derivative, Deserialize, Serialize, Clone, Eq, PartialEq, ToSchema)]
#[derivative(Debug, Default)]
pub struct EmailValue {
  #[derivative(Default(value = "4"))]
  pub(crate) r#type: i32,

  pub(crate) text: String,

  pub(crate) link: Option<String>,
}

#[derive(Derivative, Deserialize, Serialize, Clone, Eq, PartialEq, ToSchema)]
#[derivative(Debug, Default)]
pub struct UrlValue {
  // #[derivative(Default(value = "2"))]
  #[serde(skip_serializing)]
  // pub r#type: i32,
  pub r#type: Option<i32>,
  pub text: String,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub link: Option<String>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub title: Option<String>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub favicon: Option<String>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub visited: Option<bool>,
}

#[derive(Deserialize, Serialize, Debug, Clone, Default, PartialEq, Eq)]
#[serde(rename_all = "camelCase")]
pub struct AttachmentValue {
  pub id: String,
  pub name: String,
  pub mime_type: String,
  pub token: String,
  #[serde(skip_serializing)]
  pub bucket: Option<String>,
  pub size: u32,
  pub width: Option<u32>,
  pub height: Option<u32>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub url: Option<String>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub preview: Option<String>,
}

#[derive(Deserialize, Serialize, Debug, Clone, Default, PartialEq, Eq)]
#[serde(rename_all = "camelCase")]
pub struct MemberValue {
  pub id: String,
  pub unit_id: String,
  pub name: String,
  pub r#type: String,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub avatar: Option<String>,
}

#[derive(Derivative, Deserialize, Serialize, Clone, Eq, PartialEq, ToSchema)]
#[derivative(Debug)]
pub struct LookUpTreeValue {
  pub datasheet_id: String,
  pub record_id: String,
  pub field: FieldSO,
  pub cell_value: CellValue,
}

#[cfg(test)]
mod tests {
  use crate::prelude::CellValue;

  #[test]
  fn test_eq() {
    let c1 = CellValue::Null;
    let c2 = CellValue::from(vec!["1111".to_string()]);
    let c3 = CellValue::from(vec!["1111".to_string()]);
    let c4 = CellValue::Null;
    let c5 = CellValue::from(vec!["1111".to_string(), "2222".to_string()]);
    assert_ne!(c1, c2);
    assert_eq!(c2, c3);
    assert_eq!(c1, c4);
    assert_ne!(c2, c5);
  }

  #[test]
  fn test_comp() {
    let c1 = CellValue::Null;
    let c2 = CellValue::Null;
    let c3 = CellValue::Bool(true);
    let c4 = CellValue::Bool(false);
    assert_eq!(
      c1.as_bool().unwrap_or_default().cmp(&c2.as_bool().unwrap_or_default()),
      std::cmp::Ordering::Equal
    );
    assert_eq!(
      c1.as_bool().unwrap_or_default().cmp(&c3.as_bool().unwrap_or_default()),
      std::cmp::Ordering::Less
    );
    assert_eq!(
      c1.as_bool().unwrap_or_default().cmp(&c4.as_bool().unwrap_or_default()),
      std::cmp::Ordering::Greater
    );
    assert_eq!(
      c3.as_bool().unwrap_or_default().cmp(&c4.as_bool().unwrap_or_default()),
      std::cmp::Ordering::Less
    );
  }
}

pub type CellValueSo = Value;
