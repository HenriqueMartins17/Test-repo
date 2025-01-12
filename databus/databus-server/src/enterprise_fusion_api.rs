use actix_web::{delete, get, HttpResponse, post, put, Responder, web};





use crate::AppState;



use crate::shared::{ApiResponse, DataBusHttpError, DEFAULT_ERROR_CODE};

/// create_widget
///
/// Add a widget to a specified dashboard
#[utoipa::path(
    post,
    path = "/fusion/v3/dashboards/{dashboard_id}/widgets",
    responses(
        (status = 200, description = "create_widget successfully")
    ),
    params(
        ("dashboard_id" = String, Path, description = "dashboard_id"),
        ("Authorization" = String, Header, description = "Current csrf token of user"),
    )
)]
#[post("/dashboards/{dashboard_id}/widgets")]
pub async fn create_widget(
    _state: web::Data<AppState>,
    _dashboard_id: web::Path<String>,
) -> impl Responder {
    let manager = _state.data_services_manager.clone();
    match manager.create_widget().await{
        Ok(pack) => Ok(HttpResponse::Ok().json(ApiResponse::success(pack))),
        Err(err) => Err(DataBusHttpError {
            message: format!("Failed to create_widget: {}", err),
            error_code: DEFAULT_ERROR_CODE,
        }),
    }
}

/// delete_widget
///
/// delete widget in a specified dashboard
#[utoipa::path(
    delete,
    path = "/fusion/v3/dashboards/{dashboard_id}/widgets/{widget_id}",
    responses(
        (status = 200, description = "delete_widget successfully")
    ),
    params(
        ("dashboard_id" = String, Path, description = "dashboard_id"),
        ("widget_id" = String, Path, description = "widget_id"),
        ("Authorization" = String, Header, description = "Current csrf token of user"),
    )
)]
#[delete("/dashboards/{dashboard_id}/widgets/{widget_id}")]
pub async fn delete_widget(
    _state: web::Data<AppState>,
    path: web::Path<(String, String)>,
) -> impl Responder {
let (_dashboard_id, _widget_id) = path.into_inner();
let manager = _state.data_services_manager.clone();
match manager.delete_widget().await{
    Ok(pack) => Ok(HttpResponse::Ok().json(ApiResponse::success(pack))),
    Err(err) => Err(DataBusHttpError {
        message: format!("Failed to delete_widget: {}", err),
        error_code: DEFAULT_ERROR_CODE,
    }),
}
}

/// update_widget
///
/// modify widget in a specified dashboard
#[utoipa::path(
    put,
    path = "/fusion/v3/dashboards/{dashboard_id}/widgets/{widget_id}",
    responses(
        (status = 200, description = "update_widget successfully")
    ),
    params(
        ("dashboard_id" = String, Path, description = "dashboard_id"),
        ("widget_id" = String, Path, description = "widget_id"),
        ("Authorization" = String, Header, description = "Current csrf token of user"),
    )
)]
#[put("/dashboards/{dashboard_id}/widgets/{widget_id}")]
pub async fn update_widget(
    _state: web::Data<AppState>,
    path: web::Path<(String, String)>,
) -> impl Responder {
let (_dashboard_id, _widget_id) = path.into_inner();
    let manager = _state.data_services_manager.clone();
    match manager.update_widget().await{
        Ok(pack) => Ok(HttpResponse::Ok().json(ApiResponse::success(pack))),
        Err(err) => Err(DataBusHttpError {
            message: format!("Failed to update_widget: {}", err),
            error_code: DEFAULT_ERROR_CODE,
        }),
    }
}

