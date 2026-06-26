package com.wxc.aidata.server.permission.service;

import com.wxc.aidata.server.permission.model.PermissionCreateCommand;
import com.wxc.aidata.server.permission.model.PermissionUpdateCommand;
import com.wxc.aidata.server.permission.response.PermissionTreeResponse;

import java.util.List;

/**
 * 权限管理服务，封装菜单、按钮、接口权限的维护规则。
 */
public interface PermissionManageService {

    /**
     * 查询权限树。
     */
    List<PermissionTreeResponse> treePermissions();

    /**
     * 查询权限详情。
     */
    PermissionTreeResponse getPermission(Long id);

    /**
     * 创建权限。
     */
    void createPermission(PermissionCreateCommand command);

    /**
     * 更新权限。
     */
    void updatePermission(PermissionUpdateCommand command);

    /**
     * 启用或禁用权限。
     */
    void updateStatus(Long id, Integer status);

    /**
     * 删除权限。
     */
    void deletePermission(Long id);
}
