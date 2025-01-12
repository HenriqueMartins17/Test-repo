use regex::Regex;

use crate::formula::errors::{Error, FormulaResult};
use crate::formula::i18n::Strings;
use crate::formula::lexer::token::{Token, TokenIndex, TokenType};
use crate::params_i18n;

pub trait ILexer {
  fn get_current_token_index(&self) -> TokenIndex;
  fn set_current_token_index(&mut self, index: TokenIndex);

  fn get_prev_token(&mut self) -> Option<Token>;
  fn get_next_token(&mut self) -> Option<Token>;
  fn reset(&mut self);
}

type Expression = String;


pub struct FormulaExprLexer {
  #[allow(unused)]
  expression: Expression,
  #[allow(unused)]
  pub full_matches: Vec<Token>,
  pub matches: Vec<Token>,
  #[allow(unused)]
  pub errors: Vec<Error>,
  current_index: TokenIndex,
}

impl FormulaExprLexer {
  pub fn new(expression: Expression) -> FormulaResult<Self> {
    let full_matches = Self::get_full_matches(&expression)?;
    let mut errors = vec![];
    let matches = Self::filter_useless_token(&full_matches, &mut errors);
    let lexer = Self {
      expression,
      full_matches,
      matches,
      errors,
      current_index: -1,
    };
    Ok(lexer)
  }
}

impl FormulaExprLexer {
  // TODO: optimize
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

  fn get_full_matches(expression: &Expression) -> FormulaResult<Vec<Token>> {
    let re = Self::pattern();
    let matched = re.find_iter(expression.as_str())
        .map(|m| m.as_str()).collect::<Vec<&str>>();
    let mut index = 0;
    let mut token_list = vec![];
    for (idx, s) in matched.iter().enumerate() {
      let next = matched.get(idx + 1).map(|s| s.to_string());
      let token = Self::tokenizer(index as TokenIndex, s.to_string(), next)?;

      // TODO: For now, we are using `encode_utf16()` to make the index consistent with `s.length` in JavaScript.
      //   In the future, we should consider counting the actual number of characters instead.
      index += s.encode_utf16().count();

      token_list.push(token);
    }
    Ok(token_list)
  }

  fn filter_useless_token(tokens: &Vec<Token>, errors: &mut Vec<Error>) -> Vec<Token> {
    tokens
        .iter()
        .filter(|token| {
          if token.token_type == TokenType::Unknown {
            let error = Error::new(Strings::FunctionErrUnrecognizedOperator
                .with_params(params_i18n!(token = token.value)));
            errors.push(error);
          }

          token.token_type != TokenType::Blank && token.token_type != TokenType::Unknown
        }).map(|token| token.to_owned()).collect()
  }

  fn pattern() -> Regex {
    let mut p = vec![];
    for (_, reg) in Self::get_expr_grammar().iter() {
      p.push(reg.to_string())
    }
    let s = p.join("|");
    Regex::new(s.as_str()).unwrap()
  }

  fn tokenizer(index: TokenIndex, s: String, next: Option<String>) -> FormulaResult<Token> {
    for (token_type, reg) in Self::get_expr_grammar().iter() {
      if reg.is_match(s.as_str()) {
        // When the tokenType matches Call, it is necessary to judge whether there is a left bracket, if not,
        // it means a pureValue without curly brackets
        if *token_type == TokenType::Call {
          if let Some(next_value) = next {
            let next_token = Self::tokenizer(index, next_value, None).unwrap();
            if next_token.token_type != TokenType::LeftParen {
              return Ok(Token::new(TokenType::PureValue, index, s));
            }
          } else {
            return Ok(Token::new(TokenType::PureValue, index, s));
          }
        }
        return Ok(Token::new(token_type.clone(), index, s));
      }
    }
    Err(Error::new(format!("Unexpected token: {}", s)))
  }
}

impl ILexer for FormulaExprLexer {
  fn get_current_token_index(&self) -> TokenIndex {
    self.current_index
  }

