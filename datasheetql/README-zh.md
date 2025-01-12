# DatasheetQL

DatasheetQL 是一种类 SQL 语言，目标是让用户以编写 SQL 的方式操作维格表，使维格表能够应用于更多专业场景。

[语法手册](./grammar-manual.md)

# 使用

DatasheetQL 可以在终端执行，也可以作为 NPM 包导入项目中使用。

## 运行

DatasheetQL 尚未发布到 NPM 源，需要通过 `.tgz` 包安装。  
全局安装 `.tgz` 包：

```shell
npm i -g datasheetql-版本号.tgz
```

- 执行 DatasheetQL 源文件：
  ```shell
  dql exec --space 空间站名称 --api-token 用户token  源文件路径
  ```
  其中 `--space` 参数指定空间站名称，`--api-token` 指定用户API token，这两个参数都是必需的。  
  如果将 `源文件路径` 指定为 `-`，则从标准输入流读取 DatasheetQL 语句并执行。
- 查看执行计划：
  ```shell
  dql explain 源文件路径
  ```

查看更多用法，请运行 `dql --help`。

## 导入项目

DatasheetQL 尚未发布到 NPM 源，需要通过 `.tgz` 包安装。  
安装 `.tgz` 包：

```shell
npm i datasheetql-版本号.tgz
```

在代码中调用：

```typescript
import { compileFile, DEFAULT_L10N_PROVIDER } from "datasheetql"
import fs from "fs"

const sourceText = fs.readFileSync("input.dql", "utf-8")
const { queries, diagnostics } = compileFile(
  sourceText,
  {
    source: "input.dql",
    l10nProvider: DEFAULT_L10N_PROVIDER,
  })
```

# 构建

```shell
npm i           # 安装依赖
npm run build   # 编译，该命令也可以分成以下3个步骤执行

npm run grammar # 生成 DatasheetQL parser 代码
npm run langs   # 生成语言包
npm run compile # 编译代码
```

打包 `.tgz` 文件：

```shell
npm pack
```

为项目中所有长篇 markdown 文档生成 TOC：

```shell
npm run toc
```

运行 CLI：

```shell
npm start
```

如果需要传递参数给 CLI，需加上 `--`，以避免 npm 拦截参数：

```shell
npm start -- exec --space myspace --api-token uskXXXXXX example.dql
```