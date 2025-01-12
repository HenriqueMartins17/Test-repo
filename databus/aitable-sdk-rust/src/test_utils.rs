#[cfg(test)]
pub mod utils {
  use std::collections::HashMap;
  use std::env;

  use crate::apitable::{ApiTable, ApiTableConfig};
  use crate::datasheet::datasheet::Datasheet;
  use crate::datasheet::field::FieldManager;
  use crate::datasheet::record::RecordManager;
  use crate::datasheet::view::ViewManager;

  pub struct FieldName {}

  impl FieldName {
    pub const SINGLE_LINE_TEXT: &'static str = "Single line text";
    pub const LONG_TEXT: &'static str = "Long text";
    pub const SELECT: &'static str = "Select";
    pub const MULTI_SELECT: &'static str = "Multi-select";
    pub const NUMBER: &'static str = "Number";
    pub const CURRENCY: &'static str = "Currency";
    pub const PERCENT: &'static str = "Percent";
    pub const DATE_YMD: &'static str = "Date Y/M/D";
    pub const ATTACHMENT: &'static str = "Attachment";
    pub const MEMBER: &'static str = "Member";
    pub const CHECKBOX: &'static str = "Checkbox";
    pub const RATING: &'static str = "Rating";
    pub const URL: &'static str = "URL";
    pub const PHONE: &'static str = "Phone";
    pub const EMAIL: &'static str = "Email";
    pub const ONE_WAY_LINK: &'static str = "One-way Link";
    pub const TWO_WAY_LINK: &'static str = "Two-way Link";
    pub const LOOKUP: &'static str = "Lookup";
    pub const FORMULA: &'static str = "Formula";
    pub const AUTONUMBER: &'static str = "Autonumber";
    pub const CASCADER: &'static str = "Cascader";
    pub const CREATED_TIME: &'static str = "Created time";
    pub const LAST_EDITED_TIME: &'static str = "Last edited time";
    pub const CREATED_BY: &'static str = "Created by";
    pub const LAST_EDITED_BY: &'static str = "Last edited by";
    // TODO: add DOCUMENT field
  }

  #[derive(Eq, Hash, PartialEq)]
  pub enum DstId {
    Default,
    FullFieldsDstId,
  }

  struct TestEnv {
    v1_host: String,
    v3_host: String,
    token: String,
    // datasheet id map
    dst_id_map: HashMap<DstId, String>,
  }

  impl TestEnv {
    fn get() -> Self {
      let default_v1_host = "https://integration.vika.ltd".to_string();
      let default_v3_host = "https://integration.vika.ltd".to_string();

      let default_token = "uskvYtIY3W40b1osWIXiUuT".to_string();

      let default_dst_id = "dstY9JTFxroH355hKq".to_string();
      let default_full_fields_dst_id = "dstlcq0nVRdboY0sCp".to_string();


      // init datasheet id map
      let mut dst_id_map = HashMap::new();
      dst_id_map.insert(DstId::Default,
                        env::var("DATASHEET_ID")
                            .unwrap_or(default_dst_id));
      dst_id_map.insert(DstId::FullFieldsDstId,
                        env::var("FULL_FIELDS_DATASHEET_ID")
                            .unwrap_or(default_full_fields_dst_id));

      Self {
        v1_host: env::var("V1_HOST").unwrap_or(default_v1_host),
        v3_host: env::var("V3_HOST").unwrap_or(default_v3_host),
        token: env::var("TOKEN").unwrap_or(default_token),
        dst_id_map,
      }
    }

    fn get_dst_id(&self, dst_id: DstId) -> String {
      self.dst_id_map.get(&dst_id).unwrap().clone()
    }

    fn to_v1_config(&self) -> ApiTableConfig {
      ApiTableConfig::new_with_api_version(
        self.v1_host.clone(),
        self.token.clone(),
        1)
    }

    fn to_v3_config(&self) -> ApiTableConfig {
      ApiTableConfig::new_with_api_version(
        self.v3_host.clone(),
        self.token.clone(),
        3)
    }
  }


  ///
  /// create managers
  ///

  pub fn create_datasheet_manager_v3() -> Datasheet {
    create_datasheet_manager(3, DstId::Default)
  }

  pub fn create_field_manager_v3() -> FieldManager {
    create_field_manager(3, DstId::Default)
  }

  pub fn create_record_manager_v1() -> RecordManager {
    create_record_manager(1, DstId::Default)
  }

  pub fn create_record_manager_v3() -> RecordManager {
    create_record_manager(3, DstId::Default)
  }

  pub fn create_view_manager_v3() -> ViewManager {
    create_view_manager(3, DstId::Default)
  }


  /// datasheet manager
  pub fn create_datasheet_manager(version: i32, dst_id: DstId) -> Datasheet {
    let env = TestEnv::get();

    let config = if version == 1 {
      env.to_v1_config()
    } else {
      env.to_v3_config()
    };

    ApiTable::new(config)
        .datasheet(env.get_dst_id(dst_id))
  }

  /// field manager
  pub fn create_field_manager(version: i32, dst_id: DstId) -> FieldManager {
    create_datasheet_manager(version, dst_id)
        .fields()
  }

  /// record manager
  pub fn create_record_manager(version: i32, dst_id: DstId) -> RecordManager {
    create_datasheet_manager(version, dst_id)
        .records()
  }

  /// view manager
  pub fn create_view_manager(version: i32, dst_id: DstId) -> ViewManager {
    create_datasheet_manager(version, dst_id)
        .views()
  }
}
