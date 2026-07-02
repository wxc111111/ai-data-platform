CREATE DATABASE IF NOT EXISTS ai_data_platform
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE ai_data_platform;

DROP TABLE IF EXISTS sys_operation_log;
DROP TABLE IF EXISTS sys_login_log;
DROP TABLE IF EXISTS ai_skill_execution_log;
DROP TABLE IF EXISTS sync_job_log;
DROP TABLE IF EXISTS sync_job_config;
DROP TABLE IF EXISTS ai_skill_parameter;
DROP TABLE IF EXISTS ai_skill;
DROP TABLE IF EXISTS ai_skill_role;
DROP TABLE IF EXISTS biz_api_parameter;
DROP TABLE IF EXISTS biz_api;
DROP TABLE IF EXISTS biz_api_role;
DROP TABLE IF EXISTS biz_system;
DROP TABLE IF EXISTS biz_system_role;
DROP TABLE IF EXISTS sys_role_permission;
DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_permission;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS sys_user;

CREATE TABLE sys_user (
    id              BIGINT NOT NULL COMMENT '主键ID',
    username        VARCHAR(64) NOT NULL COMMENT '登录用户名，全局唯一',
    password        VARCHAR(255) NOT NULL COMMENT 'BCrypt 加密后的登录密码',
    nickname        VARCHAR(128) COMMENT '用户昵称或显示名称',
    mobile          VARCHAR(32) COMMENT '手机号码',
    email           VARCHAR(128) COMMENT '邮箱地址',
    status          TINYINT NOT NULL DEFAULT 1 COMMENT '用户状态：1启用，0禁用',
    deleted         TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0未删除，1已删除',
    last_login_time DATETIME COMMENT '最后登录时间',
    created_by      BIGINT COMMENT '创建人用户ID',
    created_time    DATETIME NOT NULL COMMENT '创建时间',
    updated_by      BIGINT COMMENT '最后更新人用户ID',
    updated_time    DATETIME NOT NULL COMMENT '最后更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表，保存平台登录用户基础信息';

CREATE TABLE sys_role (
    id              BIGINT NOT NULL COMMENT '主键ID',
    role_code       VARCHAR(64) NOT NULL COMMENT '角色编码，全局唯一',
    role_name       VARCHAR(128) NOT NULL COMMENT '角色名称',
    status          TINYINT NOT NULL DEFAULT 1 COMMENT '角色状态：1启用，0禁用',
    description     VARCHAR(500) COMMENT '角色说明',
    created_time    DATETIME NOT NULL COMMENT '创建时间',
    updated_time    DATETIME NOT NULL COMMENT '最后更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色表，保存 RBAC 角色定义';

CREATE TABLE sys_permission (
    id               BIGINT NOT NULL COMMENT '主键ID',
    parent_id        BIGINT NOT NULL DEFAULT 0 COMMENT '父级权限ID，0表示根节点',
    permission_name  VARCHAR(128) NOT NULL COMMENT '权限名称',
    permission_code  VARCHAR(128) COMMENT '权限编码，接口和按钮鉴权使用',
    permission_type  VARCHAR(16) NOT NULL COMMENT '权限类型：MENU、BUTTON、API',
    route_path       VARCHAR(255) COMMENT '前端路由路径',
    component_path   VARCHAR(255) COMMENT '前端组件路径',
    icon             VARCHAR(64) COMMENT '菜单图标',
    sort_no          INT NOT NULL DEFAULT 0 COMMENT '排序号，值越小越靠前',
    status           TINYINT NOT NULL DEFAULT 1 COMMENT '权限状态：1启用，0禁用',
    created_time     DATETIME NOT NULL COMMENT '创建时间',
    updated_time     DATETIME NOT NULL COMMENT '最后更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_permission_code (permission_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='权限表，保存菜单、按钮和接口权限';

CREATE TABLE sys_user_role (
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户角色关系表，保存用户与角色的多对多关系';

CREATE TABLE sys_role_permission (
    role_id       BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    PRIMARY KEY (role_id, permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色权限关系表，保存角色与权限的多对多关系';

CREATE TABLE biz_system (
    id                BIGINT NOT NULL COMMENT '主键ID',
    system_code       VARCHAR(64) NOT NULL COMMENT '业务系统编码，全局唯一',
    system_name       VARCHAR(128) NOT NULL COMMENT '业务系统名称',
    base_url          VARCHAR(500) NOT NULL COMMENT '业务系统基础访问地址',
    auth_type         VARCHAR(32) NOT NULL DEFAULT 'NONE' COMMENT '认证类型：NONE、BASIC、BEARER_TOKEN、API_KEY、CUSTOM_HEADER',
    auth_config       TEXT COMMENT '认证配置 JSON，敏感字段加密保存',
    connect_timeout   INT NOT NULL DEFAULT 5000 COMMENT '连接超时时间，单位毫秒',
    read_timeout      INT NOT NULL DEFAULT 10000 COMMENT '读取超时时间，单位毫秒',
    status            TINYINT NOT NULL DEFAULT 1 COMMENT '系统状态：1启用，0禁用',
    description       VARCHAR(500) COMMENT '业务系统说明',
    created_by        BIGINT NOT NULL COMMENT '创建人用户ID',
    created_time      DATETIME NOT NULL COMMENT '创建时间',
    updated_by        BIGINT NOT NULL COMMENT '最后更新人用户ID',
    updated_time      DATETIME NOT NULL COMMENT '最后更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_system_code (system_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业务系统表，保存 OA、CRM、HR 等第三方系统配置';

CREATE TABLE biz_system_role (
    system_id BIGINT NOT NULL COMMENT '业务系统ID',
    role_id   BIGINT NOT NULL COMMENT '可见角色ID',
    PRIMARY KEY (system_id, role_id),
    KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业务系统角色范围表，控制业务系统对哪些角色可见';

CREATE TABLE biz_api (
    id                  BIGINT NOT NULL COMMENT '主键ID',
    system_id           BIGINT NOT NULL COMMENT '所属业务系统ID',
    api_code            VARCHAR(128) NOT NULL COMMENT '业务接口编码，全局唯一',
    api_name            VARCHAR(256) NOT NULL COMMENT '业务接口名称',
    request_path        VARCHAR(500) NOT NULL COMMENT '请求路径，可包含 Path 参数占位符',
    request_method      VARCHAR(16) NOT NULL COMMENT '请求方法：GET、POST、PUT、DELETE、PATCH',
    content_type        VARCHAR(64) COMMENT '请求内容类型',
    connect_timeout     INT COMMENT '接口级连接超时时间，单位毫秒',
    read_timeout        INT COMMENT '接口级读取超时时间，单位毫秒',
    response_data_path  VARCHAR(500) COMMENT '响应数据提取路径，例如 data.records',
    status              TINYINT NOT NULL DEFAULT 1 COMMENT '接口状态：1启用，0禁用',
    description         VARCHAR(1000) COMMENT '业务接口说明',
    created_by          BIGINT NOT NULL COMMENT '创建人用户ID',
    created_time        DATETIME NOT NULL COMMENT '创建时间',
    updated_by          BIGINT NOT NULL COMMENT '最后更新人用户ID',
    updated_time        DATETIME NOT NULL COMMENT '最后更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_api_code (api_code),
    KEY idx_system_id (system_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业务接口表，保存第三方 HTTP 接口配置';

CREATE TABLE biz_api_role (
    api_id  BIGINT NOT NULL COMMENT '业务接口ID',
    role_id BIGINT NOT NULL COMMENT '可见角色ID',
    PRIMARY KEY (api_id, role_id),
    KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业务接口角色范围表，控制业务接口对哪些角色可见';

CREATE TABLE biz_api_parameter (
    id                  BIGINT NOT NULL COMMENT '主键ID',
    api_id              BIGINT NOT NULL COMMENT '所属业务接口ID',
    parameter_name      VARCHAR(128) NOT NULL COMMENT '参数名称',
    parameter_location  VARCHAR(16) NOT NULL COMMENT '参数位置：PATH、QUERY、HEADER、BODY',
    parameter_type      VARCHAR(32) NOT NULL COMMENT '参数类型：STRING、INTEGER、LONG、DECIMAL、BOOLEAN、DATE、DATETIME、ARRAY、OBJECT',
    required            TINYINT NOT NULL DEFAULT 0 COMMENT '是否必填：1必填，0非必填',
    default_value       VARCHAR(1000) COMMENT '默认值',
    description         VARCHAR(500) COMMENT '参数说明',
    sort_no             INT NOT NULL DEFAULT 0 COMMENT '排序号，值越小越靠前',
    created_time        DATETIME NOT NULL COMMENT '创建时间',
    updated_time        DATETIME NOT NULL COMMENT '最后更新时间',
    PRIMARY KEY (id),
    KEY idx_api_id (api_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业务接口参数表，保存第三方接口入参定义';

CREATE TABLE ai_skill (
    id                  BIGINT NOT NULL COMMENT '主键ID',
    skill_code          VARCHAR(128) NOT NULL COMMENT 'Skill 编码，全局唯一',
    skill_name          VARCHAR(256) NOT NULL COMMENT 'Skill 名称',
    description         VARCHAR(1000) NOT NULL COMMENT 'Skill 能力说明',
    api_id              BIGINT NOT NULL COMMENT '绑定的业务接口ID',
    permission_code     VARCHAR(128) COMMENT '调用该 Skill 需要的权限编码',
    visibility          VARCHAR(32) NOT NULL DEFAULT 'PRIVATE' COMMENT 'Skill 类型：PRIVATE私有，PUBLIC公共',
    timeout_ms          INT NOT NULL DEFAULT 10000 COMMENT 'Skill 执行超时时间，单位毫秒',
    max_result_count    INT NOT NULL DEFAULT 100 COMMENT '最大返回结果数量',
    status              TINYINT NOT NULL DEFAULT 1 COMMENT 'Skill 状态：1启用，0禁用',
    version_no          INT NOT NULL DEFAULT 1 COMMENT '版本号，用于配置变更控制',
    created_by          BIGINT NOT NULL COMMENT '创建人用户ID',
    created_time        DATETIME NOT NULL COMMENT '创建时间',
    updated_by          BIGINT NOT NULL COMMENT '最后更新人用户ID',
    updated_time        DATETIME NOT NULL COMMENT '最后更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_skill_code (skill_code),
    KEY idx_api_id (api_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Skill 表，保存可对外调用的业务能力配置';

CREATE TABLE ai_skill_role (
    skill_id BIGINT NOT NULL COMMENT 'Skill ID',
    role_id  BIGINT NOT NULL COMMENT '可见角色ID',
    PRIMARY KEY (skill_id, role_id),
    KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Skill 角色范围表，控制 Skill 对哪些角色可见';

CREATE TABLE ai_skill_parameter (
    id                  BIGINT NOT NULL COMMENT '主键ID',
    skill_id            BIGINT NOT NULL COMMENT '所属 Skill ID',
    parameter_name      VARCHAR(128) NOT NULL COMMENT 'Skill 对外暴露的参数名称',
    parameter_type      VARCHAR(32) NOT NULL COMMENT '参数类型：STRING、INTEGER、LONG、DECIMAL、BOOLEAN、DATE、DATETIME、ARRAY、OBJECT',
    required            TINYINT NOT NULL DEFAULT 0 COMMENT '是否必填：1必填，0非必填',
    description         VARCHAR(500) COMMENT '参数说明',
    api_parameter_name  VARCHAR(128) NOT NULL COMMENT '映射到业务接口的参数名称',
    default_value       VARCHAR(1000) COMMENT '默认值',
    value_source        VARCHAR(32) NOT NULL DEFAULT 'CALLER' COMMENT '参数值来源：CALLER、CONSTANT、LOGIN_USER、LOGIN_ROLE',
    validation_rule     VARCHAR(1000) COMMENT '参数校验规则，建议使用 JSON 保存',
    sort_no             INT NOT NULL DEFAULT 0 COMMENT '排序号，值越小越靠前',
    created_time        DATETIME NOT NULL COMMENT '创建时间',
    updated_time        DATETIME NOT NULL COMMENT '最后更新时间',
    PRIMARY KEY (id),
    KEY idx_skill_id (skill_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Skill 参数表，保存 Skill 入参与接口参数映射';

CREATE TABLE sync_job_config (
    id                   BIGINT NOT NULL COMMENT '主键ID',
    job_code             VARCHAR(128) NOT NULL COMMENT '定时任务编码，全局唯一',
    job_name             VARCHAR(256) NOT NULL COMMENT '定时任务名称',
    api_id               BIGINT NOT NULL COMMENT '定时调用的业务接口ID',
    cron_expression      VARCHAR(128) NOT NULL COMMENT 'Cron 表达式',
    executor_handler     VARCHAR(128) NOT NULL COMMENT 'XXL-JOB 执行器 Handler 名称',
    request_parameters   TEXT COMMENT '任务固定请求参数 JSON',
    save_raw_file        TINYINT NOT NULL DEFAULT 1 COMMENT '是否保存原始响应文件：1保存，0不保存',
    retry_count          INT NOT NULL DEFAULT 0 COMMENT '失败重试次数',
    timeout_seconds      INT NOT NULL DEFAULT 300 COMMENT '任务执行超时时间，单位秒',
    status               TINYINT NOT NULL DEFAULT 1 COMMENT '任务状态：1启用，0停用',
    xxl_job_id           INT COMMENT 'XXL-JOB Admin 中对应的任务ID',
    description          VARCHAR(1000) COMMENT '任务说明',
    created_time         DATETIME NOT NULL COMMENT '创建时间',
    updated_time         DATETIME NOT NULL COMMENT '最后更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_job_code (job_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='定时任务配置表，保存平台侧业务任务定义';

CREATE TABLE sync_job_log (
    id                   BIGINT NOT NULL COMMENT '主键ID',
    job_config_id        BIGINT NOT NULL COMMENT '任务配置ID',
    xxl_log_id           BIGINT COMMENT 'XXL-JOB 调度日志ID',
    trigger_type         VARCHAR(32) NOT NULL COMMENT '触发类型：SCHEDULE、MANUAL、RETRY',
    start_time           DATETIME NOT NULL COMMENT '任务开始时间',
    end_time             DATETIME COMMENT '任务结束时间',
    status               VARCHAR(32) NOT NULL COMMENT '执行状态：RUNNING、SUCCESS、FAILED',
    request_parameters   TEXT COMMENT '本次执行请求参数 JSON',
    response_file_path   VARCHAR(1000) COMMENT '响应文件保存路径',
    response_count       BIGINT COMMENT '响应记录数量',
    error_message        TEXT COMMENT '失败错误信息',
    created_time         DATETIME NOT NULL COMMENT '日志创建时间',
    PRIMARY KEY (id),
    KEY idx_job_config_id (job_config_id),
    KEY idx_start_time (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='定时任务执行日志表，保存每次任务执行结果';

CREATE TABLE ai_skill_execution_log (
    id                  BIGINT NOT NULL COMMENT '主键ID',
    request_id          VARCHAR(64) NOT NULL COMMENT '请求唯一ID',
    skill_code          VARCHAR(128) NOT NULL COMMENT '被调用的 Skill 编码',
    user_id             BIGINT COMMENT '调用用户ID',
    request_arguments   TEXT COMMENT '调用入参 JSON，敏感字段脱敏后保存',
    start_time          DATETIME NOT NULL COMMENT '调用开始时间',
    end_time            DATETIME COMMENT '调用结束时间',
    duration_ms         BIGINT COMMENT '调用耗时，单位毫秒',
    status              VARCHAR(32) NOT NULL COMMENT '执行状态：SUCCESS、FAILED',
    result_count        BIGINT COMMENT '返回结果数量',
    error_message       TEXT COMMENT '失败错误信息',
    created_time        DATETIME NOT NULL COMMENT '日志创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_request_id (request_id),
    KEY idx_skill_code (skill_code),
    KEY idx_start_time (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Skill 调用日志表，保存 Skill 每次调用记录';

CREATE TABLE sys_login_log (
    id              BIGINT NOT NULL COMMENT '主键ID',
    user_id         BIGINT COMMENT '登录用户ID，失败时可为空',
    username        VARCHAR(64) COMMENT '登录用户名',
    login_ip        VARCHAR(64) COMMENT '登录来源IP',
    user_agent      VARCHAR(1000) COMMENT '浏览器或客户端 User-Agent',
    login_status    VARCHAR(32) NOT NULL COMMENT '登录状态：SUCCESS、FAILED',
    message         VARCHAR(500) COMMENT '登录结果说明',
    login_time      DATETIME NOT NULL COMMENT '登录时间',
    PRIMARY KEY (id),
    KEY idx_username (username),
    KEY idx_login_time (login_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='登录日志表，保存用户登录成功和失败记录';

CREATE TABLE sys_operation_log (
    id              BIGINT NOT NULL COMMENT '主键ID',
    request_id      VARCHAR(64) COMMENT '请求唯一ID',
    user_id         BIGINT COMMENT '操作用户ID',
    username        VARCHAR(64) COMMENT '操作用户名',
    module_name     VARCHAR(128) COMMENT '业务模块名称',
    operation_name  VARCHAR(256) COMMENT '操作名称',
    request_method  VARCHAR(16) COMMENT 'HTTP 请求方法',
    request_path    VARCHAR(500) COMMENT '请求路径',
    request_ip      VARCHAR(64) COMMENT '请求来源IP',
    request_params  TEXT COMMENT '请求参数 JSON，敏感字段脱敏后保存',
    status          VARCHAR(32) COMMENT '操作状态：SUCCESS、FAILED',
    duration_ms     BIGINT COMMENT '操作耗时，单位毫秒',
    error_message   TEXT COMMENT '失败错误信息',
    created_time    DATETIME NOT NULL COMMENT '日志创建时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_created_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='操作日志表，保存后台重要操作审计记录';
