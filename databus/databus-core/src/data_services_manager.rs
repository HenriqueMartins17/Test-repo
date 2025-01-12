use std::collections::HashMap;
use std::rc::Rc;
use std::sync::{Arc, Mutex};

use anyhow::Context;

use databus_shared::prelude::HashSet;
use serde_json::from_value;
use crate::config::{EnvConfigService, EnvConfigKey, IServerConfig};
use crate::filter::FusionApiFilter;
use crate::logic::{IViewInfo, ViewLogic, IRecordsOptions, CellFormatEnum, RecordLogic, IAddRecordsOptions};

use crate::data_bundle::DataBundle;
use crate::dtos::enterprise_fusion_api_dtos::{
  EmbedLinkDto, MemberDetailDto, MemberDto, MemberPageDto, RoleDetailDto, RoleDto, RolePageDto, RoleUnitDetailDto,
  TeamDetailDto, TeamDto, TeamPageDto, WidgetDto,
};
use crate::dtos::fusion_api_dtos::{DatasheetCreateDto, DatasheetViewDto, DatasheetViewListDto, FieldCreateDto, ApiRecordDto};
use crate::ot::commands::{SaveOptions, ExecuteResult, AddFieldOptions};
use crate::ot::{ServerDataStorageProvider, OtService, DatasheetOtService, ResourceChangeHandler, RoomResourceRelService, DatasheetChangesetSourceService, SourceTypeEnum, FusionApiRecordService, DatasheetMetaService};
use crate::ot::commands::SetRecordOptions;
use crate::ot::types::NodeTypeEnum;
use crate::prelude::{FieldSO, IDataSourceProvider};
use crate::ro::field_create_ro::FieldCreateRo;
use crate::ro::record_update_ro::{FieldKeyEnum, RecordUpdateRO};
use crate::shared::AuthHeader;
use crate::so::datasheet_pack::DatasheetPackSO;
use crate::so::types::ViewType;
use crate::so::RecordSO;
use crate::so::{
  prepare_context_data, APINodeSO, APISpaceSO, DatasheetMetaSO, DatasheetPackContext, EmbedLinkEntitySO, ViewRowSO,
  ViewSO,
};
use crate::transformer::fusion_api_transformer::FusionApiTransformer;
use crate::vo::api_page::ApiPage;
use crate::vo::fusion_api_vo::AssetVo;
use crate::vo::list_vo::ListVO;
use crate::vo::record_vo::RecordDTO;
use crate::Datasheet;

type ILinkedRecordMap = HashMap<String, Vec<String>>;

pub struct ViewOptions {
  view_id: Option<String>,
  rows: Vec<ViewRowSO>,
  field_map: HashMap<String, FieldSO>,
}

/**
 * DataBundle Manager, the m`ain entry and binding functions' mother.
 */
pub struct DataServicesManager {
  /**
   * Shared Singleton DataBundle to store and first-level cache the DatasheetPacks.
   */
  pub state: Arc<Mutex<DataBundle>>,
  pub loader: Arc<dyn IDataSourceProvider>,
}

impl DataServicesManager {
  pub fn new(_rest_base_url: String, datasheet_loader: Arc<dyn IDataSourceProvider>) -> DataServicesManager {
    let state = Arc::new(Mutex::new(DataBundle::new_with_default()));
    DataServicesManager {
      state: state,
      loader: datasheet_loader.clone(),
    }
  }

  pub async fn get_datasheet_pack(
    &self,
    dst_id: &str,
    user_id: Option<String>,
    space_id: Option<String>,
    view_id: Option<String>,
  ) -> anyhow::Result<Box<DatasheetPackSO>> {
    let datasheet_pack = self._get_datasheet_pack(dst_id, user_id, space_id).await?;

    // TODO: add auth_header
    // let user_info = self
    //     .loader
    //     .get_user_info_by_space_id(auth_header, &datasheet_pack.datasheet.space_id)
    //     .await?;

    let context = prepare_context_data(datasheet_pack);
    // context.user_info = user_info;
    let context = Rc::new(context);

    let mut view = super::so::view::get_view(&context.datasheet_pack, view_id)?;
    // visible columns
    super::so::view::filter_columns(&context.datasheet_pack, &mut view);
    // visible fields
    let mut visible_columns = vec![];
    for col in view.columns.iter() {
      if let Some(hidden) = col.hidden {
        if hidden {
          continue;
        }
      }
      visible_columns.push(col.to_owned())
    }
    let field_list = super::so::field::get_field_map(&context.datasheet_pack, &visible_columns, None);
    // visible rows
    super::so::view::calc_visible_rows(None, context.clone(), &mut view)?;
    let row_ids = view.rows.unwrap()
      .iter()
      .map(|row| row.record_id.clone())
      .collect::<HashSet<String>>();

    let mut context = Rc::try_unwrap(context).or_else(|e| Err(anyhow::Error::msg("logic error")))?;

    let mut record_map = HashMap::new();
    for (record_id, record) in context.datasheet_pack.snapshot.record_map.iter_mut() {
      // filter rows
      if !row_ids.contains(record_id) {
        continue;
      }
      // filter columns
      let mut record_data = HashMap::new();
      for field in field_list.iter() {
        if let Some(field_data) = record.data.get(field.id.clone()) {
          record_data.insert(field.id.clone(), field_data.to_owned());
        }
      }
      record.data = serde_json::to_value(record_data)?;
      record_map.insert(record_id.to_owned(), record.to_owned());
    }

    context.datasheet_pack.snapshot.record_map = record_map;
    Ok(context.datasheet_pack)
  }

