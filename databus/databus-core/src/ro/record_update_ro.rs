use std::{collections::HashMap, sync::Arc};
use crate::{so::{FieldKindSO, FieldSO, UrlValue, AttachmentValue}, fields::common_select_field, data_source_provider::IDataSourceProvider, dtos::fusion_api_dtos::IAssetDTO, utils::uuid::{get_new_id, IDPrefix}};
use common_select_field::{SelectField, FieldOptionValue};
use serde::{Deserialize, Serialize};
use serde_json::{json, Value};
use utoipa::ToSchema;

#[derive(Debug, Deserialize, Serialize)]
pub enum SegmentType {
    Unknown = 0,
    Text = 1, // plain text
    Mention = 5, // @type
    Url = 2, // link
    Image = 3, // inline image
    Email = 4, // email, subset of URL
}

#[derive(Debug, Deserialize, Serialize, ToSchema, Clone)]
#[serde(rename_all = "camelCase")] 
pub struct FieldUpdateRO {
    pub record_id: Option<String>,
    pub fields: HashMap<String, Value>,
}

#[derive(Debug, Deserialize, Serialize,PartialEq, ToSchema, Clone)]
pub enum FieldKeyEnum {
    #[serde(rename = "name")]
    NAME,
    #[serde(rename = "id")]
    ID,
}

#[derive(Debug, Deserialize, Serialize, ToSchema, Clone)]
#[serde(rename_all = "camelCase")] 
#[schema(example = json!({
    "records": [
        {"recordId": "rec83XdeQuk8y", "fields": {"t1":"100"}}
    ], 
    "fieldKey": "name"})
)]
pub struct RecordUpdateRO {
    pub records: Vec<FieldUpdateRO>,
    pub field_key: FieldKeyEnum,
}

impl RecordUpdateRO {
    pub fn get_record_ids(self) -> Vec<Option<String>> {
        self.records.iter().map(|record| record.record_id.clone()).collect()
    }

