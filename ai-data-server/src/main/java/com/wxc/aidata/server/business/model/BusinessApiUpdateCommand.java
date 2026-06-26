package com.wxc.aidata.server.business.model;

import java.util.List;

/**
 * 更新业务接口命令，包含接口 ID、基础配置和参数定义。
 */
public record BusinessApiUpdateCommand(
        Long id,
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
