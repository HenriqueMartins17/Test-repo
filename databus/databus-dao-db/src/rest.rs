use std::sync::Arc;

use anyhow::{anyhow, Context};
use async_trait::async_trait;
use databus_core::dtos::fusion_api_dtos::IAssetDTO;
use databus_core::so::InternalSpaceSubscriptionView;
use databus_core::so::InternalSpaceUsageView;
use futures::future::BoxFuture;
use futures::TryFutureExt;
use serde::de::DeserializeOwned;
use serde_json::json;
use serde_json::Value;
use surf::http::Method;
use surf::Url;

use databus_core::shared::{AuthHeader, IUserInfo};
use databus_shared::prelude::{RestError, ServerError};
use databus_shared::prelude::HttpSuccessResponse;
use databus_shared::prelude::Json;
use databus_shared::prelude::JsonExt;

use crate::types::NodePermission;

pub struct HttpClient(surf::Client);

#[async_trait]
pub trait RestDAO: Send + Sync {
  async fn get_node_permission(
    &self,
    auth: &AuthHeader,
    node_id: &str,
    share_id: Option<&str>,
  ) -> anyhow::Result<NodePermission>;

  async fn get_field_permission(
    &self,
    auth: &AuthHeader,
    node_id: &str,
    share_id: Option<&str>,
  ) -> anyhow::Result<Json>;

  async fn get_node_contains_status(&self, folder_id: &str, node_id: &str) -> anyhow::Result<bool>;

  async fn has_login(&self, cookie: &str) -> anyhow::Result<bool>;

  async fn execute_command_with_update_records(&self, dst_id: &str, user_id: &str, json_value: Value) -> anyhow::Result<String>;

  async fn update_space_statistics(&self, space_id: &str, json_value: Value) -> anyhow::Result<()> ;

  async fn capacity_over_limit(
    &self,
    auth: &AuthHeader,
    space_id: &str,
  ) -> anyhow::Result<bool>;

  async fn get_user_info_by_space_id(
    &self,
    auth: &AuthHeader,
    space_id: &str,
  ) -> anyhow::Result<IUserInfo>;

  async fn get_asset_info(&self, token: &str) -> anyhow::Result<IAssetDTO>;

  async fn get_space_usage(&self, space_id: &str) -> anyhow::Result<InternalSpaceUsageView>;

  async fn get_space_subscription(&self, space_id: &str) -> anyhow::Result<InternalSpaceSubscriptionView>;
}

struct RestDAOImpl {
  http_client: HttpClient,
}

pub fn new_dao(base_url: String) -> Arc<dyn RestDAO> {
  Arc::new(RestDAOImpl {
    http_client: HttpClient::new(base_url),
  })
}

#[async_trait]
impl RestDAO for RestDAOImpl {
  async fn get_node_permission(
    &self,
    auth: &AuthHeader,
    node_id: &str,
    share_id: Option<&str>,
  ) -> anyhow::Result<NodePermission> {
    if auth.user_id.is_none() {
      return Ok(NodePermission {
        has_role: true,
        permissions: Some(json!({
          "readable": true
        })),
        ..NodePermission::default()
      });
    }
    let url = format!(
      "internal/node/{node_id}/permission?spaceId={space_id}&userId={user_id}",
      node_id = node_id,
      space_id = auth.space_id.as_ref().unwrap(),
      user_id = auth.user_id.as_ref().unwrap()
    );
    self
      .http_client
      .get(url, auth, share_id.map(|share_id| json!({ "shareId": share_id })))
      .await
      .map(|resp| resp.data)
  }

