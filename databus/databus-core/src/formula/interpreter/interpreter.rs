use std::rc::Rc;
use std::str::FromStr;

use crate::fields::property::field_types::{BasicValueType, FormulaFuncType};
use crate::formula::errors::{Error, FormulaResult};
use crate::formula::functions::basic::{get_black_value_by_type, FormulaEvaluateContext, FormulaParam};
use crate::formula::functions::FunctionProvider;
use crate::formula::i18n::Strings;
use crate::formula::lexer::token::TokenType;
use crate::formula::parser::ast::{
  AstNode, BinaryOperatorNode, CallOperandNode, NumberOperandNode, PureValueOperandNode, StringOperandNode,
  UnaryOperatorNode, ValueOperandNode,
};
use crate::formula::types::IField;
use crate::params_i18n;
use crate::prelude::CellValue;

pub type ResolverFunction = Box<dyn Fn(String, bool) -> FormulaResult<CellValue>>;

pub struct Interpreter {
  pub resolver: ResolverFunction,
  pub context: Rc<FormulaEvaluateContext>,
  pub function_provider: FunctionProvider,
}

impl Interpreter {
  pub fn new(resolver: ResolverFunction, context: Rc<FormulaEvaluateContext>) -> Self {
    Self {
      resolver,
      context,
      function_provider: FunctionProvider::new(),
    }
  }

  fn transform_node_value(&self, node: &AstNode, value: CellValue, token_type: TokenType) -> FormulaResult<CellValue> {
    // Nodes of field value types need to perform dedicated string conversion logic
    if let Some(node) = node.try_as_value_operand_node() {
      let field = node.get_field();
      let field_basic_value_type = IField::bind_context(field.clone(), self.context.state.clone()).basic_value_type();

      // DateTime type is for comparison operators and needs to be compared in timestamp format,
      // @example {start time} = TODAY()
      if [BasicValueType::Number, BasicValueType::Boolean, BasicValueType::String].contains(&field_basic_value_type)
        || (field_basic_value_type == BasicValueType::DateTime
          && [
            TokenType::Equal,
            TokenType::NotEqual,
            TokenType::Less,
            TokenType::LessEqual,
            TokenType::Greater,
            TokenType::GreaterEqual,
          ]
          .contains(&token_type))
      {
        return Ok(value);
      }

      if field_basic_value_type == BasicValueType::Array {
        if &Some(BasicValueType::Number) == node.get_inner_value_type() {
          // directly take the first value for calculation
          if value.len() > 1 {
            return Err(Error::new("formula base error"));
          }

          let (_, first) = value.take_by_index(0);
          return Ok(first);
        }

        let s = IField::bind_context(field.clone(), self.context.state.clone()).array_value_to_string(value);
        return Ok(s.map(|s| CellValue::String(s)).unwrap_or(CellValue::Null));
      }

      let s = IField::bind_context(field.clone(), self.context.state.clone()).cell_value_to_string(value, None);
      return Ok(s.map(|s| CellValue::String(s)).unwrap_or(CellValue::Null));
    }

    Ok(value)
  }

  pub fn visit(&self, node: &AstNode, is_error_scope: bool) -> FormulaResult<CellValue> {
    return match node {
      AstNode::BinaryOperator(node) => self.visit_binary_operator_node(node),
      AstNode::UnaryOperator(node) => self.visit_unary_operator_node(node),
      AstNode::CallOperand(node) => self.visit_call_operator_node(node, is_error_scope),
      AstNode::StringOperand(node) => self.visit_string_operator_node(node),
      AstNode::NumberOperand(node) => self.visit_number_operator_node(node),
      AstNode::ValueOperand(node) => self.visit_value_operator_node(node, false),
      AstNode::PureValueOperand(node) => self.visit_pure_value_operator_node(node, false),
    };
  }

