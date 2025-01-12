use std::collections::HashMap;

use serde::{Deserialize, Serialize};

use crate::{so::{ViewSO, FieldSO, types::ViewType, ViewRowSO, ViewColumnSO, CellValueSo}, Datasheet, ot::commands::{AddRecordsOptions, SaveOptions, CollaCommandExecuteResult, ModifyView}};

use super::{IRecordVoTransformOptions, RecordLogic, IRecordOptions, FieldLogic};

#[derive(Debug, Default)]
pub struct IAddRecordsOptions {
    pub index: usize,
    pub count: usize,
    pub group_cell_values: Option<Vec<CellValueSo>>,
    pub ignore_field_permission: Option<bool>,
    pub view_id: String,
    pub record_values: Option<Vec<HashMap<String, CellValueSo>>>,
}

#[derive(Debug, Deserialize, Serialize, Default)]
#[serde(rename_all = "camelCase")]
pub struct IRecordsOptions {
    /// Maximum number of records retrieved. The retrieved list of records is trimmed before pagination.
    /// The default value is None, which means no limit.
    pub max_records: Option<usize>,
  
    pub pagination: Option<Pagination>,
}

#[derive(Debug, Deserialize, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct Pagination {
    /// 1-based page number. If the page number exceeds maximum pages, an empty list of records is returned.
    pub page_num: i32,

    /// The maximum number of records in a single page.
    pub page_size: i32,
}

// pub struct IViewOptions {
//     pub get_view_info: Box<dyn Fn() -> Option<IViewInfo>>,
// }

#[derive(Debug, Deserialize, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct IViewInfo {
    pub property: ViewSO,
    pub field_map: HashMap<String, FieldSO>,
}

#[derive(Debug, Deserialize, Serialize, Default)]
#[serde(rename_all = "camelCase")]
pub struct FieldsOptions {
    /// If hidden fields are included in the returned field list. Defaults to false.
    pub include_hidden: Option<bool>,
}

pub struct ViewLogic<'a> {
    datasheet: &'a mut Datasheet,
    // store: Store<IReduxState>,
    // context: Rc<DatasheetPackContext>,
    property: ViewSO,
    field_map: HashMap<String, FieldSO>,
}

impl <'a> ViewLogic<'a>  {
    /// Create a `View` instance from `IViewInfo`.
    ///
    /// This constructor is not intended for public use.
    pub fn new(
        datasheet: &'a mut Datasheet, 
        // store: Store<IReduxState>, 
        // context: Rc<DatasheetPackContext>,
        info: IViewInfo
    ) -> Self {
        let IViewInfo { property, field_map } = info;

        Self {
            datasheet,
            // store,
            // context,
            property,
            field_map,
        }
    }

    pub fn id(&self) -> &str {
        self.property.id.as_ref().unwrap()
    }

    pub fn name(&self) -> &str {
        self.property.name.as_ref().unwrap()
    }

    pub fn view_type(&self) -> ViewType {
        let view_type = self.property.r#type.as_ref().unwrap();
        match view_type {
            0 => ViewType::NotSupport,
            1 => ViewType::Grid,
            2 => ViewType::Kanban,
            3 => ViewType::Gallery,
            4 => ViewType::Form,
            5 => ViewType::Calendar,
            6 => ViewType::Gantt,
            7 => ViewType::OrgChart,
            _ => ViewType::NotSupport,
        }
    }

    pub fn rows(&self) -> Vec<ViewRowSO> {
        self.property.rows.as_ref().unwrap().clone()
    }

    pub fn columns(&self) -> Vec<ViewColumnSO> {
        self.property.columns.clone()
    }

