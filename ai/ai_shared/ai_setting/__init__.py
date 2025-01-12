import json
from enum import Enum
from typing import Any, Union

from ai_shared.ai_setting.base_ai_setting import BaseAISetting, AISettingMode, AIAgentType, BaseAdvancedAISetting
from ai_shared.ai_setting.chat_ai_setting import ChatAISetting
from ai_shared.ai_setting.creator_ai_setting import CreatorAISetting
from ai_shared.ai_setting.data_ai_setting import DataAISetting
from ai_shared.ai_setting.qa_ai_setting import QAAISetting

__all__ = [
    "AIAgentType",
    "AISettingFactory",
    "AiSetting",
    "QAAISetting",
    "ChatAISetting",
    "CreatorAISetting",
    "DataAISetting",
]


from loguru import logger

from ai_shared.exceptions import AiSettingParseError
from ai_shared.tracking import Tracking

AiSetting = Union[BaseAISetting, BaseAdvancedAISetting, QAAISetting, ChatAISetting, CreatorAISetting, DataAISetting]


class AISettingFactory:
    """
    Create an AI bot setting
    """

    @staticmethod
    def new(agent_type: AIAgentType, ai_setting_any: Any) -> AiSetting:

        ai_setting_any: dict = AISettingFactory.deserialize_setting_json(ai_setting_any)

        # fix when {"mode": "Wizard"} in database
        mode = ai_setting_any.get("mode", AISettingMode.WIZARD.value)
        ai_setting_any["mode"] = str(mode).lower()

        if agent_type == AIAgentType.QA:
            return QAAISetting.parse_obj(ai_setting_any)
        elif agent_type == AIAgentType.CHAT:
            return ChatAISetting.parse_obj(ai_setting_any)
        elif agent_type == AIAgentType.CREATOR:
            return CreatorAISetting.parse_obj(ai_setting_any)
        elif agent_type == AIAgentType.DATA:
            return DataAISetting.parse_obj(ai_setting_any)
        else:
            raise AiSettingParseError(f"AISetting parse not supported: {agent_type=}, {ai_setting_any=}")
        
    @staticmethod
    def deserialize_setting_json(ai_setting_any: Any):
        """
        Deserialize AI setting json to AI setting object
        """
        try:
            if ai_setting_any is None or ai_setting_any == "":
                ai_setting_any = {}
            elif isinstance(ai_setting_any, str):
                ai_setting_any = json.loads(ai_setting_any)
            else:
                ai_setting_any = ai_setting_any

        except Exception as e:
            Tracking.capture_exception(e)
            ai_setting_any = {}
            import traceback
            logger.error(e)
            logger.error(traceback.format_exc())

        assert isinstance(ai_setting_any, dict)
        return ai_setting_any 
