# PCM Java 项目仓库

本仓库用于存放业务无关、完全自包含的 Java 后端基础架构项目起点。每个目录都应能够独立演进为真实项目。

## 仓库规则

- `main` 保存所有已经由维护者确认的最新项目内容，也是日常开发的默认分支。
- 日常直接在 `main` 修改对应项目目录；不要为每个项目维护长期开发分支。
- 需要独立评审、并行开发或隔离较大变更时，才创建短期分支；完成后合并回 `main`。
- 项目不得依赖仓库根目录文件、其他项目或共享工作区包。
- 每个项目的安装、运行、测试和构建方式以项目自身 README 与构建声明为准。

## 项目目录

- [Spring Boot + MyBatis-Plus API](templates/springboot-mybatisplus-api/README.md)：面向独立 Web API 服务起步的 Spring Boot 4 + MyBatis-Plus 项目；基础模板兼容 MySQL 与 PostgreSQL，派生项目按实际需求选定并保留一种数据库。

项目目录位于 `templates/<project-id>/`，这是仓库的组织方式，不是项目内部的开发概念。
