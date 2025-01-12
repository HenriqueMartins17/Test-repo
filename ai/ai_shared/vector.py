import asyncio
from typing import Tuple, List, Optional

from chromadb.api import IDs, GetResult
from chromadb.types import Where
from chromadb.types import WhereDocument
from langchain.schema import Document
from langchain.schema.embeddings import Embeddings
from langchain_community.vectorstores import Chroma
from loguru import logger

from ai_shared.databus import AiNode
from ai_shared.exceptions import AiNotFoundError, NotTrainedError, TrainingNotFoundError
from ai_shared.llmodel import LlModel
from ai_shared.persist import TrainingInfo, DataSource
from ai_shared.persist.ai_info import AIInfo
from ai_shared.tracking import Tracking


class Vector:

    @staticmethod
    async def _get_latest_training_info_list(ai_id: str, ai_nodes: list[AiNode]) -> list[TrainingInfo]:
        ai_info = await AIInfo.load_ai_info(ai_id)
        if not ai_info:
            raise AiNotFoundError(f"ai_id: {ai_id}")

        training_info_list = []
        unique_training_ids = set()
        training_ids = ai_info.get_training_ids_latest(ai_nodes)
        for training_id in training_ids:
            if training_id in unique_training_ids:
                continue
            training_info = await TrainingInfo.load_training_info(ai_id, training_id)
            if not training_info:
                continue
            training_info_list.append(training_info)
            unique_training_ids.add(training_id)
        return training_info_list

    @staticmethod
    async def search_docs_by_ai(ai_id: str, ai_nodes: list[AiNode], query: str, **query_kwargs) -> list[Document]:
        training_info_list = await Vector._get_latest_training_info_list(ai_id=ai_id, ai_nodes=ai_nodes)
        logger.debug(f"{[t.training_id for t in training_info_list]}")
        if not training_info_list:
            return []

        coro_list = []
        async with asyncio.TaskGroup() as tg:
            for training_info in training_info_list:
                coro = tg.create_task(Vector._search_docs_by_training(training_info, query=query, **query_kwargs))
                coro_list.append(coro)

        results: tuple[List[Tuple[Document, float]]] = await asyncio.gather(*coro_list)
        all_docs_with_score = []
        for docs_with_score in results:
            for doc_with_score in docs_with_score:
                all_docs_with_score.append(doc_with_score)

        all_docs_with_score = sorted(all_docs_with_score, key=lambda item: item[1], reverse=True)
        docs = [t[0] for t in all_docs_with_score]
        return docs[:10]

    @staticmethod
    async def _search_docs_by_training(training_info: TrainingInfo, query: str, **query_kwargs) -> list[Tuple[Document, float]]:
        embedding_model = training_info.get_embedding_model()
        embedding = LlModel.get_embeddings(embedding_model=embedding_model)
        vector_db = await Vector.get_db(ai_id=training_info.ai_id, training_id=training_info.training_id, embedding=embedding)
        docs_with_score = await vector_db.asimilarity_search_with_relevance_scores(query=query, **query_kwargs)
        logger.debug(f"{docs_with_score[:3]=}")
        return docs_with_score

    @staticmethod
    async def peek_docs_by_ai(ai_id: str, ai_nodes: list[AiNode]) -> Optional[list[Document]]:
        training_info_list = await Vector._get_latest_training_info_list(ai_id=ai_id, ai_nodes=ai_nodes)
        if not training_info_list:
            return None

        coro_list = []
        async with asyncio.TaskGroup() as tg:
            for training_info in training_info_list:
                coro = tg.create_task(Vector._peek_docs_by_training(training_info))
                coro_list.append(coro)

        results: tuple[Optional[list[Document]]] = await asyncio.gather(*coro_list)
        all_docs = []
        for documents in results:
            if not documents:
                continue

            for d in documents:
                all_docs.append(d)

        return all_docs

    @staticmethod
    async def _peek_docs_by_training(training_info: TrainingInfo) -> Optional[list[Document]]:
        try:
            embedding_model = training_info.get_embedding_model()
            embedding = LlModel.get_embeddings(embedding_model=embedding_model)
            vector_db = await Vector.get_db(ai_id=training_info.ai_id, training_id=training_info.training_id, embedding=embedding)
            result: GetResult = await Vector._peek_docs(vector_db, limit=10)
            docs = []
            for idx, d in enumerate(result.get("documents", [])):
                if not d:
                    continue
                metadata = result.get("metadatas", [])[idx]
                if metadata is None:
                    continue
                doc = Document(page_content=d, metadata=metadata)
                docs.append(doc)
            return docs
        except Exception as e:
            Tracking.capture_exception(e)
            logger.error(f"_peek_docs_by_training error: {e=}")
            return None

    @staticmethod
    async def _peek_docs(db: Chroma, limit: int) -> GetResult:
        return await asyncio.get_running_loop().run_in_executor(
            None, db._collection.peek, limit
        )

    @staticmethod
    async def _get_docs(db: Chroma, where: Where) -> GetResult:
        return await asyncio.get_running_loop().run_in_executor(
            None, lambda: db._collection.get(where=where)
        )
    @staticmethod
    async def get_docs_by_training(ai_id: str, training_id: str, embedding: Embeddings, ds: DataSource) -> Optional[list[Document]]:
        try:
            db = await Vector.get_db(ai_id, training_id, embedding)
            where = {"type_id": ds.type_id}
            result = await Vector._get_docs(db, where=where)
            docs = []
            for idx, d in enumerate(result.get("documents", [])):
                if not d:
                    continue
                metadata = result.get("metadatas", [])[idx]
                if metadata is None:
                    continue
                doc = Document(page_content=d, metadata=metadata)
                docs.append(doc)
            return docs
        except Exception as e:
            Tracking.capture_exception(e)
            logger.error(f"get_docs_by_training error: {e=}")
            return None

    @staticmethod
    async def delete_docs(
        ai_id: str,
        training_id: str,
        embedding: Embeddings,
        ds: DataSource,
    ) -> None:
        db = await Vector.get_db(ai_id=ai_id, training_id=training_id, embedding=embedding)
        where = {"type_id": ds.type_id}
        res = await Vector._delete_docs(db=db, where=where)
        return res

    @staticmethod
    async def _delete_docs(db: Chroma, where: Where):
        return await asyncio.get_running_loop().run_in_executor(
            None, lambda: db._collection.delete(where=where)
        )

    @staticmethod
    def get_vector_db(persist_path: str, embedding: Embeddings) -> Chroma:
        return Chroma(
            persist_directory=persist_path,
            embedding_function=embedding,
            collection_name="vika",
            collection_metadata={"hnsw:space": "cosine"},  # l2 is the default
        )

    @staticmethod
    async def aget_vector_db(persist_path: str, embedding: Embeddings) -> Chroma:
        return await asyncio.get_running_loop().run_in_executor(
            None, Vector.get_vector_db, persist_path, embedding
        )

    @staticmethod
    async def get_db(ai_id: str, training_id: str, embedding: Embeddings):
        """get or create collection"""
        persist_path = TrainingInfo.make_training_path(ai_id=ai_id, training_id=training_id)
        db = await Vector.aget_vector_db(persist_path=persist_path, embedding=embedding)
        logger.debug(f"{db._collection}")
        return db

    @staticmethod
    async def db_persist(db: Chroma) -> None:
        return await asyncio.get_running_loop().run_in_executor(
            None, db.persist,
        )

    @staticmethod
    async def db_count(db: Chroma) -> None:
        return await asyncio.get_running_loop().run_in_executor(
            None, db._collection.count,
        )

    @staticmethod
    def new_vector_db_with_docs(
            training_persist_path: str,
            embeddings: Embeddings,
            docs: list[Document],
            collection_name: str,
            verbose=False,
    ) -> Chroma:
        logger.debug(f"Start create vector store at `{training_persist_path}`")
        # Set up a vector store used to save the vector embeddings. Here we use Chrome local file as the vector store
        vector_store = Chroma.from_documents(
            docs,
            embedding=embeddings,
            persist_directory=training_persist_path,
            collection_name=collection_name,
            collection_metadata={"hnsw:space": "cosine"},  # l2 is the default
        )

        if verbose:
            logger.debug(f"Save the vector store to disk: {training_persist_path}")

        vector_store.persist()  # save the vector store to disk now

        return vector_store

    @staticmethod
    async def anew_vector_db_with_docs(
            training_persist_path: str,
            embeddings: Embeddings,
            docs: list[Document],
            collection_name: str,
            verbose=False,
    ) -> Chroma:
        return await asyncio.get_running_loop().run_in_executor(
            None,
            Vector.new_vector_db_with_docs,
            training_persist_path,
            embeddings,
            docs,
            collection_name,
            verbose,
        )

    @staticmethod
    async def load_vector_db_by_ai_id(ai_id: str, training_id: str = None) -> Chroma:
        """
        Helper to load vector db
        """

        ai_info = await AIInfo.load_ai_info(ai_id)
        if not ai_info:
            raise AiNotFoundError(f"ai_id: {ai_id}")
        current_training_id = ai_info.current_training_id
        if not current_training_id:
            raise NotTrainedError(f"AI {ai_id} is not trained yet")

        if not training_id:  # read from arguments
            training_id = ai_info.current_training_id

        if not training_id:  # read from ai info
            raise NotTrainedError(f"AI {ai_id} is not trained yet")

        training_info = await ai_info.get_training_info(training_id)
        if not training_info:
            raise TrainingNotFoundError(f"AI {ai_id}, training_id: {training_id}")

        persist_path = training_info.get_training_path()
        logger.debug("Loading vector store from %s..." % persist_path)

        embedding_model = training_info.get_embedding_model()
        embedding = LlModel.get_embeddings(embedding_model=embedding_model)
        return Chroma(
            persist_directory=persist_path,
            embedding_function=embedding,
            collection_name="vika",
            collection_metadata={"hnsw:space": "cosine"},  # l2 is the default
        )

    @staticmethod
    async def get_retriever_from_vector_db(
            ai_id: str, training_id: str = None, score_threshold: float = 0.8
    ):
        """
        Get retriever from vector db
        """
        vector_db = await Vector.load_vector_db_by_ai_id(ai_id, training_id)
        retriever = vector_db.as_retriever(
            search_type="similarity_score_threshold",
            search_kwargs={"score_threshold": score_threshold},
        )
        return retriever

    @staticmethod
    async def load_relevant_documents_from_vector_db(
            query: str, ai_id: str, training_id: str = None, score_threshold: float = 0.8
    ):
        """
        Load relevant documents from vector db
        """
        retriever = await Vector.get_retriever_from_vector_db(ai_id, training_id, score_threshold)
        return await retriever.aget_relevant_documents(query)
