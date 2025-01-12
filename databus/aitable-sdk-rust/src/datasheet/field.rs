use std::sync::Arc;
use databus_shared::http::HttpSuccessResponse;
use crate::client::ApiTableClient;

use crate::datasheet::datasheet::DatasheetConfig;

#[allow(dead_code)]
pub struct FieldManager {
  client: Arc<ApiTableClient>,
  config: Arc<DatasheetConfig>,
}

impl FieldManager {
  pub fn new(client: Arc<ApiTableClient>, config: Arc<DatasheetConfig>) -> Self {
    Self { client, config }
  }

  // TODO: implement
  pub async fn get(&self) -> anyhow::Result<HttpSuccessResponse<String>> {
    let url = format!("/datasheets/{}/fields", self.config.dst_id);
    let mut builder = self.client.get(url);
    Ok(builder.send().await?)
  }

  // TODO: implement
  pub async fn create(&self) -> anyhow::Result<HttpSuccessResponse<String>> {
    let url = format!("/spaces/{}/datasheets/{}/fields", "SPACE_ID", self.config.dst_id);
    let mut builder = self.client.post(url);
    Ok(builder.send().await?)
  }

  // TODO: implement
  pub async fn delete(&self) -> anyhow::Result<HttpSuccessResponse<String>> {
    let url = format!("/spaces/{}/datasheets/{}/fields/{}", "SPACE_ID", self.config.dst_id, "FIELD_ID");
    let mut builder = self.client.delete(url);
    Ok(builder.send().await?)
  }
}
