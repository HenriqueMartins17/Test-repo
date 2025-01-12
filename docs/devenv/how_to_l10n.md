# 如何自定义配置

## 如何修改env ？

对 datasheet 新增一个基础的配置项，只需修改 `apitable/packages/l10n/base/env.json` 文件，`env.json` 遵循下面的格式：
```json
{
    "DASHBOARD_WIDGET_MAX_NUM": {
      "value": 30,
      "description": "仪表盘中允许安装的小程序的最大数量"
    }
}
```

`DASHBOARD_WIDGET_MAX_NUM` 是配置的一个key， 它内部的 value 是该配置项的值，description 是该配置的描述。假设我们添加一个配置项 `IS_SHOW_SOMETHING`， 结果如下：
```json
{
    "IS_SHOW_SOMETHING": {
      "value": false,
      "description": "控制显示xxxx"
    }
}
```

## 如何修改 strings ？

### 修改旧的多语言key

如果是修改源文件，找到 `apitable/packages/l10n/base/strings.en-US.json` 进行修改即可，Crowdin 会自动提示其它语言的文件进行修改。
如果某处翻译不准确，比如某处中文需要修改，需要在 Crwodin 中找到对应的 strings 进行修改。

### 添加新的多语言key

在 `apitable/packages/l10n/base/strings.en-US.json` 中添加新的 strings 即可，Crowdin 的协作流程会提供其它语言的翻译结果。

### 修改 backend-server 错误提示的多语言文本

在 `apitable/packages/l10n/base/backend.en-US.json` 进行修改即可。

## 如何修改其它配置
- 修改表情，在 `apitable/packages/l10n/base/emojis.json` 中变更
- 修改通知，静态资源，快捷键等配置，在 `apitable/packages/l10n/base/system_config.json` 变更

## 如何修改特定用户的配置、语言及其它
每个私有化用户都有其对应的代码，在 init-settings 目录下，存在该用户的配置目录，目录名以 `l10n-{{私有化用户代码}}` 的格式命名。

该目录下的文件与 `apitable/packages/l10n/base/` 目录下的文件有相同的名字，且该目录下的文件中的配置具有更高的优先级，该目录下文件内定义的值将会覆盖base目录下的基础配置。

如果我们需要修改某特定私有化用户的配置或者语言，遵循以下流程即可：
1. 在 init-settings 目录下找到该私有化用户的配置文件夹
2. 修改或添加某个语言的文本，比如修改 `og_page_title` 这个字段的英文文本， 只需要创建一个与base目录下和 strings.en-US.json 重名的文件。

```json
{
  "og_page_title": "New Custom Title"
}
```

3. 修改 datasheet 某个配置的值，比如 `DASHBOARD_WIDGET_MAX_NUM`,只需要在该目录下创建一个名为 env.json 的文件，然后按照约定好的格式写入新的值即可，例：
```json
{
    "DASHBOARD_WIDGET_MAX_NUM": {
      "value": 80,
      "description": "仪表盘中允许安装的小程序的最大数量"
    }
}
```
4. 修改其它配置

注意修改时 `json path` 保持一致。
   - 修改表情的配置，创建同名的 emojis.json 进行替换。
   - 修改通知，静态资源，快捷键等配置，创建同名的 system_config.json 修改。


5. 如何在 datasheet 的 public 目录下添加可公开访问的文件

创建文件 `public.json`，按照这样格式定义数据：

```json
{
  "common_img_logo.png": {
  "file": [
    {
      "id": "atcjBj0Te0bft",
      "name": "image.png",
      "size": 3375,
      "mimeType": "image/png",
      "token": "space/2022/07/11/d82ed47c764543368baf96fed4493f3f",
      "width": 309,
      "height": 131,
      "url": "https://s1.vika.cn/space/2022/07/11/d82ed47c764543368baf96fed4493f3f"
    }
  ],
  "描述": ""
}
}
```

`common_img_logo.png` 是文件下载后的名称，file数组中的第一个对象的 url 是文件的下载链接。