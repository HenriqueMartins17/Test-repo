use serde_json::Value;

use crate::fields::base_field::IBaseField;
use crate::so::view_operation::filter::FOperator;
use crate::so::{FieldKindSO, FieldSO};

use super::formula_field::Formula;
use super::{Attachment, Checkbox, Currency, DateTime, Email, Link, MultiSelect, Percent, Phone, Rating, SingleSelect, Text, URL, SingleText, OneWayLinkField, LookUp, Member, LastModifiedTime, LastModifiedBy, ButtonField, CreatedTime, SelectField};
use super::Number;
use super::property::field_types::{BasicValueType, IComputedFieldFormattingProperty, IDateTimeFieldPropertyFormat, INumberBaseFieldPropertyFormat};
use super::property::{FieldPropertySO, IButtonStyle, ButtonStyleType, DateFormat, TimeFormat, SymbolAlign, CollectType, IButtonAction};

#[derive(Debug, Clone, Default)]
pub struct ValidationResult {
    pub error: Option<String>,
    pub warning: Option<String>,
    pub value: Option<Value>,
}

pub fn bind_field_context(field: FieldSO) -> BasicValueType {
    match field.kind {
        FieldKindSO::SingleSelect => SingleSelect::new(field).basic_value_type(),
        FieldKindSO::MultiSelect => MultiSelect::new(field).basic_value_type(),
        FieldKindSO::Currency => Currency::new(field).basic_value_type(),
        FieldKindSO::Percent => Percent::new(field).basic_value_type(),
        FieldKindSO::Text => Text::new(field).basic_value_type(),
        FieldKindSO::Attachment => Attachment::new(field).basic_value_type(),
        FieldKindSO::URL => URL::new(field).basic_value_type(),
        FieldKindSO::Email => Email::new(field).basic_value_type(),
        FieldKindSO::Rating => Rating::new(field).basic_value_type(),
        FieldKindSO::Checkbox => Checkbox::new(field).basic_value_type(),
        FieldKindSO::Phone => Phone::new(field).basic_value_type(),
        _ => BasicValueType::String,
    }
}

pub fn get_is_computed(field: FieldSO) -> bool {
    match field.kind {
        FieldKindSO::SingleSelect => SingleSelect::new(field).is_computed(),
        FieldKindSO::MultiSelect => MultiSelect::new(field).is_computed(),
        FieldKindSO::Currency => Currency::new(field).is_computed(),
        FieldKindSO::Percent => Percent::new(field).is_computed(),
        FieldKindSO::Text => Text::new(field).is_computed(),
        FieldKindSO::Attachment => Attachment::new(field).is_computed(),
        FieldKindSO::URL => URL::new(field).is_computed(),
        FieldKindSO::Email => Email::new(field).is_computed(),
        FieldKindSO::Rating => Rating::new(field).is_computed(),
        FieldKindSO::Checkbox => Checkbox::new(field).is_computed(),
        FieldKindSO::Phone => Phone::new(field).is_computed(),
        _=>false,
    }
}

pub fn validate_property (field: FieldSO) -> ValidationResult {
    match field.kind {
        // FieldKindSO::SingleSelect => SingleSelect::new(field).is_computed(),
        _ => ValidationResult{..Default::default()},
    }
}

