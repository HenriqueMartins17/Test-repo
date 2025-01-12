use anyhow;
use anyhow::Context;
use futures::TryStreamExt;
use mysql_common::params;
use mysql_common::prelude::FromRow;
/// Persistent Objects Definitions
use serde::{Deserialize, Serialize};
use std::sync::Arc;
use serde_json::json;
use utoipa::ToSchema;
use crate::db_manager::DBManager;

pub struct AiDAO {
  db: Arc<dyn DBManager>,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct AiNode {
  id: u64,
  ai_id: String,
  #[deprecated]
  node_id: Option<String>,
  #[deprecated]
  node_type: Option<i32>,
  #[serde(rename = "type")]
  type_: Option<String>,
  setting: serde_json::Value,
  #[deprecated]
  version: Option<i32>,
}

#[derive(Debug, Clone, PartialEq, FromRow)]
pub struct AiNodePO {
  id: u64,
  ai_id: String,
  #[deprecated]
  node_id: Option<String>,
  #[deprecated]
  node_type: Option<i32>,
  #[mysql(rename = "type")]
  type_: Option<String>,
  setting: Option<String>,
  #[deprecated]
  version: Option<i32>,
}

impl AiNodePO {
  pub fn to_entity(&self) -> anyhow::Result<AiNode> {
    let entity = AiNode {
      id: self.id,
      ai_id: self.ai_id.to_owned(),
      node_id: self.node_id.to_owned(),
      node_type: self.node_type.to_owned(),
      type_: self.type_.to_owned(),
      setting: if let Some(s) = &self.setting { serde_json::from_str(s.as_str())? } else { json!({}) },
      version: self.version,
    };
    Ok(entity)
  }
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, FromRow, ToSchema)]
#[serde(rename_all = "camelCase")]

pub struct AiPO {
  ai_id: String,
  #[mysql(rename = "type")]
  r#type: Option<String>,
  name: String,
  embedding_model: Option<String>,
  model: Option<String>,
  prompt: Option<String>,
  setting: Option<String>,
  prologue: Option<String>,
}

impl AiDAO {
  pub fn new(db: Arc<dyn DBManager>) -> Self {
    Self { db }
  }

  pub async fn get_datasheet_ids_by_ai_id(&self, ai_id: &str) -> anyhow::Result<Vec<String>> {
    let sql = format!(
      "\
      SELECT `node_id`  \
      FROM `{prefix}ai_node` \
      WHERE `ai_id` = :ai_id \
      AND `node_type` = 2 \
      AND `is_deleted` = 0",
      prefix = self.db.table_prefix(),
    );

    let params = params! {
      ai_id,
    };

    let mut client = self.db.get_client().await?;
    let result = client
      .query_all(sql, params)
      .await?
      .try_collect()
      .await
      .with_context(|| format!("get datasheet ids by ai id of {ai_id}", ai_id = ai_id))?;

    Ok(result)
  }


  pub async fn get_ai_node(&self, ai_id: &str, node_id: &str) -> anyhow::Result<Option<AiNode>> {
    let sql = format!(
      "\
      SELECT `id`, `ai_id`, `node_id`, `node_type`, `type`, `setting`, `version` \
      FROM `{prefix}ai_node` \
      WHERE `ai_id` = :ai_id \
      AND `node_id` = :node_id \
      AND `node_type` = 2 \
      AND `is_deleted` = 0",
      prefix = self.db.table_prefix(),
    );

    let params = params! {
      ai_id,
      node_id,
    };

    let mut client = self.db.get_client().await?;
    let result: Option<AiNodePO> = client.query_one(sql, params).await?;
    if let Some(ai_node_po) = result {
      let entity = ai_node_po.to_entity()?;
      return Ok(Some(entity))
    }
    Ok(None)
  }


  pub async fn get_ai_node_list(&self, ai_id: &str) -> anyhow::Result<Vec<AiNode>> {
    let sql = format!(
      "\
      SELECT `id`, `ai_id`, `node_id`, `node_type`, `type`, `setting`, `version` \
      FROM `{prefix}ai_node` \
      WHERE `ai_id` = :ai_id \
      AND `is_deleted` = 0",
      prefix = self.db.table_prefix(),
    );

    let params = params! {
      ai_id,
    };

    let mut client = self.db.get_client().await?;
    let result: Vec<AiNodePO> = client.query_all(sql, params).await?
        .try_collect()
        .await
        .with_context(|| "get ai node list")?;
    let mut entities = vec![];
    for ai_node_po in result.iter() {
      let entity = ai_node_po.to_entity()?;
      entities.push(entity)
    }
    Ok(entities)
  }


  pub async fn get_ai_node_by_id(&self, id: &str) -> anyhow::Result<Option<AiNode>> {
    let sql = format!(
      "\
      SELECT `id`, `ai_id`, `node_id`, `node_type`, `type`, `setting`, `version` \
      FROM `{prefix}ai_node` \
      WHERE `id` = :id \
      AND `is_deleted` = 0",
      prefix = self.db.table_prefix(),
    );

    let params = params! {
      id,
    };

    let mut client = self.db.get_client().await?;
    let result: Option<AiNodePO> = client.query_one(sql, params).await?;
    if let Some(ai_node_po) = result {
      let entity = ai_node_po.to_entity()?;
      return Ok(Some(entity))
    }
    Ok(None)
  }

  pub async fn get_ai(&self, ai_id: &str) -> anyhow::Result<Option<AiPO>> {
    let sql = format!(
      "\
      SELECT `ai_id`, `type`, `name`, `prologue`, `model`, `embedding_model`, `prompt`, `setting`  \
      FROM `{prefix}ai` WHERE `ai_id` = :ai_id",
      prefix = self.db.table_prefix(),
    );
    let params = params! {
      ai_id,
    };

    let mut client = self.db.get_client().await?;
    let result = client.query_one(sql, params).await?;

    Ok(result)
  }
}
