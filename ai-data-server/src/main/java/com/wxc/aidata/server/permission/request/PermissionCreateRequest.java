package com.wxc.aidata.server.permission.request;

/**
 * 创建权限请求体，对应权限管理新增表单。
 */
public record PermissionCreateRequest(
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
