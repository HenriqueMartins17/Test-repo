use serde::{Deserialize, Serialize};

use crate::so::CellValueSo;

#[derive(Deserialize, Serialize, Debug, Clone)]
#[serde(rename_all = "camelCase")]
pub struct CellValueData {
    pub cell_value: Option<CellValueSo>,
    pub cell_str: Option<String>,
    pub ignore_cache: Option<bool>,
}

// // use lru_cache::LruCache;

// pub mod cache {
//     use std::collections::HashMap;

//     use super::CellValueData;
//     type TFieldCache = HashMap<String, CellValueData>;

//     struct IDsCache {
//         cell_values: HashMap<String, TFieldCache>,
//     }

//     pub struct Cache {
//         // ds_map: LruCache<String, Option<IDsCache>>,
//         ds_map: HashMap<String, Option<IDsCache>>,
//     }

//     impl Cache {
//         fn new() -> Self {
//             Cache {
//                 // ds_map: LruCache::new(50),
//                 ds_map: HashMap::new(),
//             }
//         }

//         fn get_ds_cache(&self, ds_id: &str) -> Option<&IDsCache> {
//             self.ds_map.get(ds_id).and_then(|ds| ds.as_ref())
//         }

//         fn get_ds_fields(&self, ds_id: &str) -> Option<Vec<String>> {
//             let ds = self.get_ds_cache(ds_id)?;
//             let fields = ds.cell_values.keys().cloned().collect();
//             Some(fields)
//         }

//         pub fn add_cell_cache(&mut self, ds_id: &str, field_id: &str, record_id: &str, value: CellValueData) {
//             let ds_cache = self.ds_map.entry(ds_id.to_string()).or_insert_with(|| Some(IDsCache {
//                 cell_values: HashMap::new(),
//             }));
//             let field_cache = ds_cache.as_mut().unwrap().cell_values.entry(field_id.to_string()).or_insert_with(|| HashMap::new());
//             field_cache.insert(record_id.to_string(), value);
//         }

//         pub fn remove_cell_cache(&mut self, ds_id: &str, field_id: &str, record_id: Option<&str>) -> bool {
//             let ds = self.get_ds_cache(ds_id);
//             if ds.is_none() {
//                 return false;
//             }
//             let ds_cache = ds.unwrap();
//             if let Some(field_cache) = ds_cache.cell_values.get_mut(field_id) {
//                 if let Some(record_id) = record_id {
//                     field_cache.remove(record_id);
//                 } else {
//                     field_cache.clear();
//                 }
//                 true
//             } else {
//                 false
//             }
//         }

//         pub fn remove_cell_cache_by_record(&mut self, ds_id: &str, record_id: &str) -> bool {
//             let fields = self.get_ds_fields(ds_id);
//             if fields.is_none() {
//                 return false;
//             }
//             let fields = fields.unwrap();
//             fields.iter().all(|field_id| self.remove_cell_cache(ds_id, field_id, Some(record_id)))
//         }

//         pub fn remove_ds_cache(&mut self, ds_id: &str) {
//             self.ds_map.insert(ds_id.to_string(), None);
//         }

//         pub fn get_cell_cache(&self, ds_id: &str, field_id: &str, record_id: &str) -> Option<&CellValueData> {
//             let ds = self.get_ds_cache(ds_id)?;
//             let field = ds.cell_values.get(field_id)?;
//             field.get(record_id)
//         }

//         pub fn clear_cache(&mut self) {
//             self.ds_map.clear();
//         }
//     }
//     pub static mut CACHE: Cache = Cache::new();
// }
