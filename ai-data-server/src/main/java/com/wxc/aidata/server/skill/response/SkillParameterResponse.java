package com.wxc.aidata.server.skill.response;

import java.time.LocalDateTime;

/**
 * Skill 参数响应，返回 Skill 入参及其业务接口参数映射。
 */
public record SkillParameterResponse(
        Long id,
        Long skillId,
        String parameterName,
        String parameterType,
        Integer required,
        String description,
        String apiParameterName,
        String defaultValue,
        String valueSource,
        Integer sortNo,
        LocalDateTime createdTime,
        LocalDateTime updatedTime
) {
}
