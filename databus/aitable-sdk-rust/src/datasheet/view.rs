use std::sync::Arc;
use databus_shared::http::HttpSuccessResponse;

use crate::client::ApiTableClient;
use crate::datasheet::datasheet::DatasheetConfig;

#[allow(dead_code)]
pub struct ViewManager {
  client: Arc<ApiTableClient>,
  config: Arc<DatasheetConfig>,
}

impl ViewManager {
  pub fn new(client: Arc<ApiTableClient>, config: Arc<DatasheetConfig>) -> Self {
    Self { client, config }
  }

  // TODO: implement
  pub async fn get(&self) -> anyhow::Result<HttpSuccessResponse<String>> {
    let url = format!("/datasheets/{}/views", self.config.dst_id);
    let mut builder = self.client.get(url);
    Ok(builder.send().await?)
  }
}
