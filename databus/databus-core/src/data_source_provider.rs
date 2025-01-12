use async_trait::async_trait;
use databus_shared::prelude::Json;
use serde_json::Value;
// use databus-dao-db::database::datasheet_entity_po::DatasheetEntityPO;

use crate::dtos::fusion_api_dtos::IAssetDTO;
use crate::ot::SourceTypeEnum;
use crate::prelude::DatasheetPackSO;
use crate::prelude::{DatasheetEntitySO, RecordSO};
use crate::shared::{AuthHeader, IUserInfo, NodePermissionSO};
use crate::so::{RecordMeta, InternalSpaceUsageView, InternalSpaceSubscriptionView, FieldSO};
use std::collections::HashMap;

/**
 * Data Source Provider for databases, apis, and other datasheet pack dependencies
 */

#[cfg_attr(target_arch = "wasm32", async_trait(?Send))]
#[cfg_attr(not(target_arch = "wasm32"), async_trait(?Send))]
pub trait IDataSourceProvider: Send + Sync {
  // fn init(&mut self, rest_base_url: String) -> bool;
  async fn get_datasheet_pack(
    &self,
    datasheet_id: &str,
    user_id: Option<String>,
    space_id: Option<String>,
  ) -> anyhow::Result<DatasheetPackSO>;

  async fn get_datasheet_revision(&self, datasheet_id: &str) -> anyhow::Result<i32>;

  async fn update_revision_by_dst_id(&self, dst_id: &str, revision: &u32, updated_by: &str);

  async fn cache_snapshot(&self, key: &str, value: DatasheetPackSO, newest_revision :i32) -> anyhow::Result<bool>;

  async fn get_snapshot_from_cache(&self, key: &str) -> anyhow::Result<Option<DatasheetPackSO>>;

  async fn cache_room_ids(&self, key: &str, value: Vec<String>);

  async fn get_room_ids_from_cache(&self, key: &str) -> anyhow::Result<Vec<String>>;

  //get ids from record_dao
  async fn get_ids_by_dst_id_and_record_ids(&self, dst_id: &str, record_ids: Vec<String>) -> anyhow::Result<Vec<String>>;

  //get ids from record_dao
  async fn get_archived_ids_by_dst_id_and_record_ids(&self, dst_id: &str, record_ids: Vec<String>) -> anyhow::Result<Vec<String>>;

  //get basic records from record_dao
  async fn get_basic_records_by_record_ids(&self, dst_id: &str, record_ids: Vec<String>, is_deleted: bool) -> anyhow::Result<HashMap<String, RecordSO>>;

  //get meta from datasheet_meta_dao
  async fn get_meta_data_by_dst_id(&self, dst_id: &str, include_deleted: bool) -> anyhow::Result<Option<Json>>;

  //get field_map from datasheet_meta_dao
  async fn get_field_map_by_dst_id(&self, dst_id: &str) -> anyhow::Result<HashMap<String, FieldSO>>;

  //get field_map from datasheet_meta_dao
  async fn get_field_by_fld_id_and_dst_id(&self, fld_id: &str, dst_id: &str) -> anyhow::Result<Option<FieldSO>>;

  //in datasheet_meta_dao
  async fn select_field_type_by_fld_id_and_dst_id(&self, fld_id: &str, dst_id: &str) -> anyhow::Result<Option<u32>>;

  //get datasheet from datasheet_dao
  async fn get_datasheet_by_dst_id(&self, dst_id: &str) -> anyhow::Result<Option<DatasheetEntitySO>>;

  //http post for @Post('datasheets/:dstId/executeCommand')
  async fn execute_command_with_update_records(&self, dst_id: &str, user_id: &str, json_value: Value) -> anyhow::Result<String>;

  ///http post to back_end in rest_dao
  async fn update_space_statistics(&self, space_id: &str, json_value: Value) -> anyhow::Result<()> ;

  // get spaceid from node_dao
  async fn get_space_id_by_node_id(&self, node_id: &str) -> anyhow::Result<Option<String>>;

