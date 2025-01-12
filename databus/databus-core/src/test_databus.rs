
#[cfg(test)]
mod tests {
    use std::collections::HashMap;

    use serde_json::{to_value, Value};

    use crate::{so::{RecordSO, Comments, CommentMsg, CellValueSo}, mock::{RecordValue, get_context, get_datasheet}, logic::{IRecordsOptions, IRecordVoTransformOptions, IAddRecordsOptions}, ot::{commands::{AddRecordsOptions, SaveOptions, ExecuteResult}, CollaCommandName}};

    fn assert_record_id(mut record: RecordSO, new_id: &str) -> RecordSO {
        let id = record.id.clone();
        // println!("id: {}", id);
        assert_eq!(&id[..3], "rec");
    
        record.id = new_id.to_string();
        record
    }

    fn assert_record_ids<F>(records: Vec<RecordSO>, gen_id: F) -> Vec<RecordSO>
    where
        F: Fn(usize) -> String,
    {
        records.into_iter().enumerate().map(|(i, record)| assert_record_id(record, &gen_id(i))).collect()
    }

    fn mock_record_vo_transformer(record: RecordSO, _vo_transform_options: IRecordVoTransformOptions) -> RecordSO {
        record
    }

    fn mock_default_record() -> RecordSO {
        RecordSO {
            id: "rec4".to_string(),
            data: {
                let mut m = HashMap::new();
                m.insert("fld2", vec!["opt2", "opt1"]);
                to_value(m).unwrap()
            },
            // comments: Some(vec![]),
            comment_count: 0,
            // record_meta: HashMap::new(),
            ..Default::default()
        }
    }

    fn mock_record_values() -> Vec<HashMap<String, CellValueSo>> {
        let mut hm = HashMap::new();
        let data_value = vec![to_value(RecordValue {
            r#type: 1,
            text: "text 4".to_string(),
        }).unwrap()];
        hm.insert("fld1".to_string(), to_value(data_value).unwrap());
        let data_value = vec![to_value("opt2".to_string()).unwrap()];
        hm.insert("fld2".to_string(), to_value(data_value).unwrap());

        let mut hm2 = HashMap::new();
        let data_value = vec![to_value(RecordValue {
            r#type: 1,
            text: "text 5".to_string(),
        }).unwrap()];
        hm2.insert("fld1".to_string(), to_value(data_value).unwrap());

        let mut hm3 = HashMap::new();
        let data_value = vec![to_value("opt1".to_string()).unwrap(), to_value("opt3".to_string()).unwrap(), to_value("opt2".to_string()).unwrap()];
        hm3.insert("fld2".to_string(), to_value(data_value).unwrap());

        vec![hm, hm2, hm3]
    }

    fn mock_records() -> Vec<RecordSO> { 
        let mut hm = HashMap::new();
        let data_value = vec![to_value(RecordValue {
            r#type: 1,
            text: "text 4".to_string(),
        }).unwrap()];
        hm.insert("fld1".to_string(), to_value(data_value).unwrap());
        let data_value = vec![to_value("opt2".to_string()).unwrap()];
        hm.insert("fld2".to_string(), to_value(data_value).unwrap());

        let mut hm2 = HashMap::new();
        let data_value = vec![to_value(RecordValue {
            r#type: 1,
            text: "text 5".to_string(),
        }).unwrap()];
        hm2.insert("fld1".to_string(), to_value(data_value).unwrap());
        let data_value = vec![to_value("opt2".to_string()).unwrap(), to_value("opt1".to_string()).unwrap()];
        hm2.insert("fld2".to_string(), to_value(data_value).unwrap());

        let mut hm3 = HashMap::new();
        hm3.insert("fld1".to_string(), Value::Null);
        let data_value = vec![to_value("opt1".to_string()).unwrap(), to_value("opt3".to_string()).unwrap(), to_value("opt2".to_string()).unwrap()];
        hm3.insert("fld2".to_string(), to_value(data_value).unwrap());
        
        vec![
            RecordSO {
                id: "rec4".to_string(),
                data: to_value(hm).unwrap(),
                // comments: Some(vec![]),
                comment_count: 0,
                // record_meta: Some(RecordMeta {..Default::default()}),
                ..Default::default()
            },
            RecordSO {
                id: "rec5".to_string(),
                data: to_value(hm2).unwrap(),
                // comments: Some(vec![]),
                comment_count: 0,
                // record_meta: Some(RecordMeta {..Default::default()}),
                ..Default::default()
            },
            RecordSO {
                id: "rec6".to_string(),
                data: to_value(hm3).unwrap(),
                // comments: Some(vec![]),
                comment_count: 0,
                // record_meta: Some(RecordMeta {..Default::default()}),
                ..Default::default()
            },
        ]
    }