  fn set_current_token_index(&mut self, index: TokenIndex) {
    self.current_index = index;
  }

  fn get_prev_token(&mut self) -> Option<Token> {
    // TODO(Jover): if no prev, may not need to move index
    self.current_index -= 1;
    if self.current_index < 0 {
      return None;
    }
    let token = self.matches.get(self.current_index as usize);
    token.cloned()
  }

  fn get_next_token(&mut self) -> Option<Token> {
    // TODO(Jover): if no next, may not need to move index
    self.current_index += 1;
    if self.current_index > (self.matches.len() as isize) - 1 {
      return None;
    }
    let token = self.matches.get(self.current_index as usize);
    token.cloned()
  }

  fn reset(&mut self) {
    self.current_index = -1;
  }
}

#[cfg(test)]
mod tests {
  use crate::formula::errors::Error;
  use crate::formula::lexer::token::{Token, TokenType};

  use super::*;

  /// value
  #[test]
  fn test_value_accepts_normal_value() {
    // The length of the emoji "🤯" is 1, but the length of the UTF-16 encoding is 2.
    // For now, we are using `encode_utf16()` to make the index consistent with `s.length` in JavaScript.
    assert_eq!(1, "🤯".chars().count());
    assert_eq!(2, "🤯".encode_utf16().count());

    let s = "{abc}    {🤯区间1  -a}  {   }";
    let lexer = FormulaExprLexer::new(s.to_string()).unwrap();
    assert!(lexer.errors.is_empty());
    assert_eq!(vec![
      Token::new(TokenType::Value, 0, "{abc}".to_string()),
      Token::new(TokenType::Blank, 5, "    ".to_string()),
      Token::new(TokenType::Value, 9, "{🤯区间1  -a}".to_string()),
      Token::new(TokenType::Blank, 20, "  ".to_string()),
      Token::new(TokenType::Value, 22, "{   }".to_string()),
    ], lexer.full_matches);
  }

  #[test]
  fn test_value_accepts_whitespaces() {
    let lexer = FormulaExprLexer::new("{ Foo}{-　 }".to_string()).unwrap();
    assert!(lexer.errors.is_empty());
    assert_eq!(vec![
      Token::new(TokenType::Value, 0, "{ Foo}".to_string()),
      Token::new(TokenType::Value, 6, "{-　 }".to_string()),
    ], lexer.full_matches);
  }

  #[test]
  fn test_value_accepts_escape_sequence() {
    let lexer = FormulaExprLexer::new("{}   {  x\\{_ _\\\\\\}}{}".to_string()).unwrap();
    assert!(lexer.errors.is_empty());
    assert_eq!(vec![
      Token::new(TokenType::Value, 0, "{}".to_string()),
      Token::new(TokenType::Blank, 2, "   ".to_string()),
      Token::new(TokenType::Value, 5, "{  x\\{_ _\\\\\\}}".to_string()),
      Token::new(TokenType::Value, 19, "{}".to_string()),
    ], lexer.full_matches);
  }

  #[test]
  fn test_value_accepts_empty_name() {
    let lexer = FormulaExprLexer::new("   {}  ".to_string()).unwrap();
    assert!(lexer.errors.is_empty());
    assert_eq!(vec![
      Token::new(TokenType::Blank, 0, "   ".to_string()),
      Token::new(TokenType::Value, 3, "{}".to_string()),
      Token::new(TokenType::Blank, 5, "  ".to_string()),
    ], lexer.full_matches);
  }