/// create_embed_link
///
/// Creates an "embed link" for the specified node
#[utoipa::path(
    post,
    path = "/fusion/v3/spaces/{space_id}/nodes/{node_id}/embedlinks",
    responses(
        (status = 200, description = "create_embed_link successfully")
    ),
    params(
        ("space_id" = String, Path, description = "space_id"),
        ("node_id" = String, Path, description = "node_id"),
        ("Authorization" = String, Header, description = "Current csrf token of user"),
    )
)]
#[post("/spaces/{space_id}/nodes/{node_id}/embedlinks")]
pub async fn create_embed_link(
    _state: web::Data<AppState>,
    path: web::Path<(String, String)>,
) -> impl Responder {
    let (_space_id, _node_id) = path.into_inner();
    let manager = _state.data_services_manager.clone();
    match manager.create_embed_link().await{
        Ok(pack) => Ok(HttpResponse::Ok().json(ApiResponse::success(pack))),
        Err(err) => Err(DataBusHttpError {
            message: format!("Failed to create_embed_link: {}", err),
            error_code: DEFAULT_ERROR_CODE,
        }),
    }
}

/// get_embed_link_list
///
/// Get all embedded links for a specified node
#[utoipa::path(
    get,
    path = "/fusion/v3/spaces/{space_id}/nodes/{node_id}/embedlinks",
    responses(
        (status = 200, description = "get_embed_link_list successfully")
    ),
    params(
        ("space_id" = String, Path, description = "space_id"),
        ("node_id" = String, Path, description = "node_id"),
        ("Authorization" = String, Header, description = "Current csrf token of user"),
    )
)]
#[get("/spaces/{space_id}/nodes/{node_id}/embedlinks")]
pub async fn get_embed_link_list(
    _state: web::Data<AppState>,
    path: web::Path<(String, String)>,
) -> impl Responder {
    let (_space_id, _node_id) = path.into_inner();
    let manager = _state.data_services_manager.clone();
    match manager.get_all_embed_links().await{
        Ok(pack) => Ok(HttpResponse::Ok().json(ApiResponse::success(pack))),
        Err(err) => Err(DataBusHttpError {
            message: format!("Failed to get_embed_link_list: {}", err),
            error_code: DEFAULT_ERROR_CODE,
        }),
    }
}

/// delete_embed_link
///
/// Removes the specified Advanced Embed link. After deleted, the link cannot be accessed.
#[utoipa::path(
    path = "/fusion/v3/spaces/{space_id}/nodes/{node_id}/embedlinks/{link_id}",
    responses(
        (status = 200, description = "delete_embed_link successfully")
    ),
    params(
        ("space_id" = String, Path, description = "space_id"),
        ("node_id" = String, Path, description = "node_id"),
        ("link_id" = String, Path, description = "link_id"),
        ("Authorization" = String, Header, description = "Current csrf token of user"),
    )
)]
#[delete("/spaces/{space_id}/nodes/{node_id}/embedlinks/{link_id}")]
pub async fn delete_embed_link(
    _state: web::Data<AppState>,
    path: web::Path<(String, String, String)>,
) -> impl Responder {
    let (_space_id, _node_id, _link_id) = path.into_inner();
    let manager = _state.data_services_manager.clone();
    match manager.delete_embed_link().await{
        Ok(pack) => Ok(HttpResponse::Ok().json(ApiResponse::success(pack))),
        Err(err) => Err(DataBusHttpError {
            message: format!("Failed to delete_embed_link: {}", err),
            error_code: DEFAULT_ERROR_CODE,
        }),
    }
}

/// create_view
///
/// Add a view to a specified datasheet
#[utoipa::path(
    path = "/fusion/v3/datasheets/{dst_id}/views",
    responses(
        (status = 200, description = "create_view successfully")
    ),
    params(
        ("dst_id" = String, Path, description = "dst_id"),
        ("Authorization" = String, Header, description = "Current csrf token of user"),
    )
)]
#[post("/datasheets/{dst_id}/views")]
pub async fn create_view(
    _state: web::Data<AppState>,
    path: web::Path<String>,
) -> impl Responder {
    let _dst_id = path.into_inner();
    let manager = _state.data_services_manager.clone();
    match manager.add_view().await{
        Ok(pack) => Ok(HttpResponse::Ok().json(ApiResponse::success(pack))),
        Err(err) => Err(DataBusHttpError {
            message: format!("Failed to create_view: {}", err),
            error_code: DEFAULT_ERROR_CODE,
        }),
    }
}

