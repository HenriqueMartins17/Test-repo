use async_trait::async_trait;

use crate::{ot::{changeset::ResourceOpsCollect, commands::{SaveResult, resource_ops_to_changesets}, jot_apply}, logic::{DataSaver, SaveOpsOptions}, so::DatasheetPackContext};

#[cfg(test)]
#[derive(Clone)]
pub struct MockDataStorageProvider{
    
}

#[async_trait(?Send)]
impl DataSaver for MockDataStorageProvider {
    async fn save_ops(
        &self, 
        ops: Vec<ResourceOpsCollect>, 
        _options: SaveOpsOptions,
        context: &mut DatasheetPackContext,
    ) -> anyhow::Result<Vec<SaveResult>> {
        // let store = options.store;
        // let SaveOpsOptions { resource:_, context } = options;
        let mut changesets = resource_ops_to_changesets(ops.clone(), context);

        for cs in &mut changesets {
            jot_apply(context, cs.operations.clone());
            // if let Some(base_revision) = cs.base_revision {
            //     store.dispatch(StoreActions::UpdateRevision(base_revision + 1, cs.resource_id.clone(), cs.resource_type));
            // }
            // match cs.resource_type {
            //     ResourceType::Datasheet => {
            //         self.datasheets.insert(cs.resource_id.clone(), DatasheetPack {
            //             datasheet: selectors::get_datasheet(store.get_state())?,
            //             snapshot: selectors::get_snapshot(store.get_state())?,
            //         });
            //     },
            //     ResourceType::Dashboard => {
            //         if let Some(dashboard_pack) = self.dashboards.get_mut(&cs.resource_id) {
            //             dashboard_pack.dashboard = selectors::get_dashboard(store.get_state(), &cs.resource_id)?;
            //         }
            //     },
            // }
        }

        let mut save_results = Vec::new();
        let message_id = "jbXlnbWx7ojndVP9ATDb";
        let tmp = ops.clone();
        tmp.iter().for_each(|item| {
            let mut save_result = SaveResult {
            base_revision: 12,
            message_id: message_id.to_string(),
            ..Default::default()
            };
            save_result.resource_id = item.resource_id.clone();
            save_result.resource_type = item.resource_type.clone();
            save_result.operations = item.operations.clone();
            save_results.push(save_result);
        });
        Ok(save_results)
    }
}