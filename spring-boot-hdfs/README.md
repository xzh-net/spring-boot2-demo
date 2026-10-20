# Hadoop HDFS 文件系统

## windows设置

解压`hadoop-3.1.4_winutils.zip`，设置环境变量，对应`HdfsConfig.java`

```bash
HADOOP_HOME=D:\tools\hadoop-3.1.4
PATH=%HADOOP_HOME%\bin
```

## 环境安装

### 1. 构建centos7-ssh-sync

```bash
cd /data/dockerfile/centos7-ssh-sync
vi Dockerfile
```

```bash
FROM centos:7

# 更换为可用的yum源
RUN curl -o /etc/yum.repos.d/CentOS-Base.repo https://mirrors.aliyun.com/repo/Centos-7.repo && \
    sed -i -e '/mirrors.cloud.aliyuncs.com/d' -e '/mirrors.aliyuncs.com/d' /etc/yum.repos.d/CentOS-Base.repo

ENV TZ=Asia/Shanghai

# 清理并更新yum缓存
RUN yum clean all && yum makecache

RUN yum -y install vim net-tools rsync openssh-server openssh-clients sudo && \
    sed -i 's/UsePAM yes/UsePAM no/g' /etc/ssh/sshd_config && \
    echo "root:123456" | chpasswd && \
    echo "root   ALL=(ALL)       ALL" >> /etc/sudoers && \
    ssh-keygen -t dsa -f /etc/ssh/ssh_host_dsa_key -N '' && \
    ssh-keygen -t rsa -f /etc/ssh/ssh_host_rsa_key -N '' && \
    mkdir /var/run/sshd

EXPOSE 22
CMD ["/usr/sbin/sshd", "-D"]
```

```bash
docker build -f Dockerfile -t centos7-ssh-sync .
```

### 2. 构建centos7-hadoop

```bash
cd /data/dockerfile/centos7-hadoop3
vi Dockerfile
```

```bash
FROM centos7-ssh-sync

# install jdk8
ADD jdk-8u202-linux-x64.tar.gz /usr/local/
ENV JAVA_HOME=/usr/local/jdk1.8.0_202
ENV PATH=$PATH:$JAVA_HOME/bin

# install hadoop3.1.4
ADD hadoop-3.1.4-bin-snappy-CentOS7.tar.gz /usr/local/
ENV HADOOP_HOME=/usr/local/hadoop-3.1.4
ENV PATH=$PATH:$HADOOP_HOME/bin
ENV PATH=$PATH:$HADOOP_HOME/sbin

WORKDIR /usr/local
```

```bash
docker build -f Dockerfile -t centos7-hadoop3 .
```

### 3. 运行

```bash
docker run -dit --name hadoop3 -h hadoop3 -p 1022:22 \
-p 8020:8020 -p 9870:9870 -p 9871:9871 \
-p 9866:9866 -p 9864:9864 -p 9865:9865 \
-p 8088:8088 \
-p 14000:14000 --restart=always --privileged=true centos7-hadoop3
```

### 4. 登录容器

```bash
docker exec -it hadoop3 /bin/bash
hadoop version
java -version
```

### 5. 设置hadoop-env.sh

```bash
cd /usr/local/hadoop-3.1.4/etc/hadoop
vi hadoop-env.sh 
```

```bash
# 添加下面这些内容
export JAVA_HOME=/usr/local/jdk1.8.0_202
export HDFS_NAMENODE_USER=root
export HDFS_DATANODE_USER=root
export HDFS_SECONDARYNAMENODE_USER=root
export YARN_RESOURCEMANAGER_USER=root
export YARN_NODEMANAGER_USER=root 
```

### 6. 设置core-site.xml

```bash
vi core-site.xml
```

```yaml
<configuration>
    <property>
        <name>fs.defaultFS</name>
        <value>hdfs://hadoop3:8020</value>
    </property>
    <property>
        <name>hadoop.http.staticuser.user</name>
        <value>root</value>
    </property>
    <property>
        <name>dfs.permissions.enabled</name>
        <value>false</value>
    </property>
</configuration>
```

### 7. 设置hdfs-site.xml

```bash
vi hdfs-site.xml
```

```yaml
<configuration>
    <property>
        <name>dfs.replication</name>
        <value>1</value>
    </property>
</configuration>
```

### 8. 启动服务

```bash
ssh-keygen
ssh-copy-id hadoop3
hdfs namenode -format
start-all.sh
jps
```

访问地址：http://172.17.17.161:9870

> web上传附件，需要在本地hosts添加 172.17.17.161 hadoop3`，否则提示`Couldn't upload the file`问题

