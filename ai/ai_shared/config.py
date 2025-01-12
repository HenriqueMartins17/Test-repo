import os
from enum import Enum
from pathlib import Path

from dotenv import load_dotenv
from pydantic import BaseSettings, Field

load_dotenv()

__all__ = [
    "settings",
    "DEV_MODE",
    "Edition",
    "Env",
]


class Edition(Enum):
    VIKA_SAAS = "vika-saas"
    # apitable == aitable
    APITABLE_SAAS = "apitable-saas"
    AITABLE_SAAS = "aitable-saas"

    def is_vika_saas(self):
        return self == Edition.VIKA_SAAS

    def is_aitable_saas(self):
        # apitable == aitable
        return self in (Edition.APITABLE_SAAS, Edition.AITABLE_SAAS)


class Env(Enum):
    DEVELOPMENT = "development"
    INTEGRATION = "integration"
    TEST = "test"
    STAGING = "staging"
    PRODUCTION = "production"
    # vika
    VIKA_INTEGRATION = "vika-integration"
    VIKA_TEST = "vika-test"
    VIKA_STAGING = "vika-staging"
    VIKA_PRODUCTION = "vika-production"
    # apitable == aitable
    APITABLE_INTEGRATION = "apitable-integration"
    APITABLE_TEST = "apitable-test"
    APITABLE_STAGING = "apitable-staging"
    APITABLE_PRODUCTION = "apitable-production"
    # apitable == aitable
    AITABLE_INTEGRATION = "aitable-integration"
    AITABLE_TEST = "aitable-test"
    AITABLE_STAGING = "aitable-staging"
    AITABLE_PRODUCTION = "aitable-production"
    # airagent
    AIRAGENT_INTEGRATION = "airagent-integration"
    AIRAGENT_TEST = "airagent-test"
    AIRAGENT_STAGING = "airagent-staging"
    AIRAGENT_PRODUCTION = "airagent-production"

    def is_all_in_one(self):
        return self in [
            Env.DEVELOPMENT,
            Env.INTEGRATION,
            Env.TEST,
            Env.STAGING,
            Env.PRODUCTION,
        ]

    def is_vika(self):
        return "vika" in self.value

    def is_aitable(self):
        return "apitable" in self.value or "aitable" in self.value

    def is_airagent(self):
        return "airagent" in self.value


class CommonSettings(BaseSettings):
    """A config manager based on pydantic for ai_server.

    it will auto load env variables from .env and fill to fields.
    """

    aiserver_app_title: str = "VIKA AI SERVER"
    aiserver_app_version: str = Path(".version").resolve().read_text().strip()
    aiserver_app_description: str = r"""\
    You can get more information about ai_server from 
    [vikadata/ai](https://github.com/vikadata/ai) repository or README.
    """
    aiserver_app_log_formatter: str = (
        "[%(asctime)s][%(levelname)s][%(filename)s][%(name)s:%(lineno)s] - %(message)s"
    )
    persistent_data_root: Path = Path(".data").resolve()
    sentry_dsn: str = Field(os.getenv("SENTRY_DSN"))

    databus_server_base_url: str = Field(os.getenv("DATABUS_SERVER_BASE_URL"))
    if databus_server_base_url.default == "":
        raise ValueError("you need fill DATABUS_SERVER_BASE_URL in file .env")

    assets_url: str = Field(os.getenv("ASSETS_URL", "https://s1.aitable.ai"))
    edition: Edition = Field(os.getenv("EDITION") or Edition.APITABLE_SAAS)
    env: Env = Field(os.getenv("ENV") or Env.DEVELOPMENT)
    mock: bool = Field(default=os.getenv("MOCK") or False)
    dev_mode: bool = Field(default=os.getenv("DEV_MODE") or False)
    verbose: bool = Field(default=os.getenv("VERBOSE") or False)
    openai_data_agent_assistant_id: str = Field(os.getenv("OPENAI_DATA_AGENT_ASSISTANT_ID", ""))

    class Config:
        env_file = ".env"

    def is_mock(self) -> bool:
        return self.mock is True

    def is_dev_mode(self) -> bool:
        return self.dev_mode is True

    def is_verbose(self) -> bool:
        return self.verbose is True

    # env shortcuts for ci testing only
    # with settings.set_xxx():
    #     do testing ...
    def reset_env_default(self):
        self.env = Env.DEVELOPMENT
        self.edition = Edition.APITABLE_SAAS
        return self

    def set_env_vika(self):
        self.env = Env.VIKA_INTEGRATION
        return self

    def set_env_aitable(self):
        self.env = Env.APITABLE_INTEGRATION
        return self

    def set_env_airagent(self):
        self.env = Env.AIRAGENT_INTEGRATION
        return self

    def set_edition_vika_saas(self):
        self.edition = Edition.VIKA_SAAS
        return self

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        self.reset_env_default()


class DevSettings(CommonSettings):
    """DevSettings only will be used when DEV_MODE enabled."""

    aiserver_host: str = Field(os.getenv("AISERVER_HOST") or "127.0.0.1")
    aiserver_port: int = Field(int(os.getenv("AISERVER_PORT") or 8626))
    env: Env = Field(os.getenv("ENV") or Env.DEVELOPMENT)


class ProdSettings(CommonSettings):
    """ProdSettings is the default settings if there isn't DEV_MODE."""

    aiserver_host: str = Field(os.getenv("AISERVER_HOST") or "0.0.0.0")
    aiserver_port: int = Field(int(os.getenv("AISERVER_PORT") or 8626))
    env: Env = Field(os.getenv("ENV") or Env.PRODUCTION)


DEV_MODE = int(os.getenv("DEV_MODE", 0))  # convert to int for if condition.
if DEV_MODE:
    settings = DevSettings()
    settings.verbose = True
    import langchain
    langchain.verbose = True
else:
    settings = ProdSettings()
