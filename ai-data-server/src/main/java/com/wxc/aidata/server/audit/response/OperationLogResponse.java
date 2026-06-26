package com.wxc.aidata.server.audit.response;

import java.time.LocalDateTime;

/**
 * 操作日志响应对象，返回前端操作审计列表需要展示的字段。
 */
public record OperationLogResponse(
        Long id,
        String requestId,
        Long userId,
        String username,
        String moduleName,
        String operationName,
        String requestMethod,
        String requestPath,
        String requestIp,
        String requestParams,
        String status,
        Long durationMs,
        String errorMessage,
        LocalDateTime createdTime
) {
}
