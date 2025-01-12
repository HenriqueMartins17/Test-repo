use json0::{operation::{PathSegment, OperationKind}, Operation};
use serde_json::Value;

use crate::{so::DatasheetSnapshotSO, ot::{types::ActionOTO, get_view_index, commands::SetCalendarStyleOption}};

use super::{SetCalendarStyleOTO, SetGalleryStyleOTO, SetOrgChartStyleOTO, SetGanttStyleOTO, SetKanbanStyleOTO};


pub struct ViewAction {
}
impl ViewAction {

    pub fn set_gallery_style_to_action(
        snapshot: DatasheetSnapshotSO,
        payload: SetGalleryStyleOTO,
    ) -> anyhow::Result<Option<ActionOTO>> {
        let SetGalleryStyleOTO { view_id, style_key, style_value } = payload;
    
        let view_index = get_view_index(snapshot.clone(), view_id);
        if view_index.is_none() {
            return Ok(None);
        }
        let view_index = view_index.unwrap();
        let view = &snapshot.meta.views[view_index];
        
        // if view.type != ViewType::Gallery || style_value == view.style[style_key] {
        //     return Ok(None);
        // }
        if view.r#type.is_some() && view.r#type.unwrap() != 3 {
            return Ok(None);
        }
        let _style = view.style.clone().unwrap();
        let value_tmp = match style_key.clone() {
            // CalendarStyleKeyType::ColorOption => {
            //     let color_option = style.color_option.clone().unwrap();
            //     let json_str = serde_json::to_string(&color_option).unwrap();
            //     let json_value: JsonValue = serde_json::from_str(&json_str).unwrap();
            //     json_value
            // },
            // CalendarStyleKeyType::StartFieldId => JsonValue::String(style.start_field_id.clone().unwrap()),
            // CalendarStyleKeyType::EndFieldId => JsonValue::String(style.end_field_id.clone().unwrap()),
            // CalendarStyleKeyType::IsColNameVisible => JsonValue::Boolean(style.is_col_name_visible.clone().unwrap()),
            _=> Value::Null,
        };
        if style_value == value_tmp {
            return Ok(None);
        }
        Ok(Some(ActionOTO {
            op_name: "OR".to_string(),
            op: Operation {
                p: vec![
                    PathSegment::String("meta".to_string()),
                    PathSegment::String("views".to_string()),
                    PathSegment::Number(view_index),
                    PathSegment::String("style".to_string()),
                    PathSegment::String(style_key.to_string()),
                ],
                kind: OperationKind::ObjectDelete {
                    od: value_tmp,
                },
            },
        }))
    }

    pub fn set_calendar_style_to_action(
        snapshot: DatasheetSnapshotSO, 
        payload: SetCalendarStyleOTO 
    ) -> anyhow::Result<Option<Vec<ActionOTO>>> {
        let SetCalendarStyleOTO { view_id, data, is_clear } = payload;
        let view_index = get_view_index(snapshot.clone(), view_id);
        if view_index.is_none() {
            return Ok(None);
        }
        let view_index = view_index.unwrap();
        let view = &snapshot.meta.views[view_index];
        // if view.r#type != ViewType::Calendar {
        //     return Ok(None);
        // }
        if view.r#type.is_some() && view.r#type.unwrap() != 5 {
            return Ok(None);
        }
        let _style = view.style.clone().unwrap();
        Ok(Some(data.iter()
            .filter(|SetCalendarStyleOption{ style_key, style_value }| {
                let value_tmp = match style_key.clone() {
                    // CalendarStyleKeyType::ColorOption => {
                    //     let color_option = style.color_option.clone().unwrap();
                    //     let json_str = serde_json::to_string(&color_option).unwrap();
                    //     let json_value: JsonValue = serde_json::from_str(&json_str).unwrap();
                    //     json_value
                    // },
                    // CalendarStyleKeyType::StartFieldId => JsonValue::String(style.start_field_id.clone().unwrap()),
                    // CalendarStyleKeyType::EndFieldId => JsonValue::String(style.end_field_id.clone().unwrap()),
                    // CalendarStyleKeyType::IsColNameVisible => JsonValue::Boolean(style.is_col_name_visible.clone().unwrap()),
                    _=> Value::Null,
                };
                style_value.clone() != value_tmp
            }).map(|SetCalendarStyleOption{ style_key, style_value:_ }| {
                let value_tmp = match style_key.clone() {
                    // CalendarStyleKeyType::ColorOption => {
                    //     let color_option = style.color_option.clone().unwrap();
                    //     let json_str = serde_json::to_string(&color_option).unwrap();
                    //     let json_value: JsonValue = serde_json::from_str(&json_str).unwrap();
                    //     json_value
                    // },
                    // CalendarStyleKeyType::StartFieldId => JsonValue::String(style.start_field_id.clone().unwrap()),
                    // CalendarStyleKeyType::EndFieldId => JsonValue::String(style.end_field_id.clone().unwrap()),
                    // CalendarStyleKeyType::IsColNameVisible => JsonValue::Boolean(style.is_col_name_visible.clone().unwrap()),
                    _=> Value::Null,
                };
                if is_clear.is_some() && is_clear.unwrap() {
                    ActionOTO {
                        op_name: "OD".to_string(),
                        op: Operation {
                            p: vec![
                                PathSegment::String("meta".to_string()),
                                PathSegment::String("views".to_string()),
                                PathSegment::Number(view_index),
                                PathSegment::String("style".to_string()),
                                PathSegment::String(style_key.to_string()),
                            ],
                            kind: OperationKind::ObjectDelete {
                                od: value_tmp,
                            },
                        },
                    }
                } else {
                    ActionOTO {
                        op_name: "OR".to_string(),
                        op: Operation {
                            p: vec![
                                PathSegment::String("meta".to_string()),
                                PathSegment::String("views".to_string()),
                                PathSegment::Number(view_index),
                                PathSegment::String("style".to_string()),
                                PathSegment::String(style_key.to_string()),
                            ],
                            kind: OperationKind::ObjectDelete {
                                od: value_tmp,
                            },
                        },
                    }
                }
            }).collect::<Vec<ActionOTO>>()))
    }

    pub fn set_view_style_to_action(snapshot: DatasheetSnapshotSO, payload: SetKanbanStyleOTO
    ) -> anyhow::Result<Option<ActionOTO>> {
        let SetKanbanStyleOTO { view_id, style_key, style_value } = payload;
        let view_index = get_view_index(snapshot.clone(), view_id);
        if view_index.is_none() {
            return Ok(None);
        }
        let view_index = view_index.unwrap();
        let view = &snapshot.meta.views[view_index];
        // if view.type != ViewType::Kanban || payload.style_value == view.style[payload.style_key] {
        //     return None;
        // }
        if view.r#type.is_some() && view.r#type.unwrap() != 2 {
            return Ok(None);
        }
        let _style = view.style.clone().unwrap();
        let value_tmp = match style_key.clone() {
            // CalendarStyleKeyType::ColorOption => {
            //     let color_option = style.color_option.clone().unwrap();
            //     let json_str = serde_json::to_string(&color_option).unwrap();
            //     let json_value: JsonValue = serde_json::from_str(&json_str).unwrap();
            //     json_value
            // },
            // CalendarStyleKeyType::StartFieldId => JsonValue::String(style.start_field_id.clone().unwrap()),
            // CalendarStyleKeyType::EndFieldId => JsonValue::String(style.end_field_id.clone().unwrap()),
            // CalendarStyleKeyType::IsColNameVisible => JsonValue::Boolean(style.is_col_name_visible.clone().unwrap()),
            _=> Value::Null,
        };
        if style_value == value_tmp {
            return Ok(None);
        }

        if style_value.is_null() {
            return Ok(Some(ActionOTO {
                op_name: "OD".to_string(),
                op: Operation {
                    p: vec![
                        PathSegment::String("meta".to_string()),
                        PathSegment::String("views".to_string()),
                        PathSegment::Number(view_index),
                        PathSegment::String("style".to_string()),
                        PathSegment::String(style_key.to_string()),
                    ],
                    kind: OperationKind::ObjectDelete {
                        od: value_tmp,
                    },
                },
            }));
        }
    
        Ok(Some(ActionOTO {
            op_name: "OR".to_string(),
            op: Operation {
                p: vec![
                    PathSegment::String("meta".to_string()),
                    PathSegment::String("views".to_string()),
                    PathSegment::Number(view_index),
                    PathSegment::String("style".to_string()),
                    PathSegment::String(style_key.to_string()),
                ],
                kind: OperationKind::ObjectReplace {
                    od: value_tmp,
                    oi: style_value,
                },
            },
        }))
    }
    
    pub fn set_org_chart_style_to_action(snapshot: DatasheetSnapshotSO, payload: SetOrgChartStyleOTO
    ) -> anyhow::Result<Option<ActionOTO>> {
        let SetOrgChartStyleOTO { view_id, style_key, style_value } = payload;
        let view_index = get_view_index(snapshot.clone(), view_id);
        if view_index.is_none() {
            return Ok(None);
        }
        let view_index = view_index.unwrap();
        let view = &snapshot.meta.views[view_index];
        // if view.type != ViewType::OrgChart || payload.style_value == view.style[payload.style_key] {
        //     return None;
        // }
        if view.r#type.is_some() && view.r#type.unwrap() != 7 {
            return Ok(None);
        }
        let _style = view.style.clone().unwrap();
        let value_tmp = match style_key.clone() {
            // OrgChartStyleKeyType::CoverFieldId => {
            //     let color_option = style.color_option.clone().unwrap();
            //     let json_str = serde_json::to_string(&color_option).unwrap();
            //     let json_value: JsonValue = serde_json::from_str(&json_str).unwrap();
            //     json_value
            // },
            // OrgChartStyleKeyType::IsCoverFit => JsonValue::String(style.start_field_id.clone().unwrap()),
            // OrgChartStyleKeyType::IsColNameVisible => JsonValue::String(style.end_field_id.clone().unwrap()),
            // OrgChartStyleKeyType::LinkFieldId => JsonValue::Boolean(style.is_col_name_visible.clone().unwrap()),
            // OrgChartStyleKeyType::Horizontal => JsonValue::Boolean(style.is_col_name_visible.clone().unwrap()),
            _=> Value::Null,
        };
        if style_value == value_tmp {
            return Ok(None);
        }
    
        Ok(Some(ActionOTO {
            op_name: "OR".to_string(),
            op: Operation {
                p: vec![
                    PathSegment::String("meta".to_string()),
                    PathSegment::String("views".to_string()),
                    PathSegment::Number(view_index),
                    PathSegment::String("style".to_string()),
                    PathSegment::String(style_key.to_string()),
                ],
                kind: OperationKind::ObjectReplace {
                    od: value_tmp,
                    oi: style_value,
                },
            },
        }))
    }
    
    pub fn set_gantt_style_to_action(snapshot: DatasheetSnapshotSO, payload: SetGanttStyleOTO
    ) -> anyhow::Result<Option<Vec<ActionOTO>>> {
        let SetGanttStyleOTO { view_id, data } = payload;
        let view_index = get_view_index(snapshot.clone(), view_id);
        if view_index.is_none() {
            return Ok(None);
        }
        let view_index = view_index.unwrap();
        let view = &snapshot.meta.views[view_index];
        // if view.type != ViewType::Gantt {
        //     return vec![];
        // }
        if view.r#type.is_some() && view.r#type.unwrap() != 6 {
            return Ok(None);
        }
        // let style = view.style.clone().unwrap();
        
        Ok(Some(data
            .iter()
            .filter(|style| {
                let value_tmp = match style.style_key.clone() {
                    // CalendarStyleKeyType::ColorOption => {
                    //     let color_option = style.color_option.clone().unwrap();
                    //     let json_str = serde_json::to_string(&color_option).unwrap();
                    //     let json_value: JsonValue = serde_json::from_str(&json_str).unwrap();
                    //     json_value
                    // },
                    // CalendarStyleKeyType::StartFieldId => JsonValue::String(style.start_field_id.clone().unwrap()),
                    // CalendarStyleKeyType::EndFieldId => JsonValue::String(style.end_field_id.clone().unwrap()),
                    // CalendarStyleKeyType::IsColNameVisible => JsonValue::Boolean(style.is_col_name_visible.clone().unwrap()),
                    _=> Value::Null,
                };
                style.style_value.clone() != value_tmp
                // style.style_value != view.style[style.style_key]
            }).map(|style| {
                let value_tmp = match style.style_key.clone() {
                    // CalendarStyleKeyType::ColorOption => {
                    //     let color_option = style.color_option.clone().unwrap();
                    //     let json_str = serde_json::to_string(&color_option).unwrap();
                    //     let json_value: JsonValue = serde_json::from_str(&json_str).unwrap();
                    //     json_value
                    // },
                    // CalendarStyleKeyType::StartFieldId => JsonValue::String(style.start_field_id.clone().unwrap()),
                    // CalendarStyleKeyType::EndFieldId => JsonValue::String(style.end_field_id.clone().unwrap()),
                    // CalendarStyleKeyType::IsColNameVisible => JsonValue::Boolean(style.is_col_name_visible.clone().unwrap()),
                    _=> Value::Null,
                };
                ActionOTO {
                    op_name: "OR".to_string(),
                    op: Operation {
                        p: vec![
                            PathSegment::String("meta".to_string()),
                            PathSegment::String("views".to_string()),
                            PathSegment::Number(view_index),
                            PathSegment::String("style".to_string()),
                            PathSegment::String(style.style_key.to_string()),
                        ],
                        kind: OperationKind::ObjectReplace {
                            od: value_tmp,
                            oi: style.style_value.clone(),
                        },
                    },
            }}).collect::<Vec<ActionOTO>>()))
    }
}