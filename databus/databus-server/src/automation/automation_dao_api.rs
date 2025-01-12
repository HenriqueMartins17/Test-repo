use crate::automation::automation_model::{
  AutomationHistoryRO, AutomationHistoryStatusRO, AutomationRobotActionRO, AutomationRobotCopyRO,
  AutomationRobotIntroductionSO, AutomationRobotRunNumsSO, AutomationRobotSO, AutomationRobotTriggerRO,
  AutomationRobotUpdateRO, AutomationSO, PageDTO, ResourceIdDTO, RobotIdVecDTO,
};
use crate::automation::AutomationErrorCode;
use crate::shared::{ApiResponse, DEFAULT_ERROR_CODE};
use crate::util::{
  get_monthly_end_format, get_monthly_start_format, get_next_monthly_start_timestamp, get_prev_monthly_start_timestamp,
  system_tz,
};
use crate::{AppState, DataBusHttpError};
use actix_web::http::StatusCode;
use actix_web::web::Query;
use actix_web::{get, patch, post, put, web, HttpRequest, HttpResponse, Responder};
use databus_core::utils::utils::generate_u64_id;
use databus_core::utils::uuid::IdUtil;
use databus_dao_db::model::{AutomationTriggerSO, CreateAutomationRobotSO, CreateRobotActionSO, CreateRobotTriggerSO};

/// Get automation run history list
#[utoipa::path(
  get,
  path = "/databus/dao/automations/{robot_id}/histories",
  responses(
    (status = 200, description = "Get automation run history list", body=ApiResponseAutomationRunHistoryPO),
  ),
  params(
    PageDTO,
    ("robot_id", description = "robot id"),
  )
)]
#[get("/{robot_id}/histories")]
pub async fn dao_get_automation_run_history(
  state: web::Data<AppState>,
  robot_id: web::Path<String>,
  query: Query<PageDTO>,
) -> impl Responder {
  let dao_manager = state.dao_manager.clone();
  let dao = dao_manager.automation_dao.clone();
  let skip = (query.page_num - 1) * query.page_size;
  // last month start timestamp second
  let start_date = get_prev_monthly_start_timestamp(1);
  // current month end timestamp second
  let end_date = get_next_monthly_start_timestamp(1);
  let result = dao
    .run_history_dao
    .get_by_robot_id_with_pagination(&robot_id.to_string(), &skip, &query.page_size, start_date, end_date)
    .await;
  match result {
    Ok(data) => Ok(HttpResponse::Ok().json(ApiResponse::success(data))),
    Err(err) => Err(DataBusHttpError {
      message: format!("Failed to get automation run history list: {}", err),
      error_code: DEFAULT_ERROR_CODE,
    }),
  }
}

/// Get automation run task details todo
#[utoipa::path(
  get,
  path = "/databus/dao/automations/histories/{task_id}",
  responses(
    (status = 200, description = "Get automation run history detail"),
  ),
  params(
    ("task_id", description = "task id"),
  )
)]
#[get("/histories/{task_id}")]
pub async fn dao_get_automation_run_history_detail(
  state: web::Data<AppState>,
  task_id: web::Path<String>,
) -> impl Responder {
  let dao_manager = state.dao_manager.clone();
  let dao = dao_manager.automation_dao.clone();
  let result = dao.run_history_dao.get_by_task_id(&task_id.to_string()).await;
  match result {
    Ok(data) => Ok(HttpResponse::Ok().json(ApiResponse::success(data))),
    Err(err) => Err(DataBusHttpError {
      message: format!("Failed to get automation run history detail: {}", err),
      error_code: DEFAULT_ERROR_CODE,
    }),
  }
}

/// Get automation task input and output todo
#[utoipa::path(
  get,
  path = "/databus/dao/automations/histories/{task_id}/contexts/{action_id}",
  responses(
    (status = 200, description = "Get automation run history task context"),
  ),
  params(
    ("task_id", description = "task id"),
    ("action_id", description = "action id"),
  )
)]
#[get("/histories/{task_id}/contexts/{action_id}")]
pub async fn dao_get_automation_run_context(
  state: web::Data<AppState>,
  param: web::Path<(String, String)>,
) -> impl Responder {
  let dao_manager = state.dao_manager.clone();
  let dao = dao_manager.automation_dao.clone();
  let task_id: String = param.clone().0;
  let action_id: String = param.clone().1;
  let result = dao
    .run_history_dao
    .get_run_context_by_task_id_and_action_id(&task_id, &action_id)
    .await;
  match result {
    Ok(data) => Ok(HttpResponse::Ok().json(ApiResponse::success(data))),
    Err(err) => Err(DataBusHttpError {
      message: format!("Failed to get automation task run context: {}", err),
      error_code: DEFAULT_ERROR_CODE,
    }),
  }
}

