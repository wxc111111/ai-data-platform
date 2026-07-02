package com.wxc.aidata.server.permission.service;

import com.wxc.aidata.server.auth.service.AuthSessionManager;
import com.wxc.aidata.server.permission.mapper.PermissionMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * 当前用户资源访问上下文，统一提供用户 ID、角色 ID 和 admin 判断。
 */
@Service
public class CurrentUserAccessService {

    private static final String ADMIN_ROLE_CODE = "admin";

    private final AuthSessionManager authSessionManager;
    private final PermissionMapper permissionMapper;

    /**
     * 注入登录会话和权限查询组件。
     */
    public CurrentUserAccessService(AuthSessionManager authSessionManager, PermissionMapper permissionMapper) {
        this.authSessionManager = authSessionManager;
        this.permissionMapper = permissionMapper;
    }

    /**
     * 返回当前登录用户 ID，用于创建人和更新人审计字段。
     */
    public Long currentUserId() {
        return authSessionManager.currentUserId();
    }

    /**
     * 返回当前用户启用角色 ID，普通用户资源范围按这些角色裁剪。
     */
    public List<Long> currentRoleIds() {
        return permissionMapper.findRoleIds(currentUserId());
    }

    /**
     * 判断当前用户是否拥有 admin 角色，admin 不受资源角色范围限制。
     */
    public boolean currentUserIsAdmin() {
        return permissionMapper.findRoleCodes(currentUserId()).stream()
                .anyMatch(roleCode -> ADMIN_ROLE_CODE.equalsIgnoreCase(roleCode));
    }

    /**
     * 判断当前用户是否可访问某条资源；空角色资源仅 admin 可访问。
     */
    public boolean canAccessResource(List<Long> resourceRoleIds) {
        if (currentUserIsAdmin()) {
            return true;
        }
        Set<Long> currentRoleIdSet = Set.copyOf(currentRoleIds());
        return resourceRoleIds != null && resourceRoleIds.stream().anyMatch(currentRoleIdSet::contains);
    }
}
