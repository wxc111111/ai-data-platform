package com.wxc.aidata.server.user.request;

/**
 * 用户状态更新请求，用于启用或禁用账号。
 */
public record UserStatusRequest(
        Integer status
) {
}
