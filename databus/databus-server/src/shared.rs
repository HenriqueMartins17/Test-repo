use std::fmt::{Display, Formatter};

use actix_web::{http::StatusCode, HttpResponse};
use automation_model::{AutomationRobotIntroductionSO, AutomationRobotRunNumsSO, AutomationSO};
use databus_core::prelude::DatasheetPackSO;
use databus_core::vo::record_vo::RecordDTO;
use databus_dao_db::automation::model::{AutomationActionPO, AutomationRunHistoryPO, AutomationTriggerSO};
use databus_dao_db::AiPO;
use serde::{Deserialize, Serialize};
use utoipa::ToSchema;

use crate::automation::automation_model;

pub const DEFAULT_ERROR_CODE: u16 = 500;

#[derive(Debug, Serialize, Deserialize, ToSchema)]
#[aliases(
ApiResponseAiPO = ApiResponse<AiPO>,
ApiResponseEmptySO = ApiResponse<bool>,
ApiResponseDatasheetPackSO = ApiResponse<DatasheetPackSO>,
ApiResponseAutomationSO = ApiResponse<AutomationSO>,
ApiResponseAutomationRobotIntroductionSO = ApiResponse<AutomationRobotIntroductionSO>,
ApiResponseAutomationRunHistoryPO = ApiResponse<Vec<AutomationRunHistoryPO>>,
ApiResponseRecordDTOs = ApiResponse<Vec<RecordDTO>>,
ApiResponseAutomationTriggerSO=ApiResponse<Vec<AutomationTriggerSO>>,
ApiResponseAutomationActionPO=ApiResponse<Vec<AutomationActionPO>>,
ApiResponseAutomationRobotRunNumsSO=ApiResponse<AutomationRobotRunNumsSO>,
)]
pub struct ApiResponse<T> {
  code: u16,
  success: bool,
  #[serde(skip_serializing_if = "Option::is_none")]
  data: Option<T>,
  message: String,
}

impl<T> ApiResponse<T> {
  pub fn success(data: T) -> Self {
    Self {
      code: 200,
      success: true,
      data: Some(data),
      message: "SUCCESS".to_string(),
    }
  }

  pub fn not_found(data: T) -> Self {
    Self {
      code: StatusCode::NOT_FOUND.into(),
      success: false,
      data: Some(data),
      message: "NOT FOUND".to_string(),
    }
  }

  pub fn success_empty() -> Self {
    Self {
      code: 200,
      success: true,
      data: None,
      message: "SUCCESS".to_string(),
    }
  }

  // Constructor for failed responses
  pub fn error(code: u16, message: &str) -> Self {
    Self {
      code,
      success: false,
      data: None,
      message: message.to_string(),
    }
  }
}

// 自定义一个错误类型
#[derive(Debug)]
pub struct DataBusHttpError {
  pub message: String,
  pub error_code: u16,
}

impl DataBusHttpError {
  pub fn new(message: String) -> Self {
    Self {
      message,
      error_code: DEFAULT_ERROR_CODE,
    }
  }

  pub fn new_with_code(message: String, error_code: u16) -> Self {
    Self {
      message,
      error_code,
    }
  }
}

impl Display for DataBusHttpError {
  fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
    write!(f, "DataBusHttpError: {}", self.message)
  }
}

impl actix_web::error::ResponseError for DataBusHttpError {
  fn error_response(&self) -> HttpResponse {
    if self.error_code == 401 {
      return HttpResponse::Unauthorized().json(ApiResponse::<()>::error(self.error_code, &self.message));
    }
    HttpResponse::InternalServerError().json(ApiResponse::<()>::error(self.error_code, &self.message))
  }
}
