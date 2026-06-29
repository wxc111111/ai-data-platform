package com.wxc.aidata.server.skill.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.wxc.aidata.common.response.PageResult;
import com.wxc.aidata.common.response.Result;
import com.wxc.aidata.server.business.response.BusinessApiTestResponse;
import com.wxc.aidata.server.skill.model.SkillCreateCommand;
import com.wxc.aidata.server.skill.model.SkillPageQuery;
import com.wxc.aidata.server.skill.model.SkillParameterCommand;
import com.wxc.aidata.server.skill.model.SkillTestCommand;
import com.wxc.aidata.server.skill.model.SkillUpdateCommand;
import com.wxc.aidata.server.skill.request.SkillCreateRequest;
import com.wxc.aidata.server.skill.request.SkillParameterRequest;
import com.wxc.aidata.server.skill.request.SkillStatusRequest;
import com.wxc.aidata.server.skill.request.SkillTestRequest;
import com.wxc.aidata.server.skill.request.SkillUpdateRequest;
import com.wxc.aidata.server.skill.response.SkillResponse;
import com.wxc.aidata.server.skill.service.SkillService;
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
 * Skill 管理接口，提供 Skill 配置、发布状态和在线测试能力。
 */
@RestController
@RequestMapping("/api/skills")
public class SkillController {

    private final SkillService skillService;

    /**
     * 注入 Skill 管理服务。
     */
    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    /**
     * 分页查询 Skill 列表。
     */
    @SaCheckPermission("system:skill:list")
    @GetMapping
    public Result<PageResult<SkillResponse>> pageSkills(
            @RequestParam(name = "pageNo", required = false) Integer pageNo,
            @RequestParam(name = "pageSize", required = false) Integer pageSize,
            @RequestParam(name = "skillName", required = false) String skillName,
            @RequestParam(name = "skillCode", required = false) String skillCode,
            @RequestParam(name = "status", required = false) Integer status) {

        return Result.success(skillService.pageSkills(new SkillPageQuery(pageNo, pageSize, skillName, skillCode, status)));
    }

    /**
     * 查询 Skill 详情。
     */
    @SaCheckPermission("system:skill:list")
    @GetMapping("/{id}")
    public Result<SkillResponse> getSkill(@PathVariable("id") Long id) {
        return Result.success(skillService.getSkill(id));
    }

    /**
     * 创建 Skill。
     */
    @SaCheckPermission("system:skill:add")
    @PostMapping
    public Result<Void> createSkill(@RequestBody SkillCreateRequest request) {
        skillService.createSkill(toCreateCommand(request));
        return Result.success(null);
    }

    /**
     * 更新 Skill。
     */
    @SaCheckPermission("system:skill:update")
    @PutMapping("/{id}")
    public Result<Void> updateSkill(@PathVariable("id") Long id, @RequestBody SkillUpdateRequest request) {
        skillService.updateSkill(toUpdateCommand(id, request));
        return Result.success(null);
    }

    /**
     * 启用或禁用 Skill。
     */
    @SaCheckPermission("system:skill:update")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable("id") Long id, @RequestBody SkillStatusRequest request) {
        skillService.updateStatus(id, request.status());
        return Result.success(null);
    }

    /**
     * 删除 Skill。
     */
    @SaCheckPermission("system:skill:delete")
    @DeleteMapping("/{id}")
    public Result<Void> deleteSkill(@PathVariable("id") Long id) {
        skillService.deleteSkill(id);
        return Result.success(null);
    }

    /**
     * 使用 Skill 入参发起在线测试。
     */
    @SaCheckPermission("system:skill:test")
    @PostMapping("/{id}/test")
    public Result<BusinessApiTestResponse> testSkill(@PathVariable("id") Long id, @RequestBody SkillTestRequest request) {
        return Result.success(skillService.testSkill(id, new SkillTestCommand(request.parameterValues())));
    }

    /**
     * 将新增请求转换为服务层命令。
     */
    private SkillCreateCommand toCreateCommand(SkillCreateRequest request) {
        return new SkillCreateCommand(request.skillCode(), request.skillName(), request.description(), request.apiId(),
                request.permissionCode(), request.timeoutMs(), request.maxResultCount(), request.status(),
                toParameterCommands(request.parameters()));
    }

    /**
     * 将更新请求转换为服务层命令。
     */
    private SkillUpdateCommand toUpdateCommand(Long id, SkillUpdateRequest request) {
        return new SkillUpdateCommand(id, request.skillCode(), request.skillName(), request.description(), request.apiId(),
                request.permissionCode(), request.timeoutMs(), request.maxResultCount(), request.status(),
                toParameterCommands(request.parameters()));
    }

    /**
     * 将参数请求转换为服务层命令。
     */
    private List<SkillParameterCommand> toParameterCommands(List<SkillParameterRequest> parameters) {
        if (parameters == null) {
            return List.of();
        }
        return parameters.stream()
                .map(parameter -> new SkillParameterCommand(
                        parameter.id(),
                        parameter.parameterName(),
                        parameter.parameterType(),
                        parameter.required(),
                        parameter.description(),
                        parameter.apiParameterName(),
                        parameter.defaultValue(),
                        parameter.valueSource(),
                        parameter.sortNo()
                ))
                .toList();
    }
}
