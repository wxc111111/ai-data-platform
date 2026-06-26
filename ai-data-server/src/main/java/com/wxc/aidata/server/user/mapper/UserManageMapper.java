package com.wxc.aidata.server.user.mapper;

import com.wxc.aidata.server.user.model.UserPageQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户管理数据库访问接口，负责后台用户 CRUD 和用户角色关系维护。
 */
@Mapper
public interface UserManageMapper {

    /**
     * 查询用户名是否已经存在，用于创建账号前校验唯一性。
     */
    boolean existsByUsername(String username);

    /**
     * 查询未删除用户是否存在。
     */
    boolean existsById(Long id);

    /**
     * 分页查询用户列表。
     */
    List<UserRow> findUsers(@Param("query") UserPageQuery query);

    /**
     * 查询用户详情。
     */
    Optional<UserRow> findUserById(Long id);

    /**
     * 插入用户基础信息。
     */
    void insertUser(UserInsertRow user);

    /**
     * 更新用户基础信息。
     */
    int updateUser(UserUpdateRow user);

    /**
     * 更新用户启用状态。
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 逻辑删除用户。
     */
    int logicalDelete(Long id);

    /**
     * 删除用户已有角色关系，分配角色时采用覆盖保存。
     */
    void deleteUserRoles(Long userId);

    /**
     * 批量插入用户角色关系。
     */
    void insertUserRoles(@Param("userId") Long userId, @Param("roleIds") List<Long> roleIds);

    /**
     * 查询用户已分配角色 ID。
     */
    List<Long> findRoleIdsByUserId(Long userId);

    /**
     * 查询有效角色 ID，用于校验请求中的角色是否存在。
     */
    List<Long> findEnabledRoleIds(@Param("roleIds") List<Long> roleIds);

    /**
     * 用户查询行对象，不包含密码字段。
     */
    record UserRow(
            Long id,
            String username,
            String nickname,
            String mobile,
            String email,
            Integer status,
            LocalDateTime lastLoginTime,
            LocalDateTime createdTime,
            LocalDateTime updatedTime
    ) {
    }

    /**
     * 用户新增行对象，对应 sys_user 可写字段。
     */
    record UserInsertRow(
            Long id,
            String username,
            String password,
            String nickname,
            String mobile,
            String email,
            Integer status,
            Long createdBy,
            LocalDateTime createdTime,
            Long updatedBy,
            LocalDateTime updatedTime
    ) {
    }

    /**
     * 用户更新行对象，对应 sys_user 的可编辑字段。
     */
    record UserUpdateRow(
            Long id,
            String nickname,
            String mobile,
            String email,
            Integer status,
            Long updatedBy,
            LocalDateTime updatedTime
    ) {
    }
}
