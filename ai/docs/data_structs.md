
## Data Structs 数据结构
> [Back](../README.md)

> [Code](../ai_shared/)

### Data Structs Map

> AI Server所有的持久化数据结构，保存在文件系统，即硬盘里。
> 在执行`make test`后，你可以在ai/.data/看到所有的AI训练、推理、Conversation文件。
> 下图AI-Files为持久化的文件

```mermaid

classDiagram

    class OpenAI_CompatibleAPI {

    }
    class OpenAI_RequestBody {
      ...
      List[Function] functions
      List[Action] actions
    }

    class OpenAI_ResponseBody {
      ...
      List[Action] actions
    }

    OpenAI_CompatibleAPI --> OpenAI_RequestBody
    OpenAI_CompatibleAPI --> OpenAI_ResponseBody

    class BackendServer {

    }
    class AIServer {

    }

    class AIFiles {

    }

    BackendServer --> AIServer: "API Call"
    AIServer --> AIFiles: "Trainer/Inference/Copilot..."

    class Ai {
      string ai_id "MySQL主键"
      string type "qa,chat,analyst"

      string prologue "开场白"
      string prompt "Prompt咒语模板"
      string model "模型配置"
      string embedding_model "Embedding训练模型"

      AISetting setting "动态配置"
    }

    class AiAnalyseQueryPO {
        每次AI查询，都会把消耗的tokens记录一下
        """
        +string ai_id
        +string training_id
        +string conversation_id
        +string prompt  提示词
        +string completion   返回结果
        +int total_tokens  消耗的总token
        +int completion_tokens
        +int prompt_tokens
        +decimal total_cost
        +datetime start_time
        +datetime end_time
    }

    class AISetting {
      string mode "wizard向导|advanced高级"
      string is_enabled_prompt_box "初始prompt提示窗"
      string is_enabled_prompt_tips "每次聊天后prompt提示"
      number temperature 
      number top_p
      number n "返回消息"
      number max_tokens "回复消息有多长"
      number presence_penalty "惩罚机制"
      number frequency_penalty "惩罚机制"
      numberr logit_bias

    }

    class QAAISetting {
      string idk "I don't know"
      number score_threshold "控制向量数据库查询阈值"
    }

    class ChatAISetting {

    }

    QAAISetting --> AISetting
    ChatAISetting --> AISetting

    class DataBusServer {
      get_ai(ai_id: string): Ai
      update_ai_query(query_po: AiQueryPO) "聊天记录token"
    }

    BackendServer --> DataBusServer: "Database"
    DataBusServer --> Ai: "PO in MySQL"
    DataBusServer --> AiAnalyseQueryPO: "Ai Query in MySQL"

    AIServer --> DataBusServer: "Read Ai from databus-server"
    Ai --> AISetting: "MySQL JSON field"
   

    class AIInfo {
      string ai_id
      List[TrainingInfo] success_train_history
      string current_training_id "当前使用的训练集"
      string locking_training_id "锁定训练中的训练集"
    }

    AIFiles --> AIInfo: "training persists & inference read" 

    class TrainingInfo {
      string ai_id
      string training_id
      TrainingStatus status    
      string info
      List[DataSource] data_sources 
      int started_at
      int finished_at
    }

    AIInfo --> TrainingInfo: contains

    class ConversationInfo {
      List[Message] messages

    }
    TrainingInfo --> ConversationInfo: gens

    class DataSource {
      type: string
      words: number
      characters: number
      tokens: number
      meta: DataSourceMeta
      docs: Document
    }
    TrainingInfo --> DataSource: "training results"

    class DatasheetDataSource {
    }
    class APIDataSource {
    }
    class AttachmentDataSource {
    }

    DataSource --> DatasheetDataSource: ""
    DataSource --> APIDataSource: ""
    DataSource --> AttachmentDataSource: ""

    class LangChain {

    }
    LangChain --> Document: from
    class Memory {
      ConversationInfo conversation
    }
    ConversationInfo --> Memory : "conversation history becomes memory"


    class Document {
      string page_content
      dict metadata

    }
    DataSource --> Document: "train into vector db"

    class LLM {
    }
    class Chain {

    }
    class ConversationChain {

    }
    class ConversationRetrievalChain {
    }
    Chain <|-- ConversationChain: ""
    Chain <|-- ConversationRetrievalChain: ""
    LangChain --> Chain: "Chains"
    Chain --> LLM: "OpenAI/Llama..."
    Chain --> Memory: "memory"
    Chain --> Document: "Read from VectorDB"
    


```

