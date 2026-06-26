package com.wxc.aidata.server.business.model;

/**
 * 业务接口参数命令，封装接口参数定义的可编辑字段。
 */
public record BusinessApiParameterCommand(
        Long id,
        String parameterName,
        String parameterLocation,
        String parameterType,
        Integer required,
        String defaultValue,
        String description,
        Integer sortNo
) {
}
