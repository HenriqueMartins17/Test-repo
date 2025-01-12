use crate::DBManager;
use anyhow::Context;
use async_trait::async_trait;

use futures::TryStreamExt;
use mysql_async::params;
use std::sync::Arc;

#[async_trait]
pub trait NodeRelRepositoryDAO: Send + Sync {
    async fn get_rel_node_id_by_main_node_id(&self, main_node_id: &str) -> anyhow::Result<Vec<String>>;
}

struct NodeRelRepositoryDAOImpl {
  repo: Arc<dyn DBManager>,
}

pub fn new_dao(repo: Arc<dyn DBManager>) -> Arc<dyn NodeRelRepositoryDAO> {
  Arc::new(NodeRelRepositoryDAOImpl { repo })
}

#[async_trait]
impl NodeRelRepositoryDAO for NodeRelRepositoryDAOImpl {
    async fn get_rel_node_id_by_main_node_id(&self, main_node_id: &str) -> anyhow::Result<Vec<String>> {
        let query = format!(
            "\
                SELECT `rel_node_id` \
                FROM `{prefix}node_rel` \
                WHERE `main_node_id` = :main_node_id \
            ",
            prefix = self.repo.table_prefix()
        );
        let mut client = self.repo.get_client().await?;
        let ids: Vec<String> = client
            .query_all(query, params! { main_node_id })
            .await?
            .try_collect()
            .await
            .with_context(|| format!("get rel_node_id ids {main_node_id}"))?;
        Ok(ids)
    }
}
