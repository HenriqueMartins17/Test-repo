from typing import Optional

from langchain.schema import Document
from langchain.schema.embeddings import Embeddings
from loguru import logger

from ai_shared.config import settings
from ai_shared.databus import AiNode
from ai_shared.llmodel import LlModel
from ai_shared.persist import DataSource, TrainingInfo
from ai_shared.vector import Vector


class DataSourceProcessor:
    """docs -> embeddings, AiInfo and TrainingInfo"""

    @staticmethod
    def create_empty_document() -> Document:
        return Document(
            page_content="",
            metadata={},
        )

    @staticmethod
    async def embedding(
            training_persist_path: str,
            embedding_model: str,
            docs: list[Document],
            collection_name="vika",
            verbose=False,
    ):
        """
        Do Embedding via the LangChain Documents
        """
        if len(docs) == 0:
            # if empty docs, make sure there's on in it to avoid error, because langchain chromaDB has a code "if isinstance(target[0]..."
            docs.append(DataSourceProcessor.create_empty_document())

        embeddings = LlModel.get_embeddings(embedding_model=embedding_model)

        if verbose:
            logger.debug(f"Here's 3 of {len(docs)} docs sample: ")
            for doc in docs[:3]:
                logger.debug(f"doc sample: {doc}")

        vector_store = await Vector.anew_vector_db_with_docs(
            training_persist_path=training_persist_path,
            embeddings=embeddings,
            docs=docs,
            collection_name=collection_name,
            verbose=verbose,
        )
        return vector_store

    @staticmethod
    async def add_docs(ai_id: str, training_id: str, embedding_model: str, ds: DataSource):
        # docs: list[Document]
        """collection add docs"""
        embedding = LlModel.get_embeddings(embedding_model=embedding_model)
        db = await Vector.get_db(ai_id=ai_id, training_id=training_id, embedding=embedding)
        verbose = settings.is_verbose()
        res = await db.aadd_documents(ds.documents, verbose=verbose)
        await Vector.db_persist(db)

        logger.info("persist save to db")
        if settings.is_verbose():
            count = await Vector.db_count(db)
            logger.debug(count)

        if settings.is_verbose():
            result = await Vector.get_docs_by_training(ai_id=ai_id, training_id=training_id, embedding=embedding, ds=ds)
            logger.debug(result)

        return res

    @staticmethod
    async def get_docs(ai_id: str, training_id: str, embedding_model: str, ds: DataSource) -> Optional[list[Document]]:
        embedding = LlModel.get_embeddings(embedding_model=embedding_model)
        result = await Vector.get_docs_by_training(ai_id=ai_id, training_id=training_id, embedding=embedding, ds=ds)
        return result

    @staticmethod
    async def delete_docs(ai_id: str, training_id: str, embedding_model: str, ds: DataSource):
        # docs: list[Document]
        """collection delete docs"""
        embedding = LlModel.get_embeddings(embedding_model=embedding_model)
        if settings.is_verbose():
            result = await Vector.get_docs_by_training(ai_id=ai_id, training_id=training_id, embedding=embedding, ds=ds)
            logger.debug(result)

        res = await Vector.delete_docs(ai_id=ai_id, training_id=training_id, embedding=embedding, ds=ds)

        if settings.is_verbose():
            result = await Vector.get_docs_by_training(ai_id=ai_id, training_id=training_id, embedding=embedding, ds=ds)
            logger.debug(result)
        return res

    @staticmethod
    async def query_docs(ai_id: str, ai_nodes: list[AiNode], query: str, **query_kwargs) -> list[Document]:
        docs = await Vector.search_docs_by_ai(ai_id=ai_id, ai_nodes=ai_nodes, query=query, **query_kwargs)
        if not docs:
            return []
        return docs

    @staticmethod
    async def peek_sample_docs(ai_id: str, ai_nodes: list[AiNode]) -> list[Document]:
        docs = await Vector.peek_docs_by_ai(ai_id=ai_id, ai_nodes=ai_nodes)
        if not docs:
            return []
        return docs
