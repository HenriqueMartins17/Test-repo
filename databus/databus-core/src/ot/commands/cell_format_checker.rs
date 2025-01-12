use std::{collections::HashMap, rc::Rc};

use serde_json::{to_value, Value};

use json0::operation::{OperationKind, PathSegment};

use crate::modules::database::store::selectors::resource::datasheet::cell_calc::get_cell_value;
use crate::{
  fields::field_factory::FieldFactory,
  ot::types::ActionOTO,
  so::{DatasheetPackContext, FieldKindSO, FieldSO},
};

pub struct CellFormatChecker {
  context: Rc<DatasheetPackContext>,
  // store: Store<ReduxState, AnyAction>,
}

impl CellFormatChecker {
  pub fn new(
    // store: Store<ReduxState, AnyAction>
    context: Rc<DatasheetPackContext>,
  ) -> Self {
    // Self { store }
    Self { context }
  }

  pub fn check_value_valid(
    cell_value: &Value,
    field: &FieldSO,
    // state: &ReduxState
    context: Rc<DatasheetPackContext>,
  ) -> Value {
    if FieldFactory::create_field(field.clone(), context.clone()).validate(cell_value) {
      // Some(cell_value.clone())
      cell_value.clone()
    } else {
      // None
      Value::Null
    }
  }

  fn convert_value(
    &self,
    field_id: &str,
    record_id: &str,
    cell_value: Value,
    field_map_snapshot: &mut HashMap<String, FieldSO>,
    _datasheet_id: &str,
  ) -> Value {
    // let cv: CellValue = from_value(cell_value.clone()).unwrap();
    let cv = cell_value;
    let current_field = self
      .context
      .datasheet_pack
      .snapshot
      .meta
      .field_map
      .get(field_id)
      .unwrap()
      .clone();
    let previous_field = field_map_snapshot.get(field_id).unwrap().clone();
    if field_map_snapshot.get(&current_field.id).unwrap().kind == current_field.kind {
      return CellFormatChecker::check_value_valid(&cv, &current_field, self.context.clone());
    }

    let std_value =
      FieldFactory::create_field(previous_field.clone(), self.context.clone()).cell_value_to_std_value(cv.clone());
    let result =
      FieldFactory::create_field(current_field.clone(), self.context.clone()).std_value_to_cell_value(std_value);

    // Because the data has been corrected,
    // the old data structure will be updated at the same time to prevent the data from being correct
    // and the recorded FieldType is still wrong, resulting in an error in the middle layer
    field_map_snapshot.insert(current_field.id.clone(), current_field.clone());

    // if cv != CellValue::NoValue && result != CellValue::NoValue && current_field.kind == FieldKindSO::Link {
    if cv != Value::Null && result != Value::Null && current_field.kind == FieldKindSO::Link {
      // Fix the data exception caused by the lack of associated ops in this table
      let cell_value = get_cell_value(
        self.context.clone(),
        &self.context.datasheet_pack.snapshot,
        record_id,
        &current_field.id,
      );
      return to_value(cell_value).unwrap();
    }

    result
  }

  pub fn parse(
    &self,
    actions: Vec<ActionOTO>,
    datasheet_id: &str,
    field_map_snapshot: &Option<HashMap<String, FieldSO>>,
  ) -> Vec<ActionOTO> {
    if field_map_snapshot.is_none() {
      return actions;
    }
    let field_map_snapshot = field_map_snapshot.clone().unwrap();
    let field_ids: Vec<String> = field_map_snapshot.keys().cloned().collect();
    let mut actions = actions;
    for action in &mut actions {
      if !action.op.p.contains(&PathSegment::String("recordMap".to_string())) {
        continue;
      }
      let has_oi = match &action.op.kind {
        OperationKind::ObjectInsert { oi: _ } => true,
        OperationKind::ObjectReplace { od: _, oi: _ } => true,
        _ => false,
      };
      if !has_oi {
        continue;
      }
      let oi = match &mut action.op.kind {
        OperationKind::ObjectInsert { oi } => oi,
        OperationKind::ObjectReplace { od: _, oi } => oi,
        _ => continue,
      };
      let record_id = action.op.p[1].clone();
      let record_id = match record_id {
        PathSegment::String(s) => s,
        _ => continue,
      };
      if action.op.p.len() == 4 {
        let as_string = format!("{:?}", action.op.p[3]);
        if !field_ids.contains(&as_string) {
          continue;
        }
        let field_id = action.op.p[3].clone();
        let cell_value = oi.clone();
        let field_id = match field_id {
          PathSegment::String(s) => s,
          _ => continue,
        };
        // println!("cell_value: {:?}", cell_value);
        let cv = self.convert_value(
          &field_id,
          &record_id,
          cell_value,
          &mut field_map_snapshot.clone(),
          datasheet_id,
        );
        let value = to_value(cv).unwrap();
        // println!("cell_value2: {:?}", value);
        *oi = value;
      }
      if action.op.p.len() == 2 {
        let oi = oi.as_object_mut().unwrap();
        if oi.contains_key("data") {
          for field_id in &field_ids {
            let pre_value = oi.get_mut("data").unwrap().as_object_mut().unwrap();
            // let cell_value = oi.get("data").unwrap().clone().get(field_id).unwrap().clone();
            let cell_value = pre_value.get(field_id);
            let cell_value = match cell_value {
              Some(v) => v.clone(),
              None => Value::Null,
            };
            // println!("cell_value3: {:?}", cell_value);
            let cv = self.convert_value(
              &field_id,
              &record_id,
              cell_value,
              &mut field_map_snapshot.clone(),
              datasheet_id,
            );
            // println!("cell_value4: {:?}", cv);
            if pre_value.contains_key(field_id) {
              pre_value[field_id] = cv;
            } else {
              pre_value.insert(field_id.to_string(), cv);
            }
          }
        }
      }
    }
    actions
  }
}
