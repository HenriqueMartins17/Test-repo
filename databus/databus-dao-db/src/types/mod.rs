

pub use node::*;
pub use request::*;

#[allow(unused)]
#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum IdPrefix {
  Datasheet,
  View,
  Record,
  Field,
  Option,
  Condition,
  /// uploaded attachments
  File,
  Comment,
  WidgetPanel,
  Editor,
  Space,
  DateTimeAlarm,
  EmbedLink,
}

impl IdPrefix {
  pub fn as_str(&self) -> &'static str {
    match self {
      Self::Datasheet => "dst",
      Self::View => "viw",
      Self::Record => "rec",
      Self::Field => "fld",
      Self::Option => "opt",
      Self::Condition => "cdt",
      Self::File => "atc", // uploaded attachments
      Self::Comment => "cmt",
      Self::WidgetPanel => "wpl",
      Self::Editor => "edt",
      Self::Space => "spc",
      Self::DateTimeAlarm => "dta",
      Self::EmbedLink => "emb",
    }
  }
}

mod request;
mod node;
