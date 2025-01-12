import json
import os
import random
from enum import Enum
from typing import Any, Dict, List, Optional, Union

import aiofiles
from loguru import logger
from pydantic import Field
from tenacity import retry_if_result, stop_after_attempt, wait_exponential_jitter, retry

from ..exceptions import ConversationNotFoundError, TrainingNotFoundError
from ..helper import is_none
from ..llmodel import LlModel
from ..tracking import Tracking
from .base import BaseFilePO
from .data_source import DataSource


class TrainingStatus(str, Enum):
    FAILED = "failed"
    NEW = "new"
    TRAINING = "training"
    SUCCESS = "success"


class TrainingInfo(BaseFilePO):
    ai_id: str
    training_id: str

    # extra information
    info: Optional[str] = Field(default=None)
    started_at: Optional[int] = Field(default=None)
    finished_at: Optional[int] = Field(default=None)

    status: TrainingStatus = TrainingStatus.NEW

    data_sources: Optional[List[DataSource]] = Field(default=[])
    
    suggestions: Optional[List[str]] = Field(default=[])
    embedding_model: Optional[str] = Field(default=None)

    def set_data_sources(self, data_sources_list: List[DataSource]):
        """
        Set List[DataSource]
        """
        self.data_sources = data_sources_list

    @staticmethod
    async def add_data_source(ai_id: str, training_id: str, ds: DataSource) -> None:
        training_info = await TrainingInfo.load_training_info(ai_id, training_id)
        if not training_info:
            raise TrainingNotFoundError(f"{ai_id=}, {training_id=}")
        if training_info.data_sources is None:
            training_info.data_sources = []
        training_info.data_sources.append(ds)
        await training_info.save()
        return

    async def save(self) -> None:
        """Save the  training info model to disk."""
        import fcntl

        from ._persist import Persist

        file_path = self.get_training_info_path()
        Persist.ensure_directory_exists(file_path)
        contents = json.dumps(self.dict())
        logger.info(f"Saving TrainingInfo to {file_path=}, {contents=}")
        async with aiofiles.open(file_path, mode='w', encoding="utf-8") as f:
            fcntl.flock(f, fcntl.LOCK_EX | fcntl.LOCK_NB)
            await f.write(contents)
            fcntl.flock(f, fcntl.LOCK_UN)

    @staticmethod
    def make_training_path(ai_id: str, training_id: str) -> str:
        from ._persist import Persist

        path = f"{Persist.get_root_path()}/{ai_id}/{training_id}"
        return path

    def get_training_path(self) -> str:
        """
        Full path of training Vector DB persist path
        """
        return TrainingInfo.make_training_path(self.ai_id, self.training_id)

    @staticmethod
    def make_training_info_path(ai_id: str, training_id: str):
        return f"{TrainingInfo.make_training_path(ai_id, training_id)}/info.json"

    def get_training_info_path(self) -> str:
        return TrainingInfo.make_training_info_path(self.ai_id, self.training_id)

    @staticmethod
    def exist(ai_id: str, training_id: str):
        """Check if the AIInfo with ai_id exists"""
        file_path = TrainingInfo.make_training_info_path(ai_id, training_id)
        return os.path.exists(file_path)

    @staticmethod
    async def load_training_info(ai_id: str, training_id: str) -> Optional["TrainingInfo"]:
        try:
            training_info = await TrainingInfo._load_training_info(ai_id, training_id)
        except Exception as e:
            # RetryError
            Tracking.capture_exception(e)
            logger.error(f"load_training_info error: {ai_id=}, {training_id=}, {e=}")
            return None
        else:
            return training_info

    @staticmethod
    @retry(retry=retry_if_result(is_none), stop=stop_after_attempt(3), wait=wait_exponential_jitter())
    async def _load_training_info(ai_id: str, training_id: str) -> Union["TrainingInfo", None]:
        """
        Get the AI info from the index file
        """
        try:
            file_path = TrainingInfo.make_training_info_path(ai_id, training_id)
            if not os.path.exists(file_path):
                raise TrainingNotFoundError(f"Training info file {file_path} not found.")

            async with aiofiles.open(file_path, mode='r', encoding="utf-8") as f:
                contents = await f.read()

            logger.info(f"_load_training_info: {ai_id=}, {training_id=}, {file_path=}, {contents=}")
            data = json.loads(contents)
            training_info = TrainingInfo.parse_obj(data)
        except Exception as e:
            Tracking.capture_exception(e)
            logger.error(f"_load_training_info error: {ai_id=}, {training_id=}, {e=}")
            return None
        else:
            return training_info

    def get_conversation_dir_path(self) -> str:
        """
        Full path of conversation folder
        """
        return f"{self.get_training_path()}/conversations"

    @staticmethod
    def make_conversation_file_path(ai_id: str, training_id: str, conversation_id: str) -> str:
        training_path = TrainingInfo.make_training_path(ai_id, training_id)
        return f"{training_path}/conversations/{conversation_id}.json"

    def get_conversation_file_path(
        self, conversation_id: str
    ) -> str:
        """Get conversation history logs file path in the folder named by current training id"""
        path = TrainingInfo.make_conversation_file_path(self.ai_id, self.training_id, conversation_id)
        return path

    async def get_conversations_list(self):
        """
        Get conversations JSONs file's list only, no conversations' detail items
        """
        path = self.get_conversation_dir_path()
        conversations = []
        if not os.path.exists(path):
            return conversations

        for item in os.listdir(path):
            full_file_path = os.path.join(path, item)
            if os.path.isfile(full_file_path):
                conversation = await self._get_read_conversation_file(full_file_path)
                conversations.append(conversation)
        return conversations

    async def get_conversation(self, conversation_id: str) -> List[Optional[Dict[str, Any]]]:
        """Get Conversation file JSON."""
        path = self.get_conversation_file_path(conversation_id)
        if not os.path.exists(path):
            raise ConversationNotFoundError(
                f"AI ID: {self.ai_id}, Training ID: {self.training_id}, Conversation ID: {conversation_id}"
            )
        return await self._get_read_conversation_file(path)

    async def _get_read_conversation_file(self, file_path) -> List[Optional[Dict[str, Any]]]:
        async with aiofiles.open(file_path, mode='r', encoding="utf-8") as f:
            contents = await f.read()

        conversation_histories = json.loads(contents)
        return conversation_histories

    def set_suggestions(self, suggestions: List[str]) -> None:
        assert isinstance(suggestions, list)
        if not suggestions:
            return
        self.suggestions = suggestions

    def get_suggestions(self, n: int = 5, question: str = None) -> List[str]:
        if not self.suggestions:
            return []

        if question:
            data = [s for s in self.suggestions if s != question]
            res = random.sample(data, min(n, len(data)))
            return res
        return random.sample(self.suggestions, min(n, len(self.suggestions)))

    def get_embedding_model(self):
        """if no embedding_model saved, it is openai, before baidu qianfan take online"""
        embedding_model = self.embedding_model
        if not embedding_model:
            embedding_model = LlModel.get_default_embedding_model_of_openai()
        return embedding_model
