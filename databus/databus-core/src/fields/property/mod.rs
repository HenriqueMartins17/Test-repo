use serde::{Deserialize, Serialize};
use serde_repr::{Deserialize_repr, Serialize_repr};
use serde_json::Value;
use utoipa::ToSchema;

pub use crate::fields::property::field_types::{CollectType, DateFormat, RollUpFuncType, SymbolAlign, TimeFormat};
use crate::fields::property::field_types::{IComputedFieldFormattingProperty, LookUpLimitType};
use crate::prelude::view_operation::sort::ISortedField;
use crate::so::view_operation::filter::IFilterInfo;

pub mod field_types;
pub mod unit_enum;

#[derive(Debug, PartialEq, Eq, Clone, Serialize, Deserialize, ToSchema, Default)]
#[serde(rename_all = "camelCase")]
pub struct FieldPropertySO {
  #[serde(skip_serializing_if = "Option::is_none")]
  pub datasheet_id: Option<String>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub date_format: Option<DateFormat>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub time_format: Option<TimeFormat>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub include_time: Option<bool>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub time_zone: Option<String>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub include_time_zone: Option<bool>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub collect_type: Option<CollectType>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub field_id_collection: Option<Vec<String>>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub is_recog_url_flag: Option<bool>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub precision: Option<i32>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub default_value: Option<Value>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub next_id: Option<i32>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub view_idx: Option<i32>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub comma_style: Option<String>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub symbol: Option<String>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub symbol_align: Option<SymbolAlign>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub options: Option<Vec<SingleSelectProperty>>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub is_multi: Option<bool>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub should_send_msg: Option<bool>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub subscription: Option<bool>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub unit_ids: Option<Vec<String>>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub icon: Option<String>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub max: Option<u32>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub auto_fill: Option<bool>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub related_link_field_id: Option<String>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub look_up_target_field_id: Option<String>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub roll_up_type: Option<RollUpFuncType>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub formatting: Option<IComputedFieldFormattingProperty>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub filter_info: Option<IFilterInfo>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub open_filter: Option<bool>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub enable_filter_sort: Option<bool>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub sort_info: Option<LookUpSortInfo>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub look_up_limit: Option<LookUpLimitType>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub show_all: Option<bool>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub linked_datasheet_id: Option<String>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub linked_view_id: Option<String>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub linked_fields: Option<Vec<LinkedFields>>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub full_linked_fields: Option<Vec<LinkedFields>>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub uuids: Option<Vec<Option<Value>>>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub expression: Option<String>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub foreign_datasheet_id: Option<String>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub brother_field_id: Option<String>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub limit_to_view: Option<String>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub limit_single_record: Option<bool>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub text: Option<String>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub style: Option<IButtonStyle>,
  #[serde(skip_serializing_if = "Option::is_none")]
  pub action: Option<IButtonAction>,
}

impl FieldPropertySO {
  pub fn to_single_text_field_property(&self) -> SingleTextFieldPropertySO {
    SingleTextFieldPropertySO {
      default_value: if self.default_value.is_some() {
        serde_json::from_value(self.default_value.clone().unwrap()).unwrap()
      } else {
        None
      },
    }
  }

  pub fn to_number_field_property(&self) -> NumberFieldPropertySO {
    NumberFieldPropertySO {
      precision: self.precision.unwrap(),
      default_value: if self.default_value.is_some() {
        serde_json::from_value(self.default_value.clone().unwrap()).unwrap()
      } else {
        None
      },
      comma_style: self.comma_style.clone(),
      symbol: self.symbol.clone(),
      symbol_align: self.symbol_align.clone(),
    }
  }

  pub fn to_auto_number_field_property(&self) -> AutoNumberFieldPropertySO {
    AutoNumberFieldPropertySO {
      next_id: self.next_id.unwrap(),
      view_idx: self.view_idx.unwrap(),
      datasheet_id: self.datasheet_id.clone().unwrap(),
    }
  }

  pub fn to_created_time_field_property(&self) -> CreatedTimeFieldPropertySO {
    CreatedTimeFieldPropertySO {
      datasheet_id: self.datasheet_id.clone().unwrap(),
      date_format: self.date_format.clone().unwrap(),
      time_format: self.time_format.clone().unwrap(),
      include_time: self.include_time.unwrap(),
      time_zone: self.time_zone.clone(),
      include_time_zone: self.include_time_zone.clone(),
    }
  }

