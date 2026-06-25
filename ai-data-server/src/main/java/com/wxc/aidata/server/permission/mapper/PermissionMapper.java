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
     * 查询用户拥有的权限编码。
     */
    List<String> findPermissionCodes(Long userId);
}
