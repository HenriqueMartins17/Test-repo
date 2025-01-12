
#[cfg(test)]
mod tests {
    use std::collections::HashMap;
    use std::rc::Rc;
    use crate::mock::get_datasheet;
    use crate::prelude::Comments;
    use crate::prelude::CommentMsg;
    use serde_json::to_value;
    use serde::{Deserialize, Serialize};

    use crate::{so::{DatasheetPackContext, prepare_context_data, RecordSO}, mock::{get_datasheet_map_pack, get_datasheet_pack}, logic::IRecordsOptions};

    #[derive(Debug, Deserialize, Serialize)]
    #[serde(rename_all = "camelCase")]
    struct RecordValue {
      pub r#type: usize,
      pub text: String,
    }

    fn get_context() -> Rc<DatasheetPackContext>{
        let base_datasheet_pack = get_datasheet_map_pack(1).unwrap();
        let snapshot = base_datasheet_pack.snapshot;
        let mut datasheet_pack = get_datasheet_pack().unwrap();
        datasheet_pack.snapshot = snapshot.clone();
        let context = prepare_context_data(Box::new(datasheet_pack));
        let context = Rc::new(context);
        context
    }

    #[test]
    fn basic_record_info() {
        let mut dst1 = get_datasheet(get_context());
        let view1 = dst1.get_view("viw1");
        assert!(view1.is_some());
        let view1 = view1.unwrap();
        assert_eq!(view1.id(), "viw1");

        let records = view1.get_records(IRecordsOptions::default());

        let record_data = records.iter().map(|record| RecordSO { id: record.id().to_string(), comments: Some(record.comments()), ..Default::default() }).collect::<Vec<_>>();

        assert_eq!(record_data, vec![
            RecordSO { id: "rec1".to_string(), comments: Some(vec![]), ..Default::default() },
            RecordSO { id: "rec2".to_string(), comments: Some(vec![]), ..Default::default() },
            RecordSO {
                id: "rec3".to_string(),
                comments: Some(vec![
                    Comments {
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
                    },
                ]),
                ..Default::default()
            },
            RecordSO { id: "rec4".to_string(), comments: Some(vec![]), ..Default::default() },
            RecordSO { id: "rec5".to_string(), comments: Some(vec![]), ..Default::default() },
        ]);
    }

    #[test]
    fn get_view_info() {
        let mut dst1 = get_datasheet(get_context());
        let view1 = dst1.get_view("viw1");
        assert!(view1.is_some());
        let view1 = view1.unwrap();
        assert_eq!(view1.id(), "viw1");

        let records = view1.get_records(IRecordsOptions::default());

        assert!(records.len() > 1);

        let record = records[1].get_view_object(|x, _| x.clone());
        let mut hm = HashMap::new();
        let data_value = vec![to_value(RecordValue {
            r#type: 1,
            text: "text 2".to_string(),
        }).unwrap()];
        hm.insert("fld1".to_string(), to_value(data_value).unwrap());
        let data_value = vec![to_value("opt1".to_string()).unwrap()];
        hm.insert("fld2".to_string(), to_value(data_value).unwrap());
        assert_eq!(record, RecordSO {
            id: "rec2".to_string(),
            data: to_value(hm).unwrap(),
            comment_count: 0,
            ..Default::default()
        });
    }
}