import asyncio
import re
from typing import Union, Optional

from langchain.schema import Document
from loguru import logger

from ai_shared import prompt_utils
from ai_shared.config import settings
from ai_shared.mock_utils import is_mock
from ai_shared.suggestion.providers import SuggestionProvider, MockSuggestionProvider


class Suggestion:

    @staticmethod
    def _get_provider(mock: bool = False) -> Union[SuggestionProvider, MockSuggestionProvider]:
        if settings.is_mock() or mock:
            return MockSuggestionProvider()
        else:
            return SuggestionProvider()

    @staticmethod
    def make_suggestions(ai_id: str, docs: list[Document]) -> list[str]:
        logger.debug(f"ai_id： {ai_id=}: {docs[:3]=}")
        mock = is_mock(ai_id)
        provider = Suggestion._get_provider(mock=mock)
        if settings.edition.is_vika_saas():
            answer = provider.gen_questions_vika(docs)
        else:
            answer = provider.gen_questions(docs)
        suggestions = Suggestion._process_questions(answer)
        logger.debug(f"Suggestions: {suggestions}")
        return suggestions

    @staticmethod
    async def amake_suggestions(ai_id: str, docs: list[Document]) -> list[str]:
        loop = asyncio.get_event_loop()
        suggestions = await loop.run_in_executor(
            None, Suggestion.make_suggestions, ai_id, docs
        )
        return suggestions

    @staticmethod
    def _process_questions(answer: Optional[str]) -> list[str]:
        if not answer:
            return []

        pattern = r'\d+\.\s(.*?)(?=\d+\.|\n|$)'
        matches = re.findall(pattern, answer, re.DOTALL)
        answer = " | ".join(matches)

        questions = prompt_utils.answer_to_questions(answer)

        logger.debug(questions)

        return questions
