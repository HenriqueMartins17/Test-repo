
// fn copy_col(select_cells: &[ICell], fill_cells: &[ICell], state: &IReduxState, snapshot: &ISnapshot, datasheet_id: &str) -> Vec<(String, String, ICellValue)> {
//     let mut data: Vec<(String, String, ICellValue)> = Vec::new();

//     for (i, fill_cell) in fill_cells.iter().enumerate() {
//         let cell_index = i % select_cells.len();
//         let select_cell = &select_cells[cell_index];
//         let field_id = &fill_cell.field_id;
//         let field = Selectors::get_field(state, field_id, datasheet_id);
//         let mut cell_value = Selectors::get_cell_value(state, snapshot, &select_cell.record_id, &select_cell.field_id);
//         cell_value = handle_empty_cell_value(cell_value, Field::bind_context(&field, state).basic_value_type);
//         data.push((fill_cell.record_id.clone(), fill_cell.field_id.clone(), cell_value));
//     }
//     data
// }

// fn arithmetic_col(
//     diff: i32,
//     select_cell_col: &[ICell],
//     fill_range_cells: &[ICell],
//     unit: Option<&str>,
// ) -> Vec<ArithmeticColData> {
//     let mut data: Vec<ArithmeticColData> = Vec::new();
//     let last_cell = select_cell_col.last().unwrap();
//     let base = Selectors::get_cell_value(state, snapshot, last_cell.record_id, last_cell.field_id);
//     for (i, cell) in fill_range_cells.iter().enumerate() {
//         let value: i32;
//         if let Some(u) = unit {
//             match u {
//                 "time" => value = diff * (i as i32 + 1) + base,
//                 "day" => {
//                     let base_date = dayjs(dayjs(base).format("YYYY/MM/DD"));
//                     value = base_date.add(diff * (i as i32 + 1), "day").value_of();
//                 }
//                 "month" => {
//                     let base_date = dayjs(dayjs(base).format("YYYY/MM"));
//                     value = base_date.add(diff * (i as i32 + 1), "month").value_of();
//                 }
//                 "year" => {
//                     let base_date = dayjs(dayjs(base).format("YYYY"));
//                     value = base_date.add(diff * (i as i32 + 1), "year").value_of();
//                 }
//                 _ => value = diff * (i as i32 + 1) + base,
//             }
//         } else {
//             value = diff * (i as i32 + 1) + base;
//         }
//         data.push(ArithmeticColData {
//             record_id: cell.record_id.clone(),
//             field_id: cell.field_id.clone(),
//             value,
//         });
//     }
//     data
// }

// fn compute_fill_data_vertical(
//     fill_range_cells: Vec<Vec<ICell>>,
//     select_range_cells: Vec<Vec<ICell>>,
//     direction: FillDirection,
// ) -> Vec<HashMap<String, ICellValue>> {
//     let mut data: Vec<HashMap<String, ICellValue>> = Vec::new();
//     let t_select_range_cells = transpose(select_range_cells);
//     let t_fill_range_cells = transpose(fill_range_cells);

//     for j in 0..t_select_range_cells.len() {
//         let mut t_select_cell_col = t_select_range_cells[j].clone();
//         let mut t_fill_cell_col = t_fill_range_cells[j].clone();

//         if direction == FillDirection::Top {
//             t_select_cell_col.reverse();
//             t_fill_cell_col.reverse();
//         }

