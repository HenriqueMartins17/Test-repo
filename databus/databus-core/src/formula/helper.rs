use crate::formula::functions::basic::FormulaParam;

pub fn handle_lookup_null_value(_params: &mut Vec<FormulaParam>) {
  // TODO
}

#[cfg(test)]
pub mod tests {
  use std::collections::HashMap;
  use std::rc::Rc;

  use serde_json::{to_value, Number, Value};

  use crate::fields::property::{
    DateFormat, DateTimeFieldPropertySO, FormulaFieldPropertySO, NumberFieldPropertySO, SelectFieldPropertySO,
    SingleSelectProperty, TimeFormat,
  };
  use crate::formula::evaluate::evaluate;
  use crate::formula::functions::basic::FormulaEvaluateContext;
  use crate::formula::types::{IBaseField, IField};
  use crate::prelude::{
    CellValue, DatasheetMetaSO, DatasheetPackContext, DatasheetPackSO, DatasheetSnapshotSO, NodePermissionStateSO,
    NodeSO, RecordSO, TextValue,
  };

  type TransformFn =
    fn(HashMap<String, IField>, HashMap<String, CellValue>) -> (HashMap<String, IField>, HashMap<String, CellValue>);

  fn default_transform_fn(
    field_map: HashMap<String, IField>,
    mut record_data: HashMap<String, CellValue>,
  ) -> (HashMap<String, IField>, HashMap<String, CellValue>) {
    let field_map = field_map
      .into_iter()
      .map(|(id, field)| {
        // string to text
        let field = if let IField::Text(field) = field {
          if let Some(text) = record_data.get(&id) {
            record_data.insert(
              id.clone(),
              CellValue::from(vec![CellValue::Text(TextValue {
                r#type: 1,
                text: text.to_string(),
              })]),
            );
          }

          IField::Text(field)
        }
        // ignore
        else {
          field
        };

        (id, field)
      })
      .collect();

    (field_map, record_data)
  }

  pub fn test_assert_result(
    expected: CellValue,
    expression: &str,
    record_data: &HashMap<String, CellValue>,
    field_map: &HashMap<String, IField>,
    transform: Option<TransformFn>,
  ) {
    let mut mock_field_map = default_field_map();
    mock_field_map.extend(field_map.clone());
    let record_data = record_data.clone();

    let (mock_field_map, record_data) = if let Some(f) = transform {
      f(mock_field_map, record_data)
    } else {
      default_transform_fn(mock_field_map, record_data)
    };

    let ctx = merge_context(record_data, mock_field_map);
    match evaluate(expression.to_string(), ctx) {
      Ok(v) => assert_eq!(expected, v),
      Err(e) => {
        assert!(false, "error: {}", e.message);
      }
    }
  }

  pub fn test_assert_error(
    expected: &str,
    expression: &str,
    record_data: &HashMap<String, CellValue>,
    field_map: &HashMap<String, IField>,
    transform: Option<TransformFn>,
  ) {
    let mut mock_field_map = default_field_map();
    mock_field_map.extend(field_map.clone());
    let record_data = record_data.clone();

    let (mock_field_map, record_data) = if let Some(f) = transform {
      f(mock_field_map, record_data)
    } else {
      default_transform_fn(mock_field_map, record_data)
    };

    let ctx = merge_context(record_data, mock_field_map);
    match evaluate(expression.to_string(), ctx) {
      Err(e) => {
        assert!(e.message.contains(expected));
      }
      Ok(v) => {
        assert!(false, "value: {:?}", v);
      }
    }
  }

  pub fn mock_text_cell_value(text: &str) -> CellValue {
    CellValue::Array(vec![CellValue::Text(TextValue {
      r#type: 1,
      text: text.to_string(),
    })])
  }

  fn generate_mock_state(field_map: HashMap<String, IField>) -> DatasheetPackContext {
    let field_map = field_map.into_iter().map(|(k, v)| (k.clone(), v.to_so())).collect();

    DatasheetPackContext {
      datasheet_pack: Box::new(DatasheetPackSO {
        snapshot: DatasheetSnapshotSO {
          meta: DatasheetMetaSO {
            field_map,
            views: vec![],
            widget_panels: None,
          },
          record_map: HashMap::new(),
          datasheet_id: "".to_string(),
        },
        datasheet: NodeSO {
          id: "".to_string(),
          name: "".to_string(),
          description: "".to_string(),
          parent_id: "".to_string(),
          icon: "".to_string(),
          node_shared: false,
          node_permit_set: false,
          node_favorite: Some(false),
          space_id: "".to_string(),
          role: "".to_string(),
          permissions: NodePermissionStateSO {
            is_deleted: None,
            permissions: None,
          },
          revision: 0,
          is_ghost_node: None,
          active_view: None,
          extra: None,
        },
        field_permission_map: None,
        foreign_datasheet_map: None,
        units: None,
      }),
      user_info: Default::default(),
      unit_map: HashMap::new(),
      user_map: HashMap::new(),
    }
  }

