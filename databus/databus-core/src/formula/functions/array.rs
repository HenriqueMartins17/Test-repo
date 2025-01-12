use crate::fields::field_factory::FieldFactory;
use crate::fields::property::field_types::BasicValueType;
use crate::formula::errors::{Error, FormulaResult};
use crate::formula::functions::basic::FormulaParam;
use crate::formula::i18n::Strings;
use crate::prelude::CellValue;

pub fn flatten_params(params: &Vec<FormulaParam>) -> FormulaResult<Vec<CellValue>> {
  let mut values = vec![];
  for param in params {
    if param.node.get_value_type() == &BasicValueType::Array {
      let (field, context) = match param.node.try_as_value_operand_node() {
        Some(param) => Ok((param.get_field(), param.get_context())),
        None => Err(Error::new(Strings::UnexpectedError)),
      }?;

      // TODO: remove the clone
      let field_so = (*field).clone().to_so();
      if FieldFactory::create_field(field_so, context.state.clone()).is_computed() {
        todo!();
      }
    }
    values.push(param.value.clone());
  }

  let mut flatten_values = vec![];
  for value in values {
    match value {
      CellValue::Array(arr) => {
        for item in arr {
          flatten_values.push(item);
        }
      }
      _ => flatten_values.push(value),
    }
  }

  Ok(flatten_values)
}