  pub fn to_last_modified_time_property(&self) -> LastModifiedTimeFieldPropertySO {
    LastModifiedTimeFieldPropertySO {
      datasheet_id: self.datasheet_id.clone().unwrap(),
      date_format: self.date_format.clone().unwrap(),
      time_format: self.time_format.clone().unwrap(),
      include_time: self.include_time.unwrap(),
      time_zone: self.time_zone.clone(),
      include_time_zone: self.include_time_zone.clone(),
      collect_type: self.collect_type.clone().unwrap(),
      field_id_collection: self.field_id_collection.clone().unwrap(),
    }
  }

  pub fn to_cascader_field_property(&self) -> CascaderFieldPropertySO {
    CascaderFieldPropertySO {
      show_all: self.show_all.unwrap(),
      linked_datasheet_id: self.linked_datasheet_id.clone().unwrap(),
      linked_view_id: self.linked_view_id.clone().unwrap(),
      linked_fields: self.linked_fields.clone().unwrap(),
      full_linked_fields: self.full_linked_fields.clone().unwrap(),
    }
  }

  pub fn to_single_select_field_property(&self) -> SelectFieldPropertySO {
    SelectFieldPropertySO {
      options: self.options.clone().unwrap(),
    }
  }

  pub fn to_multi_select_field_property(&self) -> SelectFieldPropertySO {
    SelectFieldPropertySO {
      options: self.options.clone().unwrap(),
    }
  }

  pub fn to_link_field_property(&self) -> LinkFieldPropertySO {
    let tmp = self.clone();
    LinkFieldPropertySO {
      foreign_datasheet_id: tmp.foreign_datasheet_id.unwrap(),
      brother_field_id: tmp.brother_field_id,
      limit_to_view: tmp.limit_to_view,
      limit_single_record: tmp.limit_single_record,
    }
  }

  pub fn to_one_way_link_field_property(&self) -> OneWayLinkFieldPropertySO {
    let tmp = self.clone();
    OneWayLinkFieldPropertySO {
      foreign_datasheet_id: tmp.foreign_datasheet_id.unwrap(),
      brother_field_id: tmp.brother_field_id,
      limit_to_view: tmp.limit_to_view,
      limit_single_record: tmp.limit_single_record,
    }
  }

  pub fn to_url_field_property(&self) -> URLFieldPropertySO {
    URLFieldPropertySO {
      is_recog_url_flag: self.is_recog_url_flag,
    }
  }

  pub fn to_checkbox_field_property(&self) -> CheckboxFieldPropertySO {
    CheckboxFieldPropertySO {
      icon: self.icon.clone().unwrap(),
    }
  }

  pub fn to_rating_field_property(&self) -> RatingFieldPropertySO {
    RatingFieldPropertySO {
      icon: self.icon.clone().unwrap(),
      max: self.max.unwrap(),
    }
  }

  pub fn to_lookup_field_property(&self) -> LookupFieldPropertySO {
    let tmp = self.clone();
    LookupFieldPropertySO {
      datasheet_id: tmp.datasheet_id.unwrap(),
      related_link_field_id: tmp.related_link_field_id.unwrap(),
      look_up_target_field_id: tmp.look_up_target_field_id.unwrap(),
      roll_up_type: tmp.roll_up_type,
      formatting: tmp.formatting,
      filter_info: tmp.filter_info,
      open_filter: tmp.open_filter,
      sort_info: tmp.sort_info,
      look_up_limit: tmp.look_up_limit,
    }
  }

  pub fn to_created_by_field_property(&self) -> CreatedByFieldPropertySO {
    let tmp = self.clone();
    CreatedByFieldPropertySO {
      uuids: tmp
        .uuids
        .unwrap_or_else(|| Vec::new())
        .into_iter()
        .filter(|v| v.is_some() && v.as_ref().unwrap().is_string())
        .map(|v| v.map(|value| value.as_str().unwrap().to_string()))
        .collect(),
      datasheet_id: tmp.datasheet_id.unwrap(),
      subscription: tmp.subscription,
    }
  }

  pub fn to_member_field_property(&self) -> MemberFieldPropertySO {
    let tmp = self.clone();
    MemberFieldPropertySO {
      is_multi: tmp.is_multi,
      should_send_msg: tmp.should_send_msg.unwrap(),
      subscription: tmp.subscription,
      unit_ids: tmp.unit_ids.unwrap(),
    }
  }

