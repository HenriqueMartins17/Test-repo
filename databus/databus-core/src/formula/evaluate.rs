use std::collections::HashMap;
use std::rc::Rc;

use regex::Regex;

use crate::fields::property::field_types::BasicValueType;
use crate::formula::errors::{Error, FormulaResult};
use crate::formula::functions::basic::FormulaEvaluateContext;
use crate::formula::i18n::Strings;
use crate::formula::interpreter::interpreter::{Interpreter, ResolverFunction};
use crate::formula::lexer::token::TokenType;
use crate::formula::lexer::FormulaExprLexer;
use crate::formula::parser::{AstNode, Context, FormulaExprParser};
use crate::formula::types::IField;
use crate::modules::database::store::selectors::resource::datasheet::cell_calc::get_cell_value;
use crate::params_i18n;
use crate::prelude::{CellValue, DatasheetSnapshotSO, FieldPermissionMap};

pub struct FormulaExpr {
  pub ast: AstNode,
}

fn resolver_wrapper(ctx: Rc<FormulaEvaluateContext>) -> ResolverFunction {
  let ctx = ctx.clone();

  // The role of the resolver is, given a fieldId, get the value from the context according to the string
  Box::new(
    move |field_id: String, _origin_value: bool| -> FormulaResult<CellValue> {
      // Check for reserved keywords when formulas are run in the lookUp field.
      // When the keyword is hit, return LookUpField's cellValueArray instead of cellValue
      let host_field = ctx.field.clone();
      let state = ctx.state.clone();

      // TODO: The first phase of column permissions,
      // because the middle layer does not actively push data,
      // and getFieldMap actively masks the data, so here we temporarily use the datasheet to get the fieldmap directly
      let datasheet = &state.datasheet_pack.snapshot;
      let field_map = &datasheet.meta.field_map;

      // TODO: lookup field
      if matches!(*host_field, IField::LookUp(_)) && field_id == "ROLLUP_KEY_WORDS" {
        todo!();
      }

      // TODO: permission check
      let field = field_map.get(&field_id);
      if field.is_none() {
        return Err(Error::new(
          Strings::ViewFieldSearchNotFoundTip.with_params(params_i18n!(value = field_id)),
        ));
      }
      // TODO: remove this clone
      let field = Rc::new(IField::from_so(field.unwrap().clone()));

      // FIXME: use get_cell_value()
      let record = ctx.record.clone();

      let record_snapshot = DatasheetSnapshotSO {
        meta: datasheet.meta.clone(),
        datasheet_id: datasheet.datasheet_id.clone(),
        record_map: vec![(record.id.clone(), (*record).clone())].into_iter().collect(),
      };
      let cell_value = get_cell_value(ctx.state.clone(), &record_snapshot, &record.id, &field_id);

      // TODO: what if field is undefined?
      // String type fields need special treatment. Convert Segment|id type to pure string;
      let field_basic_value_type = IField::bind_context(field.clone(), state.clone()).basic_value_type();
      // Currently "", [], false will be converted to null when getCellValue,
      // in order to ensure the correct calculation result of the formula, the boolean type needs to be converted null => false
      if field_basic_value_type == BasicValueType::Boolean {
        // TODO: check cell_value is true
        return Ok(CellValue::Bool(cell_value.is_true()));
      }
      if field_basic_value_type == BasicValueType::String {
        let string = IField::bind_context(field.clone(), state.clone()).cell_value_to_string(cell_value, None);
        let value = string.map(|it| CellValue::String(it)).unwrap_or(CellValue::Null);
        return Ok(value);
      }
      if field_basic_value_type == BasicValueType::Array {
        let value = IField::bind_context(field.clone(), state.clone()).cell_value_to_array(cell_value);
        return Ok(value);
      }

      return Ok(cell_value);
    },
  )
}

/// TODO: add cache
pub fn parse(expression: String, ctx: Rc<Context>) -> FormulaResult<FormulaExpr> {
  if expression.trim().is_empty() {
    return Err(Error::new(Strings::FunctionContentEmpty));
  }

  let lexer = FormulaExprLexer::new(expression)?;
  if let Some(error) = lexer.errors.get(0) {
    return Err(error.clone());
  }

  let mut parser = FormulaExprParser::new(Box::new(lexer), ctx);
  let ast = parser.parse()?;

  return Ok(FormulaExpr { ast });
}

