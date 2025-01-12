#[cfg(test)]
#[cfg(feature = "integration_tests")]
mod tests {
  fn assert_is_not_404(e: anyhow::Error) {
    // api 404
    assert!(e.to_string().find("404").is_none(), "error reason: {}", e.to_string());
    // gateway 502
    assert!(e.to_string().find("502").is_none(), "error reason: {}", e.to_string());
  }

  /// Datasheet
  mod datasheet {
    use crate::datasheet::datasheet::Datasheet;
    use crate::test_utils::utils::create_datasheet_manager_v3;
    use crate::tests::v3_404::tests::assert_is_not_404;

    fn manager() -> Datasheet {
      create_datasheet_manager_v3()
    }

    #[test]
    fn test_create_datasheet() {
      match tokio_test::block_on(manager().create()) {
        Ok(_) => {}
        Err(e) => {
          assert_is_not_404(e);
        }
      }
    }

    #[test]
    fn test_get_presigned_url() {
      match tokio_test::block_on(manager().get_presigned_url()) {
        Ok(_) => {}
        Err(e) => {
          assert_is_not_404(e);
        }
      }
    }

    #[test]
    fn test_get_spaces() {
      match tokio_test::block_on(manager().get_spaces()) {
        Ok(_) => {}
        Err(e) => {
          assert_is_not_404(e);
        }
      }
    }

    #[test]
    fn test_get_nodes() {
      match tokio_test::block_on(manager().get_nodes()) {
        Ok(_) => {}
        Err(e) => {
          assert_is_not_404(e);
        }
      }
    }

    #[test]
    fn test_node_detail() {
      match tokio_test::block_on(manager().node_detail()) {
        Ok(_) => {}
        Err(e) => {
          assert_is_not_404(e);
        }
      }
    }

    #[test]
    fn test_execute_command() {
      match tokio_test::block_on(manager().execute_command()) {
        Ok(_) => {}
        Err(e) => {
          assert_is_not_404(e);
        }
      }
    }
  }


  /// Fields
  mod fields {
    use crate::datasheet::field::FieldManager;
    use crate::test_utils::utils::create_field_manager_v3;
    use crate::tests::v3_404::tests::assert_is_not_404;

    fn manager() -> FieldManager {
      create_field_manager_v3()
    }

    #[test]
    fn test_get_fields() {
      match tokio_test::block_on(manager().get()) {
        Ok(_) => {}
        Err(e) => {
          assert_is_not_404(e);
        }
      }
    }

    #[test]
    fn test_create_fields() {
      match tokio_test::block_on(manager().create()) {
        Ok(_) => {}
        Err(e) => {
          assert_is_not_404(e);
        }
      }
    }

    #[test]
    fn test_delete_fields() {
      match tokio_test::block_on(manager().delete()) {
        Ok(_) => {}
        Err(e) => {
          assert_is_not_404(e);
        }
      }
    }
  }


  /// Records
  mod records {
    use databus_core::ro::record_update_ro::FieldKeyEnum;

    use crate::datasheet::record::RecordManager;
    use crate::test_utils::utils::create_record_manager_v3;
    use crate::tests::v3_404::tests::assert_is_not_404;

    fn manager() -> RecordManager {
      create_record_manager_v3()
    }

    #[test]
    fn test_get_records() {
      match tokio_test::block_on(manager().get(vec![])) {
        Ok(_) => {}
        Err(e) => {
          assert_is_not_404(e);
        }
      }
    }

    #[test]
    fn test_create_records() {
      match tokio_test::block_on(manager().create(FieldKeyEnum::NAME, &vec![])) {
        Ok(_) => {}
        Err(e) => {
          assert_is_not_404(e);
        }
      }
    }

    #[test]
    fn test_update_records_put() {
      match tokio_test::block_on(manager().update_patch()) {
        Ok(_) => {}
        Err(e) => {
          assert_is_not_404(e);
        }
      }
    }

    #[test]
    fn test_update_records_patch() {
      match tokio_test::block_on(manager().update_put()) {
        Ok(_) => {}
        Err(e) => {
          assert_is_not_404(e);
        }
      }
    }

    #[test]
    fn test_delete_records_patch() {
      match tokio_test::block_on(manager().delete()) {
        Ok(_) => {}
        Err(e) => {
          assert_is_not_404(e);
        }
      }
    }
  }


  /// Views
  mod views {
    use crate::datasheet::view::ViewManager;
    use crate::test_utils::utils::create_view_manager_v3;
    use crate::tests::v3_404::tests::assert_is_not_404;

    fn manager() -> ViewManager {
      create_view_manager_v3()
    }

    #[test]
    fn test_get_records() {
      match tokio_test::block_on(manager().get()) {
        Ok(_) => {}
        Err(e) => {
          assert_is_not_404(e);
        }
      }
    }
  }
}
