use crate::prelude::{DatasheetPackSO, Json};

mod evaluator;
mod parser;
mod types;
mod evaluate;
pub use evaluate::*;

pub fn evaluate(formula: String, datasheet_pack: &DatasheetPackSO) -> anyhow::Result<Json> {
  let expr = parser::parse(formula)?;
  evaluator::evaluate(expr, datasheet_pack)
}
