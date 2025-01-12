use std::sync::Arc;

use serde::{Serialize, Deserialize};
use serde_json::{Value, from_value};
use utoipa::ToSchema;

use crate::{ot::commands::AddFieldOptions, fields::{get_field_type_by_string, validate_add_open_field_property, property::FieldPropertySO, add_open_field_property_transform_property, can_group, has_error, accept_filter_operators}, so::{FieldKindSO, FieldSO, DatasheetMetaSO}, data_source_provider::IDataSourceProvider};

#[derive(Debug, Deserialize, Serialize, ToSchema, Clone)]
pub struct FieldCreateRo {
    pub name: String,
    pub r#type: String,
    pub property: Option<Value>,
}

impl FieldCreateRo {
    pub fn new(name: String, r#type: String) -> Self {
        Self {
            name,
            r#type,
            property: None,
        }
    }

    pub async fn transform(
        self, 
        loader: Option<Arc<dyn IDataSourceProvider>>,
        dst_id: &str,
    ) -> anyhow::Result<FieldCreateRo> {
        match self.clone().validate(loader, dst_id).await {
            Ok(_) => {}
            Err(e) => {
                return Err(e);
            }
        }
        let mut vec = Vec::new();
        vec.push(self.clone());
        let _fields = self.transform_property(vec);
        Ok(self)
    }

    pub fn transform_property(&self, mut fields: Vec<FieldCreateRo>) -> Vec<FieldCreateRo> {
        for field in &mut fields {
            match field.r#type.as_str() {
                "Number" => (),
                // APIMetaFieldKindSO::Number => self.transform_number_property(field),
                _ => (),
            }
        }
        fields
    }

    pub async fn validate(
        self,
        loader: Option<Arc<dyn IDataSourceProvider>>,
        dst_id: &str,
    ) -> anyhow::Result<bool> {
        if self.name.is_empty() {
            return Err(anyhow::Error::msg("api_params_invalid_value=name"));
        }
        if self.name.len() > 100 {
            return Err(anyhow::Error::msg("api_params_max_length_error=name|100"));
        }
        let field_type = get_field_type_by_string(&self.r#type);
        if field_type == FieldKindSO::NotSupport {
            let str_err = format!("api_params_invalid_value=type {}", self.r#type);
            return Err(anyhow::Error::msg(str_err));
        }
        match validate_add_open_field_property(self.property.clone().unwrap_or(Value::Null), field_type.clone()) {
            Ok(_) => {}
            Err(e) => {
                return Err(e);
            }
        }
        if field_type == FieldKindSO::LookUp {
            self.validate_look_up_field(loader, dst_id).await?;
        }
        Ok(true)
    }

    async fn validate_look_up_field(
        self,
        loader: Option<Arc<dyn IDataSourceProvider>>,
        dst_id: &str,
    ) -> anyhow::Result<()> {
        let loader = loader.unwrap();
        let field_map = match loader.get_field_map_by_dst_id(dst_id).await {
            Ok(value) => value,
            Err(e) => return Err(e),
        };
        let property = from_value(self.property.clone().unwrap_or(Value::Null)).unwrap_or(FieldPropertySO::default());
        let related_link_field_id = property.related_link_field_id.clone().unwrap();
        let link_field = field_map.get(&related_link_field_id);
        if link_field.is_none() {
            return Err(anyhow::Error::msg(format!("api_params_lookup_related_link_field_not_exists={}", related_link_field_id)));
        }
        let link_field = link_field.unwrap();
        let kind = link_field.kind.clone();
        if kind != FieldKindSO::Link && kind != FieldKindSO::OneWayLink {
            return Err(anyhow::Error::msg(format!("api_params_lookup_related_field_not_link={}", related_link_field_id)));
        }
        let foreign_datasheet_id = link_field.property.clone().unwrap().foreign_datasheet_id.unwrap();
        let is_self_link: bool = dst_id == &foreign_datasheet_id;
        let look_up_target_field_id = property.look_up_target_field_id.clone().unwrap();
        println!("look_up_target_field_id: {}", look_up_target_field_id);
        let target_field = if is_self_link {
            field_map.contains_key(&look_up_target_field_id)
        } else {
            let kind = loader.select_field_type_by_fld_id_and_dst_id(&look_up_target_field_id, &foreign_datasheet_id).await;
            match kind {
                Ok(value) => value.is_some(),
                Err(e) => return Err(e),
            }
        };
        
        if !target_field {
            return Err(anyhow::Error::msg(format!("api_params_lookup_target_field_not_exists={}", look_up_target_field_id)));
        }
        
        if let Some(sort_info) = &property.sort_info {
            for rule in &sort_info.rules {
                let sort_field_id = &rule.field_id;
                let sort_field = if is_self_link {
                    field_map.get(sort_field_id).cloned()
                } else {
                    loader.get_field_by_fld_id_and_dst_id(&foreign_datasheet_id, &sort_field_id).await.unwrap()
                };
        
                match sort_field {
                    Some(sort_field) => {
                        let kind = sort_field.kind.clone();
                        if kind != FieldKindSO::LookUp {
                            if !can_group(kind.clone()) || has_error(kind) {
                                return Err(anyhow::Error::msg(format!("api_params_lookup_field_can_not_sort={}", sort_field_id)));
                            }
                        }
                    },
                    None => return Err(anyhow::Error::msg(format!("api_params_lookup_sort_field_not_exists={}", sort_field_id))),
                }
            }
        }
        
        if let Some(filter_info) = &property.filter_info {
            if !filter_info.conditions.is_empty() {
                let foreign_field_map = if is_self_link {
                    field_map
                } else {
                    match loader.get_field_map_by_dst_id(&foreign_datasheet_id).await {
                        Ok(value) => value,
                        Err(e) => return Err(e),
                    }
                };
        
                for condition in &filter_info.conditions {
                    let filter_field = foreign_field_map.get(&condition.field_id);
        
                    match filter_field {
                        Some(filter_field) => {
                            // let field_type = filter_field.kind.clone();
                            if ![FieldKindSO::Formula, FieldKindSO::LookUp].contains(&filter_field.kind) {
                                if !accept_filter_operators(filter_field.kind.clone()).contains(&condition.operator) {
                                    return Err(anyhow::Error::msg(format!("api_params_lookup_field_can_not_filter={}|{}", condition.field_id, condition.operator.to_string())));
                                }
                            }
                        },
                        None => return Err(anyhow::Error::msg(format!("api_params_lookup_filter_field_not_exists={}", condition.field_id))),
                    }
                }
            }
        }
        Ok(())
    }

    pub fn transfer_to_command_data(&self) -> AddFieldOptions {
        let field_type = get_field_type_by_string(&self.r#type);
        let add_property = from_value(self.property.clone().unwrap_or(Value::Null)).unwrap_or(FieldPropertySO::default());
        let property = add_open_field_property_transform_property(add_property, field_type);
        AddFieldOptions {
            data: FieldSO {
                name: self.name.clone(),
                kind: field_type,
                property: Some(property),
                ..Default::default()
            },
            index: 0,
            ..Default::default()
        }
    }

    pub fn foreign_datasheet_id(&self) -> Option<String> {
        let field_type = get_field_type_by_string(&self.r#type);
        if (field_type == FieldKindSO::Link || field_type == FieldKindSO::OneWayLink) && self.property.is_some() {
            return self.property.clone().unwrap().as_object().unwrap().get("foreignDatasheetId").unwrap().as_str().map(|s| s.to_string());
        }
        None
    }
}