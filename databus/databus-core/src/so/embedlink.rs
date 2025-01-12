use serde::{Deserialize, Serialize};

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct EmbedLinkEntitySO {
    pub space_id: String,
    pub node_id: String,
    pub embed_link_id: String,
    pub props: Option<IEmbedLinkProperty>,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct IEmbedLinkProperty {
    pub payload: Option<IEmbedLinkPropertyPayload>,
    pub theme: Option<String>,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct IEmbedLinkPropertyPayload {
    pub primary_side_bar: Option<IEmbedLinkPayloadPrimarySideBar>,
    pub view_control: Option<IEmbedLinkPayloadViewControl>,
    pub banner_logo: Option<bool>,
    pub permission_type: Option<String>,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct IEmbedLinkPayloadPrimarySideBar {
    pub collapsed: Option<bool>,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct IEmbedLinkPayloadViewControl {
    pub view_id: Option<String>,
    pub tab_bar: Option<bool>,
    pub tool_bar: Option<IEmbedLinkPayloadViewToolBar>,
    pub collapsed: Option<bool>,
}

#[derive(Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct IEmbedLinkPayloadViewToolBar {
    pub basic_tools: Option<bool>,
    pub share_btn: Option<bool>,
    pub widget_btn: Option<bool>,
    pub api_btn: Option<bool>,
    pub form_btn: Option<bool>,
    pub history_btn: Option<bool>,
    pub robot_btn: Option<bool>,
}