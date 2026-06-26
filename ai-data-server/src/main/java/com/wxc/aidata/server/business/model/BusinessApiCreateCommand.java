package com.wxc.aidata.server.business.model;

import java.util.List;

/**
 * 新增业务接口命令，包含接口基础配置和参数定义。
 */
public record BusinessApiCreateCommand(
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
        List<BusinessApiParameterCommand> parameters
) {
}
