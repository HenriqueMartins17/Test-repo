import json
from abc import ABC, abstractmethod
from typing import Optional


from ai_shared import api_client_sync
from ai_shared.ai_setting import AIAgentType
from ai_shared.config import settings
from ai_shared.databus.ai_po import Ai, AiNode
from ai_shared.databus.datasheet import Datasheet
from ai_shared.databus.ai_po import AiNodeDatasheetSetting
from ai_shared.exceptions import DatasheetPackNotFoundError
from ai_shared.llmodel import ModelNameBaiduQianFan, ModelNameOpenAI
from ai_shared.types import DatasheetPack


class DatabusProviderABC(ABC):

    @abstractmethod
    def get_ai(self, ai_id: str) -> Ai:
        raise NotImplementedError

    @abstractmethod
    def get_ai_datasheet_ids(self, ai_id: str) -> list[str]:
        raise NotImplementedError

    @abstractmethod
    def get_ai_node(self, ai_id: str, dst_id: str) -> Optional[AiNode]:
        raise NotImplementedError

    @abstractmethod
    def get_ai_node_list(self, ai_id: str) -> list[AiNode]:
        raise NotImplementedError

    @abstractmethod
    def get_ai_node_by_id(self, ai_node_id: int) -> Optional[AiNode]:
        raise NotImplementedError

    @abstractmethod
    def get_datasheet(self, dst_id: str, view_id: Optional[str]) -> Datasheet:
        raise NotImplementedError


class DataBusProvider(DatabusProviderABC):

    def get_ai(self, ai_id: str) -> Ai:
        """
        Get AI PO from databus-server remote call
        """
        base_url = settings.databus_server_base_url
        path = f"/databus/dao/get_ai/{ai_id}"
        url = api_client_sync.make_url(base_url, path)
        res = api_client_sync.get(url)
        ai_dict: dict = res.get("data", {})
        ai = Ai.parse_obj(ai_dict)
        return ai

    def get_ai_datasheet_ids(self, ai_id: str) -> list[str]:
        base_url = settings.databus_server_base_url
        path = f"/databus/dao/get_ai_datasheet_ids/{ai_id}"
        url = api_client_sync.make_url(base_url, path)
        res = api_client_sync.get(url)
        dst_ids: list[str] = res.get("data", [])

        if not isinstance(dst_ids, list):
            raise ValueError(f"can't get datasheet ids list by ai_id: {ai_id}")

        if len(dst_ids) == 0:
            raise ValueError(f"get datasheet ids empty by ai_id: {ai_id}")

        return dst_ids

    def get_ai_node(self, ai_id: str, dst_id: str) -> Optional[AiNode]:
        base_url = settings.databus_server_base_url
        path = "/databus/dao/get_ai_node"
        url = api_client_sync.make_url(base_url, path)
        params = {
            "aiId": ai_id,
            "nodeId": dst_id,
        }
        res = api_client_sync.get(url, params=params)
        data: dict = res.get("data", {})
        if not data:
            return None
        ai_node = AiNode.new(data)
        return ai_node

    def get_ai_node_list(self, ai_id: str) -> list[AiNode]:
        base_url = settings.databus_server_base_url
        path = "/databus/dao/get_ai_node_list"
        url = api_client_sync.make_url(base_url, path)
        params = {
            "aiId": ai_id,
        }
        res = api_client_sync.get(url, params=params)
        data = res.get("data")
        if not data:
            return []

        entities = []
        for row in data:
            entity = AiNode.new(row)
            entities.append(entity)
        return entities

    def get_ai_node_by_id(self, ai_node_id: int) -> Optional[AiNode]:
        base_url = settings.databus_server_base_url
        path = "/databus/dao/get_ai_node_by_id"
        url = api_client_sync.make_url(base_url, path)
        params = {
            "id": ai_node_id,
        }
        res = api_client_sync.get(url, params=params)
        data = res.get("data")
        if not data:
            return None
        entity = AiNode.new(data)
        return entity

    def get_datasheet(self, dst_id: str, view_id: Optional[str]) -> Datasheet:
        """
        Load all nodes from space exactly
        """
        return Datasheet(self._get_data_pack(dst_id=dst_id, view_id=view_id))

    def _get_data_pack(self, dst_id: str, view_id: Optional[str]) -> DatasheetPack:
        base_url = settings.databus_server_base_url
        path = f"/databus/get_datasheet_pack/{dst_id}"
        url = api_client_sync.make_url(base_url, path)
        params = {}
        if view_id:
            params["viewId"] = view_id

        res = api_client_sync.get(url, params=params)
        data = res.get("data")
        if not data:
            raise DatasheetPackNotFoundError(f"_get_data_pack: {data=}")

        return DatasheetPack(**data)


class MockDataBusProvider(DatabusProviderABC):

    def get_ai(self, ai_id: str):
        mock_ai = Ai(aiId=ai_id, type=AIAgentType.QA)  # mock ai
        if "chat" in ai_id:
            mock_ai.type = AIAgentType.CHAT
        # default is openai embedding and openai model
        if "mock_baidu" in ai_id:
            mock_ai.model = ModelNameBaiduQianFan.ERNIE_BOT_TURBO.value
        if "mock_openai" in ai_id:
            mock_ai.model = ModelNameOpenAI.GPT_3_5_TURBO.value
        return mock_ai

    def get_ai_datasheet_ids(self, ai_id: str) -> list[str]:
        return ["dstV8QmHgwl8HpNq49"]

    def get_ai_node(self, ai_id: str, dst_id: str) -> Optional[AiNode]:
        return AiNode(
            id=100,
            aiId=ai_id,
            type="datasheet",
            setting=AiNodeDatasheetSetting()
        )

    def get_ai_node_list(self, ai_id: str) -> list[AiNode]:
        data = [AiNode(
            id=100,
            aiId=ai_id,
            type="datasheet",
            setting=AiNodeDatasheetSetting()
        )]
        return data

    def get_ai_node_by_id(self, ai_node_id: int) -> Optional[AiNode]:
        return AiNode(
            id=ai_node_id,
            aiId="mock",
            type="datasheet",
            setting=AiNodeDatasheetSetting()
        )

    def get_datasheet(self, dst_id: str, view_id: Optional[str]) -> Datasheet:
        with open("./tests/dstV8QmHgwl8HpNq49.json", mode='r') as fd:
            contents = fd.read()
        res = json.loads(contents)
        data = res.get("data")
        if not data:
            raise DatasheetPackNotFoundError(f"_get_data_pack: {data=}")

        dsp = DatasheetPack(**data)
        return Datasheet(dsp)
