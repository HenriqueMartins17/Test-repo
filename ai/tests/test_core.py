import asyncio
import logging
import os
from ai_shared.ai_setting.base_ai_setting import AISettingMode

import langchain
import pytest
from dotenv import load_dotenv

from ai_inference import utils
from ai_inference.agents import AgentFactory
from ai_inference.agents.chat import DEFAULT_CHAT_TEMPLATE, ChatAgent
from ai_inference.agents.qa import DEFAULT_QA_PROMPT
from ai_inference.inference_factory import InferenceFactory
from ai_shared.ai_setting import AIAgentType, AISettingFactory
from ai_shared.persist.ai_info import AIInfo
from ai_shared.persist.training_info import TrainingInfo, TrainingStatus
from ai_shared.ros import (
    DataSourceRO,
    OpenAIChatCompletionRO,
    OpenAIChatFunctionRO,
    OpenAIChatMessageRO,
)
from ai_shared.vector import Vector
from ai_trainers import Trainer
from ai_trainers.trainers.base import TrainCallback

langchain.verbose = True
load_dotenv()
logger = logging.getLogger()


def new_request_body():
    return OpenAIChatCompletionRO(
        model="gpt-3.5-turbo",
        messages=[OpenAIChatMessageRO(role="role", content="content")],
        functions=[
            OpenAIChatFunctionRO(name="function_name", description="function_description")
        ],
        function_call={"key": "value"},
        temperature=0,
        top_p=0,
        n=1,
        stream=False,
        stop=None,
        max_tokens=256,
        presence_penalty=0,
        frequency_penalty=0,
        logit_bias={},
    )


request = new_request_body()
chat_ai_setting = AISettingFactory.new(AIAgentType.CHAT, {})
qa_ai_setting = AISettingFactory.new(AIAgentType.QA, {})
qa_ai_setting.mode = AISettingMode.ADVANCED

creator_ai_setting = AISettingFactory.new(AIAgentType.CREATOR, {})


def test_not_exist_ai_info():
    assert AIInfo.exist("not_exist_ai_info") is False
    assert TrainingInfo.exist("not_exist_ai_info", "not_exist_training_info") is False


@pytest.mark.asyncio
async def test_train_mock_core_ai_predict():
    """Just predict exist AI model"""
    ai_id = "mock_core"
    trainer = Trainer.new(ai_id)
    data_sources = list(trainer.load_data_sources(is_predict=True))
    assert len(data_sources) > 0
    assert data_sources[0].documents[0].metadata["type"] == "mock"


@pytest.mark.asyncio
async def test_train_ai_szd9dSFU09Bg0Vf():
    ai_id = "ai_szd9dSFU09Bg0Vf_mock"
    trainer = Trainer.new(ai_id)
    train_result = await trainer.do_train()

    training_id = train_result.new_training_id
    new_training_info = await TrainingInfo.load_training_info(ai_id, training_id)
    assert new_training_info.training_id == training_id
    assert len(new_training_info.data_sources) > 0


@pytest.mark.asyncio
async def test_train_mock_core_do_predict():
    """Just predict exist AI model"""
    data_sources_ros = [
        DataSourceRO(type="datasheet", type_id="dst1CW9FDtwFhy5gp1_mock"),
        # DataSourceRO(type="datasheet", type_id="dst4sM41RFPBkTMgRB"),
    ]
    trainer = Trainer.new("REAL AI")
    data_sources = trainer.predict(data_sources_ros)
    i = 0
    for data_source in data_sources:
        assert data_source.words > 0
        assert data_source.characters > 0
        assert data_source.tokens > 0
        # assert data_source.documents[0].metadata["type"] == "datasheet"
        i += 1

    assert i > 0


