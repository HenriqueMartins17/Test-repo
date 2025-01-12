use std::collections::HashMap;

use databus_core::{prelude::*, shared::{AuthHeader, NodePermissionSO}, ot::SourceTypeEnum, dtos::fusion_api_dtos::IAssetDTO};

use async_trait::async_trait;
use databus_shared::prelude::Json;
use idb::TransactionMode;

use serde::{Serialize, Deserialize};
use serde_json::{json, Value};
use wasm_bindgen::{JsValue, UnwrapThrowExt};
use crate::indexdb_utils::new_client;
use gloo_utils::format::JsValueSerdeExt;
use js_sys::Date;
use log::info;
use databus_core::shared::IUserInfo;

extern crate log;


use crate::{add_snapshot_from_index_db, ApiDAO, delete_snapshot_from_index_db, delete_snapshot_info_from_index_db, get_cache_info, get_snapshot_from_index_db, put_snapshot_info};

#[derive(Debug, Serialize, Deserialize)]
pub struct SheetDataCache {
    id: Option<u32>,
    dst_id: String,
    reversion: usize,
    content: DatasheetPackSO,
    expired_at: f64,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct SheetDataInfo {
    id: Option<i32>,
    dst_id: String,
    reversion: i32,
    snapshot_id: i32,
    expired_at: f64,
}


/**
 * Load DatasheetPack via Private APIs. (not Open APIs)
 * Especially for frontend.
 */
pub struct APIDataSourceProvider {
    api: ApiDAO,
}

impl APIDataSourceProvider {
    pub fn new(base_url: &str) -> APIDataSourceProvider {
        let api = ApiDAO::new(base_url);
        APIDataSourceProvider { api: api }
    }
}

impl APIDataSourceProvider {

    pub async fn delete_cache(&self, _datasheet_id: &str) -> anyhow::Result<bool> {
        let cache_key = format!("databus:snapshot:v3:{}", _datasheet_id);
        let cache_info = get_cache_info(cache_key.as_str()).await;
        if cache_info.is_none() {
            Ok(false)
        }else {
            let info = cache_info.unwrap();
            delete_snapshot_from_index_db(JsValue::from(info.snapshot_id)).await;
            delete_snapshot_info_from_index_db(JsValue::from(info.id.unwrap())).await;
            Ok(true)
        }
    }

    pub async fn get_datasheet_pak_js(
        &self,
        _datasheet_id: &str,
        _user_id: Option<String>,
        _space_id: Option<String>, ) -> JsValue {
        let new_revision = self.get_datasheet_revision(_datasheet_id).await.expect("get_datasheet_revision error");
        if new_revision == -1 {
            return JsValue::from_serde(&json!({
                "success": false,
                "code": 600,
                "message": "Node not found"
            })).unwrap();
        }
        let cache_key = format!("databus:snapshot:v3:{}", _datasheet_id);
        let cache_info = get_cache_info(cache_key.as_str()).await;
        if cache_info.is_none() {
            info!("new version is {} but cache_info is none", new_revision);
            return self.get_data_pack_and_cache(_datasheet_id, cache_key.as_str(), true, new_revision, None).await;
        }else {
            let info = cache_info.unwrap();
            if info.expired_at < Date::now() {
                info!("new version is {} but cache_info is expired", new_revision);
                return self.get_data_pack_and_cache(_datasheet_id, cache_key.as_str(), true, new_revision, Some(info)).await;
            }else if info.reversion == new_revision {
                info!("new version is {} and cache_info version is {}", new_revision, info.reversion);
                return self.get_data_pack_and_cache(_datasheet_id, cache_key.as_str(), false, new_revision, Some(info)).await;
            }else {
                info!("new version is {} but cache_info version is {} need up-to-date", new_revision, info.reversion);
                return self.get_data_pack_and_cache(_datasheet_id, cache_key.as_str(), true, new_revision, Some(info)).await;
            }
        }
        return JsValue::from_str("hello");
    }

