

pub struct ViewPropertyFilter {
    _from_server: Option<bool>,
}

impl ViewPropertyFilter {
    pub const IGNORE_VIEW_PROPERTY: [&str; 5] = ["id", "type", "rows", "name", "lockInfo"];

    pub fn new() -> Self {
        ViewPropertyFilter {
            _from_server: None,
        }
    }
}