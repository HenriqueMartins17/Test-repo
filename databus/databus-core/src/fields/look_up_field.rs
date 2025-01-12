use serde_json::{Value, from_value, to_value};

use crate::compute_manager::view_derivate::slice::view_filter_derivate::ViewFilterDerivate;
use crate::fields::date_time_base_field::DateTimeBaseField;
use crate::fields::Checkbox;
use crate::so::view_operation::filter::{IFilterCondition, IFilterInfo};
use crate::utils::uuid::{get_new_ids, IDPrefix};
use std::collections::{HashMap, HashSet};
use std::rc::Rc;

use tracing::{debug, info};

use crate::fields::ext::types;
use crate::fields::field_factory::FieldFactory;
use crate::fields::number_base_field::NumberBaseField;
use crate::fields::property::field_types::{BasicValueType, LookUpLimitType};
use crate::fields::property::{LookUpSortInfo, LookupFieldPropertySO, RollUpFuncType};
use crate::fields::text_base_field::TextBaseField;
use crate::formula::evaluate::{evaluate, parse};
use crate::formula::functions::basic::FormulaEvaluateContext;
use crate::formula::functions::FunctionProvider;
use crate::formula::parser::Context;
use crate::formula::types::IField;
use crate::modules::database::store::selectors::resource::datasheet::cell_calc;
use crate::modules::database::store::selectors::resource::datasheet::rows_calc::sort_rows_by_sort_info;
use crate::prelude::view_operation::filter::{ConditionValue, FOperator};
use crate::prelude::CellValue;
use crate::so::api_value::ApiValue;
use crate::so::{DatasheetPackContext, FieldKindSO, FieldSO, LookUpTreeValue, RecordSO, ViewRowSO};
use crate::utils::utils::handle_null_array;

use super::base_field::IBaseField;
use super::{computed_formatting_to_format, filter_operator_accepts_value};
use super::property::FieldPropertySO;

pub struct LookUp {
  pub field_conf: FieldSO,
  pub context: Rc<DatasheetPackContext>,
}

impl IBaseField for LookUp {
  fn basic_value_type(&self) -> BasicValueType {
    let expression = self.get_expression();
    if let Some(expression) = expression {
      let field_map = self
        .context
        .get_field_map(
          self
            .field_conf
            .property
            .as_ref()
            .unwrap()
            .datasheet_id
            .as_ref()
            .unwrap(),
        )
        .unwrap();
      let f_exp = parse(
        expression,
        Rc::new(Context {
          field: Rc::new(IField::from_so(self.field_conf.clone())),
          field_map: field_map
            .into_iter()
            .map(|(key, value)| (key.clone(), Rc::new(IField::from_so(value.clone()))))
            .collect::<HashMap<_, _>>(),
          state: self.context.clone(),
        }),
      );
      if !f_exp.is_err() {
        return f_exp.unwrap().ast.get_value_type().clone();
      }
    }
    if self.roll_up_type().is_lookup_func() {
      return BasicValueType::String;
    }
    BasicValueType::Array
  }

  /// When the underlying type is Array, get the underlying type of the elements in the Array
  fn inner_basic_value_type(&self) -> BasicValueType {
    let entity_field = self.get_look_up_entity_field();
    if entity_field.is_none() {
      return BasicValueType::String;
    }
    let value_type = FieldFactory::create_field(entity_field.unwrap(), self.context.clone()).basic_value_type();

    // The array in the array is still an array,
    // indicating that it is a multi-select field, and it can be specified as a string directly.
    return if value_type == BasicValueType::Array {
      BasicValueType::String
    } else {
      value_type
    };
  }

  fn is_computed(&self) -> bool {
    true
  }

