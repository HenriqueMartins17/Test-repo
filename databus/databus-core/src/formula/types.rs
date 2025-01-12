use std::rc::Rc;

use serde_json::Value;

use crate::fields::field_factory::FieldFactory;
use crate::fields::property::{
  AutoNumberFieldPropertySO, CascaderFieldPropertySO, CheckboxFieldPropertySO, CreatedByFieldPropertySO,
  CreatedTimeFieldPropertySO, CurrencyFieldPropertySO, DateTimeFieldPropertySO, FieldPropertySO,
  FormulaFieldPropertySO, LastModifiedByFieldPropertySO, LastModifiedTimeFieldPropertySO, LinkFieldPropertySO, OneWayLinkFieldPropertySO,
  LookupFieldPropertySO, MemberFieldPropertySO, NumberFieldPropertySO, PercentFieldPropertySO, RatingFieldPropertySO,
  SelectFieldPropertySO, SingleTextFieldPropertySO, URLFieldPropertySO,
};
use crate::prelude::DatasheetPackContext;
use crate::so::{FieldKindSO, FieldSO};

/// For conversion between `IField` and `FieldSO`
///
/// TODO: remove this Anti-corruption Layer
#[derive(Debug, PartialEq, Clone)]
pub enum IField {
  NotSupport(IBaseField<()>),
  Denied(IBaseField<()>),
  Attachment(IBaseField<()>),
  DateTime(IBaseField<DateTimeFieldPropertySO>),
  Text(IBaseField<()>),
  Number(IBaseField<NumberFieldPropertySO>),
  MultiSelect(IBaseField<SelectFieldPropertySO>),
  SingleSelect(IBaseField<SelectFieldPropertySO>),
  Link(IBaseField<LinkFieldPropertySO>),
  OneWayLink(IBaseField<OneWayLinkFieldPropertySO>),
  // TODO: OneWayLinkField
  UrlField(IBaseField<URLFieldPropertySO>),
  Email(IBaseField<()>),
  Phone(IBaseField<()>),
  Checkbox(IBaseField<CheckboxFieldPropertySO>),
  Rating(IBaseField<RatingFieldPropertySO>),
  Member(IBaseField<MemberFieldPropertySO>),
  LookUp(IBaseField<LookupFieldPropertySO>),
  Formula(IBaseField<FormulaFieldPropertySO>),
  Currency(IBaseField<CurrencyFieldPropertySO>),
  Percent(IBaseField<PercentFieldPropertySO>),
  SingleText(IBaseField<SingleTextFieldPropertySO>),
  AutoNumber(IBaseField<AutoNumberFieldPropertySO>),
  CreatedTime(IBaseField<CreatedTimeFieldPropertySO>),
  LastModifiedTime(IBaseField<LastModifiedTimeFieldPropertySO>),
  CreatedBy(IBaseField<CreatedByFieldPropertySO>),
  LastModifiedBy(IBaseField<LastModifiedByFieldPropertySO>),
  Cascader(IBaseField<CascaderFieldPropertySO>),
}

impl IField {
  pub fn get_id(&self) -> &str {
    match self {
      Self::NotSupport(field) => &field.id,
      Self::Denied(field) => &field.id,
      Self::Attachment(field) => &field.id,
      Self::DateTime(field) => &field.id,
      Self::Text(field) => &field.id,
      Self::Number(field) => &field.id,
      Self::MultiSelect(field) => &field.id,
      Self::SingleSelect(field) => &field.id,
      Self::Link(field) => &field.id,
      Self::OneWayLink(field) => &field.id,
      Self::UrlField(field) => &field.id,
      Self::Email(field) => &field.id,
      Self::Phone(field) => &field.id,
      Self::Checkbox(field) => &field.id,
      Self::Rating(field) => &field.id,
      Self::Member(field) => &field.id,
      Self::LookUp(field) => &field.id,
      Self::Formula(field) => &field.id,
      Self::Currency(field) => &field.id,
      Self::Percent(field) => &field.id,
      Self::SingleText(field) => &field.id,
      Self::AutoNumber(field) => &field.id,
      Self::CreatedTime(field) => &field.id,
      Self::LastModifiedTime(field) => &field.id,
      Self::CreatedBy(field) => &field.id,
      Self::LastModifiedBy(field) => &field.id,
      Self::Cascader(field) => &field.id,
    }
  }

