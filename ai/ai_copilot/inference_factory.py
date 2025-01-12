from ai_copilot.data_inference import DataCopilotInference
from ai_copilot.help_inference import HelpCopilotInference
from ai_shared.copilot.models import AssistantType
from ai_shared.exceptions import NoAssistantError
from ai_shared.ros import PostCopilotChatBody


class CopilotInferenceFactory:

    @classmethod
    def new(cls, body: PostCopilotChatBody):
        if body.assistant_type == AssistantType.DATA:
            inference = DataCopilotInference(body)
            return inference
        elif body.assistant_type == AssistantType.HELP:
            inference = HelpCopilotInference(body)
            return inference
        else:
            raise NoAssistantError(f"{body.assistant_type=}")

