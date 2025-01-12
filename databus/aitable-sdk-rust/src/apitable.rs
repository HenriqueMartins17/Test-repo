use std::fmt::Debug;
use std::sync::Arc;

use crate::client::ApiTableClient;
use crate::datasheet::datasheet::Datasheet;

/// ApiTable SDK facade
/// ```
/// use aitable_sdk_rust::apitable::{ApiTable, ApiTableConfig};
///
/// let host = "https://DOMAIN.xyz".to_string();
/// let token = "ACCESS_TOKEN".to_string();
/// let dst_id = "DATASHEET_ID".to_string();
///
/// // create apitable
/// let config = ApiTableConfig::new(host, token);
/// let apitable = ApiTable::new(config);
///
/// // create datasheet
/// let datasheet = apitable.datasheet(dst_id);
///
/// // create field, view, record manager
/// let field_manager = datasheet.fields();
/// let view_manager = datasheet.views();
/// let record_manager = datasheet.records();
/// ```
#[derive(Debug, Clone)]
pub struct ApiTable {
  client: Arc<ApiTableClient>,
}

impl ApiTable {
  pub fn new(config: ApiTableConfig) -> Self {
    Self { client: Arc::new(ApiTableClient::new(config)) }
  }

  pub fn datasheet(&self, dst_id: String) -> Datasheet {
    Datasheet::new(self.client.clone(), dst_id)
  }
}


/// ApiTable Client Config
#[derive(Debug, Clone)]
pub struct ApiTableConfig {
  pub host: String,
  pub token: String,
  pub api_version: i32,
}

impl ApiTableConfig {
  pub fn new(host: String, token: String) -> ApiTableConfig {
    ApiTableConfig {
      host,
      token,
      api_version: 1,
    }
  }

  pub fn new_with_api_version(host: String, token: String, api_version: i32) -> ApiTableConfig {
    ApiTableConfig {
      host,
      token,
      api_version,
    }
  }
}


#[cfg(test)]
mod tests_apitable {
  use crate::apitable::{ApiTable, ApiTableConfig};

  #[test]
  fn test_apitable() {
    let host = "https://DOMAIN.xyz".to_string();
    let token = "ACCESS_TOKEN".to_string();
    let dst_id = "DATASHEET_ID".to_string();

    let config = ApiTableConfig::new(host.clone(), token.clone());
    let apitable = ApiTable::new(config);

    let datasheet = apitable.datasheet(dst_id.clone());

    let config = apitable.client.config.clone();
    assert_eq!(host.clone(), config.clone().host);
    assert_eq!(token.clone(), config.clone().token);
    assert_eq!(1, config.clone().api_version);
    assert_eq!(dst_id.clone(), datasheet.config.dst_id);
  }
}
