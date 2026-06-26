package com.wxc.aidata.server.role.service;

import com.wxc.aidata.server.role.model.RolePermissionAssignCommand;

import java.util.List;

/**
 * 角色授权服务，封装角色权限查询和覆盖保存逻辑。
 */
public interface RolePermissionService {

    /**
     * 查询角色已授权权限 ID。
     */
    List<Long> permissionIds(Long roleId);

    /**
     * 覆盖保存角色权限。
     */
    void assignPermissions(RolePermissionAssignCommand command);
}
