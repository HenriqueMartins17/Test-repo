use std::collections::HashMap;
use std::rc::Rc;

use anyhow::anyhow;
use serde::{Deserialize, Serialize};
use serde_with::serde_as;
use serde_with::DefaultOnError;
use utoipa::ToSchema;

use crate::compute_manager::view_derivate::slice::view_filter_derivate::ViewFilterDerivate;
use crate::fields::property::FormulaFieldPropertySO;
use crate::formula::evaluate::{evaluate, expression_transform, parse, ExpressionTransformTarget};
use crate::formula::functions::basic::FormulaEvaluateContext;
use crate::formula::types::{IBaseField, IField};
use crate::prelude::style::ViewStyleSo;
use crate::prelude::types::IViewLockInfo;
use crate::prelude::view_operation::filter::IFilterInfo;
use crate::prelude::view_operation::sort::{ISortInfo, ISortedField};
use crate::prelude::DatasheetPackContext;
use crate::utils::uuid::{get_new_id, IDPrefix};

use super::datasheet_pack::DatasheetPackSO;

#[serde_as]
#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, ToSchema, Default)]
#[serde(rename_all = "camelCase")]
pub struct ViewSO {
  pub id: Option<String>,
  pub name: Option<String>,
  pub r#type: Option<u64>,
  pub columns: Vec<ViewColumnSO>,
  pub rows: Option<Vec<ViewRowSO>>,

  #[serde(skip_serializing_if = "Option::is_none")]
  pub filter_info: Option<IFilterInfo>,

  #[serde_as(deserialize_as = "DefaultOnError")]
  #[serde(default, skip_serializing_if = "Option::is_none")]
  pub sort_info: Option<ISortInfo>,

  #[serde(skip_serializing_if = "Option::is_none")]
  pub lock_info: Option<IViewLockInfo>,

  #[serde(skip_serializing_if = "Option::is_none")]
  pub display_hidden_column_within_mirror: Option<bool>,

  #[serde(skip_serializing_if = "Option::is_none")]
  pub auto_save: Option<bool>,

  #[serde(skip_serializing_if = "Option::is_none")]
  pub description: Option<String>,

  #[serde(skip_serializing_if = "Option::is_none")]
  pub hidden: Option<bool>,

  #[serde(skip_serializing_if = "Option::is_none")]
  pub group_info: Option<Vec<ISortedField>>,

  #[serde(skip_serializing_if = "Option::is_none")]
  pub frozen_column_count: Option<i32>,

  #[serde(skip_serializing_if = "Option::is_none")]
  pub row_height_level: Option<i32>,

  #[serde(skip_serializing_if = "Option::is_none")]
  pub auto_head_height: Option<bool>,

  #[serde(skip_serializing_if = "Option::is_none")]
  pub style: Option<ViewStyleSo>,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, ToSchema, Default)]
#[serde(rename_all = "camelCase")]
pub struct ViewColumnSO {
  pub field_id: String,

  #[serde(skip_serializing_if = "Option::is_none")]
  pub hidden: Option<bool>,

  #[serde(skip_serializing_if = "Option::is_none")]
  pub width: Option<f64>,

  #[serde(skip_serializing_if = "Option::is_none")]
  pub stat_type: Option<i32>,

  #[serde(skip_serializing_if = "Option::is_none")]
  pub hidden_in_gantt: Option<bool>,

  #[serde(skip_serializing_if = "Option::is_none")]
  pub hidden_in_org_chart: Option<bool>,

  #[serde(skip_serializing_if = "Option::is_none")]
  pub hidden_in_calendar: Option<bool>,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, ToSchema, Default)]
#[serde(rename_all = "camelCase")]
pub struct ViewRowSO {
  pub record_id: String,

  #[serde(skip_serializing_if = "Option::is_none")]
  pub hidden: Option<bool>,
}
// use super::types::ViewColumn;
// use super::types::ViewProperty;
// use super::types::ViewRow;

