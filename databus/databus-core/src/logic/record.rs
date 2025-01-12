use std::collections::HashMap;

use serde::{Deserialize, Serialize};

use crate::so::{RecordSO, Comments, FieldSO, ViewColumnSO};

#[derive(Deserialize, Serialize, Debug, Default, Clone, PartialEq)]
pub enum CellFormatEnum {
    String,
    #[default]
    Json,
}

pub struct RecordLogic {
    record: RecordSO,
    vo_transform_options: IRecordVoTransformOptions,
}

impl RecordLogic {
    /// Create a `Record` instance from an `RecordSO` object.
    ///
    /// This constructor is not intended for public use.
    pub fn new(record: RecordSO, options: IRecordOptions) -> Self {
        let IRecordOptions { vo_transform_options } = options;

        Self {
            record,
            vo_transform_options,
        }
    }

    pub fn id(&self) -> &str {
        &self.record.id
    }

    /// The comment list of the record. If no comments exist, an empty array is returned.
    pub fn comments(&self) -> Vec<Comments> {
        if self.record.comments.is_none(){
            return vec![];
        };
        self.record.comments.as_ref().unwrap().clone()
    }

    /// Get the view object of the record via `transform`.
    pub fn get_view_object<R, F>(&self, transform: F) -> R
    where
        F: Fn(RecordSO, IRecordVoTransformOptions) -> R,
    {
        transform(self.record.clone(), self.vo_transform_options.clone())
    }

    pub fn get_vo_transform_options(&self) -> &IRecordVoTransformOptions {
        &self.vo_transform_options
    }
}

#[derive(Debug, Deserialize, Serialize)]
#[serde(rename_all = "camelCase")]
/// The options for creating a `Record` instance.
pub struct IRecordOptions {
    pub vo_transform_options: IRecordVoTransformOptions,
}

#[derive(Debug, Deserialize, Serialize, Clone)]
#[serde(rename_all = "camelCase")]
/// The options for the record view object transformer function.
pub struct IRecordVoTransformOptions {
    pub field_map: HashMap<String, FieldSO>,
    // store: Store<IReduxState>,
    pub field_keys: Vec<String>,
    pub column_map: HashMap<String, ViewColumnSO>,
}