@pytest.mark.asyncio
async def test_train_mock_core():
    ai_id = "mock_core"
    trainer = Trainer.new(ai_id)

    callback = TrainCallback()
    train_process_info_pack = await trainer.do_train([callback])

    while callback.done.value is False:
        await asyncio.sleep(1)
        logger.debug("waiting for training done")

    # after done, get info from callback
    assert callback.done.value is True
    assert callback.err.value is False
    callback_training_info = callback.training_info
    assert callback_training_info.ai_id == ai_id
    assert train_process_info_pack.new_training_id == callback_training_info.training_id
    assert callback_training_info.status == TrainingStatus.SUCCESS

    # get info from disk
    new_training_info = await TrainingInfo.load_training_info(
        ai_id, train_process_info_pack.new_training_id
    )
    assert new_training_info.ai_id == ai_id
    assert train_process_info_pack.new_training_id == new_training_info.training_id
    assert new_training_info.status == TrainingStatus.SUCCESS

    for ds in new_training_info.data_sources:
        assert ds.type_id == "mock"
        assert ds.type == "mock"
        assert ds.words > 0
        assert ds.characters > 0
        assert ds.tokens > 0

    # get AI Info again
    new_ai_info = await AIInfo.load_ai_info(ai_id)
    assert new_ai_info.current_training_id == train_process_info_pack.new_training_id

@pytest.mark.asyncio
async def test_core_vectordb_remove_duplications():
    # Filter same input questions
    ai_id = "mock_core"
    dup_question = "What is Airtable?"
    search_filter = {
        "source": {
            # not equal https://docs.trychroma.com/usage-guide#filtering-by-metadata
            "$ne": dup_question  # remove specify duplicated question
        }
    }
    vector_db = await Vector.load_vector_db_by_ai_id(ai_id=ai_id)
    search_result = vector_db.similarity_search_with_score(
        query=dup_question, k=10, filter=search_filter
    )

    for doc, _ in search_result:
        assert (
            doc.metadata["source"] != dup_question
        )  # duplicated question should be removed


@pytest.mark.asyncio
async def test_core_vectordb_with_score_threshold():
    # Filter same input questions
    ai_id = "mock_core"
    vector_db = await Vector.load_vector_db_by_ai_id(ai_id=ai_id)
    far_search_result = await vector_db.asimilarity_search_with_relevance_scores(
        query="vika", k=10, score_threshold=0.8  # not similar
    )
    assert len(far_search_result) == 0

    near_search_result = await vector_db.asimilarity_search_with_relevance_scores(
        query="What is APITable?", k=10, score_threshold=0.7  # more similar
    )
    assert len(near_search_result) == 3


@pytest.mark.asyncio
async def test_core_get_suggestions():
    ai_id = "mock_core"
    question = "What is APITable?"
    inference = InferenceFactory.new(ai_id)
    suggestions = await inference.get_suggestions(n=10, question=question)
    assert isinstance(suggestions, list)
    assert len(suggestions) <= 10
    for suggestion in suggestions:
        assert suggestion != question  # suggestions != question


@pytest.mark.asyncio
async def test_chat_agent_with_conversation():
    ai_id = "mock_core"
    request.conversation_id = "test_chat_agent_with_conversation"
    agent_with_conversation_id = AgentFactory.new(
        ai_id,
        chat_ai_setting,
        request,
        prompt_template=DEFAULT_CHAT_TEMPLATE,
        streaming=False,
        callbacks=None,
    )
    chat_1 = await agent_with_conversation_id.arun(
        "Hi, My name is Kelly. Please remember my name. I will ask you laster."
    )
    logger.debug(chat_1)
    assert "Kelly" in chat_1
    chat_2 = await agent_with_conversation_id.arun("What is my name?")
    logger.debug(chat_2)
    assert "Kelly" in chat_2


@pytest.mark.asyncio
async def test_chat_agent_with_conversation_and_stream():
    ai_id = "mock_core"
    request.conversation_id = "test_chat_agent_with_conversation_and_stream"
    agent_with_conversation_id = AgentFactory.new(
        ai_id,
        chat_ai_setting,
        request,
        DEFAULT_CHAT_TEMPLATE,
        True,
        None,
    )
    assert isinstance(agent_with_conversation_id, ChatAgent)

    chat_1 = ""
    async for s in await agent_with_conversation_id.astream(
        "Hi, My name is Kelly. Please remember my name. I will ask you later."
    ):
        chat_1 += s.content

    logger.debug(chat_1)
    assert "Hello" in chat_1 or "Hi" in chat_1

    chat_2 = ""
    async for s in await agent_with_conversation_id.astream("What is my name?"):
        chat_2 += s.content

    logger.debug(chat_2)
    assert "Kelly" in chat_2


