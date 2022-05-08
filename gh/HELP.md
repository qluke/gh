

# Mybatis拦截器实现记录历史记录-SimpleDemo

基于 org.apache.ibatis.plugin.Interceptor 接口 和  com.alibaba.druid.sql.SQLUtils 工具类实现的mybatis拦截器。记录修改（update）和删除（delete）操作前后的数据。比如说修改操作：

有update sql： UPDATE tb_order SET content = 'new' WHERE id = 996

| 表名tb_order      | id   | content |
| ----------------- | ---- | ------- |
| before update sql | 1    | old     |
| update sql        |      |         |
| after update sql  | 1    | new     |

我们很容易根据原sql反向生成查询sql：select * from 表名 where 原查询条件，并且在sql操作前后各执行一次查询sql记录。即：

```
before image （解析SQL查询一次数据库）
after image （再查询一次数据库）
insert undo log （写一次数据库）
```

测试用例待补充。

# Getting Started

1. 引入依赖

```
<dependency>
  <groupId>com.luke</groupId>
  <artifactId>luke-spring-starter</artifactId>
  <version>0.0.1</version>
</dependency>
```

2. 添加@GlobalHistory 注解在Mapper 接口上，eg：

```
@GlobalHistory(onlyCareUpdateColumns = true)
public interface OrderMapper extends BaseMapper<Order>
```

3. 添加表结构

```
spring.datasource.history.type=com.alibaba.druid.pool.DruidDataSource
spring.datasource.history.username=root
spring.datasource.history.password=123456789
spring.datasource.history.url=jdbc:mysql://localhost:3306/test?characterEncoding=utf8&serverTimezone=UTC
```

```
CREATE TABLE `tb_update_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `before_image` json DEFAULT NULL,
  `after_image` json DEFAULT NULL,
  `update_columns` json DEFAULT NULL,
  `table_name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `original_sql` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `query_sql` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
```




