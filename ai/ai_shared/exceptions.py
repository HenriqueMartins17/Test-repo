class BaseError(Exception):
    status_code = 500
    code = 500

    def __init__(self, msg: str, data=None):
        self.msg = msg
        self.data = data

    def __str__(self):
        return f"{self.__class__.__name__}: {self.msg}"


class ParamsError(BaseError):
    """Raised when request params is not valid, Usually a Client problem， return 400"""

    status_code = 200
    code = 400


class Feedback(BaseError):
    """Raised when you need to inform the user some business prompt information, return 200"""

    status_code = 200
    code = 200


class ServiceError(BaseError):
    """Raised when the internal service do something failed"""

    status_code = 200
    code = 500


class UpstreamError(BaseError):
    """Raised when the external service you request failed"""

    status_code = 200
    code = 500


class AiNotFoundError(BaseError):
    """Raised when AI is not found. return status_code=200 business code=404"""

    status_code = 200
    code = 404


class AiSettingNotFoundError(BaseError):
    """Raised when AI is not found. return status_code=200 business code=404"""

    status_code = 200
    code = 404


class TrainingNotFoundError(BaseError):
    """Raised when Training is not found. return status_code=200 business code=404"""

    status_code = 200
    code = 404


class NotTrainedError(BaseError):
    """Raised when AI is not trained yet. return status_code=200 business code=404"""

    status_code = 200
    code = 404


class NotTrainedErrorAction(BaseError):
    """Raised when AI is not trained yet. return status_code=200 business code=404 and give action to frontend"""

    status_code = 200
    code = 404


class ConversationNotFoundError(BaseError):
    """Raised when Conversation is not found. return status_code=200 business code=404"""

    status_code = 200
    code = 404


class NoSuggestionError(BaseError):
    """Raised when there isn't any suggestion. return status_code=200 business code=404"""

    status_code = 200
    code = 404


class NoDataSourcesToPredictError(BaseError):
    """Raised when no data sources to predict. return status_code=200 business code=404"""

    status_code = 200
    code = 404


class DatasheetPackNotFoundError(BaseError):
    status_code = 200
    code = 404


class AiNodeNotFoundError(BaseError):
    status_code = 200
    code = 404


class ModelNameInvalidError(BaseError):
    status_code = 200
    code = 404


class NoEmbeddingsError(BaseError):
    status_code = 200
    code = 404


class NoLlmError(BaseError):
    status_code = 200
    code = 404


class NoAiSettingJsonSchemaError(BaseError):
    status_code = 200
    code = 404


class NoLoaderError(BaseError):
    status_code = 200
    code = 404


class AiNodeNoSettingError(BaseError):
    status_code = 200
    code = 404


class AiNodeNoTypeError(BaseError):
    status_code = 200
    code = 404


class AiNodeAirTableSettingError(BaseError):
    status_code = 200
    code = 404


class AiNodeAiTableSettingError(BaseError):
    status_code = 200
    code = 404


class AiNodeFileSettingError(BaseError):
    status_code = 200
    code = 404


class AiNodeDatasheetSettingError(BaseError):
    status_code = 200
    code = 404


class AiAgentTypeError(BaseError):
    status_code = 200
    code = 404


class AiSettingParseError(BaseError):
    status_code = 200
    code = 404


class CopilotInferenceError(BaseError):
    status_code = 200
    code = 404


class NoAssistantError(BaseError):
    status_code = 200
    code = 404