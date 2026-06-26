package com.wxc.aidata.server.user.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户管理响应对象，不返回密码密文。
 */
public record UserResponse(
        Long id,
        String username,
        String nickname,
        String mobile,
        String email,
        Integer status,
        LocalDateTime lastLoginTime,
        LocalDateTime createdTime,
        LocalDateTime updatedTime,
        List<Long> roleIds
) {
}
