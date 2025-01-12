use std::collections::HashSet;

use actix_web::web::ReqData;
use actix_web::{delete, get, patch, post, put, web, HttpRequest, HttpResponse, Responder};

use databus_core::ro::record_update_ro::RecordUpdateRO;
use databus_core::ro::field_create_ro::FieldCreateRo;
use databus_core::shared::AuthHeader;
use databus_core::config::get_err_info;
use databus_server_lib::ros::page::PageRO;
use databus_server_lib::ros::record_query::RecordQueryRO;
use databus_server_lib::ros::record_ro::{RecordViewQueryRO, RecordDeleteRO};

use crate::shared::{ApiResponse, DataBusHttpError, DEFAULT_ERROR_CODE};
use crate::AppState;

// 给到外部开发者使用，给予token

/**
Get multiple records of a datasheet
 */
#[utoipa::path(
  get,
  operation_id = "get_record_by_datasheet_id",
  path = "/fusion/v3/datasheets/{dst_id}/records",
  responses(
      (status = 200, description = "Get Datasheet", body = ApiResponseRecordDTOs),
  ),
  params(
      ("dst_id" = String, Path, description = "dst_id"),
      ("Authorization" = String, Header, description = "Current csrf token of user"),
      PageRO,
      RecordQueryRO,
  ),
)]
#[get("/datasheets/{dst_id}/records")]
pub async fn get_records(
  state: web::Data<AppState>,
  dst_id: web::Path<String>,
  page_data: web::Query<PageRO>,
  user_data: web::Query<RecordQueryRO>,
  context: Option<ReqData<u64>>,
  req: HttpRequest,
) -> impl Responder {
  let space_id = state
    .dao_manager
    .datasheet_dao
    .get_space_id_by_dst_id(dst_id.as_ref())
    .await
    .map_err(|err| DataBusHttpError::new_with_code("Can't find the specified datasheet".to_string(), 301))?;
  let auth_header = AuthHeader {
    token: req
      .headers()
      .get("Authorization")
      .map(|token| token.to_str().unwrap().to_string()),
    ..Default::default()
  };

  let user_id: Option<String> = Some(context.unwrap().into_inner().to_string());

  let view_id = user_data.view_id.clone();

  let fields = user_data.fields.clone();

  let filter_by_formula = user_data.filter_by_formula.clone();

  if space_id.is_none() {
    println!("space_id is none")
  };
  if view_id.is_none() {
    println!("view_id is none")
  };
  if user_id.is_none() {
    println!("user_id is none")
  };

  let manager = state.data_services_manager.clone();
  // 转换dst_id为str
  let dst_id_str: &str = dst_id.as_ref();

  match manager
    .get_records(
      dst_id_str,
      user_id,
      space_id,
      view_id,
      fields,
      filter_by_formula,
      &auth_header,
    )
    .await
  {
    Ok(pack) => Ok(web::Json(ApiResponse::success(pack))),
    Err(err) => Err(DataBusHttpError::new(format!("Failed to get datasheet pack: {}", err))),
  }
}

/// Query all fields of a datasheet
///
/// All lines of the doc comment will be included to operation description.
#[utoipa::path(
  get,
  path = "/fusion/v3/datasheets/{dst_id}/fields",
  responses(
      (status = 200, description = "Get Datasheet Fields")
  ),
  params(
      ("dst_id" = String, Path, description = "dst_id"),
      ("Authorization" = String, Header, description = "Current csrf token of user"),
  )
)]
#[get("/datasheets/{dst_id}/fields")]
pub async fn get_fields(state: web::Data<AppState>) -> impl Responder {
  let dst_id = "";
  let user_id = "uskWD3scKoxH9ik7eqXHtup";
  let space_id = "";
  let manager = state.data_services_manager.clone();
  match manager.get_fields(dst_id, user_id, space_id).await {
    Ok(pack) => Ok(HttpResponse::Ok().json(ApiResponse::success(pack))),
    Err(err) => Err(DataBusHttpError {
      message: format!("Failed to get_fields: {}", err),
      error_code: DEFAULT_ERROR_CODE,
    }),
  }
}