pub fn get_field_default_property(field_type: FieldKindSO) -> Option<FieldPropertySO> {
    match field_type {
        FieldKindSO::Attachment => None,
        FieldKindSO::AutoNumber => Some(FieldPropertySO {
            next_id: Some(0),
            view_idx: Some(0),
            datasheet_id: Some(String::new()),
            ..Default::default()
        }),
        FieldKindSO::Button => Some(FieldPropertySO {
            datasheet_id: Some(String::new()),
            text: Some("Click to Start".to_string()),
            style: Some(IButtonStyle {
                r#type: ButtonStyleType::Background,
                color: 50,
            }),
            action: Some(IButtonAction::default()),
            ..Default::default()
        }),
        FieldKindSO::Cascader => Some(FieldPropertySO {
            show_all: Some(false),
            linked_datasheet_id: Some(String::new()),
            linked_view_id: Some(String::new()),
            linked_fields: Some(Vec::new()),
            full_linked_fields: Some(Vec::new()),
            ..Default::default()
        }),
        FieldKindSO::Checkbox => Some(FieldPropertySO {
            icon: Some("white_check_mark".to_string()),
            ..Default::default()
        }),
        FieldKindSO::CreatedBy => Some(FieldPropertySO {
            uuids: Some(Vec::new()),
            datasheet_id: Some(String::new()),
            subscription: Some(false),
            ..Default::default()
        }),
        FieldKindSO::CreatedTime => Some(FieldPropertySO {
            datasheet_id: Some(String::new()),
            date_format: Some(DateFormat::SYyyyMmDd),
            time_format: Some(TimeFormat::Hhmm),
            include_time: Some(false),
            ..Default::default()
        }),
        FieldKindSO::Currency => Some(FieldPropertySO {
            symbol: Some("$".to_string()),
            precision: Some(2),
            symbol_align: Some(SymbolAlign::Default),
            ..Default::default()
        }),
        FieldKindSO::DateTime => Some(FieldPropertySO {
            date_format: Some(DateFormat::SYyyyMmDd),
            time_format: Some(TimeFormat::Hhmm),
            include_time: Some(false),
            auto_fill: Some(false),
            ..Default::default()
        }),
        FieldKindSO::Email => None,
        FieldKindSO::Formula => Some(FieldPropertySO {
            expression: Some(String::new()),
            datasheet_id: Some(String::new()),
            ..Default::default()
        }),
        FieldKindSO::LastModifiedBy => Some(FieldPropertySO {
            uuids: Some(Vec::new()),
            datasheet_id: Some(String::new()),
            collect_type: Some(CollectType::AllFields),
            field_id_collection: Some(Vec::new()),
            ..Default::default()
        }),
        FieldKindSO::LastModifiedTime => Some(FieldPropertySO {
            date_format: Some(DateFormat::SYyyyMmDd),
            time_format: Some(TimeFormat::Hhmm),
            include_time: Some(false),
            collect_type: Some(CollectType::AllFields),
            field_id_collection: Some(Vec::new()),
            datasheet_id: Some(String::new()),
            ..Default::default()
        }),
        FieldKindSO::Link => Some(FieldPropertySO::default()),
        FieldKindSO::LookUp => Some(FieldPropertySO {
            datasheet_id: Some(String::new()),
            related_link_field_id: Some(String::new()),
            look_up_target_field_id: Some(String::new()),
            ..Default::default()
        }),
        FieldKindSO::Member => Some(FieldPropertySO {
            is_multi: Some(true),
            should_send_msg: Some(true),
            subscription: Some(false),
            unit_ids: Some(Vec::new()),
            ..Default::default()
        }),
        FieldKindSO::Number => Some(FieldPropertySO {
            precision: Some(0),
            symbol_align: Some(SymbolAlign::Right),
            ..Default::default()
        }),
        FieldKindSO::Percent => Some(FieldPropertySO {
            precision: Some(0),
            ..Default::default()
        }),
        FieldKindSO::OneWayLink => Some(FieldPropertySO::default()),
        FieldKindSO::Phone => None,
        FieldKindSO::Rating => Some(FieldPropertySO {
            icon: Some("star".to_string()),
            max: Some(5),
            ..Default::default()
        }),
        FieldKindSO::SingleText => Some(FieldPropertySO::default()),
        FieldKindSO::Text => None,
        FieldKindSO::URL => Some(FieldPropertySO {
            is_recog_url_flag: Some(false),
            ..Default::default()
        }),
        FieldKindSO::WorkDoc => None,
        FieldKindSO::SingleSelect => Some(FieldPropertySO {options: Some(Vec::new()), ..Default::default()}),
        FieldKindSO::DeniedField => None,
        FieldKindSO::NotSupport => None,
        _ => None,
    }
}

pub fn get_field_type_by_string(field_type: &str) -> FieldKindSO {
    match field_type {
        "Text" => FieldKindSO::Text,
        "Number" => FieldKindSO::Number,
        "SingleSelect" => FieldKindSO::SingleSelect,
        "MultiSelect" => FieldKindSO::MultiSelect,
        "DateTime" => FieldKindSO::DateTime,
        "Attachment" => FieldKindSO::Attachment,
        "Link" => FieldKindSO::Link,
        "URL" => FieldKindSO::URL,
        "Email" => FieldKindSO::Email,
        "Phone" => FieldKindSO::Phone,
        "Checkbox" => FieldKindSO::Checkbox,
        "Rating" => FieldKindSO::Rating,
        "Currency" => FieldKindSO::Currency,
        "Percent" => FieldKindSO::Percent,
        "Member" => FieldKindSO::Member,
        "LookUp" => FieldKindSO::LookUp,
        "Formula" => FieldKindSO::Formula,
        "SingleText" => FieldKindSO::SingleText,
        "AutoNumber" => FieldKindSO::AutoNumber,
        "CreatedTime" => FieldKindSO::CreatedTime,
        "LastModifiedTime" => FieldKindSO::LastModifiedTime,
        "CreatedBy" => FieldKindSO::CreatedBy,
        "LastModifiedBy" => FieldKindSO::LastModifiedBy,
        "Cascader" => FieldKindSO::Cascader,
        "OneWayLink" => FieldKindSO::OneWayLink,
        "WorkDoc" => FieldKindSO::WorkDoc,
        "Button" => FieldKindSO::Button,
        _ => FieldKindSO::NotSupport,
    }
}

