use std::cmp::Ordering;
use std::collections::HashMap;
use std::rc::Rc;

use crate::fields::LookUp;
use crate::formula::types::IField;
use crate::modules::database::store::selectors::resource::datasheet::cell_calc::get_cell_value;
use crate::prelude::view_operation::sort::ISortedField;
use crate::prelude::{DatasheetPackContext, DatasheetSnapshotSO, FieldPermissionMap, FieldSO, ViewRowSO, ViewSO};
use crate::so::FieldKindSO;

pub fn sort_rows_by_sort_info(
  state: Rc<DatasheetPackContext>,
  rows: &Vec<ViewRowSO>,
  sort_rules: &Vec<ISortedField>,
  snapshot: &DatasheetSnapshotSO,
) -> Vec<ViewRowSO> {
  let mut shallow_rows = rows.clone();
  shallow_rows.sort_by(|prev, current| {
    let ordering = sort_rules.iter().fold(0, |acc, rule| {
      let field = snapshot.meta.field_map.get(rule.field_id.as_str());
      let field = match field {
        None => return acc,
        Some(field) => field,
      };
      // TODO: remove the copy
      let field = Rc::new(IField::from_so(field.clone()));

      let field_method = IField::bind_context(field.clone(), state.clone());

      // same as filter, sort remove the check of column permission
      let cv1 = get_cell_value(state.clone(), snapshot, &prev.record_id, field.get_id());
      let cv2 = get_cell_value(state.clone(), snapshot, &current.record_id, field.get_id());
      let res = field_method.compare(&cv1, &cv2, Some(true));
      let sign = if rule.desc { -1 } else { 1 };

      res * sign
    });

    return match ordering {
      -1 => Ordering::Less,
      0 => Ordering::Equal,
      1 => Ordering::Greater,
      _ => panic!("illegal ordering"),
    };
  });

  shallow_rows
}

pub fn get_group_fields(
  view: &ViewSO,
  field_map: &HashMap<String, FieldSO>,
  _field_permission_map: Option<&FieldPermissionMap>,
) -> Vec<Rc<IField>> {
  let mut fields = vec![];
  let group_info = match &view.group_info {
    Some(group_info) => group_info,
    _ => {
      return fields;
    }
  };
  for gp in group_info {
    // TODO: permission

    if let Some(field) = field_map.get(gp.field_id.as_str()) {
      // TODO: remove the copy
      let field = Rc::new(IField::from_so(field.clone()));
      fields.push(field);
    }
  }

  fields
}

pub fn find_real_field(state: Rc<DatasheetPackContext>, props_field: FieldSO) -> Option<FieldSO> {
  if props_field.kind != FieldKindSO::LookUp {
    return Some(props_field);
  }
  let look_up_field = LookUp::new(props_field.clone(), state.clone());
  return look_up_field.get_look_up_entity_field();
}