/// copy_view
///
/// copy a view at a specified datasheet
#[utoipa::path(
    path = "/fusion/v3/datasheets/{dst_id}/views/{view_id}/duplicate",
    responses(
        (status = 200, description = "copy_view successfully")
    ),
    params(
        ("dst_id" = String, Path, description = "dst_id"),
        ("view_id" = String, Path, description = "view_id"),
        ("Authorization" = String, Header, description = "Current csrf token of user"),
    )
)]
#[post("/datasheets/{dst_id}/views/{view_id}/duplicate")]
pub async fn copy_view(
    _state: web::Data<AppState>,
    path: web::Path<(String, String)>,
) -> impl Responder {
    let (_dst_id, _view_id) = path.into_inner();
    let manager = _state.data_services_manager.clone();
    match manager.copy_view().await{
        Ok(pack) => Ok(HttpResponse::Ok().json(ApiResponse::success(pack))),
        Err(err) => Err(DataBusHttpError {
            message: format!("Failed to copy_view: {}", err),
            error_code: DEFAULT_ERROR_CODE,
        }),
    }
}

/// delete_view
///
/// Delete a view in a specified datasheet
#[utoipa::path(
    path = "/fusion/v3/datasheets/{dst_id}/views/{view_id}",
    responses(
        (status = 200, description = "delete_view successfully")
    ),
    params(
        ("dst_id" = String, Path, description = "dst_id"),
        ("view_id" = String, Path, description = "view_id"),
        ("Authorization" = String, Header, description = "Current csrf token of user"),
    )
)]
#[delete("/datasheets/{dst_id}/views/{view_id}")]
pub async fn delete_view(
    _state: web::Data<AppState>,
    path: web::Path<(String, String)>,
) -> impl Responder {
    let (_dst_id, _view_id) = path.into_inner();
    let manager = _state.data_services_manager.clone();
    match manager.delete_view().await{
        Ok(pack) => Ok(HttpResponse::Ok().json(ApiResponse::success(pack))),
        Err(err) => Err(DataBusHttpError {
            message: format!("Failed to delete_view: {}", err),
            error_code: DEFAULT_ERROR_CODE,
        }),
    }
}

/// batch_delete_view
///
/// Batch delete views in a specified datasheet
#[utoipa::path(
    path = "/fusion/v3/datasheets/{dst_id}/views",
    responses(
        (status = 200, description = "batch_delete_view successfully")
    ),
    params(
        ("dst_id" = String, Path, description = "dst_id"),
        ("Authorization" = String, Header, description = "Current csrf token of user"),
    )
)]
#[delete("/datasheets/{dst_id}/views")]
pub async fn batch_delete_view(
    _state: web::Data<AppState>,
    path: web::Path<String>,
) -> impl Responder {
    let _dst_id = path.into_inner();
    let manager = _state.data_services_manager.clone();
    match manager.delete_view().await{
        Ok(pack) => Ok(HttpResponse::Ok().json(ApiResponse::success(pack))),
        Err(err) => Err(DataBusHttpError {
            message: format!("Failed to batch_delete_view: {}", err),
            error_code: DEFAULT_ERROR_CODE,
        }),
    }
}

/// update_view
///
/// update a view in a specified datasheet
#[utoipa::path(
    path = "/fusion/v3/datasheets/{dst_id}/views/{view_id}",
    responses(
        (status = 200, description = "update_view successfully")
    ),
    params(
        ("dst_id" = String, Path, description = "dst_id"),
        ("view_id" = String, Path, description = "view_id"),
        ("Authorization" = String, Header, description = "Current csrf token of user"),
    )
)]
#[put("/datasheets/{dst_id}/views/{view_id}")]
pub async fn update_view(
    _state: web::Data<AppState>,
    path: web::Path<(String, String)>,
) -> impl Responder {
    let (_dst_id, _view_id) = path.into_inner();
    let manager = _state.data_services_manager.clone();
    match manager.update_view().await{
        Ok(pack) => Ok(HttpResponse::Ok().json(ApiResponse::success(pack))),
        Err(err) => Err(DataBusHttpError {
            message: format!("Failed to update_view: {}", err),
            error_code: DEFAULT_ERROR_CODE,
        }),
    }
}