/// Get automation task input and output todo
#[utoipa::path(
  put,
  path = "/databus/dao/automations/histories/{task_id}/status",
  responses(
    (status = 200, description = "Get automation run history task context"),
  ),
  params(
    ("task_id", description = "task id")
  ),
  request_body = AutomationHistoryStatusRO
)]
#[put("/histories/{task_id}/status")]
pub async fn dao_update_automation_run_history_status(
  state: web::Data<AppState>,
  task_id: web::Path<String>,
  json: web::Json<AutomationHistoryStatusRO>,
) -> impl Responder {
  let dao_manager = state.dao_manager.clone();
  let dao = dao_manager.automation_dao.clone();
  dao
    .run_history_dao
    .update_status_by_task_id(&task_id, &json.status)
    .await;
  HttpResponse::Ok().json(ApiResponse::<()>::success_empty())
}

/// create automation run history task success todo
#[utoipa::path(
  post,
  path = "/databus/dao/automations/{robot_id}/histories",
  responses(
    (status = 200, description = "create automation run history task success"),
  ),
  params(
    ("robot_id", description = "robot id")
  ),
  request_body = Value,
)]
#[post("/{robot_id}/histories")]
pub async fn dao_create_automation_run_history(
  state: web::Data<AppState>,
  robot_id: web::Path<String>,
  body: web::Json<AutomationHistoryRO>,
) -> impl Responder {
  let dao_manager = state.dao_manager.clone();
  let dao = dao_manager.automation_dao.clone();
  match dao
    .run_history_dao
    .create(&body.task_id, &robot_id, &body.space_id, &body.status, &body.data)
    .await
  {
    Ok(id) => HttpResponse::Ok().json(ApiResponse::success(id)),
    Err(err) => {
      println!("INSERT ERROR: {:?}", err.to_string());
      HttpResponse::Ok().json(ApiResponse::<()>::error(
        StatusCode::INTERNAL_SERVER_ERROR.as_u16(),
        "insert error",
      ))
    }
  }
}

/// get automations triggers todo
#[utoipa::path(
  get,
  path = "/databus/dao/automations/triggers",
  params(RobotIdVecDTO),
  responses(
    (status = 200, description = "get automations triggers"),
  ),
)]
#[get("/triggers")]
pub async fn dao_get_robots_triggers(state: web::Data<AppState>, req: HttpRequest) -> impl Responder {
  let dao_manager = state.dao_manager.clone();
  let params: RobotIdVecDTO = serde_qs::from_str(req.query_string()).unwrap();
  let dao = dao_manager.automation_dao.clone();
  let result = dao.trigger_dao.get_by_robot_ids(params.robot_ids).await;
  match result {
    Ok(data) => Ok(HttpResponse::Ok().json(ApiResponse::success(data))),
    Err(err) => Err(DataBusHttpError {
      message: format!("Failed to get automations triggers: {}", err),
      error_code: DEFAULT_ERROR_CODE,
    }),
  }
}

/// get automations triggers todo
#[utoipa::path(
  get,
  path = "/databus/dao/automations/robots",
  params(ResourceIdDTO),
  responses(
    (status = 200, description = "get automations triggers", body=ApiResponseAutomationRobotIntroductionSO),
  ),
)]
#[get("/robots")]
pub async fn dao_get_robots_by_resource_id(state: web::Data<AppState>, query: Query<ResourceIdDTO>) -> impl Responder {
  let dao_manager = state.dao_manager.clone();
  let dao = dao_manager.automation_dao.clone();
  let robots = dao
    .robot_dao
    .get_introduction_by_resource_id(&query.resource_id)
    .await
    .unwrap();
  if robots.is_empty() {
    return HttpResponse::Ok().json(ApiResponse::<()>::success_empty());
  }
  let robot_ids: Vec<String> = robots.clone().into_iter().map(|x| x.robot_id).collect();
  let triggers = dao
    .trigger_dao
    .get_introduction_by_robot_ids(robot_ids.clone())
    .await
    .unwrap();
  let actions = dao
    .action_dao
    .get_introduction_by_robot_ids(robot_ids.clone())
    .await
    .unwrap();
  HttpResponse::Ok().json(ApiResponse::success(AutomationRobotIntroductionSO {
    robots,
    triggers,
    actions,
  }))
}

