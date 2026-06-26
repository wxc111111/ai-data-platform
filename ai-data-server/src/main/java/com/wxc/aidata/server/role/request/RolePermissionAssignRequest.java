package com.wxc.aidata.server.role.request;

import java.util.List;

/**
 * 角色授权请求体，前端以权限 ID 集合覆盖保存角色授权。
 */
public record RolePermissionAssignRequest(
        List<Long> permissionIds
) {
}
