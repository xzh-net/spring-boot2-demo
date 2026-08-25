# Spring Boot Geo - 高德地图逆地理编码服务

通过坐标查询当前地点对应的行政区划编码。

## 功能特性

- 逆地理编码：根据经纬度查询详细地址信息
- 获取行政区划编码（adcode）
- 无需数据库连接

## API 接口

### 1. 逆地理编码查询详细地址

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
        "citycode": "010"
      }
    }
  },
  "timestamp": 1787621771794
}
```

### 2. 获取行政区划编码

```
GET /api/geo/adcode
```

**参数：**
- `longitude` (String, 必填) - 经度
- `latitude` (String, 必填) - 纬度

**响应示例：**
```json
{
  "code": 200,
  "message": "success",
  "data": "110102",
  "timestamp": 1787621821870
}
```

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
AMAP_KEY=your-key java -jar target/spring-boot-geo.jar
```

或直接在 IDEA 中运行 `GeoApplication` 主类（需在 Run Configuration 中设置环境变量 `AMAP_KEY`）。

## 测试示例

```bash
# 查询天安门坐标的行政区划编码
curl "http://127.0.0.1:8080/api/geo/adcode?longitude=116.391275&latitude=39.906218"

# 查询详细地址
curl "http://127.0.0.1:8080/api/geo/regeo?longitude=116.391275&latitude=39.906218"
```

## 注意事项

1. 高德地图 Web 服务 API 有调用量限制，免费额度为 30000 次/天
2. 请妥善保管 Key，不要泄露到前端代码或公开仓库
3. 生产环境建议配置 IP 白名单或域名白名单
4. 坐标系为 GCJ-02（火星坐标系），如使用 GPS 原始坐标（WGS-84）需先转换

## 依赖版本

- JDK 1.8
- Spring Boot 2.7.0
- Hutool 5.7.22