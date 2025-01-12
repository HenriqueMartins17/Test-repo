use crate::so::view_operation::filter::IFilterCondition;
use serde::de::Error;
use serde::{Deserialize, Deserializer};

pub fn deserialize_to_option_u64<'de, D>(deserializer: D) -> Result<Option<u64>, D::Error>
where
  D: Deserializer<'de>,
{
  let value: Option<serde_json::Value> = Option::deserialize(deserializer)?;
  let result = match value {
    Some(serde_json::Value::Number(num)) => {
      if let Some(parsed) = num.as_u64() {
        Some(parsed)
      } else if let Some(parsed) = num.as_f64() {
        Some(parsed as u64)
      } else {
        return Err(Error::custom("Invalid number format"));
      }
    }
    Some(serde_json::Value::Null) => None,
    None => None,
    _ => return Err(Error::custom("Invalid number format")),
  };
  Ok(result)
}

pub fn deserialize_to_condition<'de, D>(deserializer: D) -> Result<Vec<IFilterCondition>, D::Error>
where
  D: Deserializer<'de>,
{
  let value: Option<serde_json::Value> = Option::deserialize(deserializer)?;
  let result = match value {
    Some(serde_json::Value::Array(arr)) => serde_json::from_value(serde_json::Value::Array(arr)).unwrap(),
    Some(serde_json::Value::Null) => vec![],
    None => vec![],
    _ => return Err(Error::custom("Invalid Value")),
  };
  Ok(result)
}

pub fn deserialize_to_vec_string<'de, D>(deserializer: D) -> Result<Option<Vec<String>>, D::Error>
where
  D: Deserializer<'de>,
{
  let value: Option<String> = Option::deserialize(deserializer)?;

  match value {
    Some(s) => {
      let vec: Vec<String> = s.split(',').map(String::from).collect();
      Ok(Some(vec))
    }
    None => Ok(None),
  }
}