  /**
   * Retrieves the datasheet pack for the specified datasheet ID.
   *
   * This function acquires a lock on the shared data bundle and checks if the datasheet pack is already cached.
   * If it is, it checks if the cached version is up-to-date with the newest revision.
   * If it is not up-to-date, it retrieves the new datasheet pack and updates the cache.
   * If the datasheet pack is not cached, it retrieves it and adds it to the cache.
   *
   * @param dst_id The ID of the datasheet.
   * @returns The datasheet pack for the specified datasheet ID.
   */
  pub async fn _get_datasheet_pack(
    &self,
    dst_id: &str,
    user_id: Option<String>,
    space_id: Option<String>,
  ) -> anyhow::Result<Box<DatasheetPackSO>> {
    // Acquire a lock on the shared data bundle
    let mut data_bundle = match self.state.lock() {
      Ok(bundle) => bundle,
      Err(poisoned) => {
        let inner = poisoned.into_inner();
        println!("Failed to acquire lock");
        inner
      }
    };
    // Check if the datasheet pack is already cached, and whether it needs to be updated
    let newest_revision = self.loader.get_datasheet_revision(dst_id).await?;
    let datasheet_pack = match data_bundle.get(dst_id) {
      // If the datasheet pack is already cached, check if it needs to be updated
      Some(cached_datasheet_pack) => {
        let cached_revision = cached_datasheet_pack.datasheet.revision;
        if newest_revision == cached_revision as i32 {
          // If the cached version is up-to-date, return it
          println!("cache had hit");
          cached_datasheet_pack
        } else {
          // Otherwise, get the new datasheet pack and update the cache
          let new_datasheet_pack = self.loader.get_datasheet_pack(dst_id, user_id, space_id).await?;
          data_bundle.push(dst_id, new_datasheet_pack).unwrap()
        }
      }
      // If the datasheet pack is not cached, get it and add it to the cache
      None => {
        let l2_cache_key = format!("databus:snapshot:v1:{}", dst_id);
        let snapshot_cache = self
          .loader
          .get_snapshot_from_cache(l2_cache_key.as_str())
          .await
          .unwrap();
        if snapshot_cache.is_none() {
          let new_datasheet_pack = self.loader.get_datasheet_pack(dst_id, user_id, space_id).await?;
          self
            .loader
            .cache_snapshot(l2_cache_key.as_str(), new_datasheet_pack.clone(), newest_revision)
            .await
            .unwrap();
          // data_bundle.push(dst_id, new_datasheet_pack).unwrap()
          Box::new(new_datasheet_pack)
        } else {
          let snapshot = snapshot_cache.unwrap();
          let cached_revision = snapshot.datasheet.revision;
          if newest_revision == cached_revision as i32 {
            // If the cached version is up-to-date, return it
            Box::new(snapshot)
          } else {
            // Otherwise, get the new datasheet pack and update the cache
            let new_datasheet_pack = self.loader.get_datasheet_pack(dst_id, user_id, space_id).await?;
            self
              .loader
              .cache_snapshot(l2_cache_key.as_str(), new_datasheet_pack.clone(), newest_revision)
              .await
              .unwrap();
            data_bundle.push(dst_id, new_datasheet_pack.clone()).unwrap()
          }
        }
      }
    };
    Ok(datasheet_pack)
  }

  /**
   * Loads a dashboard pack for a dashboard from the data source.
   *
   * The implementor can derive `ILoadDataboardPackOptions` and add custom fields.
   *
   * @returns If the dashboard is not found, null is returned.
   */
  pub fn get_dashboard_pack(&self, _dst_id: &str) {
    //-> anyhow::Result<DatasheetPack> {
    todo!()
  }

  /**
   * Get records in the view or datasheet_id.
   */
  pub async fn get_records(
    &self,
    dst_id: &str,
    user_id: Option<String>,
    space_id: Option<String>,
    view_id: Option<String>,
    fields: Option<Vec<String>>,
    filter_by_formula: Option<String>,
    auth_header: &AuthHeader,
  ) -> anyhow::Result<ApiPage<Vec<RecordDTO>>> {
    let datasheet_pack = self._get_datasheet_pack(dst_id, user_id, space_id).await?;

    let user_info = self
      .loader
      .get_user_info_by_space_id(auth_header, &datasheet_pack.datasheet.space_id)
      .await?;

    let mut context = prepare_context_data(datasheet_pack);
    context.user_info = user_info;
    let context = Rc::new(context);

    let mut view = super::so::view::get_view(&context.datasheet_pack, view_id)?;
    super::so::view::filter_columns(&context.datasheet_pack, &mut view);
    let field_map = super::so::field::get_field_map(&context.datasheet_pack, &view.columns, fields);

    super::so::view::calc_visible_rows(filter_by_formula, context.clone(), &mut view)?;
    let records: Vec<RecordSO> = view.rows.unwrap()
      .iter()
      .map(|row| {
        context
          .datasheet_pack
          .snapshot
          .record_map
          .get(&row.record_id)
          .unwrap()
          .clone()
      })
      .collect();

    let record_vos: Vec<_> = records
      .into_iter()
      .map(|record| record.to_dto(&field_map, context.clone()))
      .collect();

    let total = record_vos.len() as u32;
    let page_size = record_vos.len() as u32;
    Ok(ApiPage {
      page_num: 1,
      records: record_vos,
      page_size,
      total,
    })
  }

