package com.wxc.aidata.server.skill.request;

import java.util.List;

/**
 * 新增 Skill 请求体。
 */
public record SkillCreateRequest(
        String skillCode,
        String skillName,
        String description,
        Long apiId,
        String permissionCode,
        Integer timeoutMs,
        Integer maxResultCount,
        Integer status,
        List<SkillParameterRequest> parameters
) {
}
