package com.wxc.aidata.server.audit.service;

import com.wxc.aidata.server.audit.model.LoginAuditCommand;
import com.wxc.aidata.server.audit.model.OperationAuditCommand;

/**
 * 审计日志服务，统一封装登录日志和操作日志写入。
 */
public interface AuditLogService {

    /**
     * 记录登录审计日志。
     */
    void recordLogin(LoginAuditCommand command);

    /**
     * 记录操作审计日志。
     */
    void recordOperation(OperationAuditCommand command);
}
