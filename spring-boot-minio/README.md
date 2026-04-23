# MinIO 

## 使用 Webhook 和 AMQP 实现上传回调

MinIO 库：使用自建/多平台存储，或追求轻量级、灵活的客户端。

AWS SDK：项目深度依赖 AWS，或需使用 S3 独有功能

- 查询信息：http://172.17.17.165:8080/oss/info?fileName=1743488658933.jpg
- 下载地址：http://172.17.17.165:8080/oss/url?fileName=1743488658933.jpg
- 下载：http://172.17.17.165:8080/oss/download?fileName=1743488658933.jpg
- 删除：http://172.17.17.165:8080/oss/?fileName=1743488658933.jpg

### 安装中间件

```bash
# 安装 minio
docker run -dit -p 9000:9000 -p 9001:9001 --name minio \
  -v /data/minio/data:/data \
  -v /data/minio/config:/root/.minio \
  minio/minio:RELEASE.2025-04-22T22-12-26Z \
  server /data \
  --console-address ":9001" 
# 查看日志
docker logs -f minio


# 安装 rabbitmq
docker run -p 5672:5672 -p 15672:15672 --name rabbitmq -d rabbitmq:3.7.15
docker exec -it rabbitmq /bin/bash
# 启动管理界面
rabbitmq-plugins enable rabbitmq_management
# 创建用户
rabbitmqctl add_user admin 123456
rabbitmqctl set_user_tags admin administrator
rabbitmqctl set_permissions -p "/" admin ".*" ".*" ".*"
```

访问地址：http://172.17.17.161:9001/ ，账户密码：minioadmin/minioadmin

### 创建事件

![](doc/assets/1.png)

### 创建Webhook事件

![](doc/assets/2.png)

### 填写回调地址

![](doc/assets/3.png)

```
dev
http://172.17.17.165:8080/minio/webhook/{id}
Bearer 123456
```

### 创建AMQP事件

![:](doc/assets/4.png)

```
172.17.17.161:dev
amqp://admin:123456@172.17.17.161:5672
bucketevents
direct
bucketlogs
2
```

### 绑定桶事件

![](doc/assets/5.png)

> AMQP创建回调后，默认没有将交换机与队列进行绑定，绑定过程是在项目代码中完成。



## 预签名实现文件上传下载

```Mermaid
sequenceDiagram
    participant App as 客户端App
    participant Backend as 后端服务 (Java)
    participant S3 as 对象存储 (S3)

    Note over App,Backend: 1. 获取上传授权
    App->>Backend: 请求预签名URL (携带文件名、用户标识等)
    Backend->>Backend: 生成唯一objectKey<br/>调用S3 SDK生成预签名URL<br/>(有效期、权限限定)
    Backend-->>App: 返回预签名URL (HTTP PUT专用)

    Note over App,S3: 2. 客户端直传文件到S3
    App->>S3: HTTP PUT (文件二进制内容)
    Note right of S3: 预签名URL中已包含签名<br/>S3验证签名和有效期
    S3-->>App: 返回 200 OK (文件上传成功)

    Note over App,Backend: 3. 通知后端记录文件元数据
    App->>Backend: 上传完成通知 (携带objectKey、文件元数据)
    Backend->>Backend: 将文件记录存入数据库<br/>(关联用户、存储路径、大小等)
    Backend-->>App: 返回成功 (文件已入库)

    Note over App: 上传流程结束
```

**流程说明**

1. 获取预签名URL：客户端向自己的后端请求一个上传链接，后端生成一个有效期短（如5分钟）、仅能上传到指定路径的预签名URL。

2. 客户端直传：客户端拿到URL后，直接向S3发起PUT请求，文件数据不经过后端服务器。

3. 上传完成通知：S3返回成功后，客户端调用后端接口，告知文件已上传。后端将文件元数据写入数据库，完成文件归属记录。



### 📊 核心对比表

| 维度                  | 方案A：客户端直传                           | 方案B：后端中转                      |
| :-------------------- | :------------------------------------------ | :----------------------------------- |
| **上传速度**          | 快（客户端与MinIO直连，利用用户带宽）       | 慢（需经后端，受限于后端服务器带宽） |
| **后端服务器负载**    | 极低（仅处理签名请求和元数据记录）          | 高（承担所有文件数据的接收与转发）   |
| **带宽成本**          | 低（后端仅传输少量元数据，文件流量走MinIO） | 高（文件流量经过后端，产生双倍流量） |
| **文件大小限制**      | 几乎无限制（MinIO原生支持大文件分片）       | 受后端服务器内存/磁盘/超时限制       |
| **实现复杂度**        | 中等（需处理签名、STS、回调通知）           | 低（简单的HTTP转发，逻辑直观）       |
| **安全性**            | 高（后端不接触文件内容，仅下发短期凭证）    | 中（文件流经后端，可能被截获或记录） |
| **断点续传/分片上传** | 支持（MinIO SDK直接支持）                   | 难以实现（需后端自行实现分片聚合）   |
| **进度反馈**          | 客户端可直接监听上传进度                    | 需后端透传进度，复杂度高             |
| **适合场景**          | 个人网盘、大文件、高并发、海量用户          | 内部系统、小文件、低流量、快速原型   |