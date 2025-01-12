from typing import Any, Iterator

from loguru import logger

from ai_shared.persist import DataSource
from .apitable_databus import APITableDataBusLoader

__all__ = [
    "Loader",
    "APITableDataBusLoader",
]

from .attachments_field_loader import AttachmentFieldLoader

from .url_field_loader import UrlFieldLoader


class Loader:

    @staticmethod
    def load_url(url_data: Any) -> Iterator[DataSource]:
        logger.debug(f"Loader.load_url: {url_data=}")
        url_loader = UrlFieldLoader(url_data)
        for ds in url_loader.lazy_load():
            yield ds

    @staticmethod
    def load_attachment(attach_data: Any) -> Iterator[DataSource]:
        logger.debug(f"Loader.load_attachment: {attach_data=}")
        attachment_loader = AttachmentFieldLoader(attach_data)
        for ds in attachment_loader.lazy_load():
            yield ds
