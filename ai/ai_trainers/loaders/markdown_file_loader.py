from typing import Iterator, List

from langchain.docstore.document import Document
from langchain_community.document_loaders import UnstructuredMarkdownLoader
from pydantic import Field

from ai_shared.persist.data_source import (
    DataSource,
    count_openai_tokens,
    count_words_and_characters,
)
from ai_trainers.loaders import loader_utils
from ai_trainers.loaders.base import BaseDataSourceLoader


class MarkdownFileDataSource(DataSource):
    """
    Data Source instance
    """

    type: str = Field(default="markdown_file", const=True)


class MarkdownFileLoader(BaseDataSourceLoader):
    """Load DataSource from local Markdown file."""

    def __init__(self, markdown_file_path: str):
        self.markdown_file_path = markdown_file_path

    def lazy_load(self) -> Iterator[DataSource]:
        loader = UnstructuredMarkdownLoader(self.markdown_file_path)
        docs: List[Document] = loader.load()

        text_splitter = loader_utils.get_text_splitter()
        all_splits = text_splitter.split_documents(docs)

        total_words = 0
        total_chars = 0
        total_tokens = 0

        type_id = self.markdown_file_path
        for doc in all_splits:
            words, chars = count_words_and_characters(doc.page_content)
            tokens = count_openai_tokens(doc.page_content)
            total_words += words
            total_chars += chars
            total_tokens += tokens

            doc.metadata = {
                "id": self.markdown_file_path,
                "type_id": type_id,  # data_source
                "source": self.markdown_file_path,
                "type": "markdown",
                "datasheet_id": "",
                "record_id": "",
                # "primary_key": self.markdown_file_path,
                "words": words,
                "characters": chars,
                "tokens": tokens,
            }

        yield MarkdownFileDataSource(
            type="markdown_file",
            type_id=self.markdown_file_path,
            documents=all_splits,
            words=total_words,
            characters=total_chars,
            tokens=total_tokens,
        )
