use std::{env, error::Error, net::Ipv4Addr};
use std::borrow::Cow;
use std::str::FromStr;
use std::sync::Arc;

use actix_cors::Cors;
use actix_web::{App, get, HttpMessage, HttpResponse, HttpServer, middleware::Logger, Responder, Result};
use actix_web::dev::ServiceRequest;
use actix_web::web;
use actix_web_httpauth::extractors::bearer::BearerAuth;
use actix_web_httpauth::middleware::HttpAuthentication;
use dotenv::dotenv;
use sentry::types::Dsn;
use utoipa::OpenApi;
use utoipa_swagger_ui::SwaggerUi;

use automation::automation_model::{
  AutomationHistoryRO, AutomationHistoryStatusRO, AutomationRobotActionRO, AutomationRobotCopyRO,
  AutomationRobotIntroductionSO, AutomationRobotRunNumsSO, AutomationRobotSO, AutomationRobotTriggerRO,
  AutomationRobotUpdateRO, AutomationSO,
};
use databus_core::DataServicesManager;
use databus_core::dtos::fusion_api_dtos::ApiRecordDto;
use databus_core::fields::property::{CollectType, IButtonStyle, IButtonAction, IOpenLink, ButtonActionType, ButtonStyleType, OpenLinkType};
use databus_core::fields::property::DateFormat;
use databus_core::fields::property::field_types::{IComputedFieldFormattingProperty, LookUpLimitType};
use databus_core::fields::property::FieldPropertySO;
use databus_core::fields::property::LinkedFields;
use databus_core::fields::property::LookUpSortField;
use databus_core::fields::property::LookUpSortInfo;
use databus_core::fields::property::RollUpFuncType;
use databus_core::fields::property::SingleSelectProperty;
use databus_core::fields::property::SingleTextFieldPropertySO;
use databus_core::fields::property::SymbolAlign;
use databus_core::fields::property::TimeFormat;
use databus_core::prelude::AlarmUser;
use databus_core::prelude::AlarmUsersType;
use databus_core::prelude::BaseDatasheetPackSO;
use databus_core::prelude::DatasheetMetaSO;
use databus_core::prelude::DatasheetPackSO;
use databus_core::prelude::DatasheetSnapshotSO;
use databus_core::prelude::FieldExtraMapValue;
use databus_core::prelude::FieldKindSO;
use databus_core::prelude::FieldSO;
use databus_core::prelude::FieldUpdatedValue;
use databus_core::prelude::NodePermissionStateSO;
use databus_core::prelude::NodeSO;
use databus_core::prelude::RecordAlarm;
use databus_core::prelude::RecordMeta;
use databus_core::prelude::RecordSO;
use databus_core::prelude::UnitSO;
use databus_core::prelude::ViewColumnSO;
use databus_core::prelude::ViewRowSO;
use databus_core::prelude::ViewSO;
use databus_core::prelude::WidgetInPanelSO;
use databus_core::prelude::WidgetPanelSO;
use databus_core::ro::record_update_ro::{FieldKeyEnum, FieldUpdateRO, RecordUpdateRO};
use databus_core::ro::field_create_ro::FieldCreateRo;
use databus_core::so::ApiValue;
use databus_core::so::CommentMsg;
use databus_core::so::Comments;
use databus_core::so::view_mod::color::ColorOption;
use databus_core::so::view_mod::color::GanttColorOption;
use databus_core::so::view_mod::constants::AnyBaseField;
use databus_core::so::view_mod::constants::GanttColorType;
use databus_core::so::view_mod::style::ViewStyleSo;
use databus_core::so::view_mod::types::IViewLockInfo;
use databus_core::so::view_mod::view_operation::filter::FilterConjunction;
use databus_core::so::view_mod::view_operation::filter::FOperator;
use databus_core::so::view_mod::view_operation::filter::IFilterCondition;
use databus_core::so::view_mod::view_operation::filter::IFilterInfo;
use databus_core::so::view_mod::view_operation::sort::ISortedField;
use databus_core::so::view_mod::view_operation::sort::ISortInfo;
use databus_core::vo::list_vo::ListVO;
use databus_core::vo::record_vo::RecordDTO;
use databus_dao_db::{AiNode, AiPO, DAOManager, DBDataSourceProvider};
use databus_dao_db::model::{
  AutomationActionIntroductionPO, AutomationActionPO, AutomationRobotIntroductionPO, AutomationRunHistoryPO,
  AutomationTriggerIntroductionPO, AutomationTriggerPO, AutomationTriggerSO,
};
use databus_dao_db::node::model::NodeSimplePO;
use databus_server_lib::ros::page::{OrderEnum, SortRO};
use databus_server_lib::ros::record_query::CellFormatEnum;
use databus_shared::env_var;
use document::document_model::{DocumentOperationRO, DocumentPropsRO, DocumentRO};
use shared::*;

