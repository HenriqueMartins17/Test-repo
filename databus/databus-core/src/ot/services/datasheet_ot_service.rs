use core::panic;
use std::{collections::{HashMap, HashSet}, sync::Arc};

use chrono::Utc;
use json0::operation::{PathSegment, OperationKind};
use serde::{Serialize, Deserialize};
use serde_json::{Value, from_value, to_value, to_string, Map};
use time::Instant;

use crate::{ot::{changeset::{Operation, RemoteChangeset}, EffectConstantName, types::ActionOTO, CommonData, CollaCommandName, IRestoreRecordInfo, jot_apply_snapshot}, shared::{NodePermissionSO, AuthHeader}, so::{FieldKindSO, FieldSO, Comments, ViewSO, RecordAlarm, DatasheetMetaSO, WidgetPanelSO, RecordMeta, FieldUpdatedValue, FieldExtraMapValue, DatasheetSnapshotSO}, data_source_provider::IDataSourceProvider, fields::{property::FieldPropertySO, get_is_computed}, utils::utils::generate_u64_id};

use super::SourceTypeEnum;

#[derive(Debug, Serialize, Deserialize, Clone)]
#[serde(rename_all = "camelCase")]
pub struct FieldData {
    pub field_id: String,
    pub data: Value,
}

#[derive(Debug, Serialize, Deserialize, Default, Clone)]
#[serde(rename_all = "camelCase")]
pub struct AttachCiteCollector {
    pub node_id: String,
    pub add_token: Vec<TokenInfo>,
    pub remove_token: Vec<TokenInfo>,
}

#[derive(Debug, Serialize, Deserialize, Default, Clone)]
#[serde(rename_all = "camelCase")]
pub struct TokenInfo {
    pub token: String,
    pub name: String,
}

#[derive(Debug, Serialize, Deserialize, Default)]
#[serde(rename_all = "camelCase")]
pub struct OpAttach {
    pub add_token: Vec<TokenInfo>,
    pub remove_token: Vec<TokenInfo>,
}

#[derive(Debug, Serialize, Deserialize, Default, Clone)]
#[serde(rename_all = "camelCase")]
pub struct RecordSubscriptions {
    pub unit_id: String,
    pub record_id: String,
}

#[derive(Debug, Serialize, Deserialize, Default, Clone)]
pub struct CorrectComment {
    pub index: i32,
    pub comment: Comments,
}

#[derive(Debug, Serialize, Deserialize, Default, Clone)]
#[serde(rename_all = "camelCase")]
pub struct CommentEmoji {
    pub emoji_action: bool,
    pub comment: Comments,
}

#[derive(Debug, Serialize, Deserialize, Default, Clone)]
#[serde(rename_all = "camelCase")]
pub struct FieldTypeParams {
    pub uuid: Option<String>,
    pub fields: Vec<FieldSO>,
    pub next_id: Option<i32>,
}

fn intersection<T: std::cmp::Eq + std::hash::Hash + Clone>(vec1: &[T], vec2: &[T]) -> Vec<T> {
    let set1: HashSet<_> = vec1.iter().cloned().collect();
    let set2: HashSet<_> = vec2.iter().cloned().collect();
    set1.intersection(&set2).cloned().collect()
}

#[derive(Debug, Serialize, Deserialize, Default, Clone)]
#[serde(rename_all = "camelCase")]

pub struct ResultSet {
    meta_actions: Vec<ActionOTO>,
    // to_create_record: HashMap<String, RecordCellValue>,
    pub to_create_record: HashMap<String, Value>,
    // to_unarchive_record: HashMap<String, RecordCellValue>,
    to_unarchive_record: HashMap<String, Value>,
    pub to_delete_record_ids: Vec<String>,
    to_archive_record_ids: Vec<String>,
    clean_field_map: HashMap<String, FieldKindSO>,
    clean_record_cell_map: HashMap<String, Vec<FieldData>>,
    replace_cell_map: HashMap<String, Vec<FieldData>>,
    // init_field_map: HashMap<i32, Vec<FieldSO>>,
    init_field_map: HashMap<i32, Vec<Value>>,
    to_correct_comment: HashMap<String, Vec<CorrectComment>>,
    fld_op_in_view_map: HashMap<String, bool>,
    fld_op_in_rec_map: HashMap<String, String>,
    to_delete_comment_ids: HashMap<String, Vec<String>>,
    pub delete_widget_ids: Vec<String>,
    pub add_widget_ids: Vec<String>,
    space_capacity_over_limit: bool,
    auth: AuthHeader, // replace with actual type
    attach_cite_collector: AttachCiteCollector,
    datasheet_id: String,
    source_type: Option<SourceTypeEnum>,
    pub to_create_foreign_datasheet_id_map: HashMap<String, String>,
    pub to_delete_foreign_datasheet_id_map: HashMap<String, String>,
    pub to_create_look_up_properties: Vec<FieldPropertySO>,
    pub to_delete_look_up_properties: Vec<String>,
    pub to_change_formula_expressions: Vec<String>,
    change_view_number: i32,
    change_field_number: i32,
    to_delete_field_ids: Vec<String>,
    temporary_field_map: HashMap<String, FieldSO>, // replace with actual type
    temporary_views: Vec<ViewSO>, // replace with actual type
    to_update_comment_emoji: HashMap<String, Vec<CommentEmoji>>,
    link_action_main_dst_id: Option<String>,
    main_link_dst_permission_map: HashMap<String, NodePermissionSO>,
    to_create_alarms: HashMap<String, Vec<RecordAlarm>>,
    to_delete_alarms: HashMap<String, Vec<RecordAlarm>>,
    updated_alarm_ids: Vec<String>,
    pub add_views: Vec<Value>,
    pub delete_views: Vec<Value>,
    to_create_record_subscriptions: Vec<RecordSubscriptions>, // replace with actual type
    to_cancel_record_subscriptions: Vec<RecordSubscriptions>, // replace with actual type
    creator_auto_subscribed_record_ids: Vec<String>,
    pub space_id: String,
}

pub struct DatasheetOtService {
    pub loader: Arc<dyn IDataSourceProvider>,
}

impl DatasheetOtService {
    pub fn new(loader: Arc<dyn IDataSourceProvider>) -> Self {
        Self {loader}
    }

    fn is_attach_field(cell_value: &Value) -> bool {
        match cell_value {
            Value::Null => false,
            _ => {
                if cell_value.is_array() {
                    let cell_value = cell_value.as_array().unwrap();
                    if let Some(first_value) = cell_value.first() {
                        let mime_type = first_value.get("mimeType");
                        let token = first_value.get("token");
                        return mime_type.is_some() && token.is_some();
                    }
                    false
                }else {
                    false
                } 
            },
        }
    }

    fn set_map_val_if_exist<T: Clone + Eq + std::hash::Hash, S: Clone>(map: &mut HashMap<T, Vec<S>>, key: &T, value: S) {
        if map.contains_key(key) {
            if let Some(values) = map.get_mut(key) {
                values.push(value);
            }
        } else {
            map.insert(key.clone(), vec![value]);
        }
    }

    fn generate_jot_action(name: &str, path: Vec<&str>, new_value: Value, old_value: Option<Value>) -> ActionOTO {
        let mut new_path = Vec::new();
        for p in path {
            new_path.push(PathSegment::String(p.to_string()));
        }
        match name {
            "OI" => ActionOTO { 
                op_name: name.to_string(),
                op: json0::Operation {
                    p: new_path,
                    kind: OperationKind::ObjectInsert { oi: new_value },
                }
            },
            "OR" => ActionOTO { 
                op_name: name.to_string(),
                op: json0::Operation {
                    p: new_path,
                    kind: OperationKind::ObjectReplace { oi: new_value, od: old_value.unwrap() },
                }
            },
            _ => ActionOTO { 
                op_name: name.to_string(),
                op: json0::Operation {
                    p: new_path,
                    kind: OperationKind::ObjectInsert { oi: new_value },
                }
            },
        }
    }

    pub fn create_result_set(&self) -> ResultSet {
        ResultSet::default()
    }

    fn collect_by_add_field(&self, _cmd: &str, action: &ActionOTO, permission: &NodePermissionSO, result_set: &mut ResultSet) {
        let oi = match &action.op.kind {
            OperationKind::ObjectInsert{oi} => oi,
            OperationKind::ObjectReplace{od:_,oi} => oi,
            _ => &Value::Null,
        };
        println!("oi: {:?}", oi);
        let oi_data:FieldSO = from_value(oi.clone()).unwrap();
        result_set.temporary_field_map.insert(oi_data.id.clone(), oi_data.clone());
    
        let property = oi_data.property.clone().unwrap();
        if oi_data.kind == FieldKindSO::Link || oi_data.kind == FieldKindSO::OneWayLink {
            let main_dst_id = result_set.link_action_main_dst_id.clone();
    
            if main_dst_id.is_none() {
                panic!("Operation abnormal");
            }
    
            // If this action is generated by current datasheet
            let is_self_action = main_dst_id == Some(result_set.datasheet_id.clone());
    
            if is_self_action {
                if !permission.field_creatable.unwrap_or(false) {
                    panic!("Operation denied");
                }
            } else {
                if property.foreign_datasheet_id != main_dst_id {
                    panic!("Operation denied");
                }
                if !result_set.main_link_dst_permission_map.get(&main_dst_id.unwrap()).unwrap().field_creatable.unwrap_or(false) {
                    panic!("Operation denied");
                }
                // Operation of linked datasheet relating to creating link field, validate editable permission
                if !permission.editable.unwrap_or(false) {
                    panic!("Operation denied");
                }
            }
            let foreign_datasheet_id = property.foreign_datasheet_id.clone().unwrap_or_default();
            // self.logger.debug(format!("[{}] Create or copy link field -> linked datasheet [{}]", result_set.datasheet_id, foreign_datasheet_id));
            result_set.to_create_foreign_datasheet_id_map.insert(oi_data.id.clone(), foreign_datasheet_id);
        } else {
            // Not creating linked field, check permission directly
            if !permission.field_creatable.unwrap_or(false) {
                panic!("Operation denied");
            }
            if oi_data.kind == FieldKindSO::AutoNumber {
                // Create AutoNumber field, needs to initialize value according to view
                let view_idx = property.view_idx.clone();
                let next_id = property.next_id.clone();
                if next_id.is_none() || next_id.unwrap() == 0 {
                    DatasheetOtService::set_map_val_if_exist(&mut result_set.init_field_map, &view_idx.unwrap_or(0), oi.clone());
                }
            } else if oi_data.kind == FieldKindSO::LookUp {
                println!("oi_data.kind == FieldKindSO::LookUp");
                // result_set.to_create_look_up_properties.push(FieldPropertySO {
                //     field_id: oi_data.id.clone(),
                //     cmd: cmd.to_string(),
                //     ..oi_data.property.clone()
                // });
            } else if oi_data.kind == FieldKindSO::Formula {
                println!("oi_data.kind == FieldKindSO::Formula");
                // result_set.to_change_formula_expressions.push(FormulaExpression {
                //     create_expression: oi_data.property.expression.clone(),
                //     field_id: oi_data.id.clone()
                // });
            }
        }
        result_set.change_field_number += 1;
    }