  /// string
  #[test]
  fn test_string_accepts_empty_strings() {
    let lexer = FormulaExprLexer::new("\"\"  \"“  ”“ ''  '’".to_string()).unwrap();
    assert!(lexer.errors.is_empty());
    assert_eq_tokens(vec![
      Token::new(TokenType::String, 0, "\"\"".to_string()),
      Token::new(TokenType::Blank, 2, "  ".to_string()),
      Token::new(TokenType::String, 4, "\"“".to_string()),
      Token::new(TokenType::Blank, 6, "  ".to_string()),
      Token::new(TokenType::String, 8, "”“".to_string()),
      Token::new(TokenType::Blank, 10, " ".to_string()),
      Token::new(TokenType::String, 11, "''".to_string()),
      Token::new(TokenType::Blank, 13, "  ".to_string()),
      Token::new(TokenType::String, 15, "'’".to_string()),
    ], lexer.full_matches);
  }

  #[test]
  fn test_string_accepts_unicode_quotation_marks() {
    let lexer = FormulaExprLexer::new("\"abc\"  \"ab c“  ”ab c“".to_string()).unwrap();
    assert!(lexer.errors.is_empty());
    assert_eq_tokens(vec![
      Token::new(TokenType::String, 0, "\"abc\"".to_string()),
      Token::new(TokenType::Blank, 5, "  ".to_string()),
      Token::new(TokenType::String, 7, "\"ab c“".to_string()),
      Token::new(TokenType::Blank, 13, "  ".to_string()),
      Token::new(TokenType::String, 15, "”ab c“".to_string()),
    ], lexer.full_matches);
  }

  #[test]
  fn test_string_accepts_mismatched_quotation_marks() {
    let lexer = FormulaExprLexer::new("\"abc\"  \"ab c“  ”ab c\"  ”ab c“".to_string()).unwrap();
    assert!(lexer.errors.is_empty());
    assert_eq_tokens(vec![
      Token::new(TokenType::String, 0, "\"abc\"".to_string()),
      Token::new(TokenType::Blank, 5, "  ".to_string()),
      Token::new(TokenType::String, 7, "\"ab c“".to_string()),
      Token::new(TokenType::Blank, 13, "  ".to_string()),
      Token::new(TokenType::String, 15, "”ab c\"".to_string()),
      Token::new(TokenType::Blank, 21, "  ".to_string()),
      Token::new(TokenType::String, 23, "”ab c“".to_string()),
    ], lexer.full_matches);
  }

  #[test]
  fn test_string_accepts_single_quotes() {
    let lexer = FormulaExprLexer::new("   'abc'  'ab c’  ‘ab c’  ‘ab c'  ".to_string()).unwrap();
    assert!(lexer.errors.is_empty());
    assert_eq_tokens(vec![
      Token::new(TokenType::Blank, 0, "   ".to_string()),
      Token::new(TokenType::String, 3, "'abc'".to_string()),
      Token::new(TokenType::Blank, 8, "  ".to_string()),
      Token::new(TokenType::String, 10, "'ab c’".to_string()),
      Token::new(TokenType::Blank, 16, "  ".to_string()),
      Token::new(TokenType::String, 18, "‘ab c’".to_string()),
      Token::new(TokenType::Blank, 24, "  ".to_string()),
      Token::new(TokenType::String, 26, "‘ab c'".to_string()),
      Token::new(TokenType::Blank, 32, "  ".to_string()),
    ], lexer.full_matches);
  }

  #[test]
  fn test_string_accepts_escape_sequence() {
    let lexer = FormulaExprLexer::new("   \"ab \\\"c\"  ”a \\”c“  ' \\\'\' ".to_string()).unwrap();
    assert!(lexer.errors.is_empty());
    assert_eq_tokens(vec![
      Token::new(TokenType::Blank, 0, "   ".to_string()),
      Token::new(TokenType::String, 3, "\"ab \\\"c\"".to_string()),
      Token::new(TokenType::Blank, 11, "  ".to_string()),
      Token::new(TokenType::String, 13, "”a \\”c“".to_string()),
      Token::new(TokenType::Blank, 20, "  ".to_string()),
      Token::new(TokenType::String, 22, "' \\''".to_string()),
      Token::new(TokenType::Blank, 27, " ".to_string()),
    ], lexer.full_matches);
  }

