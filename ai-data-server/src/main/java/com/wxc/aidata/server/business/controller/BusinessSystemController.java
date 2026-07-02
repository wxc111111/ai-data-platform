package com.wxc.aidata.server.business.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.wxc.aidata.common.response.PageResult;
import com.wxc.aidata.common.response.Result;
import com.wxc.aidata.server.business.model.BusinessSystemCreateCommand;
import com.wxc.aidata.server.business.model.BusinessSystemPageQuery;
import com.wxc.aidata.server.business.model.BusinessSystemUpdateCommand;
import com.wxc.aidata.server.business.request.BusinessSystemCreateRequest;
import com.wxc.aidata.server.business.request.BusinessSystemStatusRequest;
import com.wxc.aidata.server.business.request.BusinessSystemUpdateRequest;
import com.wxc.aidata.server.business.response.BusinessSystemResponse;
import com.wxc.aidata.server.business.service.BusinessSystemService;
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
 * 业务系统管理接口，提供外部业务系统基础信息和认证配置维护能力。
 */
@RestController
@RequestMapping("/api/business-systems")
public class BusinessSystemController {

    private final BusinessSystemService businessSystemService;

    /**
     * 注入业务系统管理服务。
     */
    public BusinessSystemController(BusinessSystemService businessSystemService) {
        this.businessSystemService = businessSystemService;
    }

    /**
     * 分页查询业务系统列表。
     */
    @SaCheckPermission("system:business-system:list")
    @GetMapping
    public Result<PageResult<BusinessSystemResponse>> pageBusinessSystems(
            @RequestParam(name = "pageNo", required = false) Integer pageNo,
            @RequestParam(name = "pageSize", required = false) Integer pageSize,
            @RequestParam(name = "systemName", required = false) String systemName,
            @RequestParam(name = "systemCode", required = false) String systemCode,
            @RequestParam(name = "status", required = false) Integer status) {

        return Result.success(businessSystemService.pageBusinessSystems(
                new BusinessSystemPageQuery(pageNo, pageSize, systemName, systemCode, status)
        ));
    }

    /**
     * 查询业务系统详情。
     */
    @SaCheckPermission("system:business-system:list")
    @GetMapping("/{id}")
    public Result<BusinessSystemResponse> getBusinessSystem(@PathVariable("id") Long id) {
        return Result.success(businessSystemService.getBusinessSystem(id));
    }

    /**
     * 创建业务系统。
     */
    @SaCheckPermission("system:business-system:add")
    @PostMapping
    public Result<Void> createBusinessSystem(@RequestBody BusinessSystemCreateRequest request) {
        businessSystemService.createBusinessSystem(new BusinessSystemCreateCommand(
                request.systemCode(),
                request.systemName(),
                request.baseUrl(),
                request.authType(),
                request.authConfig(),
                request.connectTimeout(),
                request.readTimeout(),
                request.status(),
                request.description(),
                request.roleIds()
        ));
        return Result.success(null);
    }

    /**
     * 更新业务系统基础信息和认证配置。
     */
    @SaCheckPermission("system:business-system:update")
    @PutMapping("/{id}")
    public Result<Void> updateBusinessSystem(
            @PathVariable("id") Long id,
            @RequestBody BusinessSystemUpdateRequest request) {

        businessSystemService.updateBusinessSystem(new BusinessSystemUpdateCommand(
                id,
                request.systemCode(),
                request.systemName(),
                request.baseUrl(),
                request.authType(),
                request.authConfig(),
                request.connectTimeout(),
                request.readTimeout(),
                request.status(),
                request.description(),
                request.roleIds()
        ));
        return Result.success(null);
    }

    /**
     * 启用或禁用业务系统。
     */
    @SaCheckPermission("system:business-system:update")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable("id") Long id, @RequestBody BusinessSystemStatusRequest request) {
        businessSystemService.updateStatus(id, request.status());
        return Result.success(null);
    }

    /**
     * 删除业务系统。
     */
    @SaCheckPermission("system:business-system:delete")
    @DeleteMapping("/{id}")
    public Result<Void> deleteBusinessSystem(@PathVariable("id") Long id) {
        businessSystemService.deleteBusinessSystem(id);
        return Result.success(null);
    }
}
