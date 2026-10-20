# HBase 列式数据库

连接2.4.11测试正常，也可以连接docker版本harisekhon/hbase:2.1测试

## 安装

```bash
docker run -dit --name hbase2 -h hbase2 \
-p 2181:2181 \
-p 8080:8080 -p 8085:8085 -p 9090:9090 -p 9095:9095 \
-p 16000:16000 -p 16010:16010 -p 16020:16020 -p 16030:16030 \
harisekhon/hbase:2.1
```

## hosts配置

需要在本地hosts添加 

```
172.17.17.161 hbase2
```