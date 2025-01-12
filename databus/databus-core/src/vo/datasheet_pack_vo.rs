use std::collections::HashMap;
use serde::{Deserialize, Serialize};
use serde_json::Value;
use utoipa::ToSchema;
use databus_shared::prelude::JsonExt;
use crate::prelude::{DatasheetMetaSO, NodeSO, UnitSO};
use crate::prelude::record_vo::RecordVO;

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct DatasheetSnapshotVO {
    pub meta: DatasheetMetaSO,
    pub record_map: HashMap<String, RecordVO>,
    pub datasheet_id: String,
    // pub datasheet_id: Option<String>,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct DatasheetPackVO {
    pub snapshot: DatasheetSnapshotVO,
    pub datasheet: NodeSO,

    #[serde(skip_serializing_if = "JsonExt::is_falsy")]
    pub field_permission_map: Option<Value>,

    #[serde(skip_serializing_if = "Option::is_none")]
    pub foreign_datasheet_map: Option<HashMap<String, BaseDatasheetPackVO>>,

    #[serde(skip_serializing_if = "Option::is_none")]
    pub units: Option<Vec<UnitSO>>,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct SnapshotPackVO {
    pub snapshot: DatasheetSnapshotVO,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct BaseDatasheetPackVO {
    pub snapshot: DatasheetSnapshotVO,
    pub datasheet: Value,

    #[serde(skip_serializing_if = "JsonExt::is_falsy")]
    pub field_permission_map: Option<Value>,
}