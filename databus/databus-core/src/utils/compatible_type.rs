use std::fmt::Display;
use std::ops::Add;

/// JavaScript string
pub struct JsString {
  value: Vec<u16>,
}

impl From<&str> for JsString {
  fn from(value: &str) -> Self {
    Self {
      value: value.encode_utf16().collect(),
    }
  }
}

impl From<String> for JsString {
  fn from(value: String) -> Self {
    Self {
      value: value.encode_utf16().collect(),
    }
  }
}

impl JsString {
  pub fn len(&self) -> usize {
    self.value.len()
  }

  pub fn index_of(&self, substring: &JsString, start_index: usize) -> i64 {
    self.value[start_index..]
      .windows(substring.len())
      .position(|window| window == substring.value.as_slice())
      .map(|pos| (pos + start_index) as i64)
      .unwrap_or(-1)
  }

  pub fn last_index_of(&self, substring: &JsString, start_index: usize) -> i64 {
    self.value[..=start_index]
      .windows(substring.len())
      .rposition(|window| window == substring.value.as_slice())
      .map(|pos| pos as i64)
      .unwrap_or(-1)
  }

  pub fn slice(&self, start_index: usize, end_index: usize) -> JsString {
    let end_index = end_index.min(self.len());
    if start_index > end_index {
      return JsString::from("");
    }
    Self {
      value: self.value[start_index..end_index].to_vec(),
    }
  }

  pub fn sub_string(&self, start_index: usize, end_index: Option<usize>) -> JsString {
    let end_index = end_index.unwrap_or(self.len());
    self.slice(start_index, end_index)
  }
}

impl Add for JsString {
  type Output = Self;

  fn add(self, rhs: Self) -> Self::Output {
    let mut value = self.value;
    value.extend(rhs.value);
    Self { value }
  }
}

impl Display for JsString {
  fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
    write!(f, "{}", String::from_utf16_lossy(&self.value))
  }
}

#[cfg(test)]
mod tests {
  use super::*;

  #[test]
  fn test_emoji() {
    // The length of the emoji "🤯" is 1, but the length of the UTF-16 encoding is 2.
    // For now, we are using `encode_utf16()` to make the index consistent with `s.length` in JavaScript.
    assert_eq!(1, "🤯".chars().count());
    assert_eq!(2, "🤯".encode_utf16().count());
  }

  #[test]
  fn test_len() {
    let s = JsString::from("左边🤯🤯right");
    assert_eq!(s.len(), 11);

    let s = JsString::from("left🤯🤯右边");
    assert_eq!(s.len(), 10);
  }

  #[test]
  fn test_index_of() {
    let s = JsString::from("左边🤯🤯right");
    assert_eq!(s.index_of(&JsString::from("边"), 0), 1);
    assert_eq!(s.index_of(&JsString::from("🤯"), 0), 2);
    assert_eq!(s.index_of(&JsString::from("right"), 0), 6);
    assert_eq!(s.index_of(&JsString::from("不存在"), 0), -1);

    let s = JsString::from("left🤯🤯右边");
    assert_eq!(s.index_of(&JsString::from("🤯"), 0), 4);
    assert_eq!(s.index_of(&JsString::from("右"), 0), 8);
    assert_eq!(s.index_of(&JsString::from("left"), 0), 0);
    assert_eq!(s.index_of(&JsString::from("不存在"), 0), -1);
  }

  #[test]
  fn test_last_index_of() {
    let s = JsString::from("左边🤯🤯right");
    assert_eq!(s.last_index_of(&JsString::from("边"), s.len() - 1), 1);
    assert_eq!(s.last_index_of(&JsString::from("🤯"), s.len() - 1), 4);
    assert_eq!(s.last_index_of(&JsString::from("r"), s.len() - 1), 6);

    let s = JsString::from("left🤯🤯右边");
    assert_eq!(s.last_index_of(&JsString::from("🤯"), s.len() - 1), 6);
    assert_eq!(s.last_index_of(&JsString::from("边"), s.len() - 1), 9);
    assert_eq!(s.last_index_of(&JsString::from("l"), s.len() - 1), 0);
  }

  #[test]
  fn test_slice() {
    let s = JsString::from("左边🤯🤯right");
    assert_eq!(s.slice(0, 2).to_string(), "左边");
    assert_eq!(s.slice(2, 4).to_string(), "🤯");
    assert_eq!(s.slice(4, 6).to_string(), "🤯");
    assert_eq!(s.slice(6, 11).to_string(), "right");

    let s = JsString::from("left🤯🤯右边");
    assert_eq!(s.slice(0, 4).to_string(), "left");
    assert_eq!(s.slice(4, 6).to_string(), "🤯");
    assert_eq!(s.slice(6, 8).to_string(), "🤯");
    assert_eq!(s.slice(8, 10).to_string(), "右边");

    assert_eq!(s.slice(0, 100).to_string(), "left🤯🤯右边");
    assert_eq!(s.slice(0, 0).to_string(), "");
    assert_eq!(s.slice(100, 0).to_string(), "");
    assert_eq!(s.slice(100, 100).to_string(), "");
  }

  #[test]
  fn test_display() {
    let s = "中文🤯🤯english";
    assert_eq!(format!("{}", JsString::from(s)), s);
  }
}
