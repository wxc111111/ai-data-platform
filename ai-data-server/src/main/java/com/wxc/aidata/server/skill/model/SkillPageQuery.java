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
        Long currentUserId,
        Boolean admin) {

    public SkillPageQuery(Integer pageNo, Integer pageSize, String skillName, String skillCode, Integer status) {
        this(pageNo, pageSize, skillName, skillCode, status, List.of(), null, false);
    }

    /**
     * 补充当前用户范围，普通用户可见公共 Skill、自己创建的私有 Skill 和命中角色范围的私有 Skill。
     */
    public SkillPageQuery withAccessScope(Long currentUserId, List<Long> roleIds, boolean admin) {
        return new SkillPageQuery(pageNo, pageSize, skillName, skillCode, status, roleIds == null ? List.of() : roleIds, currentUserId, admin);
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
