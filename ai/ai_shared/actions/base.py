
from pydantic import BaseModel

# class ChatCompletionFunctionParameter(BaseModel):
#     """
#     JSON schema
#     """
#     pass
# class OpenAIFunction(BaseModel):
#     name: str
#     description: Optional[str]
#     parameter: Optional[ChatCompletionFunctionParameter]


class BaseAction(BaseModel):
    name: str

    def call(self):
        raise NotImplementedError("Subclasses must implement this method.")


class ServerRequestAction(BaseAction):
    pass


class ClientRequestAction(BaseAction):
    pass
