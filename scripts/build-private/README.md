### 用于构建专有云版本

- 读取staging版本
- 下载离线镜像
- 构建private版本
- 生成下载包 

#### 安装包目录结构
- images           离线镜像包
- gateway          网关路由文件，来源 apitable/gateway
- .env.private     private版专有环境变量 
- .env.template    模版.env ，来源于 apitable/.env + .env.vikadata/.env.apitable 合成