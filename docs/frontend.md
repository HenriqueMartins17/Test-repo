
## 本地启动开发环境

1. `yarn install` 后，打包 i18n-lang、core、icons、components、widget-sdk、widget 作为 datasheet 启动依赖：

  ```bash
  yarn build:dst:pre
  ```

2. 启动 web 端项目 datasheet：

  ```bash
  yarn sd
  ```

> 注意⚠️：如果设备是 macOS M1 芯片，需要修改控制台为 rosetta 打开方式（访达 => 应用程序 => 选择控制台程序并双击 => 打开「显示简介」 => 选择 「使用 Rosetta 打开」），然后安装相关依赖再启动开发环境。
> 
> 或者，使用`make install && make run`，以云原生方式本地开发

### 环境依赖

> * git version > 2.16.3
> * node version = 遵循 package.json 指定版本
> * yarn version = 项目自带版本
> * python3 > 3.6 (可选)

可以使用 [nvm](https://github.com/nvm-sh/nvm) 管理 node 版本，当 shell 的工作目录位于本仓库的任何目录下时，运行 `nvm use` 命令即可在本次 shell 会话中将 node 切换到仓库指定的版本。也可以安装 shell 插件自动运行 `nvm use`，例如 zsh 可以通过 oh my zsh 安装 [nvm](https://github.com/ohmyzsh/ohmyzsh/tree/master/plugins/nvm).

## 项目介绍

vikadata 大前端使用 [monorepo](https://en.wikipedia.org/wiki/Monorepo) 方式管理多个包:
### 启动 script
在项目根目录中输入 `make` 可以展示所有可执行的脚本

### packages 介绍：
* i18n-lang
* core
* datasheet
* room-server
* components
* icons
* widget-sdk
* cypress

### i18n-lang

国际化语言包。引入包时，会将语言包全量暴露到全局中。

### 暗黑主题

支持暗黑主题，datasheet 前端工程样式 less 采用 css variables 支持，修改样式时注意不要直接使用颜色 HEX 值，而是使用颜色变量。组件库默认已经支持暗黑主题，入口文件或 ReactDOM.render 组件使用 ThemeProvider 即可。特殊情况如 js 场景可以使用组件库暴露的 colors 对象规范使用颜色。

### core

共享代码逻辑，封装了底层数据操作方法、数据请求方法，可以运行在 browser & node。

### datasheet

数据表格 web 端项目
关于请求地址路由，详情请查看 setup_proxy.js 文件。

> /room 路由到 room-server
> /notification 路由到通知中心
> /api 路由到服务端地址
> /fusion 路由到 fusion api 平台

### room-server

nodejs 项目，用于处理长链消息、协同 OT 逻辑。承载 /fusion & /datasheet 相关请求

### widget-sdk

独立小组件 SDK，给第三方开发者进行小组提供工具包和运行环境

### components

通用组件库

+ 通用基础组件
+ 需要开源的业务组件，第三方开发小组件 block 使用

### icons 

svg 组件化

从维格表 icon 表，同步 icon 信息。（需要 design 仓库和 datasheet 同级）

```
# 安装 python3 依赖
sudo pip3 install -r requirements.txt

yarn sync:icons
```

build 包
```
yarn build:icons
```

### cypress
e2e 测试目录，我们使用 cypress 框架来进行 e2e 测试保障

### 编译成可执行文件
用于私有化部署代码保护
```
yarn global add pkg
```
##### macos环境
```
pkg . --targets macos-x64 --output room-server
```
##### 容器环境
```
pkg . --targets alpine-x64 --output room-server
```
##### docker 部署
```dockerfile
# From here we load our application's code in, therefore the previous docker
# "layer" thats been cached will be used if possible
FROM node:16.15.0-alpine3.15
WORKDIR /home/vikadata
COPY room-server /home/vikadata/
EXPOSE 3333
CMD ["/home/vikadata/room-server"]
```
