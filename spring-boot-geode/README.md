# Apache Geode 内存网格

‌Apache Geode 是一个数据管理平台，可在广泛分布的云架构中提供对数据密集型应用程序的实时、一致的访问。Geode 跨多个进程汇集内存、CPU、网络资源和可选的本地磁盘，以管理应用程序对象和行为。它使用动态复制和数据分区技术来实现高可用性、改进的性能、可伸缩性和容错性。除了作为分布式数据容器之外，Geode 还是一个内存数据管理系统，可提供可靠的异步事件通知和有保证的消息传递。（gemfire是它的商业版本）

## 核心组件

- locator：locator 定位器，类似于 zk ，进行选举协调，服务发现等功能，我们的应用程序链接的是 locator 定位器
- server：真正提供缓存服务的功能
- region：对数据进行区域划分，类似数据库中表的概念
- gfsh：Geode 的命令行控制台
- client：链接 Geode 服务的客户端

## 环境搭建


镜像拉取
```
docker pull apachegeode/geode
```

启动命令行
```
docker run --net=host -it -p 10334:10334 -p 7575:7575 -p 40404:40404 -p 1099:1099  apachegeode/geode gfsh
```

创建locator和server
```
gfsh> start locator --name=locator1 --port=10334
gfsh> start server --name=server1 --server-port=40404
```

查看集群状态
```
gfsh> list members
```

创建区域
```
gfsh> create region --name=User --type=REPLICATE
```

查询区域
```
gfsh> query --query="select * from /User"
```

清除区域
```
gfsh> remove --all --region=User
```

关闭服务
```
gfsh> shutdown --include-locators=true
```

