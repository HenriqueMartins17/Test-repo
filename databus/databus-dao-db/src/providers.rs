use std::sync::Arc;

use async_trait::async_trait;

use databus_core::dtos::fusion_api_dtos::IAssetDTO;
use databus_core::ot::SourceTypeEnum;
use databus_core::shared::{AuthHeader, IUserInfo, NodePermissionSO};
use databus_core::so::{RecordMeta, InternalSpaceUsageView, InternalSpaceSubscriptionView, FieldSO};
use databus_shared::prelude::Json;
use serde_json::{Value, to_string, from_str};
use tokio::runtime::Runtime;
use fred::prelude::*;
use crate::redis::IntoMultipleValues;
use databus_shared::prelude::HashSet;

use databus_core::prelude::{DatasheetEntitySO, DatasheetPackSO};
use databus_core::prelude::IDataSourceProvider;

use crate::dao_manager::IDataPackDAO;
use crate::DAOManager;
use crate::DataPackDAOOptions;
use crate::types::FetchDataPackOrigin;

use std::collections::HashMap;
use databus_core::prelude::RecordSO;

/**
 * Load Snapshot via MySQL / Databases.
 * Especially for backend.
 */
pub struct DBDataSourceProvider {
  dao_manager: Arc<DAOManager>,
}

impl DBDataSourceProvider {
  /**
   * Get DAO Manager for DAO CRUD operations outside of this crate.
   */
  pub fn get_dao_manager(&self) -> Arc<DAOManager> {
    self.dao_manager.clone()
  }

  pub async fn ainit() -> DBDataSourceProvider {
    let rest_base_url = env_var!(BACKEND_BASE_URL default "http://localhost:8081/api/v1/");
    let dao_options = DataPackDAOOptions::new(rest_base_url);
    let result = DAOManager::new(dao_options).await;

    tracing::info!("database loader init successfully");
    return DBDataSourceProvider {
      dao_manager: result.unwrap(),
    };
  }

  pub fn init() -> DBDataSourceProvider {
    let rt = Runtime::new().expect("Failed to create Tokio runtime.");
    let rest_base_url = env_var!(BACKEND_BASE_URL default "http://localhost:8081/api/v1/");
    let dao_options = DataPackDAOOptions::new(rest_base_url);

    let result = rt.block_on(async { DAOManager::new(dao_options).await });

    tracing::info!("database loader init successfully");
    return DBDataSourceProvider {
      dao_manager: result.unwrap(),
    };
  }
}

#[cfg_attr(target_arch = "wasm32", async_trait(?Send))]
#[cfg_attr(not(target_arch = "wasm32"), async_trait(?Send))]
impl IDataSourceProvider for DBDataSourceProvider {
  async fn get_datasheet_pack(
    &self,
    datasheet_id: &str,
    user_id: Option<String>,
    space_id: Option<String>,
  ) -> anyhow::Result<DatasheetPackSO> {
    self
      .dao_manager
      .fetch_datasheet_pack(
        "datasheet",
        datasheet_id,
        AuthHeader {
          internal: Some(true),
          space_id: space_id,
          user_id: user_id,
          ..Default::default()
        },
        FetchDataPackOrigin {
          internal: true,
          main: Some(true),
          ..Default::default()
        },
        None,
      )
      .await
  }
  async fn get_datasheet_revision(&self, datasheet_id: &str) -> anyhow::Result<i32> {
    let dao_manager = self.dao_manager.clone();
    let dao = dao_manager.datasheet_revision_dao.clone();
    let result = dao.get_revision_by_dst_id(datasheet_id).await;
    match result {
      Ok(Some(revision)) => Ok(revision as i32),
      Ok(None) => Err(anyhow::anyhow!("datasheet not found: {datasheet_id}")),
      Err(e) => Err(e),
    }
  }

  async fn update_revision_by_dst_id(&self, dst_id: &str, revision: &u32, updated_by: &str) {
    self.dao_manager.datasheet_revision_dao.update_revision_by_dst_id(dst_id, revision, updated_by).await;
  }

