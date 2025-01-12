import os
from typing import Iterator, Optional, AsyncIterator

from langchain.schema.embeddings import Embeddings
from loguru import logger

from ai_shared.databus import AiNode
from ai_shared.persist.data_source import DataSource
from ai_trainers.loaders.markdown_file_loader import MarkdownFileLoader
from ai_trainers.trainers.base import BaseRAGTrainer, TrainProcessInfo


class CopilotTrainer(BaseRAGTrainer):

    """
    Train AITable / APITable / Vika Help Center

    1. WebLoader -> https://help.vika.cn
    2. WebLoader -> https://help.apitable.com
    3. 训练结果 ./data/vika_copilot,  /.data/aitable_copilot
    """

    def __init__(self, markdown_paths) -> None:
        super().__init__("copilot")
        self.markdown_paths = markdown_paths

    async def get_ai_node_list(self) -> list[AiNode]:
        return [AiNode(id=101, aiId=self.ai_id)]

    def load_data_source(
            self,
            ai_node: Optional[AiNode] = None,
            unique_keys: Optional[set] = None,
            is_predict: bool = False,
    ) -> Optional[Iterator[DataSource]]:
        for path in self.markdown_paths:
            for root, dirs, files in os.walk(path):
                for file in files:
                    _, file_extension = os.path.splitext(file)
                    if file_extension == ".md":
                        markdown_path = os.path.join(root, file)
                        logger.debug(
                            "Start training markdown file: %s", markdown_path
                        )  # noqa: F821
                        loader = MarkdownFileLoader(markdown_path)
                        for data_source in loader.lazy_load():
                            # logger.error(data_source)
                            yield data_source

    def load_data_sources(self, ai_nodes: Optional[list[AiNode]] = None, is_predict: bool = False) -> Iterator[DataSource]:
        """
        load all markdown files in folders
        """
        for path in self.markdown_paths:
            for root, dirs, files in os.walk(path):
                for file in files:
                    _, file_extension = os.path.splitext(file)
                    if file_extension == ".md":
                        markdown_path = os.path.join(root, file)
                        logger.debug(
                            "Start training markdown file: %s", markdown_path
                        )  # noqa: F821
                        loader = MarkdownFileLoader(markdown_path)
                        for data_source in loader.lazy_load():
                            # logger.error(data_source)
                            yield data_source

