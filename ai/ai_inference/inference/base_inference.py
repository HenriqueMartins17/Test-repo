
from loguru import logger

from ai_shared.vos import InferenceUsageVO


class BaseInference:
    """
    Base Inference class for all chat query
    """

    def __init__(self, ai_id: str):
        self.ai_id = ai_id

    async def get_response(self, message: str):
        raise NotImplementedError(message)

    def on_finished_inference(self, inference_result: InferenceUsageVO):
        logger.debug(f"""Inference Result: {inference_result}""")
