use serde_json::Value;

use std::rc::Rc;
use crate::prelude::DatasheetPackContext;

use super::{property::{FieldPropertySO, ButtonStyleType, IButtonStyle, ButtonActionType, IOpenLink, IButtonAction, field_types::BasicValueType}, base_field::{BaseField, IBaseField}};

pub struct ButtonField {
  field: crate::prelude::FieldSO,
  context: Rc<DatasheetPackContext>,
}

impl IBaseField for ButtonField {
  fn basic_value_type(&self) -> BasicValueType {
    BasicValueType::Array
  }

  fn inner_basic_value_type(&self) -> BasicValueType {
    BasicValueType::Array
  }

  fn is_computed(&self) -> bool {
    true
  }
}

impl ButtonField {
  pub fn new(field: crate::prelude::FieldSO, context: Rc<DatasheetPackContext>) -> Self {
    return Self { field, context };
  }

  pub fn validate_add_open_field_property(_property: Value) -> anyhow::Result<()>{
    Ok(())
  }

  pub fn update_open_field_property_transform_property(open_field_property: FieldPropertySO, default_property: FieldPropertySO) -> FieldPropertySO{
    let mut default_property = default_property.clone();
    if let Some(text) = open_field_property.text {
        default_property.text = Some(text);
    }
    let pre_style = open_field_property.style.clone().and_then(|s| Some(s.r#type));
    let mut color = default_property.style.clone().unwrap().color;
    if let Some(style) = open_field_property.style {
        if style.color > 0 {
          color = style.color;
        }
    }
    let button_style = IButtonStyle {
      r#type: Self::get_style_type_by_name(pre_style),
      color,
    };
    default_property.style = Some(button_style);
    if let Some(action) = open_field_property.action {
        if action.r#type == Some(ButtonActionType::OpenLink) {
            let r#type = Some(ButtonActionType::OpenLink);

            let mut open_link = IOpenLink {
              r#type: None,
              expression: None,
            };
            
            if let Some(open_link_action) = action.open_link {
                if let Some(kind) = open_link_action.r#type {
                  open_link.r#type = Some(kind);
                }
                if let Some(_expression) = open_link_action.expression {
                  open_link.expression = Some(_expression);
                }
            }
            
            default_property.action = Some(IButtonAction {
              r#type,
              open_link: Some(open_link),
            });
        }
    }

    default_property
  }

  fn get_style_type_by_name(name: Option<ButtonStyleType>) -> ButtonStyleType {
    match name {
        Some(name) => match name {
          ButtonStyleType::Background => ButtonStyleType::Background,
          ButtonStyleType::OnlyText => ButtonStyleType::OnlyText,
          _ => ButtonStyleType::Background,
        },
        None => ButtonStyleType::Background,
    }
  }
}
