## initialize SDK [![github]({{ githubIcon }})](https://github.com/vikadata/vika.py)

If you're using OpenAI's Python SDK, you can use our service with the same SDK, you just need to change the configuration as follow:

```python
import openai
# Setup parameters
openai.api_key = "{{token}}"
openai.api_base = "{{apiBase}}/nest/v1/ai/{{aiId}}"
```

## Create chat completions

```python
chat_completion = openai.ChatCompletion.create(model="gpt-3.5-turbo", messages=[{"role": "user", "content": "Hello world"}])
```

## Returned data example

```python

{
  "id": "chatcmpl-123",
  "conversation id": "CS-0253eb8d-d6c6-4543-88d4-fcb555f52982",
  "actions": null,
  "object": "chat.completion",
  "created": 1677652288,
  "model":"gpt-3.5-turbo",
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
    "total_tokens": 21,
    "total cost": 7.900000000000001e-05.
    "result": "I am an AI, specifically a language model developed by OpenAI."
  }
}
```
