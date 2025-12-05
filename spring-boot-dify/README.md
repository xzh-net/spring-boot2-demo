# WebClient 调用 Dify API

## 1. 业务场景

开发一个智能助手功能

## 2. 系统设计

- Dify 创建工作流 + 知识库实现 RAG
- Dify 创建多个智能体，每个智能体对应不同的子页面模块
- WebClient 调用 Dify API ，根据模块不同传递不同的智能体Key

调用流程图

![](doc/assets/1.png)

## 2. 技术对比

| 特性           | RestTemplate                              | WebClient                             |
| :------------- | :---------------------------------------- | :------------------------------------ |
| **编程模型**   | 同步阻塞                                  | 异步非阻塞（也支持同步）              |
| **技术栈**     | 基于Servlet API                           | 基于Reactive Stack（Project Reactor） |
| **并发模型**   | 每个请求一个线程                          | 事件循环，少量线程处理大量请求        |
| **依赖**       | spring-web模块                            | spring-webflux模块                    |
| **HTTP客户端** | 支持多种（如Apache HTTPClient、OkHttp等） | 基于Reactor Netty（也支持其他客户端） |
| **适用场景**   | 传统同步应用，简单HTTP调用                | 高并发、流式处理、响应式应用          |

在 Spring 框架中，WebClient 和 RestTemplate 都可以用于进行HTTP请求，但它们的设计和适用场景有所不同。RestTemplate 处于维护模式，推荐使用 WebClient 作为新的HTTP客户端

## 3. API 目录

Dify版本：1.7.1

### 3.1 发送对话消息

```bash
curl -X POST 'http://172.17.18.33/v1/chat-messages' \
--header 'Authorization: Bearer {api_key}' \
--header 'Content-Type: application/json' \
--data-raw '{
    "inputs": {},
    "query": "What are the specs of the iPhone 13 Pro Max?",
    "response_mode": "streaming",
    "conversation_id": "",
    "user": "abc-123",
    "files": [
      {
        "type": "image",
        "transfer_method": "remote_url",
        "url": "https://cloud.dify.ai/logo/logo-site.png"
      }
    ]
}'
```



### 3.2 上传文件

```bash
curl -X POST 'http://172.17.18.33/v1/files/upload' \
--header 'Authorization: Bearer {api_key}' \
--form 'file=@localfile;type=image/[png|jpeg|jpg|webp|gif]' \
--form 'user=abc-123'
```

响应值

```json
{
  "id": "72fa9618-8f89-4a37-9b33-7e1178a24a67",
  "name": "example.png",
  "size": 1024,
  "extension": "png",
  "mime_type": "image/png",
  "created_by": 123,
  "created_at": 1577836800,
}

```



### 3.3 停止响应

```bash
curl -X POST 'http://172.17.18.33/v1/chat-messages/:task_id/stop' \
-H 'Authorization: Bearer {api_key}' \
-H 'Content-Type: application/json' \
--data-raw '{ "user": "abc-123"}'
```



### 3.4 消息反馈（点赞）

```bash
curl -X POST 'http://172.17.18.33/v1/messages/:message_id/feedbacks \
--header 'Authorization: Bearer {api_key}' \
--header 'Content-Type: application/json' \
--data-raw '{
    "rating": "like",
    "user": "abc-123",
    "content": "message feedback information"
}'
```



### 3.5 获取APP的消息点赞和反馈

```bash
curl -X GET 'http://172.17.18.33/v1/app/feedbacks?page=1&limit=20'
```

响应值

```json
{
    "data": [
        {
            "id": "8c0fbed8-e2f9-49ff-9f0e-15a35bdd0e25",
            "app_id": "f252d396-fe48-450e-94ec-e184218e7346",
            "conversation_id": "2397604b-9deb-430e-b285-4726e51fd62d",
            "message_id": "709c0b0f-0a96-4a4e-91a4-ec0889937b11",
            "rating": "like",
            "content": "message feedback information-3",
            "from_source": "user",
            "from_end_user_id": "74286412-9a1a-42c1-929c-01edb1d381d5",
            "from_account_id": null,
            "created_at": "2025-04-24T09:24:38",
            "updated_at": "2025-04-24T09:24:38"
        }
    ]
}

```



### 3.6 获取下一轮建议问题列表

