use std::rc::Rc;

use crate::fields::field_factory::FieldFactory;
use crate::fields::LookUp;
use crate::formula::evaluate::evaluate;
use crate::formula::functions::basic::FormulaEvaluateContext;
use crate::formula::types::IField;
use crate::prelude::RecordSO;
use crate::so::{CellValue, DatasheetPackContext, DatasheetSnapshotSO, FieldKindSO};

pub(crate) fn get_cell_value(
  context: Rc<DatasheetPackContext>,
  snapshot: &DatasheetSnapshotSO,
  record_id: &str,
  field_id: &str,
) -> CellValue {
  // TODO: permission

  let cell_value = calc_cell_value(context, snapshot, field_id, record_id);
  return cell_value;
}

fn get_stringified_cell_value(
  context: Rc<DatasheetPackContext>,
  snapshot: &DatasheetSnapshotSO,
  record_id: &str,
  field_id: &str,
) -> Option<String> {
  let (_, op_string) = calc_cell_value_and_string(context, snapshot, field_id, record_id);
  op_string
}

fn calc_cell_value_and_string(
  context: Rc<DatasheetPackContext>,
  snapshot: &DatasheetSnapshotSO,
  field_id: &str,
  record_id: &str,
) -> (CellValue, Option<String>) {
  let cell_value = calc_cell_value(context.clone(), snapshot, field_id, record_id);
  let field_map = &snapshot.meta.field_map;
  let field = field_map.get(field_id).unwrap();

  if cell_value == CellValue::Null {
    return (cell_value, None);
  }

  if field.kind == FieldKindSO::Attachment {
    if let Some(arr) = cell_value.as_attachment_array() {
      let values = arr
        .into_iter()
        .map(|value| {
          return value.name.clone();
        })
        .collect::<Vec<_>>();
      return (cell_value, Some(values.join(",")));
    }
  }

  let instance = FieldFactory::create_field(field.clone(), context.clone());
  let cell_str = instance.cell_value_to_string(cell_value.clone(), None);

  return (cell_value, cell_str);
}

pub fn calc_cell_value(
  context: Rc<DatasheetPackContext>,
  snapshot: &DatasheetSnapshotSO,
  field_id: &str,
  record_id: &str,
) -> CellValue {
  let field_map = &snapshot.meta.field_map;
  let field = field_map.get(field_id).unwrap();

  // TODO: ignore_field_permission

  let instance = FieldFactory::create_field(field.clone(), context.clone());

  return if instance.is_computed() {
    get_compute_cell_value(context.clone(), snapshot, record_id, field_id)
  } else {
    get_entity_cell_value(snapshot, record_id, field_id)
  };
}

pub fn get_compute_cell_value(
  context: Rc<DatasheetPackContext>,
  snapshot: &DatasheetSnapshotSO,
  record_id: &str,
  field_id: &str,
) -> CellValue {
  let record_map = &snapshot.record_map;
  let field_map = &snapshot.meta.field_map;
  let option_field = field_map.get(field_id);
  let option_record = record_map.get(record_id);
  if option_field.is_none() || option_record.is_none() {
    return CellValue::Null;
  }
  let field = option_field.unwrap();
  let record = option_record.unwrap();

  let base_field = FieldFactory::create_field(field.clone(), context.clone());

  return match field.kind {
    FieldKindSO::Formula => {
      // TODO: remove the clone
      let field = Rc::new(IField::from_so(field.clone()));
      let record = Rc::new(record.clone());
      get_formula_cell_value(context.clone(), field, record)
    }
    FieldKindSO::LookUp => base_field.get_cell_value(record),
    FieldKindSO::CreatedBy
    | FieldKindSO::LastModifiedBy
    | FieldKindSO::CreatedTime
    | FieldKindSO::LastModifiedTime
    | FieldKindSO::AutoNumber => base_field.get_cell_value(record),
    _ => CellValue::Null,
  };
}

fn get_formula_cell_value(state: Rc<DatasheetPackContext>, field: Rc<IField>, record: Rc<RecordSO>) -> CellValue {
  if let IField::Formula(formula_field) = &(*field) {
    let ctx = FormulaEvaluateContext {
      field: field.clone(),
      record: record.clone(),
      state: state.clone(),
    };
    return evaluate(formula_field.property.expression.clone(), ctx).unwrap_or(CellValue::Null);
  }
  panic!("illegal field type");
}

pub fn get_entity_cell_value(snapshot: &DatasheetSnapshotSO, record_id: &str, field_id: &str) -> CellValue {
  let record_map = &snapshot.record_map;
  let field_map = &snapshot.meta.field_map;
  let field = field_map.get(field_id).unwrap();
  let record_data = record_map.get(record_id);
  return match record_data {
    Some(record_data) => record_data.get_field_value(field),
    None => {
      println!("field_id: {} not found", field_id);
      println!("record_id: {} not found", record_id);
      CellValue::Null
    }
  };
}

pub(crate) fn _get_look_up_tree_value(
  context: Rc<DatasheetPackContext>,
  snapshot: &DatasheetSnapshotSO,
  record_id: &str,
  field_id: &str,
) -> CellValue {
  let field_map = &snapshot.meta.field_map;
  let field = field_map.get(field_id).unwrap();

  if FieldFactory::create_field(field.clone(), context.clone()).is_computed() {
    if field.kind == FieldKindSO::LookUp {
      return LookUp::new(field.clone(), context.clone()).get_look_up_tree_value(record_id);
    }
    return get_compute_cell_value(context.clone(), snapshot, record_id, field_id);
  }

  get_entity_cell_value(snapshot, record_id, field_id)
}