    #[test]
    fn should_return_correct_numbers_of_records() {
        let mut dst1 = get_datasheet(get_context());
        let view1 = dst1.get_view("viw1");
        assert!(view1.is_some());
        let view1 = view1.unwrap();
        let records = view1.get_records(IRecordsOptions{..Default::default()});

        assert_eq!(records.len(), 5);
    }

    #[test]
    fn should_return_correct_records() {
        let mut dst1 = get_datasheet(get_context());
        let view1 = dst1.get_view("viw1");
        assert!(view1.is_some());
        let records = view1.unwrap().get_records(IRecordsOptions{..Default::default()});

        let record_vos = records.iter().map(|record| record.get_view_object(mock_record_vo_transformer)).collect::<Vec<_>>();

        let mut hm = HashMap::new();
        let data_value = vec![to_value(RecordValue {
            r#type: 1,
            text: "text 1".to_string(),
        }).unwrap()];
        hm.insert("fld1".to_string(), data_value);
        let data_value = vec![to_value("opt2".to_string()).unwrap(), to_value("opt1".to_string()).unwrap()];
        hm.insert("fld2".to_string(), data_value);

        let mut hm2 = HashMap::new();
        let data_value = vec![to_value(RecordValue {
            r#type: 1,
            text: "text 2".to_string(),
        }).unwrap()];
        hm2.insert("fld1".to_string(), data_value);
        let data_value = vec![to_value("opt1".to_string()).unwrap()];
        hm2.insert("fld2".to_string(), data_value);

        let mut hm3 = HashMap::new();
        let data_value = vec![to_value(RecordValue {
            r#type: 1,
            text: "text 3".to_string(),
        }).unwrap()];
        hm3.insert("fld1".to_string(), data_value);
        let data_value = vec![];
        hm3.insert("fld2".to_string(), data_value);

        let mut hm4 = HashMap::new();
        let data_value = vec![to_value(RecordValue {
            r#type: 1,
            text: "text 4".to_string(),
        }).unwrap()];
        hm4.insert("fld1".to_string(), data_value);
        let data_value = vec![to_value("opt3".to_string()).unwrap(), to_value("opt2".to_string()).unwrap(), to_value("opt1".to_string()).unwrap()];
        hm4.insert("fld2".to_string(), data_value);

        let mut hm5 = HashMap::new();
        let data_value = vec![to_value(RecordValue {
            r#type: 1,
            text: "text 5".to_string(),
        }).unwrap()];
        hm5.insert("fld1".to_string(), data_value);
        let data_value = vec![to_value("opt3".to_string()).unwrap()];
        hm5.insert("fld2".to_string(), data_value);
        assert_eq!(record_vos, vec![
            RecordSO {
                id: "rec1".to_string(),
                comment_count: 0,
                data: to_value(hm).unwrap(),
                ..Default::default()
            },
            RecordSO {
                id: "rec2".to_string(),
                comment_count: 0,
                data: to_value(hm2).unwrap(),
                ..Default::default()
            },
            RecordSO {
                id: "rec3".to_string(),
                comment_count: 1,
                data: to_value(hm3).unwrap(),
                comments: Some(vec![Comments {
                    revision: 7,
                    created_at: 1669886283547,
                    comment_id: "cmt1001".to_string(),
                    unit_id: "100004".to_string(),
                    comment_msg: CommentMsg {
                        r#type: "dfs".to_string(),
                        content: "foo".to_string(),
                        html: "foo".to_string(),
                        ..Default::default()
                    },
                    ..Default::default()
                  }]),
                ..Default::default()
            },
            RecordSO {
                id: "rec4".to_string(),
                comment_count: 0,
                data: to_value(hm4).unwrap(),
                ..Default::default()
            },
            RecordSO {
                id: "rec5".to_string(),
                comment_count: 0,
                data: to_value(hm5).unwrap(),
                ..Default::default()
            },
        ]);
    }