### File Persistent & Data Storage Strategy 持久化策略

In the production environment, we use [JuiceFS](https://juicefs.com/), a distributed POSIX file system, to store persisted models.

All persistent data is stored in the `.data` directory. You can find more details about this in the [persist](./ai_server/shared/persist) Python module.

The persisted folder structure is as follows:

```text
- {ai_id}/
   - info.json
   - {training_id}/
      - info.json
      - conversations/
         - {conversation_id}.json
      - vector.db
```

So, you can get infromation from these files, like:

- AI Info
- Traning Info
  - get list
  - Status: Success | Failed | Training
- Conversation Info
  - get list

### Data Source

概念：DataSource 数据源，用于 Training 过程中，记录下对某个数据源的训练历史。

```
DataSource:
- type: string
- type_id: string
- hash: string
- documents:Lang Chain Document

举例：

- type: PDF
- type_id: 文件名
- hash： 文件MD5
- documents：  LangChain PDFLoader

---

- type: datasheet
- type_id: datasheet_id
- hash： revision
- documents：  APITableLoader

```

应用场景：

比较是否需要重新训练？
判断双方的 Data Source 的 hash 是否一致。

比照如表:

| Vika Data | Training Data |
| --------- | ------------- |
| Record    | Document      |
| Datasheet | Data Source   |

### Prompting Format

Here's the AI table model:

- `type`: QA, Chat
- `prologue`: The opening remarks
- `prompt`: text, a prompt with variables ({context}, {question}) to send to LLM
- `settings`: JSON, frontend AI settings
  - enabled_prompt_box
  - enabled_prompt_tips

room-server 中间层，只做上面配置的转发，交给 AIServer 去做，不同的 type bot，有不同的 prompt 变量。

详情不同的 type 的不同的 JSON Settings 表：
https://apitable.getoutline.com/doc/ai-bot-type-prompt-settings-HB0gWbplEQ

### Vector Database Document Persistent Strategy

```python
class Document:

  page_content="""
  field2: second column content
  field3: third column content
  ...
  """

  metadata={
    "id": record.primary_key(),
    "source": record.primary_key(),
    "suggestion:" "出现在prompt window的suggestion",
    "type": "datasheet",
    "datasheet_id": dst_id,
    "record_id": record.id,
  }
```

### Vector Database Files Persistent Strategy

We have millions of "Chatbot" tenants, each with a different documents, collection schema, and vectors to process.

To handle this, we use `ChromaDB` for local file persistence.

Each training session generates a new ChromaDB file with the name `ai_server_{AI_ID}_{DATETIME}`.

This ensures that each training session has its own unique file for storing the vectors, making it easier to manage and access them later.

```mermaid
flowchart LR
    subgraph Kubernetes
    AIServer
    end

    MySQL

    AIServer-->JuiceFS
    AIServer-->MySQL

    subgraph JuiceFS
    VectorDB1
    VectorDB2
    VectorDB3
    end


```

### Table Database -> Loader & Embedding -> Vector Database

| Table Components | Loader          | Desc                                    |
| ---------------- | --------------- | --------------------------------------- |
| Q & A            | QALoader        | Q and A robot                           |
| Attach(PDF)      | PDFLoader       | Auto Q&A split robot                    |
| Button           | FunctionsLoader | Functions                               |
| URL              | URLLoader       | Web Scrawler                            |
| Form             | AIForm          | Positive chat                           |
| View             | Roll Polling    | Positive chat within a table one by one |
