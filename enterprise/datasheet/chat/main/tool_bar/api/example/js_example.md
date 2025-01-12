## initialize SDK [![github]({{ githubIcon }})](https://github.com/vikadata/vika.py)
If you're using OpenAI's Node.js SDK, you can use our service with the same SDK, you just need to change the configuration as follow:


```javascript
const { Configuration, OpenAIApi } = require("openai");
const configuration = new Configuration({
    apiKey: "{{token}}",
    basePath: "{{apiBase}}/nest/v1/ai/{{aiId}}"
});
```

## Create chat completions

```javascript
const openai = new OpenAIApi(configuration);
const chatCompletion = await openai.createChatCompletion({
  model: "gpt-3.5-turbo",
  messages: [{role: "user", content: "Hello world"}],
});
```

## Returned data example

```javascript

{
  "id": "chatcmpl-123",
  "object": "chat.completion",
  "created": 1677652288,
  "choices": [{
    "index": 0,
    "message": {
      "role": "assistant",
      "content": "\n\nHello there, how may I assist you today",
    },
    "finish_reason": "stop"
  }],
  "usage": {
    "prompt_tokens": 9,
    "completion_tokens": 12,
    "total_tokens": 21
  }
}
```