  pub fn get_name(&self) -> &str {
    match self {
      Self::NotSupport(field) => &field.name,
      Self::Denied(field) => &field.name,
      Self::Attachment(field) => &field.name,
      Self::DateTime(field) => &field.name,
      Self::Text(field) => &field.name,
      Self::Number(field) => &field.name,
      Self::MultiSelect(field) => &field.name,
      Self::SingleSelect(field) => &field.name,
      Self::Link(field) => &field.name,
      Self::OneWayLink(field) => &field.name,
      Self::UrlField(field) => &field.name,
      Self::Email(field) => &field.name,
      Self::Phone(field) => &field.name,
      Self::Checkbox(field) => &field.name,
      Self::Rating(field) => &field.name,
      Self::Member(field) => &field.name,
      Self::LookUp(field) => &field.name,
      Self::Formula(field) => &field.name,
      Self::Currency(field) => &field.name,
      Self::Percent(field) => &field.name,
      Self::SingleText(field) => &field.name,
      Self::AutoNumber(field) => &field.name,
      Self::CreatedTime(field) => &field.name,
      Self::LastModifiedTime(field) => &field.name,
      Self::CreatedBy(field) => &field.name,
      Self::LastModifiedBy(field) => &field.name,
      Self::Cascader(field) => &field.name,
    }
  }

  pub(crate) fn bind_context(
    field: Rc<IField>,
    state: Rc<DatasheetPackContext>,
  ) -> Box<dyn crate::fields::base_field::IBaseField> {
    // TODO: remove copy and SO
    let field_so = (*field).clone().to_so();
    FieldFactory::create_field(field_so, state)
  }

