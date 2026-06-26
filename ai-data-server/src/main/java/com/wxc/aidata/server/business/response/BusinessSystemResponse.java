package com.wxc.aidata.server.business.response;

import java.time.LocalDateTime;

/**
 * 业务系统响应对象，用于列表和详情展示。
 */
public record BusinessSystemResponse(
        Long id,
        String systemCode,
        String systemName,
        String baseUrl,
        String authType,
        String authConfig,
        Integer connectTimeout,
        Integer readTimeout,
        Integer status,
        String description,
        LocalDateTime createdTime,
        LocalDateTime updatedTime
) {
}
