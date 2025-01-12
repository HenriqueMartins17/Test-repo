use databus_core::prelude::DatasheetPackSO;
use databus_shared::prelude::HttpSuccessResponse;

use crate::client::RequestClient;


/**
 * API Client for WebAssembly
 */
pub struct ApiDAO {
  base_url: String,
  client: RequestClient,
}

impl ApiDAO {
  pub fn new(base_url: &str) -> ApiDAO {
    let client = RequestClient::new(base_url);
    ApiDAO {
      base_url: base_url.to_string(),
      client: client,
    }
  }


  /**
   * the whole datasheetPack Response Body
   */
  pub async fn fetch_datasheet_pack(
    &self,
    datasheet_id: &str,
    _user_id: Option<String>,
    _space_id: Option<String>,
  ) -> anyhow::Result<HttpSuccessResponse<DatasheetPackSO>> {
    let url_datapack = format!(
      "/datasheets/{}/dataPack",
      // "/databus/get_datasheet_pack/{}",
      datasheet_id,
    );
    let datasheet_pack: HttpSuccessResponse<DatasheetPackSO> = self.client.get(&url_datapack).await?;
    Ok(datasheet_pack)
  }


  pub async fn fetch_datasheet_pack_str(
    &self,
    datasheet_id: &str,
    _user_id: Option<String>,
    _space_id: Option<String>,
  ) -> anyhow::Result<String> {
    let url_datapack = format!(
      "/datasheets/{}/dataPack",
      // "/databus/get_datasheet_pack/{}",
      datasheet_id,
    );
    let datasheet_pack: String = self.client.get_string(&url_datapack).await?;
    Ok(datasheet_pack)
  }

  /**
   * the whole datasheetPack revision
   */
  pub async fn get_datasheet_revision(
    &self,
    datasheet_id: &str,
  ) -> anyhow::Result<HttpSuccessResponse<i32>> {
    let url_datapack = format!(
      "/datasheets/{}/revision",
      // "/databus/dao/get_revision/{}",
      datasheet_id,
    );
    let datasheet_pack: HttpSuccessResponse<i32> = self.client.get(&url_datapack).await?;
    Ok(datasheet_pack)
  }
}

#[cfg(test)]
mod tests {

  use crate::api_mock::MOCK_DATASHEET_PACK_JSON;
  #[test]
  fn test_api() {
    let mut server = mockito::Server::new();
    // Use one of these addresses to configure your client
    let url = server.url();
    println!("url: {}", url);
    // Create a mock
    let mock = server
      .mock("GET", "/datasheets/mock/dataPack")
      .with_status(200)
      .with_header("content-type", "text/plain")
      .with_header("x-api-key", "1234")
      .with_body(MOCK_DATASHEET_PACK_JSON.to_string())
      .create();

    let api_dao = super::ApiDAO::new(&url);
    let api_response =
      tokio_test::block_on(api_dao.fetch_datasheet_pack("mock", Some("1234".to_string()), Some("1234".to_string())))
        .unwrap();

    assert_eq!(api_response.code, 200);
    assert_eq!(api_response.success, true);
    assert_eq!(
      api_response.data.snapshot.meta.views[0].id,
      Some("viwYLQzUcTSLL".to_string())
    );

    mock.assert();
  }
}
