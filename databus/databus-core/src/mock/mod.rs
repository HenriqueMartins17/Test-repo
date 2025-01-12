use crate::prelude::{DatasheetPackSO, IDataSourceProvider, SnapshotPackSO, BaseDatasheetPackSO};
use async_trait::async_trait;
use rand::Rng;

#[cfg(test)]
mod add_record_to_action_json;
#[cfg(test)]
mod mock_update_records_json;
#[cfg(test)]
mod mock_update_records_json_basic;
#[cfg(test)]
mod mock_update_records_json_advanced;
#[cfg(test)]
mod mock_datasheet_map;
#[cfg(test)]
pub mod mock_json;
#[cfg(test)]
pub mod mock_get_record_json;
#[cfg(test)]
pub mod mock_data_pack;
#[cfg(test)]
mod mock_func;
#[cfg(test)]
mod mock_data_storage_provider;
#[cfg(test)]
pub mod mock_daily_sheet_meta;

#[cfg(test)]
pub use mock_json::*;
#[cfg(test)]
pub use add_record_to_action_json::*;
#[cfg(test)]
pub use mock_get_record_json::*;
#[cfg(test)]
pub use mock_update_records_json::*;
#[cfg(test)]
pub use mock_update_records_json_basic::*;
#[cfg(test)]
pub use mock_update_records_json_advanced::*;
#[cfg(test)]
pub use mock_datasheet_map::*;
#[cfg(test)]
pub use mock_func::*;
#[cfg(test)]
pub use mock_data_storage_provider::*;
/**
 * Get Response Data to JSON
 */
#[cfg(test)]
pub fn get_response_data() -> Result<serde_json::Value, serde_json::Error> {
  serde_json::from_str(MOCK_DATASHEET_PACK_JSON)
}

#[cfg(test)]
pub fn get_datasheet_pack() -> anyhow::Result<DatasheetPackSO> {
  let response = get_response_data();
  let response_data = response.unwrap();
  let internal_data = response_data.get("data").unwrap();
  serde_json::from_value(internal_data.clone()).map_err(|e| anyhow::anyhow!(e))
}

#[cfg(test)]
pub fn get_snapshot_data(i:i32) -> Result<serde_json::Value, serde_json::Error> {
  let json = match i {
    1 => UPDATE_RECORDS_SNAP_SHOT_JSON,
    2 => UPDATE_RECORDS_SNAP_SHOT_JSON_BASIC,
    3 => UPDATE_RECORDS_SNAP_SHOT_JSON_ADVANCED,
    _ => ADD_RECORD_TO_ACTION_DATASHEET_PACK_JSON,
  };
  serde_json::from_str(json)
}

#[cfg(test)]
pub fn get_snapshot_pack(i:i32) -> anyhow::Result<SnapshotPackSO> {
  let response = get_snapshot_data(i);
  let response_data = response.unwrap();
  let internal_data = response_data.get("data").unwrap();
  serde_json::from_value(internal_data.clone()).map_err(|e| anyhow::anyhow!(e))
}

#[cfg(test)]
pub fn get_base_datasheet_data(i: i32) -> Result<serde_json::Value, serde_json::Error> {
  let json = match i {
    1 => MOCK_DATASHEET_MAP_JSON,
    2 => MOCK_DATASHEET_MAP_COMMAND_MANAGER_JSON,
    _ => MOCK_DATASHEET_MAP_JSON,
  };
  serde_json::from_str(json)
}

#[cfg(test)]
pub fn get_datasheet_map_pack(i: i32) -> anyhow::Result<BaseDatasheetPackSO> {
  let idx = if i == 1 { 1 } else { 2 };
  let response = get_base_datasheet_data(idx);
  let response_data = response.unwrap();
  let dst = format!("dst{}", (i-1).clone()); //dst1 dst2 dst3
  let str = if i <= 2 { "dst1" } else { dst.as_str() };
  let internal_data = response_data.get(str).unwrap();
  serde_json::from_value(internal_data.clone()).map_err(|e| anyhow::anyhow!(e))
}

