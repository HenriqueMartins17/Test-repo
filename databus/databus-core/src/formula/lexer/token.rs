// TODO: use usize is better
pub type TokenIndex = isize;

#[derive(Debug, PartialEq, Clone, Eq, Hash)]
pub enum TokenType {
  Call,
  PureValue,
  Value,
  And,
  Comma,
  Number,
  String,
  Less,
  LessEqual,
  Greater,
  GreaterEqual,
  Equal,
  NotEqual,
  Add,
  Minus,
  Times,
  Mod,
  Div,
  Concat,
  Or,
  Not,
  LeftParen,
  RightParen,
  Blank,
  Unknown,
}

#[derive(Debug, PartialEq, Clone)]
pub struct Token {
  pub token_type: TokenType,
  pub index: TokenIndex,
  pub value: String,
}

impl Token {
  pub fn new(token_type: TokenType, index: TokenIndex, value: String) -> Self {
    Self {
      token_type,
      index,
      value,
    }
  }
}
