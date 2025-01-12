from typing import Iterator, Optional, Any

from langchain_community.document_loaders import AirtableLoader
from langchain.schema import Document

from ai_shared.databus.ai_po import AiNodeAirTableSetting
from ai_shared.persist import DataSource
from pyairtable import Table

from ai_shared.persist.data_source import count_words_and_characters, count_openai_tokens


class MockAirTable:

    def __init__(
            self,
            api_token: str,
            base_id: str,
            table_name: str,
            **kwargs: Any,
    ):
        self.api_token = api_token
        self.base_id = base_id
        self.table_name = table_name

    def __repr__(self) -> str:
        return f"<Table base_id={self.base_id!r} table_name={self.table_name!r}>"

    def _mock_record(self):
        return {
            'id': 'recAdw9EjV90xbW',
            'createdTime': '2023-05-22T21:24:15.333134Z',
            'fields': {'Name': 'Alice', 'Department': 'Engineering'}
        }

    def all(self):
        return [self._mock_record()]

    def first(self):
        return self._mock_record()


class AirAgentAirTableLoader(AirtableLoader):
    table_cls = Table

    def __init__(self, setting: AiNodeAirTableSetting, is_predict: Optional[bool] = False):
        self.setting = setting
        self.is_predict = is_predict
        super().__init__(setting.api_token, setting.table_id, setting.base_id)

    def lazy_load(self) -> Iterator[DataSource]:
        total_words = 0
        total_characters = 0
        total_tokens = 0

        table = self.table_cls(self.api_token, self.base_id, self.table_id)
        records = table.all()

        type_id = self.table_id
        docs = []
        for record in records:
            page_content = str(record)

            content_words_count, content_characters_count = count_words_and_characters(
                page_content
            )
            content_tokens_count = count_openai_tokens(page_content)

            record_id = record.get("id")
            # fields = record.get("fields")
            doc = Document(
                page_content=page_content,
                metadata={
                    "id": record_id,
                    "type": "airtable",
                    "type_id": type_id,  # data_source
                    "source": "_".join([self.base_id, self.table_id, record_id]),
                    "base_id": self.base_id,
                    "table_id": self.table_id,
                    "record_id": record_id,
                    # "suggestion": list(fields.values())[0],
                    "words": content_words_count,
                    "characters": content_characters_count,
                    "tokens": content_tokens_count,
                },
            )

            total_words += content_words_count
            total_characters += content_characters_count
            total_tokens += content_tokens_count

            docs.append(doc)

        first = table.first()
        fields = []
        if first:
            fields = first.keys()
        data_source = DataSource(
            type="airtable",
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
