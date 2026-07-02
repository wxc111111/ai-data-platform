package com.wxc.aidata.server.business.request;

import java.util.List;

/**
 * 更新业务系统请求体，对应前端编辑业务系统表单。
 */
public record BusinessSystemUpdateRequest(
        String systemCode,
        String systemName,
        String baseUrl,
        String authType,
        String authConfig,
        Integer connectTimeout,
        Integer readTimeout,
        Integer status,
        String description,
        List<Long> roleIds
) {
}
