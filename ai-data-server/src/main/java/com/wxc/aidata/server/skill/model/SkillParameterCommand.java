package com.wxc.aidata.server.skill.model;

/**
 * Skill 参数保存命令，描述对外入参和业务接口参数的映射关系。
 */
public record SkillParameterCommand(
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
