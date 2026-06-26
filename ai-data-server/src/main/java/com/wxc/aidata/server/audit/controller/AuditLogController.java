package com.wxc.aidata.server.audit.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.wxc.aidata.common.response.PageResult;
import com.wxc.aidata.common.response.Result;
import com.wxc.aidata.server.audit.model.LoginLogPageQuery;
import com.wxc.aidata.server.audit.model.OperationLogPageQuery;
import com.wxc.aidata.server.audit.response.LoginLogResponse;
import com.wxc.aidata.server.audit.response.OperationLogResponse;
import com.wxc.aidata.server.audit.service.AuditLogQueryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * 审计日志查询接口，提供登录日志和操作日志分页查询能力。
 */
@RestController
@RequestMapping("/api/audit")
public class AuditLogController {

    private final AuditLogQueryService auditLogQueryService;

    /**
     * 注入审计日志查询服务。
     */
    public AuditLogController(AuditLogQueryService auditLogQueryService) {
        this.auditLogQueryService = auditLogQueryService;
    }

    /**
     * 分页查询登录日志。
     */
    @SaCheckPermission("system:login-log:list")
    @GetMapping("/login-logs")
    public Result<PageResult<LoginLogResponse>> pageLoginLogs(
            @RequestParam(name = "pageNo", required = false) Integer pageNo,
            @RequestParam(name = "pageSize", required = false) Integer pageSize,
            @RequestParam(name = "username", required = false) String username,
            @RequestParam(name = "loginStatus", required = false) String loginStatus,
            @RequestParam(name = "startTime", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(name = "endTime", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        return Result.success(auditLogQueryService.pageLoginLogs(new LoginLogPageQuery(
                pageNo,
                pageSize,
                username,
                loginStatus,
                startTime,
                endTime
        )));
    }

    /**
     * 分页查询操作日志。
     */
    @SaCheckPermission("system:operation-log:list")
    @GetMapping("/operation-logs")
    public Result<PageResult<OperationLogResponse>> pageOperationLogs(
            @RequestParam(name = "pageNo", required = false) Integer pageNo,
            @RequestParam(name = "pageSize", required = false) Integer pageSize,
            @RequestParam(name = "username", required = false) String username,
            @RequestParam(name = "requestPath", required = false) String requestPath,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "startTime", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(name = "endTime", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        return Result.success(auditLogQueryService.pageOperationLogs(new OperationLogPageQuery(
                pageNo,
                pageSize,
                username,
                requestPath,
                status,
                startTime,
                endTime
        )));
    }
}
