package com.wxc.aidata.server.audit.model;

/**
 * 登录审计命令，承载一次登录成功或失败需要写入数据库的安全审计字段。
 */
public record LoginAuditCommand(
        // 登录成功时记录用户 ID，失败且无法识别账号时允许为空。
        Long userId,
        // 登录账号，仅记录账号本身，禁止记录密码。
        String username,
        // 登录来源 IP，用于安全排查。
        String loginIp,
        // 登录客户端 User-Agent，用于识别来源设备或浏览器。
        String userAgent,
        // 登录结果：SUCCESS 或 FAILED。
        String loginStatus,
        // 登录结果说明，保存业务错误原因或成功说明。
        String message
) {
}
