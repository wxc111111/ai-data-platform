package com.wxc.aidata.server.permission.service;

import java.util.List;
import java.util.Optional;

/**
 * 用户权限缓存仓储，隔离 Redis 读写细节。
 */
public interface UserPermissionCacheRepository {

    /**
     * 从缓存读取用户角色编码。
     */
    Optional<List<String>> findRoleCodes(Long userId);

    /**
     * 从缓存读取用户权限编码。
     */
    Optional<List<String>> findPermissionCodes(Long userId);

    /**
     * 保存用户角色和权限编码，避免鉴权频繁查询数据库。
     */
    void save(Long userId, List<String> roleCodes, List<String> permissionCodes);
}
