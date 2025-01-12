import json
import logging
import time
from typing import List

import langchain
from dotenv import load_dotenv

from ai_server.inference_api import AIInfoVO
from ai_shared.persist.data_source import DataSource
from ai_shared.ros import (
    DataSourceRO,
    OpenAIChatCompletionRO,
    OpenAIChatMessageRO,
    RequestSuggestionBody,
)
from ai_shared.vos import OpenAIChatCompletionVO, TrainingInfoVO
from client import test_client

langchain.verbose = True

load_dotenv()

logger = logging.getLogger()


def create_request_body(
    conversation_id: str, message: str, stream: bool
) -> OpenAIChatCompletionRO:
    return OpenAIChatCompletionRO(
        conversation_id=conversation_id,
        model="gpt-3.5-turbo",
        messages=[OpenAIChatMessageRO(role="user", content=message)],
        functions=[],
        function_call={},
        temperature=0.7,
        top_p=1,
        n=1,
        stream=stream,
        max_tokens=256,
        presence_penalty=0,
        frequency_penalty=0,
        logit_bias={},
    )


class TestMockAI:
    def test_api_train_type_chat(self, test_client):
        ai_id = "ai_K2q5p1rgcRcFgax_mock_chat"
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

    def test_api_inference_type_chat(self, test_client):
        ai_id = "ai_K2q5p1rgcRcFgax_mock_chat"
        conversation_id = "test"
        # train a bot in "chat" type, which means dst_ids can be empty
        body = create_request_body(conversation_id, "Tell me: 1+1=?", False)

        response = test_client.post(
            f"/ai/inference/{ai_id}/chat/completions",
            json=body.dict(),
        )

        assert response.status_code == 200
        result = response.json()
        result_vo = OpenAIChatCompletionVO(**result)

        assert result_vo.id is not None
        assert result_vo.object == "chat.completion"
        respond_content = result_vo.choices[0].message.content
        assert "2" in respond_content
        # assert result_vo.usage.total_tokens > 10
        # assert result_vo.usage.completion_tokens > 0
        assert result_vo.model == "gpt-3.5-turbo"

        assert len(result_vo.choices) == 1
        assert result_vo.choices[0].finish_reason == "stop"

    def test_train_qa_type(self):
        pass

    def test_ai_info(self, test_client):
        ai_id = "mock"
        while True:  # means it is training
            time.sleep(1)
            ai_info_res = test_client.get(
                f"/ai/inference/{ai_id}"
            )  # wait the AI to be trained
            assert ai_info_res.status_code == 200
            ai_info_json = ai_info_res.json()
            ai_info_data = ai_info_json["data"]
            ai_info_vo = AIInfoVO(**ai_info_data)

            locking_training_id = ai_info_vo.locking_training_id
            if not locking_training_id:
                assert ai_info_vo.current_training_info is not None
                assert "mock" in ai_info_vo.current_training_info.training_id
                break
            else:
                assert ai_info_vo.locking_training_info is not None
                assert "mock" in ai_info_vo.locking_training_info.training_id

            logger.warning(f"AI is training, locking_training_id: {locking_training_id}")

    def test_get_suggestions(self, test_client):
        ai_id = "mock"
        body = RequestSuggestionBody(n=1)
        response = test_client.post(
            f"/ai/inference/{ai_id}/suggestions", json=body.dict()
        )
        assert response.status_code == 200
        assert response.json() != {}
        logger.warning(f"Suggestions: {response.json()}")

        # without query body is OK.
        ai_id = "mock"
        response = test_client.post(f"/ai/inference/{ai_id}/suggestions")
        assert response.status_code == 200
        assert response.json() != {}
        logger.warning(f"Suggestions: {response.json()}")

    def test_api_mock_chat_completions(self, test_client):
        ai_id, conversation_id = "mock", "test"
        body = create_request_body(
            conversation_id, "Do you know APITable? Please answer 'Yes' or 'No'.", False
        )

        response = test_client.post(
            f"/ai/inference/{ai_id}/chat/completions",
            json=body.dict(),
        )
        assert response.status_code == 200
        result = response.json()

        assert result["object"] == "chat.completion"
        assert result["model"] == "gpt-3.5-turbo"
        assert len(result["choices"]) == 1
        assert result["choices"][0]["finish_reason"] == "stop"
        respond_content = result["choices"][0]["message"]["content"]

        valid_responses = ["Yes", "No", "sorry", "know", "APITable"]
        assert any(response in respond_content for response in valid_responses)

    def test_ai_id_trainings(self, test_client):
        ai_id = "mock"
        response = test_client.get(f"/ai/inference/{ai_id}/trainings")
        assert response.status_code == 200
        result = response.json()
        assert result["code"] == 200
        trainings_list: List[TrainingInfoVO] = []
        for training_data in result["data"]:
            training_info_vo = TrainingInfoVO(**training_data)
            trainings_list.append(training_info_vo)
        assert trainings_list[0].ai_id == ai_id

    def test_not_exist_ai_id_trainings(self, test_client):
        ai_id = "not_exist_really"
        response = test_client.get(f"/ai/inference/{ai_id}/trainings")
        assert response.status_code == 200
        result = response.json()
        assert result["code"] == 404

    def test_chat_completions_with_stream(self, test_client):
        ai_id, conversation_id = "mock", "test"
        body = create_request_body(conversation_id, "Are you ChatGPT?", True)
        assert body.model == "gpt-3.5-turbo"
        assert body.stream

        body_dict = body.dict(by_alias=True)
        assert body_dict["stream"] is True

        response = test_client.post(
            f"/ai/inference/{ai_id}/chat/completions",
            json=body.dict(),
        )
        assert response.status_code == 200

        assert "text/event-stream" in response.headers["content-Type"]

        stream_text = response.text
        stream_jsons = []
        for line in stream_text.split("\n"):
            if line.strip() == "" or "[DONE]" in line:
                continue
            # sse_starlette.sse:sse.py:289 ping: : ping - 2023-11-07 03:11:42.766075
            if not line.startswith("data: "):
                continue
            json_str = line.replace("data: ", "")
            json_obj = json.loads(json_str)
            stream_jsons.append(json_obj)

        assert len(stream_jsons) > 0
        for obj in stream_jsons:
            assert obj["conversation_id"] == conversation_id
            assert obj["object"] == "chat.completion.chunk"
            assert obj["model"] == "gpt-3.5-turbo"
            assert len(obj["choices"]) == 1
            assert isinstance(
                obj["choices"][0]["delta"]["content"], str
            )  # Here would be a empty string - ""
            assert obj["choices"][0]["finish_reason"] is None

    def test_chat_completions_with_exception(self, test_client):
        ai_id, conversation_id = "not_exist", "test"
        body = create_request_body(conversation_id, "Are you ChatGPT?", False)

        response = test_client.post(
            f"/ai/inference/{ai_id}/chat/completions",
            json=body.dict(),
        )
        assert response.status_code == 200
        result = response.json()
        assert result["code"] == 404
        assert "AiNotFoundError" in result["msg"]
        assert result["data"] is None
        assert "application/json" in response.headers["content-Type"]

    def test_chat_completions_stream_with_exception(self, test_client):
        ai_id, conversation_id = "not_exist", "test"
        body = create_request_body(conversation_id, "Are you ChatGPT?", True)

        response = test_client.post(
            f"/ai/inference/{ai_id}/chat/completions",
            json=body.dict(),
        )
        assert response.status_code == 200
        assert "application/json" in response.headers["Content-Type"]
        result = response.json()
        assert result["code"] == 404

    def test_query_chat_history_by_not_exist_conversation_id(self, test_client):
        ai_id, training_id, conversation_id = "mock", "not_exist", "not_exist"
        response = test_client.get(
            f"/ai/inference/{ai_id}/trainings/{training_id}/conversations/{conversation_id}"
        )
        assert response.status_code == 200

        result = response.json()
        assert result["code"] == 404
        assert result["data"] is None

    # def test_chat_bot(self):
    #     from ai_server.inference.bots import BotFactory, BotSettingFactory, BotType
    #     ai_setting = BotSettingFactory.new(BotType.CHAT, {})
    #     bot = BotFactory.new("mock", BotType.CHAT, ai_setting, "gpt-3.5-turbo", {}, "conversation_id", False, None)

    def test_chat_maximum_context_length_with_exception(self, test_client):
        # If the caht memory parameter k=20 is set, the bug will be repeated
        ai_id, conversation_id = "ai_fucDrsXdp9USxEu", "test"
        body = create_request_body(conversation_id, "你好?", True)
        response = test_client.post(
            f"/ai/inference/{ai_id}/chat/completions",
            json=body.dict(),
        )
        assert response.status_code == 200
        data = response.json()
        assert data["code"] == 404
        assert "AiNotFoundError" in data["msg"]

    def test_api_predict(self, test_client):
        """Just predict exist AI model"""
        data_sources_ros = [
            DataSourceRO(type="datasheet", type_id="dst1CW9FDtwFhy5gp1_mock").dict(),
            # DataSourceRO(type="datasheet", type_id="dst4sM41RFPBkTMgRB").dict(),
        ]
        response = test_client.post("/ai/trainers/predict", json=data_sources_ros)

        assert response.status_code == 200
        data = response.json()
        vos: List[DataSource] = []
        for d in data["data"]:
            vos.append(DataSource(**d))

        for vo in vos:
            logger.debug(vo)
            assert vo.characters > 0
            # assert vo.type == "datasheet"
            assert vo.tokens > 0

        response = test_client.post("/ai/trainers/predict")
        assert response.status_code == 422

        response = test_client.post(
            "/ai/trainers/predict",
            json=[],
        )
        assert response.status_code == 200
        data = response.json()
        assert 404 == data["code"]

def test_api_get_ai_setting(test_client):
    response = test_client.get("/ai/inference/mock/setting")
    assert response.status_code == 200
    res = response.json()
    data = res['data']
    logger.debug(data)
    assert data['data']['mode'] == 'wizard'
    assert data['JSONSchema']['title'] == "QA Agent Setting JSON Schema" # schema 