  /**
   * Add records to the datasheet.
   *
   * @param recordOptions Options for adding records.
   * @param saveOptions The options that will be passed to the data saver.
   *
   * @return If the command execution succeeded, the `data` field of the return value is an array of IDs of newly created records.
   */
  pub async fn add_records(
    &self,
    dst_id: String,
    view_id: Option<String>,
    user_id: String,
    mut body: RecordUpdateRO,
    member_id: String,
  ) -> anyhow::Result<ListVO> {
    let meta = self
      .loader
      .get_meta_data_by_dst_id(&dst_id, false)
      .await
      .with_context(|| format!("get_meta_data_by_dst_id err by {dst_id}"))?;
    let Some(meta) = meta else {
        return Err(
            anyhow::Error::msg(format!("meta not exist by {dst_id}"))
        );
    };
    let meta: DatasheetMetaSO = serde_json::from_value(meta)
      .map_err(|err| format!("Failed to deserialize meta to DatasheetMetaSO: {}", err.to_string()))
      .unwrap();

    let field_map = if body.field_key == FieldKeyEnum::NAME {
      meta.field_map.into_iter().map(|(_, v)| (v.name.clone(), v)).collect()
    } else {
      meta.field_map
    };
    match body.transform(&field_map, Some(self.loader.clone())).await {
      Ok(_body) => {
        body = _body;
      }
      Err(err) => {
        return Err(anyhow::Error::msg(format!(
          "Failed to transform record_update: {}",
          err
        )));
      }
    }
    // self.check_dst_record_count(&dst_id, &body, &meta).await?;
    let record_count = meta.views[0].rows.clone().unwrap().len();
    let env_config_service = EnvConfigService::new();
    let limit = env_config_service.get_room_config(EnvConfigKey::CONST).unwrap();
    let limit = from_value::<IServerConfig>(limit).unwrap();
    let token = format!("Bearer {}", user_id.clone());
    let auth = AuthHeader { token: Some(token), user_id: Some(user_id.clone()), ..Default::default() };
    // let space_id = self.request[SPACE_ID_HTTP_DECORATE];
    let total_count = record_count + body.records.len();
    if total_count >= (limit.max_record_count * limit.record_remind_range) / 100 && total_count <= limit.max_record_count {
        // self.rest_service.create_record_limit_remind(
        //     &auth,
        //     NoticeTemplatesConstant::AddRecordSoonToBeLimit,
        //     vec![user_id],
        //     &space_id,
        //     dst_id,
        //     limit.max_record_count,
        //     total_count,
        // ).await?;
    }
    if total_count > limit.max_record_count {
        // self.rest_service.create_record_limit_remind(
        //     &auth,
        //     NoticeTemplatesConstant::AddRecordOutOfLimit,
        //     vec![user_id],
        //     &space_id,
        //     dst_id,
        //     limit.max_record_count,
        // ).await?;
        return Err(anyhow::anyhow!("RECORD_ADD_LIMIT"));
    }

    // let add_records_profiler = self.logger.start_timer();

    let mut dst1 = self.get_datasheet(dst_id.clone()).await;

    if let Some(view_id) = &view_id {
        match self.check_view_exists(&mut dst1, view_id) {
          Ok(_) => {},
          Err(err) => {
            return Err(anyhow::Error::msg(format!(
              "check_view_exists: {}",
              err
            )));
          }
        }
    }

    // let update_field_operations = self.get_field_update_ops(&datasheet.unwrap(), &auth).await?;
    let view_index = meta.views.iter().position(|view| view.id == view_id).unwrap_or(0);
    let result = dst1.add_records(
      IAddRecordsOptions {
            view_id: meta.views[view_index].id.clone().unwrap(),
            index: meta.views[view_index].rows.clone().unwrap().len(),
            record_values: Some(body.records.iter().map(|record| record.fields.clone()).collect()),
            ignore_field_permission: Some(true),
            ..Default::default()
        },
        SaveOptions {
          auth,
          prepend_ops: Vec::new(),
        },
    ).await?;
    if result.result != ExecuteResult::Success {
      return Err(anyhow::Error::msg("api_insert_error"));
    }

    let record_ids: Vec<String> = from_value(result.data.unwrap()).unwrap();
    let source_type = SourceTypeEnum::OpenApi as u32;
    self.loader.create_record_source(&member_id, &dst_id, &dst_id, record_ids.clone(), &source_type).await;
    let rows = record_ids.iter().map(|record_id| ViewRowSO { record_id: record_id.clone(), ..Default::default() }).collect();

    // add_records_profiler.done(format!("addRecords {} profiler", dst_id));

    self.get_new_record_list_vo(&mut dst1, ViewOptions { view_id, rows, field_map }).await
  }