/// sub_team_list
///
/// Get the list of sub teams of a team by UnitId.
#[utoipa::path(
    path = "/fusion/v3/spaces/{space_id}/teams/{unit_id}/children",
    responses(
        (status = 200, description = "sub_team_list successfully")
    ),
    params(
        ("space_id" = String, Path, description = "space_id"),
        ("unit_id" = String, Path, description = "unit_id"),
        ("Authorization" = String, Header, description = "Current csrf token of user"),
    )
)]
#[get("/spaces/{space_id}/teams/{unit_id}/children")]
pub async fn sub_team_list(
    _state: web::Data<AppState>,
    path: web::Path<(String, String)>,
) -> impl Responder {
    let (_space_id, _unit_id) = path.into_inner();
    let manager = _state.data_services_manager.clone();
    match manager.get_sub_teams().await{
        Ok(pack) => Ok(HttpResponse::Ok().json(ApiResponse::success(pack))),
        Err(err) => Err(DataBusHttpError {
            message: format!("Failed to sub_team_list: {}", err),
            error_code: DEFAULT_ERROR_CODE,
        }),
    }
}

/// create_team
///
/// Create a team for a specified space.
#[utoipa::path(
    path = "/fusion/v3/spaces/{space_id}/teams",
    responses(
        (status = 200, description = "create_team successfully")
    ),
    params(
        ("space_id" = String, Path, description = "space_id"),
        ("Authorization" = String, Header, description = "Current csrf token of user"),
    )
)]
#[post("/spaces/{space_id}/teams")]
pub async fn create_team(
    _state: web::Data<AppState>,
    path: web::Path<String>,
) -> impl Responder {
    let _space_id = path.into_inner();
    let manager = _state.data_services_manager.clone();
    match manager.create_team().await{
        Ok(pack) => Ok(HttpResponse::Ok().json(ApiResponse::success(pack))),
        Err(err) => Err(DataBusHttpError {
            message: format!("Failed to create_team: {}", err),
            error_code: DEFAULT_ERROR_CODE,
        }),
    }
}

/// update_team
///
/// Update a for a specified space
#[utoipa::path(
    path = "/fusion/v3/spaces/{space_id}/teams/{unit_id}",
    responses(
        (status = 200, description = "update_team successfully")
    ),
    params(
        ("space_id" = String, Path, description = "space_id"),
        ("unit_id" = String, Path, description = "unit_id"),
        ("Authorization" = String, Header, description = "Current csrf token of user"),
    )
)]
#[put("/spaces/{space_id}/teams/{unit_id}")]
pub async fn update_team(
    _state: web::Data<AppState>,
    path: web::Path<(String, String)>,
) -> impl Responder {
    let (_space_id, _unit_id) = path.into_inner();
    let manager = _state.data_services_manager.clone();
    match manager.update_team().await{
        Ok(pack) => Ok(HttpResponse::Ok().json(ApiResponse::success(pack))),
        Err(err) => Err(DataBusHttpError {
            message: format!("Failed to update_team: {}", err),
            error_code: DEFAULT_ERROR_CODE,
        }),
    }
}

