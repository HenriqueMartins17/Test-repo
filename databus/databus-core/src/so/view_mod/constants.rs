use serde::{Deserialize, Serialize};
use utoipa::ToSchema;

#[derive(Debug, PartialEq, Eq, Clone, Copy, Serialize, Deserialize)]
#[repr(i32)]
pub enum StatType {
    None = 0,
    CountAll = 1,
    Empty = 2,
    Filled = 3,
    Unique = 4,
    PercentEmpty = 5,
    PercentFilled = 6,
    PercentUnique = 7,
    Sum = 8,
    Average = 9,
    Max = 10,
    Min = 11,
    DateRangeOfDays = 12,
    DateRangeOfMonths = 13,
    Checked = 14,
    UnChecked = 15,
    PercentChecked = 16,
    PercentUnChecked = 17,
}


#[derive(Debug, PartialEq, Clone, Serialize, Deserialize, ToSchema)]
#[serde(untagged)]
pub enum AnyBaseField {
    Integer(i32),
    String(String),
    Float(f64),
}


#[derive(Debug, PartialEq, Eq, Clone, Copy, Serialize, Deserialize)]
#[repr(i32)]
pub enum RowHeightLevel {
    Short = 1,
    Medium = 2,
    Tall = 3,
    ExtraTall = 4,
}

#[derive(Debug, Serialize, Deserialize, PartialEq, Eq, Clone, ToSchema)]
pub enum GanttColorType {
    #[serde(rename = "Custom")]
    Custom,
    #[serde(rename = "SingleSelect")]
    SingleSelect,
}


#[cfg(test)]
mod tests {
    use super::*;
    use super::AnyBaseField;

    #[test]
    fn test_any_base_field() {
        let int_field = AnyBaseField::Integer(42);
        let float_field = AnyBaseField::Float(3.14);
        let string_field = AnyBaseField::String(String::from("Hello, Rust!"));

        match int_field {
            AnyBaseField::Integer(value) => assert_eq!(value, 42),
            _ => panic!("Expected Integer variant"),
        }

        match float_field {
            AnyBaseField::Float(value) => assert_eq!(value, 3.14),
            _ => panic!("Expected Float variant"),
        }

        match string_field {
            AnyBaseField::String(value) => assert_eq!(value, "Hello, Rust!"),
            _ => panic!("Expected String variant"),
        }
    }

    #[test]
    fn test_serialization_deserialization() {
        let stat_type_variants = [
            StatType::None,
            StatType::CountAll,
            // ... other variants ...
            StatType::PercentUnChecked,
        ];

        for variant in &stat_type_variants {
            let serialized = serde_json::to_string(variant).unwrap();
            let deserialized: StatType = serde_json::from_str(&serialized).unwrap();
            assert_eq!(*variant, deserialized);
        }


        let row_height_variants = [
            RowHeightLevel::Short,
            RowHeightLevel::Medium,
            RowHeightLevel::Tall,
            RowHeightLevel::ExtraTall,
        ];

        for variant in &row_height_variants {
            let serialized = serde_json::to_string(variant).unwrap();
            let deserialized: RowHeightLevel = serde_json::from_str(&serialized).unwrap();
            assert_eq!(*variant, deserialized);
        }

        let gantt_color_variants = [GanttColorType::Custom, GanttColorType::SingleSelect];

        for variant in &gantt_color_variants {
            let serialized = serde_json::to_string(variant).unwrap();
            let deserialized: GanttColorType = serde_json::from_str(&serialized).unwrap();
            assert_eq!(*variant, deserialized);
        }
    }

}

