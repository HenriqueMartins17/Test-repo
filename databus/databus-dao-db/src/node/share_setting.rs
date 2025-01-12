use crate::DBManager;
use crate::rest::RestDAO;
use anyhow::Context;
use async_trait::async_trait;
use databus_shared::prelude::*;

use mysql_async::params;
use std::sync::Arc;

#[async_trait]
pub trait NodeShareSettingDAO: Send + Sync {
  async fn get_share_status_by_node_id(&self, node_id: &str) -> anyhow::Result<bool>;

  async fn get_share_props(&self, node_id: &str, share_id: &str) -> anyhow::Result<Option<Json>>;
}

struct NodeShareSettingDAOImpl {
  repo: Arc<dyn DBManager>,
  rest_dao: Arc<dyn RestDAO>,
}

pub fn new_dao(repo: Arc<dyn DBManager>, rest_dao: Arc<dyn RestDAO>) -> Arc<dyn NodeShareSettingDAO> {
  Arc::new(NodeShareSettingDAOImpl { repo, rest_dao })
}

#[async_trait]
impl NodeShareSettingDAO for NodeShareSettingDAOImpl {
  async fn get_share_status_by_node_id(&self, node_id: &str) -> anyhow::Result<bool> {
    let mut client = self.repo.get_client().await?;

    Ok(
      client
        .query_one(
          format!(
            "\
            SELECT `is_enabled` \
            FROM `{prefix}node_share_setting` \
            WHERE `node_id` = :node_id\
            ",
            prefix = self.repo.table_prefix()
          ),
          params! { node_id },
        )
        .await
        .with_context(|| format!("get share status {node_id}"))?
        .is_truthy(),
    )
  }

  async fn get_share_props(&self, node_id: &str, share_id: &str) -> anyhow::Result<Option<Json>> {
    let mut client = self.repo.get_client().await?;
    let share_setting = client
      .query_one(
        format!(
          "\
            SELECT `node_id`, `is_enabled`, `props` \
            FROM `{prefix}node_share_setting` \
            WHERE `share_id` = :share_id",
          prefix = self.repo.table_prefix()
        ),
        params! {
          share_id,
        },
      )
      .await
      .with_context(|| format!("get share setting of node {node_id}, share {share_id}"))?;
    let Some((share_node_id, is_enabled, props)): Option<(String, Option<bool>, Option<Json>)> = share_setting else {
      return Ok(None);
    };
    if !is_enabled.is_truthy() {
      return Ok(None);
    }
    // Check if the node enables sharing
    if share_node_id == node_id {
      return Ok(props);
    }
    // Check if the node has children nodes
    let status = self
        .rest_dao
        .get_node_contains_status(&share_node_id, node_id)
        .await
        .with_context(|| format!("get node contains status between {share_node_id} and {share_id}"))?;
    if status {
      return Ok(props);
    }
    Ok(None)
  }
}
