---
paths:
  - "src/main/java/com/example/app/controller/**"
  - "src/main/java/com/example/app/common/ApiResponse.java"
---

# API 开发规范

- 新端点使用 `@RestController` 与明确的路径、HTTP 方法；业务路径经 `@RequestMapping("${api.prefix}/...")` 挂载，资源路径不硬编码版本前缀。请求体用 `@Valid` 校验，约束注解定义字段级错误。
- 业务成功响应显式使用 `ApiResponse.of(...)` 包装，`code` 与 HTTP 状态码一致；不增加全局自动包装器。错误信息只包含经过审查的公开内容，不将原始输入、SQL、异常对象或第三方错误放入错误响应。
- 分页直接使用 MyBatis-Plus `Page<T>` 放入 `ApiResponse.data`；列表查询显式稳定排序，分页复用已配置的分页插件。不预建查询 helper、cursor、CRUD 基类或查询 DSL。
- `/health` 是纯 liveness，不注入数据源、不执行 SQL；`/ready` 仅执行最低限度的 `SELECT 1` 连通性检查。两者返回裸 JSON，不使用 `ApiResponse` 包装，也不执行迁移、建表或业务查询。
- controller 只做参数接收、校验与响应组装；业务规则放在 service 层（出现真实复用需求才提取），不放入 `common/`、`config/`。
- 新能力改变公开 HTTP 契约时，先明确路径、字段、状态码、错误语义与权限要求；不把普通 JSON 响应约定直接套用于文件、流式响应或 Webhook。
- OpenAPI 文档由 springdoc 提供，路径与开关见 README；修改公开接口后核对 Swagger UI 与 schema 是否反映新契约。
- 修改后补充成功路径和明确失败分支的最小 MockMvc 测试，同步受影响的 README 契约。
