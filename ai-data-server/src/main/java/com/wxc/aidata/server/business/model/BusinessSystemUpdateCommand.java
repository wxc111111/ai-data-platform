package com.wxc.aidata.server.business.model;

import java.util.List;

/**
 * 更新业务系统命令，包含业务系统主键和可编辑字段。
 */
public record BusinessSystemUpdateCommand(
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
        List<Long> roleIds
) {
    public BusinessSystemUpdateCommand(Long id, String systemCode, String systemName, String baseUrl, String authType,
                                       String authConfig, Integer connectTimeout, Integer readTimeout,
                                       Integer status, String description) {
        this(id, systemCode, systemName, baseUrl, authType, authConfig, connectTimeout, readTimeout, status, description, List.of());
    }
}
