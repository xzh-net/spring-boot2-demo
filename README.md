# Spring Boot 2.7

## 1. 基础功能

- [Spring Boot 快速入门示例](spring-boot-stand) — Thymeleaf 模板、i18n 国际化、自定义 Banner 和拦截器集成演示

- [HikariCP 数据库连接池](spring-boot-hikaricp) — Spring Boot 默认连接池 HikariCP 的使用与配置演示

---

## 2. 官方 Starter

- [Spring Security + JWT 权限校验](spring-boot-security) — 整合 JWT 和 Spring Security 进行接口权限控制（含详细文档）

- [SMTP 邮件发送](spring-boot-email) — 使用 JavaMailSender 发送普通邮件、HTML 邮件及附件邮件

- [Quartz 定时任务调度](spring-boot-quartz) — Quartz 作业调度框架集成，含自定义线程池配置

- [Validation 参数校验](spring-boot-validation) — 整合 JSR-380 Validation 处理非空校验，支持国际化消息

- [ActiveMQ 消息队列](spring-boot-activemq) — 基于 JMS 规范整合 ActiveMQ 实现消息发送与订阅

- [RabbitMQ 消息中间件](spring-boot-rabbitmq) — RabbitMQ 多种交换机模式与应用场景实战

- [MongoDB 文档数据库](spring-boot-mongo) — 整合 MongoDB 实现读写分离，演示浏览记录与商品收藏

- [Redis 缓存与流控](spring-boot-redis) — Redis 分布式限流与缓存穿透/击穿解决方案（含详细文档）

- [Elasticsearch 搜索引擎](spring-boot-elasticsearch) — 整合 Elasticsearch 实现商品搜索、索引管理与高亮查询

- [WebFlux 响应式 Web 框架](spring-boot-webflux) — Spring WebFlux 高性能异步非阻塞 Web 开发示例

- [WebSocket 长连接](spring-boot-websocket) — 原生 WebSocket 协议实现服务端推送与双向通信

- [SQLite 嵌入式数据库](spring-boot-sqlite) — 使用 Spring Data JPA 操作 SQLite 嵌入式数据库

---

## 3. 三方 Starter

- [MyBatis-Plus 代码生成脚手架](spring-boot-mybatis-plus) — MyBatis-Plus ORM 框架集成与代码自动生成工具（含详细文档）

- [CXF WebService 服务](spring-boot-webservice) — 整合 Apache CXF 发布 SOAP WebService 服务及客户端调用

- [Dubbo RPC 远程调用](spring-boot-dubbo) — 整合 Apache Dubbo 实现服务提供者与消费者 RPC 远程调用示例

- [SpringDoc 接口文档](spring-boot-springdoc) — 基于 OpenAPI 3 的 SpringDoc 文档生成工具（含详细文档）

- [Apache Pulsar 消息平台](spring-boot-pulsar) — 整合云原生消息平台 Apache Pulsar 的生产与消费

- [RocketMQ 消息中间件](spring-boot-rocketmq) — 整合 Apache RocketMQ 实现消息发送、订阅与事务消息

- [ShardingJDBC 分库分表](spring-boot-sharding-jdbc) — 基于 ShardingSphere JDBC 实现水平分库分表

- [PostgreSQL 性能调优](spring-boot-pg-jmeter) — HikariCP 连接池监控 PostgreSQL，配合 JMeter 进行 SQL 性能调优

- [Jasypt 配置文件加密](spring-boot-jasypt) — 基于 Jasypt 使用国密 SM4 算法加密敏感配置项

- [Kaptcha 验证码生成](spring-boot-kaptcha) — Google Kaptcha 图形验证码生成与校验集成

- [Zipkin 分布式链路追踪](spring-boot-zipkin) — 使用 Zipkin + Sleuth 实现微服务全链路监控追踪

- [多数据源动态切换](spring-boot-datasource) — 基于注解实现多数据源自动切换与动态数据源手动切换

- [Resilience4j 熔断器](spring-boot-resilience4j) — 轻量级熔断器 Resilience4j，替代 Hystrix 与 Sentinel

- [自定义审计日志 Starter](spring-boot-log-starter) — 自定义审计日志拦截器 + Logback 日志配置最佳实践

- [分布式文件存储 Starter](spring-boot-oss-starter) — 自定义 Starter 实现 S3 协议与 FastDFS 分布式文件统一存储

---

## 4. 三方类库

- [MinIO 对象存储](spring-boot-minio) — MinIO 对象存储服务集成，使用 Webhook 和 AMQP 实现上传回调通知

- [HBase 列式数据库](spring-boot-hbase) — 集成 HBase Java API 实现表结构管理与数据增删改查

- [Hadoop HDFS 文件系统](spring-boot-hdfs) — 集成 Hadoop HDFS API 实现 HDFS 文件上传、下载与目录操作

- [Kafka 消息队列](spring-boot-kafka) — 集成 Apache Kafka 实现消息生产发送与消费监听接收

