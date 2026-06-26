package com.wxc.aidata.server.audit.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wxc.aidata.common.response.PageResult;
import com.wxc.aidata.server.audit.mapper.AuditLogQueryMapper;
import com.wxc.aidata.server.audit.model.LoginLogPageQuery;
import com.wxc.aidata.server.audit.model.OperationLogPageQuery;
import com.wxc.aidata.server.audit.response.LoginLogResponse;
import com.wxc.aidata.server.audit.response.OperationLogResponse;
import com.wxc.aidata.server.audit.service.AuditLogQueryService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 审计日志查询服务实现，使用 PageHelper 保持与现有管理列表一致的分页方式。
 */
@Service
public class AuditLogQueryServiceImpl implements AuditLogQueryService {

    private final AuditLogQueryMapper auditLogQueryMapper;

    /**
     * 注入审计日志查询 Mapper。
     */
    public AuditLogQueryServiceImpl(AuditLogQueryMapper auditLogQueryMapper) {
        this.auditLogQueryMapper = auditLogQueryMapper;
    }

    /**
     * 分页查询登录日志，并转换为前端响应对象。
     */
    @Override
    public PageResult<LoginLogResponse> pageLoginLogs(LoginLogPageQuery query) {
        LoginLogPageQuery safeQuery = query == null ? new LoginLogPageQuery(1, 10, null, null, null, null) : query;
        PageHelper.startPage(safeQuery.normalizedPageNo(), safeQuery.normalizedPageSize());
        List<AuditLogQueryMapper.LoginLogRow> rows = auditLogQueryMapper.findLoginLogs(safeQuery);
        PageInfo<AuditLogQueryMapper.LoginLogRow> pageInfo = new PageInfo<>(rows);
        List<LoginLogResponse> records = rows.stream().map(this::toLoginLogResponse).toList();
        return PageResult.of(pageInfo.getTotal(), safeQuery.normalizedPageNo(), safeQuery.normalizedPageSize(), records);
    }

    /**
     * 分页查询操作日志，并转换为前端响应对象。
     */
    @Override
    public PageResult<OperationLogResponse> pageOperationLogs(OperationLogPageQuery query) {
        OperationLogPageQuery safeQuery = query == null ? new OperationLogPageQuery(1, 10, null, null, null, null, null) : query;
        PageHelper.startPage(safeQuery.normalizedPageNo(), safeQuery.normalizedPageSize());
        List<AuditLogQueryMapper.OperationLogRow> rows = auditLogQueryMapper.findOperationLogs(safeQuery);
        PageInfo<AuditLogQueryMapper.OperationLogRow> pageInfo = new PageInfo<>(rows);
        List<OperationLogResponse> records = rows.stream().map(this::toOperationLogResponse).toList();
        return PageResult.of(pageInfo.getTotal(), safeQuery.normalizedPageNo(), safeQuery.normalizedPageSize(), records);
    }

    /**
     * 转换登录日志数据库行到响应对象。
     */
    private LoginLogResponse toLoginLogResponse(AuditLogQueryMapper.LoginLogRow row) {
        return new LoginLogResponse(
                row.id(),
                row.userId(),
                row.username(),
                row.loginIp(),
                row.userAgent(),
                row.loginStatus(),
                row.message(),
                row.loginTime()
        );
    }

    /**
     * 转换操作日志数据库行到响应对象。
     */
    private OperationLogResponse toOperationLogResponse(AuditLogQueryMapper.OperationLogRow row) {
        return new OperationLogResponse(
                row.id(),
                row.requestId(),
                row.userId(),
                row.username(),
                row.moduleName(),
                row.operationName(),
                row.requestMethod(),
                row.requestPath(),
                row.requestIp(),
                row.requestParams(),
                row.status(),
                row.durationMs(),
                row.errorMessage(),
                row.createdTime()
        );
    }
}
