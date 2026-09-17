---
paths:
  - "src/main/java/com/example/app/Application.java"
  - "src/main/java/com/example/app/config/**"
  - "src/main/java/com/example/app/common/**"
  - "src/main/resources/application.yml"
---

# 基础设施规范

- `Application` 只做启动装配与 `@MapperScan`；`common/` 只承担统一响应、错误处理、请求上下文过滤器和 CORS，`config/` 只放基础设施配置，不加入业务专属工具。
- 新运行配置统一加入 `application.yml` 并用 `${ENV:default}` 占位符读取，不在业务模块调用 `System.getenv`；同步 `.env.example`、README 和测试。密钥来自运行环境，不写入源码、日志、测试输出或文档。
- 数据库连接 URL 由 `DB_PROTOCOL`、`DB_HOST`、`DB_PORT`、`DB_NAME` 与 `DB_USER`、`DB_PASSWORD` 组成；凭据与完整数据库 URL 不写入日志或错误响应。
- `API_PREFIX`、`CORS_ORIGINS`、`ENABLE_API_DOCS`、`LOG_LEVEL` 是全局运行配置。CORS 仅使用 allowlist，默认关闭且始终禁用 credentials；调整跨域行为前验证错误响应与 `X-Request-ID` 头。
- `RequestContextFilter` 负责 request-id、MDC 与访问日志：外部请求 ID 只接受 `[A-Za-z0-9._-]{1,128}`，否则生成新 ID。访问日志只记录请求 ID、方法、路径、状态和耗时，不记录 query string、body、Authorization、Cookie、token、密码或数据库 URL。
- 错误处理统一经 `GlobalExceptionHandler`：错误体为 `ApiResponse` 信封，保留 Spring MVC 客户端错误的原始 HTTP 语义（如 400/405/406/415），参数校验使用 422，404 与 500 信息固定且不回显内部细节；新增异常处理时不得让 `Exception` 兜底把 4xx 误报为 500，原始异常只进日志。
- 新依赖必须有真实消费者、明确版本范围、验证结果、许可证/安全影响评估及最小测试；不因已安装就宣称已接入。
- 修改后覆盖探针、错误处理、CORS 或过滤器中受影响的行为；标准检查命令见 README。数据库集成测试未执行时明确报告数据库行为未验证。