  async fn get_field_permission(
    &self,
    auth: &AuthHeader,
    node_id: &str,
    share_id: Option<&str>,
  ) -> anyhow::Result<Json> {
    if auth.user_id.is_none() {
      println!("have no permission");
      return Ok(json!({
        "readable": true
      }));
    }
    let url = format!(
      "internal/node/{node_id}/field/permission?spaceId={space_id}&userId={user_id}",
      node_id = node_id,
      space_id = auth.space_id.as_ref().unwrap(),
      user_id = auth.user_id.as_ref().unwrap()
    );

    self
      .http_client
      .get(
        url,
        auth,
        Some(json!({
          "shareId": share_id,
          "spaceId": auth.space_id.as_ref().unwrap(),
          "userId": auth.user_id.as_ref().unwrap(),
        })),
      )
      .await
      .and_then(|resp| {
        Json::into_prop(resp.data, "fieldPermissionMap").map_err(|data| anyhow!("missing fieldPermissionMap: {data:?}"))
      })
  }

  async fn get_node_contains_status(&self, folder_id: &str, node_id: &str) -> anyhow::Result<bool> {
    let url = format!(
      "internal/folders/{folder_id}/nodes/{node_id}/exists",
      folder_id = folder_id,
      node_id = node_id
    );
    self
        .http_client
        .get(
          url,
          &AuthHeader {
            ..Default::default()
          },
          None,
        )
        .await
        .map(|resp| resp.data)
  }

  async fn has_login(&self, cookie: &str) -> anyhow::Result<bool> {
    self
      .http_client
      .get(
        "internal/user/session",
        &AuthHeader {
          cookie: Some(cookie.to_owned()),
          ..Default::default()
        },
        None,
      )
      .await
      .map(|resp| resp.data)
  }

  async fn execute_command_with_update_records(&self, dst_id: &str, user_id: &str, json_value: Value) -> anyhow::Result<String> {
    let url = format!(
      "http://localhost:3333/fusion/v1/datasheets/{dst_id}/executeCommandFromRust",
      dst_id = dst_id,
    );
    let auth = &AuthHeader {
      token: Some(user_id.to_string()),
      ..Default::default()
    };
    self.http_client.post_to_execute_command_with_update_records(url, auth, json_value).await
  }

  async fn update_space_statistics(&self, space_id: &str, json_value: Value) -> anyhow::Result<()> {
    let url = format!(
      "http://localhost:8081/api/v1/internal/space/{space_id}/statistics",
      space_id = space_id,
    );
    let auth = AuthHeader::default();
    self.http_client.post_to_back_end(url, auth, json_value).await
  }

  async fn capacity_over_limit(
    &self,
    auth: &AuthHeader,
    space_id: &str,
  ) -> anyhow::Result<bool> {
    let url = format!(
      "internal/space/{space_id}/capacity",
      space_id = space_id
    );

    let res = self
      .http_client
      .get(url, auth, None)
      .await
      .map(|resp: HttpSuccessResponse<Value>| resp.data);

    match res {
      Ok(data) => {
        let data = data.as_object().unwrap();
        let is_allow_over_limit = data.get("isAllowOverLimit").unwrap().as_bool().unwrap();
        if is_allow_over_limit {
          return Ok(false);
        }
        let current_bundle_capacity = data.get("currentBundleCapacity").unwrap().as_i64().unwrap();
        let used_capacity = data.get("usedCapacity").unwrap().as_i64().unwrap();
        Ok(current_bundle_capacity - used_capacity < 0)
      },
      Err(err) => {
        println!("capacity_over_limit: err: {:?}", err);
        Err(err)
      }
    }
  }

  /// Obtain user info of the current user in a given space, including basic info and member info in the space.
  async fn get_user_info_by_space_id(&self, auth: &AuthHeader, space_id: &str) -> anyhow::Result<IUserInfo> {
    let url = format!(
      "user/me?spaceId={space_id}",
      space_id = space_id,
    );
    self
        .http_client
        .get(url, auth, None)
        .await
        .map(|resp| resp.data)
  }

  async fn get_asset_info(&self, token: &str) -> anyhow::Result<IAssetDTO> {
    let url = format!(
      "internal/asset/get"
    );
    let auth = AuthHeader::default();
    let params = json!({
      "token": token,
    });
    self
        .http_client
        .get(url, &auth, Some(params))
        .await
        .map(|resp| resp.data)
  }