use crate::automation::automation_dao_api;
use crate::document::document_dao_api;
use crate::server_var::{SENTRY_DSN, SENTRY_ENV, SERVER_PORT};

mod data_dao_api;
mod data_services_api;
mod enterprise_fusion_api;
mod fusion_api;

mod automation;
mod document;
mod server_var;
mod shared;
mod util;
// #[utoipa::path(
//     request_body = Value,
//     responses(
//         (status = 200, description = "Patch completed"),
//         (status = 406, description = "Not accepted"),
//     ),
//     security(
//         ("api_key" = [])
//     ),
// )]
// #[patch("/patch_raw")]
// pub async fn patch_raw(body: Json<Value>) -> Result<impl Responder> {
//   let value: Value = body.into_inner();
//   eprintln!("body = {:?}", value);
//   Ok(HttpResponse::Ok())
// }

// #[utoipa::path(
//   responses(
//     (status = 200, description = "Home")
//   )
// )]
// #[get("/")]
// pub async fn home() -> impl Responder {
//   HttpResponse::Ok().body("Databus Server is running!")
// }

/// Homepage, for liveness check
#[utoipa::path(
  responses(
    (status = 200, description = "Databus Home")
  )
)]
#[get("/databus")]
pub async fn databus_home() -> impl Responder {
  HttpResponse::Ok().body("Databus Main Server is running!")
}

