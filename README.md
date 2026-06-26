# AI 数据服务中台

AI 数据服务中台是面向企业内部业务系统和 AI 应用的数据服务管理平台。平台目标是把 OA、CRM、HR 等第三方系统中的 HTTP 接口统一接入、统一配置、统一鉴权，并以标准化 Skill 的形式对外提供调用能力。

当前版本优先建设后台基础闭环：登录认证、用户管理、角色管理、权限管理、菜单权限控制、Redis 会话与权限缓存、统一响应、基础网关和数据库初始化脚本。业务系统接入、业务接口配置、Skill 发布调用、XXL-JOB 定时任务和执行日志仍属于后续建设范围。

## 项目定位

平台用于把企业已有系统的接口纳入统一后台管理。管理员可以维护平台用户、角色和菜单权限，并在后续版本中配置业务系统、接口地址、请求参数、认证方式和响应解析规则，再把这些接口发布为可被业务系统或 AI 应用调用的 Skill。

第一阶段不直接接入大模型，不做 Agent 编排，不做 RAG 知识库，重点是完成企业内部数据服务平台的账号、权限、接口网关和基础配置能力。

## 技术栈

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

## 工程结构

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

## 当前能力

已完成：

- Maven 多模块工程骨架。
- Gateway / Server 启动模块。
- MySQL、Redis 外部配置拆分。
- 统一响应结构、分页结构、业务异常和全局异常处理。
- Sa-Token 登录认证、退出、当前用户查询、当前权限查询。
- BCrypt 密码加密与校验。
- Redis 会话、权限缓存和 Jackson 序列化配置。
- RBAC 用户、角色、权限基础模型。
- 用户管理：分页查询、详情、新增、更新、启停、删除、分配角色。
- 角色管理：分页查询、详情、新增、更新、启停、删除、分配权限、角色下拉选项。
- 权限管理：权限树、详情、新增、更新、启停、删除。
- `admin` 超级管理员默认拥有全部启用权限。
- Vue 后台登录页、后台布局、首页概览、用户管理、角色管理、权限管理、无权限页。
- 前端登录状态、路由守卫和菜单权限控制。
- 后端单元测试覆盖认证、用户、角色、权限、缓存、ID 生成器和异常处理等核心逻辑。

数据库表已预留：

- 业务系统配置：`biz_system`
- 业务接口配置：`biz_api`、`biz_api_parameter`
- Skill 配置：`ai_skill`、`ai_skill_parameter`
- 定时任务：`sync_job_config`、`sync_job_log`
- 调用与审计日志：`ai_skill_execution_log`、`sys_login_log`、`sys_operation_log`

这些预留表对应的业务接口和前端页面尚未全部实现。

## 核心接口

认证接口：

```http
POST /api/auth/login
POST /api/auth/logout
GET  /api/auth/current-user
GET  /api/auth/permissions
```

用户管理：

```http
GET    /api/users
GET    /api/users/{id}
POST   /api/users
PUT    /api/users/{id}
PUT    /api/users/{id}/status
PUT    /api/users/{id}/roles
DELETE /api/users/{id}
```

角色管理：

```http
GET    /api/roles
GET    /api/roles/options
GET    /api/roles/{id}
POST   /api/roles
PUT    /api/roles/{id}
PUT    /api/roles/{id}/status
GET    /api/roles/{id}/permissions
PUT    /api/roles/{id}/permissions
DELETE /api/roles/{id}
```

权限管理：

```http
GET    /api/permissions/tree
GET    /api/permissions/{id}
POST   /api/permissions
PUT    /api/permissions/{id}
PUT    /api/permissions/{id}/status
DELETE /api/permissions/{id}
```

## 本地配置

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

## 数据库初始化

首次本地运行前，先创建并初始化数据库：

```bash
mysql -u root -p < sql/init/schema.sql
mysql -u root -p ai_data_platform < sql/init/login.sql
```

如已有旧数据，需要补齐系统菜单和权限结构，可执行：

```bash
mysql -u root -p ai_data_platform < sql/update/20260625-system-menu-permissions.sql
```

默认开发账号：

```text
用户名：admin
密码：admin
```

默认账号仅用于开发和初始化验证，生产环境必须及时修改密码。

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

默认访问地址：

```text
前端：http://localhost:5173
网关：http://localhost:7070
业务服务：http://localhost:7071
```

## 测试

后端测试：

```bash
mvn test
```

MyBatis-Plus 使用约束：

- 项目已接入 `mybatis-plus-spring-boot3-starter`。
- 现阶段继续使用 Mapper 接口 + XML SQL。
- 禁止使用 `LambdaQueryWrapper`、`LambdaUpdateWrapper`、`Wrappers.lambdaQuery()`、`Wrappers.lambdaUpdate()` 等 Lambda 形式 SQL 操作。
- `MybatisPlusSqlStyleTest` 会扫描源码，防止引入 Lambda Wrapper 写法。

前端类型检查与构建：

```bash
cd ai-data-web
npm run build
```

## 日志

项目使用 Spring Boot 默认的 `SLF4J + Logback`。

已配置：

- `ai-data-server/src/main/resources/logback-spring.xml`
- `ai-data-gateway/src/main/resources/logback-spring.xml`

默认输出：

```text
logs/ai-data-server/app.log
logs/ai-data-gateway/app.log
```

日志文件按天滚动，默认保留 30 天。

业务服务已接入请求日志过滤器：

```text
ai-data-server/src/main/java/com/wxc/aidata/server/common/logging/RequestLoggingFilter.java
```

每个 HTTP 请求会记录：

```text
requestId
method
path
status
costMs
```

调用方可通过请求头透传链路标识：

```http
X-Request-Id: trace-001
```

业务代码中建议使用 SLF4J 占位符写法：

```java
private static final Logger log = LoggerFactory.getLogger(DemoService.class);

log.info("用户登录成功，userId={}, username={}", userId, username);
log.warn("接口响应较慢，path={}, costMs={}", path, costMs);
log.error("调用第三方接口失败，apiCode={}", apiCode, e);
```

不要记录明文密码、完整 Token、数据库密码或第三方系统密钥。

## 安全说明

- 登录密码使用 BCrypt 加密保存，不保存明文密码。
- 接口响应和日志中不应输出明文密码或完整 Token。
- `admin` 用户和 `admin` 角色是初始化超级管理员，管理端限制直接修改或删除。
- `admin` 角色默认拥有全部启用权限，避免新增菜单或接口权限后无法访问。
- 本地配置文件中如包含真实连接信息，应避免提交到远程仓库。

## 后续建设

- 业务系统配置管理。
- 业务接口配置和在线测试。
- Skill 配置、发布、鉴权和调用日志。
- XXL-JOB 定时任务接入和执行日志。
- 登录日志、操作日志和审计查询。
- 网关统一鉴权、限流和请求追踪。
- 生产部署文档和配置安全加固。
