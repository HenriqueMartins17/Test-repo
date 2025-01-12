use idb::{Database, Error, Factory, IndexParams, KeyPath, ObjectStoreParams, TransactionMode};
use log::info;
use wasm_bindgen::JsValue;
use crate::SheetDataInfo;
use gloo_utils::format::JsValueSerdeExt;
use serde_json::Value;

pub async fn new_client() -> Result<Database, Error> {
    // Get a factory instance from global scope
    let factory = Factory::new()?;

    // Create an open request for the database
    let mut open_request = factory.open("datasheets", Some(3)).unwrap();

    // Add an upgrade handler for database
    open_request.on_upgrade_needed(|event| {
        // Get database instance from event
        let database = event.database().unwrap();
        let store_names = database.store_names();
        if !store_names.iter().any(|s| s.contains("snapshot")) {
            info!("indexDB create object store snapshot");

            let mut store_params = ObjectStoreParams::new();
            store_params.auto_increment(true);
            store_params.key_path(Some(KeyPath::new_single("id")));

            let _store = database
                .create_object_store("snapshot", store_params)
                .unwrap();
        }

        if !store_names.iter().any(|s| s.contains("snapshot_info")) {
            info!("indexDB create object store snapshot_info");

            let mut store_params = ObjectStoreParams::new();
            store_params.auto_increment(true);
            store_params.key_path(Some(KeyPath::new_single("id")));

            let store = database
                .create_object_store("snapshot_info", store_params)
                .unwrap();

            let mut index_params = IndexParams::new();
            index_params.unique(true);

            store
                .create_index("dst_id", KeyPath::new_single("dst_id"), Some(index_params))
                .unwrap();
        }
    });

    // `await` open request
    open_request.await
}


pub async fn get_snapshot_from_index_db(id: JsValue) -> anyhow::Result<Option<JsValue>> {
    let database = new_client().await.expect("open index db error");
    // Create a read-write transaction
    let transaction = database.transaction(&["snapshot"], TransactionMode::ReadOnly).unwrap();

    // Get the object store
    let store = transaction.object_store("snapshot").unwrap();

    Ok(store.get(id).await.expect("get snapshot error"))
}

pub async fn delete_snapshot_from_index_db(id: JsValue) -> () {
    let database = new_client().await.expect("open index db error");
    // Create a read-write transaction
    let transaction = database.transaction(&["snapshot"], TransactionMode::ReadWrite).unwrap();

    // Get the object store
    let store = transaction.object_store("snapshot").unwrap();

    store.delete(id).await.expect("delete snapshot error");
    transaction.done();
    database.close();
    ()
}

pub async fn delete_snapshot_info_from_index_db(id: JsValue) -> () {
    let database = new_client().await.expect("open index db error");
    // Create a read-write transaction
    let transaction = database.transaction(&["snapshot_info"], TransactionMode::ReadWrite).unwrap();

    // Get the object store
    let store = transaction.object_store("snapshot_info").unwrap();

    store.delete(id).await.expect("delete snapshot error");
    transaction.done();
    database.close();
    ()
}

pub async fn add_snapshot_from_index_db(data: JsValue) -> JsValue {
    let database = new_client().await.expect("open index db error");
    // Create a read-write transaction
    let transaction = database.transaction(&["snapshot"], TransactionMode::ReadWrite).unwrap();

    // Get the object store
    let store = transaction.object_store("snapshot").unwrap();

    let id = store.add(&data, None).await.expect("delete snapshot error");
    transaction.commit().await.expect("commit snapshot error");
    database.close();
    return id;
}


pub async fn get_cache_info(_key: &str) -> Option<SheetDataInfo>{
    let database = new_client().await.expect("open index db error");
    let transaction = database.transaction(&["snapshot_info"], TransactionMode::ReadWrite).unwrap();
    let store = transaction.object_store("snapshot_info").unwrap();
    let index = store.index("dst_id").unwrap();
    let old_cache_result = index.get(JsValue::from_serde(&_key).unwrap())
        .await
        .expect("get snapshot cache info error");
    database.close();
    return if old_cache_result.is_none() {
        None
    } else {
        let old_cache = old_cache_result.unwrap();
        let datasheet_info = old_cache.into_serde::<SheetDataInfo>().unwrap();
        Some(datasheet_info)
    }

}

pub async fn put_snapshot_info(_info: Value) -> (){
    let database = new_client().await.expect("open index db error");
    let transaction = database.transaction(&["snapshot_info"], TransactionMode::ReadWrite).unwrap();
    let store = transaction.object_store("snapshot_info").unwrap();
    store.put(&JsValue::from_serde(&_info).unwrap(), None).await.expect("put snapshot cache info error");
    transaction.commit().await.expect("commit snapshot cache info error");
    database.close();
    ()
}