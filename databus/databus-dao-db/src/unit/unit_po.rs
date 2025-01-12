use databus_core::prelude::UnitSO;
use mysql_async::prelude::*;
use serde::{Deserialize, Serialize, Serializer};

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Eq, FromRow)]
#[serde(rename_all = "camelCase")]
pub struct UnitPO {
  #[serde(serialize_with = "serialize_unit_id")]
  pub unit_id: Option<i64>,
  #[mysql(rename = "type")]
  pub r#type: Option<u8>,
  pub name: Option<String>,
  pub uuid: Option<String>,
  pub user_id: Option<String>,
  pub avatar: Option<String>,
  pub is_active: Option<u8>,
  pub is_deleted: Option<u8>,
  pub nick_name: Option<String>,
  pub avatar_color: Option<i32>,

  #[serde(skip_serializing_if = "Option::is_none")]
  pub is_member_name_modified: Option<bool>,

  #[serde(skip_serializing_if = "Option::is_none")]
  pub is_nick_name_modified: Option<bool>,

  pub original_unit_id: Option<String>,
}

fn serialize_unit_id<S>(unit_id: &Option<i64>, serializer: S) -> Result<S::Ok, S::Error>
where
  S: Serializer,
{
  if let Some(unit_id) = unit_id {
    serializer.serialize_str(&format!("{unit_id}"))
  } else {
    serializer.serialize_none()
  }
}

impl UnitPO {
  pub fn to_vo(&self) -> UnitSO {
    return UnitSO {
      unit_id: self.unit_id.map(|i| i.to_string()),
      r#type: self.r#type,
      name: self.name.clone(),
      uuid: self.uuid.clone(),
      user_id: self.user_id.clone(),
      avatar: self.avatar.clone(),
      is_active: self.is_active,
      is_deleted: self.is_deleted,
      nick_name: self.nick_name.clone(),
      avatar_color: self.avatar_color,
      is_member_name_modified: self.is_member_name_modified,
      is_nick_name_modified: self.is_nick_name_modified,
      original_unit_id: self.original_unit_id.clone(),
    };
  }
}
