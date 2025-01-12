# Trigger Data Structures

[JSON Schema](https://json-schema.org/) is a declarative language that allows you to annotate and validate JSON documents. Simply put, JsonSchema can describe the structure of a JSON object.

## Form Submission Trigger

### Form Submission Trigger Input Value Data Structure

```json
{
  "type": "object",
  "required": [
    "formId"
  ],
  "properties": {
    "formId": {
      "type": "string",
      "title": "Form ID"
    }
  },
  "additionalProperties": false
}
```

### Record Creation Trigger Input Value Data Structure

```json
{
  "type": "object",
  "required": [
    "datasheetId"
  ],
  "properties": {
    "datasheetId": {
      "type": "string",
      "title": "Datasheet ID"
    }
  },
  "additionalProperties": false
}
```

### Record Update Trigger Input Value Data Structure

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
      "title": "Filter for records to be updated"
    },
    "datasheetId": {
      "type": "string",
      "title": "Datasheet ID"
    }
  },
  "additionalProperties": false
}
```

### Trigger Output Value Data Structure

The data structure for existing trigger output values is:

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
      "title": "Record ID"
    },
    "recordUrl": {
      "type": "string",
      "title": "Record URL"
    },
    "datasheetId": {
      "type": "string",
      "title": "Datasheet ID"
    },
    "datasheetName": {
      "type": "string",
      "title": "Datasheet Name"
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