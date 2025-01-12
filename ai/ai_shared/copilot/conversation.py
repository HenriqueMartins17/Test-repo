import time
import traceback
import uuid
from typing import Optional, AsyncIterable, AsyncIterator, Any
from uuid import UUID

from loguru import logger
from openai import AsyncOpenAI
from openai.types.beta.assistant import Assistant as OpenAiAssistant
from openai.types.beta.thread import Thread as OpenAiThread
from openai.types.beta.threads import ThreadMessage as OpenAiMessage
from openai.types import FileObject as OpenAiFileObject
from openai.types.beta.threads.thread_message import MessageContentImageFile, MessageContentText
from openai.types.beta.threads.run import Run as OpenAiRun
from ai_shared.copilot.models import Conversation, Message

import asyncio

from ai_shared.tracking import Tracking
from ai_shared.vos import OpenAIChatCompletionType, OpenAIChatCompletionStatus


class ConversationService:

    def __init__(self):
        self.client = AsyncOpenAI()
        self.ai_id = "copilot"

    async def get_or_create_conversation_by_id(
            self,
            assistant: OpenAiAssistant,
            conversation_id: UUID,
            extra: Optional[Any] = None,
    ) -> Conversation:
        if Conversation.exist(conversation_id=conversation_id):
            conversation = await Conversation.read_conversation(conversation_id=conversation_id)
            if conversation:
                conversation.extra = extra
                return conversation

        thread = await self.client.beta.threads.create()
        conversation = await self._create_new_conversation(conversation_id, thread, assistant, extra)
        return conversation

    async def get_conversation_by_id(self, conversation_id: UUID) -> Optional[Conversation]:
        if not Conversation.exist(conversation_id=conversation_id):
            return None
        conversation = await Conversation.read_conversation(conversation_id=conversation_id)
        if not conversation:
            return None
        return conversation

    async def _create_new_conversation(
            self,
            conversation_id: UUID,
            thread: OpenAiThread,
            assistant: OpenAiAssistant,
            extra: Optional[Any] = None,
    ) -> Conversation:
        logger.info(f"Creating conversation {conversation_id=}")
        conversation = Conversation(
            id=conversation_id,
            messages=[],
            openai_thread_id=thread.id,
            openai_assistant_id=assistant.id,
            created_at=thread.created_at,
            metadata=thread.metadata,
            extra=extra,
        )
        await conversation.save()
        return conversation

    async def check_files_exist(self, file_ids: list[str]) -> bool:
        if not file_ids:
            return False
        for file_id in file_ids:
            if not await self._check_file_exist(file_id=file_id):
                return False
        return True

    async def _check_file_exist(self, file_id: str) -> bool:
        try:
            file_obj = await self.client.files.retrieve(file_id=file_id)
        except Exception as e:
            Tracking.capture_exception(e)
            error = f"{self.__class__}, check_files_exist: {e}"
            logger.error(error)
            logger.error(traceback.format_exc())
            return False
        else:
            if not file_obj:
                return False
            return True

    async def upload_file(self, file) -> OpenAiFileObject:
        file_obj = await self.client.files.create(file=file, purpose="assistants")
        logger.info(f"Uploaded file: {file_obj.id}")
        return file_obj

    async def delete_file(self, file_id: str) -> None:
        try:
            await self.client.files.delete(file_id=file_id)
            logger.info(f"Deleted file: {file_id}")
        except Exception as e:
            Tracking.capture_exception(e)
            error = f"{self.__class__}, delete_file: {e}"
            logger.error(error)
            logger.error(traceback.format_exc())

    async def submit_and_run(
            self,
            assistant: OpenAiAssistant,
            thread_id: str,
            content: str,
            file_ids: Optional[list[str]] = None,
    ) -> Optional[OpenAiRun]:
        try:
            if file_ids:
                message = await self.client.beta.threads.messages.create(
                    thread_id=thread_id,
                    role="user",
                    content=content,
                    file_ids=file_ids,
                )
            else:
                message = await self.client.beta.threads.messages.create(
                    thread_id=thread_id,
                    role="user",
                    content=content,
                )
            logger.info(f"{message=}")
            run = await self.client.beta.threads.runs.create(thread_id=thread_id, assistant_id=assistant.id)
            logger.info(f"submit_and_run: {thread_id=} {run=}")
        except Exception as e:
            Tracking.capture_exception(e)
            error = f"{self.__class__}, submit_and_run: {e}"
            logger.error(error)
            logger.error(traceback.format_exc())
        else:
            return run

    async def cancel_run(self, run_id: str, thread_id: str) -> Optional[OpenAiRun]:
        logger.info(f"Cancelling run: {run_id=} {thread_id=}")
        try:
            res = await self.client.beta.threads.runs.cancel(run_id=run_id, thread_id=thread_id)
        except Exception as e:
            Tracking.capture_exception(e)
            error = f"{self.__class__}, cancel_run: {e}"
            logger.error(error)
            logger.error(traceback.format_exc())
        else:
            return res

    async def wait_for_messages(
            self,
            conversation: Conversation,
            run: OpenAiRun,
            thread_id: str,
            max_wait: int = 300,
    ) -> AsyncIterator[Message]:
        yield self._message_generating(conversation=conversation)

        openai_message_ids = set([msg.openai_message_id for msg in conversation.messages])
        waited = 0
        while True:
            run = await self.client.beta.threads.runs.retrieve(
                thread_id=thread_id,
                run_id=run.id,
            )
            # "queued"
            # "in_progress", "cancelling", "requires_action"
            # "failed", "expired", "cancelled"
            # "completed"

            logger.debug(f"wait_for_messages: {run.status=}")

            steps = await self.client.beta.threads.runs.steps.list(run_id=run.id, thread_id=thread_id, order="desc")
            if steps.data:
                step = steps.data[0]
                logger.debug(f"{step=}")
                if step.type == "message_creation":
                    messages = await self.client.beta.threads.messages.list(thread_id=thread_id, order="desc")
                    logger.debug(f"{messages=}")
                    # new messages by desc, and reverse it for yield message
                    messages.data.reverse()
                    for message in messages.data:
                        if message.id in openai_message_ids:
                            continue
                        if not message.content:
                            continue
                        new_message = self._convert_to_message(conversation=conversation, message=message)
                        if not new_message:
                            continue

                        if message.run_id == run.id and message.role == "assistant":
                            yield new_message

                        # message may get by status in-process and complete, but just yield once
                        openai_message_ids.add(message.id)
                        conversation.messages.append(new_message)
                        await conversation.save()

            if run.status == "completed":
                logger.debug(f"run completed: {waited=}")
                break

            if run.status == "expired":
                logger.debug(f"run expired: {waited=}")
                yield self._message_expired(conversation=conversation)
                break

            if run.status == "failed":
                logger.debug(f"run failed: {waited=}")
                conversation.info = run.last_error
                yield self._message_failed(conversation=conversation)
                break

            if run.status == "cancelled":
                logger.debug(f"run cancelled: {waited=}")
                yield self._message_cancelled(conversation=conversation)
                break

            if waited > max_wait:
                logger.debug(f"wait_for_messages timeout: {waited=}")
                conversation.info = f"wait_for_messages timeout: {waited=}"
                yield self._message_timeout(conversation=conversation)
                break

            seconds = 1
            await asyncio.sleep(seconds)
            waited += seconds

    def _convert_to_message(self, conversation: Conversation, message: OpenAiMessage) -> Optional[Message]:
        logger.debug(f"_convert_to_message: {message=}")
        lines = []
        for item in message.content:
            if isinstance(item, MessageContentImageFile):
                s = f"""![{item.image_file.file_id}](/api/v1/ai/{self.ai_id}/files/{item.image_file.file_id}/content)"""
                lines.append(s)
            elif isinstance(item, MessageContentText):
                s = f"""{item.text.value}"""
                lines.append(s)
            else:
                continue

        if not lines:
            return None

        msg = Message(
            type=message.role,
            chat_completion_type=OpenAIChatCompletionType.CHAT,
            chat_completion_status=None,
            content="\n\n".join(lines),
            id=uuid.uuid4(),
            conversation_id=conversation.id,
            openai_message_id=message.id,
            openai_thread_id=message.thread_id,
            openai_run_id=message.run_id,
            openai_assistant_id=message.assistant_id,
            created_at=message.created_at,
            file_ids=message.file_ids,
            metadata=message.metadata,
            extra=conversation.extra,
        )
        return msg

    def _message_generating(self, conversation: Conversation) -> Message:
        return Message(
            type="system",
            chat_completion_type=OpenAIChatCompletionType.STATUS,
            chat_completion_status=OpenAIChatCompletionStatus.GENERATING,
            content="ai_copilot_generate_response",
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

    def _message_failed(self, conversation: Conversation) -> Message:
        return Message(
            type="system",
            chat_completion_type=OpenAIChatCompletionType.STATUS,
            chat_completion_status=OpenAIChatCompletionStatus.FAILED,
            content="Failed, please try again...",
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

    def _message_expired(self, conversation: Conversation) -> Message:
        return Message(
            type="system",
            chat_completion_type=OpenAIChatCompletionType.STATUS,
            chat_completion_status=OpenAIChatCompletionStatus.EXPIRED,
            content="Expired, please try again...",
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

    def _message_cancelled(self, conversation: Conversation) -> Message:
        return Message(
            type="system",
            chat_completion_type=OpenAIChatCompletionType.STATUS,
            chat_completion_status=OpenAIChatCompletionStatus.CANCELLED,
            content="Cancelled",
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

    def _message_timeout(self, conversation: Conversation) -> Message:
        return Message(
            type="system",
            chat_completion_type=OpenAIChatCompletionType.STATUS,
            chat_completion_status=OpenAIChatCompletionStatus.TIMEOUT,
            content="Wait for messages timeout, please try again...",
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
