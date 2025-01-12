## Databus Refactor Process

Hot To Test?

在 integration 环境进行测试

make test

在本地 localhost 进行测试
1. 配置连接到 integration 的数据库信息
2. 本地启动 room-server 和 databus-server
3. 运行 make test

修改 `resources/application.yml` 的 `sdk.isDebug` 开启更详细的输出

```yaml
sdk:
  isDebug: true
```

在 integration 集成环境创建了专门用于测试的空间站: `spcT925FCEw4d`

包含所有字段的表格: `dstM3ZsUuXHih1AMbj`