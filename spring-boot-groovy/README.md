# Groovy 动态脚本

Groovy是一种基于Java平台的动态语言，支持静态类型和静态编译。广泛应用于脚本编写、领域特定语言（DSL）设计、元编程和函数式编程等领域。

## 静态脚本，手动New对象（main函数）

```java
public static void main(String[] args) {
    Binding groovyBinding = new Binding();
    GroovyShell groovyShell = new GroovyShell(groovyBinding);

    String scriptContent = "import net.xzh.groovy.service.UserService; \n"
    + "def query = new UserService().testQuery(100L); \n"
    + "query";
    Script script = groovyShell.parse(scriptContent);
	System.out.println(script.run());
}
```

## 静态脚本，从环境变量中获取（main函数）

```java
public static void main(String[] args) {
    Binding groovyBinding = new Binding();
    groovyBinding.setVariable("userService", new UserService());
    GroovyShell groovyShell = new GroovyShell(groovyBinding);

    String scriptContent = "def query = userService.testQuery(200L); \n"
    + "query";
    Script script = groovyShell.parse(scriptContent);
    System.out.println(script.run());
}
```

## 动态脚本，从容器中自动获取

```groovy
import net.xzh.groovy.model.User;
import net.xzh.groovy.service.UserService;
import java.util.List;
import java.util.Random;

// 直接使用已注入的testService变量
def userList = userService.list;

// 方法执行
if(userList == null || userList.size() == 0){
    userService.getUser();
    userList = userService.list;
}

for (def i = 0; i < userList.size(); i++) {
    // 根据年龄配置随机数
    if(userList.get(i).getAge() < 25){
        userList.get(i).setRandom(new Random().nextInt(10));
    } else {
        userList.get(i).setRandom(new Random().nextInt(20) + new Random().nextInt(10));
    }
}
userService.sendDataS(userList);

return "执行成功";
```

## 动态脚本，从容器中手动获取

```groovy
import net.xzh.groovy.model.User;
import net.xzh.groovy.service.UserService;
import java.util.List;
import java.util.Random;

// 使用SpringContextUtil获取UserService
UserService userService = SpringContextUtil.getBean(UserService.class);

def userList = userService.list;

// 方法执行
if(userList == null || userList.size() == 0){
    userService.getUser();
    userList = userService.list;
}

for (def i = 0; i < userList.size(); i++) {
    // 根据年龄配置随机数
    if(userList.get(i).getAge() < 25){
        userList.get(i).setRandom(new Random().nextInt(10));
    } else {
        userList.get(i).setRandom(new Random().nextInt(20) + new Random().nextInt(10));
    }
}
userService.sendDataS(userList);

return "执行成功";
```