```bash
curl --location --request GET 'http://172.17.18.33/v1/messages/{message_id}/suggested?user=abc-123 \
--header 'Authorization: Bearer ENTER-YOUR-SECRET-KEY' \
--header 'Content-Type: application/json'
```



### 3.7 获取会话历史消息

```bash
curl -X GET 'http://172.17.18.33/v1/messages?user=abc-123&conversation_id=' \
--header 'Authorization: Bearer {api_key}'
```



### 3.8 获取会话列表

```bash
curl -X GET 'http://172.17.18.33/v1/conversations?user=abc-123&last_id=&limit=20'\
--header 'Authorization: Bearer {api_key}'
```



### 3.9 删除会话

```bash
curl -X DELETE 'http://172.17.18.33/v1/conversations/:conversation_id' \
--header 'Authorization: Bearer {api_key}' \
--header 'Content-Type: application/json' \
--data-raw '{ 
 "user": "abc-123"
}'
```



### 3.10 会话重命名

```bash
curl -X POST 'http://172.17.18.33/v1/conversations/:conversation_id/name' \
--header 'Authorization: Bearer {api_key}' \
--header 'Content-Type: application/json' \
--data-raw '{ 
 "name": "", 
 "auto_generate": true, 
 "user": "abc-123"
}'
```



### 3.11 获取对话变量

```bash
curl -X GET 'http://172.17.18.33/v1/conversations/{conversation_id}/variables?user=abc-123' \
--header 'Authorization: Bearer {api_key}'
```



### 3.12 语音转文字

```bash
curl -X POST 'http://172.17.18.33/v1/audio-to-text' \
--header 'Authorization: Bearer {api_key}' \
--form 'file=@localfile;type=audio/[mp3|mp4|mpeg|mpga|m4a|wav|webm]
```



### 3.13 文字转语音

```bash
curl --location --request POST 'http://172.17.18.33/v1/text-to-audio' \
--header 'Authorization: Bearer ENTER-YOUR-SECRET-KEY' \
--form 'text=你好Dify;user=abc-123;message_id=5ad4cb98-f0c7-4085-b384-88c403be6290
```



### 3.14 获取应用基本信息

```bash
curl -X GET 'http://172.17.18.33/v1/info' \
-H 'Authorization: Bearer {api_key}'
```

响应值

```json
{
  "name": "My App",
  "description": "This is my app.",
  "tags": [
    "tag1",
    "tag2"
  ],
  "mode": "chat",
  "author_name": "Dify"
}

```



### 3.15 获取应用参数

```bash
 curl -X GET 'http://172.17.18.33/v1/parameters'\
--header 'Authorization: Bearer {api_key}'
```

响应值

```bash
{
  "introduction": "nice to meet you",
  "user_input_form": [
    {
      "text-input": {
        "label": "a",
        "variable": "a",
        "required": true,
        "max_length": 48,
        "default": ""
      }
    },
    {
      // ...
    }
  ],
  "file_upload": {
    "image": {
      "enabled": true,
      "number_limits": 3,
      "transfer_methods": [
        "remote_url",
        "local_file"
      ]
    }
  },
  "system_parameters": {
      "file_size_limit": 15,
      "image_file_size_limit": 10,
      "audio_file_size_limit": 50,
      "video_file_size_limit": 100
  }
}

```



### 3.16 获取应用Meta信息

```bash
curl -X GET 'http://172.17.18.33/v1/meta' \
-H 'Authorization: Bearer {api_key}'
```

响应值

```
{
  "tool_icons": {
      "dalle2": "https://cloud.dify.ai/console/api/workspaces/current/tool-provider/builtin/dalle/icon",
      "api_tool": {
          "background": "#252525",
          "content": "😁"
      }
  }
}

```



### 3.17 获取应用 WebApp 设置

```bash
curl -X GET 'http://172.17.18.33/v1/site' \
-H 'Authorization: Bearer {api_key}'
```

响应值

```json
{
  "title": "My App",
  "chat_color_theme": "#ff4a4a",
  "chat_color_theme_inverted": false,
  "icon_type": "emoji",
  "icon": "😄",
  "icon_background": "#FFEAD5",
  "icon_url": null,
  "description": "This is my app.",
  "copyright": "all rights reserved",
  "privacy_policy": "",
  "custom_disclaimer": "All generated by AI",
  "default_language": "en-US",
  "show_workflow_steps": false,
  "use_icon_as_answer_icon": false,
}

```

