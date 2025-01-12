import logging

import langchain

from dotenv import load_dotenv
from fastapi.testclient import TestClient

from ai_shared.llmodel import ModelNameOpenAI, ModelNameBaiduQianFan
from client import test_client


logger = logging.getLogger()

langchain.verbose = True

load_dotenv()

"""
mock ai train to make ai info in .data
mock ai chat tests
"""


def test_llm_baidu_qianfang_ai_train(test_client: TestClient):
    from ai_shared.config import settings
    with settings.set_edition_vika_saas():
        mock_baidu_ai_ids = [
            "mock_baidu_embedding_baidu_chat",
            "mock_openai_embedding_openai_chat",
        ]
        for ai_id in mock_baidu_ai_ids:
            # train a bot in "chat" type, which means dst_ids can be empty
            res = test_client.post(f"/ai/trainers/{ai_id}/train")
            assert res.status_code == 200
            res_data = res.json()["data"]
            this_training_id = res_data["new_training_id"]
            assert len(this_training_id) > 0

            ai_agent_type = res_data.get("ai_agent_type")
            assert ai_agent_type == "chat"
            # ai_model = res_data.get("ai_model")
            # assert ai_model["model"] == "gpt-3.5-turbo"
            # ai_setting = res_data.get("ai_setting")
            # assert ai_setting[""]
            ai_info = res_data.get("ai_info")
            assert ai_info.get("ai_id") == ai_id


def test_llm_baidu_qianfan_chat_completion_with_conversation_id(test_client: TestClient):
    """
    chat model provider: baidu
    embedding model provider: baidu
    stream: False
    """
    from ai_shared.config import settings
    with settings.set_edition_vika_saas():
        ai_id = "mock_baidu_embedding_baidu_chat"
        conversation_id = "test"
        body = {
            "conversation_id": conversation_id,
            "model": ModelNameBaiduQianFan.ERNIE_BOT_TURBO.value,
            "messages": [{"role": "user", "content": "Do you know APITable?"}],
            "functions": [],
            "function_call": {},
            "temperature": 0.7,
            "top_p": 1,
            "n": 1,
            "stream": False,
            "max_tokens": 256,
            "presence_penalty": 0,
            "frequency_penalty": 0,
            "logit_bias": {},
        }

        response = test_client.post(
            f"/ai/inference/{ai_id}/chat/completions",
            json=body,
        )
        assert response.status_code == 200
        result = response.json()
        assert result["conversation_id"] == conversation_id

        if "object" not in result:
            logger.warning("!!!!!!!!!no 'object':" + result)
        assert result["object"] == "chat.completion"
        assert result["model"] == ModelNameBaiduQianFan.ERNIE_BOT_TURBO.value
        assert len(result["choices"]) == 1
        assert len(result["choices"][0]["message"]["content"]) != 0


def test_vika_old_openai_chat_completion_with_conversation_id(test_client: TestClient):
    """
    chat model provider: openai
    embedding model provider: openai
    stream: False
    """
    from ai_shared.config import settings
    with settings.set_edition_vika_saas():
        ai_id = "mock_openai_embedding_openai_chat"
        conversation_id = "test"
        body = {
            "conversation_id": conversation_id,
            # "model": ModelNameBaiduQianFan.ERNIE_BOT_TURBO.value,
            "messages": [{"role": "user", "content": "Do you know APITable?"}],
            "functions": [],
            "function_call": {},
            "temperature": 0.7,
            "top_p": 1,
            "n": 1,
            "stream": False,
            "max_tokens": 256,
            "presence_penalty": 0,
            "frequency_penalty": 0,
            "logit_bias": {},
        }

        response = test_client.post(
            f"/ai/inference/{ai_id}/chat/completions",
            json=body,
        )
        assert response.status_code == 200
        result = response.json()
        assert result["conversation_id"] == conversation_id

        if "object" not in result:
            logger.warning("!!!!!!!!!no 'object':" + result)
        assert result["object"] == "chat.completion"
        assert result["model"] == ModelNameOpenAI.GPT_3_5_TURBO.value
        assert len(result["choices"]) == 1
        assert len(result["choices"][0]["message"]["content"]) != 0

