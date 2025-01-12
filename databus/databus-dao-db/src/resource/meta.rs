use crate::DBManager;
use anyhow::Context;
use mysql_async::params;
use std::sync::Arc;

// #[async_trait]
// pub trait ResourceMetaDAO: Send + Sync {
//   async fn get_revision_by_res_id(&self, res_id: &str) -> anyhow::Result<Option<u64>>;
// }

pub struct ResourceMetaDAO {
  repo: Arc<dyn DBManager>,
}

impl ResourceMetaDAO {
  pub fn new(repo: Arc<dyn DBManager>) -> Arc<ResourceMetaDAO> {
    return Arc::new(ResourceMetaDAO { repo });
  }

  pub async fn get_revision_by_res_id(&self, res_id: &str) -> anyhow::Result<Option<u64>> {
    let mut client = self.repo.get_client().await?;
    Ok(
      client
        .query_one(
          format!(
            "\
              SELECT `revision` \
              FROM `{prefix}resource_meta` \
              WHERE `resource_id` = :res_id AND `is_deleted` = 0\
            ",
            prefix = self.repo.table_prefix()
          ),
          params! {
            res_id
          },
        )
        .await
        .with_context(|| format!("get revision by resource id {res_id}"))?,
    )
  }
}
