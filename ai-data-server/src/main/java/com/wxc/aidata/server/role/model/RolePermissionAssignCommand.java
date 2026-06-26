package com.wxc.aidata.server.role.model;

import java.util.List;

/**
 * 角色授权命令，承载角色与权限 ID 集合的覆盖保存请求。
 */
public record RolePermissionAssignCommand(
        Long roleId,
        List<Long> permissionIds
) {
}
