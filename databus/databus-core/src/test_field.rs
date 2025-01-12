
#[cfg(test)]
mod tests {
    use std::rc::Rc;

    use serde_json::to_value;

    use crate::{so::{DatasheetPackContext, prepare_context_data, FieldSO, FieldKindSO}, mock::{get_datasheet_map_pack, get_datasheet_pack, get_datasheet}, logic::FieldsOptions, fields::property::{FieldPropertySO, SingleSelectProperty}};

    fn get_context() -> Rc<DatasheetPackContext>{
        let base_datasheet_pack = get_datasheet_map_pack(1).unwrap();
        let snapshot = base_datasheet_pack.snapshot;
        let mut datasheet_pack = get_datasheet_pack().unwrap();
        datasheet_pack.snapshot = snapshot.clone();
        let context = prepare_context_data(Box::new(datasheet_pack));
        let context = Rc::new(context);
        context
    }

    #[test]
    fn basic_field_info() {
        let mut dst1 = get_datasheet(get_context());
        let view1 = dst1.get_view("viw1");
        assert!(view1.is_some());
        let view1 = view1.unwrap();
        assert_eq!(view1.id(), "viw1");

        let fields = view1.get_fields(FieldsOptions{..Default::default()});

        let field_data = fields.iter().map(|field| FieldSO { id: field.id().to_string(), name: field.name().to_string(), kind: field.kind().clone(),
        ..Default::default() }).collect::<Vec<_>>();

        assert_eq!(field_data, vec![
            FieldSO {
                id: "fld1".to_string(),
                name: "field 1".to_string(),
                kind: FieldKindSO::Text,
                ..Default::default()
            },
            FieldSO {
                id: "fld2".to_string(),
                name: "field 2".to_string(),
                kind: FieldKindSO::MultiSelect,
                ..Default::default()
            },
        ]);
    }

    #[test]
    fn get_view_object() {
        let mut dst1 = get_datasheet(get_context());
        let view1 = dst1.get_view("viw1");
        assert!(view1.is_some());
        let view1 = view1.unwrap();
        assert_eq!(view1.id(), "viw1");

        let fields = view1.get_fields(FieldsOptions::default());

        assert!(fields.len() > 1);

        let field = fields[1].get_view_object(|x, _| x.clone());
        assert_eq!(field, FieldSO {
            id: "fld2".to_string(),
            name: "field 2".to_string(),
            kind: FieldKindSO::MultiSelect,
            property: Some(FieldPropertySO {
                options: Some(vec![
                    SingleSelectProperty { color: 0, id: "opt1".to_string(), name: "option 1".to_string() },
                    SingleSelectProperty { color: 1, id: "opt2".to_string(), name: "option 2".to_string() },
                    SingleSelectProperty { color: 2, id: "opt3".to_string(), name: "option 3".to_string() },
                ]),
                default_value: Some(to_value(vec!["opt2".to_string(), "opt1".to_string()]).unwrap()),
                ..Default::default()
            }),
            ..Default::default()
        });
    }
}
