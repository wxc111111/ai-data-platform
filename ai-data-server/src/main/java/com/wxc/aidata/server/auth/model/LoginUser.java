package com.wxc.aidata.server.auth.model;

/**
 * 前端可见的登录用户信息。
 */
public record LoginUser(
        // 用户主键 ID，用于前端展示和后续业务关联。
        Long id,
        // 登录账号。
        String username,
        // 用户昵称，用于页面展示。
        String nickname
) {
}
