# AI 数据服务中台

AI 数据服务中台是面向企业内部业务系统和 AI 应用的数据服务管理平台。平台用于把 OA、CRM、HR 等第三方系统的 HTTP 接口统一接入、统一配置、统一鉴权、统一测试，并逐步沉淀为标准化 Skill 能力，对业务系统或 AI 应用提供可控的数据服务。

当前阶段重点建设后台基础能力和数据接入能力：登录认证、用户管理、角色管理、权限管理、动态菜单、业务系统管理、业务接口管理、接口在线测试、登录日志、操作日志、Redis 会话与权限缓存、统一响应、基础网关和数据库初始化脚本。

## 项目说明

平台定位：

- 统一管理企业内部业务系统接入信息。
- 统一维护业务接口地址、请求方法、参数定义、响应数据路径和在线测试结果。
- 统一维护后台用户、角色、菜单权限和接口权限。
- 统一记录登录日志和后台操作日志，便于审计追踪。
- 为后续 Skill 发布、任务调度、调用日志和数据服务治理提供基础模型。

当前不直接接入大模型，不做 Agent 编排，不做 RAG 知识库。第一阶段以账号权限、后台管理、业务系统接入、业务接口配置、接口测试和审计能力为主。

默认开发账号：

```text
用户名：admin
密码：admin
```

默认账号仅用于开发和初始化验证，生产环境必须及时修改密码。

## 架构说明

### 技术栈

后端：

- JDK 21
- Spring Boot 3.3.5
- Spring Cloud Gateway
- Sa-Token
- MyBatis-Plus
- PageHelper
- MySQL 8
- Redis
- Maven

前端：

- Vue 3
- Vite
- TypeScript
- Pinia
- Vue Router
- Element Plus
- Axios
- Sass

### 工程结构

```text
ai-data-platform
├── ai-data-api        接口契约、枚举和 DTO 定义
├── ai-data-common     公共响应、上下文、异常和通用常量
├── ai-data-gateway    统一网关入口，默认监听 7070
├── ai-data-server     核心业务服务，默认监听 7071
├── ai-data-web        Vue 3 后台管理前端
├── config             本地和生产配置模板
├── docs               项目计划和说明文档
├── sql
│   ├── init           数据库初始化脚本
│   └── update         增量更新脚本
└── 项目代码目录详情    功能说明文档
```

### 模块职责

- `ai-data-gateway`：统一网关入口，后续承载鉴权、限流、请求追踪等横切能力。
- `ai-data-server`：核心业务服务，包含认证、用户、角色、权限、菜单、业务系统、业务接口和审计日志。
- `ai-data-common`：统一响应、异常、分页、上下文和通用基础能力。
- `ai-data-api`：预留接口契约、枚举和 DTO 定义。
- `ai-data-web`：后台管理前端，菜单由后端按当前用户权限动态返回。

### 数据库模型

已建设基础表：

- 用户、角色、权限：`sys_user`、`sys_role`、`sys_permission`、`sys_user_role`、`sys_role_permission`
- 业务系统与接口：`biz_system`、`biz_api`、`biz_api_parameter`
- 审计日志：`sys_login_log`、`sys_operation_log`

已预留扩展表：

- Skill 配置：`ai_skill`、`ai_skill_parameter`
- 定时任务：`sync_job_config`、`sync_job_log`
- Skill 调用日志：`ai_skill_execution_log`

### 权限与菜单

- 登录态由 Sa-Token 管理。
- 用户权限会缓存到 Redis。
- `admin` 角色默认拥有全部启用权限。
- 前端侧边栏菜单由后端根据当前用户权限裁剪后返回。
- 无权限编码且无路由的分组菜单，只有存在可见子菜单时才展示。
- 路由守卫仍会按路由权限做兜底校验，避免直接输入地址越权访问。

## 配置说明

### 本地配置

后端默认使用 `local` 环境，并从以下位置读取本地配置：

```text
config/app-config-local.properties
```

需要配置：

```properties
MYSQL_HOST=
MYSQL_PORT=
MYSQL_USER=
MYSQL_PASSWORD=

REDIS_HOST=
REDIS_PORT=
REDIS_PASSWORD=
```

生产模板：

```text
config/app-config-prod.properties
```

生产环境不要提交真实数据库、Redis 或第三方系统账号密码。建议通过环境变量、配置中心或部署平台密钥注入。

### 数据库初始化

全新库首次运行：

```bash
mysql -u root -p < sql/init/schema.sql
mysql -u root -p ai_data_platform < sql/init/login.sql
```

已有库增量更新按顺序执行：

```bash
mysql -u root -p ai_data_platform < sql/update/20260625-system-menu-permissions.sql
mysql -u root -p ai_data_platform < sql/update/20260626-audit-log-permissions.sql
mysql -u root -p ai_data_platform < sql/update/20260626-business-system-permissions.sql
mysql -u root -p ai_data_platform < sql/update/20260626-business-api-permissions.sql
mysql -u root -p ai_data_platform < sql/update/20260626-current-user-menus.sql
```

`20260626-business-system-permissions.sql` 会创建“数据接入”和“业务系统管理”菜单。
`20260626-business-api-permissions.sql` 会创建“业务接口管理”菜单、按钮权限和在线测试权限。
`20260626-current-user-menus.sql` 会补齐“首页”“数据接入”公共菜单，并将“业务系统管理”挂到“数据接入”分组下。

### 本地启动

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

默认访问地址：

```text
前端：http://localhost:5173
网关：http://localhost:7070
业务服务：http://localhost:7071
```

### 测试与构建

后端测试：

```bash
mvn test
```

前端类型检查与构建：

```bash
cd ai-data-web
npm run build
```

### 日志配置

日志配置文件：

```text
ai-data-server/src/main/resources/logback-spring.xml
ai-data-gateway/src/main/resources/logback-spring.xml
```

默认输出：

```text
logs/ai-data-server/app.log
logs/ai-data-gateway/app.log
```

每个 HTTP 请求会记录 `requestId`、`method`、`path`、`status`、`costMs`。调用方可通过请求头透传链路标识：

```http
X-Request-Id: trace-001
```

不要记录明文密码、完整 Token、数据库密码或第三方系统密钥。

### 安全配置

- 登录密码使用 BCrypt 加密保存，不保存明文密码。
- 接口响应和日志中不应输出明文密码或完整 Token。
- `admin` 用户和 `admin` 角色是初始化超级管理员，管理端限制直接修改或删除。
- `admin` 角色默认拥有全部启用权限，避免新增菜单或接口权限后无法访问。
- 本地配置文件中如包含真实连接信息，应避免提交到远程仓库。
