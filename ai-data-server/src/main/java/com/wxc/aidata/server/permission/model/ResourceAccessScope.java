package com.wxc.aidata.server.permission.model;

import java.util.List;

/**
 * 资源访问权限快照，用于把当前用户身份安全传递到非 Web 执行上下文。
 */
public record ResourceAccessScope(
        Long userId,
        List<Long> roleIds,
        boolean admin
) {

    /**
     * 固化角色列表并拒绝缺少用户身份的执行请求。
     */
    public ResourceAccessScope {
        if (userId == null) {
            throw new IllegalArgumentException("权限上下文用户 ID 不能为空");
        }
        roleIds = roleIds == null ? List.of() : List.copyOf(roleIds);
    }
}
