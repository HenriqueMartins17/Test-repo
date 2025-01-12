use serde::{Deserialize, Serialize};
use utoipa::ToSchema;

#[derive(Debug, PartialEq, Eq, Clone, Serialize, Deserialize, ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct ISortedField {
  #[serde(default)]
  pub field_id: String,
  pub desc: bool,
}

// pub type IGroupInfo = Vec<ISortedField>;

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct ISortInfo {
  pub rules: Vec<ISortedField>,
  #[serde(default)]
  pub keep_sort: bool,
}

#[cfg(test)]
mod test {
  use super::ISortInfo;

  const DATA: &str = r#"{
        "rules": [
            {
                "desc": false
            }
        ]
    }"#;

  #[test]
  fn test() {
    let info: ISortInfo = serde_json::from_str(DATA).unwrap();
    println!("{:?}", info);
  }
}