    #[tokio::test]
    async fn should_increment_number_of_records_after_adding_a_record() {
        let mut dst1 = get_datasheet(get_context());
        println!("dst1.datasheet_pack.snapshot: {:?}", dst1.context.datasheet_pack.snapshot.datasheet_id);
        let view1 = dst1.get_view("viw1");
        assert!(view1.is_some());
        let mut view1 = view1.unwrap();
        let mut hm = HashMap::new();
        let data_value = to_value(vec![to_value(RecordValue {
            r#type: 1,
            text: "text 4".to_string(),
        }).unwrap()]).unwrap();
        hm.insert("fld1".to_string(), data_value);
        let data_value = to_value(vec![to_value("opt2".to_string()).unwrap()]).unwrap();
        hm.insert("fld2".to_string(), data_value);
        let succeeded = view1.add_records(
            IAddRecordsOptions {
                index: 0,
                record_values: Some(vec![hm]),
                ..Default::default()
            },
            SaveOptions {..Default::default()}
        ).await.unwrap();

        assert_eq!(succeeded.result, ExecuteResult::Success);
        let view1 = dst1.get_view("viw1");
        assert!(view1.is_some());
        let view1 = view1.unwrap();
        let records = view1.get_records(IRecordsOptions{..Default::default()});

        assert_eq!(records.len(), 6);
    }

    #[tokio::test]
    async fn add_a_record_before_first_record() {
        let mut dst1 = get_datasheet(get_context());
        let view1 = dst1.get_view("viw1");
        assert!(view1.is_some());
        let mut view1 = view1.unwrap();
        let result = view1.add_records(
            IAddRecordsOptions{ 
                index: 0,
                record_values: Some(vec![mock_record_values()[0].clone()]),
                ..Default::default()
            },
            SaveOptions {..Default::default()}
        ).await.unwrap();

        assert_eq!(result.result, ExecuteResult::Success);

        let view1 = dst1.get_view("viw1");
        assert!(view1.is_some());
        let view1 = view1.unwrap();
        let records = view1.get_records(IRecordsOptions{..Default::default()});

        assert_eq!(records.len(), 6);

        let mut first_record_vo = records[0].get_view_object(mock_record_vo_transformer);
        first_record_vo = assert_record_id(first_record_vo, "rec4");

        assert_eq!(first_record_vo, mock_records()[0]);
    }

    #[tokio::test]
    async fn add_a_record_in_middle() {
        let mut dst1 = get_datasheet(get_context());
        let view1 = dst1.get_view("viw1");
        assert!(view1.is_some());
        let mut view1 = view1.unwrap();
        let result = view1.add_records(
            IAddRecordsOptions{ 
                index: 1,
                record_values: Some(vec![mock_record_values()[0].clone()]),
                ..Default::default()
            },
            SaveOptions {..Default::default()}
        ).await.unwrap();

        assert_eq!(result.result, ExecuteResult::Success);

        let view1 = dst1.get_view("viw1");
        assert!(view1.is_some());
        let view1 = view1.unwrap();
        let records = view1.get_records(IRecordsOptions{..Default::default()});

        assert_eq!(records.len(), 6);
        
        let mut first_record_vo = records[1].get_view_object(mock_record_vo_transformer);
        first_record_vo = assert_record_id(first_record_vo, "rec4");

        assert_eq!(first_record_vo, mock_records()[0]);
    }

