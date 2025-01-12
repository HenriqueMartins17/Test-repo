#![recursion_limit = "256"]

pub mod consts;
pub mod errors;

pub mod logging;
// pub mod types;

#[macro_use]
pub mod macros;

pub mod http;

mod hashmap_ext;
mod result_ext;

pub mod container;
pub mod option_ext;
pub mod slice_ext;
pub mod string_ext_sql;

#[cfg(test)]
use rstest_reuse;

pub mod json_ext;
pub mod prelude {
  pub use crate::consts::*;
  pub use crate::container::*;
  pub use crate::hashmap_ext::*;
  pub use crate::http::*;
  pub use crate::json_ext::*;
  pub use crate::option_ext::*;
  pub use crate::result_ext::*;
  pub use crate::slice_ext::*;
  pub use crate::string_ext_sql::*;

  pub use crate::errors::*;
}
