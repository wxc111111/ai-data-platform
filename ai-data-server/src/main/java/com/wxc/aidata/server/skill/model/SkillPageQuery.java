package com.wxc.aidata.server.skill.model;

/**
 * Skill 分页查询条件，支持按名称、编码和状态筛选。
 */
public record SkillPageQuery(Integer pageNo, Integer pageSize, String skillName, String skillCode, Integer status) {

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
