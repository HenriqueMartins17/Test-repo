use std::{
  collections::{HashMap, HashSet},
  rc::Rc,
};

use anyhow::Ok;
use field::FieldKindSO;
use json0::{
  operation::{OperationKind, PathSegment},
  Operation,
};
use serde_json::{json, to_value, Map, Value};

use crate::modules::database::store::selectors::resource::datasheet::cell_calc::get_cell_value;
use crate::so::CellValue;
use crate::{
  fields::field_factory::FieldFactory,
  ot::{
    types::{ActionOTO, ResourceType},
    DeleteRecordItem, RecordDelete,
  },
  so::{
    constants::StatType,
    field,
    types::ViewType,
    view_operation::{filter::IFilterInfo, sort::ISortedField},
    CellValueSo, DatasheetPackContext, DatasheetSnapshotSO, MirrorSnapshot, RecordSO, UserInfoSO, ViewColumnSO, ViewSO,
    WidgetPanelSO,
  },
  PayloadAddFieldVO, PayloadDelFieldVO,
};

use super::{
  get_date_time_cell_alarm, get_resource_active_widget_panel, get_resource_widget_panels, get_view_by_id,
  get_view_index, sort_rows_by_sort_info, validate_filter_info, ChangeOneWayLinkDstIdOTO, ChangeWidgetHeightOTO,
  ColumnWidthOTO, DeleteCommentOTO, DeleteRecordsOTO, DeleteWidgetOTO, FieldOTO, InsertCommentOTO, ModifyViewOTO,
  MoveColumnsOTO, MoveRowOTO, MoveWidgetOTO, PayloadAddRecordVO, PayloadAddViewVO, PayloadDelViewVO, PayloadMoveViewVO,
  RecordAlarmOTO, SetAutoHeadHeightOTO, SetColumnStatTypeOTO, SetFilterInfoOTO, SetFrozenColumnCountOTO,
  SetGroupInfoFieldOTO, SetRowHeightLevelOTO, SetViewAutoSaveOTO, SetViewLockInfoOTO, UpdateCommentOTO, ViewOTO,
  ViewPropertyFilter, ViewSortOTO, WidgetOTO, MAX_SAFE_INTEGER,
};

pub struct DatasheetActions {}
impl DatasheetActions {
  /**
   * add `view` to table
   */
  pub fn add_view_to_action(
    snapshot: DatasheetSnapshotSO,
    payload: PayloadAddViewVO,
  ) -> anyhow::Result<Option<ActionOTO>> {
    let view = &payload.view;
    let start_index = payload.start_index;
    let views = &snapshot.meta.views;

    // if views.len() >= get_max_view_count_per_sheet() {
    if views.len() >= 30 {
      return Ok(None);
    }

    if views.iter().any(|viw| viw.id == view.id) {
      return Ok(None);
    }

    let mut start_index_tmp = 0;
    if start_index.is_none() {
      start_index_tmp = views.len();
    } else {
      if let Some(start_index) = start_index {
        if !(start_index <= views.len()) {
          start_index_tmp = views.len();
        } else {
          start_index_tmp = start_index;
        }
      }
    }

    let set_default_field_stat = |view: &ViewSO| -> ViewSO {
      if view.r#type != Some(ViewType::Grid as u64) {
        return view.clone();
      }

      let mut new_view = view.clone();
      let new_columns = view
        .columns
        .iter()
        .enumerate()
        .map(|(i, col)| {
          if i < 1 {
            let mut new_col = col.clone();
            new_col.stat_type = Some(StatType::CountAll as i32);
            return new_col;
          }

          let field = snapshot.meta.field_map.get(&col.field_id);
          if let Some(field) = field {
            let field_type = field.kind;
            if [FieldKindSO::Number, FieldKindSO::Currency].contains(&field_type) {
              let mut new_col = col.clone();
              new_col.stat_type = Some(StatType::Sum as i32);
              return new_col;
            }
          }

          return col.clone();
        })
        .collect();

      new_view.columns = new_columns;
      new_view
    };

