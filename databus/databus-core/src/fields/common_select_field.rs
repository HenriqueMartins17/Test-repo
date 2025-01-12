use serde_json::{Value, to_value};
use serde::{Deserialize, Serialize};
use crate::utils::uuid::{IDPrefix, get_new_id};

use super::property::{SingleSelectProperty, FieldPropertySO};

#[derive(Debug, Deserialize, Serialize)]
pub struct FieldOptionValue{
    pub name: String,
    pub color: Option<i32>,
}

pub struct SelectField {

}

impl SelectField {
    //SelectFieldOption->SingleSelectProperty
    pub fn get_or_create_new_option(&self, option: &FieldOptionValue, exist_options: &Vec<SingleSelectProperty>) -> (SingleSelectProperty, bool) {
        // get exist
        if let Some(opt) = self.get_option(option, exist_options) {
            return (opt, false);
        }
        // create not exist
        let new_option = self.create_new_option(option, exist_options);
        (new_option, true)
    }
    
    fn get_option(&self, option: &FieldOptionValue, exist_options: &Vec<SingleSelectProperty>) -> Option<SingleSelectProperty> {
        for opt in exist_options {
            if opt.color != Value::Null && opt.name == option.name {
                if let Some(color) = &option.color {
                    if &opt.color == color {
                        return Some(opt.clone());
                    }
                }
            } 
            if opt.name == option.name {
                return Some(opt.clone());
            }
        }
        None
    }
    
    fn create_new_option(&self, option: &FieldOptionValue, exist_options: &Vec<SingleSelectProperty>) -> SingleSelectProperty {
        let option_id = get_new_id(IDPrefix::Option, exist_options.iter().map(|op| op.id.clone()).collect());
        let new_color = option.color.clone().unwrap_or(get_option_color(exist_options.iter().map(|op| op.color.clone()).collect()));
        SingleSelectProperty {
            id: option_id,
            color: new_color.to_owned(),
            name: option.name.to_string(),
        }
    }

    pub fn update_open_field_property_transform_property(&self, open_field_property: FieldPropertySO, default_property: FieldPropertySO) -> FieldPropertySO{
        let mut new_options: Vec<SingleSelectProperty> = Vec::new();
        let mut transformed_default_value = open_field_property.default_value;
        let transformed_options: Vec<SingleSelectProperty> = open_field_property.options.unwrap_or_default().iter().map(|option| {
            println!("option: {:?}", option);
            if option.id.is_empty() || option.color == 0 {
                let color = Some(option.color);
                let option = FieldOptionValue{name:option.name.clone(), color};
                let mut exist_options = default_property.options.clone().unwrap_or_default();
                exist_options.extend(new_options.clone());
                let new_option = self.create_new_option(&option, &exist_options);
                transformed_default_value = self.transform_default_value(&new_option, &transformed_default_value);
                new_options.push(new_option.clone());
                new_option
            } else {
                SingleSelectProperty {
                    id: option.id.clone(),
                    name: option.name.clone(),
                    color: option.color.clone(),
                    // color: self.get_option_color_number_by_name(&option.color),
                }
            }
        }).collect();
        FieldPropertySO {
            default_value: transformed_default_value,
            options: Some(transformed_options),
            ..Default::default()
        }
    }

    fn transform_default_value(&self, option: &SingleSelectProperty, default_value: &Option<Value>) -> Option<Value> {
        if self.match_single_select_name(&option.name, default_value) {
           return Some(to_value(option.id.clone()).unwrap());
        };
        if default_value.is_some() {
            let value = default_value.clone().unwrap();
            if value.is_array() {
                let mut value = value.as_array().unwrap().clone();
                let idx = value.iter().position(|v| v.as_str().unwrap() == &option.name);
                if idx.is_some() {
                    value[idx.unwrap()] = to_value(option.id.clone()).unwrap();
                    return Some(to_value(value).unwrap());
                }
            }
        }
        default_value.clone()
    }

    fn match_single_select_name(&self, name: &str, default_value: &Option<Value>) -> bool {
        match default_value {
            Some(Value::String(default_name)) => name == default_name,
            _ => false,
        }
    }
}

// TODO: wait for PRD for specific logic
fn get_option_color(colors: Vec<i32>) -> i32 {
    if colors.len() < 10 {
        let diff_colors: Vec<i32> = (0..10).filter(|&c| !colors.contains(&c)).collect();
        return diff_colors[0];
    }
    (colors.len() % 10) as i32
}

// pub fn get_option_color_number_by_name(&self, name: &str) -> Option<usize> {
//     let color_names = get_color_names();
//     match color_names.iter().position(|color_name| color_name == name) {
//         Some(color_num) => Some(color_num),
//         None => None,
//     }
// }
