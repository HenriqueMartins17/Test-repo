from ai_shared.ai_setting import AIAgentType, AISettingFactory, QAAISetting, ChatAISetting
import logging
from ai_shared.ai_setting.base_ai_setting import AISettingMode
from jsonschema import validate, ValidationError
import pytest
logger = logging.getLogger()
from fastapi.testclient import TestClient
from ai_server.app import app
test_client = TestClient(app)

def test_ai_setting_empty_string():
    setting = AISettingFactory.new(agent_type=AIAgentType.QA, ai_setting_any="")
    assert isinstance(setting, QAAISetting)
    assert setting.mode == AISettingMode.WIZARD.value


def test_ai_setting_empty_dict():
    setting = AISettingFactory.new(agent_type=AIAgentType.QA, ai_setting_any={})
    assert isinstance(setting, QAAISetting)
    assert setting.mode == AISettingMode.WIZARD.value


def test_ai_setting_part_dict():
    setting = AISettingFactory.new(
        agent_type=AIAgentType.QA, ai_setting_any={"mode": "advanced"}
    )
    assert isinstance(setting, QAAISetting)
    assert setting.mode == AISettingMode.ADVANCED.value


# def test_ai_json_schema():
#     mock_wizard_qa_setting_dict = {
#         "mode": "wizard",
#         "idk": "Really don't know",
#     }
#     mock_qa_setting = QAAISetting.parse_obj(mock_wizard_qa_setting_dict)
#     assert mock_qa_setting.mode == AISettingMode.WIZARD.value

#     schema = mock_qa_setting.get_schema()
#     assert not schema["properties"].get("temperature") # 不存在高级advanced字段

#     mock_advanced_setting_dict = {
#         "mode": "advanced",
#         "idk": "Really don't know",
#         "temperature": 0.7,
#     }
#     mock_qa_advanced_setting = QAAISetting.parse_obj(mock_advanced_setting_dict)
#     assert mock_qa_advanced_setting.mode == AISettingMode.ADVANCED
#     schema2 = mock_qa_advanced_setting.get_schema()
    
#     assert schema2["properties"].get("temperature") # 存在高级advanced字段
#     assert not schema2["properties"].get('idk') # 不存在wizard字段
@pytest.mark.asyncio
async def test_ai_json_schema_qa():
    """
    testing the JSON schema of QA agent setting
    """
 
    setting = AISettingFactory.new(agent_type=AIAgentType.QA, ai_setting_any={})
    assert isinstance(setting, QAAISetting)
    schema = await setting.get_schema()
    assert isinstance(schema["JSONSchema"], dict)
    json_schema = schema["JSONSchema"]
    mock_setting_dict = {
        "type": "qa",
        "mode": "wizard",
        #"model": "gpt-3.5-turbo",
        "prologue": "Hi there, I'm an AI chatbot.",
        "isEnabledPromptTips": True,
        "prompt": "The following is a conversation between a human and an AI.",
        "temperature": 0.7,
        "scoreThreshold": 0.8001,
        "isEnableOpenUrl": True,
        "isEnableCollectInformation": False,
    }
    
    # if isEnableOpenUrl is true, openUrlTitle and openUrl are not provided, raise error
    with pytest.raises(ValidationError) as excinfo:
      validate(instance=mock_setting_dict, schema=json_schema)
    result = excinfo.match(r"'openUrl' is a required property")
    assert result

    # if openUrlTitle and openUrl are provided, no error
    mock_setting_dict["openUrlTitle"] = "Learn more"
    mock_setting_dict["openUrl"] = "https://www.google.com"
    validate(instance=mock_setting_dict, schema=json_schema)

    # if isEnableCollectInformation is True, formId is not provided, raise error
    mock_setting_dict["isEnableCollectInformation"] = True
    with pytest.raises(ValidationError) as excinfo:
      validate(instance=mock_setting_dict, schema=json_schema)
    result = excinfo.match(r"'formId' is a required property")
    assert result

@pytest.mark.asyncio
async def test_ai_json_schema_chat():
    """
    testing the JSON schema of QA agent setting
    """
    setting = AISettingFactory.new(agent_type=AIAgentType.CHAT, ai_setting_any={})
    assert isinstance(setting, ChatAISetting)
    schema = await setting.get_schema()
    assert isinstance(schema["JSONSchema"], dict)
    json_schema = schema["JSONSchema"]
    mock_setting_dict = {
        "type": "chat",
        "mode": "wizard",
        #"model": "gpt-3.5-turbo",
        "prologue": "Hi there, I'm an AI chatbot.",
        "isEnabledPromptTips": True,
        "isEnabledPromptBox": True,
        "prompt": "The following is a conversation between a human and an AI.",
        "temperature": 0.7,
    }
    
    # nothing to test for chat agent setting
    pass