  #[test]
  fn test_string_escaping_single_quotes_in_double_quoted_strings_is_not_allowed() {
    let lexer = FormulaExprLexer::new("   \"ab \\'c\"  ”a \\’c“  ' \\'' ".to_string()).unwrap();
    assert!(lexer.errors.is_empty());
    assert_eq_tokens(vec![
      Token::new(TokenType::Blank, 0, "   ".to_string()),
      Token::new(TokenType::String, 3, "\"ab \\'c\"".to_string()),
      Token::new(TokenType::Blank, 11, "  ".to_string()),
      Token::new(TokenType::String, 13, "”a \\’c“".to_string()),
      Token::new(TokenType::Blank, 20, "  ".to_string()),
      Token::new(TokenType::String, 22, "' \\''".to_string()),
      Token::new(TokenType::Blank, 27, " ".to_string()),
    ], lexer.full_matches);
  }

  #[test]
  fn test_string_escaping_double_quotes_in_single_quoted_strings_is_not_allowed() {
    let lexer = FormulaExprLexer::new("   'ab \\\"c'  ’a \\”c‘  ' \\\"' ".to_string()).unwrap();
    assert!(lexer.errors.is_empty());
    assert_eq_tokens(vec![
      Token::new(TokenType::Blank, 0, "   ".to_string()),
      Token::new(TokenType::String, 3, "'ab \\\"c'".to_string()),
      Token::new(TokenType::Blank, 11, "  ".to_string()),
      Token::new(TokenType::String, 13, "’a \\”c‘".to_string()),
      Token::new(TokenType::Blank, 20, "  ".to_string()),
      Token::new(TokenType::String, 22, "' \\\"'".to_string()),
      Token::new(TokenType::Blank, 27, " ".to_string()),
    ], lexer.full_matches);
  }

  // FIXME potential bug
  #[test]
  fn test_string_is_converted_into_pure_values_if_not_closed() {
    let lexer = FormulaExprLexer::new("   \"ab c".to_string()).unwrap();
    assert!(lexer.errors.is_empty());
    assert_eq_tokens(vec![
      Token::new(TokenType::Blank, 0, "   ".to_string()),
      Token::new(TokenType::PureValue, 3, "\"ab c".to_string()),
    ], lexer.full_matches);
  }

  // FIXME potential bug
  #[test]
  fn test_string_accepts_backslash_as_last_character_in_a_string() {
    let lexer = FormulaExprLexer::new("\"ab c\\\"".to_string()).unwrap();
    assert!(lexer.errors.is_empty());
    assert_eq_tokens(vec![
      Token::new(TokenType::String, 0, "\"ab c\\\"".to_string()),
    ], lexer.full_matches);
  }

  /// call
  #[test]
  fn test_call_should_be_followed_by_left_parenthesis() {
    let lexer = FormulaExprLexer::new("ab ab(".to_string()).unwrap();
    assert!(lexer.errors.is_empty());
    assert_eq_tokens(vec![
      Token::new(TokenType::PureValue, 0, "ab".to_string()),
      Token::new(TokenType::Blank, 2, " ".to_string()),
      Token::new(TokenType::Call, 3, "ab".to_string()),
      Token::new(TokenType::LeftParen, 5, "(".to_string()),
    ], lexer.full_matches);
  }

  #[test]
  fn test_call_should_accept_unicode_characters() {
    let lexer = FormulaExprLexer::new("α👀()".to_string()).unwrap();
    assert!(lexer.errors.is_empty());
    assert_eq_tokens(vec![
      Token::new(TokenType::Call, 0, "α👀".to_string()),
      Token::new(TokenType::LeftParen, 3, "(".to_string()),
      Token::new(TokenType::RightParen, 4, ")".to_string()),
    ], lexer.full_matches);
  }