    async fn get_data_pack_and_cache(
        &self,
        _datasheet_id: &str,
        _cache_key: &str,
        _update_cache: bool,
        _new_revision: i32,
        cache_info: Option<SheetDataInfo>
    ) -> JsValue {
        return if _update_cache {
            let snapshot = self.api.fetch_datasheet_pack_str(_datasheet_id, None, None)
                .await
                .expect("fetch_datasheet_pack_str error");
            let snapshot = js_sys::JSON::parse(snapshot.as_str()).unwrap_throw();
            if cache_info.is_none() {
                let new_id = add_snapshot_from_index_db(snapshot.clone()).await;
                let info = serde_json::json!({
                    "snapshot_id": new_id.as_f64().unwrap() as i64,
                    "dst_id": _cache_key.to_string(),
                    "reversion": _new_revision,
                    "expired_at": Date::now() + 3.0 * 24.0 * 60.0 * 60.0 * 1000.0,
                });
                put_snapshot_info(info).await;
                snapshot
            } else {
                // delete old snapshot
                let mut old_info = cache_info.unwrap();
                delete_snapshot_from_index_db(JsValue::from(old_info.snapshot_id)).await;
                let new_id = add_snapshot_from_index_db(snapshot.clone()).await;
                old_info.snapshot_id = new_id.as_f64().unwrap() as i32;
                old_info.reversion = _new_revision;
                old_info.expired_at = Date::now() + 3.0 * 24.0 * 60.0 * 60.0 * 1000.0;
                put_snapshot_info(serde_json::to_value(old_info).unwrap()).await;
                snapshot
            }
        } else {
            let id = cache_info.unwrap().snapshot_id;
            info!("get snapshot from index by id db {}", id);
            match get_snapshot_from_index_db(JsValue::from(id)).await {
                Ok(snapshot) => {
                    if snapshot.is_none() {
                        JsValue::NULL
                    }else {
                        snapshot.unwrap()
                    }
                },
                _ => { JsValue::NULL }
            }
        };
    }

}

#[cfg_attr(target_arch = "wasm32", async_trait(? Send))]
#[cfg_attr(not(target_arch = "wasm32"), async_trait(? Send))]
impl IDataSourceProvider for APIDataSourceProvider {


    #[warn(deprecated)]
    async fn get_datasheet_pack(
        &self,
        _datasheet_id: &str,
        user_id: Option<String>,
        space_id: Option<String>,
    ) -> anyhow::Result<DatasheetPackSO> {
        let api_response = self.api.fetch_datasheet_pack(_datasheet_id, user_id, space_id).await;
        match api_response {
            Ok(api_response) => {
                if api_response.success {
                    Ok(api_response.data)
                } else {
                    Err(anyhow::anyhow!(
            "Error fetching datasheet pack: {:?}",
            api_response.message
          ))
                }
            }
            Err(e) => Err(anyhow::anyhow!("Error fetching datasheet pack: {:?}", e)),
        }
    }

    async fn get_datasheet_revision(&self, _datasheet_id: &str) -> anyhow::Result<i32> {
        let api_response = self.api.get_datasheet_revision(_datasheet_id).await;
        match api_response {
            Ok(api_response) => {
                if api_response.success {
                    Ok(api_response.data)
                } else {
                    Err(anyhow::anyhow!("Error fetching datasheet pack: {:?}", api_response.message))
                }
            }
            Err(e) => Err(anyhow::anyhow!("Error fetching datasheet pack: {:?}", e)),
        }
    }

