package com.wxc.aidata.server.permission.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Redis 用户权限缓存实现，保存登录用户的角色编码和权限编码。
 */
@Repository
public class RedisUserPermissionCacheRepository implements UserPermissionCacheRepository {

    /**
     * 权限缓存有效期与默认 token 超时时间保持一致，避免长期持有过期授权信息。
     */
    private static final Duration CACHE_TTL = Duration.ofHours(2);

    private static final String ROLE_KEY_PREFIX = "ai:auth:role:";
    private static final String PERMISSION_KEY_PREFIX = "ai:auth:permission:";
    private static final String VALUE_SEPARATOR = ",";

    private final StringRedisTemplate redisTemplate;

    /**
     * 注入 StringRedisTemplate，权限编码按字符串列表序列化保存。
     */
    public RedisUserPermissionCacheRepository(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 从 Redis 读取角色编码，空 key 表示缓存未命中。
     */
    @Override
    public Optional<List<String>> findRoleCodes(Long userId) {
        return readCodes(ROLE_KEY_PREFIX + userId);
    }

    /**
     * 从 Redis 读取权限编码，空 key 表示缓存未命中。
     */
    @Override
    public Optional<List<String>> findPermissionCodes(Long userId) {
        return readCodes(PERMISSION_KEY_PREFIX + userId);
    }

    /**
     * 同时写入角色和权限缓存，供登录后初始化和后续鉴权复用。
     */
    @Override
    public void save(Long userId, List<String> roleCodes, List<String> permissionCodes) {
        redisTemplate.opsForValue().set(ROLE_KEY_PREFIX + userId, join(roleCodes), CACHE_TTL);
        redisTemplate.opsForValue().set(PERMISSION_KEY_PREFIX + userId, join(permissionCodes), CACHE_TTL);
    }

    /**
     * 读取并反序列化编码列表，保留空列表和未命中的区别。
     */
    private Optional<List<String>> readCodes(String key) {
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return Optional.empty();
        }
        if (value.isBlank()) {
            return Optional.of(List.of());
        }
        return Optional.of(Arrays.stream(value.split(VALUE_SEPARATOR))
                .filter(item -> !item.isBlank())
                .toList());
    }

    /**
     * 将编码列表序列化为简单字符串，权限编码规范中不允许逗号。
     */
    private String join(List<String> values) {
        return String.join(VALUE_SEPARATOR, values);
    }
}