#[cfg(test)]
mod tests {

  use std::collections::HashMap;

  use databus_shared::prelude::Json;
  use serde_json::Value;

  use crate::dtos::fusion_api_dtos::IAssetDTO;
pub use crate::mock::*;
  use crate::ot::SourceTypeEnum;
use crate::shared::{NodePermissionSO, AuthHeader, IUserInfo};
use crate::so::{RecordSO, RecordMeta, InternalSpaceUsageView, InternalSpaceSubscriptionView, FieldSO};
  use crate::prelude::DatasheetEntitySO;

  pub struct MockDataSourceProvider {
    pub json: String,
  }

  #[async_trait(?Send)]
  impl IDataSourceProvider for MockDataSourceProvider {
    async fn get_datasheet_pack(
      &self,
      _datasheet_id: &str,
      _user_id: Option<String>,
      _space_id: Option<String>,
    ) -> anyhow::Result<DatasheetPackSO> {
      get_datasheet_pack()
    }

    async fn get_datasheet_revision(&self, _datasheet_id: &str) -> anyhow::Result<i32> {
      let mut rng = rand::thread_rng();
      let random_number = rng.gen_range(1..=usize::MAX);
      Ok(random_number as i32)
    }

    async fn cache_snapshot(&self, _key: &str, _value: DatasheetPackSO, _newest_revision :i32) -> anyhow::Result<bool>{
      Ok(true)
    }

    async fn get_snapshot_from_cache(&self, _key: &str) -> anyhow::Result<Option<DatasheetPackSO>>{
      Ok(None)
    }

    async fn cache_room_ids(&self, _key: &str, _value: Vec<String>) {}

    async fn get_room_ids_from_cache(&self, _key: &str) -> anyhow::Result<Vec<String>> {
      Ok(Vec::new())
    }

    async fn get_ids_by_dst_id_and_record_ids(&self, _dst_id: &str, _record_ids: Vec<String>) -> anyhow::Result<Vec<String>>{
      Ok(Vec::new())
    }

    async fn get_archived_ids_by_dst_id_and_record_ids(&self, _dst_id: &str, _record_ids: Vec<String>) -> anyhow::Result<Vec<String>> {
      Ok(Vec::new())
    }

    async fn get_meta_data_by_dst_id(&self, _dst_id: &str, _include_deleted: bool) -> anyhow::Result<Option<Json>> {
      Ok(None)
    }

    async fn get_field_map_by_dst_id(&self, _dst_id: &str) -> anyhow::Result<HashMap<String, FieldSO>> {
      Ok(HashMap::new())
    }

    async fn get_field_by_fld_id_and_dst_id(&self, _fld_id: &str, _dst_id: &str) -> anyhow::Result<Option<FieldSO>> {
      Ok(None)
    }

    async fn select_field_type_by_fld_id_and_dst_id(&self, _fld_id: &str, _dst_id: &str) -> anyhow::Result<Option<u32>> {
      Ok(None)
    }

    async fn get_datasheet_by_dst_id(&self, _dst_id: &str) -> anyhow::Result<Option<DatasheetEntitySO>>{
      Ok(None)
    }

    async fn execute_command_with_update_records(&self, _st_id: &str, _user_id: &str, _json_value: Value) -> anyhow::Result<String>{
      Ok("".to_string())
    }

    async fn update_space_statistics(&self, _space_id: &str, _json_value: Value) -> anyhow::Result<()> {
      Ok(())
    }

    async fn get_basic_records_by_record_ids(&self, _dst_id: &str, _record_ids: Vec<String>, _is_deleted: bool) -> anyhow::Result<HashMap<String, RecordSO>>{
      Ok(HashMap::new())
    }

    async fn get_space_id_by_node_id(&self, _node_id: &str) -> anyhow::Result<Option<String>> {
      Ok(None)
    }