@pytest.mark.asyncio
async def test_chat_agent_without_conversation():
    ai_id = "mock_core"
    request.conversation_id = ""
    agent_without_conversation = AgentFactory.new(
        ai_id,
        chat_ai_setting,
        request,
        DEFAULT_CHAT_TEMPLATE,
        False,
        None,
    )
    chat_3 = await agent_without_conversation.arun("Please tell me:  1+9=?")
    logger.debug(chat_3)
    assert "10" in chat_3

    # def test_chat_bot(self):
    #     from ai_server.inference.bots import BotFactory, BotSettingFactory, BotType
    #     ai_setting = BotSettingFactory.new(BotType.CHAT, {})
    #     bot = BotFactory.new("mock", BotType.CHAT, ai_setting, "gpt-3.5-turbo", {}, "conversation_id", False, None)


@pytest.mark.asyncio
async def test_qa_bot_with_conversation_no_stream():
    ai_id = "mock_core"

    # clean the conversations folder! for better
    import shutil

    ai_info = await AIInfo.load_ai_info(ai_id)
    training_info = await ai_info.get_training_info(ai_info.current_training_id)
    conversations_dir_path = training_info.get_conversation_dir_path()
    if os.path.exists(conversations_dir_path):
        shutil.rmtree(conversations_dir_path)

    request.conversation_id = "test_qa_bot_with_conversation"
    qa_agent_with_conversation_id = AgentFactory.new(
        ai_id,
        qa_ai_setting,
        request,
        DEFAULT_QA_PROMPT.format(idk="I don't know"),
        False,
        None,
    )

    chat_1 = await qa_agent_with_conversation_id.arun("What is APITable?")
    logger.debug(chat_1)
    assert "APITable is" in chat_1
    chat_2 = await qa_agent_with_conversation_id.arun(
        "What difference between Airtable and APITable?"
    )
    logger.debug(chat_2)
    assert "Airtable" in chat_2
    # assert("OK" in chat_2)
    # chat_2 = await bot_with_conversation_id.arun("What is my name?")
    # logger.debug(chat_2)
    # assert("Kelly" in chat_2)


@pytest.mark.asyncio
async def test_qa_agent_with_conversation_stream():
    ai_id = "mock_core"

    # clean the conversations folder! for better
    import shutil

    ai_info = await AIInfo.load_ai_info(ai_id)
    training_info = await ai_info.get_training_info(ai_info.current_training_id)
    conversations_dir_path = training_info.get_conversation_dir_path()
    if os.path.exists(conversations_dir_path):
        shutil.rmtree(conversations_dir_path)

    request.conversation_id = "test_qa_agent_with_conversation_stream"
    qa_agent_with_conversation_id = AgentFactory.new(
        ai_id, qa_ai_setting, request, DEFAULT_QA_PROMPT, True, None
    )

    chat_1 = await qa_agent_with_conversation_id.arun("What is APITable?")
    logger.debug(chat_1)
    assert "APITable is" in chat_1
    chat_2 = await qa_agent_with_conversation_id.arun(
        "What difference between Airtable and APITable?"
    )
    logger.debug(chat_2)
    assert "Airtable" in chat_2

    chat_3 = await qa_agent_with_conversation_id.arun("So, do you know APITable?")
    logger.debug(chat_3)
    assert "No" in chat_3 or "Yes" in chat_3

    # assert("OK" in chat_2)
    # chat_2 = await bot_with_conversation_id.arun("What is my name?")
    # logger.debug(chat_2)
    # assert("Kelly" in chat_2)