pub fn calc_visible_rows(
  filter_by_formula: Option<String>,
  context: Rc<DatasheetPackContext>,
  view: &mut ViewSO,
) -> anyhow::Result<()> {
  if let Some(expression) = filter_by_formula {
    get_filter_by_formula(expression, context.clone(), view)?;
  }

  let derivate = ViewFilterDerivate::new(context.clone(), context.datasheet_pack.datasheet.id.clone());

  // where exact match by field
  let mut filtered_rows = derivate.get_filtered_rows(view);
  // hidden rows
  filtered_rows.retain(|row| !row.hidden.unwrap_or(false));

  // order by
  let sorted_rows = derivate.get_sort_rows(view, filtered_rows);
  // group by
  let sorted_rows = derivate.get_sort_rows_by_group(view, sorted_rows);

  // TODO: remove below code
  if let Some(ref mut rows) = view.rows {
    *rows = sorted_rows;
  }

  Ok(())
}

fn get_filter_by_formula(
  expression: String,
  context: Rc<DatasheetPackContext>,
  view: &mut ViewSO,
) -> anyhow::Result<()> {
  let snapshot = &context.datasheet_pack.snapshot;
  let datasheet_id = &context.datasheet_pack.datasheet.id;

  // validate_expression()
  let field = Rc::new(IField::Formula(IBaseField {
    id: get_new_id(IDPrefix::Field, vec![]),
    name: "Virtual".to_string(),
    desc: None,
    required: None,
    property: FormulaFieldPropertySO {
      datasheet_id: datasheet_id.clone(),
      expression: expression.clone(),
      formatting: None,
    },
  }));

  // TODO: remove the field_map convert
  let field_map = context
    .datasheet_pack
    .snapshot
    .meta
    .field_map
    .iter()
    .map(|(k, v)| (k.clone(), Rc::new(IField::from_so(v.clone()))))
    .collect();

  let field_permission_map = HashMap::new();
  let expression = expression_transform(
    &expression,
    &field_map,
    &field_permission_map,
    ExpressionTransformTarget::Id,
  )
  .map_err(|e| anyhow::Error::msg(e.message))?;

  let parse_result = parse(
    expression.clone(),
    Rc::new(crate::formula::parser::parser::Context {
      field: field.clone(),
      field_map,
      state: context.clone(),
    }),
  );
  if let Err(err) = parse_result {
    return Err(anyhow::Error::msg(format!("api_param_formula_error={:#?}", err)));
  }

  if let Some(ref mut rows) = view.rows {
    rows.retain(|row| {
      let record = snapshot.record_map.get(&row.record_id);
      let ctx = FormulaEvaluateContext {
        state: context.clone(),
        field: field.clone(),
        record: Rc::new(record.unwrap().clone()),
      };

      let result = evaluate(expression.clone(), ctx);
      return if let Ok(value) = result { value.is_true() } else { false };
    });
  }

  Ok(())
}

fn get_sorted_rows(_datasheet_pack: &DatasheetPackSO, _rows: Vec<ViewRowSO>) -> Vec<ViewRowSO> {
  todo!()
}

fn get_grouped_rows(_datasheet_pack: &DatasheetPackSO, _rows: Vec<ViewRowSO>) -> Vec<ViewRowSO> {
  todo!()
}

pub fn filter_columns(_datasheet_pack: &DatasheetPackSO, _view: &mut ViewSO) {
  // TODO: filter view.columns
}

/**
 * This function retrieves a specific view from a datasheet pack based on the provided view ID.
 * If the view ID is provided, it searches for the view with the matching ID in the list of views.
 * If found, it returns the view. If not found, it returns an error.
 * If no view ID is provided, it returns the first view in the list.
 */
pub fn get_view(datasheet_pack: &DatasheetPackSO, view_id: Option<String>) -> anyhow::Result<ViewSO> {
  if let Some(view_id) = view_id {
    datasheet_pack
      .snapshot
      .meta
      .views
      .iter()
      .find_map(|view| {
        let property: ViewSO = view.clone(); //serde_json::from_value(view.clone()).unwrap();
        if property.id == Some(view_id.clone()) {
          Some(property)
        } else {
          None
        }
      })
      .ok_or_else(|| anyhow!("view not found"))
  } else {
    Ok(datasheet_pack.snapshot.meta.views[0].clone())
    // Ok(serde_json::from_value(datasheet_pack.snapshot.meta.views[0].clone()).unwrap())
  }
}
