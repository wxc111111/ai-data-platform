package com.wxc.aidata.server.auth.service.impl;

import com.wxc.aidata.common.exception.BusinessException;
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
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 登录认证业务实现，负责账号校验、密码校验、token 创建和权限信息组装。
 */
@Service
public class AuthServiceImpl implements AuthService {

    /**
     * 登录失败统一错误码，避免把“用户不存在”和“密码错误”暴露给外部。
     */
    private static final int AUTH_ERROR_CODE = 10001;

    private final SysUserMapper sysUserMapper;
    private final UserPermissionService userPermissionService;
    private final PasswordService passwordService;
    private final AuthSessionManager authSessionManager;

    /**
     * 注入用户、权限、密码和会话组件，登录流程通过这些组件完成完整认证链路。
     */
    public AuthServiceImpl(
            SysUserMapper sysUserMapper,
            UserPermissionService userPermissionService,
            PasswordService passwordService,
            AuthSessionManager authSessionManager) {

        this.sysUserMapper = sysUserMapper;
        this.userPermissionService = userPermissionService;
        this.passwordService = passwordService;
        this.authSessionManager = authSessionManager;
    }

    /**
     * 执行登录：先校验入参，再校验账号状态和密码，最后创建 token 并返回会话信息。
     */
    @Override
    public LoginSession login(LoginCommand command) {
        // 登录参数缺失时直接返回统一错误，避免空值进入后续数据库查询。
        if (command == null || isBlank(command.username()) || isBlank(command.password())) {
            throw invalidCredentials();
        }

        // 只允许未删除用户登录；用户不存在和已删除都使用统一提示，减少账号枚举风险。
        SysUser user = sysUserMapper.findByUsername(command.username())
                .filter(item -> !item.isDeleted())
                .orElseThrow(AuthServiceImpl::invalidCredentials);

        // 被禁用用户不能登录，给出明确业务提示方便后台排查账号状态。
        if (!user.isEnabled()) {
            throw new BusinessException(AUTH_ERROR_CODE, "用户已被禁用");
        }

        // 使用 BCrypt 校验明文和密文，不直接比较字符串，保证带盐哈希能够正常验证。
        if (!passwordService.matches(command.password(), user.password())) {
            throw invalidCredentials();
        }

        // 密码通过后再创建登录会话，避免无效请求占用 token 会话资源。
        TokenInfo token = authSessionManager.login(user.id());
        // 登录成功后刷新角色和权限缓存，后续接口鉴权优先读取 Redis。
        userPermissionService.refreshUserPermissionCache(user.id());
        return buildSession(user, token);
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

        // token 有效但用户被删除时，按登录失效处理，避免前端继续展示历史账号信息。
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
     * 组装登录响应，登录成功时一次性返回 token、用户、角色和权限，减少前端初始化请求次数。
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
