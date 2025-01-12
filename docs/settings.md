
## 业务配置表（makeconfig）

在内网空间中，有 [云配置](https://integration.vika.ltd/workbench/fodeTKml3j4TD)  目录 里面放满了各种各样的配置表；

我们将这个作为我们的配置管理工具，并自动生成 JSON 格式。

执行命令，生成业务配置表：

```shell
make conf
```

之后，strings.auto.json 文案相关配置，就会存放到 `/package/i18n-lang/src/config/`，system_config.auto.json 这些 JSON 配置表则放到 `/packages/core/src/config/` 中。

如果想来了解业务配置表 `make conf` 的细节，可以查看 [SystemConfig 系统配置生成器介绍](scripts/system_config/README.md)。

API Key 为公用的 dev@vikadata.com	维格表 API TOKEN 专用号

### 配置代码自动生成

**TypeScript:**

上面的 `make conf` 命令，默认会生成 JSON 配置和 TypeScript 代码，自动化生成的 TypeScript 代码已经放置到 `/packages/core/src/config/system_config.interface.ts` 中，直接跟已有代码整合。

**Java:**

因为Java代码不是同一个工程，因此，需要指定 Java 工程根目录，进行 Java 代码的放置

比如，本机在前端工程根目录下，执行如下命令：

```shell
export VIKA_SERVER_PATH=$PWD/../vikadata-master && npm run scripts:makeconfig-javacode
```

之后，所有的 Java 代码就会在 `$VIKA_SERVER_PATH/application/src/main/java/com/apitable/config/` 中进行生成了。

并且会把 *.json 相关的配置表，也带到了 `$VIKA_SERVER_PATH/application/src/main/resources/` 中。

## 多语言（i18n）

多语言基于业务配置表的里的 `config.strings` 表。  执行以上makeconfig 后，会从 strings 表导出完整的字符串。

### 前端代码用法

要在前端使用多语言化，用法：

```typescript
import { t, Strings } from '@apitable/core';

// 传入 key，t 函数会在一个对象里找到对应文案，如果找不到，就会默认使用这个 key
console.log(t('new_datasheet'));
// t(Strings.new_datasheet) 返回值为 'new_datasheet'，下面这种写法是为了兼容旧写法
console.log(t(Strings.new_datasheet));
console.log(t(Strings.something, '参数1', '参数2')); // String.format字符串格式化模式
```

参数除了使用纯字符串之外，还可以借助 TComponent 传入 react 组件作为参数
```tsx
import { TComponent } from 'pc/components/common/t_component';
<TComponent
  tkey={t(Strings.hello_world)}
  params={{
    floor: <span className={styles.bold}>{t(Strings.grass)}</span>,
  }}
/>
```

### 浏览器语言自动发现（未落地）

用户在进入浏览器的时候，`Detector` 会判断用户的语言：

1. cookie (set cookie vika-i18n=LANGUAGE)
2. localStorage (set key vika-i18n=LANGUAGE)
3. navigator (set browser language)
4. querystring (append ?lang=LANGUAGE to URL)
5. htmlTag (add html language tag <html lang="LANGUAGE" ...)
6. path (http://my.site.com/LANGUAGE)
7. subdomain (http://LANGUAGE.site.com)

本地调试时，你可以通过在网址后方加上 `?lang=en-US` 来测试英文版。

语言符列表参考：[地址](https://www.iana.org/assignments/language-tags/language-tags.xhtml)

### 常见翻译工作流程

1. strings 表中，添加这个字段:  「 something   一些东西 」;
2. `make conf` 配置+代码；
3. 代码中植入代码 `t(Strings.something)`。