  pub fn to_date_time_field_property(&self) -> DateTimeFieldPropertySO {
    DateTimeFieldPropertySO {
      date_format: self.date_format.clone().unwrap(),
      time_format: self.time_format.clone().unwrap(),
      include_time: self.include_time.unwrap(),
      auto_fill: self.auto_fill.unwrap_or(false),
      time_zone: self.time_zone.clone(),
      include_time_zone: self.include_time_zone.clone(),
    }
  }

  pub fn to_formula_field_property(&self) -> FormulaFieldPropertySO {
    let tmp = self.clone();
    FormulaFieldPropertySO {
      datasheet_id: tmp.datasheet_id.unwrap(),
      expression: tmp.expression.unwrap(),
      formatting: tmp.formatting,
    }
  }

  pub fn to_currency_field_property(&self) -> CurrencyFieldPropertySO {
    CurrencyFieldPropertySO {
      symbol: self.symbol.clone().unwrap(),
      precision: self.precision.unwrap(),
      default_value: if self.default_value.is_some() {
        serde_json::from_value(self.default_value.clone().unwrap()).unwrap()
      } else {
        None
      },
      symbol_align: self.symbol_align.clone(),
    }
  }

  pub fn to_percent_field_property(&self) -> PercentFieldPropertySO {
    PercentFieldPropertySO {
      precision: self.precision.unwrap(),
      default_value: if self.default_value.is_some() {
        serde_json::from_value(self.default_value.clone().unwrap()).unwrap()
      } else {
        None
      },
    }
  }

  pub fn to_last_modified_by_field_property(&self) -> LastModifiedByFieldPropertySO {
    let tmp = self.clone();
    LastModifiedByFieldPropertySO {
      uuids: tmp
        .uuids
        .unwrap_or_else(|| Vec::new())
        .into_iter()
        .filter(|v| v.is_some() && v.as_ref().unwrap().is_string())
        .map(|v| v.map(|value| value.as_str().unwrap().to_string()))
        .collect(),
      datasheet_id: tmp.datasheet_id.unwrap(),
      collect_type: tmp.collect_type.unwrap(),
      field_id_collection: tmp.field_id_collection.unwrap(),
    }
  }
}

#[derive(Debug, PartialEq, Eq, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct CreatedTimeFieldPropertySO {
  pub datasheet_id: String,
  pub date_format: DateFormat,
  pub time_format: TimeFormat,
  pub include_time: bool,
  pub time_zone: Option<String>,
  pub include_time_zone: Option<bool>,
}

#[derive(Debug, PartialEq, Eq, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct LastModifiedTimeFieldPropertySO {
  pub datasheet_id: String,
  pub date_format: DateFormat,
  pub time_format: TimeFormat,
  pub include_time: bool,
  pub time_zone: Option<String>,
  pub include_time_zone: Option<bool>,
  pub collect_type: CollectType,
  pub field_id_collection: Vec<String>,
}

#[derive(Debug, PartialEq, Eq, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct URLFieldPropertySO {
  pub is_recog_url_flag: Option<bool>,
}

#[derive(Debug, PartialEq, Eq, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct PercentFieldPropertySO {
  pub precision: i32,
  pub default_value: Option<String>,
}

#[derive(Debug, PartialEq, Eq, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct AutoNumberFieldPropertySO {
  pub next_id: i32,
  pub view_idx: i32,
  pub datasheet_id: String,
}

#[derive(Debug, PartialEq, Eq, Clone, Serialize, Deserialize, ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct SingleSelectProperty {
  pub id: String,
  pub name: String,
  // pub color: serde_json::Value,
  pub color: i32,
}

#[derive(Debug, PartialEq, Eq, Clone, Serialize, Deserialize, ToSchema, Default)]
#[serde(rename_all = "camelCase")]
pub struct SingleTextFieldPropertySO {
  pub default_value: Option<String>,
}

#[derive(Debug, PartialEq, Eq, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct NumberFieldPropertySO {
  pub precision: i32,

  #[serde(skip_serializing_if = "Option::is_none")]
  pub default_value: Option<String>,

  #[serde(skip_serializing_if = "Option::is_none")]
  pub comma_style: Option<String>,

  #[serde(skip_serializing_if = "Option::is_none")]
  pub symbol: Option<String>,
  // #[serde(skip_serializing_if = "Option::is_none")]
  pub symbol_align: Option<SymbolAlign>,
}

#[derive(Debug, PartialEq, Eq, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct CurrencyFieldPropertySO {
  pub symbol: String,
  pub precision: i32,
  pub default_value: Option<String>,
  pub symbol_align: Option<SymbolAlign>,
}

