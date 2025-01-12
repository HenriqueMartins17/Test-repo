use url::Url;

use databus_shared::env_var;

use crate::so::AttachmentValue;

pub struct ImageSrcOption {
  pub format_to_jpg: bool,
  pub is_preview: bool,
}

pub fn get_image_url(attachment_value: AttachmentValue, _options: Option<ImageSrcOption>) -> String {
  let bucket = attachment_value.bucket;
  let token = attachment_value.token;
  let _preview_token = attachment_value.preview;
  let _mime_type = attachment_value.mime_type;
  let _name = attachment_value.name;

  if bucket.is_none() {
    return String::from("");
  }

  let host = get_host_of_attachment(bucket);
  if host.is_none() {
    return String::from("");
  }
  let base_url = Url::parse(&*host.unwrap());
  match base_url {
    Ok(base_url) => {
      let origin_src = base_url.join(&*token).unwrap();
      return String::from(origin_src.as_str());
    }
    Err(_) => {
      return String::from("");
    }
  }
}

fn get_host_of_attachment(_bucket: Option<String>) -> Option<String> {
  return match env_var!(OSS_HOST) {
    None => {Some("https://s1.vika.cn".to_string())}
    Some(_) => {env_var!(OSS_HOST) }
  }
}
