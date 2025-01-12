# AI Server


The `AI Server` is a service of https://vikadata.com, https://apitable.com and https://aitable.ai that handles the AI training and inference process.

Contents:

- [Quick Start 快速开始](#quick-start)
- [Infrastructure 架构与组件流](./docs/infrastructure.md)
- [Data Structs 数据结构ER图与持久化](./docs/data_structs.md)
- [Chat Action系统 & AI Form表单实现](./docs/action_system.md)
- [给产品策划的Copilot开发指南](./docs/copliot.md)
- [AirAgent开发指南](./docs/airagent.md)

## Quick Start

Python development environment require `poetry`, just simply run:

```bash
make setup
```

Everything about the development environment will be done for you.

Cloud service depends, API tokens needed:

- **OpenAI Key**: Get our development environment key from 1Password

Create .env file by following command:

```bash
make env ## create a .env files
```

Then, install dependencies with poetry and run server!

```bash
make install ## poetry install dependencies
make test ## You should prioritize using unit tests over running the server for debugging the API.

make swagger ## install databus-client swagger code
make run
```

After your server is running, access http://localhost:8626/docs you will see the Swagger API page, run `make pydoc` will open a package API doc.

Now run `make public` you will get a ngrok URL like https://${XXX}.ngrok-free.app

Use this URL to create a Poe chatbot to test! Use `/poe` behind the URL: https://${XXX}.ngrok-free.app/poe



## 代码规范 Code Guideline
1. 请所有参数要写Pydantic、class、Typed，不准出现dict传参；
2. 所有Python Module尽可能class；



## Questions

### Why this service is not open-source included in `vikadata`?

- Closed-source feature and prevent expose to customers that purchased APITable Enterprise code.
- Convenient for quicker development and debug.

### Any convinient way to do specific unit test?

```python
poetry run pytest -k {function_name}
```

### How to unit test a chat bot?

[Go here: test_bot.py](./tests/test_core.py)

```python
# ...
# ...
a_bot_with_conversation_id = BotFactory.new(ai_id, ai_setting, request, True, None)
chat_1 = await qa_bot_with_conversation_id.arun("What is APITable?")
assert('something' in chat_1)
# ...
```
Just that simple.


## References Projects(参考)

Projects or source codes that for your references:

- [PRD: AI Chat Apps Builder](https://apitable.getoutline.com/doc/prd-aitable-ai-chat-apps-builder-KtNHChx2oX)
- [Poe Protocol](https://github.com/poe-platform/poe-protocol)
- [FastAPI](https://fastapi.tiangolo.com/lo/)
- [Dify.ai](https://github.com/langgenius/dify)
- [LangChain](https://python.langchain.com/)
- [MegaBots](https://github.com/momegas/megabots)
- [SuperAGI](https://github.com/TransformerOptimus/SuperAGI)
- [Zilliz Vector DB](https://docs.zilliz.com/)
- [Zilliz: CVP Stack](https://zilliz.com/blog/ChatGPT-VectorDB-Prompt-as-code)
- [Zilliz: GPT-Cache QA Generation](https://gptcache.readthedocs.io/en/latest/bootcamp/langchain/qa_generation.html)
- [Zilliz: Question Answering over Documents with Zilliz Cloud and LangChain](https://docs.zilliz.com/docs/question-answering-over-documents-with-zilliz-cloud-and-langchain)

(Zilliz doc is for reference only, we use Chroma instead)