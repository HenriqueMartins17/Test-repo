use crate::{
  ot::types::ResourceType,
  so::{
    view_operation::{filter::IFilterInfo, sort::ISortedField},
    DatasheetSnapshotSO, RecordAlarm, ViewRowSO, ViewSO, WidgetPanelSO, WidgetPanelStatus,
  },
};
use std::cmp::Ordering;

pub fn get_view_index(snapshot: DatasheetSnapshotSO, view_id: String) -> Option<usize> {
  let views = &snapshot.meta.views;
  let view_index = views
    .iter()
    .position(|viw| viw.id.is_some() && viw.id.clone().unwrap() == view_id);
  view_index
}

pub fn get_view_by_id(snapshot: DatasheetSnapshotSO, view_id: String) -> Option<ViewSO> {
  let views = &snapshot.meta.views;
  let view = views
    .iter()
    .find(|viw| viw.id.is_some() && viw.id.clone().unwrap() == view_id);
  if let Some(view) = view {
    return Some(view.clone());
  }
  None
}

pub fn get_date_time_cell_alarm(
  snapshot: &DatasheetSnapshotSO,
  record_id: &str,
  field_id: &str,
) -> Option<RecordAlarm> {
  let record_meta = snapshot.record_map.get(record_id)?.record_meta.clone();
  if record_meta.is_none() {
    return None;
  }
  let field_extra_map = record_meta.unwrap().field_extra_map.clone();
  if field_extra_map.is_none() {
    return None;
  }
  field_extra_map.unwrap().get(field_id)?.alarm.clone()
}

/**
 * Single Sort Calculation.
 */
pub fn sort_rows_by_sort_info(
  // state: &IReduxState,
  rows: &Option<Vec<ViewRowSO>>,
  sort_rules: &Vec<ISortedField>,
  snapshot: &DatasheetSnapshotSO,
) -> Vec<ViewRowSO> {
  let mut shallow_rows = rows.clone().unwrap();
  shallow_rows.sort_by(|_prev, _current| {
    let res = sort_rules.iter().fold(0, |acc, rule| {
      let field = snapshot.meta.field_map.get(&rule.field_id.clone());
      if field.is_none() || acc != 0 {
        return acc;
      }
      // if field.is_none() || acc != Ordering::Equal {
      //     return acc;
      // }
      // let field_method = Field::bind_context(field.unwrap(), state);

      // let cv1 = get_cell_value(state, snapshot, prev.record_id, field.unwrap().id, None, None, true);
      // let cv1 = get_cell_value(snapshot, prev.record_id, field.unwrap().id, None, None, true);
      // let cv2 = get_cell_value(state, snapshot, current.record_id, field.unwrap().id, None, None, true);
      // let res = field_method.compare(cv1, cv2, true);
      let sign = if rule.desc { -1 } else { 1 };
      // res * sign
      sign
    });
    match res {
      -1 => Ordering::Less,
      0 => Ordering::Equal,
      1 => Ordering::Greater,
      _ => Ordering::Equal,
    }
  });

  shallow_rows
}

// /**
//  * get cell value
//  *
//  * @param state
//  * @param snapshot
//  * @param recordId
//  * @param fieldId
//  * @param withError
//  * @param datasheetId
//  * @param ignoreFieldPermission
//  * @returns
//  */
// pub fn get_cell_value(
//     // state: &IReduxState,
//     snapshot: &DatasheetSnapshotSO,
//     record_id: &str,
//     field_id: &str,
//     with_error: Option<bool>,
//     datasheet_id: Option<&str>,
//     ignore_field_permission: Option<bool>,
// ) -> Option<any> {
//     // TODO: temp code for the first version of column permission, delete this logic in next version
//     let field_permission_map = get_field_permission_map(state, snapshot.datasheet_id);
//     let field_role = get_field_role_by_field_id(&field_permission_map, field_id);

//     if !ignore_field_permission.unwrap_or(false) && field_role == Role::None {
//         return None;
//     }

//     let ds_id = datasheet_id.or(snapshot.datasheet_id).or(state.page_params.datasheet_id);
//     let calc = || {
//         calc_cell_value_and_string(
//             state,
//             snapshot,
//             field_id,
//             record_id,
//             ds_id,
//             with_error,
//             ignore_field_permission,
//         )
//     };
//     if ds_id.is_none() {
//         return calc().cell_value;
//     }

