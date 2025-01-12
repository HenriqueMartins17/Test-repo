Init-AppData
===============================
[![Build Status](https://github.com/vikadata/vikadata/actions/workflows/build_init_template.yml/badge.svg)](https://github.com/vikadata/vikadata/actions/workflows/build_init_template.yml)

`Init-AppData`是一个专门为apitable项目提供初始化数据的轻量运维自动化服务，它支持了几个主要功能:

* 初始化静态资源
* 初始化模版中心的数据
* 初始化功能引导
* 初始化小程序
* 初始化机器人 
·* 初始化帐号
* 初始化配置中心空间
* ......

# Get Started
AppData默认启用的本地服务配置（如MySQL、MinIO），是基于APITable工程`local-db模式`的基础环境配置。
如果你的本地服务配置与默认的不一致，请前往 ./env/loader.env.template 对应修改。

## 快速开始 Quick Start
```bash
make install
# make
make # 选择你想要执行的命令
```

## 使用指南 User's Guidance
提供2种搭建模式：
1. 入门款。docker运行已制作完成的 appdata image
   - 已配置在 ./apitable/docker-compose.yaml
2. 进阶款。连接数据源，构建生成配置并更新到本地服务
   1. 连接数据源DB（目前工作间位于vika.cn，需要连接线上数据库，有必要请使用 kubectl port-forward 连接只读副本库）
      1. 映射到本地的端口，请前往 ./env/generator.env.template 对应修改
   2. 配置表：可选择已配置的通用版本，或者拷贝一份配置表进行自定义配置（参考：[配置表模板](https://vika.cn/workbench/fod8lnUhAnFla) )
      1. 使用自定义的配置表，维格表ID和视图ID，请前往 ./env/generator.env.template 对应修改
    ```bash
    make switch
    ```


## Core Ability

* 数据迁移能力，特别适合模版中心样本数据副本导出
* 面向chunk处理，多次读一次写，避免多次对资源的写入
* 事务管理能力，防止脏数据重复写入
* 处理完自动停止，重启可记录历史状态，这是一大特色


## Modules
* `command` define execute command
* `generator` link datasource and write data file
* `loader` load data file and save to target db and oss
* `shared` shared configuration and method

## Development
System Requirements
- Java 8 above

Web Framework
- [Spring Batch](https://github.com/spring-projects/spring-batch)
- Spring Data JPA

IDE
- Intellij IDEA

## Reference
1. Why Spring batch

一个经典的批处理应用程序场景:
* 从 数据库/文件/队列 读取大量数据
* 处理数据
* 写回数据或者使用写到目的数据源

对于批数据处理，读、处理、写分层抽象清晰，让复杂的处理问题简单化抽象。重要的是，它不是schedule调度框架，是对执行任务的规范化和标准化，但它可以结合调度框架，有调度框架去进行调用spring-batch。

## TODO List
- [ ] 模版中心副本制作导出处理，然后写入目标数据库, 并写好单测覆盖
- [ ] 静态资源拉取并初始化同步到目标库 
- [ ] Dockerfile制作，供k8s init container能力使用
- [ ] CI Workflow Support

## Troubleshooting