/// delete_team
///
/// Delete a team for a specified space
#[utoipa::path(
    path = "/fusion/v3/spaces/{space_id}/teams/{unit_id}",
    responses(
        (status = 200, description = "delete_team successfully")
    ),
    params(
        ("space_id" = String, Path, description = "space_id"),
        ("unit_id" = String, Path, description = "unit_id"),
        ("Authorization" = String, Header, description = "Current csrf token of user"),
    )
)]
#[delete("/spaces/{space_id}/teams/{unit_id}")]
pub async fn delete_team(
    _state: web::Data<AppState>,
    path: web::Path<(String, String)>,
) -> impl Responder {
    let (_space_id, _unit_id) = path.into_inner();
    let manager = _state.data_services_manager.clone();
    match manager.delete_team().await{
        Ok(pack) => Ok(HttpResponse::Ok().json(ApiResponse::success(pack))),
        Err(err) => Err(DataBusHttpError {
            message: format!("Failed to delete_team: {}", err),
            error_code: DEFAULT_ERROR_CODE,
        }),
    }
}

/// team_member_list
///
/// List members under team.
#[utoipa::path(
    path = "/fusion/v3/spaces/{space_id}/teams/{unit_id}/members",
    responses(
        (status = 200, description = "team_member_list successfully")
    ),
    params(
        ("space_id" = String, Path, description = "space_id"),
        ("unit_id" = String, Path, description = "unit_id"),
        ("Authorization" = String, Header, description = "Current csrf token of user"),
    )
)]
#[get("/spaces/{space_id}/teams/{unit_id}/members")]
pub async fn team_member_list(
    _state: web::Data<AppState>,
    path: web::Path<(String, String)>,
) -> impl Responder {
    let (_space_id, _unit_id) = path.into_inner();
    let manager = _state.data_services_manager.clone();
    match manager.get_team_members().await{
        Ok(pack) => Ok(HttpResponse::Ok().json(ApiResponse::success(pack))),
        Err(err) => Err(DataBusHttpError {
            message: format!("Failed to team_member_list: {}", err),
            error_code: DEFAULT_ERROR_CODE,
        }),
    }
}

/// role_list
///
/// Get roles for a specified space
#[utoipa::path(
    path = "/fusion/v3/spaces/{space_id}/roles",
    responses(
        (status = 200, description = "role_list successfully")
    ),
    params(
        ("space_id" = String, Path, description = "space_id"),
        ("Authorization" = String, Header, description = "Current csrf token of user"),
    )
)]
#[get("/spaces/{space_id}/roles")]
pub async fn role_list(
    _state: web::Data<AppState>,
    path: web::Path<String>,
) -> impl Responder {
    let _space_id = path.into_inner();
    let manager = _state.data_services_manager.clone();
    match manager.get_roles().await{
        Ok(pack) => Ok(HttpResponse::Ok().json(ApiResponse::success(pack))),
        Err(err) => Err(DataBusHttpError {
            message: format!("Failed to role_list: {}", err),
            error_code: DEFAULT_ERROR_CODE,
        }),
    }
}

/// create_role
///
/// Create a role for a specified space.
#[utoipa::path(
    path = "/fusion/v3/spaces/{space_id}/roles",
    responses(
        (status = 200, description = "create_role successfully")
    ),
    params(
        ("space_id" = String, Path, description = "space_id"),
        ("Authorization" = String, Header, description = "Current csrf token of user"),
    )
)]
#[post("/spaces/{space_id}/roles")]
pub async fn create_role(
    _state: web::Data<AppState>,
    path: web::Path<String>,
) -> impl Responder {
    let _space_id = path.into_inner();
    let manager = _state.data_services_manager.clone();
    match manager.create_role().await{
        Ok(pack) => Ok(HttpResponse::Ok().json(ApiResponse::success(pack))),
        Err(err) => Err(DataBusHttpError {
            message: format!("Failed to create_role: {}", err),
            error_code: DEFAULT_ERROR_CODE,
        }),
    }
}