/// Query all views of a datasheet
///
/// A datasheet can create up to 30 views and return them all at once when requesting a view, without paging.
#[utoipa::path(
    get,
    path = "/fusion/v3/datasheets/{dst_id}/views",
    responses(
      (status = 200, description = "Get Datasheet Fields")
    ),
    params(
      ("dst_id" = String, Path, description = "dst_id"),
      ("Authorization" = String, Header, description = "Current csrf token of user"),
    ),
)]
#[get("/datasheets/{dst_id}/views")]
pub async fn get_views(state: web::Data<AppState>, dst_id: web::Path<String>) -> impl Responder {
  let _user_id = "uskWD3scKoxH9ik7eqXHtup";
  let manager = state.data_services_manager.clone();
  match manager.get_views(dst_id.to_string()).await {
    Ok(pack) => Ok(HttpResponse::Ok().json(ApiResponse::success(pack))),
    Err(err) => Err(DataBusHttpError {
      message: format!("Failed to update_records_put: {}", err),
      error_code: DEFAULT_ERROR_CODE,
    }),
  }
}

/// create_fields
///
/// create_fields
#[utoipa::path(
  request_body = FieldCreateRo,
  post,
  path = "/fusion/v3/spaces/{space_id}/datasheets/{dst_id}/fields",
  responses(
        (status = 200, description = "Get Datasheet Fields")
  ),
  params(
        ("space_id" = String, Path, description = "space_id"),
        ("dst_id" = String, Path, description = "dst_id"),
        ("Authorization" = String, Header, description = "Current csrf token of user"),
  )
)]
#[post("/spaces/{space_id}/datasheets/{dst_id}/fields")]
pub async fn create_fields(
  state: web::Data<AppState>,
  path: web::Path<(String, String)>,
  body: web::Json<FieldCreateRo>,
  _context: Option<ReqData<u64>>,
  req: HttpRequest,
) -> impl Responder {
  let (_space_id, dst_id) = path.into_inner();
  let user_id = req.headers().get("Authorization").map(|token| token.to_str().unwrap().to_string()).unwrap();
  //user_id: Bearer usk42HC0vi2ZsRQw9ibB7er get only usk42HC0vi2ZsRQw9ibB7er
  let user_id = user_id.split(" ").collect::<Vec<&str>>()[1].to_string();

  let manager = state.data_services_manager.clone();
  match manager.add_fields(dst_id, body.into_inner(), user_id).await {
    Ok(pack) => Ok(HttpResponse::Ok().json(ApiResponse::success(pack))),
    Err(err) => {
      let str_err = format!("{:#?}", err);
      let (message, error_code) = get_err_info(str_err).unwrap();
      Err(DataBusHttpError {
        message,
        error_code,
      })
    }
  }
}

/// Create Datasheet
///
/// Create Datasheet and their fields
#[utoipa::path(
  post,
  path = "/fusion/v3/spaces/{space_id}/datasheets",
  responses(
      (status = 200, description = "Get Datasheet Fields")
  ),
  params(
      ("space_id" = String, Path, description = "space_id"),
      ("Authorization" = String, Header, description = "Current csrf token of user"),
  )
)]
#[post("/spaces/{space_id}/datasheets")]
pub async fn create_datasheet(state: web::Data<AppState>) -> impl Responder {
  let _user_id = "uskWD3scKoxH9ik7eqXHtup";
  let manager = state.data_services_manager.clone();
  // match manager.add_records(dst_id.to_string(), user_id.to_string(), body.into_inner()).await{
  match manager.add_datasheet_fields().await {
    Ok(pack) => Ok(HttpResponse::Ok().json(ApiResponse::success(pack))),
    Err(err) => Err(DataBusHttpError {
      message: format!("Failed to create_datasheet: {}", err),
      error_code: DEFAULT_ERROR_CODE,
    }),
  }
}

/// Delete field
///
/// Delete field
#[utoipa::path(
  delete,
  path = "/fusion/v3/spaces/{space_id}/datasheets/{dst_id}/fields/{field_id}",
  responses(
      (status = 200, description = "Get Datasheet Fields")
  ),
  params(
        ("space_id" = String, Path, description = "space_id"),
        ("dst_id" = String, Path, description = "dst_id"),
        ("field_id" = String, Path, description = "field_id"),
        ("Authorization" = String, Header, description = "Current csrf token of user"),
  )
)]
#[delete("/spaces/{space_id}/datasheets/{dst_id}/fields/{field_id}")]
pub async fn delete_fields(state: web::Data<AppState>, path: web::Path<(String, String, String)>) -> impl Responder {
  let (_space_id, _dst_id, _field_id) = path.into_inner();
  let _user_id = "uskWD3scKoxH9ik7eqXHtup";
  let manager = state.data_services_manager.clone();
  match manager.delete_fields().await {
    Ok(pack) => Ok(HttpResponse::Ok().json(ApiResponse::success(pack))),
    Err(err) => Err(DataBusHttpError {
      message: format!("Failed to create_datasheet: {}", err),
      error_code: DEFAULT_ERROR_CODE,
    }),
  }
}

