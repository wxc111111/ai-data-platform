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
        String visibility,
        Integer timeoutMs,
        Integer maxResultCount,
        Integer status,
        Integer versionNo,
        Long createdBy,
        LocalDateTime createdTime,
        Long updatedBy,
        LocalDateTime updatedTime,
        List<Long> roleIds,
        List<SkillParameterResponse> parameters
) {
    public SkillResponse(Long id, String skillCode, String skillName, String description, Long apiId,
                         String apiName, String apiCode, String permissionCode, Integer timeoutMs,
                         Integer maxResultCount, Integer status, Integer versionNo,
                         LocalDateTime createdTime, LocalDateTime updatedTime,
                         List<SkillParameterResponse> parameters) {
        this(id, skillCode, skillName, description, apiId, apiName, apiCode, permissionCode, "PRIVATE", timeoutMs,
                maxResultCount, status, versionNo, null, createdTime, null, updatedTime, List.of(), parameters);
    }
}
