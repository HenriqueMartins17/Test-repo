use crate::shared::DEFAULT_ERROR_CODE;
use crate::{ApiResponse, AppState, DataBusHttpError};
use actix_web::{get, web, HttpResponse, Responder};
use databus_server_lib::ros::ai::{GetAiNodeByIdRO, GetAiNodeListRO, GetAiNodeRO};

/// Get AI's binding datasheet ids by AI ID
#[utoipa::path(
  get,
  path = "/databus/dao/get_ai_datasheet_ids/{ai_id}",
  responses(
      (status = 200, description = "Get AI's datasheets success"),
  ),
  params(
      ("ai_id", description = "ai id"),
  )
)]
#[get("/databus/dao/get_ai_datasheet_ids/{ai_id}")]
pub async fn dao_get_ai_datasheet_ids(state: web::Data<AppState>, ai_id: web::Path<String>) -> impl Responder {
  let dao_manager = state.dao_manager.clone();

  let the_ai_id = &ai_id.to_string();
  let result = dao_manager.ai_dao.get_datasheet_ids_by_ai_id(the_ai_id).await;
  match result {
    Ok(datasheet_ids) => {
      if datasheet_ids.len() > 0 {
        Ok(HttpResponse::Ok().json(ApiResponse::success(datasheet_ids)))
      } else {
        // AI has no binding datasheets? It means something wrong, maybe this AI doesn't exist, return 404
        Ok(HttpResponse::Ok().json(ApiResponse::not_found(datasheet_ids)))
      }
    }
    Err(err) => {
      // Ok(HttpResponse::Ok().json(ApiResponse::error(500, &err.to_string())))
      Err(DataBusHttpError {
        message: format!("Failed to get datasheet pack: {}", err),
        error_code: DEFAULT_ERROR_CODE,
      })
    }
  }
}


/// Get AI Node by AI ID
#[utoipa::path(
get,
path = "/databus/dao/get_ai_node",
responses(
(status = 200, description = "Get AI Node success", body = ApiResponseAiPO)
),
params(
("ai_id" = String, Query, description = "ai id"),
("node_id" = String, Query, description = "node id"),
)
)]
#[get("/databus/dao/get_ai_node")]
pub async fn dao_get_ai_node(state: web::Data<AppState>, query: web::Query<GetAiNodeRO>) -> impl Responder {
  let result = state.dao_manager.ai_dao.get_ai_node(
    &query.ai_id.as_str(), query.node_id.as_str()).await;
  match result {
    Ok(res) => match res {
      Some(ai_node) => Ok(HttpResponse::Ok().json(ApiResponse::success(ai_node))),
      None => {
        let err_msg = format!("ai node not found: ai_id: {} node_id: {}",
                              &query.ai_id.to_string(), &query.node_id.to_string());
        let response = ApiResponse::<()>::error(404, err_msg.as_str());
        Ok(HttpResponse::NotFound().json(response))
      }
    },
    Err(err) => Err(DataBusHttpError {
      message: format!("Failed to get ai node: {}", err),
      error_code: DEFAULT_ERROR_CODE,
    }),
  }
}


/// Get AI Node List
#[utoipa::path(
get,
path = "/databus/dao/get_ai_node_list",
responses(
(status = 200, description = "Get AI Node success", body = ApiResponseAiPO)
),
params(
("ai_id" = String, Query, description = "ai id"),
("node_id" = String, Query, description = "node id"),
)
)]
#[get("/databus/dao/get_ai_node_list")]
pub async fn dao_get_ai_node_list(state: web::Data<AppState>, query: web::Query<GetAiNodeListRO>) -> impl Responder {
  let result = state.dao_manager.ai_dao.get_ai_node_list(
    &query.ai_id.as_str()).await;
  match result {
    Ok(data) => Ok(HttpResponse::Ok().json(ApiResponse::success(data))),
    Err(err) => Err(DataBusHttpError {
      message: format!("Failed to get ai node list: {}", err),
      error_code: DEFAULT_ERROR_CODE,
    }),
  }
}


