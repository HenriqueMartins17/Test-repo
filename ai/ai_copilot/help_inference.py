from typing import AsyncGenerator
from ai_copilot.assistants.help import HelpAssistant
from loguru import logger
from ai_shared.llmodel import LlModel
from datetime import datetime, timezone
from ai_copilot.assistants.help import HelpAssistant
from loguru import logger
from ai_shared.llmodel import LlModel
from datetime import datetime, timezone
from ai_shared.ros import PostCopilotChatBody
from ai_shared.vos import (
    OpenAIChatCompletionVO,
    OpenAIChoiceResponseVO,
    OpenAIChoiceStreamResponseVO,
    OpenAIMessageResponseVO
)
from langchain.callbacks import (
    AsyncIteratorCallbackHandler,
    StdOutCallbackHandler,
)
from langchain_community.callbacks import get_openai_callback
from ai_shared.helper import guard_stream_exception
from ai_inference.inference.base_inference import InferenceUsageVO
from ai_shared.config import settings


class HelpCopilotInference:

    def __init__(self, body: PostCopilotChatBody):
        self.request_body = body
        self.prologue = self.get_prologue()

    @staticmethod
    def get_prologue() -> str:
        prologue = "Ask me anything about AITable's help center documents and I'll gladly assist."
        if settings.edition.is_vika_saas():
            prologue = "欢迎尝试与我交流，我可以为您检索有关帮助中心文档的任何信息。"
        return prologue

    @staticmethod
    async def get_suggestions():
        if settings.edition.is_vika_saas():
            return [
                "维格云的付费版本会有哪些权益？",
                "双向关联怎么用？",
                "如何使用筛选器？",
                "我想用维格云做项目管理，有没有模板可以帮助我快速上手？",
                "维格云的函数和Excel的功能有什么区别？",
            ]
        elif settings.edition.is_aitable_saas():
            return [
                "What benefits will the paid version of aitable have?",
                "How to use bi-directional association?",
                "How to use Filter?",
                "I want to use aitable for project management. Are there any templates available to help me get started quickly?",
                "What are the differences between the functions in Aitable and the functions in Excel?",
                "Are there any tricks to creating a QA Agent?",
            ]
        else:
            return []

    @guard_stream_exception
    async def get_stream_chat_completions(self) -> AsyncGenerator:
        logger.debug(f"{self.request_body=}")

        conversation_id = str(self.request_body.conversation_id)
        query = self.request_body.messages[-1].content
        streaming = self.request_body.stream
        model = LlModel.get_default_model_by_edition()

        assistant = HelpAssistant(
            request_body=self.request_body,
            streaming=streaming,
            model=model,
            #allbacks=[iter_callback, stdout_callback],
        )

        await assistant.init_retriever()

        now = int(datetime.now(timezone.utc).timestamp())
        chat_completion_id = f"copilot_{now}"

        if streaming:
            outputs = await assistant.astream(query)
            async for s in outputs:
                token = s.content
                #logger.info(f"[SSE Token]{token}")
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
                #assistant.on_response(query, resp)

                return_str = resp.json(ensure_ascii=False, exclude={"usage"})
                yield return_str

            yield "[DONE]"

        else:
            # not streaming? return VO directly
            with get_openai_callback() as openai_callback:
                answer_list = []
                outputs = await assistant.astream(query)
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

                #assistant.on_response(query, resp)

                yield resp  # only one

        
