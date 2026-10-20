# Validation 参数校验

1. 全局拦截器捕获MethodArgumentNotValidException异常

2. 自定义注解实现指定字段是否在区间范围内

3. 验证信息增加了i18n的支持，需要在header中传递参数lang=en_US，拦截器中将@NotEmpty中消息KEY进行语言转换。

