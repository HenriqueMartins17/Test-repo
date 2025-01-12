import asyncio
import json
from typing import Any, List

from ai_inference.inference.base_inference import BaseInference


class EchoInference(BaseInference):
    """Echo inference for testing purposes"""

    async def get_response(self, message):
        return f"{message} -- by VIKA AI"

    async def get_stream(self, conversation_id: str, message: str):
        async def block():
            await asyncio.sleep(0.5)

        data = json.dumps({"conversation_id": conversation_id, "message": message})
        yield f"data: {data}\n\n"

        task = asyncio.create_task(block())
        await task

    def get_conversation_histories(self, conversation_id: str) -> List[Any]:
        return [
            {
                "data": {
                    "additional_kwargs": {},
                    "content": "Hi! ChatGPT",
                    "example": False,
                },
                "type": "human",
            },
            {
                "data": {
                    "additional_kwargs": {},
                    "content": " Hi! I'm ChatGPT. I'm here to help answer your questions. What can I help you with?",
                    "example": False,
                },
                "type": "ai",
            },
        ]
