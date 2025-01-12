from ai_shared.llmodel import ModelNameOpenAI
from ai_shared.vos import (
    OpenAIChatCompletionVO,
    OpenAIChoiceResponseVO,
    OpenAIChoiceStreamResponseVO,
    OpenAIMessageResponseVO,
)


class TestOpenAIResponseVO:
    def test_OpenAIMessageResponse(self):
        response = OpenAIMessageResponseVO(content="Hello")
        # assert default role
        assert response.role == "assistant"
        assert response.content == "Hello"

    def test_OpenAIChoiceResponse(self):
        message = OpenAIMessageResponseVO(content="Hello")
        response = OpenAIChoiceResponseVO(index=0, message=message)
        assert response.index == 0
        assert response.message.role == "assistant"
        assert response.message.content == "Hello"
        assert response.finish_reason is None

        response = OpenAIChoiceResponseVO(index=0, message=message, finish_reason="stop")
        assert response.finish_reason == "stop"

    def test_OpenAIChoiceStreamResponse(self):
        delta = OpenAIMessageResponseVO(content="Hello")
        response = OpenAIChoiceStreamResponseVO(index=0, delta=delta)
        assert response.index == 0
        assert response.delta.role == "assistant"
        assert response.delta.content == "Hello"
        assert response.finish_reason is None

        response = OpenAIChoiceStreamResponseVO(
            index=0, delta=delta, finish_reason="stop"
        )
        assert response.finish_reason == "stop"

    def test_OpenAIChatCompletionResponse(self):
        response = OpenAIChatCompletionVO(
            id="test",
            conversation_id="test",
            created=0,
            model=ModelNameOpenAI.GPT_3_5_TURBO.value,
            choices=[
                OpenAIChoiceResponseVO(
                    index=0,
                    message=OpenAIMessageResponseVO(content="Hello"),
                    finish_reason="stop",
                )
            ],
        )
        assert response.conversation_id == "test"
        assert response.created == 0
        assert response.model == "gpt-3.5-turbo"
        assert len(response.choices) == 1
        assert response.choices[0].message.content == "Hello"
        assert response.choices[0].finish_reason == "stop"

        response = OpenAIChatCompletionVO(
            id="test",
            conversation_id="test",
            created=0,
            model=ModelNameOpenAI.GPT_3_5_TURBO.value,
            choices=[
                OpenAIChoiceStreamResponseVO(
                    index=0,
                    delta=OpenAIMessageResponseVO(content="Hello"),
                    finish_reason="stop",
                )
            ],
        )
        assert response.conversation_id == "test"
        assert response.created == 0
        assert response.model == "gpt-3.5-turbo"
        assert len(response.choices) == 1
        assert response.choices[0].delta.content == "Hello"
        assert response.choices[0].finish_reason == "stop"
