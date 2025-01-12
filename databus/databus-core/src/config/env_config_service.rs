use std::collections::HashMap;
// use std::sync::Mutex;
// use std::env;
use serde::{Serialize, Deserialize};
use databus_shared::env_var;
use serde_json::{to_value, Value};

pub enum EnvConfigKey {
    CONST,
    OSS,
    ApiLimit,
    ACTUATOR,
}

impl EnvConfigKey {
    pub fn to_string(&self) -> String {
        match self {
            EnvConfigKey::CONST => "const".to_string(),
            EnvConfigKey::OSS => "oss".to_string(),
            EnvConfigKey::ApiLimit => "api_limit".to_string(),
            EnvConfigKey::ACTUATOR => "actuator".to_string(),
        }
    }

    pub fn as_str(&self) -> &str {
        match self {
            EnvConfigKey::CONST => "const",
            EnvConfigKey::OSS => "oss",
            EnvConfigKey::ApiLimit => "api_limit",
            EnvConfigKey::ACTUATOR => "actuator",
        }
    }
}

#[derive(Serialize, Deserialize)]
pub struct IServerConfig {
    pub url: String,
    pub transform_limit: usize,
    pub max_view_count: usize,
    pub max_field_count: usize,
    pub max_record_count: usize,
    pub record_remind_range: usize,
}

#[derive(Serialize, Deserialize)]
struct IOssConfig {
    host: String,
    bucket: String,
    oss_signature_enabled: bool,
}

#[derive(Serialize, Deserialize)]
struct IRateLimiter {
    points: u32,
    duration: u32,
    white_list: Option<HashMap<String, IBaseRateLimiter>>,
}

#[derive(Serialize, Deserialize)]
struct IActuatorConfig {
    dns_url: String,
    rss_ratio: u32,
    heap_ratio: u32,
}

#[derive(Serialize, Deserialize)]
struct IBaseRateLimiter {
    // Define the fields of IBaseRateLimiter here
}

pub struct EnvConfigService {
    // config_store: Mutex<HashMap<String, String>>,
    config_store: HashMap<String, Value>,
}

impl EnvConfigService {
    pub fn new() -> Self {
        let mut config_store = HashMap::new();

        let server = IServerConfig {
            // url: env_var!(BACKEND_BASE_URL),
            url: env_var!(BACKEND_BASE_URL default "http://localhost:8081/api/v1/"),
            transform_limit: env_var!(SERVER_TRANSFORM_LIMIT default "100000").parse().expect("Failed to parse string to usize"),
            max_view_count: env_var!(SERVER_MAX_VIEW_COUNT default "30").parse().expect("Failed to parse string to usize"),
            max_field_count: env_var!(SERVER_MAX_FIELD_COUNT default "200").parse().expect("Failed to parse string to usize"),
            max_record_count: env_var!(SERVER_MAX_RECORD_COUNT default "50000").parse().expect("Failed to parse string to usize"),
            record_remind_range: env_var!(SERVER_RECORD_REMIND_RANGE default "90").parse().expect("Failed to parse string to usize"),
        };
        config_store.insert(EnvConfigKey::CONST.to_string(), to_value(&server).unwrap());

        // Similar code for oss, limit, and actuator

        EnvConfigService {
            config_store
            // config_store: Mutex::new(config_store),
        }
    }

    pub fn get_room_config(&self, key: EnvConfigKey) -> Option<Value> {
        let key = key.as_str();
        // let config_store = self.config_store.lock().unwrap();
        self.config_store.get(key).cloned()
    }
}

// impl Drop for EnvConfigService {
//     fn drop(&mut self) {
//         self.config_store.lock().unwrap().clear();
//     }
// }