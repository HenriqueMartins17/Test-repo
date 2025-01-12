import asyncio
from typing import AsyncIterator, List, Optional, Tuple
from ai_shared.helper import posthog_capture
from langchain.schema.runnable.utils import Output
from langchain.prompts.prompt import PromptTemplate
from langchain.prompts import ChatPromptTemplate
from langchain.schema import Document
from langchain.vectorstores.base import VectorStoreRetriever

from ai_shared.config import settings, Env
from ai_shared.vector import Vector
from ai_shared.ros import OpenAIChatCompletionRO, PostCopilotChatBody
from ai_shared.llmodel import LlModel

### 
DEFAULT_QA_PROMPT = """
Act as a customer support, with a warm and patient personality.
- Using the guide snippets below <<<THE_CONTEXT>>> to respond to the question below <<<THE_QUESTION>>>.
- If there are links to illustrative images within the guide snippets, please make an effort to display those images if possible.
- If the context does not contain relevant information, please explicitly state that you do not have the necessary information and avoid fabricating an answer.

<<<THE_CONTEXT>>>
  {context}

<<<THE_QUESTION>>>
  {question}
"""

class customOutput():
    # 定义 Output 类型
    def __init__(self, content):
        self.content = content

async def aCustomStream(text, chunk_size=5) -> AsyncIterator[customOutput]:
    position = 0
    while position < len(text):
        start = position
        end = start + chunk_size
        yield customOutput(text[start:end])
        position = end
        await asyncio.sleep(0.1)


class HelpAssistant():
    def __init__(
        self,
        model: str,
        streaming: bool = True,
        request_body: PostCopilotChatBody = None,
    ):
        self.model = model
        self.streaming = streaming
        self.request_body = request_body
        self.vectordb_folder_name = "vika_copilot" if settings.edition.is_vika_saas() else "aitable_copilot"
        self.retriever: VectorStoreRetriever = None

    def _get_runnable(self, stream:bool=False):
        self.prompt = ChatPromptTemplate.from_template(template=DEFAULT_QA_PROMPT)

        model_kwargs = OpenAIChatCompletionRO(
            model=self.model,
            stream=stream,
            temperature=0.1
        )

        self.llm = LlModel.get_llm(model_kwargs=model_kwargs, callbacks=[])

        chain = (
            self.prompt 
            | self.llm
        )
        return chain
    
    def _combine_documents(self, docs:List[Tuple[Document, float]], document_separator="\n\n"):
        page_contents = []
        for doc,score in docs:
            page_contents.append(doc.page_content)

        return document_separator.join(page_contents)
    
    async def init_retriever(self) -> VectorStoreRetriever:
        self.retriever: VectorStoreRetriever = await Vector.get_retriever_from_vector_db(self.vectordb_folder_name, score_threshold=0.75)
        return self.retriever
    
    def get_fallback_response(self) -> str:
        
        res = """Sorry, I can’t find a perfect match for your question right now. 🧐 \n\n
💡 Check out our help center for more info and resources: [🌐 Visit Now](https://help.aitable.ai/). \n
💡 Any more questions? Let us know right here: [📝 Feedback](https://aitable.ai/share/shrdaGGppsfg3pjQLXALG/fomnfpJ9XBfDDz6Dkz?fldy5ZmHYGZx2=internalhelp_contactus)."""
        
        if settings.edition.is_vika_saas():
            res = "很抱歉，我目前无法找到完全匹配您问题的信息。\n建议您访问我们的官方帮助中心网站，那里有更多资源和详细信息：[🌐点我查看](https://help.vika.cn) \n\n💡如果有其他问题，还可以表单反馈哦：[📝点我填写](https://vika.cn/share/shrCvbFC53xc3kl00B4Pg/fomqtU9sKtc52k58y6)"

        return res
    
    def print_docs(self, docs:List[Tuple[Document, float]]):
        for doc,score in docs:
            print(f"scroe:{score} \ncontent:{doc.page_content}\nmeta:{doc.metadata}\n------------------\n")
    
    async def astream(self, input: str) -> AsyncIterator[Output]:
        if self.retriever is None:
            await self.init_retriever()

        #docs = await self.retriever.aget_relevant_documents(input)
        docs = self.retriever.vectorstore.similarity_search_with_relevance_scores(input, **self.retriever.search_kwargs)

        if settings.env is Env.DEVELOPMENT:
            self.print_docs(docs)

        self.retrieved_docs = docs

        if len(docs)>0:
            context = self._combine_documents(docs)

            runnable = self._get_runnable(stream=True)
            variables = {
                "question": input,
                "context": context,
            }

            config = {
                #"callbacks": [ConsoleCallbackHandler()]
            }

            if settings.env is Env.DEVELOPMENT:
                promptStr = self.prompt.format(**variables)
                print(promptStr)
            
            async_iter = runnable.astream(input=variables, config=config)
        else:
            # No relevant documents found

            # send the user input to posthog
            distinct_id = hash(input)
            await posthog_capture(distinct_id, "copilot_docs_not_found", {"input": input})

            message = self.get_fallback_response()
            #print(message)
            async_iter = aCustomStream(message, 64)

        return async_iter