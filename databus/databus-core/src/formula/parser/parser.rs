use std::collections::HashMap;
use std::rc::Rc;

use crate::formula::errors::{Error, FormulaResult};
use crate::formula::functions::FunctionProvider;
use crate::formula::i18n::Strings;
use crate::formula::lexer::lexer::ILexer;
use crate::formula::lexer::token::{Token, TokenType};
use crate::formula::parser::ast::{AstNode, BinaryOperatorNode, CallOperandNode, NumberOperandNode, PureValueOperandNode, StringOperandNode, UnaryOperatorNode, ValueOperandNode};
use crate::formula::types::IField;
use crate::shared::IUserInfo;
use crate::so::{DatasheetPackContext, DatasheetMetaSO, DatasheetPackSO, DatasheetSnapshotSO, NodePermissionStateSO, NodeSO};

#[derive(Debug, PartialEq)]
pub struct Context {
  pub field: Rc<IField>,
  pub field_map: HashMap<String, Rc<IField>>,
  pub state: Rc<DatasheetPackContext>,
}

impl Context {
  pub fn new(field: Rc<IField>, field_map: HashMap<String, Rc<IField>>) -> Self {
    Self {
      field,
      field_map,
      state: Rc::new(Self::new_state()),
    }
  }

  // TODO: refactor
  fn new_state() -> DatasheetPackContext {
    DatasheetPackContext {
      datasheet_pack: Box::new(DatasheetPackSO {
        snapshot: DatasheetSnapshotSO {
          meta: DatasheetMetaSO {
            field_map: Default::default(),
            views: vec![],
            widget_panels: None,
          },
          record_map: Default::default(),
          datasheet_id: "".to_string(),
        },
        datasheet: NodeSO {
          id: "".to_string(),
          name: "".to_string(),
          description: "".to_string(),
          parent_id: "".to_string(),
          icon: "".to_string(),
          node_shared: false,
          node_permit_set: false,
          node_favorite: Some(false),
          space_id: "".to_string(),
          role: "".to_string(),
          permissions: NodePermissionStateSO { is_deleted: None, permissions: None },
          revision: 0,
          is_ghost_node: None,
          active_view: None,
          extra: None,
        },
        field_permission_map: None,
        foreign_datasheet_map: None,
        units: None,
      }),
        user_info: IUserInfo {..Default::default()},
        unit_map: HashMap::new(),
      user_map: HashMap::new(),
    }
  }
}

pub struct FormulaExprParser {
  lexer: Box<dyn ILexer>,
  #[allow(unused)]
  context: Rc<Context>,
  current_token: Option<Token>,

  priority_map: HashMap<TokenType, isize>,
  function_provider: FunctionProvider,
}

impl FormulaExprParser {
  pub fn new(mut lexer: Box<dyn ILexer>, context: Rc<Context>) -> Self {
    let current_token = lexer.get_next_token();

    let mut priority_map = HashMap::new();
    for (i, arr) in vec![
      vec![TokenType::Times, TokenType::Div, TokenType::Mod],
      vec![TokenType::Add, TokenType::Minus],
      vec![TokenType::Greater, TokenType::GreaterEqual, TokenType::Less, TokenType::LessEqual],
      vec![TokenType::Equal, TokenType::NotEqual],
      vec![TokenType::And],
      vec![TokenType::Or],
      vec![TokenType::Concat],
    ].iter().enumerate() {
      for token_type in arr {
        priority_map.insert(token_type.clone(), i as isize);
      }
    }

    Self {
      lexer,
      context,
      current_token,
      priority_map,
      function_provider: FunctionProvider::new(),
    }
  }

  pub fn parse(&mut self) -> FormulaResult<AstNode> {
    let node = self.expr(false)?;
    if self.current_token.is_some() {
      return Err(Error::new(Strings::FunctionErrUnrecognizedChar));
    }
    Ok(node)
  }