    fn collect_by_change_field(&self, cmd: &str, action: &ActionOTO, permission: &NodePermissionSO, result_set: &mut ResultSet) {
        let oi = match &action.op.kind {
            OperationKind::ObjectInsert{oi} => oi,
            OperationKind::ObjectReplace{od:_,oi} => oi,
            _ => &Value::Null,
        };
        let oi_data:FieldSO = from_value(oi.clone()).unwrap();
        // let oi_data = &action.oi;
        let od = match &action.op.kind {
            OperationKind::ObjectDelete{od} => od,
            OperationKind::ObjectReplace{od,oi:_} => od,
            _ => &Value::Null,
        };
        let od_data:FieldSO = from_value(od.clone()).unwrap();

        let oi_property = oi_data.property.clone().unwrap();
        let od_property = od_data.property.clone().unwrap();
    
        // Modify field name
        if oi_data.name != od_data.name {
            if !permission.field_renamable.unwrap_or(false) {
                panic!("Operation denied");
            }
        }
    
        // Modify field description
        if oi_data.desc != od_data.desc {
            if !permission.field_property_editable.unwrap_or(false) {
                panic!("Operation denied");
            }
        }
    
        // Modify field type
        if oi_data.kind != od_data.kind {
            let mut skip = false;
    
            // operations on linked datasheet
            if result_set.link_action_main_dst_id != Some(result_set.datasheet_id.clone()) {
                // Since the database deleted link field, link field of linked datasheet is changed to text field,
                // don't check permission
                if (od_data.kind == FieldKindSO::Link || od_data.kind == FieldKindSO::OneWayLink) && od_property.foreign_datasheet_id == result_set.link_action_main_dst_id && oi_data.kind == FieldKindSO::Text {
                    skip = true;
                } else if (oi_data.kind == FieldKindSO::Link || oi_data.kind == FieldKindSO::OneWayLink) && oi_property.foreign_datasheet_id == result_set.link_action_main_dst_id && cmd.starts_with("UNDO:") {
                    // Since the database undo deleting link field, original link field of linked datasheet changes back to link field,
                    // don't check permission
                    skip = true;
                }
            }
    
            // Not related operation of linked datasheet, validate edit permission of field property (corresponds to manageable)
            if !skip && !permission.field_property_editable.unwrap_or(false) {
                panic!("Operation denied");
            }
    
            // Update field type in fieldMap, to facilitate later use of fieldMap
            result_set.temporary_field_map.insert(oi_data.id.clone(), oi_data.clone());
            
            // Deleted field type
            match od_data.kind {
                FieldKindSO::OneWayLink | FieldKindSO::Link => {
                    let foreign_datasheet_id = od_property.foreign_datasheet_id.clone().unwrap();
                    result_set.to_delete_foreign_datasheet_id_map.insert(od_data.id.clone(), foreign_datasheet_id);
                }
                FieldKindSO::LookUp => {
                    // result_set.to_delete_look_up_properties.push(LookUpProperty {
                    //     field_id: od_data.id.clone(),
                    //     ..od_data.property.clone()
                    // });
                }
                FieldKindSO::Formula => {
                    // result_set.to_change_formula_expressions.push(FormulaExpression {
                    //     delete_expression: od_data.property.expression.clone(),
                    //     field_id: od_data.id.clone()
                    // });
                }
                FieldKindSO::LastModifiedBy => {
                    // When modifier field is deleted, need collecting, later operations will not update deleted field.property.uuids
                    result_set.clean_field_map.insert(od_data.id.clone(), od_data.kind.clone());
                }
                _ => {}
            }
    
            // Modified field type
            match oi_data.kind {
                FieldKindSO::OneWayLink | FieldKindSO::Link => {
                    let foreign_datasheet_id = oi_data.property.clone().unwrap().foreign_datasheet_id.clone().unwrap();
                    result_set.to_create_foreign_datasheet_id_map.insert(oi_data.id.clone(), foreign_datasheet_id);
                }
                FieldKindSO::LookUp => {
                    // result_set.to_create_look_up_properties.push(LookUpProperty {
                    //     field_id: oi_data.id.clone(),
                    //     cmd: cmd.to_string(),
                    //     ..oi_data.property.clone().unwrap()
                    // });
                }
                FieldKindSO::Formula => {
                    // result_set.to_change_formula_expressions.push(FormulaExpression {
                    //     create_expression: oi_data.property.clone().unwrap().expression.clone().unwrap(),
                    //     field_id: oi_data.id.clone()
                    // });
                }
                FieldKindSO::AutoNumber => {
                    // Convert to AutoNumber field type, need to initialize value according to view
                    let property = oi_data.property.clone().unwrap();
                    let view_idx = property.view_idx.clone().unwrap();
                    let next_id = property.next_id.clone().unwrap();
                    if next_id == 0 {
                        DatasheetOtService::set_map_val_if_exist(&mut result_set.init_field_map, &view_idx, oi.clone());
                    }
                }
                _ => {}
            }
        } else {
            let allow_edit_field_types = vec![FieldKindSO::Member];
            if allow_edit_field_types.contains(&oi_data.kind) {
                // Special field requires permission above editable
                self.check_cell_val_permission(cmd, &oi_data.id, permission, result_set);
            } else if oi_data.kind != FieldKindSO::CreatedBy {
                // Do not check permission of creator field under this condition, there is no point in checking permission
                // due to field permission.
                // Otherwise, check field property edit permission (corresponds to manageable)
                if !permission.field_property_editable.unwrap_or(false) {
                    panic!("Operation denied");
                }
            }
            result_set.temporary_field_map.insert(oi_data.id.clone(), oi_data.clone());

            // Replace linked datasheet
            if oi_data.kind == FieldKindSO::Link && od_data.kind == FieldKindSO::Link {
                let oi_foreign_datasheet_id = oi_data.property.clone().unwrap().foreign_datasheet_id.clone().unwrap();
                let od_foreign_datasheet_id = od_data.property.clone().unwrap().foreign_datasheet_id.clone().unwrap();
                if oi_foreign_datasheet_id == od_foreign_datasheet_id {
                    return;
                }
                result_set.to_create_foreign_datasheet_id_map.insert(oi_data.id.clone(), oi_foreign_datasheet_id);
                result_set.to_delete_foreign_datasheet_id_map.insert(od_data.id.clone(), od_foreign_datasheet_id);
            }

            // Replace OneWayLink datasheet
            if oi_data.kind == FieldKindSO::OneWayLink && od_data.kind == FieldKindSO::OneWayLink {
                let oi_foreign_datasheet_id = oi_data.property.clone().unwrap().foreign_datasheet_id.clone().unwrap();
                let od_foreign_datasheet_id = od_data.property.clone().unwrap().foreign_datasheet_id.clone().unwrap();
                if oi_foreign_datasheet_id == od_foreign_datasheet_id {
                    return;
                }
                result_set.to_create_foreign_datasheet_id_map.insert(oi_data.id.clone(), oi_foreign_datasheet_id);
                result_set.to_delete_foreign_datasheet_id_map.insert(od_data.id.clone(), od_foreign_datasheet_id);
            }

            // Replace datasheet query
            if oi_data.kind == FieldKindSO::LookUp && od_data.kind == FieldKindSO::LookUp {
                let mut skip_field_permission = false;
                let oi_property = oi_data.property.clone().unwrap();
                let od_property = od_data.property.clone().unwrap();
                if oi_property.related_link_field_id == od_property.related_link_field_id && oi_property.look_up_target_field_id == od_property.look_up_target_field_id {
                    // If neither linking field of lookup field nor its target field changes, don't check permission,
                    // only check if referenced field set of lookup filtering condition have changed.
                    skip_field_permission = true;
                    let mut oi_refer_field_ids: Vec<String> = Vec::new();
                    let mut od_refer_field_ids: Vec<String> = Vec::new();
                    for condition in oi_property.filter_info.unwrap().conditions {
                        oi_refer_field_ids.push(condition.field_id.clone());
                    }
                    for condition in od_property.filter_info.unwrap().conditions {
                        od_refer_field_ids.push(condition.field_id.clone());
                    }
                    // If referenced fields of filter condition are the same, no
                    if oi_refer_field_ids.len() == od_refer_field_ids.len() && oi_refer_field_ids.iter().all(|e| od_refer_field_ids.contains(e)) {
                        return;
                    }
                }
                // result_set.to_create_look_up_properties.push(LookUpProperty {
                //     field_id: oi_data.id.clone(),
                //     cmd: cmd.to_string(),
                //     skip_field_permission,
                //     ..oi_data.property.clone().unwrap()
                // });
                // result_set.to_delete_look_up_properties.push(LookUpProperty {
                //     field_id: od_data.id.clone(),
                //     ..od_data.property.clone().unwrap()
                // });
            }
            if oi_data.kind == FieldKindSO::Formula && od_data.kind == FieldKindSO::Formula {
                if oi_property.expression == od_property.expression {
                    return;
                }
                // result_set.to_change_formula_expressions.push(FormulaExpression {
                //     create_expression: oi_expression,
                //     delete_expression: od_expression,
                //     field_id: oi_data.id.clone(),
                // });
            }
        }
    }

    fn collect_by_delete_field(&self, action: &ActionOTO, permission: &NodePermissionSO, result_set: &mut ResultSet) {
        let od = match &action.op.kind {
            OperationKind::ObjectDelete{od} => od,
            OperationKind::ObjectReplace{od,oi:_} => od,
            _ => &Value::Null,
        };
        let od_data: FieldSO = from_value(od.clone()).unwrap();
        let property = od_data.property.clone().unwrap();
        if od_data.kind == FieldKindSO::Link || od_data.kind == FieldKindSO::OneWayLink {
            let main_dst_id = &result_set.link_action_main_dst_id;
    
            if main_dst_id.is_none() {
                panic!("Operation abnormal");
            }
    
            // If this action is generated by current datasheet
            let is_self_action = main_dst_id == &Some(result_set.datasheet_id.clone());
    
            if is_self_action {
                if !permission.field_removable.unwrap_or(false) {
                    panic!("Operation denied");
                }
            } else {
                if &property.foreign_datasheet_id != main_dst_id {
                    panic!("Operation denied");
                }
                if !result_set.main_link_dst_permission_map.get(&main_dst_id.clone().unwrap()).unwrap().field_removable.unwrap_or(false) {
                    panic!("Operation denied");
                }
                // Operation on linked field creation related to linked datasheet, check editable permission
                if !permission.editable.unwrap_or(false) {
                    panic!("Operation denied");
                }
            }
            let foreign_datasheet_id = od_data.property.as_ref().unwrap().foreign_datasheet_id.clone().unwrap();
            println!("[{}] Delete linked field -> linked datasheet [{}]", result_set.datasheet_id, foreign_datasheet_id);
            result_set.to_delete_foreign_datasheet_id_map.insert(od_data.id.clone(), foreign_datasheet_id);
        } else {
            if !permission.field_removable.unwrap_or(false) {
                panic!("Operation denied");
            }
            let special_field_types = vec![FieldKindSO::LastModifiedTime, FieldKindSO::CreatedTime, FieldKindSO::CreatedBy, FieldKindSO::AutoNumber];
            if !special_field_types.contains(&od_data.kind) {
                // Collect deleted fields to clean up corresponding data in field_updated_map
                result_set.clean_field_map.insert(od_data.id.clone(), od_data.kind.clone());
            }
            if od_data.kind == FieldKindSO::LookUp {
                // result_set.to_delete_look_up_properties.push(LookUpProperty {
                //     field_id: od_data.id.clone(),
                //     ..od_data.property.clone().unwrap()
                // });
            } else if od_data.kind == FieldKindSO::Formula {
                // result_set.to_change_formula_expressions.push(FormulaExpression {
                //     delete_expression: od_data.property.clone().unwrap().expression.clone().unwrap(),
                //     field_id: od_data.id.clone(),
                // });
            }
        }
        result_set.change_field_number -= 1;
        result_set.to_delete_field_ids.push(od_data.id.clone());
    }

