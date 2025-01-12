use std::collections::HashMap;
use std::rc::Rc;
use crate::fields::property::FieldPropertySO;
use crate::mock::{get_datasheet_map_pack, get_datasheet_pack};
use crate::ot::changeset::{Operation, ResourceOpsCollect, LocalChangeset};
use crate::ot::types::{ActionOTO, ResourceType};
use crate::so::{FieldKindSO, RecordSO, prepare_context_data, FieldSO, DatasheetPackContext};
use databus_shared::prelude::HashMapExt;
use json0::operation::{PathSegment, OperationKind};
use serde_json::{to_value, from_value};
use serde::{Deserialize, Serialize};
use crate::so::NodeSO;
use crate::Datasheet;
use super::MockDataStorageProvider;

#[derive(Debug, Deserialize, Serialize)]
#[serde(rename_all = "camelCase")]
struct ListDelete {
    pub field_id: String,
}

#[derive(Debug, Deserialize, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct RecordValue {
    pub r#type: usize,
    pub text: String,
}

#[cfg(test)]
pub fn get_datasheet(context: Rc<DatasheetPackContext>) -> Datasheet{
    let dst_id = context.datasheet_pack.snapshot.datasheet_id.clone();
    Datasheet::new(dst_id, context, Box::new(MockDataStorageProvider{}))
}

#[cfg(test)]
pub fn get_context() -> Rc<DatasheetPackContext>{
    let base_datasheet_pack = get_datasheet_map_pack(1).unwrap();
    let snapshot = base_datasheet_pack.snapshot;
    let mut datasheet_pack = get_datasheet_pack().unwrap();
    datasheet_pack.snapshot = snapshot.clone();
    let context = prepare_context_data(Box::new(datasheet_pack));
    let context = Rc::new(context);
    context
}

#[cfg(test)]
pub fn get_context_with_foreign(i: i32, f1: i32, f2: i32) -> Rc<DatasheetPackContext>{
    let base_datasheet_pack = get_datasheet_map_pack(i).unwrap();
    let snapshot = base_datasheet_pack.snapshot;
    let mut datasheet_pack = get_datasheet_pack().unwrap();
    datasheet_pack.snapshot = snapshot.clone();
    let datasheet = base_datasheet_pack.datasheet;
    let datasheet:anyhow::Result<NodeSO> = from_value(datasheet)
    .map_err(|e| {
        println!("e: {:?}", e);
        anyhow::anyhow!(e)
    });
    let datasheet = datasheet.unwrap();
    datasheet_pack.datasheet = datasheet;
    let base_datasheet_pack = get_datasheet_map_pack(f1).unwrap();
    let snapshot = base_datasheet_pack.snapshot;
    let datasheet = base_datasheet_pack.datasheet;
    let base_datasheet_pack = get_datasheet_map_pack(f2).unwrap();
    let snapshot2 = base_datasheet_pack.snapshot;
    let datasheet2 = base_datasheet_pack.datasheet;
    let pre = datasheet_pack.foreign_datasheet_map.clone().unwrap();
    let mut i_tmp = 0;
    let mut new_hashmap = pre.clone();
    for (_k, v) in &pre {
        if i_tmp == 0 {
            let mut n_v = v.clone();
            n_v.snapshot = snapshot.clone();
            n_v.datasheet = datasheet.clone();
            new_hashmap.insert(snapshot.datasheet_id.to_string(), n_v);
        }else if i_tmp == 1{
            let mut n_v = v.clone();
            n_v.snapshot = snapshot2.clone();
            n_v.datasheet = datasheet2.clone();
            new_hashmap.insert(snapshot2.datasheet_id.to_string(), n_v);
            break;
        }
        i_tmp += 1;
    }
    datasheet_pack.foreign_datasheet_map = Some(new_hashmap);
    let context = prepare_context_data(Box::new(datasheet_pack));
    let context = Rc::new(context);
    context
}

