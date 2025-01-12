

use crate::{
  operation::{OperationKind, PathSegment, TransformSide},
  Operation,
};
use anyhow::anyhow;
use serde_json::{Map, to_value, Value};


use std::collections::HashMap;

pub struct JSON0 {}

// JSON0 JavaScript Library has 19 functions, they are isArray(is_array), isObject(is_object), clone(clone), create(create), invertComponent(invert_component), invert(invert), checkValidOp(check_valid_op), checkList(check_list), checkObj(check_obj), apply(apply), shatter(shatter), incrementalApply(incremental_apply), pathMatches(path_matches), append(append), compose(compose), normalize(normalize), commonLengthForOps(common_length_for_ops), canOpAffectPath(can_op_affect_path), transformComponent(transform_component), convertFromText(convert_from_text), convertToText(convert_to_text).

// checks if the Object is Vector(Array)
pub fn is_array(obj: &Value) -> bool {
  matches!(obj, Value::Array(_))
}

// check if the Object is Map(Object)
pub fn is_object(obj: &Value) -> bool {
  matches!(obj, Value::Object(_))
}

fn convert_from_text(c: &mut HashMap<String, Value>) {
  c.insert("t".to_string(), Value::String("text0".to_string()));
  let mut o = HashMap::new();
  if let Some(Value::Array(p)) = c.get_mut("p") {
    if let Some(val) = p.pop() {
      o.insert("p".to_string(), val);
    }
  }
  if let Some(val) = c.get("si") {
    o.insert("i".to_string(), val.clone());
  }
  if let Some(val) = c.get("sd") {
    o.insert("d".to_string(), val.clone());
  }
  let json_value: Value = to_value(&o).unwrap();
  c.insert("o".to_string(), Value::Array(vec![json_value]));
}

fn convert_to_text(c: &mut HashMap<String, Value>) {
  let (p_val, i_val, d_val) = if let Some(Value::Array(o)) = c.get("o") {
    if let Some(Value::Object(o0)) = o.get(0) {
      (o0.get("p").cloned(), o0.get("i").cloned(), o0.get("d").cloned())
    } else {
      (None, None, None)
    }
  } else {
    (None, None, None)
  };

  if let Some(Value::Array(p)) = c.get_mut("p") {
    if let Some(val) = p_val {
      p.push(val);
    }
  }

  if let Some(val) = i_val {
    c.insert("si".to_string(), val);
  }

  if let Some(val) = d_val {
    c.insert("sd".to_string(), val);
  }

  c.remove("t");
  c.remove("o");
}

// unnecessary method can be replaced by clone() in Rust
// pub fn clone<T: Clone>(obj: &Value) -> Value {
//   obj.clone()
// }

impl JSON0 {
  // pub fn create<T: Clone>(data: Option<T>) -> Option<T> {
  //   data.map(|d| d.clone())
  // }

  pub fn create(data: Option<Value>) -> Option<Value> {
    match data {
      Some(d) => Some(d.clone()),
      None => None,
    }
  }

  pub fn create_default() -> Option<Value> {
    Self::create(None)
  }

  pub fn shatter(op: &[Operation]) -> Vec<Vec<Operation>> {
    let mut results = Vec::new();

    for i in 0..op.len() {
      results.push(vec![op[i].clone()]);
    }

    results
  }

  pub fn normalize(op: Vec<Operation>) -> Vec<Operation> {
    let mut new_op = Vec::new();

    for mut c in op {
      if c.p.is_empty() {
        c.p = Vec::new();
      }
      Self::append(&mut new_op, c).unwrap(); // Handle the error as needed
    }

    new_op
  }

  fn can_op_affect_path(_op: &Operation, _path: &[PathSegment]) -> bool {
    let path_op = Operation {
      p: _path.to_vec(),
      kind: OperationKind::NumberAdd { na: 0.0 },
    };
    Self::common_length_for_ops(&path_op, _op).is_some()
  }

  fn incremental_apply(snapshot: &Value, op: Vec<Operation>, _yield: fn(Vec<Operation>, &Value)) -> Value {
    let mut snapshot = snapshot.clone();

    for c in op {
      Self::apply_one(&mut snapshot, &c);
      _yield(vec![c], &snapshot);
    }

    snapshot
  }

  pub fn apply_json(snapshot_json_str: &str, operations_json_str: &str) -> anyhow::Result<Value> {
    let snapshot_json: Value = serde_json::from_str::<Value>(snapshot_json_str).unwrap();
    let operations_json: Vec<Operation> = serde_json::from_str(operations_json_str).unwrap();

    Self::apply(snapshot_json, operations_json)
  }

  pub fn apply_one(_snapshot: &Value, _operation: &Operation) {}

  pub fn apply(snapshot: Value, operations: Vec<Operation>) -> anyhow::Result<Value> {
    Self::check_valid_op(&operations)?;
    let mut container = Value::Object([("data".to_string(), snapshot)].iter().cloned().collect());

    for component in operations {
      let mut elem = &mut container;
      let mut key = PathSegment::from("data");

      for p in component.p {
        let Some(child) = key.index_mut(elem) else {
          return Err(anyhow!("Path invalid"));
        };
        elem = child;
        key = p;

        if elem.is_array() && !matches!(key, PathSegment::Number(_)) {
          return Err(anyhow!("List index {:?} must be a number", key));
        }

        if elem.is_object() && !matches!(key, PathSegment::String(_)) {
          return Err(anyhow!("Object key {:?} must be a string", key));
        }
      }

      match component.kind {
        OperationKind::NumberAdd { na } => {
          let n = key.index_mut(&mut elem);
          match n {

            Some(Value::Number(n1)) => {
              if n1.is_i64() {
                let n_val = n1.as_i64().unwrap();
                *n1 = serde_json::Number::from(n_val + na as i64);
              }else if n1.is_f64(){
                let n_val = n1.as_f64().unwrap();
                *n1 = serde_json::Number::from_f64(n_val + na).unwrap();
              }else {
                let n_val = n1.as_u64().unwrap();
                *n1 = serde_json::Number::from(n_val + na as u64);
              }
            }
            _ => {
              return Err(anyhow!("Referenced element: {:?} not a number", elem));
            }
          }
        }
        OperationKind::ListReplace { li, .. } => {
          (*Self::check_list(elem)?)[key.unwrap_number()] = li;
        }
        OperationKind::ListInsert { li } => {
          Self::check_list(elem)?.insert(key.unwrap_number(), li);
        }
        OperationKind::ListDelete { .. } => {
          Self::check_list(elem)?.remove(key.unwrap_number());
        }
        OperationKind::ListMove { lm: new_index } => {
          let array = Self::check_list(elem)?;
          let old_index = key.unwrap_number();
          if new_index != old_index {
            if old_index < new_index {
              array[old_index..=new_index].rotate_left(1);
            } else {
              array[new_index..=old_index].rotate_right(1);
            }
          }
        }
        OperationKind::ObjectInsert { oi } | OperationKind::ObjectReplace { oi, .. } => {
          Self::check_obj(elem)?.insert(key.unwrap_string(), oi.clone());
        }
        OperationKind::ObjectDelete { .. } => {
          Self::check_obj(elem)?.remove(&key.unwrap_string());
        }
        OperationKind::StringDelete { sd: _ } => match elem {
          Value::String(s) => {
            let index = key.unwrap_number();
            let mut chars: Vec<char> = s.chars().collect();
            if index < chars.len() {
              chars.remove(index);
              *s = chars.into_iter().collect();
            } else {
              return Err(anyhow!("Index out of bounds"));
            }
          }
          _ => {
            return Err(anyhow!("Referenced element: {:?} not a string.", elem));
          }
        },
        OperationKind::StringInsert { si } => match elem {
          Value::String(s) => {
            let index = key.unwrap_number();
            let mut chars: Vec<char> = s.chars().collect();
            let si_chars: Vec<char> = si.chars().collect();
            if index <= chars.len() {
              chars.splice(index..index, si_chars);
              *s = chars.into_iter().collect();
            } else {
              return Err(anyhow!("Index out of bounds"));
            }
          }
          _ => {
            return Err(anyhow!("Referenced element: {:?} not a string.", elem));
          }
        },
        OperationKind::SubtypeOperation { t: _, o: _ } => {
          // TODO: implement
          // Text0 related
          return Err(anyhow!("SubtypeOperation not supported"));
        }
      }
    }

    Ok(container.as_object_mut().unwrap().remove("data").unwrap())
  }

  fn check_valid_op(_ops: &[Operation]) -> anyhow::Result<()> {
    Ok(())
  }

  fn check_list(elem: &mut Value) -> anyhow::Result<&mut Vec<Value>> {
    match elem {
      Value::Array(array) => Ok(array),
      _ => Err(anyhow!("Referenced element: {:?} not a list.", elem)),
    }
  }

  fn check_obj(elem: &mut Value) -> anyhow::Result<&mut Map<String, Value>> {
    match elem {
      Value::Object(obj) => Ok(obj),
      _ => Err(anyhow!("Referenced element: {:?} not an object.", elem)),
    }
  }

  fn invert_component(mut c: Operation) -> Operation {
    let kind = match c.kind {
      OperationKind::ObjectInsert { oi } => OperationKind::ObjectDelete { od: oi },
      OperationKind::ObjectDelete { od } => OperationKind::ObjectInsert { oi: od },
      OperationKind::ObjectReplace { od, oi } => OperationKind::ObjectReplace { oi: od, od: oi },
      OperationKind::ListInsert { li } => OperationKind::ListDelete { ld: li },
      OperationKind::ListDelete { ld } => OperationKind::ListInsert { li: ld },
      OperationKind::ListReplace { ld, li } => OperationKind::ListReplace { li: ld, ld: li },
      OperationKind::ListMove { lm } => {
        let new_lm = c.p.pop().unwrap().unwrap_number();
        c.p.push(lm.into());
        return Operation {
          kind: OperationKind::ListMove { lm: new_lm },
          p: c.p,
        };
      }
      OperationKind::NumberAdd { na } => OperationKind::NumberAdd { na: -na },
      OperationKind::StringDelete { sd } => OperationKind::StringInsert { si: sd },
      OperationKind::StringInsert { si } => OperationKind::StringDelete { sd: si },
      // OperationKind::SubtypeOperation { t, o } => {
      //   let inverted_o = invert_subtype_operation(t, o);
      //   OperationKind::SubtypeOperation { t, o: inverted_o }
      // }
      OperationKind::SubtypeOperation { t, o } => OperationKind::SubtypeOperation { t, o },
      // Text0 related
    };
    Operation { p: c.p, kind }
  }

  pub fn invert(operations: Vec<Operation>) -> Vec<Operation> {
    operations.into_iter().rev().map(Self::invert_component).collect()
  }

  /// Checks if two paths, p1 and p2 match.
  fn path_matches(p1: &[PathSegment], p2: &[PathSegment], ignore_last: bool) -> bool {
    if p1.len() != p2.len() {
      return false;
    }
    if ignore_last {
      &p1[..p1.len() - 1] == &p2[..p2.len() - 1]
    } else {
      p1 == p2
    }
  }

  pub fn compose(mut op1: Vec<Operation>, op2: Vec<Operation>) -> anyhow::Result<Vec<Operation>> {
    Self::check_valid_op(&op1)?;
    Self::check_valid_op(&op2)?;

    for c in op2.clone() {
      Self::append(&mut op1, c)?;
    }

    Ok(op1)
  }

  pub fn append(operations: &mut Vec<Operation>, c: Operation) -> anyhow::Result<()> {
    let c: Operation = c.clone();

    if operations.is_empty() {
      operations.push(c);
      return Ok(());
    }

    let last_index = operations.len() - 1;

    if let OperationKind::StringInsert { si: c_si } = &c.kind {
      match operations.last_mut() {
        Some(Operation {
          p: last_p,
          kind: OperationKind::StringInsert { si: last_si },
        }) => {
          match (c.p.last().unwrap(), last_p.last().unwrap()) {
            (PathSegment::Number(n1), PathSegment::Number(n2)) => {
              if *n1 == *n2 + 1 {
                // Append the new string to the existing one
                last_si.push_str(c_si);
              } else {
                // If the paths are not adjacent, push the operation as is
                operations.push(c);
              }
            }
            _ => {
              // If the paths are not numbers, push the operation as is
              operations.push(c);
            }
          }
        }
        _ => {
          // If the last operation is not a StringInsert, push the operation as is
          operations.push(c);
        }
      }
    } else if Self::path_matches(&c.p, &operations[last_index].p, false) {
      match (&c.kind, &mut operations[last_index].kind) {
        (OperationKind::NumberAdd { na: c_na }, OperationKind::NumberAdd { na: last_na }) => {
          *last_na += c_na;
        }
        (OperationKind::ListDelete { ld: c_ld }, OperationKind::ListInsert { li: last_li }) if last_li == c_ld => {
          // insert immediately followed by delete becomes a noop.
          operations.pop();
        }
        (
          OperationKind::ListDelete { ld: c_ld },
          OperationKind::ListReplace {
            li: last_li,
            ld: last_ld,
          },
        ) if last_li == c_ld => {
          // leave the delete part of the replace
          operations[last_index].kind = OperationKind::ListDelete { ld: last_ld.clone() };
        }
        (OperationKind::ObjectInsert { oi: c_oi }, OperationKind::ObjectDelete { od: last_od }) => {
          operations[last_index].kind = OperationKind::ObjectReplace {
            od: last_od.clone(),
            oi: c_oi.clone(),
          };
        }
        (
          OperationKind::ObjectReplace { oi: c_oi, .. },
          OperationKind::ObjectInsert { oi: last_oi } | OperationKind::ObjectReplace { oi: last_oi, .. },
        ) => {
          // The last path component inserted something that the new component deletes (or replaces).
          // Just merge them.
          *last_oi = c_oi.clone();
        }
        (OperationKind::ObjectDelete { .. }, OperationKind::ObjectReplace { od: last_od, .. }) => {
          operations[last_index].kind = OperationKind::ObjectDelete { od: last_od.clone() };
        }
        (OperationKind::ObjectDelete { .. }, OperationKind::ObjectInsert { .. }) => {
          // An insert directly followed by a delete turns into a no-op and can be removed.
          operations.pop();
        }
        (OperationKind::ListMove { lm }, _) if c.p[c.p.len() - 1] == *lm => {
          // don't do anything
        }
        (OperationKind::StringInsert { si: c_si }, OperationKind::StringInsert { si: _last_si }) => {
          match (c.p.last().unwrap(), operations[last_index].p.last().unwrap()) {
            (PathSegment::Number(n1), PathSegment::Number(n2)) => {
              if *n1 == *n2 + 1 {
                // Append the new string to the existing one
                if let OperationKind::StringInsert { si } = &mut operations[last_index].kind {
                  si.push_str(&c_si);
                }
              } else if *n1 == *n2 {
                // If the paths are equal, replace the existing string with the new one
                if let OperationKind::StringInsert { si } = &mut operations[last_index].kind {
                  *si = c_si.clone();
                }
              } else {
                // If the paths are not adjacent or equal, push the operation as is
                operations.push(c);
              }
            }
            _ => operations.push(c),
          }
        }
        _ => {
          operations.push(c);
        }
      }
    } else {
      operations.push(c);
    }

    Ok(())
  }

  /// Returns the common length of the paths of ops a and b
  fn common_length_for_ops(a: &Operation, b: &Operation) -> Option<isize> {
    let mut alen = a.p.len() as isize;
    let mut blen = b.p.len() as isize;
    if matches!(a.kind, OperationKind::NumberAdd { .. }) {
      alen += 1;
    }

    if matches!(b.kind, OperationKind::NumberAdd { .. }) {
      blen += 1;
    }

    if alen == 0 {
      return Some(-1isize);
    }
    if blen == 0 {
      return None;
    }

    alen -= 1;
    blen -= 1;

    for i in 0..alen as usize {
      if i >= blen as usize || a.p[i] != b.p[i] {
        return None;
      }
    }

    Some(alen)
  }