/// Add multiple rows to a specified datasheet
///
///  Up to 10 records can be created in a single request.
///   You need to bring `Content-Type: application/json` in the Request Header to submit data in raw json format.
///    The POST data is a JSON object, which should contain an array: `records`, the records array contains multiple records to be created.
///    The object `fields` contains the values of the fields to be created in a record,
///    and can contain any number of field values, not necessarily all of them. If there are field defaults set,
///    field values that are not passed in will be saved according to the default values at the time the fields were set.
#[utoipa::path(
  post,
  path = "/fusion/v3/datasheets/{dst_id}/records",
  responses(
      (status = 200, description = "Add multiple rows to a specified datasheet")
  ),
  params(
      ("dst_id" = String, Path, description = "dst_id"),
      ("Authorization" = String, Header, description = "Current csrf token of user"),
  )
)]
#[post("/datasheets/{dst_id}/records")]
pub async fn add_records(
  state: web::Data<AppState>,
  dst_id: web::Path<String>,
  query: web::Query<RecordViewQueryRO>,
  body: web::Json<RecordUpdateRO>,
  context: Option<ReqData<u64>>,
  req: HttpRequest,
) -> impl Responder {
  let view_id = query.view_id.clone();
  let user_id = req.headers().get("Authorization").map(|token| token.to_str().unwrap().to_string()).unwrap();
  //user_id: Bearer usk42HC0vi2ZsRQw9ibB7er get only usk42HC0vi2ZsRQw9ibB7er
  let user_id = user_id.split(" ").collect::<Vec<&str>>()[1].to_string();
  let member_id = context.unwrap().into_inner().to_string();

  let manager = state.data_services_manager.clone();
  match manager.add_records(dst_id.to_string(), view_id, user_id, body.into_inner(), member_id).await {
    Ok(pack) => Ok(HttpResponse::Ok().json(ApiResponse::success(pack))),
    Err(err) => {
      let str_err = format!("{:#?}", err);
      let (message, error_code) = get_err_info(str_err).unwrap();
      Err(DataBusHttpError {
        message,
        error_code,
      })
    }
  }
}

/// Update Records
///
/// Update several records of a datasheet. When submitted using the PUT method, only the fields that are specified will have their data updated, and fields that are not specified will retain their old values.
#[utoipa::path(
  patch,
  path = "/fusion/v3/datasheets/{dst_id}/records",
  responses(
      (status = 200, description = "Get Datasheet")
  ),
  params(
      ("dst_id" = String, Path, description = "dst_id"),
      ("Authorization" = String, Header, description = "Current csrf token of user"),
      RecordViewQueryRO
  )
)]
#[patch("/datasheets/{dst_id}/records")]
pub async fn update_records_patch(
  state: web::Data<AppState>,
  dst_id: web::Path<String>,
  query: web::Query<RecordViewQueryRO>,
  body: web::Json<RecordUpdateRO>,
  context: Option<ReqData<u64>>,
  req: HttpRequest,
) -> impl Responder {
  let view_id = query.view_id.clone();
  let user_id = req.headers().get("Authorization").map(|token| token.to_str().unwrap().to_string()).unwrap();
  //user_id: Bearer usk42HC0vi2ZsRQw9ibB7er get only usk42HC0vi2ZsRQw9ibB7er
  let user_id = user_id.split(" ").collect::<Vec<&str>>()[1].to_string();
  let _member_id = context.unwrap().into_inner().to_string();

  let manager = state.data_services_manager.clone();
  match manager
    .update_records(dst_id.to_string(), view_id, user_id, body.into_inner())
    .await
  {
    Ok(pack) => Ok(HttpResponse::Ok().json(ApiResponse::success(pack))),
    Err(err) => {
      let str_err = format!("{:#?}", err);
      let (message, error_code) = get_err_info(str_err).unwrap();
      Err(DataBusHttpError {
        message,
        error_code,
      })
    }
  }
}

