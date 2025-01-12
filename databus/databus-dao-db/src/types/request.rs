use std::collections::HashMap;

use serde::Serialize;

// #[napi(object)]
#[derive(Debug, Clone, Serialize, Default, PartialEq, Eq, Hash)]
pub struct FetchDataPackOrigin {
  pub internal: bool,
  pub main: Option<bool>,
  pub share_id: Option<String>,
  pub not_dst: Option<bool>,
  pub form: Option<bool>,
}

// #[napi(object)]
#[derive(Debug, Clone, Default)]
pub struct FetchDataPackOptions {
  pub record_ids: Option<Vec<String>>,
  pub linked_record_map: Option<HashMap<String, Vec<String>>>,
  pub is_template: Option<bool>,
  /**
   * If true, the returned `resourceIds` will contain foreign datasheet IDs and widget IDs. Otherwise,
   * `resourceIds` will contain the datasheet ID and foreign datasheet IDs.
   */
  pub is_datasheet: Option<bool>,

  // Not yet handled.
  // pub meta: ???
  pub need_extend_main_dst_records: Option<bool>,
}
