# Spring Security + JWT 权限校验

## 方式一：注解和配置类

1. 开启方法级权限控制

   ```java
   @EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
   @Configuration
   public class SecurityConfig {
   }
   ```

   

2. 添加注解

   ```java
   @PreAuthorize("@sss.hasPermi('6:admin:resetPwd')")
   @ApiOperation(value = "重置密码")
   @RequestMapping(value = "/resetPwd/{id}", method = RequestMethod.PUT)
   public CommonResult<?> resetPwd(@PathVariable Long id) {
       return CommonResult.success("重置成功");
   }
   ```

   

3. 创建工具类

   ```java
   /**
    * 自定义权限实现
    * 
    */
   @Service("sss")
   public class SpringSecurityService {
   ```

   




## 方式二：动态权限校验

动态权限扩展接口（DynamicSecurityService），本身不是Security的标准组件，而是一个常用于自定义、动态权限管理方案中的核心接口，通过调用 loadDataSource 等方法在运行时刷新规则，从而实现无需重启应用即可生效的权限管理

1. 开启DynamicSecurityService，增加过滤器
2. DynamicSecurityFilter 拦截过滤器
3. DynamicSecurityMetadataSource 提供权限规则
4. DynamicAccessDecisionManager 执行权限判断




## 总结对比

| 维度         | @PreAuthorize       | DynamicSecurityService |
| :----------- | :------------------ | :--------------------- |
| **控制层级** | 方法级别            | URL/请求级别           |
| **动态性**   | 较低（编码时确定）  | 高（运行时可调整）     |
| **维护性**   | 分散在代码中        | 集中管理               |
| **灵活性**   | 高（支持复杂SpEL）  | 中等（URL-权限映射）   |
| **性能**     | 按需检查，有AOP开销 | 每次请求检查，需缓存   |
| **适用场景** | 复杂业务逻辑权限    | REST API权限管理       |

**推荐策略**：大多数项目建议结合使用：

- 用 `DynamicSecurityService` 管理接口访问权限
- 用 `@PreAuthorize` 处理复杂的业务逻辑权限



## 常见问题

#### 问：为什么登录的代码出现两个login？

1. `login`标准化流程，使用 Spring Security 的 AuthenticationManager 进行认证，委托给 UserDetailsService 执行。

2. `login2`自定义流程，绕过 Spring Security 的认证管理器（SecurityConfig中声明），手动获取 UserDetails 并检查密码。

   参考：spring-boot-mybatis-plus。

#### 问：身份验证器两种方式

1. 声明式

   官方推荐，更简洁（见 spring-boot-activiti 代码示例）

   ```java
   @Bean
   public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
       AuthenticationManagerBuilder authenticationManagerBuilder = 
           http.getSharedObject(AuthenticationManagerBuilder.class);
       authenticationManagerBuilder
           .userDetailsService(userDetailsService())
           .passwordEncoder(passwordEncoder);
       return authenticationManagerBuilder.build();
   }
   ```

   

2. 编程式

   自定义多个Provider，需要精细控制（本工程包含该示例）

   ```java
   @Bean
   public AuthenticationManager authenticationManager(UserDetailsService userDetailsService,
                                                      PasswordEncoder passwordEncoder) {
       DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
       daoAuthenticationProvider.setUserDetailsService(userDetailsService);
       daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
       return new ProviderManager(daoAuthenticationProvider);
   }
   ```

   

## JSON Web Token

JWT、JWS、JWE 三者之间的关系，其实 JWT（JSON Web Token）指的是一种规范，这种规范允许我们使用 JWT 在两个组织之间传递安全可靠的信息。而JWS（JSON Web Signature）和 JWE（JSON Web Encryption）是 JWT 规范的两种不同实现，我们平时最常使用的实现就是 JWS。

jwt对称非对称使用方式：

- 对称加密（HMAC）
  - 使用相同的密钥进行签名和验证
- 非对称加密（RSA）
  - 加密场景：任何人都可以用公钥加密数据，只有私钥持有者能解密
  - 签名场景：私钥持有者生成签名，任何人都可以用公钥验证签名真实性

```bash
 keytool -genkey -alias jwt -keyalg RSA -keystore jwt.jks
 # 输入密码为123456，然后输入各种信息之后就可以生成证书jwt.jks
```



## 类库技术标准

| 特性           | nimbus-jose-jwt | jjwt     |
| :------------- | :-------------- | :------- |
| JWS签名算法    | 完整支持        | 完整支持 |
| JWE加密算法    | **完整支持**    | 有限支持 |
| 嵌套JWT        | **支持**        | 不支持   |
| Key Management | **强大支持**    | 基础支持 |
| 标准符合性     | **严格遵循RFC** | 实用主义 |



## 整合API文档

http://127.0.0.1:8080/swagger-ui/index.html