/// Get automation robot detail.
#[utoipa::path(
  get,
  path = "/databus/dao/automations/robots/{robot_id}",
  params(
    ("robot_id", description = "Automation robot id")
  ),
  responses(
    (status = 200, description = "get automation detail", body=ApiResponseAutomationSO),
  ),
)]
#[get("/robots/{robot_id}")]
pub async fn dao_get_robot_by_robot_id(state: web::Data<AppState>, robot_id: web::Path<String>) -> impl Responder {
  let dao_manager = state.dao_manager.clone();
  let dao = dao_manager.automation_dao.clone();
  let robot = dao.robot_dao.get_introduction_by_robot_id(&robot_id).await.ok();
  if robot.is_none() {
    return HttpResponse::Ok().json(ApiResponse::<()>::success_empty());
  }
  // current month start timestamp second
  let start_date = get_prev_monthly_start_timestamp(0).clone();
  // current month end timestamp second
  let end_date = get_next_monthly_start_timestamp(1).clone();
  let count = dao
    .run_history_dao
    .get_count_between_start_and_end_by_robot_id(&robot_id, start_date, end_date)
    .await;
  let triggers = dao.trigger_dao.get_by_robot_id(&robot_id).await.unwrap();
  let actions = dao.action_dao.get_by_robot_id(&robot_id).await.unwrap();
  // get related resources
  let resource_ids = dao.trigger_dao.get_resource_ids_by_robot_id(&robot_id).await.unwrap();
  let related_resources = dao_manager
    .node_dao
    .get_simple_info_by_node_ids(resource_ids)
    .await
    .ok();
  let robot_some = robot.unwrap();
  let updated_at = match robot_some.updated_at {
    Some(date) => Some(date.and_local_timezone(system_tz().clone()).unwrap().timestamp_millis()),
    _ => None,
  };
  HttpResponse::Ok().json(ApiResponse::success(AutomationSO {
    robot: AutomationRobotSO {
      robot_id: robot_id.clone(),
      resource_id: robot_some.resource_id,
      name: robot_some.name,
      description: robot_some.description,
      props: robot_some.props,
      is_active: robot_some.is_active,
      updated_by: robot_some.updated_by,
      updated_at,
      recently_run_count: if count.is_ok() { count.unwrap() } else { 0 },
    },
    triggers,
    actions,
    related_resources,
  }))
}

/// Update automation robot
#[utoipa::path(
  put,
  path = "/databus/dao/automations/robots/{robot_id}",
  responses(
    (status = 200, description = "Update automation robot successfully", body=ApiResponseEmptySO),
  ),
  params(
    ("robot_id", description = "robot id")
  ),
  request_body = AutomationRobotUpdateRO
)]
#[put("/robots/{robot_id}")]
pub async fn dao_update_automation_robot(
  state: web::Data<AppState>,
  robot_id: web::Path<String>,
  data: web::Json<AutomationRobotUpdateRO>,
) -> impl Responder {
  let dao_manager = state.dao_manager.clone();
  let dao = dao_manager.automation_dao.clone();
  dao
    .robot_dao
    .update_by_robot_id(
      &robot_id,
      &data.updated_by,
      data.name.clone(),
      data.description.clone(),
      data.props.clone(),
      data.is_active.clone(),
      data.is_deleted.clone(),
    )
    .await;
  HttpResponse::Ok().json(ApiResponse::<()>::success_empty())
}