  async fn get_space_usage(&self, space_id: &str) -> anyhow::Result<InternalSpaceUsageView> {
    let url = format!(
      "internal/space/{space_id}/usages",
      space_id = space_id,
    );
    self
        .http_client
        .get(url, &AuthHeader::default(), None)
        .await
        .map(|resp| resp.data)
  }

  async fn get_space_subscription(&self, space_id: &str) -> anyhow::Result<InternalSpaceSubscriptionView> {
    let url = format!(
      "internal/space/{space_id}/subscription",
      space_id = space_id,
    );
    self
        .http_client
        .get(url, &AuthHeader::default(), None)
        .await
        .map(|resp| resp.data)
  }
}

fn http_log(
  req: surf::Request,
  client: surf::Client,
  next: surf::middleware::Next,
) -> BoxFuture<surf::Result<surf::Response>> {
  Box::pin(async move {
    let url = req.url().to_string();
    tracing::info!("Remote call address: {url}");
    let result = next.run(req, client).await;
    if let Err(err) = &result {
      tracing::error!("Remote call {url} failed: {err}");
    }
    result
  })
}

impl HttpClient {
  fn new<S>(base_url: S) -> Self
  where
    S: AsRef<str>,
  {
    let client: surf::Client = surf::Config::new()
      .set_base_url(Url::parse(base_url.as_ref()).unwrap())
      .add_header("X-Internal-Request", "yes")
      .unwrap()
      .try_into()
      .unwrap();
    Self(client.with(http_log))
  }

  async fn get<S, T>(&self, url: S, auth: &AuthHeader, query: Option<Json>) -> anyhow::Result<HttpSuccessResponse<T>>
  where
    S: AsRef<str>,
    T: DeserializeOwned,
  {
    self.request(Method::Get, url, auth, query, None).await
  }

  async fn post_to_execute_command_with_update_records(&self, url: String, auth: &AuthHeader, body: Value) -> anyhow::Result<String>
  {
    let user_id = auth.token.as_ref().unwrap();
    let authorization = format!("Bearer {}", user_id);
    let resp = surf::post(url)
      .header("Authorization", authorization)
      .body(body)
      .recv_string().await;
    match resp {
      Ok(resp) => Ok(resp),
      Err(err) => Err(
        RestError {
          status_code: err.status() as u16,
        }
        .into())
    }
  }

  async fn post_to_back_end(&self, url: String, auth: AuthHeader, body: Value) -> anyhow::Result<()> {
    let resp = surf::post(url)
      .body(body)
      .recv_string().await;
    match resp {
      Ok(_resp) => {
        Ok(())
      },
      Err(err) => Err(
        RestError {
          status_code: err.status() as u16,
        }
        .into())
    }
  }
  

  async fn request<S, T>(
    &self,
    method: Method,
    url: S,
    auth: &AuthHeader,
    query: Option<Json>,
    body: Option<Json>,
  ) -> anyhow::Result<HttpSuccessResponse<T>>
  where
    T: DeserializeOwned,
    S: AsRef<str>,
  {
    let url = url.as_ref();
    let mut req_builder = self.0.request(method, url);

    if let Some(cookie) = &auth.cookie {
      req_builder = req_builder.header("Cookie", cookie);
    } else if let Some(token) = &auth.token {
      req_builder = req_builder.header("Authorization", token);
    }

    if let Some(query) = query {
      req_builder = req_builder
        .query(&query)
        .map_err(|err| err.into_inner())
        .with_context(|| format!("add query {query:?} for server request {url}"))?;
    }

    if let Some(body) = body {
      req_builder = req_builder
        .body_json(&body)
        .map_err(|err| err.into_inner())
        .with_context(|| format!("add body for server request {url}"))?;
    }

    let req = req_builder.build();
    let url = req.url().to_string();

    let resp = self
      .0
      .send(req)
      .and_then(move |mut resp| async move { resp.body_json::<HttpSuccessResponse<T>>().await })
      .map_err(|err| err.into_inner())
      .await
      .with_context(|| format!("server request {url}"))?;

    if resp.success {
      return Ok(resp);
    }

    tracing::error!(
      "Remote call {url} failed, error code:[{}], error:[{}]`);",
      resp.code,
      resp.message,
    );

    if let 201 | 403 | 600 | 601 | 602 | 404 | 411 = resp.code {
      return Err(
        RestError {
          status_code: resp.code as u16,
        }
        .into(),
      );
    }

    Err(ServerError.into())
  }
}

