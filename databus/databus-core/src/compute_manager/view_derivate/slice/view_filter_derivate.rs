use std::cmp::Ordering;
use std::collections::HashMap;
use std::rc::Rc;

use crate::fields::base_field::IBaseField;
use crate::fields::field_factory::FieldFactory;
use crate::fields::property::field_types::BasicValueType;
use crate::fields::property::RollUpFuncType;
use crate::formula::types::IField;
use crate::modules::database::store::selectors::resource::datasheet::calc::get_filter_info_except_invalid;
use crate::modules::database::store::selectors::resource::datasheet::cell_calc::get_cell_value;
use crate::modules::database::store::selectors::resource::datasheet::rows_calc::{
  find_real_field, get_group_fields, sort_rows_by_sort_info,
};
use crate::prelude::ViewSO;
use crate::so::view_operation::filter::{FOperator, FilterConjunction, IFilterCondition, IFilterInfo};
use crate::so::{CellValue, DatasheetPackContext, FieldSO, RecordSO, ViewRowSO};
use crate::DatasheetActions;

pub struct ViewFilterDerivate {
  state: Rc<DatasheetPackContext>,
  datasheet_id: String,
}

impl ViewFilterDerivate {
  pub fn new(state: Rc<DatasheetPackContext>, datasheet_id: String) -> Self {
    ViewFilterDerivate { state, datasheet_id }
  }

  pub fn get_sort_rows_by_group(&self, view: &ViewSO, rows: Vec<ViewRowSO>) -> Vec<ViewRowSO> {
    let snapshot = &self.state.datasheet_pack.snapshot;
    let group_info = match &view.group_info {
      Some(group_info) => group_info,
      _ => {
        return rows;
      }
    };
    let field_map = &snapshot.meta.field_map;
    // TODO: permission
    let groups = get_group_fields(view, field_map, None);
    if groups.is_empty() {
      return rows;
    }

    let desc_orders = group_info.iter().fold(vec![], |mut acc, gp| {
      if field_map.contains_key(gp.field_id.as_str()) {
        acc.push(gp.desc.clone());
      }

      acc
    });

    let mut field_instance_map = HashMap::<String, Rc<Box<dyn IBaseField>>>::new();
    let mut cache_sort_res = HashMap::<String, i32>::new();

    // rows sorted by group
    let mut rows = rows;
    rows.sort_by(|row1, row2| {
      let ordering = groups.iter().enumerate().fold(0, |prev, (index, field)| {
        if prev != 0 {
          return prev;
        }
        let field_instance = match field_instance_map.get(field.get_id()) {
          Some(field_instance) => field_instance.clone(),
          None => {
            let field_instance = IField::bind_context(field.clone(), self.state.clone());
            let field_instance = Rc::new(field_instance);
            field_instance_map.insert(field.get_id().to_string(), field_instance.clone());

            field_instance
          }
        };

        let cv1 = get_cell_value(self.state.clone(), snapshot, &row1.record_id, field.get_id());
        let cv2 = get_cell_value(self.state.clone(), snapshot, &row2.record_id, field.get_id());

        let res = if !cv1.is_object() && !cv2.is_object() {
          let key = format!("{}{}", cv1.to_string(), cv2.to_string());
          let res = cache_sort_res
            .entry(key)
            .or_insert_with(|| field_instance.compare(&cv1, &cv2, None));
          *res
        } else {
          field_instance.compare(&cv1, &cv2, None)
        };

        let sign = if desc_orders[index] { -1 } else { 1 };

        res * sign
      });

      return match ordering {
        -1 => Ordering::Less,
        0 => Ordering::Equal,
        1 => Ordering::Greater,
        _ => panic!("illegal ordering"),
      };
    });

    rows
  }

  pub fn get_sort_rows(&self, view: &ViewSO, rows: Vec<ViewRowSO>) -> Vec<ViewRowSO> {
    let sort_info = match &view.sort_info {
      Some(sort_info) => sort_info,
      None => return rows,
    };
    if !sort_info.keep_sort {
      return rows;
    }

    let snapshot = &self.state.datasheet_pack.snapshot;

    sort_rows_by_sort_info(self.state.clone(), &rows, &sort_info.rules, snapshot)
  }

