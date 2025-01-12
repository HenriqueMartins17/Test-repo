use serde::{Deserialize, Serialize};
use std::collections::HashMap;
use serde_json::Value;

use crate::so::FieldPermissionMap;

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq)]
#[serde(rename_all = "camelCase")]
pub struct UserBaseInfo {
  pub user_id: String,
  pub uuid: String,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Default)]
#[serde(rename_all = "camelCase")]
pub struct AuthHeader {
  pub cookie: Option<String>,
  pub token: Option<String>,
  pub internal: Option<bool>,
  pub space_id: Option<String>,
  pub user_id: Option<String>,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Default)]
#[serde(rename_all = "camelCase")]
pub struct NodePermissionSO {
  pub has_role: bool,
  pub user_id: Option<String>,
  pub uuid: Option<String>,
  pub role: String,
  pub node_favorite: Option<bool>,
  pub field_permission_map: Option<FieldPermissionMap>,
  pub is_ghost_node: Option<bool>,
  pub is_deleted: Option<bool>,
  // #[serde(skip_serializing_if = "Option::is_none")]
  pub editable: Option<bool>,
  pub field_creatable: Option<bool>,
  pub field_renamable: Option<bool>,
  pub field_property_editable: Option<bool>,
  pub field_removable: Option<bool>,
  pub view_creatable: Option<bool>,
  pub view_removable: Option<bool>,
  pub view_movable: Option<bool>,
  pub view_renamable: Option<bool>,
  pub view_filterable: Option<bool>,
  pub field_groupable: Option<bool>,
  pub column_sortable: Option<bool>,
  pub row_high_editable: Option<bool>,
  pub row_creatable: Option<bool>,
  pub row_removable: Option<bool>,
  pub row_sortable: Option<bool>,
  pub column_hideable: Option<bool>,
  pub field_sortable: Option<bool>,
  pub column_width_editable: Option<bool>,
  pub column_count_editable: Option<bool>,
  pub view_layout_editable: Option<bool>,
  pub view_style_editable: Option<bool>,
  pub view_key_field_editable: Option<bool>,
  pub view_color_option_editable: Option<bool>,
  pub manageable: Option<bool>,
  pub cell_editable: Option<bool>,
  pub row_unarchivable: Option<bool>,
  pub row_archivable: Option<bool>,
  pub readable: Option<bool>,
}

#[derive(Debug, Clone, PartialEq, Deserialize, Serialize, Default)]
#[serde(rename_all = "camelCase")]
pub struct IUserInfo {
  pub active_node_id: String,
  pub space_id: String,
  pub active_view_id: String,
  pub space_name: String,
  pub avatar: String,
  pub avatar_color: Option<i32>,
  pub email: String,
  pub last_login_time: i64,
  pub sign_up_time: i64,
  pub area_code: String,
  pub mobile: String,
  pub need_create: bool,
  pub nick_name: String,
  pub is_nick_name_modified: bool,
  pub space_logo: String,

  pub uuid: String,
  pub user_id: String, // 注意：这里可能需要根据实际情况进行更改，如果 user_id 已弃用
  pub member_id: String,
  pub member_name: String,
  pub is_member_name_modified: bool,
  pub is_new_comer: bool,
  pub is_paused: bool,
  pub user_logout_status: Option<String>, // 可能需要更改成枚举类型
  pub user_can_logout: Option<bool>,
  pub close_at: Option<String>,
  pub is_deleted: Option<bool>,
  pub is_admin: bool,
  pub is_main_admin: bool,
  pub is_del_space: bool,
  pub need_pwd: bool,
  pub third_party_information: Vec<Value>, // 请定义 IBindList 结构体
  pub used_invite_reward: bool,
  pub api_key: String,
  pub wizards: HashMap<String, i32>, // 注意：这里使用 HashMap 表示 wizards
  pub unit_id: String,
  pub invite_code: Option<String>,
  pub space_domain: Option<String>,
  pub send_subscription_notify: bool,
  pub time_zone: Option<String>,
  pub locale: Option<String>,
  pub active_node_name: Option<String>,
  pub is_name_modified: Option<bool>,
}

#[cfg(test)]
mod test {
  use crate::shared::IUserInfo;

  #[test]
  pub fn test_deserialize() {
    let json = r#"{
        "userId": "082476dc25834402ba979d62804480cd",
        "uuid": "082476dc25834402ba979d62804480cd",
        "nickName": "pengcheng",
        "areaCode": "+86",
        "mobile": "18819489154",
        "email": "pengcheng@apitable.com",
        "avatar": "http://127.0.0.1:9000/assets/public/2023/09/15/56e3eed6c09d4d4a9a7f64fc9ac30a51",
        "signUpTime": 1691408557000,
        "lastLoginTime": 1700815179000,
        "thirdPartyInformation": [],
        "needPwd": false,
        "needCreate": false,
        "spaceId": "spc2qi5CvEWqw",
        "spaceName": "测试",
        "spaceLogo": "",
        "memberId": "1688817023380148225",
        "memberName": "pengcheng",
        "unitId": "1688817023401119745",
        "activeNodeId": "",
        "activeViewId": "",
        "activeNodePos": 0,
        "isAdmin": true,
        "isMainAdmin": true,
        "isPaused": false,
        "closeAt": null,
        "isDelSpace": false,
        "apiKey": "usk6cqNGCYv9OdtRWeAFbEi",
        "wizards": {
            "88": 1,
            "24": 1,
            "35": 1,
            "46": 1,
            "14": 1,
            "29": 1,
            "19": 1,
            "1": 1,
            "4": 1,
            "106": 1,
            "50": 1,
            "51": 1,
            "84": 1,
            "95": 1,
            "30": 1,
            "52": 1,
            "96": 1,
            "31": 1,
            "64": 4,
            "75": 703
        },
        "inviteCode": null,
        "spaceDomain": null,
        "isNameModified": null,
        "isNewComer": false,
        "isNickNameModified": true,
        "isMemberNameModified": true,
        "sendSubscriptionNotify": true,
        "usedInviteReward": false,
        "avatarColor": null,
        "timeZone": "Asia/Shanghai",
        "locale": "zh-CN"
    }"#;
    let user: IUserInfo = serde_json::from_str(json).unwrap();
  }
}