  pub async fn get_record_view_objects(&self, records: Vec<RecordLogic>, _cell_format: Option<CellFormatEnum>, context: Rc<DatasheetPackContext>) -> Vec<ApiRecordDto> {
    // let room_config = self.env_config_service.get_room_config(EnvConfigKey::OSS);
    // let oss_signature_enabled = room_config.oss_signature_enabled;
    let oss_signature_enabled = false;
    let transform = FusionApiTransformer::new();
    let api_record_dtos: Vec<ApiRecordDto> = records.iter().map(|record| record.get_view_object(|id, options| transform.record_vo_transform(id, options, None, context.clone()))).collect();
    if !oss_signature_enabled {
        return api_record_dtos;
    }
    api_record_dtos
    // let mut attachment_columns: Vec<String> = Vec::new();
    // let mut attachment_tokens: Vec<String> = Vec::new();

    // if !records.is_empty() {
    //     let record = &records[0];
    //     let vo_transform_options = record.get_vo_transform_options();
    //     let field_map = &vo_transform_options.field_map;
    //     for (field_id, field_value) in field_map {
    //         if field_value.field_type == FieldType::Attachment {
    //             attachment_columns.push(field_id.clone());
    //         }
    //     }
    // }

    // if attachment_columns.is_empty() {
    //     return api_record_dtos;
    // }

    // for api_record_dto in &api_record_dtos {
    //     let fields = &api_record_dto.fields;
    //     for (field_id, field_value) in fields {
    //         if attachment_columns.contains(field_id) {
    //             if let Some(field_value) = field_value.as_array() {
    //                 for obj in field_value {
    //                     attachment_tokens.push(obj.token.clone());
    //                     if let Some(preview) = &obj.preview {
    //                         attachment_tokens.push(preview.clone());
    //                     }
    //                 }
    //             }
    //         }
    //     }
    // }

    // if attachment_tokens.is_empty() {
    //     return api_record_dtos;
    // }

    // let batch_size = 100;
    // let mut signature_map: HashMap<String, String> = HashMap::new();

    // for tokens in attachment_tokens.chunks(batch_size) {
    //     let batch_signatures = self.rest_service.get_signatures(tokens.to_vec()).await;
    //     for obj in batch_signatures {
    //         signature_map.insert(obj.resource_key.clone(), obj.url.clone());
    //     }
    // }

    // for api_record_dto in &mut api_record_dtos {
    //     let fields = &mut api_record_dto.fields;
    //     for (field_id, field_value) in fields {
    //         if attachment_columns.contains(field_id) {
    //             if let Some(field_value) = field_value.as_array_mut() {
    //                 for obj in field_value {
    //                     obj.url = signature_map.get(&obj.token).cloned();
    //                     if let Some(preview) = &mut obj.preview {
    //                         *preview = signature_map.get(preview).cloned().unwrap_or_default();
    //                     }
    //                 }
    //             }
    //         }
    //     }
    // }

    // api_record_dtos
  }

  pub fn check_view_exists(&self, datasheet: &mut Datasheet, view_id: &str) -> anyhow::Result<()> {
    // test if view_id exists
    let view = datasheet.get_view(view_id);
    if view.is_none() {
        return Err(anyhow::Error::msg(format!("api_query_params_view_id_not_exists={:#?}", view_id)));
    }
    Ok(())
  }

  async fn get_datasheet(&self, dst_id: String) -> Datasheet {
    let datasheet_pack = self
      .get_datasheet_pack(
        &dst_id,None,None,None
      )
      .await
      .with_context(|| format!("get_datasheet_pack err by {dst_id}")).unwrap();

    let context = prepare_context_data(datasheet_pack);
    let context = Rc::new(context);
    let datasheet_ot_service = DatasheetOtService::new(self.loader.clone());
    let resource_change_handler = ResourceChangeHandler::new(self.loader.clone());
    let rel_service = RoomResourceRelService::new(self.loader.clone());
    let ot_service = Arc::new(OtService::new(self.loader.clone(), datasheet_ot_service, resource_change_handler, rel_service));
    let changeset_source_service = DatasheetChangesetSourceService::new(self.loader.clone());
    let dst1 = Datasheet::new(dst_id, context, Box::new(ServerDataStorageProvider::new(ot_service, changeset_source_service)));
    dst1
  }

