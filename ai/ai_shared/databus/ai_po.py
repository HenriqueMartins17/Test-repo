from enum import Enum
from typing import Any, List, Optional, Union, Tuple

from pydantic import BaseModel, Field

from ai_shared.ai_setting import AIAgentType, AISettingFactory, AiSetting
from ai_shared.exceptions import AiAgentTypeError
from ai_shared.llmodel import ModelNameOpenAI


class Ai(BaseModel):
    ai_id: str = Field(alias="aiId")
    type: Optional[AIAgentType] = Field(alias="type")
    model: Optional[str] = Field(default=None, alias="model")
    embedding_model: Optional[str] = Field(default=None, alias="embeddingModel")
    prompt: Optional[str] = Field(default="", alias="prompt")
    setting: Optional[str] = Field(default="", alias="setting")
    prologue: Optional[str] = Field(default="", alias="prologue")

    def to_ai_setting(self) -> AiSetting:
        """
        Get AI Model PO, Bot Type, and setting
        """
        ai_setting_raw: dict = AISettingFactory.deserialize_setting_json(self.setting)

        _type = ai_setting_raw.get("type")
        if _type:
            ai_type = AIAgentType(_type)
        else:
            ai_type = self.type

        if not ai_type:
            raise AiAgentTypeError(f"{ai_type=}, {ai_setting_raw=}, {self.type=}")

        # combine ai_setting_raw and AISettingModel to ai_setting
        ai_setting = AISettingFactory.new(ai_type, ai_setting_raw)

        prompt = ai_setting_raw.get("prompt")
        prologue = ai_setting_raw.get("prologue")
        model = ai_setting_raw.get("model")

        if prompt is None and self.prompt:
            ai_setting.prompt = self.prompt

        if prologue is None and self.prologue:
            ai_setting.prologue = self.prologue

        if model is None and self.model:
            ai_setting.model = self.model

        return ai_setting


class AiNodeAirTableSetting(BaseModel):
    base_id: Optional[str] = Field(alias="baseId")
    table_id: Optional[str] = Field(alias="tableId")
    api_token: Optional[str] = Field(alias="apiKey")


class AiNodeAiTableSetting(BaseModel):
    datasheet_id: Optional[str] = Field(alias="datasheetId")
    view_id: Optional[str] = Field(alias="viewId")
    api_token: Optional[str] = Field(alias="apiKey")


class AiNodeFileSetting(BaseModel):
    name: Optional[str] = Field(alias="name")
    url: Optional[str] = Field(alias="url")


class AiNodeDatasheetSetting(BaseModel):
    # tips: old data will not be sync to setting, datasheet_id etc.
    datasheet_id: Optional[str] = Field(alias="datasheetId")
    datasheet_name: Optional[str] = Field(alias="datasheetName")
    view_id: Optional[str] = Field(alias="viewId")
    rows: Optional[int] = Field(alias="rows")
    revision: Optional[int] = Field(alias="revision")
    fields: Optional[List[Any]] = Field(alias="fields")  # todo: types


AiNodeSetting = Union[AiNodeAirTableSetting, AiNodeAiTableSetting, AiNodeFileSetting, AiNodeDatasheetSetting]


class AiNodeType(Enum):
    AIRTABLE = "airtable"
    AITABLE = "aitable"
    FILE = "file"
    DATASHEET = "datasheet"

    def is_airtable(self):
        return self == AiNodeType.AIRTABLE

    def is_aitable(self):
        return self == AiNodeType.AITABLE

    def is_file(self):
        return self == AiNodeType.FILE

    def is_datasheet(self):
        return self == AiNodeType.DATASHEET


class AiNode(BaseModel):
    """AI 1--* Node"""
    id: int = Field(alias="id")
    ai_id: str = Field(alias="aiId")
    node_id: Optional[str] = Field(alias="nodeId")
    node_type: Optional[str] = Field(alias="nodeType")
    version: Optional[int] = Field(alias="version")
    type: Optional[AiNodeType] = Field(alias="type", description="airtable, aitable, file, datasheet")
    setting: Optional[AiNodeSetting] = Field(alias="setting")

    @classmethod
    def new(cls, data: dict) -> "AiNode":
        setting: dict = data.pop("setting", {})
        ai_node = AiNode(**data)
        if not ai_node.type:
            ai_node.type = AiNodeType.DATASHEET
        ai_node.set_setting(setting)
        return ai_node

    def set_setting(self, setting: dict) -> None:
        if self.type.is_airtable():
            self.setting = AiNodeAirTableSetting(**setting)
        elif self.type.is_aitable():
            self.setting = AiNodeAiTableSetting(**setting)
        elif self.type.is_file():
            self.setting = AiNodeFileSetting(**setting)
        elif self.type.is_datasheet():
            self.setting = AiNodeDatasheetSetting(**setting)
            if not self.setting.datasheet_id and self.node_id:
                self.setting.datasheet_id = self.node_id
        else:
            self.setting = None