  /// transform c so it applies to a document with other_c applied.
  fn transform_component(
    dest: &mut Vec<Operation>,
    mut c: Operation,
    other_c: Operation,
    side: TransformSide,
  ) -> anyhow::Result<()> {
    let common = Self::common_length_for_ops(&other_c, &c);
    let common2 = Self::common_length_for_ops(&c, &&other_c);
    let mut cplength = c.p.len();
    let mut other_c_plength = other_c.p.len();

    if matches!(c.kind, OperationKind::NumberAdd { .. }) {
      cplength += 1;
    }

    if matches!(other_c.kind, OperationKind::NumberAdd { .. }) {
      other_c_plength += 1;
    }

    // if c is deleting something, and that thing is changed by otherC, we need to
    // update c to reflect that change for invertibility.
    if let Some(common2) = common2 {
      // NOTE common2 may be (-1isize as usize)
      let common2 = common2 as usize;
      if other_c_plength > cplength && c.p.get(common2) == other_c.p.get(common2) {
        match &mut c.kind {
          OperationKind::ListDelete { ld } | OperationKind::ListReplace { ld, .. } => {
            *ld = Self::apply(
              ld.clone(),
              vec![Operation {
                p: other_c.p[cplength..].to_vec(),
                kind: other_c.kind.clone(),
              }],
            )?;
          }
          OperationKind::ObjectDelete { od } | OperationKind::ObjectReplace { od, .. } => {
            *od = Self::apply(
              od.clone(),
              vec![Operation {
                p: other_c.p[cplength..].to_vec(),
                kind: other_c.kind.clone(),
              }],
            )?;
          }
          _ => {}
        }
      }
    }

    if let Some(common) = common {
      // NOTE common may be (-1isize as usize)
      let common = common as usize;
      let common_operand = cplength == other_c_plength;

      // transform based on otherC
      match other_c.kind {
        OperationKind::NumberAdd { .. } => {
          // this case is handled below
        }
        OperationKind::ListReplace { li: other_c_li, .. } => {
          if other_c.p.get(common) == c.p.get(common) {
            // noop

            if !common_operand {
              return Ok(());
            } else if let OperationKind::ListReplace { ld: c_ld, .. } = &mut c.kind {
              // we're trying to delete the same element, -> noop
              if side == TransformSide::Left {
                // we're both replacing one element with another. only one can survive
                *c_ld = other_c_li.clone();
              } else {
                return Ok(());
              }
            } else if let OperationKind::ListDelete { .. } = c.kind {
              return Ok(());
            }
          }
        }
        OperationKind::ListInsert { .. } => {
          match &c.kind {
            OperationKind::ListInsert { .. } if common_operand && c.p.get(common) == other_c.p.get(common) => {
              // in li vs. li, left wins.
              if side == TransformSide::Right {
                c.p[common].increment(1);
              }
            }
            _ if other_c.p.get(common) <= c.p.get(common) => {
              c.p[common].increment(1);
            }
            _ => {}
          }
          match &mut c.kind {
            OperationKind::ListMove { lm: c_lm } if common_operand => {
              // otherC edits the same list we edit
              if other_c.p[common] <= *c_lm {
                *c_lm += 1;
                // changing c.from is handled above.
              }
            }
            _ => {}
          }
        }
        OperationKind::ListDelete { .. } => {
          if let OperationKind::ListMove { lm: c_lm } = &mut c.kind {
            if common_operand {
              if other_c.p.get(common) == c.p.get(common) {
                // they deleted the thing we're trying to move
                return Ok(());
              }
              // otherC edits the same list we edit
              let p = &other_c.p[common];
              let from = &c.p[common];
              let to = *c_lm;
              if *p < to || *p == to && *from < to {
                *c_lm -= 1;
              }
            }
          }
          if other_c.p.get(common) < c.p.get(common) {
            c.p[common].increment(-1);
          } else if other_c.p.get(common) == c.p.get(common) {
            if other_c_plength < cplength {
              // we're below the deleted element, so -> noop
              return Ok(());
            } else {
              match c.kind {
                OperationKind::ListReplace { ld: _, li: c_li } => {
                  // we're replacing, they're deleting. we become an insert.
                  c.kind = OperationKind::ListInsert { li: c_li };
                }
                OperationKind::ListDelete { .. } => {
                  // we're trying to delete the same element, -> noop
                  return Ok(());
                }
                _ => {}
              }
            }
          }
        }
        OperationKind::ListMove { lm: other_c_lm } => {
          match &mut c.kind {
            OperationKind::ListMove { lm: c_lm } if cplength == other_c_plength => {
              // lm vs lm, here we go!
              let from = c.p[common].clone().unwrap_number();
              let to = *c_lm;
              let other_from = &other_c.p[common];
              let other_to = other_c_lm;
              if *other_from != other_to {
                // if otherFrom == otherTo, we don't need to change our op.

                // where did my thing go?
                if *other_from == from {
                  // they moved it! tie break.
                  if side == TransformSide::Left {
                    c.p[common] = PathSegment::Number(other_to);
                    if from == to {
                      // ugh
                      *c_lm = other_to;
                    }
                  } else {
                    return Ok(());
                  }
                } else {
                  // they moved around it
                  if *other_from < from {
                    c.p[common].increment(-1);
                  }
                  if from > other_to {
                    c.p[common].increment(1);
                  } else if from == other_to {
                    if *other_from > other_to {
                      c.p[common].increment(1);
                      if from == to {
                        // ugh, again
                        *c_lm += 1;
                      }
                    }
                  }

                  // step 2: where am i going to put it?
                  if *other_from < to {
                    *c_lm -= 1;
                  } else if *other_from == to {
                    if from < to {
                      *c_lm -= 1;
                    }
                  }
                  if to > other_to {
                    *c_lm += 1;
                  } else if to == other_to {
                    // if we're both moving in the same direction, tie break
                    if *other_from < other_to && from < to || *other_from > other_to && from > to {
                      if side == TransformSide::Right {
                        *c_lm += 1;
                      }
                    } else {
                      if from < to {
                        *c_lm += 1;
                      } else if *other_from == to {
                        *c_lm -= 1;
                      }
                    }
                  }
                }
              }
            }
            OperationKind::ListInsert { .. } if common_operand => {
              // li
              let from = &other_c.p[common];
              let to = other_c_lm;
              let p = c.p[common].clone().unwrap_number();
              if *from < p {
                c.p[common].increment(-1);
              }
              if p > to {
                c.p[common].increment(1);
              }
            }
            _ => {
              // ld, ld+li, si, sd, na, oi, od, oi+od, any li on an element beneath
              // the lm
              //
              // i.e. things care about where their item is after the move.
              let from = &other_c.p[common];
              let to = other_c_lm;
              let p = c.p[common].clone().unwrap_number();
              if *from == p {
                c.p[common] = PathSegment::Number(to);
              } else {
                if *from < p {
                  c.p[common].increment(-1);
                }
                if p > to {
                  c.p[common].increment(1);
                } else if p == to && *from > to {
                  c.p[common].increment(1);
                }
              }
            }
          }
        }
        OperationKind::ObjectReplace { oi: other_c_oi, .. } => {
          if c.p.get(common) == other_c.p.get(common) {
            if let OperationKind::ObjectInsert { oi: c_oi } | OperationKind::ObjectReplace { oi: c_oi, .. } = c.kind {
              if common_operand {
                // we inserted where someone else replaced
                if side == TransformSide::Right {
                  // left wins
                  return Ok(());
                } else {
                  // we win, make our op replace what they inserted
                  c.kind = OperationKind::ObjectReplace {
                    od: other_c_oi,
                    oi: c_oi,
                  };
                }
              } else {
                // -> noop if the other component is deleting the same object (or any parent)
                return Ok(());
              }
            } else {
              // -> noop if the other component is deleting the same object (or any parent)
              return Ok(());
            }
          }
        }
        OperationKind::ObjectInsert { oi: other_c_oi } => {
          if let OperationKind::ObjectInsert { .. } | OperationKind::ObjectReplace { .. } = c.kind {
            if c.p.get(common) == other_c.p.get(common) {
              // left wins if we try to insert at the same place
              if side == TransformSide::Left {
                Self::append(
                  dest,
                  Operation {
                    p: c.p.clone(),
                    kind: OperationKind::ObjectDelete { od: other_c_oi },
                  },
                )?;
              } else {
                return Ok(());
              }
            }
          }
        }
        OperationKind::ObjectDelete { .. } => {
          if c.p.get(common) == other_c.p.get(common) {
            if !common_operand {
              return Ok(());
            }
            match c.kind {
              OperationKind::ObjectReplace { oi: c_oi, .. } => {
                c.kind = OperationKind::ObjectInsert { oi: c_oi };
              }
              OperationKind::ObjectInsert { .. } => {
                // do nothing
              }
              _ => {
                return Ok(());
              }
            }
          }
        }
        OperationKind::StringDelete { sd: _other_c_sd } => {
          // TODO: implement
          return Err(anyhow!("StringDelete not supported"));
        }
        OperationKind::StringInsert { si: _ } => {
          // TODO: implement
          return Err(anyhow!("StringInsert not supported"));
        }
        OperationKind::SubtypeOperation { t: _, o: _ } => {
          // TODO: implement
          return Err(anyhow!("SubtypeOperation not supported"));
        }
      }
    }

    Self::append(dest, c)
  }

  /// Transforms op with specified side ('left' or 'right') by other_op.
  pub fn transform(
    mut op: Vec<Operation>,
    mut other_op: Vec<Operation>,
    side: TransformSide,
  ) -> anyhow::Result<Vec<Operation>> {
    if other_op.is_empty() {
      return Ok(op);
    }

    if op.len() == 1 && other_op.len() == 1 {
      let mut new_op = vec![];
      Self::transform_component(&mut new_op, op.pop().unwrap(), other_op.pop().unwrap(), side)?;
      return Ok(new_op);
    }

    match side {
      TransformSide::Left => Ok(Self::transform_x(op, other_op)?.0),
      TransformSide::Right => Ok(Self::transform_x(other_op, op)?.1),
    }
  }

  pub fn transform_x(
    mut left_op: Vec<Operation>,
    right_op: Vec<Operation>,
  ) -> anyhow::Result<(Vec<Operation>, Vec<Operation>)> {
    Self::check_valid_op(&left_op)?;
    Self::check_valid_op(&right_op)?;

    let mut new_right_op = vec![];

    for right_component in right_op {
      let mut right_component = Some(right_component);

      // Generate newLeftOp by composing leftOp by rightComponent
      let mut new_left_op = vec![];
      let mut k = 0usize;
      while k < left_op.len() {
        let mut next_c = vec![];
        Self::transform_component_x(
          left_op[k].clone(),
          right_component.clone().unwrap(),
          &mut new_left_op,
          &mut next_c,
        )?;
        k += 1;

        if next_c.len() == 1 {
          right_component = next_c.pop();
        } else if next_c.is_empty() {
          for j in k..left_op.len() {
            Self::append(&mut new_left_op, left_op[j].clone())?;
          }
          right_component = None;
          break;
        } else {
          // Recurse.
          let pair = Self::transform_x(left_op[k..].to_vec(), next_c)?;
          for c in pair.0 {
            Self::append(&mut new_left_op, c)?;
          }
          for c in pair.1 {
            Self::append(&mut new_right_op, c)?;
          }
          right_component = None;
          break;
        };
      }

      if let Some(right_component) = right_component {
        Self::append(&mut new_right_op, right_component)?;
      }
      left_op = new_left_op;
    }
    Ok((left_op, new_right_op))
  }

  fn transform_component_x(
    left: Operation,
    right: Operation,
    dest_left: &mut Vec<Operation>,
    dest_right: &mut Vec<Operation>,
  ) -> anyhow::Result<()> {
    Self::transform_component(dest_left, left.clone(), right.clone(), TransformSide::Left)?;
    Self::transform_component(dest_right, right, left, TransformSide::Right)?;
    Ok(())
  }
}

#[cfg(test)]
mod tests {
  use super::*;
  use claims::{assert_ok, assert_ok_eq};
  use proptest::{
    prelude::*,
    strategy::{NewTree, ValueTree},
    test_runner::TestRunner,
  };
  use rand::distributions::{Alphanumeric, DistString};

  mod outside_functions {
    use serde_json::json;
    use super::*;

    #[test]
    fn test_convert_from_text() {
      let mut c: HashMap<String, Value> = HashMap::new();
      c.insert("p".to_owned(), json!([1]));
      c.insert("si".to_owned(), Value::String("bc".to_string()));

      println!("{:?}", c);

      convert_from_text(&mut c);

      println!("{:?}", c);
    }
  }

  mod sanity {
    use super::*;

    #[test]
    fn create() {
      let result = JSON0::create_default();
      assert!(result.is_none());
      assert_eq!(result, None);
    }

    mod compose {
      use super::*;
      use claims::assert_ok_eq;

      #[test]
      fn od_oi_to_od_oi() {
        // assert.deepEqual [{p:['foo'], od:1, oi:2}], type.compose [{p:['foo'],od:1}],[{p:['foo'],oi:2}]
        assert_ok_eq!(
          JSON0::compose(
            vec![Operation {
              p: vec!["foo".into()],
              kind: OperationKind::ObjectDelete {
                od: Value::Number(1.into())
              },
            }],
            vec![Operation {
              p: vec!["foo".into()],
              kind: OperationKind::ObjectInsert {
                oi: Value::Number(2.into())
              },
            }],
          ),
          vec![Operation {
            p: vec!["foo".into()],
            kind: OperationKind::ObjectReplace {
              od: Value::Number(1.into()),
              oi: Value::Number(2.into()),
            },
          }]
        );

        // assert.deepEqual [{p:['foo'], od:1},{p:['bar'], oi:2}], type.compose [{p:['foo'],od:1}],[{p:['bar'],oi:2}]
        assert_ok_eq!(
          JSON0::compose(
            vec![Operation {
              p: vec!["foo".into()],
              kind: OperationKind::ObjectDelete {
                od: Value::Number(1.into())
              },
            }],
            vec![Operation {
              p: vec!["bar".into()],
              kind: OperationKind::ObjectInsert {
                oi: Value::Number(2.into())
              },
            }],
          ),
          vec![
            Operation {
              p: vec!["foo".into()],
              kind: OperationKind::ObjectDelete {
                od: Value::Number(1.into())
              },
            },
            Operation {
              p: vec!["bar".into()],
              kind: OperationKind::ObjectInsert {
                oi: Value::Number(2.into())
              },
            },
          ]
        );
      }

      #[test]
      fn merges_od_oi_to_od_oi() {
        // assert.deepEqual [{p:['foo'], od:1, oi:2}], type.compose [{p:['foo'],od:1,oi:3}],[{p:['foo'],od:3,oi:2}]
        assert_ok_eq!(
          JSON0::compose(
            vec![Operation {
              p: vec!["foo".into()],
              kind: OperationKind::ObjectReplace {
                od: Value::Number(1.into()),
                oi: Value::Number(3.into())
              }
            }],
            vec![Operation {
              p: vec!["foo".into()],
              kind: OperationKind::ObjectReplace {
                od: Value::Number(3.into()),
                oi: Value::Number(2.into())
              },
            }],
          ),
          vec![Operation {
            p: vec!["foo".into()],
            kind: OperationKind::ObjectReplace {
              od: Value::Number(1.into()),
              oi: Value::Number(2.into()),
            },
          }]
        );
      }
    }

    mod transform {
      use super::*;
      use claims::assert_ok_eq;