  // TODO: optimizable
  pub fn next(&mut self, token_type: TokenType) -> FormulaResult<Option<Token>> {
    if self.current_token.is_none() {
      return Ok(None);
    }
    if self.current_token.as_ref().unwrap().token_type == token_type {
      self.current_token = self.lexer.get_next_token();
    } else {
      return match token_type {
        TokenType::LeftParen => {
          Err(Error::new(Strings::FunctionErrNoLeftBracket))
        }
        TokenType::RightParen => {
          Err(Error::new(Strings::FunctionErrEndOfRightBracket))
        }
        _ => {
          Err(Error::new(Strings::FunctionErrUnableParseChar))
        }
      };
    }
    Ok(self.current_token.clone())
  }

  pub fn factor(&mut self) -> FormulaResult<AstNode> {
    // factor : VALUE | LEFT_PAREN expr RIGHT_PAREN | NOT expr

    let token = self.current_token.as_ref()
        .ok_or_else(|| Error::new(Strings::FunctionErrWrongFunctionSuffix))
        .cloned()?;

    return match token.token_type {
      // field variable: {value}
      TokenType::Value => {
        self.next(TokenType::Value)?;
        let token_value = token.value[1..token.value.len() - 1].to_string();
        if token_value == self.context.field.get_id() {
          return Err(Error::new(Strings::FunctionErrNoRefSelfColumn));
        }

        let node = ValueOperandNode::new(token.clone(), self.context.clone(), None)?;
        Ok(AstNode::ValueOperand(node))
      }

      // field variable: value (without curly braces)
      TokenType::PureValue => {
        self.next(TokenType::PureValue)?;
        let token_value = token.value.to_string();
        if token_value == self.context.field.get_id() {
          return Err(Error::new(Strings::FunctionErrNoRefSelfColumn));
        }

        let node = PureValueOperandNode::new(token.clone(), self.context.clone(), Some(self.context.field.clone()))?;
        Ok(AstNode::PureValueOperand(node))
      }

      // Preset functions: Sum/Average ...
      TokenType::Call => {
        self.next(TokenType::Call)?;
        let mut node = CallOperandNode::new(token.clone());
        self.next(TokenType::LeftParen)?;
        if self.current_token.is_none() {
          return Err(Error::new(Strings::FunctionErrEndOfRightBracket));
        }

        while self.current_token.as_ref().unwrap().token_type != TokenType::RightParen {
          node.params.push(self.expr(false)?);
          if let Some(current_token) = &self.current_token {
            if current_token.token_type != TokenType::Comma {
              break;
            }
          } else {
            return Err(Error::new(Strings::FunctionErrEndOfRightBracket));
          }
        }

        let func = self.function_provider.get_function(&node.value)
            .ok_or(Error::new(Strings::FunctionErrUnknownFunction))?;
        let node_params = node.params.iter().collect();
        let value_type = func.func.get_return_type(&node_params)?;
        node.basic.value_type = value_type;

        self.next(TokenType::RightParen)?;
        Ok(AstNode::CallOperand(node))
      }

      // number: 123.333
      TokenType::Number => {
        self.next(TokenType::Number)?;
        Ok(AstNode::NumberOperand(NumberOperandNode::new(token.clone())))
      }

      // string: 'xyz'
      TokenType::String => {
        self.next(TokenType::String)?;
        Ok(AstNode::StringOperand(StringOperandNode::new(token.clone())))
      }

      // Left parenthesis: '('
      TokenType::LeftParen => {
        self.next(TokenType::LeftParen)?;
        let node = self.expr(false)?;
        self.next(TokenType::RightParen)?;
        Ok(node)
      }

      // Negate sign (unary arithmetic sign): '!'
      TokenType::Not => {
        self.next(TokenType::Not)?;
        let node = self.factor()?;
        Ok(AstNode::UnaryOperator(UnaryOperatorNode::new(node, token.clone())))
      }

      // + sign (unary arithmetic sign): '+'
      TokenType::Add => {
        self.next(TokenType::Add)?;
        let node = self.factor()?;
        Ok(AstNode::UnaryOperator(UnaryOperatorNode::new(node, token.clone())))
      }

      // -sign (unary arithmetic sign): '-'
      TokenType::Minus => {
        self.next(TokenType::Minus)?;
        let node = self.factor()?;
        Ok(AstNode::UnaryOperator(UnaryOperatorNode::new(node, token.clone())))
      }

      TokenType::Comma => {
        self.next(TokenType::Comma)?;
        let node = self.expr(false)?;
        Ok(node)
      }

      TokenType::Blank => {
        self.next(TokenType::Blank)?;
        self.factor()
      }

      _ => {
        Err(Error::new(Strings::FunctionErrUnknownOperator))
      }
    };
  }