#[derive(Debug, PartialEq, Eq, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct SelectFieldPropertySO {
  pub options: Vec<SingleSelectProperty>,
}

#[derive(Debug, PartialEq, Eq, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct MemberFieldPropertySO {
  /// Optional single or multiple members.
  #[serde(skip_serializing_if = "Option::is_none")]
  pub is_multi: Option<bool>,
  pub should_send_msg: bool,
  /// Whether to send a message notification after selecting a member
  #[serde(skip_serializing_if = "Option::is_none")]
  pub subscription: Option<bool>,
  pub unit_ids: Vec<String>,
}

#[derive(Debug, PartialEq, Eq, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct CheckboxFieldPropertySO {
  pub icon: String,
}

#[derive(Debug, PartialEq, Eq, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct RatingFieldPropertySO {
  pub icon: String,
  pub max: u32,
}

#[derive(Debug, PartialEq, Eq, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct DateTimeFieldPropertySO {
  pub date_format: DateFormat,
  pub time_format: TimeFormat,
  pub include_time: bool,
  pub auto_fill: bool,
  pub time_zone: Option<String>,
  pub include_time_zone: Option<bool>,
}

#[derive(Debug, PartialEq, Eq, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct LookupFieldPropertySO {
  pub datasheet_id: String,
  pub related_link_field_id: String,
  pub look_up_target_field_id: String,
  pub roll_up_type: Option<RollUpFuncType>,
  pub formatting: Option<IComputedFieldFormattingProperty>,
  pub filter_info: Option<IFilterInfo>,
  pub open_filter: Option<bool>,
  pub sort_info: Option<LookUpSortInfo>,
  pub look_up_limit: Option<LookUpLimitType>,
}

#[derive(Debug, PartialEq, Eq, Clone, Serialize, Deserialize, ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct LookUpSortField {
  pub field_id: String,
  pub desc: bool,
}

#[derive(Debug, PartialEq, Eq, Clone, Serialize, Deserialize, ToSchema)]
pub struct LookUpSortInfo {
  // pub rules: Vec<LookUpSortField>,
  pub rules: Vec<ISortedField>,
}

#[derive(Debug, PartialEq, Eq, Clone, Serialize, Deserialize, ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct LookUpFilterPO {
  pub conjunction: String,
  pub conditions: Vec<Value>,
}

#[derive(Debug, PartialEq, Eq, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct CascaderFieldPropertySO {
  pub show_all: bool,
  pub linked_datasheet_id: String,
  pub linked_view_id: String,
  pub linked_fields: Vec<LinkedFields>,
  pub full_linked_fields: Vec<LinkedFields>,
}

#[derive(Debug, PartialEq, Eq, Clone, Serialize, Deserialize, ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct LinkedFields {
  pub id: String,
  pub name: String,
  pub r#type: u32,
}

#[derive(Serialize_repr, Deserialize_repr, Debug, PartialEq, Eq, Clone, ToSchema, Default)]
#[repr(u32)]
pub enum ButtonStyleType {
  #[default]
  Background = 0,
  OnlyText = 1,
}

#[derive(Serialize_repr, Deserialize_repr, Debug, PartialEq, Eq, Clone, ToSchema, Default)]
#[repr(u32)]
pub enum ButtonActionType {
  #[default]
  OpenLink = 0,
  TriggerAutomation = 1,
}

#[derive(Serialize_repr, Deserialize_repr, Debug, PartialEq, Eq, Clone, ToSchema, Default)]
#[repr(u32)]
pub enum OpenLinkType {
  #[default]
  Url = 0,
  Expression = 1,
}

#[derive(Debug, PartialEq, Eq, Clone, Serialize, Deserialize, ToSchema, Default)]
#[serde(rename_all = "camelCase")]
pub struct IButtonStyle {
  pub r#type: ButtonStyleType,
  pub color: u32,
}

#[derive(Debug, PartialEq, Eq, Clone, Serialize, Deserialize, ToSchema, Default)]
#[serde(rename_all = "camelCase")]
pub struct IOpenLink {
  pub r#type: Option<OpenLinkType>,
  pub expression: Option<String>,
}

#[derive(Debug, PartialEq, Eq, Clone, Serialize, Deserialize, ToSchema, Default)]
#[serde(rename_all = "camelCase")]
pub struct IButtonAction {
  pub r#type: Option<ButtonActionType>,
  pub open_link: Option<IOpenLink>,
}

