
# 5种开发环境的使用指南

vikadata工程，提供5种开发环境搭建的模式：

1. **docker-all**: docker执行所有local代码的服务 + docker启动本机数据库(dataenv)，APITable开源版只保留这种模式，兼容make up命令，实现开发环境、私有化版本统一；
2. **local-db**: (推荐)只启动docker数据库环境，不启动任何应用服务，方便你用vscode或intellij idea开发；
3. **remote-db**: 将development系列数据库流量引本地，方便你用vscode或intellij idea开发；
4. **integration**: 将integration的服务和流量牵引到本地，本地debug的程序会默认连接上integration环境；
5. **testing**: 将testing的服务和流量牵引到本地，本地debug的程序会默认连接上testing环境；

> 小技巧：安装过程中遇到问题，用`make clean`或`sudo make clean`删除本地杂项，类似重启。

## 1. docker-all模式

很简单，直接使用docker运行所有的本地代码(容器里运行本地代码)，且启动好容器数据库。
```bash
make env
#> 选2
make dataenv
make install
make run
```
> 这是一种基于容器的云原生方式，这种方式生产开发环境一致，简单且版本兼容性好，可以看看[为什么](./why.md)

## 2. local-db模式

**本地容器数据库+本地IDE开发调试**

这是维格常用的模式，无需1Password账号密码，可以执行本机的Java和NodeJS，兼具灵活和性能。

```bash
make env
#> 选1，默认

# 为了顺利连接mysql和minio写入hosts文件
sudo make dataenv-setup

# 本地启动数据库
make dataenv

# 利用本地电脑的java和nodejs做dependencies install
make install

make run

```

然后，使用你喜欢的编辑器，IntelliJ IDEA或VSCode，打开根目录，即可开始开发。

> 本地编程语言环境建议使用nvm和sdkman快速切换，请参考[APITable开发者指南](https://github.com/apitable/apitable/blob/develop/docs/contribute/developer-guide.md)配置本地编程语言环境。

## 3. remote-db

**远程development数据库+本地代码调试**
development数据库是安装在development集群的共用数据库，不需要你预先安装1Password，但需要你有kubectl证书(1Password里的kubectl)。

```bash
make env
#> 3  (local app本地应用服务 + remote db远程development数据库)
make ports
```

然后，使用你喜欢的编辑器，IntelliJ IDEA或VSCode，打开根目录，即可开始开发。

你可以执行以下命令，断开数据库连接
```bash
make ports-kill
```

`make ports`转发以下数据库到你的本地电脑端口(localhost:XX)：
1) mysql-development:53306
2) mongodb-development:57017
3) redis-development:56379
4) rabbitmq-development:55672
5) clickhouse-development:49000,58123
6) minio-development:59000,59001
11) backend-server-integration:58081,58083
12) room-server-integration:53333,53334

> 随时更新，请以make ports实际执行显示结果为准 :)

## 4. integration环境模式

**远程integration数据库+远程integration相关服务+本地代码调试**

需要1Password，将连上integration，你启动的服务，也会跟integration其它服务通讯。

```bash
make env
#> 4 (integration)
make ports-pro
```

`make ports-pro`转发以下服务端口到你的本地电脑(localhost:XX)：

  1) mysql-integration:33306
  2) mongodb-integration-1:33717
  3) mongodb-integration-2:33718
  4) redis-production:46379
  5) rabbit-production:45672
  6) mysql-production:43306
  7) mongodb-production-1:43717
  8) mongodb-production-2:43718
## 5. testing环境模式

远程integration数据库+远程testing相关服务+本地代码调试

```bash
make env
#> 5 (testing)
```

## 注意事项

- .env文件，是由make env生成的，.gitginore的环境变量文件，你可以在生成后自由修改.env文件，实现最大的灵活性；
