# Collabora Online 在线文档

- 官网地址: https://www.collaboraoffice.com/
- SDK地址: https://sdk.collaboraonline.com/
- GitHub: https://github.com/CollaboraOnline/online
- WOPI 官方文档: https://api.onlyoffice.com/zh/editors/wopi/discovery
- 官方demo: https://github.com/CollaboraOnline/collabora-online-sdk-examples/tree/master/webapp/nodejs
- Nginx 代理配置: https://sdk.collaboraonline.com/docs/installation/Proxy_settings.html
- 镜像地址: https://sdk.collaboraonline.com/docs/installation/CODE_Docker_image.html

## 1. 什么是 Collabora Online？

`Collabora Onlnien` 是一款开源的基于LibreOffice 的云办公套件，它允许用户在浏览器中创建、编辑和协作处理文档、电子表格、演示文稿和绘图。它是由 Collabora Productivity Ltd开发的，这家公司是LibreOffice社区的主要贡献者之一。

### 1.1 拉取镜像

```bash
docker pull collabora/code:latest
```

### 1.2 启动镜像

```bash
# 单机
docker run -dit --name collabora --privileged=true \
-p 9980:9980 \
-u cool --restart always \
-e "extra_params=--o:ssl.enable=false" \
-e "username=admin" -e "password=123456" \
--cap-add MKNOD collabora/code

# 创建本地文件夹
mkdir /etc/coolwsd

# 复制配置文件
docker cp collabora:/etc/coolwsd/coolwsd.xml /etc/coolwsd/
chmod -R 777 /ect/coolwsd

# 删除容器
docker rm -f collabora

# 挂在配置文件启动
docker run -dit --name collabora --privileged=true \
-p 9980:9980 \
-u cool --restart always \
-e "extra_params=--o:ssl.enable=false" \
-e "username=admin" -e "password=123456" \
-v /etc/coolwsd/coolwsd.xml:/etc/coolwsd/coolwsd.xml \
--cap-add MKNOD collabora/code
```

### 1.2 配置可信域名（本地调试可忽略）

```bash
vi /etc/coolwsd/coolwsd.xml
```

```bash
<alias_groups desc="default mode is 'first' it allows only the first host when groups are not defined. set mode to 'groups' and define group to allow multiple host and its aliases" mode="groups">
	<group>
		<host desc="studio-cloud.vjsp.cn" allow="true">vjsp.cn</host>
		<alias desc="172.17.17.165">http://172.17.17.165:8080</alias>
	</group>
</alias_groups>
```

重启容器
```bash
docker restart collabora
```

### 1.3 访问地址

- 管理地址：http://172.17.17.160:9980/browser/dist/admin/admin.html
- 配置参数：http://172.17.17.160:9980/hosting/discovery
- 编辑地址：http://172.17.17.160:9980/browser/ded56d8ff7/cool.html?WOPISrc=http://172.17.17.160:8080/wopi/files/test.doc&lang=zh-CN&access_token=123

在线编辑地址后面的 `WOPISrc` 要拼接 WOPI 服务地址的。通过cool.html访问时，后面传输的任何参数，例如 `access_token` 都会被转发到对应的 WOPI 服务的请求上，用于鉴权等操作。

## 2. 什么是WOPI？

WOPI：全称为 Web Office Protocol Interface，是一种由微软提出的协议，旨在允许 Web 应用程序能够无缝地与 Office 文档进行交互。WOPI 主要应用于在线编辑和预览 Microsoft Office 文档的场景，使得用户可以在 Web应用中直接打开、编辑和保存 Office 文档，而无需下载文档到本地或安装 Office 套件。