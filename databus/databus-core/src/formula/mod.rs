pub mod errors;
pub mod functions;
pub mod interpreter;
pub mod lexer;
pub mod parser;
pub mod consts;
pub mod evaluate;
pub mod helper;
pub mod i18n;
pub mod types;

// use lazy_static::lazy_static;
use regex::Regex;

use self::parser::AstNode;

// lazy_static! {
//     static ref EXPR_GRAMMAR: Vec<(TokenType, Regex)> =
// }

#[derive(Debug, PartialEq, Clone)]
enum TokenType {
  Value,
  String,
  Call,
  Number,
  NotEqual,
  And,
  GreaterEqual,
  LessEqual,
  Or,
  Comma,
  Not,
  Add,
  Minus,
  Times,
  Div,
  Mod,
  Concat,
  Greater,
  Less,
  Equal,
  LeftParen,
  RightParen,
  Blank,
  Unknown,
}

#[derive(Debug, PartialEq, Clone)]
struct Token {
  token_type: TokenType,
  index: usize,
  value: String,
}

trait ILexer {
  fn get_next_token(&mut self) -> Option<Token>;
  fn get_prev_token(&mut self) -> Option<Token>;
  fn reset(&mut self);
}

pub struct FormulaExprLexer {
  expression: String,
  full_matches: Vec<Token>,
  matches: Vec<Token>,
  errors: Vec<String>,
  current_index: isize,
}

pub enum AstNodeType {
  BinaryOperatorNode,
  UnaryOperatorNode,
  ValueOperandNode,
  PureValueOperandNode,
  CallOperandNode,
  StringOperandNode,
  NumberOperandNode,
}

// pub trait AstNode {
// pub struct AstNode {
  // fn token(&self) -> &Token;
  // fn name(&self) -> AstNodeType;
  // fn value_type(&self) -> BasicValueType;
  // fn inner_value_type(&self) -> Option<BasicValueType>;
  // fn num_nodes(&self) -> usize {
  //     1
  // }
  // fn to_string(&self) -> String {
  //     format!("AstNode: {}::{}", self.token(), self.name())
  // }
// }

pub struct FormulaExpr {
  pub lexer: FormulaExprLexer,
  // pub ast: dyn AstNode,
  pub ast: AstNode,
}

impl FormulaExprLexer {
  fn new(expression: String) -> Self {
    let full_matches = Self::get_full_matches(&expression);
    let mut errors = vec![];
    let matches = Self::filter_useless_token(full_matches.clone(), &mut errors);
    Self {
      expression,
      full_matches,
      matches,
      errors: errors,
      current_index: -1,
    }
  }

