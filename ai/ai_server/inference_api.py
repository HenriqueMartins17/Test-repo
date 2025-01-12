import asyncio
from typing import Optional, Annotated
from ai_shared.databus import DataBus
from ai_shared.ai_setting import AIAgentType, AISettingFactory

from fastapi import Body, Query
from fastapi.routing import APIRouter
from loguru import logger
from sse_starlette import EventSourceResponse

from ai_inference import Inference, utils
from ai_shared.actions.wizard import WizardAction
from ai_shared.exceptions import (
    AiNotFoundError,
    NoSuggestionError,
    NotTrainedError,
    NotTrainedErrorAction,
    TrainingNotFoundError, NoAiSettingJsonSchemaError, AiSettingNotFoundError,
)
from ai_shared.llmodel import LlModel
from ai_shared.persist.ai_info import AIInfo
from ai_shared.persist.training_info import TrainingInfo
from ai_shared.ros import OpenAIChatCompletionRO, RequestSuggestionBody
from ai_shared.tracking import Tracking
from ai_shared.vos import (
    AIInfoVO,
    APIResponseVO,
    OpenAIChatCompletionVO,
    TrainingInfoVO,
)

router = APIRouter()


@router.post("/{ai_id}/conversations/{conversation_id}/chat/completions", deprecated=True)
async def chat_completion_with_conversation_id(
    ai_id: str, conversation_id: str, body: OpenAIChatCompletionRO
):
    """
    @deprecated use `/ai/inference/{ai_id}/chat/completions` instead
    """
    body.conversation_id = conversation_id
    return await chat_completion(ai_id, body)


@router.post("/{ai_id}/chat/completions")
async def chat_completion(ai_id: str, body: OpenAIChatCompletionRO):
    """\
    This api is compatible with OpenAI chat completion.
    the request body can refer to [OpenAI Reference](https://platform.openai.com/docs/api-reference/chat/create). 

    There are two mode of response provided by this api, which determined by `stream` parameter: 

    - In normal mode, the response will return the JSON response body. 
    - In streaming mode, the response will return the event stream.

    """
    if AIInfo.exist(ai_id) is False:
        raise AiNotFoundError(f"ai_id: {ai_id}")

    inference = Inference.new(ai_id)

    try:
        if body.stream:
            # note: the generator may carry some exception by `guard_exception`
            generator = inference.get_stream_chat_completions(body, True)
            return EventSourceResponse(generator, media_type="text/event-stream")
        else:
            generator = inference.get_stream_chat_completions(body, False)
            async for resp in generator:
                assert isinstance(resp, OpenAIChatCompletionVO)
                return resp  # only first one

    except NotTrainedError as e:
        data = {"actions": [WizardAction()]}
        raise NotTrainedErrorAction(
            msg=f"AI {ai_id} is not trained yet, there isn't any suggestion: {e}",
            data=data,
        )


@router.post("/{ai_id}/suggestions", response_model=APIResponseVO)
async def get_suggestions(
    ai_id: str, query: Optional[RequestSuggestionBody] = Body(default=None)
):
    """\
    This api will get suggestions from the vector db after training.

    The default query body can be empty, and then will use internal parameter to get suggestions.

    If there is a `question` parameter, it will get the similar content from vector database.
    """
    if AIInfo.exist(ai_id) is False:
        raise AiNotFoundError(f"ai_id: {ai_id}")

    inference = Inference.new(ai_id)
    try:
        if not query:
            suggestions = await inference.get_suggestions()  # default
        else:
            suggestions = await inference.get_suggestions(query.n, query.question)
    except ValueError:
        raise NoSuggestionError(
            f"AI {ai_id} is not trained yet, there isn't any suggestion"
        )

    if len(suggestions) < 1:
        raise NoSuggestionError("there isn't any suggestion")

    return APIResponseVO.success(msg="ok", data=suggestions)


@router.get(
    "/{ai_id}/trainings/{training_id}/conversations",
    response_model=APIResponseVO,
)
async def get_conversations_list(ai_id: str, training_id: str):
    """
    Get all conversation history of the training
    """
    if AIInfo.exist(ai_id) is False:
        raise AiNotFoundError(f"ai_id: {ai_id}")
    ai_info = await AIInfo.load_ai_info(ai_id)
    if not ai_info:
        raise AiNotFoundError(f"ai_id: {ai_id}")

    if TrainingInfo.exist(ai_id, training_id) is False:
        raise TrainingNotFoundError(f"ai_id: {ai_id}, training_id: {training_id}")
    training_info = await ai_info.get_training_info(training_id)
    if not training_info:
        raise TrainingNotFoundError(f"ai_id: {ai_id}, training_id: {training_id}")
    conversations = await training_info.get_conversations_list()

    return APIResponseVO.success(
        msg=f"Get all conversation history of the training `{training_id}` of AI `{ai_id}`",
        data=conversations,
    )


@router.get(
    "/{ai_id}/conversations/{conversation_id}",
    response_model=APIResponseVO,
)
async def get_current_training_conversation(ai_id: str, conversation_id: str):
    """
    Get the current conversation history of the training
    """
    if AIInfo.exist(ai_id) is False:
        raise AiNotFoundError(f"ai_id: {ai_id}")
    ai_info = await AIInfo.load_ai_info(ai_id)
    if not ai_info:
        raise AiNotFoundError(f"ai_id: {ai_id}")
    return await get_chat_history(ai_id, ai_info.current_training_id, conversation_id)


