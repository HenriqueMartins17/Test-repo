use serde::{Deserialize, Serialize};

#[derive(Debug, Deserialize, Serialize)]
pub struct ApiPage<T> {
    #[serde(rename = "pageNum")]
    pub page_num: u32,

    pub records: T,

    #[serde(rename = "pageSize")]
    pub page_size: u32,

    pub total: u32,
}
