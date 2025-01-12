use std::collections::HashMap;
use std::rc::Rc;

use serde::{Deserialize, Serialize};
use serde_json::{from_value, Value};
use utoipa::ToSchema;

use databus_shared::prelude::Json;

use crate::fields::field_factory::FieldFactory;
use crate::modules::database::store::selectors::resource::datasheet::cell_calc;
use crate::prelude::DatasheetPackContext;
use crate::prelude::{
  AttachmentCell, CascaderCell, CellValue, CellValueVo, EmailCell, FieldKindSO, PhoneCell, SingleTextCell, TextCell,
  UrlCell,
};
use crate::so::api_value::ApiValue;
use crate::so::field::FieldSO;
use crate::so::view::ViewSO;
use crate::vo::record_vo::{RecordDTO, RecordVO};

use super::datasheet_pack::DatasheetPackSO;
use super::{Comments, RecordAlarm};

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Eq, ToSchema, Default)]
#[serde(rename_all = "camelCase")]
pub struct RecordSO {
  pub id: String,
  pub comment_count: u32,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub comments: Option<Vec<Comments>>,
  pub data: Value,
  pub created_at: Option<i64>,
  pub updated_at: Option<i64>,
  pub revision_history: Option<Vec<u32>>,
  pub record_meta: Option<RecordMeta>,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Eq, ToSchema, Default)]
#[serde(rename_all = "camelCase")]
pub struct DatasheetRecordPO {
  pub record_id: Option<String>,
  pub dst_id: Option<String>,
  pub data: Option<Value>,
  pub revision_history: Option<String>,
  pub revision: Option<i64>,
  pub record_meta: Option<RecordMeta>,
  pub created_at: Option<i64>,
  pub updated_at: Option<i64>,
  pub created_time: Option<i64>,
  pub updated_time: Option<i64>,
  pub is_deleted: Option<bool>,
  pub created_by: Option<bool>,
  pub updated_by: Option<i64>,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Eq)]
#[serde(rename_all = "camelCase")]
pub struct RecordSOTO {
  pub id: String,
  pub data: Json,
  pub comment_count: u32,
  pub comments: Option<Vec<u32>>,
  pub record_meta: Option<Json>,
}

// pub type RecordMapSO = HashMapExt<String, RecordSO>;

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Eq, ToSchema, Default)]
#[serde(rename_all = "camelCase")]
pub struct RecordMeta {
  #[serde(skip_serializing_if = "Option::is_none")]
  pub field_updated_map: Option<HashMap<String, FieldUpdatedValue>>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub field_extra_map: Option<HashMap<String, FieldExtraMapValue>>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub created_by: Option<String>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub updated_by: Option<String>,
  #[serde(
    default,
    skip_serializing_if = "Option::is_none",
    deserialize_with = "crate::utils::serde_ext::deserialize_to_option_u64"
  )]
  pub created_at: Option<u64>,
  #[serde(
    default,
    skip_serializing_if = "Option::is_none",
    deserialize_with = "crate::utils::serde_ext::deserialize_to_option_u64"
  )]
  pub updated_at: Option<u64>,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Eq, ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct FieldExtraMapValue {
  pub alarm: Option<RecordAlarm>,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Eq, ToSchema, Default)]
#[serde(rename_all = "camelCase")]
pub struct FieldUpdatedValue {
  #[serde(
    default,
    skip_serializing_if = "Option::is_none",
    deserialize_with = "crate::utils::serde_ext::deserialize_to_option_u64"
  )]
  pub at: Option<u64>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub by: Option<String>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub auto_number: Option<u64>,
}

#[derive(Serialize, Deserialize)]
pub struct Record {
  pub record_id: String,
  pub data: Value,

  #[serde(rename = "createdAt")]
  pub created_at: u64,

  #[serde(rename = "updatedAt")]
  pub updated_at: u64,
}

#[derive(Serialize, Deserialize)]
pub struct Meta {
  views: Vec<ViewSO>,
  #[serde(rename = "fieldMap")]
  field_map: HashMap<String, super::field::Field>,
  // widgetPanel: Vec<Value>, // json value
}

impl Meta {
  pub fn new() -> Self {
    return Self {
      field_map: HashMap::new(),
      views: Vec::new(),
    };
  }
}

impl Record {
  pub fn new(datasheet_pack: &DatasheetPackSO, record_id: String) -> Self {
    let record_so = datasheet_pack.snapshot.record_map.get(&record_id).unwrap().clone();
    let record_data = record_so.data.clone();

    return Record {
      // snapshot: snapshot,
      record_id: record_id.clone(),
      data: record_data,
      created_at: 0,
      updated_at: 0,
    };
  }

