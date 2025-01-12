use std::sync::Arc;

use async_trait::async_trait;
use databus_core::utils::utils::generate_u64_id;
use mysql_common::params;

use crate::db_manager::DBManager;

#[async_trait]
pub trait DocumentOperationDAO: Send + Sync {
  async fn create(&self, space_id: &str, doc_name: &str, update_data: &Vec<u8>, created_by: Option<u64>);
}

struct DocumentOperationDAOImpl {
  repo: Arc<dyn DBManager>,
}

pub fn new_dao(repo: Arc<dyn DBManager>) -> Arc<dyn DocumentOperationDAO> {
  Arc::new(DocumentOperationDAOImpl { repo })
}

#[async_trait]
impl DocumentOperationDAO for DocumentOperationDAOImpl {
  async fn create(&self, space_id: &str, doc_name: &str, update_data: &Vec<u8>, created_by: Option<u64>) {
    let mut client = self.repo.get_client().await.unwrap();
    let id = generate_u64_id();
    let sql = format!(
      "INSERT INTO {}document_operation (id, space_id, doc_name, update_data, created_by) \
      VALUES (:id, :space_id, :doc_name, :update_data, :created_by)",
      self.repo.table_prefix(),
    );
    client
      .execute(sql, params! {id, space_id, doc_name, update_data, created_by})
      .await
      .unwrap();
  }
}
