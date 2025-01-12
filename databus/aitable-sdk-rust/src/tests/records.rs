#[cfg(test)]
#[cfg(feature = "integration_tests")]
mod tests {
  ///
  /// get records
  ///
  mod get_records {
    use std::sync::{Once, RwLock};

    use databus_core::vo::record_vo::RecordDTO;

    use crate::test_utils::utils::{create_record_manager, DstId, FieldName};

    #[test]
    fn test_on_v1() {
      let manager = create_record_manager(1, DstId::FullFieldsDstId);

      match tokio_test::block_on(manager.get(vec!["Number"])) {
        Ok(rst) => {
          assert!(rst.success);
        }
        Err(err) => {
          assert!(false);
        }
      }
    }

    // singleton data for test fields
    static SETUP_GET_RECORDS: Once = Once::new();
    static GET_RECORDS_DATA: RwLock<Option<(Vec<RecordDTO>, Vec<RecordDTO>)>> = RwLock::new(None);

    // setup singleton data
    fn setup_get_records_data() {
      SETUP_GET_RECORDS.call_once(|| {
        // get records from v1
        let manager_v1 = create_record_manager(1, DstId::FullFieldsDstId);
        let rst_v1 = tokio_test::block_on(manager_v1.get(vec![])).unwrap();
        assert!(rst_v1.success);
        let records_v1 = rst_v1.data.records;

        // get records from v3
        let manager_v3 = create_record_manager(3, DstId::FullFieldsDstId);
        let rst_v3 = tokio_test::block_on(manager_v3.get(vec![])).unwrap();
        assert!(rst_v3.success);
        let records_v3 = rst_v3.data.records;

        // check length
        assert!(records_v1.len() > 0);
        assert_eq!(rst_v1.data.total, rst_v3.data.total);

        *GET_RECORDS_DATA.write().unwrap() = Some((records_v1, records_v3));
      });
    }

    #[test]
    fn test_compare_record_id() {
      setup_get_records_data();

      let guard = GET_RECORDS_DATA.read().unwrap();
      let data = guard.as_ref().unwrap();
      let records_v1 = &data.0;
      let records_v3 = &data.1;

      // compare record_id
      for i in 0..records_v1.len() {
        assert_eq!(records_v1[i].record_id, records_v3[i].record_id);
      }
    }

    macro_rules! create_field_test {
      ($name:ident, $value:expr) => {
        #[test]
        fn $name() {
          setup_get_records_data();

          let guard = GET_RECORDS_DATA.read().unwrap();
          let data = guard.as_ref().unwrap();
          let records_v1 = &data.0;
          let records_v3 = &data.1;

          // compare field
          for i in 0..records_v1.len() {
            assert_eq!(records_v1[i].fields.get($value), records_v3[i].fields.get($value));
          }
        }
      };
    }
    create_field_test!(test_single_line_text, FieldName::SINGLE_LINE_TEXT);
    create_field_test!(test_long_text,        FieldName::LONG_TEXT);
    create_field_test!(test_select,           FieldName::SELECT);
    create_field_test!(test_multi_select,     FieldName::MULTI_SELECT);
    create_field_test!(test_number,           FieldName::NUMBER);
    create_field_test!(test_currency,         FieldName::CURRENCY);
    create_field_test!(test_percent,          FieldName::PERCENT);
    create_field_test!(test_date_ymd,         FieldName::DATE_YMD);
    create_field_test!(test_attachment,       FieldName::ATTACHMENT);
    create_field_test!(test_member,           FieldName::MEMBER);
    create_field_test!(test_checkbox,         FieldName::CHECKBOX);
    create_field_test!(test_rating,           FieldName::RATING);
    create_field_test!(test_url,              FieldName::URL);
    create_field_test!(test_phone,            FieldName::PHONE);
    create_field_test!(test_email,            FieldName::EMAIL);
    create_field_test!(test_one_way_link,     FieldName::ONE_WAY_LINK);
    create_field_test!(test_two_way_link,     FieldName::TWO_WAY_LINK);
    create_field_test!(test_lookup,           FieldName::LOOKUP);
    create_field_test!(test_formula,          FieldName::FORMULA);
    create_field_test!(test_autonumber,       FieldName::AUTONUMBER);
    create_field_test!(test_cascader,         FieldName::CASCADER);
    create_field_test!(test_created_time,     FieldName::CREATED_TIME);
    create_field_test!(test_last_edited_time, FieldName::LAST_EDITED_TIME);
    create_field_test!(test_created_by,       FieldName::CREATED_BY);
    create_field_test!(test_last_edited_by,   FieldName::LAST_EDITED_BY);
  }

  ///
  /// add record
  ///
  mod add_records {
    use std::collections::HashMap;
    use std::fmt::Debug;

