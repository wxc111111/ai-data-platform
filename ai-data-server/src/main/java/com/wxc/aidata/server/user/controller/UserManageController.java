package com.wxc.aidata.server.user.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.wxc.aidata.common.response.PageResult;
import com.wxc.aidata.common.response.Result;
import com.wxc.aidata.server.user.model.UserCreateCommand;
import com.wxc.aidata.server.user.model.UserPageQuery;
import com.wxc.aidata.server.user.model.UserRoleAssignCommand;
import com.wxc.aidata.server.user.model.UserUpdateCommand;
import com.wxc.aidata.server.user.request.UserCreateRequest;
import com.wxc.aidata.server.user.request.UserRoleAssignRequest;
import com.wxc.aidata.server.user.request.UserStatusRequest;
import com.wxc.aidata.server.user.request.UserUpdateRequest;
import com.wxc.aidata.server.user.response.UserResponse;
import com.wxc.aidata.server.user.service.UserManageService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户管理接口，提供后台账号维护能力。
 */
@RestController
@RequestMapping("/api/users")
public class UserManageController {

    private final UserManageService userManageService;

    /**
     * 注入用户管理服务。
     */
    public UserManageController(UserManageService userManageService) {
        this.userManageService = userManageService;
    }

    /**
     * 分页查询用户列表。
     */
    @SaCheckPermission("system:user:list")
    @GetMapping
    public Result<PageResult<UserResponse>> pageUsers(
            @RequestParam(name = "pageNo", required = false) Integer pageNo,
            @RequestParam(name = "pageSize", required = false) Integer pageSize,
            @RequestParam(name = "username", required = false) String username,
            @RequestParam(name = "mobile", required = false) String mobile,
            @RequestParam(name = "status", required = false) Integer status) {

        return Result.success(userManageService.pageUsers(new UserPageQuery(pageNo, pageSize, username, mobile, status)));
    }

    /**
     * 查询单个用户详情。
     */
    @SaCheckPermission("system:user:list")
    @GetMapping("/{id}")
    public Result<UserResponse> getUser(@PathVariable("id") Long id) {
        return Result.success(userManageService.getUser(id));
    }

    /**
     * 创建用户。
     */
    @SaCheckPermission("system:user:add")
    @PostMapping
    public Result<Void> createUser(@RequestBody UserCreateRequest request) {
        userManageService.createUser(new UserCreateCommand(
                request.username(),
                request.password(),
                request.nickname(),
                request.mobile(),
                request.email(),
                request.status(),
                request.roleIds()
        ));
        return Result.success(null);
    }

    /**
     * 更新用户基础信息。
     */
    @SaCheckPermission("system:user:update")
    @PutMapping("/{id}")
    public Result<Void> updateUser(@PathVariable("id") Long id, @RequestBody UserUpdateRequest request) {
        userManageService.updateUser(new UserUpdateCommand(
                id,
                request.nickname(),
                request.mobile(),
                request.email(),
                request.status()
        ));
        return Result.success(null);
    }

    /**
     * 启用或禁用用户。
     */
    @SaCheckPermission("system:user:update")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable("id") Long id, @RequestBody UserStatusRequest request) {
        userManageService.updateStatus(id, request.status());
        return Result.success(null);
    }

    /**
     * 逻辑删除用户。
     */
    @SaCheckPermission("system:user:delete")
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable("id") Long id) {
        userManageService.deleteUser(id);
        return Result.success(null);
    }

    /**
     * 覆盖分配用户角色。
     */
    @SaCheckPermission("system:user:update")
    @PutMapping("/{id}/roles")
    public Result<Void> assignRoles(@PathVariable("id") Long id, @RequestBody UserRoleAssignRequest request) {
        userManageService.assignRoles(new UserRoleAssignCommand(id, request.roleIds()));
        return Result.success(null);
    }
}
