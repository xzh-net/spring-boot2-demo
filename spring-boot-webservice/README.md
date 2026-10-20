# CXF WebService 服务


1. 通过@EnableWs注解触发扫描，扫描指定包下被@Service注解的类，并解析其中被@SoapMapping注解的方法，将funcId与对应的Bean和方法信息映射关系缓存起来（在WsComponentScanRegistrar中完成）。

2. 配置WebService（WebServiceConfig）时，将CommonService实现类CommonServiceImp暴露为SOAP服务，并添加了一个认证拦截器AuthInterceptor。

3. 当SOAP请求到来时，首先经过AuthInterceptor进行认证（检查SOAP头中的用户名和密码）。

4. 认证通过后，请求到达CommonServiceImp的doServices方法，该方法根据传入的action（即funcId）从映射关系中找到对应的Bean和方法，然后利用反射调用该方法，并将结果返回。