    let json_str = serde_json::to_string(&set_default_field_stat(view)).unwrap();
    let json_value: Value = serde_json::from_str(&json_str).unwrap();
    Ok(Some(ActionOTO {
      op_name: "LI".to_string(),
      op: Operation {
        p: vec![
          PathSegment::String("meta".to_string()),
          PathSegment::String("views".to_string()),
          PathSegment::Number(start_index_tmp),
        ],
        kind: OperationKind::ListInsert { li: json_value },
      },
    }))
  }

  /**
   * move views
   * @param {string} viewId
   */
  pub fn move_view_to_action(
    snapshot: DatasheetSnapshotSO,
    payload: PayloadMoveViewVO,
  ) -> anyhow::Result<Option<ActionOTO>> {
    let view_id = payload.view_id;
    let target = payload.target;
    let views = &snapshot.meta.views;
    let mut index = -1;

    for (i, view) in views.iter().enumerate() {
      if let Some(view_id_tmp) = &view.id {
        if view_id_tmp == &view_id {
          index = i as i32;
          break;
        }
      }
    }

    if index == -1 {
      return Ok(None);
    }

    Ok(Some(ActionOTO {
      op_name: "LM".to_string(),
      op: Operation {
        p: vec![
          PathSegment::String("meta".to_string()),
          PathSegment::String("views".to_string()),
          PathSegment::Number(index as usize),
        ],
        kind: OperationKind::ListMove { lm: target },
      },
    }))
  }

  /**
   * delete view based viewID,
   *
   * @param {string} viewId
   */
  pub fn delete_view_to_action(
    snapshot: DatasheetSnapshotSO,
    payload: PayloadDelViewVO,
  ) -> anyhow::Result<Option<ActionOTO>> {
    let views = &snapshot.meta.views;
    let view_id = payload.view_id;
    // check whether current is activeView
    let view_index = views.iter().position(|viw| viw.id == Some(view_id.to_string()));
    if let Some(index) = view_index {
      let json_str = serde_json::to_string(&views[index]).unwrap();
      let json_value: Value = serde_json::from_str(&json_str).unwrap();
      return Ok(Some(ActionOTO {
        op_name: "LD".to_string(),
        op: Operation {
          p: vec![
            PathSegment::String("meta".to_string()),
            PathSegment::String("views".to_string()),
            PathSegment::Number(index),
          ],
          kind: OperationKind::ListDelete { ld: json_value },
        },
      }));
    }
    Ok(None)
  }

  /**
   * update view based viewID
   *
   * @param {string} viewId
   */
  pub fn modify_view_to_action(
    snapshot: DatasheetSnapshotSO,
    payload: ModifyViewOTO,
  ) -> anyhow::Result<Option<Vec<ActionOTO>>> {
    let ModifyViewOTO { view_id, key, value } = payload;

    // check whether current is activeView
    let view_index = get_view_index(snapshot.clone(), view_id);
    if view_index.is_none() {
      return Ok(None);
    }
    let view_index = view_index.unwrap();
    let views = &snapshot.meta.views;

    if key == "columns" && value.is_array() {
      let mut actions = Vec::new();
      let value_array = value.as_array().unwrap();

      for item in value_array {
        let item = item.as_object().unwrap();
        let field_id = item["fieldId"].as_str().unwrap();
        let view = &views[view_index];
        let columns = &view.columns;
        let modify_column_index = columns.iter().position(|column| column.field_id == field_id).unwrap();
        let old_item = &columns[modify_column_index];

        let json_str = serde_json::to_string(&item).unwrap(); //String
        let json_value: ViewColumnSO = serde_json::from_str(&json_str).unwrap();

        if old_item != &json_value {
          let json_str = serde_json::to_string(&old_item).unwrap(); //String
          let json_value: Value = serde_json::from_str(&json_str).unwrap();
          actions.push(ActionOTO {
            op_name: "LR".to_string(),
            op: Operation {
              p: vec![
                PathSegment::String("meta".to_string()),
                PathSegment::String("views".to_string()),
                PathSegment::Number(view_index),
                PathSegment::String("columns".to_string()),
                PathSegment::Number(modify_column_index),
              ],
              kind: OperationKind::ListReplace {
                ld: json_value,
                li: Value::Object(item.clone()),
              },
            },
          });
        }
      }

      return Ok(Some(actions));
    }
    let view = &views[view_index];
    let str: Value = match key.as_str() {
      "name" => Value::String(view.name.clone().unwrap()),
      "description" => Value::String(view.description.clone().unwrap()),
      "columns" => serde_json::from_str(&serde_json::to_string(&view.columns).unwrap()).unwrap(),
      "displayHiddenColumnWithinMirror" => Value::Bool(view.display_hidden_column_within_mirror.unwrap()),
      _ => Value::Null,
    };
    let action = ActionOTO {
      op_name: "OR".to_string(),
      op: Operation {
        p: vec![
          PathSegment::String("meta".to_string()),
          PathSegment::String("views".to_string()),
          PathSegment::Number(view_index),
          PathSegment::String(key),
        ],
        kind: OperationKind::ObjectReplace {
          od: str,
          oi: value.clone(),
        },
      },
    };

    Ok(Some(vec![action]))
  }

  /**
   * add Field to table
   */
  pub fn add_field_to_action(
    snapshot: DatasheetSnapshotSO,
    payload: PayloadAddFieldVO,
  ) -> anyhow::Result<Option<Vec<ActionOTO>>> {
    let field_map = snapshot.meta.field_map;
    let views = snapshot.meta.views;
    let PayloadAddFieldVO {
      field,
      view_id,
      field_id,
      offset,
      index,
      hidden_column,
    } = payload;
    let field_id_tmp = field.id.clone();
    let field_type = field.kind.clone();

    let mut actions: Vec<ActionOTO> = views.iter().enumerate().fold(Vec::new(), |mut pre, (view_index, cur)| {
      let mut column_index = cur.columns.len();
      if cur.columns.iter().any(|column| column.field_id == field_id_tmp) {
        return pre;
      }
      // as new columns with duplicate operation, pass in `index` to take precedence
      //当右边view_id为some成员中的值时，取左边view_id
      if let Some(view_id) = &view_id {
        if let Some(index) = index {
          if let Some(cur_id) = &cur.id {
            if view_id == cur_id {
              column_index = index as usize;
            }
          }
        }
      }

      // only under specified view, if fieldId exists, calc index by every view's fieldId
      if let Some(field_id) = &field_id {
        if let Some(field_id_index) = cur.columns.iter().position(|column| &column.field_id == field_id) {
          column_index = field_id_index + offset.unwrap_or(0) as usize;
        }
      }

      let mut new_column: HashMap<String, Value> = HashMap::new();
      new_column.insert("fieldId".to_owned(), Value::String((field_id_tmp).to_string()));

      if [FieldKindSO::Number, FieldKindSO::Currency].contains(&field_type) {
        new_column.insert(
          "statType".to_owned(),
          Value::Number(serde_json::Number::from(StatType::Sum as i64)),
        );
      }

      let mut b_temp = true;
      let mut hidden_key = String::from("hidden");
      if let Some(r#type) = &cur.r#type {
        // let r#type = ViewType::from(*r#type as ViewType);
        match r#type {
          6 => hidden_key = String::from("hiddenInGantt"),
          5 => hidden_key = String::from("hiddenInCalendar"),
          7 => hidden_key = String::from("hiddenInOrgChart"),
          // ViewType::Gantt => hidden_key = String::from("hiddenInGantt"),
          // ViewType::Calendar => hidden_key = String::from("hiddenInCalendar"),
          // ViewType::OrgChart => hidden_key = String::from("hiddenInOrgChart"),
          _ => {
            if let Some(view_id) = &view_id {
              if let Some(cur_id) = &cur.id {
                if view_id == cur_id {
                  new_column.insert("hidden".to_owned(), Value::Bool(hidden_column.unwrap_or(false)));
                  b_temp = false;
                }
              }
            }
            if cur.columns.iter().all(|column| column.hidden.unwrap_or(false) == false) {
              new_column.insert("hidden".to_owned(), Value::Bool(hidden_column.unwrap_or(false)));
              b_temp = false;
            }
          }
        }
      }
      if b_temp {
        let is_gantt_view = view_id == cur.id && cur.r#type == Some(ViewType::Gantt as u64);
        new_column.insert(
          "hidden".to_owned(),
          Value::Bool(!is_gantt_view || hidden_column.unwrap_or(false)),
        );
        new_column.insert(hidden_key, Value::Bool(true));
      }

      pre.push(ActionOTO {
        op_name: "LI".to_string(),
        op: Operation {
          p: vec![
            PathSegment::String("meta".to_string()),
            PathSegment::String("views".to_string()),
            PathSegment::Number(view_index.clone()),
            PathSegment::String("columns".to_string()),
            PathSegment::Number(column_index.clone()),
          ],
          kind: OperationKind::ListInsert {
            li: to_value(&new_column).unwrap(),
          },
        },
      });

      pre
    });

    if !field_map.contains_key(&field_id_tmp) {
      actions.push(ActionOTO {
        op_name: "OI".to_string(),
        op: Operation {
          p: vec![
            PathSegment::String("meta".to_string()),
            PathSegment::String("fieldMap".to_string()),
            PathSegment::String(field_id_tmp.to_string()),
          ],
          kind: OperationKind::ObjectInsert {
            oi: to_value(field).unwrap(),
          },
        },
      });
    }

    Ok(Some(actions))
  }

  /**
   * delete field
   */
  pub fn delete_field_to_action(
    snapshot: DatasheetSnapshotSO,
    payload: PayloadDelFieldVO,
  ) -> anyhow::Result<Option<Vec<ActionOTO>>> {
    let field_map = snapshot.meta.field_map;
    let views = snapshot.meta.views;
    let PayloadDelFieldVO {
      field_id,
      datasheet_id,
      view_id,
    } = payload;

    // delete all columns related attributes in all view
    let mut actions: Vec<ActionOTO> = views.iter().enumerate().fold(Vec::new(), |mut action, (index, view)| {
      let column_index = view.columns.iter().position(|column| &column.field_id == &field_id);
      if column_index.is_none() {
        return action;
      }

      // judgement here is for the permissions tips of lock view.
      // for example, `view 2` set field A's filter condition, I delete field A in `view 1`,
      // because `view 2`'s view lock will make me operation failed,
      // from user's point of view, he may not delete one field before check all views, and close view lock
      // so, judgement here is to delete field, and at the same time, doesn't delete the information in lock view,
      // just show exception tips only
      // what's special, is the relation table operation, verifications of relation table in middle server(room-server) is not strict,
      // only require editable permission, therefor, relation table operation can go pass directly.

      // the dependencies in filter's field, also need to be deleted
      if datasheet_id != snapshot.datasheet_id || !(view.lock_info.is_some() && view.id != view_id) {
        if let Some(filter_info) = &view.filter_info {
          let new_conditions = filter_info
            .conditions
            .iter()
            .filter(|condition| condition.field_id != field_id)
            .collect::<Vec<_>>();
          let od_str = serde_json::to_string(filter_info).unwrap();
          let od_tmp: Value = serde_json::from_str(&od_str).unwrap();
          if new_conditions.is_empty() {
            action.push(ActionOTO {
              op_name: "OD".to_string(),
              op: Operation {
                p: vec![
                  PathSegment::String("meta".to_string()),
                  PathSegment::String("views".to_string()),
                  PathSegment::Number(index.clone()),
                  PathSegment::String("filterInfo".to_string()),
                ],
                kind: OperationKind::ObjectDelete { od: od_tmp },
              },
            });
          } else if new_conditions.len() != filter_info.conditions.len() {
            let od_str = serde_json::to_string(&filter_info.conditions).unwrap();
            let od_tmp: Value = serde_json::from_str(&od_str).unwrap();

            let oi_str = serde_json::to_string(&new_conditions).unwrap();
            let oi_tmp: Value = serde_json::from_str(&oi_str).unwrap();

            action.push(ActionOTO {
              op_name: "OR".to_string(),
              op: Operation {
                p: vec![
                  PathSegment::String("meta".to_string()),
                  PathSegment::String("views".to_string()),
                  PathSegment::Number(index.clone()),
                  PathSegment::String("filterInfo".to_string()),
                  PathSegment::String("conditions".to_string()),
                ],
                kind: OperationKind::ObjectReplace { od: od_tmp, oi: oi_tmp },
              },
            });
          }
        }

        for r#type in ["group_info, sort_info"] {
          let tmp = view.sort_info.as_ref().and_then(|s| Some(s.rules.clone()));
          let info = if r#type == "group_info" { &view.group_info } else { &tmp };
          if let Some(info) = info {
            if let Some(info_index) = info.iter().position(|gp| gp.field_id.clone() == field_id) {
              if info.len() > 1 {
                let sorted_field = info[info_index].clone();
                let mut tmp: HashMap<String, Value> = HashMap::new();
                tmp.insert("fieldId".to_owned(), Value::String(sorted_field.field_id.clone()));
                tmp.insert("desc".to_owned(), Value::Bool(sorted_field.desc));

                if r#type == "group_info" {
                  action.push(ActionOTO {
                    op_name: "LD".to_string(),
                    op: Operation {
                      p: vec![
                        PathSegment::String("meta".to_string()),
                        PathSegment::String("views".to_string()),
                        PathSegment::Number(index),
                        PathSegment::String(r#type.to_string()),
                        PathSegment::Number(info_index),
                      ],
                      kind: OperationKind::ListDelete {
                        ld: to_value(&tmp).unwrap(),
                      },
                    },
                  });
                  // compensator.setLastGroupInfoIfNull(info); 将info的值传到其他函数去了
                } else {
                  action.push(ActionOTO {
                    op_name: "LD".to_string(),
                    op: Operation {
                      p: vec![
                        PathSegment::String("meta".to_string()),
                        PathSegment::String("views".to_string()),
                        PathSegment::Number(index),
                        PathSegment::String(r#type.to_string()),
                        PathSegment::String("rules".to_string()),
                        PathSegment::Number(info_index),
                      ],
                      kind: OperationKind::ListDelete {
                        ld: to_value(&tmp).unwrap(),
                      },
                    },
                  });
                }
              }
              if info.len() == 1 {
                if r#type == "group_info" {
                  let sorted_field = &info[0];
                  let mut tmp: HashMap<String, Value> = HashMap::new();
                  tmp.insert(
                    "fieldId".to_owned(),
                    Value::String(sorted_field.field_id.clone().to_string()),
                  );
                  tmp.insert("desc".to_owned(), Value::Bool(sorted_field.desc));
                  let jsvalue_tmp = to_value(&tmp).unwrap();

                  action.push(ActionOTO {
                    op_name: "OD".to_string(),
                    op: Operation {
                      p: vec![
                        PathSegment::String("meta".to_string()),
                        PathSegment::String("views".to_string()),
                        PathSegment::Number(index.clone()),
                        PathSegment::String(r#type.to_string()),
                      ],
                      kind: OperationKind::ObjectDelete {
                        od: Value::Array(vec![jsvalue_tmp]),
                      },
                    },
                  });
                } else {
                  let sort_info = view.sort_info.as_ref().unwrap();
                  let od_str = serde_json::to_string(sort_info).unwrap();
                  let od_tmp: Value = serde_json::from_str(&od_str).unwrap();

                  action.push(ActionOTO {
                    op_name: "OD".to_string(),
                    op: Operation {
                      p: vec![
                        PathSegment::String("meta".to_string()),
                        PathSegment::String("views".to_string()),
                        PathSegment::Number(index.clone()),
                        PathSegment::String(r#type.to_string()),
                      ],
                      kind: OperationKind::ObjectDelete { od: od_tmp },
                    },
                  });
                }
              }
            }
          }
        }
        //ViewType::Kanban 2
        if let Some(2) = view.r#type {
          if let Some(style) = &view.style {
            if let Some(kanban_field_id) = &style.kanban_field_id {
              if kanban_field_id == &field_id {
                action.push(ActionOTO {
                  op_name: "OD".to_string(),
                  op: Operation {
                    p: vec![
                      PathSegment::String("meta".to_string()),
                      PathSegment::String("views".to_string()),
                      PathSegment::Number(index.clone()),
                      PathSegment::String("style".to_string()),
                      PathSegment::String("kanbanFieldId".to_string()),
                    ],
                    kind: OperationKind::ObjectDelete {
                      od: Value::String(kanban_field_id.to_string()),
                    },
                  },
                });
              }
            }
          }
        }
        //ViewType::Gantt 6
        if let Some(6) = view.r#type {
          if let Some(style) = &view.style {
            if let Some(start_field_id) = &style.start_field_id {
              if start_field_id == &field_id {
                action.push(ActionOTO {
                  op_name: "OD".to_string(),
                  op: Operation {
                    p: vec![
                      PathSegment::String("meta".to_string()),
                      PathSegment::String("views".to_string()),
                      PathSegment::Number(index.clone()),
                      PathSegment::String("style".to_string()),
                      PathSegment::String("startFieldId".to_string()),
                    ],
                    kind: OperationKind::ObjectDelete {
                      od: Value::String(start_field_id.to_string()),
                    },
                  },
                });
              }
            }
            if let Some(end_field_id) = &style.end_field_id {
              if end_field_id == &field_id {
                action.push(ActionOTO {
                  op_name: "OD".to_string(),
                  op: Operation {
                    p: vec![
                      PathSegment::String("meta".to_string()),
                      PathSegment::String("views".to_string()),
                      PathSegment::Number(index.clone()),
                      PathSegment::String("style".to_string()),
                      PathSegment::String("endFieldId".to_string()),
                    ],
                    kind: OperationKind::ObjectDelete {
                      od: Value::String(end_field_id.to_string()),
                    },
                  },
                });
              }
            }
          }
        }
        if let Some(column_index) = column_index {
          let column_index_tmp = column_index as i32;
          if let Some(frozen_column_count) = (view as &ViewSO).frozen_column_count {
            if frozen_column_count > 0 && column_index_tmp < frozen_column_count {
              action.push(ActionOTO {
                op_name: "OR".to_string(),
                op: Operation {
                  p: vec![
                    PathSegment::String("meta".to_string()),
                    PathSegment::String("views".to_string()),
                    PathSegment::Number(index.clone()),
                    PathSegment::String("frozenColumnCount".to_string()),
                  ],
                  kind: OperationKind::ObjectReplace {
                    od: Value::Number(frozen_column_count.into()),
                    oi: Value::Number((frozen_column_count - 1).into()),
                  },
                },
              });
            }
          }
        }
      }

      // delete columns
      if let Some(column_index) = &column_index {
        let json_str = serde_json::to_string(&view.columns[*column_index]).unwrap();
        let json_value: Value = serde_json::from_str(&json_str).unwrap();
        action.push(ActionOTO {
          op_name: "LD".to_string(),
          op: Operation {
            p: vec![
              PathSegment::String("meta".to_string()),
              PathSegment::String("views".to_string()),
              PathSegment::Number(index.clone()),
              PathSegment::String("columns".to_string()),
              PathSegment::Number(column_index.clone()),
            ],
            kind: OperationKind::ListDelete { ld: json_value },
          },
        });
      }

      action
    });

    let field = field_map.get(&field_id);
    if let Some(field) = field {
      // when deleting a date column, remove alarm
      let record_map = &snapshot.record_map;
      for _record_id in record_map.keys() {
        // if let Some(alarm) = Selectors::get_date_time_cell_alarm(&snapshot, record_id, &field.id) {
        //     if let Some(alarm_actions) = DatasheetActions::set_date_time_cell_alarm(&snapshot, &record_id, &field.id, None) {
        //         actions.extend(alarm_actions);
        //     }
        // }
      }

      let json_str = serde_json::to_string(&field).unwrap();
      let json_value: Value = serde_json::from_str(&json_str).unwrap();
      actions.push(ActionOTO {
        op_name: "OD".to_string(),
        op: Operation {
          p: vec![
            PathSegment::String("meta".to_string()),
            PathSegment::String("fieldMap".to_string()),
            PathSegment::String(field_id.to_string()),
          ],
          kind: OperationKind::ObjectDelete { od: json_value },
        },
      });
    }

    return Ok(Some(actions));
  }

  pub fn set_column_width_to_action(
    snapshot: DatasheetSnapshotSO,
    payload: ColumnWidthOTO,
  ) -> anyhow::Result<Option<ActionOTO>> {
    let ColumnWidthOTO {
      view_id,
      field_id,
      width,
    } = payload;

    let view_index = get_view_index(snapshot.clone(), view_id);
    if view_index.is_none() {
      return Ok(None);
    }
    let view_index = view_index.unwrap();
    let view = &snapshot.meta.views[view_index];

    let column_index = view.columns.iter().position(|column| column.field_id == field_id);
    if column_index.is_none() {
      return Ok(None);
    }
    let column_index = column_index.unwrap();
    let column = &view.columns[column_index];
    let f_width = width.clone().map(|w| w as f64);
    // ViewType::Grid, ViewType::Gantt]
    if view.r#type.is_none() || ![1, 6].contains(&view.r#type.unwrap()) || column.width == f_width {
      return Ok(None);
    }

    let str = if column.width.is_none() {
      Value::Null
    } else {
      Value::Number(serde_json::Number::from_f64(column.width.unwrap_or(0.0)).unwrap())
    };
    Ok(Some(ActionOTO {
      op_name: "OR".to_string(),
      op: Operation {
        p: vec![
          PathSegment::String("meta".to_string()),
          PathSegment::String("views".to_string()),
          PathSegment::Number(view_index),
          PathSegment::String("columns".to_string()),
          PathSegment::Number(column_index),
          PathSegment::String("width".to_string()),
        ],
        kind: OperationKind::ObjectReplace {
          od: str,
          oi: Value::Number(serde_json::Number::from_f64(f_width.unwrap_or(0.0)).unwrap()),
        },
      },
    }))
  }

  pub fn move_columns_to_action(
    snapshot: DatasheetSnapshotSO,
    payload: MoveColumnsOTO,
  ) -> anyhow::Result<Option<ActionOTO>> {
    let MoveColumnsOTO {
      field_id,
      target,
      view_id,
    } = payload;

    let view_index = get_view_index(snapshot.clone(), view_id);
    if view_index.is_none() {
      return Ok(None);
    }
    let view_index = view_index.unwrap();
    let view = &snapshot.meta.views[view_index];

    let column_index = view.columns.iter().position(|column| column.field_id == field_id);
    if column_index.is_none() || column_index.unwrap() == target {
      return Ok(None);
    }

    Ok(Some(ActionOTO {
      op_name: "LM".to_string(),
      op: Operation {
        p: vec![
          PathSegment::String("meta".to_string()),
          PathSegment::String("views".to_string()),
          PathSegment::Number(view_index),
          PathSegment::String("columns".to_string()),
          PathSegment::Number(column_index.unwrap()),
        ],
        kind: OperationKind::ListMove { lm: target },
      },
    }))
  }

  /**
   * set grid view's column's statistic dimension
   */
  pub fn set_column_stat_type_action(
    snapshot: DatasheetSnapshotSO,
    payload: SetColumnStatTypeOTO,
  ) -> anyhow::Result<Option<ActionOTO>> {
    let SetColumnStatTypeOTO {
      view_id,
      field_id,
      stat_type,
    } = payload;

    let view_index = get_view_index(snapshot.clone(), view_id);
    if view_index.is_none() {
      return Ok(None);
    }
    let view_index = view_index.unwrap();
    let view = &snapshot.meta.views[view_index];

    let column_index = view.columns.iter().position(|column| column.field_id == field_id);
    let column = view.columns.get(column_index.clone().unwrap_or(0)).unwrap();
    // ViewType::Grid, ViewType::Gantt]
    if column_index.is_none() || ![1, 6].contains(&view.r#type.unwrap()) || column.stat_type == stat_type {
      return Ok(None);
    }

    if stat_type.is_none() {
      return Ok(Some(ActionOTO {
        op_name: "OD".to_string(),
        op: Operation {
          p: vec![
            PathSegment::String("meta".to_string()),
            PathSegment::String("views".to_string()),
            PathSegment::Number(view_index),
            PathSegment::String("columns".to_string()),
            PathSegment::Number(column_index.unwrap()),
            PathSegment::String("statType".to_string()),
          ],
          kind: OperationKind::ObjectDelete {
            od: Value::Number(serde_json::Number::from(column.stat_type.unwrap_or(0) as i64)),
          },
        },
      }));
    }

    Ok(Some(ActionOTO {
      op_name: "OR".to_string(),
      op: Operation {
        p: vec![
          PathSegment::String("meta".to_string()),
          PathSegment::String("views".to_string()),
          PathSegment::Number(view_index),
          PathSegment::String("columns".to_string()),
          PathSegment::Number(column_index.unwrap()),
          PathSegment::String("statType".to_string()),
        ],
        kind: OperationKind::ObjectReplace {
          od: Value::Number(serde_json::Number::from(column.stat_type.unwrap_or(0) as i64)),
          oi: Value::Number(serde_json::Number::from(stat_type.unwrap_or(0) as i64)),
        },
      },
    }))
  }

  /**
   * set the row height of gridview View
   */
  pub fn set_row_height_level_to_action(
    snapshot: DatasheetSnapshotSO,
    payload: SetRowHeightLevelOTO,
  ) -> anyhow::Result<Option<ActionOTO>> {
    let SetRowHeightLevelOTO { view_id, level } = payload;

    let view_index = get_view_index(snapshot.clone(), view_id);
    if view_index.is_none() {
      return Ok(None);
    }
    let view_index = view_index.unwrap();
    let view = &snapshot.meta.views[view_index];

    // ViewType::Grid, ViewType::Gantt]
    if ![1, 6].contains(&view.r#type.unwrap()) || view.row_height_level.unwrap_or(0) == level {
      return Ok(None);
    }

    Ok(Some(ActionOTO {
      op_name: "OR".to_string(),
      op: Operation {
        p: vec![
          PathSegment::String("meta".to_string()),
          PathSegment::String("views".to_string()),
          PathSegment::Number(view_index),
          PathSegment::String("rowHeightLevel".to_string()),
        ],
        kind: OperationKind::ObjectReplace {
          od: Value::Number(serde_json::Number::from(view.row_height_level.unwrap_or(0) as i64)),
          oi: Value::Number(serde_json::Number::from(level as i64)),
        },
      },
    }))
  }

  /**
   * set Grid/Gantt view's column whether auto word wrap
   */
  pub fn set_auto_head_height_to_action(
    snapshot: DatasheetSnapshotSO,
    payload: SetAutoHeadHeightOTO,
  ) -> anyhow::Result<Option<ActionOTO>> {
    let SetAutoHeadHeightOTO { view_id, is_auto } = payload;

    let view_index = get_view_index(snapshot.clone(), view_id);
    if view_index.is_none() {
      return Ok(None);
    }
    let view_index = view_index.unwrap();
    let view = &snapshot.meta.views[view_index];

    // ViewType::Grid, ViewType::Gantt]
    if ![1, 6].contains(&view.r#type.unwrap())
      || (view.auto_head_height.is_some() && view.auto_head_height.unwrap() == is_auto)
    {
      return Ok(None);
    }

    Ok(Some(ActionOTO {
      op_name: "OR".to_string(),
      op: Operation {
        p: vec![
          PathSegment::String("meta".to_string()),
          PathSegment::String("views".to_string()),
          PathSegment::Number(view_index),
          PathSegment::String("autoHeadHeight".to_string()),
        ],
        kind: OperationKind::ObjectReplace {
          od: Value::Bool(view.auto_head_height.unwrap_or(false)),
          oi: Value::Bool(is_auto),
        },
      },
    }))
  }

  pub fn set_frozen_column_count_to_action(
    snapshot: DatasheetSnapshotSO,
    payload: SetFrozenColumnCountOTO,
  ) -> anyhow::Result<Option<ActionOTO>> {
    let SetFrozenColumnCountOTO { view_id, count } = payload;

    let view_index = get_view_index(snapshot.clone(), view_id);
    if view_index.is_none() {
      return Ok(None);
    }
    let view_index = view_index.unwrap();
    let view = &snapshot.meta.views[view_index];

    // ViewType::Grid, ViewType::Gantt]
    if ![1, 6].contains(&view.r#type.unwrap())
      || (view.frozen_column_count.is_some() && view.frozen_column_count.unwrap() == count)
    {
      return Ok(None);
    }

    Ok(Some(ActionOTO {
      op_name: "OR".to_string(),
      op: Operation {
        p: vec![
          PathSegment::String("meta".to_string()),
          PathSegment::String("views".to_string()),
          PathSegment::Number(view_index),
          PathSegment::String("frozenColumnCount".to_string()),
        ],
        kind: OperationKind::ObjectReplace {
          od: Value::Number(serde_json::Number::from(view.frozen_column_count.unwrap_or(0) as i64)),
          oi: Value::Number(serde_json::Number::from(count as i64)),
        },
      },
    }))
  }

  /**
   * add record to table
   */
  pub fn add_record_to_action(
    snapshot: DatasheetSnapshotSO,
    payload: PayloadAddRecordVO,
  ) -> anyhow::Result<Option<Vec<ActionOTO>>> {
    let record_map = snapshot.record_map;
    let views = snapshot.meta.views;
    let mut record = payload.record;
    let index = payload.index;
    let _view_id = payload.view_id;
    let tmp: Option<&mut Map<String, Value>> = record.as_object_mut();
    let record_hm: &mut Map<String, Value> = tmp.unwrap();
    let v = record_hm.get("id");
    let record_id = v.unwrap().as_str().unwrap();

    let mut actions: Vec<ActionOTO> = views.iter().enumerate().fold(Vec::new(), |mut pre, (view_index, cur)| {
      let mut row_index = cur.rows.as_ref().map_or(0, |rows| rows.len());
      if cur
        .rows
        .as_ref()
        .map_or(false, |rows| rows.iter().any(|row| &row.record_id == record_id))
      {
        return pre;
      }
      if cur.id.is_some() && cur.id.clone().unwrap() == _view_id {
        row_index = index;
      }
      let mut record: HashMap<String, Value> = HashMap::new();
      record.insert("recordId".to_owned(), Value::String((record_id).to_string()));
      pre.push(ActionOTO {
        op_name: "LI".to_string(),
        op: Operation {
          p: vec![
            PathSegment::String("meta".to_string()),
            PathSegment::String("views".to_string()),
            PathSegment::Number(view_index.clone()),
            PathSegment::String("rows".to_string()),
            PathSegment::Number(row_index.clone()),
          ],
          kind: OperationKind::ListInsert {
            li: to_value(&record).unwrap(),
          },
        },
      });

      pre
    });

    if !record_map.contains_key(record_id) {
      actions.push(ActionOTO {
        op_name: "OI".to_string(),
        op: Operation {
          p: vec![
            PathSegment::String("recordMap".to_string()),
            PathSegment::String(record_id.to_string()),
          ],
          kind: OperationKind::ObjectInsert { oi: record },
        },
      });
    }

    return Ok(Some(actions));
  }

  /**
   * delete record by record id
   * base on get_data_pack
   */
  pub fn delete_records(snapshot: DatasheetSnapshotSO, payload: DeleteRecordsOTO) -> anyhow::Result<Vec<ActionOTO>> {
    let record_map = &snapshot.record_map;
    let record_size = record_map.len();
    let views = &snapshot.meta.views;
    let field_map = &snapshot.meta.field_map;
    let DeleteRecordsOTO {
      record_ids,
      // get_field_by_field_id,
      // state,
    } = payload;
    let wait_delete_record_set: HashSet<String> = record_ids.iter().cloned().collect();
    // compensator.add_will_remove_records(record_ids);
    let mut actions: Vec<ActionOTO> = Vec::new();

    /*
     *
     * depends on records count to delete,  and the percent of delete count and total count to judge
     * delete one by one (ld) ,  or total replace(or)
     *
     * when the number of records to delete larger than 500,
     * or delete records / total records percent larger thant 50%,
     * and total records larger than 160,
     * then total replace
     * otherwise delete one by one
     *
     */

    // delete all rows in views
    let rate = wait_delete_record_set.len() as f64 / record_size as f64;
    if (rate > 0.5 && record_size > 100) || wait_delete_record_set.len() > 500 {
      actions = views
        .iter()
        .enumerate()
        .map(|(index, cur_view)| {
          let next_view_rows = cur_view
            .rows
            .clone()
            .unwrap()
            .iter()
            .filter(|row| !wait_delete_record_set.contains(&row.record_id))
            .cloned()
            .collect::<Vec<_>>();
          let json_str = serde_json::to_string(&next_view_rows).unwrap(); //String
          let json_value_oi: Value = serde_json::from_str(&json_str).unwrap();
          let json_str = serde_json::to_string(&cur_view.rows.clone().unwrap()).unwrap(); //String
          let json_value: Value = serde_json::from_str(&json_str).unwrap();
          ActionOTO {
            op_name: "OR".to_string(),
            op: Operation {
              p: vec![
                PathSegment::String("meta".to_string()),
                PathSegment::String("views".to_string()),
                PathSegment::Number(index),
                PathSegment::String("rows".to_string()),
              ],
              kind: OperationKind::ObjectReplace {
                od: json_value,
                oi: json_value_oi,
              },
            },
          }
        })
        .collect();
    } else {
      actions = views
        .iter()
        .enumerate()
        .flat_map(|(index, cur_view)| {
          let mut idx = 0;
          cur_view
            .rows
            .clone()
            .unwrap()
            .iter()
            .enumerate()
            .fold(Vec::new(), |mut collected, (_index, row)| {
              if wait_delete_record_set.contains(&row.record_id) {
                collected.push(DeleteRecordItem {
                  index: _index,
                  record_id: row.record_id.clone(),
                });
              }
              collected
            })
            .iter()
            .map(|item| {
              let action = ActionOTO {
                op_name: "LD".to_string(),
                op: Operation {
                  p: vec![
                    PathSegment::String("meta".to_string()),
                    PathSegment::String("views".to_string()),
                    PathSegment::Number(index),
                    PathSegment::String("rows".to_string()),
                    PathSegment::Number(item.index - idx),
                  ],
                  kind: OperationKind::ListDelete {
                    ld: to_value(RecordDelete {
                      record_id: item.record_id.clone(),
                    })
                    .unwrap(),
                  },
                },
              };
              idx = idx + 1;
              action
            })
            .collect::<Vec<_>>()
        })
        .collect();
    }

    for record_id in record_ids {
      if let Some(record) = record_map.get(&record_id) {
        let mut data = HashMap::new();
        let record_data = record.data.clone().as_object().unwrap().clone();
        for (k, v) in record_data {
          // let field = get_field_by_field_id(k);
          // if Field::bind_context(field, state).record_editable() {
          data.insert(k.clone(), v.clone());
          // }
        }
        let json_value = serde_json::to_value(&data).unwrap();
        let _record = RecordSO {
          data: json_value,
          ..record.clone()
        };

        // when delete date, remove alarm
        // TODO(kailang) ObjectDelete has did this
        for (field_id, field) in field_map.iter() {
          if field.kind == FieldKindSO::DateTime {
            if let Some(_alarm) = get_date_time_cell_alarm(&snapshot, &record_id, field_id) {
              let alarm_actions = DatasheetActions::set_date_time_cell_alarm(
                snapshot.clone(),
                RecordAlarmOTO {
                  record_id: record_id.to_string(),
                  field_id: field_id.to_string(),
                  alarm: None,
                },
              )
              .unwrap();
              if let Some(alarm_actions) = alarm_actions {
                alarm_actions.iter().for_each(|action| actions.push(action.clone()));
              }
            }
          }
        }

        let json_str = serde_json::to_string(&_record).unwrap(); //String
        let json_value: Value = serde_json::from_str(&json_str).unwrap();
        actions.push(ActionOTO {
          op_name: "OD".to_string(),
          op: Operation {
            p: vec![
              PathSegment::String("recordMap".to_string()),
              PathSegment::String(record_id.to_string()),
            ],
            kind: OperationKind::ObjectDelete { od: json_value },
          },
        })
      }
    }

    Ok(actions)
  }

  /**
   * set alarm by record
   * base on get_data_pack
   */
  pub fn set_date_time_cell_alarm(
    snapshot: DatasheetSnapshotSO,
    payload: RecordAlarmOTO,
  ) -> anyhow::Result<Option<Vec<ActionOTO>>> {
    let RecordAlarmOTO {
      record_id,
      field_id,
      alarm,
    } = payload;
    let old_alarm = get_date_time_cell_alarm(&snapshot, &record_id, &field_id);

    // new alarm
    if old_alarm.is_none() {
      let field_extra_map = snapshot
        .record_map
        .get(&record_id)
        .and_then(|record| record.record_meta.as_ref())
        .and_then(|meta| meta.field_extra_map.as_ref());

      // compensate snapshot fieldExtraMap default data
      let mut default_action: Option<ActionOTO> = None;

      let mut tmp = HashMap::new();
      let tmp_hashmap2 = json!({});
      tmp.insert(field_id.to_string(), to_value(&tmp_hashmap2).unwrap());
      // without fieldExtraMap
      if field_extra_map.is_none() {
        default_action = Some(ActionOTO {
          op_name: "OI".to_string(),
          op: Operation {
            p: vec![
              PathSegment::String("recordMap".to_string()),
              PathSegment::String(record_id.to_string()),
              PathSegment::String("recordMeta".to_string()),
              PathSegment::String("fieldExtraMap".to_string()),
            ],
            kind: OperationKind::ObjectInsert {
              oi: to_value(&tmp).unwrap(),
            },
          },
        })
      } else if field_extra_map.unwrap().get(&field_id).is_none() {
        default_action = Some(ActionOTO {
          op_name: "OI".to_string(),
          op: Operation {
            p: vec![
              PathSegment::String("recordMap".to_string()),
              PathSegment::String(record_id.to_string()),
              PathSegment::String("recordMeta".to_string()),
              PathSegment::String("fieldExtraMap".to_string()),
              PathSegment::String(field_id.to_string()),
            ],
            kind: OperationKind::ObjectInsert { oi: json!({}) },
          },
        })
      }

      let json_str = serde_json::to_string(&alarm).unwrap(); //String
      let json_value: Value = serde_json::from_str(&json_str).unwrap();
      let alarm_action = ActionOTO {
        op_name: "OI".to_string(),
        op: Operation {
          p: vec![
            PathSegment::String("recordMap".to_string()),
            PathSegment::String(record_id.to_string()),
            PathSegment::String("recordMeta".to_string()),
            PathSegment::String("fieldExtraMap".to_string()),
            PathSegment::String(field_id.to_string()),
            PathSegment::String("alarm".to_string()),
          ],
          kind: OperationKind::ObjectInsert { oi: json_value },
        },
      };

      return if let Some(default_action) = default_action {
        Ok(Some(vec![default_action, alarm_action]))
      } else {
        Ok(Some(vec![alarm_action]))
      };
    }

    if old_alarm == alarm {
      return Ok(None);
    }

    // delete alarm
    if alarm.is_none() {
      let json_str = serde_json::to_string(&old_alarm).unwrap(); //String
      let json_value: Value = serde_json::from_str(&json_str).unwrap();
      return Ok(Some(vec![ActionOTO {
        op_name: "OD".to_string(),
        op: Operation {
          p: vec![
            PathSegment::String("recordMap".to_string()),
            PathSegment::String(record_id),
            PathSegment::String("recordMeta".to_string()),
            PathSegment::String("fieldExtraMap".to_string()),
            PathSegment::String(field_id.to_string()),
            PathSegment::String("alarm".to_string()),
          ],
          kind: OperationKind::ObjectDelete { od: json_value },
        },
      }]));
    }

    /*
     * found a situation, alarm.alarmUsers length equals 0,
     * TODO: wait for debug, here just place the check
     */
    let users = alarm.clone().unwrap().alarm_users;
    if let Some(users) = users {
      if users.is_empty() {
        return Ok(None);
      }
    }

    // edit alarm
    let json_str = serde_json::to_string(&alarm).unwrap(); //String
    let json_value_oi: Value = serde_json::from_str(&json_str).unwrap();
    let json_str = serde_json::to_string(&old_alarm).unwrap(); //String
    let json_value: Value = serde_json::from_str(&json_str).unwrap();
    Ok(Some(vec![ActionOTO {
      op_name: "OR".to_string(),
      op: Operation {
        p: vec![
          PathSegment::String("recordMap".to_string()),
          PathSegment::String(record_id),
          PathSegment::String("recordMeta".to_string()),
          PathSegment::String("fieldExtraMap".to_string()),
          PathSegment::String(field_id.to_string()),
          PathSegment::String("alarm".to_string()),
        ],
        kind: OperationKind::ObjectReplace {
          od: json_value,
          oi: json_value_oi,
        },
      },
    }]))
  }

  pub fn move_row_to_action(snapshot: DatasheetSnapshotSO, payload: MoveRowOTO) -> anyhow::Result<Option<ActionOTO>> {
    let MoveRowOTO {
      record_id,
      target,
      view_id,
    } = payload;

    let view_index = get_view_index(snapshot.clone(), view_id);
    if view_index.is_none() {
      return Ok(None);
    }
    let view_index = view_index.unwrap();
    let view = &snapshot.meta.views[view_index];

    let record_index = view
      .rows
      .clone()
      .unwrap()
      .iter()
      .position(|row| row.record_id == record_id);
    if record_index.is_none() {
      return Ok(None);
    }

    Ok(Some(ActionOTO {
      op_name: "LM".to_string(),
      op: Operation {
        p: vec![
          PathSegment::String("meta".to_string()),
          PathSegment::String("views".to_string()),
          PathSegment::Number(view_index),
          PathSegment::String("rows".to_string()),
          PathSegment::Number(record_index.unwrap()),
        ],
        kind: OperationKind::ListMove { lm: target },
      },
    }))
  }

  pub fn set_view_sort_to_action(
    // state: &IReduxState,
    snapshot: DatasheetSnapshotSO,
    payload: ViewSortOTO,
  ) -> anyhow::Result<Option<Vec<ActionOTO>>> {
    let ViewSortOTO {
      view_id,
      sort_info,
      apply_sort,
    } = payload;
    let view_index = get_view_index(snapshot.clone(), view_id);
    if view_index.is_none() {
      return Ok(None);
    }
    let view_index = view_index.unwrap();
    let view = &snapshot.meta.views[view_index];

    // when clear sorts, delete sort field directly
    if sort_info.is_none() {
      let json_str = serde_json::to_string(&view.sort_info.clone().unwrap()).unwrap(); //String
      let json_value: Value = serde_json::from_str(&json_str).unwrap();
      return Ok(Some(vec![ActionOTO {
        op_name: "OD".to_string(),
        op: Operation {
          p: vec![
            PathSegment::String("meta".to_string()),
            PathSegment::String("views".to_string()),
            PathSegment::Number(view_index),
            PathSegment::String("sortInfo".to_string()),
          ],
          kind: OperationKind::ObjectDelete { od: json_value },
        },
      }]));
    }

    let json_str = serde_json::to_string(&sort_info).unwrap(); //String
    let json_value_oi: Value = serde_json::from_str(&json_str).unwrap();
    let json_str = serde_json::to_string(&view.sort_info).unwrap(); //String
    let json_value: Value = serde_json::from_str(&json_str).unwrap();
    if apply_sort {
      // sort method will mutate the array, so here duplicate the array first
      // let rows = sort_rows_by_sort_info(state, &view.rows, &sort_info.rules, snapshot);
      let rows = sort_rows_by_sort_info(&view.rows, &sort_info.unwrap().rules, &snapshot);

      let json_str = serde_json::to_string(&rows).unwrap(); //String
      let json_value_oi_rows: Value = serde_json::from_str(&json_str).unwrap();
      let json_str = serde_json::to_string(&view.rows.clone().unwrap()).unwrap(); //String
      let json_value_rows: Value = serde_json::from_str(&json_str).unwrap();
      return Ok(Some(vec![
        ActionOTO {
          op_name: "OR".to_string(),
          op: Operation {
            p: vec![
              PathSegment::String("meta".to_string()),
              PathSegment::String("views".to_string()),
              PathSegment::Number(view_index),
              PathSegment::String("sortInfo".to_string()),
            ],
            kind: OperationKind::ObjectReplace {
              od: json_value,
              oi: json_value_oi,
            },
          },
        },
        ActionOTO {
          op_name: "OR".to_string(),
          op: Operation {
            p: vec![
              PathSegment::String("meta".to_string()),
              PathSegment::String("views".to_string()),
              PathSegment::Number(view_index),
              PathSegment::String("rows".to_string()),
            ],
            kind: OperationKind::ObjectReplace {
              od: json_value_rows,
              oi: json_value_oi_rows,
            },
          },
        },
      ]));
    }

    Ok(Some(vec![ActionOTO {
      op_name: "OR".to_string(),
      op: Operation {
        p: vec![
          PathSegment::String("meta".to_string()),
          PathSegment::String("views".to_string()),
          PathSegment::Number(view_index),
          PathSegment::String("sortInfo".to_string()),
        ],
        kind: OperationKind::ObjectReplace {
          od: json_value,
          oi: json_value_oi,
        },
      },
    }]))
  }

  /**
   * update Field, replace field directly
   */
  pub fn set_field_attr_to_action(
    snapshot: DatasheetSnapshotSO,
    payload: FieldOTO,
  ) -> anyhow::Result<Option<ActionOTO>> {
    let field_map = &snapshot.meta.field_map;
    let field = &payload.field;
    let tmp = field_map.get(&field.id).unwrap();
    if !field_map.contains_key(&field.id) || field == tmp {
      return Ok(None);
    }

    let json_str = serde_json::to_string(&field).unwrap(); //String
    let json_value_oi: Value = serde_json::from_str(&json_str).unwrap();
    let json_str = serde_json::to_string(tmp).unwrap(); //String
    let json_value: Value = serde_json::from_str(&json_str).unwrap();
    Ok(Some(ActionOTO {
      op_name: "OR".to_string(),
      op: Operation {
        p: vec![
          PathSegment::String("meta".to_string()),
          PathSegment::String("fieldMap".to_string()),
          PathSegment::String(field.id.to_string()),
        ],
        kind: OperationKind::ObjectReplace {
          od: json_value,
          oi: json_value_oi,
        },
      },
    }))
  }

  /**
   * set view filter  filterInfo
   */
  pub fn set_filter_info_to_action(
    snapshot: DatasheetSnapshotSO,
    payload: SetFilterInfoOTO,
  ) -> anyhow::Result<Option<ActionOTO>> {
    let view_id = payload.view_id;
    let mut filter_info = payload.filter_info;

    let view_index = get_view_index(snapshot.clone(), view_id);
    if view_index.is_none() {
      return Ok(None);
    }
    let view_index = view_index.unwrap();
    let view = &snapshot.meta.views[view_index];

    if !validate_filter_info(&filter_info) {
      println!("illegal filter condition!");
      filter_info = None;
    }

    if view.filter_info == filter_info {
      return Ok(None);
    }

    let json_str = serde_json::to_string(&view.filter_info.clone().unwrap()).unwrap(); //String
    let json_value: Value = serde_json::from_str(&json_str).unwrap();
    // when clear filter, delete filter field directly
    if filter_info.is_none() {
      return Ok(Some(ActionOTO {
        op_name: "OD".to_string(),
        op: Operation {
          p: vec![
            PathSegment::String("meta".to_string()),
            PathSegment::String("views".to_string()),
            PathSegment::Number(view_index),
            PathSegment::String("filterInfo".to_string()),
          ],
          kind: OperationKind::ObjectDelete { od: json_value },
        },
      }));
    }

    let json_str = serde_json::to_string(&filter_info.unwrap()).unwrap(); //String
    let json_value_oi: Value = serde_json::from_str(&json_str).unwrap();
    Ok(Some(ActionOTO {
      op_name: "OR".to_string(),
      op: Operation {
        p: vec![
          PathSegment::String("meta".to_string()),
          PathSegment::String("views".to_string()),
          PathSegment::Number(view_index),
          PathSegment::String("filterInfo".to_string()),
        ],
        kind: OperationKind::ObjectReplace {
          od: json_value,
          oi: json_value_oi,
        },
      },
    }))
  }

  pub fn set_view_lock_info_to_action(
    snapshot: DatasheetSnapshotSO,
    payload: SetViewLockInfoOTO,
  ) -> anyhow::Result<Option<ActionOTO>> {
    let view_id = payload.view_id;
    let view_lock = payload.view_lock_info;
    let view_index = get_view_index(snapshot.clone(), view_id);
    if view_index.is_none() {
      return Ok(None);
    }
    let view_index = view_index.unwrap();
    let view = &snapshot.meta.views[view_index];

    if view_lock.is_none() {
      let json_str = serde_json::to_string(&view.lock_info.clone().unwrap()).unwrap(); //String
      let json_value: Value = serde_json::from_str(&json_str).unwrap();
      return Ok(Some(ActionOTO {
        op_name: "OD".to_string(),
        op: Operation {
          p: vec![
            PathSegment::String("meta".to_string()),
            PathSegment::String("views".to_string()),
            PathSegment::Number(view_index),
            PathSegment::String("lockInfo".to_string()),
          ],
          kind: OperationKind::ObjectDelete { od: json_value },
        },
      }));
    }
    let json_str = serde_json::to_string(&view_lock.unwrap()).unwrap(); //String
    let json_value: Value = serde_json::from_str(&json_str).unwrap();
    Ok(Some(ActionOTO {
      op_name: "OI".to_string(),
      op: Operation {
        p: vec![
          PathSegment::String("meta".to_string()),
          PathSegment::String("views".to_string()),
          PathSegment::Number(view_index),
          PathSegment::String("lockInfo".to_string()),
        ],
        kind: OperationKind::ObjectInsert { oi: json_value },
      },
    }))
  }

  pub fn set_group_info_field_to_action(
    snapshot: DatasheetSnapshotSO,
    payload: SetGroupInfoFieldOTO,
  ) -> anyhow::Result<Option<ActionOTO>> {
    let view_id = payload.view_id;
    let group_info = &payload.group_info;
    let view_index = get_view_index(snapshot.clone(), view_id);
    if view_index.is_none() {
      return Ok(None);
    }
    let view_index = view_index.unwrap();
    let view = &snapshot.meta.views[view_index];
    if view.group_info == *group_info {
      return Ok(None);
    }

    // compensator.set_last_group_info_if_null(view.group_info);

    let json_str = serde_json::to_string(&view.group_info.clone().unwrap()).unwrap(); //String
    let json_value_od: Value = serde_json::from_str(&json_str).unwrap();
    // when clear grouping, delete grouping field directly
    if group_info.is_none() {
      return Ok(Some(ActionOTO {
        op_name: "OD".to_string(),
        op: Operation {
          p: vec![
            PathSegment::String("meta".to_string()),
            PathSegment::String("views".to_string()),
            PathSegment::Number(view_index),
            PathSegment::String("groupInfo".to_string()),
          ],
          kind: OperationKind::ObjectDelete { od: json_value_od },
        },
      }));
    }

    let json_str = serde_json::to_string(&group_info.clone().unwrap()).unwrap(); //String
    let json_value: Value = serde_json::from_str(&json_str).unwrap();
    Ok(Some(ActionOTO {
      op_name: "OR".to_string(),
      op: Operation {
        p: vec![
          PathSegment::String("meta".to_string()),
          PathSegment::String("views".to_string()),
          PathSegment::Number(view_index),
          PathSegment::String("groupInfo".to_string()),
        ],
        kind: OperationKind::ObjectReplace {
          od: json_value_od,
          oi: json_value,
        },
      },
    }))
  }

  // generate new comment's action
  pub fn insert_comment_to_action(
    // state: &IReduxState,
    snapshot: DatasheetSnapshotSO,
    options: InsertCommentOTO,
  ) -> anyhow::Result<Option<Vec<ActionOTO>>> {
    let InsertCommentOTO {
      datasheet_id: _,
      record_id,
      insert_comments,
    } = options;
    // let record_map = Selectors::get_snapshot(state, datasheet_id)?.record_map;
    let record_map = snapshot.record_map;
    let record = record_map.get(&record_id).unwrap();
    let comments = &record.comments.clone().unwrap_or_default();

    if let Some(insert_comments) = insert_comments {
      let json_str = serde_json::to_string(&insert_comments[0]).unwrap(); //String
      let json_value: Value = serde_json::from_str(&json_str).unwrap();
      Ok(Some(vec![
        ActionOTO {
          op_name: "OI".to_string(),
          op: Operation {
            p: vec![
              PathSegment::String("recordMap".to_string()),
              PathSegment::String(record_id.to_string()),
              PathSegment::String("comments".to_string()),
              PathSegment::Number(comments.len() as usize),
            ],
            kind: OperationKind::ListInsert { li: json_value },
          },
        },
        ActionOTO {
          op_name: "NA".to_string(),
          op: Operation {
            p: vec![
              PathSegment::String("recordMap".to_string()),
              PathSegment::String(record_id.to_string()),
              PathSegment::String("commentCount".to_string()),
            ],
            kind: OperationKind::NumberAdd { na: 1.0 },
          },
        },
      ]))
    } else {
      Ok(None)
    }
  }

  pub fn update_comment_to_action(options: UpdateCommentOTO) -> anyhow::Result<Option<Vec<ActionOTO>>> {
    let UpdateCommentOTO {
      datasheet_id: _,
      record_id,
      update_comments,
      emoji_action,
    } = options;
    let mut actions = Vec::new();

    let json_str = serde_json::to_string(&update_comments[0]).unwrap(); //String
    let json_value: Value = serde_json::from_str(&json_str).unwrap();
    if emoji_action.is_some() {
      // new
      actions.push(ActionOTO {
        op_name: "LI".to_string(),
        op: Operation {
          p: vec![
            PathSegment::String("recordMap".to_string()),
            PathSegment::String(record_id.to_string()),
            PathSegment::String("comments".to_string()),
            PathSegment::String("emojis".to_string()),
          ],
          kind: OperationKind::ListInsert { li: json_value },
        },
      })
    } else {
      // cancel
      actions.push(ActionOTO {
        op_name: "LD".to_string(),
        op: Operation {
          p: vec![
            PathSegment::String("recordMap".to_string()),
            PathSegment::String(record_id.to_string()),
            PathSegment::String("comments".to_string()),
            PathSegment::String("emojis".to_string()),
          ],
          kind: OperationKind::ListDelete { ld: json_value },
        },
      })
    }
    Ok(Some(actions))
  }

  pub fn delete_comment_to_action(options: DeleteCommentOTO) -> anyhow::Result<Vec<ActionOTO>> {
    let DeleteCommentOTO {
      datasheet_id: _,
      record_id,
      comments,
    } = options;
    let mut actions: Vec<ActionOTO> = Vec::new();
    let json_str = serde_json::to_string(&comments[0]).unwrap(); //String
    let json_value: Value = serde_json::from_str(&json_str).unwrap();
    actions.push(ActionOTO {
      op_name: "LD".to_string(),
      op: Operation {
        p: vec![
          PathSegment::String("recordMap".to_string()),
          PathSegment::String(record_id.to_string()),
          PathSegment::String("comments".to_string()),
          PathSegment::Number(0),
        ],
        kind: OperationKind::ListDelete { ld: json_value },
      },
    });
    actions.push(ActionOTO {
      op_name: "NA".to_string(),
      op: Operation {
        p: vec![
          PathSegment::String("recordMap".to_string()),
          PathSegment::String(record_id.to_string()),
          PathSegment::String("commentCount".to_string()),
        ],
        kind: OperationKind::NumberAdd { na: -1.0 },
      },
    });
    Ok(actions)
  }

  pub fn delete_widget_panel_to_action(
    // state: &IReduxState,
    panel_id: String,
    widget_panels: &Vec<WidgetPanelSO>,
    resource_type: ResourceType,
  ) -> anyhow::Result<Option<Vec<ActionOTO>>> {
    if widget_panels.is_empty() {
      return Ok(None);
    }

    let index = widget_panels.iter().position(|item| item.id == panel_id);

    if let Some(index) = index {
      let json_str = serde_json::to_string(&widget_panels[index]).unwrap(); //String
      let json_value: Value = serde_json::from_str(&json_str).unwrap();
      if resource_type == ResourceType::Mirror {
        return Ok(Some(vec![ActionOTO {
          op_name: "LD".to_string(),
          op: Operation {
            p: vec![
              PathSegment::String("widgetPanels".to_string()),
              PathSegment::Number(index),
            ],
            kind: OperationKind::ListDelete { ld: json_value },
          },
        }]));
      } else {
        return Ok(Some(vec![ActionOTO {
          op_name: "LD".to_string(),
          op: Operation {
            p: vec![
              PathSegment::String("meta".to_string()),
              PathSegment::String("widgetPanels".to_string()),
              PathSegment::Number(index),
            ],
            kind: OperationKind::ListDelete { ld: json_value },
          },
        }]));
      }
    }

    Ok(None)
  }

  pub fn move_panel_to_action(
    target_index: usize,
    source_index: usize,
    resource_type: ResourceType,
  ) -> anyhow::Result<Option<Vec<ActionOTO>>> {
    if resource_type == ResourceType::Mirror {
      Ok(Some(vec![ActionOTO {
        op_name: "LM".to_string(),
        op: Operation {
          p: vec![
            PathSegment::String("widgetPanels".to_string()),
            PathSegment::Number(source_index),
          ],
          kind: OperationKind::ListMove { lm: target_index },
        },
      }]))
    } else {
      Ok(Some(vec![ActionOTO {
        op_name: "LM".to_string(),
        op: Operation {
          p: vec![
            PathSegment::String("meta".to_string()),
            PathSegment::String("widgetPanels".to_string()),
            PathSegment::Number(source_index),
          ],
          kind: OperationKind::ListMove { lm: target_index },
        },
      }]))
    }
  }

  pub fn add_widget_panel_to_action(
    snapshot: DatasheetSnapshotSO,
    panel: WidgetPanelSO,
  ) -> anyhow::Result<Option<Vec<ActionOTO>>> {
    let widget_panels = &snapshot.meta.widget_panels;

    let json_str = serde_json::to_string(&panel).unwrap(); //String
    let json_value: Value = serde_json::from_str(&json_str).unwrap();
    if widget_panels.is_none() {
      return Ok(Some(vec![ActionOTO {
        op_name: "OI".to_string(),
        op: Operation {
          p: vec![
            PathSegment::String("meta".to_string()),
            PathSegment::String("widgetPanels".to_string()),
          ],
          kind: OperationKind::ListInsert { li: json_value },
        },
      }]));
    }

    Ok(Some(vec![ActionOTO {
      op_name: "LI".to_string(),
      op: Operation {
        p: vec![
          PathSegment::String("meta".to_string()),
          PathSegment::String("widgetPanels".to_string()),
          PathSegment::Number(widget_panels.clone().unwrap().len() + 1),
        ],
        kind: OperationKind::ListInsert { li: json_value },
      },
    }]))
  }

  pub fn add_widget_panel_with_mirror_to_action(
    snapshot: MirrorSnapshot,
    panel: WidgetPanelSO,
  ) -> anyhow::Result<Option<Vec<ActionOTO>>> {
    let widget_panels = &snapshot.widget_panels;
    let json_str = serde_json::to_string(&panel).unwrap(); //String
    let json_value: Value = serde_json::from_str(&json_str).unwrap();
    if widget_panels.is_none() {
      return Ok(Some(vec![ActionOTO {
        op_name: "OI".to_string(),
        op: Operation {
          p: vec![PathSegment::String("widgetPanels".to_string())],
          kind: OperationKind::ListInsert { li: json_value },
        },
      }]));
    }

    Ok(Some(vec![ActionOTO {
      op_name: "LI".to_string(),
      op: Operation {
        p: vec![
          PathSegment::String("widgetPanels".to_string()),
          PathSegment::Number(widget_panels.clone().unwrap().len() + 1),
        ],
        kind: OperationKind::ListInsert { li: json_value },
      },
    }]))
  }

  pub fn modify_panel_name_to_action(
    // _state: &IReduxState,
    new_panel: WidgetPanelSO,
    widget_panels: Vec<WidgetPanelSO>,
    resource_type: ResourceType,
  ) -> anyhow::Result<Option<Vec<ActionOTO>>> {
    if widget_panels.is_empty() {
      return Ok(None);
    }

    let index = widget_panels.iter().position(|item| item.id == new_panel.id);
    if index.is_none() {
      return Ok(None);
    }
    let index = index.unwrap();
    let od_name = Value::String(widget_panels[index].name.clone().unwrap().clone());
    let oi_name = Value::String(new_panel.name.unwrap().clone());
    if resource_type == ResourceType::Mirror {
      Ok(Some(vec![ActionOTO {
        op_name: "OR".to_string(),
        op: Operation {
          p: vec![
            PathSegment::String("widgetPanels".to_string()),
            PathSegment::Number(index),
            PathSegment::String("name".to_string()),
          ],
          kind: OperationKind::ObjectReplace {
            od: od_name,
            oi: oi_name,
          },
        },
      }]))
    } else {
      Ok(Some(vec![ActionOTO {
        op_name: "OR".to_string(),
        op: Operation {
          p: vec![
            PathSegment::String("meta".to_string()),
            PathSegment::String("widgetPanels".to_string()),
            PathSegment::Number(index),
            PathSegment::String("name".to_string()),
          ],
          kind: OperationKind::ObjectReplace {
            od: od_name,
            oi: oi_name,
          },
        },
      }]))
    }
  }

  pub fn add_widget_to_panel_to_action(
    // _state: &IReduxState,
    installation_index: usize,
    panel_index: usize,
    widget_id: &str,
  ) -> anyhow::Result<Option<Vec<ActionOTO>>> {
    let new_widget = WidgetOTO {
      id: widget_id.to_string(),
      height: 6.2,
      y: MAX_SAFE_INTEGER,
    };

    let json_str = serde_json::to_string(&new_widget).unwrap(); //String
    let json_value: Value = serde_json::from_str(&json_str).unwrap();
    Ok(Some(vec![ActionOTO {
      op_name: "LI".to_string(),
      op: Operation {
        p: vec![
          PathSegment::String("meta".to_string()),
          PathSegment::String("widgetPanels".to_string()),
          PathSegment::Number(panel_index),
          PathSegment::String("widgets".to_string()),
          PathSegment::Number(installation_index),
        ],
        kind: OperationKind::ListInsert { li: json_value },
      },
    }]))
  }

  pub fn add_widget_to_panel_with_mirror_to_action(
    // _state: &IReduxState,
    installation_index: usize,
    panel_index: usize,
    widget_id: &str,
  ) -> anyhow::Result<Option<Vec<ActionOTO>>> {
    let new_widget = WidgetOTO {
      id: widget_id.to_string(),
      height: 6.2,
      y: MAX_SAFE_INTEGER,
    };

    let json_str = serde_json::to_string(&new_widget).unwrap(); //String
    let json_value: Value = serde_json::from_str(&json_str).unwrap();
    Ok(Some(vec![ActionOTO {
      op_name: "LI".to_string(),
      op: Operation {
        p: vec![
          PathSegment::String("widgetPanels".to_string()),
          PathSegment::Number(panel_index),
          PathSegment::String("widgets".to_string()),
          PathSegment::Number(installation_index),
        ],
        kind: OperationKind::ListInsert { li: json_value },
      },
    }]))
  }

  pub fn delete_widget_to_action(
    // _state: &IReduxState,
    options: DeleteWidgetOTO,
  ) -> anyhow::Result<Vec<ActionOTO>> {
    let DeleteWidgetOTO {
      widget_panel_index,
      widget,
      widget_index,
    } = options;

    let json_str = serde_json::to_string(&widget).unwrap(); //String
    let json_value: Value = serde_json::from_str(&json_str).unwrap();
    Ok(vec![ActionOTO {
      op_name: "LD".to_string(),
      op: Operation {
        p: vec![
          PathSegment::String("meta".to_string()),
          PathSegment::String("widgetPanels".to_string()),
          PathSegment::Number(widget_panel_index),
          PathSegment::String("widgets".to_string()),
          PathSegment::Number(widget_index),
        ],
        kind: OperationKind::ListDelete { ld: json_value },
      },
    }])
  }

  pub fn delete_mirror_widget_to_action(
    // _state: &IReduxState,
    options: DeleteWidgetOTO,
  ) -> anyhow::Result<Vec<ActionOTO>> {
    let DeleteWidgetOTO {
      widget_panel_index,
      widget_index,
      widget,
    } = options;

    let json_str = serde_json::to_string(&widget).unwrap(); //String
    let json_value: Value = serde_json::from_str(&json_str).unwrap();
    Ok(vec![ActionOTO {
      op_name: "LD".to_string(),
      op: Operation {
        p: vec![
          PathSegment::String("widgetPanels".to_string()),
          PathSegment::Number(widget_panel_index),
          PathSegment::String("widgets".to_string()),
          PathSegment::Number(widget_index),
        ],
        kind: OperationKind::ListDelete { ld: json_value },
      },
    }])
  }

  pub fn change_widget_height_to_action(
    // state: &IReduxState,
    snapshot: DatasheetSnapshotSO,
    options: ChangeWidgetHeightOTO,
  ) -> anyhow::Result<Option<Vec<ActionOTO>>> {
    let ChangeWidgetHeightOTO {
      widget_panel_index,
      widget_index,
      widget_height,
      resource_id,
      resource_type,
    } = options;

    let active_widget_panel = get_resource_active_widget_panel(&snapshot, &resource_id, &resource_type).unwrap();

    let widget = active_widget_panel.widgets.get(widget_index).unwrap();

    let str_widget_panel_index = widget_panel_index.to_string();
    let str_widget_panel_index = str_widget_panel_index.as_str();
    let str_widget_index = widget_index.to_string();
    let str_widget_index = str_widget_index.as_str();
    let path = if resource_type == ResourceType::Datasheet {
      vec![
        "meta",
        "widgetPanels",
        str_widget_panel_index,
        "widgets",
        str_widget_index,
        "height",
      ]
    } else {
      vec![
        "widgetPanels",
        str_widget_panel_index,
        "widgets",
        str_widget_index,
        "height",
      ]
    };

    Ok(Some(vec![ActionOTO {
      op_name: "OR".to_string(),
      op: Operation {
        p: path.iter().map(|item| PathSegment::String(item.to_string())).collect(),
        kind: OperationKind::ObjectReplace {
          od: Value::Number(serde_json::Number::from_f64(widget.height).unwrap()),
          oi: Value::Number(serde_json::Number::from_f64(widget_height as f64).unwrap()),
        },
      },
    }]))
  }

  pub fn move_widget_to_action(
    // state: &IReduxState,
    snapshot: DatasheetSnapshotSO,
    options: MoveWidgetOTO,
  ) -> anyhow::Result<Option<Vec<ActionOTO>>> {
    let MoveWidgetOTO {
      widget_panel_index,
      layout,
      resource_type,
      resource_id,
    } = options;
    let widget_panel = get_resource_widget_panels(&snapshot, &resource_id, &resource_type);
    let old_layout = widget_panel.unwrap().get(widget_panel_index).unwrap().widgets.clone();
    if layout.len() != old_layout.len() {
      return Ok(None);
    }

    let mut actions: Vec<ActionOTO> = Vec::new();

    let get_path = |widget_panel_index: usize, index: usize, key: &str| {
      let str_widget_panel_index = widget_panel_index.to_string();
      let str_index = index.to_string();
      let base_path = vec!["widgetPanels", &str_widget_panel_index, "widgets", &str_index, key];
      if resource_type == ResourceType::Mirror {
        base_path
          .iter()
          .map(|item| PathSegment::String(item.to_string()))
          .collect()
      } else {
        let mut tmp = base_path.clone();
        tmp.insert(0, "meta");
        tmp.iter().map(|item| PathSegment::String(item.to_string())).collect()
      }
    };

    for (index, old_position) in old_layout.iter().enumerate() {
      let new_position = &layout[index];
      for (k, new_value) in new_position.iter() {
        // let old_value = old_position.get(k);
        let old_value = match k.as_str() {
          "id" => Value::String(old_position.id.clone()),
          "height" => Value::Number(serde_json::Number::from_f64(old_position.height).unwrap()),
          "y" => Value::Number(serde_json::Number::from_f64(old_position.y.unwrap()).unwrap()),
          _ => Value::String(old_position.id.clone()),
        };
        if old_value != *new_value {
          // compatible with the old mini program panel, set the y coordinate attribute for the first time, use oi
          let is_first_set_y = k == "y" && old_value.is_null();
          let replace_action = ActionOTO {
            op_name: "OR".to_string(),
            op: Operation {
              p: get_path(widget_panel_index, index, k),
              kind: OperationKind::ObjectReplace {
                oi: new_value.clone(),
                od: old_value,
              },
            },
          };
          let insert_action = ActionOTO {
            op_name: "OI".to_string(),
            op: Operation {
              p: get_path(widget_panel_index, index, k),
              kind: OperationKind::ObjectInsert { oi: new_value.clone() },
            },
          };
          actions.push(if is_first_set_y { insert_action } else { replace_action });
        }
      }
    }
    Ok(Some(actions))
  }

  pub fn manual_save_view_to_action(
    snapshot: DatasheetSnapshotSO,
    payload: ViewOTO,
  ) -> anyhow::Result<Option<Vec<ActionOTO>>> {
    let ViewOTO { view_id, view_property } = payload;
    let view_index = get_view_index(snapshot.clone(), view_id);
    if view_index.is_none() {
      return Ok(None);
    }
    let view_index = view_index.unwrap();
    let local_view = &snapshot.meta.views[view_index];
    let mut action = Vec::new();

    let server_view = view_property;

    let json_string = serde_json::to_string(&server_view).unwrap();
    let server_view_hashmap: HashMap<&str, Value> = serde_json::from_str(&json_string).unwrap();
    let json_string = serde_json::to_string(&local_view).unwrap();
    let local_view_hashmap: HashMap<&str, Value> = serde_json::from_str(&json_string).unwrap();

    let integration_view = local_view_hashmap
      .iter()
      .filter(|(key, _)| !ViewPropertyFilter::IGNORE_VIEW_PROPERTY.contains(key))
      .collect::<HashMap<_, _>>();

    for (key, _value) in integration_view {
      if local_view_hashmap.get(key) == server_view_hashmap.get(key) {
        continue;
      }
      if local_view_hashmap.contains_key(key) && server_view_hashmap.contains_key(key) {
        let json_string = serde_json::to_string(&local_view_hashmap.get(key).unwrap()).unwrap();
        let json_value_oi: Value = serde_json::from_str(&json_string).unwrap();
        let json_string = serde_json::to_string(&server_view_hashmap.get(key).unwrap()).unwrap();
        let json_value: Value = serde_json::from_str(&json_string).unwrap();
        action.push(ActionOTO {
          op_name: "OR".to_string(),
          op: Operation {
            p: vec![
              PathSegment::String("meta".to_string()),
              PathSegment::String("views".to_string()),
              PathSegment::Number(view_index),
              PathSegment::String(key.to_string()),
            ],
            kind: OperationKind::ObjectReplace {
              oi: json_value_oi,
              od: json_value,
            },
          },
        })
      }
      if local_view_hashmap.contains_key(key) && !server_view_hashmap.contains_key(key) {
        let json_string = serde_json::to_string(&local_view_hashmap.get(key).unwrap()).unwrap();
        let json_value_oi: Value = serde_json::from_str(&json_string).unwrap();
        action.push(ActionOTO {
          op_name: "OI".to_string(),
          op: Operation {
            p: vec![
              PathSegment::String("meta".to_string()),
              PathSegment::String("views".to_string()),
              PathSegment::Number(view_index),
              PathSegment::String(key.to_string()),
            ],
            kind: OperationKind::ObjectInsert { oi: json_value_oi },
          },
        });
      }
      if !local_view_hashmap.contains_key(key) && server_view_hashmap.contains_key(key) {
        let json_string = serde_json::to_string(&server_view_hashmap.get(key).unwrap()).unwrap();
        let json_value: Value = serde_json::from_str(&json_string).unwrap();
        action.push(ActionOTO {
          op_name: "OD".to_string(),
          op: Operation {
            p: vec![
              PathSegment::String("meta".to_string()),
              PathSegment::String("views".to_string()),
              PathSegment::Number(view_index),
              PathSegment::String(key.to_string()),
            ],
            kind: OperationKind::ObjectDelete { od: json_value },
          },
        });
      }
    }

    Ok(Some(action))
  }

  pub fn reset_view_to_action(
    snapshot: DatasheetSnapshotSO,
    payload: ViewOTO,
  ) -> anyhow::Result<Option<Vec<ActionOTO>>> {
    let ViewOTO { view_id, view_property } = payload;
    let view_index = get_view_index(snapshot.clone(), view_id);
    if view_index.is_none() {
      return Ok(None);
    }
    let view_index = view_index.unwrap();
    let local_view = &snapshot.meta.views[view_index];
    let mut action = Vec::new();

    let server_view = view_property;

    let json_string = serde_json::to_string(&server_view).unwrap();
    let server_view_hashmap: HashMap<&str, Value> = serde_json::from_str(&json_string).unwrap();
    let json_string = serde_json::to_string(&local_view).unwrap();
    let local_view_hashmap: HashMap<&str, Value> = serde_json::from_str(&json_string).unwrap();

    let integration_view = local_view_hashmap
      .iter()
      .filter(|(key, _)| !ViewPropertyFilter::IGNORE_VIEW_PROPERTY.contains(key))
      .collect::<HashMap<_, _>>();

    for (key, _value) in integration_view {
      if local_view_hashmap.get(key) == server_view_hashmap.get(key) {
        continue;
      }
      if local_view_hashmap.contains_key(key) && server_view_hashmap.contains_key(key) {
        let json_string = serde_json::to_string(&server_view_hashmap.get(key).unwrap()).unwrap();
        let json_value_oi: Value = serde_json::from_str(&json_string).unwrap();
        let json_string = serde_json::to_string(&local_view_hashmap.get(key).unwrap()).unwrap();
        let json_value: Value = serde_json::from_str(&json_string).unwrap();
        action.push(ActionOTO {
          op_name: "OR".to_string(),
          op: Operation {
            p: vec![
              PathSegment::String("meta".to_string()),
              PathSegment::String("views".to_string()),
              PathSegment::Number(view_index),
              PathSegment::String(key.to_string()),
            ],
            kind: OperationKind::ObjectReplace {
              oi: json_value_oi,
              od: json_value,
            },
          },
        })
      }
      if !local_view_hashmap.contains_key(key) && server_view_hashmap.contains_key(key) {
        let json_string = serde_json::to_string(&server_view_hashmap.get(key).unwrap()).unwrap();
        let json_value_oi: Value = serde_json::from_str(&json_string).unwrap();
        action.push(ActionOTO {
          op_name: "OI".to_string(),
          op: Operation {
            p: vec![
              PathSegment::String("meta".to_string()),
              PathSegment::String("views".to_string()),
              PathSegment::Number(view_index),
              PathSegment::String(key.to_string()),
            ],
            kind: OperationKind::ObjectInsert { oi: json_value_oi },
          },
        });
      }
      if local_view_hashmap.contains_key(key) && !server_view_hashmap.contains_key(key) {
        let json_string = serde_json::to_string(&local_view_hashmap.get(key).unwrap()).unwrap();
        let json_value: Value = serde_json::from_str(&json_string).unwrap();
        action.push(ActionOTO {
          op_name: "OD".to_string(),
          op: Operation {
            p: vec![
              PathSegment::String("meta".to_string()),
              PathSegment::String("views".to_string()),
              PathSegment::Number(view_index),
              PathSegment::String(key.to_string()),
            ],
            kind: OperationKind::ObjectDelete { od: json_value },
          },
        });
      }
    }

    Ok(Some(action))
  }

  pub fn set_view_auto_save_to_action(
    snapshot: DatasheetSnapshotSO,
    payload: SetViewAutoSaveOTO,
  ) -> anyhow::Result<Option<ActionOTO>> {
    let SetViewAutoSaveOTO { view_id, auto_save } = payload;
    let view_index = get_view_index(snapshot.clone(), view_id);
    if view_index.is_none() {
      return Ok(None);
    }
    let view_index = view_index.unwrap();
    let view = &snapshot.meta.views[view_index];

    if view.auto_save.is_none() {
      return Ok(Some(ActionOTO {
        op_name: "OI".to_string(),
        op: Operation {
          p: vec![
            PathSegment::String("meta".to_string()),
            PathSegment::String("views".to_string()),
            PathSegment::Number(view_index),
            PathSegment::String("autoSave".to_string()),
          ],
          kind: OperationKind::ObjectInsert {
            oi: Value::Bool(auto_save),
          },
        },
      }));
    }

    Ok(Some(ActionOTO {
      op_name: "OR".to_string(),
      op: Operation {
        p: vec![
          PathSegment::String("meta".to_string()),
          PathSegment::String("views".to_string()),
          PathSegment::Number(view_index),
          PathSegment::String("autoSave".to_string()),
        ],
        kind: OperationKind::ObjectReplace {
          oi: Value::Bool(auto_save),
          od: Value::Bool(view.auto_save.unwrap()),
        },
      },
    }))
  }

  /**
   * edit single relation dstID
   *
   * @param snapshot
   * @param payload
   */
  pub fn change_one_way_link_dst_id_to_action(
    snapshot: DatasheetSnapshotSO,
    payload: ChangeOneWayLinkDstIdOTO,
  ) -> anyhow::Result<Option<ActionOTO>> {
    let ChangeOneWayLinkDstIdOTO { field_id, new_field } = payload;
    let field_map = &snapshot.meta.field_map;
    if !field_map.contains_key(&field_id) {
      return Ok(None);
    }
    let json_str = serde_json::to_string(&new_field).unwrap();
    let json_value_oi: Value = serde_json::from_str(&json_str).unwrap();
    let json_str = serde_json::to_string(&field_map[&field_id]).unwrap();
    let json_value: Value = serde_json::from_str(&json_str).unwrap();
    Ok(Some(ActionOTO {
      op_name: "OR".to_string(),
      op: Operation {
        p: vec![
          PathSegment::String("meta".to_string()),
          PathSegment::String("fieldMap".to_string()),
          PathSegment::String(field_id.to_string()),
        ],
        kind: OperationKind::ObjectReplace {
          oi: json_value_oi,
          od: json_value,
        },
      },
    }))
  }

  // fn get_cell_values_by_field_id(
  //   // state: &IReduxState,
  //   snapshot: &DatasheetSnapshotSO,
  //   field_id: &str,
  //   view: Option<&ViewSO>,
  //   is_entity: bool
  // ) -> Vec<CellValueSo> {
  //   let field_map = &snapshot.meta.field_map;
  //   let record_map = &snapshot.record_map;
  //   if !field_map.contains_key(field_id) {
  //       return vec![];
  //   }

  //   let mut cell_values: Vec<CellValueSo> = vec![];
  //   let mut record_ids = record_map.keys().cloned().collect::<Vec<_>>();
  //   if let Some(view) = view {
  //       match view.r#type.unwrap() {
  //           // ViewType::Grid | ViewType::Gantt => {
  //           1 | 6 => {
  //               record_ids = view.rows.clone().unwrap().iter().map(|row| row.record_id.clone()).collect();
  //           }
  //           _ => {}
  //       }
  //   }

  //   for record_id in record_ids {
  //       if let Some(record) = record_map.get(&record_id) {
  //           let cell_value = get_cell_value(
  //             snapshot.clone(),
  //             record.id.clone(),
  //             field_id.to_string(),
  //             None,
  //             None,
  //             None
  //           );
  //           if is_entity || cell_value.is_some() {
  //               cell_values.push(cell_value.unwrap());
  //           }
  //       }
  //   }
  //   cell_values
  // }

  pub fn get_default_new_record(
    // state: IReduxState,
    context: Rc<DatasheetPackContext>,
    snapshot: DatasheetSnapshotSO,
    record_id: String,
    view_id: Option<String>,
    _group_cell_values: Option<Vec<CellValueSo>>,
    _user_info: Option<UserInfoSO>,
  ) -> RecordSO {
    // let field_map = get_field_map(state, snapshot.datasheet_id);
    let field_map = snapshot.meta.field_map.clone();
    let mut record = RecordSO {
      id: record_id,
      // data: Value::Object(()),
      comment_count: 0,
      // comments: Some(vec![]),
      // record_meta: HashMap::new(),
      ..Default::default()
    };

    if field_map.is_empty() {
      return record;
    }
    for (_field_id, field) in field_map.iter() {
      let default_value = FieldFactory::create_field(field.clone(), context.clone()).default_value();
      if let Some(default_value) = default_value {
        let mut hm = HashMap::new();
        hm.insert(field.id.clone(), default_value);
        record.data = serde_json::to_value(hm).unwrap();
      }
    }

    if view_id.is_none() {
      return record;
    }
    let view = get_view_by_id(snapshot.clone(), view_id.unwrap());
    if view.is_none() {
      return record;
    }
    let view = view.unwrap();
    // let cur_mirror_id = state.page_params.mirror_id;
    let cur_mirror_id = Some("cur_mirror_id".to_string());

    let mut _group_info: Vec<ISortedField> = vec![];
    let filter_info: Option<IFilterInfo> = None;

    if let Some(_cur_mirror_id) = cur_mirror_id {
      // _group_info = get_active_view_group_info(state);
      // filter_info = get_filter_info(state);
    }
    if let Some(_group_info) = view.group_info {
      // let cur_group_info = get_group_info_with_permission(state, group_info, snapshot.datasheet_id);
      // _group_info = union_with(_group_info, cur_group_info, is_equal);
    }
    if _group_info.len() > 0 {
      // let data_by_group = get_default_new_record_data_by_group(_group_info, group_cell_values);
      // record.data.extend(data_by_group);
    }

    // let cur_filter_info = get_filter_info_except_invalid(state, snapshot.datasheet_id, view.filter_info);
    if let Some(_filter_info) = &filter_info {
      // if let Some(cur_filter_info) = cur_filter_info {
      //     let conjunction = cur_filter_info.conjunction;
      //     let conditions = cur_filter_info.conditions.extend(filter_info.conditions);
      //     filter_info = Some(IFilterInfo { conjunction, conditions });
      // } else {
      //     filter_info = Some(filter_info);
      // }
    }
    if let Some(_filter_info) = &filter_info {
      // let data_by_filter = get_default_new_record_data_by_filter(state, snapshot.datasheet_id, filter_info, field_map, user_info);
      // record.data.extend(data_by_filter);
    }

    record
  }

  pub(crate) fn get_cell_values_by_field_id(
    context: Rc<DatasheetPackContext>,
    snapshot: &DatasheetSnapshotSO,
    field_id: &str,
    is_entity: bool,
  ) -> Vec<CellValue> {
    let field_map = &snapshot.meta.field_map;
    let record_map = &snapshot.record_map;

    if !field_map.contains_key(field_id) {
      return vec![];
    }

    let mut cell_values = vec![];
    let record_ids = record_map.keys().cloned().collect::<Vec<_>>();

    for record_id in record_ids {
      if let Some(record) = record_map.get(&record_id) {
        let cell_value = get_cell_value(context.clone(), snapshot, &record.id, field_id);

        if is_entity || cell_value != CellValue::Null {
          cell_values.push(cell_value);
        }
      }
    }

    cell_values
  }
}
