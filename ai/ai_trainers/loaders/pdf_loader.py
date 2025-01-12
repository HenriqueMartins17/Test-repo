from typing import Iterator

from langchain_community.document_loaders import PyPDFLoader

from ai_shared.persist import DataSource
from ai_shared.persist.data_source import count_openai_tokens, count_words_and_characters
from ai_trainers.loaders import loader_utils
from ai_trainers.loaders.base import BaseDataSourceLoader


class PDFLoader(BaseDataSourceLoader):
    def __init__(self, file_path: str):
        super().__init__()
        self.file_path = file_path

    def lazy_load(self) -> Iterator[DataSource]:
        loader = PyPDFLoader(self.file_path)
        docs = loader.load_and_split()

        text_splitter = loader_utils.get_text_splitter()
        all_splits = text_splitter.split_documents(docs)

        # just use like markdown
        total_words = 0
        total_chars = 0
        total_tokens = 0
        type_id = self.file_path
        for doc in all_splits:
            words, chars = count_words_and_characters(doc.page_content)
            tokens = count_openai_tokens(doc.page_content)
            total_words += words
            total_chars += chars
            total_tokens += tokens

            doc.metadata = {
                "id": self.file_path,
                "type_id": type_id,  # data_source
                "source": self.file_path,
                "type": "pdf",
                "datasheet_id": "",
                "record_id": "",
                # "primary_key": self.file_path,
                "words": words,
                "characters": chars,
                "tokens": tokens,
            }

        result = DataSource(
            type="pdf",
            type_id=type_id,
            documents=all_splits,
            words=total_words,
            characters=total_chars,
            tokens=total_tokens,
        )
        yield result
