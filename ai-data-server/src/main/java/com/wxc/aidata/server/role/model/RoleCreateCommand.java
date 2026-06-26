package com.wxc.aidata.server.role.model;

/**
 * 创建角色命令，承载新增角色所需的业务字段。
 */
public record RoleCreateCommand(
        String roleCode,
        String roleName,
        Integer status,
        String description
) {
}
