package com.wxc.aidata.server.user.service;

import com.wxc.aidata.common.response.PageResult;
import com.wxc.aidata.server.user.model.UserCreateCommand;
import com.wxc.aidata.server.user.model.UserPageQuery;
import com.wxc.aidata.server.user.model.UserRoleAssignCommand;
import com.wxc.aidata.server.user.model.UserUpdateCommand;
import com.wxc.aidata.server.user.response.UserResponse;

/**
 * 用户管理服务，提供后台用户维护能力。
 */
public interface UserManageService {

    /**
     * 分页查询用户。
     */
    PageResult<UserResponse> pageUsers(UserPageQuery query);

    /**
     * 查询用户详情。
     */
    UserResponse getUser(Long id);

    /**
     * 创建用户。
     */
    void createUser(UserCreateCommand command);

    /**
     * 更新用户基础信息。
     */
    void updateUser(UserUpdateCommand command);

    /**
     * 更新用户状态。
     */
    void updateStatus(Long id, Integer status);

    /**
     * 逻辑删除用户。
     */
    void deleteUser(Long id);

    /**
     * 覆盖分配用户角色。
     */
    void assignRoles(UserRoleAssignCommand command);
}