    fn collect_by_view(&self, action: &ActionOTO, permission: &NodePermissionSO, result_set: &mut ResultSet) {
        if Some(result_set.datasheet_id.clone()) == result_set.link_action_main_dst_id {
            let idx = action.op.p[2].clone();
            let idx = match idx {
                PathSegment::Number(n) => n,
                _ => panic!("collect_by_view Operation abnormal idx nor number"),
            };
            let view = result_set.temporary_views.get(idx).unwrap().clone();
            let li = match &action.op.kind {
                OperationKind::ListInsert{li} => li,
                OperationKind::ListReplace{ld:_,li} => li,
                _ => &Value::Null,
            };
            let ld = match &action.op.kind {
                OperationKind::ListDelete{ld} => ld,
                OperationKind::ListReplace{ld,li:_} => ld,
                _ => &Value::Null,
            };
            let has_lm = match &action.op.kind {
                OperationKind::ListMove{lm:_} => true,
                _ => false,
            };
            if action.op.p.len() == 3 {
                if li != &Value::Null {
                    if !permission.view_creatable.unwrap_or(false) {
                        panic!("Operation denied");
                    }
                    result_set.add_views.push(li.clone());
                    return;
                }
                if ld != &Value::Null {
                    if !permission.view_removable.unwrap_or(false) || view.lock_info.is_some() {
                        panic!("Operation denied");
                    }
                    result_set.delete_views.push(ld.clone());
                    return;
                }
                if has_lm {
                    if !permission.view_movable.unwrap() {
                        panic!("Operation denied");
                    }
                    return;
                }
            } else if action.op.p.len() > 3 {
                let str_p = format!("{:?}", action.op.p[3]);
                match str_p.as_str(){
                    "name" => {
                        if !permission.view_renamable.unwrap_or(false) {
                            panic!("Operation denied");
                        }
                        return;
                    }
                    "displayHiddenColumnWithinMirror" => {
                        if !permission.editable.unwrap_or(false) || view.lock_info.is_some() {
                            panic!("Operation denied");
                        }
                        return;
                    }
                    "filterInfo" => {
                        if !permission.view_filterable.unwrap_or(false) || view.lock_info.is_some() {
                            panic!("Operation denied");
                        }
                        return;
                    }
                    "groupInfo" => {
                        if !permission.field_groupable.unwrap_or(false) || view.lock_info.is_some() {
                            panic!("Operation denied");
                        }
                        return;
                    }
                    "sortInfo" => {
                        if !permission.column_sortable.unwrap_or(false) || view.lock_info.is_some() {
                            panic!("Operation denied");
                        }
                        return;
                    }
                    "rowHeightLevel" => {
                        if !permission.row_high_editable.unwrap_or(false) || view.lock_info.is_some() {
                            panic!("Operation denied");
                        }
                        return;
                    }
                    "rows" => {
                        if li != &Value::Null {
                            if !permission.row_creatable.unwrap_or(false) {
                                panic!("Operation denied");
                            }
                            return;
                        }
                        if ld != &Value::Null {
                            if !permission.row_removable.unwrap_or(false) {
                                panic!("Operation denied");
                            }
                            return;
                        }
                        if has_lm {
                            if !permission.row_sortable.unwrap_or(false) {
                                panic!("Operation denied");
                            }
                            return;
                        }
                    }
                    "columns" => {
                        if li != &Value::Null && ld != &Value::Null {
                            let li_hidden = li.as_object().unwrap().get("hidden").unwrap().as_bool().unwrap();
                            let ld_hidden = ld.as_object().unwrap().get("hidden").unwrap().as_bool().unwrap();
                            // && action.li.unwrap().hidden != action.ld.unwrap().hidden
                            if li_hidden != ld_hidden {
                                if !permission.column_hideable.unwrap_or(false) || view.lock_info.is_some() {
                                    panic!("Operation denied");
                                }
                                return;
                            }
                        }
                        if has_lm {
                            if !permission.field_sortable.unwrap_or(false) || view.lock_info.is_some() {
                                panic!("Operation denied");
                            }
                            return;
                        }
                        // ====== FieldSO creation impact on view settings ======
                        if li != &Value::Null && ld != &Value::Null {
                            if !permission.field_creatable.unwrap_or(false) {
                                panic!("Operation denied");
                            }
                            return;
                        }
                        // ====== FieldSO deletion impact on view settings ======
                        if li == &Value::Null && ld != &Value::Null {
                            if !permission.field_removable.unwrap_or(false) {
                                panic!("Operation denied");
                            }
                            return;
                        }
                        if action.op.p.len() < 6 {
                            return;
                        }
                        // ====== FieldSO width ======
                        if action.op.p[5].as_str() == "width" {
                            if !permission.column_width_editable.unwrap_or(false) || view.lock_info.is_some() {
                                panic!("Operation denied");
                            }
                            return;
                        }
                        // ====== FieldSO statistics bar ======
                        if action.op.p[5].as_str() == "statType" {
                            if !permission.column_count_editable.unwrap_or(false) || view.lock_info.is_some() {
                                panic!("Operation denied");
                            }
                            return;
                        }
                    }
                    "style" => {
                        if action.op.p.len() < 5 {
                            return;
                        }
                        if view.lock_info.is_some() {
                            panic!("Operation denied");
                        }
                        // ====== View layout ======
                        if action.op.p[4].as_str() == "layoutType" || action.op.p[4].as_str() == "isAutoLayout" || action.op.p[4].as_str() == "cardCount" {
                            if !permission.view_layout_editable.unwrap_or(false) {
                                panic!("Operation denied");
                            }
                            return;
                        }
                        // ====== View style ======
                        if action.op.p[4].as_str() == "isCoverFit" || action.op.p[4].as_str() == "coverFieldId" || action.op.p[4].as_str() == "isColNameVisible" {
                            if !permission.view_style_editable.unwrap_or(false) {
                                panic!("Operation denied");
                            }
                            return;
                        }
                        // ====== View key field (kanban grouping field, gantt start & end datetime field) ======
                        if action.op.p[4].as_str() == "kanbanFieldId" || action.op.p[4].as_str() == "startFieldId" || action.op.p[4].as_str() == "endFieldId" {
                            if !permission.view_key_field_editable.unwrap_or(false) {
                                panic!("Operation denied");
                            }
                            return;
                        }
                        // ====== View color option ======
                        if action.op.p[4].as_str() == "colorOption" {
                            if !permission.view_color_option_editable.unwrap_or(false) {
                                panic!("Operation denied");
                            }
                            return;
                        }
                    }
                    "lock" => {
                        // ====== View lock operation ======
                        let view_index = action.op.p[2].clone();
                        let view_index = match view_index {
                            PathSegment::Number(n) => n,
                            _ => panic!("collect_by_view Operation abnormal idx not number"),
                        };
                        let oi = match &action.op.kind {
                            OperationKind::ObjectInsert{oi} => oi,
                            OperationKind::ObjectReplace{od:_,oi} => oi,
                            _ => &Value::Null,
                        };
                        if oi != &Value::Null {
                            result_set.temporary_views.insert(view_index, ViewSO {
                                lock_info: Some(from_value(oi.clone()).unwrap()),
                                ..result_set.temporary_views.get(view_index).unwrap().clone()
                            });
                            return;
                        }
                        let has_od = match &action.op.kind {
                            OperationKind::ObjectDelete{od:_} => true,
                            OperationKind::ObjectReplace{od:_,oi:_} => true,
                            _ => false,
                        };
                        if has_od {
                            result_set.temporary_views.get_mut(view_index).unwrap().lock_info = None;
                            return;
                        }
                    }
                    _ => {}
                }
            }
            if !permission.editable.unwrap_or(false) {
                panic!("Operation denied");
            } else {
                // Operations on linked datasheet, requires editable permission
                if permission.editable.unwrap_or(false) || (li != &Value::Null && permission.row_creatable.unwrap_or(false)) {
                    return;
                }
                // If no, maybe this datasheet delete linking fields two-way or undo this operation, causing
                // columns change in view.
                if action.op.p[3].as_str() != "columns" {
                    panic!("Operation denied");
                }
                // Take a note to check outside later if fields exist corresponding to deleted or recovered deleted fields.
                if li != &Value::Null {
                    let field_id = li.as_object().unwrap().get("fieldId").unwrap().as_str().unwrap().to_string();
                    result_set.fld_op_in_view_map.insert(field_id, true);
                } else if ld != &Value::Null {
                    let field_id = ld.as_object().unwrap().get("fieldId").unwrap().as_str().unwrap().to_string();
                    result_set.fld_op_in_view_map.insert(field_id, false);
                } else {
                    panic!("Operation denied");
                }
            }
        }
    }

    fn collect_by_delete_widget_or_widget_panels(&self, action: &ActionOTO, result_set: &mut ResultSet) {
        let ld = match &action.op.kind {
            OperationKind::ListDelete{ld} => ld,
            OperationKind::ListReplace{ld,li:_} => ld,
            _ => &Value::Null,
        };
        if action.op.p.contains(&PathSegment::String("widgets".to_string())) {
            // Delete widget in widget panel
            let id = ld.as_object().unwrap().get("id").unwrap().as_str().unwrap().to_string();
            result_set.delete_widget_ids.push(id);
            return;
        }
        // Delete whole widget panel
        let widgets = ld.as_object().unwrap().get("widgets").unwrap();
        let widgets: Vec<WidgetPanelSO> = from_value(widgets.clone()).unwrap();
        let ids: Vec<String> = widgets.iter().map(|item| item.id.clone()).collect();
        result_set.delete_widget_ids.extend(ids);
    }

    fn deal_with_meta(&self, cmd: &str, action: &ActionOTO, permission: &NodePermissionSO, result_set: &mut ResultSet) {
        if action.op.p[1] == PathSegment::String("fieldMap".to_string()) {
            // FieldSO operation, check permission above manageable.
            // New field(including copy field)
            let oi = match &action.op.kind {
                OperationKind::ObjectInsert{oi} => oi,
                OperationKind::ObjectReplace{od:_,oi} => oi,
                _ => &Value::Null,
            };
            let od = match &action.op.kind {
                OperationKind::ObjectDelete{od} => od,
                OperationKind::ObjectReplace{od,oi:_} => od,
                _ => &Value::Null,
            };
            let has_oi = oi != &Value::Null;
            let has_od = od != &Value::Null;
            if has_oi && !has_od {
                // Create field or copy field
                self.collect_by_add_field(cmd, action, permission, result_set);
                return;
            }
            // Modify field
            if has_oi && has_od {
                self.collect_by_change_field(cmd, action, permission, result_set);
                return;
            }
            // Delete field
            if !has_oi && has_od {
                self.collect_by_delete_field(action, permission, result_set);
                return;
            }
        } else if action.op.p[1] == PathSegment::String("views".to_string()) {
            // View operations
            self.collect_by_view(action, permission, result_set);
        } else if action.op.p[1] == PathSegment::String("widgetPanels".to_string()) {
            // Operations on widget panel
            // Creation/deletion of widgets related to widget panel requires manageable permission
            if !permission.manageable.unwrap_or(false) {
                panic!("Operation denied");
            }
            let has_ld = match &action.op.kind {
                OperationKind::ListDelete{ld:_} => true,
                OperationKind::ListReplace{ld:_,li:_} => true,
                _ => false,
            };
            if has_ld {
                self.collect_by_delete_widget_or_widget_panels(action, result_set);
            }
        }
    }

    fn collect_by_operate_for_row(&self, cmd: &str, action: &ActionOTO, permission: &NodePermissionSO, result_set: &mut ResultSet) {
        let record_id = action.op.p[1].as_str();
        let auto_subscription_fields = self.get_auto_subscription_fields(&result_set.temporary_field_map);
    
        let oi = match &action.op.kind {
            OperationKind::ObjectInsert{oi} => oi,
            OperationKind::ObjectReplace{od:_,oi} => oi,
            _ => &Value::Null,
        };
        if oi != &Value::Null && cmd == "UnarchiveRecords" {
            if !permission.row_unarchivable.unwrap_or(false) {
                panic!("Operation denied");
            }
            let oi_data = oi.as_object().unwrap();
            let record_data = match oi_data.get("data") {
                Some(data) => data.clone(),
                None => oi.clone(),
            };
            // record_data.retain(|_, v| v.is_some());
            let record_data = record_data.as_object().unwrap();
            for (field_id, _) in record_data {
                self.check_cell_val_permission(cmd, field_id, permission, result_set);
            }
            if let Some(index) = result_set.to_archive_record_ids.iter().position(|x| x == record_id) {
                result_set.to_archive_record_ids.remove(index);
                return;
            }
            result_set.to_unarchive_record.insert(record_id.to_string(), to_value(record_data).unwrap());
        } else if oi != &Value::Null {
            if !permission.row_creatable.unwrap_or(false) {
                panic!("Operation denied");
            }
            let oi_data = oi.as_object().unwrap();
            let record_data = match oi_data.get("data") {
                Some(data) => data.clone(),
                None => oi.clone(),
            };
            // record_data.retain(|_, v| v.is_some());
            let record_data = record_data.as_object().unwrap();
            for (field_id, _) in record_data {
                self.check_cell_val_permission(cmd, field_id, permission, result_set);
            }
            if let Some(index) = result_set.to_delete_record_ids.iter().position(|x| x == record_id) {
                result_set.to_delete_record_ids.remove(index);
                return;
            }
            let record_data = to_value(record_data).unwrap();
            result_set.to_create_record.insert(record_id.to_string(), record_data.clone());
            let record_data = from_value(record_data).unwrap();
            let od_data = HashMap::new();
            self.collect_record_subscriptions(&auto_subscription_fields, record_id, &record_data, &od_data, result_set);
        }
    
        let od = match &action.op.kind {
            OperationKind::ObjectDelete{od} => od,
            OperationKind::ObjectReplace{od,oi:_} => od,
            _ => &Value::Null,
        };
        if od != &Value::Null && cmd == "ArchiveRecords" {
            if !permission.row_archivable.unwrap_or(false) {
                panic!("Operation denied");
            }
            result_set.clean_record_cell_map.remove(record_id);
            result_set.replace_cell_map.remove(record_id);
            result_set.to_archive_record_ids.push(record_id.to_string());
        } else if od != &Value::Null {
            if !permission.row_removable.unwrap_or(false) {
                panic!("Operation denied");
            }
            result_set.clean_record_cell_map.remove(record_id);
            result_set.replace_cell_map.remove(record_id);
            if result_set.to_create_record.contains_key(record_id) {
                result_set.to_create_record.remove(record_id);
                return;
            }
            result_set.to_delete_record_ids.push(record_id.to_string());
            let oi_data = HashMap::new();
            let od_data = from_value(od.clone()).unwrap();
            self.collect_record_subscriptions(&auto_subscription_fields, record_id, &oi_data, &od_data, result_set);
        }
    }