  #[test]
  fn test_call_should_be_converted_into_pure_value_if_not_immediately_followed_by_left_parenthesis() {
    let lexer = FormulaExprLexer::new("ab , c".to_string()).unwrap();
    assert!(lexer.errors.is_empty());
    assert_eq_tokens(vec![
      Token::new(TokenType::PureValue, 0, "ab".to_string()),
      Token::new(TokenType::Blank, 2, " ".to_string()),
      Token::new(TokenType::Comma, 3, ",".to_string()),
      Token::new(TokenType::Blank, 4, " ".to_string()),
      Token::new(TokenType::PureValue, 5, "c".to_string()),
    ], lexer.full_matches);
  }

  // TODO potential bug
  #[test]
  fn test_call_is_converted_into_pure_value_if_following_left_parenthesis_is_separated_by_spaces() {
    let lexer = FormulaExprLexer::new("ab (".to_string()).unwrap();
    assert!(lexer.errors.is_empty());
    assert_eq_tokens(vec![
      Token::new(TokenType::PureValue, 0, "ab".to_string()),
      Token::new(TokenType::Blank, 2, " ".to_string()),
      Token::new(TokenType::LeftParen, 3, "(".to_string()),
    ], lexer.full_matches);
  }

  /// number
  #[test]
  fn test_number_should_accept_integers() {
    let lexer = FormulaExprLexer::new("1244444444444444444444444444444444444444  78".to_string()).unwrap();
    assert!(lexer.errors.is_empty());
    assert_eq_tokens(vec![
      Token::new(TokenType::Number, 0, "1244444444444444444444444444444444444444".to_string()),
      Token::new(TokenType::Blank, 40, "  ".to_string()),
      Token::new(TokenType::Number, 42, "78".to_string()),
    ], lexer.full_matches);
  }

  #[test]
  fn test_number_should_accept_trailing_decimal_point() {
    let lexer = FormulaExprLexer::new("123. 0.".to_string()).unwrap();
    assert!(lexer.errors.is_empty());
    assert_eq_tokens(vec![
      Token::new(TokenType::Number, 0, "123.".to_string()),
      Token::new(TokenType::Blank, 5, " ".to_string()),
      Token::new(TokenType::Number, 6, "0.".to_string()),
    ], lexer.full_matches);
  }

  #[test]
  fn test_number_should_accept_starting_decimal_point() {
    let lexer = FormulaExprLexer::new(".123 .0".to_string()).unwrap();
    assert!(lexer.errors.is_empty());
    assert_eq_tokens(vec![
      Token::new(TokenType::Number, 0, ".123".to_string()),
      Token::new(TokenType::Blank, 4, " ".to_string()),
      Token::new(TokenType::Number, 5, ".0".to_string()),
    ], lexer.full_matches);
  }

  // FIXME potential bug
  #[test]
  fn test_number_accepts_single_period() {
    let lexer = FormulaExprLexer::new(".".to_string()).unwrap();
    assert!(lexer.errors.is_empty());
    assert_eq_tokens(vec![
      Token::new(TokenType::Number, 0, ".".to_string()),
    ], lexer.full_matches);
  }

  // FIXME potential bug
  #[test]
  fn test_number_accepts_sequence_of_periods() {
    let lexer = FormulaExprLexer::new("123, ......".to_string()).unwrap();
    assert!(lexer.errors.is_empty());
    assert_eq_tokens(vec![
      Token::new(TokenType::Number, 0, "123".to_string()),
      Token::new(TokenType::Comma, 3, ",".to_string()),
      Token::new(TokenType::Blank, 4, " ".to_string()),
      Token::new(TokenType::Number, 5, "......".to_string()),
    ], lexer.full_matches);
  }

  // FIXME potential bug
  #[test]
  fn test_number_accepts_multiple_decimal_points() {
    let lexer = FormulaExprLexer::new("123......456".to_string()).unwrap();
    assert!(lexer.errors.is_empty());
    assert_eq_tokens(vec![
      Token::new(TokenType::Number, 0, "123......456".to_string()),
    ], lexer.full_matches);
  }

