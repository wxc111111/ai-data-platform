# AI 数据服务中台

AI 数据服务中台是一个面向企业内部业务系统和 AI 应用的数据服务管理平台。项目目标是把 OA、CRM、HR 等第三方系统中的业务接口统一接入、统一配置、统一鉴权，并以标准化 Skill 的形式对外提供调用能力。

第一版优先建设基础闭环能力：登录认证、用户角色权限、业务系统配置、业务接口配置、Skill 发布与调用、定时任务、执行日志和基础网关能力。当前版本暂不直接接入大模型，不做 Agent 编排，不做 RAG 知识库。

## 项目定位

平台用于把企业已有系统的 HTTP 接口纳入统一后台管理。管理员可以在后台配置业务系统、接口地址、请求参数、认证方式和响应解析规则，再把这些接口发布为可被业务系统或 AI 应用调用的 Skill。

后续平台会支持 XXL-JOB 定时任务，用于按计划调用已配置的业务接口，并保存执行结果和执行日志，为数据同步、数据治理和 AI 应用接入提供基础能力。

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

后端：

- JDK 21
- Spring Boot 3.x
- Spring Cloud Gateway
- Sa-Token
- MyBatis
- MySQL 8
- Redis
- XXL-JOB
- Maven

前端：

- Vue 3
- Vite
- TypeScript
- Pinia
- Vue Router
- Element Plus
- Axios

## 工程结构

```text
ai-data-platform
├── ai-data-api       接口契约、枚举和 DTO 定义
├── ai-data-common    公共响应、通用异常和基础工具
├── ai-data-gateway   统一网关入口
├── ai-data-server    核心业务服务
├── ai-data-web       Vue 3 前端后台
├── config            本地和生产配置模板
├── docs              项目计划和说明文档
└── sql               数据库初始化脚本
```

## 当前进度

已完成：

- Maven 多模块工程骨架
- Gateway / Server 启动模块
- MySQL、Redis 配置拆分
- 公共响应结构
- 统一业务异常
- 登录认证接口
- Sa-Token 会话管理
- BCrypt 密码加密与校验
- 默认管理员初始化数据
- Vue 3 登录页面基础框架

登录相关接口：

```http
POST /api/auth/login
POST /api/auth/logout
GET  /api/auth/current-user
GET  /api/auth/permissions
```

## 配置说明

项目支持本地和生产两套配置：

- `config/app-config-local.properties`：本地开发配置，不建议提交真实敏感信息。
- `config/app-config-prod.properties`：生产配置模板，提交前应保持为空白或注释形式。

后端默认使用 `local` 环境。生产启动时可指定：

```bash
--spring.profiles.active=prod
```

## 本地启动

后端编译：

```bash
mvn clean install
```

启动业务服务：

```bash
mvn -pl ai-data-server spring-boot:run
```

启动网关：

```bash
mvn -pl ai-data-gateway spring-boot:run
```

启动前端：

```bash
cd ai-data-web
npm install
npm run dev
```

## 安全说明

- 生产环境不要提交真实数据库、Redis、第三方系统账号密码。
- 登录密码使用 BCrypt 加密保存，相同明文密码每次生成的密文不同。
- 接口响应和日志中不应输出明文密码或完整 token。
- 默认管理员账号仅用于初始化开发环境，生产环境应及时修改密码。

## 建设中

项目仍在持续建设中，当前重点是完成登录认证、权限体系、业务接口配置和前端管理后台基础功能。
