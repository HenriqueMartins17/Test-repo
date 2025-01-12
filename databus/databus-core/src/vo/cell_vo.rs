use utoipa::ToSchema;
use derivative::Derivative;
use serde::{Deserialize, Serialize};
use serde::Serializer;
use serde_json::{Number, Value};

#[derive(Deserialize, Debug, Clone, Eq, PartialEq)]
#[serde(rename_all = "camelCase")]
pub enum CellValueVo {
    TextCellValue(Vec<TextCell>),
    NumberCellValue(Number),
    SingleSelectCellValue(String),
    MultiSelectCellValue(Vec<String>),
    DateTimeCellValue(Number),
    AttachmentCellValue(Vec<AttachmentCell>),
    LinkCellValue(Vec<String>),
    URLCellValue(Vec<UrlCell>),
    EmailCellValue(Vec<EmailCell>),
    PhoneCellValue(Vec<PhoneCell>),
    CheckboxCellValue(bool),
    RatingCellValue(Number),
    MemberCellValue(Vec<String>),
    LookUpCellValue(Vec<CellValueVo>),
    FormulaCellValue(Option<Vec<CellValueVo>>),
    CurrencyCellValue(Number),
    PercentCellValue(Number),
    SingleTextCellValue(Vec<SingleTextCell>),
    AutoNumberCellValue(Option<Vec<CellValueVo>>),
    CreatedTimeCellValue(Option<Vec<CellValueVo>>),
    LastModifiedTimeCellValue(Option<Vec<CellValueVo>>),
    CreatedByCellValue(Option<Vec<CellValueVo>>),
    LastModifiedByCellValue(Option<Vec<CellValueVo>>),
    CascaderCellValue(Vec<CascaderCell>),
    OneWayLinkCellValue(Vec<String>),
    DeniedFieldCellValue(Option<Vec<CellValueVo>>),
    NoValue,
}

impl Serialize for CellValueVo {
    fn serialize<S>(&self, serializer: S) -> Result<S::Ok, S::Error>
        where
            S: Serializer,
    {
        match self {
            CellValueVo::TextCellValue(text_cell) => {
                text_cell.serialize(serializer)
            }
            CellValueVo::NumberCellValue(value) => {
                value.serialize(serializer)
            }
            CellValueVo::SingleSelectCellValue(value) => {
                value.serialize(serializer)
            }
            CellValueVo::MultiSelectCellValue(value) => {
                if value.is_empty() {
                    serializer.serialize_none()
                } else {
                    value.serialize(serializer)
                }
            }
            CellValueVo::DateTimeCellValue(value) => {
                value.serialize(serializer)
            }
            CellValueVo::AttachmentCellValue(value) => {
                value.serialize(serializer)
            }
            CellValueVo::LinkCellValue(value) => {
                if value.is_empty() {
                    serializer.serialize_none()
                } else {
                    value.serialize(serializer)
                }
            }
            CellValueVo::EmailCellValue(value) => {
                value.serialize(serializer)
            }
            CellValueVo::PhoneCellValue(value) => {
                value.serialize(serializer)
            }
            CellValueVo::CheckboxCellValue(value) => {
                value.serialize(serializer)
            }
            CellValueVo::RatingCellValue(value) => {
                value.serialize(serializer)
            }
            CellValueVo::MemberCellValue(value) => {
                value.serialize(serializer)
            }
            CellValueVo::LookUpCellValue(value) => {
                if value.is_empty() {
                    serializer.serialize_none()
                } else {
                    value.serialize(serializer)
                }
            }
            CellValueVo::FormulaCellValue(value) => {
                if value.is_none() {
                    serializer.serialize_none()
                } else {
                    value.clone().unwrap().serialize(serializer)
                }
            }
            CellValueVo::CurrencyCellValue(value) => {
                value.serialize(serializer)
            }
            CellValueVo::PercentCellValue(value) => {
                value.serialize(serializer)
            }
            CellValueVo::SingleTextCellValue(value) => {
                value.serialize(serializer)
            }
            CellValueVo::AutoNumberCellValue(value) => {
                if value.is_none() {
                    serializer.serialize_none()
                } else {
                    value.clone().unwrap().serialize(serializer)
                }
            }
            CellValueVo::CreatedTimeCellValue(value) => {
                if value.is_none() {
                    serializer.serialize_none()
                } else {
                    value.clone().unwrap().serialize(serializer)
                }
            }
            CellValueVo::LastModifiedTimeCellValue(value) => {
                if value.is_none() {
                    serializer.serialize_none()
                } else {
                    value.clone().unwrap().serialize(serializer)
                }
            }
            CellValueVo::CreatedByCellValue(value) => {
                if value.is_none() {
                    serializer.serialize_none()
                } else {
                    value.clone().unwrap().serialize(serializer)
                }
            }
            CellValueVo::LastModifiedByCellValue(value) => {
                if value.is_none() {
                    serializer.serialize_none()
                } else {
                    value.clone().unwrap().serialize(serializer)
                }
            }
            CellValueVo::CascaderCellValue(value) => {
                value.serialize(serializer)
            }
            CellValueVo::OneWayLinkCellValue(value) => {
                value.serialize(serializer)
            }
            CellValueVo::DeniedFieldCellValue(value) => {
                serializer.serialize_none()
            }
            _ => { serializer.serialize_none() }
        }
    }
}

