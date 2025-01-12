# DatasheetQL

DatasheetQL is a SQL-like language, facilitating the manipulation of APITables with the experience of SQL, gearing APITable towards more professional use.

This document in other languages: [中文](./README-zh.md).

[Grammar Manual](./grammar-manual.md)

# How to Use

DatasheetQL can be run in the command line and imported as an NPM package.

## Run in the Command Line

DatasheetQL is not published on the NPM registry currently, so you need to install it with the `.tgz` file.  
Install DatasheetQL CLI globally:

```shell
npm i -g datasheetql-VERSION.tgz
```

- Execute a DatasheetQL source file:

  ```shell
  dql exec --space <SPACE NAME> --api-token <API TOKEN>  <SOURCE FILE PATH>
  ```

  where `--space` option specifies the space name in which the queries will be executed, and `--api-token` speficies the API Table user API token. Both options are required.

  If you specify the `<SOURCE FILE PATH>` as `-`, the source code will be read from the standard input stream (stdin).

- View execution plan:
  ```shell
  dql explain <SOURCE FILE PATH>
  ```

Run `dql --help` to see more usage.

## Import into Your Project

DatasheetQL is not published on the NPM registry currently, so you need to install it with the `.tgz` file.  
Install DatasheetQL package in your NPM project:

```shell
npm i datasheetql-VERSION.tgz
```

Use DatasheetQL in your project：

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

# How to Build

```shell
npm i           # Install dependencies
npm run build   # Build the entire project, which can be separated into 
                # the following three steps.

npm run grammar # Generate DatasheetQL parser code
npm run langs   # Generate language pack
npm run compile # Compile code
```

Pack a `.tgz` file：

```shell
npm pack
```

Generate TOCs for all long-winded markdown documentations:

```shell
npm run toc
```

Run the CLI:

```shell
npm start
```

If you want to pass arguments to the CLI, add a `--` before all arguments to prevent NPM from intercepting arguments.

```shell
npm start -- exec --space myspace --api-token uskXXXXXX example.dql
```