    fn collect_by_operate_for_cell_value(&self, cmd: &str, action: &ActionOTO, permission: &NodePermissionSO, result_set: &mut ResultSet) {
        let record_id = &action.op.p[1].to_string();
        let field_id = &action.op.p[3].to_string();
        // Validate permission
        self.check_cell_val_permission(cmd, field_id, permission, result_set);
        let auto_subscription_fields = self.get_auto_subscription_fields(&result_set.temporary_field_map);
        let oi = match &action.op.kind {
            OperationKind::ObjectInsert{oi} => oi,
            OperationKind::ObjectReplace{od:_,oi} => oi,
            _ => &Value::Null,
        };
        let od = match &action.op.kind {
            OperationKind::ObjectDelete{od} => od,
            OperationKind::ObjectReplace{od,oi:_} => od,
            _ => &Value::Null,
        };
        if oi != &Value::Null {
            // oi exists means writing data
            let data = oi;
            if let Some(field_data) = result_set.clean_record_cell_map.get_mut(record_id) {
                if field_data.iter().any(|cur| &cur.field_id == field_id) {
                    // writing cell data, ignoring former cell clearing operations.
                    field_data.retain(|cur| &cur.field_id != field_id);
                }
            }
            self.collect_record_subscriptions(&auto_subscription_fields, record_id, &[(field_id.clone(), data.clone())].iter().cloned().collect(), &[(field_id.clone(), od.clone())].iter().cloned().collect(), result_set);
            if let Some(add_record_data) = result_set.to_create_record.get_mut(record_id) {
                // Create record and change this record, record change can be merged into record creation
                add_record_data.as_object_mut().unwrap().insert(field_id.clone(), data.clone());
                return;
            }
            DatasheetOtService::set_map_val_if_exist(&mut result_set.replace_cell_map, record_id, FieldData{field_id: field_id.clone(), data: data.clone()});
        } else if od != &Value::Null {
            if let Some(field_data) = result_set.replace_cell_map.get_mut(record_id) {
                if field_data.iter().any(|cur| &cur.field_id == field_id) {
                    // If cell is cleared after cell change, just clear the cell
                    field_data.retain(|cur| &cur.field_id != field_id);
                }
            }
            self.collect_record_subscriptions(&auto_subscription_fields, record_id, &[(field_id.clone(), oi.clone())].iter().cloned().collect(), &[(field_id.clone(), od.clone())].iter().cloned().collect(), result_set);
            if let Some(add_record_data) = result_set.to_create_record.get_mut(record_id) {
                let add_record_data = add_record_data.as_object_mut().unwrap();
                if add_record_data.contains_key(field_id) {
                    // Check new record with default values, if cell is cleared, just clear the default value
                    add_record_data.remove(field_id);
                    return;
                }
            }
            // Only od exists, delete cell data
            DatasheetOtService::set_map_val_if_exist(&mut result_set.clean_record_cell_map, record_id, FieldData{field_id: field_id.clone(), data: Value::Null});
        }
    }

    fn collect_record_meta_operations(&self, cmd: &str, action: &ActionOTO, permission: &NodePermissionSO, result_set: &mut ResultSet) {
        if action.op.p[0].as_str() != "recordMap" || action.op.p[2].as_str() != "recordMeta" {
            return;
        }
    
        // Collect record alarms (['recordMap', ':recordId', 'recordMeta', 'fieldExtraMap', ':fieldId', 'alarm')
        if action.op.p[3].as_str() == "fieldExtraMap" && action.op.p[5].as_str() == "alarm" {
            self.collect_record_alarm_operations(cmd, action, permission, result_set);
        }
    }

    fn collect_record_alarm_operations(&self, cmd: &str, action: &ActionOTO, permission: &NodePermissionSO, result_set: &mut ResultSet) {
        let record_id = action.op.p[1].as_str();
        let field_id = action.op.p[4].as_str();
        self.check_cell_val_permission(cmd, field_id, permission, result_set);
    
        let oi = match &action.op.kind {
            OperationKind::ObjectInsert{oi} => oi,
            OperationKind::ObjectReplace{od:_,oi} => oi,
            _ => &Value::Null,
        };
        let od = match &action.op.kind {
            OperationKind::ObjectDelete{od} => od,
            OperationKind::ObjectReplace{od,oi:_} => od,
            _ => &Value::Null,
        };
        if oi != &Value::Null {
            let payload: RecordAlarm = from_value(oi.clone()).unwrap();
            let exist_alarms = result_set.to_create_alarms.entry(record_id.to_string()).or_insert_with(Vec::new);
            exist_alarms.retain(|alarm: &RecordAlarm| alarm.id != payload.id);
            exist_alarms.push(RecordAlarm { record_id: Some(record_id.to_string()), field_id: Some(field_id.to_string()), ..payload });
        }
    
        if od != &Value::Null {
            let payload: RecordAlarm = from_value(od.clone()).unwrap();
            let exist_alarms = result_set.to_delete_alarms.entry(record_id.to_string()).or_insert_with(Vec::new);
            exist_alarms.retain(|alarm: &RecordAlarm| alarm.id != payload.id);
            exist_alarms.push(RecordAlarm { record_id: Some(record_id.to_string()), field_id: Some(field_id.to_string()), ..payload });
        }
    }

    async fn collect_by_operate_for_comment(&self, action: &ActionOTO, permission: &NodePermissionSO, result_set: &mut ResultSet) -> anyhow::Result<()> {
        if !permission.readable.unwrap_or(false) {
            panic!("Operation denied");
        }
        let record_id = action.op.p[1].as_str();

        let li = match &action.op.kind {
            OperationKind::ListInsert{li} => li,
            OperationKind::ListReplace{ld:_,li} => li,
            _ => &Value::Null,
        };
        let ld = match &action.op.kind {
            OperationKind::ListDelete{ld} => ld,
            OperationKind::ListReplace{ld,li:_} => ld,
            _ => &Value::Null,
        };
        if !action.op.p.contains(&PathSegment::String("emojis".to_string())) {
            // Delete comment
            let od = match &action.op.kind {
                OperationKind::ObjectDelete{od} => od,
                OperationKind::ObjectReplace{od,oi:_} => od,
                _ => &Value::Null,
            };
            if ld != &Value::Null || od != &Value::Null {
                let comment = if ld != &Value::Null {
                    ld.clone()
                } else {
                    od.as_array().unwrap()[0].clone()
                };
                let comment = from_value::<Comments>(comment.clone()).unwrap();
                println!("check_delete_permission");
                // let can_delete_comment = self.loader.check_delete_permission(&result_set.auth, &comment.unit_id, &permission.uuid).await?;
                // if !can_delete_comment {
                //     panic!("Operation denied");
                // }
                DatasheetOtService::set_map_val_if_exist(&mut result_set.to_delete_comment_ids, &record_id.to_string(), comment.comment_id.clone());
            }
            // Create comment
            let oi = match &action.op.kind {
                OperationKind::ObjectInsert{oi} => oi,
                OperationKind::ObjectReplace{od:_,oi} => oi,
                _ => &Value::Null,
            };
            if (li != &Value::Null || oi != &Value::Null) && action.op.p.contains(&PathSegment::String("comments".to_string())) {
                let comment = if li != &Value::Null {
                    li.clone()
                } else {
                    od.clone()
                };
                let comment = from_value::<Comments>(comment.clone()).unwrap();
                DatasheetOtService::set_map_val_if_exist(&mut result_set.to_correct_comment, &record_id.to_string(), CorrectComment { index: action.op.p.last().unwrap().as_i32(), comment: comment.clone() });
            }
        } else {
            // Add emoji to comment
            if li != &Value::Null || ld != &Value::Null {
                let has_li = li != &Value::Null;
                let comment = if has_li {
                    li.clone()
                } else {
                    ld.clone()
                };
                let comment = from_value::<Comments>(comment.clone()).unwrap();
                DatasheetOtService::set_map_val_if_exist(&mut result_set.to_update_comment_emoji, &record_id.to_string(), CommentEmoji { emoji_action: has_li, comment: comment.clone() });
            }
        }
        Ok(())
    }

    async fn deal_with_record_map(&self, cmd: &str, action: &ActionOTO, permission: &NodePermissionSO, result_set: &mut ResultSet) -> anyhow::Result<()> {
        // ===== Record operation  BEGIN =====
        if !action.op.p.contains(&PathSegment::String("commentCount".to_string())) && !action.op.p.contains(&PathSegment::String("comments".to_string())) && action.op.p[0].as_str() == "recordMap" {
            // Cell data operation
            if action.op.p.contains(&PathSegment::String("data".to_string())) {
                self.collect_by_operate_for_cell_value(cmd, action, permission, result_set);
                return Ok(());
            }
    
            // RecordMeta -> fieldExtraMap operation (alarm)
            if action.op.p.contains(&PathSegment::String("recordMeta".to_string())) && action.op.p.contains(&PathSegment::String("fieldExtraMap".to_string())) {
                self.collect_record_meta_operations(cmd, action, permission, result_set);
                return Ok(());
            }
    
            // Record data operation
            self.collect_by_operate_for_row(cmd, action, permission, result_set);
        }
        // ===== Record operation END =====
    
        // ===== Comment collection operation BEGIN ====
        if action.op_name != "OI".to_string() && action.op.p.contains(&PathSegment::String("comments".to_string())) && action.op.p[0].as_str() == "recordMap" {
            self.collect_by_operate_for_comment(action, permission, result_set).await?
        }
        // ===== Comment collection operation END ====
    
        Ok(())
    }

    pub async fn transaction(&self, 
        // manager: EntityManager, 
        effect_map: &mut HashMap<String, Value>, common_data: &CommonData, result_set: &ResultSet
    ) -> anyhow::Result<()> {
        let begin_time = Instant::now();
        println!("[{}] ====> transaction start......", common_data.dst_id);
    
        // self.handle_update_comment(manager.clone(), common_data.clone(), effect_map.clone(), result_set.clone()).await?;
        self.handle_batch_delete_record(common_data.clone(), result_set.clone()).await?;
        // self.handle_batch_archive_record(manager.clone(), common_data.clone(), result_set.clone()).await?;
        // self.handle_batch_delete_widget(manager.clone(), common_data.clone(), result_set.clone()).await?;
        // self.handle_batch_add_widget(manager.clone(), common_data.clone(), result_set.clone()).await?;
        self.handle_batch_update_cell(common_data.clone(), effect_map, true, result_set.clone()).await?;
        self.handle_batch_create_record(common_data.clone(), effect_map, result_set.clone()).await?;
        // self.handle_batch_unarchive_record(manager.clone(), common_data.clone(), result_set.clone()).await?;
        self.handle_batch_update_cell(common_data.clone(), effect_map, false, result_set.clone()).await?;
        // self.delete_record_comment(manager.clone(), common_data.clone(), result_set.clone()).await?;
        // self.handle_batch_init_field(manager.clone(), common_data.clone(), effect_map.clone(), result_set.clone()).await?;
        // self.handle_comment_emoji(manager.clone(), common_data.clone(), result_set.clone()).await?;
        // self.record_alarm_service.handle_record_alarms(manager.clone(), common_data.clone(), result_set.clone()).await?;
    
        // let handle_meta = self.handle_meta(manager.clone(), common_data.clone(), effect_map.clone());
        self.handle_meta(common_data.clone(), effect_map).await?;
        self.create_new_changeset(common_data, effect_map.get(EffectConstantName::RemoteChangeset.to_string().as_str()).unwrap().clone()).await?;
        self.update_revision(common_data).await?;
    
        // join!(handle_meta, create_new_changeset, update_revision)?;
    
        // if result_set.to_delete_field_ids.len() > 0 {
        //     self.rest_service.del_field_permission(result_set.auth, common_data.dst_id, result_set.to_delete_field_ids).await?;
        // }
    
        let end_time = Instant::now();
        println!("[{}] ====> transaction finished......duration: {}ms", common_data.dst_id, end_time - begin_time);
    
        Ok(())
    }

    fn check_cell_val_permission(&self, cmd: &str, field_id: &str, permission: &NodePermissionSO, result_set: &mut ResultSet) {
        // When field permission set exists and contains current field, checking editable permission of the field only,
        // node permission does not matter
        let mut has_permission = permission.cell_editable.unwrap_or(false);
        if permission.field_permission_map.is_some() {
            if let Some(field_permission) = permission.field_permission_map.clone().unwrap().get(field_id) {
                if result_set.source_type == Some(SourceTypeEnum::FORM) {
                    has_permission = field_permission.setting.form_sheet_accessible;
                } else {
                    has_permission = field_permission.permission.editable;
                }
            }
        }
    
        // Validate node permission or field permission
        if !has_permission {
            // No enough permission, reject this operation
            if Some(result_set.datasheet_id.clone()) == result_set.link_action_main_dst_id {
                // return Err(Box::new(ServerException::OperationDenied));
                panic!("Operation denied");
            } else {
                // Operation on linked datasheet, may be related change of linked datasheet,
                // permission will be validated outside, record it here
                result_set.fld_op_in_rec_map.insert(field_id.to_string(), cmd.to_string());
            }
        }
    }

