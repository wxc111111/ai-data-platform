package com.wxc.aidata.server.auth.model;

/**
 * 登录命令对象，承载 service 层需要的用户名和密码。
 */
public record LoginCommand(
        // 登录账号，当前第一版使用后台创建的用户名登录。
        String username,
        // 用户输入的明文密码，仅用于本次校验，不写入日志或返回前端。
        String password
) {
}
