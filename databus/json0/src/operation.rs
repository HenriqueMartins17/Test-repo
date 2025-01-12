use serde::{Deserialize, Serialize};
use std::cmp::Ordering;
use serde_json::Value;

#[derive(Serialize, Deserialize, Debug, Clone, PartialEq, Eq)]
#[serde(untagged)]
pub enum PathSegment {
  String(String),
  Number(usize),
}

impl PathSegment {
  pub fn as_str(&self) ->&str {
    match self {
      Self::String(s) => s,
      _ => "as_str",
    }
  }

  pub fn to_string(&self) -> String {
    match self {
      Self::String(s) => s.clone(),
      _ => "to_string".to_string(),
    }
  }

  pub fn as_i32(&self) -> i32 {
    match self {
      Self::Number(i) => *i as i32,
      _ => 0,
    }
  }
}

#[derive(Serialize, Deserialize, Debug, Clone, PartialEq)]
pub struct Operation {
  pub p: Vec<PathSegment>, // path
  #[serde(flatten)]
  pub kind: OperationKind,
}

// The following OperationKind must be in order, for example, ListReplace must be before ListInsert and ListDelete.
#[derive(Serialize, Deserialize, Debug, Clone, PartialEq)]
#[serde(untagged)]
pub enum OperationKind {
  // adds x to the number at [path].
  // {p:[path], na:x}
  #[serde(rename = "na")]
  NumberAdd {
    na: f64, // x
  },
  // replaces the object before at the index idx in the list at [path] with the object after.
  // {p:[path,idx], ld:before, li:after}
  ListReplace {
    #[serde(rename = "ld")]
    ld: Value,
    #[serde(rename = "li")]
    li: Value,
  },
  // inserts the object obj before the item at idx in the list at [path].
  // {p:[path,idx], li:obj}
  #[serde(rename = "li")]
  ListInsert {
    li: Value, // obj
  },
  // deletes the object obj from the index idx in the list at [path].
  // {p:[path,idx], ld:obj}
  #[serde(rename = "ld")]
  ListDelete {
    ld: Value, // obj
  },
  // moves the object at idx1 in the list at [path] to idx2.
  // {p:[path,idx1], lm:idx2}
  #[serde(rename = "lm")]
  ListMove {
    lm: usize, // idx2
  },
  // replaces the object before at the index idx in the list at [path] with the object after.
  // {p:[path,idx], ld:before, li:after}
  ObjectReplace {
    #[serde(rename = "od")]
    od: Value, // before
    #[serde(rename = "oi")]
    oi: Value, // after
  },
  // inserts the object obj into the object at [path] with key key.
  // {p:[path,key], oi:obj}
  #[serde(rename = "oi")]
  ObjectInsert {
    oi: Value, //obj
  },
  // deletes the object obj with key key from the object at [path].
  // {p:[path,key], od:obj}
  #[serde(rename = "od")]
  ObjectDelete {
    od: Value, //obj
  },
  // applies the subtype op o of type t to the object at [path]
  // {p:[path], t:subtype, o:subtypeOp}
  SubtypeOperation {
    t: String,         // subtype
    o: Vec<Operation>, // subtypeOp
  },
  // inserts the string s at offset offset into the string at [path] (uses subtypes internally).
  // {p:[path,offset], si:s}
  #[serde(rename = "si")]
  StringInsert {
    si: String, // s
  },
  // deletes the string s at offset offset from the string at [path] (uses subtypes internally).
  // {p:[path,offset], sd:s}
  #[serde(rename = "sd")]
  StringDelete {
    sd: String, // s
  },
}

impl PathSegment {
  pub fn unwrap_string(self) -> String {
    match self {
      Self::String(s) => s,
      _ => panic!("expect string PathSegment"),
    }
  }

  pub fn unwrap_number(self) -> usize {
    match self {
      Self::Number(i) => i,
      _ => panic!("expect number PathSegment"),
    }
  }

  pub fn index<'a>(&self, value: &'a Value) -> Option<&'a Value> {
    match self {
      Self::Number(i) => value.get(*i),
      Self::String(s) => value.get(s),
    }
  }

  pub fn index_mut<'a>(&self, value: &'a mut Value) -> Option<&'a mut Value> {
    match self {
      Self::Number(i) => value.get_mut(*i),
      Self::String(s) => value.get_mut(s),
    }
  }

  pub(crate) fn increment(&mut self, inc: isize) {
    match self {
      Self::Number(i) => *i = i.wrapping_add_signed(inc),
      Self::String(_) => {}
    }
  }
}

impl From<usize> for PathSegment {
  fn from(value: usize) -> Self {
    Self::Number(value)
  }
}

impl From<&str> for PathSegment {
  fn from(value: &str) -> Self {
    Self::String(value.into())
  }
}

impl From<String> for PathSegment {
  fn from(value: String) -> Self {
    Self::String(value)
  }
}

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum TransformSide {
  Left,
  Right,
}

impl PartialOrd<Self> for PathSegment {
  fn partial_cmp(&self, other: &Self) -> Option<Ordering> {
    match (self, other) {
      (Self::Number(a), Self::Number(b)) => a.partial_cmp(b),
      (Self::String(a), Self::String(b)) => a.partial_cmp(b),
      _ => None,
    }
  }
}

impl PartialOrd<usize> for PathSegment {
  fn partial_cmp(&self, other: &usize) -> Option<Ordering> {
    match self {
      Self::Number(a) => a.partial_cmp(other),
      _ => None,
    }
  }
}

impl PartialEq<usize> for PathSegment {
  fn eq(&self, other: &usize) -> bool {
    match self {
      Self::Number(a) => a == other,
      _ => false,
    }
  }
}
