use serde::{Deserialize, Serialize};
use serde_json::Value;
use utoipa::ToSchema;

use crate::prelude::FieldKindSO;

#[derive(Deserialize, Serialize, Debug, Clone, Eq, PartialEq, ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct IFilterInfo {
  pub conjunction: FilterConjunction,
  #[serde(default, deserialize_with = "crate::utils::serde_ext::deserialize_to_condition")]
  pub conditions: Vec<IFilterCondition>,
}

#[derive(Deserialize, Serialize, Debug, Clone, Eq, PartialEq, ToSchema)]
pub enum FilterConjunction {
  #[serde(rename = "and")]
  And,
  #[serde(rename = "or")]
  Or,
}

#[derive(Deserialize, Serialize, Debug, Clone, Eq, PartialEq, ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct IFilterCondition {
  #[serde(default)]
  pub condition_id: String,
  #[serde(default)]
  pub field_id: String,
  #[serde(default)]
  pub operator: FOperator,
  #[serde(default)]
  pub field_type: FieldKindSO,
  #[serde(default)]
  pub value: Value,
}

impl IFilterCondition {
  pub(crate) fn get_value(&self) -> ConditionValue {
    if self.value.is_null() {
      return ConditionValue::Null;
    }
    return match self.field_type {
      FieldKindSO::Text
      | FieldKindSO::SingleText
      | FieldKindSO::URL
      | FieldKindSO::Cascader
      | FieldKindSO::Email
      | FieldKindSO::Phone => ConditionValue::StringArray(serde_json::from_value(self.value.clone()).unwrap()),
      FieldKindSO::Number
      | FieldKindSO::Currency
      | FieldKindSO::Percent
      | FieldKindSO::AutoNumber
      | FieldKindSO::Rating => ConditionValue::StringArray(serde_json::from_value(self.value.clone()).unwrap()),
      FieldKindSO::SingleSelect => {
        let value = self.value.clone();
        ConditionValue::StringArray(serde_json::from_value(value).unwrap())
      }
      FieldKindSO::MultiSelect => ConditionValue::StringArray(serde_json::from_value(self.value.clone()).unwrap()),
      FieldKindSO::DateTime | FieldKindSO::CreatedTime | FieldKindSO::LastModifiedTime => {
        ConditionValue::DateTime(serde_json::from_value(self.value.clone()).unwrap())
      }
      FieldKindSO::Checkbox => ConditionValue::Bool(self.value.clone().as_bool().unwrap()),
      FieldKindSO::Member | FieldKindSO::CreatedBy | FieldKindSO::LastModifiedBy => {
        ConditionValue::StringArray(serde_json::from_value(self.value.clone()).unwrap())
      }
      FieldKindSO::Link | FieldKindSO::OneWayLink => {
        ConditionValue::StringArray(serde_json::from_value(self.value.clone()).unwrap())
      }
      FieldKindSO::LookUp => ConditionValue::StringArray(serde_json::from_value(self.value.clone()).unwrap()),
      _ => ConditionValue::Value(self.value.clone()),
    };
  }
}

impl ConditionValue {
  pub fn as_string(&self) -> Option<&str> {
    match self {
      ConditionValue::StringArray(s) => return if s.len() > 0 { Some(s[0].as_str()) } else { None },
      _ => None,
    }
  }

  pub fn as_string_array(&self) -> Option<Vec<String>> {
    match self {
      ConditionValue::StringArray(s) => Some(s.clone()),
      _ => None,
    }
  }

  pub fn is_null(&self) -> bool {
    self.as_null().is_some()
  }

  pub fn as_null(&self) -> Option<()> {
    match *self {
      ConditionValue::Null => Some(()),
      _ => None,
    }
  }

  pub fn as_bool(&self) -> Option<bool> {
    match *self {
      ConditionValue::Bool(b) => Some(b),
      _ => None,
    }
  }

  pub fn as_date_time(&self) -> Option<IFilterDateTime> {
    match self {
      ConditionValue::DateTime(d) => Some(d.clone()),
      _ => None,
    }
  }
}

#[derive(Deserialize, Serialize, Debug, Clone, Eq, PartialEq, ToSchema, Default)]
pub enum FOperator {
  #[serde(rename = "is")]
  #[default]
  Is,
  #[serde(rename = "isNot")]
  IsNot,
  #[serde(rename = "contains")]
  Contains,
  #[serde(rename = "doesNotContain")]
  DoesNotContain,
  #[serde(rename = "isEmpty")]
  IsEmpty,
  #[serde(rename = "isNotEmpty")]
  IsNotEmpty,
  #[serde(rename = "isGreater")]
  IsGreater,
  #[serde(rename = "isGreaterEqual")]
  IsGreaterEqual,
  #[serde(rename = "isLess")]
  IsLess,
  #[serde(rename = "isLessEqual")]
  IsLessEqual,
  #[serde(rename = "isRepeat")]
  IsRepeat,
}

impl FOperator {
  pub fn to_string(&self) -> String {
    match self {
      FOperator::Is => "is".to_string(),
      FOperator::IsNot => "isNot".to_string(),
      FOperator::Contains => "contains".to_string(),
      FOperator::DoesNotContain => "doesNotContain".to_string(),
      FOperator::IsEmpty => "isEmpty".to_string(),
      FOperator::IsNotEmpty => "isNotEmpty".to_string(),
      FOperator::IsGreater => "isGreater".to_string(),
      FOperator::IsGreaterEqual => "isGreaterEqual".to_string(),
      FOperator::IsLess => "isLess".to_string(),
      FOperator::IsLessEqual => "isLessEqual".to_string(),
      FOperator::IsRepeat => "isRepeat".to_string(),
    }
  }
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq)]
#[serde(untagged)]
pub enum ConditionValue {
  Bool(bool),
  StringArray(Vec<String>),
  DateTime(IFilterDateTime),
  Value(Value),
  Null,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq)]
