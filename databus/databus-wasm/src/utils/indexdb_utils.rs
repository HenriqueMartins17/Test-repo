use gloo_utils::format::JsValueSerdeExt;
use rexie::{Index, ObjectStore, Rexie};
use rexie::{Result, TransactionMode};
use wasm_bindgen::prelude::*;

pub async fn new_client() -> anyhow::Result<Rexie> {
  let client = Rexie::builder("datasheet")
    .version(2)
    .add_object_store(
      ObjectStore::new("sheet")
        .key_path("id")
        .auto_increment(true)
        .add_index(Index::new("dst_id", "dst_id").unique(true)),
    )
    .build()
    .await;
  match client {
    Ok(client) => Ok(client),
    Err(e) => Err(anyhow::anyhow!("Error fetching datasheet pack: {:?}", e)),
  }
}
pub async fn get_snapshot_by_dst_id(dst_id: String) -> Result<JsValue> {
  let client = new_client().await.unwrap();

  let transaction = client.transaction(&["sheet"], TransactionMode::ReadWrite)?;
  // Get the `store` store
  let store = transaction.store("sheet")?;
  let data = store
    .index("dst_id")
    .unwrap()
    .get(&JsValue::from_serde(&dst_id).unwrap())
    .await;
  transaction.done().await?;
  return data;
}

pub async fn update_snapshot(data: JsValue) -> Result<JsValue> {
  let client = new_client().await.unwrap();
  let transaction = client.transaction(&["sheet"], TransactionMode::ReadWrite)?;
  let store = transaction.store("sheet")?;
  let result = store.put(&data, None).await?;
  transaction.done().await?;
  return Ok(result);
}

pub async fn delete_snapshot_by_id(id: JsValue) -> Result<()> {
  let client = new_client().await.unwrap();
  let transaction = client.transaction(&["sheet"], TransactionMode::ReadWrite)?;
  let store = transaction.store("sheet")?;
  store.delete(&id).await?;
  transaction.done().await?;
  return Ok(());
}

pub async fn get_snapshot_count() -> Result<u32> {
  let client = new_client().await.unwrap();
  let transaction = client.transaction(&["sheet"], TransactionMode::ReadWrite)?;
  let store = transaction.store("sheet")?;
  let count = store.count(None).await?;
  transaction.done().await?;
  return Ok(count);
}