  pub fn expr(&mut self, inner: bool) -> FormulaResult<AstNode> {
    // expr   : factor ((&& | ||) factor)*
    // factor : Number | String | Call | VALUE | LEFT_PAREN expr RIGHT_PAREN | NOT expr

    let mut node = self.factor()?;

    while self.current_token.is_some() &&
        [
          TokenType::And, TokenType::Or, TokenType::Add, TokenType::Times, TokenType::Div, TokenType::Minus,
          TokenType::Mod, TokenType::Concat, TokenType::Equal, TokenType::NotEqual, TokenType::Greater, TokenType::GreaterEqual,
          TokenType::Less, TokenType::LessEqual,
        ].contains(&self.current_token.as_ref().unwrap().token_type)
    {
      let token = self.current_token.as_ref().unwrap().clone();
      self.next(token.token_type.clone())?;
      if self.current_token.is_none() {
        return Err(Error::new(Strings::FunctionErrWrongFunctionSuffix));
      }
      let next_token: Option<Token>;
      let current_token = self.current_token.as_ref().unwrap().clone();
      let current_token_index = self.lexer.get_current_token_index();

      // Take a step forward, get the token and go back
      //
      // 1. If you encounter a function or left parenthesis, go forward to test the entire function or parenthesis content,
      // get the following operator and then fall back
      //
      // 2. If it is not a function, just try a token forward, get the operator and then fall back
      if [TokenType::Call, TokenType::LeftParen].contains(&current_token.token_type) {
        self.factor()?;
        next_token = self.current_token.clone();
        self.current_token = Some(current_token);
        self.lexer.set_current_token_index(current_token_index);
      } else {
        next_token = self.lexer.get_next_token();
        self.lexer.get_prev_token();
      }

      let mut right: Option<AstNode> = None;
      let current_op_index = self.priority_map.get(&token.token_type).cloned();
      if let Some(next_token) = next_token {
        let next_op_index = self.priority_map.get(&next_token.token_type).cloned();

        if current_op_index.is_some() && next_op_index.is_some() && next_op_index < current_op_index {
          let node1 = self.expr(true)?;
          right = Some(node1);
        }
        // When operators with different priorities are encountered in the loop, the recursion must be exited;
        if inner && matches!((current_op_index, next_op_index), (Some(a), Some(b)) if b > a) {
          let right = match right {
            Some(right) => right,
            None => self.factor()?,
          };
          return Ok(AstNode::BinaryOperator(BinaryOperatorNode::new(node, token.clone(), right)));
        }
      }
      let right = match right {
        Some(right) => right,
        None => self.factor()?,
      };
      node = AstNode::BinaryOperator(BinaryOperatorNode::new(node, token.clone(), right));
    }
    // 1 + (1 + 3) * 2
    Ok(node)
  }
}

#[cfg(test)]
mod tests {
  use crate::fields::property::{DateFormat, DateTimeFieldPropertySO, FormulaFieldPropertySO, NumberFieldPropertySO, SingleTextFieldPropertySO, TimeFormat};
  use crate::fields::property::field_types::BasicValueType;
  use crate::formula::lexer::lexer::FormulaExprLexer;
  use crate::formula::lexer::token::TokenType;
  use crate::formula::parser::ast::{BaseValueOperandNode, BasicAstNodeData, CallOperandNode};
  use crate::formula::types::IBaseField;

  use super::*;

