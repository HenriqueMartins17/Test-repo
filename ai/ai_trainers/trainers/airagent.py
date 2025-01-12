import traceback
from typing import Iterator, Optional, AsyncIterator, AsyncGenerator

from pydantic import BaseModel
from loguru import logger
from ai_shared.databus import AiNode, DataBus
from ai_shared.databus.ai_po import AiNodeType, AiNodeAirTableSetting, AiNodeAiTableSetting, AiNodeFileSetting
from ai_shared.exceptions import NoLoaderError, AiNodeNotFoundError, AiNodeNoSettingError, AiNodeNoTypeError, \
    AiNodeAirTableSettingError, AiNodeAiTableSettingError, AiNodeFileSettingError
from ai_shared.persist import DataSource
from ai_shared.tracking import Tracking
from ai_trainers.loaders.airagent.airtable import AirAgentAirTableLoader
from ai_trainers.loaders.airagent.aitable import AirAgentAiTableLoader
from ai_trainers.loaders.airagent.file import AirAgentFileLoader
from ai_trainers.trainers.base import BaseRAGTrainer, TrainProcessInfo


class AirAgentTrainer(BaseRAGTrainer):

    async def get_ai_node_list(self) -> list[AiNode]:
        ai_node_list = DataBus.get_ai_node_list_airagent(ai_id=self.ai_id)
        return ai_node_list

    def load_data_source(
            self,
            ai_node: Optional[AiNode] = None,
            unique_keys: Optional[set] = None,
            is_predict: bool = False,
    ) -> Optional[Iterator[DataSource]]:
        if ai_node:
            try:
                logger.info(f"load_data_sources by ai_node_id: {ai_node.id}, ai_id: {self.ai_id}")
                if not ai_node:
                    raise AiNodeNotFoundError(f"ai_id: {self.ai_id}, ai_node_id: {ai_node.id}")
                data_sources = self._load_data_sources(ai_node=ai_node, is_predict=is_predict)
            except Exception as e:
                Tracking.capture_exception(e)
                logger.error(f"{self.__class__}: ai_id: {self.ai_id}, ai_node_id: {ai_node.id}, "
                             f"load_data_sources: {str(e)}")
                logger.error(traceback.format_exc())
            else:
                for ds in data_sources:
                    yield ds

    def load_data_sources(self, ai_nodes: Optional[list[AiNode]] = None, is_predict: Optional[bool] = False) -> Iterator[DataSource]:
        logger.info(f"load_data_sources by ai_id: {self.ai_id}")
        if ai_nodes:
            for ai_node in ai_nodes:
                try:
                    data_sources = self._load_data_sources(ai_node=ai_node, is_predict=is_predict)
                except Exception as e:
                    Tracking.capture_exception(e)
                    logger.error(f"{self.__class__}: ai_id: {self.ai_id}, load_data_sources")
                    logger.error(traceback.format_exc())
                else:
                    for ds in data_sources:
                        yield ds

    def _load_data_sources(self, ai_node: AiNode, is_predict: Optional[bool] = False) -> Iterator[DataSource]:
        setting = ai_node.setting
        if not setting:
            raise AiNodeNoSettingError(f"{ai_node.id=}")

        ai_node_type = ai_node.type
        if not ai_node_type:
            raise AiNodeNoTypeError(f"{ai_node.id=}")

        if ai_node_type.is_airtable():
            setting: AiNodeAirTableSetting = ai_node.setting
            if not all([setting.api_token, setting.base_id, setting.table_id]):
                raise AiNodeAirTableSettingError(f"{ai_node.id=}, {ai_node_type=}, {setting=}")

            loader = AirAgentAirTableLoader(setting, is_predict=is_predict)

        elif ai_node_type.is_aitable():
            setting: AiNodeAiTableSetting = ai_node.setting
            if not all([setting.api_token, setting.datasheet_id]):
                raise AiNodeAiTableSettingError(f"{ai_node.id=}, {ai_node_type=}, {setting=}")

            loader = AirAgentAiTableLoader(setting, is_predict=is_predict)

        elif ai_node_type.is_file():
            setting: AiNodeFileSetting = ai_node.setting
            if not setting.url:
                raise AiNodeFileSettingError(f"{ai_node.id=}, {ai_node_type=}, {setting=}")

            loader = AirAgentFileLoader(setting, is_predict=is_predict)

        else:
            raise NoLoaderError(f"{self.__class__}: ai_id: {self.ai_id}, load_data_source: {ai_node_type=}")

        for ds in loader.lazy_load():
            yield ds
