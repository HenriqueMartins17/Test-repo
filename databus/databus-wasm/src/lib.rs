mod utils;
use databus_core::so::DatasheetSnapshotSO;
use std::sync::Arc;

use wasm_bindgen::prelude::*;

// #[cfg(target = "wasm32-wasi")]
// use databus_dao_db::DBLoader;

// #[cfg(target_family = "wasm")]
// use databus_dao_api::WebFetchLoader;

// use databus_core::DataServicesManager;
use databus_core::{ModifyViewOTO,ColumnWidthOTO};
use databus_core::{DatasheetActions,PayloadAddViewVO,PayloadMoveViewVO,PayloadDelViewVO,PayloadAddRecordVO,PayloadAddFieldVO,PayloadDelFieldVO};

use serde::{Deserialize, Serialize};

use gloo_utils::format::JsValueSerdeExt;

use databus_core::data_source_provider::IDataSourceProvider;
use databus_core::ot::commands::datasheet_action::set_record_to_action;

use databus_core::ot::types::SetRecordOTO;
use databus_core::DataServicesManager;
use databus_dao_api::APIDataSourceProvider;
extern crate console_error_panic_hook;

use json0::{Operation, TransformSide, JSON0};
use log::info;
use std::panic;
use serde_json::json;

// When the `wee_alloc` feature is enabled, use `wee_alloc` as the global
// allocator.
#[cfg(feature = "wee_alloc")]
#[global_allocator]
static ALLOC: wee_alloc::WeeAlloc = wee_alloc::WeeAlloc::INIT;

#[derive(Debug, Serialize, Deserialize)]
pub struct ApiResponse<T> {
  code: u16,
  success: bool,
  data: Option<T>,
  message: String,
}

impl<T> ApiResponse<T> {
  // Constructor for successful responses with data
  pub fn success(data: T) -> Self {
    Self {
      code: 200,
      success: true,
      data: Some(data),
      message: "SUCCESS".to_string(),
    }
  }

  // Constructor for successful responses without data
  pub fn success_empty() -> Self {
    Self {
      code: 200,
      success: true,
      data: None,
      message: "SUCCESS".to_string(),
    }
  }

  // Constructor for failed responses
  pub fn error(code: u16, message: &str) -> Self {
    Self {
      code,
      success: false,
      data: None,
      message: message.to_string(),
    }
  }
}

#[derive(Debug, Serialize, Deserialize)]
pub struct SheetData {
  id: Option<u32>,
  dst_id: String,
  reversion: i32,
  content: String,
}

#[wasm_bindgen]
extern "C" {
  // #[wasm_bindgen(js_namespace = console)]
  // fn log(s: &str);
}

#[wasm_bindgen]
pub struct DataBusBridge {
  base_url: String,
  room_server_url: String,

  loader: Arc<APIDataSourceProvider>,
  data_services_manager: Arc<DataServicesManager>,
  // index_db_client: Option<Rexie>,
}

#[wasm_bindgen]
impl DataBusBridge {
  #[wasm_bindgen(constructor)]
  pub fn new(base_url: String, room_server_url: String) -> DataBusBridge {
    panic::set_hook(Box::new(console_error_panic_hook::hook));
    wasm_logger::init(wasm_logger::Config::default().message_on_new_line());
    info!("wasm init panic hook...");
    info!("wasm databus url is {:?}!", base_url);
    info!("wasm room-server url is {:?}!", room_server_url);
    let loader = Arc::new(APIDataSourceProvider::new(room_server_url.as_str()));
    let clone_loader = loader.clone();
    let val = databus_core::init(false, true, "TODO".to_string(), loader);
    let data_services_manager = Arc::new(val);
    DataBusBridge {
      base_url,
      room_server_url,
      loader: clone_loader,
      data_services_manager,
      // index_db_client: None,
    }
  }

