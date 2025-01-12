import random
from typing import Optional

from langchain.prompts import PromptTemplate
from langchain.schema import Document

from ai_shared.config import settings

PROMPT_MAKE_QUESTIONS ="""You are an expert in generating user questions. Your workflow involves generating {n} questions based on the 》》context《《's content, ensuring that these questions cover every segment within the context. Your responses must be in the same language as the one used in the context.

》》Context Start《《
{segments}
》》Context End《《
Result:
Provide answers to the questions I give you: 1. xxx 2. xxxx. Never explain why you are doing this, and never respond with anything other than what I asked in the questions. 
The language of your generated response must be in line with the 》》context《《
Now, let's begin generating questions.
"""


PROMPT_MAKE_QUESTIONS_VIKA ="""
Human:你是生成提问的专家。 你的工作流程涉及基于 》》上下文《《 的内容生成 {n} 个提问，以确保这些提问涵盖上下文中的每个段。 你的响应语言必须与上下文中使用的响应相同。
AI:好的，请好的，请你提供要生成提问的上下文场景。
Human:
》》上下文开始《《
{segments}
》》上下文结束《《
结果：
提供我给你的问题的答案：1. xxx 2. xxxx。切勿解释你为什么这样做，而除了我在问题中问的内容之外，切勿做出任何回应。
你生成的响应的语言必须符合 》》上下文《《
AI:
"""


def make_segments(contents: list[str], max_content_length: Optional[int] = 500) -> str:
    # This model's maximum context length is 4097 tokens, however you requested 9024 tokens (8768 in your prompt; 256 for the completion). Please reduce your prompt; or completion length.
    segments = []
    for idx, content in enumerate(contents):
        s = f"{content[:max_content_length]}"
        segments.append(s)
    return "\n".join(segments)


def format_prompt_make_questions(docs: list[Document], n: int = 20) -> str | None:
    # random take 10 for llm to gen questions
    max_segments = 5
    sample_docs = random.sample(docs, min(max_segments, len(docs)))
    contents = [doc.page_content for doc in sample_docs if doc.page_content]
    if not contents:
        return None
    if settings.edition.is_vika_saas():
        template = PROMPT_MAKE_QUESTIONS_VIKA
        segments = make_segments(contents, max_content_length=500)
    else:
        template = PROMPT_MAKE_QUESTIONS
        segments = make_segments(contents)

    template_kwargs = dict(
        segments=segments,
        n=n,
    )

    template = PromptTemplate.from_template(template=template)
    text = template.format(**template_kwargs)

    return text


def answer_to_questions(answer: str) -> list[str]:
    questions = [q.strip() for q in answer.split("|") if q]  # prompt: use "|" as a separator
    # todo: what if ai answer is not questions? maybe just a declarative sentence?
    questions_checked = []
    for question in questions:
        if any(["?" in question, "？" in question]):
            questions_checked.append(question)
        else:
            continue

    return questions_checked