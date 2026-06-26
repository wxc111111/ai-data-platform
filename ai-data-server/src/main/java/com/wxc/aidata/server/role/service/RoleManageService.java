package com.wxc.aidata.server.role.service;

import com.wxc.aidata.common.response.PageResult;
import com.wxc.aidata.server.role.model.RoleCreateCommand;
import com.wxc.aidata.server.role.model.RolePageQuery;
import com.wxc.aidata.server.role.model.RoleUpdateCommand;
import com.wxc.aidata.server.role.response.RoleResponse;

/**
 * 角色管理服务，封装角色维护和默认管理员角色保护规则。
 */
public interface RoleManageService {

    /**
     * 分页查询角色列表。
     */
    PageResult<RoleResponse> pageRoles(RolePageQuery query);

    /**
     * 查询角色详情。
     */
    RoleResponse getRole(Long id);

    /**
     * 创建角色。
     */
    void createRole(RoleCreateCommand command);

    /**
     * 更新角色基础信息。
     */
    void updateRole(RoleUpdateCommand command);

    /**
     * 启用或禁用角色。
     */
    void updateStatus(Long id, Integer status);

    /**
     * 删除角色。
     */
    void deleteRole(Long id);
}