  fn get_cell_value(&self, record: &RecordSO) -> CellValue {
    let expression = self.get_expression();
    let datasheet_id = self
      .field_conf
      .property
      .as_ref()
      .unwrap()
      .to_lookup_field_property()
      .datasheet_id;
    debug!("expression: {:?}", &expression);

    if let Some(expression) = expression {
      let snapshot = self.context.get_snapshot(&datasheet_id).unwrap();
      let record = snapshot.record_map[&record.id].clone();

      let result = evaluate(
        expression,
        FormulaEvaluateContext {
          state: self.context.clone(),
          field: Rc::new(IField::from_so(self.field_conf.clone())),
          record: Rc::new(record.clone()),
        },
      );
      return match result {
        Ok(value) => CellValue::from(value),
        Err(_) => CellValue::Null,
      };
    }

    let field = self.get_look_up_entity_field();
    if field.is_none() {
      return CellValue::Null;
    }
    let field = field.unwrap();
    let value = self.get_flat_cell_value(record.id.as_str(), true);

    if value.is_array() {
      let flat_cell_value = value.as_array().unwrap().clone();
      if flat_cell_value.len() == 0 {
        return value;
      }
      let _flat_cell_value = if types::is_text_base_type(field.kind) {
        value.clone()
      } else {
        value.clone().flat(1)
      };
      return match self.roll_up_type() {
        RollUpFuncType::VALUES | RollUpFuncType::ARRAYJOIN | RollUpFuncType::CONCATENATE => _flat_cell_value,
        RollUpFuncType::ARRAYUNIQUE => _flat_cell_value,
        RollUpFuncType::ARRAYCOMPACT => {
          CellValue::Array(flat_cell_value.into_iter().filter(|x| *x != CellValue::Null).collect())
        }
        _ => value,
      };
    }
    value
  }

  fn cell_value_to_api_standard_value(&self, cell_value: CellValue) -> ApiValue {
    let cell_value = handle_null_array(cell_value);
    let option_entity_field = self.get_look_up_entity_field();
    if cell_value == CellValue::Null || option_entity_field.is_none() {
      return ApiValue::Null;
    }
    let entity_field = option_entity_field.unwrap();
    if let CellValue::Array(cell_value_array) = &cell_value {
      let base_field = FieldFactory::create_field(entity_field.clone(), self.context.clone());
      if base_field.basic_value_type() == BasicValueType::Array {
        return base_field.cell_value_to_api_standard_value(cell_value);
      }
      let mut result: Vec<ApiValue> = vec![];
      cell_value_array.iter().for_each(|it| {
        let t_api_value = base_field.cell_value_to_api_standard_value(it.clone());
        if t_api_value != ApiValue::Null {
          result.push(t_api_value);
        }
      });
      if result.len() > 0 {
        return ApiValue::from(result);
      }
    }
    ApiValue::Null
  }

