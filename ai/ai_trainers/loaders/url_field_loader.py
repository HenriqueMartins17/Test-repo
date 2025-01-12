import traceback
from typing import Any, Iterator, Union
from urllib.parse import urlparse

from loguru import logger

from ai_shared.persist.data_source import DataSource
from ai_shared.tracking import Tracking
from ai_trainers.loaders.base import BaseDataSourceLoader
from ai_trainers.loaders.url_loader import URLLoader


class UrlFieldLoader(BaseDataSourceLoader):
    def __init__(self, url_data: Any) -> None:
        super().__init__()
        self.url_data = url_data

    def lazy_load(self) -> Iterator[DataSource]:
        """Lazy load attachments from record fields."""
        for ds in self._load_url(url_data=self.url_data):
            if not ds:
                continue

            yield ds

    def _load_url(self, url_data: Any) -> Iterator[Union[DataSource, None]]:
        url = url_data.get("text", "")
        logger.debug(url)
        if not url:
            yield None
        parsed_url = urlparse(url)
        if not parsed_url.scheme:
            yield None
        if not parsed_url.netloc:
            yield None

        try:
            url_loader = URLLoader(url=url)
            ds = url_loader.lazy_load()
        except Exception as e:
            Tracking.capture_exception(e)
            logger.error(f"_load_url error: {e=}, {url=}")
            logger.error(traceback.format_exc())
            yield None
        else:
            if not ds.characters:
                yield None

            yield ds
