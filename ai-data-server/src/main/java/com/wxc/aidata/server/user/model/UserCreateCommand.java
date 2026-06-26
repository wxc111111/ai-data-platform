package com.wxc.aidata.server.user.model;

import java.util.List;

/**
 * 创建用户命令，承载服务层创建账号所需字段。
 */
public record UserCreateCommand(
        String username,
        String password,
        String nickname,
        String mobile,
        String email,
        Integer status,
        List<Long> roleIds
) {
}