#[cfg(test)]
pub mod mock {
  use std::default::Default;

  use anyhow::anyhow;

  use databus_shared::prelude::{HashMapExt, HashSet};

  use super::*;

  #[derive(Default)]
  pub struct MockRestDAOImpl {
    node_permissions: HashMapExt<(&'static str, Option<&'static str>), NodePermission>,
    field_permissions: HashMapExt<(&'static str, Option<&'static str>), Json>,
    logined: HashSet<&'static str>,
  }

  impl MockRestDAOImpl {
    pub fn new() -> Self {
      Self::default()
    }

    pub fn with_node_permissions(
      mut self,
      node_permissions: HashMapExt<(&'static str, Option<&'static str>), NodePermission>,
    ) -> Self {
      self.node_permissions = node_permissions;
      self
    }

    #[allow(unused)]
    pub fn with_field_permissions(
      mut self,
      field_permissions: HashMapExt<(&'static str, Option<&'static str>), Json>,
    ) -> Self {
      self.field_permissions = field_permissions;
      self
    }

    #[allow(unused)]
    pub fn with_logined(mut self, logined: HashSet<&'static str>) -> Self {
      self.logined = logined;
      self
    }

    pub fn build(self) -> Arc<dyn RestDAO> {
      Arc::new(self)
    }
  }

  #[async_trait]
  impl RestDAO for MockRestDAOImpl {
    async fn get_node_permission(
      &self,
      _auth: &AuthHeader,
      node_id: &str,
      share_id: Option<&str>,
    ) -> anyhow::Result<NodePermission> {
      self
        .node_permissions
        .get(&(node_id, share_id))
        .cloned()
        .ok_or_else(|| anyhow!("node permission ({node_id}, {share_id:?}) not exist"))
    }

    async fn get_field_permission(
      &self,
      _auth: &AuthHeader,
      node_id: &str,
      share_id: Option<&str>,
    ) -> anyhow::Result<Json> {
      self
        .field_permissions
        .get(&(node_id, share_id))
        .cloned()
        .ok_or_else(|| anyhow!("field permission ({node_id}, {share_id:?}) not exist"))
    }

    async fn get_node_contains_status(&self, folder_id: &str, node_id: &str) -> anyhow::Result<bool> {
      Ok(true)
    }

    async fn has_login(&self, cookie: &str) -> anyhow::Result<bool> {
      Ok(self.logined.contains(cookie))
    }

    async fn execute_command_with_update_records(&self, _dst_id: &str, _user_id: &str, _json_value: Value) -> anyhow::Result<String> {
      Ok("".to_owned())
    }

    async fn update_space_statistics(&self, _space_id: &str, _json_value: Value) -> anyhow::Result<()> {
      Ok(())
    }

    async fn capacity_over_limit(
      &self,
      _auth: &AuthHeader,
      _space_id: &str,
    ) -> anyhow::Result<bool> {
      Ok(false)
    }

    async fn get_user_info_by_space_id(&self, auth: &AuthHeader, space_id: &str) -> anyhow::Result<IUserInfo> {
      Ok(IUserInfo::default() )
    }

    async fn get_asset_info(&self, _token: &str) -> anyhow::Result<IAssetDTO> {
      Ok(IAssetDTO::default())
    }

    async fn get_space_usage(&self, _space_id: &str) -> anyhow::Result<InternalSpaceUsageView> {
      Ok(InternalSpaceUsageView::default())
    }

    async fn get_space_subscription(&self, _space_id: &str) -> anyhow::Result<InternalSpaceSubscriptionView> {
      Ok(InternalSpaceSubscriptionView::default())
    }
  }
}
