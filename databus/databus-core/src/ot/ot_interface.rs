use std::collections::HashMap;

use crate::{shared::{UserBaseInfo, AuthHeader, NodePermissionSO}, so::RecordMeta};
use super::{changeset::LocalChangeset, types::ResourceType, SourceTypeEnum, ResultSet};
use serde::{Deserialize, Serialize};
use serde_json::Value;

pub const MAX_REVISION_DIFF: i32 = 100;

pub enum EffectConstantName {
    Meta,
    MetaActions,
    RecordMetaMap,
    RemoteChangeset,
    MentionedMessages,
    AttachCite,
}

impl EffectConstantName {
    pub fn to_string(&self) -> String {
        match *self {
            EffectConstantName::Meta => "meta".to_string(),
            EffectConstantName::MetaActions => "metaActions".to_string(),
            EffectConstantName::RecordMetaMap => "recordMetaMap".to_string(),
            EffectConstantName::RemoteChangeset => "remoteChangeset".to_string(),
            EffectConstantName::MentionedMessages => "mentionedMessages".to_string(),
            EffectConstantName::AttachCite => "attachCite".to_string(),
        }
    }

    pub fn as_str(&self) -> &str {
        match *self {
            EffectConstantName::Meta => "meta",
            EffectConstantName::MetaActions => "metaActions",
            EffectConstantName::RecordMetaMap => "recordMetaMap",
            EffectConstantName::RemoteChangeset => "remoteChangeset",
            EffectConstantName::MentionedMessages => "mentionedMessages",
            EffectConstantName::AttachCite => "attachCite",
        }
    }
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Default)]
#[serde(rename_all = "camelCase")]
pub struct RoomChannelMessage {
    pub room_id: String,
    pub share_id: Option<String>,
    pub source_datasheet_id: Option<String>,
    pub source_type: Option<SourceTypeEnum>,
    pub changesets: Vec<LocalChangeset>,
    pub allow_all_entrance: Option<bool>,
    pub internal_auth: Option<UserBaseInfo>,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Default)]
#[serde(rename_all = "camelCase")]
pub struct OtEventContext {
    pub auth_header: AuthHeader,
    pub space_id: String,
    pub operator_user_id: Option<String>,
    pub from_editable_shared_node: bool,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Default)]
#[serde(rename_all = "camelCase")]
pub struct CommonData {
    pub user_id: Option<String>,
    pub uuid: Option<String>,
    pub space_id: String,
    pub dst_id: String,
    pub revision: u32,
    pub resource_id: String,
    pub resource_type: ResourceType,
    pub permission: NodePermissionSO,
}

#[derive(Deserialize, Serialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct ChangesetParseResult {
    pub transaction: Value,
    pub effect_map: HashMap<String, Value>,
    pub common_data: CommonData,
    pub result_set: ResultSet,
}

#[derive(Deserialize, Serialize, Debug, Default)]
#[serde(rename_all = "camelCase")]
pub struct IRestoreRecordInfo {
    pub data: HashMap<String, Value>,
    pub record_meta: Option<RecordMeta>,
}