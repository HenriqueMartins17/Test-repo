
#[cfg(test)]
mod tests {

    use std::rc::Rc;

    use crate::{mock::{mock_changesets_of_delete_link_field_in_dst2, mock_ops_collects_of_add_one_default_record_in_dst1, mock_ops_collects_of_delete_link_field_in_dst2, get_context_with_foreign}, ot::{commands::resource_ops_to_changesets, changeset::LocalChangeset, types::ResourceType}};

    fn mock_changesets_of_add_one_default_record_in_dst1(record_id: &str) -> Vec<LocalChangeset> {
        mock_ops_collects_of_add_one_default_record_in_dst1(record_id)
            .into_iter()
            .map(|ops| LocalChangeset {
                base_revision: 1,
                message_id: "x".to_string(),
                resource_id: "dst1".to_string(),
                resource_type: ResourceType::Datasheet,
                operations: ops.operations,
            })
            .collect()
    }
        
    #[test]
    fn convert_op_in_single_datasheet(){
        let mut context = get_context_with_foreign(2, 3, 4); //dst1 dst2 dst3
        let context = Rc::get_mut(&mut context).unwrap();
        let mut changesets = resource_ops_to_changesets(mock_ops_collects_of_add_one_default_record_in_dst1("rec10"), context);

        for changeset in &mut changesets {
            changeset.message_id = "x".to_string();
        }

        assert_eq!(changesets, mock_changesets_of_add_one_default_record_in_dst1("rec10"));
    }
    
    #[test]
    fn convert_multiple_ops_in_single_datasheet(){
        let mut context = get_context_with_foreign(2, 3, 4); //dst1 dst2 dst3
        let context = Rc::get_mut(&mut context).unwrap();
        let mut resource_ops_collects = Vec::new();
        resource_ops_collects.extend(mock_ops_collects_of_add_one_default_record_in_dst1("rec10"));
        resource_ops_collects.extend(mock_ops_collects_of_add_one_default_record_in_dst1("rec11"));
        let mut changesets = resource_ops_to_changesets(
            resource_ops_collects,
            context
        );

        for changeset in &mut changesets {
            changeset.message_id = "x".to_string();
        }

        let mut expected = mock_changesets_of_add_one_default_record_in_dst1("rec10");
        expected[0].operations.append(&mut mock_changesets_of_add_one_default_record_in_dst1("rec11")[0].operations);

        assert_eq!(changesets, expected);
    }

    #[test]
    fn convert_ops_in_multiple_datasheets() {
        let mut context = get_context_with_foreign(2, 3, 4); //dst1 dst2 dst3
        let context = Rc::get_mut(&mut context).unwrap();
        let mut changesets = resource_ops_to_changesets(mock_ops_collects_of_delete_link_field_in_dst2(), context);

        for changeset in &mut changesets {
            changeset.message_id = "x".to_string();
        }

        assert_eq!(changesets, mock_changesets_of_delete_link_field_in_dst2());
    }
}
