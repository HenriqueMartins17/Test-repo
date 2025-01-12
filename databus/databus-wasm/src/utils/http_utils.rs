use std::collections::HashMap;
use wasm_bindgen::prelude::*;
use wasm_bindgen_futures::JsFuture;
use web_sys::{Request, RequestInit, RequestMode, Response};

pub async fn http_request(
  method: &str,
  url: String,
  is_json: bool,
  custom_headers: Option<HashMap<&str, &str>>,
) -> JsValue {
  let mut opts = RequestInit::new();
  opts.method(method);
  opts.mode(RequestMode::Cors);

  let request = Request::new_with_str_and_init(&url, &opts).unwrap();

  // Get the headers from the request object
  let headers = request.headers();
  if custom_headers.is_some() {
    for (key, value) in custom_headers.unwrap().iter() {
      headers.set(key, value).unwrap();
    }
  }

  let window = web_sys::window().unwrap();
  let resp_value = JsFuture::from(window.fetch_with_request(&request)).await.unwrap();
  let resp: Response = resp_value.dyn_into().unwrap();

  // Convert this other `Promise` into a rust `Future`.
  let json_r = if is_json {
    JsFuture::from(resp.json().unwrap()).await.unwrap()
  } else {
    JsFuture::from(resp.text().unwrap()).await.unwrap()
  };
  json_r
}

pub async fn get_json(url: String) -> JsValue {
  return http_request("GET".as_ref(), url, true, None).await;
}

pub async fn get_text(url: String) -> JsValue {
  return http_request("GET".as_ref(), url, false, None).await;
}

pub async fn get_json_with_header(url: String, headers: Option<HashMap<&str, &str>>) -> JsValue {
  return http_request("GET".as_ref(), url, true, headers).await;
}

pub async fn get_text_wither_header(url: String, headers: Option<HashMap<&str, &str>>) -> JsValue {
  return http_request("GET".as_ref(), url, false, headers).await;
}
