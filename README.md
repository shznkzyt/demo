# Student Query API

基于 Java 21、Spring Boot、MyBatis、PageHelper 和 MySQL 的学生信息只读查询服务。

## 启动配置

默认连接本机 MySQL：

```bash
./mvnw spring-boot:run
```

默认数据库名为 `Test`，用户名为 `root`，密码为 `123456`。如需修改，可以在
`application.yaml` 中配置，或者按需使用以下环境变量覆盖：

```bash
export MYSQL_HOST='127.0.0.1'
export MYSQL_PORT='3306'
export MYSQL_DATABASE='Test'
export MYSQL_USER='root'
export MYSQL_PASSWORD='数据库密码'
```

应用启动时会通过 `schema.sql` 创建不存在的 `students` 表，但不会删除或覆盖已有数据。

## API

```bash
curl 'http://localhost:8080/students?page=1&size=10'

curl 'http://localhost:8080/students/1'
```

分页参数要求：`page >= 1`，`1 <= size <= 100`。联系方式在响应中会自动脱敏。

## 测试

测试使用 H2 内存数据库，不依赖本地 MySQL 或生产凭据：

```bash
./mvnw test
```
