package com.wxc.aidata.server.user.model;

import java.util.List;

/**
 * 用户角色分配命令，采用覆盖保存语义。
 */
public record UserRoleAssignCommand(
        Long userId,
        List<Long> roleIds
) {
}