    fn handle_attach_op_cite(&self, action: &ActionOTO, result_set: &mut ResultSet) {
        // cell operation
        let result =  if action.op.p.contains(&PathSegment::String("data".to_string())) {
            self.handle_attach_for_cell_value(action, result_set.space_capacity_over_limit)
        } else {
            // row data operation
            self.handle_attach_for_row(action, result_set.space_capacity_over_limit)
        };
        // add_token and remove_token are guaranteed empty vectors
        result_set.attach_cite_collector.add_token.extend(result.add_token);
        result_set.attach_cite_collector.remove_token.extend(result.remove_token);
    }

    fn handle_attach_for_cell_value(&self, action: &ActionOTO, capacity_over_limit: bool) -> OpAttach {
        let mut add_token: Vec<TokenInfo> = Vec::new();
        let mut remove_token: Vec<TokenInfo> = Vec::new();
        let oi = match &action.op.kind {
            OperationKind::ObjectInsert{oi} => oi,
            OperationKind::ObjectReplace{od:_,oi} => oi,
            _ => &Value::Null,
        };
        let od = match &action.op.kind {
            OperationKind::ObjectDelete{od} => od,
            OperationKind::ObjectReplace{od,oi:_} => od,
            _ => &Value::Null,
        };
        if oi != &Value::Null && od == &Value::Null {
            if DatasheetOtService::is_attach_field(oi) {
                assert!(!capacity_over_limit, "Space capacity over limit");
                add_token = oi.as_array().unwrap().iter().map(|item| {
                    let item = item.as_object().unwrap();
                    let token = item.get("token").unwrap().as_str().unwrap().to_string();
                    let name = item.get("name").unwrap().as_str().unwrap().to_string();
                    TokenInfo { token, name }
                }).collect();
            }
        }
        // Only deletion, no need to check, only collecting data
        if oi == &Value::Null && od != &Value::Null {
            if DatasheetOtService::is_attach_field(od) {
                remove_token = od.as_array().unwrap().iter().map(|item| {
                    let item = item.as_object().unwrap();
                    let token = item.get("token").unwrap().as_str().unwrap().to_string();
                    let name = item.get("name").unwrap().as_str().unwrap().to_string();
                    TokenInfo { token, name }
                }).collect();
            }
        }
        // Both deletion and insertion, need to compare deleted data and inserted data to check if it is partial deletion
        if oi != &Value::Null && od != &Value::Null {
            // Delete attachment partially
            if DatasheetOtService::is_attach_field(oi) && DatasheetOtService::is_attach_field(od) {
                let od_id: Vec<String> = od.as_array().unwrap().iter().map(|item| { 
                    let item = item.as_object().unwrap();
                    let token = item.get("token").unwrap().as_str().unwrap().to_string();
                    let name = item.get("name").unwrap().as_str().unwrap().to_string();
                    let id = item.get("id").unwrap().as_str().unwrap().to_string();
                    remove_token.push(TokenInfo { token, name }); 
                    id.clone()
                }).collect();
                let oi_id: Vec<String> = oi.as_array().unwrap().iter().map(|item| { 
                    let item = item.as_object().unwrap();
                    let token = item.get("token").unwrap().as_str().unwrap().to_string();
                    let name = item.get("name").unwrap().as_str().unwrap().to_string();
                    let id = item.get("id").unwrap().as_str().unwrap().to_string();
                    add_token.push(TokenInfo { token, name }); 
                    id.clone()
                }).collect();
                // Check if the length of intersection is the same as oi's, if so, it is partial deletion and no need to check,
                // otherwise it may be covering pasting and needs checking.
                assert!(!capacity_over_limit && intersection(&od_id, &oi_id).len() != oi_id.len(), "Space capacity over limit");
            }
        }
        OpAttach { add_token, remove_token }
    }

    fn handle_attach_for_row(&self, action: &ActionOTO, over_limit: bool) -> OpAttach {
        let mut add_token: Vec<TokenInfo> = Vec::new();
        let mut remove_token: Vec<TokenInfo> = Vec::new();
        
        // Record action can only be one of oi or od.
        let oi = match &action.op.kind {
            OperationKind::ObjectInsert{oi} => oi,
            OperationKind::ObjectReplace{od:_,oi} => oi,
            _ => &Value::Null,
        };
        if oi != &Value::Null {
            let record_data = if oi.is_object() && oi.as_object().unwrap().get("data").is_some() {
                oi.as_object().unwrap().get("data").unwrap()
            } else {
                oi
            };
            let record_data = record_data.as_object().unwrap();
            for (_field_id, value) in record_data {
                if value != &Value::Null && DatasheetOtService::is_attach_field(value) {
                    assert!(!over_limit, "Space capacity over limit");
                    let _ = value.as_array().unwrap().iter().map(|item| {
                        let item = item.as_object().unwrap();
                        let token = item.get("token").unwrap().as_str().unwrap().to_string();
                        let name = item.get("name").unwrap().as_str().unwrap().to_string();
                        add_token.push(TokenInfo { token, name });
                    });
                }
            }
        }
        // Only deletion, no need to check, only collect data
        let od = match &action.op.kind {
            OperationKind::ObjectDelete{od} => od,
            OperationKind::ObjectReplace{od,oi:_} => od,
            _ => &Value::Null,
        };
        if od != &Value::Null {
            let record_data = if od.is_object() && od.as_object().unwrap().get("data").is_some() {
                od.as_object().unwrap().get("data").unwrap()
            } else {
                od
            };
            let record_data = record_data.as_object().unwrap();
            for (_field_id, value) in record_data {
                if value != &Value::Null && DatasheetOtService::is_attach_field(value) {
                    assert!(!over_limit, "Space capacity over limit");
                    let _ = value.as_array().unwrap().iter().map(|item| {
                        let item = item.as_object().unwrap();
                        let token = item.get("token").unwrap().as_str().unwrap().to_string();
                        let name = item.get("name").unwrap().as_str().unwrap().to_string();
                        remove_token.push(TokenInfo { token, name });
                    });
                }
            }
        }
        OpAttach { add_token, remove_token }
    }

    async fn handle_batch_delete_record(&self, common_data: CommonData, result_set: ResultSet) -> anyhow::Result<()> {
        let record_ids = result_set.to_delete_record_ids.clone();
        println!("resultSet.to_delete_record_ids.len() = {}", record_ids.len());
        if record_ids.is_empty() {
            return Ok(());
        }
    
        let CommonData { user_id, dst_id, revision , ..} = common_data.clone();
        let user_id = user_id.unwrap();
        println!("[{}] Soft delete record", dst_id);
        let begin_time = Instant::now();
        println!("[{}] ====> Start batch deleting record......", dst_id);
    
        let all_field_can_edit = common_data.permission.field_permission_map
            .map_or(true, |map| !map.values().any(|val| !val.permission.editable));
    
        let is_delete_data = if all_field_can_edit {
            true
        } else {
            false
        };
    
        let gap = 1000;
        if record_ids.len() > gap {
            let chunks = record_ids.chunks(gap);
            for chunk in chunks {
                let record_ids_tmp = chunk.iter().map(|id| id.clone()).collect::<Vec<_>>();
                self.loader.update_record_delete(&dst_id, &record_ids_tmp, is_delete_data, &revision, &user_id).await;
                self.loader.update_record_archive_delete(&dst_id, &record_ids_tmp, &user_id).await;
            }
        } else {
            self.loader.update_record_delete(&dst_id, &record_ids, is_delete_data, &revision, &user_id).await;
            self.loader.update_record_archive_delete(&dst_id, &record_ids, &user_id).await;
        }
    
        let end_time = Instant::now();
        println!("[{}] ====> Finished batch deleting record......duration: {}ms", dst_id, end_time-begin_time);
    
        Ok(())
    }

    async fn handle_batch_update_cell(
        &self,
        // manager: EntityManager,
        common_data: CommonData,
        effect_map: &mut HashMap<String, Value>,
        is_delete: bool,
        result_set: ResultSet,
    ) -> anyhow::Result<()> {
        let record_field_map = if is_delete { result_set.clean_record_cell_map.clone() } else { result_set.replace_cell_map.clone() };
        if record_field_map.is_empty() {
            return Ok(());
        }
        let user_id = common_data.user_id.clone().unwrap();
        let uuid = common_data.uuid.clone();
        let dst_id = common_data.dst_id.clone();
        let revision = common_data.revision.clone();
        let prev_record_map = self.loader.get_basic_records_by_record_ids(&dst_id, record_field_map.keys().cloned().collect(), false).await?;
        let record_meta_map = effect_map.get(EffectConstantName::RecordMetaMap.to_string().as_str()).unwrap().clone();
        let record_meta_map: HashMap<String, RecordMeta> = match from_value(record_meta_map){
            Ok(map) => map,
            Err(_) => HashMap::new(),
        };
        let mut record_map_actions: Vec<ActionOTO> = vec![];
        let updated_at = Utc::now().timestamp_millis() as u64;
        let begin_time = Utc::now().timestamp_millis();
        println!("[{}] ====> Start batch updating cell data......", dst_id);
    
        // Batch update cell data
        for (record_id, cell_data) in record_field_map {
            if cell_data.is_empty() {
                continue;
            }
            // Record in current database
            let old_record = prev_record_map.get(&record_id).unwrap().clone();
            // Take latest recordMeta from the current op with priority, or take recordMeta in database
            let old_record_meta = record_meta_map.get(&record_id).unwrap_or(&old_record.record_meta.clone().unwrap_or(RecordMeta::default())).clone();
            let mut field_updated_map = old_record_meta.field_updated_map.clone().unwrap_or_default();
            // Field IDs that need clean-up
            let to_clean_field_ids = result_set.clean_field_map.keys().filter(|field_id| {
                result_set.clean_field_map.get(field_id.clone()) != Some(&FieldKindSO::LastModifiedBy)
            }).cloned().collect::<Vec<_>>();
    
            for field_data in &cell_data {
                let field_id = field_data.field_id.clone();
    
                // The update is cell value deletion and the cell's fieldId is within deleted fields, clean up recordMeta.fieldUpdatedMap
                if is_delete && to_clean_field_ids.contains(&field_id) {
                    field_updated_map.remove(&field_id);
                    continue;
                }
                let prev_field_updated_info = field_updated_map.get(&field_id).unwrap_or(&FieldUpdatedValue::default()).clone();
                // Update 'at' and 'by' in fieldUpdatedMap corresponding to fieldId
                field_updated_map.insert(field_id.clone(), FieldUpdatedValue { at: Some(updated_at), by: uuid.clone(), ..prev_field_updated_info });
            }
            // Construct latest recordMeta
            let mut new_record_meta = old_record_meta.clone();
            if field_updated_map.is_empty() {
                new_record_meta.field_updated_map = None;
            } else {
                new_record_meta.field_updated_map = Some(field_updated_map.clone());
            }

            if is_delete {
                let json_params = cell_data.iter().map(|data| {
                    format!("$.{}", data.field_id)
                }).collect::<String>();
                // println!("json_params1 = {}", json_params); //未验证
                let record_meta = to_string(&new_record_meta).unwrap();
                self.loader.update_record_remove(&dst_id, &record_id, json_params, record_meta, &revision, &user_id).await;
            } else {
                let mut json_map: HashMap<&str, Value> = HashMap::new();
                for data in &cell_data {
                    json_map.insert(data.field_id.as_str(), data.data.clone());
                }
                let record_meta = to_string(&new_record_meta).unwrap();
                self.loader.update_record_replace(&dst_id, &record_id, json_map, record_meta, &revision, &user_id).await;
            }

            // Actions<setRecord> in middle layer that are synced to client
            // If currently neither op nor database has record.recordMeta, create one
            let record_action;
            if !record_meta_map.contains_key(&record_id) && old_record.record_meta.is_none() {
                record_action = DatasheetOtService::generate_jot_action("OI", vec!["recordMap", &record_id, "recordMeta"], to_value(new_record_meta.clone()).unwrap(), None);
                record_map_actions.push(record_action);
            } else if !record_meta_map.contains_key(&record_id) && old_record.record_meta.is_some() && old_record.record_meta.unwrap().field_updated_map.is_none() {
                record_action = DatasheetOtService::generate_jot_action(
                    "OI",
                    vec!["recordMap", &record_id, "recordMeta", "fieldUpdatedMap"],
                    to_value(new_record_meta.field_updated_map.clone().unwrap()).unwrap(),
                    None
                );
                record_map_actions.push(record_action);
            } else {
                record_action = DatasheetOtService::generate_jot_action(
                    "OR",
                    vec!["recordMap", &record_id, "recordMeta", "fieldUpdatedMap"],
                    to_value(new_record_meta.field_updated_map.clone().unwrap()).unwrap(),
                    Some(to_value(old_record_meta.field_updated_map.clone().unwrap()).unwrap()),
                );
                record_map_actions.push(record_action);
            }
        }

        // As all changes in middle layer need to be synced to client, here effect change is performed, including meta and remoteChangeset
        let meta = self.get_meta_data_by_cache(&dst_id, effect_map).await?;
        let meta = from_value::<DatasheetMetaSO>(meta).unwrap();
        let field_list: Vec<FieldSO> = meta.field_map.values().cloned().collect();
        // Obtains all LastModifiedBy fields according to meta
        let last_modified_fields = field_list.into_iter().filter(|field| field.kind == FieldKindSO::LastModifiedBy && !result_set.clean_field_map.contains_key(&field.id)).collect::<Vec<_>>();
        // Collect UUID into field.property.uuids, and return corresponding actions
        let meta_actions = self.get_meta_action_by_field_type(FieldTypeParams { uuid: uuid.clone(), fields: last_modified_fields, ..Default::default() }, effect_map).await;
        // Update remoteChangeset
        self.update_effect_remote_changeset(effect_map, Some(meta_actions), Some(record_map_actions));
        let end_time = Utc::now().timestamp_millis();
        println!("[{}] ====> Finished batch updating cell data......duration: {}ms", dst_id, end_time - begin_time);
        Ok(())
    }

