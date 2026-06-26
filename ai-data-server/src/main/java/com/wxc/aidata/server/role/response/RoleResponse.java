package com.wxc.aidata.server.role.response;

import java.time.LocalDateTime;

/**
 * 角色管理响应对象，返回列表和详情页需要展示的字段。
 */
public record RoleResponse(
        Long id,
        String roleCode,
        String roleName,
        Integer status,
        String description,
        LocalDateTime createdTime,
        LocalDateTime updatedTime
) {
}
