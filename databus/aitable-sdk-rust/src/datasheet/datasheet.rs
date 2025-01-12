use std::sync::Arc;
use databus_shared::http::HttpSuccessResponse;

use crate::client::ApiTableClient;
use crate::datasheet::field::FieldManager;
use crate::datasheet::record::RecordManager;
use crate::datasheet::view::ViewManager;

/// Datasheet API
#[derive(Debug)]
pub struct Datasheet {
  pub(crate) client: Arc<ApiTableClient>,
  pub(crate) config: Arc<DatasheetConfig>,
}

impl Datasheet {
  pub fn new(client: Arc<ApiTableClient>, dst_id: String) -> Self {
    Self { client, config: Arc::new(DatasheetConfig { dst_id }) }
  }

  /// create field manager
  pub fn fields(&self) -> FieldManager {
    FieldManager::new(self.client.clone(), self.config.clone())
  }

  /// create view manager
  pub fn views(&self) -> ViewManager {
    ViewManager::new(self.client.clone(), self.config.clone())
  }

  /// create record manager
  pub fn records(&self) -> RecordManager {
    RecordManager::new(self.client.clone(), self.config.clone())
  }

  // TODO: implement
  pub async fn create(&self) -> anyhow::Result<HttpSuccessResponse<String>> {
    let url = format!("/spaces/{}/datasheets", "SPACE_ID");
    let mut builder = self.client.post(url);
    Ok(builder.send().await?)
  }

  // TODO: implement
  pub async fn get_presigned_url(&self) -> anyhow::Result<HttpSuccessResponse<String>> {
    let url = format!("/datasheets/{}/attachments/presignedUrl", self.config.dst_id);
    let mut builder = self.client.get(url);
    Ok(builder.send().await?)
  }

  // TODO: implement
  pub async fn get_spaces(&self) -> anyhow::Result<HttpSuccessResponse<String>> {
    let url = format!("/spaces");
    let mut builder = self.client.get(url);
    Ok(builder.send().await?)
  }

  // TODO: implement
  pub async fn get_nodes(&self) -> anyhow::Result<HttpSuccessResponse<String>> {
    let url = format!("/spaces/{}/nodes", "SPACE_ID");
    let mut builder = self.client.get(url);
    Ok(builder.send().await?)
  }

  // TODO: implement
  pub async fn node_detail(&self) -> anyhow::Result<HttpSuccessResponse<String>> {
    let url = format!("/nodes/{}", "NODE_ID");
    let mut builder = self.client.get(url);
    Ok(builder.send().await?)
  }

  // TODO: implement
  pub async fn execute_command(&self) -> anyhow::Result<HttpSuccessResponse<String>> {
    let url = format!("/datasheets/{}/executeCommand", self.config.dst_id);
    let mut builder = self.client.post(url);
    Ok(builder.send().await?)
  }
}

#[derive(Debug)]
pub struct DatasheetConfig {
  pub dst_id: String,
}
