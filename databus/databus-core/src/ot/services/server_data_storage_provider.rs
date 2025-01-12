use std::sync::Arc;

use async_trait::async_trait;
use serde_json::{json, from_value};

use crate::{ot::{changeset::{ResourceOpsCollect, LocalChangeset, RemoteChangeset, Operation}, commands::{SaveResult, InternalFix, resource_ops_to_changesets}, RoomChannelMessage, jot_apply}, shared::AuthHeader, logic::{DataSaver, SaveOpsOptions}, so::DatasheetPackContext};
use serde::{Deserialize, Serialize};

use super::{OtService, DatasheetChangesetSourceService};


/// Source Type
#[derive(Deserialize, Serialize, Debug, Clone, PartialEq)]
pub enum SourceTypeEnum {
    /// FORM
    FORM = 0,
    /// Open platform API
    OpenApi = 1,
    /// effect of relationship (duplicate or delete node)
    RelationEffect = 2,
    /// Mirror
    MIRROR = 3,
}

pub struct ServerDataStorageProvider{
    pub ot_service: Arc<OtService>,
    pub changeset_source_service: DatasheetChangesetSourceService,
}

#[async_trait(?Send)]
impl DataSaver for ServerDataStorageProvider {
    async fn save_ops(
        &self, 
        ops: Vec<ResourceOpsCollect>, 
        options: SaveOpsOptions,
        context: &mut DatasheetPackContext,
    ) -> anyhow::Result<Vec<SaveResult>> {
        //internal_fix = None
        let SaveOpsOptions { resource, auth, internal_fix, prepend_ops  } = options;
        let mut changesets = resource_ops_to_changesets(ops.clone(), context);
        // println!("changesets: {:?}", changesets);
        if !prepend_ops.is_none() { //empty
            self.comb_change_sets_op(&mut changesets, resource.id.to_string(), prepend_ops.unwrap());
        }
        // let apply_changesets = true;
        // if apply_changesets {

        // }
        if auth.is_some(){
            let result = self.apply_datasheet_changesets(resource.id, changesets, auth.unwrap(), internal_fix).await?;
            for cs in &result {
                jot_apply(context, cs.operations.clone());
            }
        }

        //tmp result
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

impl ServerDataStorageProvider {
    pub fn new(
        ot_service: Arc<OtService>,
        changeset_source_service: DatasheetChangesetSourceService,
    ) -> Self {
        Self {
            ot_service,
            changeset_source_service,
        }
    }
    fn comb_change_sets_op(
        &self,
        change_sets: &mut Vec<LocalChangeset>, 
        dst_id: String, 
        update_field_operations: Vec<Operation>
    ) {
        // If nothing is changed, change_sets is an empty array. There is nothing to do.
        if change_sets.is_empty() || update_field_operations.is_empty(){
            return;
        }
        if let Some(this_resource_change_set) = change_sets.iter_mut().find(|cs| cs.resource_id == dst_id) {
            this_resource_change_set.operations.append(&mut update_field_operations.clone());
        } else {
            // Why can't I find resources?
            let error_info = format!(
                "API_INFO: changeSets: {:?}, dstId: {:?}, updateFieldOperations: {:?}",
                change_sets, dst_id, update_field_operations
            );
            panic!("ApiException: {}", error_info);
        }
    }

    ///
    ///  * Call otServer to apply changesets to datasheet
    ///  *
    ///  * @param dstId         datasheet id
    ///  * @param changesets    array of changesets
    ///  * @param auth          authorization info (developer token)
    ///  * @param internalFix   [optional] use when repairing data
    ///
    async fn apply_datasheet_changesets(
        &self,
        dst_id: String,
        changesets: Vec<LocalChangeset>,
        auth: AuthHeader,
        internal_fix: Option<InternalFix>,
    ) -> anyhow::Result<Vec<RemoteChangeset>> {
        let mut apply_auth = auth;
        let mut message = json!({
            "roomId": dst_id,
            "changesets": changesets,
            "sourceType": SourceTypeEnum::OpenApi,
        });
    
        if let Some(internal_fix) = internal_fix {
            if !internal_fix.anonymou_fix.is_none() {
                // Internal repair: anonymous repair
                message["internalAuth"] = json!({"userId": null, "uuid": null});
                apply_auth = AuthHeader { internal: Some(true), ..Default::default() };
            } else if let Some(fix_user) = internal_fix.fix_user {
                // Internal fix: Designated user
                message["internalAuth"] = json!({"userId": fix_user.user_id, "uuid": fix_user.uuid});
                apply_auth = AuthHeader { internal: Some(true), ..Default::default() };
            }
        }
    
        let message: RoomChannelMessage = from_value(message).unwrap();
        // println!("message: {:?}", message);
        // println!("apply_auth: {:?}", apply_auth);
        let user_id = apply_auth.user_id.clone().unwrap();
        let change_result = self.ot_service.apply_room_changeset(message, apply_auth).await?;
        // println!("change_result = {:?}", change_result);
        self.changeset_source_service
            .batch_create_changeset_source(change_result.clone(), SourceTypeEnum::OpenApi, None)
            .await;
        self.ot_service.nest_room_change(dst_id, change_result.clone(), user_id).await;
    
        Ok(change_result)
        // Ok(Vec::new())
    }
}