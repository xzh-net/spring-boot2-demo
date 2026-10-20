# MongoDB 文档数据库

1. 浏览历史
2. 商品收藏
3. 读写分离

```bash
docker run -p 27017:27017 --name mongo \
-v /data/mongo/db:/data/db \
-d mongo:4.4.6
```