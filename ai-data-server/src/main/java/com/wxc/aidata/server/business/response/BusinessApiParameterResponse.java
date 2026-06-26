package com.wxc.aidata.server.business.response;

import java.time.LocalDateTime;

/**
 * 业务接口参数响应，返回接口参数定义。
 */
public record BusinessApiParameterResponse(
        Long id,
        Long apiId,
        String parameterName,
        String parameterLocation,
        String parameterType,
        Integer required,
        String defaultValue,
        String description,
        Integer sortNo,
        LocalDateTime createdTime,
        LocalDateTime updatedTime
) {
}
