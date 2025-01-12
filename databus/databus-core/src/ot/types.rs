use serde::{Deserialize, Serialize};
use json0::Operation;
use crate::prelude::cell::CellValueSo;

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Default)]
pub enum ResourceType {
  #[default]
  Datasheet,
  Form,
  Dashboard,
  Widget,
  Mirror,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Default)]
pub enum ResourceIdPrefix {
    #[default]
    Datasheet,
    Form,
    Automation,
    Dashboard,
    Widget,
    Mirror,
}

impl ResourceIdPrefix {
    pub fn as_str(&self) -> &'static str {
        match *self {
            ResourceIdPrefix::Datasheet => "dst",
            ResourceIdPrefix::Form => "fom",
            ResourceIdPrefix::Automation => "aut",
            ResourceIdPrefix::Dashboard => "dsb",
            ResourceIdPrefix::Widget => "wdt",
            ResourceIdPrefix::Mirror => "mir",
        }
    }
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Default)]
pub enum NodeTypeEnum {
  #[default]
  Folder,
  Datasheet,
  Form,
  Dashboard,
  Mirror,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq)]
#[serde(rename_all = "camelCase")]
pub struct SetRecordOTO {
    pub record_id: String,
    pub field_id: String,
    pub value: CellValueSo,
}


#[derive(Deserialize, Serialize, Debug, Clone, PartialEq)]
#[serde(rename_all = "camelCase")]
pub struct ActionOTO {
    #[serde(rename(serialize = "n", deserialize = "n"))]
    pub(crate) op_name: String,
    
    #[serde(flatten)]
    pub(crate) op: Operation,
}

#[cfg(test)]
mod tests {
    use serde_json::json;
    use json0::operation::{OperationKind, PathSegment};
    use super::*;
    
    #[test]
    fn test_action_vo_serialization(){
        let action = ActionOTO {
            op_name: "OI".to_string(),
            op: Operation {
                p: vec![
                    PathSegment::String("recordMap".to_string()),
                    PathSegment::String("recnMv9BvatQh".to_string()),
                    PathSegment::String("data".to_string()),
                    PathSegment::String("fldGzWrhZdznD".to_string()),
                ],
                kind: OperationKind::ObjectInsert {
                    oi: json!({ "type": 1, "text": "asdasd"}),
                },
            },
        };
        let _action_str = serde_json::to_string_pretty(&action).unwrap();
        let action_json = serde_json::from_str::<serde_json::Value>(r#"
                    {
              "n": "OI",
              "p": [
                "recordMap",
                "recnMv9BvatQh",
                "data",
                "fldGzWrhZdznD"
              ],
              "oi": {
                "text": "asdasd",
                "type": 1
              }
            }
        "#).unwrap();
        assert_eq!(action_json, serde_json::to_value(action).unwrap());
    }

    #[test]
    fn test_record_set_vo_serialization_deserialization() {
        let record_set = SetRecordOTO {
            record_id: "123".to_string(),
            field_id: "456".to_string(),
            value: json!(42.0),
        };

        let serialized = serde_json::to_string(&record_set).unwrap();
        println!("{}", serialized);
        let deserialized: SetRecordOTO = serde_json::from_str(&serialized).unwrap();

        assert_eq!(record_set, deserialized);
    }

    #[test]
    fn test_cell_value_so_serialization_deserialization() {
        let cell_value = CellValueSo::String("hello".to_string());

        let serialized = serde_json::to_string(&cell_value).unwrap();
        let deserialized: CellValueSo = serde_json::from_str(&serialized).unwrap();

        assert_eq!(cell_value, deserialized);
    }

    #[test]
    fn test_cell_value_so_enum_variants() {
        assert_eq!(CellValueSo::Null, CellValueSo::Null);
        assert_ne!(CellValueSo::Null, json!(0.0));
        assert_eq!(json!(42.0), json!(42.0));
        assert_ne!(CellValueSo::String("hello".to_string()), CellValueSo::String("world".to_string()));
        assert_eq!(json!(true), json!(true));
        assert_ne!(json!(true), json!(false));
    }
}
