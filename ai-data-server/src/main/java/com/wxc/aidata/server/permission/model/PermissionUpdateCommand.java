package com.wxc.aidata.server.permission.model;

/**
 * 更新权限命令，权限编码可为空但非空时必须唯一。
 */
public record PermissionUpdateCommand(
        Long id,
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
