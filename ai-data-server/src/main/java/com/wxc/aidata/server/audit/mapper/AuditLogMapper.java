package com.wxc.aidata.server.audit.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;

/**
 * 审计日志数据访问接口，负责登录日志和操作日志入库。
 */
@Mapper
public interface AuditLogMapper {

    /**
     * 插入登录日志。
     */
    void insertLoginLog(LoginLogInsertRow row);

    /**
     * 插入操作日志。
     */
    void insertOperationLog(OperationLogInsertRow row);

    /**
     * 登录日志入库行对象，对应 sys_login_log 可写字段。
     */
    record LoginLogInsertRow(
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

    /**
     * 操作日志入库行对象，对应 sys_operation_log 可写字段。
     */
    record OperationLogInsertRow(
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
}