  /// punctuation
  #[test]
  fn test_punctuation_should_accept_operators_with_multiple_characters() {
    let lexer = FormulaExprLexer::new("!=&&>=<<==||".to_string()).unwrap();
    assert!(lexer.errors.is_empty());
    assert_eq_tokens(vec![
      Token::new(TokenType::NotEqual, 0, "!=".to_string()),
      Token::new(TokenType::And, 2, "&&".to_string()),
      Token::new(TokenType::GreaterEqual, 4, ">=".to_string()),
      Token::new(TokenType::Less, 6, "<".to_string()),
      Token::new(TokenType::LessEqual, 7, "<=".to_string()),
      Token::new(TokenType::Equal, 9, "=".to_string()),
      Token::new(TokenType::Or, 10, "||".to_string()),
    ], lexer.full_matches);
  }

  #[test]
  fn test_punctuation_should_accept_full_width_commas() {
    let lexer = FormulaExprLexer::new("， ,，,,".to_string()).unwrap();
    assert!(lexer.errors.is_empty());
    assert_eq_tokens(vec![
      Token::new(TokenType::Comma, 0, "，".to_string()),
      Token::new(TokenType::Blank, 1, " ".to_string()),
      Token::new(TokenType::Comma, 2, ",".to_string()),
      Token::new(TokenType::Comma, 3, "，".to_string()),
      Token::new(TokenType::Comma, 4, ",".to_string()),
      Token::new(TokenType::Comma, 5, ",".to_string()),
    ], lexer.full_matches);
  }

  // FIXME: now is treated as `Blank` token
  #[test]
  fn test_punctuation_should_recognize_single_vertical_bar_as_unknown_token() {
    let lexer = FormulaExprLexer::new("| |".to_string()).unwrap();
    assert!(lexer.errors.is_empty());
    assert_eq_tokens(vec![
      Token::new(TokenType::Blank, 0, "| |".to_string()),
    ], lexer.full_matches);
  }

  #[test]
  fn test_punctuation_should_accept_full_width_parentheses() {
    let lexer = FormulaExprLexer::new("（(）)".to_string()).unwrap();
    assert!(lexer.errors.is_empty());
    assert_eq_tokens(vec![
      Token::new(TokenType::LeftParen, 0, "（".to_string()),
      Token::new(TokenType::LeftParen, 1, "(".to_string()),
      Token::new(TokenType::RightParen, 2, "）".to_string()),
      Token::new(TokenType::RightParen, 3, ")".to_string()),
    ], lexer.full_matches);
  }

  /// blank
  #[test]
  fn test_blank_should_recognize_contiguous_spaces_as_single_blank_token() {
    let lexer = FormulaExprLexer::new("12 3     A    ".to_string()).unwrap();
    assert!(lexer.errors.is_empty());
    assert_eq_tokens(vec![
      Token::new(TokenType::Number, 0, "12".to_string()),
      Token::new(TokenType::Blank, 2, " ".to_string()),
      Token::new(TokenType::Number, 3, "3".to_string()),
      Token::new(TokenType::Blank, 4, "     ".to_string()),
      Token::new(TokenType::PureValue, 9, "A".to_string()),
      Token::new(TokenType::Blank, 10, "    ".to_string()),
    ], lexer.full_matches);
  }

  #[test]
  fn test_blank_should_accept_full_width_spaces() {
    let lexer = FormulaExprLexer::new("123 　　  A".to_string()).unwrap();
    assert!(lexer.errors.is_empty());
    assert_eq_tokens(vec![
      Token::new(TokenType::Number, 0, "123".to_string()),
      Token::new(TokenType::Blank, 3, " 　　  ".to_string()),
      Token::new(TokenType::PureValue, 8, "A".to_string()),
    ], lexer.full_matches);
  }

