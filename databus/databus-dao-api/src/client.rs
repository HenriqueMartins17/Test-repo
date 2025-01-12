use reqwest_wasm::Client;

pub struct RequestClient {
  base_url: String,
  client: Client,
}

impl RequestClient {
  pub fn new(base_url: &str) -> RequestClient {
    RequestClient {
      base_url: base_url.to_string(),
      client: Client::new(),
    }
  }
  pub async fn get<T>(&self, sub_path: &str) -> anyhow::Result<T>
  where
    T: serde::de::DeserializeOwned,
  {
    let url = format!("{}{}", self.base_url, sub_path);

    let response: reqwest_wasm::Response = self.client.get(url).send().await?;

    let response_text: String = response.text().await?;
    let response_data: T = serde_json::from_str(&response_text)?;

    Ok(response_data)
  }
  pub async fn get_string(&self, sub_path: &str) -> anyhow::Result<String> {
    let url = format!("{}{}", self.base_url, sub_path);

    let response: reqwest_wasm::Response = self.client.get(url).send().await?;

    let response_text: String = response.text().await?;
    Ok(response_text)
  }
}

#[cfg(test)]
#[cfg(target_os = "macos")]
mod tests {
  use serde::{Deserialize, Serialize};
  use serde_json::Value;

  use crate::client::RequestClient;

  #[test]
  fn test_client() {
    let mut server = mockito::Server::new();
    // Use one of these addresses to configure your client
    let url = server.url();

    // Create a mock
    let mock = server
      .mock("GET", "/hello")
      .with_status(201)
      .with_header("content-type", "text/plain")
      .with_header("x-api-key", "1234")
      .with_body(r#"{"status":"ok"}"#)
      .create();

    let client = RequestClient::new(&url);
    let res: Value = tokio_test::block_on(client.get::<Value>("/hello")).unwrap();
    assert_eq!(res.get("status").unwrap(), "ok");

    #[derive(Deserialize, Serialize)]
    #[serde(rename_all = "camelCase")]
    struct Tmp {
      status: String,
    }
    let res2: Tmp = tokio_test::block_on(client.get::<Tmp>("/hello")).unwrap();

    assert_eq!(res2.status, "ok");

    mock.expect(2).assert();
  }
}
