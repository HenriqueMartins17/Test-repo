use std::{collections::HashMap, rc::Rc};

use crate::{ot::{commands::{CollaCommandDefExecuteResult, PasteSetRecordsOptions, ExecuteResult, LinkedActions, SetRecordOptions}, types::{ResourceType, ActionOTO}, get_view_by_id}, so::{DatasheetSnapshotSO, CellValueSo, DatasheetPackContext}};

pub struct PasteSetRecords {

}

impl PasteSetRecords {
    pub fn execute (
        _context: Rc<DatasheetPackContext>,
        snapshot: Option<DatasheetSnapshotSO>,
        options: PasteSetRecordsOptions,
    ) -> anyhow::Result<Option<CollaCommandDefExecuteResult>> { 
        // let state = context.state;
        // let field_map_snapshot = context.field_map_snapshot;
        let field_map_snapshot = HashMap::new();
        let PasteSetRecordsOptions {
            record_ids:_,
            column,
            row,
            view_id,
            std_values,
            cut,
            group_cell_values:_,
            notify_exist_incompatible_field:_,
            cmd:_,
            fields,
        } = options;
        // let datasheet_id = Selectors::get_active_datasheet_id(state)?;
        // let snapshot = Selectors::get_snapshot(state, datasheet_id)?;
        // let user_info = state.user.info;
        if snapshot.is_none() {
            return Ok(None);
        }
        let snapshot = snapshot.unwrap();
        let datasheet_id = snapshot.datasheet_id.clone();
        let view = get_view_by_id(snapshot.clone(), view_id.clone());
        // if view.is_none() || ![ViewType::Grid, ViewType::Gantt].contains(&view.unwrap().type) {
        if view.is_none() || ![1, 6].contains(&view.unwrap().r#type.clone().unwrap()) {
            return Ok(None);
        }
        if column < 0 {
            return Ok(None);
        }
        if row < 0 {
            return Ok(None);
        }
        let _field_map = snapshot.meta.field_map.clone();
        let new_record_count = std_values.len();
        if new_record_count == 0 {
            return Ok(None);
        }
        let mut actions: Vec<ActionOTO> = vec![];
        let default_alarm_actions: Vec<ActionOTO> = vec![];
        let real_alarm_actions: Vec<ActionOTO> = vec![];
        let linked_actions: Vec<LinkedActions> = vec![];
        let _record_values: Vec<SetRecordOptions> = vec![];
        if let Some(cut) = cut {
            let _datasheet_id = cut.datasheet_id;
            // if datasheet_id == state.page_params.datasheet_id {
            //     let cut_rows = cut.rows;
            //     let cut_columns = cut.columns;
            //     for row in cut_rows {
            //         for column in cut_columns {
            //             let field = field_map.get(&column.field_id)?;
            //             if let Some(field) = field {
            //                 if field.kind != FieldKindSO::NotSupport {
            //                     record_values.push(ISetRecordOptions {
            //                         record_id: row.record_id,
            //                         field_id: column.field_id,
            //                         value: Ok(None),
            //                     });
            //                 }
            //             }
            //         }
            //     }
            // }
        }
        let _column_count = fields.len();
        // let visible_columns = get_visible_columns(state)?;
        // let columns_to_paste = visible_columns
        //     .get(column..column + column_count)
        //     .unwrap_or_default();
        // if columns_to_paste.is_empty() {
        //     return Ok(None);
        // }
        // let record_ids_to_paste = get_range_rows(state, row, row + new_record_count)
        //     .map(|r| r.record_id)
        //     .collect::<Vec<_>>();

        // fn add_alarm(
        //     cv: CellValueSo,
        //     field: FieldSO,
        //     record_id: String,
        //     old_record_id: String,
        //     snapshot: &mut DatasheetSnapshotSO,
        //     default_alarm_actions: &mut Vec<ActionOTO>,
        // ) {
        //     if field.kind == FieldKindSO::DateTime && !cv.is_null() {
        //         let alarm = get_date_time_cell_alarm(&snapshot, &old_record_id, &field.id);
        //         if let Some(alarm) = alarm {
        //             let cur_alarm_actions =
        //                 DatasheetActions::set_date_time_cell_alarm(snapshot.clone(), RecordAlarmOTO {
        //                     record_id: record_id.clone(),
        //                     field_id: field.id.clone(),
        //                     alarm: Some(RecordAlarm {
        //                         id: get_new_id(IDPrefix::DateTimeAlarm, Vec::new()),
        //                         ..alarm.clone()
        //                     }),
        //                 }).unwrap();
        //             if let Some(cur_alarm_actions) = cur_alarm_actions {
        //                 if cur_alarm_actions.len() == 2 {
        //                     let mut is_merged = false;
        //                     for (idx, action) in default_alarm_actions.iter_mut().enumerate() {
        //                         if action.op.p == cur_alarm_actions[0].op.p {
        //                             match &action.op.kind {
        //                                 OperationKind::ObjectInsert { oi } => {
        //                                     let oi_2 = oi;
        //                                     match &cur_alarm_actions[0].op.kind {
        //                                         OperationKind::ObjectInsert { oi } => {
        //                                             // oi_2.as_object().unwrap().extend(oi);
        //                                         }
        //                                         _ => {}
        //                                     }
        //                                 }
        //                                 _ => {}
        //                             }
        //                             // action.op.kind.oi.extend(cur_alarm_actions[0].op.oi);
        //                             is_merged = true;
        //                         }
        //                     }
        //                     if !is_merged {
        //                         default_alarm_actions.push(cur_alarm_actions[0].clone());
        //                     }
        //                 }
        //             }
        //         }
        //     }
        // }

        let single_cell_paste = std_values.len() == 1 && std_values[0].len() == 1;
        if single_cell_paste {
            // let ranges = get_select_ranges(state)?;
            // let range = ranges.get(0)?;
            // let rows = Selectors::get_range_records(state, range)?;
            // let fields = Selectors::get_range_fields(state, range, datasheet_id)?;
            // if rows.is_none() || fields.is_none() {
            //     return Ok(None);
            // }
            // for row in rows {
            //     let record_id = row.record_id;
            //     for field in fields {
            //         push_paste_value(std_values[0][0].clone(), field, record_id.clone(), record_ids.get(0).cloned());
            //     }
            // }
        } else {
            for (_i, _std_values_row) in std_values.iter().enumerate() {
                // if let Some(record_id) = record_ids_to_paste.get(i) {
                //     for (c, std_value) in std_values_row.iter().enumerate() {
                //         if let Some(column) = columns_to_paste.get(c) {
                //             let field_id = column.field_id.clone();
                //             let std_value = std_value.clone();
                //             if let Some(field) = field_map.get(&field_id) {
                //                 push_paste_value(std_value, field.clone(), record_id.clone(), record_ids.get(i).cloned());
                //             }
                //         }
                //     }
                // }
            }
        }

        // let rst = SetRecords::execute(&snapshot, SetRecordsOptions {
        //     cmd: CollaCommandName::SetRecords,
        //     data: record_values.clone(),
        //     ..Default::default()
        // }).unwrap();
        // if let Some(rst) = rst {
        //     if rst.result == ExecuteResult::Fail {
        //         return Ok(Some(rst));
        //     }
        //     field_map_snapshot.extend(rst.field_map_snapshot.unwrap_or_default());
        //     actions.extend(rst.actions);
        // }

        let new_std_values = std_values.get(1..).unwrap_or_default();
        if !new_std_values.is_empty() {
            let mut record_values: Vec<HashMap<String, CellValueSo>> = vec![];
            let _old_record_ids: Vec<String> = vec![];
            for (_row, std_values_row) in new_std_values.iter().enumerate() {
                let cell_values: HashMap<String, CellValueSo> = HashMap::new();
                for (_column, _std_value) in std_values_row.iter().enumerate() {
                    // if let Some(column) = columns_to_paste.get(column) {
                    //     let field_id = column.field_id.clone();
                    //     let std_value = std_value.clone();
                    //     if let Some(field) = field_map.get(&field_id) {
                    //         if field.kind != FieldKindSO::NotSupport {
                    //             let value = Field::bind_context(field, state)
                    //                 .std_value_to_cell_value(std_value);
                    //             let value = handle_empty_cell_value(value, Field::bind_context(field, state).basic_value_type);
                    //             cell_values.insert(field_id.clone(), value);
                    //         }
                    //     }
                    // }
                }
                record_values.push(cell_values);
                // if let Some(record_id) = record_ids.get(record_ids_to_paste.len() + row) {
                //     old_record_ids.push(record_id.clone());
                // }
            }
            // let add_records = AddRecords{};
            // let rst = AddRecords::execute(context, CommandOptions::AddRecordsOptions(AddRecordsOptions {
            //     cmd: CollaCommandName::AddRecords,
            //     view_id: view_id.clone(),
            //     // index: get_actual_row_count(state)?,
            //     index: 0,
            //     count: new_std_values.len(),
            //     group_cell_values,
            //     cell_values: Some(record_values.clone()),
            //     ..Default::default()
            // })).unwrap();
            // if let Some(rst) = rst {
            //     if rst.result == ExecuteResult::Fail {
            //         return Ok(Some(rst));
            //     }
            //     field_map_snapshot.extend(rst.field_map_snapshot.unwrap_or_default());
            //     actions.extend(rst.actions);

            //     let new_record_ids = rst.data.unwrap_or_default();
            //     for (rv, cv_index) in record_values.iter().zip(0..) {
            //         for (f_id, _) in rv.iter() {
            //             if let Some(f) = field_map.get(f_id) {
            //                 add_alarm(
            //                     rv[f_id].clone(), 
            //                     f.clone(), 
            //                     serde_json::from_value(new_record_ids[cv_index].clone()).unwrap(), 
            //                     old_record_ids[cv_index].clone(),
            //                     &mut snapshot.clone(),
            //                     &mut default_alarm_actions,
            //                 );
            //             }
            //         }
            //     }
            // }
        }

        if !real_alarm_actions.is_empty() {
            actions.extend(default_alarm_actions);
            actions.extend(real_alarm_actions);
        }

        Ok(Some(CollaCommandDefExecuteResult {
            result: ExecuteResult::Success,
            resource_id: datasheet_id,
            resource_type: ResourceType::Datasheet,
            actions,
            linked_actions: Some(linked_actions),
            field_map_snapshot: Some(field_map_snapshot),
            ..Default::default()
        }))
    }
}