  #[test]
  fn test_blank_should_produce_single_blank_token_if_given_string_with_only_spaces() {
    let lexer = FormulaExprLexer::new("  　　  　".to_string()).unwrap();
    assert!(lexer.errors.is_empty());
    assert_eq_tokens(vec![
      Token::new(TokenType::Blank, 0, "  　　  　".to_string()),
    ], lexer.full_matches);
  }

  /// pure value
  #[test]
  fn test_pure_value_should_accept_unicode_characters() {
    let lexer = FormulaExprLexer::new("Foo a11 $T$ ᵣ☯".to_string()).unwrap();
    assert!(lexer.errors.is_empty());
    assert_eq_tokens(vec![
      Token::new(TokenType::PureValue, 0, "Foo".to_string()),
      Token::new(TokenType::Blank, 3, " ".to_string()),
      Token::new(TokenType::PureValue, 4, "a11".to_string()),
      Token::new(TokenType::Blank, 7, " ".to_string()),
      Token::new(TokenType::PureValue, 8, "$T$".to_string()),
      Token::new(TokenType::Blank, 11, " ".to_string()),
      Token::new(TokenType::PureValue, 12, "ᵣ☯".to_string()),
    ], lexer.full_matches);
  }

  /// unknown token
  // FIXME potential bug
  #[test]
  fn test_unknown_token_composed_of_rejected_characters() {
    let lexer = FormulaExprLexer::new("a~~~~~~~~~``~".to_string()).unwrap();
    assert_eq_errors(vec![
      Error::new(Strings::FunctionErrUnrecognizedOperator.with_params(params_i18n!(token = "~~~~~~~~~``~"))),
    ], lexer.errors);
    assert_eq_tokens(vec![
      Token::new(TokenType::PureValue, 0, "a".to_string()),
      Token::new(TokenType::Unknown, 1, "~~~~~~~~~``~".to_string()),
    ], lexer.full_matches);
  }

  // FIXME potential bug
  #[test]
  fn test_unknown_token_makes_other_tokens_accept_rejected_characters() {
    let lexer = FormulaExprLexer::new("abc ~~~ {}  ".to_string()).unwrap();
    assert!(lexer.errors.is_empty());
    assert_eq_tokens(vec![
      Token::new(TokenType::PureValue, 0, "abc".to_string()),
      Token::new(TokenType::Blank, 3, " ".to_string()),
      Token::new(TokenType::Value, 4, "~~~ {}  ".to_string()),
    ], lexer.full_matches);
  }

  /// full_matches
  #[test]
  fn test_empty_string_should_produce_empty_token_list() {
    let lexer = FormulaExprLexer::new("".to_string()).unwrap();
    assert!(lexer.errors.is_empty());
    assert!(lexer.full_matches.is_empty());
  }

  /// matches
  #[test]
  fn test_matches_should_not_contain_blank_tokens() {
    let lexer = FormulaExprLexer::new("  A 123 \"  \"".to_string()).unwrap();
    assert!(lexer.errors.is_empty());
    assert_eq_tokens(vec![
      Token::new(TokenType::PureValue, 2, "A".to_string()),
      Token::new(TokenType::Number, 4, "123".to_string()),
      Token::new(TokenType::String, 8, "\"  \"".to_string()),
    ], lexer.matches);
  }

  #[test]
  fn test_matches_should_produce_empty_token_list_if_given_empty_string() {
    let lexer = FormulaExprLexer::new("".to_string()).unwrap();
    assert!(lexer.errors.is_empty());
    assert!(lexer.matches.is_empty());
  }

  #[test]
  fn test_matches_should_produce_empty_token_list_if_given_string_with_only_spaces() {
    let lexer = FormulaExprLexer::new("        ".to_string()).unwrap();
    assert!(lexer.errors.is_empty());
    assert!(lexer.matches.is_empty());
  }

  #[test]
  fn test_matches_should_not_contain_unknown_tokens() {
    let lexer = FormulaExprLexer::new(" 123    ~~ ".to_string()).unwrap();
    assert!(lexer.errors.is_empty());
    assert_eq_tokens(vec![
      Token::new(TokenType::Number, 1, "123".to_string()),
    ], lexer.matches);
  }

