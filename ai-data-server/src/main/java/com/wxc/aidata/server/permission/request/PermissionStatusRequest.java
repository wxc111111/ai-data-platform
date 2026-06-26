package com.wxc.aidata.server.permission.request;

/**
 * 权限状态更新请求体，用于启用或禁用权限。
 */
public record PermissionStatusRequest(
        Integer status
) {
}
