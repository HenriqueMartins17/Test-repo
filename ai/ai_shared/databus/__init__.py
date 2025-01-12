import asyncio
from typing import Union, Optional

from ai_shared.ai_setting import AiSetting
from ai_shared.config import settings

from ai_shared.databus.ai_po import AiNode, Ai, AiNodeAirTableSetting
from ai_shared.databus.datasheet import Datasheet
from ai_shared.databus.providers import MockDataBusProvider, DataBusProvider
from ai_shared.mock_utils import is_mock


class DataBus:
    """
    DataBus Service
    sync and async pure functions
    all staticmethod
    no any inner state
    """

    @staticmethod
    def _get_provider(mock: bool = False) -> Union[DataBusProvider, MockDataBusProvider]:
        if settings.is_mock() or mock:
            return MockDataBusProvider()
        else:
            return DataBusProvider()

    @staticmethod
    def get_ai(ai_id: str) -> Ai:
        mock = is_mock(ai_id)
        provider = DataBus._get_provider(mock=mock)
        ai = provider.get_ai(ai_id)
        return ai

    @staticmethod
    async def aget_ai(ai_id: str) -> Ai:
        loop = asyncio.get_event_loop()
        ai = await loop.run_in_executor(
            None, DataBus.get_ai, ai_id
        )
        return ai

    @staticmethod
    def get_ai_setting(ai_id: str) -> AiSetting:
        mock = is_mock(ai_id)
        provider = DataBus._get_provider(mock=mock)
        ai = provider.get_ai(ai_id)
        return ai.to_ai_setting()

    @staticmethod
    async def aget_ai_setting(ai_id: str) -> AiSetting:
        loop = asyncio.get_event_loop()
        ai_setting = await loop.run_in_executor(
            None, DataBus.get_ai_setting, ai_id
        )
        return ai_setting

    @staticmethod
    def get_ai_datasheet_ids(ai_id: str) -> list[str]:
        mock = is_mock(ai_id)
        provider = DataBus._get_provider(mock=mock)
        return provider.get_ai_datasheet_ids(ai_id)

    @staticmethod
    async def aget_ai_datasheet_ids(ai_id: str) -> list[str]:
        loop = asyncio.get_event_loop()
        dst_ids = await loop.run_in_executor(
            None, DataBus.get_ai_datasheet_ids, ai_id
        )
        return dst_ids

    @staticmethod
    def get_ai_node(ai_id: str, dst_id: str) -> Optional[AiNode]:
        mock = is_mock(ai_id)
        provider = DataBus._get_provider(mock=mock)
        return provider.get_ai_node(ai_id, dst_id)

    @staticmethod
    async def aget_ai_node(ai_id: str, dst_id: str) -> Optional[AiNode]:
        loop = asyncio.get_event_loop()
        ai_node = await loop.run_in_executor(
            None, DataBus.get_ai_node, ai_id, dst_id
        )
        return ai_node

    @staticmethod
    def get_ai_node_list_aitable(ai_id: str) -> list[AiNode]:
        mock = is_mock(ai_id)
        provider = DataBus._get_provider(mock=mock)

        ai_node_list = []
        dst_ids = provider.get_ai_datasheet_ids(ai_id)
        for dst_id in dst_ids:
            ai_node = provider.get_ai_node(ai_id, dst_id)
            if not ai_node:
                continue
            ai_node_list.append(ai_node)
        return ai_node_list

    @staticmethod
    async def aget_ai_node_list_aitable(ai_id: str) -> list[AiNode]:
        loop = asyncio.get_event_loop()
        ai_nodes = await loop.run_in_executor(
            None, DataBus.get_ai_node_list_aitable, ai_id
        )
        return ai_nodes

    @staticmethod
    def get_ai_node_list_airagent(ai_id: str) -> list[AiNode]:
        mock = is_mock(ai_id)
        provider = DataBus._get_provider(mock=mock)
        return provider.get_ai_node_list(ai_id)

    @staticmethod
    async def aget_ai_node_list_airagent(ai_id: str) -> list[AiNode]:
        loop = asyncio.get_event_loop()
        ai_nodes = await loop.run_in_executor(
            None, DataBus.get_ai_node_list_airagent, ai_id
        )
        return ai_nodes

    @staticmethod
    def get_ai_node_by_id(ai_node_id: int) -> Optional[AiNode]:
        mock = is_mock(ai_node_id)
        provider = DataBus._get_provider(mock=mock)
        return provider.get_ai_node_by_id(ai_node_id)

    @staticmethod
    async def aget_ai_node_by_id(ai_node_id: int) -> Optional[AiNode]:
        loop = asyncio.get_event_loop()
        ai_node = await loop.run_in_executor(
            None, DataBus.get_ai_node_by_id, ai_node_id
        )
        return ai_node

    @staticmethod
    def get_datasheet(dst_id: str, view_id: Optional[str]) -> Datasheet:
        mock = is_mock(dst_id)
        provider = DataBus._get_provider(mock=mock)
        return provider.get_datasheet(dst_id, view_id)

    @staticmethod
    async def aget_datasheet(dst_id: str, view_id: Optional[str]) -> Datasheet:
        loop = asyncio.get_event_loop()
        dst = await loop.run_in_executor(
            None, DataBus.get_datasheet, dst_id, view_id
        )
        return dst


__all__ = ["DataBus"]
