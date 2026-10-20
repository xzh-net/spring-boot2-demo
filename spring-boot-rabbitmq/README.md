# RabbitMQ 消息中间件

## 工作模式

1. 简单模式：一个生产者，只能有一个消费者。（自动确认和手动确认示例）
2. 工作模式：也称为负载均衡模式，一个生产者，多个消费者，默认策略是轮询。（获取消息原始属性示例）
3. 订阅模式 - 扇形交换机：一个生产者，多个消费者， 队列的消费者都能拿到消息，实现一条消息被多个消费者消费。
4. 路由模式 - 直接交换机：比Fanout增加了路由键，根据键规则，适用于需要将不同消息路由到不同队列的场景。
5. 主题模式 - 主题交换机：交换机和队列绑定的时候路由键支持通配符形式。
      - `#` ：匹配一个或多个词
      - `*` ：匹配不多不少恰好1个词

## 死信队列

**死信队列的本质是：通过将消息路由到一个特定的"中转队列"，在该队列中等待满足特定条件（如TTL过期、达到重试上限等）后，自动将消息重新投递到另一个专门处理这些"失败"消息的队列中。**

基于TTL和死信队列实现延迟任务，手动确认的时候如果`异常未处理`或者`QoS=1`存在阻塞问题，必须处理完当前消息才能获取下一个。

解决办法：记录失败次数，达到一定数量的时候，将消息记录到其他载体，然后手动确认掉。

应用场景：

1. 订单超时取消：订单创建后发送延迟消息，到期检查订单状态
2. 支付超时处理：发送支付消息，超时未支付则取消订单
3. 消息重试机制：处理失败的消息进入死信队列，延迟重试
4. 定时任务：基于消息的延迟执行定时任务

## 延迟队列

与死信队列不通，异常造成的阻塞不会影响下一个数据的消费，可实现不同间隔的延迟彼此不受影响。

## 投递确认

全局开启消息投递确认

```yaml
publisher-confirm-type: correlated	# 开启确认机制
publisher-returns: true   			# 开启退回机制 
```

注册两个工具类，`rabbitTemplateWithCallback`有回调机制

```java
@Configuration
public class RabbitMQConfig {
    
    @Bean
    @Primary  // 默认实例，不设置回调
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        // 设置JSON消息转换器
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        // 不设置回调
        return rabbitTemplate;
    }
    
    @Bean("rabbitTemplateWithCallback")
    public RabbitTemplate rabbitTemplateWithCallback(ConnectionFactory connectionFactory, RabbitMQCallback callback) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        
        // 设置JSON消息转换器
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        
        //启用回调机制
        rabbitTemplate.setConfirmCallback(callback);
        rabbitTemplate.setReturnsCallback(callback);
        rabbitTemplate.setMandatory(true);
        
        return rabbitTemplate;
    }
}
```

## 安装MQ

```bash
docker run -p 5672:5672 -p 61613:61613 -p 15672:15672 --name rabbitmq -d rabbitmq:3.7.15
# 拷贝插件
docker cp rabbitmq_delayed_message_exchange-3.8.0.ez rabbitmq:/plugins/
```

### 开启管理插件

```bash
docker exec -it rabbitmq /bin/bash
# 控制台
rabbitmq-plugins enable rabbitmq_management
# 延迟插件
rabbitmq-plugins enable rabbitmq_delayed_message_exchange
```

### 添加用户、虚拟主机、授权

```bash
rabbitmqctl add_user admin 123456
rabbitmqctl set_user_tags admin administrator
rabbitmqctl set_permissions -p "/" admin ".*" ".*" ".*"
rabbitmqctl add_vhost /xzh
rabbitmqctl set_permissions -p "/xzh" admin ".*" ".*" ".*"
```

### 访问地址

http://172.17.17.161:15672/