package com.wxc.aidata.server.role.request;

/**
 * 角色状态更新请求体，用于启用或禁用角色。
 */
public record RoleStatusRequest(
        Integer status
) {
}
