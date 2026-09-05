# Student Management API

基于 Java 21、Spring Boot、MyBatis、PageHelper 和 MySQL 实现的学生信息管理服务，支持学生信息分页查询、详情查询、新增、完整修改和删除。

## 技术栈

- Java 21
- Spring Boot 4.1.1
- Spring Web MVC
- MyBatis + PageHelper
- MySQL
- H2（自动化测试）
- Maven Wrapper

## 数据库配置

默认连接本机 MySQL：

| 配置项 | 默认值 | 环境变量 |
| --- | --- | --- |
| 主机 | `127.0.0.1` | `MYSQL_HOST` |
| 端口 | `3306` | `MYSQL_PORT` |
| 数据库 | `demo` | `MYSQL_DATABASE` |
| 用户名 | `root` | `MYSQL_USER` |
| 密码 | 空 | `MYSQL_PASSWORD` |

可以通过环境变量覆盖默认配置：

```bash
export MYSQL_HOST='127.0.0.1'
export MYSQL_PORT='3306'
export MYSQL_DATABASE='Test'
export MYSQL_USER='root'
export MYSQL_PASSWORD='数据库密码'
```

应用启动时会执行 `src/main/resources/schema.sql`，创建不存在的 `students` 表，不会删除或覆盖已有数据。

## 启动项目

```bash
./mvnw spring-boot:run
```

服务默认监听 `http://localhost:8080`。

## 接口列表

| 方法 | 地址 | 功能 | 成功状态码 |
| --- | --- | --- | --- |
| `GET` | `/students?page=1&size=10` | 分页查询学生 | `200` |
| `GET` | `/students/{id}` | 根据 id 查询学生 | `200` |
| `POST` | `/students` | 添加学生 | `201` |
| `PUT` | `/students/{id}` | 完整修改学生 | `200` |
| `DELETE` | `/students/{id}` | 删除学生 | `200` |

### 分页查询

```bash
curl 'http://localhost:8080/students?page=1&size=10'
```

分页参数规则：

- `page` 默认值为 `1`，必须大于或等于 `1`。
- `size` 默认值为 `10`，取值范围为 `1` 至 `100`。

响应示例：

```json
{
  "success": true,
  "message": "查询成功",
  "data": {
    "list": [],
    "total": 0,
    "page": 1,
    "size": 10,
    "totalPages": 0
  }
}
```

### 查询学生详情

```bash
curl 'http://localhost:8080/students/1'
```

### 添加学生

```bash
curl -X POST 'http://localhost:8080/students' \
  -H 'Content-Type: application/json' \
  -d '{
    "studentNo": "S004",
    "name": "赵六",
    "gender": "女",
    "birthDate": "2005-04-04",
    "className": "二班",
    "phone": "13612345678",
    "email": "zhaoliu@example.com"
  }'
```

### 修改学生

`PUT` 接口采用完整更新语义，`studentNo` 和 `name` 必须提供；没有提供的可选字段会保存为 `null`。

```bash
curl -X PUT 'http://localhost:8080/students/1' \
  -H 'Content-Type: application/json' \
  -d '{
    "studentNo": "S001",
    "name": "张三丰",
    "gender": "男",
    "birthDate": "2005-01-01",
    "className": "三班",
    "phone": "13800001111",
    "email": "new@example.com"
  }'
```

### 删除学生

```bash
curl -X DELETE 'http://localhost:8080/students/1'
```

## 字段规则

| 字段 | 必填 | 最大长度 | 说明 |
| --- | --- | --- | --- |
| `studentNo` | 是 | 32 | 学号，全局唯一 |
| `name` | 是 | 64 | 学生姓名 |
| `gender` | 否 | 16 | 性别 |
| `birthDate` | 否 | - | 格式为 `yyyy-MM-dd` |
| `className` | 否 | 64 | 班级名称 |
| `phone` | 否 | 32 | 手机号 |
| `email` | 否 | 128 | 电子邮箱 |

请求中的文本会自动去除首尾空格，空白的可选字段会保存为 `null`。查询和写入接口返回数据时，手机号及邮箱会自动脱敏。

## 响应状态码

| 状态码 | 说明 |
| --- | --- |
| `200` | 查询、修改或删除成功 |
| `201` | 学生添加成功 |
| `400` | 请求参数、学生 id 或请求体不合法 |
| `404` | 学生或请求地址不存在 |
| `409` | 学号已被其他学生使用 |
| `500` | 服务器内部错误 |
| `503` | 数据库暂时不可用 |

所有接口使用统一响应格式：

```json
{
  "success": false,
  "message": "错误说明",
  "data": null
}
```

## 项目结构

```text
src/main/java/com/demo
├── common
│   ├── exception    # 全局异常处理
│   └── response     # 统一响应及分页对象
└── student
    ├── controller   # HTTP 接口和参数校验
    ├── dto          # 请求及脱敏响应对象
    ├── entity       # 数据库实体
    ├── mapper       # MyBatis 数据访问接口
    └── service      # 业务逻辑和事务处理
```

MyBatis SQL 位于 `src/main/resources/mapper/StudentMapper.xml`。

## 运行测试

测试使用 H2 内存数据库，不依赖本地 MySQL 或生产环境凭据：

```bash
./mvnw test
```