#[derive(Debug, PartialEq, Eq, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct CreatedByFieldPropertySO {
  pub uuids: Vec<Option<String>>,
  pub datasheet_id: String,
  pub subscription: Option<bool>,
}

#[derive(Debug, PartialEq, Eq, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct LastModifiedByFieldPropertySO {
  pub uuids: Vec<Option<String>>,

  pub datasheet_id: String,

  /// dependent field collection type
  pub collect_type: CollectType,

  /// dependent fields
  pub field_id_collection: Vec<String>,
}

#[derive(Debug, PartialEq, Eq, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct FormulaFieldPropertySO {
  /// formula is computed property, it needs to locate the current datasheetId through fieldProperty;
  pub datasheet_id: String,
  pub expression: String,
  /// todo field_types  IFormulaProperty
  #[serde(skip_serializing_if = "Option::is_none")]
  pub formatting: Option<IComputedFieldFormattingProperty>,
}

#[derive(Debug, PartialEq, Eq, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct LinkFieldPropertySO {
  pub foreign_datasheet_id: String,

  #[serde(skip_serializing_if = "Option::is_none")]
  pub brother_field_id: Option<String>,

  /// the limit is only on the optional record corresponding to the ViewId. Note: viewId may not exist in the associated table with the modification of the associated table
  #[serde(skip_serializing_if = "Option::is_none")]
  pub limit_to_view: Option<String>,

  /// whether to limit only one block to be Associated. Note: This is a soft limit that only takes effect on the current table interaction, there are actually multiple ways to break the limit.
  #[serde(skip_serializing_if = "Option::is_none")]
  pub limit_single_record: Option<bool>,
}

#[derive(Debug, PartialEq, Eq, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct OneWayLinkFieldPropertySO {
  pub foreign_datasheet_id: String,

  #[serde(skip_serializing_if = "Option::is_none")]
  pub brother_field_id: Option<String>,

  /// the limit is only on the optional record corresponding to the ViewId. Note: viewId may not exist in the associated table with the modification of the associated table
  #[serde(skip_serializing_if = "Option::is_none")]
  pub limit_to_view: Option<String>,

  /// whether to limit only one block to be Associated. Note: This is a soft limit that only takes effect on the current table interaction, there are actually multiple ways to break the limit.
  #[serde(skip_serializing_if = "Option::is_none")]
  pub limit_single_record: Option<bool>,
}

#[cfg(test)]
mod tests {
  use serde_json::{json, Value};

  use crate::fields::property::FieldPropertySO;

  #[test]
  pub fn test() {
    let x = r#"
    {
        "showAll": true,
        "linkedFields": [
            {
                "id": "fldQRr8gTkl1Y",
                "name": "一级部门",
                "type": 19
            },
            {
                "id": "fldcNeo5DmEnl",
                "name": "二级部门",
                "type": 19
            },
            {
                "id": "fldEx2FAZvvro",
                "name": "三级部门",
                "type": 19
            }
        ],
        "linkedViewId": "viwjNXPlhtvSk",
        "fullLinkedFields": [
            {
                "id": "fldQRr8gTkl1Y",
                "name": "一级部门",
                "type": 19
            },
            {
                "id": "fldcNeo5DmEnl",
                "name": "二级部门",
                "type": 19
            },
            {
                "id": "fldEx2FAZvvro",
                "name": "三级部门",
                "type": 19
            }
        ],
        "linkedDatasheetId": "dstdSpnZ1lXsQcWKcK"
    }"#;
    let field_property: FieldPropertySO = serde_json::from_str::<FieldPropertySO>(x).unwrap();
    let _so = field_property.to_cascader_field_property();
    let result = serde_json::from_str::<Value>(&serde_json::to_string(&field_property).unwrap().as_ref()).unwrap();
    assert_eq!(serde_json::from_str::<Value>(x).unwrap(), result);
  }

  #[test]
  pub fn test1() {
    let uuids1: Option<Vec<Option<String>>> = Some(vec![Some("1".to_string())]);
    let v = json!({});
    let uuids: Option<Vec<Option<Value>>> = Some(vec![Some(Value::String("1".to_string())), Some(v)]);
    let x: Vec<_> = uuids
      .unwrap_or_else(|| Vec::new())
      .into_iter()
      .filter(|v| v.is_some() && v.as_ref().unwrap().is_string())
      .map(|v| v.map(|value| value.as_str().unwrap().to_string()))
      .collect();
    assert_eq!(uuids1.unwrap(), x);
  }
}
