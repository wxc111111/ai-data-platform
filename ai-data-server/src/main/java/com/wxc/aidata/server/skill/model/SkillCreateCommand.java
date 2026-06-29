package com.wxc.aidata.server.skill.model;

import java.util.List;

/**
 * 新增 Skill 命令，包含基础配置和参数映射。
 */
public record SkillCreateCommand(
        String skillCode,
        String skillName,
        String description,
        Long apiId,
        String permissionCode,
        Integer timeoutMs,
        Integer maxResultCount,
        Integer status,
        List<SkillParameterCommand> parameters
) {
}
