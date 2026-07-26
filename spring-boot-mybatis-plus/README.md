# MyBatis-Plus 代码生成【无认证】

该项目同其它开发脚手架的主要封装区别：增加了`SuperMapper`和`SuperServiceImpl`的扩展。分别可以扩展逻辑恢复、物理删除，以及幂等+分布式锁的处理

1. 使用Velocity模板生成代码

2. SpringDoc整合

3. MyBatis-Plus增删改使用IService实现

4. 雪花ID精度丢失处理

5. MySQL建表示例

6. 常用工具类

7. 优化了SecurityConfig，使用流式 API 和 Lambda 表达式

8. 代码目录结构规范

   生成的代码遵循行业标准的分层架构：

   ```
   src/main/java/net/xzh/generator/
   ├── common/                    # 通用模块
   │   ├── constant/              # 常量定义
   │   ├── core/                  # 核心工具类
   │   ├── model/                 # 通用模型（CommonResult, CommonPage等）
   │   └── utils/                 # 工具类
   ├── config/                    # 配置类
   │   └── properties/            # 配置属性类
   ├── controller/                # 控制层 (API接口)
   ├── service/                   # 业务逻辑层
   │   └── impl/                  # 服务实现类
   ├── mapper/                    # 数据访问层 (Mapper/Repository)
   ├── model/                     # 数据模型层 (核心实体与数据流转对象)
   │   ├── entity/                # 数据库表映射对象 (DO)
   │   ├── dto/                   # 数据传输对象 (DTO)
   │   ├── request/               # 接口入参对象 (Req)
   │   ├── convert/               # 对象转换层 (MapStruct/BeanUtils等)
   │   └── response/              # 接口出参对象 (Resp)
   └── GeneratorApplication.java  # 启动类
   ```

   **各层职责说明：**

   | 层级 | 说明 | 典型类名 |
   |------|------|----------|
   | controller | REST API控制器 | UserController |
   | service | 业务接口定义 | UserService |
   | service/impl | 业务逻辑实现 | UserServiceImpl |
   | mapper | MyBatis Mapper接口 | UserMapper |
   | model/entity | 数据库实体，与表结构一一对应 | UserDO |
   | model/dto | 通用数据传输对象 | UserDTO |
   | model/request | HTTP请求参数 | UserSaveReq, UserPageQuery |
   | model/response | HTTP响应结果 | UserListResp, UserDetailResp |
   | model/convert | 对象转换接口（MapStruct） | UserConvert |

   优点：

   a. 划分清晰，避免一个类承载过多的职责
   b. 场景化设计，安全考虑只看必要字段
   c. 模型层集中管理，便于维护

   潜在问题：

   a. 类数量较多，每个实体需要多个DTO类
   b. 转换逻辑需要维护

   建议：字段差异明显的业务场景，中大型项目，对安全性要求较高的系统使用该方案


## 模板下载
http://localhost:8080/generator/code?tables=sys_user

## 文档地址
http://localhost:8080/swagger-ui/index.html