/// update_role
///
/// Update roles for a specified space
#[utoipa::path(
    path = "/fusion/v3/spaces/{space_id}/roles/{unit_id}",
    responses(
        (status = 200, description = "update_role successfully")
    ),
    params(
        ("space_id" = String, Path, description = "space_id"),
        ("unit_id" = String, Path, description = "unit_id"),
        ("Authorization" = String, Header, description = "Current csrf token of user"),
    )
)]
#[put("/spaces/{space_id}/roles/{unit_id}")]
pub async fn update_role(
    _state: web::Data<AppState>,
    path: web::Path<(String, String)>,
) -> impl Responder {
    let (_space_id, _unit_id) = path.into_inner();
    let manager = _state.data_services_manager.clone();
    match manager.update_role().await{
        Ok(pack) => Ok(HttpResponse::Ok().json(ApiResponse::success(pack))),
        Err(err) => Err(DataBusHttpError {
            message: format!("Failed to update_role: {}", err),
            error_code: DEFAULT_ERROR_CODE,
        }),
    }
}

/// delete_role
///
/// Delete a role for a specified space
#[utoipa::path(
    path = "/fusion/v3/spaces/{space_id}/roles/{unit_id}",
    responses(
        (status = 200, description = "delete_role successfully")
    ),
    params(
        ("space_id" = String, Path, description = "space_id"),
        ("unit_id" = String, Path, description = "unit_id"),
        ("Authorization" = String, Header, description = "Current csrf token of user"),
    )
)]
#[delete("/spaces/{space_id}/roles/{unit_id}")]
pub async fn delete_role(
    _state: web::Data<AppState>,
    path: web::Path<(String, String)>,
) -> impl Responder {
    let (_space_id, _unit_id) = path.into_inner();
    let manager = _state.data_services_manager.clone();
    match manager.delete_role().await{
        Ok(pack) => Ok(HttpResponse::Ok().json(ApiResponse::success(pack))),
        Err(err) => Err(DataBusHttpError {
            message: format!("Failed to delete_role: {}", err),
            error_code: DEFAULT_ERROR_CODE,
        }),
    }
}

/// role_unit_list
///
/// Get the organizational units under the specified role unitId, the returned data includes teams and members.
#[utoipa::path(
    path = "/fusion/v3/spaces/{space_id}/roles/{unit_id}/units",
    responses(
        (status = 200, description = "role_unit_list successfully")
    ),
    params(
        ("space_id" = String, Path, description = "space_id"),
        ("unit_id" = String, Path, description = "unit_id"),
        ("Authorization" = String, Header, description = "Current csrf token of user"),
    )
)]
#[get("/spaces/{space_id}/roles/{unit_id}/units")]
pub async fn role_unit_list(
    _state: web::Data<AppState>,
    path: web::Path<(String, String)>,
) -> impl Responder {
    let (_space_id, _unit_id) = path.into_inner();
    let manager = _state.data_services_manager.clone();
    match manager.get_role_units().await{
        Ok(pack) => Ok(HttpResponse::Ok().json(ApiResponse::success(pack))),
        Err(err) => Err(DataBusHttpError {
            message: format!("Failed to role_unit_list: {}", err),
            error_code: DEFAULT_ERROR_CODE,
        }),
    }
}

/// member_detail
///
/// Get member details information
#[utoipa::path(
    path = "/fusion/v3/spaces/{space_id}/members/{unit_id}",
    responses(
        (status = 200, description = "member_detail successfully")
    ),
    params(
        ("space_id" = String, Path, description = "space_id"),
        ("unit_id" = String, Path, description = "unit_id"),
        ("Authorization" = String, Header, description = "Current csrf token of user"),
    )
)]
#[get("/spaces/{space_id}/members/{unit_id}")]
pub async fn member_detail(
    _state: web::Data<AppState>,
    path: web::Path<(String, String)>,
) -> impl Responder {
    let (_space_id, _unit_id) = path.into_inner();
    let manager = _state.data_services_manager.clone();
    match manager.get_member_detail().await{
        Ok(pack) => Ok(HttpResponse::Ok().json(ApiResponse::success(pack))),
        Err(err) => Err(DataBusHttpError {
            message: format!("Failed to member_detail: {}", err),
            error_code: DEFAULT_ERROR_CODE,
        }),
    }
}

