# DataBus (Rust Native Module)

The core data (DAO / PO / BO) processing and across platforms library `DataBus`.

- [Documentation](https://apitable.getoutline.com/doc/api-reference-PtqqimrTQx)
- [API Reference(每次修改自动编译)](https://databus.surge.sh)
- [Developer Guide 开发指南](./docs/dev.md)
- [Data Structs & Layer 数据分层和ER结构图](./docs/data.md)

## Quick Start

所以，我目前我们只有2个Binding模式：

- databus-wasm
- databus-server

由于Rust编程语言的`cargo`命令非常好使，你可以利用Rust的本身机制cargo + x在各个monorepo实现build, test, run等各种目的，这里不详述。

但为了简化流程，我们提供binding和test的快捷入口。

日常DataBus的开发是单元测试驱动的，单测只需在databus目录执行：
```bash
make test
# 背后等同在根目录执行cargo test 
```

要启动databus-server，请:
```bash
make run
# 背后等同执行cd databus-server && cargo run
```

databus-wasm，会编译出WebAssembly包到../apitable/packages/databus-wasm-web或../apitable/packages/databus-wasm-web。

```bash
make install
# 背后等同 cd databus-wasm && make install
```
> databus-wasm是前端使用的和nodejs服务端调用的。


# Appendix

- [可视化数据库技术白皮书](https://vikadata.feishu.cn/wiki/wikcnSeLL7AtAjw5wmbSzf0kZyb#doxcnEQSWcGumI6ogiQFCDkUrQh)
- [数据流 Debug 演示](https://vikadata.feishu.cn/wiki/wikcnSTaIMarhNHnmRyFiPpfXne?from=space_search#UG8sdUgSYoQGMsx8FuZcVwdGn6e)
- [APITable Cookbook:Concepts](https://apitable.getoutline.com/doc/concepts-jfCZisl12u)
- [APITable Cookbook:DataBus](https://apitable.getoutline.com/doc/databus-aRCUcdaAOk)
- [2023Q3公司规划&DataBus](https://youtu.be/vmRbYrfyaSw)
- [2023Q3DataBus技术分层](https://youtu.be/Sux5DHIHnLs)