  /// TODO: optimizate the arguments
  fn merge_context(
    record_data: HashMap<String, CellValue>,
    field_map: HashMap<String, IField>,
  ) -> FormulaEvaluateContext {
    let state = Rc::new(generate_mock_state(field_map));

    fn cell_value_to_value(v: CellValue) -> Value {
      match v {
        CellValue::Null => Value::Null,
        CellValue::Number(v) => Value::Number(Number::from_f64(v).unwrap()),
        CellValue::Bool(v) => Value::Bool(v),
        CellValue::String(v) => Value::String(v),
        CellValue::Array(v) => v.into_iter().map(cell_value_to_value).collect(),
        CellValue::Text(v) => to_value(v).unwrap(),
        CellValue::URL(v) => to_value(v).unwrap(),
        CellValue::Email(v) => to_value(v).unwrap(),
        CellValue::Phone(v) => to_value(v).unwrap(),
        CellValue::Cascader(v) => to_value(v).unwrap(),
        CellValue::Attachment(v) => to_value(v).unwrap(),
        CellValue::LookUpTree(v) => to_value(v).unwrap(),
      }
    }

    let data = record_data
      .into_iter()
      .map(|(k, v)| (k, cell_value_to_value(v)))
      .collect();

    FormulaEvaluateContext {
      state,
      field: Rc::new(IField::Formula(IBaseField {
        id: "formula".to_string(),
        name: "formula".to_string(),
        desc: None,
        required: None,
        property: FormulaFieldPropertySO {
          datasheet_id: "".to_string(),
          expression: "".to_string(),
          formatting: None,
        },
      })),
      record: Rc::new(RecordSO {
        id: "xyz".to_string(),
        comment_count: 0,
        data: Value::Object(data),
        comments: None,
        created_at: None,
        updated_at: None,
        revision_history: None,
        record_meta: None,
      }),
    }
  }

  fn default_field_map() -> HashMap<String, IField> {
    vec![
      (
        "a".to_string(),
        IField::Number(IBaseField {
          id: "a".to_string(),
          name: "a".to_string(),
          desc: None,
          required: None,
          property: NumberFieldPropertySO {
            precision: 0,
            default_value: None,
            comma_style: None,
            symbol: None,
            symbol_align: None,
          },
        }),
      ),
      (
        "b".to_string(),
        IField::Text(IBaseField {
          id: "b".to_string(),
          name: "b".to_string(),
          desc: None,
          required: None,
          property: (),
        }),
      ),
      (
        "c".to_string(),
        IField::DateTime(IBaseField {
          id: "c".to_string(),
          name: "c".to_string(),
          desc: None,
          required: None,
          property: DateTimeFieldPropertySO {
            date_format: DateFormat::SYyyyMmDd,
            time_format: TimeFormat::HHmm,
            include_time: false,
            auto_fill: false,
            time_zone: None,
            include_time_zone: None,
          },
        }),
      ),
      (
        "d".to_string(),
        IField::MultiSelect(IBaseField {
          id: "d".to_string(),
          name: "d".to_string(),
          desc: None,
          required: None,
          property: SelectFieldPropertySO {
            options: vec![
              SingleSelectProperty {
                id: "opt1".to_string(),
                name: "科".to_string(),
                color: Default::default(),
              },
              SingleSelectProperty {
                id: "opt2".to_string(),
                name: "维格".to_string(),
                color: Default::default(),
              },
              SingleSelectProperty {
                id: "opt3".to_string(),
                name: "APITable".to_string(),
                color: Default::default(),
              },
              SingleSelectProperty {
                id: "opt4".to_string(),
                name: "the first".to_string(),
                color: Default::default(),
              },
              SingleSelectProperty {
                id: "opt5".to_string(),
                name: "the second".to_string(),
                color: Default::default(),
              },
              SingleSelectProperty {
                id: "x".to_string(),
                name: "x".to_string(),
                color: Default::default(),
              },
              SingleSelectProperty {
                id: "y".to_string(),
                name: "y".to_string(),
                color: Default::default(),
              },
            ],
          },
        }),
      ),
      (
        "e".to_string(),
        IField::DateTime(IBaseField {
          id: "e".to_string(),
          name: "e".to_string(),
          desc: None,
          required: None,
          property: DateTimeFieldPropertySO {
            date_format: DateFormat::SYyyyMmDd,
            time_format: TimeFormat::HHmm,
            include_time: false,
            auto_fill: false,
            time_zone: None,
            include_time_zone: None,
          },
        }),
      ),
    ]
    .into_iter()
    .collect()
  }
}
