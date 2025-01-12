use serde::{Deserialize, Serialize};
use serde_repr::{Deserialize_repr, Serialize_repr};
use strum_macros::{Display, EnumString, FromRepr};
use utoipa::ToSchema;

#[derive(Debug, Serialize, Deserialize, PartialEq, Eq, Clone)]
pub enum FormulaFuncType {
  Array,
  DateTime,
  Logical,
  Numeric,
  Record,
  Text,
}

#[derive(Debug, Serialize, Deserialize, PartialEq, Eq, Clone, EnumString)]
pub enum BasicValueType {
  String,
  Number,
  DateTime,
  Array,
  Boolean,
}

#[derive(Debug, Serialize, Deserialize, PartialEq, Eq, Clone, Default)]
#[serde(rename_all = "camelCase")]
pub struct IDateTimeFieldPropertyFormat {
  pub date_format: DateFormat,
  pub time_format: TimeFormat,
  pub include_time: bool,
  pub time_zone: Option<String>,
  pub include_time_zone: Option<bool>,
}

#[derive(Debug, Serialize, Deserialize, PartialEq, Eq, Clone, Default)]
#[serde(rename_all = "camelCase")]
pub struct INumberBaseFieldPropertyFormat {
  pub format_type: i32,
  pub precision: i32,
  pub symbol: Option<String>,
}

#[derive(Debug, Serialize, Deserialize, PartialEq, Eq, Clone)]
#[serde(rename_all = "camelCase")]
#[serde(untagged)]
pub enum IComputedFieldFormattingProperty {
  DateTime(IDateTimeFieldPropertyFormat),
  Number(INumberBaseFieldPropertyFormat),
}

impl<'__s> utoipa::ToSchema<'__s> for IComputedFieldFormattingProperty {
  fn schema() -> (&'__s str, utoipa::openapi::RefOr<utoipa::openapi::schema::Schema>) {
    (
      "IComputedFieldFormattingProperty",
      utoipa::openapi::ObjectBuilder::new()
          .into(),
    ) }
}


#[derive(Debug, Serialize, Deserialize, PartialEq, Eq, Clone, EnumString, Display, ToSchema)]
pub enum RollUpFuncType {
  VALUES,
  AVERAGE,
  COUNT,
  COUNTA,
  COUNTALL,
  SUM,
  MIN,
  MAX,
  AND,
  OR,
  XOR,
  CONCATENATE,
  ARRAYJOIN,
  ARRAYUNIQUE,
  ARRAYCOMPACT,
}

impl RollUpFuncType {
  pub fn is_origin_values_func(&self) -> bool {
    [
      RollUpFuncType::VALUES,
      RollUpFuncType::ARRAYUNIQUE,
      RollUpFuncType::ARRAYCOMPACT,
    ].contains(self)
  }

  pub fn is_lookup_func(&self) -> bool {
    [
      RollUpFuncType::ARRAYJOIN,
      RollUpFuncType::CONCATENATE,
    ].contains(self)
  }

  pub fn is_not_formula_func(&self) -> bool {
    [
      RollUpFuncType::ARRAYJOIN,
      RollUpFuncType::CONCATENATE,
      RollUpFuncType::ARRAYUNIQUE,
      RollUpFuncType::ARRAYCOMPACT,
    ].contains(self)
  }
}

#[derive(Debug, Serialize, Deserialize, PartialEq, Eq, Clone, ToSchema)]
pub enum LookUpLimitType {
  ALL,
  FIRST,
}

#[derive(Debug, Serialize_repr, Deserialize_repr, PartialEq, Eq, Clone, FromRepr, ToSchema)]
#[repr(u8)]
pub enum CollectType {
  AllFields = 0,
  SpecifiedFields = 1,
}

#[derive(Debug, Serialize_repr, Deserialize_repr, PartialEq, Eq, Clone, FromRepr, ToSchema)]
#[repr(u8)]
pub enum SymbolAlign {
  Default = 0,
  Left = 1,
  Right = 2,
}

#[derive(Debug, Serialize_repr, Deserialize_repr, PartialEq, Eq, Clone, FromRepr, ToSchema, Default)]
#[repr(u8)]
pub enum DateFormat {
  /// 'YYYY/MM/DD'
  #[default]
  SYyyyMmDd = 0,
  /// 'YYYY-MM-DD'
  UYyyyMmDd = 1,
  /// DD/MM/YYYY
  DdMmYyyy = 2,
  /// YYYY-MM
  YyyyMm = 3,
  /// MM-DD
  MmDd = 4,
  YYYY = 5,
  MM = 6,
  DD = 7,
}

impl DateFormat {
  pub fn get_format(&self) -> &'static str {
    match self {
      DateFormat::SYyyyMmDd => "YYYY/MM/DD",
      DateFormat::UYyyyMmDd => "YYYY-MM-DD",
      DateFormat::DdMmYyyy => "DD/MM/YYYY",
      DateFormat::YyyyMm => "YYYY-MM",
      DateFormat::MmDd => "MM-DD",
      DateFormat::YYYY => "YYYY",
      DateFormat::MM => "MM",
      DateFormat::DD => "DD",
    }
  }
}

#[derive(Debug, Serialize_repr, Deserialize_repr, PartialEq, Eq, Clone, FromRepr, ToSchema, Default)]
#[repr(u8)]
pub enum TimeFormat {
  /// 'HH:mm'
  #[default]
  HHmm = 0,
  /// 'hh:mm'
  Hhmm = 1,
}

impl TimeFormat {
  pub fn get_format(&self) -> &'static str {
    match self {
      TimeFormat::HHmm => "HH:mm",
      TimeFormat::Hhmm => "hh:mm",
    }
  }
}

#[cfg(test)]
mod test {
  use crate::fields::property::field_types::CollectType;
  use serde::{Deserialize, Serialize};

  #[derive(Debug, Serialize, Deserialize, PartialEq, Eq, Clone)]
  #[serde(rename_all = "camelCase")]
  struct TestObject {
    pub collect_type: CollectType,
  }

  #[test]
  fn test() {
    let object = TestObject {
      collect_type: CollectType::AllFields,
    };

    println!("{}", serde_json::to_string(&object).unwrap());

    let x = "{\"collectType\":0}";
    let result = serde_json::from_str::<TestObject>(x);

    println!("{:?}", result.unwrap());
  }
}
