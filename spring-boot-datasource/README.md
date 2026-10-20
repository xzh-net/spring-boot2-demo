# 多数据源动态切换

dynamic-datasource-spring-boot-starter注解实现自动切换

## 第一个mysql数据库
```shell
docker run --name docker-mysql -e MYSQL_ROOT_PASSWORD=123456 -p 3306:3306 -d mysql
```

## 第二个mysql数据库
```shell
docker run --name docker-mysql-2 -e MYSQL_ROOT_PASSWORD=123456 -p 3307:3306 -d mysql
```

## 初始化表结构

```sql
CREATE TABLE `role` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `role_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` int(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```



# 动态数据源切换

基于扩展表维护数据源，手动进行切换

动态数据源管理

```sql
CREATE TABLE `dynamic_datasource_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `datasource_key` varchar(100) NOT NULL COMMENT '数据源标识键',
  `datasource_name` varchar(100) DEFAULT NULL COMMENT '数据源名称',
  `database_name` varchar(100) DEFAULT NULL COMMENT '数据库名',
  `url` varchar(500) DEFAULT NULL COMMENT '数据库URL',
  `username` varchar(100) DEFAULT NULL COMMENT '用户名',
  `password` varchar(200) DEFAULT NULL COMMENT '密码',
  `driver_class_name` varchar(100) DEFAULT NULL COMMENT '驱动类名',
  `host` varchar(100) DEFAULT NULL COMMENT '主机',
  `port` varchar(10) DEFAULT NULL COMMENT '端口',
  `is_active` tinyint(1) DEFAULT '1' COMMENT '是否激活',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_datasource_key` (`datasource_key`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COMMENT='动态数据源';
```

用户管理

```
CREATE TABLE `user` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `username` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `age` int(3) DEFAULT NULL,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```
