package com.wxc.aidata.server.business.request;

import java.util.List;

/**
 * 创建业务系统请求体，对应前端新增业务系统表单。
 */
public record BusinessSystemCreateRequest(
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
