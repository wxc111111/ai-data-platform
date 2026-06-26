package com.wxc.aidata.server.role.request;

/**
 * 创建角色请求体，对应角色管理新增表单。
 */
public record RoleCreateRequest(
        String roleCode,
        String roleName,
        Integer status,
        String description
) {
}
