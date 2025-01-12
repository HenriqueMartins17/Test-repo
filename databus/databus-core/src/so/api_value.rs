use serde::{Deserialize, Serialize};
use serde_json::{Number, Value};
use utoipa::ToSchema;

use crate::so::{AttachmentValue, MemberValue, UrlValue};

#[derive(Debug, Clone, PartialEq, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
#[serde(untagged)]
pub enum ApiValue {
  Null,
  Bool(bool),
  Number(Number),
  String(String),
  StringArray(Vec<String>),
  AttachmentValue(Vec<AttachmentValue>),
  MemberValue(MemberValue),
  MemberArrayValue(Vec<MemberValue>),
  UrlValue(UrlValue),
  LookUpValue(Vec<ApiValue>),
}

impl<'__s> utoipa::ToSchema<'__s> for ApiValue {
  fn schema() -> (&'__s str, utoipa::openapi::RefOr<utoipa::openapi::schema::Schema>) {
    (
      "ApiValue",
      utoipa::openapi::ObjectBuilder::new()
          .into(),
    ) }
}



impl From<bool> for ApiValue {
  fn from(f: bool) -> Self {
    ApiValue::Bool(f)
  }
}

impl From<String> for ApiValue {
  fn from(f: String) -> Self {
    ApiValue::String(f)
  }
}

impl<'a> From<&'a str> for ApiValue {
  fn from(f: &str) -> Self {
    ApiValue::String(f.to_string())
  }
}

impl From<Vec<String>> for ApiValue {
  fn from(f: Vec<String>) -> Self {
    ApiValue::StringArray(f)
  }
}

impl From<Vec<AttachmentValue>> for ApiValue {
  fn from(f: Vec<AttachmentValue>) -> Self {
    ApiValue::AttachmentValue(f)
  }
}

impl From<Number> for ApiValue {
  fn from(f: Number) -> Self {
    ApiValue::Number(f)
  }
}

impl From<u64> for ApiValue {
  fn from(f: u64) -> Self {
    ApiValue::Number(f.into())
  }
}

impl From<MemberValue> for ApiValue {
  fn from(f: MemberValue) -> Self {
    ApiValue::MemberValue(f)
  }
}

impl From<Vec<MemberValue>> for ApiValue {
  fn from(f: Vec<MemberValue>) -> Self {
    ApiValue::MemberArrayValue(f)
  }
}

impl From<UrlValue> for ApiValue {
  fn from(f: UrlValue) -> Self {
    ApiValue::UrlValue(f)
  }
}

impl From<Vec<ApiValue>> for ApiValue {
  fn from(f: Vec<ApiValue>) -> Self {
    ApiValue::LookUpValue(f)
  }
}

impl<T> From<Option<T>> for ApiValue
  where
    T: Into<ApiValue>,
{
  fn from(opt: Option<T>) -> Self {
    match opt {
      None => ApiValue::Null,
      Some(value) => Into::into(value),
    }
  }
}

impl ApiValue {
  pub fn to_value(&self) -> Value {
    serde_json::to_value(self).unwrap()
  }

  pub fn as_bool(&self) -> Option<bool> {
    return match *self {
      ApiValue::Bool(b) => Some(b),
      _ => None
    }
  }

  pub fn as_str(&self) -> Option<&str> {
    return match self {
      ApiValue::String(s) => Some(s),
      _ => None
    }
  }

  pub fn as_number(&self) -> Option<Number> {
    return match self {
      ApiValue::Number(n) => Some(n.clone()),
      _ => None
    }
  }

  pub fn as_u64(&self) -> Option<u64> {
    return match self {
      ApiValue::Number(n) => {
        if let Some(n) = n.as_i64() {
          Some(n as u64)
        } else if let Some(n) = n.as_u64() {
          Some(n)
        } else if let Some(n) = n.as_f64() {
          Some(n as u64)
        } else {
          None
        }
      }
      _ => None
    }
  }

  pub fn as_f64(&self) -> Option<f64> {
    return match self {
      ApiValue::Number(n) => {
        if let Some(n) = n.as_i64() {
          Some(n as f64)
        } else if let Some(n) = n.as_u64() {
          Some(n as f64)
        } else if let Some(n) = n.as_f64() {
          Some(n)
        } else {
          None
        }
      }
      _ => None
    }
  }

  pub fn as_i64(&self) -> Option<i64> {
    return match self {
      ApiValue::Number(n) => {
        if let Some(n) = n.as_i64() {
          Some(n)
        } else if let Some(n) = n.as_u64() {
          Some(n as i64)
        } else if let Some(n) = n.as_f64() {
          Some(n as i64)
        } else {
          None
        }
      }
      _ => None
    }
  }

  pub fn as_array_string(&self) -> Option<Vec<String>> {
    return match self {
      ApiValue::StringArray(s) => Some(s.clone()),
      _ => None
    }
  }

  pub fn as_array_attachment(&self) -> Option<Vec<AttachmentValue>> {
    return match self {
      ApiValue::AttachmentValue(s) => Some(s.clone()),
      _ => None
    }
  }

  pub fn as_array_member(&self) -> Option<Vec<MemberValue>> {
    return match self {
      ApiValue::MemberArrayValue(s) => Some(s.clone()),
      _ => None
    }
  }

  pub fn as_url_value(&self) -> Option<UrlValue> {
    return match self {
      ApiValue::UrlValue(s) => Some(s.clone()),
      _ => None
    }
  }

  pub fn as_array_look_up(&self) -> Option<Vec<ApiValue>> {
    return match self {
      ApiValue::LookUpValue(s) => Some(s.clone()),
      _ => None
    }
  }

}

impl ToString for ApiValue {
  fn to_string(&self) -> String {
    serde_json::to_string(self).unwrap()
  }
}
