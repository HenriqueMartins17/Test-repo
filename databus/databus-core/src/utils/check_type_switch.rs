// use crate::so::{FieldKindSO, FieldSO};
// use crate::so::view_operation::filter::IFilterCondition;
//
// // Check that the column type switch causes the filter to fail
// pub fn check_type_switch(item: Option<IFilterCondition>, field: Option<FieldSO>) -> bool {
//   let is_same_type = item.map_or(false, |i| i.field_type == field.map_or(None, |f| Some(f.type)));
//   if !is_same_type {
//     return true;
//   }
//
//   // After checking for two type switches (the type didn't change), but the option id changed
//   if let Some(item) = item {
//     if [FieldKindSO::SingleSelect, FieldKindSO::MultiSelect].contains(&item.field_type) {
//       let value = item.value;
//       let options = if let Some(field) = field {
//         field.property.as_ref().unwrap().options.as_ref().unwrap();
//       } else {
//         return false;
//       };
//       let ids = options.iter().map(|o| o.id).collect::<Vec<_>>();
//       // value does not exist, indicating that it has not been selected
//       // Determine whether value has a corresponding value in options id
//       return value.is_some() && match value {
//         Some(v) => {
//           if let Some(v) = v.as_array() {
//             !v.iter().all(|v| ids.contains(v))
//           } else {
//             !ids.contains(v)
//           }
//         },
//         None => false
//       };
//     }
//   }
//
//   false
// }