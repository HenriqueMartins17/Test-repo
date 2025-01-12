from typing import Iterator, Optional

from apitable.datasheet import Record, Datasheet, QuerySet
from apitable.types import RawRecord, MetaField, MetaView
from langchain.schema import Document

from ai_shared.databus.ai_po import AiNodeAiTableSetting
from ai_shared.persist import DataSource
from ai_trainers.loaders.base import BaseDataSourceLoader
from ai_shared.persist.data_source import count_words_and_characters, count_openai_tokens

from apitable import Apitable


class MockRecordsManager:

    def __init__(self, datasheet_id: str):
        self.datasheet_id = datasheet_id

    def all(self, viewId: Optional[str] = None) -> QuerySet:
        records = [RawRecord(recordId="test", fields={"Title": "test"})]
        return QuerySet(dst=self.datasheet_id, records=records)


class MockFieldsManager:

    def __init__(self, datasheet_id: str):
        self.datasheet_id = datasheet_id

    def all(self) -> list[MetaField]:
        fields = [MetaField(id="test", name="test", type="test")]
        return fields


class MockDatasheet:

    def __init__(self, datasheet_id: str):
        self.datasheet_id = datasheet_id

    @property
    def records(self) -> MockRecordsManager:
        return MockRecordsManager(self.datasheet_id)

    @property
    def fields(self) -> MockFieldsManager:
        return MockFieldsManager(self.datasheet_id)

    def get_views(self):
        return [MetaView(id="mock_view", name="mock_view")]


class MockApiTable:

    def __init__(self, api_token: str):
        self.api_token = api_token

    def datasheet(self, datasheet_id: str) -> MockDatasheet:
        return MockDatasheet(datasheet_id)


class AirAgentAiTableLoader(BaseDataSourceLoader):
    table_cls = Apitable

    def __init__(self, setting: AiNodeAiTableSetting, is_predict: Optional[bool] = False):
        self.setting = setting
        self.is_predict = is_predict

    def lazy_load(self) -> Iterator[DataSource]:
        total_words = 0
        total_characters = 0
        total_tokens = 0

        table: Apitable | MockApiTable = self.table_cls(self.setting.api_token)
        datasheet: Datasheet | MockDatasheet = table.datasheet(self.setting.datasheet_id)
        records: QuerySet = datasheet.records.all(viewId=self.setting.view_id)

        docs = []
        type_id = datasheet.get_views()[0].id
        for record in records:
            page_content = str(record.json())

            content_words_count, content_characters_count = count_words_and_characters(
                page_content
            )
            content_tokens_count = count_openai_tokens(page_content)

            record_id = record._id
            doc = Document(
                page_content=page_content,
                metadata={
                    "id": record_id,
                    "type_id": type_id,
                    "type": "aitable",
                    "source": "_".join([self.setting.datasheet_id, str(self.setting.view_id), record_id]),
                    "datasheet_id": self.setting.datasheet_id,
                    "view_id": self.setting.view_id,
                    "record_id": record_id,
                    # "suggestion": record,
                    "words": content_words_count,
                    "characters": content_characters_count,
                    "tokens": content_tokens_count,
                },
            )

            total_words += content_words_count
            total_characters += content_characters_count
            total_tokens += content_tokens_count

            docs.append(doc)

        fields = datasheet.fields.all()
        data_source = DataSource(
            type="aitable",
            type_id=type_id,
            count=len(records),
            fields=fields,
            # revision=0,
            words=total_words,
            characters=total_characters,
            tokens=total_tokens,
            documents=docs,
        )
        yield data_source
