use once_cell::sync::Lazy;
use rand::Rng;
use serde_json::Value;
use snowflake::SnowflakeIdGenerator;
use crate::prelude::CellValue;

use crate::{fields::property::field_types::BasicValueType, types::field_api_enums::MemberType};

// todo 常量map 待处理
pub fn get_member_type_string(member_type: MemberType) -> String {
  String::from(match member_type {
    MemberType::Team => "Team",
    MemberType::Member => "Member",
    _ => "",
  })
}

pub fn generate_random_string(length: usize) -> String {
  let chars: &[char] = &[
    'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w',
    'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
    'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
  ];

  let mut rng = rand::thread_rng();
  let mut random_string = String::new();

  for _ in 0..length {
    let random_number = rng.gen_range(0..chars.len());
    random_string.push(chars[random_number]);
  }
  random_string
}

// /**
//  * Semantic null value to null
//  */
pub fn handle_empty_cell_value(cell_value: Value, basic_value_type: Option<BasicValueType>) -> Value {
  if cell_value == Value::Null {
    return Value::Null;
  }
  let mut cell_value_type = basic_value_type;
  match cell_value_type {
    None => {
      if cell_value.is_array() {
        cell_value_type = Some(BasicValueType::Array);
      } else if cell_value.is_string() {
        cell_value_type = Some(BasicValueType::String);
      } else if cell_value.is_boolean() {
        cell_value_type = Some(BasicValueType::Boolean);
      } else {
        return cell_value;
      }
    }
    Some(_) => {}
  }
  let cell_value_type = cell_value_type.unwrap();
  match cell_value_type {
    BasicValueType::Array => {
      if let Some(arr) = cell_value.as_array() {
        if arr.is_empty() {
          return Value::Null;
        }
      }
    }
    BasicValueType::Boolean => {
      if let Some(b) = cell_value.as_bool() {
        if !b {
          return Value::Null;
        }
      }
    }
    BasicValueType::String => {
      if let Some(s) = cell_value.as_str() {
        if s.is_empty() {
          return Value::Null;
        }
        if let Some(arr) = cell_value.as_array() {
          if arr.len() == 1 {
            if let Some(o) = arr[0].as_object() {
              if !o.contains_key("text") {
                return Value::Null;
              }
            }
          }
        }
      }
    }
    _ => return cell_value,
  }
  cell_value
}

static mut SNOWFLAKE_ID_GENERATOR: Lazy<SnowflakeIdGenerator> = Lazy::new(|| {
  SnowflakeIdGenerator::new(0, 0)
});


/// unsigned big int
/// 2(64) - 1
pub fn generate_u64_id() -> u64 {
  unsafe {
    SNOWFLAKE_ID_GENERATOR.generate() as u64
  }
}

pub fn handle_null_array(cell_value: CellValue) -> CellValue {
  if cell_value == CellValue::Null {
    return CellValue::Null;
  }
  if let CellValue::Array(values) = cell_value {
    let res: Vec<CellValue> = values.into_iter().filter(|x| x.clone() != CellValue::Null).collect();
    if res.is_empty() {
      return CellValue::Null;
    }
    return CellValue::Array(res);
  }
  cell_value
}