/// Update Records
///
///      Update several records of a datasheet.
///      When submitted using the PUT method, only the fields that are specified will have their data updated,
///      and fields that are not specified will retain their old values.
#[utoipa::path(
  request_body = RecordUpdateRO,
  put,
  path = "/fusion/v3/datasheets/{dst_id}/records",
  responses(
      (status = 200, description = "Update records successfully", body = ListVO),
  ),
  params(
      ("dst_id" = String, Path, description = "dst_id"),
      ("Authorization" = String, Header, description = "Current csrf token of user"),
      RecordViewQueryRO
  )
)]
#[put("/datasheets/{dst_id}/records")]
pub async fn update_records_put(
  state: web::Data<AppState>,
  dst_id: web::Path<String>,
  query: web::Query<RecordViewQueryRO>,
  body: web::Json<RecordUpdateRO>,
  context: Option<ReqData<u64>>,
  req: HttpRequest,
) -> impl Responder {
  let view_id = query.view_id.clone();
  let user_id = req.headers().get("Authorization").map(|token| token.to_str().unwrap().to_string()).unwrap();
  //user_id: Bearer usk42HC0vi2ZsRQw9ibB7er get only usk42HC0vi2ZsRQw9ibB7er
  let user_id = user_id.split(" ").collect::<Vec<&str>>()[1].to_string();
  let _member_id = context.unwrap().into_inner().to_string();

  let manager = state.data_services_manager.clone();
  match manager
    .update_records(dst_id.to_string(), view_id, user_id, body.into_inner())
    .await
  {
    Ok(pack) => Ok(HttpResponse::Ok().json(ApiResponse::success(pack))),
    Err(err) => {
      let str_err = format!("{:#?}", err);
      let (message, error_code) = get_err_info(str_err).unwrap();
      Err(DataBusHttpError {
        message,
        error_code,
      })
    }
  }
}

/// Delete records
///
/// Delete a number of records from a datasheet
#[utoipa::path(
  delete,
  path = "/fusion/v3/datasheets/{dst_id}/records",
  responses(
      (status = 200, description = "Get Datasheet successfully")
  ),
  params(
      ("dst_id" = String, Path, description = "dst_id"),
      ("Authorization" = String, Header, description = "Current csrf token of user"),
  )
)]
#[delete("/datasheets/{dst_id}/records")]
pub async fn delete_records(
  state: web::Data<AppState>,
  dst_id: web::Path<String>,
  query: web::Query<RecordDeleteRO>,
  _context: Option<ReqData<u64>>,
  req: HttpRequest,
) -> impl Responder {
  if query.record_ids.is_none() {
    let str_err = format!("api_params_empty_error:recordIds");
    let (message, error_code) = get_err_info(str_err).unwrap();
    return Err(DataBusHttpError {
      message,
      error_code,
    });
  }

  let record_ids = query.record_ids.as_ref().unwrap();
  let record_ids = record_ids.split(",").collect::<Vec<&str>>();
  // let record_ids = match record_ids {
  //   StringOrVec::S(record_id) => record_id.split(",").collect::<Vec<&str>>(),
  //   StringOrVec::V(record_ids) => record_ids.iter().map(|s| s.as_str()).collect(),
  // };
  if record_ids.len() > 10 {
    let str_err = format!("api_params_records_max_count_error:10");
    let (message, error_code) = get_err_info(str_err).unwrap();
    return Err(DataBusHttpError {
      message,
      error_code,
    });
  }

  let user_id = req.headers().get("Authorization").map(|token| token.to_str().unwrap().to_string()).unwrap();
  //user_id: Bearer usk42HC0vi2ZsRQw9ibB7er get only usk42HC0vi2ZsRQw9ibB7er
  let user_id = user_id.split(" ").collect::<Vec<&str>>()[1].to_string();

  let unique_record_ids: HashSet<String> = record_ids.into_iter().map(|record| record.to_string()).collect();
  let manager = state.data_services_manager.clone();
  match manager.delete_records(dst_id.to_string(), unique_record_ids.into_iter().collect(), user_id).await {
      Ok(_) => Ok(HttpResponse::Ok().json(ApiResponse::<()>::success_empty())),
      Err(err) => {
        let str_err = format!("{:#?}", err);
        let (message, error_code) = get_err_info(str_err).unwrap();
        Err(DataBusHttpError {
          message,
          error_code,
        })
      }
  }
}

