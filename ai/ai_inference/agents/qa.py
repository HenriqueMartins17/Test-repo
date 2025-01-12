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
from ai_shared.databus import DataBus
from ai_shared.ros import OpenAIChatCompletionRO
from ai_shared.tracking import Tracking
from ai_shared.vector import Vector
from ai_shared.vos import OpenAIChatCompletionVO

###
_question_template = """Given the following conversation and a follow up question, rephrase the follow up question to be a standalone question, in its original language.

Chat History:
{history}
Follow Up Input: {input}
Standalone question:"""

###
CONDENSE_QUESTION_PROMPT = PromptTemplate.from_template(_question_template)


###
DEFAULT_QA_PROMPT_VARIABLES_PART = """
Context:
{context}

Chat History:
{history}

Question: {input}
Helpful Answer:
"""

DEFAULT_IDK = "I'm sorry, but I currently don't have any relevant information in my knowledge base to answer your question."

###
DEFAULT_QA_PROMPT = """
Use the following pieces of context to answer the question at the end. 
If you don't know the answer, just say that "{idk}", don't try to make up an answer.

"""


class QAAgent(BaseRAGAgent):
    _is_idk = False  # I don't know

    # def _new_llm(
    #     self,
    #     model_kwargs: OpenAIChatCompletionRO,
    #     callbacks=Optional[List[Callbacks]],
    # ):
    #     """
    #     QA Agent LLM without callbacks since the combine_doc_chain chain.
    #     """
    #     return ChatOpenAI(
    #         streaming=model_kwargs.stream,
    #         verbose=True,
    #         # callbacks=callbacks, // Compare to ChatBot, QA Agent remove callbacks, the callbacks will handle by `load_qa_chain`
    #         model_name=model_kwargs.model,
    #         model_kwargs=dict(
    #             stop=model_kwargs.stop,
    #             top_p=model_kwargs.top_p,
    #             presence_penalty=model_kwargs.presence_penalty,
    #             frequency_penalty=model_kwargs.frequency_penalty,
    #             logit_bias=model_kwargs.logit_bias,
    #         ),
    #     )

    def _get_question_generator(self):
        question_generator = LLMChain(
            llm=self.llm, prompt=CONDENSE_QUESTION_PROMPT, verbose=True
        )
        return question_generator

    def _get_idk(self):
        idk_str = self.ai_setting.idk if self.ai_setting.idk else DEFAULT_IDK
        return idk_str

    def _get_prompt_template(self):
        """
        If user use QA + WIZARD mode, then use AISetting's `idk` and help him build a prompt.
        If user use QA + ADVANCED mode, then use the prompt_template_str directly,  `idk` setting will be ignored.
        """

        # idk_str = DEFAULT_IDK

        # if self.ai_setting.mode == AISettingMode.WIZARD:
        #     if self.ai_setting.idk:
        #         idk_str = self.ai_setting.idk
        idk_str = self._get_idk()

        try:

            if (
                # self.ai_setting.mode == AISettingMode.ADVANCED.value
                # and self.prompt_template_str
                self.prompt_template_str
            ):
                revised_prompt_template: str = self.prompt_template_str
                if (
                    "{context}" not in self.prompt_template_str
                    or "{input}" not in self.prompt_template_str
                    or "{history}" not in self.prompt_template_str
                ):
                    revised_prompt_template += DEFAULT_QA_PROMPT_VARIABLES_PART

                return PromptTemplate(
                    input_variables=["context", "input", "history", "idk"],
                    template=revised_prompt_template,
                )
            else:
                return PromptTemplate(
                    input_variables=["context", "input", "history"],
                    template=DEFAULT_QA_PROMPT.format(idk=idk_str)
                    + DEFAULT_QA_PROMPT_VARIABLES_PART,
                )
        except Exception as e:
            Tracking.capture_exception(e)
            logger.error(
                f"Prompt template is invalid: {self.prompt_template_str}, fallback to default template instead: {e}"
            )
            return PromptTemplate(
                input_variables=["context", "input", "history"],
                template=DEFAULT_QA_PROMPT.format(idk=idk_str)
                + DEFAULT_QA_PROMPT_VARIABLES_PART,
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
        # docs: List[Document] = await retriever.aget_relevant_documents(input)
        ai_nodes = await DataBus.aget_ai_node_list_aitable(self.ai_id)
        docs: list[Document] = await Vector.search_docs_by_ai(
            ai_id=self.ai_id,
            ai_nodes=ai_nodes,
            query=input,
            score_threshold=self.ai_setting.score_threshold,
        )
        logger.debug(f"{docs[:3]=}")

        if not docs:
            self._is_idk = True

        snippets: list[str] = []
        for doc in docs:
            snippets.append(doc.page_content)
            
        context = "\n---\n".join(snippets)

        return {
            "input": input,
            "question": input,
            "history": self.memory.buffer_as_str,
            "context": context,
            "is_idk": self._is_idk,
            "idk": self._get_idk(),
        }

    def on_response(self, query: str, resp: OpenAIChatCompletionVO):
        # if no relevant documents,
        # tell web-client to start a form
        # inject the OpenAIChatCompletion actions
        if self._is_idk:
            actions = []

            # Open URL, open url should be before form
            is_enable_open_url = self.ai_setting.is_enable_open_url  # QAAISetting
            if is_enable_open_url:
                open_url_action = OpenUrlAction()
                actions.append(open_url_action)

            # Using form to collect information
            is_enable_collect_information = self.ai_setting.is_enable_collect_information
            form_id = self.ai_setting.form_id  # QAAISetting
            if is_enable_collect_information and form_id:
                form_action = FormAction(form_id=form_id)
                actions.append(form_action)

            resp.actions = actions
