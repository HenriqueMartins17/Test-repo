use std::rc::Rc;

use json0::{TransformSide, JSON0, operation::{OperationKind, PathSegment}};
use serde_json::{to_value, from_value};

use crate::so::{DatasheetPackContext, DatasheetSnapshotSO};

use super::{types::ActionOTO, changeset::Operation};


pub fn json0_transform(
    ijot_action: Vec<ActionOTO>,
    other_ijot_action: Vec<ActionOTO>,
    side: TransformSide,
) -> anyhow::Result<Vec<ActionOTO>> {
    let op = ijot_action.iter().map(|action| action.op.clone()).collect::<Vec<_>>();
    let other_op = other_ijot_action.iter().map(|action| action.op.clone()).collect::<Vec<_>>();
    let operation = JSON0::transform(
        op,
        other_op,
        side,
    ).unwrap();
    let mut actions = Vec::new();
    for op in operation {
        let op_name = match &op.kind {
            OperationKind::NumberAdd{na:_} => "NA".to_string(),
            OperationKind::ListReplace{ld:_,li:_} => "LP".to_string(),
            OperationKind::ListInsert{li:_} => "LI".to_string(),
            OperationKind::ListDelete{ld:_} => "LD".to_string(),
            OperationKind::ListMove{lm:_} => "LM".to_string(),
            OperationKind::ObjectReplace{od:_,oi:_} => "OR".to_string(),
            OperationKind::ObjectInsert{oi:_} => "OI".to_string(),
            OperationKind::ObjectDelete{od:_} => "OD".to_string(),
            OperationKind::SubtypeOperation{t:_,o:_} => "SO".to_string(),
            OperationKind::StringInsert{si:_} => "SI".to_string(),
            OperationKind::StringDelete{sd:_} => "SD".to_string(),
        };
        let action = ActionOTO {
            op_name,
            op,
        };
        actions.push(action);
    }
    Ok(actions)
}

pub fn filter_datasheet_op(context: Rc<DatasheetPackContext>, actions: Vec<ActionOTO>) -> Vec<ActionOTO> {
    actions.into_iter().filter(|action| {
        // OPs that filter comments
        let p = action.op.p.clone();
        let is_in = match &action.op.kind {
            OperationKind::ListInsert{li:_} => true,
            OperationKind::ListDelete{ld:_} => true,
            OperationKind::ListReplace{ld:_,li:_} => true,
            _ => false,
        };
        if p[2] == PathSegment::String("comments".to_string()) && p[0] == PathSegment::String("recordMap".to_string()) && 
        is_in {
            return false;
        }

        // when state only contains part of the data, filter out actions that operate on non-existent records
        // if state.is_part_of_data {
            // if p[0] is recordMap, then p[1] is recordId
            // if action.p.len() > 2, then action has setRecord feature
            // return !(action.p[0] == "recordMap" && action.p.len() > 2 && !state.snapshot.record_map.contains_key(&action.p[1]));
        // }
        true
    }).collect()
}

// pub fn jot_apply<T: DatasheetSnapshotSO>(
//     state: &mut T,
//     payload: JOTActionPayload,
//     filter_cb: Option<&dyn Fn(&T, Vec<ActionOTO>) -> Vec<ActionOTO>>,
// ) {
pub fn jot_apply(
    // state: &mut T,
    // context: Rc<DatasheetPackContext>,
    context: &mut DatasheetPackContext,
    // payload: JOTActionPayload,
    operations: Vec<Operation>
    // filter_cb: Option<&dyn Fn(&T, Vec<ActionOTO>) -> Vec<ActionOTO>>,
// ) -> DatasheetSnapshotSO {
) {
    let max = 10000;
    // let mut pure_jot_action: Vec<ActionOTO> = payload.operations.iter().flat_map(|op| {
    let pure_jot_action: Vec<ActionOTO> = operations.iter().flat_map(|op| {
        let length = op.actions.len();
        if length > max {
            let mut result = Vec::new();
            for i in 0..((length as f64 / max as f64).ceil() as usize) {
                result.extend_from_slice(&op.actions[i * max..((i + 1) * max).min(length)]);
            }
            result
        } else {
            op.actions.clone()
        }
    }).collect();

    // if let Some(filter) = filter_cb {
    //     pure_jot_action = filter(state, pure_jot_action);
    // }

    // apply(&mut context.datasheet_pack.snapshot, &pure_jot_action);
    // println!("pure_jot_action: {:?}", pure_jot_action);
    let snapshot_value = to_value(&context.datasheet_pack.snapshot).unwrap();
    // println!("snapshot_value: {:?}", snapshot_value);
    // let operation_value = to_value(&pure_jot_action).unwrap();
    let operations = pure_jot_action.iter().map(|action| action.op.clone()).collect::<Vec<_>>();
    let result = JSON0::apply(snapshot_value, operations).map_err(|e| println!("Error applying operation: {}", e));
    match result{
        Ok(result) => {
            context.datasheet_pack.snapshot = from_value(result).unwrap();
        },
        Err(_e) => {
            println!("Error applying operation");
        }
    }
}

pub fn jot_apply_snapshot(
    snapshot: &mut DatasheetSnapshotSO,
    actions: Vec<ActionOTO>
) -> anyhow::Result<()>{
    let snapshot_value = to_value(&snapshot).unwrap();
    let operations = actions.iter().map(|action| action.op.clone()).collect::<Vec<_>>();
    let result = JSON0::apply(snapshot_value, operations).map_err(|e| println!("Error applying operation: {}", e));
    match result{
        Ok(result) => {
            *snapshot = from_value(result).unwrap();
            Ok(())
        },
        Err(_e) => {
            println!("Error applying operation");
            Err(anyhow::anyhow!("APPLY_META_ERROR"))
        }
    }
}