/// auto detection of paths/schemas for documentation https://github.com/juhaku/utoipa/issues/624
#[derive(OpenApi)]
#[openapi(
  info(description = "databus-server APIs"),
  paths(
    databus_home,
    fusion_api::get_records,
    fusion_api::get_fields,
    fusion_api::get_views,
    fusion_api::create_fields,
    fusion_api::create_fields,
    fusion_api::create_datasheet,
    fusion_api::delete_fields,
    fusion_api::add_records,
    fusion_api::update_records_patch,
    fusion_api::update_records_put,
    fusion_api::delete_records,
    fusion_api::get_presigned_url,
    fusion_api::get_spaces,
    fusion_api::get_nodes,
    fusion_api::node_detail,
    fusion_api::execute_command,
    enterprise_fusion_api::create_widget,
    enterprise_fusion_api::delete_widget,
    enterprise_fusion_api::update_widget,
    enterprise_fusion_api::create_embed_link,
    enterprise_fusion_api::get_embed_link_list,
    enterprise_fusion_api::delete_embed_link,
    enterprise_fusion_api::create_view,
    enterprise_fusion_api::copy_view,
    enterprise_fusion_api::delete_view,
    enterprise_fusion_api::batch_delete_view,
    enterprise_fusion_api::update_view,
    enterprise_fusion_api::sub_team_list,
    enterprise_fusion_api::create_team,
    enterprise_fusion_api::update_team,
    enterprise_fusion_api::delete_team,
    enterprise_fusion_api::team_member_list,
    enterprise_fusion_api::role_list,
    enterprise_fusion_api::create_role,
    enterprise_fusion_api::update_role,
    enterprise_fusion_api::delete_role,
    enterprise_fusion_api::role_unit_list,
    enterprise_fusion_api::member_detail,
    enterprise_fusion_api::create_member,
    enterprise_fusion_api::update_member,
    enterprise_fusion_api::delete_member,
    enterprise_fusion_api::create_chat_completion,
    data_services_api::get_records,
    data_services_api::get_datasheet_pack,
    data_dao_api::dao_get_ai_datasheet_ids,
    data_dao_api::dao_get_ai_po,
    data_dao_api::dao_get_ai_node,
    data_dao_api::dao_get_revision,
    automation_dao_api::dao_get_automation_run_history,
    automation_dao_api::dao_get_automation_run_history_detail,
    automation_dao_api::dao_get_automation_run_context,
    automation_dao_api::dao_update_automation_run_history_status,
    automation_dao_api::dao_create_automation_run_history,
    automation_dao_api::dao_get_robots_triggers,
    automation_dao_api::dao_get_robots_by_resource_id,
    automation_dao_api::dao_get_robot_by_robot_id,
    automation_dao_api::dao_update_automation_robot,
    automation_dao_api::dao_copy_automation_robot,
    automation_dao_api::dao_create_or_update_automation_robot_trigger,
    automation_dao_api::dao_create_or_update_automation_robot_action,
    document_dao_api::dao_get_new_document_name,
    document_dao_api::dao_get_document_data,
    document_dao_api::dao_create_or_update_document,
    document_dao_api::dao_update_document_props,
    document_dao_api::dao_create_document_operation,
    automation_dao_api::dao_get_robot_runs_by_space_id,
  ),
  components(schemas(
    AiPO,
    AiNode,
    ApiResponseAiPO,
    ApiResponseEmptySO,
    ApiResponseDatasheetPackSO,
    DatasheetPackSO,
    NodeSimplePO,
    BaseDatasheetPackSO,
    UnitSO,
    NodePermissionStateSO,
    DatasheetMetaSO,
    DatasheetSnapshotSO,
    ViewSO,
    WidgetPanelSO,
    WidgetInPanelSO,
    ViewColumnSO,
    FieldSO,
    IViewLockInfo,
    ViewStyleSo,
    AnyBaseField,
    GanttColorOption,
    ColorOption,
    Comments,
    ApiValue,
    CommentMsg,
    GanttColorType,
    IFilterInfo,
    FilterConjunction,
    IFilterCondition,
    FOperator,
    ViewRowSO,
    CollectType,
    DateFormat,
    SymbolAlign,
    SingleSelectProperty,
    RollUpFuncType,
    LinkedFields,
    SingleTextFieldPropertySO,
    TimeFormat,
    IComputedFieldFormattingProperty,
    LookUpSortInfo,
    LookUpSortField,
    LookUpLimitType,
    FieldKindSO,
    FieldPropertySO,
    RecordSO,
    RecordMeta,
    FieldExtraMapValue,
    FieldUpdatedValue,
    RecordAlarm,
    AlarmUser,
    AlarmUsersType,
    ISortedField,
    ISortInfo,
    NodeSO,
    RecordUpdateRO,
    FieldUpdateRO,
    FieldKeyEnum,
    ListVO,
    ApiRecordDto,
    AutomationSO,
    AutomationRobotSO,
    AutomationActionPO,
    AutomationActionPO,
    AutomationTriggerPO,
    AutomationHistoryRO,
    AutomationTriggerSO,
    AutomationRobotCopyRO,
    AutomationRunHistoryPO,
    AutomationRobotUpdateRO,
    ApiResponseAutomationSO,
    AutomationRobotActionRO,
    AutomationRobotTriggerRO,
    AutomationHistoryStatusRO,
    AutomationRobotIntroductionSO,
    AutomationRobotIntroductionPO,
    ApiResponseAutomationActionPO,
    AutomationActionIntroductionPO,
    ApiResponseAutomationTriggerSO,
    AutomationTriggerIntroductionPO,
    ApiResponseAutomationRunHistoryPO,
    ApiResponseAutomationRobotIntroductionSO,
    CellFormatEnum,
    OrderEnum,
    SortRO,
    ApiResponseRecordDTOs,
    RecordDTO,
    DocumentOperationRO,
    DocumentPropsRO,
    DocumentRO,
    AutomationRobotRunNumsSO,
    ApiResponseAutomationRobotRunNumsSO,
    FieldCreateRo,
    IButtonStyle,
    IButtonAction,
    IOpenLink,
    ButtonActionType,
    ButtonStyleType,
    OpenLinkType
  ))
)]
struct ApiDoc;

