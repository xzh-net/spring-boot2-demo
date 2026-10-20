# Zipkin 分布式链路追踪


## 1.获取镜像

```shell
docker pull openzipkin/zipkin
```

## 2.启动

```shell
docker run --name zipkin -d -p 9411:9411 openzipkin/zipkin
```

## 3.web端查看

http://127.0.0.1:9411