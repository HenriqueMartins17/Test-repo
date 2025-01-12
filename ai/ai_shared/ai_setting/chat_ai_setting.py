from typing import Optional
from pydantic import Field
from ai_shared.ai_setting.base_ai_setting import BaseAdvancedAISetting, AIAgentType
from ai_shared.config import settings

DEFAULT_PROMPT = """The following is a conversation between a human and an AI.

Current conversation:
{history}

Human:
{input}

AI:
"""

DEFAULT_PROMPT_IN_VIKA = """你是一个幽默的、知识渊博的AI助手，负责与用户进行对话并详细解答用户的问题。

上下文:
{history}

用户的下一个问题是: 
{input}

你的回答是:
"""

def get_default_prompt_by_edition():
    """get default prompt by edition"""

    edition = settings.edition
    prompt =  DEFAULT_PROMPT

    if edition.is_vika_saas():
        prompt = DEFAULT_PROMPT_IN_VIKA

    return prompt

def get_default_prologue_by_edition():
    """get default prologue by edition"""

    edition = settings.edition
    prologue = "Welcome aboard! I'm all set and ready to chat with you. Let's have some great conversations!"

    if edition.is_vika_saas():
        prologue = "欢迎来到与AI互动的世界！我是AI助理，期待与您有一段愉快的对话体验！"

    return prologue


class ChatAISetting(BaseAdvancedAISetting):
    type: Optional[AIAgentType] = Field(default=AIAgentType.CHAT, alias="type")
    prompt: Optional[str] = Field(default_factory=get_default_prompt_by_edition, alias="prompt")
    prologue: Optional[str] = Field(default_factory=get_default_prologue_by_edition, alias="prologue")
    temperature: Optional[float] = Field(default=0.7, ge=0, le=1, alias="temperature")
    is_enabled_prompt_box: bool = Field(default=False, alias="isEnabledPromptBox", exclude=True)
    is_enabled_prompt_tips: bool = Field(default=False, alias="isEnabledPromptTips", exclude=True)

    async def get_schema(self) -> Optional[dict]:
        schema = await super().get_schema()

        # The code below is a demo for how to modify the JSON Schema dynamically.
        prompt = schema["JSONSchema"]["allOf"][0]["then"]["properties"]["prompt"]
        prompt["default"] = get_default_prompt_by_edition()

        prologue = schema["JSONSchema"]["properties"]["prologue"]
        prologue["default"] = get_default_prologue_by_edition()

        return schema