/// create_member
///
/// Create a member for a specified space
#[utoipa::path(
    path = "/fusion/v3/spaces/{space_id}/members",
    responses(
        (status = 200, description = "create_member successfully")
    ),
    params(
        ("space_id" = String, Path, description = "space_id"),
        ("Authorization" = String, Header, description = "Current csrf token of user"),
    )
)]
#[post("/spaces/{space_id}/members")]
pub async fn create_member(
    _state: web::Data<AppState>,
    path: web::Path<String>,
) -> impl Responder {
    let _space_id = path.into_inner();
    let manager = _state.data_services_manager.clone();
    match manager.create_member().await{
        Ok(pack) => Ok(HttpResponse::Ok().json(ApiResponse::success(pack))),
        Err(err) => Err(DataBusHttpError {
            message: format!("Failed to create_member: {}", err),
            error_code: DEFAULT_ERROR_CODE,
        }),
    }
}

/// update_member
///
/// Update a member for a specified space.
#[utoipa::path(
    path = "/fusion/v3/spaces/{space_id}/members/{unit_id}",
    responses(
        (status = 200, description = "update_member successfully")
    ),
    params(
        ("space_id" = String, Path, description = "space_id"),
        ("unit_id" = String, Path, description = "unit_id"),
        ("Authorization" = String, Header, description = "Current csrf token of user"),
    )
)]
#[put("/spaces/{space_id}/members/{unit_id}")]
pub async fn update_member(
    _state: web::Data<AppState>,
    path: web::Path<(String, String)>,
) -> impl Responder {
    let (_space_id, _unit_id) = path.into_inner();
    let manager = _state.data_services_manager.clone();
    match manager.update_member().await{
        Ok(pack) => Ok(HttpResponse::Ok().json(ApiResponse::success(pack))),
        Err(err) => Err(DataBusHttpError {
            message: format!("Failed to update_member: {}", err),
            error_code: DEFAULT_ERROR_CODE,
        }),
    }
}

/// delete_member
///
/// Delete a member for a specified space.
#[utoipa::path(
    path = "/fusion/v3/spaces/{space_id}/members/{unit_id}",
    responses(
        (status = 200, description = "delete_member successfully")
    ),
    params(
        ("space_id" = String, Path, description = "space_id"),
        ("unit_id" = String, Path, description = "unit_id"),
        ("Authorization" = String, Header, description = "Current csrf token of user"),
    )
)]
#[delete("/spaces/{space_id}/members/{unit_id}")]
pub async fn delete_member(
    _state: web::Data<AppState>,
    path: web::Path<(String, String)>,
) -> impl Responder {
    let (_space_id, _unit_id) = path.into_inner();
    let manager = _state.data_services_manager.clone();
    match manager.delete_member().await{
        Ok(pack) => Ok(HttpResponse::Ok().json(ApiResponse::success(pack))),
        Err(err) => Err(DataBusHttpError {
            message: format!("Failed to delete_member: {}", err),
            error_code: DEFAULT_ERROR_CODE,
        }),
    }
}

/// create_chat_completion
///
/// Creates a model response for the given chat conversation
#[utoipa::path(
    path = "/fusion/v3/ai/{ai_id}/chat/completions",
    responses(
        (status = 200, description = "create_chat_completion successfully")
    ),
    params(
        ("ai_id" = String, Path, description = "ai_id"),
        ("Authorization" = String, Header, description = "Current csrf token of user"),
    )
)]
#[post("/ai/{ai_id}/chat/completions")]
pub async fn create_chat_completion(
    _state: web::Data<AppState>,
    path: web::Path<String>,
) -> impl Responder {
    let _ai_id = path.into_inner();
    let manager = _state.data_services_manager.clone();
    match manager.create_conversation().await{
        Ok(pack) => Ok(HttpResponse::Ok().json(ApiResponse::success(pack))),
        Err(err) => Err(DataBusHttpError {
            message: format!("Failed to create_chat_completion: {}", err),
            error_code: DEFAULT_ERROR_CODE,
        }),
    }
}
