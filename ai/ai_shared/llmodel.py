from enum import Enum
from typing import List, Union, Optional

from langchain.callbacks.base import Callbacks
from langchain_openai import ChatOpenAI
from langchain.chat_models.base import BaseChatModel
from langchain_openai import OpenAIEmbeddings
from langchain.schema.embeddings import Embeddings
from pydantic import BaseModel, Field

from ai_shared.config import settings, Edition
from ai_shared.exceptions import NoEmbeddingsError, NoLlmError
from ai_shared.langchain_vika.baidu_qianfan import MyQianfanEmbeddingsEndpoint, MyBaiduQianfanChatEndpoint


class ModelNameOpenAI(Enum):
    """OpenAI https://platform.openai.com/docs/models/models"""
    GPT_4 = "gpt-4"
    GPT_4_0613 = "gpt-4-0613"
    GPT_4_32K = "gpt-4-32k"
    GPT_4_32K_0613 = "gpt-4-32k-0613"
    GPT_4_0314_LEGACY = "gpt-4-0314 (Legacy)"
    GPT_4_32K_0314_LEGACY = "gpt-4-32k-0314 (Legacy)"
    GPT_3_5_TURBO = "gpt-3.5-turbo"
    GPT_3_5_TURBO_16K = "gpt-3.5-turbo-16k"
    GPT_3_5_TURBO_INSTRUCT = "gpt-3.5-turbo-instruct"
    GPT_3_5_TURBO_0613 = "gpt-3.5-turbo-0613"
    GPT_3_5_TURBO_16K_0613 = "gpt-3.5-turbo-16k-0613"
    GPT_3_5_TURBO_0301_LEGACY = "gpt-3.5-turbo-0301 (Legacy)"
    TEXT_DAVINCI_003_LEGACY = "text-davinci-003 (Legacy)"
    TEXT_DAVINCI_002_LEGACY = "text-davinci-002 (Legacy)"
    CODE_DAVINCI_002_LEGACY = "code-davinci-002 (Legacy)"
    BABBAGE_002 = "babbage-002"
    DAVINCI_002 = "davinci-002"
    TEXT_CURIE_001 = "text-curie-001"
    TEXT_BABBAGE_001 = "text-babbage-001"
    TEXT_ADA_001 = "text-ada-001"
    DAVINCI = "davinci"
    CURIE = "curie"
    BABBAGE = "babbage"
    ADA = "ada"
    TEXT_EMBEDDING_ADA_002 = "text-embedding-ada-002"
    WHISPER_1 = "whisper-1"

    @classmethod
    def all_values(cls):
        return set([item.value for item in cls])

    @classmethod
    def default(cls):
        return cls.GPT_3_5_TURBO

    @classmethod
    def default_embedding(cls):
        return cls.TEXT_EMBEDDING_ADA_002

    @classmethod
    def get_available_model_list(cls) -> List["ModelInfo"]:
        return [
            ModelInfo(name=ModelNameOpenAI.GPT_3_5_TURBO, kind=ModelKind.CHAT, provider=ModelProvider.OPENAI),
            ModelInfo(name=ModelNameOpenAI.GPT_3_5_TURBO_0613, kind=ModelKind.CHAT, provider=ModelProvider.OPENAI),
            ModelInfo(name=ModelNameOpenAI.GPT_3_5_TURBO_16K, kind=ModelKind.CHAT, provider=ModelProvider.OPENAI),
            ModelInfo(name=ModelNameOpenAI.GPT_3_5_TURBO_16K_0613, kind=ModelKind.CHAT, provider=ModelProvider.OPENAI),
            ModelInfo(name=ModelNameOpenAI.GPT_4, kind=ModelKind.CHAT, provider=ModelProvider.OPENAI),
            ModelInfo(name=ModelNameOpenAI.GPT_4_0613, kind=ModelKind.CHAT, provider=ModelProvider.OPENAI),
            ModelInfo(name=ModelNameOpenAI.TEXT_EMBEDDING_ADA_002, kind=ModelKind.EMBEDDING, provider=ModelProvider.OPENAI),
        ]


