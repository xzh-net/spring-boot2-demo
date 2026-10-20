# ELK 统一日志收集

## 日志使用场景

- 全局日志：控制台，日志文件。级别info
- 审计日志：存储db或logger，logger时在本地audit.log。级别debug
- 埋点日志：文件日志存储在本地point.log，同时推送logstash。级别debug



## 安装 ELK7.17.3 和分词器



## 安装 Logstash 插件

```bash
docker exec -it logstash /bin/bash
sed -i 's|source "https://rubygems.org"|source "http://mirrors.tuna.tsinghua.edu.cn/rubygems/"|g'  Gemfile
./bin/logstash-plugin install --no-verify logstash-codec-json_lines
```

重启容器