  fn is_meet_filter(&self, operator: &FOperator, cell_value: &CellValue, condition_value: &ConditionValue) -> bool {
    let mut cell_value = handle_null_array(cell_value.clone());
    let expr = self.get_expression();
    if let Some(expr) = expr {
      let basic_value_type = self.basic_value_type();
      info!(
        "basic_value_type: {:?} , expr: {:?}, cell_value: {:?}",
        basic_value_type, expr, cell_value
      );
      return match basic_value_type {
        BasicValueType::Number => NumberBaseField::_is_meet_filter(operator, &cell_value, condition_value),
        BasicValueType::Boolean => Checkbox::_is_meet_filter(operator, &cell_value, condition_value),
        BasicValueType::DateTime => DateTimeBaseField::_is_meet_filter(operator, &cell_value, condition_value, None),
        BasicValueType::String => TextBaseField::_is_meet_filter(
          operator,
          &self.cell_value_to_string(cell_value, None),
          condition_value,
          None,
        ),
        BasicValueType::Array => match operator {
          FOperator::DoesNotContain | FOperator::IsNot | FOperator::IsEmpty => cell_value
            .as_cell_value_array()
            .unwrap()
            .iter()
            .all(|cv| match self.inner_basic_value_type() {
              BasicValueType::Number => {
                return NumberBaseField::_is_meet_filter(operator, cv, condition_value);
              }
              BasicValueType::Boolean => {
                return Checkbox::_is_meet_filter(operator, cv, condition_value);
              }
              BasicValueType::DateTime => {
                return DateTimeBaseField::_is_meet_filter(operator, cv, condition_value, None);
              }
              BasicValueType::String => {
                return TextBaseField::_is_meet_filter(
                  operator,
                  &self.cell_value_to_string(cv.clone(), None),
                  condition_value,
                  None,
                );
              }
              _ => false,
            }),
          _ => cell_value
            .as_cell_value_array()
            .unwrap()
            .iter()
            .any(|cv| match self.inner_basic_value_type() {
              BasicValueType::Number => {
                return NumberBaseField::_is_meet_filter(operator, cv, condition_value);
              }
              BasicValueType::Boolean => {
                return Checkbox::_is_meet_filter(operator, cv, condition_value);
              }
              BasicValueType::DateTime => {
                return DateTimeBaseField::_is_meet_filter(operator, cv, condition_value, None);
              }
              BasicValueType::String => {
                return TextBaseField::_is_meet_filter(
                  operator,
                  &self.cell_value_to_string(cv.clone(), None),
                  condition_value,
                  None,
                );
              }
              _ => false,
            }),
        },
      };
    }
    let entity_field = self.get_look_up_entity_field();
    if entity_field.is_none() {
      return false;
    }
    let entity_field = entity_field.unwrap();
    let judge = |cv: &CellValue| {
      return FieldFactory::create_field(entity_field.clone(), self.context.clone()).is_meet_filter(
        operator,
        cv,
        condition_value,
      );
    };
    // The cv of the lookup is already the value after flat, and the original field value is an array, using common comparison logic.
    if FieldFactory::create_field(entity_field.clone(), self.context.clone()).basic_value_type()
      == BasicValueType::Array
      && operator != &FOperator::IsRepeat
    {
      // Due to the existence of the "me" filter item, cellValue and conditionValue are not congruent,
      // For Member type directly use isMeetFilter for calculation, it will convert the 'Self' tag into the corresponding UnitId for comparison
      if entity_field.kind == FieldKindSO::Member {
        return FieldFactory::create_field(entity_field.clone(), self.context.clone()).is_meet_filter(
          operator,
          &cell_value,
          condition_value,
        );
      }
      return match operator {
        FOperator::Is => FieldFactory::create_field(entity_field, self.context.clone())
          .eq(&cell_value, &condition_value.clone().into()),
        FOperator::IsNot => !FieldFactory::create_field(entity_field, self.context.clone())
          .eq(&cell_value, &condition_value.clone().into()),
        _ => FieldFactory::create_field(entity_field, self.context.clone()).is_meet_filter(
          operator,
          &cell_value,
          condition_value,
        ),
      };
    }
    return match operator {
      // The `filter` operation of negative semantics requires that each item in the array satisfies
      FOperator::DoesNotContain | FOperator::IsNot | FOperator::IsEmpty => {
        if cell_value.is_array() {
          cell_value.as_array().unwrap().iter().all(|cv| judge(cv))
        } else {
          judge(&cell_value)
        }
      }
      _ => {
        if cell_value.is_array() {
          cell_value.as_array().unwrap().iter().any(|cv| judge(cv))
        } else {
          judge(&cell_value)
        }
      }
    };
  }

  fn is_empty_or_not(&self, operator: &FOperator, cell_value: &CellValue) -> bool {
    let value = handle_null_array(cell_value.clone());
    match operator {
      FOperator::IsEmpty => {
        return value == CellValue::Null;
      }
      FOperator::IsNotEmpty => {
        return value != CellValue::Null;
      }
      _ => panic!("compare operator type error"),
    }
  }
}

