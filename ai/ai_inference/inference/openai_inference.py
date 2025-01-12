import asyncio
from datetime import datetime, timezone
from typing import Any, Coroutine, List, Optional, Tuple

from langchain.callbacks import (
    AsyncIteratorCallbackHandler,
    StdOutCallbackHandler,
)
from langchain_community.callbacks import get_openai_callback
from langchain.schema.output import LLMResult
from loguru import logger

from ai_inference.agents import AgentFactory, AIAgentType
from ai_inference.inference.base_inference import BaseInference, InferenceUsageVO
from ai_shared.databus import Ai, DataBus
from ai_shared.exceptions import AiNotFoundError, TrainingNotFoundError, ModelNameInvalidError
from ai_shared.helper import guard_stream_exception
from ai_shared.llmodel import LlModel, ModelKind
from ai_shared.persist.ai_info import AIInfo
from ai_shared.ros import OpenAIChatCompletionRO
from ai_shared.vector import Vector
from ai_shared.vos import (
    OpenAIChatCompletionVO,
    OpenAIChoiceResponseVO,
    OpenAIChoiceStreamResponseVO,
    OpenAIMessageResponseVO,
)


class CustomAsyncIteratorCallbackHandler(AsyncIteratorCallbackHandler):
    async def on_llm_end(
        self, response: LLMResult, **kwargs: Any
    ) -> None:
        await super().on_llm_end(response, **kwargs)
        logger.debug(f"""[on_llm_end LLMResult]: {response}""")
        self.llm_result = response


