package com.wxc.aidata.server.audit.service.impl;

import com.wxc.aidata.server.audit.mapper.AuditLogMapper;
import com.wxc.aidata.server.audit.model.LoginAuditCommand;
import com.wxc.aidata.server.audit.model.OperationAuditCommand;
import com.wxc.aidata.server.audit.service.AuditLogService;
import com.wxc.aidata.server.common.id.IdGenerator;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 审计日志服务实现，负责为审计记录生成主键和写入时间后调用 Mapper 入库。
 */
@Service
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogMapper auditLogMapper;
    private final IdGenerator idGenerator;

    /**
     * 注入审计 Mapper 和 ID 生成器。
     */
    public AuditLogServiceImpl(AuditLogMapper auditLogMapper, IdGenerator idGenerator) {
        this.auditLogMapper = auditLogMapper;
        this.idGenerator = idGenerator;
    }

    /**
     * 记录登录日志，不接收也不保存明文密码。
     */
    @Override
    public void recordLogin(LoginAuditCommand command) {
        auditLogMapper.insertLoginLog(new AuditLogMapper.LoginLogInsertRow(
                idGenerator.nextId(),
                command.userId(),
                command.username(),
                command.loginIp(),
                command.userAgent(),
                command.loginStatus(),
                command.message(),
                LocalDateTime.now()
        ));
    }

    /**
     * 记录操作日志，请求参数由调用方完成脱敏后传入。
     */
    @Override
    public void recordOperation(OperationAuditCommand command) {
        auditLogMapper.insertOperationLog(new AuditLogMapper.OperationLogInsertRow(
                idGenerator.nextId(),
                command.requestId(),
                command.userId(),
                command.username(),
                command.moduleName(),
                command.operationName(),
                command.requestMethod(),
                command.requestPath(),
                command.requestIp(),
                command.requestParams(),
                command.status(),
                command.durationMs(),
                command.errorMessage(),
                LocalDateTime.now()
        ));
    }
}
