package com.wxc.aidata.server.business.service;

import java.util.List;
import java.util.Map;

/**
 * 业务接口 HTTP 响应对象，承载在线测试返回内容。
 */
public record BusinessApiHttpResponse(
        Integer statusCode,
        Map<String, List<String>> headers,
        String body,
        Long costMs
) {
}