class ModelNameBaiduQianFan(Enum):
    """百度千帆 https://cloud.baidu.com/doc/WENXINWORKSHOP/s/Nlks5zkzu"""
    # 对话Chat
    ERNIE_BOT = "ERNIE-Bot"
    ERNIE_BOT_TURBO = "ERNIE-Bot-turbo"
    BLOOMZ_7B = "BLOOMZ-7B"
    QIANFAN_BLOOMZ_7B_COMPRESSED = "Qianfan-BLOOMZ-7B-compressed"
    LLAMA_2_7B_CHAT = "Llama-2-7b-chat"
    LLAMA_2_13B_CHAT = "Llama-2-13b-chat"
    LLAMA_2_70B_CHAT = "Llama-2-70b-chat"
    QIANFAN_CHINESE_LLAMA_2_7B = "Qianfan-Chinese-Llama-2-7B"
    LINLY_CHINESE_LLAMA_2_7B = "Linly-Chinese-LLaMA-2-7B"
    LINLY_CHINESE_LLAMA_2_13B = "Linly-Chinese-LLaMA-2-13B"
    CHATGLM2_6B = "ChatGLM2-6B"
    CHATGLM2_6B_32K = "ChatGLM2-6B-32K"
    CHATGLM2_6B_INT4 = "ChatGLM2-6B-INT4"
    FALCON_7B = "Falcon-7B"
    FALCON_40B_INSTRUCT = "Falcon-40B-Instruct"
    AQUILACHAT_7B = "AquilaChat-7B"
    RWKV_4_WORLD = "RWKV-4-World"
    RWKV_4_PILE_14B = "RWKV-4-pile-14B"
    RWKV_RAVEN_14B = "RWKV-Raven-14B"
    OPENLLAMA_7B = "OpenLLaMA-7B"
    DOLLY_12B = "Dolly-12B"
    MPT_7B_INSTRUCT = "MPT-7B-Instruct"
    MPT_30B_INSTRUCT = "MPT-30B-instruct"
    OA_PYTHIA_12B_SFT_4 = "OA-Pythia-12B-SFT-4"
    FALCON_180B_CHAT = "Falcon-180B-Chat"
    RWKV_5_WORLD = "RWKV-5-World"
    FLAN_UL2 = "Flan-UL2"
    # 续写Completions
    AQUILACODE_MULTI = "AquilaCode-multi"
    CEREBRAS_GPT_13B = "Cerebras-GPT-13B"
    PYTHIA_12B = "Pythia-12B"
    GPT_J_6B = "GPT-J-6B"
    GPT_NEOX_20B = "GPT-NeoX-20B"
    GPT4ALL_J = "GPT4All-J"
    STARCODER = "StarCoder"
    STABLELM_ALPHA_7B = "StableLM-Alpha -7B"
    XVERSE_13B = "XVERSE-13B"
    CEREBRAS_GPT_6_7B = "Cerebras-GPT-6.7B"
    PYTHIA_6_9B = "Pythia-6.9B"
    # 向量Embeddings
    EMBEDDING_V1 = "Embedding-V1"
    BGE_LARGE_ZH = "bge-large-zh"
    BGE_LARGE_EH = "bge-large-eh"
    # 图像Images
    VISUALGLM_6B = "VisualGLM-6B"
    STABLE_DIFFUSION_XL = "Stable-Diffusion-XL"

    @classmethod
    def all_values(cls):
        return set([item.value for item in cls])

    @classmethod
    def default(cls):
        return cls.ERNIE_BOT

    @classmethod
    def default_embedding(cls):
        return cls.EMBEDDING_V1

    @classmethod
    def get_available_model_list(cls) -> List["ModelInfo"]:
        return [
            ModelInfo(name=ModelNameBaiduQianFan.ERNIE_BOT, kind=ModelKind.CHAT, provider=ModelProvider.BAIDU),
            ModelInfo(name=ModelNameBaiduQianFan.ERNIE_BOT_TURBO, kind=ModelKind.CHAT, provider=ModelProvider.BAIDU),
            ModelInfo(name=ModelNameBaiduQianFan.CHATGLM2_6B_32K, kind=ModelKind.CHAT, provider=ModelProvider.BAIDU),
            ModelInfo(name=ModelNameBaiduQianFan.EMBEDDING_V1, kind=ModelKind.EMBEDDING, provider=ModelProvider.BAIDU),
        ]


class ModelNameBaichuan(Enum):
    """百川智能 https://www.baichuan-ai.com/home#base-test"""
    BAICHUAN2_13B = "Baichuan2-13B"
    BAICHUAN2_7B = "Baichuan2-7B"
    BAICHUAN_13B = "Baichuan-13B"
    BAICHUAN_7B = "Baichuan-7B"

    @classmethod
    def all_values(cls):
        return set([item.value for item in cls])

    @classmethod
    def default(cls):
        return cls.BAICHUAN2_7B

    @classmethod
    def get_available_model_list(cls) -> List["ModelInfo"]:
        return [
            ModelInfo(name=ModelNameBaichuan.BAICHUAN2_13B, kind=ModelKind.CHAT, provider=ModelProvider.BAICHUAN),
            ModelInfo(name=ModelNameBaichuan.BAICHUAN2_7B, kind=ModelKind.CHAT, provider=ModelProvider.BAICHUAN),
        ]


class ModelProvider(Enum):
    OPENAI = "OpenAI"  # Open AI
    BAIDU = "Baidu"  # 百度千帆
    BAICHUAN = "Baichuan"  # 百川智能


class ModelKind(Enum):
    CHAT = "Chat"  # 对话
    COMPLETION = "Completion"  # 续写
    EMBEDDING = "Embedding"  # 向量
    IMAGE = "Image"  # 图像