  fn visit_binary_operator_node(&self, node: &BinaryOperatorNode) -> FormulaResult<CellValue> {
    let token_type = &node.basic.token.token_type;
    let mut left = self.transform_node_value(&node.left, self.visit(&node.left, false)?, token_type.clone())?;
    let mut right = self.transform_node_value(&node.right, self.visit(&node.right, false)?, token_type.clone())?;

    // Perform value conversion processing on the BLANK function
    if node.left.get_token().value.to_uppercase() == "BLANK" {
      left = get_black_value_by_type(&node.right.get_value_type(), &right);
    }
    if node.right.get_token().value.to_uppercase() == "BLANK" {
      right = get_black_value_by_type(&node.left.get_value_type(), &left);
    }

    return match token_type {
      TokenType::And => Ok(CellValue::Bool(left.is_true() && right.is_true())),
      TokenType::Or => Ok(CellValue::Bool(left.is_true() || right.is_true())),
      TokenType::Add => {
        if left.is_null() {
          left = CellValue::Number(0.0)
        }
        if right.is_null() {
          right = CellValue::Number(0.0)
        }
        Ok(left + right)
      }
      TokenType::Minus => {
        if left.is_null() {
          left = CellValue::Number(0.0)
        }
        if right.is_null() {
          right = CellValue::Number(0.0)
        }
        Ok(left - right)
      }
      TokenType::Times => {
        if left.is_null() {
          left = CellValue::Number(0.0)
        }
        if right.is_null() {
          right = CellValue::Number(0.0)
        }
        Ok(left * right)
      }
      TokenType::Div => {
        if left.is_null() {
          left = CellValue::Number(0.0)
        }
        if right.is_null() {
          right = CellValue::Number(0.0)
        }
        Ok(left / right)
      }
      TokenType::Mod => {
        if left.is_null() {
          left = CellValue::Number(0.0)
        }
        if right.is_null() {
          right = CellValue::Number(0.0)
        }
        Ok(left % right)
      }
      TokenType::Equal => Ok(CellValue::Bool(left == right)),
      TokenType::NotEqual => Ok(CellValue::Bool(left != right)),
      TokenType::Greater => Ok(CellValue::Bool(left > right)),
      TokenType::GreaterEqual => Ok(CellValue::Bool(left >= right)),
      TokenType::Less => Ok(CellValue::Bool(left < right)),
      TokenType::LessEqual => Ok(CellValue::Bool(left <= right)),
      TokenType::Concat => {
        if left.is_null() {
          left = CellValue::String("".to_string());
        }
        if right.is_null() {
          right = CellValue::String("".to_string());
        }
        Ok(CellValue::String(left.to_string() + &right.to_string()))
      }

      _ => Ok(CellValue::Null),
    };
  }

  fn visit_unary_operator_node(&self, node: &UnaryOperatorNode) -> FormulaResult<CellValue> {
    return match &node.basic.token.token_type {
      TokenType::Not => Ok(CellValue::Bool(!self.visit(&node.child, false)?.is_true())),
      TokenType::Add => Ok(CellValue::Number(self.visit(&node.child, false)?.to_number())),
      TokenType::Minus => Ok(CellValue::Number(-self.visit(&node.child, false)?.to_number())),
      _ => Err(Error::new(format!(
        "Visitor can't process AST node type {:?}",
        node.basic.token.token_type
      ))),
    };
  }

  fn visit_value_operator_node(&self, node: &ValueOperandNode, origin_value: bool) -> FormulaResult<CellValue> {
    let value = &node.basic.value[1..node.basic.value.len() - 1];
    (self.resolver)(value.to_string(), origin_value)
  }

  fn visit_pure_value_operator_node(
    &self,
    node: &PureValueOperandNode,
    origin_value: bool,
  ) -> FormulaResult<CellValue> {
    (self.resolver)(node.basic.value.clone(), origin_value)
  }

  fn visit_string_operator_node(&self, node: &StringOperandNode) -> FormulaResult<CellValue> {
    let utf16_values: Vec<u16> = node.value.encode_utf16().collect();
    let value = utf16_values[1..utf16_values.len() - 1].to_vec();
    let result = match String::from_utf16(&value) {
      Ok(value) => value,
      Err(_) => return Err(Error::new("Invalid UTF-16 String")),
    };
    return Ok(CellValue::String(result));
  }