/// Create automation robot
#[utoipa::path(
  post,
  path = "/databus/dao/automations/robots/copy",
  responses(
    (status = 200, description = "Create automation robot successfully", body=ApiResponseEmptySO),
  ),
  request_body = Vec<AutomationRobotCopyRO>
)]
#[post("/robots/copy")]
pub async fn dao_copy_automation_robot(
  state: web::Data<AppState>,
  data: web::Json<Vec<AutomationRobotCopyRO>>,
) -> impl Responder {
  let dao_manager = state.dao_manager.clone();
  let dao = dao_manager.automation_dao.clone();
  let mut create_robot_so: Vec<CreateAutomationRobotSO> = vec![];
  for mut item in data.clone() {
    let robots = dao
      .robot_dao
      .get_introduction_by_resource_id(item.original_resource_id.as_mut_str())
      .await;
    if robots.is_ok() {
      for robot in robots.unwrap() {
        let mut so = CreateAutomationRobotSO {
          created_by: item.user_id,
          is_active: robot.is_active,
          resource_id: item.resource_id.clone(),
          name: if item.clone().automation_name.is_some() {
            item.clone().automation_name.unwrap()
          } else {
            robot.name
          },
          props: robot.props,
          triggers: Option::None,
          actions: Option::None,
        };
        let actions = dao
          .action_dao
          .get_by_robot_id(robot.robot_id.clone().as_mut_str())
          .await
          .unwrap();
        let mut create_actions: Vec<CreateRobotActionSO> = vec![];
        for action in actions {
          create_actions.push(CreateRobotActionSO {
            action_type_id: action.action_type_id,
            input: action.input,
          });
        }
        let triggers = dao
          .trigger_dao
          .get_by_robot_id(robot.robot_id.clone().as_mut_str())
          .await
          .unwrap();
        let mut create_triggers: Vec<CreateRobotTriggerSO> = vec![];
        for trigger in triggers {
          create_triggers.push(CreateRobotTriggerSO {
            trigger_type_id: trigger.trigger_type_id,
            input: trigger.input,
            resource_id: trigger.resource_id,
          });
        }
        so.actions = Some(create_actions);
        so.triggers = Some(create_triggers);
        create_robot_so.push(so)
      }
    }
  }
  dao.robot_dao.create(create_robot_so).await;
  HttpResponse::Ok().json(ApiResponse::<()>::success_empty())
}

/// Add or create automation robot trigger
#[utoipa::path(
  put,
  path = "/databus/dao/automations/robots/{robot_id}/triggers",
  responses(
    (status = 201, description = "Create automation robot trigger successfully", body=ApiResponseAutomationTriggerSO),
    (status = 200, description = "Update automation robot trigger successfully", body=ApiResponseAutomationTriggerSO),
  ),
  params(
    ("robot_id", description = "robot id")
  ),
  request_body = AutomationRobotTriggerRO
)]
#[put("/robots/{robot_id}/triggers")]
pub async fn dao_create_or_update_automation_robot_trigger(
  state: web::Data<AppState>,
  robot_id: web::Path<String>,
  data: web::Json<AutomationRobotTriggerRO>,
) -> impl Responder {
  let dao_manager = state.dao_manager.clone();
  let dao = dao_manager.automation_dao.clone();
  let mut schedule_id = None;
  if !dao.robot_dao.is_robot_exist(&robot_id).await.unwrap() {
    return HttpResponse::Ok().json(ApiResponse::<()>::error(
      AutomationErrorCode::AutomationRobotNotExist as u16,
      "insert or update trigger error",
    ));
  }
  if data.limit_count.is_some() {
    let trigger_count = dao.trigger_dao.get_count_by_robot_id(&robot_id).await.unwrap();
    if trigger_count >= data.limit_count.unwrap() {
      return HttpResponse::Ok().json(ApiResponse::<()>::error(
        AutomationErrorCode::AutomationTriggerCountLimit as u16,
        "insert or update trigger error",
      ));
    }
  }
  let resource_id = if data.resource_id.is_some() {
    data.resource_id.clone().unwrap()
  } else {
    String::from("")
  };
  let trigger_id = if data.trigger_id.is_some() {
    data.trigger_id.clone().unwrap()
  } else {
    IdUtil::create_automation_trigger_id()
  };
  if data.trigger_id.is_none() {
    schedule_id = Some(generate_u64_id());
    dao
      .trigger_dao
      .create(
        &data.user_id,
        robot_id.to_string().as_mut_str(),
        trigger_id.as_str(),
        data.trigger_type_id.clone().unwrap(),
        resource_id,
        data.space_id.clone(),
        data.prev_trigger_id.clone(),
        data.input.clone(),
        data.schedule_conf.clone(),
        schedule_id,
      )
      .await;
    return HttpResponse::Ok().json(ApiResponse::success(vec![AutomationTriggerSO {
      robot_id: robot_id.clone(),
      trigger_id: trigger_id.clone(),
      resource_id: data.resource_id.clone(),
      trigger_type_id: data.trigger_type_id.clone().unwrap(),
      prev_trigger_id: data.prev_trigger_id.clone(),
      input: data.input.clone(),
      schedule_id: schedule_id,
    }]));
  }
  if data.trigger_id.is_some() {
    schedule_id = dao
      .trigger_dao
      .update(
        &data.user_id,
        robot_id.to_string().as_mut_str(),
        trigger_id.as_str(),
        data.trigger_type_id.clone(),
        data.resource_id.clone(),
        data.space_id.clone(),
        data.prev_trigger_id.clone(),
        data.input.clone(),
        data.schedule_conf.clone(),
        data.is_deleted.clone(),
      )
      .await;
  }
  let trigger = dao
    .trigger_dao
    .get_by_robot_id_and_trigger_id(&robot_id, trigger_id.clone())
    .await
    .unwrap();
  if trigger.is_none() {
    return HttpResponse::Ok().json(ApiResponse::<()>::success_empty());
  }
  HttpResponse::Ok().json(ApiResponse::success(vec![AutomationTriggerSO {
    robot_id: robot_id.clone(),
    trigger_id: trigger_id.clone(),
    resource_id: trigger.clone().unwrap().resource_id,
    trigger_type_id: trigger.clone().unwrap().trigger_type_id,
    prev_trigger_id: trigger.clone().unwrap().prev_trigger_id,
    input: trigger.clone().unwrap().input,
    schedule_id,
  }]))
}

