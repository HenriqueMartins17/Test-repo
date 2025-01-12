use std::rc::Rc;

use regex::Regex;

use crate::fields::property::field_types::BasicValueType;
use crate::formula::consts::ROLLUP_KEY_WORDS;
use crate::formula::errors::{Error, FormulaResult};
use crate::formula::i18n::Strings;
use crate::formula::lexer::token::{Token, TokenType};
use crate::formula::parser::parser::Context;
use crate::formula::types::IField;
use crate::params_i18n;

#[derive(Debug, PartialEq)]
pub struct BasicAstNodeData {
  pub token: Token,
  pub value_type: BasicValueType,
  pub inner_value_type: Option<BasicValueType>,
}

impl BasicAstNodeData {
  pub fn new(token: Token, value_type: BasicValueType) -> Self {
    Self {
      token,
      value_type,
      inner_value_type: None,
    }
  }
}

#[derive(Debug, PartialEq)]
pub enum AstNode {
  BinaryOperator(BinaryOperatorNode),
  UnaryOperator(UnaryOperatorNode),
  CallOperand(CallOperandNode),
  StringOperand(StringOperandNode),
  NumberOperand(NumberOperandNode),
  ValueOperand(ValueOperandNode),
  PureValueOperand(PureValueOperandNode),
}

impl AstNode {
  pub fn get_token(&self) -> &Token {
    self.with_basic(|basic| &basic.token)
  }

  pub fn get_value_type(&self) -> &BasicValueType {
    self.with_basic(|basic| &basic.value_type)
  }

  pub fn get_inner_value_type(&self) -> &Option<BasicValueType> {
    self.with_basic(|basic| &basic.inner_value_type)
  }

  fn with_basic<'a, R, F>(&'a self, f: F) -> R
  where
    F: Fn(&'a BasicAstNodeData) -> R,
    R: 'a,
  {
    match self {
      Self::BinaryOperator(node) => f(&node.basic),
      Self::UnaryOperator(node) => f(&node.basic),
      Self::CallOperand(node) => f(&node.basic),
      Self::StringOperand(node) => f(&node.basic),
      Self::NumberOperand(node) => f(&node.basic),
      Self::ValueOperand(node) => f(&node.basic.basic),
      Self::PureValueOperand(node) => f(&node.basic.basic),
    }
  }

  #[allow(dead_code)]
  pub fn num_nodes(&self) -> isize {
    match self {
      Self::BinaryOperator(node) => node.num_nodes(),
      Self::UnaryOperator(node) => node.num_nodes(),
      Self::CallOperand(node) => node.num_nodes(),
      Self::StringOperand(_) => 1,
      Self::NumberOperand(_) => 1,
      Self::ValueOperand(_) => 1,
      Self::PureValueOperand(_) => 1,
    }
  }

  pub fn try_as_value_operand_node(&self) -> Option<&dyn IValueOperandNode> {
    match self {
      Self::ValueOperand(node) => Some(node as &dyn IValueOperandNode),
      Self::PureValueOperand(node) => Some(node as &dyn IValueOperandNode),
      _ => None,
    }
  }

  pub fn get_value(&self) -> Option<String> {
    match self {
      Self::BinaryOperator(_) => None,
      Self::UnaryOperator(_) => None,
      Self::CallOperand(_) => None,
      Self::StringOperand(node) => Some(node.value.clone()),
      Self::NumberOperand(node) => Some(node.value.clone()),
      Self::ValueOperand(node) => Some(node.basic.value.clone()),
      Self::PureValueOperand(node) => Some(node.basic.value.clone()),
    }
  }
}

#[derive(Debug, PartialEq)]
pub struct BinaryOperatorNode {
  pub basic: BasicAstNodeData,
  pub left: Box<AstNode>,
  pub right: Box<AstNode>,
}

