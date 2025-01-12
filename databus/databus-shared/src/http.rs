// mod loader;
// pub use loader::*;

// use crate::prelude::{HashMap, Json};
pub use crate::prelude::JsonExt;
use serde::{Deserialize, Serialize};

#[derive(Serialize, Debug, Clone)]
pub struct HttpResponse<T> {
  pub success: bool,
  #[serde(with = "http_serde::status_code")]
  pub code: http::StatusCode,
  pub message: StatusMessage,
  pub data: T,
}

#[allow(unused)]
#[derive(Serialize, Debug, Clone)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum StatusMessage {
  Success,
  ServerError,
}

/**
 * Http Response struct for vika and apitable
 */
#[derive(Deserialize, Debug, Clone)]
pub struct HttpSuccessResponse<T> {
  pub success: bool,
  pub code: i32,
  #[serde(default)]
  pub message: String,
  pub data: T,
}
