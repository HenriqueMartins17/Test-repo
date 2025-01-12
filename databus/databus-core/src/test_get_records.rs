use super::*;

#[cfg(test)]
mod tests {
  use std::collections::HashMap;
  use std::sync::Arc;

  use async_trait::async_trait;
  use futures::executor::block_on;
  use rand::Rng;
  use serde_json::Value;

  use databus_shared::prelude::Json;

  use crate::dtos::fusion_api_dtos::IAssetDTO;
use crate::ot::SourceTypeEnum;
use crate::prelude::{DatasheetEntitySO, DatasheetPackSO, IDataSourceProvider};
  use crate::shared::{AuthHeader, IUserInfo, NodePermissionSO};
use crate::so::{AttachmentValue, MemberValue, RecordSO, UrlValue, RecordMeta, InternalSpaceUsageView, InternalSpaceSubscriptionView, FieldSO};

  #[test]
  fn test_get_records() {
    pub struct MockLoader {
      pub json: String,
    }
    #[async_trait(?Send)]
    impl IDataSourceProvider for MockLoader {
      async fn get_datasheet_pack(
        &self,
        _datasheet_id: &str,
        _user_id: Option<String>,
        _space_id: Option<String>,
      ) -> anyhow::Result<DatasheetPackSO> {
        serde_json::from_str(crate::mock::MOCK_DATASHEET_PACK_JSON_FOR_GET_RECORD)
          .map_err(|e| anyhow::anyhow!(e))
      }
      async fn get_datasheet_revision(&self, _datasheet_id: &str) -> anyhow::Result<i32> {
        let mut rng = rand::thread_rng();
        let random_number = rng.gen_range(1..=usize::MAX);
        Ok(random_number as i32)
      }

      async fn cache_snapshot(&self, _key: &str, _value: DatasheetPackSO, _newest_revision: i32) -> anyhow::Result<bool> {
        Ok(true)
      }

      async fn get_snapshot_from_cache(&self, _key: &str) -> anyhow::Result<Option<DatasheetPackSO>> {
        Ok(None)
      }

      async fn cache_room_ids(&self, _key: &str, _value: Vec<String>) {}

      async fn get_room_ids_from_cache(&self, _key: &str) -> anyhow::Result<Vec<String>> {
        Ok(Vec::new())
      }

      async fn get_ids_by_dst_id_and_record_ids(&self, _dst_id: &str, _record_ids: Vec<String>) -> anyhow::Result<Vec<String>>{
        Ok(Vec::new())
      }

      async fn get_archived_ids_by_dst_id_and_record_ids(&self, _dst_id: &str, _record_ids: Vec<String>) -> anyhow::Result<Vec<String>> {
        Ok(Vec::new())
      }

      async fn get_meta_data_by_dst_id(&self, _dst_id: &str, _include_deleted: bool) -> anyhow::Result<Option<Json>> {
        Ok(None)
      }

      async fn get_field_map_by_dst_id(&self, _dst_id: &str) -> anyhow::Result<HashMap<String, FieldSO>> {
        Ok(HashMap::new())
      }

      async fn get_field_by_fld_id_and_dst_id(&self, _fld_id: &str, _dst_id: &str) -> anyhow::Result<Option<FieldSO>> {
        Ok(None)
      }

      async fn select_field_type_by_fld_id_and_dst_id(&self, fld_id: &str, dst_id: &str) -> anyhow::Result<Option<u32>> {
        Ok(None)
      }

      async fn get_datasheet_by_dst_id(&self, _dst_id: &str) -> anyhow::Result<Option<DatasheetEntitySO>>{
        Ok(None)
      }

      async fn execute_command_with_update_records(&self, _st_id: &str, _user_id: &str, _json_value: Value) -> anyhow::Result<String>{
        Ok("".to_string())
      }

      async fn update_space_statistics(&self, _space_id: &str, _json_value: Value) -> anyhow::Result<()> {
        Ok(())
      }

      async fn get_basic_records_by_record_ids(&self, _dst_id: &str, _record_ids: Vec<String>, _is_deleted: bool) -> anyhow::Result<HashMap<String, RecordSO>>{
        Ok(HashMap::new())
      }

      async fn get_space_id_by_node_id(&self, _node_id: &str) -> anyhow::Result<Option<String>> {
        Ok(None)
      }

      async fn get_node_role(
        &self,
        _node_id: String,
        _auth: AuthHeader, // replace with actual type
        _share_id: Option<String>,
        _room_id: Option<String>,
        _source_datasheet_id: Option<String>,
        _source_type: Option<SourceTypeEnum>,
        _allow_all_entrance: Option<bool>,
      ) -> anyhow::Result<NodePermissionSO> {
        Ok(NodePermissionSO {
          ..Default::default()
        })
      }

      async fn capacity_over_limit(
        &self,
        _auth: &AuthHeader,
        _space_id: &str,
      ) -> anyhow::Result<bool> {
        Ok(false)
      }

      async fn get_asset_info(&self, _token: &str) -> anyhow::Result<IAssetDTO> {
        Ok(IAssetDTO::default())
      }

      async fn get_space_usage(&self, _space_id: &str) -> anyhow::Result<InternalSpaceUsageView> {
        Ok(InternalSpaceUsageView::default())
      }

      async fn get_space_subscription(&self, _space_id: &str) -> anyhow::Result<InternalSpaceSubscriptionView> {
        Ok(InternalSpaceSubscriptionView::default())
      }

      async fn count_rows_by_dst_id(&self, _dst_id: &str) -> anyhow::Result<u32>{
        Ok(0)
      }

      async fn update_record_replace(
        &self,
        _dst_id: &str,
        _record_id: &str,
        _json_map: HashMap<&str, Value>,
        _record_meta: String,
        _revision: &u32,
        _updated_by: &str,
      ) {}

      async fn update_record_delete(
        &self,
        _dst_id: &str,
        _record_ids: &Vec<String>,
        _is_delete_data: bool,
        _revision: &u32,
        _updated_by: &str,
      ) {}

      async fn update_record_archive_delete(
        &self,
        _dst_id: &str,
        _record_ids: &Vec<String>,
        _updated_by: &str,
      ) {}

      async fn update_record_remove(
        &self,
        _dst_id: &str,
        _record_id: &str,
        _json_path: String,
        _record_meta: String,
        _revision: &u32,
        _updated_by: &str,
      ) {}

      async fn create_new_changeset(
        &self,
        _id: &str,
        _message_id: &str,
        _dst_id: &str,
        _member_id: &str,
        _operations: Value,
        _revision: &u32,
      ) {}

      async fn create_new_changeset_source(
        &self,
        _id: &str,
        _created_by: &str,
        _dst_id: &str,
        _message_id: &str,
        _source_id: &str,
        _source_type: &u32
      ) {}

      async fn create_record_source(
        &self,
        _user_id: &str,
        _dst_id: &str,
        _source_id: &str,
        _record_ids: Vec<String>,
        _source_type: &u32,
      ) {}

      async fn create_record(
        &self,
        _dst_id: &str,
        _revision: &u32,
        _user_id: &str,
        _save_record_entities: Vec<(&String, HashMap<String, serde_json::Value>, RecordMeta)>
      ) {}

      async fn update_revision_by_dst_id(&self, _dst_id: &str, _revision: &u32, _updated_by: &str) {}

      async fn get_rel_node_id_by_main_node_id(&self, _main_node_id: &str) -> anyhow::Result<Vec<String>> {
        Ok(Vec::new())
      }

      async fn get_has_robot_by_resource_ids(&self, _resource_ids: Vec<String>) -> anyhow::Result<bool> {
        Ok(false)
      }

      async fn get_user_info_by_space_id(&self, auth: &AuthHeader, space_id: &str) -> anyhow::Result<IUserInfo> {
        Ok(IUserInfo{
          ..Default::default()
        })
      }

      async fn update_meta_data(&self, _dst_id: &str, _meta_data: &str, _revision: &u32, _updated_by: &str) {}

      async fn select_count_by_dst_id_and_field_name(&self, _dst_id: &str, _field_name: &str) -> anyhow::Result<u32> {
        Ok(0)
      }
    }

    let loader = Arc::new(MockLoader {
      json: crate::mock::MOCK_DATASHEET_PACK_JSON_FOR_GET_RECORD.to_string(),
    });
    let manager = super::init(true, true, "TODO".to_string(), loader.clone());

    let dst_id: &str = "dstMUyAamjZxi7EPci";
    let user_id: Option<String> = Some("1688395192133423106".to_string());
    let space_id: Option<String> = Some("spc2qi5CvEWqw".to_string());
    let view_id: Option<String> = Some("viwM01QyCWxlD".to_string());
    let auth :AuthHeader= Default::default();
    let ret = manager.get_records(dst_id, user_id, space_id, view_id, None, None, &auth);
    let result = block_on(ret);
    assert_eq!(result.is_ok(), true);

    if let Ok(value) = result {
      let item = value.records.get(0).unwrap();

      let expect_url = UrlValue {
        r#type: Some(2),
        title: Option::from("https://www.baidu.com/".to_string()),
        favicon: None,
        text: "https://www.baidu.com/".to_string(),
        link: None,
        visited: None,
      };
      let mut expect_attachments: Vec<AttachmentValue> = Vec::new();
      expect_attachments.push(AttachmentValue {
        id: "atcTowrZcTpk3".to_string(),
        name: "1827824615-1827824615-7850446944248791040-3655772686-10057-A-0-1-imgplus-0001.png".to_string(),
        mime_type: "image/png".to_string(),
        token: "space/2023/08/09/21b24e25834a401da2827621314f9c0c".to_string(),
        bucket: Some("QNY1".to_string()),
        size: 2337098,
        width: Some(1920),
        height: Some(1080),
        url: Option::from("https://s1.vika.cn/space/2023/08/09/21b24e25834a401da2827621314f9c0c".to_string()),
        preview: None,
      });
      expect_attachments.push(AttachmentValue {
        id: "atcktH5nwAmhN".to_string(),
        name: "1827824615-1827824615-7850446944248791040-3655772686-10057-A-0-1-imgplus-0001.png".to_string(),
        mime_type: "image/png".to_string(),
        token: "space/2023/08/09/21b24e25834a401da2827621314f9c0c".to_string(),
        bucket: Some("QNY1".to_string()),
        size: 2337098,
        width: Some(1920),
        height: Some(1080),
        url: Option::from("https://s1.vika.cn/space/2023/08/09/21b24e25834a401da2827621314f9c0c".to_string()),
        preview: None,
      });

      let mut expect_members: Vec<MemberValue> = Vec::new();
      expect_members.push(MemberValue {
        id: "1688817023401119745".to_string(),
        unit_id: "8b01ed1f6629abf1a76e172e6082f8fc".to_string(),
        name: "pengcheng".to_string(),
        r#type: "Member".to_string(),
        avatar: None,
      });
      expect_members.push(MemberValue {
        id: "1689549079210176514".to_string(),
        unit_id: "a1ca6e96dfe5101564b132d3ee105da7".to_string(),
        name: "liushuang".to_string(),
        r#type: "Member".to_string(),
        avatar: None,
      });

      let auto_number = 1;
      assert_eq!(auto_number, item.fields["自增数字"].as_i64().unwrap());

      let multi_options = vec!["hello", "111"];
      assert_eq!(
        multi_options,
        item.fields["选项"].as_array_string().unwrap()
      );

      let multi_text = "a\nb\nc\nd\ne";
      assert_eq!(multi_text, item.fields["多行文本"].as_str().unwrap());

      let cascader = "互联网事业部/平台产品组/产品规划小组";
      assert_eq!(cascader, item.fields["级联器"].as_str().unwrap());

      let number = 1111111111;
      assert_eq!(number, item.fields["数字"].as_u64().unwrap());

      let update_time = 1692673916573;
      assert_eq!(update_time, item.fields["修改时间"].as_i64().unwrap());

      let phone = "1111";
      assert_eq!(phone, item.fields["电话"].as_str().unwrap());

      let text = "test";
      assert_eq!(text, item.fields["标题"].as_str().unwrap());

      let single_select = "得到的";
      assert_eq!(single_select, item.fields["单选"].as_str().unwrap());

      let currency = 1111.01;
      assert_eq!(currency, item.fields["货币"].as_f64().unwrap());

      let datetime = 1692201600000;
      assert_eq!(datetime, item.fields["日期"].as_u64().unwrap());

      let mail = "1@qq.com";
      assert_eq!(mail, item.fields["邮箱"].as_str().unwrap());

      let percent = 11.11012;
      assert_eq!(percent, item.fields["百分比"].as_f64().unwrap());

      let check_box = true;
      assert_eq!(check_box, item.fields["勾选"].as_bool().unwrap());

      let rating = 3;
      assert_eq!(rating, item.fields["评分"].as_i64().unwrap());

      let create_time = 1691482351148;
      assert_eq!(create_time, item.fields["创建时间"].as_i64().unwrap());

      assert_eq!(expect_url, item.fields["URL"].as_url_value().unwrap());

      assert_eq!(expect_attachments, item.fields["附件"].as_array_attachment().unwrap());

      assert_eq!(
        expect_members,
        item.fields["member"].as_array_member().unwrap()
      );
    } else if let Err(error) = result {
      println!("Error: {:?}", error);
    }
  }
}