    use serde_json::{from_value, Value};

    use databus_core::fields::field_types::{AttachmentValue, MemberValue, UrlValue};
    use databus_core::ro::record_update_ro::FieldKeyEnum;

    use crate::datasheet::types::FieldValue as FV;
    use crate::test_utils::utils::{create_record_manager_v1, FieldName as FN};

    fn full_field_records() -> Vec<HashMap<String, FV>> {
      vec![
        // a line record
        vec![
          (FN::SINGLE_LINE_TEXT.to_string(), FV::SingleLineText("a".to_string())),
          (FN::LONG_TEXT.to_string(), FV::MultiLineText("a".to_string())),
          (FN::SELECT.to_string(), FV::Select("a".to_string())),
          (FN::MULTI_SELECT.to_string(), FV::MultiSelect(vec!["a".to_string()])),
          (FN::NUMBER.to_string(), FV::Number(0.0)),
          (FN::CURRENCY.to_string(), FV::Currency(0.0)),
          (FN::PERCENT.to_string(), FV::Percent(0.0)),
          (FN::DATE_YMD.to_string(), FV::Date(1695657600000)),
          (FN::ATTACHMENT.to_string(),
           FV::Attachment(vec![AttachmentValue {
             id: "atcfy84QkmHEZ".to_string(),
             name: "cat.jpeg".to_string(),
             size: 24576,
             mime_type: "image/jpeg".to_string(),
             token: "space/2023/09/12/b938cab58f994807856880afc84fda85".to_string(),
             width: Some(384),
             height: Some(480),
             url: Some("https://s4.vika.cn/space/2023/09/12/b938cab58f994807856880afc84fda85".to_string()),
             bucket: None,
             preview: None,
           }])),
          (FN::MEMBER.to_string(),
           FV::Member(vec![
             MemberValue {
               id: "1699975234256474113".to_string(),
               unit_id: "".to_string(),
               name: "13719445996".to_string(),
               r#type: "Member".to_string(),
               avatar: Some("/assets/public/2023/09/08/6f0149fedb354cd7b24117cc1a4b067c".to_string()),
             }
           ])),
          (FN::CHECKBOX.to_string(), FV::Checkbox(true)),
          (FN::RATING.to_string(), FV::Rating(1)),
          (FN::URL.to_string(),
           FV::Url(UrlValue {
             title: "https://integration.vika.ltd/workbench/dstY9JTFxroH355hKq/viwrGHyq7JMru?spaceId=spcXYMQ4jc1N7".to_string(),
             text: "https://integration.vika.ltd/workbench/dstY9JTFxroH355hKq/viwrGHyq7JMru?spaceId=spcXYMQ4jc1N7".to_string(),
             favicon: None,
           })),
          (FN::PHONE.to_string(), FV::Phone("13712345678".to_string())),
          (FN::EMAIL.to_string(), FV::Email("a@vikadata.com".to_string())),
          (FN::ONE_WAY_LINK.to_string(), FV::OneWayLink(vec!["recIAHQ6gWNXt".to_string()])),
          (FN::TWO_WAY_LINK.to_string(), FV::TwoWayLink(vec!["recIAHQ6gWNXt".to_string()])),
          (FN::CASCADER.to_string(), FV::Cascader("a".to_string())),
        ].into_iter().collect(),
      ]
    }


