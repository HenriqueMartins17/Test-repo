from __future__ import annotations

from langchain_core.messages import AIMessageChunk
from loguru import logger
import traceback
from typing import (
    Any,
    AsyncIterator,
    List,
    Optional,
)

from langchain.callbacks.manager import (
    AsyncCallbackManagerForLLMRun,
)
from langchain_community.embeddings import QianfanEmbeddingsEndpoint

from langchain.schema.messages import BaseMessage
from langchain.schema.output import ChatGenerationChunk

from langchain.chat_models.baidu_qianfan_endpoint import QianfanChatEndpoint
from langchain_community.chat_models.baidu_qianfan_endpoint import _convert_dict_to_message

from ai_shared.tracking import Tracking


class MyBaiduQianfanChatEndpoint(QianfanChatEndpoint):
    async def _astream(
            self,
            messages: List[BaseMessage],
            stop: Optional[List[str]] = None,
            run_manager: Optional[AsyncCallbackManagerForLLMRun] = None,
            **kwargs: Any,
    ) -> AsyncIterator[ChatGenerationChunk]:
        params = self._convert_prompt_msg_params(messages, **kwargs)
        if self.streaming:
            async for res in await self.client.ado(**params):
                msg = _convert_dict_to_message(res)
                chunk = ChatGenerationChunk(
                    text=res["result"],
                    message=AIMessageChunk(
                        content=msg.content,
                        # role="assistant",
                        # additional_kwargs=msg.additional_kwargs,
                    ),
                )
                yield chunk
                if run_manager:
                    await run_manager.on_llm_new_token(chunk.text, chunk=chunk)

        else:
            res = await self.client.ado(**params)
            msg = _convert_dict_to_message(res)
            chunk = ChatGenerationChunk(
                text=res["result"],
                message=AIMessageChunk(
                    content=msg.content,
                    # role="assistant",
                    # additional_kwargs=msg.additional_kwargs,
                ),
            )
            yield chunk
            if run_manager:
                await run_manager.on_llm_new_token(chunk.text, chunk=chunk)


class MyQianfanEmbeddingsEndpoint(QianfanEmbeddingsEndpoint):

    def embed_documents(self, texts: List[str]) -> List[List[float]]:
        """
        Embeds a list of text documents using the AutoVOT algorithm.

        Args:
            texts (List[str]): A list of text documents to embed.

        Returns:
            List[List[float]]: A list of embeddings for each document in the input list.
                            Each embedding is represented as a list of float values.
        """
        text_in_chunks = [
            texts[i: i + self.chunk_size]
            for i in range(0, len(texts), self.chunk_size)
        ]
        lst = []
        for chunk in text_in_chunks:
            resp = self._embed_chunk(chunk)
            if not resp:
                continue
            lst.extend([res["embedding"] for res in resp["data"]])
        return lst

    def _embed_chunk(self, chunk: list[str]) -> dict | None:
        try:
            resp = self.client.do(texts=chunk)
        except Exception as e:
            Tracking.capture_exception(e)
            logger.error(f"{self.__class__}: chunk: {chunk}, _embed_chunk: {str(e)}")
            logger.error(traceback.format_exc())
            return None
        else:
            return resp