    async fn handle_batch_create_record(
        &self,
        // manager: EntityManager,
        common_data: CommonData,
        effect_map: &mut HashMap<String, Value>,
        result_set: ResultSet,
    ) -> anyhow::Result<()> {
        println!("result_set.to_create_record.len = {:?}", result_set.to_create_record.len());
        if result_set.to_create_record.is_empty() {
            return Ok(());
        }
    
        let CommonData{ user_id, uuid, dst_id, revision, .. } = common_data.clone();
        let user_id = user_id.unwrap();
        let begin_time = Utc::now().timestamp_millis();
        println!("[{}] ====> Start batch creating records......", common_data.dst_id);
    
        let mut restore_record_map: HashMap<String, IRestoreRecordInfo> = HashMap::new();
        let record_ids: Vec<String> = result_set.to_create_record.keys().cloned().collect();
        let mut save_record_entities: Vec<(&String, HashMap<String, Value>, RecordMeta)> = Vec::new();
        let mut record_map_actions: Vec<ActionOTO> = Vec::new();
        let mut meta_actions: Vec<ActionOTO> = Vec::new();
      
        //   if (this.logger.isDebugEnabled()) {
        //     this.logger.debug(`[${dstId}] Batch query datasheet records [${recordIds.toString()}]`);
        //   }
      
        // Query if is deleted formerly, avoid redundant insertion
        let deleted_record_map = self.loader.get_basic_records_by_record_ids(&dst_id, record_ids.clone(), true).await?;
        let delete_record_ids = deleted_record_map.keys().cloned().collect::<Vec<_>>();
        let meta = self.get_meta_data_by_cache(&dst_id, effect_map).await?;
        let meta: DatasheetMetaSO = from_value(meta)
            .map_err(|err| {
                format!(
                    "Failed to deserialize meta to DatasheetMetaSO: {}",
                    err.to_string()
                )
            }).unwrap();
        let field_map = meta.field_map.clone();
        let field_list = field_map.values().collect::<Vec<_>>();
        let auto_number_fields = field_list.clone().into_iter().filter(|field| field.kind == FieldKindSO::AutoNumber).collect::<Vec<_>>();
        let created_at = Utc::now().timestamp_millis() as u64;
        let updated_at = created_at;
        let mut record_meta_map = effect_map.get(EffectConstantName::RecordMetaMap.to_string().as_str()).unwrap().clone();
        let mut tmp_map = Map::new();
        let record_meta_map = record_meta_map.as_object_mut().unwrap_or(&mut tmp_map);
        let mut record_index = 0;
      
        //   if (this.logger.isDebugEnabled()) {
        //     this.logger.debug(`Deleted records [${deletedRecordIds}]`);
        //   }
        for (record_id, record_data) in result_set.to_create_record.iter() {
            // Recover record and replace data
            let mut record_data: HashMap<String, Value> = from_value(record_data.clone()).unwrap();
            if delete_record_ids.contains(record_id) {
                // Process CreatedAt/UpdatedAt/CreatedBy/UpdatedBy/AutoNumber data
                let prev_record_meta = deleted_record_map.get(record_id).unwrap().record_meta.clone();
                let mut cur_record_info = IRestoreRecordInfo { data: record_data.clone(), ..Default::default() };
                let mut field_updated_map = match &prev_record_meta {
                    Some(prev_record_meta) => prev_record_meta.field_updated_map.clone().unwrap_or_default(),
                    None => HashMap::new(),
                };
                let mut field_extra_map = match &prev_record_meta {
                    Some(prev_record_meta) => prev_record_meta.field_extra_map.clone().unwrap_or_default(),
                    None => HashMap::new(),
                };
      
                // TODO When cell alarm is changed to async created, remove this code block.
                // Write recordMeta.fieldExtraMap when creating cell alarm
                if result_set.to_create_alarms.len() > 0 {
                    let create_alarms = result_set.to_create_alarms.get(record_id).unwrap_or(&Vec::new()).clone();
                    for alarm in create_alarms {
                        let mut alarm_copy = alarm.clone();
                        alarm_copy.record_id = None;
                        alarm_copy.field_id = None;
                        field_extra_map.insert(alarm.field_id.unwrap(), FieldExtraMapValue { alarm: Some(alarm_copy) });
                    }
                }
      
                if record_data.len() > 0 {
                    for (field_id, _cell_val) in &record_data {
                        if field_updated_map.contains_key(field_id) {
                            let mut field_updated_info = field_updated_map.get(field_id).unwrap().clone();
                            field_updated_info.at = Some(updated_at);
                            field_updated_info.by = uuid.clone();
                            field_updated_map.insert(field_id.clone(), field_updated_info);
                        } else {
                            field_updated_map.insert(field_id.clone(), FieldUpdatedValue { at: Some(updated_at), by: uuid.clone(), ..Default::default() });
                        }
                    }
                }
                // Merge field data in original 'data'
                let tmp_data: HashMap<String, Value> = match deleted_record_map.get(record_id) {
                    Some(record) => from_value(record.data.clone()).unwrap(),
                    None => HashMap::new(),
                };
                for (field_id, cell_val) in &tmp_data {
                    if record_data.contains_key(field_id) {
                        continue;
                    }
                    record_data.insert(field_id.clone(), cell_val.clone());
                }
                if prev_record_meta.is_some() {
                    let record_meta = RecordMeta { field_updated_map: Some(field_updated_map), field_extra_map: Some(field_extra_map), ..prev_record_meta.unwrap() };
                    let record_action = DatasheetOtService::generate_jot_action("OI", vec!["recordMap", record_id, "recordMeta"], to_value(record_meta.clone()).unwrap(), None);
                    cur_record_info.record_meta = Some(record_meta.clone());
                    record_map_actions.push(record_action);
                    record_meta_map.insert(record_id.clone(), to_value(record_meta).unwrap());
                }
                restore_record_map.insert(record_id.clone(), cur_record_info);
            } else {
                let update_field_ids = record_data.keys().cloned().collect::<Vec<_>>();
                let mut field_updated_map = HashMap::new();
      
                // Field IDs that need initialization, record its modified time and modifier
                if update_field_ids.len() > 0 {
                    for field_id in &update_field_ids {
                        field_updated_map.insert(field_id.clone(), FieldUpdatedValue { at: Some(updated_at), by: uuid.clone(), ..Default::default() });
                    }
                }

                // Process AutoNumber field type, store in fieldUpdateMap
                if auto_number_fields.len() > 0 {
                    for field in &auto_number_fields {
                        let field_id = field.id.clone();
                        let next_id = field.property.clone().unwrap().next_id.clone().unwrap() + record_index;
                        field_updated_map.insert(field_id.clone(), FieldUpdatedValue { auto_number: Some(next_id as u64), ..Default::default() });
                    }
                    record_index = record_index + 1;
                }
      
                // 1. If create record direcly, no need to add modified time and modifier, namely don't update fieldUpdateMap
                // 2. If copy multiple records direcly, cell is not empty and existing records is not enough for pasting,
                //    need to add modified time and modifier, namely update fieldUpdateMap
                // 3. If AutoNumber is added, must update fieldUpdateMap
                let new_record_meta = if update_field_ids.len() > 0 || auto_number_fields.len() > 0 {
                    RecordMeta { created_at: Some(created_at), created_by: uuid.clone(), field_updated_map: Some(field_updated_map), ..Default::default() }
                } else {
                    RecordMeta { created_at: Some(created_at), created_by: uuid.clone(), ..Default::default() }
                };

                // TODO Delete this code block after cell alarm is changed to async creation
                // Write recordMeta.fieldExtraMap when creating cell alarm
                let mut field_extra_map = HashMap::new();
                if !result_set.to_create_alarms.is_empty() {
                    let tmp = Vec::new();
                    let create_alarms = result_set.to_create_alarms.get(record_id).unwrap_or(&tmp);
                    for alarm in create_alarms {
                        let mut alarm_copy = alarm.clone();
                        alarm_copy.record_id = None;
                        alarm_copy.field_id = None;
                
                        field_extra_map.insert(alarm.field_id.clone().unwrap(), FieldExtraMapValue { alarm: Some(alarm_copy) });
                    }
                }
                
                let new_meta_with_field_extra = if field_extra_map.is_empty() {
                    new_record_meta.clone()
                } else {
                    RecordMeta {
                        field_extra_map: Some(field_extra_map),
                        ..new_record_meta.clone()
                    }
                };
                save_record_entities.push(
                    {(
                        record_id,
                        record_data,
                        new_meta_with_field_extra.clone(),
                    )}
                );
                
                let record_action = DatasheetOtService::generate_jot_action(
                    "OI",
                    vec!["recordMap", &record_id, "recordMeta"],
                    to_value(new_meta_with_field_extra).unwrap(),
                    None
                );
                record_map_actions.push(record_action);
                record_meta_map.insert(record_id.clone(),to_value(new_record_meta).unwrap());
            }
        }

        effect_map.insert(EffectConstantName::RecordMetaMap.to_string(), to_value(record_meta_map).unwrap());
            
        // === Insert data rapidly ===
        if save_record_entities.len() > 0 {
            // if (this.logger.isDebugEnabled()) {
                //       this.logger.debug(`[${dstId}] Batch insert record`);
                //     }
            // Custom batch insertion will be faster by 100 times
            let chunk_size = 3000;
            if save_record_entities.len() > chunk_size {
                // Insert in chunks to avoid SQL length overflow, insert 3000 rows one time
                for entities in save_record_entities.chunks(chunk_size) {
                    let save_record_entities_tmp = entities.to_vec();
                    self.loader.create_record(&dst_id, &revision, &user_id, save_record_entities_tmp).await;
                }
            } else {
                self.loader.create_record(&dst_id, &revision, &user_id, save_record_entities).await;
            }

            // As all change in middle layer need to be synced to client, here effect change is performed => meta
            let created_by_fields = field_list.iter().filter(|fld| fld.kind == FieldKindSO::CreatedBy || fld.kind == FieldKindSO::LastModifiedBy).cloned().collect::<Vec<_>>();
            let process_fields = [&created_by_fields[..], &auto_number_fields[..]].concat();
            let process_fields = process_fields.into_iter().map(|field| field.clone()).collect::<Vec<_>>();
            // Construct actions<setFieldAttr> that are about to be synced to client
            let field_attr_actions = self.get_meta_action_by_field_type(FieldTypeParams{uuid: uuid.clone(), fields: process_fields, next_id: Some(record_index), ..Default::default()}, effect_map).await;
            meta_actions.extend(field_attr_actions);
        }

        // === Batch recover records ===
        if !restore_record_map.is_empty() {
            // if self.logger.is_debug_enabled() {
            //     self.logger.debug(format!("[{}] Batch recover records", dst_id));
            // }
            for (_record_id, _record_info) in &restore_record_map {
                // Update DatasheetRecordEntity and DatasheetRecordArchiveEntity
                // This is a placeholder, replace with your actual database update code
                // update_datasheet_record_entity(record_id, record_info, user_id, revision).await?;
                // update_datasheet_record_archive_entity(record_id, user_id).await?;
            }
            // As all changes in middle layer need to be synced to client, here effect change is performed => meta
            // filter LastModifiedBy related and not to be deleted Field
            let updated_by_fields: Vec<&FieldSO> = field_list.iter().filter(|fld| fld.kind == FieldKindSO::LastModifiedBy && !result_set.clean_field_map.contains_key(&fld.id)).cloned().collect();
            let updated_by_fields = updated_by_fields.into_iter().map(|field| field.clone()).collect::<Vec<_>>();
            // collect UUID into field.property.uuids, and return corresponding actions
            let field_attr_actions = self.get_meta_action_by_field_type(FieldTypeParams{uuid: uuid.clone(), fields: updated_by_fields, ..Default::default()}, effect_map).await;
            // let field_attr_actions = self.get_meta_action_by_field_type(uuid, &updated_by_fields, effect_map).await?;
            meta_actions.extend(field_attr_actions);
        }

        // update remoteChangeset
        self.update_effect_remote_changeset(effect_map, Some(meta_actions), Some(record_map_actions));

        let end_time = Utc::now().timestamp_millis();
        println!("[{}] ====> Finished batch creating records......duration: {}ms", dst_id, end_time - begin_time);
    
        Ok(())
    }

