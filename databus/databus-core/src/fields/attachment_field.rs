use crate::fields::ext::types::CellToStringOption;
use crate::prelude::CellValue;
use crate::so::api_value::ApiValue;
use crate::utils::file;
use crate::utils::file::ImageSrcOption;

use super::base_field::IBaseField;
use super::property::field_types::BasicValueType;
use super::property::FieldPropertySO;

pub struct Attachment {
  field_conf: crate::prelude::FieldSO,
}

impl IBaseField for Attachment {
  fn basic_value_type(&self) -> BasicValueType {
    BasicValueType::Array
  }

  fn inner_basic_value_type(&self) -> BasicValueType {
    BasicValueType::String
  }

  fn cell_value_to_string(&self, cell_value: CellValue, _option: Option<CellToStringOption>) -> Option<String> {
    if let CellValue::Attachment(attach_arr) = cell_value {

    }
    None
  }

  fn cell_value_to_api_standard_value(&self, cell_value: CellValue) -> ApiValue {
    if let Some(arr) = cell_value.to_attachment_array() {
      let mut result = vec![];
      for mut attach in arr {
        attach.url = Some(file::get_image_url(
          attach.clone(),
          Option::Some(ImageSrcOption {
            format_to_jpg: false,
            is_preview: false,
          }),
        ));
        if !attach.preview.is_none() {
          attach.preview = Some(file::get_image_url(
            attach.clone(),
            Option::Some(ImageSrcOption {
              format_to_jpg: false,
              is_preview: true,
            }),
          ));
        }
        result.push(attach);
      }

      return ApiValue::AttachmentValue(result);
    }

    ApiValue::Null
  }
}

impl Attachment {
  pub fn new(field_conf: crate::prelude::FieldSO) -> Self {
    return Self { field_conf };
  }

  pub fn default_property() -> Option<FieldPropertySO> {
    None
  }
}