    #[test]
    fn test_on_v1() {
      let manager = create_record_manager_v1();

      let params = full_field_records();
      let rst = tokio_test::block_on(manager.create(FieldKeyEnum::NAME, &params));
      if rst.is_err() {
        println!("err: {:?}", rst.err());
        assert!(false);
        return;
      }

      let rst = rst.unwrap();
      assert!(rst.success);

      assert_eq!(params.len(), rst.data.records.len());
      let record_id = &rst.data.records[0].record_id;
      let record = &rst.data.records[0].fields;
      let param = &params[0];

      assert_single_line_text_eq(param.get(FN::SINGLE_LINE_TEXT).unwrap(), record.get(FN::SINGLE_LINE_TEXT).unwrap());
      assert_long_text_eq(param.get(FN::LONG_TEXT).unwrap(), record.get(FN::LONG_TEXT).unwrap());
      assert_select_eq(param.get(FN::SELECT).unwrap(), record.get(FN::SELECT).unwrap());
      assert_multi_select_eq(param.get(FN::MULTI_SELECT).unwrap(), record.get(FN::MULTI_SELECT).unwrap());
      assert_number_eq(param.get(FN::NUMBER).unwrap(), record.get(FN::NUMBER).unwrap());
      assert_currency_eq(param.get(FN::CURRENCY).unwrap(), record.get(FN::CURRENCY).unwrap());
      assert_percent_eq(param.get(FN::PERCENT).unwrap(), record.get(FN::PERCENT).unwrap());
      assert_date_eq(param.get(FN::DATE_YMD).unwrap(), record.get(FN::DATE_YMD).unwrap());
      assert_attachment_eq(param.get(FN::ATTACHMENT).unwrap(), record.get(FN::ATTACHMENT).unwrap());
      // TODO: check member field
      // assert_member_eq(param.get(FN::MEMBER).unwrap(), record.get(FN::MEMBER).unwrap());
      assert_checkbox_eq(param.get(FN::CHECKBOX).unwrap(), record.get(FN::CHECKBOX).unwrap());
      assert_rating_eq(param.get(FN::RATING).unwrap(), record.get(FN::RATING).unwrap());
      assert_url_eq(param.get(FN::URL).unwrap(), record.get(FN::URL).unwrap());
      assert_phone_eq(param.get(FN::PHONE).unwrap(), record.get(FN::PHONE).unwrap());
      assert_email_eq(param.get(FN::EMAIL).unwrap(), record.get(FN::EMAIL).unwrap());
      assert_one_way_link_eq(param.get(FN::ONE_WAY_LINK).unwrap(), record.get(FN::ONE_WAY_LINK).unwrap());
      assert_two_way_link_eq(param.get(FN::TWO_WAY_LINK).unwrap(), record.get(FN::TWO_WAY_LINK).unwrap());
      assert_cascader_eq(param.get(FN::CASCADER).unwrap(), record.get(FN::CASCADER).unwrap());


      // call get records to check
      let rst = tokio_test::block_on(manager.get(vec![record_id]));
      if rst.is_err() {
        println!("err: {:?}", rst.err());
        assert!(false);
        return;
      }

      let rst = rst.unwrap();
      assert!(rst.success);
      assert_eq!(rst.data.records.len(), 1);
      assert_eq!(rst.data.records[0].record_id, record_id.to_string());

      let record = &rst.data.records[0].fields;

      assert_single_line_text_eq(param.get(FN::SINGLE_LINE_TEXT).unwrap(), record.get(FN::SINGLE_LINE_TEXT).unwrap());
      assert_long_text_eq(param.get(FN::LONG_TEXT).unwrap(), record.get(FN::LONG_TEXT).unwrap());
      assert_select_eq(param.get(FN::SELECT).unwrap(), record.get(FN::SELECT).unwrap());
      assert_multi_select_eq(param.get(FN::MULTI_SELECT).unwrap(), record.get(FN::MULTI_SELECT).unwrap());
      assert_number_eq(param.get(FN::NUMBER).unwrap(), record.get(FN::NUMBER).unwrap());
      assert_currency_eq(param.get(FN::CURRENCY).unwrap(), record.get(FN::CURRENCY).unwrap());
      assert_percent_eq(param.get(FN::PERCENT).unwrap(), record.get(FN::PERCENT).unwrap());
      assert_date_eq(param.get(FN::DATE_YMD).unwrap(), record.get(FN::DATE_YMD).unwrap());
      assert_attachment_eq(param.get(FN::ATTACHMENT).unwrap(), record.get(FN::ATTACHMENT).unwrap());
      // TODO: check member field
      // assert_member_eq(param.get(FN::MEMBER).unwrap(), record.get(FN::MEMBER).unwrap());
      assert_checkbox_eq(param.get(FN::CHECKBOX).unwrap(), record.get(FN::CHECKBOX).unwrap());
      assert_rating_eq(param.get(FN::RATING).unwrap(), record.get(FN::RATING).unwrap());
      // TODO: after fix url field
      // assert_url_eq(param.get(FN::URL).unwrap(), record.get(FN::URL).unwrap());
      assert_phone_eq(param.get(FN::PHONE).unwrap(), record.get(FN::PHONE).unwrap());
      assert_email_eq(param.get(FN::EMAIL).unwrap(), record.get(FN::EMAIL).unwrap());
      assert_one_way_link_eq(param.get(FN::ONE_WAY_LINK).unwrap(), record.get(FN::ONE_WAY_LINK).unwrap());
      assert_two_way_link_eq(param.get(FN::TWO_WAY_LINK).unwrap(), record.get(FN::TWO_WAY_LINK).unwrap());
      assert_cascader_eq(param.get(FN::CASCADER).unwrap(), record.get(FN::CASCADER).unwrap());
    }


    ///
    /// assert fields value
    ///

    /// extract field value from FieldValue
    macro_rules! extract_field_value {
      ($expected:expr, $variant:ident) => {
        match $expected {
          FV::$variant(v) => v.clone(),
          _ => panic!(concat!("expected FieldValue::", stringify!($variant))),
        }
      };
    }

