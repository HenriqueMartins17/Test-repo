import ctypes
import datetime
import multiprocessing
import random
import time
import traceback
from abc import abstractmethod
from typing import Iterator, List, Optional, Union, Tuple, AsyncIterator, AsyncGenerator
import re
from langchain.docstore.document import Document
from langchain.llms.openai import OpenAI
from langchain.schema.embeddings import Embeddings
from loguru import logger
from pydantic import BaseModel, Field

from ai_inference import utils
from ai_shared import prompt_utils
from ai_shared.ai_setting import AIAgentType, AiSetting
from ai_shared.config import settings
from ai_shared.data_source_processor import DataSourceProcessor
from ai_shared.databus import DataBus, Ai, AiNode
from ai_shared.exceptions import AiNotFoundError, TrainingNotFoundError, NoEmbeddingsError
from ai_shared.persist import Persist
from ai_shared.persist.ai_info import AIInfo
from ai_shared.persist.data_source import DataSource
from ai_shared.persist.training_info import TrainingInfo, TrainingStatus
from ai_shared.llmodel import ModelNameOpenAI, ModelNameBaiduQianFan, LlModel
from ai_shared.ros import PostTrainBody, TrainerAiNodes
from ai_shared.suggestion import Suggestion
from ai_shared.tracking import Tracking
from langchain.schema import(HumanMessage)
class TrainCallback:
    def __init__(self) -> None:
        self.done = multiprocessing.Value(ctypes.c_bool, False)
        self.err = multiprocessing.Value(ctypes.c_bool, False)
        self.training_info: Optional[TrainingInfo] = None

    def set_done(self, training_info: TrainingInfo):
        self.done.value = True
        self.err.value = False
        self.training_info = training_info

    def set_err(self, training_info: TrainingInfo):
        self.done.value = True
        self.err.value = True
        self.training_info = training_info


class TrainProcessInfo(BaseModel):
    """
    When we start a train, give back the info about this train info pack,
    includes ai_model(mysql), ai_setting(mysql), ai_info(json), training_info(json)
    """

    ai_id: str
    ai_info: AIInfo  # do not modify this for safe load & save
    new_training_id: str

    ai_model: Optional[Ai] = Field(default=None)
    ai_setting: Optional[AiSetting] = Field(default=None)
    ai_agent_type: AIAgentType = Field(default=AIAgentType.CHAT)

    ai_nodes: Optional[list[AiNode]] = Field(default=None)

    @staticmethod
    def make_training_id(ai_id: str) -> str:
        """
        Every "train" will have a unique Vector DB store place
        The store folder name is the `training_id`
        Returns the Vector DB's persistent name of the AI.
        """
        now = datetime.datetime.utcnow()  # GMT+0
        formatted_datetime = now.strftime("%Y%m%d%H%M%S")
        return f"{ai_id}_{formatted_datetime}"

    async def get_ai_info(self) -> Union[AIInfo, None]:
        return await AIInfo.load_ai_info(self.ai_id)

    async def get_training_info(self) -> Union[TrainingInfo, None]:
        return await TrainingInfo.load_training_info(self.ai_id, self.new_training_id)


