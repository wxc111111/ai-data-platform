package com.wxc.aidata.server.auth.model;

import java.util.List;

/**
 * 登录成功后的会话响应，包含 token、用户信息、角色和权限。
 */
public record LoginSession(
        // token 请求头名称，前端后续请求需要使用该名称携带 token。
        String tokenName,
        // token 值，前端保存后用于访问需要登录的接口。
        String tokenValue,
        // 当前登录用户基础信息，不包含密码等敏感字段。
        LoginUser user,
        // 当前用户拥有的角色编码列表。
        List<String> roles,
        // 当前用户拥有的权限编码列表。
        List<String> permissions
) {
}