    fn assert_single_line_text_eq(expected: &FV, value: &Value) {
      let expected = extract_field_value!(expected, SingleLineText);
      let value = from_value::<String>(value.clone()).unwrap();
      assert_eq!(expected, value);
    }

    fn assert_long_text_eq(expected: &FV, value: &Value) {
      let expected = extract_field_value!(expected, MultiLineText);
      let value = from_value::<String>(value.clone()).unwrap();
      assert_eq!(expected, value);
    }

    fn assert_select_eq(expected: &FV, value: &Value) {
      let expected = extract_field_value!(expected, Select);
      let value = from_value::<String>(value.clone()).unwrap();
      assert_eq!(expected, value);
    }

    fn assert_multi_select_eq(expected: &FV, value: &Value) {
      let expected = extract_field_value!(expected, MultiSelect);
      let value = from_value::<Vec<String>>(value.clone()).unwrap();
      assert_eq!(expected, value);
    }

    fn assert_number_eq(expected: &FV, value: &Value) {
      let expected = extract_field_value!(expected, Number);
      let value = from_value::<f64>(value.clone()).unwrap();
      assert_eq!(expected, value);
    }

    fn assert_currency_eq(expected: &FV, value: &Value) {
      let expected = extract_field_value!(expected, Currency);
      let value = from_value::<f64>(value.clone()).unwrap();
      assert_eq!(expected, value);
    }

    fn assert_percent_eq(expected: &FV, value: &Value) {
      let expected = extract_field_value!(expected, Percent);
      let value = from_value::<f64>(value.clone()).unwrap();
      assert_eq!(expected, value);
    }

    fn assert_date_eq(expected: &FV, value: &Value) {
      let expected = extract_field_value!(expected, Date);
      let value = from_value::<i64>(value.clone()).unwrap();
      assert_eq!(expected, value);
    }

    fn assert_attachment_eq(expected: &FV, value: &Value) {
      let expected = extract_field_value!(expected, Attachment);
      let value = from_value::<Vec<AttachmentValue>>(value.clone()).unwrap();
      for i in 0..expected.len() {
        assert_eq!(expected[i].name, value[i].name);
        assert_eq!(expected[i].size, value[i].size);
        assert_eq!(expected[i].mime_type, value[i].mime_type);
        assert_eq!(expected[i].token, value[i].token);
        assert_eq!(expected[i].width, value[i].width);
        assert_eq!(expected[i].height, value[i].height);
      }
    }

    fn assert_member_eq(expected: &FV, value: &Value) {
      let expected = extract_field_value!(expected, Member);
      let value = from_value::<Vec<MemberValue>>(value.clone()).unwrap();
      for i in 0..expected.len() {
        assert_eq!(expected[i].id, value[i].id);
        assert_eq!(expected[i].name, value[i].name);
        assert_eq!(expected[i].r#type, value[i].r#type);
        assert_eq!(expected[i].avatar, value[i].avatar);
      }
    }

    fn assert_checkbox_eq(expected: &FV, value: &Value) {
      let expected = extract_field_value!(expected, Checkbox);
      let value = from_value::<bool>(value.clone()).unwrap();
      assert_eq!(expected, value);
    }

    fn assert_rating_eq(expected: &FV, value: &Value) {
      let expected = extract_field_value!(expected, Rating);
      let value = from_value::<i64>(value.clone()).unwrap();
      assert_eq!(expected, value);
    }

    fn assert_url_eq(expected: &FV, value: &Value) {
      let expected = extract_field_value!(expected, Url);
      let value = from_value::<UrlValue>(value.clone()).unwrap();
      assert_eq!(expected.title, value.title);
      assert_eq!(expected.text, value.text);
    }

    fn assert_phone_eq(expected: &FV, value: &Value) {
      let expected = extract_field_value!(expected, Phone);
      let value = from_value::<String>(value.clone()).unwrap();
      assert_eq!(expected, value);
    }

    fn assert_email_eq(expected: &FV, value: &Value) {
      let expected = extract_field_value!(expected, Email);
      let value = from_value::<String>(value.clone()).unwrap();
      assert_eq!(expected, value);
    }

    fn assert_one_way_link_eq(expected: &FV, value: &Value) {
      let expected = extract_field_value!(expected, OneWayLink);
      let value = from_value::<Vec<String>>(value.clone()).unwrap();
      assert_eq!(expected, value);
    }

    fn assert_two_way_link_eq(expected: &FV, value: &Value) {
      let expected = extract_field_value!(expected, TwoWayLink);
      let value = from_value::<Vec<String>>(value.clone()).unwrap();
      assert_eq!(expected, value);
    }

    fn assert_cascader_eq(expected: &FV, value: &Value) {
      let expected = extract_field_value!(expected, Cascader);
      let value = from_value::<String>(value.clone()).unwrap();
      assert_eq!(expected, value);
    }
  }
}