  /**
   * Update existing records in the datasheet.
   *
   * @param recordOptions Options for updating records.
   * @param saveOptions The options that will be passed to the data saver.
   */
  pub async fn update_records(
    &self,
    dst_id: String,
    view_id: Option<String>,
    user_id: String,
    mut record_update: RecordUpdateRO,
  ) -> anyhow::Result<ListVO> {
    //FieldPipe.transform for record_update
    let datasheet = self
      .loader
      .get_datasheet_by_dst_id(&dst_id)
      .await
      .with_context(|| format!("get_datasheet_by_id err by {dst_id}"))?;
    let Some(_datasheet) = datasheet else {
      return Err(
          anyhow::Error::msg(format!("api_datasheet_not_exist"))
      );
    };
    let meta = self
      .loader
      .get_meta_data_by_dst_id(&dst_id, false)
      .await
      .with_context(|| format!("get_meta_data_by_dst_id err by {dst_id}"))?;
    let Some(meta) = meta else {
        return Err(
            anyhow::Error::msg(format!("meta not exist by {dst_id}"))
        );
    };
    let meta: DatasheetMetaSO = serde_json::from_value(meta)
      .map_err(|err| format!("Failed to deserialize meta to DatasheetMetaSO: {}", err.to_string()))
      .unwrap();

    let mut field_map = meta.field_map.clone();
    if record_update.field_key == FieldKeyEnum::NAME {
      field_map = field_map.into_iter().map(|(_, v)| (v.name.clone(), v)).collect();
    }
    match record_update.transform(&field_map, Some(self.loader.clone())).await {
      Ok(_record_update) => {
        record_update = _record_update;
      }
      Err(err) => {
        return Err(anyhow::Error::msg(format!(
          "Failed to transform record_update: {}",
          err
        )));
      }
    }
    let record_ids = record_update.clone().get_record_ids();
    for record_id in record_ids.iter() {
      if record_id.is_none() {
        return Err(anyhow::Error::msg("api_params_instance_recordid_error"));
      }
    }
    let record_ids = record_ids.iter().map(|record_id| record_id.clone().unwrap()).collect::<Vec<_>>();
    let record_id_set: std::collections::HashSet<String> = record_update
      .records
      .iter()
      .map(|record| record.record_id.clone().unwrap())
      .collect();
    let db_record_ids_pack = self
      .loader
      .get_ids_by_dst_id_and_record_ids(&dst_id, record_ids.clone())
      .await;

    match db_record_ids_pack {
      Ok(_db_record_ids) => {
        // println!("db_record_ids: {:#?}", db_record_ids);
        // if db_record_ids.is_empty() {
        //   // return Ok(HttpResponse::Ok().json(ApiResponse::success(db_record_ids)));
        //   // return Ok(HttpResponse::Ok(){
        //   //   message: format!("dbRecordIds is_empty recordId: {}", record_ids.join(", ")),
        //   // });
        // }else {
        //   // HttpResponse::Ok().json(ApiResponse::success(db_record_ids))
        //   let record_id_set_tmp = db_record_ids.iter().cloned().collect::<HashSet<_>>();
        //   if record_id_set != record_id_set_tmp {

        //   }
        // }
      }
      Err(_err) => {
        // return Err(DataBusHttpError {
        //   message: format!("Failed to get_ids_by_dst_id_and_record_ids: {}", err),
        // });
      }
    }

    // let link_datasheet: ILinkedRecordMap = request[DATASHEET_LINKED]; {}
    let link_datasheet: ILinkedRecordMap = HashMap::new();
    let _linked_record_map = if link_datasheet.is_empty() {
      None
    } else {
      Some(link_datasheet)
    };

    //use for getBasicRecordsByRecordIds
    let record_ids: Vec<String> = record_id_set.into_iter().collect();
    let rows: Vec<ViewRowSO> = record_ids
      .iter()
      .map(|record_id| ViewRowSO {
        record_id: record_id.clone(),
        hidden: None,
      })
      .collect();

    // let _update_field_operations: Vec<Operation> = Vec::new();

    // let _enriched_select_fields = {};
    // let enriched_select_fields = self.request[DATASHEET_ENRICH_SELECT_FIELD];
    // for (field_id, field) in enriched_select_fields.iter() {
    //     let result = dst.update_field(field, &auth, false).await;
    //     if result.result != ExecuteResult::Success {
    //         return Err(ApiException::tip_error(ApiTipConstant::api_insert_error));
    //     }

    //     let changesets = result.save_result as Vec<ILocalChangeset>;
    //     update_field_operations.extend(changesets.iter().flat_map(|change_set| change_set.operations.clone()));
    // }
    // this.transform.getUpdateCellOptions
    let update_cell_options = record_update
      .records
      .iter()
      .flat_map(|record| {
        record
          .fields
          .iter()
          .map(|(field_id, value)| SetRecordOptions {
            record_id: record.record_id.clone().unwrap(),
            field_id: field_id.clone(),
            value: serde_json::from_value(value.clone()).unwrap(),
            ..Default::default()
          })
          .collect::<Vec<_>>()
      })
      .collect::<Vec<_>>();

    // println!("update_cell_options: {:#?}", update_cell_options);

    // let _resource_type = ResourceType::Datasheet;
    // let _resource_id = dst_id.clone();

    // let field_permission_map = datasheet_pack.field_permission_map.clone();
    // println!("fieldPermissionMap: {:#?}", field_permission_map);
    let token = format!("Bearer {}", user_id);
    let save_options = SaveOptions {
      auth: AuthHeader {
        token: Some(token),
        user_id: Some(user_id.to_string()),
        ..Default::default()
      },
      prepend_ops: Vec::new(),
    };
    let mut dst1 = self.get_datasheet(dst_id.clone()).await;

    if let Some(view_id) = &view_id {
      match self.check_view_exists(&mut dst1, view_id) {
        Ok(_) => {},
        Err(err) => {
          return Err(anyhow::Error::msg(format!(
            "check_view_exists: {}",
            err
          )));
        }
      }
    }

    let result = dst1.update_records(update_cell_options, save_options).await?;
    // println!("result={:#?}", result);

    if result.result == ExecuteResult::None {
      // TODO return records in viewId instead of first view
      let first_view = meta.views.first().unwrap();
      let view = Some(ViewLogic::new(
        &mut dst1,
        IViewInfo {
          property: ViewSO {
            rows: Some(rows.clone()),
            ..first_view.clone()
          },
          field_map: field_map.clone(),
      }));
      if view.is_none() {
          // TODO throw exception
          return Ok(ListVO { records: vec![] });
      }

      let records = view.unwrap().get_records(IRecordsOptions::default());
      let context = dst1.context.clone();
      let record_view_objects = self.get_record_view_objects(records, None, context).await;
      // update_records_profiler.done(format!("update {}'s records profiler, records count: {}", dst_id, records.len()));
      return Ok(ListVO { records: record_view_objects });
    }

    // Command execution failed
    if result.result != ExecuteResult::Success {
      return Err(anyhow::Error::msg("api_update_error"));
    }

    // let record_map = self.loader.get_basic_records_by_record_ids(&dst_id, record_ids).await?;
    // // println!("record_map: {:#?}", record_map);
    // let tmp = ResetRecordsOptions {
    //   cmd: CollaCommandName::ResetRecords,
    //   datasheet_id: datasheet_id.clone(),
    //   data: record_map,
    // };

    self.get_new_record_list_vo(&mut dst1, ViewOptions {view_id, rows: rows.clone(), field_map}).await
  }

