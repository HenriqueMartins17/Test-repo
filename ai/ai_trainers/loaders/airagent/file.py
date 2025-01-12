from typing import Iterator, Optional

from ai_shared.databus.ai_po import AiNodeFileSetting
from ai_shared.persist import DataSource
from ai_trainers.loaders.base import BaseDataSourceLoader


class AirAgentFileLoader(BaseDataSourceLoader):

    def __init__(self, setting: AiNodeFileSetting, is_predict: Optional[bool] = False):
        self.setting = setting
        self.is_predict = is_predict

    def lazy_load(self) -> Iterator[DataSource]:
        type_id = self.setting.url
        data_source = DataSource(
            type="file",
            type_id=type_id,
            count=0,
            fields=[],
            revision=0,
            words=0,
            characters=0,
            tokens=0,
            documents=[],
        )
        yield data_source