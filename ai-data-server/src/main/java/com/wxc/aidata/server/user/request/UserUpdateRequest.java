package com.wxc.aidata.server.user.request;

/**
 * 更新用户请求，不包含密码字段，避免普通编辑误改登录密码。
 */
public record UserUpdateRequest(
        String nickname,
        String mobile,
        String email,
        Integer status
) {
}
