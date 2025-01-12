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

声明机器人 action/trigger 输入的数据结构和页面结构。

### 细节

- Type: [JsonSchema](#JsonSchema)

### 例子

- `{"schema": {"type": "object", "required": ["account", "password"], "properties": {"account": {"type": "string", "title": "账号"}, "password": {"type": "string", "title": "密码"}}}, "uiSchema": {"ui:order": ["account", "password"]}}`

## OutputJsonSchema

声明机器人 action/trigger 输出的数据结构和页面结构。

### 细节

- Type: [JsonSchema](#JsonSchema)

### 例子

- `{"schema": {"type": "object", "required": ["message"], "properties": {"message": {"type": "string", "title": "action执行结果"}}}, "uiSchema": { "ui:order": ["message"]}`

## JsonSchema

我们通过它定义机器人action/trigger 输入输出的数据结构和页面结构。

### 细节

- Type: `object`

### 属性

| Key      | Required | Type                  | Description                  |
|----------|----------|-----------------------|------------------------------|
| schema   | yes      | [Schema](#Schema)     | 声明 action/trigger 输入输出的数据结构。 |
| uiSchema | yes      | [UiSchema](#UiSchema) | 声明 action/trigger 输入输出的页面结构。 |

### 例子

- `{"schema": {"type": "object", "required": ["account", "password" ], "properties": {"account": { "type": "string", "title": "账号"}, "password": {"type": "string", "title": "密码"}}}, "uiSchema": {"ui:order": ["account", "password"]}}`
- `{"schema": {"type": "object", "required": ["message"], "properties": {"message": {"type": "string", "title": "action执行结果"}}}, "uiSchema": { "ui:order": ["message"]}`

## Schema

声明 action/trigger 输入输出的数据结构。

### 细节

- Type: `object`

### 属性

| Key        | Required | Type                                  | Description                       |
|------------|----------|---------------------------------------|-----------------------------------|
| type       | yes      | string in ('object')                  | 此键值对固定。                           |
| properties | yes      | [PropertiesSchema](#PropertiesSchema) | 声明 action/trigger 输入输出数据结构的属性。    |
| required   | no       | array                                 | 声明 action/trigger 输入输出值必须存在的属性的键。 |

### 例子

- `{"type": "object", "required": ["account", "password" ], "properties": { "account": {"type": "string", "title": "账号" }, "password": {"type": "string", "title": "密码"}}`
- `{"type": "object", "required": ["fruit"], "properties": {"fruit": { "type": "string", "title": "水果", "enum": ["苹果", "梨", "橙子"], "default": "苹果" }}}`

## PropertiesSchema

声明 action/trigger 输入输出数据结构的属性。

### 细节

- Type: `object`

### 属性

| Key      | Required | Type                              | Description |
|----------|----------|-----------------------------------|-------------|
| `[^\s]+` | no       | [PropertySchema](#PropertySchema) | 键需要是字符串。    |

### 例子

- `{"account": {"type": "string", "title": "账号" }, "password": {"type": "string", "title": "密码"}}`
- `{"fruit": {"type": "string", "title": "水果", "enum": ["苹果", "梨", "橙子"], "default": "苹果" }}`

## PropertySchema

声明属性的信息。

### 细节

- Type: `object`

### 属性

| Key         | Required | Type                                    | Description             |
|-------------|----------|-----------------------------------------|-------------------------|
| type        | yes      | string in ('string', 'array', 'object') | 属性类型                    |
| title       | yes      | string                                  | 属性的名称                   |
| description | no       | string                                  | 属性的描述                   |
| enum        | no       | array                                   | 当属性类型为 string 时，可以指定取值  |
| default     | no       | string                                  | 当属性类型为 string 时，可以指定默认值 |

### 例子

- `{"type": "string", "title": "账号"}`
- `{"type": "string", "title": "水果", "enum": ["苹果", "梨", "橙子"], "default": "苹果"}`

## UiSchema

用于声明 JSON 数据的界面的对象。

### 细节

Type: object

### 属性

| Key                                          | Required | Type                        | Description    |
|----------------------------------------------|----------|-----------------------------|----------------|
| ui:order                                     | no       | array                       | 指定属性在界面上显示的顺序。 |
| key of [PropertiesSchema](#PropertiesSchema) | no       | [SubUiSchema](#SubUiSchema) | 键取值为属性键。       |

### 例子

- `{"ui:order": [ "account", "password"]}`

## SubUiSchema

指定某个属性的 ui 样式。

### 细节

Type: object

### 属性

| Key                                          | Required | Type                        | Description      |
|----------------------------------------------|----------|-----------------------------|------------------|
| ui:options                                   | no       | [UiOptions](#UiOptions)     | 指定渲染属性时的一些样式。    |
| ui:widget                                    | no       | string                      | 指定用于渲染属性的组件的名称。  |
| ui:order                                     | no       | array                       | 指定属性组件在页面上显示的顺序。 |
| key of [PropertiesSchema](#PropertiesSchema) | no       | [SubUiSchema](#SubUiSchema) | 键取值为属性键。         |

## UiOptions

指定渲染属性时的一些样式。

### 细节

Type: object

### 属性

| Key       | Required | Type    | Description |
|-----------|----------|---------|-------------|
| showTitle | no       | boolean | 是否显示标题      |
