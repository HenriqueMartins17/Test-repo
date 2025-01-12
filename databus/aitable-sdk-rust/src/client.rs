use std::future::Future;
use std::pin::Pin;
use std::sync::Arc;

use reqwest_wasm::{Client, Method, RequestBuilder};
use serde::{de, Serialize};

use databus_shared::http::HttpSuccessResponse;

use crate::apitable::ApiTableConfig;

/// ApiTable Client
#[derive(Debug, Clone)]
pub struct ApiTableClient {
  pub config: Arc<ApiTableConfig>,
  pub client: Client,
}

impl ApiTableClient {
  pub fn new(config: ApiTableConfig) -> Self {
    Self { config: Arc::new(config), client: Client::new() }
  }

  /// create GET request
  pub fn get(&self, url: String) -> ApiTableClientBuilder {
    self.create_request(Method::GET, url)
  }

  /// create POST request
  pub fn post(&self, url: String) -> ApiTableClientBuilder {
    self.create_request(Method::POST, url)
  }

  /// create PATCH request
  pub fn patch(&self, url: String) -> ApiTableClientBuilder {
    self.create_request(Method::PATCH, url)
  }

  /// create PUT request
  pub fn put(&self, url: String) -> ApiTableClientBuilder {
    self.create_request(Method::PUT, url)
  }

  /// create DELETE request
  pub fn delete(&self, url: String) -> ApiTableClientBuilder {
    self.create_request(Method::DELETE, url)
  }

  /// create request
  fn create_request(&self, method: Method, url: String) -> ApiTableClientBuilder {
    // combine url
    let url = format!("{}/fusion/v{}{}", self.config.host, self.config.api_version, url);

    // add auth header
    let builder = self.client.request(method, url)
        .header("Authorization", format!("Bearer {}", self.config.token))
        .header("Content-Type", "application/json; charset=utf-8");

    ApiTableClientBuilder::new(builder)
  }
}


/// ApiTable Client Builder
#[derive(Debug)]
pub struct ApiTableClientBuilder {
  builder: Option<RequestBuilder>,
}

impl ApiTableClientBuilder {
  pub fn new(builder: RequestBuilder) -> Self {
    Self { builder: Some(builder) }
  }

  /// add query params
  pub fn query<T: Serialize + ?Sized>(mut self, query: &T) -> Self {
    let builder = self.builder.take().expect("Builder was already consumed");

    let builder = builder.query(query);

    self.builder = Some(builder);
    self
  }

  pub fn body<T: Serialize>(mut self, body: &T) -> Self {
    let builder = self.builder.take().expect("Builder was already consumed");

    let body = serde_json::to_string(body).unwrap();
    let builder = builder.body(body);

    self.builder = Some(builder);
    self
  }

  /// send request
  pub fn send<T>(
    &mut self
  ) -> Pin<Box<dyn Future<Output=anyhow::Result<HttpSuccessResponse<T>>> + '_>>
    where
        T: for<'de> de::Deserialize<'de>,
  {
    let builder = self.builder.take().expect("Builder was already consumed");

    Box::pin(async {
      let response = builder.send().await?;

      // check status code
      let status_code = response.status();
      if !status_code.is_success() {
        return match &response.text().await {
          // TODO: parse error message
          Ok(text) => {
            Err(anyhow::anyhow!("Request failed: {}, {}", status_code, text))
          }
          Err(err) => {
            Err(anyhow::anyhow!("Request failed: {}, {}", status_code, err))
          }
        };
      }

      let text = response.text().await?;
      let data = serde_json::from_str(&text)?;
      Ok(data)
    })
  }
}