  #[wasm_bindgen]
  pub async fn init(&mut self) {
    // Logging
    // let client = Rexie::builder("dst")
    //   .version(2)
    //   .add_object_store(
    //     ObjectStore::new("sheet")
    //       .key_path("id")
    //       .auto_increment(true)
    //       .add_index(Index::new("dst_id", "dst_id").unique(true)),
    //   )
    //   .build()
    //   .await
    //   .unwrap();
    // self.index_db_client = Some(client);
  }

  #[wasm_bindgen]
  pub async fn delete_cache(&self, dst_id: String) -> Result<JsValue, JsValue> {
    info!("wasm delete_cache for dst {}", dst_id);
    let result = self.loader.delete_cache(dst_id.as_str()).await;
    match result {
      Ok(r) => {
        return Ok(JsValue::from_bool(r));
      }
      Err(e) => {
        return Err(JsValue::from_str(e.to_string().as_str()));
      }
    }
  }

  #[wasm_bindgen]
  pub async fn get_datasheet_pack(&self, dst_id: String) -> Result<JsValue, JsValue> {
    info!("wasm get_datasheet_pack for dst {}", dst_id);
    let pack = self.loader.get_datasheet_pak_js(dst_id.as_str(), None, None).await;
    return Ok(pack);
  }

  #[wasm_bindgen]
  pub fn json0_create(&self, data: Option<String>) -> Option<String> {
    data.map(|d| d.clone())
  }

  #[wasm_bindgen]
  pub fn json0_apply(&self, snapshot: &str, operation: &str) -> Result<String, JsValue> {
    info!("json0_apply starting...");

    if snapshot.is_empty() {
      return Err(JsValue::from_str("Snapshot string is empty"));
    }

    if operation.is_empty() {
      return Err(JsValue::from_str("Operation string is empty"));
    }

    let snapshot_value = serde_json::from_str(snapshot).map_err(|e| handle_error(e, "Error parsing snapshot"))?;

    let operation_value = serde_json::from_str(operation).map_err(|e| handle_error(e, "Error parsing operation"))?;

    let result =
      JSON0::apply(snapshot_value, operation_value).map_err(|e| handle_error(e, "Error applying operation"))?;

    let result_str = serde_json::to_string(&result).map_err(|e| handle_error(e, "Error serializing result"))?;

    info!("json0_apply end...");

    Ok(result_str)
  }

  #[wasm_bindgen]
  pub fn json0_transform(&self, _op: &str, _other_op: &str, _op_type: &str) -> Result<JsValue, JsValue> {
    info!("json0_transform starting...");

    let op_value = serde_json::from_str(_op).map_err(|e| handle_error(e, "Error parsing op"))?;

    let other_op_value = serde_json::from_str(_other_op).map_err(|e| handle_error(e, "Error parsing otherOp"))?;

    let op_type_str = _op_type.to_string();

    let cleaned_op_type_str = op_type_str.trim_matches('"');

    let op_type_value = match cleaned_op_type_str {
      "left" => TransformSide::Left,
      "right" => TransformSide::Right,
      _ => {
        return Err(JsValue::from_str(&format!(
          "Unknown transform side: {}",
          cleaned_op_type_str
        )))
      }
    };

    let result = JSON0::transform(op_value, other_op_value, op_type_value)
      .map_err(|e| handle_error(e, "Error transforming operations"))?;

    let result_str = serde_json::to_string(&result).map_err(|e| handle_error(e, "Error serializing result"))?;

    info!("json0_transform end...");

    Ok(JsValue::from_str(&result_str))
  }

