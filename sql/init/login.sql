INSERT INTO sys_user (
    id,
    username,
    password,
    nickname,
    mobile,
    email,
    status,
    deleted,
    last_login_time,
    created_by,
    created_time,
    updated_by,
    updated_time
) VALUES (
             1,
             'admin',
             '$2b$12$pemY2Wp9Sro2ISJNG/mmJOaPLDYhJ8py89w4bidE0iCBepmGxLKXy',
             '系统管理员',
             NULL,
             NULL,
             1,
             0,
             NULL,
             0,
             NOW(),
             0,
             NOW()
         );

INSERT INTO sys_role (
    id,
    role_code,
    role_name,
    status,
    description,
    created_time,
    updated_time
) VALUES (
             1,
             'admin',
             '系统管理员',
             1,
             '系统默认管理员角色',
             NOW(),
             NOW()
         );

INSERT INTO sys_permission (
    id,
    parent_id,
    permission_name,
    permission_code,
    permission_type,
    route_path,
    component_path,
    icon,
    sort_no,
    status,
    created_time,
    updated_time
) VALUES
      (1001, 0, '用户列表', 'system:user:list', 'API', NULL, NULL, NULL, 1001, 1, NOW(), NOW()),
      (1002, 0, '新增用户', 'system:user:add', 'API', NULL, NULL, NULL, 1002, 1, NOW(), NOW()),
      (1003, 0, '更新用户', 'system:user:update', 'API', NULL, NULL, NULL, 1003, 1, NOW(), NOW()),
      (1004, 0, '删除用户', 'system:user:delete', 'API', NULL, NULL, NULL, 1004, 1, NOW(), NOW()),
      (1101, 0, '角色列表', 'system:role:list', 'API', NULL, NULL, NULL, 1101, 1, NOW(), NOW()),
      (1102, 0, '新增角色', 'system:role:add', 'API', NULL, NULL, NULL, 1102, 1, NOW(), NOW()),
      (1103, 0, '更新角色', 'system:role:update', 'API', NULL, NULL, NULL, 1103, 1, NOW(), NOW()),
      (1104, 0, '删除角色', 'system:role:delete', 'API', NULL, NULL, NULL, 1104, 1, NOW(), NOW()),
      (1201, 0, 'Skill 列表', 'skill:config:list', 'API', NULL, NULL, NULL, 1201, 1, NOW(), NOW()),
      (1202, 0, '新增 Skill', 'skill:config:add', 'API', NULL, NULL, NULL, 1202, 1, NOW(), NOW()),
      (1203, 0, '更新 Skill', 'skill:config:update', 'API', NULL, NULL, NULL, 1203, 1, NOW(), NOW()),
      (1204, 0, '删除 Skill', 'skill:config:delete', 'API', NULL, NULL, NULL, 1204, 1, NOW(), NOW()),
      (1205, 0, '执行 Skill', 'skill:config:execute', 'API', NULL, NULL, NULL, 1205, 1, NOW(), NOW()),
      (1301, 0, '任务列表', 'job:config:list', 'API', NULL, NULL, NULL, 1301, 1, NOW(), NOW()),
      (1302, 0, '新增任务', 'job:config:add', 'API', NULL, NULL, NULL, 1302, 1, NOW(), NOW()),
      (1303, 0, '更新任务', 'job:config:update', 'API', NULL, NULL, NULL, 1303, 1, NOW(), NOW()),
      (1304, 0, '删除任务', 'job:config:delete', 'API', NULL, NULL, NULL, 1304, 1, NOW(), NOW()),
      (1305, 0, '执行任务', 'job:config:execute', 'API', NULL, NULL, NULL, 1305, 1, NOW(), NOW());

INSERT INTO sys_user_role (
    user_id,
    role_id
) VALUES (
             1,
             1
         );

INSERT INTO sys_role_permission (
    role_id,
    permission_id
)
SELECT
    1,
    id
FROM sys_permission;