pub fn validate_add_open_field_property(add_property: Value, kind: FieldKindSO) -> anyhow::Result<()> {
    match &kind {
        FieldKindSO::SingleText => SingleText::validate_add_open_field_property(add_property),
        FieldKindSO::SingleSelect => SingleSelect::validate_add_open_field_property(add_property),
        FieldKindSO::MultiSelect => MultiSelect::validate_add_open_field_property(add_property),
        FieldKindSO::Number => Number::validate_add_open_field_property(add_property),
        FieldKindSO::Currency => Currency::validate_add_open_field_property(add_property),
        FieldKindSO::Percent => Percent::validate_add_open_field_property(add_property),
        FieldKindSO::DateTime => DateTime::validate_add_open_field_property(add_property),

        FieldKindSO::Member => Member::validate_add_open_field_property(add_property),
        FieldKindSO::Checkbox => Checkbox::validate_add_open_field_property(add_property),
        FieldKindSO::Rating => Rating::validate_add_open_field_property(add_property),

        FieldKindSO::OneWayLink => OneWayLinkField::validate_add_open_field_property(add_property),
        FieldKindSO::Link => Link::validate_add_open_field_property(add_property),
        FieldKindSO::LookUp => LookUp::validate_add_open_field_property(add_property),
        FieldKindSO::Formula => Formula::validate_add_open_field_property(add_property),
        FieldKindSO::CreatedTime => CreatedTime::validate_add_open_field_property(add_property),
        FieldKindSO::LastModifiedTime => LastModifiedTime::validate_add_open_field_property(add_property),
        FieldKindSO::LastModifiedBy => LastModifiedBy::validate_add_open_field_property(add_property),
        FieldKindSO::Button => ButtonField::validate_add_open_field_property(add_property),
        
        _ => Err(anyhow::Error::msg(format!("api_param_validate_error={} not support set property", kind.to_string()))),
    }
}

pub fn add_open_field_property_transform_property(add_property: FieldPropertySO, kind: FieldKindSO) -> FieldPropertySO {
    update_open_field_property_transform_property(add_property, kind)
}

fn update_open_field_property_transform_property(add_property: FieldPropertySO, kind: FieldKindSO) -> FieldPropertySO {
    let default_property = get_field_default_property(kind.clone()).unwrap_or(FieldPropertySO::default());
    let select_field = SelectField{};
    match &kind {
        // FieldKindSO::SingleText => SingleText::update_open_field_property_transform_property(add_property, default_property),
        FieldKindSO::SingleSelect => select_field.update_open_field_property_transform_property(add_property, default_property),
        FieldKindSO::MultiSelect => select_field.update_open_field_property_transform_property(add_property, default_property),
        // FieldKindSO::Number => Number::update_open_field_property_transform_property(add_property, default_property),
        FieldKindSO::Currency => Currency::update_open_field_property_transform_property(add_property, default_property),
        // FieldKindSO::Percent => Percent::update_open_field_property_transform_property(add_property, default_property),
        FieldKindSO::DateTime => DateTime::update_open_field_property_transform_property(add_property, default_property),

        FieldKindSO::Member => Member::update_open_field_property_transform_property(add_property, default_property),
        // FieldKindSO::Checkbox => Checkbox::update_open_field_property_transform_property(add_property, default_property),
        // FieldKindSO::Rating => Rating::update_open_field_property_transform_property(add_property, default_property),

        FieldKindSO::OneWayLink => OneWayLinkField::update_open_field_property_transform_property(add_property, default_property),
        FieldKindSO::Link => Link::update_open_field_property_transform_property(add_property, default_property),
        FieldKindSO::LookUp => LookUp::update_open_field_property_transform_property(add_property, default_property),
        FieldKindSO::Formula => Formula::update_open_field_property_transform_property(add_property, default_property),
        FieldKindSO::CreatedTime => CreatedTime::update_open_field_property_transform_property(add_property, default_property),
        FieldKindSO::LastModifiedTime => LastModifiedTime::update_open_field_property_transform_property(add_property, default_property),
        FieldKindSO::LastModifiedBy => LastModifiedBy::update_open_field_property_transform_property(add_property, default_property),
        FieldKindSO::Button => ButtonField::update_open_field_property_transform_property(add_property, default_property),
        
        _ => add_property,
    }
}

