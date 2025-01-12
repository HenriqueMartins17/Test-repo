import json
import os
from typing import List, Union, Optional

import aiofiles
from loguru import logger
from pydantic import Field
from tenacity import retry, stop_after_attempt, wait_fixed, retry_if_result, wait_exponential, wait_exponential_jitter

from ai_shared.persist.training_info import TrainingInfo
from ..databus import AiNode

from ..exceptions import AiNotFoundError
from ..helper import is_none
from ..tracking import Tracking
from .base import BaseFilePO


class AIInfo(BaseFilePO):
    """
    A persistent model of AI. Store in JSON
    """

    ai_id: str
    current_training_id: Optional[str] = Field(default=None)
    # train history is a list of success training ids, train folders means all training process folders
    success_train_history: Optional[List[str]] = Field(default=[])
    locking_training_id: Optional[str] = Field(default=None)

    ai_node_training_ids: Optional[dict[int, list[str]]] = Field(default={})

    async def save(self) -> None:
        """Save the AI model to disk."""
        import fcntl

        from ._persist import Persist

        file_path = AIInfo.get_ai_info_file_path(self.ai_id)
        Persist.ensure_directory_exists(file_path)
        contents = json.dumps(self.dict())
        logger.info(f"Saving AIInfo to {file_path=}, {contents=}")
        async with aiofiles.open(file_path, mode='w', encoding="utf-8") as f:
            fcntl.flock(f, fcntl.LOCK_EX | fcntl.LOCK_NB)
            await f.write(contents)
            fcntl.flock(f, fcntl.LOCK_UN)

    async def get_training_info(self, training_id: str) -> Union[TrainingInfo, None]:
        """
        Get TrainingInfo model by AI ID + Training ID
        """
        return await TrainingInfo.load_training_info(self.ai_id, training_id)

    def get_trainings_folders(self):
        """
        Get all training ids
        """
        path = AIInfo.get_ai_info_dir_path(self.ai_id)
        folders = []
        for item in os.listdir(path):
            if os.path.isdir(os.path.join(path, item)):
                folders.append(item)
        return folders

    async def lock(self, training_id: str) -> None:
        """Set the the training id to ai model.

        Args:
            training_id (str): the training id
        """
        self.locking_training_id = training_id
        await self.save()

    async def unlock(self) -> None:
        """Unset the the training id to ai model."""
        self.locking_training_id = None
        await self.save()

    @staticmethod
    def exist(ai_id: str):
        """Check if the AIInfo with ai_id exists"""
        return os.path.exists(AIInfo.get_ai_info_file_path(ai_id))

    @staticmethod
    async def new(ai_id: str) -> "AIInfo":
        """
        Create and save(persist) a new AIInfo object
        """
        ai_info = AIInfo(ai_id=ai_id)
        await ai_info.save()
        return ai_info

    @staticmethod
    async def load_ai_info(ai_id: str) -> Optional["AIInfo"]:
        try:
            ai_info = await AIInfo._load_ai_info(ai_id)
        except Exception as e:
            # RetryError
            Tracking.capture_exception(e)
            logger.error(f"load_ai_info error: {ai_id=}, {e=}")
            return None
        else:
            return ai_info

    @staticmethod
    @retry(retry=retry_if_result(is_none), stop=stop_after_attempt(3), wait=wait_exponential_jitter())
    async def _load_ai_info(ai_id: str) -> Union["AIInfo", None]:
        """
        Get the AI info from the index file
        """
        try:
            file_path = AIInfo.get_ai_info_file_path(ai_id)
            if not os.path.exists(file_path):
                raise AiNotFoundError(f"AI info file {file_path} not found.")

            async with aiofiles.open(file_path, mode='r', encoding="utf-8") as f:
                contents = await f.read()

            logger.info(f"_load_ai_info: {ai_id=}, {file_path=}, {contents=}")
            data = json.loads(contents)
            ai_info = AIInfo.parse_obj(data)
        except Exception as e:
            Tracking.capture_exception(e)
            logger.error(f"_load_ai_info error: {ai_id=}, {e=}")
            return None
        else:
            return ai_info

    @staticmethod
    def get_ai_info_dir_path(ai_id):
        from ._persist import Persist

        path = f"{Persist.get_root_path()}/{ai_id}"
        return path

    @staticmethod
    def get_ai_info_file_path(ai_id):
        """
        Full path of AI info file
        """
        dir_path = AIInfo.get_ai_info_dir_path(ai_id)
        path = f"{dir_path}/info.json"
        return path

    def get_training_ids_latest(self, ai_nodes: list[AiNode]) -> list[str]:
        ids = []

        if self.ai_node_training_ids:
            ai_node_ids = [ai_node.id for ai_node in ai_nodes]
            for ai_node_id, training_ids in self.ai_node_training_ids.items():
                if ai_node_id in ai_node_ids:
                    ids.append(training_ids[-1])

        # current_training_id for old trained AiInfo data
        if self.current_training_id and self.current_training_id not in ids:
            ids.append(self.current_training_id)
        return ids

    def get_training_ids_all(self, ai_nodes: list[AiNode]) -> list[str]:
        if self.ai_node_training_ids is None:
            return []

        ai_node_ids = [ai_node.id for ai_node in ai_nodes]
        ids = []
        for ai_node_id, training_ids in self.ai_node_training_ids.items():
            if ai_node_id in ai_node_ids:
                ids.extend(training_ids)
        return sorted(ids, reverse=True)

    def get_ai_node_training_ids(self, ai_node_id: int) -> list[str]:
        if self.ai_node_training_ids is None:
            return []

        ids = self.ai_node_training_ids.get(ai_node_id)
        if not ids:
            return []
        return sorted(ids, reverse=True)

    @staticmethod
    async def add_ai_node_training_id(ai_id: str, ai_node_id: int, training_id: str) -> None:
        ai_info = await AIInfo.load_ai_info(ai_id)
        if not ai_info:
            raise AiNotFoundError(f"add_ai_node_training_id: {ai_id=}")
        if ai_info.ai_node_training_ids is None:
            ai_info.ai_node_training_ids = {}

        ids = ai_info.ai_node_training_ids.get(ai_node_id, [])
        if training_id not in ids:
            ids.append(training_id)
            ai_info.ai_node_training_ids[ai_node_id] = ids
            await ai_info.save()
        return
