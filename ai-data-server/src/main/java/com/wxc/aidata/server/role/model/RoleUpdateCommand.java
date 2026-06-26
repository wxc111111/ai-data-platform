package com.wxc.aidata.server.role.model;

/**
 * 更新角色命令，角色编码允许编辑但必须保持唯一。
 */
public record RoleUpdateCommand(
        Long id,
        String roleCode,
        String roleName,
        Integer status,
        String description
) {
}
