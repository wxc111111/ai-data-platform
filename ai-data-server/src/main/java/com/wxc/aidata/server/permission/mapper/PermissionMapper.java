package com.wxc.aidata.server.permission.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 权限数据库访问接口，负责查询用户拥有的角色和权限编码。
 */
@Mapper
public interface PermissionMapper {

    /**
     * 查询用户拥有的角色编码。
     */
    List<String> findRoleCodes(Long userId);

    /**
     * 查询用户拥有的启用角色 ID，用于资源角色范围过滤。
     */
    List<Long> findRoleIds(Long userId);

    /**
     * 查询用户拥有的权限编码。
     */
    List<String> findPermissionCodes(Long userId);

    /**
     * 查询全部启用权限编码，供 admin 超级管理员权限展开使用。
     */
    List<String> findAllPermissionCodes();
}
