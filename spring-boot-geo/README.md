# Spring Boot Geo - 高德地图逆地理编码服务

通过坐标查询当前地点对应的行政区划编码、详细地址等信息。

## 功能特性

- 单次逆地理编码：根据经纬度查询详细地址、行政区划等信息
- 批量逆地理编码：一次请求最多20个坐标，返回批量结果

## API 接口

### 1. 单次逆地理编码

```
GET /api/geo/regeo
```

**参数：**
- `longitude` (String, 必填) - 经度，例：116.391275
- `latitude` (String, 必填) - 纬度，例：39.906218

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "status": "1",
    "info": "OK",
    "infocode": "10000",
    "regeocode": {
      "formattedAddress": "北京市西城区西长安街街道人大会堂西路国家大剧院",
      "addressComponent": {
        "country": "中国",
        "province": "北京市",
        "city": "[]",
        "district": "西城区",
        "township": "西长安街街道",
        "neighborhood": "{\"name\":[],\"type\":[]}",
        "building": "{\"name\":[],\"type\":[]}",
        "adcode": "110102",
        "citycode": "010",
        "towncode": "110102001000"
      }
    },
    "regeocodes": null
  },
  "timestamp": 1787704509247
}
```

### 2. 批量逆地理编码

```
GET /api/geo/batch
```

**参数：**
- `locations` (String, 必填) - 多个坐标，用分号分隔，最多20个。格式：`经度1,纬度1;经度2,纬度2;...`

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "formattedAddress": "北京市西城区...",
      "addressComponent": {
        "country": "中国",
        "province": "北京市",
        "city": "[]",
        "district": "西城区",
        "township": "西长安街街道",
        "adcode": "110102",
        "citycode": "010",
        "towncode": "110102001000"
      }
    },
    {
      "formattedAddress": "上海市浦东新区...",
      "addressComponent": { ... }
    }
  ]
}
```

**调用示例：**

```bash
# 20个测试坐标
curl "http://127.0.0.1:8080/api/geo/batch?locations=116.391275,39.906218;121.473701,31.230416;113.264385,23.129112;114.057868,22.543099;116.407428,39.904211;120.155070,30.274659;118.767413,32.041544;108.940174,34.261124;117.000772,36.668447;114.305393,30.593019;121.499763,31.233708;113.640097,34.749539;117.283042,31.861270;106.551556,29.563009;126.642464,45.756967;112.938814,28.227795;120.374737,36.064817;118.168900,24.489234;119.296494,26.074478;91.111891,29.662062"
```

> **业务逻辑说明**：批量接口内部将分号分隔的坐标直接替换为管道符 `|` 拼接后发送给高德 API，高德返回 `regeocodes` 数组，结果与请求坐标一一对应。该接口未添加重试、限流等逻辑，适用于示例演示场景。

## 高德地图 Key 申请指南

### 1. 注册高德开放平台账号

