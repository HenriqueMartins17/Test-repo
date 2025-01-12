"""
This script is used to build the AI Copilot to .data/copilot
"""
import asyncio

from ai_trainers.trainer_factory import TrainerFactory
from ai_trainers.trainers.copilot import CopilotTrainer

from .config import markdown_paths


if __name__ == "__main__":
  print("Start build the VikaAI Copilot")
  trainer: CopilotTrainer = CopilotTrainer(markdown_paths)
  asyncio.run(trainer.do_train())

