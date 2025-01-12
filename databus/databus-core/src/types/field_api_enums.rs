use strum_macros::FromRepr;

pub enum APIMetaMemberType {
  Team,
  Member,
}

#[derive(FromRepr, Debug, PartialEq)]
#[repr(u8)]
pub enum MemberType {
  Team = 1,
  Role = 2,
  Member = 3,
}