impl LookUp {
  pub fn new(field_conf: FieldSO, context: Rc<DatasheetPackContext>) -> Self {
    return Self { field_conf, context };
  }

  fn roll_up_type(&self) -> RollUpFuncType {
    self
      .field_conf
      .property
      .as_ref()
      .unwrap()
      .to_lookup_field_property()
      .roll_up_type
      .unwrap_or(RollUpFuncType::VALUES)
  }

  pub fn get_look_up_entity_field(&self) -> Option<FieldSO> {
    return self.get_look_up_entity_field_info(&mut HashSet::new());
  }

  pub fn get_look_up_entity_field_info(&self, visited_fields: &mut HashSet<String>) -> Option<FieldSO> {
    let next_target_info = self.get_look_up_target_field_and_datasheet();
    if next_target_info.is_none() {
      return None;
    }

    let (field, datasheet_id) = next_target_info.unwrap();

    if visited_fields.contains(format!("{}-{}", datasheet_id, field.id).as_str()) {
      return None;
    }

    if field.kind == FieldKindSO::LookUp {
      visited_fields.insert(format!("{}-{}", datasheet_id, field.id));
      let look_up_field: LookUp = LookUp::new(field.clone(), self.context.clone());
      return look_up_field.get_look_up_entity_field_info(visited_fields);
      // return if let Some(look_up_field) = base_field.downcast_ref::<LookUp>() {
      //   l
      // } else {
      //   println!("look_up error");
      //   None
      // };
    }
    return Some(field);
  }

  fn get_link_fields(&self) -> Vec<FieldSO> {
    let snapshot = self.context.get_snapshot(
      self
        .field_conf
        .property
        .as_ref()
        .unwrap()
        .to_lookup_field_property()
        .datasheet_id
        .as_str(),
    );
    if let Some(snapshot) = snapshot {
      return snapshot
        .meta
        .field_map
        .values()
        .filter(|field| field.kind == FieldKindSO::Link || field.kind == FieldKindSO::OneWayLink)
        .cloned()
        .collect::<Vec<FieldSO>>();
    }
    vec![]
  }

  fn get_related_link_field(&self) -> Option<FieldSO> {
    let link_fields = self.get_link_fields();
    return link_fields
      .iter()
      .find(|field| {
        field.id
          == self
            .field_conf
            .property
            .as_ref()
            .unwrap()
            .related_link_field_id
            .as_ref()
            .unwrap()
            .clone()
      })
      .cloned();
  }

  fn get_look_up_target_field(&self) -> Option<FieldSO> {
    let option = self.get_look_up_target_field_and_datasheet();
    option.map(|it| it.0)
  }

  fn get_look_up_target_field_and_datasheet(&self) -> Option<(FieldSO, String)> {
    let related_link_field = self.get_related_link_field();
    if related_link_field.is_none() {
      return None;
    }
    let related_link_field = related_link_field.unwrap();
    let foreign_datasheet_id = related_link_field
      .property
      .as_ref()
      .unwrap()
      .to_link_field_property()
      .foreign_datasheet_id;
    let look_up_target_field_id = self
      .field_conf
      .property
      .as_ref()
      .unwrap()
      .to_lookup_field_property()
      .look_up_target_field_id;
    let foreign_snapshot = self.context.get_snapshot(&foreign_datasheet_id);
    if foreign_snapshot.is_none() {
      return None;
    }
    let foreign_snapshot = foreign_snapshot.unwrap();
    let field = foreign_snapshot.meta.field_map.get(&look_up_target_field_id).unwrap();
    Some((field.clone(), foreign_datasheet_id.clone()))
  }

