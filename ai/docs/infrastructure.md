

# Infrastructure 架构与基础数据流
> [Back](../README.md)

## Concepts & Modules

三个基本词语:

- **Inference/推理者**: Handle query chats, such as LLMs text, Image Generators and so on.
- **Trainer/训练者**: Train an embedding models.
- **Copilot/AI助手**: AITable软件内右侧边栏的AI助手

AIServer includes the following features and coding modules:

- Training Module
  - For training a model via APITable's data (`/ai/trainers/`)
  - Different trainers
  - Communicating with different LLMs
    - OpenAI
    - Google Bard / PaLM
  - Loader & Embedding
    - Vika API / APITable API Loader
    - APITable ORM Loader
    - PDF Loader
    - ...
  - Refiner
    - Refine a table from attachments like PDF to table, Word to table.
    - `/ai/trainers/refiners/`
- Inference Module
  - Respond the users' querying chat (`/ai/inference/`)
  - Different inferences
  - Implement Poe Protocol
- Shared Module
  - Data structs & persistent
  - Celery, async tasks
  - Shared libs, helpers and utils.

## Python modules dependencies
```
└── ai_server
  └── ai_shared
    └── databus_client
  └── ai_inference
    └── ai_shared
      └── databus_client
  └── ai_trainers
    └── ai_shared
      └── databus_client
```



## Services Flow

```mermaid

sequenceDiagram
    participant BACK as Backend Server
    participant AI as AI Server
    participant LLM as LLM
    participant VDB as VectorDB

    BACK->>+AI: Train a custom AI model
    AI->>+LLM: Load data and embedding them
    LLM->>VDB: Store embedded vectors into Vector DB
    VDB->>AI: Vectors stored & Suggestions Generate
    AI->>-BACK: Finish the train

    BACK->>+AI: Query a chat
    AI->>VDB: Embedding the chat and search for
    VDB->>LLM: Prompting instruct to LLMs
    AI->>-BACK: Poe Protocol


    BACK->>+AI: Suggestions
    AI->>-BACK: Get suggestions from ORM

```

## Frameworks & Infrastructure

AIServer consist of multi open-source framework, this flowchart shows how we should use.

```mermaid
flowchart
  LangChain
  LLM(LLMs)
  Vectorstore
  FastAPI
  Poe(Poe Protocol)
  Embedding
  Back(backend-server)
  Bard(Google Bard)
  QUEUE(Background Tasks)

  Back -- "output" --> Poe
  Back  -- "output" --> FusionAPI --> OpenAI-compatible-apis
  Back  -- "output" --> InternalAPI

  Back -- "SSE (Server-Side Event)"  <--> AIServer
  AIServer --> FastAPI
  FastAPI --> LangChain
  FastAPI --> FilePersist
  FastAPI --> QUEUE --> Async-Training

  LangChain --> LLM
  LangChain --> Embedding
  LangChain --> Memory
  Embedding --> LLM
  Embedding --> OSS(Open-source Models)
  LLM --> OpenAI
  LLM --> Bard


  FilePersist --> TrainingInfo
  Vectorstore --> Chroma
  FilePersist --> AIInfo
  TrainingInfo --> Vectorstore


```

## Development Components

> 注意了，我们的Loader不同于LangChain的Loader
> 我们的Loader，是返回DataSource的； 
> LangChain的Loader返回Document

```mermaid
flowchart
  Inference
  Trainers
  Shared

  AIServer-->Shared
  Shared-->FilePersist
  Shared-->Schemas
  Shared-->databus-client

  AIServer-->|/query|Inference
  Inference-->|/echo|EchoInference
  Inference-->|/$ai_id|OpenAIInference
  OpenAIInference-->BaseRAGAgent
  BaseRAGAgent-->QAAgent
  BaseRAGAgent-->ChatAgent
  BaseRAGAgent-->CreatorAgent


  AIServer-->|/training|Trainers
  Trainers-->BaseRAGTrainer
  BaseRAGTrainer-->MockTrainer-->Vectors&Suggetions
  BaseRAGTrainer-->|/$ai_id to $dst_id|SDK(APITable SDK Trainer)
  BaseRAGTrainer-->|/$ai_id to $dst_id|ORM(APITable ORM Trainer)
  BaseRAGTrainer-->Loaders(AITable Loaders)

```




### Emerging LLM App Stack

![Emerging LLM App Stack](./EmergingLLMAppStack.jpg)

