use serde::{Deserialize, Serialize};
use utoipa::IntoParams;

#[derive(Serialize, Deserialize, IntoParams)]
#[into_params(parameter_in = Query)]
#[serde(rename_all = "camelCase")]
pub struct GetAiNodeRO {
    pub ai_id: String,
    pub node_id: String,
}


#[derive(Serialize, Deserialize, IntoParams)]
#[into_params(parameter_in = Query)]
#[serde(rename_all = "camelCase")]
pub struct GetAiNodeListRO {
    pub ai_id: String,
}

#[derive(Serialize, Deserialize, IntoParams)]
#[into_params(parameter_in = Query)]
#[serde(rename_all = "camelCase")]
pub struct GetAiNodeByIdRO {
    pub id: String,
}