    #[tokio::test]
    async fn add_multiple_records() {
        let mut dst1 = get_datasheet(get_context());
        let view1 = dst1.get_view("viw1");
        assert!(view1.is_some());
        let mut view1 = view1.unwrap();
        let result = view1.add_records(
            IAddRecordsOptions {
                index: 1,
                record_values: Some(mock_record_values()),
                ..Default::default()
            },
            SaveOptions {..Default::default()}
        ).await.unwrap();

        assert_eq!(result.result, ExecuteResult::Success);

        let view1 = dst1.get_view("viw1");
        assert!(view1.is_some());
        let view1 = view1.unwrap();
        let records = view1.get_records(IRecordsOptions{..Default::default()});

        assert_eq!(records.len(), 8);

        let mut record_vos = records[1..4].iter().map(|record| record.get_view_object(mock_record_vo_transformer)).collect::<Vec<_>>();
        record_vos = assert_record_ids(record_vos, |i| format!("rec{}", i + 4));

        // assert_eq!(record_vos, mock_records);
        for i in 0..record_vos.len() {
            assert_eq!(record_vos[i], mock_records()[i]);
        }
    }

    #[tokio::test]
    async fn add_multiple_records_by_count() {
        let mut dst1 = get_datasheet(get_context());
        let view1 = dst1.get_view("viw1");
        assert!(view1.is_some());
        let mut view1 = view1.unwrap();
        let result = view1.add_records(
            IAddRecordsOptions {
                index: 1,
                count: 3,
                ..Default::default()
            },
            SaveOptions {..Default::default()}
        ).await.unwrap();

        assert_eq!(result.result, ExecuteResult::Success);

        let view1 = dst1.get_view("viw1");
        assert!(view1.is_some());
        let view1 = view1.unwrap();
        let records = view1.get_records(IRecordsOptions{..Default::default()});

        assert_eq!(records.len(), 8);

        let record_vos = records[1..4].iter().map(|record| record.get_view_object(mock_record_vo_transformer)).collect::<Vec<_>>();

        let mut record_vo = assert_record_id(record_vos[0].clone(), "rec4");
        assert_eq!(record_vo, mock_default_record());

        record_vo = assert_record_id(record_vos[1].clone(), "rec4");
        assert_eq!(record_vo, mock_default_record());

        record_vo = assert_record_id(record_vos[2].clone(), "rec4");
        assert_eq!(record_vo, mock_default_record());
    }

    #[tokio::test]
    async fn should_be_identical_to_add_records_via_do_command() {
        let mut dst1 = get_datasheet(get_context());
        let result = dst1.add_records(
            IAddRecordsOptions {
                // datasheet_id: Some(dst1.context.datasheet_pack.snapshot.datasheet_id.clone()),
                view_id: "viw1".to_string(),
                index: 0,
                count: 1,
                record_values: Some(vec![mock_record_values()[0].clone()]),
                ignore_field_permission: Some(true),
                ..Default::default()
            },
            SaveOptions { ..Default::default() },
        ).await.unwrap();

        assert_eq!(result.result, ExecuteResult::Success);

        let view1 = dst1.get_view("viw1");
        assert!(view1.is_some());
        let view1 = view1.unwrap();
        let records = view1.get_records(IRecordsOptions{..Default::default()});

        assert_eq!(records.len(), 6);

        let mut first_record_vo = records[0].get_view_object(mock_record_vo_transformer);
        first_record_vo = assert_record_id(first_record_vo, "rec4");

        assert_eq!(first_record_vo, mock_records()[0]);
    }
}
