use std::{sync::Arc, collections::HashSet};

use crate::data_source_provider::IDataSourceProvider;

pub struct FusionApiRecordService {
    // changeset_source_repository: DatasheetChangesetSourceRepository,
    pub loader: Arc<dyn IDataSourceProvider>,
}

impl FusionApiRecordService {
    pub fn new(
        // changeset_source_repository: DatasheetChangesetSourceRepository
        loader: Arc<dyn IDataSourceProvider>,
    ) -> Self {
        Self {
            // changeset_source_repository,
            loader
        }
    }

    pub async fn validate_record_exists(&self, dst_id: &str, record_ids: &Vec<String>, error: &str) -> anyhow::Result<()> {
        let db_record_ids = self.loader.get_ids_by_dst_id_and_record_ids(dst_id, record_ids.clone()).await?;
        let str_err = format!("{}", error);
        if db_record_ids.is_empty() {
            // let str_err = format!("{}:{}", error, record_ids.join(", "));
            return Err(anyhow::Error::msg(str_err.clone()));
        }
        let hash_record_ids: HashSet<String> = HashSet::from_iter(record_ids.iter().cloned());
        let hash_db_record_ids: HashSet<String> = HashSet::from_iter(db_record_ids.iter().cloned());
        let diffs: HashSet<_> = hash_record_ids
            .difference(&hash_db_record_ids)
            .cloned()
            .collect();
        if !diffs.is_empty() {
            // let _diffs: Vec<String> = diffs.into_iter().map(|record| record.to_string()).collect();
            // let str_err = format!("{}:{}", error, diffs.join(", "));
            return Err(anyhow::Error::msg(str_err));
        }
        Ok(())
    }

    pub async fn validate_archived_record_includes(&self, dst_id: &str, record_ids: &Vec<String>, error: &str) -> anyhow::Result<()> {
        let archived_record_ids = self.loader.get_archived_ids_by_dst_id_and_record_ids(dst_id, record_ids.clone()).await?;
        if !archived_record_ids.is_empty() {
            let str_err = format!("{}", error);
            return Err(anyhow::Error::msg(str_err.clone()));
        }
        Ok(())
    }
}