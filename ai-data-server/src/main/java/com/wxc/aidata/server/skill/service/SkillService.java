package com.wxc.aidata.server.skill.service;

import com.wxc.aidata.common.response.PageResult;
import com.wxc.aidata.server.business.response.BusinessApiTestResponse;
import com.wxc.aidata.server.permission.model.ResourceAccessScope;
import com.wxc.aidata.server.skill.model.SkillCreateCommand;
import com.wxc.aidata.server.skill.model.SkillPageQuery;
import com.wxc.aidata.server.skill.model.SkillTestCommand;
import com.wxc.aidata.server.skill.model.SkillUpdateCommand;
import com.wxc.aidata.server.skill.response.SkillResponse;

/**
 * Skill 管理服务，封装 Skill 配置、参数映射和在线测试规则。
 */
public interface SkillService {

    /**
     * 分页查询 Skill 列表。
     */
    PageResult<SkillResponse> pageSkills(SkillPageQuery query);

    /**
     * 查询 Skill 详情和参数映射。
     */
    SkillResponse getSkill(Long id);

    /**
     * 创建 Skill 配置。
     */
    void createSkill(SkillCreateCommand command);

    /**
     * 更新 Skill 配置和参数映射。
     */
    void updateSkill(SkillUpdateCommand command);

    /**
     * 启用或禁用 Skill。
     */
    void updateStatus(Long id, Integer status);

    /**
     * 删除 Skill 及其参数映射。
     */
    void deleteSkill(Long id);

    /**
     * 按 Skill 参数映射发起一次在线测试。
     */
    BusinessApiTestResponse testSkill(Long id, SkillTestCommand command);

    /**
     * 使用显式权限快照执行 Skill，供 Agent 等非 Web 调用方使用。
     */
    BusinessApiTestResponse testSkill(Long id, SkillTestCommand command, ResourceAccessScope accessScope);
}