    /**
     * Get all fields in the view. Hidden fields are not included by default.
     */
    pub fn get_fields(&self, options: FieldsOptions) -> Vec<FieldLogic> {
        let mut fields = Vec::new();
        for column in self.columns() {
            if let Some(field) = self.field_map.get(&column.field_id) {
                if options.include_hidden.is_some() && options.include_hidden.clone().unwrap() || 
                (column.hidden.is_none() || column.hidden.clone().unwrap() == false) {
                    fields.push(FieldLogic::new(field.clone(), self.datasheet.context.clone()));
                }
            }
        }
        fields
    }

    /// get view index, begin with 0.
    ///
    /// @return index of view
    pub fn index(&self) -> usize {
        // Selectors::get_view_index(&self.datasheet.snapshot, &self.id())
        self.datasheet.context.datasheet_pack.snapshot.get_view_index(self.id()).unwrap()
    }

    pub fn get_records(&self, options: IRecordsOptions) -> Vec<RecordLogic> {
        // let snapshot = Selectors::get_snapshot(self.store.get_state());
        let snapshot = self.datasheet.context.datasheet_pack.snapshot.clone();
        // if snapshot.is_none() {
        //     return vec![];
        // }
    
        let IRecordsOptions {pagination, max_records}= options;
    
        // Pagination
        let mut page_rows = self.rows().clone();
        if let Some(max) = max_records {
            if max < self.rows().len() {
                page_rows = self.rows()[0..max].to_vec();
            }
        }
    
        if let Some(p) = pagination {
            let start = (p.page_num - 1) * p.page_size;
            let end = start + p.page_size;
            let start = start as usize;
            let mut end = end as usize;
            page_rows = if p.page_size == -1 { page_rows } else { 
                if start > page_rows.len() {
                    return vec![];
                }
                if end > page_rows.len() {
                    end = page_rows.len();
                }
                page_rows[start..end].to_vec() 
            };
        }
    
        if page_rows.is_empty() {
            return vec![];
        }
    
        let record_map = snapshot.record_map.clone();
        let field_keys = self.field_map.keys().into_iter().map(|k| k.clone()).collect::<Vec<_>>();
        // let column_map = key_by(&self.columns(), "fieldId");
        let column_map = self.columns().iter().map(|column| (column.field_id.clone(), column.clone())).collect::<HashMap<_, _>>();
        let vo_transform_options = IRecordVoTransformOptions {
            field_map: self.field_map.clone(),
            // store: &self.store,
            field_keys,
            column_map,
        };
    
        let mut records: Vec<RecordLogic> = vec![];
        for row in page_rows {
            if let Some(record) = record_map.get(&row.record_id) {
                records.push(
                    RecordLogic::new(record.clone(), IRecordOptions {vo_transform_options: vo_transform_options.clone()}),
                );
            }
        }
        records
    }

    /**
     * Add records to the datasheet via this view.
     *
     * @param options Options for adding records.
     * @param saveOptions Options for the data saver.
     * @return If the command execution succeeded, the `data` field of the return value is an array of record IDs.
     */
    pub async fn add_records(&mut self, record_options: IAddRecordsOptions, save_options: SaveOptions) -> anyhow::Result<CollaCommandExecuteResult> {
        let mut record_options = record_options;
        record_options.view_id = self.id().to_string();
        self.datasheet.add_records(record_options, save_options).await
    }

    /// Delete this view.
    ///
    /// # Arguments
    ///
    /// * `save_options` - The options that will be passed to the data saver.
    pub async fn delete(&mut self, save_options: SaveOptions) -> anyhow::Result<CollaCommandExecuteResult> {
        self.datasheet.delete_views(vec![self.id().to_string()], save_options).await
    }

    /// Modify view property.
    ///
    /// # Arguments
    ///
    /// * `view` - The view info to be modified.
    /// * `save_options` - The options that will be passed to the data saver.
    pub async fn modify(&mut self, view: ModifyView, save_options: SaveOptions) -> anyhow::Result<CollaCommandExecuteResult> {
        let view = ModifyView {
            view_id: self.id().to_string(),
            ..view
        };
        self.datasheet.modify_views(vec![view], save_options).await
    }
}