class ModelInfo(BaseModel):
    name: Union[ModelNameOpenAI, ModelNameBaiduQianFan] = Field()
    kind: ModelKind = Field()
    provider: ModelProvider = Field()


class LlModel:

    @classmethod
    def get_model_list(cls, edition: Edition, model_kind: ModelKind = None) -> List[ModelInfo]:
        model_list = []

        if edition.is_vika_saas():
            baidu_qianfan_model_list = ModelNameBaiduQianFan.get_available_model_list()
            model_list.extend(baidu_qianfan_model_list)

        if edition.is_aitable_saas():
            openai_model_list = ModelNameOpenAI.get_available_model_list()
            model_list.extend(openai_model_list)

        if model_kind:
            model_list = [item for item in model_list if item.kind == model_kind]

        return model_list

    @classmethod
    def get_available_model_names_for_schema(cls, model_kind: ModelKind = None) -> list[str]:
        edition = settings.edition
        model_list = LlModel.get_model_list(edition=edition, model_kind=model_kind)
        model_names = [model_info.name.value for model_info in model_list]
        return model_names

    @classmethod
    def get_available_model_names_for_chat(cls, model_kind: ModelKind = None) -> list[str]:
        edition = settings.edition
        model_list = LlModel.get_model_list(edition=edition, model_kind=model_kind)
        model_names = [model_info.name.value for model_info in model_list]

        # vika-saas old openai data must can chat
        if edition.is_vika_saas():
            model_list = LlModel.get_model_list(edition=Edition.APITABLE_SAAS, model_kind=model_kind)
            model_names.extend([model_info.name.value for model_info in model_list])
        return model_names

    @classmethod
    def get_default_model_by_edition(cls) -> str:
        """get model by edition"""
        edition = settings.edition
        if edition.is_vika_saas():
            return ModelNameBaiduQianFan.default().value
        if edition.is_aitable_saas():
            return ModelNameOpenAI.default().value

    @classmethod
    def get_default_embedding_model_of_openai(cls) -> str:
        return ModelNameOpenAI.default_embedding().value

    @classmethod
    def get_embedding_model_by_model(cls, model: str | None) -> str:
        """get embedding_model by model, if model not given or not valid, choose one by edition"""
        if model in ModelNameOpenAI.all_values():
            return ModelNameOpenAI.default_embedding().value

        if model in ModelNameBaiduQianFan.all_values():
            return ModelNameBaiduQianFan.default_embedding().value

        if model in ModelNameBaichuan.all_values():
            return ModelNameBaiduQianFan.default_embedding().value

        edition = settings.edition
        if edition.is_vika_saas():
            return ModelNameBaiduQianFan.default_embedding().value
        if edition.is_aitable_saas():
            return ModelNameOpenAI.default_embedding().value

    @staticmethod
    def get_embeddings(embedding_model: str) -> Embeddings:
        """Select AI Embeddings Model"""
        # to use this function, you should always offer a valid embedding model name
        if not embedding_model:
            raise NoEmbeddingsError(msg=f"Check embedding_model: {embedding_model}")

        if embedding_model in ModelNameOpenAI.all_values():
            return OpenAIEmbeddings(model=embedding_model)
        if embedding_model in ModelNameBaiduQianFan.all_values():
            return MyQianfanEmbeddingsEndpoint(model=embedding_model)

        raise NoEmbeddingsError(msg=f"Check embedding_model: {embedding_model}")

    @staticmethod
    def get_llm(
            model_kwargs: "OpenAIChatCompletionRO",
            callbacks=Optional[List[Callbacks]],
    ) -> BaseChatModel:
        """
        Create a new LLM model with the given model name and model kwargs

        Currently, we use OpenAI, but we should also compatible with OpenAI API's kwargs.
        """
        model = model_kwargs.model
        if model in ModelNameOpenAI.all_values():
            return ChatOpenAI(
                streaming=model_kwargs.stream,
                verbose=True,
                callbacks=callbacks,
                model_name=model_kwargs.model,
                temperature=model_kwargs.temperature,
                model_kwargs=dict(
                    stop=model_kwargs.stop,
                    # top_p=model_kwargs.top_p,
                    presence_penalty=model_kwargs.presence_penalty,
                    frequency_penalty=model_kwargs.frequency_penalty,
                    logit_bias=model_kwargs.logit_bias
                ),
            )
        if model in ModelNameBaiduQianFan.all_values():
            return MyBaiduQianfanChatEndpoint(
                streaming=model_kwargs.stream,
                verbose=True,
                callbacks=callbacks,
                model_name=model_kwargs.model,
                temperature=model_kwargs.temperature,
                model_kwargs=dict(
                    stop=model_kwargs.stop,
                    # top_p=model_kwargs.top_p,
                    presence_penalty=model_kwargs.presence_penalty,
                    frequency_penalty=model_kwargs.frequency_penalty,
                    logit_bias=model_kwargs.logit_bias
                ),
            )

        raise NoLlmError(msg=f"Check model: {model}")
