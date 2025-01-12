use crate::DBManager;
use anyhow::Context;
use async_trait::async_trait;
use mysql_async::params;
use std::sync::Arc;

#[async_trait]
pub trait NodeDescDAO: Send + Sync {
  async fn get_description(&self, node_id: &str) -> anyhow::Result<Option<String>>;
}

struct NodeDescDAOImpl {
  repo: Arc<dyn DBManager>,
}

pub fn new_dao(repo: Arc<dyn DBManager>) -> Arc<dyn NodeDescDAO> {
  Arc::new(NodeDescDAOImpl { repo })
}

#[async_trait]
impl NodeDescDAO for NodeDescDAOImpl {
  async fn get_description(&self, node_id: &str) -> anyhow::Result<Option<String>> {
    let mut client = self.repo.get_client().await?;
    Ok(
      client
        .query_one(
          format!(
            "\
              SELECT `description` \
              FROM `{prefix}node_desc` \
              WHERE `node_id` = :node_id\
            ",
            prefix = self.repo.table_prefix()
          ),
          params! {
            node_id
          },
        )
        .await
        .with_context(|| format!("get description of {node_id}"))?,
    )
  }
}
