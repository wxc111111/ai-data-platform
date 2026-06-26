package com.wxc.aidata.server.permission.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.wxc.aidata.common.response.Result;
import com.wxc.aidata.server.permission.model.PermissionCreateCommand;
import com.wxc.aidata.server.permission.model.PermissionUpdateCommand;
import com.wxc.aidata.server.permission.request.PermissionCreateRequest;
import com.wxc.aidata.server.permission.request.PermissionStatusRequest;
import com.wxc.aidata.server.permission.request.PermissionUpdateRequest;
import com.wxc.aidata.server.permission.response.PermissionTreeResponse;
import com.wxc.aidata.server.permission.service.PermissionManageService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 权限管理接口，提供菜单、按钮和接口权限维护能力。
 */
@RestController
@RequestMapping("/api/permissions")
public class PermissionManageController {

    private final PermissionManageService permissionManageService;

    /**
     * 注入权限管理服务。
     */
    public PermissionManageController(PermissionManageService permissionManageService) {
        this.permissionManageService = permissionManageService;
    }

    /**
     * 查询权限树。
     */
    @SaCheckPermission("system:permission:list")
    @GetMapping("/tree")
    public Result<List<PermissionTreeResponse>> treePermissions() {
        return Result.success(permissionManageService.treePermissions());
    }

    /**
     * 查询权限详情。
     */
    @SaCheckPermission("system:permission:list")
    @GetMapping("/{id}")
    public Result<PermissionTreeResponse> getPermission(@PathVariable("id") Long id) {
        return Result.success(permissionManageService.getPermission(id));
    }

    /**
     * 创建权限。
     */
    @SaCheckPermission("system:permission:add")
    @PostMapping
    public Result<Void> createPermission(@RequestBody PermissionCreateRequest request) {
        permissionManageService.createPermission(toCreateCommand(request));
        return Result.success(null);
    }

    /**
     * 更新权限。
     */
    @SaCheckPermission("system:permission:update")
    @PutMapping("/{id}")
    public Result<Void> updatePermission(@PathVariable("id") Long id, @RequestBody PermissionUpdateRequest request) {
        permissionManageService.updatePermission(toUpdateCommand(id, request));
        return Result.success(null);
    }

    /**
     * 启用或禁用权限。
     */
    @SaCheckPermission("system:permission:update")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable("id") Long id, @RequestBody PermissionStatusRequest request) {
        permissionManageService.updateStatus(id, request.status());
        return Result.success(null);
    }

    /**
     * 删除权限。
     */
    @SaCheckPermission("system:permission:delete")
    @DeleteMapping("/{id}")
    public Result<Void> deletePermission(@PathVariable("id") Long id) {
        permissionManageService.deletePermission(id);
        return Result.success(null);
    }

    /**
     * 将创建请求转换为服务命令。
     */
    private PermissionCreateCommand toCreateCommand(PermissionCreateRequest request) {
        return new PermissionCreateCommand(
                request.parentId(),
                request.permissionName(),
                request.permissionCode(),
                request.permissionType(),
                request.routePath(),
                request.componentPath(),
                request.icon(),
                request.sortNo(),
                request.status()
        );
    }

    /**
     * 将更新请求转换为服务命令。
     */
    private PermissionUpdateCommand toUpdateCommand(Long id, PermissionUpdateRequest request) {
        return new PermissionUpdateCommand(
                id,
                request.parentId(),
                request.permissionName(),
                request.permissionCode(),
                request.permissionType(),
                request.routePath(),
                request.componentPath(),
                request.icon(),
                request.sortNo(),
                request.status()
        );
    }
}