#[cfg(test)]
mod tests {
    use std::collections::HashMap;
    use derivative::Derivative;
    use serde::{Deserialize, Serialize, Serializer};
    use serde_json::Number;
    use utoipa::ToSchema;
    use crate::prelude::{AttachmentCell, CellValueVo, TextCell};


    #[derive(Deserialize, Serialize, Debug, Clone, Eq, PartialEq, ToSchema)]
    pub struct TestRecord {
        #[serde(serialize_with = "skip_serializing_if_none")]
        #[schema(value_type = Object)]
        data: HashMap<String, Option<CellValueVo>>,
    }

    fn skip_serializing_if_none<S>(
        data: &HashMap<String, Option<CellValueVo>>,
        serializer: S,
    ) -> Result<S::Ok, S::Error>
        where
            S: Serializer,
    {
        let filtered_data: HashMap<String, Option<CellValueVo>> = data
            .iter()
            .filter_map(|(key, value)| {
                if value.is_some() {
                    Some((key.clone(), value.clone()))
                } else {
                    None
                }
            })
            .collect();

        filtered_data.serialize(serializer)
    }


    #[test]
    fn test_ser() {
        let mut map = HashMap::new();
        map.insert("TextCellValue".to_string(), CellValueVo::TextCellValue(
            vec![TextCell { r#type: 1, text: Some("123".to_string()) }]
        ));
        map.insert("NumberCellValue".to_string(), CellValueVo::NumberCellValue(Number::from(123)));
        map.insert("SingleSelectCellValue".to_string(), CellValueVo::SingleSelectCellValue(String::from("123")));
        map.insert("MultiSelectCellValue".to_string(), CellValueVo::MultiSelectCellValue(vec![String::from("123")]));
        map.insert("MultiSelectCellValueEmp".to_string(), CellValueVo::MultiSelectCellValue(vec![]));
        map.insert("AttachmentCellValue".to_string(), CellValueVo::AttachmentCellValue(vec![
            AttachmentCell {
                id: Some("Nonexasdxa".to_string()),
                name: Some("Nonexasdxa".to_string()),
                mime_type: Some("Nonexasdxa".to_string()),
                token: Some("Nonexasdxa".to_string()),
                width: Some(1200),
                height: Some(1300),
                size: Some(500),
                bucket: Some("Nonexasdxa".to_string()),
                preview: Some("Nonexasdxa".to_string()),
            }
        ]));
        map.insert("LinkCellValue".to_string(), CellValueVo::LinkCellValue(vec![]));
        map.insert("CheckboxCellValue".to_string(), CellValueVo::CheckboxCellValue(false));
        map.insert("FormulaCellValueNone".to_string(), CellValueVo::FormulaCellValue(None));
        map.insert("FormulaCellValue".to_string(), CellValueVo::FormulaCellValue(Some(vec![
            CellValueVo::TextCellValue(
                vec![
                    TextCell {
                        r#type: 1,
                        text: Some("123".to_string()),
                    }
                ]
            ),
            CellValueVo::NumberCellValue(Number::from(123)),
        ])));
        let str = serde_json::to_string(&map).unwrap();
        println!("{}", str);


        let mut map2 = HashMap::new();

        map2.insert("a".to_string(), None);
        map2.insert("aa1".to_string(), map.get("FormulaCellValue").cloned());
        map2.insert("aa2".to_string(), map.get("FormulaCellValueNone").cloned());
        map2.insert("c".to_string(), map.get("AttachmentCellValue").cloned());
        map2.insert("b".to_string(), Some(CellValueVo::NumberCellValue(Number::from(12))));
        let record = TestRecord {
            data: map2.clone(),
        };
        let str1 = serde_json::to_string(&record).unwrap();
        println!("{}", str1)
    }
}


#[derive(Derivative, Deserialize, Serialize, Clone, Eq, PartialEq, ToSchema)]
#[derivative(Debug, Default)]
pub struct SingleTextCell {
    #[derivative(Default(value = "1"))]
    pub(crate) r#type: i32,

    #[serde(skip_serializing_if = "Option::is_none")]
    pub(crate) text: Option<String>,
}

#[derive(Derivative, Deserialize, Serialize, Clone, Eq, PartialEq, ToSchema)]
#[derivative(Debug, Default)]
pub struct CascaderCell {
    #[derivative(Default(value = "1"))]
    pub(crate) r#type: i32,

    #[serde(skip_serializing_if = "Option::is_none")]
    pub(crate) text: Option<String>,
}


#[derive(Derivative, Deserialize, Serialize, Clone, Eq, PartialEq, ToSchema)]
#[derivative(Debug, Default)]
pub struct PhoneCell {
    #[derivative(Default(value = "1"))]
    pub(crate) r#type: i32,

    #[serde(skip_serializing_if = "Option::is_none")]
    pub(crate) text: Option<String>,
}

#[derive(Derivative, Deserialize, Serialize, Clone, Eq, PartialEq, ToSchema)]
#[derivative(Debug, Default)]
pub struct TextCell {
    #[derivative(Default(value = "1"))]
    pub(crate) r#type: i32,

    #[serde(skip_serializing_if = "Option::is_none")]
    pub(crate) text: Option<String>,
}

#[derive(Derivative, Deserialize, Serialize, Clone, Eq, PartialEq, ToSchema)]
#[derivative(Debug, Default)]
pub struct EmailCell {
    #[derivative(Default(value = "4"))]
    pub(crate) r#type: i32,

    #[serde(skip_serializing_if = "Option::is_none")]
    pub(crate) text: Option<String>,

    #[serde(skip_serializing_if = "Option::is_none")]
    pub(crate) link: Option<String>,
}

#[derive(Derivative, Deserialize, Serialize, Clone, Eq, PartialEq, ToSchema)]
#[derivative(Debug, Default)]
pub struct UrlCell {
    #[derivative(Default(value = "2"))]
    pub(crate) r#type: i32,

    #[serde(skip_serializing_if = "Option::is_none")]
    pub(crate) text: Option<String>,

    #[serde(skip_serializing_if = "Option::is_none")]
    pub(crate) title: Option<String>,
}

#[derive(Derivative, Deserialize, Serialize, Clone, Eq, PartialEq, ToSchema)]
#[derivative(Debug, Default)]
pub struct AttachmentCell {
    #[serde(skip_serializing_if = "Option::is_none")]
    pub(crate) id: Option<String>,

    #[serde(skip_serializing_if = "Option::is_none")]
    pub(crate) name: Option<String>,

    #[serde(skip_serializing_if = "Option::is_none")]
    pub(crate) mime_type: Option<String>,

    #[serde(skip_serializing_if = "Option::is_none")]
    pub(crate) token: Option<String>,

    #[serde(skip_serializing_if = "Option::is_none")]
    pub(crate) width: Option<i32>,

    #[serde(skip_serializing_if = "Option::is_none")]
    pub(crate) height: Option<i32>,

    #[serde(skip_serializing_if = "Option::is_none")]
    pub(crate) size: Option<i32>,

    #[serde(skip_serializing_if = "Option::is_none")]
    pub(crate) bucket: Option<String>,

    #[serde(skip_serializing_if = "Option::is_none")]
    pub(crate) preview: Option<String>,
}