/// get_presigned_url
///
/// Get the pre-signed URL of the datasheet attachment
#[utoipa::path(
  get,
  path = "/fusion/v3/datasheets/{dst_id}/attachments/presignedUrl",
  responses(
      (status = 200, description = "get_presigned_url successfully")
  ),
  params(
      ("dst_id" = String, Path, description = "dst_id"),
      ("Authorization" = String, Header, description = "Current csrf token of user"),
  )
)]
#[get("/datasheets/{dst_id}/attachments/presignedUrl")]
pub async fn get_presigned_url(state: web::Data<AppState>) -> impl Responder {
  let manager = state.data_services_manager.clone();
  match manager.get_upload_presigned_url().await {
    Ok(pack) => Ok(HttpResponse::Ok().json(ApiResponse::success(pack))),
    Err(err) => Err(DataBusHttpError {
      message: format!("Failed to get_presigned_url: {}", err),
      error_code: DEFAULT_ERROR_CODE,
    }),
  }
}

/// get_spaces
///
/// Query space list
#[utoipa::path(
  get,
  path = "/fusion/v3/spaces",
  responses(
      (status = 200, description = "get_spaces successfully")
  ),
  params(
      ("Authorization" = String, Header, description = "Current csrf token of user"),
  )
)]
#[get("/spaces")]
pub async fn get_spaces(state: web::Data<AppState>) -> impl Responder {
  let manager = state.data_services_manager.clone();
  match manager.get_space_list().await {
    Ok(pack) => Ok(HttpResponse::Ok().json(ApiResponse::success(pack))),
    Err(err) => Err(DataBusHttpError {
      message: format!("Failed to get_spaces: {}", err),
      error_code: DEFAULT_ERROR_CODE,
    }),
  }
}

/// get_nodes
///
/// Query the list of space station level 1 document nodes
#[utoipa::path(
  get,
  path = "/fusion/v3/spaces/{space_id}/nodes",
  responses(
      (status = 200, description = "get_nodes successfully")
  ),
  params(
      ("space_id" = String, Path, description = "space_id"),
      ("Authorization" = String, Header, description = "Current csrf token of user"),
  )
)]
#[get("/spaces/{space_id}/nodes")]
pub async fn get_nodes(state: web::Data<AppState>, _space_id: web::Path<String>) -> impl Responder {
  let manager = state.data_services_manager.clone();
  match manager.get_node_list().await {
    Ok(pack) => Ok(HttpResponse::Ok().json(ApiResponse::success(pack))),
    Err(err) => Err(DataBusHttpError {
      message: format!("Failed to get_nodes: {}", err),
      error_code: DEFAULT_ERROR_CODE,
    }),
  }
}

/// Query Node Details
///
/// Query the details of the specified file node
#[utoipa::path(
    path = "/fusion/v3/nodes/{node_id}",
    responses(
        (status = 200, description = "node_detail successfully")
    ),
    params(
        ("node_id" = String, Path, description = "node_id"),
        ("Authorization" = String, Header, description = "Current csrf token of user"),
    )
)]
#[get("/nodes/{node_id}")]
pub async fn node_detail(state: web::Data<AppState>, path: web::Path<String>) -> impl Responder {
  let _node_id = path.into_inner();
  let manager = state.data_services_manager.clone();
  match manager.get_node_detail().await {
    Ok(pack) => Ok(HttpResponse::Ok().json(ApiResponse::success(pack))),
    Err(err) => Err(DataBusHttpError {
      message: format!("Failed to node_detail: {}", err),
      error_code: DEFAULT_ERROR_CODE,
    }),
  }
}

/// Create the op of the resource
///
/// For flexibility reasons and for internal automation testing, provide an interface to freely create commands
#[utoipa::path(
    path = "/fusion/v3/datasheets/{dst_id}/executeCommand",
    responses(
        (status = 200, description = "execute_command successfully")
    ),
    params(
        ("dst_id" = String, Path, description = "dst_id"),
        ("Authorization" = String, Header, description = "Current csrf token of user"),
    )
)]
#[post("/datasheets/{dst_id}/executeCommand")]
pub async fn execute_command(state: web::Data<AppState>, path: web::Path<String>) -> impl Responder {
  let _dst_id = path.into_inner();
  let manager = state.data_services_manager.clone();
  match manager.execute_command().await {
    Ok(pack) => Ok(HttpResponse::Ok().json(ApiResponse::success(pack))),
    Err(err) => Err(DataBusHttpError {
      message: format!("Failed to execute_command: {}", err),
      error_code: DEFAULT_ERROR_CODE,
    }),
  }
}
