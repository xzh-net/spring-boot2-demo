# Redis 缓存与流控

访问地址：http://127.0.0.1:8080/swagger-ui/index.html (无认证)

1. 自定义redisTemplate模板，设置序列化函数

2. 开启@EnableCaching注解，设置不同key使用不同的失效时间

3. 基于时间窗滑块算法实现三种方式限流

4. 设置读写分离规则

5. 封装工具类调用生成短信验证码并验证

6. 基本数据类型操作

7. redisson分布式锁

8. redisson布隆过滤器

---


基于时间窗限流脚本

```lua
local key = KEYS[1] -- 限流的资源KEY
local now = tonumber(ARGV[1]) -- 当前时间戳（通常由客户端传入，如 `unixtime * 1000`）
local ttl = tonumber(ARGV[2]) -- 一个时间窗口的毫秒数，用于设置Key的过期时间
local expired = tonumber(ARGV[3]) -- 过期时间点的时间戳（now - 时间窗口大小）
local max = tonumber(ARGV[4]) -- 时间窗口内的最大限额

-- 1. 清除所有过期的数据（得分在 0 到 expired 之间的成员）
redis.call('zremrangebyscore', key, 0, expired)

-- 2. 获取当前窗口中剩余的元素数量（即最近一个时间窗口内的请求数）
local current = tonumber(redis.call('zcard', key))
local next = current + 1

-- 3. 判断是否超限
if next > max then
  return 0; -- 0 表示被限流
else
  -- 4. 没有超限，则将当前请求的时间戳作为成员和得分添加到zset中
  redis.call("zadd", key, now, now)
  -- 5. 刷新整个zset的过期时间
  redis.call("pexpire", key, ttl)
  return next -- 返回当前计数
end
```