use serde::{Deserialize, Serialize};
use serde_repr::{Deserialize_repr, Serialize_repr};
use utoipa::ToSchema;
use std::collections::HashSet;


use crate::fields::property::FieldPropertySO;

use super::{DatasheetPackSO, ViewColumnSO};

// pub type FieldMapSO = HashMapExt<String, FieldSO>;

#[derive(Serialize, Deserialize)]
pub struct Field {
  id: String,
  name: String,
  r#type: u64,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Eq, ToSchema, Default)]
#[serde(rename_all = "camelCase")]
pub struct FieldSO {
  pub id: String,
  pub name: String,

  #[serde(skip_serializing_if = "Option::is_none")]
  pub desc: Option<String>,

  #[serde(skip_serializing_if = "Option::is_none")]
  pub required: Option<bool>,

  #[serde(rename = "type")]
  pub kind: FieldKindSO,

  #[serde(skip_serializing_if = "Option::is_none")]
  pub property: Option<FieldPropertySO>,
}

#[derive(Serialize_repr, Deserialize_repr, Debug, Clone, Copy, PartialEq, Eq, Hash, ToSchema, Default)]
#[repr(u32)]
pub enum FieldKindSO {
  #[default]
  NotSupport = 0,
  Text = 1,
  Number = 2,
  SingleSelect = 3,
  MultiSelect = 4,
  DateTime = 5,
  Attachment = 6,
  Link = 7,
  URL = 8,
  Email = 9,
  Phone = 10,
  Checkbox = 11,
  Rating = 12,
  Member = 13,
  LookUp = 14,
  // RollUp = 15,
  Formula = 16,
  Currency = 17,
  Percent = 18,
  SingleText = 19,
  AutoNumber = 20,
  CreatedTime = 21,
  LastModifiedTime = 22,
  CreatedBy = 23,
  LastModifiedBy = 24,
  Cascader = 25,
  OneWayLink = 26,
  WorkDoc = 27,
  Button = 28,
  /// no permission column
  DeniedField = 999,
}

impl FieldKindSO {
  pub fn to_string(&self) -> String {
    match self {
      FieldKindSO::NotSupport => "NotSupport".to_string(),
      FieldKindSO::Text => "Text".to_string(),
      FieldKindSO::Number => "Number".to_string(),
      FieldKindSO::SingleSelect => "SingleSelect".to_string(),
      FieldKindSO::MultiSelect => "MultiSelect".to_string(),
      FieldKindSO::DateTime => "DateTime".to_string(),
      FieldKindSO::Attachment => "Attachment".to_string(),
      FieldKindSO::Link => "Link".to_string(),
      FieldKindSO::URL => "URL".to_string(),
      FieldKindSO::Email => "Email".to_string(),
      FieldKindSO::Phone => "Phone".to_string(),
      FieldKindSO::Checkbox => "Checkbox".to_string(),
      FieldKindSO::Rating => "Rating".to_string(),
      FieldKindSO::Member => "Member".to_string(),
      FieldKindSO::LookUp => "LookUp".to_string(),
      // FieldKindSO::RollUp => "RollUp".to_string(),
      FieldKindSO::Formula => "Formula".to_string(),
      FieldKindSO::Currency => "Currency".to_string(),
      FieldKindSO::Percent => "Percent".to_string(),
      FieldKindSO::SingleText => "SingleText".to_string(),
      FieldKindSO::AutoNumber => "AutoNumber".to_string(),
      FieldKindSO::CreatedTime => "CreatedTime".to_string(),
      FieldKindSO::LastModifiedTime => "LastModifiedTime".to_string(),
      FieldKindSO::CreatedBy => "CreatedBy".to_string(),
      FieldKindSO::LastModifiedBy => "LastModifiedBy".to_string(),
      FieldKindSO::Cascader => "Cascader".to_string(),
      FieldKindSO::OneWayLink => "OneWayLink".to_string(),
      FieldKindSO::WorkDoc => "WorkDoc".to_string(),
      FieldKindSO::Button => "Button".to_string(),
      FieldKindSO::DeniedField => "DeniedField".to_string(),
    }
  }
}

pub fn get_field_map(datasheet_pack: &DatasheetPackSO, columns: &Vec<ViewColumnSO>,
                     fields: Option<Vec<String>>) -> Vec<FieldSO> {
  // println!("column.field_id: {:?}", columns);
  // println!("datasheet_pack.snapshot.meta.field_map: {:?}", datasheet_pack.snapshot.meta.field_map);
  // println!("zzq see column {:?}", columns);
  let mut set = HashSet::new();
  if fields.is_some() {
    let fields = fields.unwrap();
    for field in fields {
      set.insert(field);
    }
  };
  columns
    .iter()
    .map(|column| datasheet_pack.snapshot.meta.field_map[&column.field_id].clone())
      .filter(|field| {
        if set.is_empty(){
          true
        }else {
          let field_name = field.name.clone();
            if set.contains(&field_name) {
              true
            } else {
              false
            }
        }
      })
      .collect()
}
