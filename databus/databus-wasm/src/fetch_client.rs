// use wasm_bindgen::prelude::*;
// use wasm_bindgen_futures::JsFuture;
// use web_sys::{Request, RequestInit, RequestMode, Response};

// pub struct WebFecthClient {
//   base_url: String,
// }

// impl WebFecthClient {
//   pub fn new(base_url: &str) -> WebFecthClient {
//     WebFecthClient {
//       base_url: base_url.to_string(),
//     }
//   }
//   pub async fn get(&self, sub_path: String) -> Result<JsValue, JsValue> {
//     let mut opts = RequestInit::new();
//     opts.method("GET");
//     opts.mode(RequestMode::Cors);

//     let url = format!("{}{}", self.base_url, sub_path);

//     let request = Request::new_with_str_and_init(&url, &opts)?;

//     request.headers().set("Accept", "application/vnd.github.v3+json")?;

//     let window = web_sys::window().unwrap();
//     let resp_value = JsFuture::from(window.fetch_with_request(&request)).await?;

//     // `resp_value` is a `Response` object.
//     assert!(resp_value.is_instance_of::<Response>());
//     let resp: Response = resp_value.dyn_into().unwrap();

//     // Convert this other `Promise` into a rust `Future`.
//     let json = JsFuture::from(resp.json()?).await?;

//     // Send the JSON response back to JS.
//     Ok(json)
//   }
// }