  pub fn get_filtered_rows(&self, view: &ViewSO) -> Vec<ViewRowSO> {
    let snapshot = &self.state.datasheet_pack.snapshot;

    let rows = match &view.rows {
      Some(rows) => rows,
      _ => return vec![],
    };
    if rows.is_empty() {
      return vec![];
    }

    let record_map = &snapshot.record_map;

    // TODO: empty data filter, subsequent data repair should be able to delete
    let mut rows = rows
      .iter()
      .filter(|row| record_map.contains_key(&row.record_id))
      .cloned()
      .collect::<Vec<ViewRowSO>>();

    let filter_info = get_filter_info_except_invalid(&self.state, &self.datasheet_id, &view.filter_info);
    self.get_filter_rows_base(filter_info, &mut rows, record_map);

    // TODO: mirror filter

    rows
  }

  fn get_filter_rows_base(
    &self,
    filter_info: Option<IFilterInfo>,
    rows: &mut Vec<ViewRowSO>,
    record_map: &HashMap<String, RecordSO>,
  ) {
    let mut filter_info = match filter_info {
      Some(filter_info) => filter_info,
      None => return,
    };

    let is_repeat_condition = filter_info
      .conditions
      .iter()
      .find(|condition| condition.operator == FOperator::IsRepeat);
    let is_and = filter_info.conjunction == FilterConjunction::And;
    let mut repeat_rows: Option<Vec<String>> = None;

    if let Some(is_repeat_condition) = is_repeat_condition {
      if is_and {
        let result = self.find_repeat_row(&rows, self.state.clone(), &is_repeat_condition.field_id);
        let record_id_map = result
          .iter()
          .enumerate()
          .map(|(k, v)| (v.clone(), k))
          .collect::<HashMap<String, usize>>();
        rows.retain(|row| record_id_map.contains_key(&row.record_id));

        filter_info = IFilterInfo {
          conjunction: filter_info.conjunction,
          conditions: filter_info
            .conditions
            .iter()
            .filter(|condition| condition.operator != FOperator::IsRepeat)
            .cloned()
            .collect(),
        };
      } else {
        repeat_rows = Some(self.find_repeat_row(&rows, self.state.clone(), &is_repeat_condition.field_id));
      }
    }

    rows.retain(|row| self.check_conditions(record_map.get(&row.record_id).unwrap(), &filter_info, &repeat_rows));
  }

  /// Filter out duplicate cellValue, return the de-duplicated rows,
  /// only allow adding one with duplicate conditions
  ///
  /// TODO: Repeat is not quite in line with the logic of "filtering",
  /// it should be a separate function, and the following calculation is not rigorous, it needs to be rewritten.
  fn find_repeat_row(
    &self,
    rows: &Vec<ViewRowSO>,
    context: Rc<DatasheetPackContext>,
    field_id: &String,
  ) -> Vec<String> {
    let mut map: HashMap<String, Vec<String>> = HashMap::new();
    let snaoshot = &context.datasheet_pack.snapshot;
    let field = snaoshot.meta.field_map.get(field_id).unwrap();
    // TODO: remove this clone
    let i_field = Rc::new(IField::from_so(field.clone()));
    let field_method = IField::bind_context(i_field.clone(), context.clone());

    let values = DatasheetActions::get_cell_values_by_field_id(context.clone(), snaoshot, i_field.get_id(), true);

    if !values.is_empty() {
      for row in rows.iter() {
        let cell_value = get_cell_value(context.clone(), snaoshot, &row.record_id, i_field.get_id());
        let lookup_field = find_real_field(context.clone(), field.clone()).map(|f| IField::from_so(f));

        let roll_up_type = if let IField::LookUp(i_field) = i_field.as_ref() {
          match &i_field.property.roll_up_type {
            Some(t) => t.clone(),
            None => RollUpFuncType::VALUES,
          }
        } else {
          RollUpFuncType::VALUES
        };

        let cell_value = if let CellValue::Array(mut array) = cell_value {
          let is_need_sort = match i_field.as_ref() {
            IField::LookUp(_) => roll_up_type == RollUpFuncType::VALUES,
            IField::MultiSelect(_) => true,
            IField::Member(_) => true,
            IField::Link(_) => true,
            _ => false,
          };

          if is_need_sort {
            array.sort_by(|a, b| {
              let a = a.get_text().unwrap_or(a.to_string());
              let b = b.get_text().unwrap_or(b.to_string());
              a.cmp(&b)
            });
          }

          CellValue::Array(array)
        } else {
          cell_value
        };

        // Do you need to call cellValueToString to convert to string form.
        let is_need_to_string = match i_field.as_ref() {
          IField::Currency(_) => true,
          IField::SingleText(_) => true,
          IField::Text(_) => true,
          IField::UrlField(_) => true,
          IField::Phone(_) => true,
          IField::Email(_) => true,
          IField::DateTime(_) => true,
          IField::CreatedTime(_) => true,
          IField::LastModifiedTime(_) => true,
          IField::Number(_) => true,
          IField::Percent(_) => true,
          IField::LookUp(_) => {
            if let Some(lookup_field) = lookup_field {
              !matches!(
                lookup_field,
                IField::SingleSelect(_) | IField::MultiSelect(_) | IField::Link(_)
              )
            } else {
              false
            }
          }
          _ => false,
        };
        let cell_value = if is_need_to_string {
          match field_method.cell_value_to_string(cell_value, None) {
            Some(cell_value) => CellValue::String(cell_value),
            None => CellValue::Null,
          }
        } else {
          cell_value
        };

        let cell_value = if let CellValue::Null = cell_value {
          "".to_string()
        } else {
          cell_value.to_string().trim().to_string()
        };

        map
          .entry(cell_value)
          .or_insert_with(Vec::new)
          .push(row.record_id.clone());
      }
    }
    let result: Vec<String> = map
      .iter()
      .filter(|(_, value)| value.len() > 1)
      .flat_map(|(_, value)| value.clone())
      .collect();

    result
  }