  async fn cache_snapshot(&self, key: &str, _datasheet_pack: DatasheetPackSO, _newest_revision :i32) -> anyhow::Result<bool>{
    // let value = serde_json::to_string(&_datasheet_pack).unwrap();
    // self.dao_manager.redis_dao.get_connection().await.unwrap().clone()
    //     .set(key, value, Some(Expiration::EX(259200)), None, false).await?;
    Ok(true)
  }

  async fn get_snapshot_from_cache(&self, key: &str) -> anyhow::Result<Option<DatasheetPackSO>>{
    // let content = self.dao_manager.redis_dao.get_connection().await.unwrap().clone().get::<String, _>(key).await;
    // return match content {
    //   Ok(content) => {
    //     if content == "nil" {
    //       return Ok(None);
    //     };
    //     let data_pack = serde_json::from_str::<DatasheetPackSO>(content.as_str()).unwrap();
    //     Ok(Some(data_pack))
    //   },
    //   Err(_e) => {
    //     Ok(None)
    //   }
    // }
    Ok(None)
  }


  async fn cache_room_ids(&self, key: &str, value: Vec<String>) {
    let result: Result<HashSet<String>, RedisError> = self.dao_manager
      .redis_dao
      .get_connection().await.unwrap().clone()
      .sadd(key, IntoMultipleValues(value.iter()))
      .await;
      // .await;
    match result {
      Ok(_) => {
      },
      Err(e) => {
        tracing::error!("cache_room_ids error: {:?}", e);
      }
    }
  }

  async fn get_room_ids_from_cache(&self, key: &str) -> anyhow::Result<Vec<String>> {
    let result: Result<HashSet<String>, RedisError> = self.dao_manager
      .redis_dao
      .get_connection().await.unwrap().clone()
      .smembers(key)
      .await;
    match result {
      Ok(members) => {
        let mut room_ids = vec![];
        for member in members {
          room_ids.push(member);
        }
        Ok(room_ids)
      },
      Err(e) => {
        Err(anyhow::anyhow!("get_room_ids_from_cache error: {:?}", e))
      }
    }
  }

  async fn get_ids_by_dst_id_and_record_ids(&self, dst_id: &str, record_ids: Vec<String>) -> anyhow::Result<Vec<String>>{
    self.dao_manager.record_dao.get_ids_by_dst_id_and_record_ids(&dst_id, record_ids.clone()).await
  }

  async fn get_archived_ids_by_dst_id_and_record_ids(&self, dst_id: &str, record_ids: Vec<String>) -> anyhow::Result<Vec<String>> {
    self.dao_manager.record_dao.get_archived_ids_by_dst_id_and_record_ids(&dst_id, record_ids.clone()).await
  }

  async fn get_basic_records_by_record_ids(&self, dst_id: &str, record_ids: Vec<String>, is_deleted: bool) -> anyhow::Result<HashMap<String, RecordSO>>{
    self.dao_manager.record_dao.get_records(dst_id, Some(record_ids), is_deleted, false).await
  }

  async fn get_meta_data_by_dst_id(&self, dst_id: &str, include_deleted: bool) -> anyhow::Result<Option<Json>> {
    self.dao_manager.datasheet_meta_dao.get_meta_data_by_dst_id(dst_id, include_deleted).await
  }

  async fn get_field_map_by_dst_id(&self, dst_id: &str) -> anyhow::Result<HashMap<String, FieldSO>> {
    let res = self.dao_manager.datasheet_meta_dao.get_field_map_by_dst_id(dst_id).await;
    match res {
      Ok(Some(field_map)) => {
        Ok(field_map)
      },
      Ok(None) => {
        Err(anyhow::anyhow!("NODE_NOT_EXIST"))
      },
      Err(e) => {
        Err(e)
      }
    }
  }

  async fn get_field_by_fld_id_and_dst_id(&self, fld_id: &str, dst_id: &str) -> anyhow::Result<Option<FieldSO>> {
    self.dao_manager.datasheet_meta_dao.get_field_by_fld_id_and_dst_id(fld_id, dst_id).await
  }

