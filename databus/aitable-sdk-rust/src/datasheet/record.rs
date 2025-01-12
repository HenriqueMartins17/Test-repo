use std::collections::HashMap;
use std::sync::Arc;

use serde_json::Value;

use databus_core::ro::record_create_ro::{RecordCreateItemRO, RecordCreateRO};
use databus_core::ro::record_update_ro::FieldKeyEnum;
use databus_core::vo::api_page::ApiPage;
use databus_core::vo::record_vo::{RecordCreateVo, RecordDTO};
use databus_shared::http::HttpSuccessResponse;

use crate::client::ApiTableClient;
use crate::datasheet::datasheet::DatasheetConfig;
use crate::datasheet::types::FieldValue;

pub type GetRecordsResponse = HttpSuccessResponse<ApiPage<Vec<RecordDTO>>>;

pub struct RecordManager {
  client: Arc<ApiTableClient>,
  #[allow(dead_code)]
  config: Arc<DatasheetConfig>,
  url: String,
}

impl RecordManager {
  pub fn new(client: Arc<ApiTableClient>, config: Arc<DatasheetConfig>) -> Self {
    let url = format!("/datasheets/{}/records", config.dst_id.clone());
    Self { client, config, url }
  }

  pub async fn get(
    &self,
    record_ids: Vec<&str>,
  ) -> anyhow::Result<HttpSuccessResponse<ApiPage<Vec<RecordDTO>>>> {
    let mut builder = self.client.get(self.url.clone());
    if record_ids.len() > 0 {
      builder = builder.query(&[("recordIds", record_ids.join(","))]);
    }
    Ok(builder.send().await?)
  }

  pub async fn query(&self) -> anyhow::Result<()> {
    unimplemented!()
  }

  pub async fn create(
    &self,
    field_key: FieldKeyEnum,
    records: &Vec<HashMap<String, FieldValue>>,
  ) -> anyhow::Result<HttpSuccessResponse<RecordCreateVo>> {
    let mut builder = self.client.post(self.url.clone());

    let records_ro = records.iter().map(|record| {
      let mut fields = HashMap::<String, Value>::new();
      for (k, v) in record {
        fields.insert(k.to_string(), v.to_json_value());
      }

      RecordCreateItemRO { fields }
    }).collect();

    let ro = RecordCreateRO {
      records: records_ro,
      field_key,
    };

    builder = builder.body(&ro);
    Ok(builder.send().await?)
  }

  // TODO: implement
  pub async fn update_patch(&self) -> anyhow::Result<HttpSuccessResponse<String>> {
    let mut builder = self.client.patch(self.url.clone());
    Ok(builder.send().await?)
  }

  // TODO: implement
  pub async fn update_put(&self) -> anyhow::Result<HttpSuccessResponse<String>> {
    let mut builder = self.client.put(self.url.clone());
    Ok(builder.send().await?)
  }

  // TODO: implement
  pub async fn delete(&self) -> anyhow::Result<HttpSuccessResponse<String>> {
    let mut builder = self.client.delete(self.url.clone());
    Ok(builder.send().await?)
  }
}
