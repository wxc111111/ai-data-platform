package com.wxc.aidata.server.audit.response;

import java.time.LocalDateTime;

/**
 * 登录日志响应对象，返回前端登录审计列表需要展示的字段。
 */
public record LoginLogResponse(
        Long id,
        Long userId,
        String username,
        String loginIp,
        String userAgent,
        String loginStatus,
        String message,
        LocalDateTime loginTime
) {
}
