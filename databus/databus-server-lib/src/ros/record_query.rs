use databus_core::utils::serde_ext;
use serde::{de, Deserialize, Deserializer, Serialize};
use utoipa::{IntoParams, ToSchema};
use validator::Validate;

#[derive(Debug, Deserialize, Validate, IntoParams)]
pub struct RecordQueryRO {
  #[serde(
    default,
    rename = "recordIds",
    deserialize_with = "serde_ext::deserialize_to_vec_string"
  )]
  pub record_ids: Option<Vec<String>>,

  #[serde(rename = "viewId")]
  pub view_id: Option<String>,

  #[serde(
    default,
    rename = "fields",
    deserialize_with = "serde_ext::deserialize_to_vec_string"
  )]
  pub fields: Option<Vec<String>>,

  #[serde(rename = "filterByFormula")]
  pub filter_by_formula: Option<String>,

  #[serde(default = "default_cell_format", rename = "cellFormat")]
  pub cell_format: CellFormatEnum,

  #[serde(default = "default_field_key", rename = "fieldKey")]
  pub field_key: FieldKeyEnum,
}

fn default_cell_format() -> CellFormatEnum {
  CellFormatEnum::Json
}

fn default_field_key() -> FieldKeyEnum {
  FieldKeyEnum::Name
}

#[derive(Debug, Serialize, ToSchema)]
#[serde(rename_all = "snake_case")]
pub enum CellFormatEnum {
  String,
  Json,
}
impl<'de> Deserialize<'de> for CellFormatEnum {
  fn deserialize<D>(deserializer: D) -> Result<Self, D::Error>
    where
        D: Deserializer<'de>,
  {
    struct CellFormatEnumVisitor;

    impl<'de> de::Visitor<'de> for CellFormatEnumVisitor {
      type Value = CellFormatEnum;

      fn expecting(&self, formatter: &mut std::fmt::Formatter) -> std::fmt::Result {
        formatter.write_str("enum FieldKeyEnum")
      }

      fn visit_str<E>(self, value: &str) -> Result<Self::Value, E>
        where
            E: de::Error,
      {
        match value.to_lowercase().as_str() {
          "string" => Ok(CellFormatEnum::String),
          "json" => Ok(CellFormatEnum::Json),
          _ => Err(de::Error::unknown_variant(value, &["string", "json"])),
        }
      }
    }

    deserializer.deserialize_str(CellFormatEnumVisitor)
  }
}

#[derive(Debug, Serialize)]
#[serde(rename_all = "snake_case")]
pub enum FieldKeyEnum {
  Id,
  Name,
}


impl<'de> Deserialize<'de> for FieldKeyEnum {
  fn deserialize<D>(deserializer: D) -> Result<Self, D::Error>
    where
        D: Deserializer<'de>,
  {
    struct FieldKeyEnumVisitor;

    impl<'de> de::Visitor<'de> for FieldKeyEnumVisitor {
      type Value = FieldKeyEnum;

      fn expecting(&self, formatter: &mut std::fmt::Formatter) -> std::fmt::Result {
        formatter.write_str("enum FieldKeyEnum")
      }

      fn visit_str<E>(self, value: &str) -> Result<Self::Value, E>
        where
            E: de::Error,
      {
        match value.to_lowercase().as_str() {
          "id" => Ok(FieldKeyEnum::Id),
          "name" => Ok(FieldKeyEnum::Name),
          _ => Err(de::Error::unknown_variant(value, &["id", "name"])),
        }
      }
    }

    deserializer.deserialize_str(FieldKeyEnumVisitor)
  }
}