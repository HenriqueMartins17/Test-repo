use std::collections::HashMap;

use serde::{Deserialize, Serialize};

#[derive(Deserialize, Serialize, Debug)]
pub enum BindAccount {
    DINGDING,
    WECHAT,
    QQ,
    WECOM
}

#[derive(Deserialize, Serialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct BindList {
    pub r#type: BindAccount,
    pub create_time: String,
    pub nick_name: String,
}

#[derive(Deserialize, Serialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct UserInfoSO {
    pub active_node_id: String,
    pub space_id: String,
    pub active_view_id: String,
    pub space_name: String,
    pub avatar: String,
    pub avatar_color: Option<i32>,
    pub email: String,
    pub last_login_time: String,
    pub sign_up_time: String,
    pub area_code: String,
    pub mobile: String,
    pub need_create: bool,
    pub nick_name: String,
    pub is_nick_name_modified: bool,
    pub space_logo: String,
    pub uuid: String, // user global ID
    pub user_id: String, // this is deprecated but compatible, name mistake before 
    pub member_id: String, // member id (user in the space as member)
    pub member_name: String,
    pub is_member_name_modified: bool,
    pub is_newcomer: bool,
    pub is_paused: bool,
    pub user_logout_status: Option<String>,
    pub user_can_logout: Option<bool>,
    pub close_at: Option<String>,
    /**
     * whether the account has been deleted or logged out (`true` for the cooling period)
     */
    pub is_deleted: Option<bool>,
    pub is_admin: bool,
    pub is_main_admin: bool,
    pub is_del_space: bool,
    pub need_pwd: bool,
    pub third_party_information: Vec<BindList>,
    /**
     * whether get award from invite code
     */
    pub used_invite_reward: bool,
    pub api_key: String,
    pub wizards: HashMap<i32, i32>,
    pub unit_id: String,
    pub invite_code: String,
    /**
     * the domain for the space
     */
    pub space_domain: String,
    /**
     * a global switch.
     * whether permits to send subscription notification message
     */
    pub send_subscription_notify: bool,
    pub time_zone: Option<String>,
    pub locale: Option<String>,
}