#[cfg(test)]
pub fn mock_ops_collects_of_add_one_default_record_in_dst1(record_id: &str) -> Vec<ResourceOpsCollect> {
    vec![
        ResourceOpsCollect {
            operations: vec![mock_operation_of_add_one_default_record_in_dst1(record_id)],
            resource_id: "dst1".to_string(),
            resource_type: ResourceType::Datasheet,
            ..Default::default()
        },
    ]
}

#[cfg(test)]
pub fn mock_operation_of_add_one_default_record_in_dst1(record_id: &str) -> Operation {
    let mut hm = HashMap::new();
    hm.insert("recordId".to_string(), record_id);
    let mut hm2 = HashMap::new();
    hm2.insert("fld2".to_string(), vec!["opt2".to_string(), "opt1".to_string()]);

    let actions = vec![
        ActionOTO {
            op_name: "LI".to_string(),
            op: json0::Operation {
                p: vec![
                    PathSegment::String("meta".to_string()),
                    PathSegment::String("views".to_string()),
                    PathSegment::Number(0),
                    PathSegment::String("rows".to_string()),
                    PathSegment::Number(0),
                ],
                kind: OperationKind::ListInsert {
                    li: to_value(&hm).unwrap(),
                },
            },
        },
        ActionOTO {
            op_name: "LI".to_string(),
            op: json0::Operation {
                p: vec![
                    PathSegment::String("meta".to_string()),
                    PathSegment::String("views".to_string()),
                    PathSegment::Number(1),
                    PathSegment::String("rows".to_string()),
                    PathSegment::Number(3),
                ],
                kind: OperationKind::ListInsert {
                    li: to_value(&hm).unwrap(),
                },
            },
        },
        ActionOTO {
            op_name: "OI".to_string(),
            op: json0::Operation {
                p: vec![
                    PathSegment::String("recordMap".to_string()),
                    PathSegment::String(record_id.to_string()),
                ],
                kind: OperationKind::ObjectInsert {
                    oi: to_value(&RecordSO {
                        id: record_id.to_string(),
                        comment_count: 0,
                        data: to_value(hm2).unwrap(),
                        ..Default::default()
                    }).unwrap(),
                },
            },
        },
    ];
    let field_type_map = HashMapExt::from_iter(vec![("fld2".to_owned(), FieldKindSO::MultiSelect)]);
    Operation {
        cmd: "AddRecords".to_string(),
        actions,
        field_type_map: Some(field_type_map),
        ..Default::default()
    }
}

#[cfg(test)]
pub fn mock_operation_of_delete_link_field_in_dst2() -> Operation {
    let vec: Vec<i32> = Vec::new();
        Operation{
        cmd: "DeleteField".to_string(),
        actions: vec![
            ActionOTO {
            op_name: "OD".to_string(),
            op: json0::Operation {
                p: vec![
                    PathSegment::String("recordMap".to_string()),
                    PathSegment::String("rec2-1".to_string()),
                    PathSegment::String("data".to_string()),
                    PathSegment::String("fld2-2".to_string()),
                ],
                kind: OperationKind::ObjectDelete {
                    od: to_value(vec).unwrap(),
                }
            }
            },
            ActionOTO {
            op_name: "OD".to_string(),
            op: json0::Operation {
                p: vec![
                    PathSegment::String("recordMap".to_string()),
                    PathSegment::String("rec2-2".to_string()),
                    PathSegment::String("data".to_string()),
                    PathSegment::String("fld2-2".to_string()),
                ],
                kind: OperationKind::ObjectDelete {
                    od: to_value(vec!["rec3-1".to_string()]).unwrap(),
                }
            }
            },
            ActionOTO {
                op_name: "LD".to_string(),
                op: json0::Operation {
                    p: vec![
                        PathSegment::String("meta".to_string()),
                        PathSegment::String("views".to_string()),
                        PathSegment::Number(0),
                        PathSegment::String("columns".to_string()),
                        PathSegment::Number(1),
                    ],
                    kind: OperationKind::ListDelete {
                    ld: to_value(ListDelete {field_id: "fld2-2".to_string()}).unwrap()
                    },
                }
            },
            ActionOTO {
            op_name: "OD".to_string(),
            op: json0::Operation {
                p: vec![
                    PathSegment::String("meta".to_string()),
                    PathSegment::String("fieldMap".to_string()),
                    PathSegment::String("fld2-2".to_string()),
                ],
                kind: OperationKind::ObjectDelete {
                    od: serde_json::to_value(FieldSO {
                    id: "fld2-2".to_string(),
                    name: "Field 2".to_string(),
                    property: Some(FieldPropertySO {
                        brother_field_id: Some("fld3-2".to_string()),
                        foreign_datasheet_id: Some("dst3".to_string()),
                        ..Default::default()
                    }),
                    kind: FieldKindSO::Link,
                    ..Default::default()
                    }).unwrap(),
                }
            }
            },
        ],
        ..Default::default()
    }
}

