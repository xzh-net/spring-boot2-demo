# MyBatis-Plus 代码生成脚手架

基于 Spring Boot 2.x + MyBatis-Plus 的后端代码生成脚手架，支持快速生成完整的 CRUD 代码结构。

## 技术特性

- **Velocity 模板引擎**：灵活的代码生成模板
- **SpringDoc OpenAPI**：自动生成 API 文档
- **MyBatis-Plus IService**：增删改查统一使用 Service 层实现
- **雪花算法 ID**：全局唯一 ID 生成
- **逻辑删除支持**：MyBatis-Plus 逻辑删除配置
- **自动填充**：`createTime`、`updateTime`、`delFlag` 字段自动填充
- **AuditLog 切面**：审计日志注解支持
- **Jackson 多格式时间解析**：支持 ISO-8601、`yyyy-MM-dd HH:mm:ss` 等多种格式

## 项目结构

```
src/main/java/net/xzh/generator/
├── common/                          # 通用模块
│   ├── enums/                       # 枚举类 (BusinessType, CodeEnum)
│   ├── utils/                       # 工具类 (Convert, GenUtils)
│   └── vo/                          # 值对象 (BasePage, PageResult, Result)
├── controller/                      # 控制层
├── framework/                       # 框架层
│   ├── aspectj/                     # AOP 切面
│   │   ├── annotation/              # 注解定义 (AuditLog)
│   │   └── aspect/                  # 切面实现 (AuditLogAspect)
│   ├── config/                      # 配置类
│   │   ├── properties/              # 配置属性 (IgnoreUrlsProperties)
│   │   ├── JacksonConfig.java       # Jackson 配置
│   │   ├── MybatisPlusConfig.java   # MyBatis-Plus 配置
│   │   ├── MyMetaObjectHandler.java # 自动填充处理器
│   │   ├── SecurityConfig.java      # Spring Security 配置
│   │   └── WebMvcConfig.java        # Web MVC 配置
│   ├── entity/                      # 基础实体类 (SuperEntity)
│   ├── repository/                  # 基础 Mapper (SuperMapper)
│   └── service/                     # 基础 Service (SuperService)
├── mapper/                          # 数据访问层
├── model/                           # 数据模型层
│   ├── convert/                     # 对象转换 (MapStruct)
│   ├── dto/                         # 数据传输对象
│   ├── entity/                      # 数据库实体 (DO)
│   ├── request/                     # 请求参数
│   └── response/                    # 响应结果
├── service/                         # 业务逻辑层
│   └── impl/                        # 服务实现类
└── GeneratorApplication.java        # 启动类
```

## 代码规范

| 规范项 | 要求 |
|--------|------|
| 实体类后缀 | 使用 `DO`（Data Object） |
| 时间字段类型 | `java.time.LocalDateTime` |
| ID 生成策略 | 全局 `ASSIGN_ID`（雪花算法） |
| 数据库字段映射 | `head_img_url` → `headImgUrl` |
| JSON 序列化格式 | `yyyy-MM-dd HH:mm:ss` |

## 使用方式

### 1. 代码生成

访问以下 URL 生成指定表的代码：

```
http://localhost:8080/generator/code?tables=sys_user
```

### 2. API 文档

启动项目后访问 Swagger UI：

```
http://localhost:8080/swagger-ui/index.html
```

## 核心配置说明

### 自动填充字段

`MyMetaObjectHandler` 自动填充以下字段：

- `createTime`：INSERT 时自动填充当前时间
- `updateTime`：INSERT 和 UPDATE 时自动填充当前时间
- `delFlag`：INSERT 时默认填充 `0`

### Jackson 时间格式

支持多种时间格式反序列化：
- `ISO_LOCAL_DATE_TIME`
- `ISO_OFFSET_DATE_TIME`
- `yyyy-MM-dd HH:mm:ss`
- `yyyy-MM-dd HH:mm:ss.SSS`
- `yyyy-MM-dd`

输出格式统一为：`yyyy-MM-dd HH:mm:ss`