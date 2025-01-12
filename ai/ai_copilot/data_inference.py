import time
import uuid
from datetime import datetime, timezone
import io
from typing import AsyncGenerator, AsyncIterator
import asyncio
import traceback
from typing import AsyncGenerator, Self
from uuid import UUID

from langchain.agents.openai_assistant import OpenAIAssistantRunnable
from loguru import logger

from ai_shared.copilot.conversation import ConversationService
from ai_shared.copilot.models import Assistants, Message, Conversation
from ai_shared.databus import DataBus
from ai_shared.exceptions import ParamsError
from ai_shared.helper import guard_stream_exception
from ai_shared.ros import PostCopilotChatBody
from ai_shared.tracking import Tracking
from ai_shared.vos import OpenAIMessageResponseVO, OpenAIChatCompletionVO, OpenAIChoiceResponseVO, \
    OpenAIChoiceStreamResponseVO, OpenAIChatCompletionStatus, OpenAIChatCompletionType
from ai_shared.config import settings, Edition


class DataCopilotInference:

    def __init__(self, body: PostCopilotChatBody):
        self.body = body

    @staticmethod
    def get_prologue() -> str:
        prologue = "Hello, I am your data assistant, specializing in data analysis tasks. Whether it's creating charts or handling complex statistical tasks, I can handle them effortlessly.(Defaults to using the view you are currently in as the data source for data analysis.)"
        if settings.edition.is_vika_saas():
            prologue = "你好，我是你的数据助手，专注于数据分析工作。无论是创建图表还是处理复杂的统计任务我都可以轻松应对.(默认使用您当前停留的视图用作数据分析的数据源)"
        return prologue

    @staticmethod
    async def get_suggestions():
        if settings.edition.is_vika_saas():
            return [
                "我们的数据里，有哪些信息是关联关系的呢？",
                "在我们的业务中，可能会有哪些隐患？",
                "能否简单描述一下我们的数据特点？",
                "我们的数据中有没有一些不正常或者缺失的部分？",
                "我们能确定数据中的哪些因素是互相影响的吗？",
                "我们能从不同角度来看待这些数据，找出一些没那么明显的联系和模式吗？",
                "你能根据过去的数据预测一下未来的市场趋势吗？",
            ]
        elif settings.edition.is_aitable_saas():
            return [
                "In our data, what information is related to each other?",
                "What potential risks might there be in our business?",
                "Can you briefly describe the characteristics of our data?",
                "Are there any abnormal or missing parts in our data?",
                "Can we determine which factors in the data are influencing each other?",
                "Can we look at these data from different angles and find some less obvious connections and patterns?",
                "Can you predict future market trends based on past data?",
            ]
        else:
            return []

    async def get_stream_chat_completions(self) -> AsyncGenerator:
        logger.debug(f"{self.body=}")
        try:
            if not self.body.conversation_id:
                raise ParamsError(f"conversation id is required")
            if not self.body.meta:
                raise ParamsError(f"meta is required")
            if not self.body.meta.datasheet_id:
                raise ParamsError(f"datasheet id is required")
            if not self.body.messages:
                raise ParamsError(f"messages is required")

            assistant = Assistants.get_assistant_by_type(self.body.assistant_type)
            conversation_svc = ConversationService()
            conversation = await conversation_svc.get_or_create_conversation_by_id(
                assistant,
                self.body.conversation_id,
                extra=self.body,
            )
            msg = self._message_starting(conversation=conversation)
            async for s in self._output_message(msg):
                yield s

            # datasheet upload to file, delete when chat end, upload file every new chat, we have no file management yet
            datasheet = await DataBus.aget_datasheet(self.body.meta.datasheet_id, self.body.meta.view_id)

            # check current revision and view, if updated, delete and upload again
            datasheet_revision = datasheet.get_revision()
            datasheet_view_id = self.body.meta.view_id or datasheet.get_default_view_id()
            current_file_ids = conversation.current_file_ids
            file_ids_exist = await conversation_svc.check_files_exist(current_file_ids)
            is_new_file_uploaded = False
            if any([
                # not file uploaded yet, or file not exist
                not current_file_ids,
                not file_ids_exist,
                # or view updated or revision updated, include not set yet
                conversation.current_datasheet_revision != datasheet_revision,
                conversation.current_datasheet_view_id != datasheet_view_id,
            ]):
                logger.info(f"{current_file_ids=}, {file_ids_exist=}")
                logger.info(f"{conversation.current_datasheet_revision=}, {datasheet_revision=}")
                logger.info(f"{conversation.current_datasheet_view_id=}, {datasheet_view_id=}")
                # delete old
                old_file_ids = conversation.current_file_ids
                if old_file_ids:
                    for file_id in old_file_ids:
                        if conversation.history_file_ids is None:
                            conversation.history_file_ids = []
                        conversation.history_file_ids.append(file_id)
                        # await conversation_svc.delete_file(file_id=file_id)

                # upload new
                df = await datasheet.to_df()
                logger.info(f"{df.head()=}")
                buffer = io.BytesIO()
                df.to_csv(buffer, index=False)
                file = await conversation_svc.upload_file(buffer)
                file_ids = [file.id]
                conversation.current_file_ids = file_ids
                is_new_file_uploaded = True

            # set current revision and view id
            conversation.current_datasheet_revision = datasheet_revision
            conversation.current_datasheet_view_id = datasheet_view_id
            await conversation.save()

            # compatible with openai chat
            content = self.body.messages[0].content

            msg = self._message_in_progress(conversation=conversation)
            async for s in self._output_message(msg):
                yield s

            thread_id = conversation.openai_thread_id

            if is_new_file_uploaded:
                if conversation.has_talked():
                    prompt_prefix = "Note: The data source has been refreshed, please use the latest data source."
                    if settings.edition.is_vika_saas():
                        prompt_prefix = "注意：数据源已刷新，请使用最新的数据源"
                    content = "\n".join([prompt_prefix, content])
                run = await conversation_svc.submit_and_run(
                    assistant=assistant,
                    thread_id=thread_id,
                    content=content,
                    file_ids=conversation.current_file_ids,
                )
            else:
                run = await conversation_svc.submit_and_run(
                    assistant=assistant,
                    thread_id=thread_id,
                    content=content,
                )
            if not run:
                msg = self._message_in_progress(conversation=conversation)
                async for s in self._output_message(msg):
                    yield s
                return
            conversation.current_run_id = run.id


            try:
                new_messages = conversation_svc.wait_for_messages(conversation=conversation, run=run, thread_id=thread_id)
                logger.debug(f"{new_messages=}")
            except Exception as e:
                Tracking.capture_exception(e)
                error = f"{self.__class__}, wait_for_messages: {e}"
                logger.error(error)
                logger.error(traceback.format_exc())
                await conversation_svc.cancel_run(run_id=run.id, thread_id=run.thread_id)
                conversation.info = error
                await conversation.save()
            else:
                async for s in self._output_messages(new_messages):
                    yield s
            finally:
                await conversation.save()
        except Exception as e:
            Tracking.capture_exception(e)
            error = f"{self.__class__}, get_stream_chat_completions: {e}"
            logger.error(error)
            logger.error(traceback.format_exc())
            async for s in self._output_error(e):
                yield s

    def _message_starting(self, conversation: Conversation) -> Message:
        return Message(
            type="system",
            chat_completion_type=OpenAIChatCompletionType.STATUS,
            chat_completion_status=OpenAIChatCompletionStatus.STARTING,
            content="ai_copilot_start_process_request",
            id=uuid.uuid4(),
            conversation_id=conversation.id,
            openai_message_id=None,
            openai_thread_id=conversation.openai_thread_id,
            openai_run_id=conversation.current_run_id,
            openai_assistant_id=conversation.openai_assistant_id,
            created_at=int(time.time()),
            file_ids=conversation.current_file_ids,
            metadata=None,
            extra=conversation.extra,
        )

    def _message_in_progress(self, conversation: Conversation) -> Message:
        return Message(
            type="system",
            chat_completion_type=OpenAIChatCompletionType.STATUS,
            chat_completion_status=OpenAIChatCompletionStatus.IN_PROGRESS,
            content="ai_copilot_processs",
            id=uuid.uuid4(),
            conversation_id=conversation.id,
            openai_message_id=None,
            openai_thread_id=conversation.openai_thread_id,
            openai_run_id=conversation.current_run_id,
            openai_assistant_id=conversation.openai_assistant_id,
            created_at=int(time.time()),
            file_ids=conversation.current_file_ids,
            metadata=None,
            extra=conversation.extra,
        )

    async def _output_error(self, error: Exception):
        now = int(datetime.now(timezone.utc).timestamp())
        chat_completion_id = f"aitable_copilot_{time.time_ns()}"
        conversation_id = str(self.body.conversation_id) if self.body.conversation_id else None
        message = OpenAIMessageResponseVO(content=str(error), role="system")
        if self.body.stream:
            resp = OpenAIChatCompletionVO(
                id=chat_completion_id,
                type=OpenAIChatCompletionType.STATUS,
                status=OpenAIChatCompletionStatus.ERROR,
                conversation_id=str(conversation_id),
                object="chat.completion",
                created=now,
                model="",
                choices=[
                    OpenAIChoiceStreamResponseVO(
                        index=0, delta=OpenAIMessageResponseVO(content=str(error))
                    )
                ],
            )

            return_str = resp.json(ensure_ascii=False, exclude={"usage"})
            yield return_str
            yield "[DONE]"
        else:
            resp = OpenAIChatCompletionVO(
                id=chat_completion_id,
                type=OpenAIChatCompletionType.STATUS,
                status=OpenAIChatCompletionStatus.ERROR,
                conversation_id=conversation_id,
                created=now,
                model="",
                choices=[
                    OpenAIChoiceResponseVO(
                        index=0, message=message, finish_reason="stop"
                    )
                ],
                usage=None,
            )
            yield resp

    async def _output_messages(self, messages: AsyncIterator[Message]) -> AsyncIterator:
        if self.body.stream:
            async for message in messages:
                async for s in self._fmt_stream(message):
                    yield s
            yield "[DONE]"
        else:
            async for message in messages:
                async for s in self._fmt_once(message):
                    yield s

    async def _output_message(self, message: Message) -> AsyncIterator:
        if self.body.stream:
            async for s in self._fmt_stream(message):
                yield s
        else:
            async for s in self._fmt_once(message):
                yield s

    async def _fmt_once(self, message: Message) -> AsyncIterator[OpenAIChatCompletionVO]:
        conversation_id = str(self.body.conversation_id) if self.body.conversation_id else None
        now = int(datetime.now(timezone.utc).timestamp())
        logger.info(f"[SSE Token]{message.content}")
        chat_completion_id = f"aitable_copilot_{str(uuid.uuid4())}"  # new id, will show like a new message
        s = OpenAIMessageResponseVO(content=message.content)
        resp = OpenAIChatCompletionVO(
            id=chat_completion_id,
            type=message.chat_completion_type,
            status=message.chat_completion_status,
            conversation_id=conversation_id,
            created=now,
            model="",
            choices=[
                OpenAIChoiceResponseVO(
                    index=0, message=s, finish_reason="stop"
                )
            ],
            usage=None,
        )
        yield resp

    async def _fmt_stream(self, message: Message) -> AsyncIterator[str]:
        conversation_id = str(self.body.conversation_id) if self.body.conversation_id else None
        now = int(datetime.now(timezone.utc).timestamp())
        chat_completion_id = f"aitable_copilot_{str(uuid.uuid4())}"  # new id, will show like a new message
        if message.chat_completion_type == OpenAIChatCompletionType.CHAT:
            for token in message.content:
                logger.info(f"[SSE Token]{token}")
                resp = OpenAIChatCompletionVO(
                    id=chat_completion_id,
                    type=message.chat_completion_type,
                    status=message.chat_completion_status,
                    conversation_id=conversation_id,
                    object="chat.completion.chunk",
                    created=now,
                    model="",
                    choices=[
                        OpenAIChoiceStreamResponseVO(
                            index=0, delta=OpenAIMessageResponseVO(content=token)
                        )
                    ],
                )

                return_str = resp.json(ensure_ascii=False, exclude={"usage"})
                yield return_str
        else:
            logger.info(f"[SSE Token]{message.content}")
            resp = OpenAIChatCompletionVO(
                id=chat_completion_id,
                type=message.chat_completion_type,
                status=message.chat_completion_status,
                conversation_id=conversation_id,
                object="chat.completion",
                created=now,
                model="",
                choices=[
                    OpenAIChoiceStreamResponseVO(
                        index=0, delta=OpenAIMessageResponseVO(content=message.content)
                    )
                ],
            )

            return_str = resp.json(ensure_ascii=False, exclude={"usage"})
            yield return_str

