

#[cfg(test)]
mod tests {

  use crate::mock::get_snapshot_pack;
  use crate::ot::{PayloadAddRecordVO, DatasheetActions};

  #[test]
  fn test_add_record_to_action() {
    let datasheet_pack = get_snapshot_pack(0).unwrap();
    let snapshot = datasheet_pack.snapshot;
    //行尾插入预期结果
    // let result_value = serde_json::from_str::<Value>(r#"
    // [
    //   {
    //       "n": "LI",
    //       "p": [
    //           "meta",
    //           "views",
    //           0,
    //           "rows",
    //           3
    //       ],
    //       "li": {
    //           "recordId": "reclj2P5LfpTF"
    //       }
    //   },
    //   {
    //       "n": "OI",
    //       "p": [
    //           "recordMap",
    //           "reclj2P5LfpTF"
    //       ],
    //       "oi": {
    //           "id": "reclj2P5LfpTF",
    //           "data": {},
    //           "commentCount": 0,
    //           "comments": [],
    //           "recordMeta": {}
    //       }
    //   }
    // "#).unwrap();
    let payload = PayloadAddRecordVO {
      view_id: "viwDtemXMuFxz".to_string(),
      record: serde_json::from_str(r#"
            {
              "id": "reclj2P5LfpTF",
              "data": {},
              "commentCount": 0,
              "comments": [],
              "recordMeta": {}
            }
      "#).unwrap(),
      index: 3,
    };
    let _result = DatasheetActions::add_record_to_action(snapshot.clone(), payload).unwrap();
    // let json_value = serde_json::to_value(result.unwrap()).unwrap();
    // assert_eq!(json_value, result_value);
    // println!("result = {:#?}", result);

    //行首插入
    // [
    //   {
    //       "n": "LI",
    //       "p": [
    //           "meta",
    //           "views",
    //           0,
    //           "rows",
    //           0
    //       ],
    //       "li": {
    //           "recordId": "recslko04eos1"
    //       }
    //   },
    //   {
    //       "n": "OI",
    //       "p": [
    //           "recordMap",
    //           "recslko04eos1"
    //       ],
    //       "oi": {
    //           "id": "recslko04eos1",
    //           "data": {},
    //           "commentCount": 0,
    //           "comments": [],
    //           "recordMeta": {}
    //       }
    //   }
    // ]
    let payload = PayloadAddRecordVO {
      view_id: "viwDtemXMuFxz".to_string(),
      record: serde_json::from_str(r#"
            {
              "id": "recslko04eos1",
              "data": {},
              "commentCount": 0,
              "comments": [],
              "recordMeta": {}
            }
      "#).unwrap(),
      index: 0,
    };
    let _actions = DatasheetActions::add_record_to_action(snapshot.clone(), payload).unwrap();
    // println!("actions2 = {:#?}", actions);

    //行中插入
    // [
  //     {
  //       "n": "LI",
  //       "p": [
  //           "meta",
  //           "views",
  //           0,
  //           "rows",
  //           2
  //       ],
  //       "li": {
  //           "recordId": "recyYpNFsYmUo"
  //       }
  //   },
  //   {
  //       "n": "OI",
  //       "p": [
  //           "recordMap",
  //           "recyYpNFsYmUo"
  //       ],
  //       "oi": {
  //           "id": "recyYpNFsYmUo",
  //           "data": {},
  //           "commentCount": 0,
  //           "comments": [],
  //           "recordMeta": {}
  //       }
  //   }
  // ]
    let payload = PayloadAddRecordVO {
      view_id: "viwDtemXMuFxz".to_string(),
      record: serde_json::from_str(r#"
            {
              "id": "recyYpNFsYmUo",
              "data": {},
              "commentCount": 0,
              "comments": [],
              "recordMeta": {}
            }
      "#).unwrap(),
      index: 2,
    };
    let _actions = DatasheetActions::add_record_to_action(snapshot.clone(), payload).unwrap();
    // println!("actions3 = {:#?}", actions);

    //覆盖率测试,测试结果说明grcov结果有问题Record id recNMf9zMl35W already exists
    let payload = PayloadAddRecordVO {
      view_id: "viwDtemXMuFxz".to_string(),
      record: serde_json::from_str(r#"
            {
              "id": "recNMf9zMl35W",
              "data": {},
              "commentCount": 0,
              "comments": [],
              "recordMeta": {}
            }
      "#).unwrap(),
      index: 2,
    };
    let _actions = DatasheetActions::add_record_to_action(snapshot.clone(), payload).unwrap();
  }
}
