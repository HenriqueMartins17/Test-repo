from abc import ABC, abstractmethod
from typing import Any, AsyncIterator, Coroutine, Dict, List, Optional, Union

from langchain.chat_models.base import BaseChatModel
from langchain.schema.messages import AIMessageChunk
from langchain.schema.runnable.utils import Output

from ai_inference import utils

from langchain.callbacks.base import AsyncCallbackHandler
from langchain.callbacks.manager import Callbacks
from ai_shared.langchain_vika.baidu_qianfan import MyBaiduQianfanChatEndpoint

# from langchain.llms import OpenAI
from langchain.memory import ConversationBufferWindowMemory, ConversationBufferMemory
from langchain.memory.chat_memory import BaseChatMemory
from langchain.memory.chat_message_histories.file import FileChatMessageHistory
from langchain.schema.output import LLMResult
from langchain.schema.runnable import Runnable
from loguru import logger

from ai_shared.ai_setting import QAAISetting, AiSetting
from ai_shared.exceptions import AiNotFoundError
from ai_shared.persist import Persist
from ai_shared.persist.ai_info import AIInfo
from ai_shared.persist.training_info import TrainingInfo
from ai_shared.ros import OpenAIChatCompletionRO
from ai_shared.llmodel import ModelNameOpenAI, ModelNameBaiduQianFan, LlModel
from ai_shared.vos import OpenAIChatCompletionVO

from langchain.vectorstores.base import VectorStoreRetriever

class BaseAgent(ABC):

    """
    Base inference class for RAG(Retrieval Augmented Generation) agent

    You must implement functions:

    - _get_runnable
    - _get_runnable_variables

    """

    llm:  BaseChatModel
    memory: BaseChatMemory | ConversationBufferMemory

    @abstractmethod
    def _get_runnable(self) -> Runnable:
        raise NotImplementedError

    @abstractmethod
    async def _get_runnable_variables(self, input: str) -> Dict:
        raise NotImplementedError

    def __init__(
        self,
        ai_id: str,
        ai_setting: AiSetting,
        model_kwargs: OpenAIChatCompletionRO,
        streaming: bool = False,
        prompt_template: str = None,
        callbacks: Optional[List[Callbacks]] = None,
    ):
        self.ai_id = ai_id
        self.ai_setting = ai_setting
        self.prompt_template_str = prompt_template
        self.model_name = model_kwargs.model
        self.conversation_id = model_kwargs.conversation_id

        self.llm = self._new_llm(model_kwargs, callbacks)

        self.memory: ConversationBufferWindowMemory

        # self.chain = self._new_chain(ai_id, self.llm, self.memory)

        # if streaming:
        #     self._setup_stream(model_kwargs, callbacks)
        # else:
        #     self._setup_query(model_kwargs, callbacks)

    def _new_llm(
        self,
        model_kwargs: OpenAIChatCompletionRO,
        callbacks=Optional[List[Callbacks]],
    ) -> BaseChatModel:
        """
        Create a new LLM model with the given model name and model kwargs

        Currently, we use OpenAI, but we should also compatible with OpenAI API's kwargs.
        """
        return LlModel.get_llm(model_kwargs, callbacks)

    async def _new_memory(self, ai_id: str, conversation_id: str) -> ConversationBufferWindowMemory:
        if self.conversation_id is None or self.conversation_id == "0":
            memory = ConversationBufferWindowMemory(  # empty conversation in memory only, no persistent in file (means no SAVE)
                k=0,
                return_messages=True,
                # , memory_key=self._get_memory_key()
            )  # empty memory
            return memory

        ai_info = await AIInfo.load_ai_info(ai_id)
        if not ai_info:
            raise AiNotFoundError(f"ai_id: {ai_id}")
        # this object will save the chat history in local file with json format.
        conversation_history_file_path = TrainingInfo.make_conversation_file_path(
            ai_id, ai_info.current_training_id, conversation_id
        )
        Persist.ensure_directory_exists(conversation_history_file_path)
        # this object will save the chat history in local file with json format.
        history_store = FileChatMessageHistory(conversation_history_file_path)
        return ConversationBufferWindowMemory(
            k=5,  # the max number of context keep in prompt, langchain default is 5
            chat_memory=history_store,
            return_messages=True,
        )

    async def _save_memory(self, input: str, result: str):
        """
        Save the conversation memory
        """
        self.memory.save_context({"input": input}, {"output": result})

    async def arun(self, input: str) -> str:
        runnable = self._get_runnable()
        variables = await self._get_runnable_variables(input)

        ai_msg = await runnable.ainvoke(variables)

        await self._save_memory(input, ai_msg.content)

        return ai_msg.content

    async def _output_idk(self) -> AsyncIterator[Output]:
        # QA only
        assert isinstance(self.ai_setting, QAAISetting)
        yield AIMessageChunk(content=self.ai_setting.idk)

    async def astream(self, input: str) -> AsyncIterator[Output]:
        runnable = self._get_runnable()
        variables = await self._get_runnable_variables(input)

        is_idk = variables.pop("is_idk", False)
        if is_idk:
            logger.info(f"_output_idk: ai: {self.ai_id}, input: {input}, idk: {self.ai_setting.idk}")
            return self._output_idk()

        config = {
            "callbacks": [RAGAgentAStreamAsyncCallbackHandler(input, self)],
        }
        async_iter = runnable.astream(variables, config)

        return async_iter

    def on_response(self, query: str, resp: OpenAIChatCompletionVO):
        """
        Inject Action into OpenAIChatCompletionVO
        """
        pass


class BaseRAGAgent(BaseAgent):
    def __init__(
        self,
        ai_id: str,
        ai_setting: QAAISetting,
        model_kwargs: OpenAIChatCompletionRO,
        streaming: bool = False,
        prompt_template: str = None,
        callbacks: List[Callbacks] | None = None,
    ):
        super().__init__(
            ai_id, ai_setting, model_kwargs, streaming, prompt_template, callbacks
        )
    

class RAGAgentAStreamAsyncCallbackHandler(AsyncCallbackHandler):
    """
    Save memory after `.astream` done
    """

    def __init__(self, input: str, agent: BaseRAGAgent):
        super().__init__()
        self.input = input
        self.agent = agent

    async def on_llm_end(
        self, response: LLMResult, **kwargs: Any
    ) -> None:
        await super().on_llm_end(response, **kwargs)
        logger.info(f"[RAGAgentAStreamAsyncIteratorCallbackHandler on_llm_end LLMResult]: {response}")
        await self.agent._save_memory(self.input, response.generations[0][0].text)
