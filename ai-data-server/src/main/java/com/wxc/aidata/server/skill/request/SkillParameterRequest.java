package com.wxc.aidata.server.skill.request;

/**
 * Skill 参数请求体，前端用它维护参数映射。
 */
public record SkillParameterRequest(
        Long id,
        String parameterName,
        String parameterType,
        Integer required,
        String description,
        String apiParameterName,
        String defaultValue,
        String valueSource,
        Integer sortNo
) {
}
