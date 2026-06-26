package com.wxc.aidata.server.permission.model;

/**
 * 创建权限命令，承载菜单、按钮或接口权限的基础字段。
 */
public record PermissionCreateCommand(
        Long parentId,
        String permissionName,
        String permissionCode,
        String permissionType,
        String routePath,
        String componentPath,
        String icon,
        Integer sortNo,
        Integer status
) {
}
