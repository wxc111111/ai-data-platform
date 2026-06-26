package com.wxc.aidata.server.business.response;

import java.util.List;
import java.util.Map;

/**
 * 业务接口在线测试响应，返回请求结果和耗时。
 */
public record BusinessApiTestResponse(
        Integer statusCode,
        Map<String, List<String>> headers,
        String body,
        Object extractedData,
        Long costMs
) {
}
