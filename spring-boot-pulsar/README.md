# Apache Pulsar 消息平台

Spring Boot for Apache Pulsar 是 Spring Boot 3.0 以上版本才正式引入的，本次使用 pulsar-java-spring-boot-starter 集成。


```bash
docker run -dit \
    -p 6650:6650 \
    -p 8080:8080 \
    -v pulsardata:/data/pulsar/data \
    -v pulsarconf:/data/pulsar/conf \
    --name pulsar-standalone \
    apachepulsar/pulsar:2.8.4 \
    bin/pulsar standalone
```