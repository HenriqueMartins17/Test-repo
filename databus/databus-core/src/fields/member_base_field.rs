use std::fmt;
use std::rc::Rc;

use databus_shared::prelude::HashSet;

use crate::fields::base_field::{BaseField, IBaseField};
use crate::fields::ext::types::CellToStringOption;
use crate::prelude::view_operation::filter::{ConditionValue, FOperator};
use crate::prelude::{CellValue, DatasheetPackContext, FieldSO};
use crate::so::FieldKindSO;
use crate::utils::hash_set_ext::is_same_set;

#[derive(Debug)]
enum OtherTypeUnitId {
  SelfUser, // used to identify the current user
  Alien,    // used to identify anonymous
}

impl fmt::Display for OtherTypeUnitId {
  fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
    match self {
      OtherTypeUnitId::SelfUser => write!(f, "Self"),
      OtherTypeUnitId::Alien => write!(f, "Alien"),
    }
  }
}

pub trait IMemberBaseField: IBaseField {
  fn get_unit_ids(&self, cell_value: &CellValue) -> Option<Vec<String>> {
    cell_value.clone().to_string_array()
  }

  fn unit_ids_to_string(&self, cell_value: &CellValue) -> Option<String> {
    let option_vec = self.get_unit_ids(cell_value);
    if option_vec.is_none() {
      return None;
    }
    let mut vec = option_vec.unwrap();
    vec.sort();

    MemberBaseField::array_value_to_string(CellValue::from(vec))
  }

  fn get_unit_names(&self, cell_value: Vec<String>) -> Option<Vec<String>>;
}

pub struct MemberBaseField;

impl MemberBaseField {
  pub fn compare(
    self_base: &dyn IBaseField,
    self_member: &dyn IMemberBaseField,
    cell_value1: &CellValue,
    cell_value2: &CellValue,
    order_in_cell_value_sensitive: Option<bool>,
  ) -> i32 {
    if !order_in_cell_value_sensitive.unwrap_or(false) {
      if self_base.eq(cell_value1, cell_value2) {
        return 0;
      }
      if cell_value1.is_null() {
        return -1;
      }
      if cell_value2.is_null() {
        return 1;
      }
      let str1 = self_member.unit_ids_to_string(cell_value1);
      let str2 = self_member.unit_ids_to_string(cell_value2);
      if str1 == str2 {
        return 0;
      }
      if str1.is_none() {
        return -1;
      }
      if str2.is_none() {
        return 1;
      }
      let maybe_str1 = str1.unwrap();
      let maybe_str2 = str2.unwrap();

      let str1 = maybe_str1.trim();
      let str2 = maybe_str2.trim();
      return if str1 == str2 {
        0
      } else if str1 < str2 {
        -1
      } else {
        1
      };
    }
    BaseField::compare(self_base, cell_value1, cell_value2, order_in_cell_value_sensitive)
  }

  pub fn cell_value_to_string(
    self_member: &dyn IMemberBaseField,
    cell_value: CellValue,
    _option: Option<CellToStringOption>,
  ) -> Option<String> {
    if cell_value.is_null() {
      return None;
    }
    Self::array_value_to_string(Self::cell_value_to_array(
      self_member,
      CellValue::Array(vec![cell_value]).flat(1),
    ))
  }

  pub fn array_value_to_string(cell_value: CellValue) -> Option<String> {
    if let Some(arr) = cell_value.to_string_array() {
      return Some(arr.join(", "));
    }
    None
  }

  pub fn cell_value_to_array(self_member: &dyn IMemberBaseField, cell_value: CellValue) -> CellValue {
    if cell_value.is_null() {
      return CellValue::Null;
    }

    let unit_ids = self_member.get_unit_ids(&cell_value);
    let names = self_member.get_unit_names(unit_ids.unwrap());
    return match names {
      Some(v) => CellValue::Array(v.into_iter().map(|v| CellValue::String(v)).collect()),
      None => CellValue::Null,
    };
  }

  pub fn is_meet_filter(operator: &FOperator, cell_value: &CellValue, condition_value: &ConditionValue) -> bool {
    tracing::info!(
      "operator: {:?}, cell_value: {:?}, condition_value: {:?}",
      operator,
      cell_value,
      condition_value
    );
    let cv4filter: Vec<String> = if cell_value.is_null() {
      vec![]
    } else {
      cell_value.clone().to_string_array().unwrap()
    };
    match operator {
      FOperator::IsEmpty => cv4filter.is_empty(),
      FOperator::IsNotEmpty => !cv4filter.is_empty(),
      FOperator::Is | FOperator::IsNot | FOperator::Contains | FOperator::DoesNotContain => {
        let condition_value = if condition_value.is_null() {
          vec![]
        } else {
          condition_value.clone().as_string_array().unwrap()
        };
        let cell_value_set = cv4filter.into_iter().collect::<HashSet<String>>();
        let condition_value_set = condition_value.into_iter().collect::<HashSet<String>>();
        match operator {
          FOperator::Is => !cell_value.is_null() && is_same_set(&cell_value_set, &condition_value_set),
          FOperator::IsNot => cell_value.is_null() || !is_same_set(&cell_value_set, &condition_value_set),
          FOperator::Contains => !cell_value.is_null() && cell_value_set.intersection(&condition_value_set).count() > 0,
          FOperator::DoesNotContain => {
            cell_value.is_null() || !cell_value_set.intersection(&condition_value_set).count() > 0
          }
          _ => false,
        }
      }

      _ => BaseField::is_meet_filter(operator, cell_value, condition_value),
    }
  }

  pub fn condition_value_to_real(
    field: &FieldSO,
    context: &Rc<DatasheetPackContext>,
    value: Vec<String>,
  ) -> Vec<String> {
    let mut condition_value = value.clone();
    if let Some(self_index) = condition_value
      .iter()
      .position(|v| v == &OtherTypeUnitId::SelfUser.to_string())
    {
      let self_unit_id = if field.kind == FieldKindSO::Member {
        &context.user_info.unit_id
      } else {
        &context.user_info.uuid
      };
      condition_value[self_index] = self_unit_id.clone();
    }
    condition_value
  }
}

#[cfg(test)]
mod test {
  use crate::fields::member_base_field::MemberBaseField;
  use crate::prelude::view_operation::filter::FOperator;
  use crate::prelude::CellValue;
  use crate::so::view_operation::filter::ConditionValue;

  #[test]
  pub fn test_is_meet_filter() {
    let cell_value = CellValue::Array(vec![CellValue::String("111111".to_string())]);
    let cell_value1 = CellValue::Array(vec![
      CellValue::String("111111".to_string()),
      CellValue::String("112321".to_string()),
    ]);
    let condition_value: ConditionValue = ConditionValue::StringArray(vec!["111111".to_string()]);
    let result = MemberBaseField::is_meet_filter(&FOperator::Is, &cell_value, &condition_value);
    assert_eq!(result, true);

    let result = MemberBaseField::is_meet_filter(&FOperator::Is, &cell_value1, &condition_value);
    assert_eq!(result, false);

    let result = MemberBaseField::is_meet_filter(&FOperator::Is, &CellValue::Null, &condition_value);
    assert_eq!(result, false);

    let result = MemberBaseField::is_meet_filter(&FOperator::IsNot, &cell_value, &condition_value);
    assert_eq!(result, false);

    let result = MemberBaseField::is_meet_filter(&FOperator::IsNot, &CellValue::Null, &condition_value);
    assert_eq!(result, true);
  }
}
