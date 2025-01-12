use crate::DBManager;
use async_trait::async_trait;
use mysql_async::params;
use serde_json::Value;
use std::sync::Arc;

/// NOTE Separate this service from DatasheetDAO to avoid circular dependency.
#[async_trait]
pub trait DatasheetChangesetDAO: Send + Sync {
    async fn create_new_changeset(
        &self,
        id: &str,
        message_id: &str,
        dst_id: &str,
        member_id: &str,
        operations: Value,
        revision: &u32,
    );

    async fn create_new_changeset_source(
        &self,
        id: &str,
        created_by: &str,
        dst_id: &str,
        message_id: &str,
        source_id: &str,
        source_type: &u32
    );
}

struct DatasheetChangesetDAOImpl {
  repo: Arc<dyn DBManager>,
}

pub fn new_dao(repo: Arc<dyn DBManager>) -> Arc<dyn DatasheetChangesetDAO> {
  Arc::new(DatasheetChangesetDAOImpl { repo })
}

#[async_trait]
impl DatasheetChangesetDAO for DatasheetChangesetDAOImpl {
    async fn create_new_changeset(
        &self,
        id: &str,
        message_id: &str,
        dst_id: &str,
        member_id: &str,
        operations: Value,
        revision: &u32,
    ) {
        let mut client = self.repo.get_client().await.unwrap();
        let sql = format!(
            "
            INSERT INTO {}datasheet_changeset \
            (id, message_id, dst_id, member_id, operations, revision, created_by, updated_by) \
            VALUES (:id, :message_id, :dst_id, :member_id, :operations, :revision, :created_by, :updated_by)",
            self.repo.table_prefix()
        );
        let created_by = member_id;
        let updated_by = member_id;
        client.execute(sql, params! {id, message_id, dst_id, member_id, operations, revision, created_by, updated_by}).await.unwrap()
    }

    async fn create_new_changeset_source(
        &self,
        id: &str,
        created_by: &str,
        dst_id: &str,
        message_id: &str,
        source_id: &str,
        source_type: &u32
    ) {
        let mut client = self.repo.get_client().await.unwrap();
        let sql = format!(
            "
            INSERT INTO {}datasheet_changeset_source \
            (id, created_by, dst_id, resource_id, message_id, source_id, source_type) \
            VALUES (:id, :created_by, :dst_id, :resource_id, :message_id, :source_id, :source_type)",
            self.repo.table_prefix()
        );
        let resource_id = dst_id;
        client.execute(sql, params! {id, created_by, dst_id, resource_id, message_id, source_id, source_type}).await.unwrap()
    }
}
