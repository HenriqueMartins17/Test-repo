use crate::DBManager;
use anyhow::Context;
use async_trait::async_trait;
use mysql_async::params;
use std::sync::Arc;

/// NOTE Separate this service from DatasheetDAO to avoid circular dependency.
#[async_trait]
pub trait DatasheetRevisionDAO: Send + Sync {
  async fn get_revision_by_dst_id(&self, dst_id: &str) -> anyhow::Result<Option<u64>>;
  async fn update_revision_by_dst_id(&self, dst_id: &str, revision: &u32, updated_by: &str);
}

struct DatasheetRevisionDAOImpl {
  repo: Arc<dyn DBManager>,
}

pub fn new_dao(repo: Arc<dyn DBManager>) -> Arc<dyn DatasheetRevisionDAO> {
  Arc::new(DatasheetRevisionDAOImpl { repo })
}

#[async_trait]
impl DatasheetRevisionDAO for DatasheetRevisionDAOImpl {
  async fn get_revision_by_dst_id(&self, dst_id: &str) -> anyhow::Result<Option<u64>> {
    let mut client = self.repo.get_client().await?;
    Ok(
      client
        .query_one(
          format!(
            "\
              SELECT `revision` \
              FROM `{prefix}datasheet` \
              WHERE `dst_id` = :dst_id AND `is_deleted` = 0\
            ",
            prefix = self.repo.table_prefix()
          ),
          params! {
            dst_id
          },
        )
        .await
        .with_context(|| format!("get revision by dst id {dst_id}"))?,
    )
  }

  async fn update_revision_by_dst_id(&self, dst_id: &str, revision: &u32, updated_by: &str) {
    let mut client = self.repo.get_client().await.unwrap();
    let sql = format!(
      "\
        UPDATE `{prefix}datasheet` \
        SET `revision` = :revision, `updated_by` = :updated_by \
        WHERE `dst_id` = :dst_id AND `is_deleted` = 0\
      ",
      prefix = self.repo.table_prefix()
    );
    client.execute(sql, params!{dst_id, revision, updated_by}).await.unwrap()
  }
}
