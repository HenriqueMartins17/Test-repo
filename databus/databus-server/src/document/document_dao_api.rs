use crate::document::document_model::{DocumentOperationRO, DocumentPropsRO, DocumentRO};
use crate::shared::ApiResponse;
use crate::AppState;
use actix_web::http::StatusCode;
use actix_web::{get, patch, post, put, web, HttpResponse, Responder};
use databus_core::utils::uuid::IdUtil;


/// Get new document name
#[utoipa::path(
  get,
  path = "/databus/dao/documents/name",
  responses(
    (status = 200, description = "Get new document name successfully"),
  )
)]
#[get("/name")]
pub async fn dao_get_new_document_name(state: web::Data<AppState>) -> impl Responder {
  let dao_manager = state.dao_manager.clone();
  let dao = dao_manager.document_manager_dao.clone();
  loop {
    let name = IdUtil::create_document_name();
    if !dao.document_dao.is_document_exist(&name).await.unwrap() {
      return HttpResponse::Ok().json(ApiResponse::success(name));
    }
  }
}

/// Get document data
#[utoipa::path(
  get,
  path = "/databus/dao/documents/{document_name}/data",
  responses(
    (status = 200, description = "Get document data successfully"),
  ),
  params(
    ("document_name", description = "document name"),
  )
)]
#[get("/{document_name}/data")]
pub async fn dao_get_document_data(
  state: web::Data<AppState>,
  document_name: web::Path<String>,
) -> impl Responder {
  let dao_manager = state.dao_manager.clone();
  let dao = dao_manager.document_manager_dao.clone();
  let result = dao
    .document_dao
    .select_data_by_name(&document_name)
    .await;
  match result {
    Ok(data) => HttpResponse::Ok().json(ApiResponse::success(data)),
    Err(err) => {
      HttpResponse::Ok().json(ApiResponse::<()>::error(
        StatusCode::INTERNAL_SERVER_ERROR.as_u16(), &err.to_string()
      ))
    }
  }
}

/// Create or update document
#[utoipa::path(
  put,
  path = "/databus/dao/documents/{document_name}",
  responses(
    (status = 200, description = "Create or update document successfully"),
  ),
  params(
    ("document_name", description = "document name")
  ),
  request_body = DocumentRO
)]
#[put("/{document_name}")]
pub async fn dao_create_or_update_document(
  state: web::Data<AppState>,
  document_name: web::Path<String>,
  body: web::Json<DocumentRO>,
) -> impl Responder {
  let dao_manager = state.dao_manager.clone();
  let dao = dao_manager.document_manager_dao.clone();
  if dao.document_dao.is_document_exist(&document_name).await.unwrap() {
    dao
      .document_dao
      .update_by_name(&document_name, &body.data, body.title.clone(), body.updated_by)
      .await;
  } else {
    dao
      .document_dao
      .create(
        &body.space_id,
        &body.resource_id,
        &body.document_type,
        &document_name,
        &body.data,
        body.props.clone(), 
        body.title.clone(), 
        body.updated_by
      )
      .await;
  }
  HttpResponse::Ok().json(ApiResponse::<()>::success_empty())
}

/// batch update document props
#[utoipa::path(
  patch,
  path = "/databus/dao/documents/props",
  responses(
    (status = 200, description = "batch update document props successfully"),
  ),
  request_body = DocumentPropsRO,
)]
#[patch("/props")]
pub async fn dao_update_document_props (
  state: web::Data<AppState>,
  body: web::Json<DocumentPropsRO>,
) -> impl Responder {
  let dao_manager = state.dao_manager.clone();
  let dao = dao_manager.document_manager_dao.clone();
  dao
    .document_dao
    .update_props(&body.resource_id, &body.document_names, &body.record_id)
    .await;
  HttpResponse::Ok().json(ApiResponse::<()>::success_empty())
}

/// create document operation success 
#[utoipa::path(
  post,
  path = "/databus/dao/documents/{document_name}/operations",
  responses(
    (status = 200, description = "create document operation successfully"),
  ),
  params(
    ("document_name", description = "document name")
  ),
  request_body = DocumentOperationRO,
)]
#[post("/{document_name}/operations")]
pub async fn dao_create_document_operation (
  state: web::Data<AppState>,
  document_name: web::Path<String>,
  body: web::Json<DocumentOperationRO>,
) -> impl Responder {
  let dao_manager = state.dao_manager.clone();
  let dao = dao_manager.document_manager_dao.clone();
  dao
    .document_operation_dao
    .create(&body.space_id, &document_name, &body.update_data, body.created_by.clone())
    .await;
  HttpResponse::Ok().json(ApiResponse::<()>::success_empty())
}