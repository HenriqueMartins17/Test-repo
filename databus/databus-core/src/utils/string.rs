use regex::{Captures, Regex};

pub fn date_str_replace_cn(s: &str) -> String {
  if s.is_empty() {
    return s.to_string();
  }

  let re = Regex::new(r"(\s?[年月]\s?)|(\s?[时分]\s?)|([日秒])").unwrap();
  return re
    .replace_all(s, |caps: &Captures| {
      if let Some(_) = caps.get(1) {
        return "/".to_string();
      }
      if let Some(_) = caps.get(2) {
        return ":".to_string();
      }
      if let Some(_) = caps.get(3) {
        return "".to_string();
      }
      return match caps.get(0) {
        Some(p0) => p0.as_str().to_string(),
        None => "".to_string(),
      };
    })
    .to_string();
}
