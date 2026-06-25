package com.wxc.aidata.server.user.entity;

import java.time.LocalDateTime;

/**
 * 系统用户实体，对应 sys_user 表中的登录账号数据。
 */
public record SysUser(
        // 用户主键 ID。
        Long id,
        // 登录账号。
        String username,
        // BCrypt 加密后的密码密文。
        String password,
        // 用户昵称。
        String nickname,
        // 用户状态：1 表示启用，0 表示禁用。
        Integer status,
        // 逻辑删除标记：1 表示已删除，0 表示未删除。
        Integer deleted,
        // 最近一次登录时间，后续可用于审计和账号安全提示。
        LocalDateTime lastLoginTime
) {

    /**
     * 判断账号是否启用，只有启用账号允许登录。
     */
    public boolean isEnabled() {
        return Integer.valueOf(1).equals(status);
    }

    /**
     * 判断账号是否已被逻辑删除，已删除账号不能登录。
     */
    public boolean isDeleted() {
        return Integer.valueOf(1).equals(deleted);
    }
}