  // get permission from node_dao
  async fn get_node_role(
    &self,
    node_id: String,
    auth: AuthHeader,
    share_id: Option<String>,
    room_id: Option<String>,
    source_datasheet_id: Option<String>,
    source_type: Option<SourceTypeEnum>,
    allow_all_entrance: Option<bool>,
  ) -> anyhow::Result<NodePermissionSO>;

  async fn get_user_info_by_space_id(
    &self,
    auth: &AuthHeader,
    space_id: &str,
  ) -> anyhow::Result<IUserInfo>;

  // get is capacity_over_limit from rest_dao
  async fn capacity_over_limit(
    &self,
    auth: &AuthHeader,
    space_id: &str,
  ) -> anyhow::Result<bool>;

  // update records in record_dao
  async fn update_record_replace(
    &self,
    dst_id: &str,
    record_id: &str,
    json_map: HashMap<&str, Value>,
    record_meta: String,
    revision: &u32,
    updated_by: &str,
  );

  // update records in record_dao
  async fn update_record_delete(
    &self,
    dst_id: &str,
    record_ids: &Vec<String>,
    is_delete_data: bool,
    revision: &u32,
    updated_by: &str,
  );

  // update records in record_dao
  async fn update_record_archive_delete(
    &self,
    dst_id: &str,
    record_ids: &Vec<String>,
    updated_by: &str,
  );

  // update records in record_dao
  async fn update_record_remove(
    &self,
    dst_id: &str,
    record_id: &str,
    json_path: String,
    record_meta: String,
    revision: &u32,
    updated_by: &str,
  );

  // insert into new changeset in DatasheetChangesetDAO
  async fn create_new_changeset(
    &self,
    id: &str,
    message_id: &str,
    dst_id: &str,
    member_id: &str,
    operations: Value,
    revision: &u32,
  );

  // insert into new changeset_source in DatasheetChangesetDAO
  async fn create_new_changeset_source(
    &self,
    id: &str,
    created_by: &str,
    dst_id: &str,
    message_id: &str,
    source_id: &str,
    source_type: &u32
  );

  // insert into datasheet_record_source in DatasheetRecordDAO
  async fn create_record_source(
    &self,
    user_id: &str,
    dst_id: &str,
    source_id: &str,
    record_ids: Vec<String>,
    source_type: &u32,
  );

  // insert into datasheet_record in DatasheetRecordDAO
  async fn create_record(
    &self,
    dst_id: &str,
    revision: &u32,
    user_id: &str,
    save_record_entities: Vec<(&String, HashMap<String, Value>, RecordMeta)>
  );

  // get rel_node_id ids from node_rel_dao
  async fn get_rel_node_id_by_main_node_id(&self, main_node_id: &str) -> anyhow::Result<Vec<String>>;

  async fn get_has_robot_by_resource_ids(&self, resource_ids: Vec<String>) -> anyhow::Result<bool> ;

  // get IAssetDTO from rest_dao
  async fn get_asset_info(&self, token: &str) -> anyhow::Result<IAssetDTO>;

  // get from rest_dao
  async fn get_space_usage(&self, space_id: &str) -> anyhow::Result<InternalSpaceUsageView>;

  // get from rest_dao
  async fn get_space_subscription(&self, space_id: &str) -> anyhow::Result<InternalSpaceSubscriptionView>;

  // datasheet_meta_dao
  async fn count_rows_by_dst_id(&self, dst_id: &str) -> anyhow::Result<u32>;

  // update meta data in datasheet_meta_dao
  async fn update_meta_data(&self, dst_id: &str, meta_data: &str, revision: &u32, updated_by: &str) ;

  //in datasheet_meta_dao
  async fn select_count_by_dst_id_and_field_name(&self, dst_id: &str, field_name: &str) -> anyhow::Result<u32>;
}

// #[derive(Deserialize, Debug, Clone)]
// #[serde(rename_all = "camelCase")]
// pub struct UrlFieldProperty {
//   #[serde(rename = "isRecogURLFlag")]
//   #[serde(skip_serializing_if = "Option::is_none")]
//   pub is_recog_url_flag: Option<bool>,
// }
