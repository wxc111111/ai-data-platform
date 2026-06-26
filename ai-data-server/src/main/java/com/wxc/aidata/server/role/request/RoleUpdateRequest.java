package com.wxc.aidata.server.role.request;

/**
 * 更新角色请求体，对应角色管理编辑表单。
 */
public record RoleUpdateRequest(
        String roleCode,
        String roleName,
        Integer status,
        String description
) {
}
