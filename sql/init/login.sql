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
      (800, 0, '首页', NULL, 'MENU', '/home/overview', 'views/DashboardHomeView.vue', 'House', 800, 1, NOW(), NOW()),
      (900, 0, '系统管理', 'system:manage', 'MENU', NULL, NULL, 'Setting', 900, 1, NOW(), NOW()),
      (950, 0, '数据接入', NULL, 'MENU', NULL, NULL, 'Database', 950, 1, NOW(), NOW()),
      (1000, 900, '用户管理', 'system:user:menu', 'MENU', '/home/users', 'views/UserManageView.vue', 'User', 1000, 1, NOW(), NOW()),
      (1001, 1000, '用户列表', 'system:user:list', 'API', NULL, NULL, NULL, 1001, 1, NOW(), NOW()),
      (1002, 1000, '新增用户', 'system:user:add', 'API', NULL, NULL, NULL, 1002, 1, NOW(), NOW()),
      (1003, 1000, '更新用户', 'system:user:update', 'API', NULL, NULL, NULL, 1003, 1, NOW(), NOW()),
      (1004, 1000, '删除用户', 'system:user:delete', 'API', NULL, NULL, NULL, 1004, 1, NOW(), NOW()),
      (1100, 900, '角色管理', 'system:role:menu', 'MENU', '/home/roles', 'views/RoleManageView.vue', 'UserFilled', 1100, 1, NOW(), NOW()),
      (1101, 1100, '角色列表', 'system:role:list', 'API', NULL, NULL, NULL, 1101, 1, NOW(), NOW()),
      (1102, 1100, '新增角色', 'system:role:add', 'API', NULL, NULL, NULL, 1102, 1, NOW(), NOW()),
      (1103, 1100, '更新角色', 'system:role:update', 'API', NULL, NULL, NULL, 1103, 1, NOW(), NOW()),
      (1104, 1100, '删除角色', 'system:role:delete', 'API', NULL, NULL, NULL, 1104, 1, NOW(), NOW()),
      (1150, 900, '菜单权限', 'system:permission:menu', 'MENU', '/home/permissions', 'views/PermissionManageView.vue', 'Key', 1150, 1, NOW(), NOW()),
      (1151, 1150, '权限列表', 'system:permission:list', 'API', NULL, NULL, NULL, 1151, 1, NOW(), NOW()),
      (1152, 1150, '新增权限', 'system:permission:add', 'API', NULL, NULL, NULL, 1152, 1, NOW(), NOW()),
      (1153, 1150, '更新权限', 'system:permission:update', 'API', NULL, NULL, NULL, 1153, 1, NOW(), NOW()),
      (1154, 1150, '删除权限', 'system:permission:delete', 'API', NULL, NULL, NULL, 1154, 1, NOW(), NOW()),
      (1160, 950, '业务系统管理', 'system:business-system:menu', 'MENU', '/home/business-systems', 'views/BusinessSystemView.vue', 'Connection', 1160, 1, NOW(), NOW()),
      (1161, 1160, '业务系统列表', 'system:business-system:list', 'API', NULL, NULL, NULL, 1161, 1, NOW(), NOW()),
      (1162, 1160, '新增业务系统', 'system:business-system:add', 'API', NULL, NULL, NULL, 1162, 1, NOW(), NOW()),
      (1163, 1160, '更新业务系统', 'system:business-system:update', 'API', NULL, NULL, NULL, 1163, 1, NOW(), NOW()),
      (1164, 1160, '删除业务系统', 'system:business-system:delete', 'API', NULL, NULL, NULL, 1164, 1, NOW(), NOW()),
      (1170, 950, '业务接口管理', 'system:business-api:menu', 'MENU', '/home/business-apis', 'views/BusinessApiView.vue', 'Link', 1170, 1, NOW(), NOW()),
      (1171, 1170, '业务接口列表', 'system:business-api:list', 'API', NULL, NULL, NULL, 1171, 1, NOW(), NOW()),
      (1172, 1170, '新增业务接口', 'system:business-api:add', 'API', NULL, NULL, NULL, 1172, 1, NOW(), NOW()),
      (1173, 1170, '更新业务接口', 'system:business-api:update', 'API', NULL, NULL, NULL, 1173, 1, NOW(), NOW()),
      (1174, 1170, '删除业务接口', 'system:business-api:delete', 'API', NULL, NULL, NULL, 1174, 1, NOW(), NOW()),
      (1175, 1170, '业务接口测试', 'system:business-api:test', 'API', NULL, NULL, NULL, 1175, 1, NOW(), NOW()),
      (1200, 900, '登录日志', 'system:login-log:menu', 'MENU', '/home/login-logs', 'views/LoginLogView.vue', 'Document', 1200, 1, NOW(), NOW()),
      (1201, 1200, '登录日志查询', 'system:login-log:list', 'API', NULL, NULL, NULL, 1201, 1, NOW(), NOW()),
      (1210, 900, '操作日志', 'system:operation-log:menu', 'MENU', '/home/operation-logs', 'views/OperationLogView.vue', 'Tickets', 1210, 1, NOW(), NOW()),
      (1211, 1210, '操作日志查询', 'system:operation-log:list', 'API', NULL, NULL, NULL, 1211, 1, NOW(), NOW());

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
