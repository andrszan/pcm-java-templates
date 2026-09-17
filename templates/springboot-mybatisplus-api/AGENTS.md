# 项目开发规则

## 底线

- 保持 MySQL + PostgreSQL 双库兼容（`DB_PROTOCOL` 决定协议）；只按已确认需求增加业务、依赖、基础设施和有真实职责的架构层，不预建空目录或通用抽象。项目须能独立安装、运行、测试和构建。
- 包名 `com.example.app` 固定不改；Maven 坐标（`artifactId`、`name`）在 `pom.xml` 中改为产品名。派生项目选定数据库后删除另一个驱动、Flyway 模块与迁移目录。
- 业务 controller 经 `API_PREFIX` 挂载（`@RequestMapping("${api.prefix}/...")`），不硬编码 `/api/v1`；`Application` 只做装配，`common/`、`config/` 只放基础设施。业务响应统一使用 `ApiResponse`（`code` 与 HTTP 状态码一致），探针不包装。
- `/health` 不访问数据库；`/ready` 只执行 `SELECT 1` 连通性检查，不迁移、建表或执行业务查询。
- Schema 只通过 Flyway 变更并人工审查 SQL，`mysql/` 与 `postgresql/` 双目录同步维护；禁止在代码、测试或启动过程中建表或绕过迁移。查询逻辑不放 controller，不预建通用 CRUD 基类。
- 事务用 `@Transactional` 声明在 service 层或明确的事务边界，只读查询标 `readOnly`；不在 controller 开事务。
- 配置统一在 `application.yml` 用 `${ENV:default}` 占位符读取，不在业务代码调用 `System.getenv`；密钥不进源码、日志、测试输出或文档。CORS 默认关闭且禁用 credentials。错误响应与访问日志不得泄露原始输入、SQL、凭据、数据库 URL、Authorization、Cookie、token 或内部异常细节。
- 可靠推断的低影响工程细节采用最简单方案；公开 API、数据模型、权限、迁移或部署有实质歧义时先定向确认，不自行发明业务或示例数据。

## 按任务阅读与验证

修改前阅读直接相关源码、调用方与测试。下列 Markdown 正文供所有 Agent 按任务读取；Claude 的 `paths` 只是自动加载入口，不限制规则的适用范围，已加载的内容无需重复读取：

- API、响应、错误处理、分页、探针：`.claude/rules/api.md`。
- 实体、Mapper、事务或迁移：`.claude/rules/database.md`；涉及迁移时另读 `src/main/resources/db/migration/` 现有脚本。
- 应用装配、配置、日志、过滤器、CORS 或依赖：`.claude/rules/infrastructure.md`。
- 行为变化增加最小 JUnit 5 + MockMvc 测试，不预建通用 fixture/mock 层；使用现有依赖，不引入重复工具。同步受影响的 README、配置示例与迁移说明。
- 完成前执行 README 的测试和构建命令；数据库集成测试未执行或跳过（未配置 `TEST_DB_NAME`）时明确报告数据库行为未验证。