  async fn select_field_type_by_fld_id_and_dst_id(&self, fld_id: &str, dst_id: &str) -> anyhow::Result<Option<u32>> {
    self.dao_manager.datasheet_meta_dao.select_field_type_by_fld_id_and_dst_id(fld_id, dst_id).await
  }

  async fn get_datasheet_by_dst_id(&self, dst_id: &str) -> anyhow::Result<Option<DatasheetEntitySO>> {
    let po = self.dao_manager.datasheet_dao.get_datasheet_by_dst_id(dst_id).await;
    match po {
      Ok(Some(po)) => {
        let so = DatasheetEntitySO {
          id: po.id,
          is_deleted: false,
          creator: po.creator,
          revision: po.revision,
          created_by: None,
          updated_by: None,
          created_at: 0,
          updated_at: 0,
          dst_id: po.dst_id,
          node_id: po.node_id,
          dst_name: po.dst_name,
          space_id: po.space_id,
        };
        Ok(Some(so))
      },
      Ok(None) => {
        Ok(None)
      },
      Err(e) => {
        Err(e)
      }
    }
  }

  async fn execute_command_with_update_records(&self, dst_id: &str, user_id: &str, json_value: Value) -> anyhow::Result<String>{
    self.dao_manager.rest_dao.execute_command_with_update_records(dst_id, user_id, json_value).await
  }

  async fn update_space_statistics(&self, space_id: &str, json_value: Value) -> anyhow::Result<()> {
    self.dao_manager.rest_dao.update_space_statistics(space_id, json_value).await
  }

  async fn get_space_id_by_node_id(&self, node_id: &str) -> anyhow::Result<Option<String>>{
    self.dao_manager.node_dao.get_space_id_by_node_id(node_id).await
  }

  async fn get_node_role(
    &self,
    node_id: String,
    auth: AuthHeader,
    share_id: Option<String>,
    room_id: Option<String>,
    source_datasheet_id: Option<String>,
    source_type: Option<SourceTypeEnum>,
    allow_all_entrance: Option<bool>,
  ) -> anyhow::Result<NodePermissionSO> {
    let result = self.dao_manager.node_dao.get_node_role(
      node_id,
      auth,
      share_id,
      room_id,
      source_datasheet_id,
      source_type,
      allow_all_entrance,
    ).await;
    match result {
      Ok(node_permission) => {
        let json_str = to_string(&node_permission).unwrap();
        let json_value:NodePermissionSO = from_str(&json_str).unwrap();
        Ok(json_value)
      },
      Err(e) => {
        Err(e)
      }
    }
  }

  async fn get_user_info_by_space_id(&self, auth: &AuthHeader, space_id: &str) -> anyhow::Result<IUserInfo> {
    self.dao_manager.rest_dao.get_user_info_by_space_id(auth, space_id).await
  }

  async fn capacity_over_limit(
    &self,
    auth: &AuthHeader,
    space_id: &str,
  ) -> anyhow::Result<bool> {
    self.dao_manager.rest_dao.capacity_over_limit(auth, space_id).await
  }

  async fn get_asset_info(&self, token: &str) -> anyhow::Result<IAssetDTO> {
    self.dao_manager.rest_dao.get_asset_info(token).await
  }

  async fn get_space_usage(&self, space_id: &str) -> anyhow::Result<InternalSpaceUsageView> {
    self.dao_manager.rest_dao.get_space_usage(space_id).await
  }

  async fn get_space_subscription(&self, space_id: &str) -> anyhow::Result<InternalSpaceSubscriptionView> {
    self.dao_manager.rest_dao.get_space_subscription(space_id).await
  }

  async fn update_record_replace(
    &self,
    dst_id: &str,
    record_id: &str,
    json_map: HashMap<&str, Value>,
    record_meta: String,
    revision: &u32,
    updated_by: &str,
  ) {
    self.dao_manager.record_dao.update_record_replace(dst_id, record_id, json_map, record_meta, revision, updated_by).await;
  }

  async fn update_record_archive_delete(
    &self,
    dst_id: &str,
    record_ids: &Vec<String>,
    updated_by: &str,
  ) {
    self.dao_manager.record_dao.update_record_archive_delete(dst_id, record_ids, updated_by).await;
  }

