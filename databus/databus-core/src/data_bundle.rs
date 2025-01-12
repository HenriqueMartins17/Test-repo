use std::sync::Arc;
use std::sync::Mutex;

use lrumap::LruHashMap;
use lrumap::LruMap;

use crate::prelude::DatasheetPackSO;
/**
 * Singleton Shared instance, for future flexible
 */
// static mut SHARED_DATA_BUNDLE: Option<DataBundle> = None; // singleton databundle

/**
 * DataBundle  = DataState =   NodeJS IDatasheetMap
 *
 * Memory cache stores DatasheetPacks.
 * Replacement of frontend's redux.
 *
 * TODO: Auto GC depends on the memory size, size_of<HashMap<K, V>>() + size_of<K>() * capacity + size_of<V>() * capacity
 */
pub struct DataBundle {
  pub datasheet_map: Arc<Mutex<LruHashMap<String, Box<DatasheetPackSO>>>>,
}

impl DataBundle {
  pub fn new_with_default() -> DataBundle {
    return Self::new(i8::MAX as usize); // 127
  }

  pub fn new(lru_cache_capacity: usize) -> DataBundle {
    return DataBundle {
      datasheet_map: Arc::new(Mutex::new(LruHashMap::new(lru_cache_capacity))),
    };
  }

  /**
   * Whether the datasheet exists
   */
  pub fn exists(&mut self, dst_id: &str) -> bool {
    return self.get(dst_id).is_some();
  }

  /**
   * Get from LRU cache map
   */
  pub fn get(&self, key: &str) -> Option<Box<DatasheetPackSO>> {
    let map = self.datasheet_map.clone();
    let mut locked_map = map.lock().unwrap();
    match locked_map.get(key) {
      Some(entry) => Some(entry.clone()),
      None => None,
    }
  }

  /**
   * Update the datasheet
   */
  pub fn push(&mut self, dst_id: &str, new_datasheet_pack: DatasheetPackSO) -> Option<Box<DatasheetPackSO>> {
    // self.entry(dst_id).get_or_insert(new_datasheet_pack);
    let mut map = self.datasheet_map.as_ref().lock().unwrap();
    let _removed = map.push(dst_id.to_string(), Box::new(new_datasheet_pack));
    drop(map);

    let new = self.get(dst_id);
    return new;
  }
}

#[cfg(test)]
mod tests {
  use crate::mock::get_datasheet_pack;

  use super::*;

  struct TestStruct {
    pub a: i32,
    pub b: i32,
  }

  #[test]
  fn test_lru_map() {
    let mut map: LruHashMap<String, TestStruct> = LruHashMap::new(3);
    map.push("a".to_string(), TestStruct { a: 1, b: 2 });
    map.push("b".to_string(), TestStruct { a: 2, b: 3 });
    map.push("b".to_string(), TestStruct { a: 4, b: 5 });
    assert_eq!(map.get("b").unwrap().a, 4);

    map.push("b".to_string(), TestStruct { a: 6, b: 7 });
    assert_eq!(map.get("b").unwrap().b, 7);
    assert_eq!(map.len(), 2);

    map.push("c".to_string(), TestStruct { a: 6, b: 7 });
    map.push("d".to_string(), TestStruct { a: 6, b: 7 });
    assert_eq!(map.len(), 3);

    let a_option = map.get("a");
    assert!(a_option.is_none());
  }

  #[test]
  fn test_data_bundle() {
    let datasheet_pack = get_datasheet_pack().unwrap();
    assert_eq!(datasheet_pack.snapshot.meta.views.len(), 3);

    let mut data_bundle = DataBundle::new(3);

    data_bundle.push("a", datasheet_pack.clone());
    data_bundle.push("b", datasheet_pack.clone());
    data_bundle.push("c", datasheet_pack.clone());
    data_bundle.push("d", datasheet_pack.clone());

    assert_eq!(data_bundle.datasheet_map.lock().unwrap().len(), 3);

    let b_bundle = data_bundle.get("b");
    assert_eq!(b_bundle.unwrap().snapshot.meta.views.len(), 3);
  }
}
