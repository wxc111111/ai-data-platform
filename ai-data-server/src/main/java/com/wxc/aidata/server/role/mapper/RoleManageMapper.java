package com.wxc.aidata.server.role.mapper;

import com.wxc.aidata.server.role.model.RolePageQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 角色管理数据库访问接口，负责角色 CRUD 和角色关联关系查询。
 */
@Mapper
public interface RoleManageMapper {

    /**
     * 分页查询角色列表。
     */
    List<RoleRow> findRoles(@Param("query") RolePageQuery query);

    /**
     * 查询角色详情。
     */
    Optional<RoleRow> findRoleById(Long id);

    /**
     * 查询角色编码是否已存在，用于新增唯一性校验。
     */
    boolean existsByRoleCode(String roleCode);

    /**
     * 查询除当前角色外是否存在相同编码，用于编辑唯一性校验。
     */
    boolean existsByRoleCodeExcludeId(@Param("roleCode") String roleCode, @Param("id") Long id);

    /**
     * 查询角色是否存在。
     */
    boolean existsById(Long id);

    /**
     * 插入角色基础信息。
     */
    void insertRole(RoleInsertRow role);

    /**
     * 更新角色基础信息。
     */
    int updateRole(RoleUpdateRow role);

    /**
     * 更新角色状态。
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 统计角色已分配的用户数量，删除前用于保护数据关系。
     */
    int countUsersByRoleId(Long roleId);

    /**
     * 查询角色关联的用户 ID，用于角色状态变化后刷新权限缓存。
     */
    List<Long> findUserIdsByRoleId(Long roleId);

    /**
     * 删除角色权限关系，删除角色前清理无效授权。
     */
    void deleteRolePermissions(Long roleId);

    /**
     * 删除角色定义。
     */
    int deleteRole(Long id);

    /**
     * 角色查询行对象，对应 sys_role 展示字段。
     */
    record RoleRow(
            Long id,
            String roleCode,
            String roleName,
            Integer status,
            String description,
            LocalDateTime createdTime,
            LocalDateTime updatedTime
    ) {
    }

    /**
     * 角色新增行对象，对应 sys_role 可写字段。
     */
    record RoleInsertRow(
            Long id,
            String roleCode,
            String roleName,
            Integer status,
            String description,
            LocalDateTime createdTime,
            LocalDateTime updatedTime
    ) {
    }

    /**
     * 角色更新行对象，对应 sys_role 可编辑字段。
     */
    record RoleUpdateRow(
            Long id,
            String roleCode,
            String roleName,
            Integer status,
            String description,
            LocalDateTime updatedTime
    ) {
    }
}