impl BinaryOperatorNode {
  pub fn new(left: AstNode, token: Token, right: AstNode) -> Self {
    match token.token_type {
      TokenType::Add => {
        let is_number_type = |node: &AstNode| {
          node.get_value_type() == &BasicValueType::Number
            || node
              .get_inner_value_type()
              .as_ref()
              .map_or(false, |v| v == &BasicValueType::Number)
            || node.get_token().value.to_uppercase() == "BLANK"
        };
        if is_number_type(&left) && is_number_type(&right) {
          return Self {
            basic: BasicAstNodeData::new(token, BasicValueType::Number),
            left: Box::new(left),
            right: Box::new(right),
          };
        }

        return Self {
          basic: BasicAstNodeData::new(token, BasicValueType::String),
          left: Box::new(left),
          right: Box::new(right),
        };
      }
      TokenType::Minus | TokenType::Times | TokenType::Mod | TokenType::Div => Self {
        basic: BasicAstNodeData::new(token, BasicValueType::Number),
        left: Box::new(left),
        right: Box::new(right),
      },
      TokenType::Or
      | TokenType::And
      | TokenType::Equal
      | TokenType::NotEqual
      | TokenType::Greater
      | TokenType::GreaterEqual
      | TokenType::Less
      | TokenType::LessEqual => Self {
        basic: BasicAstNodeData::new(token, BasicValueType::Boolean),
        left: Box::new(left),
        right: Box::new(right),
      },
      TokenType::Concat => Self {
        basic: BasicAstNodeData::new(token, BasicValueType::String),
        left: Box::new(left),
        right: Box::new(right),
      },
      _ => panic!("Unexpected token: {:?}", token),
    }
  }

  pub fn num_nodes(&self) -> isize {
    1 + self.left.num_nodes() + self.right.num_nodes()
  }
}

#[derive(Debug, PartialEq)]
pub struct UnaryOperatorNode {
  pub basic: BasicAstNodeData,
  pub child: Box<AstNode>,
}

impl UnaryOperatorNode {
  pub fn new(child: AstNode, token: Token) -> Self {
    match token.token_type {
      TokenType::Minus => Self {
        basic: BasicAstNodeData::new(token, BasicValueType::Number),
        child: Box::new(child),
      },
      TokenType::Not => Self {
        basic: BasicAstNodeData::new(token, BasicValueType::Boolean),
        child: Box::new(child),
      },
      TokenType::Add => Self {
        basic: BasicAstNodeData::new(token, child.get_value_type().clone()),
        child: Box::new(child),
      },
      _ => panic!("Unexpected token: {:?}", token),
    }
  }

  pub fn num_nodes(&self) -> isize {
    1 + self.child.num_nodes()
  }
}

#[derive(Debug, PartialEq)]
pub struct CallOperandNode {
  pub basic: BasicAstNodeData,
  pub value: String,
  pub params: Vec<AstNode>,
}

impl CallOperandNode {
  pub fn new(token: Token) -> Self {
    let value = token.value.clone();
    Self {
      basic: BasicAstNodeData::new(token, BasicValueType::String),
      value,
      params: vec![],
    }
  }

  pub fn num_nodes(&self) -> isize {
    1 + self.params.iter().map(|node| node.num_nodes()).sum::<isize>()
  }
}

#[derive(Debug, PartialEq)]
pub struct NumberOperandNode {
  pub basic: BasicAstNodeData,
  pub value: String,
}

impl NumberOperandNode {
  pub fn new(token: Token) -> Self {
    let value = token.value.clone();
    Self {
      basic: BasicAstNodeData::new(token, BasicValueType::Number),
      value,
    }
  }
}

#[derive(Debug, PartialEq)]
pub struct StringOperandNode {
  pub basic: BasicAstNodeData,
  pub value: String,
}

