# 微信开发

## 微信公众平台

访问地址：https://mp.weixin.qq.com

为个人、企业和组织提供的内容发布、用户管理、服务提供及商业运营的综合平台。通过公众号，运营者可以与微信用户建立连接，实现信息传递、服务互动和商业转化

### 公众号

申请测试公众号：https://mp.weixin.qq.com/debug/cgi-bin/sandboxinfo?action=showinfo&t=sandbox/index

#### 网页授权

1. 配置安全域名

   域名不能带端口号，备案通过同时上传验证文件TXT

	![](doc/assets/1.png)

2. 添加网页授权

	![](doc/assets/2.png)

3. 开发者配置

	![](doc/assets/3.png)

4. 网页授权流程

   [1 第一步：用户同意授权，获取code](https://developers.weixin.qq.com/doc/offiaccount/OA_Web_Apps/Wechat_webpage_authorization.html#0)

   [2 第二步：通过 code 换取网页授权access_token](https://developers.weixin.qq.com/doc/offiaccount/OA_Web_Apps/Wechat_webpage_authorization.html#1)

   [3 第三步：刷新access_token（如果需要）](https://developers.weixin.qq.com/doc/offiaccount/OA_Web_Apps/Wechat_webpage_authorization.html#2)

   [4 第四步：拉取用户信息(需 scope 为 snsapi_userinfo)](https://developers.weixin.qq.com/doc/offiaccount/OA_Web_Apps/Wechat_webpage_authorization.html#3)

   [5 附：检验授权凭证（access_token）是否有效](https://developers.weixin.qq.com/doc/offiaccount/OA_Web_Apps/Wechat_webpage_authorization.html#4)

5. 访问地址

   https://digpm.vjsp.cn/cms/index

	> 开发者工具无法收到消息，只能使用真机测试
	
#### 发送消息

1. 设置类目

	![](doc/assets/4.png)

2. 添加模板

	![](doc/assets/5.png)
	
3. 设置模板字段

	![](doc/assets/6.png)

4. 模板消息接口

	https://developers.weixin.qq.com/doc/offiaccount/Message_Management/Template_Message_Interface.html

5. 文本消息接口

	https://developers.weixin.qq.com/doc/offiaccount/Message_Management/Service_Center_messages.html#%E5%AE%A2%E6%9C%8D%E6%8E%A5%E5%8F%A3-%E5%8F%91%E6%B6%88%E6%81%AF

#### 分享

1. 设置安全域名

2. 引入JS

	http://res.wx.qq.com/open/js/jweixin-1.6.0.js

	![](doc/assets/7.png)

3. 说明文档

	https://developers.weixin.qq.com/doc/offiaccount/OA_Web_Apps/JS-SDK.html

4. 分享

5. 上传图片



### 服务号

#### 注册



### 企业微信

#### 注册

注册地址：https://work.weixin.qq.com/wework_admin/register_wx?from=myhome

![](doc/assets/e1.png)

#### 创建应用

![](doc/assets/e2.png)

#### 企业微信授权登录

![](doc/assets/e3.png)

![](doc/assets/e4.png)

#### 网页授权及JS-SDK

![](doc/assets/e5.png)

![](doc/assets/e6.png)

#### 企业可信IP

![](doc/assets/e7.png)

![](doc/assets/e8.png)

#### 设置可见范围并记录密钥

![](doc/assets/e9.png)





## 微信开放平台

访问地址：https://open.weixin.qq.com

一个面向**移动应用、网站应用、以及第三方开发者**的统一接入平台。包括：登录，分享与收藏，支付等。



## 微信支付-商户平台

访问地址：https://pay.weixin.qq.com/

商家在接入微信支付后，用于管理交易、资金、产品。包括：开通支付产品，设置参数，关联应用等。



