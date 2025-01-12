mod client;

mod providers;
pub use providers::*;

mod indexdb_utils;
pub use indexdb_utils::*;

mod types;

pub mod prelude {
  pub use super::types::*;
}

mod api;
pub use api::*;
mod api_mock;

pub fn add(left: usize, right: usize) -> usize {
  left + right
}

#[cfg(test)]
mod tests {
  use super::*;

  #[test]
  fn it_works() {
    let result = add(2, 2);
    assert_eq!(result, 4);
  }
}
