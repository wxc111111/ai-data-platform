package com.wxc.aidata.server.business.request;

import java.util.List;

/**
 * 更新业务接口请求，包含接口基础字段和参数定义。
 */
public record BusinessApiUpdateRequest(
        Long systemId,
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
        List<Long> roleIds,
        List<BusinessApiParameterRequest> parameters
) {
}
