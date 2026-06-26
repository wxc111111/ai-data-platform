package com.wxc.aidata.server.role.service.impl;

import com.wxc.aidata.common.exception.BusinessException;
import com.wxc.aidata.server.permission.service.UserPermissionService;
import com.wxc.aidata.server.role.mapper.RolePermissionMapper;
import com.wxc.aidata.server.role.model.RolePermissionAssignCommand;
import com.wxc.aidata.server.role.service.RolePermissionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色授权服务实现，负责角色权限覆盖保存和用户权限缓存刷新。
 */
@Service
public class RolePermissionServiceImpl implements RolePermissionService {

    /**
     * 默认管理员角色 ID，禁止在授权页面手工调整。
     */
    private static final Long ADMIN_ROLE_ID = 1L;

    private static final int ROLE_PERMISSION_ERROR_CODE = 11004;

    private final RolePermissionMapper rolePermissionMapper;
    private final UserPermissionService userPermissionService;

    /**
     * 注入角色授权 Mapper 和用户权限缓存服务。
     */
    public RolePermissionServiceImpl(
            RolePermissionMapper rolePermissionMapper,
            UserPermissionService userPermissionService) {

        this.rolePermissionMapper = rolePermissionMapper;
        this.userPermissionService = userPermissionService;
    }

    /**
     * 查询角色已授权权限 ID。
     */
    @Override
    public List<Long> permissionIds(Long roleId) {
        validateRoleId(roleId);
        return rolePermissionMapper.findPermissionIdsByRoleId(roleId);
    }

    /**
     * 覆盖保存角色权限，保存后刷新该角色下所有用户缓存。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignPermissions(RolePermissionAssignCommand command) {
        if (command == null || command.roleId() == null) {
            throw new BusinessException(ROLE_PERMISSION_ERROR_CODE, "角色ID不能为空");
        }
        protectAdminRole(command.roleId());
        if (!rolePermissionMapper.existsRoleById(command.roleId())) {
            throw new BusinessException(ROLE_PERMISSION_ERROR_CODE, "角色不存在");
        }

        List<Long> permissionIds = distinctIds(command.permissionIds());
        if (!permissionIds.isEmpty()) {
            List<Long> enabledPermissionIds = rolePermissionMapper.findEnabledPermissionIds(permissionIds);
            if (!Set.copyOf(enabledPermissionIds).containsAll(permissionIds)) {
                throw new BusinessException(ROLE_PERMISSION_ERROR_CODE, "权限不存在或已禁用");
            }
        }

        List<Long> userIds = rolePermissionMapper.findUserIdsByRoleId(command.roleId());
        rolePermissionMapper.deleteRolePermissions(command.roleId());
        if (!permissionIds.isEmpty()) {
            rolePermissionMapper.insertRolePermissions(command.roleId(), permissionIds);
        }
        refreshUsers(userIds);
    }

    /**
     * 默认管理员角色默认拥有全部启用权限，不允许手工覆盖。
     */
    private void protectAdminRole(Long roleId) {
        if (ADMIN_ROLE_ID.equals(roleId)) {
            throw new BusinessException(ROLE_PERMISSION_ERROR_CODE, "默认管理员角色不能分配权限");
        }
    }

    /**
     * 刷新角色关联用户的权限缓存。
     */
    private void refreshUsers(List<Long> userIds) {
        for (Long userId : userIds) {
            userPermissionService.refreshUserPermissionCache(userId);
        }
    }

    /**
     * 校验角色 ID 不能为空。
     */
    private void validateRoleId(Long roleId) {
        if (roleId == null) {
            throw new BusinessException(ROLE_PERMISSION_ERROR_CODE, "角色ID不能为空");
        }
    }

    /**
     * 去重并过滤非法权限 ID，保留前端提交顺序。
     */
    private List<Long> distinctIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return ids.stream()
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toCollection(LinkedHashSet<Long>::new))
                .stream()
                .toList();
    }
}