  fn mock_context(expr: String) -> Rc<Context> {
    let field = Rc::new(IField::Formula(IBaseField {
      id: "fld4".to_string(),
      name: "Field 4".to_string(),
      desc: None,
      required: None,
      property: FormulaFieldPropertySO {
        datasheet_id: "dst1".to_string(),
        expression: expr.clone(),
        formatting: None,
      },
    }));
    let map: HashMap<String, Rc<IField>> = vec![
      ("fld1".to_string(),
       Rc::new(IField::SingleText(IBaseField {
         id: "fld1".to_string(),
         name: "Field 1".to_string(),
         desc: None,
         required: None,
         property: SingleTextFieldPropertySO {
           default_value: None,
         },
       }))),
      ("fld2".to_string(),
       Rc::new(IField::Number(IBaseField {
         id: "fld2".to_string(),
         name: "Field 2".to_string(),
         desc: None,
         required: None,
         property: NumberFieldPropertySO {
           precision: 0,
           default_value: None,
           comma_style: None,
           symbol: None,
           symbol_align: None,
         },
       }))),
      ("fld3".to_string(),
       Rc::new(IField::DateTime(IBaseField {
         id: "fld3".to_string(),
         name: "Field 3".to_string(),
         desc: None,
         required: None,
         property: DateTimeFieldPropertySO {
           date_format: DateFormat::YyyyMm,
           time_format: TimeFormat::HHmm,
           include_time: false,
           auto_fill: false,
           time_zone: None,
           include_time_zone: None,
         },
       }))),
      ("fld4".to_string(), field.clone()),
    ].into_iter().collect();

    Rc::new(Context::new(field, map))
  }

  fn parse_with_context(expression: &str) -> (FormulaResult<AstNode>, Rc<Context>) {
    let context = mock_context(expression.to_string());

    let lexer = FormulaExprLexer::new(expression.to_string()).unwrap();
    let mut parser = FormulaExprParser::new(Box::new(lexer), context.clone());

    (parser.parse(), context)
  }

  fn parse(expression: &str) -> FormulaResult<AstNode> {
    parse_with_context(expression).0
  }

  #[test]
  fn test_function_call_nullary_function() {
    let ast = parse("record_id()").unwrap();
    assert_eq!(AstNode::CallOperand(CallOperandNode {
      basic: BasicAstNodeData {
        token: Token {
          token_type: TokenType::Call,
          index: 0,
          value: "record_id".to_string(),
        },
        value_type: BasicValueType::String,
        inner_value_type: None,
      },
      value: "record_id".to_string(),
      params: vec![],
    }), ast);
  }

  #[test]
  fn test_function_call_unary_function() {
    let ast = parse("inT('babe')").unwrap();
    assert_eq!(AstNode::CallOperand(CallOperandNode {
      basic: BasicAstNodeData {
        token: Token {
          token_type: TokenType::Call,
          index: 0,
          value: "inT".to_string(),
        },
        value_type: BasicValueType::Number,
        inner_value_type: None,
      },
      value: "inT".to_string(),
      params: vec![
        AstNode::StringOperand(StringOperandNode {
          basic: BasicAstNodeData {
            token: Token {
              token_type: TokenType::String,
              index: 4,
              value: "'babe'".to_string(),
            },
            value_type: BasicValueType::String,
            inner_value_type: None,
          },
          value: "'babe'".to_string(),
        }),
      ],
    }), ast);
  }

  #[test]
  fn test_function_call_3_arity_function() {
    let (ast, context) = parse_with_context("Max(31, -77, {fld3})");
    let ast = ast.unwrap();
    assert_eq!(AstNode::CallOperand(CallOperandNode {
      basic: BasicAstNodeData {
        token: Token {
          token_type: TokenType::Call,
          index: 0,
          value: "Max".to_string(),
        },
        value_type: BasicValueType::Number,
        inner_value_type: None,
      },
      value: "Max".to_string(),
      params: vec![
        AstNode::NumberOperand(NumberOperandNode {
          basic: BasicAstNodeData {
            token: Token {
              token_type: TokenType::Number,
              index: 4,
              value: "31".to_string(),
            },
            value_type: BasicValueType::Number,
            inner_value_type: None,
          },
          value: "31".to_string(),
        }),
        AstNode::UnaryOperator(UnaryOperatorNode {
          basic: BasicAstNodeData {
            token: Token {
              token_type: TokenType::Minus,
              index: 8,
              value: "-".to_string(),
            },
            value_type: BasicValueType::Number,
            inner_value_type: None,
          },
          child: Box::new(AstNode::NumberOperand(NumberOperandNode {
            basic: BasicAstNodeData {
              token: Token {
                token_type: TokenType::Number,
                index: 9,
                value: "77".to_string(),
              },
              value_type: BasicValueType::Number,
              inner_value_type: None,
            },
            value: "77".to_string(),
          })),
        }),
        AstNode::ValueOperand(ValueOperandNode {
          basic: BaseValueOperandNode {
            basic: BasicAstNodeData {
              token: Token {
                token_type: TokenType::Value,
                index: 13,
                value: "{fld3}".to_string(),
              },
              value_type: BasicValueType::DateTime,
              inner_value_type: Some(BasicValueType::String),
            },
            value: "{fld3}".to_string(),
            field: context.field_map.get("fld3").unwrap().clone(),
            context,
          },
        }),
      ],
    }), ast);
  }

