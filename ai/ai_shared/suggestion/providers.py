import random
from abc import ABC
from typing import Optional

from langchain.schema import Document
from langchain_openai import ChatOpenAI
from langchain_community.llms.baidu_qianfan_endpoint import QianfanLLMEndpoint
from langchain_core.messages import HumanMessage
from loguru import logger

from ai_shared import prompt_utils


class SuggestionProviderABC(ABC):

    @staticmethod
    def gen_questions(docs: list[Document]) -> Optional[str]:
        raise NotImplementedError

    @staticmethod
    def gen_questions_vika(docs: list[Document]) -> Optional[str]:
        raise NotImplementedError


class SuggestionProvider(SuggestionProviderABC):

    @staticmethod
    def gen_questions(docs: list[Document]) -> Optional[str]:
        if not docs:
            return None

        text = prompt_utils.format_prompt_make_questions(docs)
        logger.debug(f"{text=}")
        if not text:
            return None

        # todo: get model by ai_id
        # llm = OpenAI(max_tokens=8192,model_name="gpt-3.5-turbo-16k-0613")
        llm = ChatOpenAI(temperature=0, model_name="gpt-3.5-turbo-16k-0613", max_tokens=8192, request_timeout=120)
        answer = llm([HumanMessage(content=text)]).content
        logger.debug(f"{answer=}")
        return answer

    @staticmethod
    def gen_questions_vika(docs: list[Document]) -> Optional[str]:
        if not docs:
            return None
        text = prompt_utils.format_prompt_make_questions(docs)
        logger.debug(f"{text=}")
        if not text:
            return None

        llm = QianfanLLMEndpoint()
        answer = llm(prompt=text)
        logger.debug(f"{answer=}")
        return answer


class MockSuggestionProvider(SuggestionProviderABC):

    @staticmethod
    def gen_questions(docs: list[Document]) -> Optional[str]:
        if not docs:
            return None

        mock_suggestions = [
            "1. What is mock suggestion?",
            "2. Why choose mock suggestion?",
            "3. How to mock suggestion?",
        ]
        answer = "|".join(mock_suggestions)
        logger.debug(f"{answer=}")
        return answer

    @staticmethod
    def gen_questions_vika(docs: list[Document]) -> Optional[str]:
        if not docs:
            return None

        mock_suggestions = [
            "1. 什么是建议？",
            "2. 为什么建议？",
            "3. 怎么生成建议？",
        ]
        answer = "|".join(mock_suggestions)
        logger.debug(f"{answer=}")
        return answer


