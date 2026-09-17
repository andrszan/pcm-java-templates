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
| `DB_USER` / `DB_PASSWORD` | 数据库账号 | 无 |
| `DB_NAME` | 开发数据库名（需自行创建） | 无 |
| `TEST_DB_NAME` | 测试数据库名（集成测试用，需自行创建） | 无 |
| `API_PREFIX` | 业务 API 统一前缀 | `/api/v1` |
| `CORS_ORIGINS` | 逗号分隔的跨域来源 | 空（关闭） |
| `ENABLE_API_DOCS` | OpenAPI / Swagger UI 开关 | `true` |
| `LOG_LEVEL` | 日志级别 | `INFO` |

复制 `.env.example` 为 `.env`，按选定数据库填写。Spring Boot 不自动读取 `.env`，运行前先导出：

```bash
set -a; source .env; set +a
```

## 准备数据库

使用本机数据库，创建项目专用开发库和测试库（名称与 `.env` 中 `DB_NAME`、`TEST_DB_NAME` 一致）：

```sql
CREATE DATABASE app_dev;
CREATE DATABASE app_test;
```

## 常用命令

```bash
./mvnw spring-boot:run         # 启动
./mvnw test                    # 测试；未配置 TEST_DB_NAME 时跳过数据库集成测试
./mvnw clean verify            # 完整检查和构建
./mvnw clean package           # 构建可执行 jar（target/ 下）
```

- 探针：`GET /health`（纯存活，不访问数据库）、`GET /ready`（数据库连通性检查）。
- 接口文档：`http://localhost:8080/swagger-ui.html`（`ENABLE_API_DOCS=false` 时关闭）。

## 数据库迁移

迁移脚本位于 `src/main/resources/db/migration/`，按 `mysql/` 和 `postgresql/` 分目录，应用启动时自动对当前连接的数据库执行。每个变更新增一个 `V<n>__描述.sql` 文件，两个数据库目录都要同步维护。派生项目选定数据库后，删除另一个目录和 `pom.xml` 中对应的 JDBC 驱动、Flyway 模块依赖。
