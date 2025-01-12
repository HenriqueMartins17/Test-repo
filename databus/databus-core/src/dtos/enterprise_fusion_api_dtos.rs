use serde::{Deserialize, Serialize};
use utoipa::ToSchema;
use validator::Validate;

#[derive(Debug, Deserialize, Serialize, ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct WidgetDto {
    pub widget_id: Option<String>,
    pub name: Option<String>,
    pub layout: Option<String>,
}

#[derive(Debug, Deserialize, Serialize, Validate)]
pub struct WidgetLayout {
    pub x: i32,
    pub y: i32,
    #[validate(range(min = 1))]
    pub width: i32,
    #[validate(range(min = 1))]
    pub height: i32,
}

#[derive(Debug, Deserialize, Serialize, ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct EmbedLinkDto {
    pub link_id: String,
    pub url: String,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct TeamPageDto {
    pub teams: Vec<TeamDto>,
    pub total: i32,
    pub page_size: i32,
    pub page_num: i32,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct TeamDto {
    pub unit_id: String,
    pub name: String,
    pub sequence: Option<u32>,
    pub parent_unit_id: Option<String>,
    pub roles: Option<Vec<RoleDto>>,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct RoleDto {
    pub unit_id: String,
    pub name: String,
    pub sequence: Option<i32>,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct TeamDetailDto{
    pub team: TeamDto,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct MemberPageDto {
    pub members: Vec<MemberDto>,
    pub total: i32,
    pub page_size: i32,
    pub page_num: i32,
}

#[derive(Debug, Serialize, Deserialize, Default)]
#[serde(rename_all = "camelCase")]
pub struct MemberDto {
    pub unit_id: String,
    pub name: String,
    pub avatar: Option<String>,
    pub status: i32,
    pub member_type: String,
    pub email: Option<String>,
    pub mobile: Option<MemberMobile>,
    pub teams: Option<Vec<TeamDto>>,
    pub roles: Option<Vec<RoleDto>>,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct MemberMobile {
    pub number: String,
    pub area_code: String,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct RolePageDto {
    pub roles: Vec<RoleDto>,
    pub total: i32,
    pub page_size: i32,
    pub page_num: i32,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct RoleDetailDto {
    pub role: RoleDto,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct RoleUnitDetailDto {
    pub members: Vec<MemberDto>,
    pub teams: Vec<TeamDto>,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct MemberDetailDto {
    pub member: MemberDto,
}