  #[wasm_bindgen]
  pub fn json0_transform_x(&self, left_op: &str, right_op: &str) -> Result<JsValue, JsValue> {
    info!("json0_transformX starting...");

    let left_op_value = serde_json::from_str(left_op).map_err(|e| handle_error(e, "Error parsing leftOp"))?;

    let right_op_value = serde_json::from_str(right_op).map_err(|e| handle_error(e, "Error parsing rightOP"))?;

    let result = JSON0::transform_x(left_op_value, right_op_value)
      .map_err(|e| handle_error(e, "Error transforming operations"))?;

    let result_str = serde_json::to_string(&result).map_err(|e| handle_error(e, "Error serializing result"))?;

    info!("json0_transformX end...");

    Ok(JsValue::from_str(&result_str))
  }

  #[wasm_bindgen]
  pub fn json0_invert(&self, op: &str) -> Result<JsValue, JsValue> {
    info!("json0_invert starting...");

    let op_value = serde_json::from_str(op).map_err(|e| handle_error(e, "Error parsing op"))?;

    let result = JSON0::invert(op_value);

    let result_str = serde_json::to_string(&result).map_err(|e| handle_error(e, "Error serializing result"))?;

    info!("json0_invert end...");

    Ok(JsValue::from_str(&result_str))
  }

  #[wasm_bindgen]
  pub fn json0_compose(&self, op: &str, other_op: &str) -> Result<JsValue, JsValue> {
    info!("json0_compose starting...");

    let op_value = serde_json::from_str(op).map_err(|e| handle_error(e, "Error parsing op"))?;

    let other_op_value = serde_json::from_str(other_op).map_err(|e| handle_error(e, "Error parsing otherOp"))?;

    let result = JSON0::compose(op_value, other_op_value).map_err(|e| handle_error(e, "Error composing operations"))?;

    let result_str = serde_json::to_string(&result).map_err(|e| handle_error(e, "Error serializing result"))?;

    info!("json0_compose end...");

    Ok(JsValue::from_str(&result_str))
  }
}

fn handle_error<E: std::fmt::Display>(e: E, message: &str) -> JsValue {
  let error_message = format!("{}: {}", message, e);
  info!("{}", error_message);
  JsValue::from_str(&error_message)
}

#[wasm_bindgen]
pub fn json0_seri() -> Result<JsValue, JsValue> {
  let obj = json!([
    {
      "name": "Alice",
      "age": 30,
      "is_student": false
    }
  ]
  );
  return Ok(JsValue::from_serde(&obj).unwrap());
}

#[wasm_bindgen]
pub fn json0_inverse(op: JsValue) -> Result<JsValue, JsValue> {
  info!("wasm in json0_inverse");
  let op_obj_in_json0 = op.into_serde::<Vec<Operation>>().unwrap();
  let result = JSON0::invert(op_obj_in_json0);
  return Ok(JsValue::from_serde(&result).unwrap());
}

// not work in ndoejs
#[wasm_bindgen]
pub fn action_set_cell(snapshot: JsValue, payload: JsValue) -> Result<JsValue, JsValue> {
  let snapshot_in_wasm = snapshot.into_serde::<DatasheetSnapshotSO>().unwrap();
  let payload_in_wasm = payload.into_serde::<SetRecordOTO>().unwrap();
  let result = set_record_to_action(snapshot_in_wasm, payload_in_wasm).unwrap();
  return Ok(JsValue::from_serde(&result).unwrap());
}

#[wasm_bindgen]
pub fn add_tn(a: i32, b: i32) -> i32 {
  a + b
}

#[wasm_bindgen]
pub fn get_records(_dst_id: &str) -> JsValue {
  todo!()
}

#[wasm_bindgen]
pub fn action_add_view(
  snapshot: JsValue,
  payload: JsValue,
) -> Result<JsValue, JsValue> {
  let snapshot_in_wasm = snapshot.into_serde::<DatasheetSnapshotSO>().unwrap();
  let payload_in_wasm = payload.into_serde::<PayloadAddViewVO>().unwrap();
  let result = DatasheetActions::add_view_to_action(snapshot_in_wasm, payload_in_wasm).unwrap();
  return Ok(JsValue::from_serde(&result).unwrap())
}

