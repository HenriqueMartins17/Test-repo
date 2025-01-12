import logging
import uuid

import pytest

from ai_inference import utils
from ai_shared.persist.training_info import TrainingInfo
from ai_shared.vector import Vector
from ai_trainers.trainers.copilot import CopilotTrainer
from client import test_client

logger = logging.getLogger()


class TestCopilot:
    @pytest.mark.asyncio
    async def test_copilot_train(self, test_client):
        markdown_paths = ["./docs"]  # only train ai/ readme for test
        trainer: CopilotTrainer = CopilotTrainer(markdown_paths)
        train_result = await trainer.do_train()
        training_info = await TrainingInfo.load_training_info(
            train_result.ai_id, train_result.new_training_id
        )

        logger.debug(training_info.data_sources)

        db = await Vector.load_vector_db_by_ai_id(train_result.ai_id)  # chroma db
        assert db

        assert training_info.training_id == train_result.new_training_id
        assert training_info.ai_id == train_result.ai_id
        assert len(training_info.data_sources) > 0

    # def test_copilot_inference_help(self, test_client):
    #     data = {
    #         "assistant_type": "help",
    #         "conversation_id": str(uuid.uuid4()),
    #         "messages": [
    #             {
    #                 "content": "What is APITable?"
    #             }
    #         ]
    #     }
    #     res = test_client.post("/ai/copilot/chat/completions", json=data)
    #     assert res.status_code == 200

    def test_copilot_inference_data(self, test_client):
        data = {
            "assistant_type": "data",
            "conversation_id": str(uuid.uuid4()),
            "messages": [
                {
                    "content": "What is APITable?"
                }
            ]
        }
        res = test_client.post("/ai/copilot/chat/completions", json=data)
        assert res.status_code == 200

    def test_talk_to_markdown_content(self):
        pass

    def test_talk_to_pdf_content(self):
        pass

    def test_talk_to_datasheet(self):
        pass

    def test_talk_to_datasheet_actions(self):
        pass
