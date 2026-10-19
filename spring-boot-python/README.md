# Process 方式调用 Python3

演示 Spring Boot 通过 **ProcessBuilder 启动本机 Python3 子进程** 调用 Python 脚本的示例项目。
支持完整的 Python3 语法，可直接使用 pip 安装的第三方库（如 numpy、pandas）。

## 技术栈

- Java 8
- Spring Boot 2.7.0
- Python 3.x（本机安装，通过子进程调用）

## 环境要求

- JDK 1.8+
- Maven 3.x（或直接使用项目自带的 `mvnw`）
- Python 3.6+

## Python3 安装说明

### Windows

1. 打开 https://www.python.org/downloads/ 下载 Python 3.x 安装包（如 3.12.x）
2. 运行安装包，**务必勾选 "Add Python to PATH"**，然后选择 "Install Now"
3. 验证安装（新开一个 cmd / PowerShell 窗口）：

```bash
python --version
# 输出类似：Python 3.12.4
```

> 如果 `python` 命令打开的是 Microsoft Store，请在「应用执行别名」中关闭 `python.exe` / `python3.exe` 的商店别名，
> 或在 `application.yml` 中把 `python.command` 改成实际的 python 路径。

### macOS

方式一（Homebrew，推荐）：

```bash
brew install python3
python3 --version
```

方式二：从 https://www.python.org/downloads/ 下载 macOS 安装包安装。

### Linux（以 Ubuntu/Debian 为例）

```bash
sudo apt update
sudo apt install -y python3
python3 --version
```

CentOS/RHEL：

```bash
sudo yum install -y python3
```

### 配置解释器命令

不同系统下命令可能是 `python` 或 `python3`，在 `src/main/resources/application.yml` 中配置：

```yaml
python:
  command: python        # Windows 常见；Linux/macOS 一般改为 python3
  script-dir: D:\xuzhihao\spring-boot2-demo\spring-boot-python\script   # 脚本目录（绝对路径）
  timeout-seconds: 10
```

## 项目结构

```
spring-boot-python
├── pom.xml
├── script
│   └── add.py                           # 演示用 Python3 脚本（改完实时生效）
├── test.http                            # IDEA HTTP Client 测试脚本
└── src
    ├── main
    │   ├── java/net/xzh/python
    │   │   ├── PythonApplication.java       # 启动类
    │   │   ├── controller
    │   │   │   └── PythonController.java    # 接口层
    │   │   └── service
    │   │       └── PythonScriptService.java # ProcessBuilder 调用 Python3
    │   └── resources
    │       └── application.yml
    └── test
        └── java/net/xzh/python
            └── PythonApplicationTests.java
```

## 运行方式

```bash
# Windows
mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

启动成功后，服务运行在 `http://localhost:8080`。

## 接口说明

| 接口 | 方法 | 说明 | 参数 |
|------|------|------|------|
| `/api/python/execute` | POST | 执行一段 Python 代码块（`python -c`），输出 `hello world` | 无 |
| `/api/python/compute` | POST | 向 Python 传参并获取结果 | `a`（默认 122）、`b`（默认 23）、`code`（可选） |

`/compute` 支持两种用法：

1. **固定函数调用**：不传 `code`，执行预置脚本 `add.py`，适合算法固定、参数动态的场景
2. **公式计算 / 模板渲染**：传入 `code`（函数或模板内容），在内存中直接执行。
   函数无需落盘为 `.py` 文件，可由 Java 侧**从数据库查出、或在内存中动态拼接**后传入。
   注意：允许执行任意代码，生产环境需做白名单或沙箱限制。

### 示例

#### 1. 执行 Python 代码块

```bash
curl -X POST http://localhost:8080/api/python/execute
```

响应：

```json
{
  "success": true,
  "result": "hello world"
}
```

#### 2. 固定函数调用（执行预置脚本 add.py）

```bash
curl -X POST "http://localhost:8080/api/python/compute?a=122&b=23"
```

响应：

```json
{
  "success": true,
  "result": "145"
}
```

#### 3. 公式/模板场景：函数内容来自内存或数据库，无需落盘 .py 文件

```bash
curl -X POST -d "code=def add(a, b): return a * b" "http://localhost:8080/api/python/compute?a=5&b=10"
```

响应：

```json
{
  "success": true,
  "result": "50"
}
```

也可以直接使用 IDEA 打开根目录下的 `test.http` 文件一键测试。

## 实现原理

1. 根据配置 `python.script-dir` 定位脚本目录，直接加载并执行其中的脚本（如 `script/add.py`），
   同时将子进程**工作目录**设置为脚本目录，脚本可 `import` 同目录模块、读取同目录配置文件
2. 通过 `ProcessBuilder` 启动本机 Python3 子进程执行：`python add.py 5 10`
3. 守护线程持续消费子进程输出（合并 stderr，防止管道堵塞），设置 UTF-8 编码环境变量防乱码
4. 读取输出作为接口返回值；超时或非 0 退出码视为执行失败

脚本放在外部目录、不打包进 jar，修改脚本后**实时生效**，无需重新打包发布，
适合公式计算、模板渲染等需要频繁调整脚本的场景。生产环境将 `script-dir` 指向服务器上的固定目录即可：

```yaml
python:
  command: python3
  script-dir: /opt/scripts   # Linux 示例；Windows 例如 D:/scripts
```

## 常见问题

**Q: 报错 `Cannot run program "python"`？**
本机未安装 Python3，或命令名不是 `python`。安装后修改 `application.yml` 中的 `python.command`（Linux/macOS 通常为 `python3`）。

**Q: 想用第三方库（numpy 等）？**
直接用 pip 安装即可，子进程会使用同一个解释器环境：

```bash
pip install numpy
```
