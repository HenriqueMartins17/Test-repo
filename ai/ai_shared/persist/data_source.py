from typing import List, Optional, Tuple, TypedDict

import jieba
import tiktoken
from langchain.docstore.document import Document
from pydantic import BaseModel, Field

from ai_shared.types import FieldMap


class DataSourceMeta(TypedDict):
    """
    The meta of DataSource
    Compare to Lang Chain Document's metadata
    """

    pass


class DataSource(BaseModel):
    """
    Generic Abstract Data Source
    Data Source is a wrapper for the training result, includes Lang Chain Document inside
    """

    type: str
    type_id: str
    words: Optional[int] = Field(default=0)
    characters: Optional[int] = Field(default=0)
    tokens: Optional[int] = Field(default=0)
    documents: Optional[List[Document]] = Field(exclude=True)  # ignore on .dict()


class DatasheetDataSource(DataSource):
    type: str = Field(default="datasheet", const=True)
    count: int
    fields: FieldMap
    revision: int


def count_openai_tokens(text) -> int:
    """
    Count the number of tokens in OpenAI GPT-3
    """
    encoding = tiktoken.encoding_for_model("gpt-3.5-turbo")
    tokens = encoding.encode(text)
    token_count = len(tokens)
    return token_count


def count_words_and_characters(text: str) -> Tuple[int, int]:
    """
    Count the number of words and characters in a text
    """
    words = jieba.cut(text)
    word_count = sum(1 for c in words if c.strip() != "")
    character_count = len(text)

    return word_count, character_count