  fn do_filter(&self, condition: &IFilterCondition, field: &FieldSO, cell_value: CellValue) -> bool {
    let field_method = FieldFactory::create_field(field.clone(), self.state.clone());
    if condition.operator == FOperator::IsEmpty || condition.operator == FOperator::IsNotEmpty {
      field_method.is_empty_or_not(&condition.operator, &cell_value)
    } else if condition.value.is_null()
      && field_method.basic_value_type() != BasicValueType::Number
      && condition.operator != FOperator::IsRepeat
    {
      true
    } else {
      field_method.is_meet_filter(&condition.operator, &cell_value, &condition.get_value())
    }
  }

  fn do_filter_operations(
    &self,
    condition: &IFilterCondition,
    record: &RecordSO,
    repeat_rows: &Option<Vec<String>>,
  ) -> bool {
    // or condition to have repeatRows
    // or condition on the presence or absence of repeatRows
    if matches!(repeat_rows, Some(repeat_rows) if repeat_rows.contains(&record.id)) {
      return true;
    }
    // If the condition is isRepeat, and no duplicate records are hit, return false early
    if condition.operator == FOperator::IsRepeat {
      return false;
    }
    let field_id = &condition.field_id;
    let field_map = self.state.get_field_map(self.datasheet_id.as_str());
    if field_map.is_none() {
      return false;
    }

    let field = field_map.unwrap().get(field_id).unwrap();

    let cell_value = get_cell_value(
      self.state.clone(),
      self.state.get_snapshot(&self.datasheet_id).unwrap(),
      record.id.as_str(),
      field.id.as_str(),
    );
    return self.do_filter(condition, field, cell_value);
  }

  fn check_conditions(&self, record: &RecordSO, filter_info: &IFilterInfo, repeat_rows: &Option<Vec<String>>) -> bool {
    let conditions = &filter_info.conditions;

    if filter_info.conjunction == FilterConjunction::And {
      return conditions
        .iter()
        .all(|condition| self.do_filter_operations(condition, record, &None));
    }

    if filter_info.conjunction == FilterConjunction::Or {
      return conditions
        .iter()
        .any(|condition| self.do_filter_operations(condition, record, repeat_rows));
    }

    // never happen
    return false;
  }

  pub fn get_filtered_records(
    &self,
    link_field_record_ids: &Vec<String>,
    filter_info: &Option<IFilterInfo>,
  ) -> Vec<String> {
    let snapshot = self.state.get_snapshot(self.datasheet_id.as_str());

    if link_field_record_ids.is_empty() || snapshot.is_none() {
      return vec![];
    }

    let record_map = &snapshot.unwrap().record_map;

    let filter_info = get_filter_info_except_invalid(&self.state, &self.datasheet_id, filter_info);

    if filter_info.is_none() {
      return link_field_record_ids.clone();
    }

    let result = link_field_record_ids
      .iter()
      .filter(|link_field_record_id| {
        let record = record_map.get(*link_field_record_id);
        self.check_conditions(record.unwrap(), &filter_info.as_ref().unwrap(), &None)
      })
      .cloned()
      .collect();

    result
  }
}