pub struct AppState {
  data_services_manager: Arc<DataServicesManager>,
  dao_manager: Arc<DAOManager>,
}

#[actix_web::main]
async fn main() -> Result<(), impl Error> {
  dotenv().ok();

  let loader = Arc::new(DBDataSourceProvider::ainit().await);

  let clone_loader = loader.clone();
  let data_services_manager = Arc::new(databus_core::init(true, true, "TODO".to_string(), loader));

  let openapi = ApiDoc::openapi();

  let host = Ipv4Addr::UNSPECIFIED;
  let port = env::var(SERVER_PORT)
    .ok()
    .and_then(|port| port.parse().ok())
    .unwrap_or(8625);
  println!("Running DataBus-Server: http://{}:{}/databus/docs/", host, port);

  // sentry
  let sentry_dsn = Dsn::from_str(&*env::var(SENTRY_DSN).unwrap_or_default());
  let sentry_env = env_var!(ENV default SENTRY_ENV).as_mut_str().to_owned();
  let _guard = sentry::init(sentry::ClientOptions {
    dsn: sentry_dsn.ok(),
    release: sentry::release_name!(),
    debug: if sentry_env.eq(SENTRY_ENV) { true } else { false },
    environment: Option::from(Cow::Owned(sentry_env)),
    ..Default::default()
  });

  HttpServer::new(move || {
    let cors = Cors::permissive();
    App::new()
      .app_data(web::Data::new(AppState {
        data_services_manager: data_services_manager.clone(),
        dao_manager: clone_loader.get_dao_manager().clone(),
      }))
      .wrap(cors)
      .wrap(Logger::default())
      .wrap(sentry_actix::Sentry::new())
      .service(databus_home)
      .service(
        web::scope("/fusion/v3")
          .wrap(HttpAuthentication::with_fn(validator))
          .service(fusion_api::get_records)
          .service(fusion_api::add_records)
          .service(fusion_api::update_records_patch)
          .service(fusion_api::update_records_put)
          .service(fusion_api::get_fields)
          .service(fusion_api::create_fields)
          .service(fusion_api::delete_fields)
          .service(fusion_api::get_views)
          .service(fusion_api::create_datasheet)
          .service(fusion_api::delete_records)
          .service(fusion_api::get_presigned_url)
          .service(fusion_api::get_spaces)
          .service(fusion_api::get_nodes)
          .service(fusion_api::node_detail)
          .service(fusion_api::execute_command)
          .service(enterprise_fusion_api::create_widget)
          .service(enterprise_fusion_api::delete_widget)
          .service(enterprise_fusion_api::update_widget)
          .service(enterprise_fusion_api::create_embed_link)
          .service(enterprise_fusion_api::get_embed_link_list)
          .service(enterprise_fusion_api::delete_embed_link)
          .service(enterprise_fusion_api::create_view)
          .service(enterprise_fusion_api::copy_view)
          .service(enterprise_fusion_api::delete_view)
          .service(enterprise_fusion_api::batch_delete_view)
          .service(enterprise_fusion_api::update_view)
          .service(enterprise_fusion_api::sub_team_list)
          .service(enterprise_fusion_api::create_team)
          .service(enterprise_fusion_api::update_team)
          .service(enterprise_fusion_api::delete_team)
          .service(enterprise_fusion_api::team_member_list)
          .service(enterprise_fusion_api::role_list)
          .service(enterprise_fusion_api::create_role)
          .service(enterprise_fusion_api::update_role)
          .service(enterprise_fusion_api::delete_role)
          .service(enterprise_fusion_api::role_unit_list)
          .service(enterprise_fusion_api::member_detail)
          .service(enterprise_fusion_api::create_member)
          .service(enterprise_fusion_api::update_member)
          .service(enterprise_fusion_api::delete_member)
          .service(enterprise_fusion_api::create_chat_completion),
      )
      .service(data_services_api::get_records)
      .service(data_dao_api::dao_get_ai_datasheet_ids)
      .service(data_dao_api::dao_get_ai_po)
      .service(data_dao_api::dao_get_ai_node)
      .service(data_dao_api::dao_get_ai_node_list)
      .service(data_dao_api::dao_get_ai_node_by_id)
      .service(data_services_api::get_datasheet_pack)
      .service(data_dao_api::dao_get_revision)
      .service(
        web::scope("/databus/dao/automations")
          .service(automation_dao_api::dao_get_automation_run_history)
          .service(automation_dao_api::dao_get_automation_run_history_detail)
          .service(automation_dao_api::dao_get_automation_run_context)
          .service(automation_dao_api::dao_update_automation_run_history_status)
          .service(automation_dao_api::dao_create_automation_run_history)
          .service(automation_dao_api::dao_get_robots_triggers)
          .service(automation_dao_api::dao_get_robots_by_resource_id)
          .service(automation_dao_api::dao_get_robot_by_robot_id)
          .service(automation_dao_api::dao_update_automation_robot)
          .service(automation_dao_api::dao_create_or_update_automation_robot_trigger)
          .service(automation_dao_api::dao_create_or_update_automation_robot_action)
          .service(automation_dao_api::dao_copy_automation_robot)
          .service(automation_dao_api::dao_get_robot_runs_by_space_id),
      )
      .service(
        web::scope("/databus/dao/documents")
          .service(document_dao_api::dao_get_new_document_name)
          .service(document_dao_api::dao_get_document_data)
          .service(document_dao_api::dao_create_or_update_document)
          .service(document_dao_api::dao_update_document_props)
          .service(document_dao_api::dao_create_document_operation),
      )
      .service(SwaggerUi::new("/databus/docs/{_:.*}").url("/databus/api-docs/openapi.json", openapi.clone()))
  })
  .bind((host, port))?
  .run()
  .await
}

async fn validator(
  req: ServiceRequest,
  credentials: Option<BearerAuth>,
) -> Result<ServiceRequest, (actix_web::Error, ServiceRequest)> {
  if credentials.is_none() {
    return Err((
      actix_web::Error::from(DataBusHttpError {
        message: "Unauthorized".to_string(),
        error_code: 401,
      }),
      req,
    ));
  }
  let state = req.app_data::<web::Data<AppState>>().unwrap();
  let dao_manager = state.dao_manager.clone();
  let result = dao_manager
    .user_dao
    .get_user_id_by_api_key(credentials.unwrap().token())
    .await;
  match result {
    Ok(Some(user_id)) => {
      req.extensions_mut().insert(user_id);
      Ok(req)
    }
    Err(err) => Err((
      actix_web::Error::from(DataBusHttpError {
        message: format!("system error: {}", err),
        error_code: DEFAULT_ERROR_CODE,
      }),
      req,
    )),
    Ok(None) => Err((
      actix_web::Error::from(DataBusHttpError {
        message: "Unauthorized".to_string(),
        error_code: 401,
      }),
      req,
    )),
  }
}
