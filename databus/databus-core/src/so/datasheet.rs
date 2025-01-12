use std::collections::HashMap;

use serde::{Deserialize, Serialize};

use crate::config::Role;

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq)]
#[serde(rename_all = "camelCase")]
pub struct FieldRoleSetting {
    pub form_sheet_accessible: bool,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq)]
pub struct FieldPermission {
    pub editable: bool,
    pub readable: bool,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq)]
pub struct FieldPermissionInfo {
    pub role: Role,
    pub setting: FieldRoleSetting,
    pub permission: FieldPermission,
    pub manageable: bool,
}

pub type FieldPermissionMap = HashMap<String, FieldPermissionInfo>;