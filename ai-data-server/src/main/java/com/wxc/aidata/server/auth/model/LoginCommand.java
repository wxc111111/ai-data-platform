package com.wxc.aidata.server.auth.model;

/**
 * 登录命令对象，承载 service 层完成认证和登录审计所需的信息。
 */
public record LoginCommand(
        // 登录账号，当前第一版使用后台创建的用户名登录。
        String username,
        // 用户输入的明文密码，仅用于本次校验，不写入日志或返回前端。
        String password,
        // 登录来源 IP，仅用于登录日志审计。
        String loginIp,
        // 登录客户端 User-Agent，仅用于登录日志审计。
        String userAgent
) {
}
