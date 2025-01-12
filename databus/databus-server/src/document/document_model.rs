use serde::{Deserialize, Serialize};
use utoipa::ToSchema;


#[derive(Deserialize, Serialize, ToSchema)]
pub struct DocumentRO {
  pub space_id: String,
  pub resource_id: String,
  pub document_type: u8,
  pub data: Vec<u8>,
  pub props: Option<String>,
  pub title: Option<String>,
  #[serde(deserialize_with = "deserialize_optional_u64")]
  pub updated_by: Option<u64>,
}

#[derive(Deserialize, Serialize, ToSchema)]
pub struct DocumentOperationRO {
  pub space_id: String,
  pub update_data: Vec<u8>,
  #[serde(deserialize_with = "deserialize_optional_u64")]
  pub created_by: Option<u64>,
}

fn deserialize_optional_u64<'de, D>(deserializer: D) -> Result<Option<u64>, D::Error>
where
  D: serde::Deserializer<'de>,
{
    let s: Option<&str> = Deserialize::deserialize(deserializer)?;
    match s {
        Some(value) => {
            if !value.is_empty() {
                match value.parse::<u64>() {
                    Ok(parsed_value) => Ok(Some(parsed_value)),
                    Err(_) => Err(serde::de::Error::custom("Invalid u64 value")),
                }
            } else {
                Ok(None)
            }
        }
        None => Ok(None),
    }
}

#[derive(Deserialize, Serialize, ToSchema)]
pub struct DocumentPropsRO {
  pub resource_id: String,
  pub document_names: Vec<String>,
  pub record_id: String,
}