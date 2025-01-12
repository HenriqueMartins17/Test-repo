use crate::DBManager;
use anyhow::Context;
use async_trait::async_trait;
use databus_shared::prelude::HashMapExt;
use futures::TryStreamExt;
use mysql_async::params;
use std::sync::Arc;

#[async_trait]
pub trait DatasheetRecordCommentDAO: Send + Sync {
  async fn get_record_comment_map_by_dst_id(&self, dst_id: &str) -> anyhow::Result<HashMapExt<String, i64>>;
}

pub struct DatasheetRecordCommentDAOImpl {
  repo: Arc<dyn DBManager>,
}

impl DatasheetRecordCommentDAOImpl {
  pub fn new(repo: Arc<dyn DBManager>) -> Arc<dyn DatasheetRecordCommentDAO> {
    Arc::new(DatasheetRecordCommentDAOImpl { repo })
  }
}
#[async_trait]
impl DatasheetRecordCommentDAO for DatasheetRecordCommentDAOImpl {
  async fn get_record_comment_map_by_dst_id(&self, dst_id: &str) -> anyhow::Result<HashMapExt<String, i64>> {
    let mut client = self.repo.get_client().await?;
    let result = client
      .query_all(
        format!(
          "\
            SELECT `record_id`, COUNT(*) AS `count` \
            FROM `{}datasheet_record_comment` \
            WHERE `dst_id` = :dst_id AND `is_deleted` = 0 \
            GROUP BY `record_id`",
          self.repo.table_prefix()
        ),
        params! {
          dst_id,
        },
      )
      .await
      .with_context(|| format!("get record comments of {dst_id}"))?
      .try_collect::<HashMapExt<String, i64>>()
      .await
      .with_context(|| format!("collect record comments of {dst_id}"))?;
    Ok(result)
  }
}
