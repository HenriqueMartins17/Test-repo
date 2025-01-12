use std::rc::Rc;

use crate::so::{FieldSO, DatasheetPackContext, FieldKindSO};


pub struct FieldLogic {
    field: FieldSO,
    // store: Arc<Mutex<Store>>,
    context: Rc<DatasheetPackContext>,
}

impl FieldLogic {
    pub fn new(
        field: FieldSO, 
        // store: Arc<Mutex<Store>>
        context: Rc<DatasheetPackContext>,
    ) -> Self {
        Self { field, context }
    }

    pub fn id(&self) -> &str {
        &self.field.id
    }

    pub fn name(&self) -> &str {
        &self.field.name
    }

    pub fn kind(&self) -> &FieldKindSO {
        &self.field.kind
    }

    pub fn get_view_object<R, F>(&self, transform: F) -> R
    where
        F: Fn(&FieldSO, FieldVoTransformOptions) -> R,
    {
        // let state = self.store.lock().unwrap().get_state();
        let context = self.context.clone();
        transform(&self.field, FieldVoTransformOptions { _context: context })
    }
}

pub struct FieldVoTransformOptions {
    // state: ReduxState,
    _context: Rc<DatasheetPackContext>,
}
