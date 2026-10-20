# ActiveMQ 消息队列

JMS (Java Message Service) JAVA消息服务，基于JVM消息的规范。ActiveMQ、HornetMQ是JMS实现。

JMS提供了两种消息模型：点对点、发布订阅。支持五种消息类型：TextMessage 、MapMessage 、BytesMessage 、StreamMessage 、ObjectMessage。

spring-jms提供了JMS的支持，JmsMessagingTemplate对JmsTemplate进行了封装，使用@JmsListener监听消息。

```bash
# 安装
docker run -d --name activemq -p 8161:8161 -p 1883:1883 -p 61614:61614 -p 61616:61616  webcenter/activemq:5.14.3
```

控制台地址：http://0.0.0.0:8161 默认账号密码 admin/admin