    async fn handle_meta(&self, common_data: CommonData, effect_map: &mut HashMap<String, Value>) -> anyhow::Result<()> {
        let CommonData { user_id, dst_id, revision, .. } = common_data;
        let meta_actions = effect_map.get(EffectConstantName::MetaActions.as_str()).unwrap();
        let meta_actions:Vec<ActionOTO> = from_value(meta_actions.clone()).unwrap();
        if meta_actions.len() == 0 {
            return Ok(());
        }
    
        let start_time = Instant::now();
        let meta = self.get_meta_data_by_cache(&dst_id, effect_map).await?;
        let duration = start_time.elapsed();
        println!("Time elapsed in get_meta_data_by_cache() is: {:?}", duration);
        let meta = from_value::<DatasheetMetaSO>(meta).unwrap();
        // Merge Meta
        let mut snapshot = DatasheetSnapshotSO {
            meta: meta.clone(),
            record_map: HashMap::new(),
            datasheet_id: dst_id.clone(),
        };
        match jot_apply_snapshot(&mut snapshot, meta_actions) {
            Ok(_) => (),
            Err(e) => {
                println!("handle_meta err {}", e);
                return Err(e);
            }
        }
        // if meta.views.iter().any(|view| view.is_none()) {
        //     // After successfully applying OP, check views, if null exists then report error
        //     return Err(ServerException::ApplyMetaError);
        // }
        let str_meta = to_string(&snapshot.meta).unwrap();
        let updated_by = user_id.unwrap();
        self.loader.update_meta_data(&dst_id, &str_meta, &revision, &updated_by).await;
        Ok(())
    }

    async fn create_new_changeset(&self, common_data: &CommonData, remote_changeset: Value) -> anyhow::Result<()> {
        let remote_changeset: RemoteChangeset = from_value(remote_changeset.clone()).unwrap();
        // println!("[{}] Insert new changeset", remote_changeset.resource_id);
    
        let begin_time = Instant::now();
        println!("[{}] ====> Starting storing changeset......", remote_changeset.resource_id);

        let id = generate_u64_id().to_string();
        let message_id = remote_changeset.message_id.clone();
        let dst_id = remote_changeset.resource_id.clone();
        let member_id = common_data.user_id.clone().unwrap();
        let operations = to_value(remote_changeset.operations.clone()).unwrap();
        let revision = remote_changeset.revision as u32;
        self.loader.create_new_changeset(&id, &message_id, &dst_id, &member_id, operations, &revision).await;
    
        let end_time = Instant::now();
        println!("[{}] ====> Finished storing changeset......duration: {}ms", remote_changeset.resource_id, end_time - begin_time);
    
        Ok(())
    }

    async fn update_revision(&self, common_data: &CommonData) -> anyhow::Result<()> {
        let user_id = &common_data.user_id.clone().unwrap();
        let dst_id = &common_data.dst_id;
        let revision = common_data.revision;
        self.loader.update_revision_by_dst_id(dst_id, &revision, user_id).await;
    
        Ok(())
    }

    pub async fn analyse_operates(
        &self,
        space_id: String,
        main_datasheet_id: String,
        operation: &Vec<Operation>,
        datasheet_id: &str,
        permission: &NodePermissionSO,
        effect_map: &mut HashMap<String, Value>,
        result_set: &mut ResultSet,
        auth: &AuthHeader,
        source_type: &Option<SourceTypeEnum>,
    ) -> anyhow::Result<Value> {
        result_set.space_id = space_id.to_string();
        result_set.datasheet_id = datasheet_id.to_string();
        result_set.auth = auth.clone();
        result_set.source_type = source_type.clone();
        result_set.attach_cite_collector.node_id = datasheet_id.to_string();

        result_set.space_capacity_over_limit = self.loader.capacity_over_limit(auth, &space_id).await?;
        let meta = self.get_meta_data_by_cache(datasheet_id, effect_map).await?;
        let meta: DatasheetMetaSO = from_value(meta)
            .map_err(|err| {
                format!(
                    "Failed to deserialize meta to DatasheetMetaSO: {}",
                    err.to_string()
                )
            }).unwrap();
        let field_map = meta.field_map.clone();
        result_set.temporary_field_map = field_map;
        result_set.temporary_views = meta.views;

        for op in operation.iter() {
            let condition = op.main_link_dst_id.as_deref().unwrap_or(&main_datasheet_id);
            let main_dst_permission = if auth.internal.unwrap_or(false) || datasheet_id == condition || *source_type == Some(SourceTypeEnum::FORM) {
                permission.clone()
            } else {
                self.loader.get_node_role(condition.to_string(), auth.clone(), None, None, None, None, None).await?
            };
            result_set.main_link_dst_permission_map.insert(condition.to_string(), main_dst_permission);
        }

        let mut meta_actions: Vec<ActionOTO> = Vec::new();

        for cur in operation.iter() {
            // There are many logs during big data operation, commenting out this log is ok
            // if self.logger.is_debug_enabled() {
            //     self.logger.debug(format!("[{}] changeset OperationAction: {:?}", datasheet_id, cur.actions));
            // }
            let cmd = cur.cmd.clone();
            result_set.link_action_main_dst_id = Some(cur.main_link_dst_id.clone().unwrap_or(main_datasheet_id.clone()));

            for action in cur.actions.iter() {
                if action.op.p[0].as_str() == "meta" {
                    self.deal_with_meta(&cmd, action, &permission, result_set);
                    meta_actions.push(action.clone());
                } else {
                    // Collect attachment fields
                    self.handle_attach_op_cite(action, result_set);
                    self.deal_with_record_map(&cmd, action, &permission, result_set).await?;
                }
            }
        }
        result_set.meta_actions = meta_actions;

        println!("result_set.add_views = {:?}", result_set.add_views);
        if !result_set.add_views.is_empty() {
            // let space_usages = self.rest_service.get_space_usage(space_id).await?;
        //     let subscribe_info = self.rest_service.get_space_subscription(space_id).await?;
        //     let mut after_add_gantt_view_count_in_space = space_usages.gantt_view_nums;
        //     let mut after_add_calendar_count_in_space = space_usages.calendar_view_nums;
        
        //     for view in &result_set.add_views {
        //         match view.view_type {
        //             ViewType::Gantt => after_add_gantt_view_count_in_space += 1,
        //             ViewType::Calendar => after_add_calendar_count_in_space += 1,
        //             _ => (),
        //         }
        //     }
        
        //     let check_gantt_views_num = after_add_gantt_view_count_in_space != space_usages.gantt_view_nums;
        //     let check_calendar_views_num = after_add_calendar_count_in_space != space_usages.calendar_view_nums;
        
        //     if subscribe_info.max_gantt_views_in_space != -1 && check_gantt_views_num && after_add_gantt_view_count_in_space > subscribe_info.max_gantt_views_in_space {
        //         self.rest_service.create_notification(&result_set.auth, vec![
        //             Notification {
        //                 space_id: space_id.to_string(),
        //                 template_id: "space_gantt_limit".to_string(),
        //                 body: Extras {
        //                     usage: after_add_gantt_view_count_in_space,
        //                     specification: subscribe_info.max_gantt_views_in_space,
        //                 },
        //             },
        //         ]).await?;
        //         panic!(DatasheetException::ViewAddLimitForGantt(subscribe_info.max_gantt_views_in_space, after_add_gantt_view_count_in_space));
        //     }
        
        //     if subscribe_info.max_calendar_views_in_space != -1 && check_calendar_views_num && after_add_calendar_count_in_space > subscribe_info.max_calendar_views_in_space {
        //         self.rest_service.create_notification(&result_set.auth, vec![
        //             Notification {
        //                 space_id: space_id.to_string(),
        //                 template_id: "space_calendar_limit".to_string(),
        //                 body: Extras {
        //                     usage: after_add_calendar_count_in_space,
        //                     specification: subscribe_info.max_calendar_views_in_space,
        //                 },
        //             },
        //         ]).await?;
        //         panic!(DatasheetException::ViewAddLimitForCalendar(subscribe_info.max_calendar_views_in_space, after_add_calendar_count_in_space));
        //     }
        }

        println!("result_set.to_create_record = {:?}", result_set.to_create_record);
        println!("result_set.to_unarchive_record = {:?}", result_set.to_unarchive_record);
        if !result_set.to_create_record.is_empty() || !result_set.to_unarchive_record.is_empty() {
            let current_record_count_in_dst = self.loader.count_rows_by_dst_id(datasheet_id).await? as usize;
            let space_usages = self.loader.get_space_usage(&space_id).await?;
            let subscribe_info = self.loader.get_space_subscription(&space_id).await?;
            let after_create_count_in_dst = current_record_count_in_dst + result_set.to_create_record.len() + result_set.to_unarchive_record.len();
            let after_create_count_in_space = space_usages.record_nums as usize + result_set.to_create_record.len() + result_set.to_unarchive_record.len();
        
            if after_create_count_in_dst > subscribe_info.max_rows_per_sheet as usize {
                let _datasheet_entity = self.loader.get_datasheet_by_dst_id(datasheet_id).await?;
                let specification = subscribe_info.max_rows_per_sheet;
                let usage = after_create_count_in_dst;
                // self.rest_service.create_notification(&result_set.auth, vec![
                //     Notification {
                //         space_id: space_id.to_string(),
                //         template_id: "datasheet_record_limit".to_string(),
                //         body: Extras {
                //             usage: after_create_count_in_dst,
                //             specification: subscribe_info.max_rows_per_sheet,
                //             node_name: datasheet_entity.dst_name.clone(),
                //         },
                //     },
                // ]).await?;
                return Err(anyhow::Error::msg(format!("max_rows_per_sheet={}|{}", specification, usage)));
                // panic!(DatasheetException::RecordAddLimitPerDatasheet(subscribe_info.max_rows_per_sheet, after_create_count_in_dst));
            }
        
            if after_create_count_in_space > subscribe_info.max_rows_in_space as usize {
                let specification = subscribe_info.max_rows_in_space;
                let usage = after_create_count_in_space;
                // self.rest_service.create_notification(&result_set.auth, vec![
                //     Notification {
                //         space_id: space_id.to_string(),
                //         template_id: "space_record_limit".to_string(),
                //         body: Extras {
                //             usage: after_create_count_in_space,
                //             specification: subscribe_info.max_rows_in_space,
                //         },
                //     },
                // ]).await?;
                return Err(anyhow::Error::msg(format!("max_rows_in_space={}|{}", specification,usage)));
                // panic!(DatasheetException::RecordAddLimitWithinSpace(subscribe_info.max_rows_in_space, after_create_count_in_space));
            }
        
            let field_map = &result_set.temporary_field_map;
        
            for (_record_id, value) in &mut result_set.to_create_record {
                let map_tmp = Map::new();
                let new_value = value.as_object().unwrap_or(&map_tmp);
                if new_value.is_empty() {
                    continue;
                }
                let mut _value = HashMap::new();
                for (field_id, field_value) in new_value {
                    let field = field_map.get(field_id);
                    if field.is_none() {
                        // Log the error
                        continue;
                    }

                    let field = field.unwrap();
                    if get_is_computed(field.clone()) {
                        continue;
                    }
        
                    // Validate the cell value
                    // match field.validate_cell_value(field_value) {
                    //     Ok(_) => { _value.insert(field_id.clone(), field_value.clone()); },
                    //     Err(error) => {
                    //         // Log the error
                    //         continue;
                    //     }
                    // }
                    _value.insert(field_id.clone(), field_value.clone());
                }
                *value = to_value(_value).unwrap();
                // result_set.to_create_record.insert(record_id.clone(), to_value(_value).unwrap());
            }
        }

        println!("result_set.to_archive_record_ids = {:?}", result_set.to_archive_record_ids);
        if !result_set.to_archive_record_ids.is_empty() {
            // let current_archived_record_count_in_dst = self.record_service.get_archived_record_count(datasheet_id).await?;
            // let subscribe_info = self.rest_service.get_space_subscription(space_id).await?;
            // let after_archive_count_in_dst = current_archived_record_count_in_dst + result_set.to_archive_record_ids.len();
        
            // if subscribe_info.max_archived_rows_per_sheet >= 0 && after_archive_count_in_dst > subscribe_info.max_archived_rows_per_sheet {
            //     panic!(DatasheetException::RecordArchiveLimitPerDatasheet(subscribe_info.max_archived_rows_per_sheet, after_archive_count_in_dst));
            // }
        }

        println!("result_set.replace_cell_map = {:?}", result_set.replace_cell_map);
        if !result_set.replace_cell_map.is_empty() {
            let field_map = &result_set.temporary_field_map;
        
            for (_record_id, value) in &mut result_set.replace_cell_map {
                let mut new_value = Vec::new();
                for item in value.clone() {
                    let field_id = &item.field_id;
                    let _data = &item.data;
                    let field = field_map.get(field_id);
                    if field.is_none() {
                        // Log the error
                        continue;
                    }
        
                    let field = field.unwrap();
                    // let is_computed = FieldFactory::create_field(field.clone(), context.clone()).is_computed();
                    let is_computed = get_is_computed(field.clone());
                    if is_computed {
                        // Log the error
                        continue;
                    }
                    // Validate the cell value
                    // match field.validate_cell_value(data) {
                    //     Ok(_) => { _value.push(item.clone()); },
                    //     Err(error) => {
                    //         // Log the error
                    //         continue;
                    //     }
                    // }
                    new_value.push(item.clone());
                }
                *value = new_value;
            }
        }

        if !result_set.fld_op_in_view_map.is_empty() {
            for (field_id, is_li) in &result_set.fld_op_in_view_map {
                if *is_li {
                    if !result_set.to_create_foreign_datasheet_id_map.contains_key(field_id) {
                        panic!("Operation denied");
                    }
                } else if !result_set.to_delete_foreign_datasheet_id_map.contains_key(field_id) {
                    panic!("Operation denied");
                }
            }
        }
        
        if !result_set.fld_op_in_rec_map.is_empty() {
            for (field_id, cmd) in &result_set.fld_op_in_rec_map {
                match cmd.as_str() {
                    "DeleteField" | "SetFieldAttr" => {
                        if !result_set.to_delete_foreign_datasheet_id_map.contains_key(field_id) {
                            panic!("Operation denied");
                        }
                    },
                    "UNDO:DeleteField" | "UNDO:SetFieldAttr" => {
                        if !result_set.to_create_foreign_datasheet_id_map.contains_key(field_id) {
                            panic!("Operation denied");
                        }
                    },
                    _ => {
                        if result_set.to_create_foreign_datasheet_id_map.contains_key(field_id) || result_set.to_delete_foreign_datasheet_id_map.contains_key(field_id) {
                            continue;
                        }
                        let kind = meta.field_map.get(field_id).unwrap().kind;
                        if kind != FieldKindSO::Link && kind != FieldKindSO::OneWayLink {
                            panic!("Operation denied");
                        }
                    }
                }
            }
        }

        println!("result_set.to_create_look_up_properties = {:?}", result_set.to_create_look_up_properties);
        if !result_set.to_create_look_up_properties.is_empty() {
            for _item in &result_set.to_create_look_up_properties {
                // if item.skip_field_permission || item.cmd == "UNDO:DeleteField" || item.cmd == "UNDO:SetFieldAttr" {
                //     continue;
                // }
                // let foreign_datasheet_id;
                // if result_set.to_create_foreign_datasheet_id_map.contains_key(&item.related_link_field_id) {
                //     foreign_datasheet_id = result_set.to_create_foreign_datasheet_id_map.get(&item.related_link_field_id).unwrap().clone();
                // } else {
                //     let meta = self.get_meta_data_by_cache(datasheet_id, &effect_map).await?;
                //     let field = meta.field_map.get(&item.related_link_field_id);
                //     if field.is_none() || (field.unwrap().kind != FieldKindSO::Link && field.unwrap().kind != FieldKindSO::OneWayLink) {
                //         panic!("Operation denied");
                //     }
                //     foreign_datasheet_id = field.unwrap().property.foreign_datasheet_id.clone();
                // }
                // let foreign_permission = get_node_role(foreign_datasheet_id, &auth).await?;
                // if !foreign_permission.readable {
                //     panic!("Operation denied");
                // }
                // let target_field_permission = !foreign_permission.field_permission_map.is_empty() && foreign_permission.field_permission_map.get(&item.look_up_target_field_id).unwrap().permission.readable;
                // if !target_field_permission {
                //     panic!("Operation denied");
                // }
            }
        }
        
        effect_map.insert(EffectConstantName::MetaActions.to_string(), to_value(result_set.meta_actions.clone()).unwrap());
        effect_map.insert(EffectConstantName::AttachCite.to_string(), to_value(result_set.attach_cite_collector.clone()).unwrap());
        effect_map.insert(EffectConstantName::RecordMetaMap.to_string(), Value::Null);

        Ok(Value::Null)
    }

