package com.wxc.aidata.server.auth.controller;

import com.wxc.aidata.common.response.Result;
import com.wxc.aidata.server.audit.web.ClientIpResolver;
import com.wxc.aidata.server.auth.model.LoginCommand;
import com.wxc.aidata.server.auth.model.LoginSession;
import com.wxc.aidata.server.auth.model.LoginUser;
import com.wxc.aidata.server.auth.request.LoginRequest;
import com.wxc.aidata.server.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 登录认证控制器，负责接收认证相关 HTTP 请求并包装统一响应。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * 注入登录认证服务。
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 用户登录接口，校验用户名密码，成功后返回 token、用户信息、角色和权限。
     */
    @PostMapping("/login")
    public Result<LoginSession> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return Result.success(authService.login(new LoginCommand(
                request.username(),
                request.password(),
                ClientIpResolver.resolve(httpRequest),
                httpRequest.getHeader("User-Agent")
        )));
    }

    /**
     * 用户退出接口，清理当前登录会话，让当前 token 失效。
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.success(null);
    }

    /**
     * 当前用户接口，根据 token 获取已登录用户的基础信息。
     */
    @GetMapping("/current-user")
    public Result<LoginUser> currentUser() {
        return Result.success(authService.currentUser());
    }

    /**
     * 当前权限接口，返回当前登录用户拥有的权限编码。
     */
    @GetMapping("/permissions")
    public Result<List<String>> permissions() {
        return Result.success(authService.currentPermissions());
    }
}
