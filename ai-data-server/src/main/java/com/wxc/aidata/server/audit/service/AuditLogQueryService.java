package com.wxc.aidata.server.audit.service;

import com.wxc.aidata.common.response.PageResult;
import com.wxc.aidata.server.audit.model.LoginLogPageQuery;
import com.wxc.aidata.server.audit.model.OperationLogPageQuery;
import com.wxc.aidata.server.audit.response.LoginLogResponse;
import com.wxc.aidata.server.audit.response.OperationLogResponse;

/**
 * 审计日志查询服务，提供登录日志和操作日志分页查询能力。
 */
public interface AuditLogQueryService {

    /**
     * 分页查询登录日志。
     */
    PageResult<LoginLogResponse> pageLoginLogs(LoginLogPageQuery query);

    /**
     * 分页查询操作日志。
     */
    PageResult<OperationLogResponse> pageOperationLogs(OperationLogPageQuery query);
}
