from typing import Any, Dict, Iterator, List, Optional, Union

import requests
from langchain.docstore.document import Document
from langchain_community.document_loaders import WebBaseLoader
from langchain_community.document_loaders.web_base import default_header_template
from langchain_community.document_transformers import Html2TextTransformer
from loguru import logger

from ai_shared.persist.data_source import (
    DataSource,
    count_openai_tokens,
    count_words_and_characters,
)
from ai_trainers.loaders import loader_utils


# https://github.com/langchain-ai/langchain/issues/9925
class MyWebBaseLoader(WebBaseLoader):
    """Load HTML pages using `urllib` and parse them with `BeautifulSoup'."""

    web_paths: List[str]

    requests_per_second: int = 2
    """Max number of concurrent requests to make."""

    default_parser: str = "html.parser"
    """Default parser to use for BeautifulSoup."""

    requests_kwargs: Dict[str, Any] = {}
    """kwargs for requests"""

    raise_for_status: bool = False
    """Raise an exception if http status code denotes an error."""

    bs_get_text_kwargs: Dict[str, Any] = {}
    """kwargs for beatifulsoup4 get_text"""

    def __init__(
        self,
        web_path: Union[str, List[str]],
        header_template: Optional[dict] = None,
        verify_ssl: Optional[bool] = True,
        proxies: Optional[dict] = None,
        continue_on_failure: Optional[bool] = False,
        autoset_encoding: Optional[bool] = True,
        encoding: Optional[str] = None,
    ):
        """Initialize with webpage path."""

        # TODO: Deprecate web_path in favor of web_paths, and remove this
        # left like this because there are a number of loaders that expect single
        # urls
        super().__init__(
            web_path, header_template, verify_ssl, proxies, continue_on_failure
        )
        if isinstance(web_path, str):
            self.web_paths = [web_path]
        elif isinstance(web_path, List):
            self.web_paths = web_path

        try:
            import bs4  # noqa:F401
        except ImportError:
            raise ImportError(
                "bs4 package not found, please install it with " "`pip install bs4`"
            )

        headers = header_template or default_header_template
        if not headers.get("User-Agent"):
            try:
                from fake_useragent import UserAgent

                headers["User-Agent"] = UserAgent().random
            except ImportError:
                logger.info(
                    "fake_useragent not found, using default user agent."
                    "To get a realistic header for requests, "
                    "`pip install fake_useragent`."
                )

        self.session = requests.Session()
        self.session.headers = dict(headers)
        self.session.verify = verify_ssl
        self.continue_on_failure = continue_on_failure
        self.autoset_encoding = autoset_encoding
        self.encoding = encoding
        if proxies:
            self.session.proxies.update(proxies)

    def _scrape(
            self,
            url: str,
            parser: Union[str, None] = None,
            bs_kwargs: Optional[dict] = None,
    ) -> Any:
        from bs4 import BeautifulSoup

        if parser is None:
            if url.endswith(".xml"):
                parser = "xml"
            else:
                parser = self.default_parser

        self._check_parser(parser)

        html_doc = self.session.get(url, **self.requests_kwargs)
        if self.raise_for_status:
            html_doc.raise_for_status()

        if self.encoding is not None:
            html_doc.encoding = self.encoding
        elif self.autoset_encoding:
            html_doc.encoding = html_doc.apparent_encoding
        return BeautifulSoup(html_doc.text, parser)


class URLLoader:
    def __init__(self, url: str):
        self.url = url

    def lazy_load(self) -> DataSource:
        loader = MyWebBaseLoader(self.url, encoding="utf-8")
        docs = loader.load()
        html2text = Html2TextTransformer()
        docs_html2text: List[Document] = list(html2text.transform_documents(docs))

        text_splitter = loader_utils.get_text_splitter()
        all_splits = text_splitter.split_documents(docs_html2text)

        # docs: List[Document] = []
        type_id = self.url
        for doc in all_splits:
            page_content = doc.page_content
            # words, characters, tokens count
            content_words_count, content_characters_count = count_words_and_characters(page_content)
            content_tokens_count = count_openai_tokens(page_content)

            doc.metadata = {
                "id": self.url,
                "type_id": type_id,
                "source": self.url,
                "type": "url",
                "datasheet_id": "",
                "record_id": "",
                "words": content_words_count,
                "characters": content_characters_count,
                "tokens": content_tokens_count,
            }

            # spilted_docs: List[Document] = text_splitter.create_documents(
            #     texts=[page_content],
            #     metadatas=[
            #         {
            #             "id": "|".join(self.urls),
            #             "source": "|".join(self.urls),
            #             "type": "url",
            #             "datasheet_id": "",
            #             "record_id": "",
            #             # "primary_key": "|".join(self.urls),
            #             "words": content_words_count,
            #             "characters": content_characters_count,
            #             "tokens": content_tokens_count,
            #         }
            #     ],
            # )
            # docs.extend(spilted_docs)

        # just use like markdown
        total_words = 0
        total_chars = 0
        total_tokens = 0
        for doc in all_splits:
            words, chars = count_words_and_characters(doc.page_content)
            tokens = count_openai_tokens(doc.page_content)
            total_words += words
            total_chars += chars
            total_tokens += tokens

        result = DataSource(
            type="url",
            type_id=type_id,
            documents=all_splits,
            words=total_words,
            characters=total_chars,
            tokens=total_tokens,
        )

        return result