  fn get_expr_grammar() -> Vec<(TokenType, Regex)> {
    vec![
      // The value in the record obtained by the field name constant
      (
        TokenType::Value,
        Regex::new(r"(\{\})|(\{(\\[{}])*[\s\S]*?[^\\]\})").unwrap(),
      ),
      // string literal
      (
        TokenType::String,
        Regex::new(r#"(["”“](([^\\"“”])*?\\["”“])*.*?["”“])|(['‘’](([^\\'‘’])*?\\['‘’])*.*?['‘’])"#).unwrap(),
      ),
      // function name or field name constant
      (
        TokenType::Call,
        Regex::new(r#"[^0-9.+\-|=*/><()（）!&%'"“”‘’^`~,，\s][^+\-|=*/><()（）!&%'"“”‘’^`~,，\s]*"#).unwrap(),
      ),
      // number literal
      (TokenType::Number, Regex::new(r"[0-9.]+").unwrap()),
      // not equal to
      (TokenType::NotEqual, Regex::new(r"!=").unwrap()),
      // and
      (TokenType::And, Regex::new(r"&&").unwrap()),
      // greater or equal to
      (TokenType::GreaterEqual, Regex::new(r">=").unwrap()),
      // less than or equal to
      (TokenType::LessEqual, Regex::new(r"<=").unwrap()),
      // or
      (TokenType::Or, Regex::new(r"\|\|").unwrap()),
      // comma, parameter separator
      (TokenType::Comma, Regex::new(r"[,，]").unwrap()),
      // Not
      (TokenType::Not, Regex::new(r"!").unwrap()),
      // add +
      (TokenType::Add, Regex::new(r"\+").unwrap()),
      // reduce -
      (TokenType::Minus, Regex::new(r"-").unwrap()),
      // take *
      (TokenType::Times, Regex::new(r"\*").unwrap()),
      // remove /
      (TokenType::Div, Regex::new(r"/").unwrap()),
      // take remainder %
      (TokenType::Mod, Regex::new(r"%").unwrap()),
      // string concatenation
      (TokenType::Concat, Regex::new(r"&").unwrap()),
      // more than the
      (TokenType::Greater, Regex::new(r">").unwrap()),
      // less than
      (TokenType::Less, Regex::new(r"<").unwrap()),
      // equal to
      (TokenType::Equal, Regex::new(r"=").unwrap()),
      // Left parenthesis
      (TokenType::LeftParen, Regex::new(r"[(（]").unwrap()),
      // closing parenthesis
      (TokenType::RightParen, Regex::new(r"[)）]").unwrap()),
      // whitespace characters
      (TokenType::Blank, Regex::new(r"\s+").unwrap()),
      // all other
      (TokenType::Unknown, Regex::new(r".+").unwrap()),
    ]
  }
  fn get_full_matches(expression: &str) -> Vec<Token> {
    let pattern = Self::get_expr_grammar()
      .iter()
      .map(|(_, regex)| format!("({})", regex.as_str()))
      .collect::<Vec<String>>()
      .join("|");
    let re = Regex::new(&pattern).unwrap();
    let mut index = 0;
    re.captures_iter(expression)
      .map(|cap| {
        let matched_str = cap.get(0).unwrap().as_str().to_owned();
        let token = Self::tokenizer(index, &matched_str);
        index += token.value.len();
        token
      })
      .collect()
  }

  fn filter_useless_token(tokens: Vec<Token>, errors: &mut Vec<String>) -> Vec<Token> {
    tokens
      .into_iter()
      .filter(|token| {
        if token.token_type == TokenType::Unknown {
          // push error message to errors
          errors.push(format!("Unknown token: {} at index {}", token.value, token.index));

          return false;
        }
        token.token_type != TokenType::Blank && token.token_type != TokenType::Unknown
      })
      .collect()
  }

  fn tokenizer(index: usize, matched_str: &str) -> Token {
    for (key, regex) in Self::get_expr_grammar().iter() {
      if regex.is_match(matched_str) {
        if *key == TokenType::Call {
          if let Some(next_token) = matched_str
            .chars()
            .next()
            .and_then(|c| Self::tokenizer(index + 1, &matched_str[c.len_utf8()..]).into())
          {
            if next_token.token_type != TokenType::LeftParen {
              return Token {
                token_type: TokenType::Value,
                index,
                value: matched_str.to_owned(),
              };
            }
          }
        }
        return Token {
          token_type: key.clone(),
          index,
          value: matched_str.to_owned(),
        };
      }
    }
    return Token {
      token_type: TokenType::Unknown,
      index,
      value: matched_str.to_owned(),
    };
    // panic!("Unexpected token: {}", matched_str);
  }
}

impl ILexer for FormulaExprLexer {
  fn get_next_token(&mut self) -> Option<Token> {
    self.current_index += 1;
    if self.current_index > (self.matches.len() - 1) as isize {
      return None;
    }
    Some(self.matches[self.current_index as usize].clone())
  }

  fn get_prev_token(&mut self) -> Option<Token> {
    self.current_index -= 1;
    if self.current_index < 0 {
      return None;
    }
    Some(self.matches[self.current_index as usize].clone())
  }

  fn reset(&mut self) {
    self.current_index = -1;
  }
}
#[cfg(test)]
mod tests {
  use super::*;

  #[test]
  fn unknown_token_is_composed_of_only_characters_rejected_by_other_tokens() {
    let lexer = FormulaExprLexer::new("a~~~~~~~~~``~".to_string());
    for m in lexer.full_matches {
      println!("{:?}", m);
    }
    assert_eq!(lexer.errors.is_empty(), false);
    // Snapshot testing is not common in Rust, you might want to assert against specific values
  }

  #[test]
  fn unknown_token_makes_other_tokens_accept_characters_that_should_not_be_accepted() {
    let lexer = FormulaExprLexer::new("abc ~~~ {}  ".to_string());
    assert_eq!(lexer.errors.is_empty(), true);
    // Snapshot testing is not common in Rust, you might want to assert against specific values
  }
}
