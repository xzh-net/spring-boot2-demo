# RocketMQ 消息中间件
> 依赖 RocketMQ 服务端 4.9.6

## 核心方法

1. `convertAndSend` - 同步发送，发送消息后，当前线程会阻塞等待，直到收到 RocketMQ Broker 返回的发送结果。发送成功则继续执行后续代码；发送失败则会抛出异常。

   使用场景：**对可靠性要求高的核心业务**：例如，用户下单后，需要发送订单创建消息。如果消息发送失败，可能需要回滚之前的数据库操作

2. `asyncSend` - 异步发送，发送消息后，当前线程立即返回，不会阻塞。你需要提供一个 `SendCallback` 回调函数。当 Broker 返回响应后，RocketMQ 客户端会在另一个线程中调用这个回调，通知你发送成功或失败。

   使用场景：**对性能（吞吐量）要求极高的场景**：例如，日志采集、监控数据上报、活动秒杀等，需要短时间内发送海量消息

3. `sendOneWay` - 单向发送，发送消息后，不等待 Broker 的响应，也没有回调函数。

   使用场景：**对可靠性要求不高的场景**：例如，发送一些不重要的操作日志、用户行为追踪数据。即使丢失一小部分，也完全不影响核心业务

4. `sendOneWayOrderly` - 单向顺序发送，通过一个 `hashKey` 参数，RocketMQ 会保证同一 `hashKey` 的所有消息被发送到同一个 MessageQueue。因为单个队列是 FIFO（先进先出）的，所以保证了同一业务键的消息顺序。

   使用场景：**对顺序有要求，但对可靠性要求不高的场景**，**binlog 同步**：同一个数据库表的变更需要按顺序发送，但偶尔丢失一条可以接受（因为会有定时全量同步补偿

   

总结对比表格

| 方法                    | 发送方式 | 性能     | 可靠性 | 顺序性 | 使用场景                                   |
| :---------------------- | :------- | :------- | :----- | :----- | :----------------------------------------- |
| **`convertAndSend`**    | 同步     | 低       | **高** | 无     | **核心业务**，需要确保消息发送成功         |
| **`asyncSend`**         | 异步     | 中       | **高** | 无     | **高吞吐业务**，主线程不能阻塞，但需知结果 |
| **`sendOneWay`**        | 单向     | **极高** | 低     | 无     | **日志/采集**等非核心业务，可容忍丢失      |
| **`sendOneWayOrderly`** | 单向     | **极高** | 低     | **有** | 对**顺序有要求**且可容忍丢失的非核心业务   |



## 消息异常重新投递处理源码

DefaultRocketMQListenerContainer.java

```java
public class DefaultMessageListenerConcurrently implements MessageListenerConcurrently {

    @SuppressWarnings("unchecked")
    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
        for (MessageExt messageExt : msgs) {
            log.debug("received msg: {}", messageExt);
            try {
                long now = System.currentTimeMillis();
                rocketMQListener.onMessage(doConvertMessage(messageExt));
                long costTime = System.currentTimeMillis() - now;
                log.debug("consume {} cost: {} ms", messageExt.getMsgId(), costTime);
            } catch (Exception e) {
                log.warn("consume message failed. messageExt:{}", messageExt, e);
                context.setDelayLevelWhenNextConsume(delayLevelWhenNextConsume);
                return ConsumeConcurrentlyStatus.RECONSUME_LATER;
            }
        }

        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }
}
```

消息重试

```java
public class MessageListenerImpl implements MessageListener {
    @Override
    public Action consume(Message message, ConsumeContext context) {
        //处理消息
        doConsumeMessage(message);
        //方式1：返回 Action.ReconsumeLater，消息将重试
        return Action.ReconsumeLater;
        //方式2：返回 null，消息将重试
        return null;
        //方式3：直接抛出异常， 消息将重试
        throw new RuntimeException("Consumer Message exceotion");
    }
}
```

失败不重试

```java
public class MessageListenerImpl implements MessageListener {
    @Override
    public Action consume(Message message, ConsumeContext context) {
        try {
            doConsumeMessage(message);
        } catch (Throwable e) {
            //捕获消费逻辑中的所有异常，并返回 Action.CommitMessage;
            return Action.CommitMessage;
        }
        //消息处理正常，直接返回 Action.CommitMessage;
        return Action.CommitMessage;
    }
}
```
