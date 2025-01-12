from typing import Iterator, List, Optional, AsyncIterator, AsyncGenerator

from langchain.docstore.document import Document
from langchain.schema.embeddings import Embeddings

from ai_shared.databus import AiNode
from ai_shared.persist import Persist

from .base import BaseRAGTrainer, DataSource, TrainProcessInfo


class MockTrainer(BaseRAGTrainer):

    async def get_ai_node_list(self) -> list[AiNode]:
        return [AiNode(id=100, aiId=self.ai_id)]

    def get_mock_documents(self):
        doc1 = Document(
            page_content="""
            {
                "question": "What is APITable?",
                "answer": "APITable is an incredibly simple and powerful work management OS. You'll be able to get started in just one second"
            }
            """,
            metadata={
                "source": "What is APITable?",
                "type": "mock",
                "suggestion": "What is APITable?",
                "words": 999,
                "characters": 999,
                "tokens": 999,
            },
        )
        
        doc2 = Document(
            page_content="""
            {
                "question": "What is Airtable?"
                "answer": "Airtable is a low‒code platform to build next‒gen apps. Move beyond rigid tools, operationalize your critical data, and reimagine workflows with AI."
            }
            """,
            metadata={
                "source": "What is Airtable?",
                "type": "mock",
                "suggestion": "What is Airtable?",
                "words": 999,
                "characters": 999,
                "tokens": 999,
            },
        )
        doc3 = Document(
            page_content="""
            {
                "question": "What is AITable.ai?",
                "answer": "AITable.ai is a platform that offers custom ChatGPT and AI Agents. It allows users to turn tables into AI chatbots with just one click, eliminating the need for coding or document uploads."
            }
            """, 
            metadata={
                "source": "What is AITable.ai?",
                "type": "mock",
                "suggestion": "What is AITable.ai?",
                "words": 999,
                "characters": 999,
                "tokens": 999,
            },
        )
        return [doc1, doc2, doc3]

    def load_data_source(
            self,
            ai_node: Optional[AiNode] = None,
            unique_keys: Optional[set] = None,
            is_predict: bool = False,
    ) -> Optional[Iterator[DataSource]]:
        docs = self.get_mock_documents()

        data_sources: List[DataSource] = []

        new_data_sources = DataSource(
            type="mock",
            type_id="mock",
            meta={},
            words=999,
            characters=999,
            tokens=999,
            documents=docs,
        )
        data_sources.append(new_data_sources)

        for ds in data_sources:
            yield ds

    def load_data_sources(self, ai_nodes: Optional[list[AiNode]] = None, is_predict: bool = False) -> Iterator[DataSource]:
        # ai_id = self.ai_id
        mock_csv_path = ".data/mock.csv"
        Persist.ensure_directory_exists(mock_csv_path)
        #         csv = """Q,A
        # What is APITable?,APITable is an incredibly simple and powerful work management OS. You'll be able to get started in just one second
        # What is Airtable?,"Airtable is a low‒code platform to build next‒gen apps. Move beyond rigid tools, operationalize your critical data, and reimagine workflows with AI."
        # """
        #         with open(mock_csv_path, "w", encoding="utf-8") as f:
        #             f.write(csv)

        #         loader = CSVLoader(file_path=mock_csv_path, source_column="Q")

        docs = self.get_mock_documents()

        data_sources: List[DataSource] = []

        new_data_sources = DataSource(
            type="mock",
            type_id="mock",
            meta={},
            words=999,
            characters=999,
            tokens=999,
            documents=docs,
        )
        data_sources.append(new_data_sources)

        for ds in data_sources:
            yield ds

