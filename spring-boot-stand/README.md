# Spring Boot 快速入门示例
> 基于 Spring Boot 2.7.0 开发

## 主要功能

1. thymeleaf模板
2. i18n
3. webjars
4. 请求白名单
5. banner设置
6. 获取数据常用6种方式

## 访问地址

http://127.0.0.1:8080/cms


## commons-lang3

- Java 语言核心扩展：提供对 Java 标准库的增强功能
- 工具类集合：字符串处理、数组操作、对象工具等

```java
// 字符串工具
StringUtils.isBlank("  ");        // 检查空字符串
StringUtils.substring("hello", 2); // 子字符串
StringUtils.join(array, ",");     // 数组拼接

// 对象工具
ObjectUtils.defaultIfNull(obj, defaultValue); // 空值处理
ObjectUtils.toString(obj);                    // 安全toString

// 数组工具
ArrayUtils.contains(array, value);            // 数组包含检查
ArrayUtils.add(array, element);               // 数组添加元素

// 数字工具
NumberUtils.toInt("123", 0);                  // 安全转换
NumberUtils.isCreatable("123.45");            // 数字验证

// 随机工具
RandomStringUtils.randomAlphanumeric(10);     // 生成随机字符串

// 构建器模式
ToStringBuilder.reflectionToString(obj);      // 反射toString
EqualsBuilder.reflectionEquals(obj1, obj2);   // 反射equals
```



## commons-io

- I/O 操作工具：文件、流、目录操作
- 输入输出增强：简化 Java I/O API 的使用

```java
// 文件工具
FileUtils.copyFile(srcFile, destFile);        // 文件复制
FileUtils.readFileToString(file, "UTF-8");    // 读取文件内容
FileUtils.writeStringToFile(file, content, "UTF-8"); // 写入文件

// 流工具
IOUtils.copy(inputStream, outputStream);      // 流复制
IOUtils.toString(inputStream, "UTF-8");       // 流转字符串
IOUtils.closeQuietly(stream);                 // 安静关闭流

// 文件名工具
FilenameUtils.getExtension("file.txt");       // 获取扩展名
FilenameUtils.concat("/path", "file.txt");    // 路径拼接

// 文件监控
FileAlterationMonitor monitor = new FileAlterationMonitor(1000);
FileAlterationObserver observer = new FileAlterationObserver(directory);
```