#[serde(untagged)]
pub enum IFilterDateTime {
  Single(Vec<FilterDuration>),
  ExactDateOption(FilterDuration, Option<i64>),
  DateRangeOption(FilterDuration, Option<String>),
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq)]
pub enum FilterDuration {
  ExactDate,
  DateRange,
  Today,
  Tomorrow,
  Yesterday,
  ThisWeek,
  PreviousWeek,
  ThisMonth,
  PreviousMonth,
  ThisYear,
  SomeDayBefore,
  SomeDayAfter,
  TheLastWeek,
  TheNextWeek,
  TheLastMonth,
  TheNextMonth,
}

pub type Timestamp = u64;

#[cfg(test)]
mod tests {
  use serde_json::{json, Value};

  use crate::prelude::view_operation::filter::{
    FOperator, FilterConjunction, IFilterCondition, IFilterDateTime, IFilterInfo,
  };
  use crate::prelude::FieldKindSO;

  #[test]
  fn test_ser_to_under() {
    let json_str = r#"{
                            "conjunction": "and"
                        }"#;
    let x: IFilterInfo = serde_json::from_str(json_str).unwrap();
    assert_eq!(x.conditions, vec![]);
    assert_eq!(x.conjunction, FilterConjunction::And);
  }

  #[test]
  fn test_ser_from_null_json() {
    let json_str = r#"{
                            "conditions": null,
                            "conjunction": "and"
                        }"#;
    let x: IFilterInfo = serde_json::from_str(json_str).unwrap();
    assert_eq!(x.conditions, vec![]);
    assert_eq!(x.conjunction, FilterConjunction::And);
  }

  #[test]
  fn test_ser_from_json() {
    let json_str = r#"{
                            "conditions": [
                                {
                                    "value": [
                                        "的"
                                    ],
                                    "fieldId": "fldmHjmSjZxVn",
                                    "operator": "contains",
                                    "fieldType": 19,
                                    "conditionId": "cdtYvVSlw23A6"
                                }
                            ],
                            "conjunction": "and"
                        }"#;
    let json_body = json!({
        "conditions": [
            {
                "value": [
                    "的"
                ],
                "fieldId": "fldmHjmSjZxVn",
                "operator": "contains",
                "fieldType": 19,
                "conditionId": "cdtYvVSlw23A6"
            }
        ],
        "conjunction": "and"
    });
    let filter_info: IFilterInfo = serde_json::from_str(json_str).unwrap();
    assert_eq!(filter_info.conjunction, FilterConjunction::And);
    assert_eq!(json_body, serde_json::to_value(filter_info).unwrap());
  }

  #[test]
  fn test_serialize_filter_info() {
    let filter_info = IFilterInfo {
      conjunction: FilterConjunction::And,
      conditions: vec![IFilterCondition {
        condition_id: "cond_1".to_string(),
        field_id: "field_1".to_string(),
        operator: FOperator::Is,
        field_type: FieldKindSO::Text, // 这里替换为实际的 FieldKindSO 变体
        value: Value::String("value_1".to_string()),
      }],
    };

    let serialized = serde_json::to_string(&filter_info).unwrap();

    let expected_json = json!({
        "conjunction": "and",
        "conditions": [
            {
                "conditionId": "cond_1",
                "fieldId": "field_1",
                "operator": "is",
                "fieldType": 1,
                "value": "value_1"
            },
        ]
    });

    let expected_value: Value = serde_json::from_value(expected_json).unwrap();

    let serialized_value: Value = serde_json::from_str(&serialized).unwrap();

    assert_eq!(expected_value, serialized_value);
  }

  #[test]
  fn test_deserialize_filter_info() {
    let json_str = r#"{
        "conditions": [
            {
                "value": [
                    "ExactDate",
                    1700150400000
                ],
                "fieldId": "fldPHYeJ3xUfu",
                "operator": "is",
                "fieldType": 21,
                "conditionId": "cdt2kNs2mQmck"
            },
            {
                "value": [
                    "DateRange",
                    "1700150400000-1700236799999"
                ],
                "fieldId": "fldPHYeJ3xUfu",
                "operator": "is",
                "fieldType": 21,
                "conditionId": "cdtVwFOV8Z6GT"
            },
            {
                "value": [
                    "PreviousWeek"
                ],
                "fieldId": "fldPHYeJ3xUfu",
                "operator": "is",
                "fieldType": 21,
                "conditionId": "cdt6EgVP8GUh4"
            },
            {
                "value": [
                    "ExactDate",
                    null
                ],
                "fieldId": "fldPHYeJ3xUfu",
                "operator": "is",
                "fieldType": 21,
                "conditionId": "cdtZ96vu0vHIu"
            }
        ],
        "conjunction": "and"
    }"#;
    let x: IFilterInfo = serde_json::from_str(json_str).unwrap();

    assert_eq!(x.conditions.len(), 4);
  }

  #[test]
  pub fn test() {
    let json_str = r#"["PreviousWeek"]"#;
    // let json_str = r#"["ExactDate",1700150400000]"#;
    let x: IFilterDateTime = serde_json::from_str(json_str).unwrap();
  }
}
