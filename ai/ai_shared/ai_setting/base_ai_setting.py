from abc import abstractmethod
import json
import os
import sys
from enum import Enum
from typing import Dict, Optional

import aiofiles
from loguru import logger
from pydantic import BaseModel, Field

from ai_shared.tracking import Tracking

max_int = sys.maxsize


class AISettingMode(Enum):
    WIZARD = "wizard"
    ADVANCED = "advanced"


class AIAgentType(Enum):
    """
    AI bot type, a setting in "AIModel"
    """

    QA = "qa"
    CHAT = "chat"
    CREATOR = "creator"
    DATA = "data"

    def is_chat(self):
        return self == AIAgentType.CHAT

    def is_qa(self):
        return self == AIAgentType.QA

    def is_creator(self):
        return self == AIAgentType.CREATOR

    def is_data(self):
        return self == AIAgentType.DATA

    def need_embeddings(self):
        return self in (
            AIAgentType.QA,
            AIAgentType.CREATOR,
        )


class AIAgentModel(Enum):
    pass


class AISettingJsonSchemas:
    """cache schemas in memory, get schema by agent type"""
    _instance = None

    def __new__(cls, *args, **kwargs):
        if cls._instance is None:
            cls._instance = super().__new__(cls)
            cls._instance._schemas = {}
        return cls._instance

    async def get_ai_setting_schema(self, ai_agent_type: AIAgentType) -> Dict | None:
        schema = self._schemas.get(ai_agent_type)
        if not schema:
            schema = await self._read_json_schema_file(ai_agent_type)
            if not schema:
                logger.error(f"Cannot read json schema file from disk: {schema=}")
                return None
            self._schemas[ai_agent_type] = schema
        return schema

    @staticmethod
    async def _read_json_schema_file(ai_agent_type: AIAgentType) -> Dict | None:
        current_dir = os.path.dirname(os.path.abspath(__file__))
        file_name = f"{ai_agent_type.value}_ai_schema.json"
        file_path = os.path.join(current_dir, 'schemas', file_name)

        try:
            async with aiofiles.open(file_path, mode='r', encoding="utf-8") as f:
                contents = await f.read()

            json_data = json.loads(contents)
        except FileNotFoundError as e:
            Tracking.capture_exception(e)
            logger.error(f"_read_json_schema_file, File not found: {file_path}")
            return None
        except json.JSONDecodeError as e:
            Tracking.capture_exception(e)
            logger.error(f"_read_json_schema_file, JSON decoding error: {e}")
            return None
        except Exception as e:
            Tracking.capture_exception(e)
            logger.error(f"_read_json_schema_file, got error: {e}")
            return None
        else:
            return json_data


class BaseAISetting(BaseModel):
    mode: str = Field(default=AISettingMode.WIZARD.value, alias="mode")
    type: AIAgentType = Field(default=AIAgentType.QA, alias="type")
    prompt: Optional[str] = Field(default=None, alias="prompt")
    prologue: Optional[str] = Field(default=None, alias="prologue")
    model: Optional[str] = Field(default=None, alias="model")
    is_enabled_prompt_box: bool = Field(default=True, alias="isEnabledPromptBox")
    is_enabled_prompt_tips: bool = Field(default=True, alias="isEnabledPromptTips")

    @abstractmethod
    async def get_schema(self) -> Optional[dict]:
        """
        Return JSON Schema for Frontend Form Rendering.
        https://rjsf-team.github.io/
        """
        schema = await AISettingJsonSchemas().get_ai_setting_schema(ai_agent_type=self.type)
        if not schema:
            return None

        """
        Dinamically modify the JSON Schema, inject the agent types into the schema
        """
        agent_type = schema["JSONSchema"]["properties"].get("type")
        agent_model = schema["JSONSchema"]["properties"].get("model")
        oneOf = []
        if agent_type is not None:
            agent_type["default"] = self.type.value
            for at in AIAgentType:
                if at in [AIAgentType.QA, AIAgentType.CHAT]:
                    oneOf.append({
                        "type": "string",
                        "title": at.name,
                        "enum": [at.value]
                    })
            agent_type["oneOf"] = oneOf

        if agent_model is not None:
            # todo: should hava a ai setting business module to manage this code
            from ai_shared.llmodel import ModelKind, LlModel
            model_names = LlModel.get_available_model_names_for_schema(model_kind=ModelKind.CHAT)
            agent_model["enum"] = model_names
            agent_model["default"] = model_names[0]

            # embedding_model_list = LlModel.get_model_list(edition=edition, model_kind=ModelKind.EMBEDDING)
            # embedding_model_names = [model_info.name.value for model_info in embedding_model_list]

        return schema

    async def get_json_schema(self, indent=0) -> str:
        schema = await self.get_schema()
        return json.dumps(schema, indent=indent)


class BaseAdvancedAISetting(BaseAISetting):
    temperature = Field(
        default=0.7,
        alias="temperature",
        description="What sampling temperature to use, between 0 and 2. Higher values like 0.8 will make the output more random, while lower values like 0.2 will make it more focused and deterministic.",
    )
    top_p = Field(
        default=1.0,
        alias="topP",
        description="An alternative to sampling with temperature, called nucleus sampling, where the model considers the results of the tokens with top_p probability mass. So 0.1 means only the tokens comprising the top 10% probability mass are considered.We generally recommend altering this or temperature but not both.",
    )
    n: int = Field(
        default=1,
        alias="n",
        description="How many chat completion choices to generate for each input message",
    )
    max_tokens: int = Field(
        default=max_int,
        alias="maxTokens",
        description="The maximum number of tokens to generate in the chat completion.",
    )
    presence_penalty: float = Field(
        default=0,
        alias="presencePenalty",
        description="Number between -2.0 and 2.0. Positive values penalize new tokens based on whether they appear in the text so far, increasing the model's likelihood to talk about new topics.",
    )  # "惩罚机制"
    frequency_penalty: float = Field(
        default=0,
        alias="frequencyPenalty",
        description="Number between -2.0 and 2.0. Positive values penalize new tokens based on their existing frequency in the text so far, decreasing the model's likelihood to repeat the same line verbatim.",
    )  # "惩罚机制"
    logit_bias: Optional[float] = Field(
        default=None,
        alias="logitBias",
        description="Accepts a json object that maps tokens (specified by their token ID in the tokenizer) to an associated bias value from -100 to 100. Mathematically, the bias is added to the logits generated by the model prior to sampling. The exact effect will vary per model, but values between -1 and 1 should decrease or increase likelihood of selection; values like -100 or 100 should result in a ban or exclusive selection of the relevant token.",
    )  # "惩罚机制"