@pytest.mark.asyncio
async def test_qa_agent_with_custom_prompt():
    ai_id = "mock_core"
    request_body = new_request_body()

    custom_prompt_without_vars = """
    Here is a prompt without variables!
    """
    qa_agent_prompt_without_vars = AgentFactory.new(
        ai_id,
        qa_ai_setting,
        request_body,
        custom_prompt_without_vars,
        False,
        None,
    )
    chat_1 = await qa_agent_prompt_without_vars.arun("What is APITable?")
    assert "APITable" in chat_1

    custom_prompt_with_vars = """
    History: {history}

    Here's context:
    \n{context}  
    
    If I ask ou "Day day up", you should answer "Good good study". 
    If I don't question you "Day day up", you should answer "Confirm".

    Question: {input}
    """
    chat_agent_with_custom_prompt = AgentFactory.new(
        ai_id,
        qa_ai_setting,
        request_body,
        custom_prompt_with_vars,
        False,
        None,
    )

    chat_1 = await chat_agent_with_custom_prompt.arun("Day day up")
    assert "Good good study" in chat_1
    chat_2 = await chat_agent_with_custom_prompt.arun("Other things else")
    assert "Confirm" in chat_2


@pytest.mark.asyncio
async def test_chat_agent_with_custom_prompt():
    ai_id = "mock_core"
    request_body = new_request_body()

    custom_prompt_without_vars = """
    First, you should know that: 101+102=8888
    The sum of 101 and 102 is 8888.
    If human ask you 101+102=?, you should answer 8888.
    """
    chat_agent_without_vars_prompt = AgentFactory.new(
        ai_id,
        qa_ai_setting,
        request_body,
        custom_prompt_without_vars,
        False,
        None,
    )
    chat_1 = await chat_agent_without_vars_prompt.arun("101+102=?")
    assert "8888" in chat_1  # fallback to default prompt

    custom_prompt_with_vars = """
    First, you should know that: 101+102=8888
    The sum of 101 and 102 is 8888.
    If human ask you 101+102=?, you should answer 8888.

    Current conversation:
    {history}

    Human: {input}
    AI:
    """
    qa_agent_with_conversation_id = AgentFactory.new(
        ai_id,
        qa_ai_setting,
        request_body,
        custom_prompt_with_vars,
        False,
        None,
    )

    chat_1 = await qa_agent_with_conversation_id.arun("101+102=?")
    assert "8888" in chat_1


@pytest.mark.asyncio
async def test_vectordb_retriever_with_score_threshold():
    """
    Set the score_threshold higher, no documents will be hit
    """
    docs = await Vector.load_relevant_documents_from_vector_db(
        query="这个字符计算距离很远", ai_id="mock_core", training_id=None, score_threshold=0.99
    )
    assert len(docs) == 0


@pytest.mark.asyncio
async def test_creator_agent():
    ai_id = "mock_core"
    request_body = new_request_body()
    creator_agent = AgentFactory.new(
        ai_id,
        creator_ai_setting,
        request_body,
        None,
        False,
        None,
    )
    chat_1 = await creator_agent.arun("Please generate an article with 100 words, topic: What is AITable.ai?")
    assert len(chat_1) > 90
    assert "AITable" in chat_1  # fallback to default prompt

# @pytest.mark.asyncio
# async def test_train_mock_creator():
#     ai_id = "mock_creator"
#     trainer = Trainer.new(ai_id)

#     callback = TrainCallback()
#     train_process_info = trainer.train([callback])

#     while callback.done.value is False:
#         await asyncio.sleep(1)
#         logger.debug("waiting `mock_creator` for training done")

#     # after done, get info from callback
#     assert callback.done.value is True
#     assert callback.err.value is False
#     callback_training_info = callback.training_info
#     assert callback_training_info.ai_id == ai_id
#     assert train_process_info.new_training_id == callback_training_info.training_id
#     assert callback_training_info.status == TrainingStatus.SUCCESS

#     # get info from disk
#     new_training_info = TrainingInfo.load_training_info(
#         ai_id, train_process_info.new_training_id
#     )
#     assert new_training_info.ai_id == ai_id
#     assert train_process_info.new_training_id == new_training_info.training_id
#     assert new_training_info.status == TrainingStatus.SUCCESS

#     for ds in new_training_info.data_sources:
#         assert ds.type_id == "mock"
#         assert ds.type == "mock"
#         assert ds.words > 0
#         assert ds.characters > 0
#         assert ds.tokens > 0

#     # get AI Info again
#     new_ai_info = AIInfo.load_ai_info(ai_id)
#     assert new_ai_info.current_training_id == train_process_info.new_training_id