class BaseRAGTrainer:
    """
    BaseTrainer for RAG (Retrieval Augmented Generation) model
    """

    def __init__(self, ai_id: str) -> None:
        self.ai_id = ai_id

    def prepare(self):
        """
        Prepare the data, for example, crawl web pages and cache them before train them.
        """
        raise NotImplementedError("predict is not implemented")

    @abstractmethod
    def load_data_sources(self, ai_nodes: Optional[list[AiNode]] = None, is_predict: bool = False) -> Iterator[DataSource]:
        """
        Load `Data Sources` from AI's data sources

        Return:
        - List[Document]: the documents for training
        - Dict: the data sources info that return
        """
        raise NotImplementedError("load_data_sources is not implemented")

    @abstractmethod
    def load_data_source(
            self,
            ai_node: Optional[AiNode] = None,
            unique_keys: Optional[set] = None,
            is_predict: bool = False,
    ) -> Optional[Iterator[DataSource]]:
        raise NotImplementedError("load_data_source is not implemented")

    async def do_train(
        self, callbacks: Optional[List[TrainCallback]] = None
    ) -> TrainProcessInfo:
        """
        Train with new training ID automatically
        Quick function to do train in sync
        """
        if callbacks:
            assert isinstance(callbacks, list)

        train_process_info = await self.new_train_process_info()
        await self.do_train_with_process_info(train_process_info, callbacks)
        return train_process_info

    async def get_ai_node_list(self) -> list[AiNode]:
        raise NotImplementedError("you should to impl get_ai_node_list")

    async def get_trainer_ai_nodes(self, body: Optional[PostTrainBody] = None) -> TrainerAiNodes:
        trainer_ai_nodes = TrainerAiNodes(ai_id=self.ai_id)

        ai_node_list = []
        try:
            if body and body.ai_node_id:
                # one node of ai
                ai_node = await DataBus.aget_ai_node_by_id(body.ai_node_id)
                ai_node_list.append(ai_node)
            elif body and body.ai_node_ids:
                # multi node of ai
                for ai_node_id in body.ai_node_ids:
                    ai_node = await DataBus.aget_ai_node_by_id(ai_node_id)
                    ai_node_list.append(ai_node)
            elif body and body.datasheet_id:
                # one datasheet
                ai_node = await DataBus.aget_ai_node(ai_id=self.ai_id, dst_id=body.datasheet_id)
                ai_node_list.append(ai_node)
            elif body and body.datasheet_ids:
                # multi datasheets
                for dst_id in body.datasheet_ids:
                    ai_node = await DataBus.aget_ai_node(ai_id=self.ai_id, dst_id=dst_id)
                    ai_node_list.append(ai_node)
            else:
                # all node of ai
                ai_node_list = await self.get_ai_node_list()
        except Exception as e:
            Tracking.capture_exception(e)
            logger.error(f"get_trainer_ai_nodes: {str(e)}")
            logger.error(traceback.format_exc())

        trainer_ai_nodes.ai_nodes = ai_node_list
        return trainer_ai_nodes

    async def new_train_process_info(self, body: Optional[PostTrainBody] = None) -> TrainProcessInfo:
        # the training ID format like `{ai_id}_{%Y%m%d%H%M%S}`
        new_training_id = TrainProcessInfo.make_training_id(self.ai_id)

        ai: Optional[Ai] = None
        ai_setting: Optional[AiSetting] = None
        ai_agent_type: AIAgentType = AIAgentType.CHAT

        try:
            ai = await DataBus.aget_ai(self.ai_id)
            ai_setting = ai.to_ai_setting()
            ai_agent_type = ai_setting.type
        except Exception as e:
            Tracking.capture_exception(e)
            logger.warning(
                f"AI: {self.ai_id} Training {new_training_id} cannot find ai po in MySQL. But go on. {str(e)}"
            )
            logger.error(traceback.format_exc())

        ai_info: AIInfo
        if not AIInfo.exist(self.ai_id):
            ai_info = await AIInfo.new(self.ai_id)
        else:
            ai_info = await AIInfo.load_ai_info(self.ai_id)
            if not ai_info:
                raise AiNotFoundError(f"ai_id: {self.ai_id}")

        # if not ai_node_ids should train as old logic, like copilot trainer etc.
        trainer_ai_nodes = await self.get_trainer_ai_nodes(body)

        training_info = TrainingInfo(ai_id=ai_info.ai_id, training_id=new_training_id)
        await training_info.save()

        train_process_info: TrainProcessInfo = TrainProcessInfo(
            new_training_id=new_training_id,
            ai_id=self.ai_id,
            ai_info=ai_info,
            ai_model=ai,
            ai_setting=ai_setting,
            ai_agent_type=ai_agent_type,
            ai_nodes=trainer_ai_nodes.ai_nodes,
        )
        return train_process_info

    async def do_train_with_process_info(
        self,
        train_process_info: TrainProcessInfo,
        callbacks: Optional[List[TrainCallback]] = None,
    ):
        try:
            await self._do_train_with_process_info(train_process_info, callbacks)
        except Exception as e:
            Tracking.capture_exception(e)
            logger.error(f"do_train_with_process_info: failed: {str(e)}")
            logger.error(traceback.format_exc())
        else:
            logger.info(f"do_train_with_process_info: succeed: "
                        f"{train_process_info.ai_id=}, {train_process_info.new_training_id=}")

    async def _do_train_with_process_info(
        self,
        train_process_info: TrainProcessInfo,
        callbacks: Optional[List[TrainCallback]] = None,
    ):
        """
        Train with the training ID you provided
        """
        new_training_id = train_process_info.new_training_id
        logger.info(f"_do_train_with_process_info: AI: {self.ai_id} Training {new_training_id} started.....")
        try:
            # 1. starting...
            await self._training_start(train_process_info)

            # 2. processing...
            data_sources_with_docs = await self._training_processing(train_process_info)
            suggestions = await self._make_suggestions(
                ai_id=train_process_info.ai_id,
                ai_nodes=train_process_info.ai_nodes,
            )
        except Exception as e:
            Tracking.capture_exception(e)
            logger.error(f"_do_train_with_process_info: caught exception in train_thread: {str(e)}")
            logger.error(traceback.format_exc())

            # 4. when failed...
            training_info = await self._training_failed(train_process_info, str(e))
            if callbacks:
                for callback in callbacks:
                    callback.set_err(training_info)
        else:
            # 3. when succeed...
            training_info = await self._training_succeed(train_process_info, data_sources_with_docs, suggestions)
            if callbacks:
                for callback in callbacks:
                    callback.set_done(training_info)
        finally:
            logger.info(f"_do_train_with_process_info: AI: {self.ai_id} Training {new_training_id} end.....")

    async def _training_start(self, train_process_info: TrainProcessInfo) -> None:
        # AiInfo update
        ai_info = await train_process_info.get_ai_info()
        if not ai_info:
            raise AiNotFoundError(f"_training_start: ai_id: {self.ai_id}")
        training_info = await train_process_info.get_training_info()
        if not training_info:
            raise TrainingNotFoundError(f"_training_start: ai_id: {self.ai_id}, "
                                        f"training_id: {train_process_info.new_training_id}")

        await ai_info.lock(train_process_info.new_training_id)

        # TrainingInfo update
        ai_setting = await DataBus.aget_ai_setting(self.ai_id)
        model = ai_setting.model or LlModel.get_default_model_by_edition()
        embedding_model = LlModel.get_embedding_model_by_model(model=model)

        training_info.embedding_model = embedding_model

        training_info.status = TrainingStatus.TRAINING
        training_info.started_at = int(time.time())
        await training_info.save()
        return

    async def _training_processing(self, train_process_info: TrainProcessInfo) -> list[DataSource]:
        training_info = await train_process_info.get_training_info()
        if not training_info:
            raise TrainingNotFoundError(f"_training_processing: ai_id: {self.ai_id}, "
                                        f"training_id: {train_process_info.new_training_id}")

        # iter_data_sources_with_docs = self.load_data_sources(train_process_info.ai_nodes)
        # data_sources_with_docs = list(iter_data_sources_with_docs)
        # docs: List[Document] = []
        # for data_source in data_sources_with_docs:
        #     logger.debug(f"do_train_with_process_info: data source: {data_source.type} -> {data_source.type_id}")
        #     for d in data_source.documents:
        #         docs.append(d)

        # training_persist_path = training_info.get_training_path()
        # assert training_persist_path

        # todo: remove data_sources_with_docs later
        data_sources_with_docs = []

        ai_id = train_process_info.ai_id
        training_id = train_process_info.new_training_id
        embedding_model = training_info.embedding_model
        # todo: how if url and attachment loader change call by tasks?
        unique_keys = set()
        if train_process_info.ai_nodes:
            all_docs = []
            for ai_node in train_process_info.ai_nodes:
                # AiInfo ai_node -> training_ids
                await AIInfo.add_ai_node_training_id(ai_id=ai_id, ai_node_id=ai_node.id, training_id=training_id)
                # chat agent no need to load docs
                if train_process_info.ai_agent_type.is_chat():
                    continue

                for ds in self.load_data_source(ai_node=ai_node, unique_keys=unique_keys):
                    if not ds:
                        continue
                    # TrainingInfo datasources
                    await TrainingInfo.add_data_source(ai_id=ai_id, training_id=training_id, ds=ds)

                    if train_process_info.ai_agent_type.need_embeddings():
                        # delete by ds.type_id in db
                        # todo: now is fake delete and add, be cause every new train always gen new Chroma db
                        # res = await DataSourceProcessor.delete_docs(
                        #     ai_id=ai_id,
                        #     training_id=training_id,
                        #     embedding_model=embedding_model,
                        #     ds=ds
                        # )
                        # logger.info(f"DataSourceProcessor.delete_docs: {res=}")
                        # # add docs to db
                        # res = await DataSourceProcessor.add_docs(
                        #     ai_id=ai_id,
                        #     training_id=training_id,
                        #     embedding_model=embedding_model,
                        #     ds=ds,
                        # )
                        # logger.info(f"DataSourceProcessor.add_docs: {res=}")
                        all_docs.extend(ds.documents)
                        data_sources_with_docs.append(ds)

            # todo: use old from_documents yet, chroma add + delete is not working as we expected
            if train_process_info.ai_agent_type.need_embeddings():
                persist_path = TrainingInfo.make_training_path(ai_id=ai_id, training_id=training_id)
                db = await DataSourceProcessor.embedding(
                    training_persist_path=persist_path,
                    embedding_model=embedding_model,
                    docs=all_docs,
                    verbose=True
                )
                assert db
        return data_sources_with_docs

    async def _make_suggestions(self, ai_id: str, ai_nodes: list[AiNode]) -> list[str]:
        try:
            docs = await DataSourceProcessor.peek_sample_docs(ai_id=ai_id, ai_nodes=ai_nodes)
            suggestions = await Suggestion.amake_suggestions(ai_id, docs)
        except Exception as e:
            Tracking.capture_exception(e)
            logger.error(f"_make_suggestions: failed: {str(e)}")
            logger.error(traceback.format_exc())
            return []
        else:
            return suggestions

    async def _training_succeed(
            self,
            train_process_info: TrainProcessInfo,
            data_sources_with_docs: list[DataSource],
            suggestions: list[str],
    ) -> TrainingInfo:
        # TrainingInfo update
        new_training_id = train_process_info.new_training_id
        training_info = await train_process_info.get_training_info()  # because it is saved, you should get new obj
        if not training_info:
            raise TrainingNotFoundError(f"_training_succeed: ai_id: {self.ai_id}, training_id: {new_training_id}")

        training_info.set_data_sources(data_sources_with_docs)
        training_info.set_suggestions(suggestions)

        training_info.status = TrainingStatus.SUCCESS
        take_time = int(time.time()) - training_info.started_at
        training_info.info = f"AI: {self.ai_id} Training {new_training_id} take {take_time}s."
        training_info.finished_at = int(time.time())
        await training_info.save()

        # AiInfo update
        ai_info = await train_process_info.get_ai_info()
        if not ai_info:
            raise AiNotFoundError(f"_training_succeed: ai_id: {self.ai_id}")

        ai_info.success_train_history.append(new_training_id)  # after success
        ai_info.current_training_id = new_training_id  # latest_training_id
        await ai_info.unlock()
        return training_info

    async def _training_failed(self, train_process_info: TrainProcessInfo, error: str) -> TrainingInfo:
        # TrainingInfo update
        new_training_id = train_process_info.new_training_id
        training_info = await train_process_info.get_training_info()  # because it is saved, you should get new obj
        if not training_info:
            raise TrainingNotFoundError(f"_training_failed: ai_id: {self.ai_id}, training_id: {new_training_id}")

        training_info.status = TrainingStatus.FAILED
        training_info.info = error
        training_info.finished_at = int(time.time())
        await training_info.save()

        # AiInfo update
        ai_info = await train_process_info.get_ai_info()
        if not ai_info:
            raise AiNotFoundError(f"_training_failed: ai_id: {self.ai_id}")
        await ai_info.unlock()
        return training_info