pub fn evaluate(expression: String, ctx: FormulaEvaluateContext) -> FormulaResult<CellValue> {
  let state = &ctx.state;

  // TODO: maybe replace
  let field_map = &state.datasheet_pack.snapshot.meta.field_map;
  let field_map = field_map
    .iter()
    .map(|(k, v)| (k.clone(), Rc::new(IField::from_so(v.clone()))))
    .collect();

  let context = Rc::new(Context {
    field: ctx.field.clone(),
    field_map,
    state: ctx.state.clone(),
  });
  let expr = parse(expression, context)?;

  let ctx = Rc::new(ctx);
  let resolver = resolver_wrapper(ctx.clone());
  let interpreter = Interpreter::new(resolver, ctx);

  let result = interpreter.visit(&expr.ast, false)?;

  if result.is_number() {
    let num = result.to_number();
    if num.is_nan() || num.is_infinite() {
      return Err(Error::new("NaN"));
    }
  }

  Ok(result)
}

pub fn expression_transform(
  expression: &String,
  field_map: &HashMap<String, Rc<IField>>,
  _field_permission_map: &FieldPermissionMap,
  to: ExpressionTransformTarget,
) -> FormulaResult<String> {
  if expression.trim().is_empty() {
    return Ok(expression.to_string());
  }

  let result = FormulaExprLexer::new(expression.clone());
  if result.is_err() {
    return Ok("".to_string());
  }
  let lexer = result.unwrap();

  // Convert fieldMap and use name as key
  let mut revert_field_map = field_map
    .iter()
    .map(|(key, value)| (key.clone(), value.clone()))
    .collect::<HashMap<String, Rc<IField>>>();
  if to == ExpressionTransformTarget::Id {
    revert_field_map = revert_field_map
      .into_iter()
      .map(|(_, field)| (field.get_name().to_string(), field))
      .collect();
  }

  return Ok(lexer.full_matches.iter().fold(String::new(), |str, token| {
    let mut token_value = token.value.clone();

    let get_pure_token_value = || -> String {
      let re_escape = Regex::new(r"\\(.)").unwrap();
      let pure_token_value = re_escape.replace_all(&token_value, "$1").to_string();

      return match revert_field_map.get(&pure_token_value) {
        Some(field) => {
          if to == ExpressionTransformTarget::Id {
            field.get_id().to_string()
          } else {
            let re_special_chars = Regex::new(r"[{}\\]").unwrap();
            let name = re_special_chars.replace_all(field.get_name(), "\\$0").to_string();

            let re_illegal_chars = Regex::new(r#"[/+\-|=*/><()（）!&%'"“”‘’^`~,，\s]"#).unwrap();
            if re_illegal_chars.is_match(&name) {
              format!("{{{}}}", name)
            } else {
              // Implement logging here, Rust does not have a direct equivalent of `console.log`
              println!("Field not found for the name: {}", pure_token_value); // Placeholder for logging
              name
            }
          }
        }
        None => token_value.to_string(),
      };
    };

    let get_token_value = || -> String {
      let re_escape = Regex::new(r"\\(.)").unwrap();
      let pure_token_value = re_escape
        .replace_all(&token_value[1..token_value.len() - 1], "$1")
        .to_string();

      // TODO: permission check

      return if let Some(field) = revert_field_map.get(&pure_token_value) {
        let formatted_field = match to {
          ExpressionTransformTarget::Id => format!("{{{}}}", field.get_id()),
          ExpressionTransformTarget::Name => {
            let re_special_chars = Regex::new(r"[{}\\]").unwrap();
            let name = re_special_chars.replace_all(field.get_name(), "\\$0").to_string();
            format!("{{{}}}", name)
          }
        };
        formatted_field
      } else {
        println!("Field not found for the name: {}", pure_token_value); // Placeholder for logging
        token_value.to_string()
      };
    };

    if token.token_type == TokenType::PureValue {
      token_value = get_pure_token_value();
    } else if token.token_type == TokenType::Value {
      token_value = get_token_value();
    }

    return str + token_value.as_str();
  }));
}

#[derive(PartialEq)]
pub enum ExpressionTransformTarget {
  Id,
  Name,
}

#[cfg(test)]
mod tests {
  use std::collections::HashMap;
  use std::rc::Rc;

  use crate::fields::property::{FormulaFieldPropertySO, NumberFieldPropertySO};
  use crate::formula::evaluate::{expression_transform, ExpressionTransformTarget};
  use crate::formula::helper::tests::test_assert_result;
  use crate::formula::types::{IBaseField, IField};
  use crate::prelude::CellValue;

  fn field_map() -> HashMap<String, IField> {
    vec![
      (
        "a".to_string(),
        IField::Number(IBaseField {
          id: "a".to_string(),
          name: "a".to_string(),
          desc: None,
          required: None,
          property: NumberFieldPropertySO {
            precision: 0,
            default_value: None,
            comma_style: None,
            symbol: None,
            symbol_align: None,
          },
        }),
      ),
      (
        "b".to_string(),
        IField::Text(IBaseField {
          id: "b".to_string(),
          name: "b".to_string(),
          desc: None,
          required: None,
          property: (),
        }),
      ),
      (
        "b{".to_string(),
        IField::Number(IBaseField {
          id: "b{".to_string(),
          name: "b{".to_string(),
          desc: None,
          required: None,
          property: NumberFieldPropertySO {
            precision: 0,
            default_value: None,
            comma_style: None,
            symbol: None,
            symbol_align: None,
          },
        }),
      ),
      (
        "c".to_string(),
        IField::Number(IBaseField {
          id: "c".to_string(),
          name: "c".to_string(),
          desc: None,
          required: None,
          property: NumberFieldPropertySO {
            precision: 1,
            default_value: None,
            comma_style: None,
            symbol: None,
            symbol_align: None,
          },
        }),
      ),
      (
        "d".to_string(),
        IField::Number(IBaseField {
          id: "d".to_string(),
          name: "d".to_string(),
          desc: None,
          required: None,
          property: NumberFieldPropertySO {
            precision: 1,
            default_value: None,
            comma_style: None,
            symbol: None,
            symbol_align: None,
          },
        }),
      ),
      (
        "x".to_string(),
        IField::Formula(IBaseField {
          id: "x".to_string(),
          name: "x".to_string(),
          desc: None,
          required: None,
          property: FormulaFieldPropertySO {
            datasheet_id: "dst123".to_string(),
            expression: "".to_string(),
            formatting: None,
          },
        }),
      ),
    ]
    .into_iter()
    .collect()
  }

  fn assert_result(expected: CellValue, expression: &str, record_data: &HashMap<String, CellValue>) {
    test_assert_result(expected, expression, record_data, &field_map(), None);
  }

  #[test]
  fn test_formula_evaluate_mix_1_or_2_operator() {
    let record_data = vec![("a".to_string(), CellValue::Number(0.0))].into_iter().collect();

    assert_result(CellValue::Number(-2.0), "-1-1", &record_data);
    assert_result(CellValue::Number(-3.0), "{a}-1 - 2", &record_data);
    assert_result(CellValue::Number(-2.0), "-2 - 2 - -2", &record_data);
    assert_result(CellValue::Number(6.0), "+2 + 2 + +2", &record_data);
  }

  #[test]
  fn test_formula_evaluate_expression_escape() {
    let record_data = vec![
      ("b".to_string(), CellValue::from("456")),
      ("b{".to_string(), CellValue::from(456.0)),
    ]
    .into_iter()
    .collect();

    assert_result(
      CellValue::String("”456”".to_string()),
      r#"“\”” & 456 & “\””"#,
      &record_data,
    );
    assert_result(
      CellValue::String("”456”".to_string()),
      r#"“\”” & {b} & “\””"#,
      &record_data,
    );
    assert_result(
      CellValue::String("”456”".to_string()),
      r#"“\”” & {b\{} & “\””"#,
      &record_data,
    );
  }

  #[test]
  fn test_formula_evaluate_expression_priority() {
    let record_data = vec![
      ("a".to_string(), CellValue::from(1.0)),
      ("b".to_string(), CellValue::from("456")),
      ("c".to_string(), CellValue::from(2.0)),
      ("d".to_string(), CellValue::from(3.0)),
    ]
    .into_iter()
    .collect();

    assert_result(CellValue::Number(7.0), "1 + 2 * 3", &record_data);
    assert_result(CellValue::Number(9.0), "1 + (1 + 3) * 2", &record_data);
    assert_result(CellValue::Number(0.0), "2 * (2 + 3) - 10", &record_data);
    assert_result(
      CellValue::String("Courses平均成绩=2".to_string()),
      "'Courses平均成绩' & '=' & ({a}+{c}+{d}) / 3",
      &record_data,
    );
    assert_result(
      CellValue::Number(10.5),
      "1 + 2 + 3 + 5 - 1 * 2 * 3 / 4 % 5 * 323 % 1",
      &record_data,
    );
    assert_result(CellValue::Number(15.0), "1 * 2 * 3 + 4 + 5", &record_data);
    assert_result(CellValue::Number(5.0), "IF(1 > 2, 3, 5)", &record_data);
    assert_result(CellValue::Number(5.0), "IF（1 > 2， 3， 5）", &record_data);
    assert_result(CellValue::Number(25.0), "1 + 2 * 3 * 4", &record_data);
    assert_result(CellValue::Number(5.0), "1 + 2 * 3 - 4 * 5 % 6", &record_data);
    // TODO: different from TS
    assert_result(CellValue::Number(6.0), "1 + {c} * 3 - 4 * {a} % 3", &record_data);
    assert_result(
      CellValue::String("5123".to_string()),
      r#"(1 + 2 * 3 - 4 * 5 % 6) & "123""#,
      &record_data,
    );
    assert_result(CellValue::Number(3.0), "1 + 2 * 3 - 4", &record_data);
    assert_result(
      CellValue::String("25x".to_string()),
      r#"1 + 2 * 3 * 4 + "x""#,
      &record_data,
    );
    assert_result(
      CellValue::String("25x".to_string()),
      r#"1 + 2 * 3 * 4 + “x”"#,
      &record_data,
    );
    assert_result(
      CellValue::String("6123".to_string()),
      r#"5 + 1 % 10 + "123""#,
      &record_data,
    );
    assert_result(
      CellValue::String("5123".to_string()),
      r#"1 + 2 * 3 - 4 * 5 % 6 & "123""#,
      &record_data,
    );
  }

  #[test]
  fn test_formula_transform_expression() {
    let field_map = vec![
      (
        "fld11111".to_string(),
        Rc::new(IField::Number(IBaseField {
          id: "fld11111".to_string(),
          name: "a".to_string(),
          desc: None,
          required: None,
          property: NumberFieldPropertySO {
            precision: 0,
            default_value: None,
            comma_style: None,
            symbol: None,
            symbol_align: None,
          },
        })),
      ),
      (
        "fld22222".to_string(),
        Rc::new(IField::Text(IBaseField {
          id: "fld22222".to_string(),
          name: "b".to_string(),
          desc: None,
          required: None,
          property: (),
        })),
      ),
      (
        "fld33333".to_string(),
        Rc::new(IField::Number(IBaseField {
          id: "fld33333".to_string(),
          name: "c".to_string(),
          desc: None,
          required: None,
          property: NumberFieldPropertySO {
            precision: 1,
            default_value: None,
            comma_style: None,
            symbol: None,
            symbol_align: None,
          },
        })),
      ),
      (
        "fld44444".to_string(),
        Rc::new(IField::Number(IBaseField {
          id: "fld44444".to_string(),
          name: "d{".to_string(),
          desc: None,
          required: None,
          property: NumberFieldPropertySO {
            precision: 1,
            default_value: None,
            comma_style: None,
            symbol: None,
            symbol_align: None,
          },
        })),
      ),
      (
        "fld55555".to_string(),
        Rc::new(IField::Number(IBaseField {
          id: "fld55555".to_string(),
          name: "{e".to_string(),
          desc: None,
          required: None,
          property: NumberFieldPropertySO {
            precision: 1,
            default_value: None,
            comma_style: None,
            symbol: None,
            symbol_align: None,
          },
        })),
      ),
      (
        "fld66666".to_string(),
        Rc::new(IField::Number(IBaseField {
          id: "fld66666".to_string(),
          name: r#"f" {}"#.to_string(),
          desc: None,
          required: None,
          property: NumberFieldPropertySO {
            precision: 1,
            default_value: None,
            comma_style: None,
            symbol: None,
            symbol_align: None,
          },
        })),
      ),
    ]
    .into_iter()
    .collect();

    let field_permission_map = HashMap::new();

    assert_eq!(
      r#"“\”” & {fld33333} & “\””"#,
      expression_transform(
        &r#"“\”” & {c} & “\””"#.to_string(),
        &field_map,
        &field_permission_map,
        ExpressionTransformTarget::Id,
      )
      .unwrap(),
    );
    assert_eq!(
      r#"{fld33333} + 1"#,
      expression_transform(
        &r#"{c} + 1"#.to_string(),
        &field_map,
        &field_permission_map,
        ExpressionTransformTarget::Id,
      )
      .unwrap(),
    );

    assert_eq!(
      r#"{fld44444} + 1"#,
      expression_transform(
        &r#"{d\{} + 1"#.to_string(),
        &field_map,
        &field_permission_map,
        ExpressionTransformTarget::Id,
      )
      .unwrap(),
    );
    assert_eq!(
      r#"{d\{} + 1"#,
      expression_transform(
        &r#"{fld44444} + 1"#.to_string(),
        &field_map,
        &field_permission_map,
        ExpressionTransformTarget::Name,
      )
      .unwrap(),
    );

    assert_eq!(
      r#"{fld44444} + 1"#,
      expression_transform(
        &r#"{d\{} + 1"#.to_string(),
        &field_map,
        &field_permission_map,
        ExpressionTransformTarget::Id,
      )
      .unwrap(),
    );
    assert_eq!(
      r#"{d\{} + 1"#,
      expression_transform(
        &r#"{fld44444} + 1"#.to_string(),
        &field_map,
        &field_permission_map,
        ExpressionTransformTarget::Name,
      )
      .unwrap(),
    );

    assert_eq!(
      r#"fld55555 + 1"#,
      expression_transform(
        &r#"\{e + 1"#.to_string(),
        &field_map,
        &field_permission_map,
        ExpressionTransformTarget::Id,
      )
      .unwrap(),
    );
    assert_eq!(
      r#"\{e + 1"#,
      expression_transform(
        &r#"fld55555 + 1"#.to_string(),
        &field_map,
        &field_permission_map,
        ExpressionTransformTarget::Name,
      )
      .unwrap(),
    );

    assert_eq!(
      r#"{fld66666} + 1"#,
      expression_transform(
        &r#"{f" \{\}} + 1"#.to_string(),
        &field_map,
        &field_permission_map,
        ExpressionTransformTarget::Id,
      )
      .unwrap(),
    );
    assert_eq!(
      r#"{f" \{\}} + 1"#,
      expression_transform(
        &r#"{fld66666} + 1"#.to_string(),
        &field_map,
        &field_permission_map,
        ExpressionTransformTarget::Name,
      )
      .unwrap(),
    );
    assert_eq!(
      r#"{f" \{\}} + 1"#,
      expression_transform(
        &r#"fld66666 + 1"#.to_string(),
        &field_map,
        &field_permission_map,
        ExpressionTransformTarget::Name,
      )
      .unwrap(),
    );

    assert_eq!(
      r#"fld33333 + 1"#,
      expression_transform(
        &r#"c + 1"#.to_string(),
        &field_map,
        &field_permission_map,
        ExpressionTransformTarget::Id,
      )
      .unwrap(),
    );
    assert_eq!(
      r#"{c} + 1"#,
      expression_transform(
        &r#"{fld33333} + 1"#.to_string(),
        &field_map,
        &field_permission_map,
        ExpressionTransformTarget::Name,
      )
      .unwrap(),
    );
    assert_eq!(
      r#"c + 1"#,
      expression_transform(
        &r#"fld33333 + 1"#.to_string(),
        &field_map,
        &field_permission_map,
        ExpressionTransformTarget::Name,
      )
      .unwrap(),
    );

    assert_eq!(
      r#"IF({fld33333}, SUM(1,2,3), 3)"#,
      expression_transform(
        &r#"IF({c}, SUM(1,2,3), 3)"#.to_string(),
        &field_map,
        &field_permission_map,
        ExpressionTransformTarget::Id,
      )
      .unwrap(),
    );
    assert_eq!(
      r#"IF(fld33333, SUM(1,2,3), 3)"#,
      expression_transform(
        &r#"IF(c, SUM(1,2,3), 3)"#.to_string(),
        &field_map,
        &field_permission_map,
        ExpressionTransformTarget::Id,
      )
      .unwrap(),
    );
    assert_eq!(
      r#"IF({c}, SUM(1,2,3), 3)"#,
      expression_transform(
        &r#"IF({fld33333}, SUM(1,2,3), 3)"#.to_string(),
        &field_map,
        &field_permission_map,
        ExpressionTransformTarget::Name,
      )
      .unwrap(),
    );
    assert_eq!(
      r#"IF(c, SUM(1,2,3), 3)"#,
      expression_transform(
        &r#"IF(fld33333, SUM(1,2,3), 3)"#.to_string(),
        &field_map,
        &field_permission_map,
        ExpressionTransformTarget::Name,
      )
      .unwrap(),
    );
    // TODO: permission
    // assert_eq!(
    //   r#"{该列无权访问}"#,
    //   expression_transform(
    //     &r#"{fld66666}"#.to_string(),
    //     &field_map,
    //     &field_permission_map,
    //     ExpressionTransformTarget::Name,
    //   )
    //   .unwrap(),
    // );
  }
}
