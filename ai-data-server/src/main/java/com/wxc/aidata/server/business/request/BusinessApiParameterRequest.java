package com.wxc.aidata.server.business.request;

/**
 * 业务接口参数请求，承载前端提交的参数定义。
 */
public record BusinessApiParameterRequest(
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
