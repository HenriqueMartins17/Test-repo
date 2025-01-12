use actix_web::HttpResponse;

use actix_web::web::ReqData;
use actix_web::{get, web, Responder};
use databus_core::shared::AuthHeader;
use sentry_anyhow::capture_anyhow;

use crate::shared::DEFAULT_ERROR_CODE;
use crate::{ApiResponse, AppState, DataBusHttpError};
use databus_server_lib::ros::datasheet_pack_ro::DatasheetPackRO;
use databus_server_lib::ros::record_ro::RecordRO;

// 给AIServer、RoomServer、JavaServer等使用
// option参数，仅仅是为了是否调用权限，才会传入
// get_datasheet_pack(datasheet_id,: &str, space_id: Option<String>, user_id: Option<String>);

// 记住！！！ Data API 等同 ORM API！！！等于ORM做的事情的HTTP API化！！！

#[utoipa::path(
  get,
  path = "/databus/get_datasheet_pack/{id}",
  responses(
  (status = 200, description = "get data pack data", body = ApiResponseDatasheetPackSO)
  ),

  params(
      ("id", description = "datasheet id"),
      DatasheetPackRO,
  )
)]
#[get("/databus/get_datasheet_pack/{id}")]
pub async fn get_datasheet_pack(
  state: web::Data<AppState>,
  id: web::Path<String>,
  query_params: web::Query<DatasheetPackRO>,
) -> impl Responder {
  let user_id = query_params.user_id.clone();
  let space_id = query_params.space_id.clone();
  let view_id = query_params.view_id.clone();
  if user_id.is_none() {
    println!("user_id is none")
  };
  if space_id.is_none() {
    println!("space_id is none")
  }
  if view_id.is_none() {
    println!("view_id is none")
  }
  let manager = state.data_services_manager.clone();
  // let pack = match manager
  //     .get_datasheet_pack(&id.to_string(), user_id, space_id)
  //     .await.unwrap() {
  //     Ok(pack) => ApiResponse::success(pack),
  //     Err(err) => ApiResponse::error(500, &err.to_string()),
  // };

  match manager
    .get_datasheet_pack(&id.to_string(), user_id, space_id, view_id)
    .await
  {
    Ok(pack) => Ok(HttpResponse::Ok().json(ApiResponse::success(pack))),
    Err(err) => {
      capture_anyhow(&err);
      log::error!("Failed to get datasheet pack: {:?}", err);
      Err(DataBusHttpError {
        message: format!("Failed to get datasheet pack: {:#}", err),
        error_code: DEFAULT_ERROR_CODE,
      })
    }
  }
}

#[utoipa::path(
  get,
  path = "/databus/get_records",
  responses(
      (status = 200, description = "Patch completed"),
      (status = 406, description = "Not accepted"),
  ),

  params(
    RecordRO,
  )
)]
#[get("/databus/get_records")]
pub async fn get_records(
  state: web::Data<AppState>,
  query_params: web::Query<RecordRO>,
  _data: Option<ReqData<u64>>,
) -> impl Responder {
  // let user_id = data.unwrap().into_inner();

  let dst_id = query_params.dst_id.clone();
  let user_id = query_params.user_id.clone();
  let space_id = query_params.space_id.clone();
  let view_id = query_params.view_id.clone();

  if dst_id.is_none() {
    println!("dst_id is none")
  };
  if user_id.is_none() {
    println!("user_id is none")
  };
  if space_id.is_none() {
    println!("space_id is none")
  };
  if view_id.is_none() {
    println!("view_id is none")
  } else {
    println!("view_id is {:?}", view_id.as_ref().unwrap())
  }

  let manager = state.data_services_manager.clone();
  // 转换dst_id为str
  let dst_id_str: &str = dst_id.as_ref().unwrap();

  match manager
    .get_records(
      dst_id_str,
      user_id,
      space_id,
      view_id,
      None,
      None,
      &AuthHeader::default(),
    )
    .await
  {
    Ok(pack) => Ok(HttpResponse::Ok().json(ApiResponse::success(pack))),
    Err(err) => Err(DataBusHttpError {
      message: format!("Failed to get datasheet pack: {}", err),
      error_code: DEFAULT_ERROR_CODE,
    }),
  }
}
