use std::collections::HashMap;

use crate::{
  config::Role,
  ot::{
    commands::{CollaCommandDefExecuteResult, ExecuteResult, SetGroupOptions},
    types::{ActionOTO, ResourceType},
    SetGroupInfoFieldOTO,
  },
  so::DatasheetSnapshotSO,
  DatasheetActions,
};

use crate::ot::datasheet::get_field_role_by_field_id;

pub struct SetGroup {}

impl SetGroup {
  pub fn execute(
    snapshot: DatasheetSnapshotSO,
    options: SetGroupOptions,
  ) -> anyhow::Result<Option<CollaCommandDefExecuteResult>> {
    let data = options.data;
    let view_id = options.view_id;
    // let datasheet_id = get_active_datasheet_id(state).ok_or("Failed to get active datasheet ID")?;
    let datasheet_id = snapshot.datasheet_id.clone();
    // let datasheet = get_datasheet(state, datasheet_id).ok_or("Failed to get datasheet")?;
    let current_view = snapshot
      .meta
      .views
      .iter()
      .find(|item| item.id.is_some() && item.id.clone().unwrap() == view_id)
      .unwrap();
    // let field_ids = get_current_view(state, datasheet_id)
    //     .ok_or("Failed to get current view")?
    //     .columns
    //     .iter()
    //     .map(|item| item.field_id)
    //     .collect::<Vec<_>>();
    let field_ids = current_view
      .columns
      .iter()
      .map(|item| item.field_id.clone())
      .collect::<Vec<_>>();
    // let field_permission_map = get_field_permission_map(state, datasheet_id);
    let field_permission_map = HashMap::new();

    // if state.is_none() || datasheet.is_none() {
    //     return Ok(());
    // }

    // Determine whether the currently operating view is the active view
    // if datasheet.active_view != view_id {
    //     return Err(Box::new(Error::new(ErrorKind::Other, t(Strings.error_group_failed_wrong_target_view))));
    // }

    let check_miss_group_field = || {
      data.is_some()
        && data
          .clone()
          .unwrap()
          .iter()
          .filter(|item| {
            let field_id = item.field_id.clone();
            // Data missing due to permissions is expected and will not be processed
            if get_field_role_by_field_id(Some(&field_permission_map), &field_id) == Some(Role::None) {
              return false;
            }
            !field_ids.contains(&field_id)
          })
          .count()
          > 0
    };

    // Check if the field used by grouping exists in the current view
    if check_miss_group_field() {
      // return Err(Box::new(Error::new(ErrorKind::Other, t(Strings.error_group_failed_the_column_not_exist))));
      panic!("error_group_failed_the_column_not_exist");
    }

    let mut actions: Vec<ActionOTO> = Vec::new();
    if let Some(set_sort_info_action) = DatasheetActions::set_group_info_field_to_action(
      snapshot,
      SetGroupInfoFieldOTO {
        view_id,
        group_info: data,
      },
    )
    .unwrap()
    {
      actions.push(set_sort_info_action);
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
