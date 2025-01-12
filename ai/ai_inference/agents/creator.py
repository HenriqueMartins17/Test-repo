


from typing import List

from ai_inference import utils
from ai_inference.agents.base import BaseRAGAgent
from langchain.schema import Document
from langchain.prompts.prompt import PromptTemplate

from ai_shared.databus import DataBus
from ai_shared.vector import Vector

DEFAULT_CREATOR_PROMPT = """
You are an excellent creator, content strategist and professional copywriter.

Chat History:
{history}

You need to provide your readers with engaging articles. 
We provide you some reference articles context:
{context}

Please write new article topics based on the reference articles context above and the following demands: 

Demand: {input}
Content Generation:
"""

class CreatorAgent(BaseRAGAgent):

    def _get_prompt_template(self):
        return PromptTemplate(
            input_variables=["context", "input", "history"],
            template=DEFAULT_CREATOR_PROMPT,
        )

    def _get_runnable(self):
        prompt = self._get_prompt_template()
        chain = prompt | self.llm
        return chain

    async def _get_runnable_variables(self, input: str):
        self.memory = await self._new_memory(self.ai_id, self.conversation_id)
        assert self.memory
        assert self.ai_setting.score_threshold
        # retriever = await Vector.get_retriever_from_vector_db(
        #     ai_id=self.ai_id,
        #     score_threshold=self.ai_setting.score_threshold,
        # )
        # docs: List[Document] = retriever.get_relevant_documents(input)
        ai_nodes = await DataBus.aget_ai_node_list_aitable(self.ai_id)
        docs: list[Document] = await Vector.search_docs_by_ai(
            ai_id=self.ai_id,
            ai_nodes=ai_nodes,
            query=input,
            score_threshold=self.ai_setting.score_threshold,
        )
        context: str = ""
        for doc in docs:
            context += doc.page_content + "\n"

        return {
            "input": input,
            "question": input,
            "history": self.memory.buffer_as_str,
            "context": context,
        }