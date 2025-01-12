# Json Schema

- [InputJsonSchema](#inputjsonschema)
- [OutputJsonSchema](#outputjsonschema)
- [JsonSchema](#jsonschema)
- [Schema](#schema)
- [PropertiesSchema](#propertiesschema)
- [PropertySchema](#propertyschema)
- [UiSchema](#uischema)
- [SubUiSchema](#subuischema)
- [UiOptions](#uioptions)

## InputJsonSchema

Declares the data structure and page structure for the input of an action/trigger.

### Details

- Type: [JsonSchema](#JsonSchema)

### Example

- `{"schema": {"type": "object", "required": ["account", "password"], "properties": {"account": {"type": "string", "title": "Account"}, "password": {"type": "string", "title": "Password"}}}, "uiSchema": {"ui:order": ["account", "password"]}}`

## OutputJsonSchema

Declares the data structure and page structure for the output of an action/trigger.

### Details

- Type: [JsonSchema](#JsonSchema)

### Example

- `{"schema": {"type": "object", "required": ["message"], "properties": {"message": {"type": "string", "title": "action execute result"}}}, "uiSchema": { "ui:order": ["message"]}`

## JsonSchema

Used to define the data structure and page structure for the input/output of an action/trigger.

### Details

- Type: `object`

### Properties

| Key      | Required | Type                  | Description                                                            |
|----------|----------|-----------------------|------------------------------------------------------------------------|
| schema   | yes      | [Schema](#Schema)     | Declares the data structure for the input/output of an action/trigger. |
| uiSchema | yes      | [UiSchema](#UiSchema) | Declares the page structure for the input/output of an action/trigger. |

### Example

- `{"schema": {"type": "object", "required": ["account", "password" ], "properties": {"account": { "type": "string", "title": "Account"}, "password": {"type": "string", "title": "Password"}}}, "uiSchema": {"ui:order": ["account", "password"]}}`
- `{"schema": {"type": "object", "required": ["message"], "properties": {"message": {"type": "string", "title": "action execute result"}}}, "uiSchema": { "ui:order": ["message"]}`

## Schema

Declares the data structure for the input/output of an action/trigger.

### Details

- Type: `object`

### Properties

| Key        | Required | Type                                  | Description                                                                                      |
|------------|----------|---------------------------------------|--------------------------------------------------------------------------------------------------|
| type       | yes      | string in ('object')                  | This key-value pair is fixed.                                                                    |
| properties | yes      | [PropertiesSchema](#PropertiesSchema) | Declares the properties of the input/output data structure for an action/trigger.                |
| required   | no       | array                                 | Declares the keys of the properties that are required in the input/output for an action/trigger. |

### Example

- `{"type": "object", "required": ["account", "password" ], "properties": { "account": {"type": "string", "title": "Account" }, "password": {"type": "string", "title": "Password"}}`
- `{"type": "object", "required": ["fruit"], "properties": {"fruit": { "type": "string", "title": "Fruit", "enum": ["Apple", "Pear", "Orange"], "default": "Apple" }}}`

## PropertiesSchema

Declares the properties of the input/output data structure for an action/trigger.

### Details

- Type: `object`

### Properties

| Key      | Required | Type                              | Description                         |
|----------|----------|-----------------------------------|-------------------------------------|
| `[^\s]+` | no       | [PropertySchema](#PropertySchema) | The key must be a non-empty string. |

### Example

- `{"account": {"type": "string", "title": "Account" }, "password": {"type": "string", "title": "Password"}}`
- `{"fruit": {"type": "string", "title": "Fruit", "enum": ["Apple", "Pear", "Orange"], "default": "Apple" }}`

## PropertySchema

Declares the information of a property.

### Details

- Type: `object`

### Properties

| Key         | Required | Type                                    | Description                                                      |
|-------------|----------|-----------------------------------------|------------------------------------------------------------------|
| type        | yes      | string in ('string', 'array', 'object') | The type of the property.                                        |
| title       | yes      | string                                  | The name of the property.                                        |
| description | no       | string                                  | The description of the property.                                 |
| enum        | no       | array                                   | When the property type is string, specifies the possible values. |
| default     | no       | string                                  | When the property type is string, specifies the default value.   |

### Example

- `{"type": "string", "title": "Account"}`
- `{"type": "string", "title": "Fruit", "enum": ["Apple", "Pear", "Orange"], "default": "Apple"}`

## UiSchema

Used to declare the object for the interface of JSON data.

### Details

Type: object

### Properties

| Key                                          | Required | Type                        | Description                                                             |
|----------------------------------------------|----------|-----------------------------|-------------------------------------------------------------------------|
| ui:order                                     | no       | array                       | Specifies the order in which properties are displayed on the interface. |
| key of [PropertiesSchema](#PropertiesSchema) | no       | [SubUiSchema](#SubUiSchema) | The key is the property key.                                            |

### Example

- `{"ui:order": [ "account", "password"]}`

## SubUiSchema

Specifies the UI style for a property.

### Details

Type: object

### Properties

| Key                                          | Required | Type                        | Description                                                                   |
|----------------------------------------------|----------|-----------------------------|-------------------------------------------------------------------------------|
| ui:options                                   | no       | [UiOptions](#UiOptions)     | Specifies some styles when rendering a property.                              |
| ui:widget                                    | no       | string                      | Specifies the name of the component used to render the property.              |
| ui:order                                     | no       | array                       | Specifies the order in which the property component is displayed on the page. |
| key of [PropertiesSchema](#PropertiesSchema) | no       | [SubUiSchema](#SubUiSchema) | The key is the property key.                                                  |

## UiOptions

Specifies some styles when rendering a property.

### Details

Type: object

### Properties

| Key       | Required | Type    | Description                |
|-----------|----------|---------|----------------------------|
| showTitle | no       | boolean | Whether to show the title. |
