use std::collections::HashMap;

use crate::{
  config::Role,
  ot::{
    commands::{CollaCommandDefExecuteResult, ExecuteResult, SetSortInfoOptions},
    types::{ActionOTO, ResourceType},
    ViewSortOTO,
  },
  so::{DatasheetSnapshotSO, FieldPermissionInfo},
  DatasheetActions,
};

use super::get_field_role_by_field_id;

pub struct SetSortInfo {}

impl SetSortInfo {
  pub fn execute(
    snapshot: DatasheetSnapshotSO,
    options: SetSortInfoOptions,
  ) -> anyhow::Result<Option<CollaCommandDefExecuteResult>> {
    let SetSortInfoOptions {
      data,
      view_id,
      apply_sort,
      cmd: _,
    } = options;
    // let datasheet_id = get_active_datasheet_id(state).unwrap();
    // let datasheet = get_datasheet(state, datasheet_id);
    let current_view = snapshot
      .meta
      .views
      .iter()
      .find(|item| item.id.is_some() && item.id.clone().unwrap() == view_id)
      .unwrap();
    let field_ids = current_view
      .columns
      .iter()
      .map(|item| item.field_id.clone())
      .collect::<Vec<_>>();
    // let field_ids = get_current_view(state, datasheet_id).unwrap().columns.iter().map(|item| item.field_id).collect::<Vec<_>>();
    // let field_permission_map = get_field_permission_map(state, datasheet_id);

    // if state.is_none() || datasheet.is_none() {
    //     return Ok(None);
    // }
    let datasheet_id = snapshot.datasheet_id.clone();
    let field_permission_map: HashMap<String, FieldPermissionInfo> = HashMap::new();

    let has_invalid_sort = data.clone().map_or(false, |d| {
      d.rules.iter().any(|sort_field| {
        let field_id = sort_field.field_id.clone();
        let field_role = get_field_role_by_field_id(Some(&field_permission_map), &field_id);

        if field_role.is_some() && field_role.unwrap() == Role::None {
          return false;
        }

        !field_ids.contains(&field_id)
      })
    });

    if has_invalid_sort {
      // return Err(Error::new(t(Strings::ErrorSortedFailedTheFieldNotExist)));
      return Ok(None);
    }

    // if datasheet.active_view != view_id {
    //     return Err(Error::new(t(Strings::ErrorSortedFailedWrongTargetView)));
    // }

    let mut actions: Vec<ActionOTO> = Vec::new();

    if let Some(set_sort_info_action) = DatasheetActions::set_view_sort_to_action(
      snapshot,
      ViewSortOTO {
        view_id,
        sort_info: data,
        apply_sort: apply_sort.unwrap(),
      },
    )
    .unwrap()
    {
      actions.extend(set_sort_info_action);
    }

    if actions.is_empty() {
      return Ok(None);
    }

    Ok(Some(CollaCommandDefExecuteResult {
      result: ExecuteResult::Success,
      resource_id: datasheet_id,
      resource_type: ResourceType::Datasheet,
      actions,
      ..Default::default()
    }))
  }
}
