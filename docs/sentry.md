

## Bug 监控

我们使用 Sentry 平台来记录、分析 bug 信息。代码的 sourceMap 会传输到 Sentry 上，在平台上可以统计到 bug 的上下文信息。https://sentry.vika.ltd 使用 ldap 账号登录
有一些预制筛选项需要重点关注，比如【页面崩溃】类的报错是致命伤，当持续出现时，需要紧急响应。

### 监控原理

Sentry 会监听全局 error，包括网络 error，script error 等，记录错误栈以及控制台 console 上下文。
同时，我们在 ErrorBoundary 组件中对 React 的 componentDidCatch 事件进行了监听。上报主动上报所有 react crash 的错误堆栈。

### 如何自定义 bug

除了自动监控之外，有时候代码逻辑中需要主动的上报 bug，这个时候可以使用自定义发送能力。
有时候我们需要特别的关注一段代码逻辑是不是会出错，这时候可以主动的去发送 Error 信息。

Sentry 本身提供了主动上报的 API：

```js
// @error 错误对象，可以是 catch 到的 error 对象，也可以是自己 new Error 的对象
// @config: { extra: object } extra 可以记录额外的自定义记录，可以选择性的上传对 debug 有帮助的任何格式信息
Sentry.captureException(error, {
  extra: {
    info,
  },
});
```

因为 Sentry 模块在 core 工程中并没有引入，所以上面的方法有时候不能直接使用，为了解决这个问题，
我们在这个基础上面封装了一层，统一使用 Player 模块，通过事件的形式上报 bug。

```js
Player.doTrigger(Hooks.app_error_logger, {
  error: new Error('这里发生了一条错误，我需要主动上报'),
  metaData: { more: '这个问题可能和浏览器版本有关' },
});
```

上面的代码可以在任何模块中调用。该事件的响应处理在 event_bindings.ts 文件中可以找到。
