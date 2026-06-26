package com.wxc.aidata.server.role.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.wxc.aidata.common.response.PageResult;
import com.wxc.aidata.common.response.Result;
import com.wxc.aidata.server.role.model.RoleCreateCommand;
import com.wxc.aidata.server.role.model.RolePageQuery;
import com.wxc.aidata.server.role.model.RolePermissionAssignCommand;
import com.wxc.aidata.server.role.model.RoleUpdateCommand;
import com.wxc.aidata.server.role.request.RoleCreateRequest;
import com.wxc.aidata.server.role.request.RolePermissionAssignRequest;
import com.wxc.aidata.server.role.request.RoleStatusRequest;
import com.wxc.aidata.server.role.request.RoleUpdateRequest;
import com.wxc.aidata.server.role.response.RoleResponse;
import com.wxc.aidata.server.role.service.RoleManageService;
import com.wxc.aidata.server.role.service.RolePermissionService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 角色管理接口，提供后台角色维护能力。
 */
@RestController
@RequestMapping("/api/roles")
public class RoleManageController {

    private final RoleManageService roleManageService;
    private final RolePermissionService rolePermissionService;

    /**
     * 注入角色管理服务和角色授权服务。
     */
    public RoleManageController(RoleManageService roleManageService, RolePermissionService rolePermissionService) {
        this.roleManageService = roleManageService;
        this.rolePermissionService = rolePermissionService;
    }

    /**
     * 分页查询角色列表。
     */
    @SaCheckPermission("system:role:list")
    @GetMapping
    public Result<PageResult<RoleResponse>> pageRoles(
            @RequestParam(name = "pageNo", required = false) Integer pageNo,
            @RequestParam(name = "pageSize", required = false) Integer pageSize,
            @RequestParam(name = "roleCode", required = false) String roleCode,
            @RequestParam(name = "status", required = false) Integer status) {
        PageResult<RoleResponse> roleResponsePageResult = roleManageService.pageRoles(new RolePageQuery(pageNo, pageSize, roleCode, status));
        System.out.println("Role response page result: " + roleResponsePageResult);
        return Result.success(roleResponsePageResult);
    }

    /**
     * 查询角色详情。
     */
    @SaCheckPermission("system:role:list")
    @GetMapping("/{id}")
    public Result<RoleResponse> getRole(@PathVariable("id") Long id) {
        return Result.success(roleManageService.getRole(id));
    }

    /**
     * 创建角色。
     */
    @SaCheckPermission("system:role:add")
    @PostMapping
    public Result<Void> createRole(@RequestBody RoleCreateRequest request) {
        roleManageService.createRole(new RoleCreateCommand(
                request.roleCode(),
                request.roleName(),
                request.status(),
                request.description()
        ));
        return Result.success(null);
    }

    /**
     * 更新角色基础信息。
     */
    @SaCheckPermission("system:role:update")
    @PutMapping("/{id}")
    public Result<Void> updateRole(@PathVariable("id") Long id, @RequestBody RoleUpdateRequest request) {
        roleManageService.updateRole(new RoleUpdateCommand(
                id,
                request.roleCode(),
                request.roleName(),
                request.status(),
                request.description()
        ));
        return Result.success(null);
    }

    /**
     * 启用或禁用角色。
     */
    @SaCheckPermission("system:role:update")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable("id") Long id, @RequestBody RoleStatusRequest request) {
        roleManageService.updateStatus(id, request.status());
        return Result.success(null);
    }

    /**
     * 删除角色。
     */
    @SaCheckPermission("system:role:delete")
    @DeleteMapping("/{id}")
    public Result<Void> deleteRole(@PathVariable("id") Long id) {
        roleManageService.deleteRole(id);
        return Result.success(null);
    }

    /**
     * 查询角色已授权权限 ID。
     */
    @SaCheckPermission("system:role:update")
    @GetMapping("/{id}/permissions")
    public Result<List<Long>> permissionIds(@PathVariable("id") Long id) {
        return Result.success(rolePermissionService.permissionIds(id));
    }

    /**
     * 覆盖保存角色授权。
     */
    @SaCheckPermission("system:role:update")
    @PutMapping("/{id}/permissions")
    public Result<Void> assignPermissions(@PathVariable("id") Long id, @RequestBody RolePermissionAssignRequest request) {
        rolePermissionService.assignPermissions(new RolePermissionAssignCommand(id, request.permissionIds()));
        return Result.success(null);
    }
}
