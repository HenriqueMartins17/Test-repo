// use crate::{so::DatasheetSnapshotSO, ot::datasheet::calc_cell_value_and_string};

// use super::{CellValueData, cache::cache::CACHE};


// pub struct CacheManager;

// impl CacheManager {
//     pub fn calc_ds_cache(
//         // state: &IReduxState, 
//         snapshot: &DatasheetSnapshotSO
//     ) {
//         let datasheet_id = snapshot.datasheet_id.clone();
//         let record_map = snapshot.record_map.clone();
//         let field_map = snapshot.meta.field_map.clone();
        
//         // if datasheet_id.is_none() {
//         //     return;
//         // }
        
//         for field_id in field_map.keys() {
//             for record_id in record_map.keys() {
//                 let cell_cache = calc_cell_value_and_string(
//                     // state,
//                     snapshot,
//                     field_id,
//                     record_id,
//                     Some(&datasheet_id),
//                     Some(false),
//                     Some(true),
//                 );
//                 CacheManager::set_cell_cache(
//                     &datasheet_id,
//                     field_id,
//                     record_id,
//                     cell_cache,
//                 );
//             }
//         }
//     }
    
//     pub fn clear_ds_cache(ds_id: &str) {
//         unsafe { CACHE.remove_ds_cache(ds_id) };
//     }
    
//     pub fn remove_cell_cache(ds_id: &str, field_id: &str, record_id: Option<&str>) {
//         unsafe { CACHE.remove_cell_cache(ds_id, field_id, record_id) };
//     }
    
//     pub fn remove_cell_cache_by_record(ds_id: &str, record_id: &str) {
//         unsafe { CACHE.remove_cell_cache_by_record(ds_id, record_id) };
//     }
    
//     pub fn get_cell_cache(ds_id: &str, field_id: &str, record_id: &str) -> Option<CellValueData> {
//         let cv = unsafe { CACHE.get_cell_cache(ds_id, field_id, record_id) };
//         let cv = cv.unwrap();
//         let cv = *cv;
//         Some(cv)
//     }
    
//     pub fn set_cell_cache(ds_id: &str, field_id: &str, record_id: &str, cell_cache: CellValueData) {
//         unsafe { CACHE.add_cell_cache(ds_id, field_id, record_id, cell_cache) };
//     }
    
//     pub fn clear() {
//         unsafe { CACHE.clear_cache() };
//     }
// }