  #[test]
  fn test_function_call_nested_function() {
    let ast = parse("Max(rOuNd(31+true()), value('')&'b', --0)").unwrap();
    assert_eq!(AstNode::CallOperand(
      CallOperandNode {
        basic: BasicAstNodeData {
          token: Token {
            token_type: TokenType::Call,
            index: 0,
            value: "Max".to_string(),
          },
          value_type: BasicValueType::Number,
          inner_value_type: None,
        },
        value: "Max".to_string(),
        params: vec![
          AstNode::CallOperand(
            CallOperandNode {
              basic: BasicAstNodeData {
                token: Token {
                  token_type: TokenType::Call,
                  index: 4,
                  value: "rOuNd".to_string(),
                },
                value_type: BasicValueType::Number,
                inner_value_type: None,
              },
              value: "rOuNd".to_string(),
              params: vec![
                AstNode::BinaryOperator(BinaryOperatorNode {
                  basic: BasicAstNodeData {
                    token: Token {
                      token_type: TokenType::Add,
                      index: 12,
                      value: "+".to_string(),
                    },
                    value_type: BasicValueType::String,
                    inner_value_type: None,
                  },
                  left: Box::new(AstNode::NumberOperand(NumberOperandNode {
                    basic: BasicAstNodeData {
                      token: Token {
                        token_type: TokenType::Number,
                        index: 10,
                        value: "31".to_string(),
                      },
                      value_type: BasicValueType::Number,
                      inner_value_type: None,
                    },
                    value: "31".to_string(),
                  })),
                  right: Box::new(AstNode::CallOperand(CallOperandNode {
                    basic: BasicAstNodeData {
                      token: Token {
                        token_type: TokenType::Call,
                        index: 13,
                        value: "true".to_string(),
                      },
                      value_type: BasicValueType::Boolean,
                      inner_value_type: None,
                    },
                    value: "true".to_string(),
                    params: vec![],
                  })),
                }),
              ],
            },
          ),
          AstNode::BinaryOperator(
            BinaryOperatorNode {
              basic: BasicAstNodeData {
                token: Token {
                  token_type: TokenType::Concat,
                  index: 31,
                  value: "&".to_string(),
                },
                value_type: BasicValueType::String,
                inner_value_type: None,
              },
              left: Box::new(AstNode::CallOperand(CallOperandNode {
                basic: BasicAstNodeData {
                  token: Token {
                    token_type: TokenType::Call,
                    index: 22,
                    value: "value".to_string(),
                  },
                  value_type: BasicValueType::Number,
                  inner_value_type: None,
                },
                value: "value".to_string(),
                params: vec![
                  AstNode::StringOperand(StringOperandNode {
                    basic: BasicAstNodeData {
                      token: Token {
                        token_type: TokenType::String,
                        index: 28,
                        value: "''".to_string(),
                      },
                      value_type: BasicValueType::String,
                      inner_value_type: None,
                    },
                    value: "''".to_string(),
                  })
                ],
              })),
              right: Box::new(AstNode::StringOperand(StringOperandNode {
                basic: BasicAstNodeData {
                  token: Token {
                    token_type: TokenType::String,
                    index: 32,
                    value: "'b'".to_string(),
                  },
                  value_type: BasicValueType::String,
                  inner_value_type: None,
                },
                value: "'b'".to_string(),
              })),
            }
          ),
          AstNode::UnaryOperator(UnaryOperatorNode {
            basic: BasicAstNodeData {
              token: Token {
                token_type: TokenType::Minus,
                index: 37,
                value: "-".to_string(),
              },
              value_type: BasicValueType::Number,
              inner_value_type: None,
            },
            child: Box::new(AstNode::UnaryOperator(UnaryOperatorNode {
              basic: BasicAstNodeData {
                token: Token {
                  token_type: TokenType::Minus,
                  index: 38,
                  value: "-".to_string(),
                },
                value_type: BasicValueType::Number,
                inner_value_type: None,
              },
              child: Box::new(AstNode::NumberOperand(NumberOperandNode {
                basic: BasicAstNodeData {
                  token: Token {
                    token_type: TokenType::Number,
                    index: 39,
                    value: "0".to_string(),
                  },
                  value_type: BasicValueType::Number,
                  inner_value_type: None,
                },
                value: "0".to_string(),
              })),
            })),
          }),
        ],
      },
    ), ast);
  }

