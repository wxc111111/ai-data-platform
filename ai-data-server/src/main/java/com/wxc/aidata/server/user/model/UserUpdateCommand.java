package com.wxc.aidata.server.user.model;

/**
 * 更新用户命令，只允许修改用户基础资料，不直接修改密码。
 */
public record UserUpdateCommand(
        Long id,
        String nickname,
        String mobile,
        String email,
        Integer status
) {
}
