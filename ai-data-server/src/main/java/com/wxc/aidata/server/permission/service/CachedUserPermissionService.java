package com.wxc.aidata.server.permission.service;

import com.wxc.aidata.server.permission.mapper.PermissionMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 带缓存的用户权限服务，MySQL 是准数据源，Redis 只保存登录态下的读取加速数据。
 */
@Service
public class CachedUserPermissionService implements UserPermissionService {

    /**
     * 系统内置超级管理员角色编码，拥有该角色的用户默认拥有全部启用权限。
     */
    private static final String ADMIN_ROLE_CODE = "admin";

    private final PermissionMapper permissionMapper;
    private final UserPermissionCacheRepository cacheRepository;

    /**
     * 注入权限查询和缓存仓储，形成“缓存优先、缺失回源”的读取链路。
     */
    public CachedUserPermissionService(
            PermissionMapper permissionMapper,
            UserPermissionCacheRepository cacheRepository) {

        this.permissionMapper = permissionMapper;
        this.cacheRepository = cacheRepository;
    }

    /**
     * 查询用户角色编码，缓存命中时不访问数据库。
     */
    @Override
    public List<String> findRoleCodes(Long userId) {
        return cacheRepository.findRoleCodes(userId)
                .orElseGet(() -> loadAndCache(userId).roleCodes());
    }

    /**
     * 查询用户权限编码，admin 角色直接拥有所有启用权限，保证新增页面和权限默认可见。
     */
    @Override
    public List<String> findPermissionCodes(Long userId) {
        Optional<List<String>> cachedRoleCodes = cacheRepository.findRoleCodes(userId);
        Optional<List<String>> cachedPermissionCodes = cacheRepository.findPermissionCodes(userId);

        if (cachedRoleCodes.isPresent() && cachedRoleCodes.get().contains(ADMIN_ROLE_CODE)) {
            // admin 不能依赖旧权限缓存，否则新增页面或接口权限后当前会话可能看不到。
            List<String> allPermissionCodes = permissionMapper.findAllPermissionCodes();
            cacheRepository.save(userId, cachedRoleCodes.get(), allPermissionCodes);
            return allPermissionCodes;
        }

        if (cachedRoleCodes.isPresent() && cachedPermissionCodes.isPresent()) {
            return cachedPermissionCodes.get();
        }

        // 角色缓存缺失时必须回源数据库，避免无法识别 admin 超级管理员。
        return loadAndCache(userId).permissionCodes();
    }

    /**
     * 主动刷新用户权限缓存，适用于登录成功、角色分配或权限分配变更后。
     */
    @Override
    public void refreshUserPermissionCache(Long userId) {
        loadAndCache(userId);
    }

    /**
     * 从数据库加载角色和权限，并一次性写入缓存，避免角色和权限缓存出现半更新。
     */
    private UserPermissionSnapshot loadAndCache(Long userId) {
        List<String> roleCodes = permissionMapper.findRoleCodes(userId);
        List<String> permissionCodes = roleCodes.contains(ADMIN_ROLE_CODE)
                ? permissionMapper.findAllPermissionCodes()
                : permissionMapper.findPermissionCodes(userId);

        cacheRepository.save(userId, roleCodes, permissionCodes);
        return new UserPermissionSnapshot(roleCodes, permissionCodes);
    }

    /**
     * 用户权限快照，保证同一次读取使用同一批角色和权限数据。
     */
    private record UserPermissionSnapshot(
            List<String> roleCodes,
            List<String> permissionCodes
    ) {
    }
}
