package com.wxc.aidata.server.user.request;

import java.util.List;

/**
 * 创建用户请求，来自后台用户管理页面。
 */
public record UserCreateRequest(
        String username,
        String password,
        String nickname,
        String mobile,
        String email,
        Integer status,
        List<Long> roleIds
) {
}