  /// get token
  #[test]
  fn test_get_next_token_should_produce_tokens_in_order() {
    let mut lexer = FormulaExprLexer::new(" 123    ~~ ".to_string()).unwrap();
    assert!(lexer.errors.is_empty());

    let tokens = collect_tokens(&mut lexer);
    assert_eq_tokens(tokens, vec![
      Token::new(TokenType::Number, 1, "123".to_string()),
    ]);

    // Ensure `getNextToken` returns `None` when there are no more tokens.
    assert_eq!(lexer.get_next_token(), None);
  }

  #[test]
  fn test_get_next_token_should_produce_null_if_no_more_token() {
    let mut lexer = FormulaExprLexer::new("   ".to_string()).unwrap();
    assert!(lexer.errors.is_empty());
    assert_eq!(lexer.get_next_token(), None);
  }

  #[test]
  fn test_get_prev_token() {
    let mut lexer = FormulaExprLexer::new(" 123 a  ".to_string()).unwrap();
    assert!(lexer.errors.is_empty());

    let token1 = lexer.get_next_token();
    let token2 = lexer.get_next_token();
    assert_eq!(lexer.get_next_token(), None);

    assert_eq!(lexer.get_prev_token(), token2);
    assert_eq!(lexer.get_prev_token(), token1);

    // Ensure `getPrevToken` returns `None` when there is no previous token.
    assert_eq!(lexer.get_prev_token(), None);
  }

  #[test]
  fn test_interleaving_get_next_token_and_get_prev_token() {
    let mut lexer = FormulaExprLexer::new(" 123 a=  ".to_string()).unwrap();
    assert!(lexer.errors.is_empty());

    let token1 = lexer.get_next_token();
    let token2 = lexer.get_next_token();

    assert_eq!(lexer.get_prev_token(), token1.clone());
    assert_eq!(lexer.get_prev_token(), None);

    assert_eq!(lexer.get_next_token(), token1.clone());
    assert_eq!(lexer.get_next_token(), token2.clone());
    assert_eq!(lexer.get_prev_token(), token1);
    assert_eq!(lexer.get_next_token(), token2);

    let token3 = lexer.get_next_token();
    assert_eq!(lexer.get_next_token(), None);

    assert_eq!(lexer.get_prev_token(), token3);
  }

  #[test]
  fn test_reset() {
    let mut lexer = FormulaExprLexer::new(" 123 a=  ".to_string()).unwrap();
    assert!(lexer.errors.is_empty());

    let token1 = lexer.get_next_token();
    lexer.get_next_token();
    lexer.get_next_token();
    lexer.reset();

    assert_eq!(lexer.get_next_token(), token1);
  }

  // Helper function to compare Token vectors.
  fn assert_eq_tokens(expected: Vec<Token>, actual: Vec<Token>) {
    assert_eq!(expected.len(), actual.len());
    for (expected_token, actual_token) in expected.iter().zip(actual.iter()) {
      assert_eq!(expected_token.token_type, actual_token.token_type);
      assert_eq!(expected_token.value, actual_token.value);
      // Add more assertions as needed.
    }
  }

  // Helper function to compare Error vectors.
  fn assert_eq_errors(expected: Vec<Error>, actual: Vec<Error>) {
    assert_eq!(expected.len(), actual.len());
    for (expected_error, actual_error) in expected.iter().zip(actual.iter()) {
      assert_eq!(*expected_error.message, actual_error.message);
      // Add more assertions as needed.
    }
  }

  // Helper function to collect tokens.
  fn collect_tokens(lexer: &mut FormulaExprLexer) -> Vec<Token> {
    let mut tokens = Vec::new();
    while let Some(token) = lexer.get_next_token() {
      tokens.push(token);
    }
    tokens
  }
}
