# 触发器数据结构

[JSON Schema](https://json-schema.org/) 是一种声明性语言，允许您注释和验证 JSON 文档。简单来说，JsonSchema可以描述某个json的结构。

## 表单提交触发器

### 表单提交触发器输入值数据结构

```json
{
  "type": "object",
  "required": [
    "formId"
  ],
  "properties": {
    "formId": {
      "type": "string",
      "title": "表单id"
    }
  },
  "additionalProperties": false
}
```

### 记录创建触发器输入值数据结构

```json
{
  "type": "object",
  "required": [
    "datasheetId"
  ],
  "properties": {
    "datasheetId": {
      "type": "string",
      "title": "数表id"
    }
  },
  "additionalProperties": false
}
```

### 记录更新触发器输入值数据结构

```json
{
  "type": "object",
  "required": [
    "datasheetId",
    "filter"
  ],
  "properties": {
    "filter": {
      "type": "string",
      "title": "记录所需满足的条件"
    },
    "datasheetId": {
      "type": "string",
      "title": "数表id"
    }
  },
  "additionalProperties": false
}
```

## 触发器输出值数据结构

现有触发器输出值的数据结构都为：

```json
{
  "type": "object",
  "title": "$robot_trigger_record_created_title",
  "required": [
    "datasheetId",
    "datasheetName",
    "recordId",
    "recordUrl"
  ],
  "properties": {
    "recordId": {
      "type": "string",
      "title": "记录id"
    },
    "recordUrl": {
      "type": "string",
      "title": "记录url地址"
    },
    "datasheetId": {
      "type": "string",
      "title": "数表id"
    },
    "datasheetName": {
      "type": "string",
      "title": "数表"
    },
    "[^\\s]+": {
      "type": {
        "oneOf": [
          "null",
          "string", 
          "object",
          "boolean", 
          "array", 
          "number"
        ]
      },
      "title": "[^\\s]+"
    }
  }
}
```