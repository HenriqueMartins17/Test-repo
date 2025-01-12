from ai_inference.inference.echo_inference import EchoInference
from ai_inference.inference.openai_inference import OpenAIInference


class InferenceFactory:
    """
    This is a `responder` factory or manager
    """

    @staticmethod
    def new(ai_id: str):
        return EchoInference(ai_id) if ai_id == "echo" else OpenAIInference(ai_id)
