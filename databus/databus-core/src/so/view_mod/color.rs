use serde::{Serialize, Deserialize};
use utoipa::ToSchema;
use crate::prelude::constants::GanttColorType;

#[derive(Debug, PartialEq, Eq, Clone, Serialize, Deserialize, ToSchema)]
pub struct  GanttColorOption {
    #[serde(flatten)]
    base: ColorOption,
    r#type: GanttColorType,
}

#[derive(Debug, PartialEq, Eq, Clone, Serialize, Deserialize, ToSchema)]
pub struct ColorOption {
    field_id: Option<String>,
    color: Option<i32>,
}

// Unit test for GanttColor serialization
#[cfg(test)]
mod tests {
    use super::*;
    

    #[test]
    fn test_gantt_color_serialization() {
        let gantt_color = GanttColorOption {
            r#type: GanttColorType::Custom,
            base: ColorOption {
                field_id: Some("example_field".to_string()),
                color: Some(123456),
            },
        };

        let serialized_gantt_color = serde_json::to_string(&gantt_color).unwrap();

        assert_eq!(
            serialized_gantt_color,
            r#"{"field_id":"example_field","color":123456,"type":"Custom"}"#
        );
    }
}
