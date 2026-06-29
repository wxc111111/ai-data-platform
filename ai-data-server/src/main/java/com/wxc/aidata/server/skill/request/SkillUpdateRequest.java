package com.wxc.aidata.server.skill.request;

import java.util.List;

/**
 * 更新 Skill 请求体。
 */
public record SkillUpdateRequest(
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
