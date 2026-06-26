package com.wxc.aidata.server.audit.web;

import com.wxc.aidata.common.context.RequestContext;
import com.wxc.aidata.server.audit.model.OperationAuditCommand;
import com.wxc.aidata.server.audit.service.AuditLogService;
import com.wxc.aidata.server.auth.service.AuthSessionManager;
import com.wxc.aidata.server.user.mapper.SysUserMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

/**
 * 操作日志拦截器，在接口请求结束后生成后台操作审计记录。
 */
@Component
public class OperationLogInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(OperationLogInterceptor.class);
    private static final String START_TIME_ATTRIBUTE = OperationLogInterceptor.class.getName() + ".startTime";
    private static final String LOGIN_PATH = "/api/auth/login";
    private static final String SUCCESS = "SUCCESS";
    private static final String FAILED = "FAILED";
    private static final Map<String, String> MODULE_NAMES = Map.of(
            "/api/users", "用户管理",
            "/api/roles", "角色管理",
            "/api/permissions", "权限管理",
            "/api/auth", "认证管理"
    );

    private final AuditLogService auditLogService;
    private final AuthSessionManager authSessionManager;
    private final SysUserMapper sysUserMapper;

    /**
     * 注入审计服务、会话管理器和用户查询组件。
     */
    public OperationLogInterceptor(AuditLogService auditLogService,
                                   AuthSessionManager authSessionManager,
                                   SysUserMapper sysUserMapper) {
        this.auditLogService = auditLogService;
        this.authSessionManager = authSessionManager;
        this.sysUserMapper = sysUserMapper;
    }

    /**
     * 请求进入业务处理前记录开始时间，用于计算接口耗时。
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME_ATTRIBUTE, System.currentTimeMillis());
        return true;
    }

    /**
     * 请求结束后写入操作日志；登录接口由登录日志单独负责，避免重复记录。
     */
    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception exception) {
        if (shouldSkip(request)) {
            return;
        }

        try {
            auditLogService.recordOperation(buildCommand(request, response, exception));
        } catch (RuntimeException auditException) {
            // 审计失败不能影响业务响应，只输出应用日志供排查。
            log.warn("记录操作日志失败，path={}", request.getRequestURI(), auditException);
        }
    }

    /**
     * 组装操作审计命令，异常请求和 4xx/5xx 响应都标记为失败。
     */
    private OperationAuditCommand buildCommand(HttpServletRequest request,
                                               HttpServletResponse response,
                                               Exception exception) {
        Long userId = resolveCurrentUserId();
        String username = resolveUsername(userId);
        long durationMs = System.currentTimeMillis() - startTime(request);
        boolean success = exception == null && response.getStatus() < 400;

        return new OperationAuditCommand(
                RequestContext.getRequestId(),
                userId,
                username,
                resolveModuleName(request.getRequestURI()),
                request.getMethod() + " " + request.getRequestURI(),
                request.getMethod(),
                request.getRequestURI(),
                ClientIpResolver.resolve(request),
                RequestParamSanitizer.sanitize(request),
                success ? SUCCESS : FAILED,
                durationMs,
                exception == null ? null : exception.getMessage()
        );
    }

    /**
     * 尝试获取当前登录用户 ID，未登录或登录态异常时返回空。
     */
    private Long resolveCurrentUserId() {
        try {
            return authSessionManager.currentUserId();
        } catch (RuntimeException exception) {
            return null;
        }
    }

    /**
     * 根据用户 ID 查询用户名，用户不存在时返回空。
     */
    private String resolveUsername(Long userId) {
        if (userId == null) {
            return null;
        }
        return sysUserMapper.findById(userId)
                .map(user -> user.username())
                .orElse(null);
    }

    /**
     * 根据接口路径推断业务模块名称。
     */
    private String resolveModuleName(String requestPath) {
        return MODULE_NAMES.entrySet()
                .stream()
                .filter(entry -> requestPath.startsWith(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse("系统接口");
    }

    /**
     * 读取请求开始时间，缺失时以当前时间兜底。
     */
    private long startTime(HttpServletRequest request) {
        Object value = request.getAttribute(START_TIME_ATTRIBUTE);
        return value instanceof Long startTime ? startTime : System.currentTimeMillis();
    }

    /**
     * 跳过非 API 请求和登录接口。
     */
    private boolean shouldSkip(HttpServletRequest request) {
        String requestPath = request.getRequestURI();
        return !requestPath.startsWith("/api/") || LOGIN_PATH.equals(requestPath);
    }
}
