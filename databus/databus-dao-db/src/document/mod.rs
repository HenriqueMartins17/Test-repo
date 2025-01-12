use std::sync::Arc;

use document::DocumentDAO;
use document_operation::DocumentOperationDAO;

pub mod document;
pub mod document_operation;

pub struct DocumentManagerDAO {
  pub document_dao: Arc<dyn DocumentDAO>,
  pub document_operation_dao: Arc<dyn DocumentOperationDAO>,
}

pub fn new_dao(
  document_dao: Arc<dyn DocumentDAO>,
  document_operation_dao: Arc<dyn DocumentOperationDAO>,
) -> Arc<DocumentManagerDAO> {
  Arc::new(DocumentManagerDAO {
    document_dao,
    document_operation_dao,
  })
}

