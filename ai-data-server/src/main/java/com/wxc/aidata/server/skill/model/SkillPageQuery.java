package com.wxc.aidata.server.skill.model;

import java.util.List;

/**
 * Skill 分页查询条件，支持按名称、编码、状态和角色范围筛选。
 */
public record SkillPageQuery(
        Integer pageNo,
        Integer pageSize,
        String skillName,
        String skillCode,
        Integer status,
        List<Long> roleIds,
        Boolean admin) {

    public SkillPageQuery(Integer pageNo, Integer pageSize, String skillName, String skillCode, Integer status) {
        this(pageNo, pageSize, skillName, skillCode, status, List.of(), false);
    }

    /**
     * 补充当前用户角色范围，普通用户只能看到命中角色范围的 Skill。
     */
    public SkillPageQuery withAccessScope(List<Long> roleIds, boolean admin) {
        return new SkillPageQuery(pageNo, pageSize, skillName, skillCode, status, roleIds == null ? List.of() : roleIds, admin);
    }

    public int normalizedPageNo() {
        return pageNo == null || pageNo < 1 ? 1 : pageNo;
    }

    public int normalizedPageSize() {
        if (pageSize == null || pageSize < 1) {
            return 10;
        }
        return Math.min(pageSize, 100);
    }
}