- [FISCO BCOS 区块链](spring-boot-fisco) — 集成 FISCO BCOS 联盟链进行智能合约编写、部署与调用

- [GitLab API 代码管理](spring-boot-gitlab) — 集成 GitLab Java API 管理仓库、用户、分支与 Merge Request

- [Jenkins API 流水线](spring-boot-jenkins) — 集成 Jenkins REST API 实现流水线任务创建、构建与日志查询

- [Kubernetes 容器编排](spring-boot-k8s) — 集成 Fabric8io / Official Client 操作 Kubernetes API 进行容器编排

- [OpenStack 云平台管理](spring-boot-openstack) — 集成 Jclouds 与 OpenStack API 管理主机实例与云资源状态

- [Kettle ETL 数据处理](spring-boot-etl) — 集成 Pentaho Kettle 进行 ETL 数据抽取、转换与加载作业

- [Netty 网络编程](spring-boot-netty) — 基于 Netty 的聊天室项目（Server/Client/Chat 三模块）及调用示例

- [MQTT 物联网协议](spring-boot-mqtt) — MQTT 多种集成方式（Paho/EMQX/Paho JS）客户端接入汇总

- [SFTP 文件传输](spring-boot-sftp) — 基于 JSch 的 SFTP 文件上传下载与远程目录同步

- [Jexl 表达式引擎](spring-boot-jexl) — Apache Commons JEXL 表达式引擎执行动态表达式与脚本

- [Groovy 动态脚本](spring-boot-groovy) — 集成 Groovy 脚本引擎实现运行时动态编程与热加载

- [Java 源码分析生成](spring-boot-parser) — 基于 JavaParser 进行源码解析，让程序自动生成代码

- [Jsoup HTML 解析器](spring-boot-winkawaks) — 使用 Jsoup 解析 HTML 抓取游戏 ROM 资源与图片批量处理

- [Java 调用 Python](spring-boot-python) — 通过 Process/Jython/ScriptEngine 等多种方式调用 Python 脚本

- [Apache Geode 内存网格](spring-boot-geode) — 使用 Apache Geode 分布式内存数据网格解决千万级 QPS 并发查询

- [ELK 统一日志收集](spring-boot-elk) — 基于 Elasticsearch + Logstash + Kibana 技术栈实现微服务统一日志收集分析

- [SockJS + RabbitMQ 实时聊天](spring-boot-sockjs) — SockJS + STOMP 协议结合 RabbitMQ 实现 WebSocket 实时聊天

- [SockJS + Redis 聊天集群](spring-boot-sockjs-redis) — SockJS + STOMP 协议结合 Redis Pub/Sub 实现聊天集群化

- [Activiti 工作流引擎](spring-boot-activiti) — 整合 Activiti 7 工作流设计器与数据验证分组校验

- [YAML 格式校验](spring-boot-yaml) — 使用 SnakeYAML 对 YAML 配置文件进行格式与语法合法性校验

---

## 5. 开发工具

> 不依赖中间件、服务框架的通用工具与集成集合

- [RSA + AES 混合接口加密](spring-boot-rsa) — 使用 RSA 公钥加密 AES 密钥，实现零侵入式接口请求响应混合加密

- [Maven Docker 镜像构建](spring-boot-harbor) — 使用 fabric8io/docker-maven-plugin 构建镜像并推送到私有 Harbor 仓库

- [XSS 防护与 Referer 拦截](spring-boot-xss) — 通过自定义 Filter 防范 XSS 跨站脚本攻击和伪造 Referrer 请求

- [API 接口版本控制](spring-boot-api-version) — 基于自定义注解与 RequestMapping 实现 RESTful API 多版本管理

- [SSE 服务端推送](spring-boot-sse) — Server-Sent Events 服务端单向流式传输协议集成示例

- [SonarQube 代码质量管理](spring-boot-sonar) — 通过 RestTemplate 调用 SonarQube Web API 实现代码质量与安全度量管理

- [EMQX 自定义认证授权](spring-boot-emqx) — 基于 EMQX HTTP API 为 MQTT 客户端实现自定义登录校验与 ACL 授权

- [Collabora Online 在线文档](spring-boot-wopi) — 通过 WOPI 协议集成 Collabora Online 实现在线 Office 文档编辑

- [Dify 大模型平台调用](spring-boot-dify) — 使用 Spring WebClient 调用 Dify API 接入大模型应用能力

- [微信公众号与企业微信](spring-boot-wechat) — 微信公众号消息、菜单、授权及企业微信开发集成

- [海康互联开放平台视频取流](spring-boot-hikiot) — 对接海康互联 Hikiot Open API 实现在线监控视频流获取

- [萤石开放平台视频取流](spring-boot-ys7) — 对接萤石 EZOPEN OPEN API 实现在线监控视频流播放

- [高德地图逆地理编码](spring-boot-geo) — 通过经纬度坐标查询行政区划编码与详细地址信息
