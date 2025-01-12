"""
RO - Requests Object
"""
from typing import Any, Dict, List, Literal, Optional, Union
from uuid import UUID

from pydantic import BaseModel, Field, validator

from ai_shared.actions.base import BaseAction
from ai_shared.ai_setting.base_ai_setting import BaseAdvancedAISetting
from ai_shared.copilot.models import AssistantType
from ai_shared.databus import AiNode
from sympy import content

# ==========
# OpenAI
# ==========

_object = Dict[str, Any]


class DataSourceRO(BaseModel):
    """
    Client request specific data source detail
    """

    type: Literal["datasheet"]
    type_id: str


class OpenAIChatMessageRO(BaseModel):
    role: str = Field(default="user")
    content: Optional[str]
    name: Optional[str] = Field(default=None, max_length=64)
    function_call: Optional[Dict[str, Any]]


class OpenAIChatFunctionRO(BaseModel):
    name: str
    description: Optional[str]
    parameter: Optional[_object]

    @validator("name")
    def validate_name_length(cls, name: str) -> str:
        if len(name) > 64:
            raise ValueError(
                "The name of the function to be called. Must be a-z, A-Z, 0-9, or contain underscores and dashes, "
                + "with a maximum length of 64."
                + "See: https://platform.openai.com/docs/api-reference/chat/create"
            )

        return name


class OpenAIChatCompletionRO(BaseModel):
    # AITable / vika conversation_id, it should be pop as a OpenAI request body
    conversation_id: Optional[str] = Field(default=None)
    actions: Optional[List[BaseAction]] = Field(default=None)

    # langchain `model_name` compatibility
    model: Optional[str] = Field(default=None)

    messages: List[OpenAIChatMessageRO] = Field(default_factory=list)
    functions: List[OpenAIChatFunctionRO] = Field(default_factory=list)
    function_call: Optional[_object] = Field(default_factory=dict)
    temperature: float = 0.7
    top_p: float = 1
    n: int = 1

    # langchain `streaming` compatibility
    stream: bool = Field(default=False)

    stop: Optional[List[str]] = Field(default=None)
    max_tokens: int = 256
    presence_penalty: float = Field(default=0, ge=-2.0, le=2.0)
    frequency_penalty: float = Field(default=0, ge=-2.0, le=2.0)
    logit_bias: Optional[Dict[str, float]] = Field(default_factory=dict)

    class Config:
        schema_extra = {
            "example": {
                "model": "gpt-3.5-turbo",
                "messages": [
                    {
                        "role": "user",
                        "content": "Are you ChatGPT?",
                    }
                ],
                "functions": [],
                "function_call": {},
                "temperature": 0.7,
                "top_p": 1,
                "n": 1,
                "stream": False,
                "max_tokens": 256,
                "presence_penalty": 0,
                "frequency_penalty": 0,
                "logit_bias": {},
                "stop": None,
            },
        }

    def overwrite_chat_completion_request(self, ai_setting: BaseAdvancedAISetting) -> "OpenAIChatCompletionRO":
        """
        Overwrite the OpenAI chat completion request
        """
        if self.temperature:
            self.temperature = ai_setting.temperature
        if self.top_p:
            self.top_p = ai_setting.top_p
        if self.n:
            self.n = ai_setting.n
        if self.max_tokens:
            self.max_tokens = ai_setting.max_tokens
        if self.presence_penalty:
            self.presence_penalty = ai_setting.presence_penalty
        if self.frequency_penalty:
            self.frequency_penalty = ai_setting.frequency_penalty
        if self.logit_bias:
            self.logit_bias = ai_setting.logit_bias
        return self


class RequestSuggestionBody(BaseModel):
    question: Optional[str] = Field(
        default="",
        description="The similar question to search in the vector database.",
    )
    n: int = Field(default=10, ge=0, description="The number of suggestions to return.")
    filter: Optional[str] = Field(
        description="The filter to remove duplicates in the vector database."
    )


class PostTrainBody(BaseModel):
    """
    Client request specific data source detail
    """
    ai_node_id: Optional[int] = Field()
    ai_node_ids: Optional[list[int]] = Field()
    datasheet_id: Optional[str] = Field()
    datasheet_ids: Optional[list[str]] = Field()


class TrainerAiNodes(BaseModel):
    """Trainer Data"""
    ai_id: str = Field()
    ai_nodes: list[AiNode] = Field(default=[])


class PostCopilotChatBodyDataMeta(BaseModel):
    datasheet_id: Optional[str] = Field(default=None)
    view_id: Optional[str] = Field(default=None)


class PostCopilotChatBodyHelpMeta(BaseModel):
    ...


class PostCopilotChatBody(BaseModel):
    conversation_id: Optional[UUID] = Field()
    assistant_type: Optional[AssistantType] = Field(default=AssistantType.HELP)
    stream: Optional[bool] = Field(default=False)
    messages: List[OpenAIChatMessageRO] = Field(default_factory=list)
    meta: Optional[Union[PostCopilotChatBodyDataMeta, PostCopilotChatBodyHelpMeta]] = Field(default=None)


class PostCopilotSuggestionsBody(BaseModel):
    assistant_type: Optional[AssistantType] = Field(default=AssistantType.HELP)
