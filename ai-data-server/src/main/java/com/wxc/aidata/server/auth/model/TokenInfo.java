package com.wxc.aidata.server.auth.model;

/**
 * token 信息对象，隔离具体 token 框架返回值。
 */
public record TokenInfo(
        // token 名称，通常作为请求头名称传给前端。
        String tokenName,
        // token 值，前端访问受保护接口时需要携带。
        String tokenValue
) {
}