  async fn get_new_record_list_vo(
    &self,
    new_datasheet: &mut Datasheet,
    options: ViewOptions,
  ) -> anyhow::Result<ListVO> {
    let ViewOptions {view_id, rows, field_map} = options;
    let fusion_api_filter = FusionApiFilter::new();
    let new_view = if view_id.is_some() {
      let view_id = view_id.unwrap();
      let view = new_datasheet.context.datasheet_pack.snapshot.meta.views.iter().find(|view| view.id.as_ref().unwrap() == &view_id);
      if view.is_none() {
        return Err(anyhow::Error::msg(format!("api_query_params_view_id_not_exists={:#?}", view_id)));
      }
      ViewLogic::new(
        new_datasheet,
        IViewInfo {
          property: ViewSO {
            rows: Some(rows.clone()),
            columns: fusion_api_filter.get_columns_by_view_id(new_datasheet.context.clone(), &new_datasheet.id, view),
            ..view.unwrap().clone()
          },
          field_map: field_map.clone(),
      })

    }else {
      let first_view = new_datasheet.context.datasheet_pack.snapshot.meta.views.first();
      ViewLogic::new(
        new_datasheet,
        IViewInfo {
          property: ViewSO {
            rows: Some(rows.clone()),
            columns: fusion_api_filter.get_columns_by_view_id(new_datasheet.context.clone(), &new_datasheet.id, None),
            ..first_view.unwrap().clone()
          },
          field_map: field_map.clone(),
      })
    };
    let records = new_view.get_records(IRecordsOptions::default());
    let record_vos = self.get_record_view_objects(records, None, new_datasheet.context.clone()).await;
    Ok(ListVO { records: record_vos })
  }

  /**
   * Delete records from the datasheet.
   *
   * @param dstId Record IDs of records that will be deleted.
   * @param recordIds The options that will be passed to the data saver.
   */
  pub async fn delete_records(&self, dst_id: String, record_ids: Vec<String>, user_id: String) -> anyhow::Result<bool> {
    // Validate the existence in advance to prevent repeatedly swiping all the count table data
    let fusion_api_record_service = FusionApiRecordService::new(self.loader.clone());
    match fusion_api_record_service.validate_archived_record_includes(&dst_id, &record_ids, "api_param_record_archived").await {
      Ok(_) => {},
      Err(err) => {
        return Err(err);
      }
    }
    match fusion_api_record_service.validate_record_exists(&dst_id, &record_ids, "api_param_record_not_exists").await {
      Ok(_) => {},
      Err(err) => {
        return Err(err);
      }
    }

    let mut dst1 = self.get_datasheet(dst_id.clone()).await;
    let token = format!("Bearer {}", user_id);
    let save_options = SaveOptions {
      auth: AuthHeader {
        token: Some(token),
        user_id: Some(user_id.to_string()),
        ..Default::default()
      },
      prepend_ops: Vec::new(),
    };

    let result = dst1.delete_records(record_ids, save_options).await?;
    // command execution failed
    if result.result != ExecuteResult::Success {
      return Err(anyhow::Error::msg("api_delete_error"));
    }

    Ok(true)
  }

  /**
   * A map of all fields in the datasheet, including fields hidden in some views.
   */
  pub async fn get_fields(
    &self,
    dst_id: &str,
    user_id: &str,
    space_id: &str,
  ) -> anyhow::Result<HashMap<String, FieldSO>> {
    let datasheet_pack = self
      ._get_datasheet_pack(dst_id, Some(user_id.to_string()), Some(space_id.to_string()))
      .await?;

    let field_map = &datasheet_pack.snapshot.meta.field_map;

    return Ok(field_map.clone());
  }