访问 [高德开放平台](https://lbs.amap.com/) 并注册/登录账号。

### 2. 创建应用

1. 进入 [控制台](https://console.amap.com/dev/key/app)
2. 点击「创建新应用」
3. 填写应用名称（如：spring-boot-geo）
4. 选择应用类型：「Web服务」
5. 点击「提交」

### 3. 添加 Key

1. 在应用详情页，点击「添加 Key」
2. Key 名称：自定义（如：spring-boot-geo-key）
3. 选择服务平台：「Web服务」
4. 绑定域名/IP（可选，建议配置为服务器 IP 或域名，如：127.0.0.1）
5. 点击「提交」

### 4. 开启 Web 服务 API

1. 在应用详情页，找到「Web服务」
2. 点击「添加服务」
3. 勾选以下服务：
   - **逆地理编码** (必选)
   - 地理编码 (可选)
4. 点击「提交」

### 5. 配置 Key

复制生成的 Key，配置到项目中：

**方式一：application.yml**
```yaml
amap:
  key: your-amap-key-here
```

**方式二：环境变量（推荐，生产环境更安全）**
```bash
export AMAP_KEY=your-amap-key-here
```

项目启动时会自动读取 `AMAP_KEY` 环境变量，优先级高于配置文件。

## 运行项目

```bash
# 编译
mvn clean package -DskipTests

# 运行（需设置 AMAP_KEY 环境变量）
AMAP_KEY=your-key java -jar target/spring-boot-geo-2.7.0.jar
```

或直接在 IDEA 中运行 `GeoApplication` 主类（需在 Run Configuration 中设置环境变量 `AMAP_KEY`）。

## 测试示例

```bash
# 查询详细地址
curl "http://127.0.0.1:8080/api/geo/regeo?longitude=116.391275&latitude=39.906218"

# 批量查询（分号分隔坐标）
curl "http://127.0.0.1:8080/api/geo/batch?locations=116.391275,39.906218;121.473701,31.230416"
```

## 注意事项

1. 高德地图 Web 服务 API 有调用量限制，免费额度为 30000 次/天
2. 请妥善保管 Key，不要泄露到前端代码或公开仓库
3. 生产环境建议配置 IP 白名单或域名白名单
4. 坐标系为 GCJ-02（火星坐标系），如使用 GPS 原始坐标（WGS-84）需先转换
5. 批量接口单次最多20个坐标，用分号分隔（`;`），坐标内部用逗号（`,`）

## 依赖版本

- JDK 1.8
- Spring Boot 2.7.0
- Hutool 5.7.22

## 批量逆地理编码（数据抽取 + 回写）

从 `t_farm_land`（老系统）和 `hr_bd_land`（V1系统）中抽取土地坐标，通过高德逆地理编码批量回写行政区划等信息到临时表。

### 执行步骤

```bash
# 第一步：抽取数据并重建临时表（会清空之前的数据）
mvn clean package -DskipTests
java -jar target/spring-boot-geo-2.7.0.jar extract

# 第二步：批量逆地理编码回写
java -jar target/spring-boot-geo-2.7.0.jar geocode
```

> 两步必须按顺序执行，`extract` 会 DROP + CREATE 临时表，之前回写的数据会丢失。

### 回写字段说明

每条记录回写以下 10 个字段：

| 字段 | 说明 | 示例 |
|------|------|------|
| `adcode` | 行政区划编码 | 620922 |
| `formatted_address` | 格式化完整地址 | 甘肃省酒泉市瓜州县南岔镇... |
| `country` | 国家 | 中国 |
| `province` | 省份 | 甘肃省 |
| `city` | 城市 | 酒泉市 |
| `district` | 区县 | 瓜州县 |
| `township` | 乡镇/街道 | 南岔镇 |
| `citycode` | 城市编码 | 0937 |
| `towncode` | 乡镇/街道编码 | 620922103000 |

### 涉及的临时表

| 表名 | 说明 |
|------|------|
| `tmp_all_coded_land` | 有编码的土地合并表（老系统优先） |
| `tmp_old_uncoded_land` | 老系统无编码数据 |
| `tmp_v1_uncoded_land` | V1系统无编码数据 |

### 同步到源表的 SQL

项目根目录下 `sql/` 文件夹包含3个同步脚本，用于将临时表中的 `formatted_address` 和 `towncode` 回写到源表：

| 文件 | 说明 |
|------|------|
| `01_update_by_code.sql` | 按 code 匹配更新两个库（`t_farm_land` + `hr_bd_land`） |
| `02_update_old_system_by_id.sql` | 老系统专用，按 id 匹配更新 `t_farm_land` |
| `03_update_v1_system_by_id.sql` | V1系统专用，按 id 匹配更新 `hr_bd_land` |

### 限流配置

默认配置（`AmapBatchGeoService.java`）：

| 参数 | 默认值 | 说明 |
|------|--------|------|
| `BATCH_SIZE` | 20 | 每批发送坐标数（高德上限） |
| `MAX_CONCURRENT` | 2 | 并发线程数 |
| `DELAY_MS` | 300 | 批次间延迟（毫秒） |
| `MAX_REQUESTS_PER_RUN` | 2000 | 单次运行最大请求数 |
| `MAX_RETRIES` | 3 | 失败重试次数 |