  #[test]
  fn test_function_call_missing_right_parenthesis() {
    let err = parse("max(1,7'").err();
    assert_eq!(Strings::FunctionErrEndOfRightBracket.to_string(), err.unwrap().message);
  }

  #[test]
  fn test_function_call_redundant_comma() {
    let err = parse("max(1,7,)").err();
    assert_eq!(Strings::FunctionErrUnknownOperator.to_string(), err.unwrap().message);
  }

  #[test]
  fn test_field_with_curly_braces() {
    let (ast, context) = parse_with_context("  {fld2}");
    let ast = ast.unwrap();
    assert_eq!(AstNode::ValueOperand(ValueOperandNode {
      basic: BaseValueOperandNode {
        basic: BasicAstNodeData {
          token: Token {
            token_type: TokenType::Value,
            index: 2,
            value: "{fld2}".to_string(),
          },
          value_type: BasicValueType::Number,
          inner_value_type: Some(BasicValueType::String),
        },
        value: "{fld2}".to_string(),
        field: context.field_map.get("fld2").unwrap().clone(),
        context,
      }
    }), ast);
  }

  #[test]
  fn test_field_without_curly_braces() {
    let (ast, context) = parse_with_context("fld2  ");
    let ast = ast.unwrap();
    assert_eq!(AstNode::PureValueOperand(PureValueOperandNode {
      basic: BaseValueOperandNode {
        basic: BasicAstNodeData {
          token: Token {
            token_type: TokenType::PureValue,
            index: 0,
            value: "fld2".to_string(),
          },
          value_type: BasicValueType::Number,
          inner_value_type: Some(BasicValueType::String),
        },
        value: "fld2".to_string(),
        field: context.field_map.get("fld2").unwrap().clone(),
        context,
      }
    }), ast);
  }

  #[test]
  fn test_field_two_consecutive_fields() {
    let err = parse("fld2  {fld4} ").err();
    assert_eq!(Strings::FunctionErrUnrecognizedChar.to_string(), err.unwrap().message);
  }

  #[test]
  fn test_unary_operator_not() {
    let ast = parse("  ! \"foo\"").unwrap();
    assert_eq!(AstNode::UnaryOperator(UnaryOperatorNode {
      basic: BasicAstNodeData {
        token: Token {
          token_type: TokenType::Not,
          index: 2,
          value: "!".to_string(),
        },
        value_type: BasicValueType::Boolean,
        inner_value_type: None,
      },
      child: Box::new(AstNode::StringOperand(StringOperandNode {
        basic: BasicAstNodeData {
          token: Token {
            token_type: TokenType::String,
            index: 4,
            value: "\"foo\"".to_string(),
          },
          value_type: BasicValueType::String,
          inner_value_type: None,
        },
        value: "\"foo\"".to_string(),
      })),
    }), ast);
  }

