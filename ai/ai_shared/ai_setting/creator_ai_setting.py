



from typing import Dict, Optional
from ai_shared.ai_setting.base_ai_setting import BaseAdvancedAISetting, AIAgentType
from pydantic import Field


class CreatorAISetting(BaseAdvancedAISetting):
  type: Optional[AIAgentType] = Field(default=AIAgentType.CREATOR, alias="type")
  score_threshold: Optional[float] = Field(
      default=0.01, alias="scoreThreshold"
  )  # score must larger that this threshold to be considered as a valid context
  
  async def get_schema(self) -> Optional[dict]:
    return await super().get_schema()