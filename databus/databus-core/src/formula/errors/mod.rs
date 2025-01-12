mod params_count;
mod self_ref;
mod unit;

pub type FormulaResult<T> = Result<T, Error>;

#[derive(Debug, PartialEq, Clone)]
pub struct Error {
  pub message: String,
}

impl Error {
  pub fn new(message: impl ToString) -> Self {
    Self { message: message.to_string() }
  }
}
