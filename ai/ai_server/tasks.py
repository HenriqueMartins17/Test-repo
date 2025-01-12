from typing import Iterator, Any

from loguru import logger

from ai_shared.persist import DataSource
from ai_trainers.loaders import Loader


# ai_server.tasks.xxx


class Tasks:
    """todo: Fake task now, change to real task later"""

    @staticmethod
    def add(x: int, y: int):
        logger.debug(f"Tasks.add: {x=}, {y=}")
        return x + y

    @staticmethod
    def load_url(url_data: Any) -> Iterator[DataSource]:
        logger.debug(f"Tasks.load_url: {url_data=}")
        return Loader.load_url(url_data)

    @staticmethod
    def load_attachment(attach_data: Any) -> Iterator[DataSource]:
        logger.debug(f"Tasks.load_attachment: {attach_data=}")
        return Loader.load_attachment(attach_data)

