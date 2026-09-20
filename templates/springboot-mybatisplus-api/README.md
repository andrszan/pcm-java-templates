# Spring Boot + MyBatis-Plus API 项目起点

业务无关、完全自包含的 Java 后端 API 项目起点。同时兼容 MySQL 和 PostgreSQL，派生项目按需选定其中一个数据库。

## 技术选型

- Java 21、Spring Boot 4.1.x、Maven（含 Maven Wrapper）
- MyBatis-Plus 3.5.x（分页插件按数据源自动推断方言）
- Flyway 数据库迁移（按数据库分目录）
- Bean Validation、springdoc-openapi（Swagger UI）
- Lombok

## 环境要求

- JDK 21
- 本机已安装并运行 MySQL 或 PostgreSQL

## 运行配置

| 环境变量 | 说明 | 默认值 |
| --- | --- | --- |
| `DB_PROTOCOL` | JDBC 协议 | `jdbc:mysql`；PostgreSQL 设为 `jdbc:postgresql` |
| `DB_HOST` | 数据库主机 | `127.0.0.1` |
| `DB_PORT` | 数据库端口 | `3306` |
| `DB_USER` / `DB_PASSWORD` | 项目数据库账号 | 无 |
| `DB_NAME` | 开发数据库名 | 无 |
| `TEST_DB_NAME` | 测试数据库名（集成测试用） | 无 |
| `API_PREFIX` | 业务 API 统一前缀 | `/api/v1` |
| `CORS_ORIGINS` | 逗号分隔的跨域来源 | 空（关闭） |
| `ENABLE_API_DOCS` | OpenAPI / Swagger UI 开关 | `true` |
| `LOG_LEVEL` | 日志级别 | `INFO` |

复制 `.env.example` 为 `.env`，按选定数据库填写。Spring Boot 从项目根自动把 `.env` 作为 UTF-8 Java properties 导入；它不是 shell 脚本，不要执行 `source .env`，也不要添加 `export` 或 shell 引号。操作系统环境变量、JVM 系统属性和命令行参数仍可覆盖本地文件中的值。

Java properties 中反斜杠具有转义含义，字面量 `\` 应写为 `\\`；包含字面量 `${...}` 的值应按 Spring placeholder 规则转义，或改由环境变量等更高优先级来源注入。修改 `.env` 后需重启应用。

## 准备数据库

使用数据库管理员身份创建项目专用开发库、测试库和项目账号；应用运行配置不能使用服务器级 root 或个人账号。当前模板由应用启动时运行 Flyway，因此项目账号需要两个项目数据库内的建表和读写权限，但不应获得其它数据库或服务器级管理权限。

MySQL 示例：

```sql
CREATE DATABASE app_dev CHARACTER SET utf8mb4;
CREATE DATABASE app_test CHARACTER SET utf8mb4;
CREATE USER 'app_user'@'localhost' IDENTIFIED BY '<项目数据库密码>';
GRANT ALL PRIVILEGES ON app_dev.* TO 'app_user'@'localhost';
GRANT ALL PRIVILEGES ON app_test.* TO 'app_user'@'localhost';
```

PostgreSQL 示例：

```sql
CREATE ROLE app_user LOGIN PASSWORD '<项目数据库密码>';
CREATE DATABASE app_dev OWNER app_user;
CREATE DATABASE app_test OWNER app_user;
```

## 常用命令

以下命令从项目根目录执行，以便 Spring 找到同级 `.env`：

```bash
./mvnw spring-boot:run         # 启动
./mvnw test                    # 测试；未配置 TEST_DB_NAME 时跳过数据库集成测试
./mvnw clean verify            # 完整检查和构建
./mvnw clean package           # 构建可执行 jar（target/ 下）
```

执行测试时应核对汇总中的 `Skipped`。数据库已经准备且 `.env` 含 `TEST_DB_NAME` 时，`ApplicationTests` 必须实际运行；跳过表示本次没有证明数据库、MyBatis-Plus 和 Flyway 链路可用。

- 探针：`GET /health`（纯存活，不访问数据库）、`GET /ready`（数据库连通性检查）。
- 接口文档：`http://localhost:8080/swagger-ui.html`（`ENABLE_API_DOCS=false` 时关闭）。

## 数据库迁移与派生状态

迁移脚本位于 `src/main/resources/db/migration/`，模板维护态同时具备两套完整能力：

- MySQL JDBC 驱动、`flyway-mysql` 和 `db/migration/mysql/`；
- PostgreSQL JDBC 驱动、`flyway-database-postgresql` 和 `db/migration/postgresql/`。

模板维护态的每次 Schema 变更都要同步维护两个迁移目录并分别验证。派生项目根据已确认的数据库绑定一次性删除另一套驱动、Flyway 模块和迁移目录；只剩一套完整能力后进入单库派生态，只维护所选数据库，不得因模板旧规则恢复另一套。驱动、Flyway 模块和迁移目录不一致属于未完成的半派生状态，必须先修复。