  fn get_flat_cell_value(&self, record_id: &str, with_empty: bool) -> CellValue {
    // Starting from the current record, recursively search the look_up tree.
    let record_cell_values = self.get_look_up_tree_value(record_id);
    if record_cell_values == CellValue::Null {
      return CellValue::Null;
    }
    let field = LookUp::new(self.field_conf.clone(), self.context.clone()).get_look_up_entity_field();
    if field.is_none() {
      return CellValue::Null;
    }
    let mut flat_cell_values: Vec<CellValue> = Vec::new();
    self.get_real_cell_value(&record_cell_values, &mut flat_cell_values);
    if with_empty {
      CellValue::Array(flat_cell_values)
    } else {
      CellValue::Array(
        flat_cell_values
          .into_iter()
          .filter(|item| item != &CellValue::Null)
          .collect(),
      )
    }
  }

  fn get_real_cell_value(&self, values: &CellValue, flat_cell_values: &mut Vec<CellValue>) {
    if let CellValue::Array(values) = values {
      for value in values {
        match value {
          CellValue::LookUpTree(value) => {
            if value.field.kind == FieldKindSO::LookUp {
              self.get_real_cell_value(&value.cell_value, flat_cell_values);
            } else {
              flat_cell_values.push(value.cell_value.clone());
            }
          }
          _ => panic!("illegal inner value type"),
        }
      }
    }
  }

  pub(crate) fn get_look_up_tree_value(&self, record_id: &str) -> CellValue {
    // The column corresponding to the filter condition is deleted and the result is not displayed
    // TODO check_filter_info
    // let is_filter_error = self.check_filter_info().error;
    // if is_filter_error {
    //   return vec![];
    // }
    let related_link_field = self.get_related_link_field();
    if related_link_field.is_none() {
      // console.log('Cannot find foreign key field', related_link_field);
      return CellValue::Null;
    }
    let related_link_field = related_link_field.unwrap();
    let LookupFieldPropertySO {
      look_up_target_field_id,
      datasheet_id,
      filter_info,
      open_filter,
      sort_info,
      look_up_limit,
      ..
    } = self.field_conf.property.as_ref().unwrap().to_lookup_field_property();
    let this_snapshot = self.context.get_snapshot(datasheet_id.as_str()).unwrap();
    // IDs of the associated table records
    let cell_value = cell_calc::get_cell_value(
      self.context.clone(),
      this_snapshot,
      record_id,
      related_link_field.id.as_str(),
    );
    if cell_value == CellValue::Null {
      return CellValue::Null;
    }

    let mut record_ids: Vec<String>;
    if let CellValue::Array(value) = cell_value {
      if value.is_empty() {
        return CellValue::Null;
      }
      record_ids = value.into_iter().map(|it| it.to_string()).collect();
    } else {
      return CellValue::Null;
    }

    let foreign_datasheet_id = related_link_field
      .property
      .as_ref()
      .unwrap()
      .foreign_datasheet_id
      .as_ref()
      .unwrap();
    let foreign_snapshot = self
      .context
      .get_snapshot(foreign_datasheet_id)
      .unwrap_or_else(|| panic!("Cannot find foreign datasheet: {}", foreign_datasheet_id));
    let look_up_target_field = self.get_look_up_target_field().unwrap();

    info!("open_filter {:?}", open_filter);
    if open_filter.unwrap_or(false) {
      // look_up filter
      record_ids = ViewFilterDerivate::new(self.context.clone(), foreign_datasheet_id.clone())
        .get_filtered_records(&record_ids, &filter_info);
      // look_up sort
      let sort_rows = self.get_sort_look_up(sort_info, foreign_datasheet_id, &record_ids);

      record_ids = sort_rows
        .into_iter()
        .filter(|row| record_ids.contains(&row.record_id))
        .map(|row| row.record_id)
        .collect();
    }

    if look_up_limit.is_some() {
      if look_up_limit.unwrap() == LookUpLimitType::FIRST && record_ids.len() > 1 {
        record_ids = record_ids.into_iter().take(1).collect();
      }
    }

    if !record_ids.is_empty() {
      CellValue::Array(
        record_ids
          .into_iter()
          .map(|record_id| {
            let cell_value = cell_calc::_get_look_up_tree_value(
              self.context.clone(),
              foreign_snapshot,
              record_id.as_str(),
              look_up_target_field_id.as_str(),
            );
            CellValue::LookUpTree(Box::new(LookUpTreeValue {
              field: look_up_target_field.clone(),
              record_id,
              cell_value,
              datasheet_id: foreign_datasheet_id.clone(),
            }))
          })
          .collect(),
      )
    } else {
      CellValue::Null
    }
  }

