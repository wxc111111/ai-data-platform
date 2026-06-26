package com.wxc.aidata.server.business.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 业务接口响应，返回接口基础配置和参数定义。
 */
public record BusinessApiResponse(
        Long id,
        Long systemId,
        String systemName,
        String systemBaseUrl,
        String fullRequestUrl,
        String apiCode,
        String apiName,
        String requestPath,
        String requestMethod,
        String contentType,
        Integer connectTimeout,
        Integer readTimeout,
        String responseDataPath,
        Integer status,
        String description,
        LocalDateTime createdTime,
        LocalDateTime updatedTime,
        List<BusinessApiParameterResponse> parameters
) {
}