  pub fn from_so(so: FieldSO) -> Self {
    match so.kind {
      FieldKindSO::NotSupport => Self::NotSupport(IBaseField {
        id: so.id,
        name: so.name,
        desc: so.desc,
        required: so.required,
        property: (),
      }),
      FieldKindSO::DeniedField => Self::Denied(IBaseField {
        id: so.id,
        name: so.name,
        desc: so.desc,
        required: so.required,
        property: (),
      }),
      FieldKindSO::Attachment => Self::Attachment(IBaseField {
        id: so.id,
        name: so.name,
        desc: so.desc,
        required: so.required,
        property: (),
      }),
      FieldKindSO::DateTime => Self::DateTime(IBaseField {
        id: so.id,
        name: so.name,
        desc: so.desc,
        required: so.required,
        property: so.property.unwrap().to_date_time_field_property(),
      }),
      FieldKindSO::Text => Self::Text(IBaseField {
        id: so.id,
        name: so.name,
        desc: so.desc,
        required: so.required,
        property: (),
      }),
      FieldKindSO::Number => Self::Number(IBaseField {
        id: so.id,
        name: so.name,
        desc: so.desc,
        required: so.required,
        property: so.property.unwrap().to_number_field_property(),
      }),
      FieldKindSO::MultiSelect => Self::MultiSelect(IBaseField {
        id: so.id,
        name: so.name,
        desc: so.desc,
        required: so.required,
        property: so.property.unwrap().to_multi_select_field_property(),
      }),
      FieldKindSO::SingleSelect => Self::SingleSelect(IBaseField {
        id: so.id,
        name: so.name,
        desc: so.desc,
        required: so.required,
        property: so.property.unwrap().to_single_select_field_property(),
      }),
      FieldKindSO::Link => Self::Link(IBaseField {
        id: so.id,
        name: so.name,
        desc: so.desc,
        required: so.required,
        property: so.property.unwrap().to_link_field_property(),
      }),
      FieldKindSO::OneWayLink => Self::OneWayLink(IBaseField {
        id: so.id,
        name: so.name,
        desc: so.desc,
        required: so.required,
        property: so.property.unwrap().to_one_way_link_field_property(),
      }),
      FieldKindSO::URL => Self::UrlField(IBaseField {
        id: so.id,
        name: so.name,
        desc: so.desc,
        required: so.required,
        property: so.property.unwrap().to_url_field_property(),
      }),
      FieldKindSO::Email => Self::Email(IBaseField {
        id: so.id,
        name: so.name,
        desc: so.desc,
        required: so.required,
        property: (),
      }),
      FieldKindSO::Phone => Self::Phone(IBaseField {
        id: so.id,
        name: so.name,
        desc: so.desc,
        required: so.required,
        property: (),
      }),
      FieldKindSO::Checkbox => Self::Checkbox(IBaseField {
        id: so.id,
        name: so.name,
        desc: so.desc,
        required: so.required,
        property: so.property.unwrap().to_checkbox_field_property(),
      }),
      FieldKindSO::Rating => Self::Rating(IBaseField {
        id: so.id,
        name: so.name,
        desc: so.desc,
        required: so.required,
        property: so.property.unwrap().to_rating_field_property(),
      }),
      FieldKindSO::Member => Self::Member(IBaseField {
        id: so.id,
        name: so.name,
        desc: so.desc,
        required: so.required,
        property: so.property.unwrap().to_member_field_property(),
      }),
      FieldKindSO::LookUp => Self::LookUp(IBaseField {
        id: so.id,
        name: so.name,
        desc: so.desc,
        required: so.required,
        property: so.property.unwrap().to_lookup_field_property(),
      }),
      FieldKindSO::Formula => Self::Formula(IBaseField {
        id: so.id,
        name: so.name,
        desc: so.desc,
        required: so.required,
        property: so.property.unwrap().to_formula_field_property(),
      }),
      FieldKindSO::Currency => Self::Currency(IBaseField {
        id: so.id,
        name: so.name,
        desc: so.desc,
        required: so.required,
        property: so.property.unwrap().to_currency_field_property(),
      }),
      FieldKindSO::Percent => Self::Percent(IBaseField {
        id: so.id,
        name: so.name,
        desc: so.desc,
        required: so.required,
        property: so.property.unwrap().to_percent_field_property(),
      }),
      FieldKindSO::SingleText => Self::SingleText(IBaseField {
        id: so.id,
        name: so.name,
        desc: so.desc,
        required: so.required,
        property: so.property.unwrap().to_single_text_field_property(),
      }),
      FieldKindSO::AutoNumber => Self::AutoNumber(IBaseField {
        id: so.id,
        name: so.name,
        desc: so.desc,
        required: so.required,
        property: so.property.unwrap().to_auto_number_field_property(),
      }),
      FieldKindSO::CreatedTime => Self::CreatedTime(IBaseField {
        id: so.id,
        name: so.name,
        desc: so.desc,
        required: so.required,
        property: so.property.unwrap().to_created_time_field_property(),
      }),
      FieldKindSO::LastModifiedTime => Self::LastModifiedTime(IBaseField {
        id: so.id,
        name: so.name,
        desc: so.desc,
        required: so.required,
        property: so.property.unwrap().to_last_modified_time_property(),
      }),
      FieldKindSO::CreatedBy => Self::CreatedBy(IBaseField {
        id: so.id,
        name: so.name,
        desc: so.desc,
        required: so.required,
        property: so.property.unwrap().to_created_by_field_property(),
      }),
      FieldKindSO::LastModifiedBy => Self::LastModifiedBy(IBaseField {
        id: so.id,
        name: so.name,
        desc: so.desc,
        required: so.required,
        property: so.property.unwrap().to_last_modified_by_field_property(),
      }),
      FieldKindSO::Cascader => Self::Cascader(IBaseField {
        id: so.id,
        name: so.name,
        desc: so.desc,
        required: so.required,
        property: so.property.unwrap().to_cascader_field_property(),
      }),
      _ => panic!("not support field type"),
    }
  }

