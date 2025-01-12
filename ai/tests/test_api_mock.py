import logging
import os
import time

import langchain
import pytest
from dotenv import load_dotenv
from fastapi.testclient import TestClient

from ai_server.app import app
from ai_shared.persist.ai_info import AIInfo

# global
last_train_id: str

logger = logging.getLogger()

test_client = TestClient(app)

langchain.verbose = True

load_dotenv()


def test_api_mock_train_predict():
    response = test_client.post("/ai/trainers/mock/train/predict")
    assert response.status_code == 200
    res = response.json()
    assert res["data"][0]["type"] == "mock"
    assert res["data"][0]["words"] == 999


def test_api_mock_train():
    response = test_client.post("/ai/trainers/mock/train")
    assert response.status_code == 200
    res = response.json()

    global last_train_id
    last_train_id = res["data"]["new_training_id"]

    assert "mock" in last_train_id
    assert res["code"] == 200


def test_api_mock_get_ai_trainings_if_training_id_exist_but_empty_yet():
    ai_id = "mock"
    training_id = "exist_but_empty"
    path = os.path.dirname(f".data/{ai_id}/{training_id}/")
    if not os.path.exists(path):
        os.makedirs(path, exist_ok=True)
    file = f".data/{ai_id}/{training_id}/info.json"
    with open(file, "w", encoding="utf-8") as f:
        f.write("")
    response = test_client.get(f"/ai/trainers/{ai_id}/trainings/{training_id}")
    assert response.status_code == 200
    res = response.json()
    logger.debug(res)
    assert res["code"] == 404


def test_api_mock_chat_completion_with_conversation_id():
    ai_id, conversation_id = "mock", "test"
    body = {
        "conversation_id": conversation_id,
        "model": "gpt-3.5-turbo",
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
    assert result["model"] == "gpt-3.5-turbo"
    assert len(result["choices"]) == 1
    assert len(result["choices"][0]["message"]["content"]) != 0

@pytest.mark.asyncio
async def test_api_mock_query_chat_history_by_conversation_id():
    logger.debug("Waiting for conversation save....")
    time.sleep(3)
    ai_id = "mock"
    ai_info = await AIInfo.load_ai_info(ai_id)
    current_training_id = ai_info.current_training_id
    conversation_id = "test"

    global last_train_id
    assert last_train_id == current_training_id

    response = test_client.get(
        f"/ai/inference/{ai_id}/trainings/{current_training_id}/conversations/{conversation_id}"
    )
    assert response.status_code == 200

    result = response.json()

    if result["code"] != 200:
        logger.warning("!!!!!!!!!" + result["msg"])

    assert result["code"] == 200
    assert len(result["data"]) > 0


def test_query_current_chat_history():
    logger.debug("Waiting for conversation save....")
    time.sleep(3)  # 上一个chat completion还没写入conversation可能，所以这里等一下
    ai_id = "mock"
    conversation_id = "test"

    response = test_client.get(f"/ai/inference/{ai_id}/conversations/{conversation_id}")
    assert response.status_code == 200
    result = response.json()
    assert result["code"] == 200
    assert len(result["data"]) > 0
