use serde::{Deserialize, Serialize};
use serde_json::Value;
use utoipa::ToSchema;



#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct WidgetPanelSO {
  pub id: String,
  pub name: Option<String>,
  pub widgets: Vec<WidgetInPanelSO>,
  /// Don't care about other fields currently, but they must still be serialized.
  #[serde(flatten)]
  pub others: Option<Value>,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct WidgetInPanelSO {
  pub id: String,
  pub height: f64,
  pub y: Option<f64>,
  /// Don't care about other fields currently, but they must still be serialized.
  #[serde(flatten)]
  pub others: Option<Value>,
}
