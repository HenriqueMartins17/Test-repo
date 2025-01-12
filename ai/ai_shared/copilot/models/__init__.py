import json
import os.path
import time
from datetime import datetime
from enum import Enum
from typing import Optional, Self, Any
from typing import Optional, Union

import aiofiles
from loguru import logger
from openai import AsyncClient
from openai.types.beta.assistant import Assistant as OpenAiAssistant
from openai.types.beta.thread import Thread as OpenAiThread
from openai.types.beta.threads import ThreadMessage as OpenAiMessage

from langchain.schema import BaseMessage
from pydantic import BaseModel, Field
import uuid
from uuid import UUID

from ai_shared.ai_setting import AiSetting
from ai_shared.config import settings
from ai_shared.tracking import Tracking
from ai_shared.vos import OpenAIChatCompletionType, OpenAIChatCompletionStatus


class AssistantType(Enum):
    DATA = "data"
    HELP = "help"
    AUTO = "auto"


class Assistants(Enum):
    data = OpenAiAssistant(
        id=settings.openai_data_agent_assistant_id,
        created_at=0,
        name="Data Assistant",
        instructions="",
        description="Collects and analyzes data.",
        object="assistant",
        model="",
        file_ids=[],
        tools=[]
    )
    help = OpenAiAssistant(
        id="asst_RwsLonYiQevj6c4wnPzTy3Nt",
        created_at=0,
        name="Help Assistant",
        instructions="",
        description="Provides user support and assistance",
        object="assistant",
        model="",
        file_ids=[],
        tools=[]
    )
    auto = OpenAiAssistant(
        id="asst_RwsLonYiQevj6c4wnPzTy3Nt",
        created_at=0,
        name="Auto Assistant",
        instructions="",
        description="Auto assistant",
        object="assistant",
        model="",
        file_ids=[],
        tools=[]
    )

    @classmethod
    def get_assistant_by_type(cls, assistant_type: AssistantType) -> OpenAiAssistant:
        if assistant_type == AssistantType.DATA:
            return Assistants.data.value
        elif assistant_type == AssistantType.HELP:
            return Assistants.help.value
        elif assistant_type == AssistantType.AUTO:
            return Assistants.auto.value
        else:
            return Assistants.help.value


class Message(BaseMessage):
    id: UUID = Field(default_factory=uuid.uuid4)
    chat_completion_type: Optional[OpenAIChatCompletionType] = Field(default=OpenAIChatCompletionType.CHAT)
    chat_completion_status: Optional[OpenAIChatCompletionStatus] = Field(default=None)
    conversation_id: UUID = Field()
    openai_message_id: Optional[str] = Field()
    openai_thread_id: Optional[str] = Field()
    openai_run_id: Optional[str] = Field()
    openai_assistant_id: Optional[str] = Field()
    created_at: Optional[int] = Field()
    file_ids: Optional[list[str]] = Field(default_factory=list)
    metadata: Optional[dict] = Field(default_factory=dict)
    extra: Optional[Any] = Field()


class Conversation(BaseModel):
    id: UUID = Field()
    messages: Optional[list[Message]] = Field(default=[])
    openai_thread_id: Optional[str] = Field()
    openai_assistant_id: Optional[str] = Field()
    current_datasheet_view_id: Optional[str] = Field()
    current_datasheet_revision: Optional[int] = Field()
    current_run_id: Optional[str] = Field()
    current_file_ids: Optional[list[str]] = Field(default=[])
    history_file_ids: Optional[list[str]] = Field(default=[])
    created_at: Optional[int] = Field()
    updated_at: Optional[int] = Field()
    metadata: Optional[dict] = Field(default_factory=dict)
    info: Optional[str] = Field()
    extra: Optional[Any] = Field()

    @staticmethod
    def make_conversation_dir_path():
        from ...persist import Persist
        ai_id = "copilot"
        training_id = "0"
        path = f"{Persist.get_root_path()}/{ai_id}/{training_id}"
        return f"{path}/conversations"

    def get_conversation_dir_path(self):
        return Conversation.make_conversation_dir_path()

    @staticmethod
    def make_conversation_file_path(conversation_id: UUID) -> str:
        conversation_dir_path = Conversation.make_conversation_dir_path()
        return f"{conversation_dir_path}/{str(conversation_id)}.json"

    def get_conversation_file_path(self) -> str:
        conversation_dir_path = Conversation.make_conversation_dir_path()
        return f"{conversation_dir_path}/{str(self.id)}.json"

    @staticmethod
    def exist(conversation_id: UUID):
        """Check if the Conversation exists"""
        return os.path.exists(Conversation.make_conversation_file_path(conversation_id))

    @staticmethod
    async def read_conversation(conversation_id: UUID) -> Optional["Conversation"]:
        logger.info(f"Reading conversation: {conversation_id=}")
        file_path = Conversation.make_conversation_file_path(conversation_id=conversation_id)
        if not os.path.exists(file_path):
            logger.info(f"Conversation not exists yet: {file_path=}")
            return None

        try:
            async with aiofiles.open(file_path, mode="r", encoding="utf-8") as f:
                contents = await f.read()
            data = json.loads(contents)
        except Exception as e:
            Tracking.capture_exception(e)
            logger.error(f"Conversation load error: {file_path=} {contents=}")
            return None
        else:
            return Conversation.parse_obj(data)

    async def save(self) -> None:
        """save conversation to disk"""
        import fcntl
        from ...persist import Persist

        self.updated_at = int(time.time())

        file_path = self.get_conversation_file_path()
        Persist.ensure_directory_exists(file_path)
        if settings.is_dev_mode():
            contents = self.json(ensure_ascii=False, indent=4)
        else:
            contents = self.json(ensure_ascii=False)
        async with aiofiles.open(file_path, mode='w', encoding="utf-8") as f:
            fcntl.flock(f, fcntl.LOCK_EX | fcntl.LOCK_NB)
            await f.write(contents)
            fcntl.flock(f, fcntl.LOCK_UN)
        logger.info(f"Conversation saved: {file_path=}")

    def has_talked(self) -> bool:
        """Check if conversation has talked"""
        return len(self.messages) > 0
