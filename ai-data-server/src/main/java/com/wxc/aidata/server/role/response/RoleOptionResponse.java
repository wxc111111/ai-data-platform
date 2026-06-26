package com.wxc.aidata.server.role.response;

/**
 * 角色选项响应，用于用户分配角色下拉框。
 */
public record RoleOptionResponse(
        Long id,
        String roleCode,
        String roleName
) {
}
