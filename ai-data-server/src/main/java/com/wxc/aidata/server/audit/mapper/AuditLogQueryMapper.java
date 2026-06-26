package com.wxc.aidata.server.audit.mapper;

import com.wxc.aidata.server.audit.model.LoginLogPageQuery;
import com.wxc.aidata.server.audit.model.OperationLogPageQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审计日志查询 Mapper，负责登录日志和操作日志分页检索。
 */
@Mapper
public interface AuditLogQueryMapper {

    /**
     * 按条件查询登录日志列表。
     */
    List<LoginLogRow> findLoginLogs(@Param("query") LoginLogPageQuery query);

    /**
     * 按条件查询操作日志列表。
     */
    List<OperationLogRow> findOperationLogs(@Param("query") OperationLogPageQuery query);

    /**
     * 登录日志查询行对象，对应 sys_login_log 展示字段。
     */
    record LoginLogRow(
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
     * 操作日志查询行对象，对应 sys_operation_log 展示字段。
     */
    record OperationLogRow(
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
