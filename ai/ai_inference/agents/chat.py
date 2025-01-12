
import pydantic
from langchain.prompts.prompt import PromptTemplate
from loguru import logger

from ai_inference.agents.base import BaseAgent, BaseRAGAgent

DEFAULT_CHAT_TEMPLATE_VARIABLES_PART = """
Current conversation:
{history}

Human: {input}
AI:
"""

DEFAULT_CHAT_TEMPLATE = f"""
The following is a conversation between a human and an AI. 

{DEFAULT_CHAT_TEMPLATE_VARIABLES_PART}
"""


class ChatAgent(BaseAgent):
    """
    Here is a standard LangChain chatbot with conversations, memory, Chroma embedding db.
    """

    def _get_runnable(self):
        runnable = self._get_prompt_template() | self.llm
        return runnable

    async def _get_runnable_variables(self, input: str):
        self.memory = await self._new_memory(self.ai_id, self.conversation_id)
        if self.memory:
            mem_buffer = self.memory.buffer
        else:
            mem_buffer = {}  # it means no memory

        assert mem_buffer is not None

        context = ""

        variables = {
            "input": input,  # noqa: F821
            "question": input,
            "history": mem_buffer,  #  warning: no effect, seems langchain ConversationChain will replace here automatically
            "context": context,
        }
        return variables

    def _get_prompt_template(self):
        try:
            if self.prompt_template_str:
                revised_prompt_template: str = self.prompt_template_str
                if (
                    "{history}" not in self.prompt_template_str
                    or "{input}" not in self.prompt_template_str
                ):
                    revised_prompt_template += DEFAULT_CHAT_TEMPLATE_VARIABLES_PART

                return PromptTemplate(
                    input_variables=["history", "input"], template=revised_prompt_template
                )
            else:
                return PromptTemplate(
                    input_variables=["history", "input"], template=DEFAULT_CHAT_TEMPLATE
                )
        except pydantic.error_wrappers.ValidationError as e:
            logger.error(
                f"Prompt template is invalid: {self.prompt_template_str}, fallback to default template instead: {e}"
            )
            return PromptTemplate(
                input_variables=["history", "input"], template=DEFAULT_CHAT_TEMPLATE
            )
