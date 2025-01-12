import traceback
from typing import Optional
from uuid import UUID
from fastapi import Body, Query
from fastapi.requests import Request
from fastapi.routing import APIRouter
from loguru import logger
from sse_starlette import EventSourceResponse, ServerSentEvent

from ai_copilot.inference_factory import CopilotInferenceFactory, DataCopilotInference, HelpCopilotInference
from ai_shared.copilot.conversation import ConversationService
from ai_shared.exceptions import NoAssistantError, ConversationNotFoundError
from ai_shared.ros import RequestSuggestionBody, PostCopilotChatBody, PostCopilotSuggestionsBody
from ai_shared.tracking import Tracking
from ai_shared.vos import APIResponseVO, OpenAIChatCompletionVO
from ai_shared.copilot.models import AssistantType

router = APIRouter()

@router.options("/chat/completions")
async def options_chat_completions(ai_id: str):
    """
    handle OPTIONS request for /ai/inference/{ai_id}/chat/completions
    """

    response_headers = {
        "Access-Control-Allow-Methods": "POST, OPTIONS",
        "Access-Control-Allow-Origin": "*",
    }
    return {"message": "This is an OPTIONS request for chat completions."}


@router.post("/chat/completions")
async def copilot_inference(body: Optional[PostCopilotChatBody] = None):
    inference = CopilotInferenceFactory.new(body)
    generator = inference.get_stream_chat_completions()

    if body.stream:
        return EventSourceResponse(generator, media_type="text/event-stream")
    else:
        async for resp in generator:
            assert isinstance(resp, OpenAIChatCompletionVO)
            return resp  # only first one


@router.get("/conversations/{conversation_id}")
async def copilot_conversation_get(conversation_id: UUID) -> APIResponseVO:
    conversation_svc = ConversationService()
    conversation = await conversation_svc.get_conversation_by_id(conversation_id)
    if not conversation:
        raise ConversationNotFoundError(f"{conversation_id}")
    # For message type compatibility
    from langchain.schema.messages import message_to_dict
    # if frontend disconnected when yield messages, will be "" content saved, lost openai messages
    history = [message_to_dict(message) for message in conversation.messages if message.content]
    return APIResponseVO.success(msg='ok', data=history)


@router.post("/conversations/{conversation_id}/runs/cancel")
async def copilot_conversation_cancel(conversation_id: UUID) -> APIResponseVO:
    conversation_svc = ConversationService()
    conversation = await conversation_svc.get_conversation_by_id(conversation_id)
    if not conversation:
        raise ConversationNotFoundError(f"{conversation_id}")
    if not conversation.current_run_id:
        return APIResponseVO.success(msg='ok')
    if not conversation.openai_thread_id:
        return APIResponseVO.success(msg='ok')
    res = await conversation_svc.cancel_run(conversation.current_run_id, conversation.openai_thread_id)
    return APIResponseVO.success(msg='ok')

@router.post("/suggestions", response_model=APIResponseVO)
async def get_suggestions(
    body: Optional[PostCopilotSuggestionsBody] = Body(default=None)
):
    """
    This api will get suggestions base on assistant type.

    The default query body can be empty, and then will use internal parameter to get suggestions.

    If there is a `question` parameter, it will get the similar content from vector database.
    """
    suggestions = []
    if body.assistant_type == AssistantType.HELP:
        suggestions = await HelpCopilotInference.get_suggestions()
    if body.assistant_type == AssistantType.DATA:
        suggestions = await DataCopilotInference.get_suggestions()
    return APIResponseVO.success(msg="ok", data=suggestions)

@router.get("/setting")
async def get_assistant_setting_schema(type: str | None = "help"):
    """
    This api will get the schema of assistant settings.
    """

    if type == AssistantType.DATA.value:
        prologue = DataCopilotInference.get_prologue()
    elif type == AssistantType.HELP.value:
        prologue = HelpCopilotInference.get_prologue()
    else:
        raise NoAssistantError(f"{type=}")

    response = {
        "JSONSchema": {},
        "UISchema": {},
        "data": {
            "prologue": prologue
        }
    }
    return APIResponseVO.success(f"the schema of {type} assistant", response)