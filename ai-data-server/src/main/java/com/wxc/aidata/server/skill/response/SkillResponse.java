package com.wxc.aidata.server.skill.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Skill 响应，返回 Skill 基础配置、关联业务接口和参数映射。
 */
public record SkillResponse(
        Long id,
        String skillCode,
        String skillName,
        String description,
        Long apiId,
        String apiName,
        String apiCode,
        String permissionCode,
        Integer timeoutMs,
        Integer maxResultCount,
        Integer status,
        Integer versionNo,
        LocalDateTime createdTime,
        LocalDateTime updatedTime,
        List<SkillParameterResponse> parameters
) {
}
