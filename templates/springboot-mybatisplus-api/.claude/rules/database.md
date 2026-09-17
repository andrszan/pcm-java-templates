---
paths:
  - "src/main/java/com/example/app/entity/**"
  - "src/main/java/com/example/app/mapper/**"
  - "src/main/resources/db/migration/**"
---

# 数据库与迁移规范

- 保持 MySQL + PostgreSQL 双库兼容：实体映射与手写 SQL 不使用单库专属语法或类型；确需差异时先确认方案，派生项目选定单库后才解除该约束。
- 实体使用 `@TableName` 显式表名、`@TableId` 显式主键；列名映射依赖已开启的 `map-underscore-to-camel-case`。主键策略、审计字段、软删除、多租户、金额精度、时区、枚举、索引、外键和级联属于数据语义，存在实质歧义时先确认，不按惯例自行补齐。
- 每次 Schema 变更新增 `V<n>__描述.sql`，`mysql/` 与 `postgresql/` 双目录同步维护并人工审查方言差异；Flyway 在应用启动时自动执行。禁止把 DDL 写到迁移目录之外、在代码或测试中建表、或跳过迁移直接交付。
- 查询通过 Mapper 接口 + MyBatis-Plus 方法或 XML 实现；复杂 SQL 写 XML 并只用双库兼容语法（如 `COALESCE` 而非 `IFNULL`）。禁止在 controller 拼 `QueryWrapper`/`LambdaQueryWrapper`；查询逻辑放 service 或专用查询类。
- 事务用 `@Transactional` 声明在 service 层或明确的业务操作边界，只读查询标 `readOnly`；按 Spring 默认语义（RuntimeException/Error 回滚）设计异常与事务的关系，不依赖受检异常回滚。
- 业务包 `entity/`、`mapper/`、`service/` 按需求创建，不预建空包；`@MapperScan` 已指向 `com.example.app.mapper`。
- 集成测试通过 `@DynamicPropertySource` 连接 `TEST_DB_NAME` 指定的测试库，不影响开发库；测试库由项目自行创建。未运行真实数据库集成测试或迁移验证时明确报告对应部分未验证；MockMvc 测试通过不能代替数据库验证。
