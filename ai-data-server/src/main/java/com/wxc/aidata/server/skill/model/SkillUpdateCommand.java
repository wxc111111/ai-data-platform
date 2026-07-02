package com.wxc.aidata.server.skill.model;

import java.util.List;

/**
 * 更新 Skill 命令，参数映射采用整体覆盖保存。
 */
public record SkillUpdateCommand(
        Long id,
        String skillCode,
        String skillName,
        String description,
        Long apiId,
        String permissionCode,
        String visibility,
        Integer timeoutMs,
        Integer maxResultCount,
        Integer status,
        List<Long> roleIds,
        List<SkillParameterCommand> parameters
) {
    public SkillUpdateCommand(Long id, String skillCode, String skillName, String description, Long apiId,
                              String permissionCode, Integer timeoutMs, Integer maxResultCount,
                              Integer status, List<SkillParameterCommand> parameters) {
        this(id, skillCode, skillName, description, apiId, permissionCode, "PRIVATE", timeoutMs, maxResultCount, status, List.of(), parameters);
    }
}
