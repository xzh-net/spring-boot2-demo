# Kafka 消息队列
> 服务端验证版本：2.13-2.8.1 / 3.2.3

YML方式可以直接运行，使用config配置方式会覆盖yml方式。

1. 消费简单消息，设置自动提交工厂。
2. 消费简单消息[自定义回调]，未设置提交工厂，走系统默认，但是系统没有设置提交，此类消息重启后会重新推送。（根据重试次数扩展后续业务处理）
3. 消费对象消息，未设置提交工厂，走系统默认，但是手动提交了。
4. 消费批量简单消息，手动提交。
5. 发送key消息，保证消息顺序。
   - 日志收集：不需要顺序保证。
   - 行为跟踪，订单状态更新，保证同一订单状态变更顺序。
   - 根据key的hash值分配到特定分区，相同key的消息分配到同一分区
   - 没有partition，系统自动管理分区位置



## 安装中间件


```bash
# 安装 zookeeper
docker run -d -p 2181:2181 --name some-zookeeper --restart always -d zookeeper:3.7.0

# 安装 kafak
docker run -itd --name kafka -p 9092:9092 \
  --link some-zookeeper:zookeeper \
  -e KAFKA_BROKER_ID=0 \
  -e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://172.17.17.161:9092 \
  -e KAFKA_LISTENERS=PLAINTEXT://0.0.0.0:9092 \
  -t wurstmeister/kafka:2.13-2.8.1
```

