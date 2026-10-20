# Kettle ETL 数据处理
> 依赖 Kettle (pdi-ce) 7.1.0.0-12

安装说明：https://www.xuzhihao.net/#/deploy/kettle

## 设置仓库地址

kattle依赖jar，在官方仓库里面，在`settings.xml`中增加官方仓库下载地址

```xml
<profiles>
	<profile>
	  <id>pentaho</id>
	  <activation>
		<activeByDefault>true</activeByDefault>
	  </activation>
	  <repositories>
		<repository>
		  <id>pentaho-public</id>
		  <name>Pentaho Public</name>
		  <url>http://nexus.pentaho.org/content/groups/omni</url>
		  <releases>
			<enabled>true</enabled>
			<updatePolicy>always</updatePolicy>
		  </releases>
		  <snapshots>
			<enabled>true</enabled>
			<updatePolicy>always</updatePolicy>
		  </snapshots>
		</repository>
	  </repositories>
	  <pluginRepositories>
		<pluginRepository>
		  <id>pentaho-public</id>
		  <name>Pentaho Public</name>
		  <url>http://nexus.pentaho.org/content/groups/omni</url>
		  <releases>
			<enabled>true</enabled>
			<updatePolicy>always</updatePolicy>
		  </releases>
		  <snapshots>
			<enabled>true</enabled>
			<updatePolicy>always</updatePolicy>
		  </snapshots>
		</pluginRepository>
	  </pluginRepositories>
	</profile>
</profiles>
```

## 安装Oracle驱动

```bash
mvn install:install-file -Dfile=D:\ojdbc6.jar -DgroupId=com.oracle -DartifactId=ojdbc6 -Dversion=11.2.0.3.0 -Dpackaging=jar -DgeneratePom=true -DcreateChecksum=true
```

```conf
<dependency>
	<groupId>com.oracle</groupId>
	<artifactId>ojdbc6</artifactId>
	<version>11.2.0.3.0</version>
</dependency>
```

## 调用过程

```sql
CREATE TABLE TEST(
  ID   VARCHAR2(100),
  NAME VARCHAR2(100),
  YEAR NUMBER
)

CREATE OR REPLACE PROCEDURE TEST_KATTLE(I_NAME VARCHAR2, I_YEAR NUMBER) AS
  P_ID VARCHAR2(100);
BEGIN
  SELECT SYS_GUID() INTO P_ID FROM DUAL;
  INSERT INTO TEST (ID, NAME, YEAR) VALUES (P_ID, I_NAME, I_YEAR);
END;
```

## 调用Job