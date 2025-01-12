"""
VOs, View Objects

"""
import time
from enum import Enum
from typing import Any, List, Optional, Union

from loguru import logger
from pydantic import BaseModel, Field

from ai_shared.actions.base import BaseAction
from ai_shared.persist.ai_info import AIInfo
from ai_shared.persist.training_info import TrainingInfo, TrainingStatus


class InferenceUsageVO(BaseModel):
    total_tokens: int
    prompt_tokens: int
    completion_tokens: int
    total_cost: Optional[float]
    result: Optional[str]


class APIResponseVO(BaseModel):
    code: int = Field(default=200, description="http status code")
    msg: str = Field(description="request message")
    data: Optional[Any] = Field(description="response data")

    @staticmethod
    def success(msg: str, data: Optional[Any] = None) -> "APIResponseVO":
        return APIResponseVO(msg=msg, data=data)

    @staticmethod
    def error(code: int, msg: str, data: Optional[Any] = None) -> "APIResponseVO":
        return APIResponseVO(code=code, msg=msg, data=data)


class OpenAIMessageResponseVO(BaseModel):
    role: str = Field(default="assistant")
    content: str


class OpenAIChoiceResponseVO(BaseModel):
    index: int
    message: OpenAIMessageResponseVO
    finish_reason: Optional[str] = None


class OpenAIChoiceStreamResponseVO(BaseModel):
    index: int
    delta: OpenAIMessageResponseVO
    finish_reason: Optional[str] = None


class OpenAIChatCompletionType(Enum):
    CHAT = "chat"
    STATUS = "status"


class OpenAIChatCompletionStatus(Enum):
    # customize define status
    STARTING = "starting"
    GENERATING = "generating"
    TIMEOUT = "timeout"
    ERROR = "error"
    # openai run status
    QUEUED = "queued"
    IN_PROGRESS = "in_progress"
    REQUIRES_ACTION = "requires_action"
    CANCELLING = "cancelling"
    CANCELLED = "cancelled"
    FAILED = "failed"
    COMPLETED = "completed"
    EXPIRED = "expired"


class OpenAIChatCompletionVO(BaseModel):
    id: str
    conversation_id: Optional[str]
    type: Optional[OpenAIChatCompletionType] = Field(default=OpenAIChatCompletionType.CHAT)
    status: Optional[OpenAIChatCompletionStatus] = Field(default=None)
    actions: Optional[List[BaseAction]] = Field(default=None)

    object: str = "chat.completion"
    created: int
    model: str
    choices: List[Union[OpenAIChoiceResponseVO, OpenAIChoiceStreamResponseVO]] = Field(
        default_factory=list
    )

    # extend
    usage: Optional[InferenceUsageVO]


class TrainingInfoVO(TrainingInfo):
    training_id: Optional[str] = Field(default=None)

    def __init__(self, **data):
        super().__init__(**data)
        self._on_init()

    def _on_init(self):
        if self.started_at:
            if (
                int(time.time()) - self.started_at > 60 * 60 * 24 * 1
            ) and self.status == TrainingStatus.TRAINING:
                # 60 seconds * 60 minutes * 24 hours * 1 day timeout if TrainingStatus keep Training
                logger.error(
                    f"Training {self.training_id} is too old timeout, set it failed"
                )
                self.status = TrainingStatus.FAILED


# 1. 前端直接使用/ai/{ai_id}进行轮询
# 2. TrainingInfoVO，进行包装
class AIInfoVO(AIInfo):
    current_training_info: Optional[TrainingInfoVO] = Field(default=None)
    locking_training_info: Optional[TrainingInfoVO] = Field(default=None)