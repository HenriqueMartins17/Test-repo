from typing import Optional

from ai_shared.config import settings
from ai_shared.ros import TrainerAiNodes
from ai_trainers.trainers.apitable import APITableTrainer
from ai_trainers.trainers.base import BaseRAGTrainer
from ai_trainers.trainers.copilot import CopilotTrainer
from .trainers.airagent import AirAgentTrainer

from .trainers.mock import MockTrainer


class TrainerFactory:
    """
    This is a `trainer` factory or manager
    """

    @staticmethod
    def new(ai_id: str):
        trainer: BaseRAGTrainer
        if settings.env.is_airagent():
            trainer = AirAgentTrainer(ai_id)
        else:
            # aitable & vika
            if "mock" in ai_id:
                trainer = MockTrainer(ai_id)
            elif "copilot" in ai_id:
                trainer = CopilotTrainer(ai_id)
            else:
                trainer = APITableTrainer(ai_id)
        return trainer
