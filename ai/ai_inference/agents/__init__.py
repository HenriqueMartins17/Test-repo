from typing import List, Optional, Union
from ai_inference.agents.creator import CreatorAgent
from ai_inference.agents.data import DataAgent

from langchain.callbacks.manager import Callbacks

from ai_inference.agents.chat import ChatAgent
from ai_inference.agents.qa import QAAgent
from ai_shared.ai_setting import AIAgentType, AiSetting
from ai_shared.exceptions import AiAgentTypeError
from ai_shared.ros import OpenAIChatCompletionRO


class AgentFactory:
    @staticmethod
    def new(
        ai_id: str,
        ai_setting: AiSetting,
        model_kwargs: OpenAIChatCompletionRO,
        # conversation_id: str,
        prompt_template: str = None,
        streaming: bool = None,
        callbacks: Optional[List[Callbacks]] = None,
    ):
        assert isinstance(ai_setting, AiSetting)
        agent_type = ai_setting.type
        if agent_type == AIAgentType.QA:
            return QAAgent(
                ai_id,
                ai_setting,
                model_kwargs,
                streaming=streaming,
                prompt_template=prompt_template,
                callbacks=callbacks,
            )
        elif agent_type == AIAgentType.CHAT:
            return ChatAgent(
                ai_id,
                ai_setting,
                model_kwargs,
                streaming=streaming,
                prompt_template=prompt_template,
                callbacks=callbacks,
            )
        elif agent_type == AIAgentType.CREATOR:
            return CreatorAgent(
                ai_id,
                ai_setting,
                model_kwargs,
                streaming=streaming,
                prompt_template=prompt_template,
                callbacks=callbacks,
            )
        elif agent_type == AIAgentType.DATA:
            return DataAgent(
                ai_id,
                ai_setting,
                model_kwargs,
                streaming=streaming,
                prompt_template=prompt_template,
                callbacks=callbacks,
            )
        else:
            raise AiAgentTypeError(f"Agent type not supported: {ai_id=}, {agent_type=}, {ai_setting=}")
