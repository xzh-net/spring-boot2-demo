# Jexl 表达式引擎

## 1. SPEL（Spring Expression Language）

Spring 框架的官方表达式语言，深度集成于 Spring 生态（如 @Value 注解、XML 配置、Security 权限表达式）。

编译型表达式，性能较高（Spring 5 后支持运行时编译），默认受限于 Spring 的安全上下文，可限制访问权限。

典型场景：Spring 配置文件、注解中的动态值注入、条件化 Bean 初始化

## 2. OGNL（Object-Graph Navigation Language）

早期广泛用于 Apache Struts 2 的表达式语言，用于数据绑定和视图层（如 JSP 标签）。

解释型执行，性能低于 SPEL。因历史漏洞（如 Struts 2 的远程代码执行漏洞）而声名狼藉，需严格限制表达式来源。随着 Struts 2 的没落，使用率下降，逐渐被其他技术替代。

## 3. Groovy

完整的动态编程语言（JVM 语言），支持脚本和类 Java 语法，远超简单表达式求值。

动态类型特性导致性能低于 Java，但可通过 `@CompileStatic` 注解优化。需通过沙箱（如 `GroovyShell` 的安全策略）限制危险操作。

典型场景：  规则引擎、动态脚本（如 Jenkins Pipeline）、快速原型开发。

## 4. JEXL（Java Expression Language）

Apache 的轻量级表达式引擎，设计目标是简化 Java 代码中的表达式求值。

解释型执行，性能适中，适合低频次操作。无默认沙箱，需自行限制表达式内容。

典型场景：  配置文件中的动态值、模板引擎（如 Velocity）、规则表达式。