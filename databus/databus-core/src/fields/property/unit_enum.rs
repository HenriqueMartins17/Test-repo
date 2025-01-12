use serde::{Deserialize, Serialize};
use serde_repr::{Deserialize_repr, Serialize_repr};
use strum_macros::{EnumString, FromRepr};

#[derive(Debug, Serialize_repr, Deserialize_repr, PartialEq, Eq, Clone, FromRepr)]
#[repr(u8)]
pub enum UnitTypeEnum {
  Team = 1,
  Role = 2,
  Member = 3,
}

#[derive(Debug, Serialize, Deserialize, PartialEq, Eq, Clone, EnumString)]
pub enum UnitTypeTextEnum {
  Team,
  Member,
}
