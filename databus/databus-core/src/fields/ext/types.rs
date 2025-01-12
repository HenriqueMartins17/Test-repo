use crate::so::FieldKindSO;


pub struct CellToStringOption {
  pub datasheet_id: Option<String>,
  pub hide_unit: Option<bool>,
  pub order_in_cell_value_sensitive: Option<bool>,


}

impl CellToStringOption {
  pub fn new(datasheet_id: Option<String>, hide_unit: Option<bool>, order_in_cell_value_sensitive: Option<bool>) -> Self {
    Self { datasheet_id, hide_unit, order_in_cell_value_sensitive }
  }
}

pub(crate) fn is_text_base_type(type_: FieldKindSO) -> bool {
  [FieldKindSO::Text, FieldKindSO::Phone, FieldKindSO::Email, FieldKindSO::URL, FieldKindSO::SingleText].contains(&type_)
}