#[wasm_bindgen]
pub fn action_move_view(
  snapshot: JsValue,
  payload: JsValue,
) -> Result<JsValue, JsValue> {
  let snapshot_in_wasm = snapshot.into_serde::<DatasheetSnapshotSO>().unwrap();
  let payload_in_wasm = payload.into_serde::<PayloadMoveViewVO>().unwrap();
  let result = DatasheetActions::move_view_to_action(snapshot_in_wasm, payload_in_wasm).unwrap();
  return Ok(JsValue::from_serde(&result).unwrap())
}

#[wasm_bindgen]
pub fn action_del_view(
  snapshot: JsValue,
  payload: JsValue,
) -> Result<JsValue, JsValue> {
  let snapshot_in_wasm = snapshot.into_serde::<DatasheetSnapshotSO>().unwrap();
  let payload_in_wasm = payload.into_serde::<PayloadDelViewVO>().unwrap();
  let result = DatasheetActions::delete_view_to_action(snapshot_in_wasm, payload_in_wasm).unwrap();
  return Ok(JsValue::from_serde(&result).unwrap())
}

#[wasm_bindgen]
pub fn action_modify_view(
  snapshot: JsValue,
  payload: JsValue,
) -> Result<JsValue, JsValue> {
  let snapshot_in_wasm = snapshot.into_serde::<DatasheetSnapshotSO>().unwrap();
  let payload_in_wasm = payload.into_serde::<ModifyViewOTO>().unwrap();
  let result = DatasheetActions::modify_view_to_action(snapshot_in_wasm, payload_in_wasm).unwrap();
  return Ok(JsValue::from_serde(&result).unwrap())
}

#[wasm_bindgen]
pub fn action_set_column_width(
  snapshot: JsValue,
  payload: JsValue,
) -> Result<JsValue, JsValue> {
  let snapshot_in_wasm = snapshot.into_serde::<DatasheetSnapshotSO>().unwrap();
  let payload_in_wasm = payload.into_serde::<ColumnWidthOTO>().unwrap();
  let result = DatasheetActions::set_column_width_to_action(snapshot_in_wasm, payload_in_wasm).unwrap();
  return Ok(JsValue::from_serde(&result).unwrap())
}

#[wasm_bindgen]
pub fn action_add_record(
  snapshot: JsValue,
  payload: JsValue,
) -> Result<JsValue, JsValue> {
  let snapshot_in_wasm = snapshot.into_serde::<DatasheetSnapshotSO>().unwrap();
  let payload_in_wasm = payload.into_serde::<PayloadAddRecordVO>().unwrap();
  let result = DatasheetActions::add_record_to_action(snapshot_in_wasm, payload_in_wasm).unwrap();
  return Ok(JsValue::from_serde(&result).unwrap());
}

#[wasm_bindgen]
pub fn action_add_field(
  snapshot: JsValue,
  payload: JsValue,
) -> Result<JsValue, JsValue> {
  let snapshot_in_wasm = snapshot.into_serde::<DatasheetSnapshotSO>().unwrap();
  let payload_in_wasm = payload.into_serde::<PayloadAddFieldVO>().unwrap();
  let result = DatasheetActions::add_field_to_action(snapshot_in_wasm, payload_in_wasm).unwrap();
  return Ok(JsValue::from_serde(&result).unwrap())
}

#[wasm_bindgen]
pub fn action_delete_field(
  snapshot: JsValue,
  payload: JsValue,
) -> Result<JsValue, JsValue> {
  let snapshot_in_wasm = snapshot.into_serde::<DatasheetSnapshotSO>().unwrap();
  let payload_in_wasm = payload.into_serde::<PayloadDelFieldVO>().unwrap();
  let result = DatasheetActions::delete_field_to_action(snapshot_in_wasm, payload_in_wasm).unwrap();
  return Ok(JsValue::from_serde(&result).unwrap())
}
