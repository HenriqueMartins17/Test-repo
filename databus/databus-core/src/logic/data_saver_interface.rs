use async_trait::async_trait;
use serde::{Deserialize, Serialize};

use crate::{ot::{changeset::{ResourceOpsCollect, Operation}, commands::{Resource, SaveResult, InternalFix}}, so::DatasheetPackContext, shared::AuthHeader};

#[async_trait(?Send)]
pub trait DataSaver {
    /// Save the OPs resulted from command execution to some storage system, such as a database or send them to the server.
    ///
    /// # Arguments
    ///
    /// * `ops` - The OPs resulted from command execution.
    /// * `options` - Save options
    ///
    /// # Returns
    ///
    /// The return value of `save_ops` will be included in the return value of the `do_command` method of `Database`.
    async fn save_ops(
        &self, 
        ops: Vec<ResourceOpsCollect>, 
        options: SaveOpsOptions,
        context: &mut DatasheetPackContext,
    ) -> anyhow::Result<Vec<SaveResult>>;
}

/// The options for saving command execution results. Implementors of `DataSaver` can derive this struct, adding necessary fields.
#[derive(Deserialize, Serialize, Debug, Clone)]
#[serde(rename_all = "camelCase")]
pub struct SaveOpsOptions {
    pub resource: Resource,
    // pub context: Rc<DatasheetPackContext>,
    pub auth: Option<AuthHeader>,
    pub internal_fix: Option<InternalFix>,
    pub prepend_ops: Option<Vec<Operation>>,
}