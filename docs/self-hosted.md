
# 私有化部署模式的开发和调试
## 开发
> 注意事项，所有代码逻辑都应该兼容私有化部署和云部署的情况，只是通过读取配置来区分，不在代码分支上作区分。所以为私有化部署写的代码逻辑要能够安全的合并到主分支
> 私有化部署模式下拉取的 config 配置文件会影响到打包结果，将代码合并到主分支之前，需要重新执行 make config 覆盖掉配置文件

除了下面的命令之外，其余的开发过程和正常无区别。
1. yarn scripts:makeconfig:private
    * 通过这个形式拉取的 config 会注入 Settings.deploy_mode=private 通过这个配置，代码里可以区分是否是私有化部署。
2. yarn build:dst:pre
    * 构建依赖，这个和普通开发没区别
3. yarn sd:private
    * 这个命令会往环境变量里注入 REACT_APP_DEPLOYMENT_MODELS=PRIVATE 这个环境变量在前端代码里可以访问

私有化模式下，不允许访问任何公网资源，所有涉及到第三方云服务、写死公网 CDN 地址的情况都要针对性做处理

## 资源引用原则
### 域名禁止写死
私有化部署有自定义域名，所以在代码中尽量使用缺省的域名配置，或者动态进行域名拼接
### 禁止外部请求
一个严格的私有化部署环境，是不允许发起任何除主域名之外的第三方请求的（无法访问外网），这不仅是从网络环境考虑，也是处于私有化安全角度，禁止未经允许的情况下向外部发送数据。这要求我们在代码中不能使用第三方提供的静态资源地址，包括脚本、图片等。如果涉及到第三方 SDK 这种一定要使用的，则需要在私有化环境中主动屏蔽请求，防止出现请求失败。
### 工具方法
有时候需要对私有化环境进行特殊判断，比如屏蔽掉第三方登录，代码中有 `isPrivateDeployment()` 工具方法来判断是否处于私有化构建环境。
