package com.wxc.aidata.server.auth.service.impl;

import cn.dev33.satoken.stp.StpInterface;
import com.wxc.aidata.server.permission.service.UserPermissionService;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Sa-Token 权限接口实现，负责把系统角色和权限编码交给 Sa-Token 鉴权使用。
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    private final UserPermissionService userPermissionService;

    /**
     * 注入用户权限服务，角色和权限优先从 Redis 缓存读取。
     */
    public StpInterfaceImpl(UserPermissionService userPermissionService) {
        this.userPermissionService = userPermissionService;
    }

    /**
     * 获取当前登录用户的权限编码列表，供 Sa-Token 的权限校验注解使用。
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return userPermissionService.findPermissionCodes(toUserId(loginId));
    }

    /**
     * 获取当前登录用户的角色编码列表，供 Sa-Token 的角色校验注解使用。
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return userPermissionService.findRoleCodes(toUserId(loginId));
    }

    /**
     * 将 Sa-Token 传入的登录标识转换为系统用户 ID。
     */
    private Long toUserId(Object loginId) {
        return Long.valueOf(loginId.toString());
    }
}
