package com.wxc.aidata.server.audit.model;

/**
 * 操作审计命令，承载一次后台接口访问需要写入数据库的审计字段。
 */
public record OperationAuditCommand(
        // 请求链路 ID，用于关联接口响应、应用日志和操作日志。
        String requestId,
        // 当前操作用户 ID，未登录或登录态异常时允许为空。
        Long userId,
        // 当前操作用户名，未登录或用户已删除时允许为空。
        String username,
        // 业务模块名称，用于后台审计查询聚合。
        String moduleName,
        // 操作名称，当前使用 HTTP 方法和路径描述。
        String operationName,
        // HTTP 请求方法。
        String requestMethod,
        // HTTP 请求路径，不包含 query string。
        String requestPath,
        // 请求来源 IP。
        String requestIp,
        // 脱敏后的请求参数，不记录请求体。
        String requestParams,
        // 操作结果：SUCCESS 或 FAILED。
        String status,
        // 接口耗时，单位毫秒。
        Long durationMs,
        // 异常信息，成功时为空。
        String errorMessage
) {
}
