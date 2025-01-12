from typing import Optional

from pydantic import Field

from ai_shared.ai_setting.base_ai_setting import BaseAdvancedAISetting, AIAgentType
from ai_shared.config import settings

DEFAULT_PROMPT = """Use the following pieces of context to answer the question at the end. 
If you don't know the answer, just say that you don't know, don't try to make up an answer. 

Context:
{context} 

Chat History:
{history}

Question: 
{input}

Helpful Answer:
"""

DEFAULT_PROMPT_IN_VIKA = """请基于“聊天上下文”和“引用资料”，来回答用户的问题。
如果你不知道答案，可以直接说不知道，不要试图编造答案。

聊天记录:
{history}

引用资料:
{context} 

用户的问题是: 
{input}

你给出的答案或解释是:
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
    prologue = "Welcome! I am your intelligent Q&A assistant. Is there anything I can help you with?"

    if edition.is_vika_saas():
        prologue = "欢迎！我是您的智能问答助手。请问有什么可以帮您解决的问题吗？"

    return prologue

def get_default_idk_by_edition():
    """get default prologue by edition"""

    edition = settings.edition
    prologue = "I'm sorry, but I currently don't have any relevant information in my knowledge base to answer your question."

    if edition.is_vika_saas():
        prologue = "很抱歉，但我目前的知识库中没有任何相关信息来回答您的问题。"

    return prologue

class QAAISetting(BaseAdvancedAISetting):
    type: Optional[AIAgentType] = Field(default=AIAgentType.QA, alias="type")
    prompt: Optional[str] = Field(default_factory=get_default_prompt_by_edition, alias="prompt")
    prologue: Optional[str] = Field(default_factory=get_default_prologue_by_edition, alias="prologue")
    idk: Optional[str] = Field(default_factory=get_default_idk_by_edition, alias="idk")

    # score must larger that this threshold to be considered as a valid context
    score_threshold: Optional[float] = Field(default=0.6001, ge=0, le=1, alias="scoreThreshold")
    temperature: Optional[float] = Field(default=0.1, ge=0, le=1, alias="temperature")
    
    is_enable_open_url: Optional[bool] = Field(default=False, alias="isEnableOpenUrl")
    open_url: Optional[str] = Field(default="", alias="openUrl")
    open_url_title: Optional[str] = Field(default="", alias="openUrlTitle")
    
    is_enable_collect_information: Optional[bool] = Field(default=False, alias="isEnableCollectInformation")
    form_id: Optional[str] = Field(default=None, alias="formId", description="Form ID for QA AI")

    async def get_schema(self) -> Optional[dict]:
        schema = await super().get_schema()
        
        # The code below is a demo for how to modify the JSON Schema dynamically.
        prompt = schema["JSONSchema"]["allOf"][0]["then"]["properties"]["prompt"]
        prompt["default"] = get_default_prompt_by_edition()

        prologue = schema["JSONSchema"]["properties"]["prologue"]
        prologue["default"] = get_default_prologue_by_edition()

        idk = schema["JSONSchema"]["properties"]["idk"]
        idk["default"] = get_default_idk_by_edition()

        return schema