@router.get(
    "/{ai_id}/trainings/{training_id}/conversations/{conversation_id}",
    response_model=APIResponseVO,
)
async def get_chat_history(ai_id: str, training_id: str, conversation_id: str):
    """\
    This api will return the all history of the conversation based on `ai_id` and `conversation_id`.

    The history of conversation will display as a pair message including Human and AI.
    """
    inference = Inference.new(ai_id)

    history = await inference.get_conversation_info(training_id, conversation_id)
    # TODO: return the whole "Conversation", DO NOT USE any

    if not history:
        message = f"there isn't any history for conversation ID: {conversation_id}"
    else:
        message = f"get {len(history) // 2} history records"

    return APIResponseVO.success(msg=message, data=history)


@router.get("/{ai_id}/setting")
async def get_ai_setting_schema(ai_id: str, type: str | None = None):
    """
    Return AI Setting JSON Schema and Form Data
    For frontend `react-jsonschema-form` framework usage

    https://rjsf-team.github.io/react-jsonschema-form/
    """

    if type is not None:
        try:
            ai_type = AIAgentType(str(type).lower())
        except KeyError:
            raise NoAiSettingJsonSchemaError(f"ai agent({ai_id})'s type: \"{type}\" is not supported")

        ai_setting = AISettingFactory.new(ai_type, {})

        schema = await ai_setting.get_schema()

        json_schema = schema["JSONSchema"]

        ai_setting.model = ai_setting.model or LlModel.get_default_model_by_edition()
    else:
        ai_setting = await DataBus.aget_ai_setting(ai_id)
        if not ai_setting:
          raise AiSettingNotFoundError(f"ai_id: {ai_id}")
  
        schema = await ai_setting.get_schema()
        
        if not schema:
            raise NoAiSettingJsonSchemaError(f"ai_id: {ai_id}")

        json_schema = schema["JSONSchema"]

    form_data = ai_setting.dict(by_alias=True)

    response = {
        "JSONSchema": json_schema,
        "UISchema": schema["UISchema"],
        "data": form_data
    }
    return APIResponseVO.success(f"AI_ID: {ai_id} Setting data and JSON Schema", response)
    
    
@router.get("/{ai_id}")
async def get_ai_info(ai_id: str):
    """\
    this api will return the information about AI model after training. 
    It includes current training id, locking training id.
    """
    if AIInfo.exist(ai_id) is False:
        raise AiNotFoundError(f"ai_id: {ai_id}")
    ai_info = await AIInfo.load_ai_info(ai_id)
    if not ai_info:
        raise AiNotFoundError(f"ai_id: {ai_id}")

    ai_info_vo = AIInfoVO(**ai_info.dict())

    if ai_info.current_training_id:
        ai_info_vo.current_training_info = TrainingInfoVO(
            ai_id=ai_info.ai_id,
            training_id=ai_info.current_training_id
        )
        current_training_info = await ai_info.get_training_info(ai_info.current_training_id)
        if current_training_info:
            ai_info_vo.current_training_info = TrainingInfoVO(
                **current_training_info.dict()
            )

    if ai_info.locking_training_id:
        ai_info_vo.locking_training_info = TrainingInfoVO(
            ai_id=ai_info.ai_id,
            training_id=ai_info.locking_training_id
        )
        locking_training_info = await ai_info.get_training_info(ai_info.locking_training_id)
        if locking_training_info:
            ai_info_vo.locking_training_info = TrainingInfoVO(
                **locking_training_info.dict()
            )

    return APIResponseVO.success(f"AI_ID: {ai_id} info", ai_info_vo.dict())


@router.get("/{ai_id}/trainings")
async def get_ai_trainings_infos(ai_id: str, ai_node_id: Annotated[Optional[int], Query(alias="aiNodeId")] = None):
    """
    Get all training info of the AI
    """
    if not AIInfo.exist(ai_id):
        raise AiNotFoundError(f"ai_id: {ai_id}")
    ai_info = await AIInfo.load_ai_info(ai_id)
    if not ai_info:
        raise AiNotFoundError(f"ai_id: {ai_id}")

    if ai_node_id and ai_info.ai_node_training_ids:
        training_ids = ai_info.get_ai_node_training_ids(ai_node_id=ai_node_id)
    else:
        training_ids = ai_info.get_trainings_folders()
    trainings_info_dict_list = []
    for training_id in training_ids:
        training_info = await ai_info.get_training_info(training_id)
        if not training_info:
            continue

        training_info_dict = training_info.dict()
        try:
            training_info_vo = TrainingInfoVO(**training_info_dict)
        except Exception as e:
            Tracking.capture_exception(e)
            logger.error(
                f"error on parse training_info_dict: {training_info_dict}, error: {e}"
            )
        finally:
            training_info_vo = training_info  # fallback to

        trainings_info_dict_list.append(training_info_vo)

    # sort by started_at time
    trainings_info_dict_list.sort(
        key=lambda x: x.started_at if x.started_at is not None else float("-inf"),
        reverse=True,
    )

    return APIResponseVO.success(
        msg=f"AI_ID: {ai_id} info", data=trainings_info_dict_list
    )
