use std::sync::Arc;

use crate::data_source_provider::IDataSourceProvider;

pub struct DatasheetMetaService {
    pub loader: Arc<dyn IDataSourceProvider>,
}

impl DatasheetMetaService {
    pub fn new(
        loader: Arc<dyn IDataSourceProvider>,
    ) -> Self {
        Self {
            loader
        }
    }

    pub async fn is_field_name_exist(&self, dst_id: &str, field_name: &str) -> anyhow::Result<bool> {
        let count = self.loader.select_count_by_dst_id_and_field_name(dst_id, field_name).await?;
        Ok(count != 0)
    }
}