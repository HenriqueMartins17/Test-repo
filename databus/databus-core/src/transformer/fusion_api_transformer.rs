use std::{collections::HashMap, rc::Rc};

use serde_json::Value;

use crate::modules::database::store::selectors::resource::datasheet::cell_calc::get_cell_value;
use crate::{
  dtos::fusion_api_dtos::ApiRecordDto,
  fields::field_factory::FieldFactory,
  logic::{CellFormatEnum, IRecordVoTransformOptions},
  so::{CellValue, DatasheetPackContext, FieldSO, RecordSO},
};

#[derive(Debug)]
pub struct IFieldVoTransformOptions {
  field_map: Option<HashMap<String, FieldSO>>,
  record: Option<RecordSO>,
  // foreign_sheet_map: Option<HashMap<String, IDatasheetData>>,
  pub cell_format: Option<CellFormatEnum>,
}

pub struct FusionApiTransformer {}

impl FusionApiTransformer {
  pub fn new() -> Self {
    Self {}
  }

  pub fn record_vo_transform(
    &self,
    record: RecordSO,
    options: IRecordVoTransformOptions,
    cell_format: Option<CellFormatEnum>,
    context: Rc<DatasheetPackContext>,
  ) -> ApiRecordDto {
    let IRecordVoTransformOptions {
      field_keys,
      column_map,
      field_map,
    } = options;
    // let state = store.get_state();
    let snapshot = context.datasheet_pack.snapshot.clone();
    let mut fields = HashMap::new();
    for field in &field_keys {
      let column = column_map.get(&field_map.get(field).unwrap().id);
      if let Some(column) = column {
        if !column.hidden.unwrap_or(false) {
          let cell_value = get_cell_value(
            context.clone(),
            &snapshot,
            &record.id,
            &field_map.get(field).unwrap().id,
          );
          let value = self.vo_transform(
            cell_value,
            field_map.get(field).unwrap(),
            IFieldVoTransformOptions {
              field_map: Some(field_map.clone()),
              record: Some(record.clone()),
              cell_format: cell_format.clone(),
            },
            context.clone(),
          );
          if value != Value::Null {
            fields.insert(field.clone(), value);
          }
        }
      }
    }
    ApiRecordDto {
      record_id: record.id.clone(),
      created_at: record.created_at.clone(),
      updated_at: record.updated_at,
      fields,
    }
  }

  pub fn vo_transform(
    &self,
    field_value: CellValue,
    field: &FieldSO,
    options: IFieldVoTransformOptions,
    context: Rc<DatasheetPackContext>,
  ) -> Value {
    let base_field = FieldFactory::create_field(field.clone(), context.clone());
    let value = base_field.vo_transform(field_value, field.clone(), options);
    value.to_value()
  }
}
