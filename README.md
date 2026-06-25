# AI 数据服务中台

AI 数据服务中台是一个面向 AI 应用和业务系统的数据服务平台，目标是把企业内部或第三方系统中的业务接口统一配置、统一管理，并以标准化 Skill 的方式对外提供调用能力。

项目第一版聚焦基础数据服务能力，不直接接入大模型、不引入 Agent 编排和 RAG 知识库，优先完成认证、权限、接口配置、Skill 发布、定时任务和执行日志等核心闭环。

## 项目定位

本项目用于将 OA、CRM、HR 等业务系统的 HTTP 接口纳入统一平台管理，通过后台配置业务系统、接口参数、认证方式和响应解析规则，再将接口发布为可被业务系统或 AI 应用调用的 Skill。

平台同时支持定时任务配置，通过 XXL-JOB 调度已配置的业务接口，并保存任务执行结果和日志，为后续数据同步、数据治理和 AI 应用接入提供基础能力。

## 核心能力

- 用户登录认证
- 用户、角色、权限管理
- 业务系统配置
- 第三方业务接口配置
- 接口在线测试
- Skill 配置与调用
- Skill 权限控制
- XXL-JOB 定时任务接入
- 定时任务执行日志
- 登录日志和操作日志
- MySQL 初始化脚本
- Redis 会话、缓存和分布式锁支持

## 技术栈

- JDK 21
- Spring Boot 3.x
- Spring Cloud Gateway
- Sa-Token
- MyBatis
- MySQL 8
- Redis
- XXL-JOB
- Maven

## 工程结构

```text
ai-data-platform
├── ai-data-api       接口契约、枚举和 DTO 定义
├── ai-data-common    公共响应、上下文和通用工具
├── ai-data-gateway   统一网关入口
├── ai-data-server    核心业务服务
├── config            本地和生产配置模板
└── sql               数据库初始化脚本
```

## 配置说明

项目支持本地和生产两套配置：

- `config/app-config-local.properties`：本地开发配置，不建议提交真实敏感信息。
- `config/app-config-prod.properties`：生产配置模板，提交前保持注释或空值。

默认启动使用 `local` 环境。生产环境启动时指定：

```bash
--spring.profiles.active=prod
```

## 当前进度

当前已完成 Maven 多模块工程骨架、Gateway/Server 启动模块、基础配置拆分、公共响应结构和数据库初始化脚本。

建设中
