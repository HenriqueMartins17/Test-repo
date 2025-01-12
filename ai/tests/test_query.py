import time

import pytest

from ai_shared.persist.training_info import TrainingStatus
from ai_shared.ros import (
    OpenAIChatCompletionRO,
    OpenAIChatFunctionRO,
    OpenAIChatMessageRO,
    RequestSuggestionBody,
)
from ai_shared.vos import TrainingInfoVO


class TestOpenAIChatRequestVOs:
    @pytest.fixture
    def openai_chat_message(self):
        return OpenAIChatMessageRO(
            content="content", name="name", function_call={"key": "value"}
        )

    def test_training_info_vo(self):
        vo = TrainingInfoVO(
            ai_id="mock",
            info="test info",
            training_id="test",
            status="training",
            started_at=123,
        )
        assert vo.status == TrainingStatus.FAILED  # validator overwrite

        vo = TrainingInfoVO(
            ai_id="mock",
            info="test info",
            training_id="test",
            status="training",
            started_at=int(time.time()),
        )
        assert vo.status == TrainingStatus.TRAINING  # validator overwrite

    def test_openai_chat_message(self, openai_chat_message):
        # assert default role
        assert openai_chat_message.role == "user"
        assert openai_chat_message.content == "content"
        assert openai_chat_message.name == "name"
        assert openai_chat_message.function_call == {"key": "value"}

    @pytest.fixture
    def openai_chat_function(self):
        return OpenAIChatFunctionRO(
            name="function_name",
            description="function_description",
            parameter={"key": "value"},
        )

    def test_openai_chat_function(self, openai_chat_function):
        assert openai_chat_function.name == "function_name"
        assert openai_chat_function.description == "function_description"
        assert openai_chat_function.parameter == {"key": "value"}

    @pytest.fixture
    def openai_chat_completion_request(self):
        return OpenAIChatCompletionRO(
            model="gpt-3.5-turbo",
            messages=[OpenAIChatMessageRO(role="role", content="content")],
            functions=[
                OpenAIChatFunctionRO(
                    name="function_name", description="function_description"
                )
            ],
            function_call={"key": "value"},
            temperature=0.7,
            top_p=1,
            n=1,
            stream=False,
            stop=None,
            max_tokens=256,
            presence_penalty=0,
            frequency_penalty=0,
            logit_bias={},
        )

    def test_openai_chat_completion_request(
        self, openai_chat_completion_request: OpenAIChatCompletionRO
    ):
        assert openai_chat_completion_request.model == "gpt-3.5-turbo"
        assert len(openai_chat_completion_request.messages) == 1
        assert openai_chat_completion_request.functions[0].name == "function_name"
        assert openai_chat_completion_request.function_call == {"key": "value"}
        assert openai_chat_completion_request.temperature == 0.7
        assert openai_chat_completion_request.top_p == 1
        assert openai_chat_completion_request.n == 1
        assert openai_chat_completion_request.stream is False
        assert openai_chat_completion_request.stop is None
        assert openai_chat_completion_request.max_tokens == 256
        assert openai_chat_completion_request.presence_penalty == 0
        assert openai_chat_completion_request.frequency_penalty == 0
        assert openai_chat_completion_request.logit_bias == {}


class TestQuerySuggestionBody:
    def test_message_default_value(self):
        body = RequestSuggestionBody()
        assert body.question == ""

    def test_n_default_value(self):
        body = RequestSuggestionBody()
        assert body.n == 10

    def test_n_greater_than_zero(self):
        body = RequestSuggestionBody(n=5)
        assert body.n > 0

    # Add more test cases as needed

    def test_invalid_n_raises_exception(self):
        with pytest.raises(ValueError):
            RequestSuggestionBody(n=-1)