  /**
   * Add fields to the datasheet.
   *
   * @param fieldOptions Options for adding fields.
   * @param saveOptions The options that will be passed to the data saver.
   *
   * @return If the command execution succeeded, the `data` field of the return value is the ID of the newly created field.
   */
  pub async fn add_fields(&self, dst_id: String, mut field_create_ro: FieldCreateRo, user_id: String
  ) -> anyhow::Result<FieldCreateDto> {
    match field_create_ro.transform(Some(self.loader.clone()), &dst_id).await {
      Ok(body) => {
        field_create_ro = body;
      }
      Err(err) => {
        return Err(err);
      }
    }
    let datasheet_meta_service = DatasheetMetaService::new(self.loader.clone());
    match datasheet_meta_service.is_field_name_exist(&dst_id, &field_create_ro.name).await {
      Ok(is_true) => {
        if is_true {
          return Err(anyhow::Error::msg("api_params_must_unique"));
        }
      }
      Err(err) => {
        return Err(err);
      }
    }
    let foreign_datasheet_id = field_create_ro.foreign_datasheet_id();
    if foreign_datasheet_id.is_some() {
      //检测id对应的表是否存在
      let foreign_datasheet_id = foreign_datasheet_id.unwrap();
      let datasheet = self
        .loader
        .get_datasheet_by_dst_id(&foreign_datasheet_id)
        .await
        .with_context(|| format!("get_datasheet_by_id err by {dst_id}"))?;
      if datasheet.is_none() {
        return Err(anyhow::Error::msg(format!("api_params_foreign_datasheet_id_not_exists={:#?}", foreign_datasheet_id)));
      }
    }
    let token = format!("Bearer {}", user_id);
    let save_options = SaveOptions {
      auth: AuthHeader {
        token: Some(token),
        user_id: Some(user_id.to_string()),
        ..Default::default()
      },
      prepend_ops: Vec::new(),
    };
    let mut dst1 = self.get_datasheet(dst_id.clone()).await;
    let command_data = field_create_ro.transfer_to_command_data();
    let field_id = self.add_datasheet_field(&mut dst1, command_data, save_options).await?;
    Ok(FieldCreateDto { id: Some(field_id), name: Some(field_create_ro.name) })
  }

  async fn add_datasheet_field(&self, datasheet: &mut Datasheet, field_options: AddFieldOptions, save_options: SaveOptions) -> anyhow::Result<String> {
    let result = datasheet.add_fields(vec![field_options], save_options).await?;
    if result.result != ExecuteResult::Success {
      return Err(anyhow::Error::msg("api_insert_error"));
    }
    let str = from_value::<String>(result.data.unwrap()).unwrap();
    Ok(str)
  }

  pub async fn add_datasheet_fields(&self) -> anyhow::Result<DatasheetCreateDto> {
    Ok(DatasheetCreateDto {
      id: None,
      created_at: None,
      fields: vec![],
    })
  }

  /**
   * Update existing fields in the datasheet.
   *
   * @param field New field property.
   * @param saveOptions The options that will be passed to the data saver.
   */
  pub fn update_fields() {
    todo!()
  }

  /**
   * Delete fields from the datasheet.
   *
   * @param fields Options for deleting fields.
   * @param saveOptions The options that will be passed to the data saver.
   */
  pub async fn delete_fields(&self) -> anyhow::Result<bool> {
    Ok(false)
  }

  pub async fn get_views(
    &self,
    _dst_id: String,
    // user_id: String,
  ) -> anyhow::Result<DatasheetViewListDto> {
    let mut res = DatasheetViewListDto { views: vec![] };
    let view = DatasheetViewDto {
      id: "viewId".to_string(),
      name: "viewName".to_string(),
      r#type: ViewType::Grid,
    };
    res.views.push(view);
    Ok(res)
    // Ok(DatasheetViewListDto { views: vec![] })
  }

  /**
   * Get the first view of the datasheet.
   */
  pub fn get_first_view() {
    todo!()
  }
  /**
   * Get the view specified by `id`.
   *
   * @returns If the view if not found, null is returned.
   *
   * Get the view specified by options.
   *
   * @returns If the view if not found, null is returned.
   */
  pub fn get_view(_id: &str) {
    todo!()
  }

  pub async fn get_upload_presigned_url(&self) -> anyhow::Result<AssetVo> {
    Ok(AssetVo {
      token: None,
      upload_url: None,
      upload_request_method: None,
    })
  }

  pub async fn get_space_list(&self) -> anyhow::Result<Vec<APISpaceSO>> {
    let mut result = Vec::new();
    let space = APISpaceSO {
      id: "spaceId".to_string(),
      name: "spaceName".to_string(),
      is_admin: None,
    };
    result.push(space);
    Ok(result)
  }

  /**
   * Query the list of space station level 1 document nodes
   *
   * @param spaceId space id
   */
  pub async fn get_node_list(&self) -> anyhow::Result<Vec<APINodeSO>> {
    let mut result = Vec::new();
    let space = APINodeSO {
      id: "nodeId".to_string(),
      name: "nodeName".to_string(),
      r#type: NodeTypeEnum::Datasheet,
      icon: "icon".to_string(),
      is_fav: false,
      permission: None,
    };
    result.push(space);
    Ok(result)
  }