pub fn computed_formatting_to_format(format: Option<IComputedFieldFormattingProperty>) -> Option<IComputedFieldFormattingProperty> {
    match format {
        Some(format) => {
            // match format {
            //     IComputedFieldFormattingProperty::DateTime(format) => {
            //         Some(IComputedFieldFormattingProperty::DateTime(format))
            //     },
            //     IComputedFieldFormattingProperty::Currency(format) => {
            //         Some(IComputedFieldFormattingProperty::Currency(format))
            //     },
            //     IComputedFieldFormattingProperty::Number(format) => {
            //         Some(IComputedFieldFormattingProperty::Number(format))
            //     },
            //     IComputedFieldFormattingProperty::Percent(format) => {
            //         Some(IComputedFieldFormattingProperty::Percent(format))
            //     },
            //     _ => None,
            // }
            match format {
                IComputedFieldFormattingProperty::DateTime(format) => {
                    Some(IComputedFieldFormattingProperty::DateTime(IDateTimeFieldPropertyFormat {
                        date_format: format.date_format,
                        time_format: format.time_format,
                        include_time: format.include_time,
                        ..Default::default()
                    }))
                },
                // IComputedFieldFormattingProperty::Currency => {
                //     Some(IComputedFieldFormattingProperty::Currency {
                //         format_type: FieldType::Currency,
                //         precision: format.precision,
                //         symbol: format.symbol.clone(),
                //     })
                // },
                IComputedFieldFormattingProperty::Number(format) => {
                    Some(IComputedFieldFormattingProperty::Number(INumberBaseFieldPropertyFormat {
                        format_type: FieldKindSO::Number as i32,
                        precision: format.precision,
                        ..Default::default()
                    }))
                },
                // IComputedFieldFormattingProperty::Percent => {
                //     let format = format.format.as_ref().unwrap().as_percent().unwrap();
                //     Some(IComputedFieldFormattingProperty::Percent {
                //         format_type: FieldType::Percent,
                //         precision: format.precision,
                //     })
                // },
                _ => None,
            }
        },
        None => None,
    }
}

pub fn filter_operator_accepts_value(operator: &FOperator) -> bool {
    operator != &FOperator::IsEmpty && operator != &FOperator::IsNotEmpty && operator != &FOperator::IsRepeat
}

pub fn can_group(kind: FieldKindSO) -> bool {
    match kind {
        // FieldKindSO::SingleSelect => SingleSelect::new(field).can_group(),
        // FieldKindSO::MultiSelect => MultiSelect::new(field).can_group(),
        // FieldKindSO::Currency => Currency::new(field).can_group(),
        // FieldKindSO::Percent => Percent::new(field).can_group(),
        // FieldKindSO::Text => Text::new(field).can_group(),
        // FieldKindSO::Attachment => Attachment::new(field).can_group(),
        // FieldKindSO::URL => URL::new(field).can_group(),
        // FieldKindSO::Email => Email::new(field).can_group(),
        // FieldKindSO::Rating => Rating::new(field).can_group(),
        // FieldKindSO::Checkbox => Checkbox::new(field).can_group(),
        // FieldKindSO::Phone => Phone::new(field).can_group(),
        _ => true,
    }
}

pub fn has_error(kind: FieldKindSO) -> bool {
    match kind {
        // FieldKindSO::SingleSelect => SingleSelect::new(field).has_error(),
        // FieldKindSO::MultiSelect => MultiSelect::new(field).has_error(),
        // FieldKindSO::Currency => Currency::new(field).has_error(),
        // FieldKindSO::Percent => Percent::new(field).has_error(),
        // FieldKindSO::Text => Text::new(field).has_error(),
        // FieldKindSO::Attachment => Attachment::new(field).has_error(),
        // FieldKindSO::URL => URL::new(field).has_error(),
        // FieldKindSO::Email => Email::new(field).has_error(),
        // FieldKindSO::Rating => Rating::new(field).has_error(),
        // FieldKindSO::Checkbox => Checkbox::new(field).has_error(),
        // FieldKindSO::Phone => Phone::new(field).has_error(),
        _ => false,
    }
}

pub fn accept_filter_operators(kind: FieldKindSO) -> Vec<FOperator> {
    match kind {
        // FieldKindSO::SingleSelect => SingleSelect::new(field).accept_filter_operators(),
        // FieldKindSO::MultiSelect => MultiSelect::new(field).accept_filter_operators(),
        // FieldKindSO::Currency => Currency::new(field).accept_filter_operators(),
        // FieldKindSO::Percent => Percent::new(field).accept_filter_operators(),
        // FieldKindSO::Text => Text::new(field).accept_filter_operators(),
        // FieldKindSO::Attachment => Attachment::new(field).accept_filter_operators(),
        // FieldKindSO::URL => URL::new(field).accept_filter_operators(),
        // FieldKindSO::Email => Email::new(field).accept_filter_operators(),
        // FieldKindSO::Rating => Rating::new(field).accept_filter_operators(),
        // FieldKindSO::Checkbox => Checkbox::new(field).accept_filter_operators(),
        // FieldKindSO::Phone => Phone::new(field).accept_filter_operators(),
        _ => vec![
            FOperator::IsEmpty,
            FOperator::IsNotEmpty,
            FOperator::IsRepeat,
        ],
    }
}
