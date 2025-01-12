use std::rc::Rc;

use crate::so::view_operation::filter::IFilterInfo;
use crate::so::DatasheetPackContext;

// TODO: refactor
pub fn get_filter_info_except_invalid(
  state: &Rc<DatasheetPackContext>,
  datasheet_id: &String,
  filter_info: &Option<IFilterInfo>,
) -> Option<IFilterInfo> {
  // filterInfo.conditions is empty will cause no filter but no data returned, so we need to filter it

  if filter_info.is_none() || filter_info.as_ref().unwrap().conditions.is_empty() {
    return None;
  }

  // let field_permission_map = get_field_permission_map(state, datasheet_id);
  let snapshot = state.get_snapshot(datasheet_id.as_str());
  if snapshot.is_none() {
    return None;
  }

  return Some(IFilterInfo {
    conditions: filter_info
      .as_ref()
      .unwrap()
      .conditions
      .iter()
      .filter(|condition| snapshot.unwrap().meta.field_map.get(&condition.field_id).is_some())
      .cloned()
      .collect(),
    conjunction: filter_info.as_ref().unwrap().conjunction.clone(),
  });
}
