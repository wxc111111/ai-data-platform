package com.wxc.aidata.server.auth.service;

import com.wxc.aidata.server.auth.model.TokenInfo;

/**
 * 登录会话管理接口，封装 Sa-Token 操作，避免业务层直接依赖具体 token 框架。
 */
public interface AuthSessionManager {

    /**
     * 为指定用户创建登录会话，并返回前端需要保存的 token 信息。
     */
    TokenInfo login(Long userId);

    /**
     * 注销当前登录会话。
     */
    void logout();

    /**
     * 从当前请求 token 中解析已登录用户 ID。
     */
    Long currentUserId();
}