  #[test]
  fn test_unary_operator_minus() {
    let ast = parse("  - \"foo\"").unwrap();
    assert_eq!(AstNode::UnaryOperator(UnaryOperatorNode {
      basic: BasicAstNodeData {
        token: Token {
          token_type: TokenType::Minus,
          index: 2,
          value: "-".to_string(),
        },
        value_type: BasicValueType::Number,
        inner_value_type: None,
      },
      child: Box::new(AstNode::StringOperand(StringOperandNode {
        basic: BasicAstNodeData {
          token: Token {
            token_type: TokenType::String,
            index: 4,
            value: "\"foo\"".to_string(),
          },
          value_type: BasicValueType::String,
          inner_value_type: None,
        },
        value: "\"foo\"".to_string(),
      })),
    }), ast);
  }

  #[test]
  fn test_unary_operator_plus() {
    let ast = parse("  + \"foo\"").unwrap();
    assert_eq!(AstNode::UnaryOperator(UnaryOperatorNode {
      basic: BasicAstNodeData {
        token: Token {
          token_type: TokenType::Add,
          index: 2,
          value: "+".to_string(),
        },
        value_type: BasicValueType::String,
        inner_value_type: None,
      },
      child: Box::new(AstNode::StringOperand(StringOperandNode {
        basic: BasicAstNodeData {
          token: Token {
            token_type: TokenType::String,
            index: 4,
            value: "\"foo\"".to_string(),
          },
          value_type: BasicValueType::String,
          inner_value_type: None,
        },
        value: "\"foo\"".to_string(),
      })),
    }), ast);
  }

  #[test]
  fn test_unary_operator_nested() {
    let ast = parse("  +-!( +--\"foo\")").unwrap();
    assert_eq!(AstNode::UnaryOperator(
      UnaryOperatorNode {
        basic: BasicAstNodeData {
          token: Token {
            token_type: TokenType::Add,
            index: 2,
            value: "+".to_string(),
          },
          value_type: BasicValueType::Number,
          inner_value_type: None,
        },
        child: Box::new(AstNode::UnaryOperator(
          UnaryOperatorNode {
            basic: BasicAstNodeData {
              token: Token {
                token_type: TokenType::Minus,
                index: 3,
                value: "-".to_string(),
              },
              value_type: BasicValueType::Number,
              inner_value_type: None,
            },
            child: Box::new(AstNode::UnaryOperator(
              UnaryOperatorNode {
                basic: BasicAstNodeData {
                  token: Token {
                    token_type: TokenType::Not,
                    index: 4,
                    value: "!".to_string(),
                  },
                  value_type: BasicValueType::Boolean,
                  inner_value_type: None,
                },
                child: Box::new(AstNode::UnaryOperator(
                  UnaryOperatorNode {
                    basic: BasicAstNodeData {
                      token: Token {
                        token_type: TokenType::Add,
                        index: 7,
                        value: "+".to_string(),
                      },
                      value_type: BasicValueType::Number,
                      inner_value_type: None,
                    },
                    child: Box::new(AstNode::UnaryOperator(
                      UnaryOperatorNode {
                        basic: BasicAstNodeData {
                          token: Token {
                            token_type: TokenType::Minus,
                            index: 8,
                            value: "-".to_string(),
                          },
                          value_type: BasicValueType::Number,
                          inner_value_type: None,
                        },
                        child: Box::new(AstNode::UnaryOperator(
                          UnaryOperatorNode {
                            basic: BasicAstNodeData {
                              token: Token {
                                token_type: TokenType::Minus,
                                index: 9,
                                value: "-".to_string(),
                              },
                              value_type: BasicValueType::Number,
                              inner_value_type: None,
                            },
                            child: Box::new(AstNode::StringOperand(StringOperandNode {
                              basic: BasicAstNodeData {
                                token: Token {
                                  token_type: TokenType::String,
                                  index: 10,
                                  value: "\"foo\"".to_string(),
                                },
                                value_type: BasicValueType::String,
                                inner_value_type: None,
                              },
                              value: "\"foo\"".to_string(),
                            })),
                          }
                        )),
                      }
                    )),
                  }
                )),
              }
            )),
          }
        )),
      }
    ), ast);
  }
}