impl StringOperandNode {
  pub fn new(token: Token) -> Self {
    let token_value = token.value.clone();
    let terminator_map = vec![
      (Regex::new(r"\\n").unwrap(), "\n"),
      (Regex::new(r"\\r").unwrap(), "\r"),
      (Regex::new(r"\\t").unwrap(), "\t"),
    ];

    let mut value = token_value.clone();
    for (regex, replacement) in terminator_map {
      value = regex.replace_all(&value, replacement).to_string();
    }
    value = Regex::new(r"\\(.?)").unwrap().replace_all(&value, "$1").to_string();

    Self {
      basic: BasicAstNodeData::new(token, BasicValueType::String),
      value,
    }
  }
}

#[derive(Debug, PartialEq)]
pub struct BaseValueOperandNode {
  pub basic: BasicAstNodeData,
  pub value: String,
  pub field: Rc<IField>,
  pub context: Rc<Context>,
}

impl BaseValueOperandNode {
  pub fn new(
    token: Token,
    field_id: String,
    context: Rc<Context>,
    host_field: Option<Rc<IField>>,
  ) -> FormulaResult<Self> {
    let value = Regex::new(r"\\(.?)")
      .unwrap()
      .replace_all(&token.value, "$1")
      .to_string();

    let field_id = Regex::new(r"\\(.?)").unwrap().replace_all(&field_id, "$1").to_string();

    let field;
    let value_type;
    if field_id == ROLLUP_KEY_WORDS && host_field.is_some() {
      field = host_field.unwrap();
      value_type = BasicValueType::Array;
    } else {
      field = context
        .field_map
        .get(&field_id)
        .ok_or_else(|| Error::new(Strings::FunctionErrInvalidFieldName.with_params(params_i18n!(field_id = field_id))))
        .cloned()?;

      value_type = IField::bind_context(field.clone(), context.state.clone()).basic_value_type();
    }

    let basic = {
      let inner_value_type = IField::bind_context(field.clone(), context.state.clone()).inner_basic_value_type();

      let mut basic = BasicAstNodeData::new(token, value_type);
      basic.inner_value_type = Some(inner_value_type);
      basic
    };

    Ok(Self {
      context,
      field,
      basic,
      value,
    })
  }
}

pub trait IValueOperandNode {
  fn get_field(&self) -> Rc<IField>;
  fn get_inner_value_type(&self) -> &Option<BasicValueType>;
  fn get_context(&self) -> Rc<Context>;
}

#[derive(Debug, PartialEq)]
pub struct ValueOperandNode {
  pub basic: BaseValueOperandNode,
}

impl ValueOperandNode {
  pub fn new(token: Token, context: Rc<Context>, host_field: Option<Rc<IField>>) -> FormulaResult<ValueOperandNode> {
    let field_id = token.value[1..token.value.len() - 1].to_string();
    let basic = BaseValueOperandNode::new(token, field_id, context, host_field)?;

    Ok(Self { basic })
  }
}

impl IValueOperandNode for ValueOperandNode {
  fn get_field(&self) -> Rc<IField> {
    self.basic.field.clone()
  }

  fn get_inner_value_type(&self) -> &Option<BasicValueType> {
    &self.basic.basic.inner_value_type
  }

  fn get_context(&self) -> Rc<Context> {
    self.basic.context.clone()
  }
}

#[derive(Debug, PartialEq)]
pub struct PureValueOperandNode {
  pub basic: BaseValueOperandNode,
}

impl PureValueOperandNode {
  pub fn new(
    token: Token,
    context: Rc<Context>,
    host_field: Option<Rc<IField>>,
  ) -> FormulaResult<PureValueOperandNode> {
    let field_id = token.value.clone();
    let basic = BaseValueOperandNode::new(token, field_id, context, host_field)?;

    Ok(Self { basic })
  }
}

impl IValueOperandNode for PureValueOperandNode {
  fn get_field(&self) -> Rc<IField> {
    self.basic.field.clone()
  }

  fn get_inner_value_type(&self) -> &Option<BasicValueType> {
    &self.basic.basic.inner_value_type
  }

  fn get_context(&self) -> Rc<Context> {
    self.basic.context.clone()
  }
}
