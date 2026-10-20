# SSE 服务端推送

SSE(Server Sent Event)，是一种可以主动从服务端推送消息的技术。SSE的本质其实就是一个HTTP的长连接，只不过它给客户端发送的不是一次性的数据包，而是一个stream流，格式为text/event-stream。所以客户端不会关闭连接，会一直等着服务器发过来的新的数据流。

# EventSource

EventSource 对象接口用于接收服务器发送的事件。它通过 HTTP 连接到服务器，已 text/event-stream 格式接收事件，不关闭连接。

EventSource 对象主要用于 Server-Sent Events（简称 SSE）的技术。这是一种能让浏览器通过 HTTP 连接自动收到服务器端更新的技术。

这个技术的作用是可以完成从服务器端到客户端（浏览器）单向的消息传递。因此我们可以用这个来做推送。不过需要注意的是，IE 并不支持这项技术。

W3C 关于 Server-Sent Events 部分的描述 W3C Server-Sent Events


#  构造函数

```
const eventSource = new EventSource();
```

# 属性

此接口从其父接口 EventTarget 继承属性。

|  属性   | 说明  |
|  :---  | ----  |
| `EventSource.onerror`  | 当发生错误时被调用，并且在此对象上派发 `error` 事件 |
| `EventSource.onmessage`  | 服务器端发送给客户端一条消息时触发                  |
| `EventSource.onopen`  | SSE 连接刚打开时触发                              |
| `EventSource.readyState`  | 表示连接状态（`CONNECTING` 、`OPEN` 和 `CLOSED`）   |
| `EventSource.url`  | 代表源头的 URL |

> onopen 并不是创建连接成功事件，而是首次接收到消息触发事件，需要使用其他方式获取连接成功的状态（暂未处理）

# 方法

|  属性   | 说明  |
|  :---  | ----  |
| `EventSource.close`  | 如果存在，则关闭连接 |

# 测试地址：

此接口从其父接口 EventTarget 继承属性。

- 登录地址：http://localhost:8080/index.html
- 向【admin】发消息：http://localhost:8080/send/admin?message=hello
- 向所有人发消息：http://localhost:8080/send/all?message=wahaha

