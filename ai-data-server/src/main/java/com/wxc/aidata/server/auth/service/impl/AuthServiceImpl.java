package com.wxc.aidata.server.auth.service.impl;

import com.wxc.aidata.common.exception.BusinessException;
import com.wxc.aidata.server.audit.model.LoginAuditCommand;
import com.wxc.aidata.server.audit.service.AuditLogService;
import com.wxc.aidata.server.auth.model.LoginCommand;
import com.wxc.aidata.server.auth.model.LoginSession;
import com.wxc.aidata.server.auth.model.LoginUser;
import com.wxc.aidata.server.auth.model.TokenInfo;
import com.wxc.aidata.server.auth.service.AuthService;
import com.wxc.aidata.server.auth.service.AuthSessionManager;
import com.wxc.aidata.server.auth.service.PasswordService;
import com.wxc.aidata.server.permission.service.UserPermissionService;
import com.wxc.aidata.server.user.entity.SysUser;
import com.wxc.aidata.server.user.mapper.SysUserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 登录认证业务实现，负责账号校验、密码校验、token 创建、权限信息组装和登录日志审计。
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    /**
     * 登录失败统一错误码，避免把“用户不存在”和“密码错误”暴露给外部。
     */
    private static final int AUTH_ERROR_CODE = 10001;
    private static final String LOGIN_SUCCESS = "SUCCESS";
    private static final String LOGIN_FAILED = "FAILED";

    private final SysUserMapper sysUserMapper;
    private final UserPermissionService userPermissionService;
    private final PasswordService passwordService;
    private final AuthSessionManager authSessionManager;
    private final AuditLogService auditLogService;

    /**
     * 注入用户、权限、密码、会话和审计组件，完成完整认证链路。
     */
    public AuthServiceImpl(
            SysUserMapper sysUserMapper,
            UserPermissionService userPermissionService,
            PasswordService passwordService,
            AuthSessionManager authSessionManager,
            AuditLogService auditLogService) {

        this.sysUserMapper = sysUserMapper;
        this.userPermissionService = userPermissionService;
        this.passwordService = passwordService;
        this.authSessionManager = authSessionManager;
        this.auditLogService = auditLogService;
    }

    /**
     * 执行登录：校验账号和密码，成功时创建会话，成功或失败都写入登录日志。
     */
    @Override
    public LoginSession login(LoginCommand command) {
        Long auditUserId = null;
        String auditUsername = command == null ? null : command.username();

        try {
            if (command == null || isBlank(command.username()) || isBlank(command.password())) {
                throw invalidCredentials();
            }

            SysUser user = sysUserMapper.findByUsername(command.username())
                    .filter(item -> !item.isDeleted())
                    .orElseThrow(AuthServiceImpl::invalidCredentials);
            auditUserId = user.id();

            if (!user.isEnabled()) {
                throw new BusinessException(AUTH_ERROR_CODE, "用户已被禁用");
            }

            if (!passwordService.matches(command.password(), user.password())) {
                throw invalidCredentials();
            }

            TokenInfo token = authSessionManager.login(user.id());
            userPermissionService.refreshUserPermissionCache(user.id());
            LoginSession session = buildSession(user, token);
            recordLogin(command, user.id(), user.username(), LOGIN_SUCCESS, "登录成功");
            return session;
        } catch (BusinessException exception) {
            recordLogin(command, auditUserId, auditUsername, LOGIN_FAILED, exception.getMessage());
            throw exception;
        } catch (RuntimeException exception) {
            recordLogin(command, auditUserId, auditUsername, LOGIN_FAILED, "系统异常");
            throw exception;
        }
    }

    /**
     * 退出当前登录会话。
     */
    @Override
    public void logout() {
        authSessionManager.logout();
    }

    /**
     * 查询当前登录用户信息，供前端刷新页面后恢复用户状态。
     */
    @Override
    public LoginUser currentUser() {
        Long userId = authSessionManager.currentUserId();

        SysUser user = sysUserMapper.findById(userId)
                .filter(item -> !item.isDeleted())
                .orElseThrow(() -> new BusinessException(AUTH_ERROR_CODE, "登录已失效"));
        return toLoginUser(user);
    }

    /**
     * 查询当前登录用户的权限编码列表。
     */
    @Override
    public List<String> currentPermissions() {
        return userPermissionService.findPermissionCodes(authSessionManager.currentUserId());
    }

    /**
     * 写入登录日志；审计失败不影响登录主流程。
     */
    private void recordLogin(LoginCommand command, Long userId, String username, String status, String message) {
        try {
            auditLogService.recordLogin(new LoginAuditCommand(
                    userId,
                    username,
                    command == null ? null : command.loginIp(),
                    command == null ? null : command.userAgent(),
                    status,
                    message
            ));
        } catch (RuntimeException exception) {
            log.warn("记录登录日志失败，username={}", username, exception);
        }
    }

    /**
     * 组装登录响应，登录成功时一次性返回 token、用户、角色和权限。
     */
    private LoginSession buildSession(SysUser user, TokenInfo token) {
        Long userId = user.id();
        return new LoginSession(
                token.tokenName(),
                token.tokenValue(),
                toLoginUser(user),
                userPermissionService.findRoleCodes(userId),
                userPermissionService.findPermissionCodes(userId)
        );
    }

    /**
     * 转换为前端可见的用户信息，不返回密码、状态等敏感或后台字段。
     */
    private LoginUser toLoginUser(SysUser user) {
        return new LoginUser(user.id(), user.username(), user.nickname());
    }

    /**
     * 生成统一的账号密码错误异常，避免外部判断账号是否存在。
     */
    private static BusinessException invalidCredentials() {
        return new BusinessException(AUTH_ERROR_CODE, "用户名或密码错误");
    }

    /**
     * 判断登录入参是否为空白字符串，兼容 null、空字符串和纯空格。
     */
    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
