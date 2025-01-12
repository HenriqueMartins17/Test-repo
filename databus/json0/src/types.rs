// use std::collections::HashMap;
// use std::fmt;
// use serde::{Deserialize, Serialize};

// #[macro_export]
// macro_rules! json_value {
//     ($json_str:expr) => {{
//         let json_str = $json_str;
//         let value: JsonValue = serde_json::from_str(json_str).unwrap();
//         value
//     }};
// }

// #[derive(Deserialize, Serialize, Debug, Clone, PartialEq)]
// #[serde(untagged)]
// pub enum JsonValue {
//     Object(HashMap<String, JsonValue>),
//     Array(Vec<JsonValue>),
//     String(String),
//     Int(i64),
//     Number(f64),
//     Boolean(bool),
//     Null,
// }

// pub trait JsonTypeChecker {
//     fn is_object(&self) -> bool;
//     fn is_array(&self) -> bool;
//     fn is_string(&self) -> bool;
//     fn is_float(&self) -> bool;
//     fn is_int(&self) -> bool;
//     fn is_boolean(&self) -> bool;
//     fn is_null(&self) -> bool;
// }

// impl fmt::Display for JsonValue {
//     fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
//         match self {
//             JsonValue::Object(obj) => write!(f, "{}", serde_json::to_string(obj).unwrap()),
//             JsonValue::Array(arr) => write!(f, "{}", serde_json::to_string(arr).unwrap()),
//             JsonValue::String(s) => write!(f, "{}", s),
//             JsonValue::Number(n) => write!(f, "{}", n),
//             JsonValue::Boolean(b) => write!(f, "{}", b),
//             JsonValue::Null => write!(f, "null"),
//             _ => write!(f, "")
//         }
//     }
// }
//
//
// impl JsonTypeChecker for JsonValue {
//     fn is_object(&self) -> bool {
//         matches!(self, JsonValue::Object(_))
//     }
//
//     fn is_array(&self) -> bool {
//         matches!(self, JsonValue::Array(_))
//     }
//
//     fn is_string(&self) -> bool {
//         matches!(self, JsonValue::String(_))
//     }
//
//     fn is_float(&self) -> bool {
//         matches!(self, JsonValue::Number(_))
//     }
//
//     fn is_int(&self) -> bool {
//         matches!(self, JsonValue::Int(_))
//     }
//
//     fn is_boolean(&self) -> bool {
//         matches!(self, JsonValue::Boolean(_))
//     }
//
//     fn is_null(&self) -> bool {
//         matches!(self, JsonValue::Null)
//     }
// }
//
// impl JsonValue {
//     pub fn as_array(&self) -> Option<&Vec<JsonValue>> {
//         match self {
//             JsonValue::Array(array) => Some(array),
//             _ => None,
//         }
//     }
//
//     pub fn as_object(&self) -> Option<&HashMap<String, JsonValue>> {
//         match self {
//             JsonValue::Object(object) => Some(object),
//             _ => None,
//         }
//     }
//
//     pub fn as_array_mut(&mut self) -> Option<&mut Vec<JsonValue>> {
//         match self {
//             JsonValue::Array(array) => Some(array),
//             _ => None,
//         }
//     }
//
//     pub fn as_object_mut(&mut self) -> Option<&mut HashMap<String, JsonValue>> {
//         match self {
//             JsonValue::Object(object) => Some(object),
//             _ => None,
//         }
//     }
//
//     pub fn path_key(&self, key: &str) -> Option<&JsonValue> {
//         match self {
//             JsonValue::Object(map) => map.get(key),
//             _ => None,
//         }
//     }
//
//     pub fn path_index(&self, index: usize) -> Option<&JsonValue> {
//         if let JsonValue::Array(array) = self {
//             array.get(index)
//         } else {
//             None
//         }
//     }
//
//     pub fn path_key_mut(&mut self, key: &str) -> Option<&mut JsonValue> {
//         match self {
//             JsonValue::Object(ref mut map) => map.get_mut(key),
//             _ => None,
//         }
//     }
//
//     pub fn path_index_mut(&mut self, index: usize) -> Option<&mut JsonValue> {
//         if let JsonValue::Array(ref mut array) = self {
//             array.get_mut(index)
//         } else {
//             None
//         }
//     }
//
//     pub fn as_str(&self) -> Option<&str> {
//         if let JsonValue::String(s) = self {
//             Some(s)
//         } else {
//             None
//         }
//     }
//
//     pub fn as_float(&self) -> Option<f64> {
//         if let JsonValue::Number(n) = self {
//             Some(*n)
//         } else {
//             None
//         }
//     }
//
//     pub fn as_integer(&self) -> Option<i64> {
//         if let JsonValue::Int(n) = self {
//             Some(*n)
//         } else {
//             None
//         }
//     }
//
//     pub fn as_boolean(&self) -> Option<bool> {
//         if let JsonValue::Boolean(n) = self {
//             Some(*n)
//         } else {
//             None
//         }
//     }
// }
//
// #[cfg(test)]
// mod tests{
//     use serde_json::Value;
//     use crate::types::{JsonTypeChecker, JsonValue};
//
//
//
//     #[test]
//     fn test_macro(){
//         let v1 = json_value!(r#"[1,2,3,4,5,6]"#);
//
//         println!("value is {:?}", v1);
//         let v2 = json_value!(r#"{}"#);
//
//         println!("value is {:?}", v2);
//         let v3 = json_value!(r#"[1, 2, 3]"#);
//
//         println!("value is {:?}", v3);
//         let value = json_value!(r#"{"x":[{"z":null},{"G":{"a":"a1","b":11}}],"y":7.0}"#);
//         println!("value is {:?}", value);
//         assert_eq!(
//             *(value.path_key("x").unwrap().path_index(0).unwrap()
//                 .path_key("z").unwrap()),
//             JsonValue::Null);
//         assert_eq!(
//             *(value.path_key("x").unwrap().path_index(1).unwrap()
//                 .path_key("G").unwrap().path_key("a").unwrap()),
//             JsonValue::String("a1".to_string()));
//         assert_eq!(*(value.path_key("y").unwrap()), JsonValue::Number(7.0));
//     }
//     #[test]
//     fn test_json_value(){
//         println!("Hello world");
//         let json_obj = JsonValue::Object(
//             [("name".to_string(), JsonValue::String("Alice".to_string())),
//                 ("age".to_string(), JsonValue::Number(30.0)),
//                 ("is_student".to_string(), JsonValue::Boolean(false))]
//                 .iter()
//                 .cloned()
//                 .collect(),
//         );
//
//         let obj_str: &str = r#"{"age": 30.0,"name": "Alice","is_student": false}"#;
//         let serdes = serde_json::from_str::<JsonValue>(obj_str).unwrap();
//         assert_eq!(serdes.path_key("name").unwrap().is_string(), true);
//         println!("see JsonNode ser {:?}", serdes);
//         assert_eq!(serde_json::to_value(&json_obj).unwrap(), serde_json::from_str::<Value>(obj_str).unwrap());
//         assert_eq!(json_obj.path_key("name").unwrap(), &JsonValue::String("Alice".to_string()));
//         assert_eq!(json_obj.path_key("name").unwrap().is_string(), true);
//         assert_eq!(json_obj.path_key("name").unwrap().is_float(), false);
//         assert_eq!(json_obj.path_key("name").unwrap().is_boolean(), false);
//         assert_eq!(json_obj.path_key("age").unwrap().is_float(), true);
//         assert_eq!(json_obj.path_key("is_student").unwrap().is_boolean(), true);
//         println!("{}", serde_json::to_string_pretty(&json_obj).unwrap());
//         let json_array = JsonValue::Array(vec![
//             JsonValue::Number(42.0),
//             JsonValue::Int(12),
//             JsonValue::String("Hello".to_string()),
//             JsonValue::Null,
//         ]);
//
//         assert_eq!(json_array.path_index(0).unwrap().is_float(), true);
//         assert_eq!(json_array.path_index(1).unwrap().is_int(), true);
//         assert_eq!(json_array.path_index(2).unwrap().is_string(), true);
//         assert_eq!(json_array.path_index(3).unwrap().is_null(), true);
//         println!("{}", serde_json::to_string_pretty(&json_array).unwrap());
//     }
// }
