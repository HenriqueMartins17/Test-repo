// use self::MockLoader;
// mod mock_json;
// mod mock_loader;
use crate::mock::{MockLoader, MockLoaderType, MOCK_DATASHEET_PACK_JSON};

#[cfg(test)]
mod tests {
  use std::sync::Arc;

  use futures::executor::block_on;

  use super::*;

  #[test]
  fn test_lib() {
    let loader = Arc::new(MockLoader {
      json: MOCK_DATASHEET_PACK_JSON.to_string(),
    });
    let f = databus_core::init(true, "https://integration.vika.ltd".to_string(), loader);
    block_on(f);

    let f2 = databus_core::DataFunctionsManager::get_instance().get_datasheet_pack("mock");
    let result_datasheet_pack = block_on(f2);
    let datasheet_pack = result_datasheet_pack.unwrap();
    let view = &datasheet_pack.snapshot.meta.views[0];
    let view_id = view.get("id").unwrap().as_str();
    assert_eq!(view_id, Some("viwFhekcq6AGY"));
  }
}
