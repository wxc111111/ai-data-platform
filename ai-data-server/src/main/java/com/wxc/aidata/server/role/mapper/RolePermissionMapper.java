package com.wxc.aidata.server.role.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色授权数据库访问接口，负责 sys_role_permission 关系维护。
 */
@Mapper
public interface RolePermissionMapper {

    /**
     * 查询角色是否存在。
     */
    boolean existsRoleById(Long roleId);

    /**
     * 查询角色已授权权限 ID。
     */
    List<Long> findPermissionIdsByRoleId(Long roleId);

    /**
     * 查询启用权限 ID，用于校验前端提交的权限是否可授权。
     */
    List<Long> findEnabledPermissionIds(@Param("permissionIds") List<Long> permissionIds);

    /**
     * 查询角色关联用户，用于授权变更后刷新权限缓存。
     */
    List<Long> findUserIdsByRoleId(Long roleId);

    /**
     * 删除角色已有权限关系，授权保存采用覆盖模式。
     */
    void deleteRolePermissions(Long roleId);

    /**
     * 批量插入角色权限关系。
     */
    void insertRolePermissions(@Param("roleId") Long roleId, @Param("permissionIds") List<Long> permissionIds);
}
