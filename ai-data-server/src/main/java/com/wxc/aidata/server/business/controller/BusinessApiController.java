package com.wxc.aidata.server.business.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.wxc.aidata.common.response.PageResult;
import com.wxc.aidata.common.response.Result;
import com.wxc.aidata.server.business.model.BusinessApiCreateCommand;
import com.wxc.aidata.server.business.model.BusinessApiPageQuery;
import com.wxc.aidata.server.business.model.BusinessApiParameterCommand;
import com.wxc.aidata.server.business.model.BusinessApiTestCommand;
import com.wxc.aidata.server.business.model.BusinessApiUpdateCommand;
import com.wxc.aidata.server.business.request.BusinessApiCreateRequest;
import com.wxc.aidata.server.business.request.BusinessApiParameterRequest;
import com.wxc.aidata.server.business.request.BusinessApiStatusRequest;
import com.wxc.aidata.server.business.request.BusinessApiTestRequest;
import com.wxc.aidata.server.business.request.BusinessApiUpdateRequest;
import com.wxc.aidata.server.business.response.BusinessApiResponse;
import com.wxc.aidata.server.business.response.BusinessApiTestResponse;
import com.wxc.aidata.server.business.service.BusinessApiService;
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
 * 业务接口管理接口，提供第三方接口配置、参数定义和在线测试能力。
 */
@RestController
@RequestMapping("/api/business-apis")
public class BusinessApiController {

    private final BusinessApiService businessApiService;

    /**
     * 注入业务接口管理服务。
     */
    public BusinessApiController(BusinessApiService businessApiService) {
        this.businessApiService = businessApiService;
    }

    /**
     * 分页查询业务接口列表。
     */
    @SaCheckPermission("system:business-api:list")
    @GetMapping
    public Result<PageResult<BusinessApiResponse>> pageBusinessApis(
            @RequestParam(name = "pageNo", required = false) Integer pageNo,
            @RequestParam(name = "pageSize", required = false) Integer pageSize,
            @RequestParam(name = "systemId", required = false) Long systemId,
            @RequestParam(name = "apiName", required = false) String apiName,
            @RequestParam(name = "apiCode", required = false) String apiCode,
            @RequestParam(name = "status", required = false) Integer status) {

        return Result.success(businessApiService.pageBusinessApis(
                new BusinessApiPageQuery(pageNo, pageSize, systemId, apiName, apiCode, status)
        ));
    }

    /**
     * 查询业务接口详情。
     */
    @SaCheckPermission("system:business-api:list")
    @GetMapping("/{id}")
    public Result<BusinessApiResponse> getBusinessApi(@PathVariable("id") Long id) {
        return Result.success(businessApiService.getBusinessApi(id));
    }

    /**
     * 创建业务接口。
     */
    @SaCheckPermission("system:business-api:add")
    @PostMapping
    public Result<Void> createBusinessApi(@RequestBody BusinessApiCreateRequest request) {
        businessApiService.createBusinessApi(toCreateCommand(request));
        return Result.success(null);
    }

    /**
     * 更新业务接口。
     */
    @SaCheckPermission("system:business-api:update")
    @PutMapping("/{id}")
    public Result<Void> updateBusinessApi(@PathVariable("id") Long id, @RequestBody BusinessApiUpdateRequest request) {
        businessApiService.updateBusinessApi(toUpdateCommand(id, request));
        return Result.success(null);
    }

    /**
     * 启用或禁用业务接口。
     */
    @SaCheckPermission("system:business-api:update")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable("id") Long id, @RequestBody BusinessApiStatusRequest request) {
        businessApiService.updateStatus(id, request.status());
        return Result.success(null);
    }

    /**
     * 删除业务接口。
     */
    @SaCheckPermission("system:business-api:delete")
    @DeleteMapping("/{id}")
    public Result<Void> deleteBusinessApi(@PathVariable("id") Long id) {
        businessApiService.deleteBusinessApi(id);
        return Result.success(null);
    }

    /**
     * 按当前接口配置发起在线测试。
     */
    @SaCheckPermission("system:business-api:test")
    @PostMapping("/{id}/test")
    public Result<BusinessApiTestResponse> testBusinessApi(@PathVariable("id") Long id, @RequestBody BusinessApiTestRequest request) {
        return Result.success(businessApiService.testBusinessApi(id, new BusinessApiTestCommand(request.parameterValues())));
    }

    /**
     * 将新增请求转换为服务命令。
     */
    private BusinessApiCreateCommand toCreateCommand(BusinessApiCreateRequest request) {
        return new BusinessApiCreateCommand(
                request.systemId(),
                request.apiCode(),
                request.apiName(),
                request.requestPath(),
                request.requestMethod(),
                request.contentType(),
                request.connectTimeout(),
                request.readTimeout(),
                request.responseDataPath(),
                request.status(),
                request.description(),
                request.roleIds(),
                toParameterCommands(request.parameters())
        );
    }

    /**
     * 将更新请求转换为服务命令。
     */
    private BusinessApiUpdateCommand toUpdateCommand(Long id, BusinessApiUpdateRequest request) {
        return new BusinessApiUpdateCommand(
                id,
                request.systemId(),
                request.apiCode(),
                request.apiName(),
                request.requestPath(),
                request.requestMethod(),
                request.contentType(),
                request.connectTimeout(),
                request.readTimeout(),
                request.responseDataPath(),
                request.status(),
                request.description(),
                request.roleIds(),
                toParameterCommands(request.parameters())
        );
    }

    /**
     * 将参数请求转换为服务命令。
     */
    private List<BusinessApiParameterCommand> toParameterCommands(List<BusinessApiParameterRequest> parameters) {
        if (parameters == null) {
            return List.of();
        }
        return parameters.stream()
                .map(parameter -> new BusinessApiParameterCommand(
                        parameter.id(),
                        parameter.parameterName(),
                        parameter.parameterLocation(),
                        parameter.parameterType(),
                        parameter.required(),
                        parameter.defaultValue(),
                        parameter.description(),
                        parameter.sortNo()
                ))
                .toList();
    }
}