      #[test]
      fn returns_same_values() {
        // assert.deepEqual [], type.transform [], []
        assert_ok_eq!(JSON0::transform(vec![], vec![], TransformSide::Left), vec![]);

        // assert.deepEqual [{p:['foo'], oi:1}], type.transform [], [{p:['foo'], oi:1}]
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec!["foo".into()],
              kind: OperationKind::ObjectInsert {
                oi: Value::Number(1.into()),
              },
            }],
            vec![],
            TransformSide::Right
          ),
          vec![Operation {
            p: vec!["foo".into()],
            kind: OperationKind::ObjectInsert {
              oi: Value::Number(1.into()),
            },
          }]
        );

        // assert.deepEqual [{p:['foo'], oi:1}], type.transform [{p:['foo'], oi:1}], []
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec!["foo".into()],
              kind: OperationKind::ObjectInsert {
                oi: Value::Number(1.into()),
              },
            }],
            vec![Operation {
              p: vec!["bar".into()],
              kind: OperationKind::ObjectInsert {
                oi: Value::Number(2.into()),
              },
            }],
            TransformSide::Left
          ),
          vec![Operation {
            p: vec!["foo".into()],
            kind: OperationKind::ObjectInsert {
              oi: Value::Number(1.into()),
            },
          }]
        );
      }
    }
  }

  mod number {
    use super::*;
    use claims::{assert_ok, assert_ok_eq};
    use serde_json::json;

    #[test]
    fn adds_a_number() {
      // assert.deepEqual 3, type.apply 1, [{p:[], na:2}]
      assert_ok_eq!(
        JSON0::apply(
          Value::Number(serde_json::Number::from_f64(1.0).unwrap()),
          vec![Operation {
            p: vec![],
            kind: OperationKind::NumberAdd { na: 2.0 }
          }]
        ),
        json!(3.0),
      );

      // assert.deepEqual [3], type.apply [1], [{p:[0], na:2}]
      assert_ok_eq!(
        JSON0::apply(
          Value::Array(vec![Value::Number(serde_json::Number::from_f64(1.0).unwrap())]),
          vec![Operation {
            p: vec![0usize.into()],
            kind: OperationKind::NumberAdd { na: 2.0 }
          }]
        ),
        json!([3.0]),
      );
    }

    #[test]
    fn compress_two_adds_together_in_compose() {
      // assert.deepEqual [{p:['a', 'b'], na:3}], type.compose [{p:['a', 'b'], na:1}], [{p:['a', 'b'], na:2}]
      assert_ok_eq!(
        JSON0::compose(
          vec![Operation {
            p: vec!["a".into(), "b".into()],
            kind: OperationKind::NumberAdd { na: 1.0 },
          }],
          vec![Operation {
            p: vec!["a".into(), "b".into()],
            kind: OperationKind::NumberAdd { na: 2.0 },
          }],
        ),
        vec![Operation {
          p: vec!["a".into(), "b".into()],
          kind: OperationKind::NumberAdd { na: 3.0 },
        }]
      );

      // assert.deepEqual [{p:['a'], na:1}, {p:['b'], na:2}], type.compose [{p:['a'], na:1}], [{p:['b'], na:2}]
      assert_ok_eq!(
        JSON0::compose(
          vec![Operation {
            p: vec!["a".into()],
            kind: OperationKind::NumberAdd { na: 1.0 },
          }],
          vec![Operation {
            p: vec!["b".into()],
            kind: OperationKind::NumberAdd { na: 2.0 },
          }],
        ),
        vec![
          Operation {
            p: vec!["a".into()],
            kind: OperationKind::NumberAdd { na: 1.0 },
          },
          Operation {
            p: vec!["b".into()],
            kind: OperationKind::NumberAdd { na: 2.0 },
          }
        ]
      );
    }

    #[test]
    fn does_not_overwrite_values_when_it_merges_na_in_append() {
      //  rightHas = 21
      //  leftHas = 3

      //  rightOp = [{"p":[],"od":0,"oi":15},{"p":[],"na":4},{"p":[],"na":1},{"p":[],"na":1}]
      //  leftOp = [{"p":[],"na":4},{"p":[],"na":-1}]
      //  [right_, left_] = transformX type, rightOp, leftOp

      //  s_c = type.apply rightHas, left_
      //  c_s = type.apply leftHas, right_
      // assert.deepEqual s_c, c_s
      let right_has = Value::Number(serde_json::Number::from_f64(21.0).unwrap());
      let left_has = Value::Number(serde_json::Number::from_f64(3.0).unwrap());

      let right_op = vec![
        Operation {
          p: vec![],
          kind: OperationKind::ObjectReplace {
            od: Value::Number(serde_json::Number::from_f64(0.0).unwrap()),
            oi: Value::Number(serde_json::Number::from_f64(15.0).unwrap()),
          },
        },
        Operation {
          p: vec![],
          kind: OperationKind::NumberAdd { na: 4.0 },
        },
        Operation {
          p: vec![],
          kind: OperationKind::NumberAdd { na: 1.0 },
        },
        Operation {
          p: vec![],
          kind: OperationKind::NumberAdd { na: 1.0 },
        },
      ];
      let left_op = vec![
        Operation {
          p: vec![],
          kind: OperationKind::NumberAdd { na: 4.0 },
        },
        Operation {
          p: vec![],
          kind: OperationKind::NumberAdd { na: -1.0 },
        },
      ];
      let (right_, left_) = assert_ok!(JSON0::transform_x(right_op, left_op));

      let s_c = assert_ok!(JSON0::apply(right_has, left_));
      let c_s = assert_ok!(JSON0::apply(left_has, right_));
      assert_eq!(s_c, c_s);
    }

    #[test]
    fn throw_when_adding_a_string_to_a_number() {}

    #[test]
    fn throw_when_adding_a_number_to_a_string() {}
  }

  mod string {
    use super::*;
    

    mod apply {
      use super::*;
      use claims::assert_ok_eq;

      #[test]
      fn it_works() {
        // assert.deepEqual 'abc', type.apply 'a', [{p:[1], si:'bc'}]
        println!("apply: si");
        assert_ok_eq!(
          JSON0::apply(
            Value::String("a".to_string()),
            vec![Operation {
              p: vec![1usize.into()],
              kind: OperationKind::StringInsert { si: "bc".to_string() }
            }]
          ),
          Value::String("abc".to_string())
        );

        // assert.deepEqual 'bc', type.apply 'abc', [{p:[0], sd:'a'}]
        println!("apply: sd");
        assert_ok_eq!(
          JSON0::apply(
            Value::String("abc".to_string()),
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::StringDelete { sd: "a".to_string() }
            }]
          ),
          Value::String("bc".to_string())
        );

        // assert.deepEqual {x:'abc'}, type.apply {x:'a'}, [{p:['x', 1], si:'bc'}]
        println!("apply: si");
        assert_ok_eq!(
          JSON0::apply(
            Value::Object(
              [("x".to_string(), Value::String("a".to_string()))]
                .iter()
                .cloned()
                .collect()
            ),
            vec![Operation {
              p: vec!["x".into(), 1usize.into()],
              kind: OperationKind::StringInsert { si: "bc".to_string() }
            }]
          ),
          Value::Object(
            [("x".to_string(), Value::String("abc".to_string()))]
              .iter()
              .cloned()
              .collect()
          )
        );
      }

      #[test]
      fn throws_when_the_target_is_not_a_string() {}

      #[test]
      fn throws_when_the_inserted_content_is_not_a_string() {}
    }

    mod transform {
      
      

      #[test]
      fn splits_deletes() {
        // assert.deepEqual type.transform([{p:[0], sd:'ab'}], [{p:[1], si:'x'}], 'left'), [{p:[0], sd:'a'}, {p:[1], sd:'b'}]
        // println!("text0 related");
        // assert_ok_eq!(
        //   JSON0::transform(
        //     vec![Operation {
        //       p: vec![0usize.into()],
        //       kind: OperationKind::StringDelete { sd: "ab".to_string() }
        //     }],
        //     vec![Operation {
        //       p: vec![1usize.into()],
        //       kind: OperationKind::StringInsert { si: "x".to_string() }
        //     }],
        //     TransformSide::Left
        //   ),
        //   vec![
        //     Operation {
        //       p: vec![0usize.into()],
        //       kind: OperationKind::StringDelete { sd: "a".to_string() }
        //     },
        //     Operation {
        //       p: vec![1usize.into()],
        //       kind: OperationKind::StringDelete { sd: "b".to_string() }
        //     }
        //   ]
        // );
      }

      #[test]
      fn cancels_out_other_deletes() {
        // assert.deepEqual type.transform([{p:['k', 5], sd:'a'}], [{p:['k', 5], sd:'a'}], 'left'), []
        // println!("text0 related");
        // assert_ok_eq!(
        //   JSON0::transform(
        //     vec![Operation {
        //       p: vec!["k".into(), 5usize.into()],
        //       kind: OperationKind::StringDelete { sd: "a".to_string() }
        //     }],
        //     vec![Operation {
        //       p: vec!["k".into(), 5usize.into()],
        //       kind: OperationKind::StringDelete { sd: "a".to_string() }
        //     }],
        //     TransformSide::Left
        //   ),
        //   vec![]
        // );
      }

      #[test]
      fn does_not_throw_errors_with_blank_inserts() {
        // assert.deepEqual type.transform([{p: ['k', 5], si:''}], [{p: ['k', 3], si: 'a'}], 'left'), []
        // println!("text0 related");
        // assert_ok_eq!(
        //   JSON0::transform(
        //     vec![Operation {
        //       p: vec!["k".into(), 5usize.into()],
        //       kind: OperationKind::StringInsert { si: "".to_string() }
        //     }],
        //     vec![Operation {
        //       p: vec!["k".into(), 3usize.into()],
        //       kind: OperationKind::StringInsert { si: "a".to_string() }
        //     }],
        //     TransformSide::Left
        //   ),
        //   vec![]
        // );
      }
    }
  }

  mod string_subtype {
    
    

    mod apply {
      
      

      #[test]
      fn it_works() {
        // assert.deepEqual 'abc', type.apply 'a', [{p:[], t:'text0', o:[{p:1, i:'bc'}]}]
        // println!("test 1");
        // assert_ok_eq!(
        //   JSON0::apply(
        //     Value::String("a".to_string()),
        //     vec![Operation {
        //       p: vec![],
        //       kind: OperationKind::SubtypeOperation {
        //         t: "text0".to_string(),
        //         o: vec![Operation {
        //           p: vec![1usize.into()],
        //           kind: OperationKind::StringInsert { si: "bc".to_string() }
        //         }],
        //       },
        //     }]
        //   ),
        //   Value::String("abc".to_string())
        // );

        // assert.deepEqual 'bc', type.apply 'abc', [{p:[], t:'text0', o:[{p:0, d:'a'}]}]
        // println!("test 2");
        // assert_ok_eq!(
        //   JSON0::apply(
        //     Value::String("abc".to_string()),
        //     vec![Operation {
        //       p: vec![],
        //       kind: OperationKind::SubtypeOperation {
        //         t: "text0".to_string(),
        //         o: vec![Operation {
        //           p: vec![0usize.into()],
        //           kind: OperationKind::StringDelete { sd: "a".to_string() }
        //         }],
        //       },
        //     }]
        //   ),
        //   Value::String("bc".to_string())
        // );

        // assert.deepEqual {x:'abc'}, type.apply {x:'a'}, [{p:['x'], t:'text0', o:[{p:1, i:'bc'}]}]
        // println!("test 3");
        // assert_ok_eq!(
        //   JSON0::apply(
        //     Value::Object(
        //       [("x".to_string(), Value::String("a".to_string()))]
        //         .iter()
        //         .cloned()
        //         .collect()
        //     ),
        //     vec![Operation {
        //       p: vec!["x".into()],
        //       kind: OperationKind::SubtypeOperation {
        //         t: "text0".to_string(),
        //         o: Value::Array(vec![Value::Number(serde_json::Number::from_f64(1.0).unwrap()), Value::String("bc".to_string())]),
        //       },
        //     }]
        //   ),
        //   Value::Object(
        //     [("x".to_string(), Value::String("abc".to_string()))]
        //       .iter()
        //       .cloned()
        //       .collect()
        //   )
        // )
      }
    }

    mod transform {
      
      

      #[test]
      fn splits_deletes() {
        // assert.deepEqual type.transform([{p:[], t:'text0', o:[{p:0, d:'ab'}]}], [{p:[], t:'text0', o:[{p:1, i:'x'}]}], 'left'), [{p:[], t:'text0', o:[{p:0, d:'a'}, {p:1, d:'b'}]}]
        // assert_ok_eq!(
        // JSON0::transform(
        //   vec![Operation {
        //     p: vec![],
        //     kind: OperationKind::SubtypeOperation {
        //       t: "text0".to_string(),
        //       o: vec![Operation {
        //         p: vec![0usize.into()],
        //         kind: OperationKind::StringDelete { sd: "ab".to_string() }
        //       }],
        //     },
        //   }],
        //   vec![Operation {
        //     p: vec![],
        //     kind: OperationKind::SubtypeOperation {
        //       t: "text0".to_string(),
        //       o: vec![Operation {
        //         p: vec![1usize.into()],
        //         kind: OperationKind::StringInsert { si: "x".to_string() }
        //       }],
        //     },
        //   }],
        //   TransformSide::Left
        // ),
        // vec![Operation {
        //   p: vec![],
        //   kind: OperationKind::SubtypeOperation {
        //     t: "text0".to_string(),
        //     o: vec![
        //       Operation {
        //         p: vec![0usize.into()],
        //         kind: OperationKind::StringDelete { sd: "a".to_string() }
        //       },
        //       Operation {
        //         p: vec![1usize.into()],
        //         kind: OperationKind::StringDelete { sd: "b".to_string() }
        //       }
        //     ],
        //   },
        // }]
        // );
      }

      #[test]
      fn cancels_out_other_deletes() {
        // assert.deepEqual type.transform([{p:['k'], t:'text0', o:[{p:5, d:'a'}]}], [{p:['k'], t:'text0', o:[{p:5, d:'a'}]}], 'left'), []
        // assert_ok_eq!(
        //   JSON0::transform(
        //     vec![Operation {
        //       p: vec!["k".into()],
        //       kind: OperationKind::SubtypeOperation {
        //         t: "text0".to_string(),
        //         o: vec![Operation {
        //           p: vec![5usize.into()],
        //           kind: OperationKind::StringDelete { sd: "a".to_string() }
        //         }],
        //       },
        //     }],
        //     vec![Operation {
        //       p: vec!["k".into()],
        //       kind: OperationKind::SubtypeOperation {
        //         t: "text0".to_string(),
        //         o: vec![Operation {
        //           p: vec![5usize.into()],
        //           kind: OperationKind::StringDelete { sd: "a".to_string() }
        //         }],
        //       },
        //     }],
        //     TransformSide::Left
        //   ),
        //   vec![]
        // );
      }

      #[test]
      fn does_not_throw_errors_with_blank_inserts() {
        // assert.deepEqual type.transform([{p:['k'], t:'text0', o:[{p:5, i:''}]}], [{p:['k'], t:'text0', o:[{p:3, i:'a'}]}], 'left'), []
        //   assert_ok_eq!(
        //     JSON0::transform(
        //       vec![Operation {
        //         p: vec!["k".into()],
        //         kind: OperationKind::SubtypeOperation {
        //           t: "text0".to_string(),
        //           o: vec![Operation {
        //             p: vec![5usize.into()],
        //             kind: OperationKind::StringInsert { si: "".to_string() }
        //           }],
        //         },
        //       }],
        //       vec![Operation {
        //         p: vec!["k".into()],
        //         kind: OperationKind::SubtypeOperation {
        //           t: "text0".to_string(),
        //           o: vec![Operation {
        //             p: vec![3usize.into()],
        //             kind: OperationKind::StringInsert { si: "a".to_string() }
        //           }],
        //         },
        //       }],
        //       TransformSide::Left
        //     ),
        //     vec![]
        //   );
      }
    }
  }

  mod subtype_with_non_array_operation {
    
    

    #[test]
    fn it_works() {
      // a = [{p:[], t:'mock', o:'foo'}]
      //  b = [{p:[], t:'mock', o:'bar'}]
      // assert.deepEqual type.transform(a, b, 'left'), [{p:[], t:'mock', o:{mock:true}}]
      // let a = vec![Operation {
      //   p: vec![],
      //   kind: OperationKind::SubtypeOperation {
      //     t: "mock".to_string(),
      //     o: vec![Value::String("foo".to_string())],
      //   },
      // }];
      // let b = vec![Operation {
      //   p: vec![],
      //   kind: OperationKind::SubtypeOperation {
      //     t: "mock".to_string(),
      //     o: Value::String("bar".to_string()),
      //   },
      // }];
      // assert_ok_eq!(
      //   JSON0::transform(a, b, TransformSide::Left),
      //   vec![Operation {
      //     p: vec![],
      //     kind: OperationKind::SubtypeOperation {
      //       t: "mock".to_string(),
      //       o: Value::Object(
      //         [("mock".to_string(), Value::Boolean(true))]
      //           .iter()
      //           .cloned()
      //           .collect()
      //       ),
      //     },
      //   }]
      // );
    }
  }

  mod list {
    use super::*;
    

    mod apply {
      use super::*;
      use claims::assert_ok_eq;

      #[test]
      fn inserts() {
        // assert.deepEqual ['a', 'b', 'c'], type.apply ['b', 'c'], [{p:[0], li:'a'}]
        assert_ok_eq!(
          JSON0::apply(
            Value::Array(vec![
              Value::String("b".to_string()),
              Value::String("c".to_string())
            ]),
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::ListInsert {
                li: Value::String("a".to_string())
              }
            }]
          ),
          Value::Array(vec![
            Value::String("a".to_string()),
            Value::String("b".to_string()),
            Value::String("c".to_string())
          ])
        );

        // assert.deepEqual ['a', 'b', 'c'], type.apply ['a', 'c'], [{p:[1], li:'b'}]
        assert_ok_eq!(
          JSON0::apply(
            Value::Array(vec![
              Value::String("a".to_string()),
              Value::String("c".to_string())
            ]),
            vec![Operation {
              p: vec![1usize.into()],
              kind: OperationKind::ListInsert {
                li: Value::String("b".to_string())
              }
            }]
          ),
          Value::Array(vec![
            Value::String("a".to_string()),
            Value::String("b".to_string()),
            Value::String("c".to_string())
          ])
        );

        // assert.deepEqual ['a', 'b', 'c'], type.apply ['a', 'b'], [{p:[2], li:'c'}]
        assert_ok_eq!(
          JSON0::apply(
            Value::Array(vec![
              Value::String("a".to_string()),
              Value::String("b".to_string())
            ]),
            vec![Operation {
              p: vec![2usize.into()],
              kind: OperationKind::ListInsert {
                li: Value::String("c".to_string())
              }
            }]
          ),
          Value::Array(vec![
            Value::String("a".to_string()),
            Value::String("b".to_string()),
            Value::String("c".to_string())
          ])
        );
      }

      #[test]
      fn deletes() {
        // assert.deepEqual ['b', 'c'], type.apply ['a', 'b', 'c'], [{p:[0], ld:'a'}]
        assert_ok_eq!(
          JSON0::apply(
            Value::Array(vec![
              Value::String("a".to_string()),
              Value::String("b".to_string()),
              Value::String("c".to_string())
            ]),
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::ListDelete {
                ld: Value::String("a".to_string())
              }
            }]
          ),
          Value::Array(vec![
            Value::String("b".to_string()),
            Value::String("c".to_string())
          ])
        );

        // assert.deepEqual ['a', 'c'], type.apply ['a', 'b', 'c'], [{p:[1], ld:'b'}]
        assert_ok_eq!(
          JSON0::apply(
            Value::Array(vec![
              Value::String("a".to_string()),
              Value::String("b".to_string()),
              Value::String("c".to_string())
            ]),
            vec![Operation {
              p: vec![1usize.into()],
              kind: OperationKind::ListDelete {
                ld: Value::String("b".to_string())
              }
            }]
          ),
          Value::Array(vec![
            Value::String("a".to_string()),
            Value::String("c".to_string())
          ])
        );

        // assert.deepEqual ['a', 'b'], type.apply ['a', 'b', 'c'], [{p:[2], ld:'c'}]
        assert_ok_eq!(
          JSON0::apply(
            Value::Array(vec![
              Value::String("a".to_string()),
              Value::String("b".to_string()),
              Value::String("c".to_string())
            ]),
            vec![Operation {
              p: vec![2usize.into()],
              kind: OperationKind::ListDelete {
                ld: Value::String("c".to_string())
              }
            }]
          ),
          Value::Array(vec![
            Value::String("a".to_string()),
            Value::String("b".to_string())
          ])
        );
      }

      #[test]
      fn replaces() {
        // assert.deepEqual ['a', 'y', 'b'], type.apply ['a', 'x', 'b'], [{p:[1], ld:'x', li:'y'}]
        assert_ok_eq!(
          JSON0::apply(
            Value::Array(vec![
              Value::String("a".to_string()),
              Value::String("x".to_string()),
              Value::String("b".to_string())
            ]),
            vec![Operation {
              p: vec![1usize.into()],
              kind: OperationKind::ListReplace {
                ld: Value::String("x".to_string()),
                li: Value::String("y".to_string())
              }
            }]
          ),
          Value::Array(vec![
            Value::String("a".to_string()),
            Value::String("y".to_string()),
            Value::String("b".to_string())
          ])
        );
      }

      #[test]
      fn moves() {
        // assert.deepEqual ['a', 'b', 'c'], type.apply ['b', 'a', 'c'], [{p:[1], lm:0}]
        assert_ok_eq!(
          JSON0::apply(
            Value::Array(vec![
              Value::String("b".to_string()),
              Value::String("a".to_string()),
              Value::String("c".to_string())
            ]),
            vec![Operation {
              p: vec![1usize.into()],
              kind: OperationKind::ListMove { lm: 0 }
            }]
          ),
          Value::Array(vec![
            Value::String("a".to_string()),
            Value::String("b".to_string()),
            Value::String("c".to_string())
          ])
        );
        // assert.deepEqual ['a', 'b', 'c'], type.apply ['b', 'a', 'c'], [{p:[0], lm:1}]
        assert_ok_eq!(
          JSON0::apply(
            Value::Array(vec![
              Value::String("b".to_string()),
              Value::String("a".to_string()),
              Value::String("c".to_string())
            ]),
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::ListMove { lm: 1 }
            }]
          ),
          Value::Array(vec![
            Value::String("a".to_string()),
            Value::String("b".to_string()),
            Value::String("c".to_string())
          ])
        );
      }

      #[test]
      fn throws_when_keying_a_list_replacement_with_a_string() {}

      #[test]
      fn throws_when_keying_a_list_insertion_with_a_string() {}

      #[test]
      fn throws_when_keying_a_list_deletion_with_a_string() {}

      #[test]
      fn throws_when_keying_a_list_move_with_a_string() {}

      #[test]
      fn throws_when_specifying_a_string_as_a_list_move_target() {}

      #[test]
      fn throws_when_an_array_index_part_way_through_the_path_is_a_string() {}
    }

    mod transform {
      use super::*;
      use claims::assert_ok_eq;

      #[test]
      fn bumps_paths_when_list_elements_are_inserted_or_removed() {
        // assert.deepEqual [{p:[2, 200], si:'hi'}], type.transform [{p:[1, 200], si:'hi'}], [{p:[0], li:'x'}], 'left'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![1usize.into(), 200usize.into()],
              kind: OperationKind::StringInsert { si: "hi".to_string() }
            }],
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::ListInsert {
                li: Value::String("x".to_string())
              }
            }],
            TransformSide::Left
          ),
          vec![Operation {
            p: vec![2usize.into(), 200usize.into()],
            kind: OperationKind::StringInsert { si: "hi".to_string() }
          }]
        );

        // assert.deepEqual [{p:[1, 201], si:'hi'}], type.transform [{p:[0, 201], si:'hi'}], [{p:[0], li:'x'}], 'right'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![0usize.into(), 201usize.into()],
              kind: OperationKind::StringInsert { si: "hi".to_string() }
            }],
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::ListInsert {
                li: Value::String("x".to_string())
              }
            }],
            TransformSide::Right
          ),
          vec![Operation {
            p: vec![1usize.into(), 201usize.into()],
            kind: OperationKind::StringInsert { si: "hi".to_string() }
          }]
        );

        // assert.deepEqual [{p:[0, 202], si:'hi'}], type.transform [{p:[0, 202], si:'hi'}], [{p:[1], li:'x'}], 'left'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![0usize.into(), 202usize.into()],
              kind: OperationKind::StringInsert { si: "hi".to_string() }
            }],
            vec![Operation {
              p: vec![1usize.into()],
              kind: OperationKind::ListInsert {
                li: Value::String("x".to_string())
              }
            }],
            TransformSide::Left
          ),
          vec![Operation {
            p: vec![0usize.into(), 202usize.into()],
            kind: OperationKind::StringInsert { si: "hi".to_string() }
          }]
        );

        // assert.deepEqual [{p:[2], t:'text0', o:[{p:200, i:'hi'}]}], type.transform [{p:[1], t:'text0', o:[{p:200, i:'hi'}]}], [{p:[0], li:'x'}], 'left'
        // assert_ok_eq!(
        //   JSON0::transform(
        //     vec![Operation {
        //       p: vec![1usize.into()],
        //       kind: OperationKind::SubtypeOperation {
        //         t: "text0".to_string(),
        //         o: vec![Operation {
        //           p: vec![200usize.into()],
        //           kind: OperationKind::StringInsert { si: "hi".to_string() }
        //         }],
        //       },
        //     }],
        //     vec![Operation {
        //       p: vec![0usize.into()],
        //       kind: OperationKind::ListInsert {
        //         li: Value::String("x".to_string())
        //       }
        //     }],
        //     TransformSide::Left
        //   ),
        //   vec![Operation {
        //     p: vec![2usize.into()],
        //     kind: OperationKind::SubtypeOperation {
        //       t: "text0".to_string(),
        //       o: vec![Operation {
        //         p: vec![200usize.into()],
        //         kind: OperationKind::StringInsert { si: "hi".to_string() }
        //       }],
        //     },
        //   }]
        // );

        // assert.deepEqual [{p:[1], t:'text0', o:[{p:201, i:'hi'}]}], type.transform [{p:[0], t:'text0', o:[{p:201, i:'hi'}]}], [{p:[0], li:'x'}], 'right'
        // assert_ok_eq!(
        //   JSON0::transform(
        //     vec![Operation {
        //       p: vec![0usize.into()],
        //       kind: OperationKind::SubtypeOperation {
        //         t: "text0".to_string(),
        //         o: vec![Operation {
        //           p: vec![201usize.into()],
        //           kind: OperationKind::StringInsert { si: "hi".to_string() }
        //         }],
        //       },
        //     }],
        //     vec![Operation {
        //       p: vec![0usize.into()],
        //       kind: OperationKind::ListInsert {
        //         li: Value::String("x".to_string())
        //       }
        //     }],
        //     TransformSide::Right
        //   ),
        //   vec![Operation {
        //     p: vec![1usize.into()],
        //     kind: OperationKind::SubtypeOperation {
        //       t: "text0".to_string(),
        //       o: vec![Operation {
        //         p: vec![201usize.into()],
        //         kind: OperationKind::StringInsert { si: "hi".to_string() }
        //       }],
        //     },
        //   }]
        // );

        // assert.deepEqual [{p:[0], t:'text0', o:[{p:202, i:'hi'}]}], type.transform [{p:[0], t:'text0', o:[{p:202, i:'hi'}]}], [{p:[1], li:'x'}], 'left'
        // assert_ok_eq!(
        //   JSON0::transform(
        //     vec![Operation {
        //       p: vec![0usize.into()],
        //       kind: OperationKind::SubtypeOperation {
        //         t: "text0".to_string(),
        //         o: vec![Operation {
        //           p: vec![202usize.into()],
        //           kind: OperationKind::StringInsert { si: "hi".to_string() }
        //         }]
        //       },
        //     }],
        //     vec![Operation {
        //       p: vec![1usize.into()],
        //       kind: OperationKind::ListInsert {
        //         li: Value::String("x".to_string())
        //       }
        //     }],
        //     TransformSide::Left
        //   ),
        //   vec![Operation {
        //     p: vec![0usize.into()],
        //     kind: OperationKind::SubtypeOperation {
        //       t: "text0".to_string(),
        //       o: vec![Operation {
        //         p: vec![202usize.into()],
        //         kind: OperationKind::StringInsert { si: "hi".to_string() }
        //       }],
        //     },
        //   }]
        // );

        // assert.deepEqual [{p:[0, 203], si:'hi'}], type.transform [{p:[1, 203], si:'hi'}], [{p:[0], ld:'x'}], 'left'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![1usize.into(), 203usize.into()],
              kind: OperationKind::StringInsert { si: "hi".to_string() }
            }],
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::ListDelete {
                ld: Value::String("x".to_string())
              }
            }],
            TransformSide::Left
          ),
          vec![Operation {
            p: vec![0usize.into(), 203usize.into()],
            kind: OperationKind::StringInsert { si: "hi".to_string() }
          }]
        );

        // assert.deepEqual [{p:[0, 204], si:'hi'}], type.transform [{p:[0, 204], si:'hi'}], [{p:[1], ld:'x'}], 'left'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![0usize.into(), 204usize.into()],
              kind: OperationKind::StringInsert { si: "hi".to_string() }
            }],
            vec![Operation {
              p: vec![1usize.into()],
              kind: OperationKind::ListDelete {
                ld: Value::String("x".to_string())
              }
            }],
            TransformSide::Left
          ),
          vec![Operation {
            p: vec![0usize.into(), 204usize.into()],
            kind: OperationKind::StringInsert { si: "hi".to_string() }
          }]
        );

        // assert.deepEqual [{p:['x',3], si: 'hi'}], type.transform [{p:['x',3], si:'hi'}], [{p:['x',0,'x'], li:0}], 'left'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec!["x".into(), 3usize.into()],
              kind: OperationKind::StringInsert { si: "hi".to_string() }
            }],
            vec![Operation {
              p: vec!["x".into(), 0usize.into(), "x".into()],
              kind: OperationKind::ListInsert {
                li: Value::Number(serde_json::Number::from_f64(0.0).unwrap())
              }
            }],
            TransformSide::Left
          ),
          vec![Operation {
            p: vec!["x".into(), 3usize.into()],
            kind: OperationKind::StringInsert { si: "hi".to_string() }
          }]
        );

        // assert.deepEqual [{p:['x',3,'x'], si: 'hi'}], type.transform [{p:['x',3,'x'], si:'hi'}], [{p:['x',5], li:0}], 'left'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec!["x".into(), 3usize.into(), "x".into()],
              kind: OperationKind::StringInsert { si: "hi".to_string() }
            }],
            vec![Operation {
              p: vec!["x".into(), 5usize.into()],
              kind: OperationKind::ListInsert {
                li: Value::Number(serde_json::Number::from_f64(0.0).unwrap())
              }
            }],
            TransformSide::Left
          ),
          vec![Operation {
            p: vec!["x".into(), 3usize.into(), "x".into()],
            kind: OperationKind::StringInsert { si: "hi".to_string() }
          }]
        );

        // assert.deepEqual [{p:['x',4,'x'], si: 'hi'}], type.transform [{p:['x',3,'x'], si:'hi'}], [{p:['x',0], li:0}], 'left'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec!["x".into(), 3usize.into(), "x".into()],
              kind: OperationKind::StringInsert { si: "hi".to_string() }
            }],
            vec![Operation {
              p: vec!["x".into(), 0usize.into()],
              kind: OperationKind::ListInsert {
                li: Value::Number(serde_json::Number::from_f64(0.0).unwrap())
              }
            }],
            TransformSide::Left
          ),
          vec![Operation {
            p: vec!["x".into(), 4usize.into(), "x".into()],
            kind: OperationKind::StringInsert { si: "hi".to_string() }
          }]
        );

        // assert.deepEqual [{p:[0], t:'text0', o:[{p:203, i:'hi'}]}], type.transform [{p:[1], t:'text0', o:[{p:203, i:'hi'}]}], [{p:[0], ld:'x'}], 'left'
        // assert_ok_eq!(
        //   JSON0::transform(
        //     vec![Operation {
        //       p: vec![1usize.into()],
        //       kind: OperationKind::SubtypeOperation {
        //         t: "text0".to_string(),
        //         o: vec![Operation {
        //           p: vec![203usize.into()],
        //           kind: OperationKind::StringInsert { si: "hi".to_string() }
        //         }],
        //       },
        //     }],
        //     vec![Operation {
        //       p: vec![0usize.into()],
        //       kind: OperationKind::ListDelete {
        //         ld: Value::String("x".to_string())
        //       }
        //     }],
        //     TransformSide::Left
        //   ),
        //   vec![Operation {
        //     p: vec![0usize.into()],
        //     kind: OperationKind::SubtypeOperation {
        //       t: "text0".to_string(),
        //       o: vec![Operation {
        //         p: vec![203usize.into()],
        //         kind: OperationKind::StringInsert { si: "hi".to_string() }
        //       }],
        //     },
        //   }]
        // );

        // assert.deepEqual [{p:[0], t:'text0', o:[{p:204, i:'hi'}]}], type.transform [{p:[0], t:'text0', o:[{p:204, i:'hi'}]}], [{p:[1], ld:'x'}], 'left'
        // assert_ok_eq!(
        //   JSON0::transform(
        //     vec![Operation {
        //       p: vec![0usize.into()],
        //       kind: OperationKind::SubtypeOperation {
        //         t: "text0".to_string(),
        //         o: vec![Operation {
        //           p: vec![204usize.into()],
        //           kind: OperationKind::StringInsert { si: "hi".to_string() }
        //         }],
        //       },
        //     }],
        //     vec![Operation {
        //       p: vec![1usize.into()],
        //       kind: OperationKind::ListDelete {
        //         ld: Value::String("x".to_string())
        //       }
        //     }],
        //     TransformSide::Left
        //   ),
        //   vec![Operation {
        //     p: vec![0usize.into()],
        //     kind: OperationKind::SubtypeOperation {
        //       t: "text0".to_string(),
        //       o: vec![Operation {
        //         p: vec![204usize.into()],
        //         kind: OperationKind::StringInsert { si: "hi".to_string() }
        //       }],
        //     },
        //   }]
        // );

        // assert.deepEqual [{p:['x'], t:'text0', o:[{p:3,i: 'hi'}]}], type.transform [{p:['x'], t:'text0', o:[{p:3, i:'hi'}]}], [{p:['x',0,'x'], li:0}], 'left'
        // assert_ok_eq!(
        //   JSON0::transform(
        //     vec![Operation {
        //       p: vec!["x".into()],
        //       kind: OperationKind::SubtypeOperation {
        //         t: "text0".to_string(),
        //         o: vec![Operation {
        //           p: vec![3usize.into()],
        //           kind: OperationKind::StringInsert { si: "hi".to_string() }
        //         }],
        //       },
        //     }],
        //     vec![Operation {
        //       p: vec!["x".into(), 0usize.into(), "x".into()],
        //       kind: OperationKind::ListInsert {
        //         li: Value::Number(serde_json::Number::from_f64(0.0).unwrap())
        //       }
        //     }],
        //     TransformSide::Left
        //   ),
        //   vec![Operation {
        //     p: vec!["x".into()],
        //     kind: OperationKind::SubtypeOperation {
        //       t: "text0".to_string(),
        //       o: vec![Operation {
        //         p: vec![3usize.into()],
        //         kind: OperationKind::StringInsert { si: "hi".to_string() }
        //       }],
        //     },
        //   }]
        // );

        // assert.deepEqual [{p:[1],ld:2}], type.transform [{p:[0],ld:2}], [{p:[0],li:1}], 'left'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::ListDelete {
                ld: Value::Number(serde_json::Number::from_f64(2.0).unwrap())
              }
            }],
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::ListInsert {
                li: Value::Number(serde_json::Number::from_f64(1.0).unwrap())
              }
            }],
            TransformSide::Left
          ),
          vec![Operation {
            p: vec![1usize.into()],
            kind: OperationKind::ListDelete {
              ld: Value::Number(serde_json::Number::from_f64(2.0).unwrap())
            }
          }]
        );

        // assert.deepEqual [{p:[1],ld:2}], type.transform [{p:[0],ld:2}], [{p:[0],li:1}], 'right'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::ListDelete {
                ld: Value::Number(serde_json::Number::from_f64(2.0).unwrap())
              }
            }],
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::ListInsert {
                li: Value::Number(serde_json::Number::from_f64(1.0).unwrap())
              }
            }],
            TransformSide::Right
          ),
          vec![Operation {
            p: vec![1usize.into()],
            kind: OperationKind::ListDelete {
              ld: Value::Number(serde_json::Number::from_f64(2.0).unwrap())
            }
          }]
        );
      }

      #[test]
      fn converts_ops_on_deleted_elements_to_noops() {
        // assert.deepEqual [], type.transform [{p:[1, 0], si:'hi'}], [{p:[1], ld:'x'}], 'left'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![1usize.into(), 0usize.into()],
              kind: OperationKind::StringInsert { si: "hi".to_string() }
            }],
            vec![Operation {
              p: vec![1usize.into()],
              kind: OperationKind::ListDelete {
                ld: Value::String("x".to_string())
              }
            }],
            TransformSide::Left
          ),
          vec![]
        );

        // assert.deepEqual [], type.transform [{p:[1], t:'text0', o:[{p:0, i:'hi'}]}], [{p:[1], ld:'x'}], 'left'
        // assert_ok_eq!(
        //   JSON0::transform(
        //     vec![Operation {
        //       p: vec![1usize.into()],
        //       kind: OperationKind::SubtypeOperation {
        //         t: "text0".to_string(),
        //         o: vec![Operation {
        //           p: vec![0usize.into()],
        //           kind: OperationKind::StringInsert { si: "hi".to_string() }
        //         }],
        //       },
        //     }],
        //     vec![Operation {
        //       p: vec![1usize.into()],
        //       kind: OperationKind::ListDelete {
        //         ld: Value::String("x".to_string())
        //       }
        //     }],
        //     TransformSide::Left
        //   ),
        //   vec![]
        // );

        // assert.deepEqual [{p:[0],li:'x'}], type.transform [{p:[0],li:'x'}], [{p:[0],ld:'y'}], 'left'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::ListInsert {
                li: Value::String("x".to_string())
              }
            }],
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::ListDelete {
                ld: Value::String("y".to_string())
              }
            }],
            TransformSide::Left
          ),
          vec![Operation {
            p: vec![0usize.into()],
            kind: OperationKind::ListInsert {
              li: Value::String("x".to_string())
            }
          }]
        );

        // assert.deepEqual [], type.transform [{p:[0],na:-3}], [{p:[0],ld:48}], 'left'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::NumberAdd { na: -3.0 }
            }],
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::ListDelete {
                ld: Value::Number(serde_json::Number::from_f64(48.0).unwrap())
              }
            }],
            TransformSide::Left
          ),
          vec![]
        );
      }

      #[test]
      fn converts_ops_on_replaced_elements_to_noops() {
        // assert.deepEqual [], type.transform [{p:[1, 0], si:'hi'}], [{p:[1], ld:'x', li:'y'}], 'left'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![1usize.into(), 0usize.into()],
              kind: OperationKind::StringInsert { si: "hi".to_string() }
            }],
            vec![Operation {
              p: vec![1usize.into()],
              kind: OperationKind::ListReplace {
                ld: Value::String("x".to_string()),
                li: Value::String("y".to_string())
              }
            }],
            TransformSide::Left
          ),
          vec![]
        );

        // assert.deepEqual [], type.transform [{p:[1], t:'text0', o:[{p:0, i:'hi'}]}], [{p:[1], ld:'x', li:'y'}], 'left'
        // assert_ok_eq!(
        //   JSON0::transform(
        //     vec![Operation {
        //       p: vec![1usize.into()],
        //       kind: OperationKind::SubtypeOperation {
        //         t: "text0".to_string(),
        //         o: vec![Operation {
        //           p: vec![0usize.into()],
        //           kind: OperationKind::StringInsert { si: "hi".to_string() }
        //         }],
        //       },
        //     }],
        //     vec![Operation {
        //       p: vec![1usize.into()],
        //       kind: OperationKind::ListReplace {
        //         ld: Value::String("x".to_string()),
        //         li: Value::String("y".to_string())
        //       }
        //     }],
        //     TransformSide::Left
        //   ),
        //   vec![]
        // );

        // assert.deepEqual [{p:[0], li:'hi'}], type.transform [{p:[0], li:'hi'}], [{p:[0], ld:'x', li:'y'}], 'left'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::ListInsert {
                li: Value::String("hi".to_string())
              }
            }],
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::ListReplace {
                ld: Value::String("x".to_string()),
                li: Value::String("y".to_string())
              }
            }],
            TransformSide::Left
          ),
          vec![Operation {
            p: vec![0usize.into()],
            kind: OperationKind::ListInsert {
              li: Value::String("hi".to_string())
            }
          }]
        );
      }

      #[test]
      fn changes_deleted_data_to_reflect_edits() {
        // assert.deepEqual [{p:[1], ld:'abc'}], type.transform [{p:[1], ld:'a'}], [{p:[1, 1], si:'bc'}], 'left'
        println!("test 1");
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![1usize.into()],
              kind: OperationKind::ListDelete {
                ld: Value::String("a".to_string())
              }
            }],
            vec![Operation {
              p: vec![1usize.into(), 1usize.into()],
              kind: OperationKind::StringInsert { si: "bc".to_string() }
            }],
            TransformSide::Left
          ),
          vec![Operation {
            p: vec![1usize.into()],
            kind: OperationKind::ListDelete {
              ld: Value::String("abc".to_string())
            }
          }]
        );

        // assert.deepEqual [{p:[1], ld:'abc'}], type.transform [{p:[1], ld:'a'}], [{p:[1], t:'text0', o:[{p:1, i:'bc'}]}], 'left'
        // println!("test 2");
        // assert_ok_eq!(
        //   JSON0::transform(
        //     vec![Operation {
        //       p: vec![1usize.into()],
        //       kind: OperationKind::ListDelete {
        //         ld: Value::String("a".to_string())
        //       }
        //     }],
        //     vec![Operation {
        //       p: vec![1usize.into()],
        //       kind: OperationKind::SubtypeOperation {
        //         t: "text0".to_string(),
        //         o: vec![Operation {
        //           p: vec![1usize.into()],
        //           kind: OperationKind::StringInsert { si: "bc".to_string() }
        //         }]
        //       },
        //     }],
        //     TransformSide::Left
        //   ),
        //   vec![Operation {
        //     p: vec![1usize.into()],
        //     kind: OperationKind::ListDelete {
        //       ld: Value::String("abc".to_string())
        //     }
        //   }]
        // );
      }

      #[test]
      fn puts_the_left_op_first_if_two_inserts_are_simultaneous() {
        // assert.deepEqual [{p:[1], li:'a'}], type.transform [{p:[1], li:'a'}], [{p:[1], li:'b'}], 'left'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![1usize.into()],
              kind: OperationKind::ListInsert {
                li: Value::String("a".to_string())
              }
            }],
            vec![Operation {
              p: vec![1usize.into()],
              kind: OperationKind::ListInsert {
                li: Value::String("b".to_string())
              }
            }],
            TransformSide::Left
          ),
          vec![Operation {
            p: vec![1usize.into()],
            kind: OperationKind::ListInsert {
              li: Value::String("a".to_string())
            }
          }]
        );

        // assert.deepEqual [{p:[2], li:'b'}], type.transform [{p:[1], li:'b'}], [{p:[1], li:'a'}], 'right'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![1usize.into()],
              kind: OperationKind::ListInsert {
                li: Value::String("b".to_string())
              }
            }],
            vec![Operation {
              p: vec![1usize.into()],
              kind: OperationKind::ListInsert {
                li: Value::String("a".to_string())
              }
            }],
            TransformSide::Right
          ),
          vec![Operation {
            p: vec![2usize.into()],
            kind: OperationKind::ListInsert {
              li: Value::String("b".to_string())
            }
          }]
        );
      }

      #[test]
      fn converts_an_attempt_to_redelete_a_list_element_into_a_no_op() {
        // assert.deepEqual [], type.transform [{p:[1], ld:'x'}], [{p:[1], ld:'x'}], 'left'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![1usize.into()],
              kind: OperationKind::ListDelete {
                ld: Value::String("x".to_string())
              }
            }],
            vec![Operation {
              p: vec![1usize.into()],
              kind: OperationKind::ListDelete {
                ld: Value::String("x".to_string())
              }
            }],
            TransformSide::Left
          ),
          vec![]
        );

        // assert.deepEqual [], type.transform [{p:[1], ld:'x'}], [{p:[1], ld:'x'}], 'right'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![1usize.into()],
              kind: OperationKind::ListDelete {
                ld: Value::String("x".to_string())
              }
            }],
            vec![Operation {
              p: vec![1usize.into()],
              kind: OperationKind::ListDelete {
                ld: Value::String("x".to_string())
              }
            }],
            TransformSide::Right
          ),
          vec![]
        );
      }
    }

    mod compose {
      use super::*;
      use claims::assert_ok_eq;
      use serde_json::json;

      #[test]
      fn composes_insert_then_delete_into_a_no_op() {
        // assert.deepEqual [], type.compose [{p:[1], li:'abc'}], [{p:[1], ld:'abc'}]
        assert_ok_eq!(
          JSON0::compose(
            vec![Operation {
              p: vec![1usize.into()],
              kind: OperationKind::ListInsert {
                li: Value::String("abc".to_string())
              }
            }],
            vec![Operation {
              p: vec![1usize.into()],
              kind: OperationKind::ListDelete {
                ld: Value::String("abc".to_string())
              }
            }],
          ),
          vec![]
        );

        // assert.deepEqual [{p:[1],ld:null,li:'x'}], type.transform [{p:[0],ld:null,li:"x"}], [{p:[0],li:"The"}], 'right'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::ListDelete { ld: Value::Null }
            }],
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::ListInsert {
                li: Value::String("x".to_string())
              }
            }],
            TransformSide::Right
          ),
          vec![Operation {
            p: vec![1usize.into()],
            kind: OperationKind::ListDelete { ld: Value::Null }
          }]
        );
      }

      #[test]
      fn doesnt_change_the_original_object() {
        // assert.deepEqual [{p:[0],ld:'abc'}], type.compose [{p:[0],ld:'abc',li:null}], [{p:[0],ld:null}]
        assert_ok_eq!(
          JSON0::compose(
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::ListReplace {
                ld: Value::String("abc".to_string()),
                li: Value::Null
              }
            }],
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::ListDelete { ld: Value::Null }
            }],
          ),
          vec![Operation {
            p: vec![0usize.into()],
            kind: OperationKind::ListDelete {
              ld: Value::String("abc".to_string())
            }
          }]
        );
      }

      #[test]
      fn composes_together_adjacent_string_ops() {
        // assert.deepEqual [{p:[100], si:'hi'}], type.compose [{p:[100], si:'h'}], [{p:[101], si:'i'}]
        println!("si");
        assert_ok_eq!(
          JSON0::compose(
            vec![Operation {
              p: vec![100usize.into()],
              kind: OperationKind::StringInsert { si: "h".to_string() }
            }],
            vec![Operation {
              p: vec![101usize.into()],
              kind: OperationKind::StringInsert { si: "i".to_string() }
            }],
          ),
          vec![Operation {
            p: vec![100usize.into()],
            kind: OperationKind::StringInsert { si: "hi".to_string() }
          }]
        );

        // assert.deepEqual [{p:[], t:'text0', o:[{p:100, i:'hi'}]}], type.compose [{p:[], t:'text0', o:[{p:100, i:'h'}]}], [{p:[], t:'text0', o:[{p:101, i:'i'}]}]
        // println!("text0");
        // assert_ok_eq!(
        //   JSON0::compose(
        //     vec![Operation {
        //       p: vec![],
        //       kind: OperationKind::SubtypeOperation {
        //         t: "text0".to_string(),
        //         o: vec![Operation {
        //           p: vec![100usize.into()],
        //           kind: OperationKind::StringInsert { si: "h".to_string() }
        //         }],
        //       },
        //     }],
        //     vec![Operation {
        //       p: vec![],
        //       kind: OperationKind::SubtypeOperation {
        //         t: "text0".to_string(),
        //         o: vec![Operation {
        //           p: vec![101usize.into()],
        //           kind: OperationKind::StringInsert { si: "i".to_string() }
        //         }],
        //       },
        //     }],
        //   ),
        //   vec![Operation {
        //     p: vec![],
        //     kind: OperationKind::SubtypeOperation {
        //       t: "text0".to_string(),
        //       o: vec![Operation {
        //         p: vec![100usize.into()],
        //         kind: OperationKind::StringInsert { si: "hi".to_string() }
        //       }],
        //     },
        //   }]
        // );
      }

      #[test]
      fn moves_ops_on_a_moved_element_with_the_element() {
        // assert.deepEqual [{p:[10], ld:'x'}], type.transform [{p:[4], ld:'x'}], [{p:[4], lm:10}], 'left'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![4usize.into()],
              kind: OperationKind::ListDelete {
                ld: Value::String("x".to_string())
              }
            }],
            vec![Operation {
              p: vec![4usize.into()],
              kind: OperationKind::ListMove { lm: 10 }
            }],
            TransformSide::Left
          ),
          vec![Operation {
            p: vec![10usize.into()],
            kind: OperationKind::ListDelete {
              ld: Value::String("x".to_string())
            }
          }]
        );

        // assert.deepEqual [{p:[10, 1], si:'a'}], type.transform [{p:[4, 1], si:'a'}], [{p:[4], lm:10}], 'left'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![4usize.into(), 1usize.into()],
              kind: OperationKind::StringInsert { si: "a".to_string() }
            }],
            vec![Operation {
              p: vec![4usize.into()],
              kind: OperationKind::ListMove { lm: 10 }
            }],
            TransformSide::Left
          ),
          vec![Operation {
            p: vec![10usize.into(), 1usize.into()],
            kind: OperationKind::StringInsert { si: "a".to_string() }
          }]
        );

        // assert.deepEqual [{p:[10], t:'text0', o:[{p:1, i:'a'}]}], type.transform [{p:[4], t:'text0', o:[{p:1, i:'a'}]}], [{p:[4], lm:10}], 'left'
        // println!("text0");
        // assert_ok_eq!(
        //   JSON0::transform(
        //     vec![Operation {
        //       p: vec![4usize.into()],
        //       kind: OperationKind::SubtypeOperation {
        //         t: "text0".to_string(),
        //         o: vec![Operation {
        //           p: vec![1usize.into()],
        //           kind: OperationKind::StringInsert { si: "a".to_string() }
        //         }],
        //       },
        //     }],
        //     vec![Operation {
        //       p: vec![4usize.into()],
        //       kind: OperationKind::ListMove { lm: 10 }
        //     }],
        //     TransformSide::Left
        //   ),
        //   vec![Operation {
        //     p: vec![10usize.into()],
        //     kind: OperationKind::SubtypeOperation {
        //       t: "text0".to_string(),
        //       o: vec![Operation {
        //         p: vec![1usize.into()],
        //         kind: OperationKind::StringInsert { si: "a".to_string() }
        //       }],
        //     },
        //   }]
        // );

        // assert.deepEqual [{p:[10, 1], li:'a'}], type.transform [{p:[4, 1], li:'a'}], [{p:[4], lm:10}], 'left'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![4usize.into(), 1usize.into()],
              kind: OperationKind::ListInsert {
                li: Value::String("a".to_string())
              }
            }],
            vec![Operation {
              p: vec![4usize.into()],
              kind: OperationKind::ListMove { lm: 10 }
            }],
            TransformSide::Left
          ),
          vec![Operation {
            p: vec![10usize.into(), 1usize.into()],
            kind: OperationKind::ListInsert {
              li: Value::String("a".to_string())
            }
          }]
        );

        // assert.deepEqual [{p:[10, 1], ld:'b', li:'a'}], type.transform [{p:[4, 1], ld:'b', li:'a'}], [{p:[4], lm:10}], 'left'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![4usize.into(), 1usize.into()],
              kind: OperationKind::ListReplace {
                ld: Value::String("b".to_string()),
                li: Value::String("a".to_string())
              }
            }],
            vec![Operation {
              p: vec![4usize.into()],
              kind: OperationKind::ListMove { lm: 10 }
            }],
            TransformSide::Left
          ),
          vec![Operation {
            p: vec![10usize.into(), 1usize.into()],
            kind: OperationKind::ListReplace {
              ld: Value::String("b".to_string()),
              li: Value::String("a".to_string())
            }
          }]
        );

        // assert.deepEqual [{p:[0],li:null}], type.transform [{p:[0],li:null}], [{p:[0],lm:1}], 'left'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::ListInsert { li: Value::Null }
            }],
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::ListMove { lm: 1 }
            }],
            TransformSide::Left
          ),
          vec![Operation {
            p: vec![0usize.into()],
            kind: OperationKind::ListInsert { li: Value::Null }
          }]
        );

        // assert.deepEqual [{p:[6],li:'x'}], type.transform [{p:[5],li:'x'}], [{p:[5],lm:1}], 'left'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![5usize.into()],
              kind: OperationKind::ListInsert {
                li: Value::String("x".to_string())
              }
            }],
            vec![Operation {
              p: vec![5usize.into()],
              kind: OperationKind::ListMove { lm: 1 }
            }],
            TransformSide::Left
          ),
          vec![Operation {
            p: vec![6usize.into()],
            kind: OperationKind::ListInsert {
              li: Value::String("x".to_string())
            }
          }]
        );

        // assert.deepEqual [{p:[1],ld:6}], type.transform [{p:[5],ld:6}], [{p:[5],lm:1}], 'left'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![5usize.into()],
              kind: OperationKind::ListDelete {
                ld: Value::Number(serde_json::Number::from_f64(6.0).unwrap())
              }
            }],
            vec![Operation {
              p: vec![5usize.into()],
              kind: OperationKind::ListMove { lm: 1 }
            }],
            TransformSide::Left
          ),
          vec![Operation {
            p: vec![1usize.into()],
            kind: OperationKind::ListDelete {
              ld: Value::Number(serde_json::Number::from_f64(6.0).unwrap())
            }
          }]
        );

        // assert.deepEqual [{p:[0],li:[]}], type.transform [{p:[0],li:[]}], [{p:[1],lm:0}], 'left'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::ListInsert {
                li: Value::Array(vec![])
              }
            }],
            vec![Operation {
              p: vec![1usize.into()],
              kind: OperationKind::ListMove { lm: 0 }
            }],
            TransformSide::Left
          ),
          vec![Operation {
            p: vec![0usize.into()],
            kind: OperationKind::ListInsert {
              li: Value::Array(vec![])
            }
          }]
        );

        // assert.deepEqual [{p:[2],li:'x'}], type.transform [{p:[2],li:'x'}], [{p:[0],lm:1}], 'left'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![2usize.into()],
              kind: OperationKind::ListInsert {
                li: Value::String("x".to_string())
              }
            }],
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::ListMove { lm: 1 }
            }],
            TransformSide::Left
          ),
          vec![Operation {
            p: vec![2usize.into()],
            kind: OperationKind::ListInsert {
              li: Value::String("x".to_string())
            }
          }]
        );
      }

      #[test]
      fn moves_target_index_on_ld_or_li() {
        // assert.deepEqual [{p:[0],lm:1}], type.transform [{p:[0], lm: 2}], [{p:[1], ld:'x'}], 'left'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::ListMove { lm: 2 }
            }],
            vec![Operation {
              p: vec![1usize.into()],
              kind: OperationKind::ListDelete {
                ld: Value::String("x".to_string())
              }
            }],
            TransformSide::Left
          ),
          vec![Operation {
            p: vec![0usize.into()],
            kind: OperationKind::ListMove { lm: 1 }
          }]
        );

        // assert.deepEqual [{p:[1],lm:3}], type.transform [{p:[2], lm: 4}], [{p:[1], ld:'x'}], 'left'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![2usize.into()],
              kind: OperationKind::ListMove { lm: 4 }
            }],
            vec![Operation {
              p: vec![1usize.into()],
              kind: OperationKind::ListDelete {
                ld: Value::String("x".to_string())
              }
            }],
            TransformSide::Left
          ),
          vec![Operation {
            p: vec![1usize.into()],
            kind: OperationKind::ListMove { lm: 3 }
          }]
        );

        // assert.deepEqual [{p:[0],lm:3}], type.transform [{p:[0], lm: 2}], [{p:[1], li:'x'}], 'left'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::ListMove { lm: 2 }
            }],
            vec![Operation {
              p: vec![1usize.into()],
              kind: OperationKind::ListInsert {
                li: Value::String("x".to_string())
              }
            }],
            TransformSide::Left
          ),
          vec![Operation {
            p: vec![0usize.into()],
            kind: OperationKind::ListMove { lm: 3 }
          }]
        );

        // assert.deepEqual [{p:[3],lm:5}], type.transform [{p:[2], lm: 4}], [{p:[1], li:'x'}], 'left'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![2usize.into()],
              kind: OperationKind::ListMove { lm: 4 }
            }],
            vec![Operation {
              p: vec![1usize.into()],
              kind: OperationKind::ListInsert {
                li: Value::String("x".to_string())
              }
            }],
            TransformSide::Left
          ),
          vec![Operation {
            p: vec![3usize.into()],
            kind: OperationKind::ListMove { lm: 5 }
          }]
        );

        // assert.deepEqual [{p:[1],lm:1}], type.transform [{p:[0], lm: 0}], [{p:[0], li:28}], 'left'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::ListMove { lm: 0 }
            }],
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::ListInsert {
                li: Value::Number(serde_json::Number::from_f64(28.0).unwrap())
              }
            }],
            TransformSide::Left
          ),
          vec![Operation {
            p: vec![1usize.into()],
            kind: OperationKind::ListMove { lm: 1 }
          }]
        );
      }

      #[test]
      fn tiebreaks_lm_vs_ld_or_li() {
        // assert.deepEqual [], type.transform [{p:[0], lm: 2}], [{p:[0], ld:'x'}], 'left'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::ListMove { lm: 2 }
            }],
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::ListDelete {
                ld: Value::String("x".to_string())
              }
            }],
            TransformSide::Left
          ),
          vec![]
        );

        // assert.deepEqual [], type.transform [{p:[0], lm: 2}], [{p:[0], ld:'x'}], 'right'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::ListMove { lm: 2 }
            }],
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::ListDelete {
                ld: Value::String("x".to_string())
              }
            }],
            TransformSide::Right
          ),
          vec![]
        );

        // assert.deepEqual [{p:[1], lm:3}], type.transform [{p:[0], lm: 2}], [{p:[0], li:'x'}], 'left'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::ListMove { lm: 2 }
            }],
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::ListInsert {
                li: Value::String("x".to_string())
              }
            }],
            TransformSide::Left
          ),
          vec![Operation {
            p: vec![1usize.into()],
            kind: OperationKind::ListMove { lm: 3 }
          }]
        );

        // assert.deepEqual [{p:[1], lm:3}], type.transform [{p:[0], lm: 2}], [{p:[0], li:'x'}], 'right'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::ListMove { lm: 2 }
            }],
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::ListInsert {
                li: Value::String("x".to_string())
              }
            }],
            TransformSide::Right
          ),
          vec![Operation {
            p: vec![1usize.into()],
            kind: OperationKind::ListMove { lm: 3 }
          }]
        );
      }

      #[test]
      fn replacement_vs_deletion() {
        // assert.deepEqual [{p:[0],li:'y'}], type.transform [{p:[0],ld:'x',li:'y'}], [{p:[0],ld:'x'}], 'right'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::ListReplace {
                ld: Value::String("x".to_string()),
                li: Value::String("y".to_string())
              }
            }],
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::ListDelete {
                ld: Value::String("x".to_string())
              }
            }],
            TransformSide::Right
          ),
          vec![Operation {
            p: vec![0usize.into()],
            kind: OperationKind::ListInsert {
              li: Value::String("y".to_string())
            }
          }]
        );
      }

      #[test]
      fn replacement_vs_insertion() {
        // assert.deepEqual [{p:[1],ld:{},li:"brillig"}], type.transform [{p:[0],ld:{},li:"brillig"}], [{p:[0],li:36}], 'left'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::ListReplace {
                ld: json!({}),
                li: Value::String("brillig".to_string())
              }
            }],
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::ListInsert {
                li: Value::Number(serde_json::Number::from_f64(36.0).unwrap())
              }
            }],
            TransformSide::Left
          ),
          vec![Operation {
            p: vec![1usize.into()],
            kind: OperationKind::ListReplace {
              ld: json!({}),
              li: Value::String("brillig".to_string())
            }
          }]
        );
      }

      #[test]
      fn replacement_vs_replacement() {
        // assert.deepEqual [], type.transform [{p:[0],ld:null,li:[]}], [{p:[0],ld:null,li:0}], 'right'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::ListReplace {
                ld: Value::Null,
                li: Value::Array(vec![])
              }
            }],
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::ListReplace {
                ld: Value::Null,
                li: Value::Number(serde_json::Number::from_f64(0.0).unwrap())
              }
            }],
            TransformSide::Right
          ),
          vec![]
        );

        // assert.deepEqual [{p:[0],ld:[],li:0}], type.transform [{p:[0],ld:null,li:0}], [{p:[0],ld:null,li:[]}], 'left'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::ListReplace {
                ld: Value::Null,
                li: Value::Number(serde_json::Number::from_f64(0.0).unwrap())
              }
            }],
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::ListReplace {
                ld: Value::Null,
                li: Value::Array(vec![])
              }
            }],
            TransformSide::Left
          ),
          vec![Operation {
            p: vec![0usize.into()],
            kind: OperationKind::ListReplace {
              ld: Value::Array(vec![]),
              li: Value::Number(serde_json::Number::from_f64(0.0).unwrap())
            }
          }]
        );
      }

      #[test]
      fn composes_replace_with_delete_of_replaced_element_results_in_insert() {
        // assert.deepEqual [{p:[2],ld:[]}], type.compose [{p:[2],ld:[],li:null}], [{p:[2],ld:null}]
        assert_ok_eq!(
          JSON0::compose(
            vec![Operation {
              p: vec![2usize.into()],
              kind: OperationKind::ListReplace {
                ld: Value::Array(vec![]),
                li: Value::Null
              }
            }],
            vec![Operation {
              p: vec![2usize.into()],
              kind: OperationKind::ListDelete { ld: Value::Null }
            }],
          ),
          vec![Operation {
            p: vec![2usize.into()],
            kind: OperationKind::ListDelete {
              ld: Value::Array(vec![])
            }
          }]
        );
      }

      #[test]
      fn lm_vs_lm() {
        //  assert.deepEqual [{p:[0],lm:2}], type.transform [{p:[0],lm:2}], [{p:[2],lm:1}], 'left'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::ListMove { lm: 2 }
            }],
            vec![Operation {
              p: vec![2usize.into()],
              kind: OperationKind::ListMove { lm: 1 }
            }],
            TransformSide::Left
          ),
          vec![Operation {
            p: vec![0usize.into()],
            kind: OperationKind::ListMove { lm: 2 }
          }]
        );

        // assert.deepEqual [{p:[4],lm:4}], type.transform [{p:[3],lm:3}], [{p:[5],lm:0}], 'left'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![3usize.into()],
              kind: OperationKind::ListMove { lm: 3 }
            }],
            vec![Operation {
              p: vec![5usize.into()],
              kind: OperationKind::ListMove { lm: 0 }
            }],
            TransformSide::Left
          ),
          vec![Operation {
            p: vec![4usize.into()],
            kind: OperationKind::ListMove { lm: 4 }
          }]
        );

        // assert.deepEqual [{p:[2],lm:0}], type.transform [{p:[2],lm:0}], [{p:[1],lm:0}], 'left'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![2usize.into()],
              kind: OperationKind::ListMove { lm: 0 }
            }],
            vec![Operation {
              p: vec![1usize.into()],
              kind: OperationKind::ListMove { lm: 0 }
            }],
            TransformSide::Left
          ),
          vec![Operation {
            p: vec![2usize.into()],
            kind: OperationKind::ListMove { lm: 0 }
          }]
        );

        // assert.deepEqual [{p:[2],lm:1}], type.transform [{p:[2],lm:0}], [{p:[1],lm:0}], 'right'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![2usize.into()],
              kind: OperationKind::ListMove { lm: 0 }
            }],
            vec![Operation {
              p: vec![1usize.into()],
              kind: OperationKind::ListMove { lm: 0 }
            }],
            TransformSide::Right
          ),
          vec![Operation {
            p: vec![2usize.into()],
            kind: OperationKind::ListMove { lm: 1 }
          }]
        );

        // assert.deepEqual [{p:[3],lm:1}], type.transform [{p:[2],lm:0}], [{p:[5],lm:0}], 'right'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![2usize.into()],
              kind: OperationKind::ListMove { lm: 0 }
            }],
            vec![Operation {
              p: vec![5usize.into()],
              kind: OperationKind::ListMove { lm: 0 }
            }],
            TransformSide::Right
          ),
          vec![Operation {
            p: vec![3usize.into()],
            kind: OperationKind::ListMove { lm: 1 }
          }]
        );

        // assert.deepEqual [{p:[3],lm:0}], type.transform [{p:[2],lm:0}], [{p:[5],lm:0}], 'left'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![2usize.into()],
              kind: OperationKind::ListMove { lm: 0 }
            }],
            vec![Operation {
              p: vec![5usize.into()],
              kind: OperationKind::ListMove { lm: 0 }
            }],
            TransformSide::Left
          ),
          vec![Operation {
            p: vec![3usize.into()],
            kind: OperationKind::ListMove { lm: 0 }
          }]
        );

        // assert.deepEqual [{p:[0],lm:5}], type.transform [{p:[2],lm:5}], [{p:[2],lm:0}], 'left'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![2usize.into()],
              kind: OperationKind::ListMove { lm: 5 }
            }],
            vec![Operation {
              p: vec![2usize.into()],
              kind: OperationKind::ListMove { lm: 0 }
            }],
            TransformSide::Left
          ),
          vec![Operation {
            p: vec![0usize.into()],
            kind: OperationKind::ListMove { lm: 5 }
          }]
        );

        // assert.deepEqual [{p:[0],lm:5}], type.transform [{p:[2],lm:5}], [{p:[2],lm:0}], 'left'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![2usize.into()],
              kind: OperationKind::ListMove { lm: 5 }
            }],
            vec![Operation {
              p: vec![2usize.into()],
              kind: OperationKind::ListMove { lm: 0 }
            }],
            TransformSide::Left
          ),
          vec![Operation {
            p: vec![0usize.into()],
            kind: OperationKind::ListMove { lm: 5 }
          }]
        );

        // assert.deepEqual [{p:[0],lm:0}], type.transform [{p:[1],lm:0}], [{p:[0],lm:5}], 'right'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![1usize.into()],
              kind: OperationKind::ListMove { lm: 0 }
            }],
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::ListMove { lm: 5 }
            }],
            TransformSide::Right
          ),
          vec![Operation {
            p: vec![0usize.into()],
            kind: OperationKind::ListMove { lm: 0 }
          }]
        );

        // assert.deepEqual [{p:[0],lm:0}], type.transform [{p:[1],lm:0}], [{p:[0],lm:1}], 'right'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![1usize.into()],
              kind: OperationKind::ListMove { lm: 0 }
            }],
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::ListMove { lm: 1 }
            }],
            TransformSide::Right
          ),
          vec![Operation {
            p: vec![0usize.into()],
            kind: OperationKind::ListMove { lm: 0 }
          }]
        );

        // assert.deepEqual [{p:[1],lm:1}], type.transform [{p:[0],lm:1}], [{p:[1],lm:0}], 'left'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::ListMove { lm: 1 }
            }],
            vec![Operation {
              p: vec![1usize.into()],
              kind: OperationKind::ListMove { lm: 0 }
            }],
            TransformSide::Left
          ),
          vec![Operation {
            p: vec![1usize.into()],
            kind: OperationKind::ListMove { lm: 1 }
          }]
        );

        // assert.deepEqual [{p:[1],lm:2}], type.transform [{p:[0],lm:1}], [{p:[5],lm:0}], 'right'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::ListMove { lm: 1 }
            }],
            vec![Operation {
              p: vec![5usize.into()],
              kind: OperationKind::ListMove { lm: 0 }
            }],
            TransformSide::Right
          ),
          vec![Operation {
            p: vec![1usize.into()],
            kind: OperationKind::ListMove { lm: 2 }
          }]
        );

        // assert.deepEqual [{p:[3],lm:2}], type.transform [{p:[2],lm:1}], [{p:[5],lm:0}], 'right'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![2usize.into()],
              kind: OperationKind::ListMove { lm: 1 }
            }],
            vec![Operation {
              p: vec![5usize.into()],
              kind: OperationKind::ListMove { lm: 0 }
            }],
            TransformSide::Right
          ),
          vec![Operation {
            p: vec![3usize.into()],
            kind: OperationKind::ListMove { lm: 2 }
          }]
        );

        // assert.deepEqual [{p:[2],lm:1}], type.transform [{p:[3],lm:1}], [{p:[1],lm:3}], 'left'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![3usize.into()],
              kind: OperationKind::ListMove { lm: 1 }
            }],
            vec![Operation {
              p: vec![1usize.into()],
              kind: OperationKind::ListMove { lm: 3 }
            }],
            TransformSide::Left
          ),
          vec![Operation {
            p: vec![2usize.into()],
            kind: OperationKind::ListMove { lm: 1 }
          }]
        );

        // assert.deepEqual [{p:[2],lm:3}], type.transform [{p:[1],lm:3}], [{p:[3],lm:1}], 'left'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![1usize.into()],
              kind: OperationKind::ListMove { lm: 3 }
            }],
            vec![Operation {
              p: vec![3usize.into()],
              kind: OperationKind::ListMove { lm: 1 }
            }],
            TransformSide::Left
          ),
          vec![Operation {
            p: vec![2usize.into()],
            kind: OperationKind::ListMove { lm: 3 }
          }]
        );

        // assert.deepEqual [{p:[2],lm:6}], type.transform [{p:[2],lm:6}], [{p:[0],lm:1}], 'left'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![2usize.into()],
              kind: OperationKind::ListMove { lm: 6 }
            }],
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::ListMove { lm: 1 }
            }],
            TransformSide::Left
          ),
          vec![Operation {
            p: vec![2usize.into()],
            kind: OperationKind::ListMove { lm: 6 }
          }]
        );

        // assert.deepEqual [{p:[2],lm:6}], type.transform [{p:[2],lm:6}], [{p:[0],lm:1}], 'right'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![2usize.into()],
              kind: OperationKind::ListMove { lm: 6 }
            }],
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::ListMove { lm: 1 }
            }],
            TransformSide::Right
          ),
          vec![Operation {
            p: vec![2usize.into()],
            kind: OperationKind::ListMove { lm: 6 }
          }]
        );

        // assert.deepEqual [{p:[2],lm:6}], type.transform [{p:[2],lm:6}], [{p:[1],lm:0}], 'left'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![2usize.into()],
              kind: OperationKind::ListMove { lm: 6 }
            }],
            vec![Operation {
              p: vec![1usize.into()],
              kind: OperationKind::ListMove { lm: 0 }
            }],
            TransformSide::Left
          ),
          vec![Operation {
            p: vec![2usize.into()],
            kind: OperationKind::ListMove { lm: 6 }
          }]
        );

        // assert.deepEqual [{p:[2],lm:6}], type.transform [{p:[2],lm:6}], [{p:[1],lm:0}], 'right'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![2usize.into()],
              kind: OperationKind::ListMove { lm: 6 }
            }],
            vec![Operation {
              p: vec![1usize.into()],
              kind: OperationKind::ListMove { lm: 0 }
            }],
            TransformSide::Right
          ),
          vec![Operation {
            p: vec![2usize.into()],
            kind: OperationKind::ListMove { lm: 6 }
          }]
        );

        // assert.deepEqual [{p:[0],lm:2}], type.transform [{p:[0],lm:1}], [{p:[2],lm:1}], 'left'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::ListMove { lm: 1 }
            }],
            vec![Operation {
              p: vec![2usize.into()],
              kind: OperationKind::ListMove { lm: 1 }
            }],
            TransformSide::Left
          ),
          vec![Operation {
            p: vec![0usize.into()],
            kind: OperationKind::ListMove { lm: 2 }
          }]
        );

        // assert.deepEqual [{p:[2],lm:0}], type.transform [{p:[2],lm:1}], [{p:[0],lm:1}], 'right'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![2usize.into()],
              kind: OperationKind::ListMove { lm: 1 }
            }],
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::ListMove { lm: 1 }
            }],
            TransformSide::Right
          ),
          vec![Operation {
            p: vec![2usize.into()],
            kind: OperationKind::ListMove { lm: 0 }
          }]
        );

        // assert.deepEqual [{p:[1],lm:1}], type.transform [{p:[0],lm:0}], [{p:[1],lm:0}], 'left'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::ListMove { lm: 0 }
            }],
            vec![Operation {
              p: vec![1usize.into()],
              kind: OperationKind::ListMove { lm: 0 }
            }],
            TransformSide::Left
          ),
          vec![Operation {
            p: vec![1usize.into()],
            kind: OperationKind::ListMove { lm: 1 }
          }]
        );

        // assert.deepEqual [{p:[0],lm:0}], type.transform [{p:[0],lm:1}], [{p:[1],lm:3}], 'left'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::ListMove { lm: 1 }
            }],
            vec![Operation {
              p: vec![1usize.into()],
              kind: OperationKind::ListMove { lm: 3 }
            }],
            TransformSide::Left
          ),
          vec![Operation {
            p: vec![0usize.into()],
            kind: OperationKind::ListMove { lm: 0 }
          }]
        );

        // assert.deepEqual [{p:[3],lm:1}], type.transform [{p:[2],lm:1}], [{p:[3],lm:2}], 'left'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![2usize.into()],
              kind: OperationKind::ListMove { lm: 1 }
            }],
            vec![Operation {
              p: vec![3usize.into()],
              kind: OperationKind::ListMove { lm: 2 }
            }],
            TransformSide::Left
          ),
          vec![Operation {
            p: vec![3usize.into()],
            kind: OperationKind::ListMove { lm: 1 }
          }]
        );

        // assert.deepEqual [{p:[3],lm:3}], type.transform [{p:[3],lm:2}], [{p:[2],lm:1}], 'left'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![3usize.into()],
              kind: OperationKind::ListMove { lm: 2 }
            }],
            vec![Operation {
              p: vec![2usize.into()],
              kind: OperationKind::ListMove { lm: 1 }
            }],
            TransformSide::Left
          ),
          vec![Operation {
            p: vec![3usize.into()],
            kind: OperationKind::ListMove { lm: 3 }
          }]
        );
      }

      #[test]
      fn changes_indices_correctly_around_a_move() {
        // assert.deepEqual [{p:[1,0],li:{}}], type.transform [{p:[0,0],li:{}}], [{p:[1],lm:0}], 'left'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![0usize.into(), 0usize.into()],
              kind: OperationKind::ListInsert {
                li: json!({})
              }
            }],
            vec![Operation {
              p: vec![1usize.into()],
              kind: OperationKind::ListMove { lm: 0 }
            }],
            TransformSide::Left
          ),
          vec![Operation {
            p: vec![1usize.into(), 0usize.into()],
            kind: OperationKind::ListInsert {
              li: json!({})
            }
          }]
        );

        // assert.deepEqual [{p:[0],lm:0}], type.transform [{p:[1],lm:0}], [{p:[0],ld:{}}], 'left'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![1usize.into()],
              kind: OperationKind::ListMove { lm: 0 }
            }],
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::ListDelete {
                ld: json!({})
              }
            }],
            TransformSide::Left
          ),
          vec![Operation {
            p: vec![0usize.into()],
            kind: OperationKind::ListMove { lm: 0 }
          }]
        );

        // assert.deepEqual [{p:[0],lm:0}], type.transform [{p:[0],lm:1}], [{p:[1],ld:{}}], 'left'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::ListMove { lm: 1 }
            }],
            vec![Operation {
              p: vec![1usize.into()],
              kind: OperationKind::ListDelete {
                ld: json!({})
              }
            }],
            TransformSide::Left
          ),
          vec![Operation {
            p: vec![0usize.into()],
            kind: OperationKind::ListMove { lm: 0 }
          }]
        );

        // assert.deepEqual [{p:[5],lm:0}], type.transform [{p:[6],lm:0}], [{p:[2],ld:{}}], 'left'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![6usize.into()],
              kind: OperationKind::ListMove { lm: 0 }
            }],
            vec![Operation {
              p: vec![2usize.into()],
              kind: OperationKind::ListDelete {
                ld: json!({})
              }
            }],
            TransformSide::Left
          ),
          vec![Operation {
            p: vec![5usize.into()],
            kind: OperationKind::ListMove { lm: 0 }
          }]
        );

        // assert.deepEqual [{p:[1],lm:0}], type.transform [{p:[1],lm:0}], [{p:[2],ld:{}}], 'left'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![1usize.into()],
              kind: OperationKind::ListMove { lm: 0 }
            }],
            vec![Operation {
              p: vec![2usize.into()],
              kind: OperationKind::ListDelete {
                ld: json!({})
              }
            }],
            TransformSide::Left
          ),
          vec![Operation {
            p: vec![1usize.into()],
            kind: OperationKind::ListMove { lm: 0 }
          }]
        );

        // assert.deepEqual [{p:[1],lm:1}], type.transform [{p:[2],lm:1}], [{p:[1],ld:3}], 'right'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![2usize.into()],
              kind: OperationKind::ListMove { lm: 1 }
            }],
            vec![Operation {
              p: vec![1usize.into()],
              kind: OperationKind::ListDelete { ld: json!(3) }
            }],
            TransformSide::Right
          ),
          vec![Operation {
            p: vec![1usize.into()],
            kind: OperationKind::ListMove { lm: 1 }
          }]
        );

        // assert.deepEqual [{p:[1],ld:{}}], type.transform [{p:[2],ld:{}}], [{p:[1],lm:2}], 'right'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![2usize.into()],
              kind: OperationKind::ListDelete {
                ld: json!({})
              }
            }],
            vec![Operation {
              p: vec![1usize.into()],
              kind: OperationKind::ListMove { lm: 2 }
            }],
            TransformSide::Right
          ),
          vec![Operation {
            p: vec![1usize.into()],
            kind: OperationKind::ListDelete {
              ld: json!({})
            }
          }]
        );

        // assert.deepEqual [{p:[2],ld:{}}], type.transform [{p:[1],ld:{}}], [{p:[2],lm:1}], 'left'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![1usize.into()],
              kind: OperationKind::ListDelete {
                ld: json!({})
              }
            }],
            vec![Operation {
              p: vec![2usize.into()],
              kind: OperationKind::ListMove { lm: 1 }
            }],
            TransformSide::Left
          ),
          vec![Operation {
            p: vec![2usize.into()],
            kind: OperationKind::ListDelete {
              ld: json!({})
            }
          }]
        );

        // assert.deepEqual [{p:[0],ld:{}}], type.transform [{p:[1],ld:{}}], [{p:[0],lm:1}], 'right'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![1usize.into()],
              kind: OperationKind::ListDelete {
                ld: json!({})
              }
            }],
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::ListMove { lm: 1 }
            }],
            TransformSide::Right
          ),
          vec![Operation {
            p: vec![0usize.into()],
            kind: OperationKind::ListDelete {
              ld: json!({})
            }
          }]
        );

        // assert.deepEqual [{p:[0],ld:1,li:2}], type.transform [{p:[1],ld:1,li:2}], [{p:[1],lm:0}], 'left'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![1usize.into()],
              kind: OperationKind::ListReplace {
                ld: json!(1),
                li: json!(2)
              }
            }],
            vec![Operation {
              p: vec![1usize.into()],
              kind: OperationKind::ListMove { lm: 0 }
            }],
            TransformSide::Left
          ),
          vec![Operation {
            p: vec![0usize.into()],
            kind: OperationKind::ListReplace {
              ld: json!(1),
              li: json!(2)
            }
          }]
        );

        // assert.deepEqual [{p:[0],ld:2,li:3}], type.transform [{p:[1],ld:2,li:3}], [{p:[0],lm:1}], 'left'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![1usize.into()],
              kind: OperationKind::ListReplace {
                ld: json!(2),
                li: json!(3)
              }
            }],
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::ListMove { lm: 1 }
            }],
            TransformSide::Left
          ),
          vec![Operation {
            p: vec![0usize.into()],
            kind: OperationKind::ListReplace {
              ld: json!(2),
              li: json!(3)
            }
          }]
        );

        // assert.deepEqual [{p:[1],ld:3,li:4}], type.transform [{p:[0],ld:3,li:4}], [{p:[1],lm:0}], 'left'
        assert_ok_eq!(
          JSON0::transform(
            vec![Operation {
              p: vec![0usize.into()],
              kind: OperationKind::ListReplace {
                ld: json!(3),
                li: json!(4)
              }
            }],
            vec![Operation {
              p: vec![1usize.into()],
              kind: OperationKind::ListMove { lm: 0 }
            }],
            TransformSide::Left
          ),
          vec![Operation {
            p: vec![1usize.into()],
            kind: OperationKind::ListReplace {
              ld: json!(3),
              li: json!(4)
            }
          }]
        );
      }

      #[test]
      fn li_vs_lm() {
        // li = (p) -> [{p:[p],li:[]}]
        // lm = (f,t) -> [{p:[f],lm:t}]
        // xf = type.transform

        // assert.deepEqual (li 0), xf (li 0), (lm 1, 3), 'left'
        // assert.deepEqual (li 1), xf (li 1), (lm 1, 3), 'left'
        // assert.deepEqual (li 1), xf (li 2), (lm 1, 3), 'left'
        // assert.deepEqual (li 2), xf (li 3), (lm 1, 3), 'left'
        // assert.deepEqual (li 4), xf (li 4), (lm 1, 3), 'left'

        // assert.deepEqual (lm 2, 4), xf (lm 1, 3), (li 0), 'right'
        // assert.deepEqual (lm 2, 4), xf (lm 1, 3), (li 1), 'right'
        // assert.deepEqual (lm 1, 4), xf (lm 1, 3), (li 2), 'right'
        // assert.deepEqual (lm 1, 4), xf (lm 1, 3), (li 3), 'right'
        // assert.deepEqual (lm 1, 3), xf (lm 1, 3), (li 4), 'right'

        // assert.deepEqual (li 0), xf (li 0), (lm 1, 2), 'left'
        // assert.deepEqual (li 1), xf (li 1), (lm 1, 2), 'left'
        // assert.deepEqual (li 1), xf (li 2), (lm 1, 2), 'left'
        // assert.deepEqual (li 3), xf (li 3), (lm 1, 2), 'left'

        // assert.deepEqual (li 0), xf (li 0), (lm 3, 1), 'left'
        // assert.deepEqual (li 1), xf (li 1), (lm 3, 1), 'left'
        // assert.deepEqual (li 3), xf (li 2), (lm 3, 1), 'left'
        // assert.deepEqual (li 4), xf (li 3), (lm 3, 1), 'left'
        // assert.deepEqual (li 4), xf (li 4), (lm 3, 1), 'left'

        // assert.deepEqual (lm 4, 2), xf (lm 3, 1), (li 0), 'right'
        // assert.deepEqual (lm 4, 2), xf (lm 3, 1), (li 1), 'right'
        // assert.deepEqual (lm 4, 1), xf (lm 3, 1), (li 2), 'right'
        // assert.deepEqual (lm 4, 1), xf (lm 3, 1), (li 3), 'right'
        // assert.deepEqual (lm 3, 1), xf (lm 3, 1), (li 4), 'right'

        // assert.deepEqual (li 0), xf (li 0), (lm 2, 1), 'left'
        // assert.deepEqual (li 1), xf (li 1), (lm 2, 1), 'left'
        // assert.deepEqual (li 3), xf (li 2), (lm 2, 1), 'left'
        // assert.deepEqual (li 3), xf (li 3), (lm 2, 1), 'left'
      }
    }
  }

  mod object {
    use super::*;
    use claims::{assert_ok_eq};
    use serde_json::json;

    #[test]
    fn passes_sanity_checks() {
      // assert.deepEqual {x:'a', y:'b'}, type.apply {x:'a'}, [{p:['y'], oi:'b'}]
      assert_ok_eq!(
        JSON0::apply(
          Value::Object(
            [("x".to_string(), Value::String("a".to_string()))]
              .iter()
              .cloned()
              .collect()
          ),
          vec![Operation {
            p: vec!["y".into()],
            kind: OperationKind::ObjectInsert {
              oi: Value::String("b".to_string())
            }
          }]
        ),
        Value::Object(
          [
            ("x".to_string(), Value::String("a".to_string())),
            ("y".to_string(), Value::String("b".to_string()))
          ]
          .iter()
          .cloned()
          .collect()
        )
      );

      // assert.deepEqual {}, type.apply {x:'a'}, [{p:['x'], od:'a'}]
      assert_ok_eq!(
        JSON0::apply(
          Value::Object(
            [("x".to_string(), Value::String("a".to_string()))]
              .iter()
              .cloned()
              .collect()
          ),
          vec![Operation {
            p: vec!["x".into()],
            kind: OperationKind::ObjectDelete {
              od: Value::String("a".to_string())
            }
          }]
        ),
        json!({})
      );

      // assert.deepEqual {x:'b'}, type.apply {x:'a'}, [{p:['x'], od:'a', oi:'b'}]
      assert_ok_eq!(
        JSON0::apply(
          Value::Object(
            [("x".to_string(), Value::String("a".to_string()))]
              .iter()
              .cloned()
              .collect()
          ),
          vec![Operation {
            p: vec!["x".into()],
            kind: OperationKind::ObjectReplace {
              od: Value::String("a".to_string()),
              oi: Value::String("b".to_string())
            }
          }]
        ),
        Value::Object(
          [("x".to_string(), Value::String("b".to_string()))]
            .iter()
            .cloned()
            .collect()
        )
      );
    }

    #[test]
    fn ops_on_deleted_elements_become_noops() {
      // assert.deepEqual [], type.transform [{p:[1, 0], si:'hi'}], [{p:[1], od:'x'}], 'left'
      assert_ok_eq!(
        JSON0::transform(
          vec![Operation {
            p: vec![1usize.into(), 0usize.into()],
            kind: OperationKind::StringInsert { si: "hi".to_string() }
          }],
          vec![Operation {
            p: vec![1usize.into()],
            kind: OperationKind::ObjectDelete {
              od: Value::String("x".to_string())
            }
          }],
          TransformSide::Left
        ),
        vec![]
      );

      // assert.deepEqual [], type.transform [{p:[1], t:'text0', o:[{p:0, i:'hi'}]}], [{p:[1], od:'x'}], 'left'
      // assert_ok_eq!(
      //   JSON0::transform(
      //     vec![Operation {
      //       p: vec![1usize.into()],
      //       kind: OperationKind::TextInsert {
      //         t: "text0".to_string(),
      //         o: vec![Operation {
      //           p: vec![0usize.into()],
      //           kind: OperationKind::StringInsert {
      //             si: "hi".to_string()
      //           }
      //         }]
      //       }
      //     }],
      //     vec![Operation {
      //       p: vec![1usize.into()],
      //       kind: OperationKind::ObjectDelete {
      //         od: Value::String("x".to_string())
      //       }
      //     }],
      //     TransformSide::Left
      //   ),
      //   vec![]
      // );

      // assert.deepEqual [], type.transform [{p:[9],si:"bite "}], [{p:[],od:"agimble s",oi:null}], 'right'
      assert_ok_eq!(
        JSON0::transform(
          vec![Operation {
            p: vec![9usize.into()],
            kind: OperationKind::StringInsert {
              si: "bite ".to_string()
            }
          }],
          vec![Operation {
            p: vec![],
            kind: OperationKind::ObjectReplace {
              od: Value::String("agimble s".to_string()),
              oi: Value::Null
            }
          }],
          TransformSide::Right
        ),
        vec![]
      );

      // assert.deepEqual [], type.transform [{p:[], t:'text0', o:[{p:9, i:"bite "}]}], [{p:[],od:"agimble s",oi:null}], 'right'
      // assert_ok_eq!(
      //   JSON0::transform(
      //     vec![Operation {
      //       p: vec![],
      //       kind: OperationKind::TextInsert {
      //         t: "text0".to_string(),
      //         o: vec![Operation {
      //           p: vec![9usize.into()],
      //           kind: OperationKind::StringInsert {
      //             si: "bite ".to_string()
      //           }
      //         }]
      //       }
      //     }],
      //     vec![Operation {
      //       p: vec![],
      //       kind: OperationKind::ObjectReplace {
      //         od: Value::String("agimble s".to_string()),
      //         oi: Value::Null
      //       }
      //     }],
      //     TransformSide::Right
      //   ),
      //   vec![]
      // );
    }

    #[test]
    fn ops_on_replaced_elements_become_noops() {
      // assert.deepEqual [], type.transform [{p:[1, 0], si:'hi'}], [{p:[1], od:'x', oi:'y'}], 'left'
      assert_ok_eq!(
        JSON0::transform(
          vec![Operation {
            p: vec![1usize.into(), 0usize.into()],
            kind: OperationKind::StringInsert { si: "hi".to_string() }
          }],
          vec![Operation {
            p: vec![1usize.into()],
            kind: OperationKind::ObjectReplace {
              od: Value::String("x".to_string()),
              oi: Value::String("y".to_string())
            }
          }],
          TransformSide::Left
        ),
        vec![]
      );

      // assert.deepEqual [], type.transform [{p:[1], t:'text0', o:[{p:0, i:'hi'}]}], [{p:[1], od:'x', oi:'y'}], 'left'
      // assert_ok_eq!(
      //   JSON0::transform(
      //     vec![Operation {
      //       p: vec![1usize.into()],
      //       kind: OperationKind::TextInsert {
      //         t: "text0".to_string(),
      //         o: vec![Operation {
      //           p: vec![0usize.into()],
      //           kind: OperationKind::StringInsert { si: "hi".to_string() }
      //         }]
      //       }
      //     }],
      //     vec![Operation {
      //       p: vec![1usize.into()],
      //       kind: OperationKind::ObjectReplace {
      //         od: Value::String("x".to_string()),
      //         oi: Value::String("y".to_string())
      //       }
      //     }],
      //     TransformSide::Left
      //   ),
      //   vec![]
      // );
    }

    #[test]
    fn deleted_data_is_changed_to_reflect_edits() {
      // assert.deepEqual [{p:[1], od:'abc'}], type.transform [{p:[1], od:'a'}], [{p:[1, 1], si:'bc'}], 'left'
      println!("test 1");
      assert_ok_eq!(
        JSON0::transform(
          vec![Operation {
            p: vec![1usize.into()],
            kind: OperationKind::ObjectDelete {
              od: Value::String("a".to_string())
            }
          }],
          vec![Operation {
            p: vec![1usize.into(), 1usize.into()],
            kind: OperationKind::StringInsert { si: "bc".to_string() }
          }],
          TransformSide::Left
        ),
        vec![Operation {
          p: vec![1usize.into()],
          kind: OperationKind::ObjectDelete {
            od: Value::String("abc".to_string())
          }
        }]
      );

      // assert.deepEqual [{p:[1], od:'abc'}], type.transform [{p:[1], od:'a'}], [{p:[1], t:'text0', o:[{p:1, i:'bc'}]}], 'left'
      // assert_ok_eq!(
      //   JSON0::transform(
      //     vec![Operation {
      //       p: vec![1usize.into()],
      //       kind: OperationKind::ObjectDelete {
      //         od: Value::String("a".to_string())
      //       }
      //     }],
      //     vec![Operation {
      //       p: vec![1usize.into()],
      //       kind: OperationKind::TextInsert {
      //         t: "text0".to_string(),
      //         o: vec![Operation {
      //           p: vec![1usize.into()],
      //           kind: OperationKind::StringInsert { si: "bc".to_string() }
      //         }]
      //       }
      //     }],
      //     TransformSide::Left
      //   ),
      //   vec![Operation {
      //     p: vec![1usize.into()],
      //     kind: OperationKind::ObjectDelete {
      //       od: Value::String("abc".to_string())
      //     }
      //   }]
      // );

      // assert.deepEqual [{p:[],od:25,oi:[]}], type.transform [{p:[],od:22,oi:[]}], [{p:[],na:3}], 'left'
      println!("test 2");
      assert_ok_eq!(
        JSON0::transform(
          vec![Operation {
            p: vec![],
            kind: OperationKind::ObjectDelete { od: json!(22) }
          }],
          vec![Operation {
            p: vec![],
            kind: OperationKind::NumberAdd { na: 3.0 }
          }],
          TransformSide::Left
        ),
        vec![Operation {
          p: vec![],
          kind: OperationKind::ObjectDelete { od: json!(25) }
        }]
      );

      // assert.deepEqual [{p:[],od:{toves:""},oi:4}], type.transform [{p:[],od:{toves:0},oi:4}], [{p:["toves"],od:0,oi:""}], 'left'
      println!("test 3");
      assert_ok_eq!(
        JSON0::transform(
          vec![Operation {
            p: vec![],
            kind: OperationKind::ObjectReplace {
              od: Value::Object([("toves".to_string(), json!(0),)].iter().cloned().collect()),
              oi: json!(4)
            }
          }],
          vec![Operation {
            p: vec!["toves".into()],
            kind: OperationKind::ObjectReplace {
              od: json!(0),
              oi: Value::String("".to_string())
            }
          }],
          TransformSide::Left
        ),
        vec![Operation {
          p: vec![],
          kind: OperationKind::ObjectReplace {
            od: json!({
                "toves": ""
            }),
            oi: json!(4)
          }
        }]
      );

      // assert.deepEqual [{p:[],od:"thou an",oi:[]}], type.transform [{p:[],od:"thou and",oi:[]}], [{p:[7],sd:"d"}], 'left'
      println!("test 4");
      assert_ok_eq!(
        JSON0::transform(
          vec![Operation {
            p: vec![],
            kind: OperationKind::ObjectReplace {
              od: Value::String("thou and".to_string()),
              oi: Value::Array(vec![])
            }
          }],
          vec![Operation {
            p: vec![7usize.into()],
            kind: OperationKind::StringDelete { sd: "d".to_string() }
          }],
          TransformSide::Left
        ),
        vec![Operation {
          p: vec![],
          kind: OperationKind::ObjectReplace {
            od: Value::String("thou an".to_string()),
            oi: Value::Array(vec![])
          }
        }]
      );

      // assert.deepEqual [{p:[],od:"thou an",oi:[]}], type.transform [{p:[],od:"thou and",oi:[]}], [{p:[], t:'text0', o:[{p:7, d:"d"}]}], 'left'
      // assert_ok_eq!(
      //   JSON0::transform(
      //     vec![Operation {
      //       p: vec![],
      //       kind: OperationKind::ObjectReplace {
      //         od: Value::String("thou and".to_string()),
      //         oi: Value::Array(vec![])
      //       }
      //     }],
      //     vec![Operation {
      //       p: vec![],
      //       kind: OperationKind::TextInsert {
      //         t: "text0".to_string(),
      //         o: vec![Operation {
      //           p: vec![7usize.into()],
      //           kind: OperationKind::StringDelete {
      //             sd: "d".to_string()
      //           }
      //         }]
      //       }
      //     }],
      //     TransformSide::Left
      //   ),
      //   vec![Operation {
      //     p: vec![],
      //     kind: OperationKind::ObjectReplace {
      //       od: Value::String("thou an".to_string()),
      //       oi: Value::Array(vec![])
      //     }
      //   }]
      // );

      // assert.deepEqual [], type.transform([{p:["bird"],na:2}], [{p:[],od:{bird:38},oi:20}], 'right')
      println!("test 5");
      assert_ok_eq!(
        JSON0::transform(
          vec![Operation {
            p: vec!["bird".into()],
            kind: OperationKind::NumberAdd { na: 2.0 }
          }],
          vec![Operation {
            p: vec![],
            kind: OperationKind::ObjectReplace {
              od: json!({
                "bird": 38
              }),
              oi: json!(20)
            }
          }],
          TransformSide::Right
        ),
        vec![]
      );

      // assert.deepEqual [{p:[],od:{bird:40},oi:20}], type.transform([{p:[],od:{bird:38},oi:20}], [{p:["bird"],na:2}], 'left')
      println!("test 6");
      assert_ok_eq!(
        JSON0::transform(
          vec![Operation {
            p: vec![],
            kind: OperationKind::ObjectReplace {
              od: json!({
                "bird": 38
              }),
              oi: json!(20)
            }
          }],
          vec![Operation {
            p: vec!["bird".into()],
            kind: OperationKind::NumberAdd { na: 2.0 }
          }],
          TransformSide::Left
        ),
        vec![Operation {
          p: vec![],
          kind: OperationKind::ObjectReplace {
            od: json!({
                "bird": 40
            }),
            oi: json!(20)
          }
        }]
      );

      // assert.deepEqual [{p:['He'],od:[]}], type.transform [{p:["He"],od:[]}], [{p:["The"],na:-3}], 'right'
      println!("test 7");
      assert_ok_eq!(
        JSON0::transform(
          vec![Operation {
            p: vec!["He".into()],
            kind: OperationKind::ObjectDelete {
              od: Value::Array(vec![])
            }
          }],
          vec![Operation {
            p: vec!["The".into()],
            kind: OperationKind::NumberAdd { na: -3.0 }
          }],
          TransformSide::Right
        ),
        vec![Operation {
          p: vec!["He".into()],
          kind: OperationKind::ObjectDelete {
            od: Value::Array(vec![])
          }
        }]
      );

      // assert.deepEqual [], type.transform [{p:["He"],oi:{}}], [{p:[],od:{},oi:"the"}], 'left'
      println!("test 8");
      assert_ok_eq!(
        JSON0::transform(
          vec![Operation {
            p: vec!["He".into()],
            kind: OperationKind::ObjectInsert {
              oi: json!({})
            }
          }],
          vec![Operation {
            p: vec![],
            kind: OperationKind::ObjectReplace {
              od: json!({}),
              oi: Value::String("the".to_string())
            }
          }],
          TransformSide::Left
        ),
        vec![]
      );
    }

    #[test]
    fn if_two_inserts_are_simultaneous_the_lefts_insert_will_win() {
      // assert.deepEqual [{p:[1], oi:'a', od:'b'}], type.transform [{p:[1], oi:'a'}], [{p:[1], oi:'b'}], 'left'
      assert_ok_eq!(
        JSON0::transform(
          vec![Operation {
            p: vec![1usize.into()],
            kind: OperationKind::ObjectInsert {
              oi: Value::String("a".to_string())
            }
          }],
          vec![Operation {
            p: vec![1usize.into()],
            kind: OperationKind::ObjectInsert {
              oi: Value::String("b".to_string())
            }
          }],
          TransformSide::Left
        ),
        vec![Operation {
          p: vec![1usize.into()],
          kind: OperationKind::ObjectReplace {
            oi: Value::String("a".to_string()),
            od: Value::String("b".to_string())
          }
        }]
      );

      // assert.deepEqual [], type.transform [{p:[1], oi:'b'}], [{p:[1], oi:'a'}], 'right'
      assert_ok_eq!(
        JSON0::transform(
          vec![Operation {
            p: vec![1usize.into()],
            kind: OperationKind::ObjectInsert {
              oi: Value::String("b".to_string())
            }
          }],
          vec![Operation {
            p: vec![1usize.into()],
            kind: OperationKind::ObjectInsert {
              oi: Value::String("a".to_string())
            }
          }],
          TransformSide::Right
        ),
        vec![]
      );
    }

    #[test]
    fn parallel_ops_on_different_keys_miss_each_other() {
      // assert.deepEqual [{p:['a'], oi: 'x'}], type.transform [{p:['a'], oi:'x'}], [{p:['b'], oi:'z'}], 'left'
      assert_ok_eq!(
        JSON0::transform(
          vec![Operation {
            p: vec!["a".into()],
            kind: OperationKind::ObjectInsert {
              oi: Value::String("x".to_string())
            }
          }],
          vec![Operation {
            p: vec!["b".into()],
            kind: OperationKind::ObjectInsert {
              oi: Value::String("z".to_string())
            }
          }],
          TransformSide::Left
        ),
        vec![Operation {
          p: vec!["a".into()],
          kind: OperationKind::ObjectInsert {
            oi: Value::String("x".to_string())
          }
        }]
      );

      // assert.deepEqual [{p:['a'], oi: 'x'}], type.transform [{p:['a'], oi:'x'}], [{p:['b'], od:'z'}], 'left'
      assert_ok_eq!(
        JSON0::transform(
          vec![Operation {
            p: vec!["a".into()],
            kind: OperationKind::ObjectInsert {
              oi: Value::String("x".to_string())
            }
          }],
          vec![Operation {
            p: vec!["b".into()],
            kind: OperationKind::ObjectDelete {
              od: Value::String("z".to_string())
            }
          }],
          TransformSide::Left
        ),
        vec![Operation {
          p: vec!["a".into()],
          kind: OperationKind::ObjectInsert {
            oi: Value::String("x".to_string())
          }
        }]
      );

      // assert.deepEqual [{p:["in","he"],oi:{}}], type.transform [{p:["in","he"],oi:{}}], [{p:["and"],od:{}}], 'right'
      assert_ok_eq!(
        JSON0::transform(
          vec![Operation {
            p: vec!["in".into(), "he".into()],
            kind: OperationKind::ObjectInsert {
              oi: json!({})
            }
          }],
          vec![Operation {
            p: vec!["and".into()],
            kind: OperationKind::ObjectDelete {
              od: json!({})
            }
          }],
          TransformSide::Right
        ),
        vec![Operation {
          p: vec!["in".into(), "he".into()],
          kind: OperationKind::ObjectInsert {
            oi: json!({})
          }
        }]
      );

      // assert.deepEqual [{p:['x',0],si:"his "}], type.transform [{p:['x',0],si:"his "}], [{p:['y'],od:0,oi:1}], 'right'
      assert_ok_eq!(
        JSON0::transform(
          vec![Operation {
            p: vec!["x".into(), 0usize.into()],
            kind: OperationKind::StringInsert { si: "his ".to_string() }
          }],
          vec![Operation {
            p: vec!["y".into()],
            kind: OperationKind::ObjectDelete { od: json!(0) }
          }],
          TransformSide::Right
        ),
        vec![Operation {
          p: vec!["x".into(), 0usize.into()],
          kind: OperationKind::StringInsert { si: "his ".to_string() }
        }]
      );

      // assert.deepEqual [{p:['x'], t:'text0', o:[{p:0, i:"his "}]}], type.transform [{p:['x'],t:'text0', o:[{p:0, i:"his "}]}], [{p:['y'],od:0,oi:1}], 'right'
      // assert_ok_eq!(
      //   JSON0::transform(
      //     vec![Operation {
      //       p: vec!["x".into()],
      //       kind: OperationKind::TextInsert {
      //         t: "text0".to_string(),
      //         o: vec![Operation {
      //           p: vec![0usize.into()],
      //           kind: OperationKind::StringInsert {
      //             si: "his ".to_string()
      //           }
      //         }]
      //       }
      //     }],
      //     vec![Operation {
      //       p: vec!["y".into()],
      //       kind: OperationKind::ObjectDelete {
      //         od: json!(0),
      //       }
      //     }],
      //     TransformSide::Right
      //   ),
      //   vec![Operation {
      //     p: vec!["x".into()],
      //     kind: OperationKind::TextInsert {
      //       t: "text0".to_string(),
      //       o: vec![Operation {
      //         p: vec![0usize.into()],
      //         kind: OperationKind::StringInsert {
      //           si: "his ".to_string()
      //         }
      //       }]
      //     }
      //   }]
      // );
    }

    #[test]
    fn replacement_vs_deletion() {
      // assert.deepEqual [{p:[],oi:{}}], type.transform [{p:[],od:[''],oi:{}}], [{p:[],od:['']}], 'right'
      assert_ok_eq!(
        JSON0::transform(
          vec![Operation {
            p: vec![],
            kind: OperationKind::ObjectReplace {
              od: Value::Array(vec![]),
              oi: json!({})
            }
          }],
          vec![Operation {
            p: vec![],
            kind: OperationKind::ObjectDelete {
              od: Value::Array(vec![])
            }
          }],
          TransformSide::Right
        ),
        vec![Operation {
          p: vec![],
          kind: OperationKind::ObjectInsert {
            oi: json!({})
          }
        }]
      );
    }

    #[test]
    fn replacement_vs_replacement() {
      // assert.deepEqual [], type.transform [{p:[],od:['']},{p:[],oi:{}}], [{p:[],od:['']},{p:[],oi:null}], 'right'
      println!("test 1");
      assert_ok_eq!(
        JSON0::transform(
          vec![
            Operation {
              p: vec![],
              kind: OperationKind::ObjectDelete {
                od: Value::Array(vec![])
              }
            },
            Operation {
              p: vec![],
              kind: OperationKind::ObjectInsert {
                oi: json!({})
              }
            }
          ],
          vec![
            Operation {
              p: vec![],
              kind: OperationKind::ObjectDelete {
                od: Value::Array(vec![]),
              }
            },
            Operation {
              p: vec![],
              kind: OperationKind::ObjectInsert { oi: Value::Null }
            }
          ],
          TransformSide::Right
        ),
        vec![]
      );

      // assert.deepEqual [{p:[],od:null,oi:{}}], type.transform [{p:[],od:['']},{p:[],oi:{}}], [{p:[],od:['']},{p:[],oi:null}], 'left'
      println!("test 2");
      assert_ok_eq!(
        JSON0::transform(
          vec![
            Operation {
              p: vec![],
              kind: OperationKind::ObjectDelete {
                od: Value::Array(vec![])
              }
            },
            Operation {
              p: vec![],
              kind: OperationKind::ObjectInsert {
                oi: json!({})
              }
            }
          ],
          vec![
            Operation {
              p: vec![],
              kind: OperationKind::ObjectDelete {
                od: Value::Array(vec![])
              }
            },
            Operation {
              p: vec![],
              kind: OperationKind::ObjectInsert { oi: Value::Null }
            }
          ],
          TransformSide::Left
        ),
        vec![Operation {
          p: vec![],
          kind: OperationKind::ObjectReplace {
            od: Value::Null,
            oi: json!({})
          }
        }]
      );

      // assert.deepEqual [], type.transform [{p:[],od:[''],oi:{}}], [{p:[],od:[''],oi:null}], 'right'
      println!("test 3");
      assert_ok_eq!(
        JSON0::transform(
          vec![Operation {
            p: vec![],
            kind: OperationKind::ObjectReplace {
              od: Value::Array(vec![]),
              oi: json!({})
            }
          }],
          vec![Operation {
            p: vec![],
            kind: OperationKind::ObjectReplace {
              od: Value::Array(vec![]),
              oi: Value::Null
            }
          }],
          TransformSide::Right
        ),
        vec![]
      );

      // assert.deepEqual [{p:[],od:null,oi:{}}], type.transform [{p:[],od:[''],oi:{}}], [{p:[],od:[''],oi:null}], 'left'
      println!("test 4");
      assert_ok_eq!(
        JSON0::transform(
          vec![Operation {
            p: vec![],
            kind: OperationKind::ObjectReplace {
              od: Value::Array(vec![]),
              oi: json!({})
            }
          }],
          vec![Operation {
            p: vec![],
            kind: OperationKind::ObjectReplace {
              od: Value::Array(vec![]),
              oi: Value::Null
            }
          }],
          TransformSide::Left
        ),
        vec![Operation {
          p: vec![],
          kind: OperationKind::ObjectReplace {
            od: Value::Null,
            oi: json!({})
          }
        }]
      );

      //  # test diamond property
      // rightOps = [ {"p":[],"od":null,"oi":{}} ]
      // leftOps = [ {"p":[],"od":null,"oi":""} ]
      // rightHas = type.apply(null, rightOps)
      // leftHas = type.apply(null, leftOps)

      // [left_, right_] = transformX type, leftOps, rightOps
      // assert.deepEqual leftHas, type.apply rightHas, left_
      // assert.deepEqual leftHas, type.apply leftHas, right_
    }

    #[test]
    fn an_attempt_to_redelete_a_key_becomes_a_no_op() {
      // assert.deepEqual [], type.transform [{p:['k'], od:'x'}], [{p:['k'], od:'x'}], 'left'
      assert_ok_eq!(
        JSON0::transform(
          vec![Operation {
            p: vec!["k".into()],
            kind: OperationKind::ObjectDelete {
              od: Value::String("x".to_string())
            }
          }],
          vec![Operation {
            p: vec!["k".into()],
            kind: OperationKind::ObjectDelete {
              od: Value::String("x".to_string())
            }
          }],
          TransformSide::Left
        ),
        vec![]
      );

      // assert.deepEqual [], type.transform [{p:['k'], od:'x'}], [{p:['k'], od:'x'}], 'right'
      assert_ok_eq!(
        JSON0::transform(
          vec![Operation {
            p: vec!["k".into()],
            kind: OperationKind::ObjectDelete {
              od: Value::String("x".to_string())
            }
          }],
          vec![Operation {
            p: vec!["k".into()],
            kind: OperationKind::ObjectDelete {
              od: Value::String("x".to_string())
            }
          }],
          TransformSide::Right
        ),
        vec![]
      );
    }

    #[test]
    fn throws_when_the_insertion_key_is_a_number() {}

    #[test]
    fn throws_when_the_deletion_key_is_a_number() {}

    #[test]
    fn throws_when_an_object_key_part_way_through_the_path_is_a_number() {}
  }

  // ------------ Tests above is the same as JSON0 ----------------

  #[derive(Debug, Clone)]
  struct TestOp {
    ops: Vec<Vec<Operation>>,
    result: Value,
  }

  #[derive(Debug)]
  struct TestOpStrategy(Value);

  struct TestOpTree(TestOp);

  impl Strategy for TestOpStrategy {
    type Tree = TestOpTree;
    type Value = TestOp;

    fn new_tree(&self, runner: &mut TestRunner) -> NewTree<Self> {
      let mut op_set = TestOp {
        ops: vec![],
        result: self.0.clone(),
      };
      for _ in 0..runner.rng().gen_range(3..15) {
        let (op, new_doc) = Self::gen_random_op(runner.rng(), op_set.result);
        op_set.result = new_doc;
        op_set.ops.push(op);
      }
      Ok(TestOpTree(op_set))
    }
  }

  impl ValueTree for TestOpTree {
    type Value = TestOp;

    fn current(&self) -> Self::Value {
      self.0.clone()
    }

    fn complicate(&mut self) -> bool {
      false
    }

    fn simplify(&mut self) -> bool {
      false
    }
  }

  impl TestOpStrategy {
    fn random_json<G>(g: &mut G) -> Value
    where
      G: Rng,
    {
      match g.gen_range(0..6) {
        0 => Value::Null,
        1 => Value::String("".into()),
        2 => Value::String(Self::random_word(g).into()),
        3 => {
          let mut obj = Map::<String, Value>::new();
          for _ in 1..g.gen_range(0..5) {
            let k = Self::random_new_key(g, &obj);
            obj.insert(k, Self::random_json(g));
          }
          Value::Object(obj.into())
        }
        4 => {
          let mut arr = vec![];
          for _ in 1..g.gen_range(0..5) {
            arr.push(Self::random_json(g));
          }
          Value::Array(arr.into())
        }
        5 => Value::Number(serde_json::Number::from_f64((g.gen_range(0i32..50) as f64)).unwrap()),
        _ => unreachable!(),
      }
    }

    fn random_new_key<G>(g: &mut G, obj: &Map<String, Value>) -> String
    where
      G: Rng,
    {
      loop {
        let k = Self::random_word(g);
        if !obj.contains_key(&k) {
          return k;
        }
      }
    }

    fn random_word<G>(g: &mut G) -> String
    where
      G: Rng,
    {
      let len = g.gen_range(3..8);
      Alphanumeric.sample_string(g, len)
    }

    fn random_key<G>(g: &mut G, data: &Value) -> Option<PathSegment>
    where
      G: Rng,
    {
      if let Value::Array(arr) = data {
        if arr.is_empty() {
          None
        } else {
          Some(g.gen_range(0..arr.len()).into())
        }
      } else if let Value::Object(obj) = data {
        if obj.is_empty() {
          return None;
        }
        let mut count = 0.0;
        let mut result = "";
        for key in obj.keys() {
          count += 1.0;
          if g.gen_range(0.0..1.0) < 1.0 / count {
            result = key;
          }
        }
        Some(result.into())
      } else {
        None
      }
    }

    /// Pick a random path to something in the object.
    fn random_path<G>(g: &mut G, mut data: &Value) -> Vec<PathSegment>
    where
      G: Rng,
    {
      let mut path = vec![];

      while g.gen_range(0.0..1.0) > 0.85 && ((*data).is_array() || data.is_object()) {
        let Some(key) = Self::random_key(g, data) else {
          break
        };
        data = key.index(data).unwrap();
        path.push(key);
      }

      path
    }

    fn gen_random_op<G>(g: &mut G, data: Value) -> (Vec<Operation>, Value)
    where
      G: Rng,
    {
      let mut pct = 0.95;
      let mut container = Value::Object([("data".to_string(), data)].iter().cloned().collect());
      let mut op: Vec<Operation> = vec![];

      while g.gen_range(0.0..1.0) < pct {
        pct *= 0.6;

        // Pick a random object in the document operate on.
        let mut path = Self::random_path(g, container.get("data").unwrap());

        // parent = the container for the operand. parent[key] contains the operand.
        let container_ptr = &container as *const _;
        let mut parent = &mut container;
        let mut key = PathSegment::from("data");
        for p in &path {
          parent = key.index_mut(parent).unwrap();
          key = p.clone();
        }
        let parent_is_array = parent.is_array();
        let parent_ptr = parent as *const _;
        let operand = key.index_mut(parent).unwrap();

        if g.gen_range(0.0..1.0) < 0.4 && parent_ptr != container_ptr && parent_is_array {
          // List move
          let old_index = key.unwrap_number();
          let new_index = g.gen_range(0..parent.as_array().unwrap().len());

          if old_index < new_index {
            parent.as_array_mut().unwrap()[old_index..=new_index].rotate_left(1);
          } else {
            parent.as_array_mut().unwrap()[new_index..=old_index].rotate_right(1);
          }
          op.push(Operation {
            p: path,
            kind: OperationKind::ListMove { lm: new_index },
          });
        } else if g.gen_range(0.0..1.0) < 0.3 || operand.is_null() {
          // Replace

          let new_value = Self::random_json(g);
          let old_value = std::mem::replace(operand, new_value.clone());

          if parent_is_array {
            op.push(Operation {
              p: path,
              kind: OperationKind::ListReplace {
                ld: old_value,
                li: new_value,
              },
            });
          } else {
            op.push(Operation {
              p: path,
              kind: OperationKind::ObjectReplace {
                od: old_value,
                oi: new_value,
              },
            });
          }
        } else {
          match operand {
            Value::Number(num) => {
              // Number
              let inc = (g.gen_range(0..10) - 3) as f64;
              if num.is_f64() {
                let num_val = num.as_f64().unwrap();
                *num = serde_json::Number::from_f64(num_val + inc).unwrap()
              }else if num.is_i64(){
                let num_val = num.as_i64().unwrap();
                *num = serde_json::Number::from_f64((num_val as f64) + (inc)).unwrap()
              }else {
                let num_val = num.as_u64().unwrap();
                *num = serde_json::Number::from(num_val + inc as u64);
              }
              op.push(Operation {
                p: path,
                kind: OperationKind::NumberAdd { na: inc },
              });
            }
            Value::Array(arr) => {
              // Array. Replace is covered above, so we'll just randomly insert or delete.

              if g.gen::<bool>() || arr.is_empty() {
                // Insert
                let pos = g.gen_range(0..arr.len() + 1);
                let obj = Self::random_json(g);
                path.push(pos.into());
                arr.insert(pos, obj.clone());
                op.push(Operation {
                  p: path,
                  kind: OperationKind::ListInsert { li: obj },
                });
              } else {
                // Delete
                let pos = g.gen_range(0..arr.len());
                let obj = arr.remove(pos);
                path.push(pos.clone().into());
                op.push(Operation {
                  p: path,
                  kind: OperationKind::ListDelete { ld: obj },
                });
              }
            }
            Value::Object(_) => {
              // Object
              let k = Self::random_key(g, operand);
              let object = operand.as_object_mut().unwrap();
              if k.is_none() || g.gen::<bool>() {
                // Insert
                let k = Self::random_new_key(g, object);
                let obj = Self::random_json(g);
                path.push(k.clone().into());
                object.insert(k, obj.clone());
                op.push(Operation {
                  p: path,
                  kind: OperationKind::ObjectInsert { oi: obj },
                });
              } else {
                let k = k.unwrap().unwrap_string();
                let obj = object[&k].clone();
                object.remove(&k);
                path.push(k.into());
                op.push(Operation {
                  p: path,
                  kind: OperationKind::ObjectDelete { od: obj },
                });
              }
            }
            _ => {}
          }
        }
      }

      (op, container.get("data").unwrap().clone())
    }
  }

  fn compose_list(ops: Vec<Vec<Operation>>) -> Vec<Operation> {
    ops.into_iter().try_fold(vec![], JSON0::compose).unwrap()
  }

  /// Cross-transform function. Transform server by client and client by server. Returns [server, client].
  fn transform_x(left: Vec<Operation>, right: Vec<Operation>) -> anyhow::Result<(Vec<Operation>, Vec<Operation>)> {
    Ok((
      JSON0::transform(left.clone(), right.clone(), TransformSide::Left)?,
      JSON0::transform(right, left, TransformSide::Right)?,
    ))
  }

  /// Transform a list of server ops by a list of client ops.
  /// Returns [serverOps', clientOps'].
  /// This is O(serverOps.length * clientOps.length)
  fn transform_lists(
    server_ops: Vec<Vec<Operation>>,
    mut client_ops: Vec<Vec<Operation>>,
  ) -> anyhow::Result<(Vec<Vec<Operation>>, Vec<Vec<Operation>>)> {
    let mut server_ops_out = vec![];

    for mut s in server_ops {
      client_ops = client_ops
        .into_iter()
        .map(|c| {
          let (new_s, new_c) = transform_x(s.clone(), c.clone())?;
          s = new_s;
          Ok(new_c)
        })
        .collect::<anyhow::Result<Vec<_>>>()?;

      server_ops_out.push(s);
    }

    Ok((server_ops_out, client_ops))
  }

  proptest! {
    #![proptest_config(ProptestConfig {
      cases: 1000,
      ..ProptestConfig::default()
    })]

    #[test]
    fn apply(op_set in TestOpStrategy(Value::Null)) {
      let mut s = Value::Null;
      for op in op_set.ops {
        s = assert_ok!(JSON0::apply(s, op));
      }

      assert_eq!(s, op_set.result);
    }

    /// Invert all the ops and apply them to result. Should end up with initialDoc.
    #[test]
    fn invert(op_set in TestOpStrategy(Value::Null)) {
      let mut snapshot = op_set.result;

      for op in op_set.ops.into_iter().rev() {
        let op = JSON0::invert(op);
        snapshot = assert_ok!(JSON0::apply(snapshot, op));
      }

      assert_eq!(snapshot, Value::Null);
    }

    /// If all the ops are composed together, then applied, we should get the same result.
    #[test]
    fn compose(op_set in TestOpStrategy(Value::Null)) {
      let op = compose_list(op_set.ops);
      assert_ok_eq!(JSON0::apply(Value::Null, op), op_set.result);
    }

    /// Check the diamond property holds
    #[test]
    fn diamond_property(client in TestOpStrategy(Value::Null), server in TestOpStrategy(Value::Null)) {
      let (server_, client_) = assert_ok!(transform_x(compose_list(server.ops), compose_list(client.ops)));
      let s_c = assert_ok!(JSON0::apply(server.result, client_));
      let c_s = assert_ok!(JSON0::apply(client.result, server_));

      // Interestingly, these will not be the same as s_c and c_s above.
      // Eg, when:
      //  server.ops = [ [ { d: 'x' } ], [ { i: 'c' } ] ]
      //  client.ops = [ 1, { i: 'b' } ]
      prop_assert_eq!(s_c, c_s);
    }

    /// Now we'll check the n^2 transform method.
    #[test]
    fn transform(client in TestOpStrategy(Value::Null), server in TestOpStrategy(Value::Null)) {
      let (s_, c_) = assert_ok!(transform_lists(server.ops.clone(), client.ops));

      let s_c = assert_ok!(c_.clone().into_iter().try_fold(server.result.clone(), JSON0::apply));
      let c_s = assert_ok!(s_.into_iter().try_fold(client.result, JSON0::apply));

      prop_assert_eq!(&s_c, &c_s);

      // ... And we'll do a round-trip using invert().
      let mut c_inv = c_.into_iter().rev().map(JSON0::invert);
      let server_result_ = assert_ok!(c_inv.try_fold(s_c, JSON0::apply));
      prop_assert_eq!(&server.result, &server_result_);

      let orig_ = assert_ok!(server.ops.into_iter().rev()
        .map(JSON0::invert)
        .try_fold(server_result_, JSON0::apply));
      prop_assert_eq!(orig_, Value::Null);
    }
  }
}
