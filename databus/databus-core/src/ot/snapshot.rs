use serde::{Deserialize, Serialize};
use std::collections::HashMap;

use crate::so::record::{Meta, Record};

/**
 * Snapshot is the basic data OT data structure tho hold the data includes:
 * - Datasheet Views
 * - Views Records
 * - Records Rows
 */
#[derive(Serialize, Deserialize)]
pub struct Snapshot {
  pub meta: Meta,

  #[serde(rename = "recordMap")]
  pub record_map: HashMap<String, Record>,

  #[serde(rename = "datasheetId")]
  pub datasheet_id: Option<String>,
  // pub foreignDatasheetMap: HashMap<String, Snapshot>,
}

impl Snapshot {
  /**
   * Make a JSON string in to a snapshot
   */
  pub fn deserialize(snapshot_json: &str) -> Self {
    let snapshot: Snapshot = serde_json::from_str(snapshot_json).unwrap();
    return snapshot;
  }

  pub fn new(datasheet_id: &str) -> Self {
    let record_map: HashMap<String, Record> = HashMap::new();

    return Self {
      meta: Meta::new(),
      record_map: record_map,
      datasheet_id: Some(datasheet_id.to_string()),
    };
  }

  pub fn get_records(&self) -> Vec<&Record> {
    let mut records: Vec<&Record> = Vec::new();
    for (_, record) in &self.record_map {
      records.push(record);
    }
    return records;
  }
}
