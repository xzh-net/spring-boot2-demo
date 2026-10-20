# SFTP 文件传输

Spring Integration提供了对通过SFTP进行文件传输操作的支持。

安全文件传输协议（SFTP）是一种网络协议，允许您在Internet上通过任何可靠的流传输文件两台计算机之间。

SFTP协议要求安全通道，如SSH，并在整个SFTP会话期间可见客户端的身份。

Spring Integration通过提供三个客户端端点：入站通道适配器、出站通道适配器和出站网关，来支持通过SFTP发送和接收文件。它还提供了方便的命名空间配置来定义这些客户端组件。

```bash
# 安装sftp
docker run -p 2222:22 -d atmoz/sftp xzh:123456:::upload
```

参考文档：http://www.springdocs.cn/spring-integration/6.2.4/zh/sftp.html

