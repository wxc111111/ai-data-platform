package com.wxc.aidata.server.permission.service;

import java.util.List;

/**
 * 用户权限服务，统一封装角色和权限编码读取逻辑。
 */
public interface UserPermissionService {

    /**
     * 查询用户拥有的角色编码。
     */
    List<String> findRoleCodes(Long userId);

    /**
     * 查询用户拥有的权限编码。
     */
    List<String> findPermissionCodes(Long userId);

    /**
     * 刷新用户权限缓存，登录成功或权限变更后调用。
     */
    void refreshUserPermissionCache(Long userId);
}
