use serde_json::{Number, Value};
use std::collections::HashMap;
use std::rc::Rc;

use crate::fields::base_field::IBaseField;
use crate::fields::date_time_base_field::{datetime_format, get_user_time_zone};
use crate::fields::ext::types::CellToStringOption;
use crate::fields::number_base_field::number_format;
use crate::fields::property::field_types::{BasicValueType, IComputedFieldFormattingProperty};
use crate::fields::property::{DateTimeFieldPropertySO, FormulaFieldPropertySO};
use crate::formula::errors::FormulaResult;
use crate::formula::evaluate::{parse, FormulaExpr};
use crate::formula::parser::Context;
use crate::formula::types::IBaseField as BaseField;
use crate::formula::types::IField;
use crate::prelude::api_value::ApiValue;
use crate::prelude::{CellValue, DatasheetPackContext};

use super::computed_formatting_to_format;
use super::property::FieldPropertySO;

pub struct Formula {
  field_conf: Rc<IField>,
  state: Rc<DatasheetPackContext>,
}

impl Formula {
  pub fn new(field_conf: Rc<IField>, state: Rc<DatasheetPackContext>) -> Self {
    return Self { field_conf, state };
  }

  fn field(&self) -> &BaseField<FormulaFieldPropertySO> {
    return match self.field_conf.as_ref() {
      IField::Formula(field) => field,
      _ => panic!("logic error"),
    };
  }

  fn get_parse_result(&self) -> FormulaResult<FormulaExpr> {
    let field = self.field();
    let snapshot = self.state.get_snapshot(&field.property.datasheet_id);

    // TODO: remove the clone
    let field_map = snapshot
      .unwrap()
      .meta
      .field_map
      .iter()
      .map(|(k, v)| (k.clone(), Rc::new(IField::from_so(v.clone()))))
      .collect::<HashMap<_, _>>();

    let ctx = Context {
      field: self.field_conf.clone(),
      field_map,
      state: self.state.clone(),
    };
    return parse(field.property.expression.clone(), Rc::new(ctx));
  }

  fn array_value_to_array_string_value_array(
    &self,
    cell_value: Vec<CellValue>,
    _option: Option<CellToStringOption>,
  ) -> Vec<Option<String>> {
    let inner_basic_value_type = self.inner_basic_value_type();
    let vec = cell_value
      .into_iter()
      .map(|cv| {
        return match inner_basic_value_type {
          BasicValueType::Number | BasicValueType::Boolean => number_format(&cv, &self.field().property.formatting),
          BasicValueType::DateTime => {
            let mut formatting = None;

            // TODO: refactor
            if let Some(IComputedFieldFormattingProperty::DateTime(property)) = &self.field().property.formatting {
              formatting = Some(DateTimeFieldPropertySO {
                date_format: property.date_format.clone(),
                time_format: property.time_format.clone(),
                include_time: property.include_time,
                auto_fill: false,
                time_zone: property.time_zone.clone(),
                include_time_zone: None,
              });
            };
            datetime_format(&cv, &formatting, &get_user_time_zone(&self.state))
          }
          BasicValueType::String => {
            return Some(cv.to_string());
          }
          _ => {
            return Some(cv.to_string());
          }
        };
      })
      .collect();
    return vec;
  }
}

impl IBaseField for Formula {
  fn basic_value_type(&self) -> BasicValueType {
    return match self.get_parse_result() {
      Ok(expr) => expr.ast.get_value_type().clone(),
      Err(_) => BasicValueType::String,
    };
  }

  fn inner_basic_value_type(&self) -> BasicValueType {
    return match self.get_parse_result() {
      Ok(expr) => expr
        .ast
        .get_inner_value_type()
        .clone()
        .unwrap_or(BasicValueType::String),
      Err(_) => BasicValueType::String,
    };
  }

  fn is_computed(&self) -> bool {
    true
  }

  fn cell_value_to_string(&self, cell_value: CellValue, option: Option<CellToStringOption>) -> Option<String> {
    if cell_value.is_null() {
      return None;
    }
    let field = self.field();
    return match self.basic_value_type() {
      BasicValueType::Number | BasicValueType::Boolean => number_format(&cell_value, &field.property.formatting),
      BasicValueType::DateTime => {
        let mut formatting = None;

        // TODO: refactor
        if let Some(IComputedFieldFormattingProperty::DateTime(property)) = &field.property.formatting {
          formatting = Some(DateTimeFieldPropertySO {
            date_format: property.date_format.clone(),
            time_format: property.time_format.clone(),
            include_time: property.include_time,
            auto_fill: false,
            time_zone: property.time_zone.clone(),
            include_time_zone: None,
          });
        };

        datetime_format(&cell_value, &formatting, &get_user_time_zone(&self.state))
      }
      BasicValueType::String => {
        return Some(cell_value.to_string());
      }
      BasicValueType::Array => self.array_value_to_string(cell_value),
    };
  }

  fn array_value_to_string(&self, cell_value: CellValue) -> Option<String> {
    if let CellValue::Array(arr) = cell_value {
      let v_array = self.array_value_to_array_string_value_array(arr, None);

      let s = v_array
        .into_iter()
        .map(|v| v.unwrap_or("".to_string()))
        .collect::<Vec<_>>()
        .join(", ");
      return Some(s);
    }
    panic!("logic error")
  }

  fn cell_value_to_api_standard_value(&self, cell_value: CellValue) -> ApiValue {
    return match cell_value {
      CellValue::Null => ApiValue::Null,
      CellValue::Number(v) => ApiValue::Number(Number::from_f64(v).unwrap()),
      CellValue::Bool(v) => ApiValue::Bool(v),
      CellValue::String(v) => ApiValue::String(v),
      _ => todo!(),
    };
  }
}

impl Formula {
  pub fn validate_add_open_field_property(_property: Value) -> anyhow::Result<()>{
    Ok(())
  }

  pub fn update_open_field_property_transform_property(open_field_property: FieldPropertySO, default_property: FieldPropertySO) -> FieldPropertySO{
    FieldPropertySO {
      datasheet_id: default_property.datasheet_id,
      expression: Some(open_field_property.expression.unwrap_or(String::new())),
      formatting: computed_formatting_to_format(open_field_property.formatting),
      ..Default::default()
    }
  }
}
