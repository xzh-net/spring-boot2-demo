# ShardingJDBC 分库分表

1. 使用取模的方式来实现表分片

   配置文件`sharding-tables`，用户ID用2取模，ID尾号是双数的保存在`user_info0`，单数保存在`user_info0`

2. 一主多从模式下的读写分离

   配置文件`master-slave`，执行完数据初始化以后，需要手动把数据同步到两个从库中。清空同样也是操作主库。

3. 使用取模的方式来实现库分片

   配置文件`sharding-databases`，用户ID用2取模，ID尾号是双数的保存在`0`号库的`user_info`表，单数保存在`1`号库的`user_info`表。

4. 使用固定值的方式来实现库分片

   配置文件`sharding-databases2`，使用`company_id`为拆分条件，值`alibaba`在`0`号库，值`baidu`在`1`号库。
   
5. 工程里既有分片的表，也有不分片的表（使用默认的库）

   配置文件`sharding-databases3`，通过配置`default-data-source-name`属性，让没有规则的表插入到默认数据库中，既支持分片表也支持非分片表
   
6. 分库分表公共表

   配置文件`sharding-databases4`
