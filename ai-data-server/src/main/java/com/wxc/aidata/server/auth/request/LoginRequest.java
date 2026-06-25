package com.wxc.aidata.server.auth.request;

/**
 * 登录请求参数，接收前端提交的账号和密码。
 */
public record LoginRequest(
        // 登录账号。
        String username,
        // 登录密码。
        String password
) {
}
