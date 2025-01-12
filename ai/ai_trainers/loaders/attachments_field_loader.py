import mimetypes
import traceback
from typing import Any, Iterator, List, Callable, Union

from langchain.docstore.document import Document
from langchain_community.document_loaders import (
    OnlinePDFLoader,
    UnstructuredMarkdownLoader,
    UnstructuredWordDocumentLoader, PyMuPDFLoader,
)
from langchain.document_loaders.pdf import BasePDFLoader
from loguru import logger

from ai_shared.config import settings
from ai_shared.persist.data_source import (
    DataSource,
    count_openai_tokens,
    count_words_and_characters,
)
from ai_shared.tracking import Tracking
from ai_trainers.loaders import loader_utils
from ai_trainers.loaders.base import BaseDataSourceLoader


class MyOnlinePDFLoader(OnlinePDFLoader):
    """Load online `PDF`."""

    def load(self) -> List[Document]:
        """Load documents."""
        loader = PyMuPDFLoader(str(self.file_path))
        return loader.load()


class MyOnlineMarkdownLoader(BasePDFLoader):
    """Load online `.md`."""

    def load(self) -> List[Document]:
        """Load documents."""
        loader = UnstructuredMarkdownLoader(str(self.file_path), strategy="fast")
        return loader.load()


class MyOnlineDocxLoader(BasePDFLoader):
    """Load online `.docx`."""

    def load(self) -> List[Document]:
        """Load documents."""
        loader = UnstructuredWordDocumentLoader(str(self.file_path), strategy="fast")
        return loader.load()


class AttachmentFieldLoader(BaseDataSourceLoader):
    def __init__(self, attach_data: Any) -> None:
        super().__init__()
        self.attach_data = attach_data

    def lazy_load(self) -> Iterator[DataSource]:
        """Lazy load attachments from record fields."""
        for ds in self._load_attach(attach_data=self.attach_data):
            if not ds:
                continue

            yield ds

    def _load_attach(self, attach_data: Any) -> Iterator[Union[DataSource, None]]:
        mime_type = attach_data.get("mimeType", "")
        file_token = attach_data.get("token", "")
        if not file_token:
            yield None

        # Example: space/2023/09/20/2c645c53a35b49c78329ece3223c3003
        hash_token = file_token.split("/")[-1]
        if not hash_token:
            yield None

        file_name = attach_data.get("name", "")
        if mime_type == mimetypes.types_map[".pdf"]:
            load_fn = self._load_pdf
        elif file_name.endswith(".md"):  # mimetypes.types_map not have .md
            load_fn = self._load_markdown
        elif mime_type == mimetypes.types_map[".doc"]:  # todo not sure yet
            logger.info(f"file type not support yet: {mime_type}")
            load_fn = None
        elif file_name.endswith(".docx"):  # mimetypes.types_map not have .docx
            load_fn = self._load_docx
        else:
            logger.info(f"file type not support yet: {mime_type}")
            load_fn = None

        try:
            ds = load_fn(file_token=file_token)
        except Exception as e:
            if load_fn is not None:
                Tracking.capture_exception(e)
                logger.error(f"_load_attach error: {e=}, {file_token=}")
                logger.error(traceback.format_exc())
            yield None
        else:
            yield ds

    def _load_pdf(self, file_token: str) -> DataSource:
        file_path = f"{settings.assets_url}/{file_token}"
        pdf_loader = MyOnlinePDFLoader(file_path=file_path)
        docs = pdf_loader.load()

        text_splitter = loader_utils.get_text_splitter()
        all_splits = text_splitter.split_documents(docs)

        # just use like markdown
        total_words = 0
        total_chars = 0
        total_tokens = 0
        type_id = file_path
        for doc in all_splits:
            words, chars = count_words_and_characters(doc.page_content)
            tokens = count_openai_tokens(doc.page_content)
            total_words += words
            total_chars += chars
            total_tokens += tokens

            doc.metadata = {
                "id": file_path,
                "type_id": type_id,
                "source": file_path,
                "type": "pdf",
                "datasheet_id": "",
                "record_id": "",
                "words": words,
                "characters": chars,
                "tokens": tokens,
            }

        return DataSource(
            type="pdf",
            type_id=type_id,
            documents=all_splits,
            words=total_words,
            characters=total_chars,
            tokens=total_tokens,
        )

    def _load_markdown(self, file_token: str) -> DataSource:
        file_path = f"{settings.assets_url}/{file_token}"
        markdown_loader = MyOnlineMarkdownLoader(file_path=file_path)
        docs = markdown_loader.load()

        text_splitter = loader_utils.get_text_splitter()
        all_splits = text_splitter.split_documents(docs)

        # just use like markdown
        total_words = 0
        total_chars = 0
        total_tokens = 0
        type_id = file_path
        for doc in all_splits:
            words, chars = count_words_and_characters(doc.page_content)
            tokens = count_openai_tokens(doc.page_content)
            total_words += words
            total_chars += chars
            total_tokens += tokens

            doc.metadata = {
                "id": file_path,
                "type_id": type_id,
                "source": file_path,
                "type": "markdown",
                "datasheet_id": "",
                "record_id": "",
                "words": words,
                "characters": chars,
                "tokens": tokens,
            }

        return DataSource(
            type="markdown",
            type_id=type_id,
            documents=all_splits,
            words=total_words,
            characters=total_chars,
            tokens=total_tokens,
        )

    def _load_docx(self, file_token: str) -> DataSource:
        file_path = f"{settings.assets_url}/{file_token}"
        docx_loader = MyOnlineDocxLoader(file_path=file_path)
        docs = docx_loader.load()

        text_splitter = loader_utils.get_text_splitter()
        all_splits = text_splitter.split_documents(docs)

        # just use like markdown
        total_words = 0
        total_chars = 0
        total_tokens = 0
        type_id = file_path
        for doc in all_splits:
            words, chars = count_words_and_characters(doc.page_content)
            tokens = count_openai_tokens(doc.page_content)
            total_words += words
            total_chars += chars
            total_tokens += tokens

            doc.metadata = {
                "id": file_path,
                "type_id": type_id,  # data_source
                "source": file_path,
                "type": "docx",
                "datasheet_id": "",
                "record_id": "",
                "words": words,
                "characters": chars,
                "tokens": tokens,
            }

        return DataSource(
            type="docx",
            type_id=type_id,
            documents=all_splits,
            words=total_words,
            characters=total_chars,
            tokens=total_tokens,
        )

    def _load_doc(self, file_token: str) -> DataSource:
        pass
