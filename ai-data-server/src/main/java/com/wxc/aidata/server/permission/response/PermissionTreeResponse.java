package com.wxc.aidata.server.permission.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 权限树响应对象，用于菜单/权限管理和角色授权树形展示。
 */
public record PermissionTreeResponse(
        Long id,
        Long parentId,
        String permissionName,
        String permissionCode,
        String permissionType,
        String routePath,
        String componentPath,
        String icon,
        Integer sortNo,
        Integer status,
        LocalDateTime createdTime,
        LocalDateTime updatedTime,
        List<PermissionTreeResponse> children
) {
}