#[cfg(test)]
pub fn mock_linked_operations_of_delete_link_field_in_dst2() -> Vec<Operation> {
    vec![Operation{
      cmd: "DeleteField".to_string(),
      main_link_dst_id: Some("dst2".to_string()),
      actions: vec![
        ActionOTO {
          op_name: "LD".to_string(),
          op: json0::Operation {
              p: vec![
                  PathSegment::String("meta".to_string()),
                  PathSegment::String("views".to_string()),
                  PathSegment::Number(0),
                  PathSegment::String("columns".to_string()),
                  PathSegment::Number(1),
              ],
              kind: OperationKind::ListDelete {
                ld: to_value(ListDelete {field_id: "fld3-2".to_string()}).unwrap()
              },
          }
        },
        ActionOTO {
          op_name: "OD".to_string(),
          op: json0::Operation {
              p: vec![
                  PathSegment::String("meta".to_string()),
                  PathSegment::String("fieldMap".to_string()),
                  PathSegment::String("fld3-2".to_string()),
              ],
              kind: OperationKind::ObjectDelete {
                od: serde_json::to_value(FieldSO {
                  id: "fld3-2".to_string(),
                  name: "3 my field 2".to_string(),
                  property: Some(FieldPropertySO {
                      brother_field_id: Some("fld2-2".to_string()),
                      foreign_datasheet_id: Some("dst2".to_string()),
                      ..Default::default()
                  }),
                  kind: FieldKindSO::Link,
                  ..Default::default()
                }).unwrap(),
            }
          }
        },
      ],
      ..Default::default()
    }]
}

#[cfg(test)]
pub fn mock_changesets_of_delete_link_field_in_dst2() -> Vec<LocalChangeset> { 
    vec![
        LocalChangeset {
            base_revision: 2,
            message_id: "x".to_string(),
            resource_id: "dst2".to_string(),
            resource_type: ResourceType::Datasheet,
            operations: mock_ops_collects_of_delete_link_field_in_dst2()[0].operations.clone(),
        },
        LocalChangeset {
            base_revision: 3,
            message_id: "x".to_string(),
            resource_id: "dst3".to_string(),
            resource_type: ResourceType::Datasheet,
            operations: mock_ops_collects_of_delete_link_field_in_dst2()[1].operations.clone(),
        },
    ]
}

#[cfg(test)]
pub fn mock_ops_collects_of_delete_link_field_in_dst2() -> Vec<ResourceOpsCollect> {
    let mut op = mock_operation_of_delete_link_field_in_dst2();
    op.main_link_dst_id = Some("dst2".to_string());
    vec![
      ResourceOpsCollect { 
        resource_id: "dst2".to_string(), 
        resource_type: ResourceType::Datasheet, 
        operations: vec![op], 
        ..Default::default()
      },
      ResourceOpsCollect { 
        resource_id: "dst3".to_string(), 
        resource_type: ResourceType::Datasheet, 
        operations: mock_linked_operations_of_delete_link_field_in_dst2(), 
        ..Default::default()
      },
    ]
  }