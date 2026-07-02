package com.wxc.aidata.server.business.model;

import java.util.List;

/**
 * 创建业务系统命令，承载外部系统基础信息和认证配置。
 */
public record BusinessSystemCreateCommand(
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
    public BusinessSystemCreateCommand(String systemCode, String systemName, String baseUrl, String authType,
                                       String authConfig, Integer connectTimeout, Integer readTimeout,
                                       Integer status, String description) {
        this(systemCode, systemName, baseUrl, authType, authConfig, connectTimeout, readTimeout, status, description, List.of());
    }
}
