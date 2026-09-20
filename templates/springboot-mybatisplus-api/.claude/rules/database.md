---
paths:
  - "src/main/java/com/example/app/entity/**"
  - "src/main/java/com/example/app/mapper/**"
  - "src/main/resources/db/migration/**"
---

# 数据库与迁移规范

- 数据库模式按实际三件套判断：JDBC 驱动、Flyway 数据库模块和对应迁移目录。MySQL/PostgreSQL 两套三件套都完整时属于模板维护态，实体映射与手写 SQL 不使用单库专属语法或类型；只剩一套完整三件套时属于派生单库态，只维护保留数据库，不恢复另一套；三者不一致时先修复半派生状态。
- 实体使用 `@TableName` 显式表名、`@TableId` 显式主键；列名映射依赖已开启的 `map-underscore-to-camel-case`。主键策略、审计字段、软删除、多租户、金额精度、时区、枚举、索引、外键和级联属于数据语义，存在实质歧义时先确认，不按惯例自行补齐。
- 每次 Schema 变更新增 `V<n>__描述.sql`。模板维护态同步维护 `mysql/` 与 `postgresql/` 并人工审查方言差异；派生单库态只在保留目录新增迁移。Flyway 在应用启动时自动执行。禁止把 DDL 写到迁移目录之外、在代码或测试中建表、或跳过迁移直接交付。
- 查询通过 Mapper 接口 + MyBatis-Plus 方法或 XML 实现；复杂 SQL 写 XML 并只用双库兼容语法（如 `COALESCE` 而非 `IFNULL`）。禁止在 controller 拼 `QueryWrapper`/`LambdaQueryWrapper`；查询逻辑放 service 或专用查询类。
- 事务用 `@Transactional` 声明在 service 层或明确的业务操作边界，只读查询标 `readOnly`；按 Spring 默认语义（RuntimeException/Error 回滚）设计异常与事务的关系，不依赖受检异常回滚。
- 业务包 `entity/`、`mapper/`、`service/` 按需求创建，不预建空包；`@MapperScan` 已指向 `com.example.app.mapper`。
- 集成测试通过 Spring Config Data 读取项目根 `.env`，再用 `@DynamicPropertySource` 将 `TEST_DB_NAME` 映射为测试数据源的 `DB_NAME`，不直接调用 `System.getenv`，也不影响开发库；测试库由项目自行创建。已配置 `TEST_DB_NAME` 时集成测试必须真实执行，跳过、MockMvc 或上下文加载都不能代替数据库与迁移验证。
