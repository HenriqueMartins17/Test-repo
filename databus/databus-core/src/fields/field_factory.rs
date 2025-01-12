use crate::fields::formula_field::Formula;
use crate::formula::types::IField;
use crate::prelude::{DatasheetPackContext, FieldSO};
use crate::so::field::FieldKindSO;
use std::rc::Rc;

pub use super::*;

pub struct FieldFactory;

impl FieldFactory {
  pub fn create_field(field: FieldSO, context: Rc<DatasheetPackContext>) -> Box<dyn base_field::IBaseField> {
    match field.kind {
      FieldKindSO::SingleText => Box::new(SingleText::new(field)),
      FieldKindSO::Text => Box::new(Text::new(field)),
      FieldKindSO::URL => Box::new(URL::new(field)),
      FieldKindSO::Phone => Box::new(Phone::new(field)),
      FieldKindSO::Email => Box::new(Email::new(field)),

      FieldKindSO::Number => Box::new(Number::new(field)),
      FieldKindSO::Currency => Box::new(Currency::new(field)),
      FieldKindSO::Rating => Box::new(Rating::new(field)),
      FieldKindSO::Percent => Box::new(Percent::new(field)),
      FieldKindSO::AutoNumber => Box::new(AutoNumber::new(field)),

      FieldKindSO::DateTime => Box::new(DateTime::new(field, context)),
      FieldKindSO::CreatedTime => Box::new(CreatedTime::new(field, context)),
      FieldKindSO::LastModifiedTime => Box::new(LastModifiedTime::new(field)),

      FieldKindSO::Member => Box::new(Member::new(field, context)),
      FieldKindSO::CreatedBy => Box::new(CreatedBy::new(field, context)),
      FieldKindSO::LastModifiedBy => Box::new(LastModifiedBy::new(field, context)),

      FieldKindSO::Checkbox => Box::new(Checkbox::new(field)),

      FieldKindSO::Attachment => Box::new(Attachment::new(field)),

      FieldKindSO::SingleSelect => Box::new(SingleSelect::new(field)),
      FieldKindSO::MultiSelect => Box::new(MultiSelect::new(field)),

      FieldKindSO::Link => Box::new(Link::new(field, context)),
      FieldKindSO::OneWayLink => Box::new(OneWayLinkField::new(field, context)),
      FieldKindSO::LookUp => Box::new(LookUp::new(field, context)),

      FieldKindSO::Cascader => Box::new(Cascader::new(field)),

      FieldKindSO::Formula => Box::new(Formula::new(Rc::new(IField::from_so(field)), context)),
      FieldKindSO::Button => Box::new(ButtonField::new(field, context)),
      _ => Box::new(empty_field::EmptyField::new(field)),
    }
  }
}
