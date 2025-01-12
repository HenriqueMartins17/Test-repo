use std::sync::Arc;

use crate::{data_source_provider::IDataSourceProvider, ot::changeset::RemoteChangeset, utils::utils::generate_u64_id};

use super::SourceTypeEnum;

pub struct DatasheetChangesetSourceService {
    // changeset_source_repository: DatasheetChangesetSourceRepository,
    pub loader: Arc<dyn IDataSourceProvider>,
}

impl DatasheetChangesetSourceService {
    pub fn new(
        // changeset_source_repository: DatasheetChangesetSourceRepository
        loader: Arc<dyn IDataSourceProvider>,
    ) -> Self {
        Self {
            // changeset_source_repository,
            loader
        }
    }

    pub async fn batch_create_changeset_source(&self, changesets: Vec<RemoteChangeset>, source_type: SourceTypeEnum, source_id: Option<String>) {
        for remote_changeset in changesets.iter() {
            let id = generate_u64_id().to_string();
            let created_by = remote_changeset.user_id.clone().unwrap();
            let dst_id = remote_changeset.resource_id.clone();
            let message_id = remote_changeset.message_id.clone();
            let source_id = source_id.clone().unwrap_or(remote_changeset.resource_id.clone());
            let source_type = source_type.clone() as u32;

            self.loader.create_new_changeset_source(&id, &created_by, &dst_id, &message_id, &source_id, &source_type).await;
        }
    }
}