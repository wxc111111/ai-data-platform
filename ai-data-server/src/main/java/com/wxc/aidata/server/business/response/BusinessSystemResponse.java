package com.wxc.aidata.server.business.response;

import java.time.LocalDateTime;
import java.util.List;

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
        Long createdBy,
        LocalDateTime createdTime,
        Long updatedBy,
        LocalDateTime updatedTime,
        List<Long> roleIds
) {
    public BusinessSystemResponse(Long id, String systemCode, String systemName, String baseUrl, String authType,
                                  String authConfig, Integer connectTimeout, Integer readTimeout, Integer status,
                                  String description, LocalDateTime createdTime, LocalDateTime updatedTime) {
        this(id, systemCode, systemName, baseUrl, authType, authConfig, connectTimeout, readTimeout, status,
                description, null, createdTime, null, updatedTime, List.of());
    }
}
