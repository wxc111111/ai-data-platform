package com.wxc.aidata.server.auth.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.wxc.aidata.server.auth.model.TokenInfo;
import com.wxc.aidata.server.auth.service.AuthSessionManager;
import org.springframework.stereotype.Service;

/**
 * Sa-Token 会话管理实现，统一封装登录、退出和当前用户解析。
 */
@Service
public class SaTokenAuthSessionManager implements AuthSessionManager {

    /**
     * 创建 Sa-Token 登录态，并把 token 名称和值返回给前端。
     */
    @Override
    public TokenInfo login(Long userId) {
        StpUtil.login(userId);
        return new TokenInfo(StpUtil.getTokenName(), StpUtil.getTokenValue());
    }

    /**
     * 注销当前请求携带的 token 对应会话。
     */
    @Override
    public void logout() {
        StpUtil.logout();
    }

    /**
     * 获取当前 token 绑定的用户 ID，未登录时由 Sa-Token 抛出未登录异常。
     */
    @Override
    public Long currentUserId() {
        return StpUtil.getLoginIdAsLong();
    }
}