  async fn update_record_delete(
    &self,
    dst_id: &str,
    record_ids: &Vec<String>,
    is_delete_data: bool,
    revision: &u32,
    updated_by: &str,
  ) {
    self.dao_manager.record_dao.update_record_delete(dst_id, record_ids, is_delete_data, revision, updated_by).await;
  }

  async fn update_record_remove(
    &self,
    dst_id: &str,
    record_id: &str,
    json_path: String,
    record_meta: String,
    revision: &u32,
    updated_by: &str,
  ) {
    self.dao_manager.record_dao.update_record_remove(dst_id, record_id, json_path, record_meta, revision, updated_by).await;
  }

  async fn create_new_changeset(
    &self,
    id: &str,
    message_id: &str,
    dst_id: &str,
    member_id: &str,
    operations: Value,
    revision: &u32,
  ) {
    self.dao_manager.datasheet_changeset_dao.create_new_changeset(id, message_id, dst_id, member_id, operations, revision).await;
  }

  async fn create_new_changeset_source(
    &self,
    id: &str,
    created_by: &str,
    dst_id: &str,
    message_id: &str,
    source_id: &str,
    source_type: &u32
  ) {
    self.dao_manager.datasheet_changeset_dao.create_new_changeset_source(id, created_by, dst_id, message_id, source_id, source_type).await;
  }

  async fn create_record_source(
    &self,
    user_id: &str,
    dst_id: &str,
    source_id: &str,
    record_ids: Vec<String>,
    source_type: &u32,
  ) {
    self.dao_manager.record_dao.create_record_source(user_id, dst_id, source_id, record_ids, source_type).await;
  }

  async fn create_record(
    &self,
    dst_id: &str,
    revision: &u32,
    user_id: &str,
    save_record_entities: Vec<(&String, HashMap<String, Value>, RecordMeta)>
  ) {
    self.dao_manager.record_dao.create_record(dst_id, revision, user_id, save_record_entities).await;
  }

  async fn get_rel_node_id_by_main_node_id(&self, main_node_id: &str) -> anyhow::Result<Vec<String>> {
    self.dao_manager.node_dao.get_rel_node_id_by_main_node_id(main_node_id).await
  }

  async fn get_has_robot_by_resource_ids(&self, resource_ids: Vec<String>) -> anyhow::Result<bool> {
    let result = self.dao_manager.automation_dao.robot_dao.is_resources_has_robots(resource_ids.clone()).await;
    match result {
      Ok(has_robot) => {
        if !has_robot {
          let result = self.dao_manager.automation_dao.trigger_dao.get_robot_id_and_resource_id_by_resource_ids(resource_ids).await;
          match result {
            Ok(triggers) => {
              println!("triggers: {:?}", triggers);
              if triggers.len() > 0 {
                let robot_ids: HashSet<_> = triggers.iter().map(|i| i.robot_id.clone()).collect();
                let number = self.dao_manager.automation_dao.robot_dao.get_active_count_by_robot_ids(robot_ids.into_iter().collect()).await?;
                return Ok(number > 0);
              }
              return Ok(false);
            },
            Err(e) => {
              Err(e)
            }
          }
        } else {
          Ok(true)
        }
      },
      Err(e) => {
        Err(e)
      }
    }
  }

  async fn count_rows_by_dst_id(&self, dst_id: &str) -> anyhow::Result<u32> {
    self.dao_manager.datasheet_meta_dao.count_rows_by_dst_id(dst_id).await
  }

  async fn update_meta_data(&self, dst_id: &str, meta_data: &str, revision: &u32, updated_by: &str) {
    self.dao_manager.datasheet_meta_dao.update_meta_data(dst_id, meta_data, revision, updated_by).await;
  }

  async fn select_count_by_dst_id_and_field_name(&self, dst_id: &str, field_name: &str) -> anyhow::Result<u32> {
    self.dao_manager.datasheet_meta_dao.select_count_by_dst_id_and_field_name(dst_id, field_name).await
  }
}
