#![recursion_limit = "256"]
use std::sync::Arc;

///
/// 这里lib的主入口，涉及到对外暴露的接口，请不要随意暴露接口，要得到大家的评审确认，避免对外接口爆炸和凌乱。
/// This is the main entry point of the lib, involving the exposed interfaces.
/// Please do not expose interfaces arbitrarily, and obtain the approval of everyone to avoid interface explosion and disorder.
///
/// Author: Kelly Peilin Chan <kelly@apitable.com>
///
// TODO, refactor types2.rs
// use types::*;

/// Will be private in the future.
// pub mod services;
// pub mod tablebundle;
// mod business;

// mod data_objects_manager;
/**
 * DataBundleManager, bindings functions all here.
 */
mod data_services_manager;
pub use data_services_manager::DataServicesManager;
// pub use data_objects_manager::DataObjectsManager;
pub use ot::DatasheetActions;
pub use ot::{ModifyViewOTO, ColumnWidthOTO, PayloadAddViewVO,PayloadMoveViewVO,PayloadDelViewVO,PayloadAddRecordVO, PayloadAddFieldVO,PayloadDelFieldVO};
pub use logic::datasheet::Datasheet;

mod logic;
pub mod shared;
pub mod config;

/// Will be private in the future.
// mod services;

#[macro_use]
use databus_shared;
// mod shared;
pub mod data_bundle;

pub mod data_source_provider;
use data_source_provider::IDataSourceProvider;

pub mod fields;
pub mod so;
pub mod vo;
pub mod ro;
pub mod dtos;
pub mod ot;
pub mod types;
pub mod utils;
// pub use shared::*;
// #[macro_use]
// extern crate napi_derive;

use databus_shared::logging;
// mod bo;
// pub use bo::space::Space;

// Unit models
// #[macro_use]
// pub mod dao;

// #[macro_use]
// extern crate databus_core;

// #[macro_use]
// extern crate napi_derive;

/**
 * Initialize function
 */
pub fn init(init_log: bool, is_dev_mode: bool, rest_base_url: String, loader: Arc<dyn IDataSourceProvider>) -> DataServicesManager {
  if init_log {
    logging::init(is_dev_mode);
  };
  println!("databus-core init done and return");
  return DataServicesManager::new(rest_base_url, loader);
}

// #[cfg(test)]
// mod test_data_objects_manager;

#[cfg(test)]
pub mod mock;

#[cfg(test)]
mod test_get_records;
#[cfg(test)]
mod test_add_record_to_action;
#[cfg(test)]
mod test_set_records_cmd;
#[cfg(test)]
mod test_set_records_cmd_basic;
#[cfg(test)]
mod test_set_records_cmd_advanced;
#[cfg(test)]
mod test_datasheet;
#[cfg(test)]
mod test_command_manager;
#[cfg(test)]
mod test_databus;
#[cfg(test)]
mod test_field;
#[cfg(test)]
mod test_record;
#[cfg(test)]
mod test_view;
#[cfg(test)]
mod test_changesets;

// #[cfg(test)]
// mod test_use;

pub mod prelude {
  pub use crate::data_source_provider::*;
  // pub use crate::so::view::{ViewColumnSO, ViewSO, ViewRowSO};
  pub use crate::so::*;
  pub use crate::vo::*;
  // pub use crate::shared::types::*;
  // pub use crate::shared::*;
}

pub mod formula;
pub mod compute_manager;
pub mod transformer;
pub mod filter;
pub mod modules;
