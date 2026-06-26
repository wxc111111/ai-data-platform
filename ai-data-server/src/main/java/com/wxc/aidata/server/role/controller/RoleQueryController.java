package com.wxc.aidata.server.role.controller;

import com.wxc.aidata.common.response.Result;
import com.wxc.aidata.server.role.response.RoleOptionResponse;
import com.wxc.aidata.server.role.service.RoleQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 角色查询接口，服务用户管理页面的角色选择。
 */
@RestController
@RequestMapping("/api/roles")
public class RoleQueryController {

    private final RoleQueryService roleQueryService;

    /**
     * 注入角色查询服务。
     */
    public RoleQueryController(RoleQueryService roleQueryService) {
        this.roleQueryService = roleQueryService;
    }

    /**
     * 查询启用角色选项。
     */
    @GetMapping("/options")
    public Result<List<RoleOptionResponse>> enabledRoleOptions() {
        return Result.success(roleQueryService.enabledRoleOptions());
    }
}
