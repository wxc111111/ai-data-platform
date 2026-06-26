package com.wxc.aidata.server.user.request;

import java.util.List;

/**
 * 用户角色分配请求，roleIds 使用覆盖保存。
 */
public record UserRoleAssignRequest(
        List<Long> roleIds
) {
}