  fn visit_call_operator_node(&self, node: &CallOperandNode, is_error_scope: bool) -> FormulaResult<CellValue> {
    let mut is_error_scope = is_error_scope;

    let fn_name = node.value.to_uppercase();
    let fn_class = self.function_provider.get_function(&fn_name);
    if fn_class.is_none() {
      return Err(Error::new(
        Strings::FunctionErrNotFoundFunctionNameAs.with_params(params_i18n!(fn_name = fn_name)),
      ));
    }
    let fn_class = fn_class.unwrap();

    if fn_name == "ISERROR" || fn_name == "IS_ERROR" || fn_name == "IF" || fn_name == "SWITCH" {
      is_error_scope = true;
    }

    let params = node
      .params
      .iter()
      .map(|param| {
        let mut value = self.visit(&param, is_error_scope)?;
        let mut value_type = param.get_value_type();

        // Nodes of field value types need to perform dedicated string conversion logic
        if let Some(param) = param.try_as_value_operand_node() {
          // If the parameter type is Array && is not accepted by the function parameter type
          if !fn_class.func.get_accept_value_type().contains(value_type) && value_type == &BasicValueType::Array {
            let inner_value_type = param.get_inner_value_type();

            // When the built-in type of the array is Number and there is only one item, the corresponding operation can be performed.
            // If there are multiple items, in order to avoid user misunderstanding, take the form of direct error reporting
            if matches!(inner_value_type, Some(BasicValueType::Number)) {
              if value.len() > 1 {
                return Err(Error::new("formula base error"));
              }
              let (_, first) = value.take_by_index(0);
              value = first;
            } else {
              // The built-in type of the array is not Number, directly converted to a string
              match &value {
                CellValue::Array(arr) => {
                  let string = arr
                    .iter()
                    .filter(|v| !v.is_null())
                    .map(|v| v.to_string())
                    .collect::<Vec<String>>()
                    .join(", ");
                  value = CellValue::String(string);
                }
                _ => {}
              }
            }

            if let Some(inner_value_type) = inner_value_type {
              value_type = inner_value_type;
            }
          }

          // value value return rules (granularity is specific formula class):
          // 1. If acceptValueType does not contain valueType, it will be processed uniformly by the cellValueToString method;
          // 2. If you need to return the original value, you need to add the current valueType to acceptValueType;
          if !fn_class.func.get_accept_value_type().contains(value_type) {
            if let CellValue::String(s) = &value {
              let arr = s.split(", ").map(|s| CellValue::String(s.to_string())).collect();
              value = CellValue::Array(arr);
            }

            let string =
              IField::bind_context(param.get_field(), self.context.state.clone()).cell_value_to_string(value, None);
            value = CellValue::from(string.unwrap_or("".to_string()));
          }
        } else {
          // Convert array type value to string type. (In theory, only field values will have array types, so this will not take effect)
          if !fn_class.func.get_accept_value_type().contains(value_type) && value_type == &BasicValueType::Array {
            value = CellValue::String(value.to_string());
          }

          // Convert date type value to string type.
          if !fn_class.func.get_accept_value_type().contains(value_type) && value_type == &BasicValueType::DateTime {
            if value.is_null() {
              value = CellValue::Null;
            } else {
              value = CellValue::String(value.to_string());
            }
          }
        }

        return Ok(FormulaParam { node: param, value });
      })
      .collect::<FormulaResult<Vec<FormulaParam>>>()?;

    let node_params = node.params.iter().collect();
    fn_class.func.validate_params(&node_params)?;

    // pre-check for DateTime class functions
    // LAST_MODIFIED_TIME is calculated based on record.recordMeta without the value of other cells
    if fn_class.func.get_type() == FormulaFuncType::DateTime && fn_name != "LAST_MODIFIED_TIME" {
      match params.get(0) {
        Some(first_param) => {
          if first_param.value.is_null() {
            return Ok(CellValue::Null);
          }
        }
        _ => {}
      }
    }

    fn_class.func.func(&params, &self.context)
  }

  fn visit_number_operator_node(&self, node: &NumberOperandNode) -> FormulaResult<CellValue> {
    Ok(CellValue::Number(f64::from_str(&node.value).unwrap_or(0.0)))
  }
}