  pub fn to_so(self) -> FieldSO {
    match self {
      Self::NotSupport(field) => FieldSO {
        id: field.id,
        name: field.name,
        desc: field.desc,
        required: field.required,
        kind: FieldKindSO::NotSupport,
        property: None,
      },
      Self::Denied(field) => FieldSO {
        id: field.id,
        name: field.name,
        desc: field.desc,
        required: field.required,
        kind: FieldKindSO::DeniedField,
        property: None,
      },
      Self::Attachment(field) => FieldSO {
        id: field.id,
        name: field.name,
        desc: field.desc,
        required: field.required,
        kind: FieldKindSO::Attachment,
        property: None,
      },
      Self::DateTime(field) => FieldSO {
        id: field.id,
        name: field.name,
        desc: field.desc,
        required: field.required,
        kind: FieldKindSO::DateTime,
        property: Some(FieldPropertySO {
          date_format: Some(field.property.date_format),
          time_format: Some(field.property.time_format),
          include_time: Some(field.property.include_time),
          auto_fill: Some(field.property.auto_fill),
          time_zone: field.property.time_zone,
          include_time_zone: field.property.include_time_zone,
          ..FieldPropertySO::default()
        }),
      },
      Self::Text(field ) => FieldSO {
        id: field.id,
        name: field.name,
        desc: field.desc,
        required: field.required,
        kind: FieldKindSO::Text,
        property: Some(FieldPropertySO::default()),
      },
      Self::Number(field) => FieldSO {
        id: field.id,
        name: field.name,
        desc: field.desc,
        required: field.required,
        kind: FieldKindSO::Number,
        property: Some(FieldPropertySO {
          precision: Some(field.property.precision),
          default_value: match field.property.default_value {
            Some(value) => Some(Value::String(value)),
            None => None,
          },
          comma_style: field.property.comma_style,
          symbol: field.property.symbol,
          symbol_align: field.property.symbol_align,
          ..FieldPropertySO::default()
        }),
      },
      Self::MultiSelect(field) => FieldSO {
        id: field.id,
        name: field.name,
        desc: field.desc,
        required: field.required,
        kind: FieldKindSO::MultiSelect,
        property: Some(FieldPropertySO {
          options: Some(field.property.options),
          ..FieldPropertySO::default()
        }),
      },
      Self::SingleSelect(field) => FieldSO {
        id: field.id,
        name: field.name,
        desc: field.desc,
        required: field.required,
        kind: FieldKindSO::SingleSelect,
        property: Some(FieldPropertySO {
          options: Some(field.property.options),
          ..FieldPropertySO::default()
        }),
      },
      Self::Link(field) => FieldSO {
        id: field.id,
        name: field.name,
        desc: field.desc,
        required: field.required,
        kind: FieldKindSO::Link,
        property: Some(FieldPropertySO {
          foreign_datasheet_id: Some(field.property.foreign_datasheet_id),
          brother_field_id: field.property.brother_field_id,
          limit_to_view: field.property.limit_to_view,
          limit_single_record: field.property.limit_single_record,
          ..FieldPropertySO::default()
        }),
      },
      Self::OneWayLink(field) => FieldSO {
        id: field.id,
        name: field.name,
        desc: field.desc,
        required: field.required,
        kind: FieldKindSO::Link,
        property: Some(FieldPropertySO {
          foreign_datasheet_id: Some(field.property.foreign_datasheet_id),
          brother_field_id: field.property.brother_field_id,
          limit_to_view: field.property.limit_to_view,
          limit_single_record: field.property.limit_single_record,
          ..FieldPropertySO::default()
        }),
      },
      Self::UrlField(field) => FieldSO {
        id: field.id,
        name: field.name,
        desc: field.desc,
        required: field.required,
        kind: FieldKindSO::URL,
        property: Some(FieldPropertySO {
          is_recog_url_flag: field.property.is_recog_url_flag,
          ..FieldPropertySO::default()
        }),
      },
      Self::Email(field ) => FieldSO {
        id: field.id,
        name: field.name,
        desc: field.desc,
        required: field.required,
        kind: FieldKindSO::Email,
        property: Some(FieldPropertySO::default()),
      },
      Self::Phone(field ) => FieldSO {
        id: field.id,
        name: field.name,
        desc: field.desc,
        required: field.required,
        kind: FieldKindSO::Phone,
        property: Some(FieldPropertySO::default()),
      },
      Self::Checkbox(field) => FieldSO {
        id: field.id,
        name: field.name,
        desc: field.desc,
        required: field.required,
        kind: FieldKindSO::Checkbox,
        property: Some(FieldPropertySO {
          icon: Some(field.property.icon),
          ..FieldPropertySO::default()
        }),
      },
      Self::Rating(field) => FieldSO {
        id: field.id,
        name: field.name,
        desc: field.desc,
        required: field.required,
        kind: FieldKindSO::Rating,
        property: Some(FieldPropertySO {
          icon: Some(field.property.icon),
          max: Some(field.property.max),
          ..FieldPropertySO::default()
        }),
      },
      Self::Member(field) => FieldSO {
        id: field.id,
        name: field.name,
        desc: field.desc,
        required: field.required,
        kind: FieldKindSO::Member,
        property: Some(FieldPropertySO {
          is_multi: field.property.is_multi,
          should_send_msg: Some(field.property.should_send_msg),
          subscription: field.property.subscription,
          unit_ids: Some(field.property.unit_ids),
          ..FieldPropertySO::default()
        }),
      },
      Self::LookUp(field) => FieldSO {
        id: field.id,
        name: field.name,
        desc: field.desc,
        required: field.required,
        kind: FieldKindSO::LookUp,
        property: Some(FieldPropertySO {
          datasheet_id: Some(field.property.datasheet_id),
          related_link_field_id: Some(field.property.related_link_field_id),
          look_up_target_field_id: Some(field.property.look_up_target_field_id),
          roll_up_type: field.property.roll_up_type,
          formatting: field.property.formatting,
          filter_info: field.property.filter_info,
          open_filter: field.property.open_filter,
          sort_info: field.property.sort_info,
          look_up_limit: field.property.look_up_limit,
          ..FieldPropertySO::default()
        }),
      },
      Self::Formula(field) => FieldSO {
        id: field.id,
        name: field.name,
        desc: field.desc,
        required: field.required,
        kind: FieldKindSO::Formula,
        property: Some(FieldPropertySO {
          datasheet_id: Some(field.property.datasheet_id),
          expression: Some(field.property.expression),
          formatting: field.property.formatting,
          ..FieldPropertySO::default()
        }),
      },
      Self::Currency(field) => FieldSO {
        id: field.id,
        name: field.name,
        desc: field.desc,
        required: field.required,
        kind: FieldKindSO::Currency,
        property: Some(FieldPropertySO {
          symbol: Some(field.property.symbol),
          precision: Some(field.property.precision),
          default_value: match field.property.default_value {
            Some(value) => Some(Value::String(value)),
            None => None,
          },
          symbol_align: field.property.symbol_align,
          ..FieldPropertySO::default()
        }),
      },
      Self::Percent(field) => FieldSO {
        id: field.id,
        name: field.name,
        desc: field.desc,
        required: field.required,
        kind: FieldKindSO::Percent,
        property: Some(FieldPropertySO {
          precision: Some(field.property.precision),
          default_value: match field.property.default_value {
            Some(value) => Some(Value::String(value)),
            None => None,
          },
          ..FieldPropertySO::default()
        }),
      },
      Self::SingleText(field) => FieldSO {
        id: field.id,
        name: field.name,
        desc: field.desc,
        required: field.required,
        kind: FieldKindSO::SingleText,
        property: Some(FieldPropertySO {
          default_value: match field.property.default_value {
            Some(value) => Some(Value::String(value)),
            None => None,
          },
          ..FieldPropertySO::default()
        }),
      },
      Self::AutoNumber(field) => FieldSO {
        id: field.id,
        name: field.name,
        desc: field.desc,
        required: field.required,
        kind: FieldKindSO::AutoNumber,
        property: Some(FieldPropertySO {
          next_id: Some(field.property.next_id),
          view_idx: Some(field.property.view_idx),
          datasheet_id: Some(field.property.datasheet_id),
          ..FieldPropertySO::default()
        }),
      },
      Self::CreatedTime(field) => FieldSO {
        id: field.id,
        name: field.name,
        desc: field.desc,
        required: field.required,
        kind: FieldKindSO::CreatedTime,
        property: Some(FieldPropertySO {
          datasheet_id: Some(field.property.datasheet_id),
          date_format: Some(field.property.date_format),
          time_format: Some(field.property.time_format),
          include_time: Some(field.property.include_time),
          time_zone: field.property.time_zone,
          include_time_zone: field.property.include_time_zone,
          ..FieldPropertySO::default()
        }),
      },
      Self::LastModifiedTime(field) => FieldSO {
        id: field.id,
        name: field.name,
        desc: field.desc,
        required: field.required,
        kind: FieldKindSO::LastModifiedTime,
        property: Some(FieldPropertySO {
          datasheet_id: Some(field.property.datasheet_id),
          date_format: Some(field.property.date_format),
          time_format: Some(field.property.time_format),
          include_time: Some(field.property.include_time),
          time_zone: field.property.time_zone,
          include_time_zone: field.property.include_time_zone,
          collect_type: Some(field.property.collect_type),
          field_id_collection: Some(field.property.field_id_collection),
          ..FieldPropertySO::default()
        }),
      },
      Self::CreatedBy(field) => FieldSO {
        id: field.id,
        name: field.name,
        desc: field.desc,
        required: field.required,
        kind: FieldKindSO::CreatedBy,
        property: Some(FieldPropertySO {
          uuids: Some(field.property.uuids.into_iter().map(|s| s.map(Value::String)).collect()),
          datasheet_id: Some(field.property.datasheet_id),
          subscription: field.property.subscription,
          ..FieldPropertySO::default()
        }),
      },
      Self::LastModifiedBy(field) => FieldSO {
        id: field.id,
        name: field.name,
        desc: field.desc,
        required: field.required,
        kind: FieldKindSO::LastModifiedBy,
        property: Some(FieldPropertySO {
          uuids: Some(field.property.uuids.into_iter().map(|s| s.map(Value::String)).collect()),
          datasheet_id: Some(field.property.datasheet_id),
          collect_type: Some(field.property.collect_type),
          field_id_collection: Some(field.property.field_id_collection),
          ..FieldPropertySO::default()
        }),
      },
      Self::Cascader(field) => FieldSO {
        id: field.id,
        name: field.name,
        desc: field.desc,
        required: field.required,
        kind: FieldKindSO::Cascader,
        property: Some(FieldPropertySO {
          show_all: Some(field.property.show_all),
          linked_datasheet_id: Some(field.property.linked_datasheet_id),
          linked_view_id: Some(field.property.linked_view_id),
          linked_fields: Some(field.property.linked_fields),
          full_linked_fields: Some(field.property.full_linked_fields),
          ..FieldPropertySO::default()
        }),
      },
    }
  }
}

#[derive(Debug, PartialEq, Clone)]
pub struct IBaseField<T> {
  pub id: String,
  pub name: String,
  pub desc: Option<String>,
  pub required: Option<bool>,
  pub property: T,
}
