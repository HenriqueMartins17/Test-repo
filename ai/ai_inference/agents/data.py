

from typing import List, Optional

from langchain.chains import LLMChain
from langchain.callbacks.manager import Callbacks
from langchain.prompts.prompt import PromptTemplate
from langchain.schema import Document
from loguru import logger

from ai_inference import utils
from ai_inference.agents.base import BaseRAGAgent
from ai_shared.actions.form import FormAction
from ai_shared.actions.url import OpenUrlAction
from ai_shared.ai_setting.base_ai_setting import AISettingMode
from ai_shared.ros import OpenAIChatCompletionRO
from ai_shared.vos import OpenAIChatCompletionVO

from typing import List
from ai_inference.agents.base import BaseAgent

DEFAULT_DATA_PROMPT = """
You are an excellent data analyst.

Chat History:
{history}

Please give me a pandas dataframe based on the following demands: 

Demand: {input}
Content Generation:
"""

class DataAgent(BaseAgent):
    def _get_prompt_template(self):
        return PromptTemplate(
            input_variables=["context", "input", "history"],
            template=DEFAULT_DATA_PROMPT,
        )

    def _get_runnable(self):
        prompt = self._get_prompt_template()
        # TODO: Data Agent
        # https://python.langchain.com/docs/integrations/toolkits/pandas
        # https://docs.pandas-ai.com/en/latest/

        chain = prompt | self.llm
        return chain

    async def _get_runnable_variables(self, input: str):
        self.memory = await self._new_memory(self.ai_id, self.conversation_id)
        assert self.memory
        context = ""

        return {
            "input": input,
            "question": input,
            "history": self.memory.buffer_as_str,
            "context": context,
        }