//     let cache_value = cache_manager::get_cell_cache(ds_id.unwrap(), field_id, record_id);
//     if cache_value != NO_CACHE {
//         return cache_value.cell_value;
//     }
//     let res = calc();
//     if !res.ignore_cache {
//         cache_manager::set_cell_cache(ds_id.unwrap(), field_id, record_id, res);
//     }
//     return res.cell_value;
// }

pub fn validate_filter_info(filter_info: &Option<IFilterInfo>) -> bool {
  if filter_info.is_none() {
    return true;
  }

  let filter_info = filter_info.clone().unwrap();

  // if filter_info.conjunction.is_none() {
  //     return false;
  // }

  true
}

pub fn get_resource_active_widget_panel(
  // state: &IReduxState,
  snapshot: &DatasheetSnapshotSO,
  resource_id: &str,
  resource_type: &ResourceType,
) -> Option<WidgetPanelSO> {
  let panels = get_resource_widget_panels(snapshot, resource_id, resource_type);

  if panels.is_none() {
    return None;
  }

  let widget_panel_status = get_resource_widget_panel_status(snapshot, resource_id, resource_type);
  let active_panel_id = widget_panel_status.clone().and_then(|status| status.active_panel_id);
  let panel_opening = widget_panel_status.is_some() && widget_panel_status.unwrap().opening;

  if !panel_opening {
    return None;
  }

  if let Some(active_id) = active_panel_id {
    return panels
      .clone()
      .unwrap()
      .iter()
      .find(|item| item.id == active_id)
      .or(panels.unwrap().get(0))
      .cloned();
  }

  panels.unwrap().get(0).cloned()
}

pub fn get_resource_widget_panels(
  // state: &IReduxState,
  snapshot: &DatasheetSnapshotSO,
  resource_id: &str,
  resource_type: &ResourceType,
) -> Option<Vec<WidgetPanelSO>> {
  match resource_type {
    ResourceType::Datasheet => get_widget_panels(snapshot, Some(resource_id)),
    // ResourceType::Mirror => get_widget_panels_with_mirror(state, resource_id),
    // state->mirrorMap->mirror->mirror_snapshot->widgetPanels,
    _ => get_widget_panels(snapshot, Some(resource_id)),
  }
}

pub fn get_widget_panels(
  // state: &IReduxState,
  snapshot: &DatasheetSnapshotSO,
  _datasheet_id: Option<&str>,
) -> Option<Vec<WidgetPanelSO>> {
  // let snapshot = get_snapshot(state, datasheet_id);
  // snapshot.and_then(|s| s.meta.widget_panels.as_ref())
  snapshot.meta.widget_panels.clone()
}

// pub fn get_widget_panels_with_mirror(
//     // state: &IReduxState,
//     mirror_id: &str
// ) -> Option<Vec<WidgetPanelSO>> {
//     let snapshot = get_mirror_snapshot(state, mirror_id);
//     snapshot.and_then(|s| s.widget_panels.as_ref())
// }

pub fn get_resource_widget_panel_status(
  // state: &IReduxState,
  snapshot: &DatasheetSnapshotSO,
  resource_id: &str,
  resource_type: &ResourceType,
) -> Option<WidgetPanelStatus> {
  match resource_type {
    ResourceType::Datasheet => get_widget_panel_status(snapshot, Some(resource_id)),
    // ResourceType::Mirror => get_widget_panel_status_with_mirror(state, resource_id),
    _ => get_widget_panel_status(snapshot, Some(resource_id)),
  }
}

pub fn get_widget_panel_status(
  // state: &IReduxState,
  _snapshot: &DatasheetSnapshotSO,
  _datasheet_id: Option<&str>,
) -> Option<WidgetPanelStatus> {
  // let client = get_datasheet_client(state, datasheet_id);
  // client.and_then(|c| c.widget_panel_status)
  //data_pack->client->widget_panel_status
  Some(WidgetPanelStatus { ..Default::default() })
}

// 缺少数据结构IRecordAlarm暂时不处理
// pub fn get_date_time_cell_alarm(snapshot: DatasheetSnapshotSO, record_id: &str, field_id: &str) -> Option<&IRecordAlarm> {
//   // notification center open card without snapshot
//   let record_meta = snapshot.record_map.get(record_id)?.record_meta;
//   if record_meta.is_none() {
//       return None;
//   }
//   let field_extra_map = record_meta?.field_extra_map;
//   if field_extra_map.is_none() {
//       return None;
//   }
//   field_extra_map.get(field_id)?.alarm
// }
