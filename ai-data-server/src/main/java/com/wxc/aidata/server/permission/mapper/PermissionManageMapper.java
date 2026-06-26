package com.wxc.aidata.server.permission.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 权限管理数据库访问接口，负责 sys_permission 的树形查询和维护。
 */
@Mapper
public interface PermissionManageMapper {

    /**
     * 查询全部权限节点，由服务层组装树结构。
     */
    List<PermissionRow> findPermissions();

    /**
     * 查询单个权限节点。
     */
    Optional<PermissionRow> findPermissionById(Long id);

    /**
     * 查询权限节点是否存在。
     */
    boolean existsById(Long id);

    /**
     * 查询权限编码是否已存在。
     */
    boolean existsByPermissionCode(String permissionCode);

    /**
     * 查询除当前权限外是否存在相同编码。
     */
    boolean existsByPermissionCodeExcludeId(@Param("permissionCode") String permissionCode, @Param("id") Long id);

    /**
     * 插入权限节点。
     */
    void insertPermission(PermissionInsertRow permission);

    /**
     * 更新权限节点。
     */
    int updatePermission(PermissionUpdateRow permission);

    /**
     * 更新权限状态。
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 统计子权限数量，删除前用于保护树结构。
     */
    int countChildren(Long id);

    /**
     * 统计权限被非 admin 角色引用数量，删除前用于保护普通角色授权关系。
     */
    int countNonAdminRolesByPermissionId(Long id);

    /**
     * 删除权限关联的角色授权关系，允许清理 admin 的冗余授权。
     */
    void deleteRolePermissionsByPermissionId(Long id);

    /**
     * 查询拥有该权限的用户 ID，用于刷新 Redis 权限缓存。
     */
    List<Long> findUserIdsByPermissionId(Long id);

    /**
     * 删除权限节点。
     */
    int deletePermission(Long id);

    /**
     * 权限查询行对象，对应 sys_permission 展示字段。
     */
    record PermissionRow(
            Long id,
            Long parentId,
            String permissionName,
            String permissionCode,
            String permissionType,
            String routePath,
            String componentPath,
            String icon,
            Integer sortNo,
            Integer status,
            LocalDateTime createdTime,
            LocalDateTime updatedTime
    ) {
    }

    /**
     * 权限新增行对象，对应 sys_permission 可写字段。
     */
    record PermissionInsertRow(
            Long id,
            Long parentId,
            String permissionName,
            String permissionCode,
            String permissionType,
            String routePath,
            String componentPath,
            String icon,
            Integer sortNo,
            Integer status,
            LocalDateTime createdTime,
            LocalDateTime updatedTime
    ) {
    }

    /**
     * 权限更新行对象，对应 sys_permission 可编辑字段。
     */
    record PermissionUpdateRow(
            Long id,
            Long parentId,
            String permissionName,
            String permissionCode,
            String permissionType,
            String routePath,
            String componentPath,
            String icon,
            Integer sortNo,
            Integer status,
            LocalDateTime updatedTime
    ) {
    }
}