//         let field_id = t_select_cell_col[0].field_id.clone();
//         let field = Selectors::get_field(state, field_id, datasheet_id);
//         if field.field_type == FieldType::Number || field.field_type == FieldType::DateTime {
//             let pattern = pattern_finder(t_select_cell_col.clone(), field.field_type, state, snapshot, field);
//             match pattern.field_type {
//                 PatternType::Arithmetic => {
//                     let diff = pattern.args.diff;
//                     let col_data = arithmetic_col(diff, t_select_cell_col.clone(), t_fill_cell_col.clone());
//                     data.extend(col_data);
//                 }
//                 PatternType::DateArithmetic => {
//                     let d_diff = pattern.args.d_diff;
//                     let d_unit = pattern.args.d_unit;
//                     let d_col_data = arithmetic_col(d_diff, t_select_cell_col.clone(), t_fill_cell_col.clone(), d_unit);
//                     if d_col_data.iter().any(|data| data.value < EARLIEST_DATE) {
//                         data.extend(copy_col(t_select_cell_col.clone(), t_fill_cell_col.clone()));
//                     } else {
//                         data.extend(d_col_data);
//                     }
//                 }
//                 PatternType::Copy => {
//                     data.extend(copy_col(t_select_cell_col.clone(), t_fill_cell_col.clone()));
//                 }
//             }
//         } else {
//             data.extend(copy_col(t_select_cell_col.clone(), t_fill_cell_col.clone()));
//         }
//     }
//     data
// }

// fn update_fill_fields_property(
//     select_fields: &[IField],
//     fill_fields: &[IField],
//     select_cells: &[ICell],
// ) -> Vec<IField> {
//     let mut new_fill_fields: Vec<IField> = Vec::new();
//     for (index, field) in fill_fields.iter().enumerate() {
//         let select_field = select_fields[index % select_fields.len()].clone();
//         let std_values: Vec<ICellValue> = select_cells
//             .iter()
//             .filter(|cell| cell.fieldId == select_field.id)
//             .map(|cell| {
//                 let mut cell_value = Selectors::get_cell_value(
//                     state,
//                     snapshot,
//                     cell.recordId,
//                     cell.fieldId,
//                 );
//                 cell_value = handle_empty_cell_value(
//                     cell_value,
//                     Field::bind_context(select_field, state).basic_value_type,
//                 );
//                 Field::bind_context(select_field, state).cell_value_to_std_value(cell_value)
//             })
//             .collect();
//         let mut new_field = fast_clone_deep(field);

//         let new_property = if new_field.type == FieldType::Member {
//             new_field.property
//         } else {
//             Field::bind_context(new_field, state).enrich_property(std_values)
//         };

//         let data = IField {
//             property: new_property,
//             ..new_field
//         };
//         new_fill_fields.push(data);
//         let rst = set_field_attr.execute(
//             context,
//             CollaCommandName::SetFieldAttr,
//             new_field.id,
//             data,
//         );
//         if let Some(rst) = rst {
//             if rst.result == ExecuteResult::Success {
//                 actions.extend(rst.actions);
//             }
//         }
//     }
//     new_fill_fields
// }

// // Calculate horizontally filled data
// fn compute_fill_data_horizontal(
//     fill_range_cells: &[Vec<ICell>],
//     select_range_cells: &[Vec<ICell>],
//     direction: FillDirection,
// ) -> Vec<ICell> {
//     let mut data: Vec<ICell> = Vec::new();
//     let select_cells: Vec<ICell> = select_range_cells.iter().flatten().cloned().collect();
//     let fill_cells: Vec<ICell> = fill_range_cells.iter().flatten().cloned().collect();
//     if direction == FillDirection::Left {
//         select_cells.reverse();
//         fill_cells.reverse();
//     }
//     // Horizontal padding, may need to extend the property of the field