  pub fn get_primary_field_id(&self) -> String {
    return "".to_string();
  }
}

impl RecordSO {
  pub fn to_dto(&self, field_map: &[FieldSO], context: Rc<DatasheetPackContext>) -> RecordDTO {
    let mut fields: HashMap<String, ApiValue> = HashMap::new();

    for field in field_map {
      let field_name = field.name.clone();

      let cell_value =
        cell_calc::get_cell_value(context.clone(), &context.datasheet_pack.snapshot, &self.id, &field.id);
      if cell_value == CellValue::Null {
        continue;
      }

      let base_field = FieldFactory::create_field(field.clone(), context.clone());
      let value = base_field.cell_value_to_api_standard_value(cell_value);
      if value != ApiValue::Null {
        fields.insert(field_name.clone(), value);
      }
    }
    RecordDTO {
      record_id: self.id.clone(),
      fields,
      created_at: self.created_at.unwrap() as u64,
      updated_at: self.updated_at.unwrap() as u64,
    }
  }

  pub fn to_vo(&self, field_map: &HashMap<String, FieldSO>) -> RecordVO {
    let mut data: HashMap<String, CellValueVo> = HashMap::new();
    if let Value::Object(obj) = &self.data {
      for (key, val) in obj {
        if let Some(field_info) = field_map.get(&*key) {
          match &field_info.kind {
            FieldKindSO::NotSupport => {
              data.insert(key.clone(), CellValueVo::NoValue);
            }
            FieldKindSO::Text => {
              if let Some(arr) = val.as_array() {
                let texts: Vec<TextCell> = arr.iter().filter_map(|item| from_value(item.clone()).ok()).collect();
                data.insert(key.clone(), CellValueVo::TextCellValue(texts));
              }
            }
            FieldKindSO::Number => match val {
              Value::Number(num) => {
                data.insert(key.clone(), CellValueVo::NumberCellValue((*num).clone()));
              }
              _ => {}
            },
            FieldKindSO::SingleSelect => match val {
              Value::String(str) => {
                data.insert(key.clone(), CellValueVo::SingleSelectCellValue(str.clone()));
              }
              _ => {}
            },
            FieldKindSO::MultiSelect => match val {
              Value::Array(arr) => {
                let mut selects: Vec<String> = Vec::new();
                for item in arr {
                  if let Value::String(str) = item {
                    selects.push(str.clone());
                  }
                }
                data.insert(key.clone(), CellValueVo::MultiSelectCellValue(selects));
              }
              _ => {}
            },
            FieldKindSO::DateTime => match val {
              Value::Number(num) => {
                data.insert(key.clone(), CellValueVo::DateTimeCellValue((*num).clone()));
              }
              _ => {}
            },
            FieldKindSO::Attachment => match val {
              Value::Array(arr) => {
                let mut attachments: Vec<AttachmentCell> = Vec::new();
                for item in arr {
                  let attachment = from_value(item.clone()).unwrap();
                  attachments.push(attachment);
                }
                data.insert(key.clone(), CellValueVo::AttachmentCellValue(attachments));
              }
              _ => {}
            },
            FieldKindSO::Link => match val {
              Value::Array(arr) => {
                let mut links: Vec<String> = Vec::new();
                for item in arr {
                  if let Value::String(str) = item {
                    links.push(str.clone());
                  }
                }
                data.insert(key.clone(), CellValueVo::LinkCellValue(links));
              }
              _ => {}
            },
            FieldKindSO::URL => match val {
              Value::Array(arr) => {
                let mut urls: Vec<UrlCell> = Vec::new();
                for item in arr {
                  let attachment = from_value(item.clone()).unwrap();
                  urls.push(attachment);
                }
                data.insert(key.clone(), CellValueVo::URLCellValue(urls));
              }
              _ => {}
            },
            FieldKindSO::Email => match val {
              Value::Array(arr) => {
                let mut emails: Vec<EmailCell> = Vec::new();
                for item in arr {
                  let attachment = from_value(item.clone()).unwrap();
                  emails.push(attachment);
                }
                data.insert(key.clone(), CellValueVo::EmailCellValue(emails));
              }
              _ => {}
            },
            FieldKindSO::Phone => match val {
              Value::Array(arr) => {
                let mut phones: Vec<PhoneCell> = Vec::new();
                for item in arr {
                  let attachment = from_value(item.clone()).unwrap();
                  phones.push(attachment);
                }
                data.insert(key.clone(), CellValueVo::PhoneCellValue(phones));
              }
              _ => {}
            },
            FieldKindSO::Checkbox => match val {
              Value::Bool(b) => {
                data.insert(key.clone(), CellValueVo::CheckboxCellValue(*b));
              }
              _ => {}
            },
            FieldKindSO::Rating => match val {
              Value::Number(num) => {
                data.insert(key.clone(), CellValueVo::RatingCellValue((*num).clone()));
              }
              _ => {}
            },
            FieldKindSO::Member => match val {
              Value::Array(arr) => {
                let mut members: Vec<String> = Vec::new();
                for item in arr {
                  if let Value::String(str) = item {
                    members.push(str.clone());
                  }
                }
                data.insert(key.clone(), CellValueVo::MemberCellValue(members));
              }
              _ => {}
            },
            FieldKindSO::LookUp => {
              data.insert(key.clone(), CellValueVo::LookUpCellValue(vec![]));
            }
            FieldKindSO::Formula => {
              data.insert(key.clone(), CellValueVo::FormulaCellValue(None));
            }
            FieldKindSO::Currency => match val {
              Value::Number(num) => {
                data.insert(key.clone(), CellValueVo::CurrencyCellValue((*num).clone()));
              }
              _ => {}
            },
            FieldKindSO::Percent => match val {
              Value::Number(num) => {
                data.insert(key.clone(), CellValueVo::PercentCellValue((*num).clone()));
              }
              _ => {}
            },
            FieldKindSO::SingleText => {
              if let Some(arr) = val.as_array() {
                let texts: Vec<SingleTextCell> = arr.iter().filter_map(|item| from_value(item.clone()).ok()).collect();
                data.insert(key.clone(), CellValueVo::SingleTextCellValue(texts));
              }
            }
            FieldKindSO::AutoNumber => {
              data.insert(key.clone(), CellValueVo::AutoNumberCellValue(None));
            }
            FieldKindSO::CreatedTime => {
              data.insert(key.clone(), CellValueVo::CreatedTimeCellValue(None));
            }
            FieldKindSO::LastModifiedTime => {
              data.insert(key.clone(), CellValueVo::LastModifiedTimeCellValue(None));
            }
            FieldKindSO::CreatedBy => {
              data.insert(key.clone(), CellValueVo::CreatedByCellValue(None));
            }
            FieldKindSO::LastModifiedBy => {
              data.insert(key.clone(), CellValueVo::LastModifiedByCellValue(None));
            }
            FieldKindSO::Cascader => match val {
              Value::Array(arr) => {
                let mut cascaders: Vec<CascaderCell> = Vec::new();
                for item in arr {
                  let cascader = from_value(item.clone()).unwrap();
                  cascaders.push(cascader);
                }
                data.insert(key.clone(), CellValueVo::CascaderCellValue(cascaders));
              }
              _ => {}
            },
            FieldKindSO::OneWayLink => match val {
              Value::Array(arr) => {
                let mut links: Vec<String> = Vec::new();
                for item in arr {
                  if let Value::String(str) = item {
                    links.push(str.clone());
                  }
                }
                data.insert(key.clone(), CellValueVo::OneWayLinkCellValue(links));
              }
              _ => {}
            },
            _ => {}
          }
        }
      }
    }
    RecordVO {
      id: self.id.clone(),
      comment_count: self.comment_count,
      comments: self.comments.clone(),
      data,
      created_at: self.created_at,
      updated_at: self.updated_at,
      revision_history: self.revision_history.clone(),
      record_meta: self.record_meta.clone(),
    }
  }

  pub fn get_field_value(&self, field: &FieldSO) -> CellValue {
    CellValue::from_value(&self.data[&field.id], &field.kind)
  }

  pub fn get_field_value_map(&self, field_map: &[FieldSO]) -> HashMap<String, CellValue> {
    let mut data: HashMap<String, CellValue> = HashMap::new();
    for field in field_map {
      let field_name = field.name.clone();
      let value = &self.data[&field.id];
      if !value.is_null() {
        data.insert(field_name, CellValue::from_value(value, &field.kind));
      }
    }
    data
  }
}

#[test]
fn test_json() {
  use serde_json::json;

  let json = json!({
    "createdAt": 111111100.0f64
  });

  let result: RecordMeta = serde_json::from_value(json).unwrap();
  println!("{:?}", result);

  let json = json!({});

  let result: RecordMeta = serde_json::from_value(json).unwrap();
  println!("{:?}", result);

  let json = json!({
    "createdAt": 111111100
  });

  let result: RecordMeta = serde_json::from_value(json).unwrap();
  println!("{:?}", result);
}
