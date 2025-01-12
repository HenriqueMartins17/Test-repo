from typing import Iterator, List

from ai_shared.persist.data_source import DataSource


class BaseDataSourceLoader:
    """
    Base Data source Loader.

    Different from LangChain Loader to load "Document"
    This Loader is used to load DataSource from local file system.
    """

    def lazy_load(self) -> Iterator[DataSource]:
        raise NotImplementedError()

    def load(self) -> List[DataSource]:
        return list(self.lazy_load())
