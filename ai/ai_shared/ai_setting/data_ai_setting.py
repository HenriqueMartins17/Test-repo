
from typing import Dict, Optional
from ai_shared.ai_setting.base_ai_setting import BaseAdvancedAISetting, AIAgentType
from pydantic import Field



class DataAISetting(BaseAdvancedAISetting):
  """
  Data Analyst
  """
  type: Optional[AIAgentType] = Field(default=AIAgentType.DATA, alias="type")

  async def get_schema(self) -> Optional[dict]:
    return await super().get_schema()
  