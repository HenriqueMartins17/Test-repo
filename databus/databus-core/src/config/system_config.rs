use serde::{Deserialize, Serialize};
use serde_json::{Value, from_str};
use std::collections::HashMap;
use std::fs;
use once_cell::sync::Lazy;
use std::env;

#[derive(Serialize, Deserialize)]
struct APITipConfigInterface {
    // Define the fields of the Tips struct here
    api: API
}

#[derive(Serialize, Deserialize)]
struct API {
    // Define the fields of the Tips struct here
    tips: HashMap<String, Value>
}

fn load_api_tip_constants() -> APITipConfigInterface {
    let path = env::current_dir().unwrap();
    let path = path.to_str().unwrap().replace("databus-server", "databus-core") + "/api_tip_config.source.json";
    let api_tip_config: String = fs::read_to_string(path)
        .expect("Could not read the JSON file");
    let api_tip_constant: APITipConfigInterface = from_str(&api_tip_config)
        .expect("Could not deserialize the JSON into a HashMap");
    api_tip_constant
}

static API_TIP_CONSTANT: Lazy<HashMap<String, Value>> = Lazy::new(|| {
    load_api_tip_constants().api.tips
});

//str_err like "api_params_invalid_value=type|value Number"
pub fn get_err_info(str_err: String) -> anyhow::Result<(String, u16)> {
    let str_err = str_err.replace("\\", "").replace("\"", "");
    let str_map = str_err.split("=").collect::<Vec<&str>>();
    let (str_err, extra) = if str_map.len() == 2 {
        (str_map[0].to_string(), Some(str_map[1].to_string()))
    } else {
        (str_err, None)
    };
    let value = API_TIP_CONSTANT.get(&str_err);
    match value {
        Some(v) => {
            let v_obj = v.as_object().unwrap();
            let mut message = v_obj.get("message").unwrap().as_str().unwrap().to_string();
            if extra.is_some() {
                let extra = extra.unwrap();
                let vec_extra = extra.split("|").collect::<Vec<&str>>();
                let vec_str = get_replace_strs(&message);
                let mut idx = 0;
                for str in &vec_str {
                    message = message.replacen(str, vec_extra[idx], 1);
                    idx += 1;
                }
            }
            let code = v_obj.get("code").unwrap().as_u64().unwrap() as u16;
            Ok((message, code))
        }
        None => {
            println!("not found str_err {}", str_err);
            Err(anyhow::anyhow!(format!("not found str_err {}", str_err)))
        }
    
    }
}

fn get_replace_strs(str: &str) -> Vec<String> {
    let mut strs = Vec::new();
    let mut start = 0;
    let mut end = 0;
    let mut is_start = false;
    for (i, c) in str.chars().enumerate() {
        if c == '{' {
            is_start = true;
            start = i;
        }
        if c == '}' && is_start {
            end = i;
            is_start = false;
            strs.push(str[start..end + 1].to_string());
        }
    }
    strs
}