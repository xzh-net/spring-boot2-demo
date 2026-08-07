# RSA + AES 混合加密 Demo

基于 Spring Boot 2.7.0 实现的 RSA + AES 混合加密数据传输演示项目，通过 Spring `RequestBodyAdvice` / `ResponseBodyAdvice` 切面机制实现请求解密和响应加密的透明化，业务代码无需感知加密细节。

## 目录

- [加密方案](#加密方案)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [快速开始](#快速开始)
- [接口说明](#接口说明)
- [加密流程](#加密流程)
- [核心组件](#核心组件)

## 加密方案

采用 **RSA + AES 混合加密**（非对称 + 对称）的经典方案：

| 环节 | 算法 | 作用 |
|---|---|---|
| AES 会话密钥 | 随机生成 32 字节字符串 | 加密业务数据（每请求一密） |
| AES 数据加密 | AES/CBC/PKCS5Padding | 加密请求体 / 响应体 |
| AES 密钥传输 | RSA/ECB/PKCS1Padding（2048 位） | 客户端用服务端 RSA 公钥加密 AES 密钥后随请求一起发送 |
| 完整性校验 | HmacSHA256 | 用 AES 密钥对加密数据做 HMAC 签名，防止篡改 |

为什么混合？

- **AES 对称加密速度快**，适合加密大量业务数据
- **RSA 非对称加密安全性高**，但速度慢，适合加密小数据量的 AES 密钥
- 两者结合：用 RSA 安全地传递 AES 密钥，用 AES 高效地加密实际数据

## 技术栈

- Java 8
- Spring Boot 2.7.0
- Jackson（JSON 序列化）
- CryptoJS + JSEncrypt（前端加密库）

## 项目结构

```
src/main/java/net/xzh/rsa/
├── RsaAesApplication.java          # 启动类
├── advice/                          # 请求/响应切面（核心）
│   ├── CryptoContext.java           # ThreadLocal 暂存 AES 密钥
│   ├── DecryptRequestBodyAdvice.java   # 请求体解密切面
│   └── EncryptResponseBodyAdvice.java # 响应体加密切面
├── config/                          # 配置类
│   ├── CryptoConfig.java            # 注册 KeyManager Bean
│   └── GlobalExceptionHandler.java # 全局异常处理
├── controller/                      # 控制器
│   ├── CryptoController.java       # 公钥获取接口
│   └── UserController.java          # 业务接口（演示加解密透明处理）
├── crypto/                          # 加密工具层
│   ├── AesUtils.java                # AES 加解密
│   ├── KeyManager.java              # RSA 密钥对管理
│   ├── RsaUtils.java                # RSA 加解密
│   └── SignUtils.java               # 签名/验签
└── dto/                             # 数据传输对象
    ├── CryptoRequest.java           # 加密请求体结构
    ├── CryptoResponse.java          # 加密响应体结构
    ├── User.java                    # 用户实体
    └── UserListResponse.java        # 用户列表响应结构

src/main/resources/
├── application.yml                  # 应用配置
└── static/index.html                # 前端测试页面
```

## 快速开始

### 环境要求

- JDK 1.8+
- Maven 3.6+

### 运行

```bash
# 克隆项目后进入目录
cd spring-boot-rsa

# Maven 运行
mvn spring-boot:run

# 或先打包再运行
mvn clean package -DskipTests
java -jar target/rsa-aes-1.0.0.jar
```

启动成功后访问：

- **前端测试页面**：http://localhost:8080/index.html
- **获取公钥接口**：http://localhost:8080/api/publicKey

前端页面内置完整的加密流程演示，点击按钮即可看到每一步的明文、密文和签名。

## 接口说明

### 1. 获取 RSA 公钥

```
GET /api/publicKey
```

响应：
```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "publicKey": "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8A..."
  }
}
```

### 2. 业务接口（加密通信）

```
POST /api/user/list
Content-Type: application/json
```

请求体（加密格式）：
```json
{
  "encryptedKey": "IopDD5xSm/77hX/YcmUPhcwtH...",   // RSA 加密的 AES 密钥
  "encryptedData": "nesPiAk+skzTWTtIaGSs2z...",   // AES 加密的业务数据
  "iv": "4rjoVvZApVE8scmA",                       // AES 初始向量
  "sign": "2ga+bbp+iQMd38P8VfdIkaY3V7ZxAFF..."    // HMAC 签名
}
```

其中 `encryptedData` 解密后为：`{"query":"users"}`

响应体（加密格式）：
```json
{
  "encryptedData": "...",   // AES 加密的业务响应
  "iv": "...",              // AES 初始向量
  "sign": "..."             // HMAC 签名
}
```

其中 `encryptedData` 解密后为：
```json
{
  "code": 0,
  "message": "ok",
  "data": [
    {"id": 1, "name": "Alice", "email": "alice@example.com", "age": 25},
    {"id": 2, "name": "Bob", "email": "bob@example.com", "age": 30},
    {"id": 3, "name": "Charlie", "email": "charlie@example.com", "age": 35},
    {"id": 4, "name": "Diana", "email": "diana@example.com", "age": 28},
    {"id": 5, "name": "Eve", "email": "eve@example.com", "age": 22}
  ]
}
```

## 加密流程

### 请求加密流程（客户端 → 服务器）

```
┌─────────────────────────────────────────────────────────────────┐
│  客户端                                                         │
│                                                                 │
│  1. 生成随机 AES 密钥 (32字节) + IV (16字节)                     │
│  2. RSA(公钥) 加密 AES 密钥 → encryptedKey                       │
│  3. AES(密钥+IV) 加密业务数据 → encryptedData                    │
│  4. HMAC-SHA256(encryptedData, AES密钥) → sign                 │
│  5. 发送 {encryptedKey, encryptedData, iv, sign}                │
└──────────────────────────────┬──────────────────────────────────┘
                               │ HTTP POST JSON
                               ▼
┌─────────────────────────────────────────────────────────────────┐
│  DecryptRequestBodyAdvice（请求体解密切面）                       │
│                                                                 │
│  1. 解析请求体为 CryptoRequest                                   │
│  2. RSA(私钥) 解密 encryptedKey → AES 密钥                       │
│  3. 校验 HMAC 签名（防止数据篡改）                                │
│  4. AES(密钥+IV) 解密 encryptedData → 明文请求体                  │
│  5. AES 密钥存入 CryptoContext（ThreadLocal）                     │
└──────────────────────────────┬──────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────┐
│  UserController.getUserList()   ← 业务代码，完全不感知加密！      │
└──────────────────────────────┬──────────────────────────────────┘
```

### 响应加密流程（服务器 → 客户端）

```
┌─────────────────────────────────────────────────────────────────┐
│  EncryptResponseBodyAdvice（响应体加密切面）                      │
│                                                                 │
│  1. 从 CryptoContext 取出 AES 密钥                               │
│  2. 生成随机新 IV（响应有自己的 IV）                               │
│  3. AES(密钥+IV) 加密业务响应 → encryptedData                    │
│  4. HMAC-SHA256(encryptedData, AES密钥) → sign                 │
│  5. 返回 {encryptedData, iv, sign}                               │
│  6. finally → CryptoContext.clear() 清理 ThreadLocal             │
└──────────────────────────────┬──────────────────────────────────┘
                               │ HTTP JSON Response
                               ▼
┌─────────────────────────────────────────────────────────────────┐
│  客户端                                                         │
│                                                                 │
│  1. 校验 HMAC 签名（用会话 AES 密钥）                             │
│  2. AES(密钥+IV) 解密 encryptedData → 明文响应                   │
│  3. 拿到业务数据                                                 │
└─────────────────────────────────────────────────────────────────┘
```

## 核心组件

### DecryptRequestBodyAdvice

实现 `RequestBodyAdvice` 接口，在 Controller 方法执行前拦截请求体。

> **注意**：Spring 的 `RequestBodyAdvice` 只有在 Controller 方法有 `@RequestBody` 参数时才会触发。

### EncryptResponseBodyAdvice

实现 `ResponseBodyAdvice` 接口，在 Controller 方法执行后加密响应体。通过检查 `CryptoContext` 是否有 AES 密钥来判断是否需要加密（非加密请求会透传）。

### CryptoContext

基于 `ThreadLocal` 的 AES 密钥暂存器，在一次请求链路中把密钥从解密切面传递给加密切面。请求结束后必须 `clear()` 防止线程池场景下的密钥串用和内存泄漏。

### KeyManager

Spring Bean，启动时自动生成 2048 位 RSA 密钥对。当前每次应用重启会重新生成密钥对，可扩展为从配置文件或密钥管理服务加载持久化密钥。

## 扩展方向

- **密钥持久化**：将 RSA 密钥对存储到配置文件或 HashiCorp Vault 等密钥管理服务，避免重启后客户端公钥失效
- **会话密钥方案**：改成登录时生成 AES 会话密钥并存 Redis，后续请求通过 token 关联，减少每次 RSA 加解密开销
- **防重放攻击**：在请求体中加入时间戳 + nonce，服务器端校验时间窗口和 nonce 唯一性
- **双向认证**：客户端也持有 RSA 密钥对，服务端用客户端公钥加密 AES 密钥返回