//     let fill_fields = Selectors::get_range_fields(state, fill_range, datasheet_id).unwrap();
//     let mut new_fill_fields = fill_fields.clone();
//     let select_fields = Selectors::get_range_fields(state, selection_range[0], datasheet_id).unwrap();
//     new_fill_fields = update_fill_fields_property(select_fields, fill_fields, select_cells);
//     for (i, fill_cell) in fill_cells.iter().enumerate() {
//         let cell_index = i % select_cells.len();
//         let select_cell = select_cells[cell_index].clone();
//         let fill_field = new_fill_fields
//             .iter()
//             .find(|f| f.id == fill_cell.fieldId)
//             .unwrap();
//         let select_field = Selectors::get_field(state, select_cell.fieldId, datasheet_id);
//         let mut select_cell_value =
//             Selectors::get_cell_value(state, snapshot, select_cell.recordId, select_cell.fieldId);
//         select_cell_value = handle_empty_cell_value(
//             select_cell_value,
//             Field::bind_context(select_field, state).basic_value_type,
//         );
//         let select_std_val =
//             Field::bind_context(select_field, state).cell_value_to_std_value(select_cell_value);
//         let will_fill_cell_value =
//             Field::bind_context(fill_field, state).std_value_to_cell_value(select_std_val);
//         // Horizontal padding involves field conversion, select cv > stdVal > Fill cv
//         data.push(ICell {
//             recordId: fill_cell.recordId,
//             fieldId: fill_cell.fieldId,
//             value: will_fill_cell_value,
//         });
//     }
//     data
// }

// fn fill_data_to_cell(
//     selection_range: Vec<IRange>,
//     fill_range: Option<IRange>,
//     direction: Option<String>,
// ) -> Option<ExecuteResult> {
//     if fill_range.is_none() || selection_range.is_none() {
//         return None;
//     }
//     let selection_range_cells = Selectors::get_cell_matrix_from_range(state, selection_range[0]);
//     let fill_range_cells = Selectors::get_cell_matrix_from_range(state, fill_range.unwrap());
//     if selection_range_cells.is_none() || fill_range_cells.is_none() {
//         return None;
//     }
//     let mut data: Vec<{
//         record_id: String,
//         field_id: String,
//         value: ICellValue,
//     }> = Vec::new();
//     match direction {
//         Some(dir) => {
//             match dir.as_str() {
//                 FillDirection::Below | FillDirection::Top => {
//                     data = compute_fill_data_vertical(fill_range_cells.unwrap(), selection_range_cells.unwrap(), dir);
//                 },
//                 FillDirection::Right | FillDirection::Left => {
//                     data = compute_fill_data_horizontal(fill_range_cells.unwrap(), selection_range_cells.unwrap(), dir);
//                 },
//                 _ => return None,
//             }
//         },
//         None => return None,
//     }
//     if !data.is_empty() {
//         let rst = set_records.execute(context, CollaCommandName::SetRecords, data);
//         if let Some(rst) = rst {
//             if rst.result == ExecuteResult::Fail {
//                 return Some(rst);
//             }
//             actions.extend(rst.actions);
//             field_map_snapshot.extend(rst.field_map_snapshot);
//             linked_actions.extend(rst.linked_actions.unwrap_or_default());
//         }
//     }
//     return None;
// }
use std::collections::HashMap;

use crate::{ot::{commands::{CollaCommandDefExecuteResult, FillDataToCellOptions, ExecuteResult, LinkedActions}, types::{ResourceType, ActionOTO}}, so::DatasheetSnapshotSO};

pub struct FillDataToCell {

}

impl FillDataToCell {
    pub fn execute (
        snapshot: DatasheetSnapshotSO,
        options: FillDataToCellOptions,
    ) -> anyhow::Result<Option<CollaCommandDefExecuteResult>> { 
        // let state = context.state;
        let FillDataToCellOptions {
            selection_range:_,
            fill_range:_,
            direction:_,
            cmd:_,
        } = options;
        let field_map_snapshot = HashMap::new();
        // let field_map_snapshot = &mut context.field_map_snapshot;
        let datasheet_id = snapshot.datasheet_id.clone();
        // let datasheet_id = selectors::get_active_datasheet_id(state);

        // if state.is_none() || datasheet_id.is_none() || fill_range.is_none() {
        //     return Ok(None);
        // }

        // let snapshot = selectors::get_snapshot(state.unwrap(), datasheet_id.unwrap()).unwrap();
        let actions: Vec<ActionOTO> = Vec::new();
        let linked_actions: Vec<LinkedActions> = Vec::new();

        // fill_data_to_cell(selection_range, fill_range.unwrap(), direction);

        if actions.is_empty() {
            return Ok(None);
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