  fn get_expression(&self) -> Option<String> {
    let roll_func_type = self.roll_up_type();
    debug!("roll_func_type {:?}", roll_func_type);

    if !roll_func_type.is_origin_values_func() && !roll_func_type.is_lookup_func() {
      return FunctionProvider::new()
        .get_function(roll_func_type.to_string().as_str())
        .map(|func| format!("{}({})", func.name, crate::formula::consts::ROLLUP_KEY_WORDS));
    }
    None
  }
  
  pub fn validate_add_open_field_property(value: Value) -> anyhow::Result<()>{
    let property = from_value::<LookupFieldPropertySO>(value.clone());
    match property{
      Ok(_property) => {
        return Ok(());
      },
      Err(e) => {
        return Err(anyhow::Error::msg(format!("api_param_validate_error={}", e)));
      }
    }
  }

  pub fn update_open_field_property_transform_property(open_field_property: FieldPropertySO, default_property: FieldPropertySO) -> FieldPropertySO{
    let formatting = computed_formatting_to_format(open_field_property.formatting);
    let mut filter_info = None;
    if let Some(api_filter_info) = &open_field_property.filter_info {
      let condition_ids = get_new_ids(IDPrefix::Condition, api_filter_info.conditions.len(), Vec::new());
      let conditions: Vec<IFilterCondition> = api_filter_info.conditions.iter().enumerate().map(|(i, cond)| {
          let field_type = cond.field_type.clone();
          let value = if filter_operator_accepts_value(&cond.operator) {
              if cond.field_type == FieldKindSO::Checkbox || cond.value.is_array() {
                  cond.value.clone()
              } else {
                  to_value(vec![cond.value.clone()]).unwrap()
              }
          } else {
              Value::Null
          };
          IFilterCondition {
              field_type,
              operator: cond.operator.clone(),
              condition_id: condition_ids[i].clone(),
              value,
              ..cond.clone()
          }
      }).collect();
      filter_info = Some(IFilterInfo {
          conjunction: api_filter_info.conjunction.clone(),
          conditions,
      });
    }
    FieldPropertySO {
        datasheet_id: default_property.datasheet_id.clone(),
        related_link_field_id: open_field_property.related_link_field_id,
        look_up_target_field_id: open_field_property.look_up_target_field_id,
        roll_up_type: open_field_property.roll_up_type,
        formatting,
        open_filter: open_field_property.enable_filter_sort,
        filter_info,
        sort_info: open_field_property.sort_info,
        look_up_limit: open_field_property.look_up_limit,
        ..Default::default()
    }
  }
    
  fn get_sort_look_up(
    &self,
    sort_info: Option<LookUpSortInfo>,
    datasheet_id: &String,
    record_ids: &Vec<String>,
  ) -> Vec<ViewRowSO> {
    let snapshot = self.context.get_snapshot(datasheet_id);
    if snapshot.is_none() {
      return vec![];
    }
    let rows = snapshot.unwrap().meta.views.get(0).unwrap().rows.as_ref().unwrap();

    // 过滤行记录
    let rows = rows
      .iter()
      .filter(|row| record_ids.contains(&row.record_id))
      .cloned()
      .collect::<Vec<_>>();

    // 根据 sort_info 排序
    if let Some(sort_info) = sort_info {
      sort_rows_by_sort_info(self.context.clone(), &rows, &sort_info.rules, snapshot.unwrap())
    } else {
      rows
    }
  }
}