    fn update_effect_remote_changeset(&self, effect_map: &mut HashMap<String, Value>, meta_actions: Option<Vec<ActionOTO>>, record_map_actions: Option<Vec<ActionOTO>>) {
        let mut remote_change_operations: Vec<Operation> = Vec::new();
        if let Some(meta_actions) = meta_actions {
            if !meta_actions.is_empty() {
                remote_change_operations.push(Operation{ cmd: CollaCommandName::SystemSetFieldAttr.to_string(), actions: meta_actions, ..Default::default() });
            }
        }
        if let Some(record_map_actions) = record_map_actions {
            if !record_map_actions.is_empty() {
                remote_change_operations.push(Operation{ cmd: CollaCommandName::SystemSetRecords.to_string(), actions: record_map_actions, ..Default::default() });
            }
        }
        if !remote_change_operations.is_empty() {
            self.update_effect_map(effect_map, EffectConstantName::RemoteChangeset, to_value(remote_change_operations).unwrap());
        }
    }

    async fn get_meta_data_by_cache(
        &self,
        dst_id: &str,
        effect_map: &mut HashMap<String, Value>,
    ) -> anyhow::Result<Value> {
        if let Some(meta) = effect_map.get(&EffectConstantName::Meta.to_string()) {
            return Ok(meta.clone());
        }
    
        let meta = self.loader.get_meta_data_by_dst_id(dst_id, false).await?;
        let Some(meta) = meta else {
            return Err(
                anyhow::Error::msg(format!("meta not exist by {dst_id}"))
            );
        };
        effect_map.insert(EffectConstantName::Meta.to_string(), meta.clone());
        Ok(meta)
    }

    async fn get_meta_action_by_field_type(&self, data: FieldTypeParams, effect_map: &mut HashMap<String, Value>) -> Vec<ActionOTO> {
        let mut meta_actions: Vec<ActionOTO> = Vec::new();
        let FieldTypeParams { uuid, fields , ..} = data;
    
        if fields.is_empty() {
            return vec![];
        }
    
        for field in fields {
            let field_id = field.id.clone();
            let property = field.property.clone().unwrap();
            let kind = field.kind.clone();
    
            match kind {
                FieldKindSO::AutoNumber => {
                    let next_id = match property.next_id {
                        Some(id) => id + data.next_id.unwrap_or(0),
                        None => data.next_id.unwrap_or(1),
                    };
                    let mut new_field = field.clone();
                    let mut new_property = new_field.property.clone().unwrap();
                    new_property.next_id = Some(next_id);
                    new_field.property = Some(new_property);
                    let meta_action = DatasheetOtService::generate_jot_action("OR", vec!["meta", "fieldMap", &field_id], to_value(new_field.clone()).unwrap(), Some(to_value(field.clone()).unwrap()));
                    meta_actions.push(meta_action);
                },
                FieldKindSO::CreatedBy | FieldKindSO::LastModifiedBy => {
                    if property.uuids.clone().unwrap().contains(
                        &Some(serde_json::Value::String(uuid.clone().unwrap()))
                    ) {
                        continue;
                    }
                    let new_field = field.clone();
                    new_field.property.clone().unwrap().uuids.unwrap().push(Some(serde_json::Value::String(uuid.clone().unwrap())));
                    let meta_action = DatasheetOtService::generate_jot_action("OR", vec!["meta", "fieldMap", &field_id], to_value(new_field.clone()).unwrap(), Some(to_value(field.clone()).unwrap()));
                    meta_actions.push(meta_action);
                },
                _ => {}
            }
        }
    
        self.update_effect_map(effect_map, EffectConstantName::MetaActions, to_value(meta_actions.clone()).unwrap());
        meta_actions
    }

    fn update_effect_map(&self, effect_map: &mut HashMap<String, Value>, constant_name: EffectConstantName, value: Value) {
        let value = value.as_array().unwrap().clone();
        let base = effect_map.get(&constant_name.to_string()).cloned();
        let current = match constant_name {
            EffectConstantName::RemoteChangeset => {
                let draft = base.unwrap_or(Value::Array(vec![]));
                let mut draft_tmp = draft.as_object().unwrap().clone();
                let mut op = draft_tmp.get("operations").unwrap().clone();
                if let Value::Array(arr) = &mut op {
                    arr.extend(value);
                }
                draft_tmp.insert("operations".to_string(), op);
                to_value(draft_tmp).unwrap()
            },
            EffectConstantName::MetaActions => {
                let mut draft = base.unwrap_or(Value::Array(vec![]));
                if let Value::Array(arr) = &mut draft {
                    arr.extend(value);
                }
                draft
            },
            _ => base.unwrap_or(Value::Array(vec![])),
        };
        effect_map.insert(constant_name.to_string(), current);
    }

    fn subscription_supported_field_type(&self, kind: &FieldKindSO) -> bool {
        matches!(kind, FieldKindSO::Member | FieldKindSO::CreatedBy)
    }

    fn get_auto_subscription_fields(&self, field_map: &HashMap<String, FieldSO>) -> Vec<FieldSO> {
        let mut auto_subscription_fields: Vec<FieldSO> = Vec::new();
        for field in field_map.values() {
            if self.subscription_supported_field_type(&field.kind) && field.property.as_ref().map_or(false, |p| p.subscription.unwrap_or(false)) {
                auto_subscription_fields.push(field.clone());
            }
        }
        auto_subscription_fields
    }

    fn collect_record_subscriptions(&self, auto_subscription_fields: &Vec<FieldSO>, record_id: &str, oi_data: &HashMap<String, Value>, od_data: &HashMap<String, Value>, result_set: &mut ResultSet) {
        if !auto_subscription_fields.is_empty() {
            for field in auto_subscription_fields {
                if field.kind == FieldKindSO::Member {
                    let tmp = Value::Array(vec![]);
                    let tmp_2 = vec![];
                    let oi_unit_ids = oi_data.get(&field.id).unwrap_or(&tmp).as_array().unwrap_or(&tmp_2);
                    let od_unit_ids = od_data.get(&field.id).unwrap_or(&tmp).as_array().unwrap_or(&tmp_2);
                    if oi_unit_ids.is_empty() && od_unit_ids.is_empty() {
                        continue;
                    }
                    let to_subscribe_unit_ids: Vec<&Value> = oi_unit_ids.iter().filter(|unit_id| !od_unit_ids.contains(unit_id)).collect();
                    let to_unsubscribe_unit_ids: Vec<&Value> = od_unit_ids.iter().filter(|unit_id| !oi_unit_ids.contains(unit_id)).collect();
                    for unit_id in to_subscribe_unit_ids {
                        result_set.to_create_record_subscriptions.push(RecordSubscriptions{unit_id: unit_id.clone().as_str().unwrap().to_string(), record_id: record_id.to_string()});
                    }
                    for unit_id in to_unsubscribe_unit_ids {
                        result_set.to_cancel_record_subscriptions.push(RecordSubscriptions{unit_id: unit_id.clone().as_str().unwrap().to_string(), record_id: record_id.to_string()});
                    }
                } else if field.kind == FieldKindSO::CreatedBy {
                    if oi_data.contains_key(&field.id) && !result_set.creator_auto_subscribed_record_ids.contains(&record_id.to_string()) {
                        result_set.creator_auto_subscribed_record_ids.push(record_id.to_string());
                    }
                }
            }
        }
    }
}