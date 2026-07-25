# MyBatis-Plus 代码生成【无认证】

该项目同其它开发脚手架的主要封装区别：增加了`SuperMapper`和`SuperServiceImpl`的扩展。分别可以扩展逻辑恢复、物理删除，以及幂等+分布式锁的处理

1. 使用Velocity模板生成代码

2. SpringDoc整合

3. MyBatis-Plus增删改使用IService实现

4. 雪花ID精度丢失处理

5. MySQL建表示例

6. 常用工具类

7. 优化了SecurityConfig，使用流式 API 和 Lambda 表达式

8. VO按照使用场景划分为：提交表单对象，查询条件对象，详情对象，列表对象，分页对象五大类

   优点：

   a. 划分清晰，避免一个VO承载过多的职责
   b. 场景化设计，安全考虑只看必要字段

   潜在问题：

   a. 类爆炸，每个实体都需要4-5个VO类，维护成本高
   b. 转换逻辑复杂重复

   建议：字段差异明显的业务场景，中大型项目，对安全性要求较高的系统使用该方案


## 模板下载
http://localhost:8080/generator/code?tables=sys_user

## 文档地址
http://localhost:8080/swagger-ui/index.html


