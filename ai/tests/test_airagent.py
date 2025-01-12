

import logging

import pytest

from ai_inference import utils
from ai_shared.config import Env, settings
from ai_shared.persist.training_info import TrainingInfo
from ai_shared.vector import Vector
from ai_trainers.loaders.airagent.airtable import MockAirTable, AirAgentAirTableLoader
from ai_trainers.trainers.airagent import AirAgentTrainer
from client import test_client

logger = logging.getLogger()


class TestCopilot:
    ...
    # @pytest.mark.asyncio
    # async def test_airagent_train(self, test_client):
    #     with settings.set_env_airagent():
    #         AirAgentAirTableLoader.table_cls = MockAirTable
    #
    #         ai_id = "ag_mock"
    #         trainer: AirAgentTrainer = AirAgentTrainer(ai_id=ai_id)
    #         train_result = await trainer.do_train()
    #         training_info = await TrainingInfo.load_training_info(
    #             train_result.ai_id, train_result.new_training_id
    #         )
    #
    #         logger.debug(training_info.data_sources)
    #
    #         db = await Vector.load_vector_db_by_ai_id(train_result.ai_id)  # chroma db
    #         assert db
    #
    #         assert training_info.training_id == train_result.new_training_id
    #         assert training_info.ai_id == train_result.ai_id
    #         assert len(training_info.data_sources) > 0