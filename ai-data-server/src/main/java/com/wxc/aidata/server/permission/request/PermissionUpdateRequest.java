package com.wxc.aidata.server.permission.request;

/**
 * 更新权限请求体，对应权限管理编辑表单。
 */
public record PermissionUpdateRequest(
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