    async fn get_node_role(
      &self,
      _node_id: String,
      _auth: AuthHeader,
      _share_id: Option<String>,
      _room_id: Option<String>,
      _source_datasheet_id: Option<String>,
      _source_type: Option<SourceTypeEnum>,
      _allow_all_entrance: Option<bool>,
    ) -> anyhow::Result<NodePermissionSO> {
      Ok(NodePermissionSO{
        ..Default::default()
      })
    }

    async fn capacity_over_limit(
      &self,
      _auth: &AuthHeader,
      _space_id: &str,
    ) -> anyhow::Result<bool> {
      Ok(false)
    }

    async fn get_asset_info(&self, _token: &str) -> anyhow::Result<IAssetDTO> {
      Ok(IAssetDTO::default())
    }

    async fn get_space_usage(&self, _space_id: &str) -> anyhow::Result<InternalSpaceUsageView> {
      Ok(InternalSpaceUsageView::default())
    }

    async fn get_space_subscription(&self, _space_id: &str) -> anyhow::Result<InternalSpaceSubscriptionView> {
      Ok(InternalSpaceSubscriptionView::default())
    }

    async fn count_rows_by_dst_id(&self, _dst_id: &str) -> anyhow::Result<u32>{
      Ok(0)
    }

    async fn update_record_replace(
      &self,
      _dst_id: &str,
      _record_id: &str,
      _json_map: HashMap<&str, Value>,
      _record_meta: String,
      _revision: &u32,
      _updated_by: &str,
    ) {}

    async fn update_record_delete(
      &self,
      _dst_id: &str,
      _record_ids: &Vec<String>,
      _is_delete_data: bool,
      _revision: &u32,
      _updated_by: &str,
    ) {}

    async fn update_record_archive_delete(
      &self,
      _dst_id: &str,
      _record_ids: &Vec<String>,
      _updated_by: &str,
    ) {}

    async fn update_record_remove(
      &self,
      _dst_id: &str,
      _record_id: &str,
      _json_path: String,
      _record_meta: String,
      _revision: &u32,
      _updated_by: &str,
    ) {}

    async fn create_new_changeset(
      &self,
      _id: &str,
      _message_id: &str,
      _dst_id: &str,
      _member_id: &str,
      _operations: Value,
      _revision: &u32,
    ) {}

    async fn create_new_changeset_source(
      &self,
      _id: &str,
      _created_by: &str,
      _dst_id: &str,
      _message_id: &str,
      _source_id: &str,
      _source_type: &u32
    ) {}

    async fn create_record_source(
      &self,
      _user_id: &str,
      _dst_id: &str,
      _source_id: &str,
      _record_ids: Vec<String>,
      _source_type: &u32,
    ) {}

    async fn create_record(
      &self,
      _dst_id: &str,
      _revision: &u32,
      _user_id: &str,
      _save_record_entities: Vec<(&String, HashMap<String, serde_json::Value>, RecordMeta)>
    ) {}

    async fn update_revision_by_dst_id(&self, _dst_id: &str, _revision: &u32, _updated_by: &str) {}

    async fn get_rel_node_id_by_main_node_id(&self, _main_node_id: &str) -> anyhow::Result<Vec<String>> {
      Ok(Vec::new())
    }

    async fn get_has_robot_by_resource_ids(&self, _resource_ids: Vec<String>) -> anyhow::Result<bool> {
      Ok(false)
    }
    
    async fn get_user_info_by_space_id(&self, _auth: &AuthHeader, _space_id: &str) -> anyhow::Result<IUserInfo> {
      Ok(Default::default())
    }

    async fn update_meta_data(&self, _dst_id: &str, _meta_data: &str, _revision: &u32, _updated_by: &str) {}

    async fn select_count_by_dst_id_and_field_name(&self, _dst_id: &str, _field_name: &str) -> anyhow::Result<u32> {
      Ok(0)
    }
  }
}