class OpenAIInference(BaseInference):
    """
    A inference handler that API / Interfaces compatible with OpenAI
    """

    def __init__(self, ai_id: str):
        super().__init__(ai_id)

    async def _get_suggestions_from_vector_db_with_question(self, n: int = 10, question: Optional[str] = None) -> List[str]:
        vector_db = await Vector.load_vector_db_by_ai_id(self.ai_id)
        docs_with_score = await vector_db.asimilarity_search_with_relevance_scores(
            query=question,
            k=n,
            filter={
                "suggestion": {
                    # not equal https://docs.trychroma.com/usage-guide#filtering-by-metadata
                    "$ne": question  # avoid the `question == suggestion`
                }
            },
        )

        if len(docs_with_score) < 1:
            return []
        suggestions = list(set([
                    doc.metadata.get("suggestion", "")
                    for doc, _ in docs_with_score
                    if doc.metadata.get("suggestion", "")
                ]))  # remove duplicates by set
        return suggestions[:n]

    async def _get_suggestions_from_vector_db_without_question(self, n: int = 10) -> List[str]:
        vector_db = await Vector.load_vector_db_by_ai_id(self.ai_id)
        # make vector_db._collection.get() method be async.
        loop = asyncio.get_event_loop()
        docs_with_score = await loop.run_in_executor(
            None, lambda n: vector_db._collection.get(limit=n, where={"suggestion": {"$ne": ""}}), n
        )

        if len(docs_with_score.get("metadatas", [])) < 1:
            return []

        ret_list = []
        for doc in docs_with_score.get("metadatas", []):
            if doc:
                suggestion = doc.get("suggestion", "")
                if suggestion:
                    ret_list.append(suggestion)
        suggestions = list(set(ret_list))  # remove duplicates by set
        return suggestions[:n]

    async def _get_suggestions_from_training_info(self, n: int = 10, question: Optional[str] = None) -> List[str]:
        ai_info = await AIInfo.load_ai_info(self.ai_id)
        if not ai_info:
            raise AiNotFoundError(f"ai_id: {self.ai_id}")
        if not ai_info.current_training_id:
            raise TrainingNotFoundError(
                f"ai_id: {self.ai_id}, training_id: {ai_info.current_training_id}"
            )
        training_info = await ai_info.get_training_info(ai_info.current_training_id)
        if not training_info:
            raise TrainingNotFoundError(
                f"ai_id: {self.ai_id}, training_id: {ai_info.current_training_id}"
            )
        suggestions = training_info.get_suggestions(n, question)
        return suggestions[:n]

    async def get_suggestions(
        self, n: int = 10, question: Optional[str] = None
    ) -> List[str]:
        """get_suggestions will load the suggestion data from vector db

        Returns:
            List[str]: a string list of suggestions.
        """
        if question:
            # suggestions = await self._get_suggestions_from_vector_db_with_question(n, question)
            suggestions = await self._get_suggestions_from_training_info(n, question)
        else:
            # suggestions = await self._get_suggestions_from_vector_db_without_question(n)
            suggestions = await self._get_suggestions_from_training_info(n)
        return suggestions

    async def get_conversation_info(self, training_id: str, conversation_id: str) -> List[Any]:
        """Get chat history from local file by conversation Idea

        Args:
            conversation_id (str): the conversation ID

        Raises:
            ConversationNotFoundError: if conversation ID not found

        Returns:
            List[Any]: return the chat history list
        """
        ai_info = await AIInfo.load_ai_info(self.ai_id)
        if not ai_info:
            raise AiNotFoundError(f"ai_id: {self.ai_id}")
        training_info = await ai_info.get_training_info(training_id)
        if not training_info:
            raise TrainingNotFoundError(
                f"ai_id: {self.ai_id}, training_id: {training_id}"
            )
        return await training_info.get_conversation(conversation_id)

    @guard_stream_exception
    async def get_stream_chat_completions(
        self, request_body: OpenAIChatCompletionRO, streaming=True
    ):
        # -> OpenAIChatCompletionVO | AsyncGenerator[str]:
        ai_setting = await DataBus.aget_ai_setting(self.ai_id)

        # use BaseAdvancedAISetting to overwrite default
        request_body.overwrite_chat_completion_request(ai_setting)  # All ai_setting inherited BaseAdvancedAISetting

        iter_callback = CustomAsyncIteratorCallbackHandler()
        stdout_callback = StdOutCallbackHandler()
        conversation_id = request_body.conversation_id

        # If you haven't set it, you can use the parameter provided by the request
        # If not set it and also no parameter provided, use default of edition
        model = ai_setting.model or request_body.model or LlModel.get_default_model_by_edition()

        available_model_names = LlModel.get_available_model_names_for_chat(model_kind=ModelKind.CHAT)
        if model not in available_model_names:
            raise ModelNameInvalidError(msg=f"model: {model}, available_model_names: {available_model_names}")

        # set model for get llm at last, now it always has a valid model name
        request_body.model = model

        agent = AgentFactory.new(
            self.ai_id,
            ai_setting,
            request_body,
            prompt_template=ai_setting.prompt,
            streaming=streaming,
            callbacks=[iter_callback, stdout_callback],
        )

        # Begin a task that runs in the background.
        query = request_body.messages[0].content

        now = int(datetime.now(timezone.utc).timestamp())
        chat_completion_id = f"aitable_{self.ai_id}_{now}"

        if streaming:
            outputs = await agent.astream(query)
            async for s in outputs:
                token = s.content
                logger.info(f"[SSE Token]{token}")
                # Use server-sent-events to stream the response
                resp = OpenAIChatCompletionVO(
                    id=chat_completion_id,
                    conversation_id=conversation_id,
                    object="chat.completion.chunk",
                    created=now,
                    model=model,
                    choices=[
                        OpenAIChoiceStreamResponseVO(
                            index=0, delta=OpenAIMessageResponseVO(content=token)
                        )
                    ],
                )
                agent.on_response(query, resp)

                return_str = resp.json(ensure_ascii=False, exclude={"usage"})
                yield return_str

            yield "[DONE]"

        else:
            # not streaming? return VO directly
            with get_openai_callback() as openai_callback:
                query = request_body.messages[0].content

                answer_list = []
                outputs = await agent.astream(query)
                async for s in outputs:
                    answer_list.append(s.content)

                answer = "".join(answer_list)
                logger.debug(f"""[Chat Result]: {answer}""")

                usage = InferenceUsageVO(
                    total_tokens=openai_callback.total_tokens,
                    completion_tokens=openai_callback.completion_tokens,
                    prompt_tokens=openai_callback.prompt_tokens,
                    total_cost=openai_callback.total_cost,
                    result=answer,
                )
                self.on_finished_inference(usage)

                now = int(datetime.now(timezone.utc).timestamp())
                message = OpenAIMessageResponseVO(content=answer)
                resp = OpenAIChatCompletionVO(
                    id=chat_completion_id,
                    conversation_id=conversation_id,
                    created=now,
                    model=model,
                    choices=[
                        OpenAIChoiceResponseVO(
                            index=0, message=message, finish_reason="stop"
                        )
                    ],
                    usage=usage,
                )

                agent.on_response(query, resp)

                yield resp  # only one