  pub async fn get_node_detail(&self) -> anyhow::Result<APINodeSO> {
    let node = APINodeSO {
      id: "nodeId".to_string(),
      name: "nodeName".to_string(),
      r#type: NodeTypeEnum::Datasheet,
      icon: "icon".to_string(),
      is_fav: false,
      permission: None,
    };
    Ok(node)
  }

  pub async fn execute_command(&self) -> anyhow::Result<String> {
    Ok("save_result".to_string())
  }

  //enterprise
  pub async fn create_widget(&self) -> anyhow::Result<WidgetDto> {
    Ok(WidgetDto {
      widget_id: None,
      name: None,
      layout: None,
    })
  }

  pub async fn delete_widget(&self) -> anyhow::Result<bool> {
    Ok(false)
  }

  pub async fn update_widget(&self) -> anyhow::Result<WidgetDto> {
    Ok(WidgetDto {
      widget_id: None,
      name: None,
      layout: None,
    })
  }

  pub async fn create_embed_link(&self) -> anyhow::Result<EmbedLinkEntitySO> {
    Ok(EmbedLinkEntitySO {
      space_id: "None".to_string(),
      node_id: "None".to_string(),
      embed_link_id: "None".to_string(),
      props: None,
    })
  }

  pub async fn get_all_embed_links(&self) -> anyhow::Result<Vec<EmbedLinkDto>> {
    let mut result = Vec::new();
    let space = EmbedLinkDto {
      link_id: "linkId".to_string(),
      url: "url".to_string(),
    };
    result.push(space);
    Ok(result)
  }

  pub async fn delete_embed_link(&self) -> anyhow::Result<bool> {
    Ok(false)
  }

  pub async fn add_view(&self) -> anyhow::Result<ViewSO> {
    Ok(ViewSO { ..Default::default() })
  }

  pub async fn copy_view(&self) -> anyhow::Result<ViewSO> {
    Ok(ViewSO { ..Default::default() })
  }

  pub async fn delete_view(&self) -> anyhow::Result<bool> {
    Ok(false)
  }

  pub async fn update_view(&self) -> anyhow::Result<ViewSO> {
    Ok(ViewSO { ..Default::default() })
  }

  pub async fn get_sub_teams(&self) -> anyhow::Result<TeamPageDto> {
    Ok(TeamPageDto {
      teams: vec![],
      total: 0,
      page_size: 0,
      page_num: 0,
    })
  }

  pub async fn create_team(&self) -> anyhow::Result<TeamDetailDto> {
    Ok(TeamDetailDto {
      team: TeamDto {
        unit_id: "unitId".to_string(),
        name: "name".to_string(),
        sequence: None,
        parent_unit_id: None,
        roles: None,
      },
    })
  }

  pub async fn update_team(&self) -> anyhow::Result<TeamDetailDto> {
    Ok(TeamDetailDto {
      team: TeamDto {
        unit_id: "unitId".to_string(),
        name: "name".to_string(),
        sequence: None,
        parent_unit_id: None,
        roles: None,
      },
    })
  }

  pub async fn delete_team(&self) -> anyhow::Result<bool> {
    Ok(false)
  }

  pub async fn get_team_members(&self) -> anyhow::Result<MemberPageDto> {
    Ok(MemberPageDto {
      members: vec![],
      total: 0,
      page_size: 0,
      page_num: 0,
    })
  }

  pub async fn get_roles(&self) -> anyhow::Result<RolePageDto> {
    Ok(RolePageDto {
      roles: vec![],
      total: 0,
      page_size: 0,
      page_num: 0,
    })
  }

  pub async fn create_role(&self) -> anyhow::Result<RoleDetailDto> {
    Ok(RoleDetailDto {
      role: RoleDto {
        unit_id: "unit_id".to_string(),
        name: "name".to_string(),
        sequence: None,
      },
    })
  }

  pub async fn update_role(&self) -> anyhow::Result<RoleDetailDto> {
    Ok(RoleDetailDto {
      role: RoleDto {
        unit_id: "unit_id".to_string(),
        name: "name".to_string(),
        sequence: None,
      },
    })
  }

  pub async fn delete_role(&self) -> anyhow::Result<bool> {
    Ok(false)
  }

  pub async fn get_role_units(&self) -> anyhow::Result<RoleUnitDetailDto> {
    Ok(RoleUnitDetailDto {
      members: vec![],
      teams: vec![],
    })
  }

  pub async fn get_member_detail(&self) -> anyhow::Result<MemberDetailDto> {
    Ok(MemberDetailDto {
      member: MemberDto { ..Default::default() },
    })
  }

  pub async fn create_member(&self) -> anyhow::Result<MemberDetailDto> {
    Ok(MemberDetailDto {
      member: MemberDto { ..Default::default() },
    })
  }

  pub async fn update_member(&self) -> anyhow::Result<MemberDetailDto> {
    Ok(MemberDetailDto {
      member: MemberDto { ..Default::default() },
    })
  }

  pub async fn delete_member(&self) -> anyhow::Result<bool> {
    Ok(false)
  }

  pub async fn create_conversation(&self) -> anyhow::Result<String> {
    Ok("create_conversation".to_string())
  }
}