/// Get AI Node by ID
#[utoipa::path(
get,
path = "/databus/dao/get_ai_node_by_id",
responses(
(status = 200, description = "Get AI Node success", body = ApiResponseAiPO)
),
params(
("ai_id" = String, Query, description = "ai id"),
("node_id" = String, Query, description = "node id"),
)
)]
#[get("/databus/dao/get_ai_node_by_id")]
pub async fn dao_get_ai_node_by_id(state: web::Data<AppState>, query: web::Query<GetAiNodeByIdRO>) -> impl Responder {
  let result = state.dao_manager.ai_dao.get_ai_node_by_id(
    &query.id.as_str()).await;
  match result {
    Ok(res) => match res {
      Some(ai_node) => Ok(HttpResponse::Ok().json(ApiResponse::success(ai_node))),
      None => {
        let err_msg = format!("ai node not found: id: {}",
                              &query.id.to_string());
        let response = ApiResponse::<()>::error(404, err_msg.as_str());
        Ok(HttpResponse::NotFound().json(response))
      }
    },
    Err(err) => Err(DataBusHttpError {
      message: format!("Failed to get ai node: {}", err),
      error_code: DEFAULT_ERROR_CODE,
    }),
  }
}

/// Get AI Entity by AI ID
#[utoipa::path(
  get,
  path = "/databus/dao/get_ai/{ai_id}",
  responses(
      (status = 200, description = "Get AI success", body = ApiResponseAiPO)
  ),
  params(
      ("ai_id" = String, Path, description = "ai id"),
  )
)]
#[get("/databus/dao/get_ai/{ai_id}")]
pub async fn dao_get_ai_po(state: web::Data<AppState>, ai_id: web::Path<String>) -> impl Responder {
  let result = state.dao_manager.ai_dao.get_ai(&ai_id.to_string()).await;
  match result {
    Ok(ai) => match ai {
      Some(ai) => Ok(HttpResponse::Ok().json(ApiResponse::success(ai))),
      None => {
        let err_msg = format!("ai not found: {}", &ai_id.to_string());
        let response = ApiResponse::<()>::error(404, err_msg.as_str());
        Ok(HttpResponse::NotFound().json(response))
      }
    },
    Err(err) => Err(DataBusHttpError {
      message: format!("Failed to get ai: {}", err),
      error_code: DEFAULT_ERROR_CODE,
    }),
  }
}

/// Get Datasheet's Revision number
#[utoipa::path(
  get,
  path = "/databus/dao/get_revision/{datasheet_id}",
  responses(
      (status = 200, description = "Get Revision number success"),
  ),
  params(
      ("datasheet_id", description = "datasheet_id"),
  )
)]
#[get("/databus/dao/get_revision/{datasheet_id}")]
pub async fn dao_get_revision(state: web::Data<AppState>, datasheet_id: web::Path<String>) -> impl Responder {
  let dao_manager = state.dao_manager.clone();
  let dao = dao_manager.datasheet_revision_dao.clone();

  let result = dao.get_revision_by_dst_id(&datasheet_id.to_string()).await;
  // let result = dao_manager.ai_dao.get_ai(&ai_id.to_string()).await;
  match result {
    Ok(revision) => match revision {
      Some(revision) => Ok(HttpResponse::Ok().json(ApiResponse::success(revision))),
      None => {
        let err_msg = format!("datasheet_id not found: {}", &datasheet_id.to_string());
        let response = ApiResponse::<()>::error(404, err_msg.as_str());
        Ok(HttpResponse::NotFound().json(response))
      }
    },
    Err(err) => Err(DataBusHttpError {
      message: format!("Failed to get ai: {}", err),
      error_code: DEFAULT_ERROR_CODE,
    }),
  }
}