    pub async fn transform(
        self, 
        field_map:&HashMap<String, FieldSO>,
        loader: Option<Arc<dyn IDataSourceProvider>>,
    ) -> anyhow::Result<RecordUpdateRO> {
        let mut records = Vec::new();
        for record in &self.records {
            let mut fields = HashMap::new();
            for (field_key, _field_value) in &record.fields {
                if let Some(field) = field_map.get(field_key) {
                    let field_value = record.fields.get(field_key).unwrap();
                    // let mut field_tmp = &field.id;
                    // if record_update.field_key == FieldKeyEnum::NAME {
                    //   field_tmp = &field.name;
                    // }
                    // let mut hm = HashMap::new();
                    // hm.insert("field", field_tmp);

                    let kind = field.kind.clone();
                    // let name = hashmap.get(&kind).unwrap();

                    // validate(field_value, field, hm);
                    let result = self.validate(field_value, field);
                    match result {
                        Ok(_) => {},
                        Err(e) => {
                            return Err(e);
                        }
                    }
                    if [FieldKindSO::SingleSelect, FieldKindSO::MultiSelect].contains(&kind) {
                        let mut exist_options = field.property.as_ref().unwrap().options.clone().unwrap();
                        if field_value.is_null() {
                            fields.insert(field.id.clone(), Value::Null);
                            continue;
                        }
                        let select_field = SelectField{};
                        let mut tmp = Vec::new();
                        if kind == FieldKindSO::SingleSelect {
                            tmp.push(field_value.clone());
                        }else{
                            tmp = field_value.as_array().unwrap().to_vec();
                        }
                        let transformed_option_ids: Vec<String> = tmp.iter().filter(|v| !v.is_null()).map(|option_value| {
                            let option_value = option_value.as_str().unwrap().to_string();
                            let (option, is_created) = select_field.get_or_create_new_option(&FieldOptionValue { name: option_value, color: None }, &exist_options);
                            if is_created {
                                exist_options.push(option.clone());
                                // self.request[DATASHEET_ENRICH_SELECT_FIELD][&field.id] = field;
                            }
                            option.id.clone()
                        }).collect();
                        let value = if kind == FieldKindSO::SingleSelect { 
                            serde_json::to_value(&transformed_option_ids[0].clone()).unwrap()
                        } else { 
                            serde_json::to_value(&transformed_option_ids).unwrap()
                        };
                        fields.insert(field.id.clone(), value);
                    } else {
                        if kind == FieldKindSO::Attachment {
                            // fields.insert(field.id.clone(), field_value.clone());
                            let field_values = field_value.as_array().unwrap();
                            if loader.is_some() {
                                let loader = loader.clone().unwrap();
                                let mut json_value: Vec<Value> = Vec::new();
                                let ids = Vec::new();
                                for value in field_values.iter() {
                                    let value = value.as_object().unwrap();
                                    let token = value.get("token").unwrap().as_str().unwrap();
                                    let asset: IAssetDTO = match loader.get_asset_info(token).await {
                                        Ok(asset) => asset,
                                        Err(e) => {
                                            println!("e: {:?}", e);
                                            return Err(anyhow::anyhow!("The format of the fields parameter value is wrong"));
                                        }
                                    };
                                    let name = value.get("name").unwrap().as_str().unwrap();
                                    let cell_value = AttachmentValue {
                                        id: get_new_id(IDPrefix::File, ids.clone()),
                                        name: name.to_string(),
                                        size: asset.size,
                                        token: token.to_string(),
                                        width: asset.width,
                                        height: asset.height,
                                        bucket: Some(asset.bucket.clone()),
                                        mime_type: asset.mime_type.clone(),
                                        preview: asset.preview.clone(),
                                        ..Default::default()
                                    };
                                    let new_value = serde_json::to_value(&cell_value).unwrap();
                                    json_value.push(new_value);
                                }
                                let json_value = serde_json::to_value(&json_value).unwrap();
                                fields.insert(field.id.clone(), json_value);
                            }
                        }else if kind == FieldKindSO::Member {
                            let mut unit_ids = Vec::new();
                            let values = field_value.as_array().unwrap();
                            values.iter().for_each(|value| {
                                let value = value.as_object().unwrap();
                                let id = value.get("id").unwrap();
                                let id = serde_json::from_value::<String>(id.clone()).unwrap();
                                unit_ids.push(id);
                            });
                            fields.insert(field.id.clone(), serde_json::to_value(&unit_ids).unwrap());
                        }else if kind == FieldKindSO::Link || kind == FieldKindSO::OneWayLink {
                            fields.insert(field.id.clone(), field_value.clone());
                        }else if kind == FieldKindSO::Number || kind == FieldKindSO::Currency || kind == FieldKindSO::Percent 
                            || kind == FieldKindSO::Rating || kind == FieldKindSO::Checkbox || kind == FieldKindSO::DateTime {
                            fields.insert(field.id.clone(), field_value.clone());
                        }else if kind == FieldKindSO::URL && field_value.is_object() {
                            let mut json_value: Vec<Value> = Vec::new();
                            let field_value = field_value.as_object().unwrap();
                            let link = field_value.get("text").unwrap().as_str().unwrap().to_string();
                            let cell_value = UrlValue {
                                link: Some(link.to_string()),
                                text: link,
                                title: Some(field_value.get("title").unwrap().as_str().unwrap().to_string()),
                                r#type: Some(SegmentType::Url as i32),
                                favicon: Some(field_value.get("favicon").unwrap().as_str().unwrap().to_string()),
                                ..Default::default()
                            };
                            json_value.push(serde_json::to_value(&cell_value).unwrap());
                            let json_value = serde_json::to_value(&json_value).unwrap();
                            fields.insert(field.id.clone(), json_value);
                        }else {
                            let mut json_value: Vec<Value> = Vec::new();
                            let str_value = serde_json::from_value::<String>(field_value.clone());
                            match str_value {
                                Ok(str_value) => {
                                    let json_object = json!({
                                        "text": str_value,
                                        "type": SegmentType::Text as i64,
                                    });
                                    json_value.push(json_object);
                                    let json_value = serde_json::to_value(&json_value).unwrap();
                                    fields.insert(field.id.clone(), json_value);
                                },
                                Err(e) => {
                                    println!("kind: {:?}", kind);
                                    println!("field_value: {:?}", field_value);
                                    println!("e: {:?}", e);
                                    return Err(anyhow::anyhow!("The format of the fields parameter value is wrong"));
                                }
                            }
                        }
                    }
                    if kind == FieldKindSO::Link || kind == FieldKindSO::OneWayLink {
                        let _foreign_datasheet_id = field.property.as_ref().unwrap().foreign_datasheet_id.clone();
                        let mut link_record_ids = Vec::new();
                        if let Some(field_value) = fields.get(&field.id) {
                            link_record_ids.extend_from_slice(field_value.as_array().unwrap());
                        }
                    }
                }
            }
            if fields.is_empty() {
                return Err(anyhow::anyhow!("The format of the fields parameter value is wrong"));
                // return Err(Box::new(ApiException::tip_error(ApiTipConstant::ApiParamsInvalidFieldsValue)));
            }
            let mut record = record.clone();
            record.fields = fields;
            records.push(record);
        }
        // if records.len() > API_MAX_MODIFY_RECORD_COUNTS {
        if records.len() > 10 {
            return Err(anyhow::anyhow!("Can't update/delete/create more than 10 records in one single request"));
            // return Err(Box::new(ApiException::tip_error(ApiTipConstant::ApiParamsRecordsMaxCountError { count: API_MAX_MODIFY_RECORD_COUNTS })));
        }
        let mut self_tmp = self.clone();
        self_tmp.records = records;
        Ok(self_tmp)
    }

    pub fn validate(&self, _field_value: &Value, field: &FieldSO
    ) ->anyhow::Result<String> {
        let kind = field.kind.clone();
        match kind {
            FieldKindSO::LookUp => return Err(anyhow::anyhow!("Lookup field can't be edited")),
            FieldKindSO::Formula => return Err(anyhow::anyhow!("Formula field can't be edited")),
            FieldKindSO::AutoNumber => return Err(anyhow::anyhow!("Autonumber field can't be edited")),
            FieldKindSO::CreatedTime => return Err(anyhow::anyhow!("Created Time field can't be edited")),
            FieldKindSO::LastModifiedTime => return Err(anyhow::anyhow!("Last Edited Time field can't be edited")),
            FieldKindSO::CreatedBy => return Err(anyhow::anyhow!("Created By field can't be edited")),
            FieldKindSO::LastModifiedBy => return Err(anyhow::anyhow!("Last Edited By field can't be edited")),
            _ => {},
        }
        // if field.kind == FieldKindSO::LastModifiedBy {
        //     return ;
        // }
        // return Err(anyhow::anyhow!("Can't update/delete/create more than 10 records in one single request"));
        Ok("".to_string())
    }
}