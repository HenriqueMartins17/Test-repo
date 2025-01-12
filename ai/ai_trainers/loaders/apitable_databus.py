"""
APITable Python SDK Loader
"""
from typing import Iterator, List, Optional

from langchain.docstore.document import Document

from ai_shared.ai_setting import AIAgentType
from ai_shared.config import settings
from ai_shared.databus import DataBus
from ai_shared.databus.ai_po import AiNodeDatasheetSetting
from ai_shared.databus.record import Record
from ai_shared.persist.data_source import (
    DatasheetDataSource,
    DataSource,
    count_openai_tokens,
    count_words_and_characters,
)
from ai_shared.ros import DataSourceRO
from ai_shared.send_task import SendTask
from ai_shared.types import FieldType
from ai_trainers.loaders.attachments_field_loader import AttachmentFieldLoader
from . import loader_utils

from .base import BaseDataSourceLoader
from .url_field_loader import UrlFieldLoader


class APITableDataBusLoader(BaseDataSourceLoader):
    """Load DataSource from remote APITable database."""

    def __init__(self, setting: AiNodeDatasheetSetting, unique_keys: set, is_predict: bool = False):
        super().__init__()
        self.setting = setting
        self.unique_keys = unique_keys
        self.is_predict = is_predict

    @staticmethod
    def load_data_sources_from_datasheet(
            dst_id: str,
            view_id: Optional[str],
            unique_keys: set,
            is_predict: bool = False,
    ) -> Iterator[DataSource]:
        dst = DataBus.get_datasheet(dst_id=dst_id, view_id=view_id)

        records: list[Record] = dst.get_records()
        skip_field_types = [
            FieldType.Member, 
            FieldType.Link, 
            FieldType.Formula, 
            FieldType.OneWayLink, 
            FieldType.LookUp, 
            FieldType.WorkDoc,
            FieldType.Button
        ]

        docs = []
        type_id = view_id or dst.get_default_view_id()
        view = dst.get_view_by_id(type_id)

        sort_field_ids = []
        for column in view.get("columns", []):
            sort_field_ids.append(column["fieldId"])

        for record in records:
            page_content = record.to_string(skip_field_types, sort_field_ids)

            doc = Document(
                page_content=page_content,
                metadata={
                    "id": record.id,
                    "type_id": type_id,  # data_source
                    "source": f"/{dst_id}/{type_id}/{record.id}",
                    "type": "datasheet",
                    "datasheet_id": dst_id,
                    "record_id": record.id
                },
            )
            docs.append(doc)

        # todo: use embedding model
        if settings.edition.is_vika_saas():
            text_splitter = loader_utils.get_text_splitter()
            all_splits = text_splitter.split_documents(docs)
        else:
            all_splits = docs

        total_words = 0
        total_chars = 0
        total_tokens = 0
        for doc in all_splits:
            # words, characters, tokens count
            words, chars = count_words_and_characters(doc.page_content)
            tokens = count_openai_tokens(doc.page_content)
            total_words += words
            total_chars += chars
            total_tokens += tokens

            doc.metadata.update({
                "words": words,
                "characters": chars,
                "tokens": tokens,
            })

        data_source = DatasheetDataSource(
            type="datasheet",
            type_id=type_id,
            count=len(records),
            fields=dst.get_fields(),
            revision=dst.get_revision(),
            words=total_words,
            characters=total_chars,
            tokens=total_tokens,
            documents=all_splits,
        )
        yield data_source

        # No need to calculate except Datasheet itself
        if not is_predict:
            for record in records:
                # Attachments Field
                field_data_list = record.get_field_data_list_by_field_type(
                    kind=FieldType.Attachment
                )
                for field_data in field_data_list:
                    for attach_data in field_data:
                        file_token = attach_data.get("token", "")
                        if not file_token:
                            continue
                        hash_token = file_token.split("/")[-1]
                        if not hash_token:
                            continue
                        if hash_token in unique_keys:
                            continue
                        unique_keys.add(hash_token)

                        for ds in SendTask.send_load_attachment(attach_data):
                            yield ds

                # URL Field
                field_data_list = record.get_field_data_list_by_field_type(kind=FieldType.URL)

                for field_data in field_data_list:
                    for url_data in field_data:
                        url = url_data.get("text", "")
                        if not url:
                            continue
                        if url in unique_keys:
                            continue
                        unique_keys.add(url)

                        for ds in SendTask.send_load_url(url_data):
                            yield ds

    def lazy_load(self) -> Iterator[DataSource]:
        """Lazy load records from datasheets."""
        for data_source in APITableDataBusLoader.load_data_sources_from_datasheet(
                dst_id=self.setting.datasheet_id,
                view_id=self.setting.view_id,
                unique_keys=self.unique_keys,
                is_predict=self.is_predict,
        ):
            yield data_source

    def load(self) -> List[DataSource]:
        return list(self.lazy_load())