/// Add or create automation robot action
#[utoipa::path(
  put,
  path = "/databus/dao/automations/robots/{robot_id}/actions",
  responses(
  (status = 201, description = "Create automation robot action successfully", body=ApiResponseAutomationActionPO),
  (status = 200, description = "Update automation robot action successfully", body=ApiResponseAutomationActionPO),
  ),
  params(
  ("robot_id", description = "robot id")
  ),
  request_body = AutomationRobotActionRO
)]
#[put("/robots/{robot_id}/actions")]
pub async fn dao_create_or_update_automation_robot_action(
  state: web::Data<AppState>,
  robot_id: web::Path<String>,
  data: web::Json<AutomationRobotActionRO>,
) -> impl Responder {
  let dao_manager = state.dao_manager.clone();
  let dao = dao_manager.automation_dao.clone();
  if !dao.robot_dao.is_robot_exist(&robot_id).await.unwrap() {
    return HttpResponse::Ok().json(ApiResponse::<()>::error(
      AutomationErrorCode::AutomationRobotNotExist as u16,
      "insert or update action error",
    ));
  }
  if data.limit_count.is_some() {
    let trigger_count = dao.trigger_dao.get_count_by_robot_id(&robot_id).await.unwrap();
    if trigger_count >= data.limit_count.unwrap() {
      return HttpResponse::Ok().json(ApiResponse::<()>::error(
        AutomationErrorCode::AutomationActionCountLimit as u16,
        "insert or update action error",
      ));
    }
  }
  dao
    .action_dao
    .insert_or_update(
      &data.user_id,
      robot_id.to_string().as_mut_str(),
      data.action_id.clone(),
      data.action_type_id.clone(),
      data.prev_action_id.clone(),
      data.input.clone(),
      data.is_deleted.clone(),
    )
    .await;
  let actions = dao.action_dao.get_by_robot_id(&robot_id).await.unwrap();
  HttpResponse::Ok().json(ApiResponse::success(actions))
}

/// Get automation robot running times.
#[utoipa::path(
get,
path = "/databus/dao/automations/robots/runs/{space_id}",
params(
("space_id", description = " space id")
),
responses(
(status = 200, description = "get automation robot running times", body=ApiResponseAutomationRobotRunNumsSO),
),
)]
#[get("/robots/runs/{space_id}")]
pub async fn dao_get_robot_runs_by_space_id(state: web::Data<AppState>, space_id: web::Path<String>) -> impl Responder {
  let dao_manager = state.dao_manager.clone();
  let dao = dao_manager.automation_dao.clone();
  // current month start timestamp second
  let start_date = get_monthly_start_format(0).clone();
  // current month end timestamp second
  let end_date = get_monthly_end_format(1).clone();
  let count = dao
    .run_history_dao
    .get_count_between_start_and_end_by_space_id(&space_id, start_date, end_date)
    .await
    .unwrap();

  HttpResponse::Ok().json(ApiResponse::success(AutomationRobotRunNumsSO {
    recently_run_count: count,
  }))
}
