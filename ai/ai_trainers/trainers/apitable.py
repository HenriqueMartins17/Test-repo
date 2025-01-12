import traceback
from typing import Iterator, Tuple, Optional, AsyncIterator, AsyncGenerator

from langchain.schema.embeddings import Embeddings
from loguru import logger

from ai_inference import utils
from ai_shared.ai_setting import AIAgentType
from ai_shared.databus import DataBus, Ai, AiNode
from ai_shared.databus.ai_po import AiNodeDatasheetSetting
from ai_shared.exceptions import NoEmbeddingsError, AiNodeNoSettingError, AiNodeNoTypeError, \
    AiNodeDatasheetSettingError, NoLoaderError, AiNodeNotFoundError
from ai_shared.ros import DataSourceRO
from ai_shared.llmodel import ModelNameOpenAI, ModelNameBaiduQianFan
from ai_shared.tracking import Tracking
from ai_trainers.loaders import APITableDataBusLoader

from .base import BaseRAGTrainer, DataSource, TrainProcessInfo


class APITableTrainer(BaseRAGTrainer):
    """\
    Read APITable MySQL Database via DataBus component to training data
    """

    async def get_ai_node_list(self) -> list[AiNode]:
        ai_node_list = await DataBus.aget_ai_node_list_aitable(self.ai_id)
        return ai_node_list

    def load_data_sources(self, ai_nodes: Optional[list[AiNode]] = None, is_predict: bool = False) -> Iterator[DataSource]:
        """\
        Load apitable datasheet with AI_ID from MySQL database as Document object.
        """
        ai_setting = DataBus.get_ai_setting(self.ai_id)

        if ai_setting.type.is_chat():
            # Chat bot doesn't need to load datasheet
            return

        if not ai_nodes:
            return

        unique_keys = set()
        for ai_node in ai_nodes:
            setting = ai_node.setting
            loader = APITableDataBusLoader(setting, unique_keys=unique_keys, is_predict=is_predict)
            for ds in loader.lazy_load():
                yield ds

    def load_data_source(
            self,
            ai_node: Optional[AiNode] = None,
            unique_keys: Optional[set] = None,
            is_predict: bool = False,
    ) -> Optional[Iterator[DataSource]]:
        if not ai_node:
            yield None
        setting = ai_node.setting
        if not setting:
            yield None

        if unique_keys is None:
            unique_keys = set()

        loader = APITableDataBusLoader(setting, unique_keys=unique_keys, is_predict=is_predict)
        for ds in loader.lazy_load():
            yield ds

    def predict(self, data_source_ros: Iterator[DataSourceRO]) -> Iterator[DataSource]:
        unique_keys = set()
        for ro in data_source_ros:
            if ro.type == "datasheet":
                setting = AiNodeDatasheetSetting(datasheetId=ro.type_id)
                loader = APITableDataBusLoader(setting, unique_keys=unique_keys, is_predict=True)
                for ds in loader.lazy_load():
                    yield ds
            else:
                raise NotImplementedError(f"Unsupport data source type: {ro.type}")