    #[warn(deprecated)]
    async fn cache_snapshot(&self, key: &str, _datasheet_pack: DatasheetPackSO, new_version: i32) -> anyhow::Result<bool> {
        let database_result = new_client().await;
        match database_result {
            Ok(database) => {
                // Create a read-write transaction
                let transaction = database.transaction(&["snapshot"], TransactionMode::ReadWrite).unwrap();

                // Get the object store
                let store = transaction.object_store("snapshot").unwrap();

                let index = store.index("dst_id").unwrap();
                let old_cache_result = index.get(JsValue::from_serde(&key).unwrap()).await;

                match old_cache_result {
                    Ok(old_cache) => {
                        if old_cache.is_none() {
                            // Prepare data to add
                            let sheet_data_vo = serde_json::json!({
                    "dst_id": key,
                    "content": _datasheet_pack,
                    "revision": new_version,
                    "expired_at": Date::now() + 3.0 * 24.0 * 60.0 * 60.0 * 1000.0,
                });
                            store
                                .add(
                                    &JsValue::from_serde(&sheet_data_vo).unwrap(),
                                    None,
                                )
                                .await.expect("indexeddb add error");
                        } else {
                            let old_cache = old_cache.unwrap();
                            let mut old_cache_value = old_cache.into_serde::<Value>().unwrap();
                            old_cache_value["content"] = serde_json::to_value(_datasheet_pack).unwrap();
                            old_cache_value["revision"] = serde_json::Value::Number(new_version.into());
                            old_cache_value["expired_at"] = serde_json::Value::Number(serde_json::Number::from_f64(Date::now() + 3.0 * 24.0 * 60.0 * 60.0 * 1000.0).unwrap());
                            store
                                .put(
                                    &JsValue::from_serde(&old_cache_value).unwrap(),
                                    None,
                                )
                                .await.expect("indexeddb put error");
                        }
                        Ok(true)
                    }
                    Err(e) => {
                        Err(anyhow::anyhow!("Error fetching datasheet pack: {:?}", e))
                    }
                }
            }
            Err(e) => {
                Err(anyhow::anyhow!("Error fetching datasheet pack: {:?}", e))
            }
        }
    }

    #[warn(deprecated)]
    async fn get_snapshot_from_cache(&self, key: &str) -> anyhow::Result<Option<DatasheetPackSO>> {
        let database_result = new_client().await;
        match database_result {
            Ok(database) => {
                let transaction = database.transaction(&["snapshot"], TransactionMode::ReadWrite).unwrap();
                let store = transaction.object_store("snapshot").unwrap();
                let index = store.index("dst_id").unwrap();
                let old_cache_result = index.get(JsValue::from_serde(&key).unwrap()).await;
                match old_cache_result {
                    Ok(old_cache) => {
                        if old_cache.is_none() {
                            Ok(None)
                        } else {
                            let old_cache = old_cache.unwrap();
                            let old_cache_value = old_cache.into_serde::<Value>().unwrap();
                            if old_cache_value["expired_at"].as_f64().unwrap() < Date::now() {
                                Ok(None)
                            } else {
                                let datasheet_pack = serde_json::from_str::<DatasheetPackSO>(old_cache_value["content"].to_string().as_str()).unwrap();
                                // let datasheet_pack = old_cache_value.content;
                                Ok(Some(datasheet_pack))
                            }
                        }
                    }
                    Err(e) => {
                        Err(anyhow::anyhow!("Error fetching datasheet pack: {:?}", e))
                    }
                }
            }
            Err(e) => {
                Err(anyhow::anyhow!("Error fetching datasheet pack: {:?}", e))
            }
        }
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

    async fn execute_command_with_update_records(&self, _dst_id: &str, _user_id: &str, _json_value: serde_json::Value) -> anyhow::Result<String>{
        Ok(String::new())
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
    ){}

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
        Ok(IUserInfo::default())
    }

    async fn update_meta_data(&self, _dst_id: &str, _meta_data: &str, _revision: &u32, _updated_by: &str) {}

    async fn select_count_by_dst_id_and_field_name(&self, _dst_id: &str, _field_name: &str) -> anyhow::Result<u32> {
        Ok(0)
    }
}

