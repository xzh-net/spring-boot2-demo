# SockJS + RabbitMQ 实时聊天

## 为什么需要SockJS

SockJS 是一个 JavaScript 库，它提供了一个类似于 WebSocket 的 API，但其核心目标是：在浏览器不支持 WebSocket 或其他实时通信技术时，能够自动降级使用其他替代方案（如轮询），以确保实时通信的可行性和兼容性。

相关资料：http://jmesnil.net/stomp-websocket/doc/




## 为什么需要STOMP 

STOMP 的全称是 **Simple (or Streaming) Text Oriented Messaging Protocol**，即 **面向简单（或流）文本的消息协议**。

在没有 STOMP 的情况下，如果我们使用原始的 **WebSocket** 进行消息传递：

- 需要自定义协议： 你需要自己设计消息的格式、结构、命令和通信流程。例如，如何区分是订阅请求还是发送消息？如何包含消息头？
- 缺乏标准功能： 你需要自己实现订阅/发布模式、消息确认、事务、心跳等高级消息功能。
- 客户端复杂性： 每个客户端（Web、移动端）都需要实现这套自定义逻辑，维护成本高。
- 与消息代理集成困难： 直接让 WebSocket 客户端连接到像 RabbitMQ、ActiveMQ 这样的专业消息代理会很困难，因为代理不理解你的自定义格式。



## 常用消息代理和支持

- RabbitMQ： 通过 `rabbitmq_stomp` 插件完美支持 STOMP。
- Apache ActiveMQ / Artemis： 原生支持 STOMP。
- Spring Framework： 提供了强大的 STOMP 支持，包括 `@MessageMapping` 注解等，使得在 Spring 应用中构建 STOMP over WebSocket 的服务端变得非常简单。



## 为什么选择 RabbitMQ 作为 STOMP 消息代理

- 原生 STOMP 插件支持

- 完整的 STOMP 协议实现

- 灵活的路由和消息模型

- 支持集群部署



## 安装RabbitMQ 

在 RabbitMQ 中合法的目的前缀：/temp-queue, /exchange, /topic, /queue, /amq/queue, /reply-queue/

```bash
# 安装
docker run -p 5672:5672 -p 61613:61613 -p 15672:15672 --name rabbitmq -d rabbitmq:3.7.15

# 开启管理插件
rabbitmq-plugins enable rabbitmq_management

# 开启rabbitmq_stomp
rabbitmq-plugins enable rabbitmq_stomp

# 添加用户、授权
rabbitmqctl add_user admin 123456
rabbitmqctl set_user_tags admin administrator
rabbitmqctl set_permissions -p "/" admin ".*" ".*" ".*"
```



## 访问地址

http://localhost:8080/index.html

## 账号

- admin 123456 有权限
- test  123456 有权限
- orderAdmin 123456 无权限 



